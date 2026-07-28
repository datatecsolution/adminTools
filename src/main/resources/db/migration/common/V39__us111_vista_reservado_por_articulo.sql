-- US-111 (Fase 0 stock reservado) — vista agregada de RESERVADO por
-- artículo/bodega: cantidades en pedidos PENDIENTES (estado < 3, mismo
-- filtro que f_existencia_y_ordenes). La bodega del pedido llega vía
-- encabezado_factura_temp.codigo_caja -> cajas.codigo_bodega (US-109
-- asigna la caja real del vendedor; el legado quedaba en DEFAULT 1).
--
-- Motivación: f_existencia_y_ordenes hace 2 subconsultas POR LLAMADA y
-- está marcada DETERMINISTIC leyendo tablas — usable puntual, cara en
-- listados fila a fila. Esta vista entrega el reservado de TODO el
-- catálogo en una sola consulta agrupada (la leen el guard de la API,
-- /inventory y el Swing en Fase 1). La función queda para compatibilidad
-- (articulo_view). Ver docs/analisis-impacto-stock-reservado.md.
--
-- disponible(art, bodega) = saldo kardex − IFNULL(reservado, 0)

CREATE OR REPLACE
    SQL SECURITY INVOKER
VIEW v_reservado_por_articulo AS
SELECT d.codigo_articulo   AS codigo_articulo,
       c.codigo_bodega     AS codigo_bodega,
       SUM(d.cantidad)     AS reservado
FROM detalle_factura_temp d
         INNER JOIN encabezado_factura_temp e ON e.numero_factura = d.numero_factura
         INNER JOIN cajas c ON c.codigo = e.codigo_caja
WHERE e.estado < 3
GROUP BY d.codigo_articulo, c.codigo_bodega;

-- Apoyo del filtro estado<3 + resolución de bodega (la tabla solo tenía
-- PK, codigo_cliente y fecha).
ALTER TABLE encabezado_factura_temp
    ADD INDEX idx_eft_estado_caja (estado, codigo_caja);
