-- V31: ventana (en días) para el ranking de "más vendidos" en facturación.
-- El POS ordena las categorías por unidades vendidas en esta ventana y arma
-- el chip "Más vendidos". Configurable desde /settings (ADMIN). Default 30.
ALTER TABLE config_app
  ADD COLUMN dias_ranking_mas_vendidos INT NOT NULL DEFAULT 30;
