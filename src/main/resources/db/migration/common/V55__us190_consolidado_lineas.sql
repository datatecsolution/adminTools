-- =====================================================================
-- V55 — US-190: el consolidado guarda sus LÍNEAS (lo pedido) al crearse
--
-- La V54 guardaba solo los números de orden y el reporte se recalculaba
-- desde encabezado/detalle_factura_temp cada vez. Pero el Swing BORRA
-- físicamente la orden al facturarla (CtlFacturarFrame → eliminarOrden) y
-- al eliminarla de la lista, así que un consolidado quedaba vacío en cuanto
-- sus órdenes se facturaban. Y «Generar compra» precarga lo PEDIDO
-- (decisión del usuario 2026-09-23), que tiene que sobrevivir a eso.
--
-- Una fila por línea de orden, tal como estaba al consolidar. La existencia,
-- el faltante y el costo se siguen calculando al consultar.
--
-- Aditiva: tabla nueva + relleno de los consolidados existentes desde las
-- órdenes que todavía estén (en producción la tabla V54 está vacía).
-- La V54 NO se toca: ya está aplicada en producción de dulce.
-- =====================================================================

CREATE TABLE IF NOT EXISTS consolidados_pedidos_lineas (
  id              INT NOT NULL AUTO_INCREMENT,
  id_consolidado  INT NOT NULL,
  numero_factura  INT NOT NULL,
  codigo_articulo INT NOT NULL,
  cantidad        DECIMAL(15,2) NOT NULL,
  precio          DECIMAL(15,2) NOT NULL DEFAULT 0,
  total           DECIMAL(15,2) NOT NULL DEFAULT 0,
  codigo_cliente  INT NULL,
  fecha_orden     DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_cpl_consolidado (id_consolidado),
  KEY idx_cpl_articulo (codigo_articulo),
  CONSTRAINT fk_cpl_consolidado FOREIGN KEY (id_consolidado)
    REFERENCES consolidados_pedidos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3
  COMMENT='US-190: líneas de las órdenes de cada consolidado, congeladas al consolidar';

INSERT INTO consolidados_pedidos_lineas
       (id_consolidado, numero_factura, codigo_articulo, cantidad, precio, total, codigo_cliente, fecha_orden)
SELECT o.id_consolidado, d.numero_factura, d.codigo_articulo,
       IFNULL(d.cantidad, 0), IFNULL(d.precio, 0), IFNULL(d.total, 0),
       e.codigo_cliente, e.fecha
  FROM consolidados_pedidos_ordenes o
  JOIN encabezado_factura_temp e ON e.numero_factura = o.numero_factura
  JOIN detalle_factura_temp d    ON d.numero_factura = o.numero_factura
 WHERE NOT EXISTS (SELECT 1 FROM consolidados_pedidos_lineas l WHERE l.id_consolidado = o.id_consolidado);
