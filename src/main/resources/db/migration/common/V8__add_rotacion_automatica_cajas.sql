-- Rotación automática de cajas para cliente id=1.
-- Default 0 (manual): el cajero decide la caja con Ctrl+P. Si el admin
-- activa el toggle en la configuración del usuario, se aplica la lógica
-- histórica (rota cada 2 facturas de consumidor final).

ALTER TABLE `config_user_facturacion`
    ADD COLUMN `rotacion_automatica_cajas` tinyint NOT NULL DEFAULT 0
    AFTER `cant_facturas_imprimir`;
