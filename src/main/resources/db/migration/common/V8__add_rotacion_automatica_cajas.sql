-- Rotación automática de cajas para cliente id=1.
-- Cuando es 1 (default) se preserva la lógica histórica (rota cada 2 facturas
-- de consumidor final). Cuando es 0, el usuario decide la caja manualmente
-- con Ctrl+P.

ALTER TABLE `config_user_facturacion`
    ADD COLUMN `rotacion_automatica_cajas` tinyint NOT NULL DEFAULT 1
    AFTER `cant_facturas_imprimir`;
