-- =====================================================================
-- V22 — Fix de drifts en tablas de compra para que la API arranque
--
-- Detectado al planear INV-5 (PurchaseCtl). Las entidades
-- EncabezadoFacturaCompra y DetalleFacturaCompra del API usan BigDecimal
-- para precio/cantidad/impuesto/subtotal/total/etc. → Hibernate espera
-- DECIMAL(38,2). admin_tools (y probablemente otros clientes viejos)
-- los tienen como float(8,2), heredado de antes de V13.
--
-- Patron idéntico a V21: helper SP idempotente que solo aplica ALTER si
-- el tipo actual difiere. Clientes con los tipos correctos lo saltan.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS v22_alter_si_tipo_difiere$$

CREATE PROCEDURE v22_alter_si_tipo_difiere(
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

-- ---------------------------------------------------------------------
-- encabezado_factura_compra: 8 columnas float → DECIMAL(38,2)
-- ---------------------------------------------------------------------
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','subtotal_excento','decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN subtotal_excento DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','subtotal15',      'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN subtotal15       DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','subtotal18',      'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN subtotal18       DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','subtotal',        'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN subtotal         DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','impuesto',        'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN impuesto         DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','total',           'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN total            DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','isv18',           'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN isv18            DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('encabezado_factura_compra','pago',            'decimal(38,2)','ALTER TABLE encabezado_factura_compra MODIFY COLUMN pago             DECIMAL(38,2) NOT NULL DEFAULT 0');

-- ---------------------------------------------------------------------
-- detalle_factura_compra: 4 columnas float → DECIMAL(38,2)
-- ---------------------------------------------------------------------
CALL v22_alter_si_tipo_difiere('detalle_factura_compra','precio',   'decimal(38,2)','ALTER TABLE detalle_factura_compra MODIFY COLUMN precio   DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('detalle_factura_compra','cantidad', 'decimal(38,2)','ALTER TABLE detalle_factura_compra MODIFY COLUMN cantidad DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('detalle_factura_compra','impuesto', 'decimal(38,2)','ALTER TABLE detalle_factura_compra MODIFY COLUMN impuesto DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v22_alter_si_tipo_difiere('detalle_factura_compra','subtotal', 'decimal(38,2)','ALTER TABLE detalle_factura_compra MODIFY COLUMN subtotal DECIMAL(38,2) NOT NULL DEFAULT 0');

DROP PROCEDURE v22_alter_si_tipo_difiere;
