SELECT usuario,
       COUNT(*) lineas,
       SUM(exacto) lista,
       SUM(NOT exacto AND base_m1 IS NOT NULL) m1,
       SUM(NOT exacto AND base_m1 IS NULL AND base_m2 IS NOT NULL) m2,
       SUM(NOT exacto AND base_m1 IS NULL AND base_m2 IS NULL AND base_rec IS NOT NULL) recargo,
       ROUND(100 * SUM(NOT exacto AND base_m1 IS NOT NULL) / COUNT(*), 1) pct_m1,
       ROUND(SUM(CASE WHEN NOT exacto AND base_m1 IS NOT NULL THEN cantidad * (base_m1 - precio) END), 2) L_m1,
       ROUND(SUM(CASE WHEN NOT exacto AND base_m1 IS NULL AND base_m2 IS NOT NULL THEN cantidad * (base_m2 - precio) END), 2) L_m2
FROM m GROUP BY usuario ORDER BY m1 DESC;
-- pedidos con -1%: ¿la línea con -1% está sola o en un pedido que también tiene -2%?
SELECT CASE WHEN tiene_m2 THEN 'pedido con -1% y tambien -2%' WHEN lineas_m1 = lineas THEN 'todas las lineas del pedido con -1%'
            ELSE 'solo algunas lineas con -1%, sin -2%' END patron,
       COUNT(*) pedidos, SUM(lineas_m1) lineas_m1
FROM (SELECT numero_factura, COUNT(*) lineas,
             SUM(NOT exacto AND base_m1 IS NOT NULL) lineas_m1,
             MAX(NOT exacto AND base_m1 IS NULL AND base_m2 IS NOT NULL) tiene_m2
        FROM m GROUP BY numero_factura HAVING lineas_m1 > 0) x
GROUP BY 1;
