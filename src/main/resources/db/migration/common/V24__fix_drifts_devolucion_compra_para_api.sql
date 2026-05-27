-- =====================================================================
-- V24 — Fix de drifts en detalle_devoluciones_compra para que la API arranque
--
-- Detectado al planear INV-7 (PurchaseReturnCtl):
--   1) codigo_articulo es VARCHAR(45) — deberia ser INT como en el resto
--      del modelo. El trigger detalle_devolucion_compra_b_i hace
--      comparaciones implicitas int↔string que aun asi funcionan, pero
--      la entity JPA del API usa Integer.
--   2) precio/cantidad/impuesto/subtotal/descuento/total son float —
--      hay que pasarlos a DECIMAL(38,2) igual que en V21/V22/V23.
--
-- Verificado en admin_tools: 5 filas, codigo_articulo SIEMPRE numerico,
-- conversion segura. Patron idempotente identico a V21/V22/V23.
--
-- (dev_compra es una tabla de log denormalizada sin PK ni trigger; no
-- la tocamos. El API NO la usa.)
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS v24_alter_si_tipo_difiere$$
CREATE PROCEDURE v24_alter_si_tipo_difiere(
    IN p_tabla    VARCHAR(64),
    IN p_columna  VARCHAR(64),
    IN p_esperado VARCHAR(64),
    IN p_alter    TEXT
)
BEGIN
    DECLARE v_actual VARCHAR(64) DEFAULT NULL;
    SELECT COLUMN_TYPE INTO v_actual
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_tabla AND COLUMN_NAME = p_columna;
    IF v_actual IS NOT NULL AND v_actual <> p_esperado THEN
        SET @sql = p_alter;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- (1) codigo_articulo varchar → INT (datos verificados numericos)
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','codigo_articulo','int','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN codigo_articulo INT NOT NULL DEFAULT 0');

-- (2) Floats → DECIMAL(38,2)
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','precio',   'decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN precio    DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','cantidad', 'decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN cantidad  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','impuesto', 'decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN impuesto  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','subtotal', 'decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN subtotal  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','descuento','decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN descuento DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v24_alter_si_tipo_difiere('detalle_devoluciones_compra','total',    'decimal(38,2)','ALTER TABLE detalle_devoluciones_compra MODIFY COLUMN total     DECIMAL(38,2) NOT NULL DEFAULT 0');

DROP PROCEDURE v24_alter_si_tipo_difiere;
