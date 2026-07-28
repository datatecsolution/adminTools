-- =====================================================================
-- V40 — US-115 (Fase 2 stock reservado): la reserva vive hasta FACTURAR
-- o ELIMINAR el pedido.
--
-- Decisión 2026-07-28: "Enviado" (estado 4) es una etiqueta de
-- SEGUIMIENTO del pedido (vendedor/cliente), no un cierre — el pedido
-- sigue vivo y debe seguir descontando del disponible. Con el filtro
-- histórico `estado < 3`, marcar Enviado liberaba la reserva sin haberse
-- facturado (hueco #5 del análisis docs/analisis-impacto-stock-reservado.md).
--
-- Filtro nuevo en función Y vista: estado NOT IN (3, 5)
--   1 Activa · 2 Modificada · 4 Enviado  → RESERVAN
--   3 Imprimida/facturada · 5 Eliminado  → liberan
--
-- Los LISTADOS de pendientes (estado < 3 en Swing/API) NO cambian: un
-- pedido Enviado nunca apareció ahí y eso es visibilidad, no reserva.
-- El add-back del guard de la API se alinea en el mismo US (código Java).
-- =====================================================================

DROP FUNCTION IF EXISTS `f_existencia_y_ordenes`;

DELIMITER $$
CREATE FUNCTION `f_existencia_y_ordenes`(p_cod_articulo int(11), p_cod_bodega int(11)) RETURNS decimal(10,2)
    DETERMINISTIC
BEGIN

 DECLARE can_saldo DECIMAL(10,2) DEFAULT 0;
    DECLARE total_pedidos DECIMAL(10,2) DEFAULT 0;
    DECLARE resultado_total DECIMAL(10,2);

    -- Consulta 1: Obtener el saldo
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

    -- Consulta 2: total pedido en órdenes VIVAS (V40: antes estado<3;
    -- Enviado=4 sigue reservando — solo facturada/eliminada libera)
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
	cajas.codigo_bodega = p_cod_bodega AND encabezado_factura_temp.estado NOT IN (3, 5);

    -- Sumar los resultados
    SET resultado_total = IFNULL(can_saldo, 0) - IFNULL(total_pedidos, 0);

    RETURN resultado_total;
end$$
DELIMITER ;

-- Vista agregada (V39) con el mismo filtro nuevo.
CREATE OR REPLACE
    SQL SECURITY INVOKER
VIEW v_reservado_por_articulo AS
SELECT d.codigo_articulo   AS codigo_articulo,
       c.codigo_bodega     AS codigo_bodega,
       SUM(d.cantidad)     AS reservado
FROM detalle_factura_temp d
         INNER JOIN encabezado_factura_temp e ON e.numero_factura = d.numero_factura
         INNER JOIN cajas c ON c.codigo = e.codigo_caja
WHERE e.estado NOT IN (3, 5)
GROUP BY d.codigo_articulo, c.codigo_bodega;
