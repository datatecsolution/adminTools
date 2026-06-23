# Análisis de migración — miscelanías Wyc

> Análisis **read-only** del 2026-06-23 sobre la BD de producción de Wyc
> (`user_pos@192.168.1.25`, MySQL 8.0.45). No se modificó nada. Sigue el
> [runbook de migración de cliente](./runbook-migracion-cliente.md) — este
> documento es la **Fase 1 (inspección)** + el plan específico de Wyc.

## TL;DR
- **Flyway nunca corrió en Wyc**: ninguna de las 8 BDs tiene `schema_version`. El
  esquema está armado a mano (v0), **parcial y fuera de orden**.
- **Común** (`admin_tools`): efectiva ~**V7**; faltan **V6 (dato) + V8→V31**. Los
  objetos destino no existen → las `ALTER ADD` crudas aplican limpio.
- **Cajas** (`caja_1..7`): estado **mixto** — trigger V8 ✓ pero **V7 decimal NO
  aplicado** (`subtotal_excento` sigue `float`); `caja_7` sin la función V6. Las 4
  migraciones de caja son idempotentes → aplican seguras.
- **Riesgo**: con `schema_version` vacío, Flyway baselinea V1 y **reintenta V2-V7**
  además de V8-V31 → hay que confirmar en **ensayo** que ninguna `ALTER` cruda choque.
- **Volumen alto**: `caja_1` = 1.16M líneas de factura → la conversión V7
  (`ALTER … MODIFY`) bloquea tabla varios minutos → **ventana obligatoria**.

## Entorno
| | |
|---|---|
| Host | `192.168.1.25` (user `user_pos`) |
| Servidor | MySQL **8.0.45**-0ubuntu0.22.04.1 |
| BDs | `admin_tools` (común) + `admin_tools_caja_1 … _7` (7 cajas) |
| `schema_version` | **AUSENTE en las 8** → Flyway nunca inicializado |

Migraciones objetivo del repo: **común → V31** (28 scripts `.sql` + Java V3/V4/V5),
**cada caja → V8** (`V1,V2,V6,V7,V8`).

## Común (`admin_tools`) — efectiva ~V7, parcial
115 tablas, 38 rutinas. Marcadores verificados:

| Migración | Marcador | Wyc |
|---|---|---|
| V2 | tablas `kardex`, `rutas_cobro` | ✓ |
| V7 | funciones `f_can_saldo_kardex`, `f_costo_dev` (`DROP IF EXISTS` → idempotentes) | ✓ |
| **V6** | dato: `UPDATE usuario SET tipo_permiso=4 WHERE tipo_permiso=1` | ✗ — **hay 1 usuario `tipo_permiso=1`** sin promover |
| V8 | `config_user_facturacion.rotacion_automatica_cajas` | ✗ |
| V9 | `cliente.id_cobrador` | ✗ |
| **V18** | tabla `existencia_articulo_bodega` | ✗ FALTA |
| **V21** | vista `articulo_view` | ✗ FALTA |
| V25 | `proveedor.es_ajuste` | ✗ |
| V28 | `datos_empresa.logo_url` | ✗ |
| V29 | `marcas.mostrar_pos` | ✗ |
| V30 | `config_user_facturacion.crear_cliente_credito` | ✗ |
| V31 | `config_app.dias_ranking_mas_vendidos` | ✗ |

→ **Pendientes reales: V6 + V8→V31.** Como las columnas/objetos destino no existen,
las `ALTER ADD` crudas (V8/V9/V28-V31) aplican sin "Duplicate column"; V18/V21 crean
lo faltante; V10-V27 son idempotentes (procedures `*_si_falta`/`*_si_tipo_difiere`,
`IF NOT EXISTS`). Las tablas kardex base (`articulo_kardex`, `movimiento_kardex`,
`codigos_articulos`, `kardex`) **sí** existen.

## Cajas (`caja_1..7`) — estado MIXTO
| Caja | V6 fn `f_costo_factura` | V7 `subtotal_excento` | V8 trigger |
|---|---|---|---|
| caja_1 | ✓ | **float(8,2)** ✗ | ✓ |
| caja_2..6 | ✓ | **float(8,2)** ✗ | ✓ |
| **caja_7** | **✗ FALTA** | **float(8,2)** ✗ | ✓ |

