-- =====================================================================
-- V20 — INV-CC fase 2: replicar el patrón a los 6 SPs restantes del kardex
--        + completar el mantenimiento de existencia_articulo_bodega (INV-2)
--
-- Reescribe con el mismo patrón validado en V19 (ver docs/inventario-api-design.md
-- §9 en el repo admin-tools-api):
--
--   crear_compa_kardex                  (entrada: compra)
--   crear_inventario_inicial_kardex     (entrada: primer movimiento, sin saldo previo)
--   crear_dev_venta_kardex              (entrada: devolución de cliente)
--   crear_dev_compa_kardex              (salida:  devolución a proveedor)
--   crear_requisicion_entrada_kardex    (entrada: requisición destino)
--   crear_requisicion_salida_kardex    (salida:  requisición origen)
--
-- Patrón (igual que V19 para crear_venta_kardex, sin SIGNAL):
--   1) Header lock (FOR UPDATE sobre articulo_kardex) — serializa
--      movimientos por (articulo, bodega) + resuelve articulo+bodega
--      para el UPSERT al final.
--   2) (Si tiene saldo previo) Lectura consolidada del saldo con FOR UPDATE
--      — current read que bypassa snapshot REPEATABLE READ del trigger.
--      crear_inventario_inicial_kardex NO lleva esta lectura (es el primer
--      movimiento, no hay saldo viejo).
--   3) Lógica original preservada exacta (matemática, tipos de movimiento,
--      descripciones, quirks históricos como tipo 2 en dev_venta y tipo 1
--      en dev_compa con precio negativo).
--   4) UPSERT a existencia_articulo_bodega — mantiene la tabla materializada
--      sincronizada en la misma transacción del kardex.
--
-- Ningún SP de V20 lleva SIGNAL (solo crear_venta_kardex tiene la regla
-- de sobreventa, en V19).
--
-- Post-V20: TODOS los crear_*_kardex mantienen existencia_articulo_bodega
-- → la tabla queda completamente confiable como fuente de lectura del
-- stock → el API puede empezar a leer de ella (historia INV-1).
--
-- Idempotente: DROP IF EXISTS + CREATE para cada SP.
-- =====================================================================

DELIMITER $$

-- ---------------------------------------------------------------------
-- crear_compa_kardex: ENTRADA por compra. tipo 1.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_compa_kardex$$

CREATE PROCEDURE crear_compa_kardex(
    IN p_cod_kardex   INT,
    IN p_no_factura   INT,
    IN p_cantidad     FLOAT,
    IN p_precio_comp  FLOAT
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE existencia_old    FLOAT DEFAULT 0;
    DECLARE total_old         FLOAT DEFAULT 0;
    DECLARE precio_old        FLOAT DEFAULT 0;
    DECLARE valor_total_saldo FLOAT;
    DECLARE newExistencia     FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM articulo_kardex ak
    JOIN detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE ak.codigo_kardex = p_cod_kardex AND mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC LIMIT 1
    FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'Compra de productos', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 1, p_cantidad, p_precio_comp, (p_cantidad * p_precio_comp));

    SET valor_total_saldo = total_old + (p_cantidad * p_precio_comp);
    SET newExistencia     = existencia_old + p_cantidad;

    IF newExistencia > 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, (valor_total_saldo / newExistencia), valor_total_saldo);
    ELSEIF newExistencia = 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, 0, 0, 0);
    ELSE
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, valor_total_saldo);
    END IF;

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

-- ---------------------------------------------------------------------
-- crear_inventario_inicial_kardex: PRIMER movimiento (sin saldo previo).
-- No necesita la lectura del saldo viejo; sí necesita el header lock.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_inventario_inicial_kardex$$

CREATE PROCEDURE crear_inventario_inicial_kardex(
    IN p_cod_kardex   INT,
    IN p_cantidad     FLOAT,
    IN p_precio_comp  FLOAT,
    IN p_referencia   VARCHAR(20)
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE valor_total_saldo FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'Inventario inicial', p_referencia);
    SET cod_movimiento = LAST_INSERT_ID();

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 1, p_cantidad, p_precio_comp, (p_cantidad * p_precio_comp));

    SET valor_total_saldo = (p_cantidad * p_precio_comp);

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 3, p_cantidad, (valor_total_saldo / p_cantidad), valor_total_saldo);

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, p_cantidad)
    ON DUPLICATE KEY UPDATE cantidad = p_cantidad;
END$$

-- ---------------------------------------------------------------------
-- crear_dev_venta_kardex: ENTRADA por devolución de venta.
-- Quirk histórico: tipo_movimiento = 2 aunque sube stock; precio *-1; ELSE total=0.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_dev_venta_kardex$$

