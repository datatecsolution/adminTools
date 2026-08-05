-- =====================================================================
-- US-132 — arnés de verificación de f_can_saldo_kardex (V44)
--
-- Correr contra una BD que YA tenga aplicada la V44. La sección [C]
-- crea y borra datos de prueba con códigos 999903+ (US132TEST).
--
-- [A] Equivalencia: para CADA ficha, IFNULL(funcion,0) debe igualar
--     IFNULL(paseo del kardex, 0). (El IFNULL es parte del contrato:
--     ver matiz NULL/0 documentado en la V44 — ningún caller los
--     distingue.) Esperado: 0 filas.
-- [C] Fallback y precedencia de la tabla.
-- =====================================================================

-- [A] EQUIVALENCIA TOTAL ------------------------------------------------
SELECT '[A] IFNULL(funcion,0) vs IFNULL(paseo,0) en todas las fichas (esperado: 0 filas)' AS prueba;
SELECT ak.codigo_articulo, ak.codigo_bodega,
       IFNULL(f_can_saldo_kardex(ak.codigo_articulo, ak.codigo_bodega), 0) AS funcion_nueva,
       IFNULL((SELECT s.cantidad
               FROM articulo_kardex ak2
               JOIN detalle_movimiento_kardex dmk ON ak2.codigo_kardex = dmk.codigo_kardex
               JOIN movimiento_kardex s ON dmk.codigo_movimiento = s.codigo_movimiento
                    AND s.codigo_tipo_movimiento = 3
               WHERE ak2.codigo_articulo = ak.codigo_articulo
                 AND ak2.codigo_bodega  = ak.codigo_bodega
               ORDER BY dmk.codigo_movimiento DESC LIMIT 1), 0) AS paseo_kardex
FROM articulo_kardex ak
HAVING ABS(funcion_nueva - paseo_kardex) > 0.01;

-- [C] FALLBACK Y PRECEDENCIA -------------------------------------------
SELECT '[C] fallback / precedencia / NULL historico' AS prueba;

INSERT INTO articulo (codigo_articulo, articulo, codigo_marca, cod_articulo, codigo_impuesto, precio_articulo, tipo_articulo, estado)
VALUES (999903, 'US132TEST', 1, 0, 1, 1.00, 1, 1);
INSERT INTO articulo_kardex (codigo_articulo, codigo_bodega, cantidad_maxima, cantidad_minima, metodo)
VALUES (999903, 1, 10, 20, 'Promedio ponderado');
SET @kardex_test = LAST_INSERT_ID();

-- C1: ficha SIN saldo tipo 3 y SIN fila en la tabla -> NULL (paridad exacta)
SELECT CASE WHEN f_can_saldo_kardex(999903,1) IS NULL
       THEN 'OK C1: sin fila ni kardex -> NULL (contrato historico)'
       ELSE CONCAT('FALLO C1: ', f_can_saldo_kardex(999903,1)) END AS c1;

-- C2: con saldo tipo 3 en kardex y SIN fila en la tabla -> fallback al paseo
INSERT INTO detalle_movimiento_kardex (codigo_kardex, fecha, descripcion, no_documento)
VALUES (@kardex_test, CURDATE(), 'US132TEST saldo', 'US132TEST');
SET @mov_test = LAST_INSERT_ID();
INSERT INTO movimiento_kardex (codigo_movimiento, codigo_tipo_movimiento, cantidad, precio_unidad, total)
VALUES (@mov_test, 3, 88.00, 1.00, 88.00);
SELECT CASE WHEN f_can_saldo_kardex(999903,1) = 88.00
       THEN 'OK C2: fallback camino el kardex -> 88.00'
       ELSE CONCAT('FALLO C2: ', IFNULL(f_can_saldo_kardex(999903,1),'NULL')) END AS c2;

-- C3: con fila en la tabla, manda la tabla
INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad) VALUES (999903, 1, 61.00);
SELECT CASE WHEN f_can_saldo_kardex(999903,1) = 61.00
       THEN 'OK C3: con fila, manda la tabla -> 61.00'
       ELSE CONCAT('FALLO C3: ', f_can_saldo_kardex(999903,1)) END AS c3;

-- C4: cero legitimo en la tabla NO dispara el fallback
UPDATE existencia_articulo_bodega SET cantidad = 0.00 WHERE codigo_articulo = 999903;
SELECT CASE WHEN f_can_saldo_kardex(999903,1) = 0.00
       THEN 'OK C4: cero legitimo de la tabla (no cae al kardex)'
       ELSE CONCAT('FALLO C4: ', f_can_saldo_kardex(999903,1)) END AS c4;

-- limpieza
DELETE FROM existencia_articulo_bodega WHERE codigo_articulo = 999903;
DELETE FROM movimiento_kardex WHERE codigo_movimiento = @mov_test;
DELETE FROM detalle_movimiento_kardex WHERE codigo_movimiento = @mov_test;
DELETE FROM articulo_kardex WHERE codigo_articulo = 999903;
DELETE FROM articulo WHERE codigo_articulo = 999903;
SELECT '[C] datos de prueba eliminados' AS limpieza;

-- [E] INTEGRACION: f_existencia_y_ordenes (V43) sigue coherente --------
SELECT '[E] spot-check: articulo_view y f_existencia siguen funcionando' AS prueba;
SELECT COUNT(*) AS filas_articulo_view FROM articulo_view LIMIT 1;
