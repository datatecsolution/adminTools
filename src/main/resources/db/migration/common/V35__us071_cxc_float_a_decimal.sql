-- =====================================================================
-- V35 (common) — US-071: float → DECIMAL(15,2) en Cuentas por Cobrar
--
-- Fase 1 del cierre Hito 1. Retype de las columnas monetarias de la CxC de
-- clientes, para eliminar los descuadres por redondeo float en saldos/abonos.
--
-- SOLO retype (ALTER ... MODIFY), sin renombrar ni dropear → Swing viejo
-- sigue arrancando (JDBC-safe). Preserva NOT NULL y defaults exactos.
-- Idempotente. Precisión 15,2 (auditoría: MAX local ~16,7K; sobra).
--
-- Alcance (tablas base con float de CxC):
--   cliente                     (saldo)                     -- float, @Transient en la API
--   cuentas_por_cobrar          (debito, credito, saldo)    -- float(8,2)
--   cuentas_por_cobrar_facturas (debito, credito, saldo)    -- float(8,2)
--   pagos_creditos              (saldo_anterior, pago, saldo) -- float(8,2), no mapeada en API
--   recibo_pago                 (total, saldo_anterio, saldo) -- float(8,2); OJO typo "anterio"
-- API (espejo, quitar @JdbcTypeCode(REAL)): CuentaPorCobrar,
--     CuentaPorCobrarFactura, ReciboPago. cliente.saldo es @Transient
--     (se calcula por f_saldo_cliente) y pagos_creditos no está mapeada.
-- =====================================================================

ALTER TABLE `cliente`
    MODIFY `saldo` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `cuentas_por_cobrar`
    MODIFY `debito`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `credito` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `cuentas_por_cobrar_facturas`
    MODIFY `debito`  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `credito` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo`   DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `pagos_creditos`
    MODIFY `saldo_anterior` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `pago`           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo`          DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `recibo_pago`
    MODIFY `total`         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `saldo_anterio` DECIMAL(15,2) NOT NULL,
    MODIFY `saldo`         DECIMAL(15,2) NOT NULL;
