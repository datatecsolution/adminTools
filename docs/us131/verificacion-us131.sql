-- =====================================================================
-- US-131 — arnés de verificación de f_existencia_y_ordenes (V43)
--
-- Correr contra una BD que YA tenga aplicada la V43. Solo lectura,
-- salvo la sección [C] que crea y borra datos de prueba con códigos
-- 999901+ (marcados US131TEST).
--
-- [A] Equivalencia total: para CADA ficha del kardex, la función nueva
--     debe devolver lo mismo que el cálculo viejo (paseo del kardex −
--     reservas 1,2) expresado inline. Esperado: 0 diferencias.
-- [B] Semántica de reservas: solo estados 1 y 2 restan.
-- [C] Fallback: ficha con kardex y SIN fila en la tabla materializada
--     → la función camina el kardex (no devuelve 0 falso).
-- =====================================================================

-- [A] EQUIVALENCIA TOTAL ------------------------------------------------
SELECT '[A] equivalencia funcion nueva vs calculo viejo (esperado: 0 filas)' AS prueba;
SELECT ak.codigo_articulo, ak.codigo_bodega,
       f_existencia_y_ordenes(ak.codigo_articulo, ak.codigo_bodega) AS funcion_nueva,
       ( IFNULL((SELECT s.cantidad
                 FROM articulo_kardex ak2
                 JOIN detalle_movimiento_kardex dmk ON ak2.codigo_kardex = dmk.codigo_kardex
                 JOIN movimiento_kardex s ON dmk.codigo_movimiento = s.codigo_movimiento
                      AND s.codigo_tipo_movimiento = 3
                 WHERE ak2.codigo_articulo = ak.codigo_articulo
                   AND ak2.codigo_bodega  = ak.codigo_bodega
                 ORDER BY dmk.codigo_movimiento DESC LIMIT 1), 0)
       - IFNULL((SELECT SUM(d.cantidad)
                 FROM detalle_factura_temp d
                 JOIN encabezado_factura_temp e ON e.numero_factura = d.numero_factura
                 JOIN cajas c ON c.codigo = e.codigo_caja
                 WHERE d.codigo_articulo = ak.codigo_articulo
                   AND c.codigo_bodega = ak.codigo_bodega
                   AND e.estado IN (1,2)), 0) ) AS calculo_viejo
FROM articulo_kardex ak
HAVING ABS(funcion_nueva - calculo_viejo) > 0.01;

-- [B] SEMANTICA DE RESERVAS --------------------------------------------
SELECT '[B] reservas por estado en la BD (control de contexto)' AS prueba;
SELECT e.estado, COUNT(DISTINCT e.numero_factura) AS ordenes, IFNULL(SUM(d.cantidad),0) AS unidades
FROM encabezado_factura_temp e
LEFT JOIN detalle_factura_temp d ON d.numero_factura = e.numero_factura
GROUP BY e.estado ORDER BY e.estado;

-- [C] FALLBACK SIN FILA MATERIALIZADA ----------------------------------
SELECT '[C] fallback: ficha con kardex y sin fila en la tabla' AS prueba;

INSERT INTO articulo (codigo_articulo, articulo, codigo_marca, cod_articulo, codigo_impuesto, precio_articulo, tipo_articulo, estado)
VALUES (999901, 'US131TEST FALLBACK', 1, 0, 1, 1.00, 1, 1);
INSERT INTO articulo_kardex (codigo_articulo, codigo_bodega, cantidad_maxima, cantidad_minima, metodo)
VALUES (999901, 1, 10, 20, 'Promedio ponderado');
SET @kardex_test = LAST_INSERT_ID();
INSERT INTO detalle_movimiento_kardex (codigo_kardex, fecha, descripcion, no_documento)
VALUES (@kardex_test, CURDATE(), 'US131TEST saldo inicial', 'US131TEST');
SET @mov_test = LAST_INSERT_ID();
INSERT INTO movimiento_kardex (codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
VALUES (@mov_test, 3, 77.00, 1.00, 77.00);
-- deliberadamente NO se inserta fila en existencia_articulo_bodega

SELECT CASE WHEN f_existencia_y_ordenes(999901, 1) = 77.00
            THEN 'OK: fallback devolvio 77.00 (el saldo del kardex)'
            ELSE CONCAT('FALLO: devolvio ', f_existencia_y_ordenes(999901, 1)) END AS resultado_fallback;

-- caso opuesto: CON fila materializada, manda la tabla
INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad) VALUES (999901, 1, 55.00);
SELECT CASE WHEN f_existencia_y_ordenes(999901, 1) = 55.00
            THEN 'OK: con fila, manda la tabla (55.00)'
            ELSE CONCAT('FALLO: devolvio ', f_existencia_y_ordenes(999901, 1)) END AS resultado_tabla;

