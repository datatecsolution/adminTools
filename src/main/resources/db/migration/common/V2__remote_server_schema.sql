-- V2: Alinear todas las bases de clientes V1 con el esquema del servidor Sharon.
-- Cada statement es independiente y condicional via information_schema.

SET FOREIGN_KEY_CHECKS=0;

-- ============================================
-- 1. TABLAS NUEVAS
-- ============================================

CREATE TABLE IF NOT EXISTS `kardex` (
  `idKardex` int NOT NULL AUTO_INCREMENT,
  `no_documento` varchar(255) NOT NULL DEFAULT 'NA',
  `codigo_articulo` int NOT NULL,
  `codigo_bodega` int NOT NULL,
  `entrada` double NOT NULL DEFAULT 0,
  `salida` double NOT NULL DEFAULT 0,
  `existencia` double NOT NULL DEFAULT 0,
  `fecha` datetime NOT NULL,
  PRIMARY KEY (`idKardex`),
  KEY `idx_kardex_articulo` (`codigo_articulo`),
  KEY `idx_kardex_bodega` (`codigo_bodega`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE IF NOT EXISTS `articulo_imagen` (
  `id_img` int NOT NULL AUTO_INCREMENT,
  `codigo_articulo` int NOT NULL,
  `img` mediumblob NOT NULL,
  `extension` varchar(10) NOT NULL,
  PRIMARY KEY (`id_img`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE IF NOT EXISTS `rutas_cobro` (
  `codigo_ruta` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  `observaciones` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_ruta`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE IF NOT EXISTS `usuarios_precios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_precio` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `FKbrb18nke07df543c9fc5v760j` (`codigo_precio`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `pagos_creditos` (
  `fecha_pago` date NOT NULL DEFAULT '1990-01-01',
  `numero_recibo` int NOT NULL DEFAULT '1',
  `saldo_anterior` float(8,2) NOT NULL DEFAULT '0.00',
  `pago` float(8,2) NOT NULL DEFAULT '0.00',
  `saldo` float(8,2) NOT NULL DEFAULT '0.00',
  `numero_factura` int NOT NULL,
  UNIQUE KEY `numero_factura` (`numero_factura`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 ROW_FORMAT=FIXED;

CREATE TABLE IF NOT EXISTS `authorities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) NOT NULL,
  `username` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5q6gmks7pw74h93w5gxq4146q` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

SET FOREIGN_KEY_CHECKS=1;
