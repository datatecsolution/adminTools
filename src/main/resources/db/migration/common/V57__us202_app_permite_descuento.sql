-- =====================================================================
-- V57 — US-202: permiso por usuario para aplicar descuentos en la app de
-- pedidos (at-ordenes-ventas).
--
-- Hoy cualquier vendedor puede tocar una línea del pedido y cambiar su
-- precio (+14 % … −2 %). El administrador necesita poder quitarle ese
-- permiso a un usuario puntual desde Usuarios → configuración (POS), en la
-- misma tabla que el resto de la config por usuario (US-151/152/187).
--
-- 1 = puede aplicar descuentos (DEFAULT: todos los usuarios, existentes y
-- nuevos —el trigger de alta de usuario crea la fila con el default—,
-- nacen con el permiso activado, que es el comportamiento actual).
-- 0 = la app no le deja cambiar el precio de la línea.
-- Aditiva pura: no cambia el comportamiento de nadie.
-- =====================================================================

ALTER TABLE config_user_facturacion
  ADD COLUMN app_permite_descuento TINYINT NOT NULL DEFAULT 1;

-- TINYINT sin ancho, igual que sus columnas hermanas: con TINYINT(1) el
-- driver de MySQL devuelve Boolean (tinyInt1isBit) y rompe las lecturas que
-- esperan un entero (ver V51/V52).
