-- =====================================================================
-- V26 — Fix drifts en detalle_devoluciones (Sale Returns para API)
--
-- Sale Returns expone POST /sale-returns mapeando la tabla
-- detalle_devoluciones via la entidad DetalleDevolucion (BigDecimal).
-- Con ddl-auto=validate falla porque los campos numericos estan en
-- float(8,2) heredados del baseline historico:
--
--   Schema-validation: wrong column type encountered in column
--   [cantidad] in table [detalle_devoluciones]; found
--   [float (Types#REAL)], but expecting [decimal(38,2) (Types#NUMERIC)]
--
-- Patron identico a V21/V22/V23/V24: helper SP idempotente que
-- verifica el tipo actual antes del ALTER. Clientes con tipos ya
-- correctos lo saltan. Sin perdida de datos (float -> decimal preserva
-- valores positivos hasta el limite de precision).
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS v26_alter_si_tipo_difiere$$

CREATE PROCEDURE v26_alter_si_tipo_difiere(
    IN p_tabla    VARCHAR(64),
    IN p_columna  VARCHAR(64),
    IN p_esperado VARCHAR(64),
    IN p_alter    TEXT
)
BEGIN
    DECLARE v_actual VARCHAR(64) DEFAULT NULL;
    SELECT COLUMN_TYPE INTO v_actual
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = p_tabla
      AND COLUMN_NAME  = p_columna;

    IF v_actual IS NOT NULL AND v_actual <> p_esperado THEN
        SET @sql = p_alter;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- detalle_devoluciones: 6 columnas numericas float(8,2) → DECIMAL(38,2).
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'precio',    'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN precio    DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'cantidad',  'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN cantidad  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'impuesto',  'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN impuesto  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'subtotal',  'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN subtotal  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'descuento', 'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN descuento DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v26_alter_si_tipo_difiere('detalle_devoluciones', 'total',     'decimal(38,2)',
    'ALTER TABLE detalle_devoluciones MODIFY COLUMN total     DECIMAL(38,2) NOT NULL');

DROP PROCEDURE v26_alter_si_tipo_difiere;
