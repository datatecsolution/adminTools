-- =====================================================================
-- V13 — float a DECIMAL(15,2) en columnas monetarias de
-- encabezado_factura y detalle_factura
--
-- US-014 del sprint de remediacion BD (lote 1 — facturas).
--
-- Por que: los descuadres por redondeo en reportes financieros vienen
-- de almacenar montos en float (IEEE 754, binario, no representa
-- exactamente decimales como 0.10). DECIMAL es base 10, sin perdida.
--
-- Alcance de este lote: solo las 2 tablas de facturacion final
-- (encabezado_factura + detalle_factura). Lotes futuros cubriran
-- kardex, cuentas_por_cobrar, recibos, etc.
--
-- Precision = 15, scale = 2: rango [-9.999.999.999.999,99 a
-- +9.999.999.999.999,99]. Verificado con docs/auditoria-decimal-pre-v13.sql
-- bloque C que ningun valor productivo lo excede.
--
-- Columnas excluidas intencionalmente:
--   - detalle_factura.cantidad: no es monetario (unidades fisicas;
--     en algunos casos litros/kg con > 2 decimales).
--
-- Idempotencia: ALTER TABLE ... MODIFY es self-idempotent — si la
-- columna ya es DECIMAL(15,2), MySQL no rehace la conversion. Si esta
-- en float(8,2), la convierte preservando valores (con redondeo a
-- 2 decimales, deseado).
--
-- Riesgo:
--   - Lock de tabla durante ALTER. Tablas no chicas (encabezado_factura
--     potencialmente cientos de miles de filas) pueden tardar minutos.
--     Programar en ventana de mantenimiento si el volumen es alto.
--   - Si ALGUN valor excede la precision (bloque C de auditoria), el
--     ALTER falla. Por eso correr la auditoria PRIMERO.
-- =====================================================================

-- encabezado_factura — 12 columnas monetarias
ALTER TABLE `encabezado_factura`
    MODIFY `subtotal_excento` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal15`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal18`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `impuesto`         DECIMAL(15,2) NOT NULL,
    MODIFY `total`            DECIMAL(15,2) NOT NULL,
    MODIFY `isvOtros`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `isv18`            DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `pago`             DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `descuento`        DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `cobro_tarjeta`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `cobro_efectivo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00;

-- detalle_factura — 5 columnas monetarias (cantidad se queda float, no es monetaria)
ALTER TABLE `detalle_factura`
    MODIFY `precio`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `impuesto`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `descuento` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total`     DECIMAL(15,2) NOT NULL DEFAULT 0.00;
