-- =====================================================================
-- V51 — US-160: preferencia de presentación del catálogo en facturación
--
-- La facturación táctil puede pintar el catálogo como LISTA de filas
-- (default, pantallas 1024x1024 o verticales) o como PARRILLA de
-- tarjetas (pantallas anchas 16:9/16:10, donde la lista desperdicia
-- ancho). Es una preferencia por usuario, en la misma tabla que el
-- resto de la config de facturación (US-151/152), así el admin la
-- cambia desde el POS sin tocar la terminal.
--
-- 0 = lista (default, comportamiento actual) · 1 = parrilla.
-- Aditiva pura: no toca datos existentes.
-- =====================================================================

ALTER TABLE config_user_facturacion
  ADD COLUMN catalogo_parrilla TINYINT NOT NULL DEFAULT 0;

-- OJO con el tipo: TINYINT sin ancho, igual que sus columnas hermanas. Con
-- TINYINT(1) el driver de MySQL devuelve Boolean (tinyInt1isBit) y rompe las
-- lecturas que esperan un entero (mismo problema que rotacion_automatica_cajas).
