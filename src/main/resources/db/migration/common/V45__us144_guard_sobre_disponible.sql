-- =====================================================================
-- V45 — US-144: el guard de sobreventa trabaja sobre DISPONIBLE, no
--       sobre la existencia física.
--
-- ORIGEN: el bug de las facturas descuadradas (US-142/143, Sharon, 11
-- facturas entre el 31-jul y el 11-ago) dejó a la vista que el sistema
-- manejaba DOS definiciones distintas de "hay stock":
--
--   - el Swing, al cargar un pedido:  físico − reservado por pedidos
--   - este SP, al facturar:           físico a secas
--
-- Con la segunda, la caja puede vender mercadería que ya está prometida
-- en pedidos de vendedores: se factura, sale del inventario, y cuando el
-- reparto va a entregar el pedido no hay qué entregar. Decisión de
-- negocio (2026-08-12): SIEMPRE se trabaja contra el disponible, o sea
-- respetando lo comprometido en pedidos.
--
-- DISEÑO:
--   - El guard pasa a comparar la cantidad pedida contra
--         disponible = existencia_física − reservado
--     donde `reservado` sale de v_reservado_por_articulo (pedidos en
--     estado 1/2, por artículo y bodega — V41).
--   - newExistencia NO cambia: el kardex sigue registrando el saldo
--     FÍSICO resultante. Lo que cambia es únicamente la condición que
--     decide si la venta se permite.
--   - El mensaje del SIGNAL ahora dice "disponible" y desglosa físico y
--     reservado, para que el cajero entienda por qué se rechaza algo que
--     "sí está en bodega".
--
-- POR QUÉ EL PEDIDO NO SE BLOQUEA A SÍ MISMO: al facturar un pedido, su
-- propia reserva dejaría el disponible en cero y se rechazaría solo. Por
-- eso FacturaDao.registrar() marca el pedido como facturado (estado 3)
-- DENTRO de la misma transacción y ANTES de insertar el detalle: cuando
-- este SP corre, el pedido ya no reserva y `reservado` refleja sólo los
-- OTROS pedidos. Si la factura falla, el rollback devuelve el pedido a
-- activo y su reserva vuelve.
--
-- ALCANCE: aplica a todo lo que factura por el trigger de caja — Swing y
-- POS por igual. En el POS no hay pedido propio que excluir, así que el
-- efecto es el buscado: no puede vender lo comprometido.
--
-- Sigue siendo OPT-IN por usuario: config_user_facturacion.
-- facturar_sin_inventario = 0 bloquea; sin fila → 1 (permitir), que es el
-- comportamiento histórico.
-- =====================================================================

DROP PROCEDURE IF EXISTS `crear_venta_kardex_v2`;

DELIMITER $$

CREATE PROCEDURE `crear_venta_kardex_v2`(
    IN p_cod_kardex INT,
    IN p_no_factura VARCHAR(100),
    IN p_cantidad   DOUBLE,
    IN p_usuario    VARCHAR(255)
)
BEGIN
    DECLARE v_articulo          INT;
    DECLARE v_bodega            INT;
    DECLARE cod_movimiento      INT;
    DECLARE existencia_old      FLOAT DEFAULT 0;
    DECLARE total_old           FLOAT DEFAULT 0;
    DECLARE precio_old          FLOAT DEFAULT 0;
    DECLARE valor_total_saldo   FLOAT;
    DECLARE newExistencia       FLOAT;
    DECLARE v_permite           TINYINT DEFAULT 1;
    -- US-144
    DECLARE v_reservado         FLOAT DEFAULT 0;
    DECLARE v_disponible        FLOAT DEFAULT 0;

    SELECT codigo_articulo, codigo_bodega
    INTO   v_articulo, v_bodega
    FROM   articulo_kardex
    WHERE  codigo_kardex = p_cod_kardex
    FOR UPDATE;

    SELECT mk.cantidad, mk.total, mk.precio_unidad
    INTO   existencia_old, total_old, precio_old
    FROM   articulo_kardex ak
    JOIN   detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex
    JOIN   movimiento_kardex mk          ON dmk.codigo_movimiento = mk.codigo_movimiento
    WHERE  ak.codigo_kardex = p_cod_kardex
      AND  mk.codigo_tipo_movimiento = 3
    ORDER BY dmk.codigo_movimiento DESC
    LIMIT 1
    FOR UPDATE;

    SET v_permite = COALESCE((
        SELECT cu.facturar_sin_inventario
        FROM   config_user_facturacion cu
        WHERE  cu.usuario = p_usuario
        LIMIT 1
    ), 1);

    -- US-144: lo comprometido en pedidos ACTIVOS de esta bodega. El pedido
    -- que se está facturando ya pasó a estado 3, así que no se cuenta.
    SET v_reservado = COALESCE((
        SELECT r.reservado
        FROM   v_reservado_por_articulo r
        WHERE  r.codigo_articulo = v_articulo
          AND  r.codigo_bodega   = v_bodega
        LIMIT 1
    ), 0);

    -- El kardex sigue llevando el saldo FÍSICO...
    SET newExistencia = existencia_old - p_cantidad;
    -- ...pero la venta se autoriza contra el DISPONIBLE.
    SET v_disponible  = existencia_old - v_reservado;

    IF v_permite = 0 AND p_cantidad > v_disponible THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sin disponible: la existencia esta comprometida en pedidos (V45).';
    END IF;

    INSERT INTO detalle_movimiento_kardex(codigo_kardex, fecha, descripcion, no_documento)
        VALUES (p_cod_kardex, NOW(), 'Venta de productos', p_no_factura);
    SET cod_movimiento = LAST_INSERT_ID();

    INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
        VALUES (cod_movimiento, 2, p_cantidad, precio_old, (p_cantidad * precio_old));

    SET valor_total_saldo = total_old - (p_cantidad * precio_old);

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
