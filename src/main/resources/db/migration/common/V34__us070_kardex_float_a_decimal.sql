-- =====================================================================
-- V34 (common) — US-070: float → DECIMAL(15,2) en el subsistema kardex/stock
--
-- Fase 1 del cierre Hito 1. Elimina los descuadres por redondeo de FLOAT en
-- el kardex. Ejemplo real observado en local: movimiento_kardex.total =
-- -889.1000366210938 (artefacto float). En Ronal estos totales llegan a
-- ~23,6M, por encima del rango entero exacto del FLOAT (16.777.216) → la
-- valuación pierde precisión a nivel de lempira.
--
-- Precisión 15, escala 2 → rango ±9.999.999.999.999,99. Auditoría previa:
-- MAX local << 1M; Ronal ~23,6M en total → sobra capacidad.
--
-- SOLO retype (ALTER ... MODIFY), sin renombrar ni dropear columnas → una
-- terminal con Swing viejo sigue arrancando (getFloat/setFloat contra una
-- columna DECIMAL es JDBC-safe). Se preservan NOT NULL y los defaults
-- exactos. ALTER ... MODIFY es idempotente (re-aplicar no cambia nada).
--
-- Alcance (tablas base con columnas float del kardex/stock):
--   kardex             (entrada, salida, existencia)   -- double
--   movimiento_kardex  (cantidad, precio_unidad, total) -- float(8,2)
--   articulo_kardex    (cantidad_minima, cantidad_maxima) -- float(8,2)
--   articulo_bodega    (existencia)                    -- float
-- NOTA: detalle_movimiento_kardex NO tiene columnas float (solo ids/fechas),
--       no aplica. existencia_articulo_bodega ya es DECIMAL(14,2) (tabla que
--       consume la API), no se toca.
-- API: mapea articulo_kardex (ya BigDecimal) y NO articulo_bodega; el
--      ddl-auto=validate tolera el retype (arranca con float o decimal).
-- =====================================================================

ALTER TABLE `kardex`
    MODIFY `entrada`    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `salida`     DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    MODIFY `existencia` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `movimiento_kardex`
    MODIFY `cantidad`      DECIMAL(15,2) NOT NULL,
    MODIFY `precio_unidad` DECIMAL(15,2) NOT NULL,
    MODIFY `total`         DECIMAL(15,2) NOT NULL;

ALTER TABLE `articulo_kardex`
    MODIFY `cantidad_minima` DECIMAL(15,2) NOT NULL DEFAULT 20.00,
    MODIFY `cantidad_maxima` DECIMAL(15,2) NOT NULL DEFAULT 10.00;

ALTER TABLE `articulo_bodega`
    MODIFY `existencia` DECIMAL(15,2) NOT NULL;
