-- =====================================================================
-- V53 — `v_existencia_alert`: la alerta de inventario del Swing pasa a
-- leer el saldo materializado y a avisar "bajo el mínimo" de verdad.
--
-- Contexto (análisis 2026-09-17): el menú principal del administrador
-- muestra "N articulos con poca existencia" y el botón abre el Jasper
-- `ReporteAlertaExistencia` (`SELECT * FROM v_existencia_alert`). La vista
-- de la baseline tenía dos problemas:
--
--   1. Semántica: exigía `saldo < 0 AND saldo < cantidad_minima`, es decir
--      SOLO listaba saldos NEGATIVOS. Un artículo con mínimo 10 y saldo 3
--      nunca aparecía (dulce: 109 filas en la vista vs 153 artículos
--      realmente bajo el mínimo). Y `precio_saldo` salía de la fila de
--      saldo del kardex, casi siempre 0.
--   2. Costo: recorría TODO `detalle_movimiento_kardex` con un
--      `GROUP BY codigo_kardex` derivado para hallar el último movimiento
--      de cada kardex. En MySQL 8 es tolerable (0,2 s en dulce) pero en un
--      motor viejo congela el EDT del Swing ("se queda bloqueado").
--
-- Qué cambia (SOLO base de datos; el Swing y el Jasper no se tocan):
--   - Fuente: `existencia_articulo_bodega` (saldo por artículo/bodega
--     mantenido transaccionalmente desde V19, el mismo que usa el POS en
--     /inventory/low-stock). Un kardex sin fila ahí vale 0.
--   - Regla: artículo activo (`estado = 1`) y
--       saldo < 0                                       (lo que ya listaba)
--       OR (cantidad_minima > 0 AND saldo <= cantidad_minima)  (lo nuevo)
--     Es un SUPERCONJUNTO de la vista anterior: nada de lo que se veía
--     desaparece. Se excluye mínimo 0 con saldo 0 (el artículo sin mínimo
--     configurado no es una alerta; en eso difiere del POS, que lo incluye).
--   - `precio_saldo` = costo promedio del kardex (`f_precio_saldo_kardex`),
--     `total_saldo` = saldo × costo; así "VR. Unitario" y "VR. TOTAL" del
--     reporte dejan de salir en 0. Van en DECIMAL para que un saldo
--     negativo con costo 0 no imprima "-HNL 0.00" (el -0.0 del double).
--   - Se conservan TODAS las columnas de la vista original con el mismo
--     nombre y tipo compatible, porque el `.jasper` compilado declara 14
--     fields y JRResultSetDataSource falla si falta alguno. Las que no
--     tienen equivalente en el saldo materializado (`codigo_movimiento`,
--     `no_documento`) van fijas; `descripcion` dice el motivo de la alerta.
--   - SQL SECURITY INVOKER (convención de las vistas nuevas, V39+): no
--     depende de que exista el DEFINER de la baseline en el cliente.
--
-- Lo que NO arregla (es código Java, no BD): el contador del botón queda
-- en "0.0" por una carrera entre el hilo que consulta y el que pinta el
-- label. El clic sigue funcionando y ahora abre el reporte correcto.
--
-- Reversible: volver a ejecutar la definición de V1 (documentada en
-- V1__baseline.sql). No toca datos.
-- =====================================================================

CREATE OR REPLACE
    SQL SECURITY INVOKER
VIEW v_existencia_alert AS
SELECT IFNULL(eab.cantidad, 0)                                   AS can_saldo,
       ak.codigo_bodega                                          AS codigo_bodega,
       ak.codigo_articulo                                        AS codigo_articulo,
       0                                                         AS codigo_movimiento,
       a.articulo                                                AS articulo,
       ak.codigo_kardex                                          AS cod,
       DATE(eab.fecha_actualizacion)                             AS fecha,
       CASE WHEN IFNULL(eab.cantidad, 0) < 0 THEN 'Saldo negativo'
            ELSE 'Bajo el minimo' END                            AS descripcion,
       NULL                                                      AS no_documento,
       CAST(IFNULL(f_precio_saldo_kardex(ak.codigo_kardex), 0)
           AS DECIMAL(14, 2))                                    AS precio_saldo,
       CAST(IFNULL(eab.cantidad, 0)
           * IFNULL(f_precio_saldo_kardex(ak.codigo_kardex), 0)
           AS DECIMAL(14, 2))                                    AS total_saldo,
       b.descripcion_bodega                                      AS descripcion_bodega,
       ak.cantidad_maxima                                        AS cantidad_maxima,
       ak.cantidad_minima                                        AS cantidad_minima,
       ak.metodo                                                 AS metodo
FROM articulo_kardex ak
         INNER JOIN articulo a ON a.codigo_articulo = ak.codigo_articulo
         INNER JOIN bodega b ON b.codigo_bodega = ak.codigo_bodega
         LEFT JOIN existencia_articulo_bodega eab
                   ON eab.codigo_articulo = ak.codigo_articulo
                       AND eab.codigo_bodega = ak.codigo_bodega
WHERE a.estado = 1
  AND (IFNULL(eab.cantidad, 0) < 0
    OR (ak.cantidad_minima > 0 AND IFNULL(eab.cantidad, 0) <= ak.cantidad_minima))
ORDER BY b.descripcion_bodega, a.articulo;
