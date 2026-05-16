-- =====================================================================
-- Auditoria de columnas float/double en encabezado_factura y
-- detalle_factura previa a la migracion V13 (US-014).
--
-- Ejecutar en cada esquema productivo (admin_tools, admin_tools_caja_*)
-- antes de aplicar V13. Permite confirmar:
--   - Que las columnas siguen siendo float (no fueron migradas ya por
--     algun proceso historico).
--   - Que ningun valor excede DECIMAL(15,2) (precision = 15 digitos
--     totales, scale = 2 decimales → rango [-9.999.999.999.999,99
--     a +9.999.999.999.999,99]).
--   - Que no hay valores con > 2 decimales que se vayan a redondear
--     al convertir (esto es la firma del descuadre por float).
--
-- Es 100% solo-lectura. Seguro contra produccion.
--
-- Uso:
--   mysql -u <user> -p <esquema> < auditoria-decimal-pre-v13.sql
-- =====================================================================

SET @db := DATABASE();

-- ---------------------------------------------------------------------
-- Bloque A — Inventario de columnas a migrar
-- ---------------------------------------------------------------------
-- Lista las columnas monetarias que V13 va a tocar, con su tipo actual.
-- Si una columna ya esta en DECIMAL, V13 la deja sin cambios.
SELECT
    TABLE_NAME              AS tabla,
    COLUMN_NAME             AS columna,
    DATA_TYPE               AS tipo,
    NUMERIC_PRECISION       AS num_precision,
    NUMERIC_SCALE           AS num_scale,
    COLUMN_DEFAULT          AS valor_default
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND (
        (TABLE_NAME = 'encabezado_factura' AND COLUMN_NAME IN (
            'subtotal_excento','subtotal15','subtotal18','subtotal',
            'impuesto','total','isvOtros','isv18',
            'pago','descuento','cobro_tarjeta','cobro_efectivo'))
        OR
        (TABLE_NAME = 'detalle_factura' AND COLUMN_NAME IN (
            'precio','impuesto','subtotal','descuento','total'))
      )
ORDER BY TABLE_NAME, ORDINAL_POSITION;


-- ---------------------------------------------------------------------
-- Bloque B — Rangos de valores actuales (para validar precision)
-- ---------------------------------------------------------------------
-- DECIMAL(15,2) acepta valores con hasta 13 digitos enteros y 2 decimales.
-- Si MAX de alguna columna supera 9.999.999.999.999,99 hay que ampliar.
-- Tambien: count() de filas con > 2 decimales (firma del descuadre).
SELECT
    'encabezado_factura' AS tabla,
    'total'              AS columna,
    COUNT(*)             AS filas,
    MIN(total)           AS min_val,
    MAX(total)           AS max_val,
    SUM(CASE WHEN ROUND(total, 2) <> total THEN 1 ELSE 0 END) AS filas_con_descuadre
FROM encabezado_factura
UNION ALL
SELECT 'encabezado_factura','subtotal',COUNT(*),MIN(subtotal),MAX(subtotal),
    SUM(CASE WHEN ROUND(subtotal, 2) <> subtotal THEN 1 ELSE 0 END)
FROM encabezado_factura
UNION ALL
SELECT 'encabezado_factura','impuesto',COUNT(*),MIN(impuesto),MAX(impuesto),
    SUM(CASE WHEN ROUND(impuesto, 2) <> impuesto THEN 1 ELSE 0 END)
FROM encabezado_factura
UNION ALL
SELECT 'detalle_factura','total',COUNT(*),MIN(total),MAX(total),
    SUM(CASE WHEN ROUND(total, 2) <> total THEN 1 ELSE 0 END)
FROM detalle_factura
UNION ALL
SELECT 'detalle_factura','subtotal',COUNT(*),MIN(subtotal),MAX(subtotal),
    SUM(CASE WHEN ROUND(subtotal, 2) <> subtotal THEN 1 ELSE 0 END)
FROM detalle_factura
UNION ALL
SELECT 'detalle_factura','precio',COUNT(*),MIN(precio),MAX(precio),
    SUM(CASE WHEN ROUND(precio, 2) <> precio THEN 1 ELSE 0 END)
FROM detalle_factura;


-- ---------------------------------------------------------------------
-- Bloque C — Valores fuera de rango de DECIMAL(15,2)
-- ---------------------------------------------------------------------
-- Filas que NO caben en DECIMAL(15,2). Si devuelve algo, hay que
-- aumentar la precision en V13 (probablemente DECIMAL(19,2)).
SELECT 'encabezado_factura' AS tabla, 'total' AS columna,
       numero_factura AS id, total AS valor
FROM encabezado_factura
WHERE ABS(total) >= 10000000000000
UNION ALL
SELECT 'detalle_factura','total',id, total
FROM detalle_factura
WHERE ABS(total) >= 10000000000000;
