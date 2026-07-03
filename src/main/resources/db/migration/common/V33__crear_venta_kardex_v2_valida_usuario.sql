-- =====================================================================
-- V33 — crear_venta_kardex_v2: validación REAL de sobreventa desde cajas
--
-- ORIGEN (investigación Ronal 2026-07-02, categoría 107/INFARMA): la
-- validación de V19 quedó PASIVA tras V27 — el SP busca la factura en
-- admin_tools.encabezado_factura pero las facturas reales viven en
-- admin_tools_caja_N, así que el JOIN nunca matchea y v_permite cae al
-- default 1 (permitir). Resultado: saldos de kardex negativos por
-- sobreventa (art. 2705 llegó a −54 el 17-jun) y stock del sistema
-- divergiendo del físico. Esta es la solución de firma nueva que dejó
-- documentada la retrospectiva del epic INV §6 y el propio comment de V27.
--
-- DISEÑO:
--   - SP NUEVO crear_venta_kardex_v2 con 4º parámetro p_usuario. El
--     trigger de caja (V9) resuelve el usuario desde SU encabezado_factura
--     y lo pasa — sin cross-schema imposible, sin adivinar la caja.
--   - v_permite = config_user_facturacion.facturar_sin_inventario del
--     usuario; sin row → default 1 (PERMITIR, comportamiento histórico
--     del Swing; el bloqueo es OPT-IN por usuario, misma semántica V27):
--         facturar_sin_inventario = 0  → necesita stock (BLOQUEA + SIGNAL)
--         facturar_sin_inventario = 1  → permite sobreventa
--         row no existe                → PERMITE (histórico)
--   - El SP viejo crear_venta_kardex (3 args, V27) QUEDA INTACTO: las
--     cajas cuyo trigger siga en V8 (aún no migradas) lo siguen llamando
--     con el comportamiento actual. Coexistencia total.
--   - Cuerpo idéntico a V27 (header lock + saldo FOR UPDATE + inserts +
--     UPSERT a existencia_articulo_bodega); solo cambia cómo se resuelve
--     v_permite y el mensaje del SIGNAL.
--
-- SECUENCIA: V33 (común) corre antes que caja V9 en el mismo boot del
-- Swing (SchemaMigrator.migrateAll migra común primero) → el trigger
-- nuevo nunca llama a un SP inexistente.
--
-- Idempotente: DROP IF EXISTS + CREATE. Re-aplicar es no-op.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS crear_venta_kardex_v2$$

CREATE PROCEDURE crear_venta_kardex_v2(
    IN p_cod_kardex INT,
    IN p_no_factura INT,
    IN p_cantidad   FLOAT,
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

    -- (3) Config de sobreventa del usuario, resuelta DIRECTO por parámetro
    --     (ya no depende de dónde viva la factura). Sin row → 1 (histórico).
    SET v_permite = COALESCE((
        SELECT cu.facturar_sin_inventario
        FROM   config_user_facturacion cu
        WHERE  cu.usuario = p_usuario
        LIMIT 1
    ), 1);

    SET newExistencia = existencia_old - p_cantidad;

    -- (4) SIGNAL atomico bajo el lock: aborta la venta si el usuario está
    --     bloqueado explícitamente y no alcanza el stock.
    IF v_permite = 0 AND newExistencia < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente; usuario bloqueado para sobrevender (V33).';
    END IF;

    -- (5) Inserts del kardex (igual que V19/V27).
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
    INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
        VALUES (v_articulo, v_bodega, newExistencia)
    ON DUPLICATE KEY UPDATE cantidad = newExistencia;
END$$

DELIMITER ;
