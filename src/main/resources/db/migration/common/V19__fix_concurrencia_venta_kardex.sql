-- =====================================================================
-- V19 — INV-CC (fase 1: crear_venta_kardex) + INV-2 mantenimiento
--
-- Reescribe crear_venta_kardex con el patron bulletproof validado en
-- el prototipo (ver docs/inventario-api-design.md §9 en repo admin-tools-api):
--
-- 1) Header lock (FOR UPDATE sobre articulo_kardex) - serializa los
--    movimientos por (articulo, bodega), cerrando la race de
--    read-modify-write del saldo. Tambien resuelve articulo+bodega
--    para el UPSERT al final.
-- 2) Lectura consolidada del saldo con FOR UPDATE - una sola query
--    (antes eran 3 SELECTs separados). El FOR UPDATE hace current read,
--    bypassando el snapshot REPEATABLE READ que establecen las lecturas
--    previas del trigger detalle_factura_b_insert. Imprescindible para
--    correctness en contexto trigger.
-- 3) SIGNAL para sobreventa, respetando config_user_facturacion.
--    facturar_sin_inventario por usuario (resuelto del encabezado_factura
--    via p_no_factura). Si flag=0 y newExistencia<0 -> aborta atomicamente
--    bajo el lock. Si flag=1 -> permite saldo negativo (comportamiento
--    historico). Insumos (tipo_articulo=2) van por crear_venta_insumo_kardex,
--    no aplica esta regla.
-- 4) UPSERT a existencia_articulo_bodega - mantiene la tabla de saldos
--    materializada (V18) en la misma transaccion del kardex. Defensa en
--    profundidad: kardex como ledger inmutable + balance como cache rapido,
--    siempre consistentes.
--
-- Pre-validado: prototipo crear_venta_kardex_fix corrido contra
-- admin_tools con 3 escenarios concurrentes (envueltos en START TRANSACTION
-- = simula contexto trigger): permisivo 3+5/167->159 OK; bloqueado
-- 100+100/167->67 + SIGNAL OK; bloqueado 30+30/167->107 OK.
--
-- Idempotente: DROP IF EXISTS + CREATE. Si necesita rollback, restaurar
-- el body original desde la V7 baseline (V7__recrear_funciones_procedures_triggers.sql).
--
-- ALCANCE LIMITADO: V19 solo toca crear_venta_kardex. Los demas
-- crear_*_kardex (compa, dev_venta, dev_compa, requisicion_*) siguen
-- con la version vieja (con su race latente). Se corrigen en V20 con
-- el mismo patron, una vez V19 este verificada en produccion. Mientras
-- tanto, el API NO debe leer de existencia_articulo_bodega (estaria
-- stale para articulos con movimientos de compra/devolucion): sigue
-- usando f_can_saldo_kardex hasta V20.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS crear_venta_kardex$$

CREATE PROCEDURE crear_venta_kardex(
    IN p_cod_kardex INT,
    IN p_no_factura INT,
    IN p_cantidad   FLOAT
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
    DECLARE v_permite           TINYINT DEFAULT 0;

    -- (1) HEADER LOCK: serializa movimientos por (articulo, bodega).
    --     Resuelve (articulo, bodega) que necesitamos para el UPSERT final.
    SELECT codigo_articulo, codigo_bodega
    INTO   v_articulo, v_bodega
    FROM   articulo_kardex
    WHERE  codigo_kardex = p_cod_kardex
    FOR UPDATE;

    -- (2) SALDO con FOR UPDATE (current read, bypassa snapshot).
    --     Lectura consolidada: una sola query trae cantidad+total+precio.
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

    -- (3) Config de sobreventa del usuario que esta facturando.
    --     Default 0 = bloquear (si no hay config_user_facturacion para el usuario).
    SET v_permite = COALESCE((
        SELECT cu.facturar_sin_inventario
        FROM   encabezado_factura ef
        JOIN   config_user_facturacion cu ON cu.usuario = ef.usuario
        WHERE  ef.numero_factura = p_no_factura
    ), 0);

    SET newExistencia = existencia_old - p_cantidad;

    -- (4) SIGNAL atomico bajo el lock: aborta si bloqueado y no alcanza el stock.
    IF v_permite = 0 AND newExistencia < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente; usuario bloqueado para sobrevender (V19).';
    END IF;

    -- (5) Inserts del kardex (igual que la version original).
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
        -- existencia negativa: precio del saldo en 0 (consistente con original).
        INSERT INTO movimiento_kardex(codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
            VALUES (cod_movimiento, 3, newExistencia, 0, valor_total_saldo);
    END IF;

    -- (6) UPSERT a la tabla materializada (mantenimiento INV-2).
    --     INSERT si la fila no existe (kardex recien creado por el trigger);
    --     UPDATE al nuevo saldo si existe (caso comun).
    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

DELIMITER ;
