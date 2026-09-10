-- =====================================================================
-- V50 — US-059: galería de imágenes por producto (app de pedidos)
--
-- La imagen PRINCIPAL del producto sigue viviendo en `articulo_imagen`
-- (US-079: thumbnail de listados, POS y Swing intactos). Esta tabla
-- guarda las imágenes ADICIONALES con su orden para la galería del
-- detalle de producto. Mismas convenciones de columnas que
-- articulo_imagen (img MEDIUMBLOB con JPEG ~600px, extension).
--
-- Aditiva pura: no toca datos ni tablas existentes.
-- =====================================================================

CREATE TABLE IF NOT EXISTS articulo_galeria (
  id INT NOT NULL AUTO_INCREMENT,
  codigo_articulo INT NOT NULL,
  orden INT NOT NULL DEFAULT 1,
  img MEDIUMBLOB NOT NULL,
  extension VARCHAR(10) NOT NULL DEFAULT 'jpg',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_articulo_galeria (codigo_articulo, orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