- **V7 decimal NO aplicado** en ninguna (sigue `float`, debe ser `decimal`). Es el
  fix de precisión — la migración lo aplica vía `ALTER … MODIFY` (self-idempotente).
- `caja_7` sin `f_costo_factura` (V6 lo recrea con `DROP IF EXISTS`).
- Las 4 migraciones de caja (`V2` `IF NOT EXISTS`, `V6` `DROP+CREATE`, `V7` `MODIFY`,
  `V8` `DROP TRIGGER IF EXISTS + CREATE`) son **todas idempotentes** → seguras.

### ⚠️ Volumen y ventana
| Caja | `encabezado_factura` | `detalle_factura` |
|---|---|---|
| caja_1 | 329.708 | **1.161.625** |
| caja_2 | 76.687 | 298.165 |
| caja_3 | 272.217 | 851.379 |
| caja_7 | 28 | 84 (nueva/sin uso) |

La conversión V7 (`ALTER TABLE detalle_factura MODIFY … DECIMAL`) reescribe la tabla
y **la bloquea**: en caja_1 (1.16M filas) y caja_3 (851K) tardará **minutos** →
**ventana de mantenimiento obligatoria** (con todos los Swing cerrados).

## Riesgo central
`schema_version` vacío + `baselineOnMigrate=true, baselineVersion=1` → Flyway baselina
V1 (no lo ejecuta) y **aplica V2→V31** (común) / **V2→V8** (cajas). La mayoría son
idempotentes o apuntan a objetos ausentes, **pero** hay que confirmar en el ensayo que
ninguna `ALTER` cruda choque con un objeto ya presente (p.ej. `V10` agrega índices
compuestos a `articulo_kardex`; si ya existieran, `ADD INDEX` falla). **Por eso el
ensayo es obligatorio** (cliente sin Flyway, esquema divergente).

## Plan de migración (resumen; detalle en el runbook)
1. **Ensayo en copia efímera** (Fase 2 del runbook) — `deploy/ensayo-wyc/ensayo.sh`
   automatiza: dump de esquema → MySQL docker efímero → runner Flyway (idéntico a
   `SchemaMigrator`) → verificar común=V31, cajas=V8, 0 fallidas → opcional: API
   `ddl-auto=validate`. **Obligatorio antes de tocar prod.**
2. **Backup en caliente** de las 8 BDs (`--single-transaction --routines --triggers`).
3. **Ventana**: parar la API y cerrar los Swing (0 transacciones abiertas) — clave por
   el lock de V7 en caja_1/caja_3.
4. **Migrar** con el runner Flyway (común + 7 cajas). Referencia de tiempo: el lock de
   V7 sobre 1.16M filas puede dominar la ventana; medirlo en el ensayo con datos.
5. **Verificar**: `schema_version` común=V31, cajas=V8, 0 fallidas; `existencia_articulo_bodega`
   poblada por V18 (`INSERT…SELECT FROM articulo_kardex`, no vacía); usuario
   `tipo_permiso=1` → 4 (V6); estructuras nuevas presentes.
6. **Validar**: API arranca (`Started AdmintoolsApplication`), smoke test; factura de
   prueba desde el Swing (kardex/triggers).
7. **Post**: desplegar el **build de Swing actualizado** a las terminales **antes** de
   reiniciar ninguna (si no, `Detected applied migration not resolved locally`).

## Notas
- La migración usa **Flyway 6.5.7** (igual que el Swing). MySQL 8.0 → solo el warning
  cosmético del parser (ver `project_flyway_upgrade_diferido`).
- El runner debe usar el **classpath del proyecto** (jar/`build`), no Flyway CLI,
  porque V3/V4/V5 son migraciones **Java** (no `.sql`).
- Inspección hecha por JDBC (`pymysql`) con credenciales en `~/.wyc.env` (en memoria,
  nunca la contraseña a disco/stdout). Borrar al terminar: `rm ~/.wyc.env`.
