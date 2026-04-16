# Migraciones de base de datos (Flyway)

Este directorio contiene los scripts SQL que Flyway aplica automáticamente al arrancar
el app. Cada cambio de schema debe quedar versionado aquí.

## Estructura

- `common/` — migraciones sobre `admin_tools` (datos comunes: clientes, artículos,
  usuarios, configuración, catálogo de cajas, etc.).
- `caja/` — migraciones sobre cada `admin_tools_caja_N` (facturación por caja:
  `encabezado_factura`, `detalle_factura`, cierres, etc.). Se ejecutan una vez por
  caja listada en `admin_tools.cajas.nombre_db`.

## Convención de nombres

`V{numero}__{descripcion_snake_case}.sql`

Reglas:
- Dos guiones bajos (`__`) separan versión de descripción.
- Secuencia independiente por carpeta.
- **Nunca editar** una migración ya aplicada en producción: Flyway valida checksum y
  fallará. Para corregir algo, agregar una nueva versión.

## Escenarios que Flyway cubre

### Escenario A — Base existente con datos (clientes en producción)

Al arrancar el app por primera vez con Flyway activado:

1. Flyway ve que la base tiene tablas pero no `schema_version`.
2. Gracias a `baselineOnMigrate=true` **marca baseline en versión 1 y NO ejecuta
   `V1__baseline.sql`**.
3. Los datos quedan intactos.
4. A partir de ahí, cualquier `V2__*.sql`, `V3__*.sql` nueva se aplica en el siguiente
   arranque.

**Esto es el caso del 100% del parque actual.** Los clientes no pierden datos al
adoptar Flyway.

### Escenario B — Instalación nueva (base vacía)

1. Crear la base común con `./gradlew bootstrapDatabases`.
2. El bootstrap:
   a. Hace `CREATE DATABASE IF NOT EXISTS admin_tools`.
   b. Detecta que la base está vacía (cero tablas) → marca la instalación como
      **fresh install**.
   c. Aplica `common/V1__baseline.sql` vía Flyway. Queda en versión 1.
   d. Aplica el **seed de datos base** desde `src/main/resources/db/seed/common.sql`
      (usuarios, bodega, configuración, catálogos de tipos, etc.). Este paso sólo
      corre en fresh install: si al comienzo la base ya tenía tablas, el seed se
      omite para no pisar datos reales.
3. Arrancar el app y registrar al menos una caja desde la UI. En ese flujo el app
   ejecuta `CREATE DATABASE admin_tools_caja_N` y aplica `caja/V1__baseline.sql`
   vía Flyway, parametrizando los placeholders `${caja_db}` y `${codigo_bodega}`.

### Escenario C — Cajas ya existen, sólo aplicar la última versión

Es el caso típico después de un upgrade del JAR en un cliente en producción que ya
tiene `admin_tools_caja_1`, `admin_tools_caja_2`, ... funcionando.

Al arrancar el app, `SchemaMigrator.migrateAll()`:

1. Aplica las pendientes de `common/` contra `admin_tools` (si hay `Vn+1` nuevas).
2. Lee `SELECT nombre_db FROM cajas` y **para cada caja existente** corre Flyway
   contra esa base con las migraciones `caja/`. Flyway compara `schema_version` de
   cada caja con las migraciones presentes y aplica sólo las que faltan.
3. Si una caja ya está en la última versión, Flyway no hace nada sobre esa base.
4. Si una caja estaba en `V3` y el JAR trae `V4` y `V5`, Flyway aplica V4 y V5 en
   orden sobre esa caja específica, y registra cada una en su propia tabla
   `schema_version`.

No hay que correr ningún comando manual: basta con que el cliente actualice el JAR
y arranque el app. Todas las cajas quedan alineadas a la última versión al terminar
el arranque (antes de que la UI empiece a operar).

### Escenario D — SHAROM va adelante del resto del parque

Situación real: el baseline `V1__baseline.sql` se generó desde SHAROM, que tiene
tablas/columnas/procedimientos que el resto del parque todavía no tiene. El resto
de clientes, al adoptar Flyway (Escenario A), quedan baselineados en V1 pero con
un schema **más viejo** que lo que V1 describe. Hay un gap entre "lo que dice
`schema_version.version=1`" y "lo que realmente tiene el cliente".

Cómo se resuelve:

1. **Identificar el diff**: correr `dumpSchema` apuntando a un cliente "atrasado"
   y comparar contra el dump de SHAROM. Todo lo que sobra en SHAROM es lo que
   falta en el resto.