CREATE PROCEDURE crear_dev_venta_kardex(
    IN p_cod_kardex   INT,
    IN p_no_factura   INT,
    IN p_cantidad     FLOAT,
    IN p_precio_fact  FLOAT
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE existencia_old    FLOAT DEFAULT 0;
    DECLARE total_old         FLOAT DEFAULT 0;
    DECLARE precio_old        FLOAT DEFAULT 0;
    DECLARE valor_total_saldo FLOAT;
    DECLARE newExistencia     FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM articulo_kardex ak
    JOIN detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE ak.codigo_kardex = p_cod_kardex AND mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC LIMIT 1
    FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'Devolucion sobre venta', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    -- quirk histórico: tipo 2 (salida) con total negativo para señalar devolución
    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 2, p_cantidad, p_precio_fact, (p_cantidad * p_precio_fact * -1));

    SET valor_total_saldo = total_old - (p_cantidad * p_precio_fact * -1);
    SET newExistencia     = existencia_old + p_cantidad;

    IF newExistencia > 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, (valor_total_saldo / newExistencia), valor_total_saldo);
    ELSEIF newExistencia = 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, 0, 0, 0);
    ELSE
        -- quirk histórico: total=0 (NO valor_total_saldo) en negativo
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, 0);
    END IF;

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

-- ---------------------------------------------------------------------
-- crear_dev_compa_kardex: SALIDA por devolución a proveedor.
-- Quirk histórico: tipo_movimiento = 1 aunque baja stock; precio *-1; ELSE total=0.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_dev_compa_kardex$$

CREATE PROCEDURE crear_dev_compa_kardex(
    IN p_cod_kardex   INT,
    IN p_no_factura   INT,
    IN p_cantidad     FLOAT,
    IN p_precio_comp  FLOAT
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE existencia_old    FLOAT DEFAULT 0;
    DECLARE total_old         FLOAT DEFAULT 0;
    DECLARE precio_old        FLOAT DEFAULT 0;
    DECLARE valor_total_saldo FLOAT;
    DECLARE newExistencia     FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM articulo_kardex ak
    JOIN detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE ak.codigo_kardex = p_cod_kardex AND mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC LIMIT 1
    FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'Devolucion sobre compra', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    -- quirk histórico: tipo 1 (entrada) con total negativo para señalar devolución
    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 1, p_cantidad, p_precio_comp, (p_cantidad * p_precio_comp * -1));

    SET valor_total_saldo = total_old + (p_cantidad * p_precio_comp * -1);
    SET newExistencia     = existencia_old - p_cantidad;

    IF newExistencia > 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, (valor_total_saldo / newExistencia), valor_total_saldo);
    ELSEIF newExistencia = 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, 0, 0, 0);
    ELSE
        -- quirk histórico: total=0 (NO valor_total_saldo) en negativo
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, 0);
    END IF;

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

-- ---------------------------------------------------------------------
-- crear_requisicion_entrada_kardex: ENTRADA al destino. tipo 1.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_requisicion_entrada_kardex$$

CREATE PROCEDURE crear_requisicion_entrada_kardex(
    IN p_cod_kardex   INT,
    IN p_no_factura   INT,
    IN p_cantidad     FLOAT,
    IN p_precio_comp  FLOAT
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE existencia_old    FLOAT DEFAULT 0;
    DECLARE total_old         FLOAT DEFAULT 0;
    DECLARE precio_old        FLOAT DEFAULT 0;
    DECLARE valor_total_saldo FLOAT;
    DECLARE newExistencia     FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM articulo_kardex ak
    JOIN detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE ak.codigo_kardex = p_cod_kardex AND mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC LIMIT 1
    FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'requisicion de producto', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 1, p_cantidad, p_precio_comp, (p_cantidad * p_precio_comp));

    SET valor_total_saldo = total_old + (p_cantidad * p_precio_comp);
    SET newExistencia     = existencia_old + p_cantidad;

    IF newExistencia > 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, (valor_total_saldo / newExistencia), valor_total_saldo);
    ELSEIF newExistencia = 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, 0, 0, 0);
    ELSE
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, valor_total_saldo);
    END IF;

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

-- ---------------------------------------------------------------------
-- crear_requisicion_salida_kardex: SALIDA del origen. tipo 2. usa precio_old.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crear_requisicion_salida_kardex$$

CREATE PROCEDURE crear_requisicion_salida_kardex(
    IN p_cod_kardex   INT,
    IN p_no_factura   INT,
    IN p_cantidad     FLOAT
)
BEGIN
    DECLARE v_articulo        INT;
    DECLARE v_bodega          INT;
    DECLARE cod_movimiento    INT;
    DECLARE existencia_old    FLOAT DEFAULT 0;
    DECLARE total_old         FLOAT DEFAULT 0;
    DECLARE precio_old        FLOAT DEFAULT 0;
    DECLARE valor_total_saldo FLOAT;
    DECLARE newExistencia     FLOAT;

    SELECT codigo_articulo, codigo_bodega INTO v_articulo, v_bodega
    FROM articulo_kardex WHERE codigo_kardex = p_cod_kardex FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM articulo_kardex ak
    JOIN detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE ak.codigo_kardex = p_cod_kardex AND mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC LIMIT 1
    FOR UPDATE;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'requisicion de articulo', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 2, p_cantidad, precio_old, (p_cantidad * precio_old));

    SET valor_total_saldo = total_old - (p_cantidad * precio_old);
    SET newExistencia     = existencia_old - p_cantidad;

    IF newExistencia > 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, (valor_total_saldo / newExistencia), valor_total_saldo);
    ELSEIF newExistencia = 0 THEN
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, 0, 0, 0);
    ELSE
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, valor_total_saldo);
    END IF;

    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

DELIMITER ;
