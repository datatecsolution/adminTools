-- Comparativo trimestral por categoría: 2025 completo + 2026 a la fecha
-- Cajas 1, 3, 4. Sin facturas NULA. Sin TECNO.
SELECT YEAR(v.fecha)    AS anio,
       QUARTER(v.fecha) AS trimestre,
       m.descripcion    AS categoria,
       ROUND(SUM(v.cantidad), 0) AS unidades,
       ROUND(SUM(v.total), 2)    AS total_lps
  FROM (
    SELECT e.fecha, d.codigo_articulo, d.cantidad, d.total
      FROM admin_tools_caja_1.detalle_factura d
      JOIN admin_tools_caja_1.encabezado_factura e ON e.numero_factura = d.numero_factura
     WHERE e.fecha >= '2024-01-01' AND e.estado_factura <> 'NULA'
    UNION ALL
    SELECT e.fecha, d.codigo_articulo, d.cantidad, d.total
      FROM admin_tools_caja_3.detalle_factura d
      JOIN admin_tools_caja_3.encabezado_factura e ON e.numero_factura = d.numero_factura
     WHERE e.fecha >= '2024-01-01' AND e.estado_factura <> 'NULA'
    UNION ALL
    SELECT e.fecha, d.codigo_articulo, d.cantidad, d.total
      FROM admin_tools_caja_4.detalle_factura d
      JOIN admin_tools_caja_4.encabezado_factura e ON e.numero_factura = d.numero_factura
     WHERE e.fecha >= '2024-01-01' AND e.estado_factura <> 'NULA'
  ) v
  JOIN admin_tools.articulo a ON a.codigo_articulo = v.codigo_articulo
  JOIN admin_tools.marcas m   ON m.codigo_marca = a.codigo_marca
 WHERE m.descripcion <> 'TECNO'
 GROUP BY anio, trimestre, m.codigo_marca, m.descripcion
 ORDER BY anio, trimestre, total_lps DESC;