2. **Escribir `common/V2__alinear_con_sharom.sql`** con ese diff, usando DDL
   **idempotente**:
   ```sql
   -- Agregar columna si no existe (MySQL 8+: ADD COLUMN IF NOT EXISTS)
   ALTER TABLE cliente ADD COLUMN IF NOT EXISTS email VARCHAR(100) NULL;

   -- Para tablas nuevas:
   CREATE TABLE IF NOT EXISTS nueva_tabla ( ... );

   -- Para procedimientos/funciones/triggers:
   DROP PROCEDURE IF EXISTS mi_proc;
   DELIMITER $
   CREATE PROCEDURE mi_proc(...) BEGIN ... END$
   DELIMITER ;
   ```
   La idempotencia es crítica porque **V2 va a correr también contra SHAROM** (que
   ya tiene esos objetos). Con `IF NOT EXISTS` / `DROP IF EXISTS + CREATE`, SHAROM
   la aplica sin romperse y queda marcada en V2 junto con el resto del parque.
3. **Resultado del arranque siguiente**:
   - Cliente atrasado: `schema_version` estaba en 1 → Flyway aplica V2 → queda en 2
     con el schema alineado a SHAROM.
   - SHAROM: `schema_version` estaba en 1 → Flyway aplica V2 → los `IF NOT EXISTS`
     no hacen nada, los `DROP+CREATE` reemplazan rutinas sin pérdida → queda en 2.
   - Cliente nuevo (bootstrap): aplica V1 + V2 de corrido → queda en 2.
4. **Para cajas**: mismo patrón con `caja/V2__...sql`. Se aplica a todas las
   `admin_tools_caja_*` existentes durante `migrateAll()`, y a las cajas nuevas
   durante `migrateNewCajaDatabase()`.

> Alternativa no recomendada: marcar manualmente a SHAROM en V2 con
> `UPDATE schema_version SET version='2' ...` y hacer V2 no-idempotente. Funciona
> pero deja a SHAROM "fuera de banda" — cualquier error en el diff no se detecta
> hasta que otro cliente explota. Mejor que V2 corra en todos lados por igual.

## Cómo funciona durante el arranque normal del app

`ConexionStatic.conectarBD()` llama a `SchemaMigrator.migrateAll()`, que:

1. Corre las migraciones `common/` contra `admin_tools`.
2. Lee `SELECT nombre_db FROM cajas` y por cada resultado corre las migraciones
   `caja/` contra esa base.

Cada base lleva su propia tabla `schema_version` (una por base).

## Modelo multi-caja (detallado)

Una instalación de adminTools consta de **una base común** `admin_tools` y **N bases
por caja** `admin_tools_caja_1`, `admin_tools_caja_2`, ... Cada caja física del
negocio (un POS, una estación de facturación) tiene su propia base dedicada para la
facturación, y todas comparten el catálogo/inventario/usuarios vía `admin_tools`.

### Por qué una base por caja

- **Aislamiento transaccional**: cada caja factura en su propia secuencia de
  `numero_factura` sin contención con las demás.
- **Contabilidad por caja**: cierres, rangos CAI, totales de tarjeta/efectivo se
  llevan localmente en la base de la caja.
- **Triggers parametrizados**: los triggers de `detalle_factura` impactan el kardex
  global en `admin_tools.articulo_kardex` usando *el `codigo_bodega` asignado a esa
  caja*. Dos cajas pueden estar atadas a la misma bodega o a bodegas distintas.

### Qué vive en `admin_tools` (común)

- Catálogo: `articulo`, `precios_articulos`, `categorias`, `marca`, ...
- Inventario: `bodega`, `articulo_kardex`, `kardex`, procedimientos de ajuste/venta.
- Clientes, proveedores, vendedores, usuarios, permisos, configuración general.
- **`cajas`**: catálogo de cajas registradas. Columnas relevantes:
  - `codigo` (PK autogenerada)
  - `descripcion`
  - `codigo_bodega` — bodega con la que esa caja descuenta inventario
  - `nombre_db` — nombre de la base física (`admin_tools_caja_<codigo>`)

### Qué vive en cada `admin_tools_caja_N`

- `encabezado_factura` — una fila por factura emitida en esa caja.
- `detalle_factura` — líneas de la factura; al insertar dispara el trigger que
  descuenta inventario en `admin_tools.articulo_kardex` vía
  `crear_venta_kardex` / `crear_venta_insumo_kardex`.
- `datos_factura` — rangos CAI / autorización SAR asignados a esa caja.
- `f_costo_factura(numero_factura)` — función que calcula el costo (usando
  `admin_tools.precios_articulos` con `codigo_precio=4`) de una factura específica.