-- y saldo legitimo 0.00 en la tabla NO dispara el fallback
UPDATE existencia_articulo_bodega SET cantidad = 0.00 WHERE codigo_articulo = 999901;
SELECT CASE WHEN f_existencia_y_ordenes(999901, 1) = 0.00
            THEN 'OK: cero legitimo de la tabla (no cae al kardex)'
            ELSE CONCAT('FALLO: devolvio ', f_existencia_y_ordenes(999901, 1)) END AS resultado_cero_legitimo;

-- limpieza
DELETE FROM existencia_articulo_bodega WHERE codigo_articulo = 999901;
DELETE FROM movimiento_kardex WHERE codigo_movimiento = @mov_test;
DELETE FROM detalle_movimiento_kardex WHERE codigo_movimiento = @mov_test;
DELETE FROM articulo_kardex WHERE codigo_articulo = 999901;
DELETE FROM articulo WHERE codigo_articulo = 999901;
SELECT '[C] datos de prueba eliminados' AS limpieza;

-- [D] SEMANTICA DE RESERVAS CON DATOS VIVOS -----------------------------
-- Crea articulo 999902 con saldo materializado 50 y una orden de prueba
-- con 10 unidades; recorre los 5 estados verificando cuales restan.
SELECT '[D] estados que reservan (US-121: solo 1 y 2)' AS prueba;

INSERT INTO articulo (codigo_articulo, articulo, codigo_marca, cod_articulo, codigo_impuesto, precio_articulo, tipo_articulo, estado)
VALUES (999902, 'US131TEST RESERVAS', 1, 0, 1, 1.00, 1, 1);
INSERT INTO articulo_kardex (codigo_articulo, codigo_bodega, cantidad_maxima, cantidad_minima, metodo)
VALUES (999902, 1, 10, 20, 'Promedio ponderado');
INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad) VALUES (999902, 1, 50.00);

INSERT INTO encabezado_factura_temp (fecha, codigo_cliente, tipo_factura, codigo_caja, usuario, estado, observacion)
VALUES (NOW(), 1, 1, 1, 'US131TEST', 1, 'US131TEST');
SET @orden_test = LAST_INSERT_ID();
INSERT INTO detalle_factura_temp (numero_factura, codigo_articulo, precio, cantidad, impuesto, subtotal)
VALUES (@orden_test, 999902, 1.00, 10.00, 0, 10.00);

SELECT CASE WHEN f_existencia_y_ordenes(999902,1)=40.00 THEN 'OK: estado 1 (activa) RESTA -> 40'
       ELSE CONCAT('FALLO estado 1: ', f_existencia_y_ordenes(999902,1)) END AS d1;
UPDATE encabezado_factura_temp SET estado=2 WHERE numero_factura=@orden_test;
SELECT CASE WHEN f_existencia_y_ordenes(999902,1)=40.00 THEN 'OK: estado 2 (modificada) RESTA -> 40'
       ELSE CONCAT('FALLO estado 2: ', f_existencia_y_ordenes(999902,1)) END AS d2;
UPDATE encabezado_factura_temp SET estado=3 WHERE numero_factura=@orden_test;
SELECT CASE WHEN f_existencia_y_ordenes(999902,1)=50.00 THEN 'OK: estado 3 (facturada) NO resta -> 50'
       ELSE CONCAT('FALLO estado 3: ', f_existencia_y_ordenes(999902,1)) END AS d3;
UPDATE encabezado_factura_temp SET estado=4 WHERE numero_factura=@orden_test;
SELECT CASE WHEN f_existencia_y_ordenes(999902,1)=50.00 THEN 'OK: estado 4 (enviada) NO resta -> 50'
       ELSE CONCAT('FALLO estado 4: ', f_existencia_y_ordenes(999902,1)) END AS d4;
UPDATE encabezado_factura_temp SET estado=5 WHERE numero_factura=@orden_test;
SELECT CASE WHEN f_existencia_y_ordenes(999902,1)=50.00 THEN 'OK: estado 5 (anulada) NO resta -> 50'
       ELSE CONCAT('FALLO estado 5: ', f_existencia_y_ordenes(999902,1)) END AS d5;

-- limpieza
DELETE FROM detalle_factura_temp WHERE numero_factura=@orden_test;
DELETE FROM encabezado_factura_temp WHERE numero_factura=@orden_test;
DELETE FROM existencia_articulo_bodega WHERE codigo_articulo=999902;
DELETE FROM articulo_kardex WHERE codigo_articulo=999902;
DELETE FROM articulo WHERE codigo_articulo=999902;
SELECT '[D] datos de prueba eliminados' AS limpieza;
