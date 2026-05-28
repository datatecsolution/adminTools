-- =====================================================================
-- V25 — INV-9: ajustes de inventario como compras a proveedores ficticios
--
-- DECISION ARQUITECTONICA: los ajustes de inventario por SOBRANTE
-- (físico > sistema) se modelan como "compras" a proveedores ficticios.
-- Eso reusa todo el flujo de INV-5 (validacion, trigger
-- detalle_compra_b_inset, crear_compa_kardex con fix V20) sin crear
-- infraestructura nueva. Los faltantes ya tienen su ruta separada:
-- requisicion a la bodega "Pérdidas" (INV-6).
--
-- Simetria del modelo:
--   FALTANTE  -> POST /requisitions      destino=Pérdidas (bodega ficticia)
--   SOBRANTE  -> POST /purchases         supplier=Donación/Sobrante (proveedor ficticio)
--   INICIAL   -> POST /purchases         supplier=Inventario inicial (proveedor ficticio)
--
-- Esta V25 hace:
--   1) Agregar columna proveedor.es_ajuste TINYINT(1) NOT NULL DEFAULT 0.
--      Permite al frontend filtrar proveedores reales (es_ajuste=0) vs
--      proveedores de ajuste (es_ajuste=1) en sus selectores sin
--      hardcodear IDs. Idempotente via helper SP.
--
--   2) Marcar el proveedor 1 (Inventario inicial), que ya existe del
--      baseline, con es_ajuste=1.
--
--   3) Sembrar 2 proveedores nuevos para los otros casos comunes
--      ("Sobrante conteo físico" y "Donación recibida") con es_ajuste=1.
--      INSERT condicional por nombre (proveedor.nombre_proveedor NO tiene
--      UNIQUE), seguro de re-correr.
--
-- POSTCONDICION: cualquier proveedor con es_ajuste=1 implica que las
-- "compras" hechas contra el son ajustes de inventario, no compras
-- reales. NO se generan cuentas por pagar automaticamente para ninguna
-- compra (verificado: ni crear_compa_kardex ni trigger toca CXP).
-- =====================================================================

-- (1) Helper SP: agrega columna solo si no existe (idempotente).
DROP PROCEDURE IF EXISTS v25_add_col_si_falta;

DELIMITER $$

CREATE PROCEDURE v25_add_col_si_falta(
    IN p_tabla    VARCHAR(64),
    IN p_columna  VARCHAR(64),
    IN p_alter    VARCHAR(500)
)
BEGIN
    DECLARE v_existe INT DEFAULT 0;
    SELECT COUNT(*) INTO v_existe
    FROM   information_schema.COLUMNS
    WHERE  TABLE_SCHEMA = DATABASE()
      AND  TABLE_NAME   = p_tabla
      AND  COLUMN_NAME  = p_columna;

    IF v_existe = 0 THEN
        SET @s = p_alter;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- (2) Agregar columna es_ajuste a proveedor.
CALL v25_add_col_si_falta(
    'proveedor',
    'es_ajuste',
    'ALTER TABLE proveedor ADD COLUMN es_ajuste TINYINT(1) NOT NULL DEFAULT 0'
);

DROP PROCEDURE v25_add_col_si_falta;

-- (3) Marcar "Inventario inicial" (preexistente, codigo_proveedor=1) como
--     proveedor de ajuste. Filtramos por nombre tambien para no marcar
--     accidentalmente un proveedor real que tenga codigo=1 en clientes
--     viejos con baseline distinto.
UPDATE proveedor
SET    es_ajuste = 1
WHERE  codigo_proveedor = 1
  AND  nombre_proveedor = 'Inventario inicial';

-- (4) Sembrar los dos proveedores ficticios faltantes. Idempotente por
--     nombre porque nombre_proveedor NO tiene UNIQUE en el esquema.
INSERT INTO proveedor (nombre_proveedor, telefono, celular, direccion, es_ajuste)
SELECT 'Sobrante conteo físico', 'NA', 'NA', 'NA', 1
WHERE NOT EXISTS (
    SELECT 1 FROM proveedor WHERE nombre_proveedor = 'Sobrante conteo físico'
);

INSERT INTO proveedor (nombre_proveedor, telefono, celular, direccion, es_ajuste)
SELECT 'Donación recibida', 'NA', 'NA', 'NA', 1
WHERE NOT EXISTS (
    SELECT 1 FROM proveedor WHERE nombre_proveedor = 'Donación recibida'
);
