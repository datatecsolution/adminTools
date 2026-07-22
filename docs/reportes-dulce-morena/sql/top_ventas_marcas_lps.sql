-- Top 10 productos por TOTAL VENDIDO (L) por categoría (marcas), último año
-- (2025-07-18 → hoy). Cajas 1, 3, 4. Sin facturas NULA. Sin TECNO.
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
  SELECT m.descripcion AS categoria,
         a.articulo,
         ROUND(SUM(v.cantidad), 0) AS unidades,
         ROUND(SUM(v.total), 2)   AS total_lps
    FROM ventas v
    JOIN admin_tools.articulo a ON a.codigo_articulo = v.codigo_articulo
    JOIN admin_tools.marcas m   ON m.codigo_marca = a.codigo_marca
   WHERE m.descripcion <> 'TECNO'
   GROUP BY m.codigo_marca, m.descripcion, a.codigo_articulo, a.articulo
),
rankeado AS (
  SELECT categoria, articulo, unidades, total_lps,
         ROW_NUMBER() OVER (PARTITION BY categoria ORDER BY total_lps DESC) AS rn,
         SUM(total_lps) OVER (PARTITION BY categoria) AS cat_total
    FROM agregado
)
SELECT categoria, rn AS puesto, articulo, unidades, total_lps,
       ROUND(100 * total_lps / cat_total, 1) AS pct_categoria
  FROM rankeado
 WHERE rn <= 10
 ORDER BY cat_total DESC, rn;
