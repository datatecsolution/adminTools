-- =====================================================================
-- V28 — Logo de la empresa en datos_empresa (US-031)
--
-- La pantalla de configuración general del panel admin (US-031) permite
-- subir el logo del negocio. La imagen se sube vía el endpoint POST /upload
-- (US-030) y se guarda su URL aquí. La tabla datos_empresa no tenía dónde
-- almacenarla, así que se agrega la columna logo_url (nullable).
--
-- Flyway garantiza que V28 se aplique una sola vez (no se necesita
-- IF NOT EXISTS, que ademas MySQL 8 no soporta para ADD COLUMN).
-- =====================================================================

ALTER TABLE `datos_empresa`
    ADD COLUMN `logo_url` VARCHAR(500) NULL;
