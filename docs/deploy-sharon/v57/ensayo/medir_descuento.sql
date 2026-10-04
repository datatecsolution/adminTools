-- Clasifica las líneas de pedidos de la app (client_ref) contra los precios de lista actuales.
DROP TEMPORARY TABLE IF EXISTS m;
CREATE TEMPORARY TABLE m AS
SELECT l.id, l.numero_factura, l.usuario, l.fecha, l.estado, l.codigo_articulo, l.precio, l.cantidad,
       MAX(l.precio = p.precio_articulo)                                   AS exacto,
       MIN(CASE WHEN l.precio = ROUND(p.precio_articulo * 0.99) AND l.precio <> p.precio_articulo THEN p.precio_articulo END) AS base_m1,
       MIN(CASE WHEN l.precio = ROUND(p.precio_articulo * 0.98) AND l.precio <> p.precio_articulo THEN p.precio_articulo END) AS base_m2,
       MIN(CASE WHEN l.precio > p.precio_articulo
                 AND ROUND((l.precio / p.precio_articulo - 1) * 100) BETWEEN 1 AND 14
                 AND l.precio = ROUND(p.precio_articulo * (1 + ROUND((l.precio / p.precio_articulo - 1) * 100) / 100))
                THEN p.precio_articulo END)                                 AS base_rec
FROM (SELECT d.id, d.numero_factura, e.usuario, e.fecha, e.estado, d.codigo_articulo, d.precio, d.cantidad
        FROM detalle_factura_temp d JOIN encabezado_factura_temp e USING (numero_factura)
       WHERE e.client_ref IS NOT NULL AND e.numero_factura <> 111896) l
JOIN precios_articulos p ON p.codigo_articulo = l.codigo_articulo AND p.codigo_precio IN (1, 2, 3, 5) AND p.precio_articulo > 0
GROUP BY l.id;

SELECT CASE WHEN exacto THEN '1 precio de lista' WHEN base_m1 IS NOT NULL THEN '2 -1% (opcion preseleccionada)'
            WHEN base_m2 IS NOT NULL THEN '3 -2%' WHEN base_rec IS NOT NULL THEN '4 recargo +1..+14%' ELSE '5 otro' END clase,
       COUNT(*) lineas, COUNT(DISTINCT numero_factura) pedidos,
       ROUND(SUM(cantidad * precio), 2) vendido,
       ROUND(SUM(cantidad * (COALESCE(base_m1, base_m2) - precio)), 2) descontado
FROM m GROUP BY 1 ORDER BY 1;

SELECT 'pedidos de la app' x, COUNT(DISTINCT numero_factura), COUNT(*) lineas, MIN(fecha), MAX(fecha) FROM m;
