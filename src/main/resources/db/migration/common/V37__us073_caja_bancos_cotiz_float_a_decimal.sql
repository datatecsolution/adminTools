-- =====================================================================
-- V37 (common) — US-073: barrido final float → DECIMAL(15,2) del esquema COMÚN
--
-- Fase 1 del cierre Hito 1. Convierte TODAS las columnas float/double base que
-- quedaban en `admin_tools` tras US-070 (kardex), US-071 (CxC) y US-072
-- (precio). Al aplicar esta V37, el esquema común queda con CERO float/double.
--
-- SOLO retype (ALTER ... MODIFY), sin renombrar ni dropear → Swing viejo sigue
-- arrancando (JDBC set/getDouble contra DECIMAL es transparente). Se preservan
-- NOT NULL y los defaults EXACTOS por columna (varias sin default: se dejan sin
-- default). Idempotente. Precisión 15,2 (auditoría: MAX ~665K en cierre_caja;
-- sobra de largo). OJO typos legacy preservados: `saldo_anterio`.
--
-- Alcance (tablas base comunes con float):
--   cierre_caja (17 cols)      cuentas_bancos (3)      cuentas_por_pagar (3)
--   detalle_cotizacion (6)     encabezado_cotizacion (9)
--   detalle_factura (cantidad) detalle_factura_temp (precio)
--   entradas_caja (cantidad)   salidas_caja (cantidad)   insumos (cantidad)
--   movimientos_bancos (cantidad)  recibo_pago_proveedores (3)
--
-- API (espejo): CierreCaja (quitar 17× @JdbcTypeCode(REAL)), EntradaCaja y
--   SalidaCaja (quitar REAL de `cantidad`), DetalleOrden (AÑADIR
--   @JdbcTypeCode(DECIMAL) a `precio` Double — caso inverso). El resto de
--   tablas no tienen entidad en la API (solo las usa el Swing). La entidad
--   tenant DetalleFactura ya es BigDecimal (no cambia) y NO se toca el
--   esquema caja aquí (la `cantidad` por-caja quedó float a propósito en
--   caja/V7 por no ser monetaria; residual documentado en el plan).
-- =====================================================================

ALTER TABLE `cierre_caja`
    MODIFY `efectivo`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `creditos`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `isv15`            DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `isv18`            DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `totalventa`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `totalimpuesto`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `tarjeta`          DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `efectivo_inicial` DECIMAL(15,2) NOT NULL,
    MODIFY `total_isv15`      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_isv18`      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_excento`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_efectivo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_salida`     DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_cobro`      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `efectivo_caja`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_pago`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total_entrada`    DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `cuentas_bancos`
    MODIFY `debito`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `credito` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `cuentas_por_pagar`
    MODIFY `debito`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `credito` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `detalle_cotizacion`
    MODIFY `precio`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `cantidad`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `impuesto`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `descuento` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total`     DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `encabezado_cotizacion`
    MODIFY `subtotal_excento` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal15`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal18`       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `subtotal`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `impuesto`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `total`            DECIMAL(15,2) NOT NULL,
    MODIFY `isvOtros`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `isv18`            DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `descuento`        DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `detalle_factura`
    MODIFY `cantidad` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `detalle_factura_temp`
    MODIFY `precio` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `entradas_caja`
    MODIFY `cantidad` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `salidas_caja`
    MODIFY `cantidad` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `insumos`
    MODIFY `cantidad` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `movimientos_bancos`
    MODIFY `cantidad` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `recibo_pago_proveedores`
    MODIFY `total`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo_anterio` DECIMAL(15,2) NOT NULL,
    MODIFY `saldo`         DECIMAL(15,2) NOT NULL;
