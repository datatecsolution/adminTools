-- =====================================================================
-- V21 — Fix de drifts detectados en admin_tools al arrancar el API
--
-- Detectado al pointear la API admintools-api a admin_tools local con
-- spring.jpa.hibernate.ddl-auto=validate el 2026-05-26:
--
--   Schema-validation: wrong column type encountered in column [estado]
--   in table [articulo_view]; found [int (Types#INTEGER)], but
--   expecting [bit (Types#BOOLEAN)]
--
-- La entidad Articulo del API tiene @Column(name="estado") Boolean estado;
-- Hibernate espera tinyint(1)/bit. admin_tools tiene int. PacRoc_backup
-- ya tenia bit(1) (probablemente ALTER manual historico, igual que el
-- caso authorities.username de V14).
--
-- Patron de fix igual que V14/V15/V16: procedure idempotente que
-- verifica el tipo actual y solo modifica si esta mal. Asi clientes con
-- el tipo correcto skipean. Sin perdida de datos: los valores int 0/1
-- caben en tinyint(1) sin cambio semantico.
--
-- Tambien recrea articulo_view porque la vista hereda el tipo de la
-- tabla; al cambiar articulo.estado a tinyint(1), la vista refleja
-- automaticamente.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS v21_fix_articulo_estado_to_boolean$$

CREATE PROCEDURE v21_fix_articulo_estado_to_boolean()
BEGIN
    DECLARE v_type VARCHAR(64) DEFAULT NULL;

    SELECT COLUMN_TYPE INTO v_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'articulo'
      AND COLUMN_NAME  = 'estado';

    -- solo si NO es tinyint(1) (y existe la columna)
    IF v_type IS NOT NULL AND v_type <> 'tinyint(1)' THEN
        ALTER TABLE articulo MODIFY COLUMN estado TINYINT(1) NOT NULL DEFAULT 1;

        -- recrear articulo_view para que herede el nuevo tipo
        DROP VIEW IF EXISTS articulo_view;
        CREATE VIEW articulo_view AS
        SELECT articulo.codigo_articulo, articulo.articulo, articulo.codigo_marca,
               articulo.cod_articulo, articulo.codigo_impuesto, articulo.precio_articulo,
               articulo.tipo_articulo, articulo.estado,
               IFNULL(f_existencia_y_ordenes(articulo.codigo_articulo, 1), 0) AS existencia
        FROM articulo
        WHERE articulo.estado = 1;
    END IF;
END$$

DELIMITER ;

CALL v21_fix_articulo_estado_to_boolean();
DROP PROCEDURE v21_fix_articulo_estado_to_boolean;


-- ---------------------------------------------------------------------
-- Fix 2: cliente.limite_credito — float → DECIMAL(38,2)
-- La entidad Cliente del API tiene BigDecimal sin precision/scale →
-- Hibernate espera DECIMAL(38,2). admin_tools tiene float; PacRoc_backup
-- ya tenia DECIMAL(38,2).
-- ---------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS v21_fix_cliente_limite_credito_to_decimal$$

CREATE PROCEDURE v21_fix_cliente_limite_credito_to_decimal()
BEGIN
    DECLARE v_type VARCHAR(64) DEFAULT NULL;

    SELECT COLUMN_TYPE INTO v_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'cliente'
      AND COLUMN_NAME  = 'limite_credito';

    IF v_type IS NOT NULL AND v_type <> 'decimal(38,2)' THEN
        ALTER TABLE cliente MODIFY COLUMN limite_credito DECIMAL(38,2) NOT NULL DEFAULT 0.00;
    END IF;
END$$

DELIMITER ;

CALL v21_fix_cliente_limite_credito_to_decimal();
DROP PROCEDURE v21_fix_cliente_limite_credito_to_decimal;


-- ---------------------------------------------------------------------
-- Fix 3..N: drifts varios de varchar/decimal en tablas que la API mapea
-- (cliente.movil/rtn/telefono, empleados.sueldo_base, usuario.usuario)
-- Helper SP generico que recibe (tabla, columna, tipo_esperado, alter_sql)
-- y solo aplica el ALTER si el tipo actual difiere.
-- ---------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS v21_alter_si_tipo_difiere$$

CREATE PROCEDURE v21_alter_si_tipo_difiere(
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

CALL v21_alter_si_tipo_difiere('cliente',   'movil',       'varchar(255)', 'ALTER TABLE cliente   MODIFY COLUMN movil       VARCHAR(255)');
CALL v21_alter_si_tipo_difiere('cliente',   'rtn',         'varchar(255)', 'ALTER TABLE cliente   MODIFY COLUMN rtn         VARCHAR(255)');
CALL v21_alter_si_tipo_difiere('cliente',   'telefono',    'varchar(255)', 'ALTER TABLE cliente   MODIFY COLUMN telefono    VARCHAR(255)');
CALL v21_alter_si_tipo_difiere('empleados', 'sueldo_base', 'decimal(38,2)','ALTER TABLE empleados MODIFY COLUMN sueldo_base DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v21_alter_si_tipo_difiere('usuario',   'usuario',     'varchar(255)', 'ALTER TABLE usuario   MODIFY COLUMN usuario     VARCHAR(255)');

-- detalle_factura_temp: la entidad DetalleFacturaTemp del API usa BigDecimal
-- para cantidad/precio/subtotal/impuesto/descuento/total. admin_tools tiene
-- floats heredados (la encabezado/detalle "definitivos" ya fueron a DECIMAL
-- en V13 pero los _temp se quedaron atras).
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'cantidad',  'decimal(38,2)', 'ALTER TABLE detalle_factura_temp MODIFY COLUMN cantidad  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'descuento', 'decimal(38,2)', 'ALTER TABLE detalle_factura_temp MODIFY COLUMN descuento DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'impuesto',  'decimal(38,2)', 'ALTER TABLE detalle_factura_temp MODIFY COLUMN impuesto  DECIMAL(38,2) NOT NULL DEFAULT 0');
-- detalle_factura_temp.precio es Double en la entity (no BigDecimal) → DOUBLE
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'precio',    'double',        'ALTER TABLE detalle_factura_temp MODIFY COLUMN precio    DOUBLE NOT NULL DEFAULT 0');

-- impuesto.porcentaje es String en la entity Impuesto (probablemente para formato "%")
CALL v21_alter_si_tipo_difiere('impuesto', 'porcentaje', 'varchar(255)', 'ALTER TABLE impuesto MODIFY COLUMN porcentaje VARCHAR(255)');

-- precios_articulos.precio_articulo: BigDecimal en entity PrecioArticulo
CALL v21_alter_si_tipo_difiere('precios_articulos', 'precio_articulo', 'decimal(38,2)', 'ALTER TABLE precios_articulos MODIFY COLUMN precio_articulo DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'subtotal',  'decimal(38,2)', 'ALTER TABLE detalle_factura_temp MODIFY COLUMN subtotal  DECIMAL(38,2) NOT NULL DEFAULT 0');
CALL v21_alter_si_tipo_difiere('detalle_factura_temp', 'total',     'decimal(38,2)', 'ALTER TABLE detalle_factura_temp MODIFY COLUMN total     DECIMAL(38,2) NOT NULL DEFAULT 0');

DROP PROCEDURE v21_alter_si_tipo_difiere;
