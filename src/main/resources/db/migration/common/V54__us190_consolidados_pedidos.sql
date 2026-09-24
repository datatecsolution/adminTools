-- =====================================================================
-- V54 — US-190: consolidado de pedidos para compras, persistente
--
-- Réplica de Rutas de entrega del Swing (rutas_entregas + entregas_facturas:
-- una ruta agrupa facturas y una factura no puede estar en dos rutas) pero
-- sobre las ÓRDENES de venta (encabezado_factura_temp) y con fin de COMPRA:
-- el POS une órdenes, calcula por producto pedido/existencia/faltante y
-- guarda el documento para que (a) una orden no se consolide dos veces
-- mientras el consolidado siga abierto, (b) quede historial y (c) al
-- comprar se anote el número de compra.
--
-- Estado: 1 Abierto · 2 Comprado · 3 Cerrado (sin compra).
-- Aditiva pura: dos tablas nuevas, no toca datos existentes. Sin FK a
-- encabezado_factura_temp porque el Swing borra órdenes físicamente
-- (delete fisico) y el histórico del consolidado debe sobrevivir.
-- =====================================================================

CREATE TABLE IF NOT EXISTS consolidados_pedidos (
  id            INT NOT NULL AUTO_INCREMENT,
  fecha         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario       VARCHAR(100) NOT NULL,
  codigo_bodega INT NOT NULL DEFAULT 1,
  estado        TINYINT NOT NULL DEFAULT 1,
  observacion   VARCHAR(255) NULL,
  numero_compra INT NULL,
  PRIMARY KEY (id),
  KEY idx_consolidado_estado (estado),
  KEY idx_consolidado_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3
  COMMENT='US-190: consolidado de pedidos para compras (cabecera)';

CREATE TABLE IF NOT EXISTS consolidados_pedidos_ordenes (
  id             INT NOT NULL AUTO_INCREMENT,
  id_consolidado INT NOT NULL,
  numero_factura INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_consolidado_orden (id_consolidado, numero_factura),
  KEY idx_cpo_orden (numero_factura),
  CONSTRAINT fk_cpo_consolidado FOREIGN KEY (id_consolidado)
    REFERENCES consolidados_pedidos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3
  COMMENT='US-190: órdenes (encabezado_factura_temp.numero_factura) de cada consolidado';
