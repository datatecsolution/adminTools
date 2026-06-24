# Migración de esquema — cliente venecia

> Análisis + ensayo + **ejecución en producción** (2026-06-24). Sigue el
> [runbook de migración de cliente](./runbook-migracion-cliente.md). Cliente
> **solo Swing + MySQL** (sin API). Inspección por JDBC read-only; credenciales
> en `~/.venecia.env` (en memoria, nunca la contraseña a disco/stdout).

## TL;DR
- **MIGRADO Y VERIFICADO OK.** Común → V31, cajas activas (2/3/6/7) → V8, 0 fallidas.
- Igual que Wyc: **Flyway nunca había corrido** (sin `schema_version`); esquema v0 parcial.
- **Hallazgo del ensayo:** 3 BDs de caja (1/4/5) existen con datos pero **no están
  registradas en `admin_tools.cajas`** → resultaron ser **archivos viejos**
  (última factura 2022-2023). **Decisión: NO migrarlas** (quedan intactas).

## Entorno
| | |
|---|---|
| Host | `192.168.88.251` (user `admin`) |
| Servidor | MySQL **8.0.42**-0ubuntu0.20.04.1 |
| BDs | `admin_tools` + `admin_tools_caja_1 … _7` |
| Tamaño total | ~290 MB |

## Estado pre-migración
- **Común:** efectiva ~pre-V8 (faltaban V6 + V8→V31; sin `existencia_articulo_bodega`
  ni `articulo_view`). **19 usuarios `tipo_permiso=1`** sin promover (V6).
- **Cajas:** V8 trigger ✓ y V6 fn ✓, pero **V7 decimal NO aplicado**
  (`subtotal_excento` = `float`). Todas las migraciones de caja son idempotentes.

### Cajas: registradas vs existentes (chequeo clave — lo atrapó el ensayo)
| caja | en `cajas` | #facturas | última factura | acción |
|---|---|---|---|---|
| caja_2 | ✅ | 133.739 | 2026-06-24 (activa) | **migrar** |
| caja_3 | ✅ | 175.969 | 2026-06-24 (activa) | **migrar** |
| caja_6 | ✅ | 26.825 | 2026-06-24 (activa) | **migrar** |
| caja_7 | ✅ | 37.959 | 2026-06-24 (activa) | **migrar** |
| caja_1 | ⚠️ no | 45.028 | 2023-08-16 | dejar (vieja) |
| caja_4 | ⚠️ no | 107.367 | 2023-08-16 | dejar (vieja) |
| caja_5 | ⚠️ no | 18 | 2022-03-07 | dejar (muerta) |

> El Swing solo migra/usa las cajas de `SELECT nombre_db FROM cajas`. Las no
> registradas se saltean solas. **Verificar esta tabla es paso obligatorio** para
> no dejar atrás una caja activa (acá no pasó: las 4 activas estaban registradas).

## Ensayo (Fase 2, copia efímera)
`CLIENT=venecia bash deploy/ensayo-wyc/ensayo.sh` → común→V31 y cajas activas→V8
sin error (solo warnings cosméticos: utf8mb3, `IF NOT EXISTS`, `DROP … IF EXISTS`).

## Tiempos
- **Medido** (datos reales, M2 Pro local): V7 decimal caja_2 (752K líneas) ~4s;
  V10 índice `movimiento_kardex` (3.26M filas) 3.4s. El backfill V18 lee de
  `articulo_kardex` (1.527 filas) → trivial.
- **Real en prod (server del cliente, más lento):** **~14 min** total — común
  9m45s, cajas ~4 min. Backup (290 MB) ~segundos.

## Ejecución en producción (2026-06-24)
1. **Backup** de las 8 BDs → `~/venecia_premig_20260624_122153.sql` (432 MB,
   "Dump completed").
2. **Ventana**: 0 conexiones a `admin_tools*`, 0 transacciones abiertas (Swings cerrados).
3. **Migración** con el runner `EnsayoMigrate` apuntado a prod (`ENS_*` = venecia) —
   `SchemaMigrator.migrateAll()`: común + las 4 cajas registradas.

### Verificación (todo ✓)
| Check | Resultado |
|---|---|
| `schema_version` común | V31, 0 fallidas |
| `schema_version` caja_2/3/6/7 | V8, 0 fallidas |
| caja_1/4/5 | intactas (sin `schema_version`) |
| Backfill V18 `existencia_articulo_bodega` | 4.620 filas |
| V6 `usuario.tipo_permiso` | 19 usuarios 1 → 4 (sin tipo 1) |
| V7 decimal `detalle_factura.precio` | `decimal(15,2)` |
| V21/V28 | `articulo_view` + `datos_empresa.logo_url` presentes |

**Sin un solo error.**

## Pendiente / post
- **Smoke test** del usuario: factura de prueba desde el Swing actualizado
  (kardex/triggers/decimal).
- Conservar el backup unos días por rollback. Sin API que reiniciar.
- Las terminales ya tenían el **Swing actualizado** (cabo crítico del runbook).
