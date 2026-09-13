-- =====================================================================
-- V52 — normaliza el tipo de `catalogo_parrilla` a TINYINT sin ancho.
--
-- La V51 original se escribió con TINYINT(1) y así se aplicó en Samuel
-- (2026-09-13) antes de que se corrigiera el archivo. Con TINYINT(1) el
-- driver de MySQL activa tinyInt1isBit y devuelve Boolean en vez de
-- Integer: el mismo código funciona en todos los clientes y revienta solo
-- donde el ancho quedó en 1 — el bug más caro de diagnosticar.
--
-- Este ALTER deja a todos los clientes con el mismo tipo que las columnas
-- hermanas de la tabla. Donde la columna ya es TINYINT no cambia nada.
-- No toca valores: 0/1 se conservan.
-- =====================================================================

ALTER TABLE config_user_facturacion
  MODIFY COLUMN catalogo_parrilla TINYINT NOT NULL DEFAULT 0;
