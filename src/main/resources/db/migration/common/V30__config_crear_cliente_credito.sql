-- =====================================================================
-- V30 — Permiso para crear clientes de crédito desde el POS
--
-- "Nuevo cliente" del POS permite tipo_cliente 1 (contado, alta rápida)
-- o 2 (crédito, formulario completo con límite). Crear clientes de
-- crédito queda BLOQUEADO para el cajero salvo que el admin lo habilite
-- por usuario con esta bandera (misma tabla que ventana_vendedor /
-- ventana_observaciones / pwd_precio). Default 0 = bloqueado.
--
-- Global (config_user_facturacion vive en la BD común).
-- =====================================================================

ALTER TABLE `config_user_facturacion`
    ADD COLUMN `crear_cliente_credito` TINYINT NOT NULL DEFAULT 0;
