-- =====================================================================
-- V43 / US-131 — f_existencia_y_ordenes lee el saldo materializado
--
-- La funcion es el camino CALIENTE del stock: la usan articulo_view, el
-- guard anti-sobreventa del save (US-074/109), y desde US-130 la
-- busqueda de productos de la app de pedidos — una llamada POR PRODUCTO
-- del resultado de CADA busqueda (3.443 busquedas/dia en Sharon).
--
-- Hasta ahora su "Consulta 1" calculaba el saldo caminando el kardex:
-- 3 joins + ORDER BY DESC + LIMIT 1 sobre movimiento_kardex (1.6M de
-- filas en Sharon) EN CADA LLAMADA. Ese costo es exactamente el que la
-- V18 creo existencia_articulo_bodega para eliminar (patron ledger +
-- balance, mantenida transaccionalmente por los SPs desde V19/V20)…
-- pero la funcion nunca se migro a leerla.
--
-- Este cambio: Consulta 1 pasa a un SELECT por PRIMARY KEY sobre
-- existencia_articulo_bodega. La resta de reservas (ordenes estado 1,2
-- — US-121) queda IDENTICA.
--
-- Evidencia que habilita el cambio (auditoria US-131, 2026-08-03):
--  - Reconciliacion COMPLETA en produccion (Sharon): 5.141 fichas de
--    articulo_kardex comparadas tabla-vs-paseo → 0 mismatches, 0 filas
--    faltantes. La tabla ES el saldo desde V20.
--  - Auditoria de escritores: todos los flujos runtime del kardex pasan
--    por los 7 SPs (Swing y API); el unico camino directo en codigo
--    (KardexDao.registrarKardex) solo crea la FICHA articulo_kardex,
--    sin movimientos — no afecta saldos.
--
-- Defensa en profundidad: si una ficha existe en articulo_kardex pero
-- NO tiene fila en existencia_articulo_bodega (un escritor futuro que
-- se salte los SPs), la funcion CAE AL PASEO DEL KARDEX de siempre en
-- vez de reportar 0 — un desync jamas puede inventar un faltante ni
-- disparar 409 falsos. El camino caliente (fila presente) sigue O(1).
--
-- Sin cambio de contrato: misma firma, mismos tipos, misma semantica
-- de reservas. Verificacion de equivalencia en docs/us131/.
-- =====================================================================

DROP FUNCTION IF EXISTS `f_existencia_y_ordenes`;

DELIMITER $$
CREATE FUNCTION `f_existencia_y_ordenes`(p_cod_articulo int(11), p_cod_bodega int(11)) RETURNS decimal(10,2)
    DETERMINISTIC
BEGIN

    DECLARE can_saldo DECIMAL(10,2) DEFAULT NULL;
    DECLARE total_pedidos DECIMAL(10,2) DEFAULT 0;
    DECLARE resultado_total DECIMAL(10,2);

    -- Consulta 1 (US-131): saldo desde la tabla materializada — un
    -- SELECT por PK en vez del paseo del kardex.
    SELECT `cantidad` INTO can_saldo
    FROM `existencia_articulo_bodega`
    WHERE `codigo_articulo` = p_cod_articulo
      AND `codigo_bodega`   = p_cod_bodega;

    -- Fallback defensivo: sin fila en la tabla (escritor fuera de los
    -- SPs, cliente recien migrado a medio backfill), se camina el
    -- kardex como siempre. can_saldo NULL distingue "sin fila" de un
    -- saldo legitimo en 0.00.
    IF can_saldo IS NULL THEN
        SELECT `saldos`.`cantidad` INTO can_saldo
        FROM `articulo_kardex`
        JOIN `detalle_movimiento_kardex`
            ON `articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`
        JOIN `movimiento_kardex` `saldos`
            ON `detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`
            AND `saldos`.`codigo_tipo_movimiento` = 3
        WHERE `articulo_kardex`.`codigo_articulo` = p_cod_articulo
          AND `articulo_kardex`.`codigo_bodega` = p_cod_bodega
        ORDER BY `detalle_movimiento_kardex`.`codigo_movimiento` DESC
        LIMIT 1;
    END IF;

    -- Consulta 2: total pedido en ordenes VIVAS (US-121: solo estado
    -- 1=Activa y 2=Modificada reservan). IDENTICA a V41.
    SELECT SUM(detalle_factura_temp.cantidad) INTO total_pedidos
    FROM
	detalle_factura_temp
	INNER JOIN
	encabezado_factura_temp
	ON
		detalle_factura_temp.numero_factura = encabezado_factura_temp.numero_factura
	INNER JOIN
	cajas
	ON
		encabezado_factura_temp.codigo_caja = cajas.codigo
WHERE
	detalle_factura_temp.codigo_articulo = p_cod_articulo AND
	cajas.codigo_bodega = p_cod_bodega AND encabezado_factura_temp.estado IN (1, 2);

    -- Sumar los resultados
    SET resultado_total = IFNULL(can_saldo, 0) - IFNULL(total_pedidos, 0);

    RETURN resultado_total;
end$$
DELIMITER ;
