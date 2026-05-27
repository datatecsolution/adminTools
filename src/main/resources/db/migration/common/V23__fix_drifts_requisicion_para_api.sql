-- =====================================================================
-- V23 — Fix de drifts en tablas de requisicion para que la API arranque
--
-- Detectado al planear INV-6 (RequisitionCtl). Dos clases de problema:
--
-- 1) detalle_requisicion NO tiene PRIMARY KEY. JPA requiere @Id. Hay
--    que agregar id_detalle_requisicion INT AUTO_INCREMENT PRIMARY KEY.
--    (detalle_factura_compra ya tiene id_detalle_compra; replicamos.)
--
-- 2) Drifts de tipo (float → DECIMAL, varchar(45) → varchar(255)) para
--    que las entidades JPA con BigDecimal y String validen contra el
--    esquema. Patron idempotente identico a V21/V22.
-- =====================================================================

DELIMITER $$

-- helper: agrega columna PRI si no existe (segun nombre)
DROP PROCEDURE IF EXISTS v23_add_pk_si_falta$$
CREATE PROCEDURE v23_add_pk_si_falta(
    IN p_tabla   VARCHAR(64),
    IN p_columna VARCHAR(64),
    IN p_alter   TEXT
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_tabla AND COLUMN_NAME = p_columna;
    IF v_exists = 0 THEN
        SET @sql = p_alter;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

-- helper: ALTER tipo si el actual difiere del esperado
DROP PROCEDURE IF EXISTS v23_alter_si_tipo_difiere$$
CREATE PROCEDURE v23_alter_si_tipo_difiere(
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
-- (1) Agregar id_detalle_requisicion como PRIMARY KEY auto_increment.
--     MySQL asigna valores secuenciales a las filas existentes.
-- ---------------------------------------------------------------------
CALL v23_add_pk_si_falta(
    'detalle_requisicion',
    'id_detalle_requisicion',
    'ALTER TABLE detalle_requisicion ADD COLUMN id_detalle_requisicion INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST'
);

-- ---------------------------------------------------------------------
-- (2) Drifts de tipo (float → DECIMAL, varchar(45) → varchar(255))
-- ---------------------------------------------------------------------
CALL v23_alter_si_tipo_difiere('encabezado_requisicion', 'total',   'decimal(38,2)', 'ALTER TABLE encabezado_requisicion MODIFY COLUMN total   DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v23_alter_si_tipo_difiere('encabezado_requisicion', 'usuario', 'varchar(255)',  'ALTER TABLE encabezado_requisicion MODIFY COLUMN usuario VARCHAR(255) NOT NULL');

CALL v23_alter_si_tipo_difiere('detalle_requisicion',    'cantidad',      'decimal(38,2)', 'ALTER TABLE detalle_requisicion MODIFY COLUMN cantidad      DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v23_alter_si_tipo_difiere('detalle_requisicion',    'precio_unidad', 'decimal(38,2)', 'ALTER TABLE detalle_requisicion MODIFY COLUMN precio_unidad DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v23_alter_si_tipo_difiere('detalle_requisicion',    'total',         'decimal(38,2)', 'ALTER TABLE detalle_requisicion MODIFY COLUMN total         DECIMAL(38,2) NOT NULL DEFAULT 0');

DROP PROCEDURE v23_add_pk_si_falta;
DROP PROCEDURE v23_alter_si_tipo_difiere;