### Cómo se crea una caja nueva desde la UI

El flujo lo maneja `CajaDao.registrar(Caja)`:

1. `INSERT INTO admin_tools.cajas (descripcion, codigo_bodega, nombre_db) ...`
   y recupera el `codigo` autogenerado.
2. Fija `nombreBd = "admin_tools_caja_" + codigo` y actualiza la fila.
3. Llama a `crearDataBase(codigo)` que ejecuta
   `CREATE DATABASE admin_tools_caja_<codigo>` en el servidor MySQL.
4. Llama a `ConexionStatic.buildSchemaMigrator().migrateNewCajaDatabase(nombreBd, codigoBodega)`.
   Esto configura un Flyway apuntado a la base recién creada, con los placeholders:
   - `${caja_db}` → `admin_tools_caja_<codigo>`
   - `${codigo_bodega}` → el código de bodega elegido en el formulario de la caja
   y aplica `caja/V1__baseline.sql` + cualquier `caja/V2+` que exista.
5. La base queda con su propia tabla `schema_version` en versión `N` (la última
   migración disponible), alineada con el resto del parque.

### Por qué los placeholders

`caja/V1__baseline.sql` se generó vía `SchemaDumper` desde una base caja real
(`admin_tools_caja_1`, bodega `1`), así que el dump trae esos valores literales
hardcodeados dentro del cuerpo del trigger `detalle_factura_b_insert`:
el `codigo_bodega` que se usa para buscar el kardex, el string descriptivo
`'facturado en <caja_db>'` que va a los movimientos de kardex, y los argumentos
al procedimiento `crear_venta_insumo_kardex`.

Si una caja 3 atada a bodega 7 se creara con ese SQL tal cual, estaría descontando
inventario de la bodega 1 y etiquetando los movimientos como `facturado en
admin_tools_caja_1` — un bug silencioso de contabilidad. Los placeholders se
sustituyen en **tiempo de migración**, una sola vez, al crear esa caja; el trigger
queda compilado en MySQL con los valores correctos de esa caja específica.

### Qué pasa en arranques posteriores

`SchemaMigrator.migrateAll()` recorre `SELECT nombre_db FROM cajas` y corre las
migraciones `caja/` contra cada base. Para la V1 esto no hace nada (ya estaba
baselineada al crear la caja), pero cualquier `caja/V2+.sql` sí se aplica. Los
placeholders se pasan igual (con `codigo_bodega=1` por defecto en ese recorrido,
ya que V1 no se re-ejecuta; si escribís una V2 que los use, tenés que enterarte de
que en el arranque normal vienen con un valor placeholder y diseñar la migración
para no depender de ellos — o refactorizar `migrateAll` para leer `codigo_bodega`
por caja desde la tabla `cajas`).

### Agregar un cambio que afecta a todas las cajas

Ejemplo: querés agregar una columna `observacion_interna` a `detalle_factura` en
todas las cajas. Creá `src/main/resources/db/migration/caja/V2__detalle_observacion_interna.sql`:

```sql
ALTER TABLE detalle_factura ADD COLUMN observacion_interna VARCHAR(255) NULL;
```

Al siguiente arranque del app, `SchemaMigrator.migrateAll()` la aplica contra
`admin_tools_caja_1`, `admin_tools_caja_2`, ... una por una. Y cualquier caja nueva
que se registre desde la UI despues de ese punto aplicará V1 + V2 en una sola
pasada durante `migrateNewCajaDatabase`.

## Cómo hacer bootstrap de una instalación nueva

Configurar las credenciales en `ConexionStatic.java` apuntando al servidor destino,
después:

```bash
./gradlew bootstrapDatabases   # crea admin_tools y aplica common/V1__baseline.sql
```

Esto ejecuta `DatabaseBootstrap` que:
1. Conecta al MySQL del servidor.
2. Hace `CREATE DATABASE IF NOT EXISTS admin_tools`.
3. Aplica `common/V1__baseline.sql` vía Flyway.

**Las bases por caja no se crean aquí.** El app crea cada `admin_tools_caja_N`
desde la UI al registrar la caja (`CajaDao.registrar`): ejecuta `CREATE DATABASE`
y luego `SchemaMigrator.migrateNewCajaDatabase(nombreBd, codigoBodega)`, que aplica
`caja/V1__baseline.sql` con los placeholders `${caja_db}` y `${codigo_bodega}`
resueltos al nombre de la base y al código de bodega de esa caja.

