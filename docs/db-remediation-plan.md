# Plan de remediación de la base de datos

Documento de referencia generado a partir del análisis del esquema `admin_tools` y las bases `admin_tools_caja_*`. Las fases están pensadas para implementarse como migraciones Flyway (`V2__…`, `V3__…`, …) sobre el baseline `V1__baseline.sql` ya existente.

## Resumen de hallazgos

Problemas estructurales detectados en el esquema actual:

- **Columnas monetarias en `float`** (`float(11,2)`, `float(8,2)`, `double`) en facturación, kardex, cuentas por cobrar. Causa descuadres por redondeo.
- **Tablas MyISAM** mezcladas con InnoDB → sin transacciones ni integridad referencial real en esas tablas.
- **Faltan claves primarias** en varias tablas de detalle / auxiliares.
- **Faltan claves foráneas** en relaciones críticas (facturas ↔ clientes, detalle ↔ artículos, kardex ↔ bodega, etc.).
- **Índices duplicados** PK + UNIQUE sobre la misma columna (ej. `encabezado_factura.numero_factura`), penalizan escrituras.
- **Faltan índices** en columnas de fecha usadas por reportes (`fecha`, `fecha_vencimiento`, etc.).
- **Charset `utf8mb3`** en todas las tablas en lugar de `utf8mb4`.
- **Valores centinela** (`'NA'`, `-1`, `'1990-01-01'`) en lugar de `NULL`, ensuciando queries y agregaciones.
- **Estados como strings libres** (`estado_factura varchar(25)`) en vez de ENUMs o tablas catálogo.
- **Typos en nombres de columnas** (`saldo_anterio`, `cantida_solicitada`, etc.).
- **Kardex compartido entre cajas** vía triggers que referencian `admin_tools.*` desde cada `admin_tools_caja_*` → acoplamiento fuerte, sin aislamiento multi-tenant.

## Plan de remediación por fases

| Fase | Alcance | Esfuerzo | Riesgo | Beneficio |
|------|---------|----------|--------|-----------|
| 1 | `float` → `DECIMAL` en columnas monetarias | Alto (~80 columnas + casts en Java) | Medio (migrar datos existentes sin perder precisión) | Descuadres desaparecen |
| 2 | MyISAM → InnoDB + agregar PKs faltantes | Bajo | Bajo | Integridad + transaccionalidad |
| 3 | Agregar FKs (empezar por las 10–12 más críticas) | Medio | Medio (puede fallar si hay huérfanos históricos) | Prevención real de inconsistencias |
| 4 | Drop de índices duplicados PK+UNIQUE | Muy bajo | Muy bajo | Writes más rápidos inmediato |
| 5 | Índices en columnas de fecha | Bajo | Muy bajo | Reportes rápidos |
| 6 | Migración de charset a `utf8mb4` | Medio | Medio | Soporte de caracteres modernos |
| 7 | `NULL` en lugar de centinelas | Alto | Alto (requiere tocar todos los DAOs) | Queries correctas |
| 8 | Unificar estados en ENUMs / catálogos | Medio | Bajo | Consistencia |
| 9 | Corregir typos (`saldo_anterio`, etc.) | Medio | Bajo | Limpieza |
| 10 | Rediseño del kardex multi-tenant | Muy alto | Muy alto | Aislamiento real entre cajas |

## Estado de avance

- **Fase 4 (drop de índices duplicados)** — **completada**. Implementada por
  `V4__drop_duplicate_indexes.java` (mayoría) y `V11__drop_indices_duplicados.sql`
  (los 2 que quedaban). US-005 cerrada. Ver `auditoria-resultado.md`.
- **Fase 5 (índices en columnas de fecha)** — **verificada**. La auditoría
  contra `admin_tools` y `admin_tools_caja_2` mostró que las 17 columnas de
  fecha críticas ya tenían índice. Las 15 candidatas sin índice no se usan
  en `WHERE/ORDER BY` en ningún DAO ni stored procedure. US-006 cerrada
  sin migración. Ver `auditoria-resultado.md`.
- **Fase 2 (MyISAM → InnoDB)** — **completada**. Implementada por
  `V12__convert_myisam_to_innodb.sql` con stored procedure dinámica que
  itera `information_schema` y convierte cualquier tabla MyISAM detectada.
  Idempotente y robusta contra drift (no hardcodea nombres del baseline).
  Baseline declara 3 tablas MyISAM: `articulo_bodega`, `pagos_creditos`,
  `salida_productos`. US-013 cerrada. Auditoría en
  `auditoria-myisam-pre-v12.sql`.

## Orden recomendado de ejecución

1. **Fases 4 y 5** — quick wins sin riesgo (drop de índices duplicados + índices de fecha).
2. **Fase 2** — MyISAM → InnoDB. Urgente por riesgo de corrupción ante crash.
3. **Fase 1** — `float` → `DECIMAL`. Crítico para correctness financiera.
4. **Fases 3, 6, 8, 9** — programables como V4–V7 de Flyway, riesgo acotado.
5. **Fase 7** — costosa (tocar DAOs), programar después de estabilizar lo anterior.
6. **Fase 10** — rediseño mayor; evaluar solo cuando el resto esté estable.

## Notas de implementación

- Cada fase debe entrar como un archivo Flyway versionado en `src/main/resources/db/migration/common/` o `.../caja/` según corresponda.
- Antes de Fase 1 y Fase 3: ejecutar queries de auditoría para detectar datos que impidan la migración (valores fuera de rango, huérfanos, etc.).
- Fase 10 probablemente requiere un documento de diseño propio antes de escribir SQL.
