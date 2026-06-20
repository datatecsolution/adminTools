# Runbook — Migración de esquema (Flyway) en un cliente

Procedimiento para llevar el esquema de un cliente al estado actual del repo
(common **V31**, cajas **V8** al momento de escribir esto), con downtime mínimo
y de forma reproducible. Basado en la migración de **Ronal** (2026-06-20:
common V16→V31, 6 cajas V7→V8, ~6 min de downtime, 0 fallidas).

> **Cliente desde cero (sin ninguna migración):** ver la sección
> [Caso: cliente sin Flyway inicializado](#caso-cliente-sin-flyway-inicializado).
> El flujo es el mismo, pero el **ensayo (Fase 2) es obligatorio**, no opcional.

## Arquitectura que estás tocando

Una sola BD compartida por 3 consumidores:
- **Swing** (escritorio del cliente) — aplica las migraciones vía `SchemaMigrator`
  (Flyway, `baselineOnMigrate=true`, `outOfOrder=true`) al arrancar.
- **API** (`admin-tools-api-v2`, Spring Boot, perfil `pdn`, **`ddl-auto=validate`**)
  — NO migra; solo valida que el esquema coincida con sus `@Entity`. Si no
  coincide, **no arranca**.
- **App de órdenes** (`at-ordenes-ventas-v2`, React) — pega a la API; no toca la
  BD. Su único riesgo es indirecto (si la API no arranca).

Esquema multi-tenant: BD común `admin_tools` + una BD por caja
`admin_tools_caja_N`. La API resuelve las cajas por `cajas.nombre_db`, así que en
ensayos las BDs deben llamarse **exactamente** igual.

---

## Pre-requisitos

- **Red:** VPN/acceso a la LAN del cliente; el MySQL suele estar en `<host>:3306`.
- **SSH** al server del cliente; **docker** accesible (`usermod -aG docker <user>`
  si hace falta; revertir con `gpasswd -d <user> docker`).
- **Herramientas locales:** `java` + `mysql-connector-j` (para inspección por JDBC
  sin cliente `mysql`); en el server: `mysqldump`, `mysql`, `docker`.
- **Credenciales** del cliente: en `~/Library/Application Support/AdminTools/<cliente>_connection.dat`
  (AES-128-CBC). Descifrar:
  ```bash
  read KEY IV < <(python3 -c "import hashlib;k=hashlib.sha256(b'AdminTools2024@Sec').digest()[:16];iv=hashlib.sha256(k).digest()[:16];print(k.hex(),iv.hex())")
  openssl enc -d -aes-128-cbc -K "$KEY" -iv "$IV" -in <cliente>_connection.dat
  # → db.server / db.port / db.name / db.login / db.password (Properties)
  ```

---

## Fase 1 — Inspección (read-only, sin downtime)

Determinar el estado de partida. Por JDBC (o cliente mysql):
1. **Versión de MySQL** (8.0 = soportado por Flyway 6.5.7; 9.x = warning cosmético,
   ver `docs`/memoria `project_flyway_upgrade_diferido`).
2. **¿Existe `schema_version`?** En `admin_tools` y en cada `admin_tools_caja_N`.
   - Si existe: `SELECT MAX(CAST(version AS UNSIGNED)), SUM(success=0) FROM schema_version` →
     versión actual + fallidas (debe ser 0; si hay fallidas, `repair` antes).
   - Si NO existe → cliente desde cero (ver sección dedicada).
3. **Drift fuera de Flyway:** ¿las estructuras destino ya existen por parches manuales?
   (columnas/tablas/SP de las migraciones pendientes). Si hay drift, las migraciones
   condicionales (`*_si_tipo_difiere`, `add_col_si_falta`) lo absorben; las `ADD COLUMN`
   crudas (V29/V30/V31) fallarían por duplicado → reconciliar primero.
4. **Tamaño y tablas grandes:** `information_schema.tables`. Cruzar las tablas que los
   `ALTER ... MODIFY` de las migraciones pendientes tocan contra su tamaño real —
   sólo las que **difieren de tipo Y tienen muchas filas** cuestan tiempo (en Ronal,
   la conversión pesada ya estaba hecha → no-op; los `ALTER` reales eran sobre tablas
   ≤22k filas → segundos).

---

## Fase 2 — Ensayo en copia efímera (recomendado; **obligatorio** para cliente desde cero)

Probar TODO sin tocar producción, en docker aislado. Verifica dos cosas: (a) las
migraciones aplican sin error sobre el esquema real; (b) **la API arranca contra el
esquema migrado** (`ddl-auto=validate`).

1. **Dump de esquema** del cliente (read-only): por BD,
   `mysqldump --no-data --routines --triggers --skip-add-drop-table --set-gtid-purged=OFF <db>`
   (sanitizar DEFINER: `sed -E 's/DEFINER=\`[^\`]*\`@\`[^\`]*\`//g'`), más
   `mysqldump --no-create-info --skip-triggers <db> schema_version` (el historial).
2. **MySQL efímero** en una red docker aislada, con los **nombres exactos**:
   ```bash
   docker network create ensayo-net
   docker run -d --name mysql-ensayo --network ensayo-net \
     -e MYSQL_ROOT_PASSWORD=throwaway \
     mysql:8.0 --character-set-server=utf8mb3 --collation-server=utf8mb3_general_ci --log-bin-trust-function-creators=1
   # crear admin_tools + admin_tools_caja_1..N (utf8mb3) y cargar schema + history
   ```
3. **Migrar** la copia a V31/V8 (runner Flyway — ver Apéndice A; exponer el mysql
   efímero con un proxy socat + túnel SSH si corrés Flyway desde tu Mac).
4. **Levantar la API de prod** (misma imagen) contra la copia migrada y leer el log:
   ```bash
   docker run -d --name api-ensayo --network ensayo-net \
     -e SPRING_PROFILES_ACTIVE=pdn \
     -e MYSQL_HOST=mysql-ensayo -e MYSQL_PORT=3306 -e MYSQL_DB=admin_tools \
     -e MYSQL_USER=root -e MYSQL_PASSWORD=throwaway -e MYSQL_TZ=GMT-6 \
     -e APP_JWT_SECRET=<base64-largo-dummy> -e APP_JWT_EXPIRATION_MS=86400000 \
     -e APP_TIMEZONE=America/Tegucigalpa -e CORS_ALLOWED_ORIGINS='*' -e SERVER_PORT=8080 \
     <imagen-de-la-api>
   docker logs api-ensayo 2>&1 | grep -iE "Started Admintools|Schema-validation|missing|wrong column"
   ```
   - ✅ `Started AdmintoolsApplication` → valida OK.
   - ❌ `Schema-validation: ... missing/wrong column` → el build de la API NO es
     compatible con el esquema destino. **Resolver antes de migrar prod** (alinear
     versión de API o migración).
   - ⚠️ Sin las env `APP_JWT_SECRET` (+ las demás), la API muere antes de validar
     (`Could not resolve placeholder 'APP_JWT_SECRET'`) — es config, no esquema.
5. **Limpiar:** `docker rm -f api-ensayo mysql-ensayo <proxy>; docker network rm ensayo-net`.

---

## Fase 3 — Backup en caliente (sin downtime)

Red de seguridad **imprescindible**. No para nada (`--single-transaction`):
```bash
mysqldump -h<host> -u<user> -p<pass> --single-transaction --routines --triggers --events \
  --databases admin_tools admin_tools_caja_1 ... admin_tools_caja_N > backup_<cliente>_premig_<ts>.sql
```
Verificar: `tail -1` dice `Dump completed`, tamaño razonable, rc=0, las N BDs presentes.

---

## Fase 4 — Ventana: detener escrituras

1. **Parar la API:** `docker stop admin-tools-api-v2` (corta la app de órdenes).
2. **Confirmar que nadie factura:** el Swing pega directo a MySQL. Verificar
   `information_schema.processlist` y `innodb_trx`:
   - **0 transacciones abiertas** → los `ALTER`/DDL no se bloquean.
   - Conexiones `Sleep` de terminales con Swing abierto pero idle son tolerables si
     nadie opera durante la ventana (confirmar con quien esté en sitio). Mejor aún:
     que cierren los Swings.

---

## Fase 5 — Migración controlada (Flyway)

Disparar Flyway directo al MySQL del cliente (más trazable que esperar al Swing).
Ver **Apéndice A** para el runner. Aplica `common` (locations `db/migration/common`)
y luego cada caja (locations `db/migration/caja`, con placeholders `caja_db`/`codigo_bodega`).
Usa `outOfOrder(true)` + `repair()` antes de `migrate()` (el mismo `SchemaMigrator`).

Tiempo de referencia (Ronal): common ~163s, cada caja ~8s → **~3.5 min**.

---

## Fase 6 — Verificación

- `schema_version`: common = **target**, cada caja = **target**, **0 fallidas**.
- **Backfill de inventario:** V18 puebla `existencia_articulo_bodega` con
  `INSERT…SELECT FROM articulo_kardex` → debe quedar con filas (en Ronal, 5023; NO
  vacía). V17 crea la bodega "Pérdidas".
- Estructuras nuevas presentes (`datos_empresa.logo_url`, `marcas.mostrar_pos`, etc.).

---

## Fase 7 — Validación

1. **Reiniciar la API:** `docker start admin-tools-api-v2` → esperar
   `Started AdmintoolsApplication` (valida contra el esquema nuevo).
2. **Smoke test:**
   - `swagger-ui` → 200; app de órdenes pública → 200.
   - Un endpoint que use el esquema nuevo (p.ej. `/inventory/low-stock`) sin auth →
     **401** (no 500: 401 = auth, esquema OK; 500 = problema).
   - Logs sin errores reales (ojo: `ExceptionTranslationFilter` en una línea `INFO`
     es falso positivo).
3. Cliente abre el Swing y hace una factura de prueba (verifica kardex/triggers).

---

## Fase 8 — Rollback (si algo falla)

Restaurar el dump de la Fase 3 y reiniciar la API. La API valida tanto en el
esquema viejo como en el nuevo, así que el rollback es restaurar datos+esquema.

---

## Fase 9 — Post-migración: ⚠️ Swing de las terminales (cabo crítico)

El Swing de las terminales suele ser la versión que aplicó la última migración
**vieja** (su jar sólo tiene migraciones hasta esa versión). Mientras siga
**abierto**, funciona. Pero **al reiniciar** correría Flyway y encontraría las
migraciones nuevas **aplicadas en la BD pero no en su jar** →
`Detected applied migration not resolved locally: ...` → **no arranca**.

- `outOfOrder` **NO** arregla esto (es migración aplicada-pero-no-en-jar, no faltante).
- **Acción:** desplegar el **build de Swing actualizado** (con las migraciones nuevas
  en el classpath) a las terminales **antes** de que alguien reinicie una.

---

## Caso: cliente sin Flyway inicializado

Cliente que nunca corrió migraciones (sin tabla `schema_version`):

- **BD con esquema legacy existente (datos reales):** `baselineOnMigrate=true` marca
  un baseline en **V1** (NO ejecuta V1) y aplica **V2→V31** (common) y **V2→V8** (cajas).
  Las migraciones condicionales (`fix_drifts_*`, `*_si_tipo_difiere`, `add_col_si_falta`)
  están diseñadas para **adoptar esquemas divergentes** — por eso el **ensayo (Fase 2)
  es obligatorio**: un esquema legacy muy distinto de V1 puede hacer fallar alguna
  migración, y querés descubrirlo en la copia efímera, no en producción.
- **BD nueva/vacía:** Flyway ejecuta **V1** (crea el esquema completo) y luego V2→V31.
- **Cajas:** crear cada `admin_tools_caja_N` y registrarla en `admin_tools.cajas`
  (`nombre_db`, `codigo_bodega`) antes de migrarla; aplicar `db/migration/caja` V1→V8.
- Resto del flujo (backup, ventana, validación, Swing) idéntico.

---

## Apéndice A — Runner Flyway (compilar y ejecutar)

Java mínimo que aplica common + N cajas, mide tiempo y captura errores por BD
(idéntico a `SchemaMigrator`: `baselineOnMigrate`, `table("schema_version")`,
`outOfOrder(true)`, `repair()` + `migrate()`). Classpath:
`src/main/resources` (los `.sql`) + `build/classes/java/main` (las migraciones Java
V3/V4/V5) + `flyway-core-6.5.7.jar` + `mysql-connector-j` + `commons-dbcp2/pool2/logging`.

```java
// ProdMigrate.java — apuntar a jdbc:mysql://<host>:3306/<db>, user/pass del cliente.
// run("admin_tools","classpath:db/migration/common", null)
// run("admin_tools_caja_i","classpath:db/migration/caja", {caja_db, codigo_bodega})
//   Flyway.configure().dataSource(ds).locations(loc).table("schema_version")
//     .baselineOnMigrate(true).baselineVersion("1").baselineDescription("baseline")
//     .outOfOrder(true).placeholders(ph).load();
//   f.repair(); f.migrate();
```
> El `build/resources/main` puede estar **desactualizado** (sin las últimas `.sql`).
> Usar `src/main/resources` en el classpath para los `.sql`, y `build/classes/java/main`
> para las migraciones Java compiladas (V3/V4/V5).

## Apéndice B — Inspección por JDBC sin cliente `mysql`

En macOS no siempre hay `mysql` CLI. Inspeccionar/migrar con `java` + el
`mysql-connector-j-8.0.33.jar` del cache de Gradle, pasando la password por
**variable de entorno** (nunca a disco — el clasificador lo bloquea). Para correr
Flyway contra un MySQL que sólo escucha en loopback del server, exponerlo con un
proxy `socat` (`-p 127.0.0.1:<p>:3306`) + túnel SSH `-L`.

---

_Referencias de memoria: `project_ronal_migracion_v16_v31` (ejecución real),
`project_flyway_upgrade_diferido` (MySQL 9.x), `project_deploy_pdn_paralelo`
(layout del server), `reference_related_repos`, `project_pruebas_dulce_ronal`
(stack de pruebas)._