**Después del bootstrap:** arrancar el app y registrar al menos una caja desde
la UI para tener una instalación funcional end-to-end.

## Cómo agregar una migración nueva (workflow típico)

Ejemplo: necesitás agregar una columna `email` a `cliente`.

1. Crear archivo `src/main/resources/db/migration/common/V2__cliente_email.sql`:
   ```sql
   ALTER TABLE cliente ADD COLUMN email VARCHAR(100) NULL AFTER movil;
   ```
2. Actualizar el código Java que lo necesita (`Cliente.java`, `ClienteDao.java`, etc.).
3. Probar localmente:
   - Si tu local ya tiene el schema, al arrancar el app Flyway detecta V2, la aplica,
     y registra la fila en `schema_version`.
   - Verificar con: `SELECT * FROM admin_tools.schema_version;`
4. Commitear el `.sql` junto con los cambios de Java.
5. Cuando cualquier cliente actualice el JAR y arranque el app, Flyway aplica V2
   automáticamente antes de que el app empiece a operar. Los datos existentes no
   se tocan.

## Seed de datos base (instalaciones nuevas)

El archivo `src/main/resources/db/seed/common.sql` contiene las filas mínimas
que debe traer cargada una instalación nueva para poder operar (usuarios, bodega,
configuración, impuestos, tipos de pago, tipos de movimiento, etc.). Se genera
con:

```bash
./gradlew dumpSeed
```

Esto corre `SeedDumper`, que lee la conexión activa de `ConexionStatic`
(base `admin_tools`) y emite un `INSERT` por fila para una lista predefinida de
tablas. Por tabla se puede pedir todas las filas o un límite (por ejemplo
`cliente` → 1 fila, `banco` → 2 filas, `config_app` → todas).

### Qué se dumpea

Definido en `SeedDumper.TABLES`. Lista actual:

- `banco` (2), `bodega` (todas), `cliente` (1), `config_app` (todas),
  `config_user_facturacion` (todas), `datos_empresa` (todas), `departamento`
  (todas), `empleados` (1), `impuesto` (todas), `marcas` (1), `precios` (todas),
  `proveedor` (1), `tipo_articulo` (todas), `tipo_cuenta_banco` (todas),
  `tipo_empleado` (todas), `tipo_factura` (todas), `tipo_movimiento_banco`
  (todas), `tipo_movimiento_kardex` (todas), `tipo_pago` (todas), `usuario`
  (todas).

### Cuándo se aplica

**Sólo en fresh install**, desde `DatabaseBootstrap`:

1. Antes de migrar, cuenta las tablas de `admin_tools`. Si hay 0, marca
   `wasFreshInstall = true`.
2. Flyway aplica `common/V1__baseline.sql`.
3. Si `wasFreshInstall`, lee `/db/seed/common.sql` del classpath, lo parte por
   `;` y ejecuta cada sentencia contra la base recién creada.
4. Si la base ya tenía tablas, el seed se **omite** — nunca corre contra un
   cliente en producción, ni siquiera si por accidente alguien ejecuta
   `bootstrapDatabases` sobre un servidor con datos.

### Regenerar el seed

Cada vez que cambie la lista de datos base o que cambien esas filas en la base
de referencia, correr `./gradlew dumpSeed` y commitear el `common.sql`
resultante. Si hay que agregar o quitar tablas del alcance, editar el array
`TABLES` en `SeedDumper.java`.

## Regenerar baselines desde una base viva

Si el schema del `V1__baseline.sql` quedó desactualizado o se agregó una instalación
nueva con divergencias:

```bash
./gradlew dumpSchema
```

Ejecuta `SchemaDumper`, que consulta `information_schema` del servidor activo y
regenera `common/V1__baseline.sql` y `caja/V1__baseline.sql` con el schema actual.
Revisar el diff antes de commitear.

## Troubleshooting

**"Migration checksum mismatch for migration version 2"**:
Alguien editó un `.sql` ya aplicado en algún cliente. Soluciones:
- Revertir el cambio y hacer una migración nueva (recomendado).
- `flyway repair` en el cliente afectado (último recurso).

**"Table 'schema_version' already exists" en bootstrap de base vacía**:
La base no estaba vacía. Usar el escenario A (arrancar el app normal) en vez del
bootstrap.

**V1 re-ejecutada por error y tablas destruidas**:
No debería pasar con el setup actual: V1 ya no contiene `DROP TABLE IF EXISTS`, y si
alguien borra `schema_version` y Flyway intenta re-correr V1, fallará en el primer
`CREATE TABLE` existente. Si aún así perdieron datos, restaurar del backup.
