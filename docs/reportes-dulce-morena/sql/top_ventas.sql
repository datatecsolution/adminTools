-- Top 5 productos más vendidos por categoría, último año (2025-07-18 → hoy)
-- Cajas con ventas: 1, 3, 4. Se excluyen facturas NULA.
WITH ventas AS (
  SELECT d.codigo_articulo, d.cantidad, d.total
    FROM admin_tools_caja_1.detalle_factura d
    JOIN admin_tools_caja_1.encabezado_factura e ON e.numero_factura = d.numero_factura
   WHERE e.fecha >= '2025-07-18' AND e.estado_factura <> 'NULA'
  UNION ALL
  SELECT d.codigo_articulo, d.cantidad, d.total
    FROM admin_tools_caja_3.detalle_factura d
    JOIN admin_tools_caja_3.encabezado_factura e ON e.numero_factura = d.numero_factura
   WHERE e.fecha >= '2025-07-18' AND e.estado_factura <> 'NULA'
  UNION ALL
  SELECT d.codigo_articulo, d.cantidad, d.total
    FROM admin_tools_caja_4.detalle_factura d
    JOIN admin_tools_caja_4.encabezado_factura e ON e.numero_factura = d.numero_factura
   WHERE e.fecha >= '2025-07-18' AND e.estado_factura <> 'NULA'
),
agregado AS (
  SELECT ta.descripcion AS categoria,
         a.articulo,
         ROUND(SUM(v.cantidad), 2) AS unidades,
         ROUND(SUM(v.total), 2)   AS total_lps
    FROM ventas v
    JOIN admin_tools.articulo a       ON a.codigo_articulo = v.codigo_articulo
    JOIN admin_tools.tipo_articulo ta ON ta.codigo_tipo_articulo = a.tipo_articulo
   GROUP BY ta.codigo_tipo_articulo, ta.descripcion, a.codigo_articulo, a.articulo
),
rankeado AS (
  SELECT categoria, articulo, unidades, total_lps,
         ROW_NUMBER() OVER (PARTITION BY categoria ORDER BY unidades DESC) AS rn,
         SUM(total_lps) OVER (PARTITION BY categoria) AS cat_total
    FROM agregado
)
SELECT categoria, rn AS puesto, articulo, unidades, total_lps, ROUND(cat_total,2) AS total_categoria_lps
  FROM rankeado
 WHERE rn <= 5
 ORDER BY cat_total DESC, categoria, rn;
