-- =====================================================================
-- V27 — Fix del default de facturar_sin_inventario en crear_venta_kardex
--
-- PROBLEMA REPORTADO (cliente A, 2026-05-28): tras aplicar V19, al
-- facturar en el Swing salta "Stock insuficiente; usuario bloqueado
-- para sobrevender (V19)" incluso para usuarios que tienen
-- facturar_sin_inventario=1 (o que no tienen row en config_user_facturacion).
-- El comportamiento histórico del Swing siempre fue PERMITIR sobreventa
-- cuando no había restricción explícita.
--
-- ANÁLISIS DEL BUG:
--
-- La consulta de V19 que resuelve v_permite hace JOIN sobre
-- encabezado_factura sin prefijo de schema:
--
--   SELECT cu.facturar_sin_inventario
--   FROM   encabezado_factura ef
--   JOIN   config_user_facturacion cu ON cu.usuario = ef.usuario
--   WHERE  ef.numero_factura = p_no_factura
--
-- El SP vive en admin_tools, así que la consulta resuelve a
-- admin_tools.encabezado_factura. PERO:
--
--   - El Swing siempre escribió las facturas en
--     admin_tools_caja_N.encabezado_factura (per-caja).
--   - El API (INV-8) también escribe en cajas vía tenant routing.
--   - admin_tools.encabezado_factura está esencialmente vacía.
--
-- Por tanto el JOIN nunca encuentra el row del usuario para facturas
-- desde cajas. El SELECT devuelve NULL, y V19 hacía
-- COALESCE(NULL, 0) = 0 → "bloquear". Cuando newExistencia < 0, SIGNAL.
--
-- Resultado: V19 introdujo bloqueo de sobreventa accidental para CADA
-- venta desde cajas con stock <= cantidad. Comportamiento opuesto al
-- histórico, sin que la validación fina (por config_user_facturacion)
-- siquiera pudiera aplicarse en práctica.
--
-- SEMÁNTICA CORRECTA (confirmada por el negocio):
--   facturar_sin_inventario = 0  →  necesita stock (BLOQUEA)
--   facturar_sin_inventario = 1  →  permite sin stock (PERMITE)
--   row no existe                →  default histórico: PERMITE
--
-- FIX V27: cambiar UNA línea. COALESCE(..., 0) → COALESCE(..., 1).
-- Sin cambio de firma del SP. Sin tocar triggers. Cero blast radius.
--
-- IMPLICANCIA: la validación V19 queda PASIVA para el flujo real
-- (cross-schema desde cajas). Solo aplica si una factura vive en
-- admin_tools.encabezado_factura directamente (caso histórico, no
-- ocurre hoy). Si en el futuro se necesita validación real desde
-- cajas, hay que cambiar la firma del SP para recibir el usuario
-- como parámetro (V28 + V9 caja). Documentado en
-- docs/inv-epic-retrospective.md sección 6.
--
-- Idempotente: DROP IF EXISTS + CREATE. Re-aplicar es no-op.
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

    -- (3) Config de sobreventa del usuario que esta facturando.
    --     Default 1 = PERMITIR (V27): el flujo real escribe facturas en
    --     admin_tools_caja_N.encabezado_factura, no aqui, asi que el JOIN
    --     nunca encuentra el row. Devolver el default historico del Swing
    --     (permitir) evita falsos negativos en cajas.
    --
    --     La validacion fina (por config_user_facturacion) solo se activa
    --     si la factura vive en admin_tools.encabezado_factura (caso
    --     historico, no usado actualmente). Para validacion real desde
    --     cajas, ver retrospectiva del epic INV §6 — requiere cambiar
    --     firma del SP.
    SET v_permite = COALESCE((
        SELECT cu.facturar_sin_inventario
        FROM   encabezado_factura ef
        JOIN   config_user_facturacion cu ON cu.usuario = ef.usuario
        WHERE  ef.numero_factura = p_no_factura
    ), 1);

    SET newExistencia = existencia_old - p_cantidad;

    -- (4) SIGNAL atomico bajo el lock: aborta si bloqueado explicitamente
    --     y no alcanza el stock. Con default=1 esto solo dispara cuando
    --     el usuario tiene row con facturar_sin_inventario=0 explicito.
    IF v_permite = 0 AND newExistencia < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente; usuario bloqueado para sobrevender (V19/V27).';
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
