-- =====================================================================
-- V15 — Migrar encabezado_factura_temp.isvOtros de float a DECIMAL(15,2)
--
-- Detectado al correr API admintools con ddl-auto=validate contra Ronal
-- el 2026-05-21. La entidad EncabezadoFacturaTemp mapea isvOtros como
-- BigDecimal (DECIMAL), pero en Ronal quedo en float(10,2).
--
-- Las demas columnas monetarias de encabezado_factura_temp ya estan en
-- DECIMAL(38,2) (probablemente migracion historica). Solo isvOtros
-- quedo afuera. Hay tambien una columna duplicada isv_otros (snake_case)
-- ya en DECIMAL — no la tocamos, V15 solo arregla isvOtros (camelCase
-- que es la que la API consume).
--
-- Idempotente: si ya es decimal, no hace nada.
-- Riesgo: tabla con ~89K filas. ALTER puede tardar 5-30s.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS fix_encabezado_temp_isvOtros$$

CREATE PROCEDURE fix_encabezado_temp_isvOtros()
BEGIN
    DECLARE v_type VARCHAR(20) DEFAULT NULL;

    SELECT DATA_TYPE INTO v_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'encabezado_factura_temp'
      AND COLUMN_NAME  = 'isvOtros';

    IF v_type IS NOT NULL AND v_type <> 'decimal' THEN
        ALTER TABLE encabezado_factura_temp
            MODIFY COLUMN `isvOtros` DECIMAL(15,2) NOT NULL DEFAULT 0.00;
    END IF;
END$$

DELIMITER ;

CALL fix_encabezado_temp_isvOtros();
DROP PROCEDURE fix_encabezado_temp_isvOtros;
