# Resultado de auditoría — US-005 y US-006

Documenta la auditoría previa a las migraciones V11 (drop de índices
duplicados) y V12 (índices en columnas de fecha). El script
`auditoria-indices-pre-v11-v12.sql` se ejecutó contra dos esquemas
productivos.

## Esquemas auditados

| Esquema | Rol | Observaciones |
|---|---|---|
| `admin_tools` | Caja legacy + catálogos compartidos | También contiene tablas transversales (compras, CxC, bancos) |
| `admin_tools_caja_2` | Caja transaccional | Subconjunto de tablas: encabezado/detalle_factura, datos_factura, kardex per-caja |

## Bloque 1 — Índices duplicados PK + UNIQUE/INDEX

### `admin_tools`

| Tabla | Columna PK | Índice redundante | Tipo |
|---|---|---|---|
| `articulo_kardex` | `codigo_kardex` | `codigo_kardex` | INDEX |
| `cuentas_por_cobrar_facturas` | `codigo_reguistro` | `codigo` | INDEX |

### `admin_tools_caja_2`

Sin duplicados — bloque vacío.

### Conclusión

Solo 2 índices a eliminar, ambos en `admin_tools`. El resto de
candidatos sospechosos detectados por revisión del baseline ya habían
sido limpiados por `V4__drop_duplicate_indexes.java` en migraciones
anteriores.

**Implementado en:** `common/V11__drop_indices_duplicados.sql`

## Bloque 2 — Columnas de fecha sin índice

Se analizaron 15 candidatas en `admin_tools` y 2 en `admin_tools_caja_2`.
Para cada una se verificó:

- Si la columna se usa en `WHERE`, `ORDER BY`, `BETWEEN` o `GROUP BY` en
  Java DAOs y stored procedures (`V7__recrear_funciones_procedures_triggers.sql`).
- Tamaño de la tabla (row count).
- Existencia de otros índices que ya cubran las consultas existentes.

### Resultado del análisis por candidata

| # | Tabla.columna | Esquema | Filas | Uso en WHERE/ORDER | Decisión |
|---|---|---|---|---|---|
| 1 | `detalle_movimiento_kardex.fecha` | admin_tools (caja) | 657,383 | No (solo INSERT con now()) | NO indexar |
| 2 | `detalle_factura_compra.fecha_venc` | admin_tools | 18,677 | No | NO indexar |
| 3 | `cuentas_bancos.fecha` | admin_tools | 6,890 | No (ORDER por codigo PK) | NO indexar |
| 4 | `encabezado_factura_compra.fecha_ingreso` | admin_tools | 6,030 | No | NO indexar |
| 5 | `rutas_entregas.fecha` | admin_tools | 4,516 | No | NO indexar |
| 6 | `cuentas_por_cobrar_facturas.fecha` | admin_tools | 2,657 | No (filtran por codigo_cuenta) | NO indexar |
| 7 | `cierre_caja.fecha_final/inicio` | admin_tools | 444 | No | NO indexar |
| 8 | `detalle_devoluciones_compra.fecha` | admin_tools | 191 | No | NO indexar |
| 9 | `datos_factura.fecha_limite_emision` | caja | 1 | No | NO indexar |
| 10 | `encabezado_factura.fecha_vencimiento` | admin_tools | 0 | No (caja_2 ya tiene el índice) | NO indexar |
| 11 | `kardex.fecha` | admin_tools | 0 | No | NO indexar |
| 12 | `pagos_creditos.fecha_pago` | admin_tools | 0 | No | NO indexar |
| 13 | `precios_programados.fecha` | admin_tools | 0 | No | NO indexar |
| 14 | `salida_productos.fecha` | admin_tools | 0 | No | NO indexar |
| - | `Prueba_1.Fecha` | caja_2 | - | Tabla huérfana de prueba | Ignorar |

### Conclusión

**Ninguna de las 15 candidatas amerita índice.** El patrón identificado:
las columnas de fecha se escriben (INSERT/UPDATE) y se leen para mostrar
en pantalla, pero **nunca se usan como filtro u ordenamiento** en
consultas. Los reportes que sí filtran por fecha (cierre diario,
movimientos por período) atacan columnas `fecha` que **ya tienen
índice** según el Bloque 3.

**No se crea migración V12 vacía.** US-006 se cierra como _verificada_:
el criterio de aceptación "reportes de fecha más rápidos" y "EXPLAIN
muestra uso de índices" ya está cumplido por los índices preexistentes
(probablemente añadidos por una migración no documentada o por V10).

## Bloque 3 — Columnas de fecha ya indexadas (referencia)

### `admin_tools` (17 índices existentes)

```
cierre_caja.fecha                          → idx_cierre_fecha
cuentas_facturas.fecha                     → idx_cuentas_fact_fecha
cuentas_facturas.fecha_vencimiento         → idx_cuentas_fact_fecha_venc
cuentas_por_cobrar.fecha                   → idx_cxc_fecha
cuentas_por_pagar.fecha                    → idx_cxp_fecha
detalle_devoluciones.fecha                 → idx_det_devol_fecha
encabezado_cotizacion.fecha                → idx_enc_cotiz_fecha
encabezado_factura.fecha                   → idx_enc_fact_fecha
encabezado_factura_compra.fecha            → idx_enc_fact_compra_fecha
encabezado_factura_compra.fecha_vencimiento → idx_enc_fact_compra_fecha_venc
encabezado_factura_temp.fecha              → idx_enc_fact_temp_fecha
encabezado_requisicion.fecha               → idx_enc_requi_fecha
entradas_caja.fecha                        → idx_entradas_caja_fecha
movimientos_bancos.fecha                   → idx_mov_bancos_fecha
recibo_pago.fecha                          → idx_recibo_pago_fecha
recibo_pago_proveedores.fecha              → idx_recibo_pago_prov_fecha
salidas_caja.fecha                         → idx_salidas_caja_fecha
```

### `admin_tools_caja_2` (2 índices existentes)

```
encabezado_factura.fecha             → idx_enc_fact_fecha
encabezado_factura.fecha_vencimiento → idx_enc_fact_fecha_venc
```

## Deriva de esquema detectada

`admin_tools_caja_2` tiene `idx_enc_fact_fecha_venc` sobre
`encabezado_factura.fecha_vencimiento` pero `admin_tools` no. Se acepta
la deriva: `admin_tools` es legacy/caja_1 y ya no se factura activamente
desde ahí. No se alinea el esquema en esta US.

## Decisión sobre numeración Flyway

El CSV original pedía "V2" para US-005 y "V3" para US-006. Ambos números
ya estaban ocupados:

- `V2__remote_server_schema.sql` (alinear esquema con Sharon)
- `V3__alter_columns_sharon.java` (migración Java)

Se usa **V11** para US-005. US-006 queda sin migración propia.
