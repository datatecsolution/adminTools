-- =====================================================================
-- V29 — Visibilidad de categorías en el POS (whitelist)
--
-- El POS táctil solo debe ofrecer las categorías marcadas explícitamente.
-- Se agrega `mostrar_pos` a `marcas` (categorías). Default 0 = OCULTA:
-- el admin activa cuáles se muestran. Las categorías existentes quedan en 0
-- (whitelist estricta, decidido con el usuario).
--
-- Global (aplica a todas las cajas; marcas vive en la BD común).
-- =====================================================================

ALTER TABLE `marcas`
    ADD COLUMN `mostrar_pos` TINYINT(1) NOT NULL DEFAULT 0;
