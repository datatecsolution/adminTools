-- Baseline de `admin_tools`
-- Generado 2026-04-14 08:37:58 por SchemaDumper
-- Fuente: conexion activa de ConexionStatic

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
-- Requerido para que CREATE FUNCTION funcione cuando el binlog
-- esta activo y las funciones no declaran caracteristicas SQL.
SET @ADMIN_TOOLS_ORIG_LBT = @@GLOBAL.log_bin_trust_function_creators;
SET GLOBAL log_bin_trust_function_creators = 1;

-- ============================================
-- TABLAS (66)
-- ============================================

CREATE TABLE `articulo` (
  `codigo_articulo` int unsigned NOT NULL AUTO_INCREMENT,
  `articulo` varchar(255) NOT NULL,
  `codigo_marca` int unsigned NOT NULL DEFAULT '1',
  `cod_articulo` int unsigned NOT NULL DEFAULT '0',
  `codigo_impuesto` int NOT NULL,
  `precio_articulo` double DEFAULT NULL,
  `tipo_articulo` int NOT NULL DEFAULT '1',
  `estado` bit(1) DEFAULT b'1',
  PRIMARY KEY (`codigo_articulo`),
  UNIQUE KEY `codigo` (`codigo_articulo`),
  KEY `codigo_marca` (`codigo_marca`),
  KEY `codigo_impuesto` (`codigo_impuesto`),
  KEY `tipo_articulo` (`tipo_articulo`),
  CONSTRAINT `articulos_impuestos` FOREIGN KEY (`codigo_impuesto`) REFERENCES `impuesto` (`codigo_impuesto`) ON UPDATE CASCADE,
  CONSTRAINT `articulos_marcas` FOREIGN KEY (`codigo_marca`) REFERENCES `marcas` (`codigo_marca`) ON UPDATE CASCADE,
  CONSTRAINT `articulos_tipo` FOREIGN KEY (`tipo_articulo`) REFERENCES `tipo_articulo` (`codigo_tipo_articulo`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `articulo_bodega` (
  `codigo_bodega` int unsigned NOT NULL,
  `codigo_articulo` int NOT NULL,
  `existencia` float NOT NULL,
  KEY `codigo_articulo` (`codigo_articulo`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 ROW_FORMAT=FIXED;

CREATE TABLE `articulo_imagen` (
  `id_img` int NOT NULL AUTO_INCREMENT,
  `codigo_articulo` int NOT NULL,
  `img` mediumblob NOT NULL,
  `extension` varchar(10) NOT NULL,
  PRIMARY KEY (`id_img`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `articulo_kardex` (
  `codigo_kardex` int NOT NULL AUTO_INCREMENT,
  `codigo_articulo` int NOT NULL,
  `codigo_bodega` int NOT NULL DEFAULT '1',
  `cantidad_maxima` float(8,2) NOT NULL DEFAULT '10.00',
  `cantidad_minima` float(8,2) NOT NULL DEFAULT '20.00',
  `metodo` varchar(145) NOT NULL DEFAULT 'Promedio ponderado',
  PRIMARY KEY (`codigo_kardex`),
  KEY `codigo_kardex` (`codigo_kardex`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `codigo_bodega` (`codigo_bodega`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `authorities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) NOT NULL,
  `username` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5q6gmks7pw74h93w5gxq4146q` (`username`),
  CONSTRAINT `FK5q6gmks7pw74h93w5gxq4146q` FOREIGN KEY (`username`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `bancos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL DEFAULT 'NA',
  `no_cuenta` varchar(100) NOT NULL DEFAULT 'NA',
  `id_tipo_cuenta` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `tipo_cuenta` (`id_tipo_cuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `bodega` (
  `codigo_bodega` int NOT NULL AUTO_INCREMENT,
  `descripcion_bodega` varchar(255) NOT NULL,
  PRIMARY KEY (`codigo_bodega`),
  UNIQUE KEY `codigo` (`codigo_bodega`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `cajas` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(150) NOT NULL DEFAULT 'NA',
  `codigo_bodega` int NOT NULL DEFAULT '0',
  `nombre_db` varchar(150) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

CREATE TABLE `cajas_usuarios` (
  `codigo_caja` int NOT NULL DEFAULT '0',
  `usuario` varchar(150) NOT NULL DEFAULT 'system',
  `por_defecto` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

CREATE TABLE `cierre_caja` (
  `idCierre` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `factura_inicial` varchar(100) NOT NULL DEFAULT '-1',
  `factura_final` varchar(100) NOT NULL DEFAULT '-1',
  `efectivo` float(11,2) NOT NULL DEFAULT '0.00',
  `creditos` float(11,2) NOT NULL DEFAULT '0.00',
  `isv15` float(11,2) NOT NULL DEFAULT '0.00',
  `isv18` float(11,2) NOT NULL DEFAULT '0.00',
  `totalventa` float(11,2) NOT NULL DEFAULT '0.00',
  `totalimpuesto` float(11,2) NOT NULL DEFAULT '0.00',
  `tarjeta` float(11,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(255) NOT NULL DEFAULT 'system',
  `efectivo_inicial` float(11,2) NOT NULL,
  `estado` int NOT NULL DEFAULT '1',
  `total_isv15` float(11,2) NOT NULL DEFAULT '0.00',
  `total_isv18` float(11,2) NOT NULL DEFAULT '0.00',
  `total_excento` float(11,2) NOT NULL DEFAULT '0.00',
  `total_efectivo` float(11,2) NOT NULL DEFAULT '0.00',
  `no_salida_inicial` int NOT NULL DEFAULT '0',
  `no_salida_final` int NOT NULL DEFAULT '0',
  `total_salida` float(11,2) NOT NULL DEFAULT '0.00',
  `no_cobro_inicial` int NOT NULL DEFAULT '0',
  `no_cobro_final` int NOT NULL DEFAULT '0',
  `total_cobro` float(11,2) NOT NULL DEFAULT '0.00',
  `efectivo_caja` float(11,2) NOT NULL DEFAULT '0.00',
  `no_pago_inicial` int NOT NULL DEFAULT '0',
  `no_pago_final` int NOT NULL DEFAULT '0',
  `total_pago` float(11,2) NOT NULL DEFAULT '0.00',
  `fecha_inicio` datetime NOT NULL DEFAULT '1999-01-01 00:00:00',
  `fecha_final` datetime NOT NULL DEFAULT '1999-01-01 00:00:00',
  `no_entrada_inicial` int NOT NULL DEFAULT '0',
  `no_entrada_final` int NOT NULL DEFAULT '0',
  `total_entrada` float(11,2) NOT NULL DEFAULT '0.00',
  `turno` varchar(5) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`idCierre`),
  UNIQUE KEY `idCierre` (`idCierre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `cierre_facturacion` (
  `codigo_cierre` int NOT NULL DEFAULT '0',
  `codigo_caja` int NOT NULL DEFAULT '0',
  `usuario` varchar(150) NOT NULL DEFAULT 'system',
  `factura_inicial` int NOT NULL DEFAULT '-1',
  `factura_final` int NOT NULL DEFAULT '-1',
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `codigo_cierre` (`codigo_cierre`),
  CONSTRAINT `cierres_facturas` FOREIGN KEY (`codigo_cierre`) REFERENCES `cierre_caja` (`idCierre`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

CREATE TABLE `cliente` (
  `codigo_cliente` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_cliente` varchar(255) NOT NULL DEFAULT 'NA',
  `direccion` varchar(255) NOT NULL DEFAULT 'NA',
  `telefono` varchar(255) DEFAULT NULL,
  `movil` varchar(255) DEFAULT NULL,
  `rtn` varchar(255) DEFAULT NULL,
  `zona_procedencia` varchar(255) NOT NULL DEFAULT 'NA',
  `codigo_postal` varchar(255) NOT NULL DEFAULT 'NA',
  `codiciones_pago` varchar(255) NOT NULL DEFAULT 'NA',
  `limite_credito` decimal(38,2) DEFAULT NULL,
  `saldo` float NOT NULL DEFAULT '0',
  `clasificacion` varchar(255) NOT NULL DEFAULT 'NA',
  `estado` int NOT NULL DEFAULT '0',
  `tipo_cliente` int NOT NULL DEFAULT '1',
  `id_vendedor` int NOT NULL DEFAULT '1',
  `id_ruta_cobro` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`codigo_cliente`),
  UNIQUE KEY `codigo` (`codigo_cliente`),
  KEY `tipo_cliente` (`tipo_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

CREATE TABLE `codigos_articulos` (
  `codigo_articulo` int unsigned NOT NULL,
  `codigo_barra` varchar(255) DEFAULT NULL,
  `id_codigo` int unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_codigo`),
  KEY `codigo_articulo` (`codigo_articulo`),
  CONSTRAINT `codigos_articulos` FOREIGN KEY (`codigo_articulo`) REFERENCES `articulo` (`codigo_articulo`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `config_app` (
  `dia_vencimiento_factura` int NOT NULL DEFAULT '0',
  `interes_para_facturas_venc` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `config_user_facturacion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(100) NOT NULL DEFAULT 'system',
  `formato_factura` varchar(50) NOT NULL DEFAULT 'tiket',
  `ventana_vendedor` tinyint NOT NULL DEFAULT '0',
  `pwd_descuento` tinyint NOT NULL DEFAULT '0',
  `pwd_precio` tinyint NOT NULL DEFAULT '0',
  `descuento_porcentaje` tinyint NOT NULL DEFAULT '0',
  `ventana_observaciones` tinyint NOT NULL DEFAULT '0',
  `precio_redondiar` tinyint NOT NULL DEFAULT '0',
  `facturar_sin_inventario` tinyint NOT NULL DEFAULT '0',
  `impr_report_categ_cierre` tinyint NOT NULL DEFAULT '0',
  `impr_report_salida` tinyint NOT NULL DEFAULT '0',
  `show_report_salida` tinyint NOT NULL DEFAULT '0',
  `impr_report_entrada` tinyint NOT NULL DEFAULT '0',
  `show_report_entrada` tinyint NOT NULL DEFAULT '0',
  `activar_busqueda_facturacion` tinyint NOT NULL DEFAULT '0',
  `agregar_cliente_credito` tinyint NOT NULL DEFAULT '0',
  `formato_factura_credito` varchar(50) NOT NULL DEFAULT 'tiket',
  `pwd_entre_precio` tinyint NOT NULL DEFAULT '0',
  `imp_report_order` tinyint DEFAULT '0',
  `unir_can_item` tinyint DEFAULT '0',
  `delete_item_fact` tinyint DEFAULT '0',
  `cant_facturas_imprimir` int DEFAULT '1',
  PRIMARY KEY (`id`,`usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `cuentas_bancos` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL DEFAULT '1999-01-01 00:00:00',
  `codigo_banco` int NOT NULL DEFAULT '0',
  `descripcion` varchar(300) NOT NULL DEFAULT 'NA',
  `referencia` varchar(50) NOT NULL DEFAULT '0',
  `debito` float(10,2) NOT NULL DEFAULT '0.00',
  `credito` float(10,2) NOT NULL DEFAULT '0.00',
  `saldo` float(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `cuentas_facturas` (
  `codigo_cuenta` int NOT NULL AUTO_INCREMENT,
  `codigo_cliente` int NOT NULL DEFAULT '-1',
  `no_factura` int NOT NULL,
  `codigo_caja` int NOT NULL,
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `fecha_vencimiento` date NOT NULL DEFAULT '1990-01-01',
  PRIMARY KEY (`codigo_cuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `cuentas_por_cobrar` (
  `codigo_reguistro` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `codigo_cliente` int NOT NULL DEFAULT '-1',
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  `debito` float(10,2) NOT NULL DEFAULT '0.00',
  `credito` float(10,2) NOT NULL DEFAULT '0.00',
  `saldo` float(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`codigo_reguistro`),
  UNIQUE KEY `codigo` (`codigo_reguistro`),
  KEY `codigo_cliente` (`codigo_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `cuentas_por_cobrar_facturas` (
  `codigo_reguistro` int NOT NULL AUTO_INCREMENT,
  `codigo_cuenta` int NOT NULL DEFAULT '-1',
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  `debito` float(10,2) NOT NULL DEFAULT '0.00',
  `credito` float(10,2) NOT NULL DEFAULT '0.00',
  `saldo` float(10,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(150) NOT NULL DEFAULT 'NA',
  `tipo_movimiento` int NOT NULL DEFAULT '1' COMMENT '1=saldo inicial 2=pago 3=interes moratorio',
  PRIMARY KEY (`codigo_reguistro`),
  KEY `codigo` (`codigo_reguistro`),
  KEY `idx_cuenta_registro` (`codigo_cuenta`,`codigo_reguistro` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `cuentas_por_pagar` (
  `codigo_reguistro` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `codigo_proveedor` int NOT NULL DEFAULT '-1',
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  `debito` float(10,2) NOT NULL DEFAULT '0.00',
  `credito` float(10,2) NOT NULL DEFAULT '0.00',
  `saldo` float(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`codigo_reguistro`),
  UNIQUE KEY `codigo` (`codigo_reguistro`),
  KEY `codigo_proveedor` (`codigo_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `datos_empresa` (
  `nombre` varchar(500) NOT NULL,
  `rtn` varchar(500) NOT NULL,
  `telefono` varchar(300) NOT NULL,
  `correo` varchar(150) NOT NULL,
  `propietario` varchar(300) NOT NULL,
  `direccion` varchar(500) NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `datos_factura` (
  `codigo_rango` int NOT NULL AUTO_INCREMENT,
  `CAI` varchar(300) NOT NULL DEFAULT 'NA',
  `factura_inicial` varchar(11) NOT NULL DEFAULT 'NA',
  `factura_final` varchar(11) NOT NULL DEFAULT 'NA',
  `codigo_tipo_facturacion` varchar(50) NOT NULL DEFAULT 'NA',
  `cantida_solicitada` int NOT NULL DEFAULT '0',
  `fecha_limite_emision` date NOT NULL DEFAULT '1990-01-01',
  PRIMARY KEY (`codigo_rango`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `departamento` (
  `codigo_departamento` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(245) NOT NULL,
  PRIMARY KEY (`codigo_departamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_cotizacion` (
  `numero_cotizacion` int NOT NULL,
  `codigo_articulo` int NOT NULL,
  `precio` float(10,2) NOT NULL DEFAULT '0.00',
  `cantidad` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `descuento` float(10,2) NOT NULL DEFAULT '0.00',
  `total` float(10,2) NOT NULL DEFAULT '0.00',
  `id` int NOT NULL AUTO_INCREMENT,
  `codigo_barra` varchar(255) DEFAULT 'NA',
  PRIMARY KEY (`id`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `numero_factura` (`numero_cotizacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_devoluciones` (
  `codigo_devolucion` int NOT NULL AUTO_INCREMENT,
  `numero_factura` int NOT NULL,
  `codigo_caja` int NOT NULL DEFAULT '-1',
  `codigo_articulo` int NOT NULL,
  `precio` float(10,2) NOT NULL DEFAULT '0.00',
  `cantidad` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `descuento` float(10,2) NOT NULL DEFAULT '0.00',
  `total` float(10,2) NOT NULL,
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `agrega_kardex` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`codigo_devolucion`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `numero_factura` (`numero_factura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_devoluciones_compra` (
  `codigo_devolucion` int NOT NULL AUTO_INCREMENT,
  `numero_factura` int NOT NULL,
  `codigo_articulo` varchar(45) NOT NULL,
  `precio` float(10,2) NOT NULL DEFAULT '0.00',
  `cantidad` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `descuento` float(10,2) NOT NULL DEFAULT '0.00',
  `total` float(10,2) NOT NULL,
  `fecha` date NOT NULL DEFAULT '1990-01-01',
  `agrega_kardex` int NOT NULL DEFAULT '0',
  `codigo_bodega` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`codigo_devolucion`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `numero_factura` (`numero_factura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_factura` (
  `numero_factura` int NOT NULL,
  `codigo_articulo` int NOT NULL DEFAULT '-1',
  `precio` float(8,2) NOT NULL DEFAULT '0.00',
  `cantidad` float(8,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(8,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(8,2) NOT NULL DEFAULT '0.00',
  `descuento` float(8,2) NOT NULL DEFAULT '0.00',
  `total` float(8,2) NOT NULL DEFAULT '0.00',
  `id` int NOT NULL AUTO_INCREMENT,
  `codigo_barra` varchar(255) NOT NULL DEFAULT 'NA',
  `agrega_kardex` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`id`),
  KEY `numero_factura` (`numero_factura`),
  KEY `cod_articulo` (`codigo_articulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_factura_compra` (
  `id_detalle_compra` int NOT NULL AUTO_INCREMENT,
  `numero_compra` int NOT NULL,
  `codigo_articulo` int unsigned NOT NULL,
  `precio` float(10,2) NOT NULL DEFAULT '0.00',
  `cantidad` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `agrega_kardex` int NOT NULL DEFAULT '0',
  `codigo_bodega` int NOT NULL DEFAULT '1',
  `fecha_venc` date DEFAULT '1990-01-01',
  PRIMARY KEY (`id_detalle_compra`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `numero_factura` (`numero_compra`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_factura_temp` (
  `numero_factura` int NOT NULL,
  `codigo_articulo` int NOT NULL,
  `precio` double DEFAULT NULL,
  `cantidad` decimal(38,2) DEFAULT NULL,
  `impuesto` decimal(38,2) DEFAULT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `descuento` decimal(38,2) DEFAULT NULL,
  `total` decimal(38,2) DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `numero_factura` (`numero_factura`),
  CONSTRAINT `FKcwtlk8v608q5i1ch86bqdmpvi` FOREIGN KEY (`numero_factura`) REFERENCES `encabezado_factura_temp` (`numero_factura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_movimiento_kardex` (
  `codigo_movimiento` int NOT NULL AUTO_INCREMENT,
  `codigo_kardex` int NOT NULL DEFAULT '-1',
  `fecha` date NOT NULL DEFAULT '2024-11-02',
  `descripcion` varchar(255) NOT NULL DEFAULT 'Inventario Inicial',
  `no_documento` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_movimiento`),
  UNIQUE KEY `codigo_movimiento` (`codigo_movimiento`),
  KEY `codigo_kardex` (`codigo_kardex`),
  CONSTRAINT `kardex` FOREIGN KEY (`codigo_kardex`) REFERENCES `articulo_kardex` (`codigo_kardex`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_pago` (
  `no_recibo_pago` int NOT NULL DEFAULT '-1',
  `no_factura_pagada` int NOT NULL DEFAULT '-1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `detalle_requisicion` (
  `codigo_requisicion` int unsigned NOT NULL,
  `codigo_articulo` int unsigned NOT NULL,
  `cantidad` float(10,2) NOT NULL,
  `precio_unidad` float(10,2) NOT NULL,
  `total` float(10,2) NOT NULL,
  `codigo_depart_origen` int NOT NULL DEFAULT '0',
  `codigo_depart_destino` int NOT NULL DEFAULT '0',
  `agrega_kardex` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `empleados` (
  `codigo_empleado` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL DEFAULT 'NA',
  `apellido` varchar(255) NOT NULL DEFAULT 'NA',
  `telefono` varchar(255) NOT NULL DEFAULT 'NA',
  `correo` varchar(255) NOT NULL DEFAULT 'NA',
  `direccion` varchar(255) NOT NULL DEFAULT 'NA',
  `sueldo_base` decimal(38,2) DEFAULT NULL,
  `codigo_tipo_empleado` int NOT NULL DEFAULT '-1',
  `usuario` varchar(150) NOT NULL DEFAULT 'system',
  PRIMARY KEY (`codigo_empleado`),
  UNIQUE KEY `id` (`codigo_empleado`),
  KEY `tipo_empleado` (`codigo_tipo_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `encabezado_cotizacion` (
  `numero_cotizacion` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `subtotal_excento` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal15` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal18` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `total` float(10,2) NOT NULL,
  `codigo_cliente` int NOT NULL DEFAULT '1',
  `codigo` varchar(5) NOT NULL DEFAULT '-1',
  `estado` varchar(25) NOT NULL DEFAULT 'NA',
  `isvOtros` float(10,2) NOT NULL DEFAULT '0.00',
  `isv18` float(10,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `descuento` float(10,2) NOT NULL DEFAULT '0.00',
  `observacion` varchar(255) NOT NULL DEFAULT 'NA',
  `total_letras` varchar(500) NOT NULL DEFAULT ' NA',
  PRIMARY KEY (`numero_cotizacion`),
  UNIQUE KEY `numero_factura` (`numero_cotizacion`),
  KEY `codigo_cliente` (`codigo_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `encabezado_factura` (
  `numero_factura` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `subtotal_excento` float(8,2) NOT NULL DEFAULT '0.00',
  `subtotal15` float(8,2) NOT NULL DEFAULT '0.00',
  `subtotal18` float(8,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(8,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(8,2) NOT NULL,
  `total` float(10,2) NOT NULL,
  `codigo_cliente` int NOT NULL,
  `codigo` varchar(11) NOT NULL DEFAULT '-1',
  `estado_factura` varchar(25) NOT NULL DEFAULT 'NA',
  `isvOtros` float(8,2) NOT NULL DEFAULT '0.00',
  `isv18` float(8,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `pago` float(8,2) NOT NULL DEFAULT '0.00',
  `descuento` float(8,2) NOT NULL DEFAULT '0.00',
  `tipo_factura` int NOT NULL DEFAULT '1',
  `agrega_kardex` int NOT NULL DEFAULT '0',
  `tipo_pago` int NOT NULL,
  `observacion` varchar(255) NOT NULL DEFAULT 'NA',
  `total_letras` varchar(500) NOT NULL DEFAULT 'NA',
  `codigo_vendedor` int NOT NULL DEFAULT '1',
  `estado_pago` int NOT NULL DEFAULT '0',
  `cod_rango` int NOT NULL DEFAULT '1',
  `cobro_tarjeta` float(8,2) NOT NULL DEFAULT '0.00',
  `cobro_efectivo` float(8,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`numero_factura`),
  UNIQUE KEY `numero_factura` (`numero_factura`),
  KEY `codigo_cliente` (`codigo_cliente`),
  KEY `tipo_factura` (`tipo_factura`),
  KEY `usuario` (`usuario`),
  KEY `tipo_pago` (`tipo_pago`),
  KEY `codigo_empleado` (`codigo_vendedor`),
  KEY `codigo_rango` (`cod_rango`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `encabezado_factura_compra` (
  `numero_compra` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `subtotal_excento` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal15` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal18` float(10,2) NOT NULL DEFAULT '0.00',
  `subtotal` float(10,2) NOT NULL DEFAULT '0.00',
  `impuesto` float(10,2) NOT NULL DEFAULT '0.00',
  `total` float(10,2) NOT NULL DEFAULT '0.00',
  `codigo_proveedor` int NOT NULL DEFAULT '-1',
  `codigo` varchar(5) NOT NULL DEFAULT 'NA',
  `estado_factura` varchar(25) NOT NULL DEFAULT 'NA',
  `isv18` float(10,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `pago` float(10,2) NOT NULL DEFAULT '0.00',
  `no_factura_compra` varchar(100) NOT NULL,
  `fecha_ingreso` date NOT NULL DEFAULT '1990-01-01',
  `tipo_factura` int unsigned NOT NULL,
  `fecha_vencimiento` date NOT NULL DEFAULT '1990-01-01',
  `agrega_kardex` int unsigned NOT NULL DEFAULT '0',
  `codigo_bodega` int NOT NULL DEFAULT '-1',
  PRIMARY KEY (`numero_compra`),
  UNIQUE KEY `numero_factura` (`numero_compra`),
  KEY `codigo_cliente` (`codigo_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `encabezado_factura_temp` (
  `numero_factura` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) DEFAULT NULL,
  `subtotal_excento` decimal(38,2) DEFAULT '0.00',
  `subtotal15` decimal(38,2) DEFAULT '0.00',
  `subtotal18` decimal(38,2) DEFAULT '0.00',
  `subtotal` decimal(38,2) DEFAULT '0.00',
  `impuesto` decimal(38,2) DEFAULT '0.00',
  `total` decimal(38,2) DEFAULT '0.00',
  `codigo_cliente` int NOT NULL DEFAULT '1',
  `codigo` varchar(5) NOT NULL DEFAULT 'NA',
  `estado_factura` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'ACT',
  `isvOtros` float(10,2) NOT NULL DEFAULT '0.00',
  `isv18` decimal(38,2) DEFAULT '0.00',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `pago` decimal(38,2) DEFAULT NULL,
  `descuento` decimal(38,2) DEFAULT NULL,
  `tipo_factura` int NOT NULL,
  `codigo_caja` int NOT NULL DEFAULT '1',
  `codigo_vendedor` int DEFAULT '1',
  `isv_otros` decimal(38,2) DEFAULT '0.00',
  `estado` int DEFAULT '1',
  `observacion` varchar(255) DEFAULT 'NA',
  PRIMARY KEY (`numero_factura`),
  UNIQUE KEY `numero_factura` (`numero_factura`),
  KEY `codigo_cliente` (`codigo_cliente`),
  KEY `FKrl9u6gcvooikd0eym6yi44bvc` (`codigo_vendedor`),
  CONSTRAINT `FKrl9u6gcvooikd0eym6yi44bvc` FOREIGN KEY (`codigo_vendedor`) REFERENCES `empleados` (`codigo_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `encabezado_requisicion` (
  `codigo_requisicion` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `total` float(10,2) NOT NULL,
  `codigo_depart_destino` int unsigned NOT NULL,
  `usuario` varchar(45) NOT NULL,
  `agrega_kardex` int unsigned NOT NULL DEFAULT '0',
  `estado_requisicion` varchar(45) NOT NULL DEFAULT 'ACT',
  `codigo_depart_origen` int NOT NULL DEFAULT '-1',
  PRIMARY KEY (`codigo_requisicion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `entradas_caja` (
  `codigo_entrada` int NOT NULL AUTO_INCREMENT,
  `concepto` varchar(400) NOT NULL DEFAULT 'NA',
  `cantidad` float(8,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(300) NOT NULL DEFAULT 'system',
  `fecha` datetime NOT NULL,
  `estado` varchar(25) NOT NULL DEFAULT 'NA',
  `codigo_cuenta` int NOT NULL DEFAULT '-1',
  PRIMARY KEY (`codigo_entrada`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `entregas_facturas` (
  `id_entrega` int NOT NULL DEFAULT '0',
  `id_factura` int NOT NULL DEFAULT '0',
  `id_caja` varchar(45) NOT NULL DEFAULT '0',
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `impuesto` (
  `codigo_impuesto` int NOT NULL AUTO_INCREMENT,
  `descripcion_impuesto` varchar(255) NOT NULL,
  `porcentaje` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`codigo_impuesto`),
  UNIQUE KEY `codigo` (`codigo_impuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `insumos` (
  `id_insumo` int NOT NULL AUTO_INCREMENT,
  `codigo_servicio` int NOT NULL DEFAULT '-1',
  `codigo_articulo` int NOT NULL DEFAULT '-1',
  `cantidad` float(11,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `marcas` (
  `codigo_marca` int unsigned NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`codigo_marca`),
  UNIQUE KEY `codigo_marca` (`codigo_marca`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `movimiento_kardex` (
  `codigo_movimiento` int NOT NULL,
  `codigo_tipo_movimiento` int NOT NULL DEFAULT '3',
  `cantidad` float(10,2) NOT NULL,
  `precio_unidad` float(10,2) NOT NULL,
  `total` float(10,2) NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `codigo_movimiento` (`codigo_movimiento`),
  KEY `codigo_tipo_movimiento` (`codigo_tipo_movimiento`),
  CONSTRAINT `detalle_movimiento` FOREIGN KEY (`codigo_movimiento`) REFERENCES `detalle_movimiento_kardex` (`codigo_movimiento`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tipo_movimiento` FOREIGN KEY (`codigo_tipo_movimiento`) REFERENCES `tipo_movimiento_kardex` (`codigo_tipo_moviemiento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `movimientos_bancos` (
  `codigo_movimiento` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(400) NOT NULL DEFAULT 'NA',
  `cantidad` float(10,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(300) NOT NULL DEFAULT 'system',
  `fecha` datetime NOT NULL,
  `codigo_cuenta` int NOT NULL DEFAULT '-1',
  `id_tipo_movimiento` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`codigo_movimiento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `pagos_creditos` (
  `fecha_pago` date NOT NULL DEFAULT '1990-01-01',
  `numero_recibo` int NOT NULL DEFAULT '1',
  `saldo_anterior` float(8,2) NOT NULL DEFAULT '0.00',
  `pago` float(8,2) NOT NULL DEFAULT '0.00',
  `saldo` float(8,2) NOT NULL DEFAULT '0.00',
  `numero_factura` int NOT NULL,
  UNIQUE KEY `numero_factura` (`numero_factura`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 ROW_FORMAT=FIXED;

CREATE TABLE `precios` (
  `codigo_precio` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`codigo_precio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `precios_articulos` (
  `codigo_articulo` int NOT NULL DEFAULT '-1',
  `precio_articulo` decimal(38,2) DEFAULT NULL,
  `codigo_precio` int NOT NULL DEFAULT '-1',
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `codigo_articulo` (`codigo_articulo`),
  KEY `codigo_tipo_precio` (`codigo_precio`),
  CONSTRAINT `FKgaohhghg52245qi85xk13xf7q` FOREIGN KEY (`codigo_precio`) REFERENCES `precios` (`codigo_precio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `precios_programados` (
  `codigo_programado` int NOT NULL AUTO_INCREMENT,
  `codigo_articulo` int NOT NULL,
  `nuevo_precio` float NOT NULL,
  `precio_aplicado` int NOT NULL DEFAULT '0',
  `fecha` date NOT NULL,
  PRIMARY KEY (`codigo_programado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `proveedor` (
  `codigo_proveedor` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_proveedor` varchar(255) NOT NULL DEFAULT '',
  `telefono` varchar(9) NOT NULL DEFAULT 'NA',
  `celular` varchar(9) NOT NULL DEFAULT 'NA',
  `direccion` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `recibo_pago` (
  `no_recibo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL DEFAULT '1990-01-01 00:00:00',
  `codigo_cliente` int NOT NULL DEFAULT '-1',
  `total_letras` varchar(500) NOT NULL DEFAULT 'NA',
  `total` float(10,2) NOT NULL DEFAULT '0.00',
  `concepto` varchar(255) NOT NULL DEFAULT 'NA',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `saldo_anterio` float(10,2) NOT NULL,
  `saldo` float(10,2) NOT NULL,
  `ref` varchar(100) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`no_recibo`),
  UNIQUE KEY `codigo` (`no_recibo`),
  KEY `codigo_cliente` (`codigo_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `recibo_pago_proveedores` (
  `no_recibo` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL DEFAULT '1990-01-01 00:00:00',
  `codigo_proveedor` int NOT NULL DEFAULT '-1',
  `total_letras` varchar(500) NOT NULL DEFAULT 'NA',
  `total` float(10,2) NOT NULL DEFAULT '0.00',
  `concepto` varchar(255) NOT NULL DEFAULT 'NA',
  `usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',
  `saldo_anterio` float(10,2) NOT NULL,
  `saldo` float(10,2) NOT NULL,
  `codigo_tipo_pago` int NOT NULL DEFAULT '-1',
  PRIMARY KEY (`no_recibo`),
  UNIQUE KEY `codigo` (`no_recibo`),
  KEY `codigo_proveedor` (`codigo_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `rutas_cobro` (
  `codigo_ruta` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  `observaciones` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_ruta`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `rutas_entregas` (
  `id_entrega` int NOT NULL AUTO_INCREMENT,
  `id_vendedor` int NOT NULL DEFAULT '0',
  `fecha` date NOT NULL DEFAULT '1999-01-01',
  `estado` varchar(100) NOT NULL DEFAULT 'Creado',
  PRIMARY KEY (`id_entrega`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `salida_productos` (
  `no_documento` int unsigned NOT NULL,
  `codigo_articulo` int unsigned NOT NULL,
  `codigo_bodega` int unsigned NOT NULL,
  `cantidad` int unsigned NOT NULL,
  `fecha` datetime NOT NULL,
  `codigo_salida` int unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`codigo_salida`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 ROW_FORMAT=FIXED;

CREATE TABLE `salidas_caja` (
  `codigo_salida` int NOT NULL AUTO_INCREMENT,
  `concepto` varchar(400) NOT NULL DEFAULT 'NA',
  `cantidad` float(8,2) NOT NULL DEFAULT '0.00',
  `usuario` varchar(300) NOT NULL DEFAULT 'system',
  `fecha` datetime NOT NULL,
  `codigo_empleado` int NOT NULL DEFAULT '1',
  `estado` varchar(25) NOT NULL DEFAULT 'NA',
  `codigo_cuenta` int NOT NULL DEFAULT '-1',
  PRIMARY KEY (`codigo_salida`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_articulo` (
  `codigo_tipo_articulo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL,
  PRIMARY KEY (`codigo_tipo_articulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_cuenta_bancos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tipo_cuenta` varchar(100) NOT NULL,
  `observaciones` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_empleado` (
  `codigo_tipo` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_factura` (
  `id_tipo_factura` int NOT NULL,
  `tipo_factura` varchar(50) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`id_tipo_factura`),
  UNIQUE KEY `id_tipo_factura` (`id_tipo_factura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_movimiento_bancos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tipo_movimiento` varchar(100) NOT NULL,
  `observaciones` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_movimiento_kardex` (
  `codigo_tipo_moviemiento` int NOT NULL AUTO_INCREMENT,
  `movimiento` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`codigo_tipo_moviemiento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_pago` (
  `codigo_tipo_pago` int unsigned NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL DEFAULT 'NA',
  PRIMARY KEY (`codigo_tipo_pago`),
  UNIQUE KEY `id` (`codigo_tipo_pago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `tipo_venta` (
  `codigo` varchar(5) NOT NULL,
  `descripcion` varchar(255) DEFAULT 'NA',
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario` varchar(255) NOT NULL,
  `nombre_completo` varchar(255) NOT NULL DEFAULT 'NA',
  `clave` varchar(255) NOT NULL DEFAULT '',
  `permiso` varchar(255) NOT NULL DEFAULT 'NA',
  `tipo_permiso` int NOT NULL,
  `codigo_caja` int NOT NULL DEFAULT '0',
  `api_token` varchar(60) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `codigo_empleado` int DEFAULT '1',
  PRIMARY KEY (`id`,`usuario`,`clave`),
  UNIQUE KEY `usuario` (`usuario`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `usuario_api_token_uindex` (`api_token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

CREATE TABLE `usuarios_precios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_precio` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `FKbrb18nke07df543c9fc5v760j` (`codigo_precio`) USING BTREE,
  CONSTRAINT `FKbrb18nke07df543c9fc5v760j` FOREIGN KEY (`codigo_precio`) REFERENCES `precios` (`codigo_precio`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- VISTAS (47)
-- ============================================

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `articulo_view` AS select `articulo`.`codigo_articulo` AS `codigo_articulo`,`articulo`.`articulo` AS `articulo`,`articulo`.`codigo_marca` AS `codigo_marca`,`articulo`.`cod_articulo` AS `cod_articulo`,`articulo`.`codigo_impuesto` AS `codigo_impuesto`,`articulo`.`precio_articulo` AS `precio_articulo`,`articulo`.`tipo_articulo` AS `tipo_articulo`,`articulo`.`estado` AS `estado`,ifnull(`f_existencia_y_ordenes`(`articulo`.`codigo_articulo`,1),0) AS `existencia` from `articulo` where (`articulo`.`estado` = 1);

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `dev_compra` AS select `detalle_devoluciones_compra`.`fecha` AS `fecha_devolucion`,`encabezado_factura_compra`.`numero_compra` AS `numero_compra`,`detalle_devoluciones_compra`.`total` AS `total_dev`,`detalle_devoluciones_compra`.`cantidad` AS `cantidad`,`proveedor`.`codigo_proveedor` AS `codigo_proveedor`,`proveedor`.`nombre_proveedor` AS `nombre_proveedor`,`proveedor`.`telefono` AS `telefono`,`articulo`.`articulo` AS `articulo` from (((`encabezado_factura_compra` join `detalle_devoluciones_compra` on((`encabezado_factura_compra`.`numero_compra` = `detalle_devoluciones_compra`.`numero_factura`))) join `proveedor` on((`encabezado_factura_compra`.`codigo_proveedor` = `proveedor`.`codigo_proveedor`))) join `articulo` on((`detalle_devoluciones_compra`.`codigo_articulo` = `articulo`.`codigo_articulo`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_precios_general` AS select `precios_articulos`.`codigo_articulo` AS `codigo_articulo`,`precios_articulos`.`precio_articulo` AS `precio_articulo`,`precios_articulos`.`codigo_precio` AS `codigo_precio` from `precios_articulos` where (`precios_articulos`.`codigo_precio` = 1);

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_articulo_codigo_barra` AS select `articulo`.`codigo_articulo` AS `codigo_articulo`,`articulo`.`articulo` AS `articulo`,`marcas`.`descripcion` AS `marca`,`impuesto`.`porcentaje` AS `impuesto`,`marcas`.`codigo_marca` AS `codigo_marca`,`impuesto`.`codigo_impuesto` AS `codigo_impuesto`,`articulo`.`precio_articulo` AS `precio_articulo1`,`articulo`.`tipo_articulo` AS `tipo_articulo`,`codigos_articulos`.`codigo_barra` AS `codigo_barra`,`a`.`precio_articulo` AS `precio_articulo` from ((((`articulo` join `marcas` on((`marcas`.`codigo_marca` = `articulo`.`codigo_marca`))) join `impuesto` on((`impuesto`.`codigo_impuesto` = `articulo`.`codigo_impuesto`))) join `codigos_articulos` on((`articulo`.`codigo_articulo` = `codigos_articulos`.`codigo_articulo`))) join `v_precios_general` `a` on((`articulo`.`codigo_articulo` = `a`.`codigo_articulo`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_articulos` AS select `articulo`.`codigo_articulo` AS `codigo_articulo`,`articulo`.`articulo` AS `articulo`,`marcas`.`descripcion` AS `marca`,`impuesto`.`porcentaje` AS `impuesto`,`marcas`.`codigo_marca` AS `codigo_marca`,`impuesto`.`codigo_impuesto` AS `codigo_impuesto`,`articulo`.`precio_articulo` AS `precio_articulo1`,`articulo`.`tipo_articulo` AS `tipo_articulo`,`a`.`precio_articulo` AS `precio_articulo` from (((`articulo` join `marcas` on((`marcas`.`codigo_marca` = `articulo`.`codigo_marca`))) join `impuesto` on((`impuesto`.`codigo_impuesto` = `articulo`.`codigo_impuesto`))) join `v_precios_general` `a` on((`articulo`.`codigo_articulo` = `a`.`codigo_articulo`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_bodega` AS select `bodega`.`codigo_bodega` AS `codigo_bodega`,`bodega`.`descripcion_bodega` AS `descripcion_bodega` from `bodega`;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_cierre_caja` AS select `cierre_caja`.`idCierre` AS `idCierre`,date_format(`cierre_caja`.`fecha`,'%d/%m/%Y') AS `fecha`,`cierre_caja`.`fecha` AS `fecha2`,`cierre_caja`.`factura_inicial` AS `factura_inicial`,`cierre_caja`.`factura_final` AS `factura_final`,`cierre_caja`.`efectivo` AS `efectivo`,ifnull(`cierre_caja`.`creditos`,0) AS `creditos`,`cierre_caja`.`isv15` AS `isv15`,`cierre_caja`.`isv18` AS `isv18`,`cierre_caja`.`totalventa` AS `totalventa`,ifnull(`cierre_caja`.`totalimpuesto`,0) AS `totalimpuesto`,ifnull(`cierre_caja`.`tarjeta`,0) AS `tarjeta`,ifnull(`cierre_caja`.`usuario`,' ') AS `usuario` from `cierre_caja` order by `cierre_caja`.`idCierre` desc;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_clientes` AS select `cliente`.`codigo_cliente` AS `codigo_cliente`,`cliente`.`codigo_cliente` AS `codigo_cliente2`,`cliente`.`nombre_cliente` AS `nombre_cliente`,ifnull(`cliente`.`direccion`,'NA') AS `direccion`,ifnull(`cliente`.`telefono`,'NA') AS `telefono`,ifnull(`cliente`.`movil`,'NA') AS `movil`,ifnull(`cliente`.`rtn`,'NA') AS `rtn`,`cliente`.`zona_procedencia` AS `zona_procedencia`,`cliente`.`codigo_postal` AS `codigo_postal`,`cliente`.`codiciones_pago` AS `codiciones_pago`,ifnull(`cliente`.`limite_credito`,0) AS `limite_credito`,`cliente`.`saldo` AS `saldo2`,`cliente`.`clasificacion` AS `clasificacion`,`cliente`.`estado` AS `estado`,ifnull(`f_saldo_cliente`(`cliente`.`codigo_cliente`),0) AS `saldo`,`cliente`.`tipo_cliente` AS `tipo_cliente` from `cliente` order by `cliente`.`codigo_cliente`;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_encabezado_factura` AS select `encabezado_factura`.`fecha` AS `fecha1`,`encabezado_factura`.`fecha` AS `fecha2`,lpad(`encabezado_factura`.`numero_factura`,8,'0') AS `numero_factura`,lpad(`encabezado_factura`.`numero_factura`,8,'0') AS `numero_factura2`,date_format(`encabezado_factura`.`fecha`,'%d/%m/%Y') AS `fecha`,`cliente`.`codigo_cliente` AS `codigo_cliente`,`cliente`.`nombre_cliente` AS `nombre_cliente`,`cliente`.`direccion` AS `direccion`,`cliente`.`telefono` AS `telefono`,`cliente`.`movil` AS `movil`,`cliente`.`rtn` AS `rtn`,`encabezado_factura`.`subtotal` AS `subtotal`,`encabezado_factura`.`impuesto` AS `impuesto`,`encabezado_factura`.`total` AS `total`,`encabezado_factura`.`codigo` AS `codigo`,`encabezado_factura`.`estado_factura` AS `estado_factura`,`encabezado_factura`.`isv18` AS `isv18`,`encabezado_factura`.`usuario` AS `usuario`,`encabezado_factura`.`pago` AS `pago`,`encabezado_factura`.`descuento` AS `descuento`,`tipo_factura`.`tipo_factura` AS `tipo_factura`,(`encabezado_factura`.`pago` - `encabezado_factura`.`total`) AS `cambio`,`encabezado_factura`.`total_letras` AS `total_letras`,`tipo_pago`.`descripcion` AS `tipo_pago`,concat(`empleados`.`nombre`,' ',`empleados`.`apellido`) AS `vendedor`,`tipo_factura`.`id_tipo_factura` AS `id_tipo_factura`,`encabezado_factura`.`agrega_kardex` AS `agrega_kardex`,`encabezado_factura`.`subtotal_excento` AS `subtotal_excento`,`encabezado_factura`.`subtotal15` AS `subtotal15`,`encabezado_factura`.`subtotal18` AS `subtotal18`,`encabezado_factura`.`isvOtros` AS `isvOtros`,`encabezado_factura`.`cod_rango` AS `cod_rango`,`empleados`.`nombre` AS `nombre_vendedor`,`empleados`.`apellido` AS `apellido_vendedor`,`encabezado_factura`.`codigo_vendedor` AS `codigo_vendedor`,`encabezado_factura`.`cobro_tarjeta` AS `cobro_tarjeta`,`encabezado_factura`.`cobro_efectivo` AS `cobro_efectivo` from ((((`encabezado_factura` join `cliente` on((`cliente`.`codigo_cliente` = `encabezado_factura`.`codigo_cliente`))) join `tipo_factura` on((`tipo_factura`.`id_tipo_factura` = `encabezado_factura`.`tipo_factura`))) join `tipo_pago` on((`encabezado_factura`.`tipo_pago` = `tipo_pago`.`codigo_tipo_pago`))) join `empleados` on((`encabezado_factura`.`codigo_vendedor` = `empleados`.`codigo_empleado`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_comisiones` AS select max(`v_encabezado_factura`.`fecha`) AS `fecha_min`,min(`v_encabezado_factura`.`fecha`) AS `fecha_max`,`v_encabezado_factura`.`vendedor` AS `vendedor`,count(`v_encabezado_factura`.`codigo_cliente`) AS `no_clientes`,sum(`v_encabezado_factura`.`total`) AS `total_venta`,(sum(`v_encabezado_factura`.`total`) * 0.01) AS `comision` from `v_encabezado_factura` group by `v_encabezado_factura`.`vendedor`;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_contador_final` AS select min(`encabezado_factura`.`fecha`) AS `fecha`,max(`encabezado_factura`.`fecha`) AS `fecha2`,min(`encabezado_factura`.`numero_factura`) AS `no_factura_inicio`,max(`encabezado_factura`.`numero_factura`) AS `no_factura_final`,sum(`encabezado_factura`.`subtotal_excento`) AS `t_exectos`,sum(`encabezado_factura`.`impuesto`) AS `isv15`,sum(`encabezado_factura`.`subtotal15`) AS `total_isv15`,sum(`encabezado_factura`.`isv18`) AS `isv18`,sum(`encabezado_factura`.`subtotal18`) AS `total_isv18`,sum(`encabezado_factura`.`total`) AS `total_venta_dia` from `encabezado_factura` where (`encabezado_factura`.`estado_factura` = 'ACT') group by `encabezado_factura`.`fecha`;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_detalle_cotizacion` AS select `detalle_cotizacion`.`numero_cotizacion` AS `numero_cotizacion_detalle`,`articulo`.`articulo` AS `articulo`,`detalle_cotizacion`.`precio` AS `precio_detalle`,`detalle_cotizacion`.`cantidad` AS `cantidad_detalle`,`detalle_cotizacion`.`impuesto` AS `impuesto_detalle`,`detalle_cotizacion`.`descuento` AS `descuento_detalle`,`detalle_cotizacion`.`subtotal` AS `subtotal_detalle`,`detalle_cotizacion`.`total` AS `total_detalle`,`articulo`.`codigo_articulo` AS `codigo_articulo`,`detalle_cotizacion`.`id` AS `id`,`detalle_cotizacion`.`codigo_barra` AS `codigo_barra` from (`detalle_cotizacion` join `articulo` on((`articulo`.`codigo_articulo` = `detalle_cotizacion`.`codigo_articulo`)));

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_encabezado_cotizacion` AS select `encabezado_cotizacion`.`fecha` AS `fecha1`,`encabezado_cotizacion`.`fecha` AS `fecha2`,lpad(`encabezado_cotizacion`.`numero_cotizacion`,8,'0') AS `numero_cotizacion`,date_format(`encabezado_cotizacion`.`fecha`,'%d/%m/%Y') AS `fecha`,`cliente`.`codigo_cliente` AS `codigo_cliente`,`cliente`.`nombre_cliente` AS `nombre_cliente`,`cliente`.`direccion` AS `direccion`,`cliente`.`telefono` AS `telefono`,`cliente`.`movil` AS `movil`,`cliente`.`rtn` AS `rtn`,`encabezado_cotizacion`.`subtotal` AS `subtotal`,`encabezado_cotizacion`.`impuesto` AS `impuesto`,`encabezado_cotizacion`.`total` AS `total`,`encabezado_cotizacion`.`codigo` AS `codigo`,`encabezado_cotizacion`.`estado` AS `estado`,`encabezado_cotizacion`.`isv18` AS `isv18`,`encabezado_cotizacion`.`usuario` AS `usuario`,`encabezado_cotizacion`.`descuento` AS `descuento`,`encabezado_cotizacion`.`total_letras` AS `total_letras`,`encabezado_cotizacion`.`subtotal_excento` AS `subtotal_excento`,`encabezado_cotizacion`.`subtotal15` AS `subtotal15`,`encabezado_cotizacion`.`subtotal18` AS `subtotal18`,`encabezado_cotizacion`.`isvOtros` AS `isvOtros` from (`encabezado_cotizacion` join `cliente` on((`cliente`.`codigo_cliente` = `encabezado_cotizacion`.`codigo_cliente`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_cotizaciones` AS select `v_encabezado_cotizacion`.`fecha1` AS `fecha1`,`v_encabezado_cotizacion`.`fecha2` AS `fecha2`,`v_encabezado_cotizacion`.`numero_cotizacion` AS `numero_cotizacion`,`v_encabezado_cotizacion`.`fecha` AS `fecha`,`v_encabezado_cotizacion`.`codigo_cliente` AS `codigo_cliente`,`v_encabezado_cotizacion`.`nombre_cliente` AS `nombre_cliente`,`v_encabezado_cotizacion`.`direccion` AS `direccion`,`v_encabezado_cotizacion`.`telefono` AS `telefono`,`v_encabezado_cotizacion`.`movil` AS `movil`,`v_encabezado_cotizacion`.`rtn` AS `rtn`,`v_encabezado_cotizacion`.`subtotal` AS `subtotal`,`v_encabezado_cotizacion`.`impuesto` AS `impuesto`,`v_encabezado_cotizacion`.`total` AS `total`,`v_encabezado_cotizacion`.`codigo` AS `codigo`,`v_encabezado_cotizacion`.`estado` AS `estado`,`v_encabezado_cotizacion`.`isv18` AS `isv18`,`v_encabezado_cotizacion`.`usuario` AS `usuario`,`v_encabezado_cotizacion`.`descuento` AS `descuento`,`v_encabezado_cotizacion`.`total_letras` AS `total_letras`,`v_encabezado_cotizacion`.`subtotal_excento` AS `subtotal_excento`,`v_encabezado_cotizacion`.`subtotal15` AS `subtotal15`,`v_encabezado_cotizacion`.`subtotal18` AS `subtotal18`,`v_encabezado_cotizacion`.`isvOtros` AS `isvOtros`,`v_detalle_cotizacion`.`numero_cotizacion_detalle` AS `numero_cotizacion_detalle`,`v_detalle_cotizacion`.`articulo` AS `articulo`,`v_detalle_cotizacion`.`precio_detalle` AS `precio_detalle`,`v_detalle_cotizacion`.`cantidad_detalle` AS `cantidad_detalle`,`v_detalle_cotizacion`.`impuesto_detalle` AS `impuesto_detalle`,`v_detalle_cotizacion`.`descuento_detalle` AS `descuento_detalle`,`v_detalle_cotizacion`.`subtotal_detalle` AS `subtotal_detalle`,`v_detalle_cotizacion`.`total_detalle` AS `total_detalle`,`v_detalle_cotizacion`.`codigo_articulo` AS `codigo_articulo` from (`v_encabezado_cotizacion` join `v_detalle_cotizacion` on((`v_encabezado_cotizacion`.`numero_cotizacion` = `v_detalle_cotizacion`.`numero_cotizacion_detalle`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_detalle_devolucion` AS select `detalle_devoluciones`.`numero_factura` AS `numero_factura_detalle`,`articulo`.`articulo` AS `articulo`,`detalle_devoluciones`.`precio` AS `precio_detalle`,`detalle_devoluciones`.`cantidad` AS `cantidad_detalle`,`detalle_devoluciones`.`impuesto` AS `impuesto_detalle`,`detalle_devoluciones`.`descuento` AS `descuento_detalle`,`detalle_devoluciones`.`subtotal` AS `subtotal_detalle`,`detalle_devoluciones`.`total` AS `total_detalle`,`detalle_devoluciones`.`fecha` AS `fecha_devolucion` from (`detalle_devoluciones` join `articulo` on((`detalle_devoluciones`.`codigo_articulo` = `articulo`.`codigo_articulo`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_detalle_factura` AS select `detalle_factura`.`numero_factura` AS `numero_factura_detalle`,`articulo`.`articulo` AS `articulo`,`detalle_factura`.`precio` AS `precio_detalle`,`detalle_factura`.`cantidad` AS `cantidad_detalle`,`detalle_factura`.`impuesto` AS `impuesto_detalle`,`detalle_factura`.`descuento` AS `descuento_detalle`,`detalle_factura`.`subtotal` AS `subtotal_detalle`,`detalle_factura`.`total` AS `total_detalle`,`articulo`.`codigo_articulo` AS `codigo_articulo`,`detalle_factura`.`id` AS `id`,`detalle_factura`.`agrega_kardex` AS `agrega_kardex`,`detalle_factura`.`codigo_barra` AS `codigo_barra` from (`detalle_factura` join `articulo` on((`articulo`.`codigo_articulo` = `detalle_factura`.`codigo_articulo`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_detalle_factura_compra` AS select `detalle_factura_compra`.`numero_compra` AS `numero_compra_detalle`,`articulo`.`articulo` AS `articulo`,ifnull(`detalle_factura_compra`.`precio`,0) AS `precio_detalle`,ifnull(`detalle_factura_compra`.`cantidad`,0) AS `cantidad_detalle`,ifnull(`detalle_factura_compra`.`impuesto`,0) AS `impuesto_detalle`,ifnull(`detalle_factura_compra`.`subtotal`,0) AS `subtotal_detalle`,`detalle_factura_compra`.`codigo_articulo` AS `codigo_articulo`,`detalle_factura_compra`.`id_detalle_compra` AS `id_detalle_compra`,`detalle_factura_compra`.`precio` AS `precio`,`detalle_factura_compra`.`cantidad` AS `cantidad`,`detalle_factura_compra`.`impuesto` AS `impuesto`,`detalle_factura_compra`.`subtotal` AS `subtotal`,`detalle_factura_compra`.`agrega_kardex` AS `agrega_kardex`,`detalle_factura_compra`.`codigo_bodega` AS `codigo_bodega` from (`detalle_factura_compra` join `articulo` on((`articulo`.`codigo_articulo` = `detalle_factura_compra`.`codigo_articulo`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_detalle_factura_temp` AS select `detalle_factura_temp`.`numero_factura` AS `numero_factura_detalle`,`articulo`.`articulo` AS `articulo`,`detalle_factura_temp`.`precio` AS `precio_detalle`,`detalle_factura_temp`.`cantidad` AS `cantidad_detalle`,`detalle_factura_temp`.`impuesto` AS `impuesto_detalle`,`detalle_factura_temp`.`descuento` AS `descuento_detalle`,`detalle_factura_temp`.`subtotal` AS `subtotal_detalle`,`detalle_factura_temp`.`total` AS `total_detalle`,`articulo`.`codigo_articulo` AS `codigo_articulo`,`detalle_factura_temp`.`id` AS `id`,`impuesto`.`descripcion_impuesto` AS `descripcion_impuesto`,`impuesto`.`porcentaje` AS `impuesto`,`articulo`.`codigo_impuesto` AS `codigo_impuesto` from ((`detalle_factura_temp` join `articulo` on((`articulo`.`codigo_articulo` = `detalle_factura_temp`.`codigo_articulo`))) join `impuesto` on((`articulo`.`codigo_impuesto` = `impuesto`.`codigo_impuesto`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_kardex_ingresos` AS select `movimiento_kardex`.`cantidad` AS `can_ingreso`,`movimiento_kardex`.`codigo_movimiento` AS `codigo_movimiento`,`movimiento_kardex`.`codigo_tipo_movimiento` AS `codigo_tipo_movimiento`,`movimiento_kardex`.`precio_unidad` AS `precio_ingreso`,`movimiento_kardex`.`total` AS `total_ingreso` from `movimiento_kardex` where (`movimiento_kardex`.`codigo_tipo_movimiento` = 1) order by `movimiento_kardex`.`codigo_movimiento`;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_kardex_salidas` AS select `movimiento_kardex`.`cantidad` AS `can_salida`,`movimiento_kardex`.`codigo_movimiento` AS `codigo_movimiento`,`movimiento_kardex`.`codigo_tipo_movimiento` AS `codigo_tipo_movimiento`,`movimiento_kardex`.`precio_unidad` AS `precio_salida`,`movimiento_kardex`.`total` AS `total_salida` from `movimiento_kardex` where (`movimiento_kardex`.`codigo_tipo_movimiento` = 2) order by `movimiento_kardex`.`codigo_movimiento`;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_kardex_saldos` AS select `saldos`.`cantidad` AS `can_saldo`,`saldos`.`precio_unidad` AS `precio_saldo`,`saldos`.`total` AS `total_saldo`,`articulo_kardex`.`codigo_bodega` AS `codigo_bodega`,`articulo_kardex`.`codigo_articulo` AS `codigo_articulo`,`detalle_movimiento_kardex`.`codigo_movimiento` AS `codigo_movimiento` from ((`articulo_kardex` join `detalle_movimiento_kardex` on((`articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`))) join `movimiento_kardex` `saldos` on(((`detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`) and (`saldos`.`codigo_tipo_movimiento` = 3))));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_detalle_kardex` AS select `detalle_movimiento_kardex`.`codigo_kardex` AS `codigo_kardex`,`detalle_movimiento_kardex`.`fecha` AS `fecha`,`detalle_movimiento_kardex`.`descripcion` AS `descripcion`,`detalle_movimiento_kardex`.`no_documento` AS `no_documento`,ifnull(`v_kardex_ingresos`.`can_ingreso`,0) AS `can_ingreso`,ifnull(`v_kardex_ingresos`.`precio_ingreso`,0) AS `precio_ingreso`,ifnull(`v_kardex_ingresos`.`total_ingreso`,0) AS `total_ingreso`,ifnull(`v_kardex_salidas`.`can_salida`,0) AS `can_salida`,ifnull(`v_kardex_salidas`.`precio_salida`,0) AS `precio_salida`,ifnull(`v_kardex_salidas`.`total_salida`,0) AS `total_salida`,`v_kardex_saldos`.`can_saldo` AS `can_saldo`,`v_kardex_saldos`.`precio_saldo` AS `precio_saldo`,`v_kardex_saldos`.`total_saldo` AS `total_saldo` from (((`detalle_movimiento_kardex` left join `v_kardex_ingresos` on((`detalle_movimiento_kardex`.`codigo_movimiento` = `v_kardex_ingresos`.`codigo_movimiento`))) left join `v_kardex_salidas` on((`detalle_movimiento_kardex`.`codigo_movimiento` = `v_kardex_salidas`.`codigo_movimiento`))) left join `v_kardex_saldos` on((`detalle_movimiento_kardex`.`codigo_movimiento` = `v_kardex_saldos`.`codigo_movimiento`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_empleados` AS select `empleados`.`codigo_empleado` AS `codigo_empleado`,`empleados`.`nombre` AS `nombre`,`empleados`.`apellido` AS `apellido`,`empleados`.`telefono` AS `telefono`,`empleados`.`correo` AS `correo`,`empleados`.`direccion` AS `direccion`,`empleados`.`sueldo_base` AS `sueldo_base`,`empleados`.`codigo_tipo_empleado` AS `codigo_tipo_empleado`,`tipo_empleado`.`descripcion` AS `descripcion` from (`empleados` join `tipo_empleado` on((`empleados`.`codigo_tipo_empleado` = `tipo_empleado`.`codigo_tipo`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_encabezado_factura_compra` AS select `encabezado_factura_compra`.`numero_compra` AS `numero_compra`,`encabezado_factura_compra`.`no_factura_compra` AS `no_factura_compra`,date_format(`encabezado_factura_compra`.`fecha`,'%d/%m/%Y') AS `fecha`,`proveedor`.`codigo_proveedor` AS `codigo_proveedor`,`proveedor`.`nombre_proveedor` AS `nombre_proveedor`,`proveedor`.`direccion` AS `direccion`,`proveedor`.`telefono` AS `telefono`,`proveedor`.`celular` AS `celular`,`encabezado_factura_compra`.`subtotal` AS `subtotal`,`encabezado_factura_compra`.`impuesto` AS `impuesto`,`encabezado_factura_compra`.`total` AS `total`,`encabezado_factura_compra`.`estado_factura` AS `estado_factura`,`encabezado_factura_compra`.`isv18` AS `isv18`,`encabezado_factura_compra`.`usuario` AS `usuario`,`encabezado_factura_compra`.`pago` AS `pago`,`tipo_factura`.`tipo_factura` AS `tipo_factura`,date_format(`encabezado_factura_compra`.`fecha_ingreso`,'%d/%m/%Y') AS `fecha_ingreso`,date_format(`encabezado_factura_compra`.`fecha_vencimiento`,'%d/%m/%Y') AS `fecha_vencimiento`,`encabezado_factura_compra`.`agrega_kardex` AS `agrega_kardex` from ((`encabezado_factura_compra` join `proveedor` on((`proveedor`.`codigo_proveedor` = `encabezado_factura_compra`.`codigo_proveedor`))) join `tipo_factura` on((`tipo_factura`.`id_tipo_factura` = `encabezado_factura_compra`.`tipo_factura`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_encabezado_factura_orden` AS select `encabezado_factura`.`numero_factura` AS `numero_factura`,`encabezado_factura`.`fecha` AS `fecha`,`encabezado_factura`.`subtotal_excento` AS `subtotal_excento`,`encabezado_factura`.`subtotal15` AS `subtotal15`,`encabezado_factura`.`subtotal18` AS `subtotal18`,`encabezado_factura`.`subtotal` AS `subtotal`,`encabezado_factura`.`impuesto` AS `impuesto`,`encabezado_factura`.`total` AS `total`,`encabezado_factura`.`codigo_cliente` AS `codigo_cliente`,`encabezado_factura`.`codigo` AS `codigo`,`encabezado_factura`.`estado_factura` AS `estado_factura`,`encabezado_factura`.`isvOtros` AS `isvOtros`,`encabezado_factura`.`isv18` AS `isv18`,`encabezado_factura`.`usuario` AS `usuario`,`encabezado_factura`.`pago` AS `pago`,`encabezado_factura`.`descuento` AS `descuento`,`encabezado_factura`.`tipo_factura` AS `tipo_factura`,`encabezado_factura`.`agrega_kardex` AS `agrega_kardex`,`encabezado_factura`.`tipo_pago` AS `tipo_pago`,`encabezado_factura`.`observacion` AS `observacion`,`encabezado_factura`.`total_letras` AS `total_letras`,`encabezado_factura`.`codigo_vendedor` AS `codigo_vendedor`,`encabezado_factura`.`estado_pago` AS `estado_pago`,`encabezado_factura`.`cod_rango` AS `cod_rango`,`encabezado_factura`.`cobro_tarjeta` AS `cobro_tarjeta`,`encabezado_factura`.`cobro_efectivo` AS `cobro_efectivo` from `encabezado_factura` order by `encabezado_factura`.`numero_factura` desc;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_encabezado_factura_temp` AS select `encabezado_factura_temp`.`numero_factura` AS `numero_factura`,`encabezado_factura_temp`.`fecha` AS `fecha1`,date_format(`encabezado_factura_temp`.`fecha`,'%d/%m/%Y') AS `fecha`,`encabezado_factura_temp`.`subtotal_excento` AS `subtotal_excento`,`encabezado_factura_temp`.`subtotal15` AS `subtotal15`,`encabezado_factura_temp`.`subtotal18` AS `subtotal18`,`encabezado_factura_temp`.`subtotal` AS `subtotal`,`encabezado_factura_temp`.`impuesto` AS `impuesto`,`encabezado_factura_temp`.`total` AS `total`,`cliente`.`codigo_cliente` AS `codigo_cliente`,`cliente`.`nombre_cliente` AS `nombre_cliente`,`encabezado_factura_temp`.`codigo` AS `codigo`,`encabezado_factura_temp`.`estado_factura` AS `estado_factura`,`encabezado_factura_temp`.`isvOtros` AS `isvOtros`,`encabezado_factura_temp`.`isv18` AS `isv18`,`encabezado_factura_temp`.`usuario` AS `usuario`,`encabezado_factura_temp`.`pago` AS `pago`,`encabezado_factura_temp`.`descuento` AS `descuento`,`encabezado_factura_temp`.`tipo_factura` AS `tipo_factura`,`cliente`.`rtn` AS `rtn` from (`encabezado_factura_temp` join `cliente` on((`encabezado_factura_temp`.`codigo_cliente` = `cliente`.`codigo_cliente`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_kardex` AS select `articulo`.`articulo` AS `articulo`,`articulo`.`codigo_marca` AS `codigo_marca`,`detalle_movimiento_kardex`.`codigo_kardex` AS `cod`,`detalle_movimiento_kardex`.`codigo_movimiento` AS `codigo_movimiento`,`detalle_movimiento_kardex`.`fecha` AS `fecha`,`detalle_movimiento_kardex`.`descripcion` AS `descripcion`,`detalle_movimiento_kardex`.`no_documento` AS `no_documento`,ifnull(`entradas`.`cantidad`,0) AS `can_ingreso`,ifnull(`entradas`.`precio_unidad`,0) AS `precio_ingreso`,ifnull(`entradas`.`total`,0) AS `total_ingreso`,ifnull(`salidas`.`cantidad`,0) AS `can_salida`,ifnull(`salidas`.`precio_unidad`,0) AS `precio_salida`,ifnull(`salidas`.`total`,0) AS `total_salida`,`saldos`.`cantidad` AS `can_saldo`,`saldos`.`precio_unidad` AS `precio_saldo`,`saldos`.`total` AS `total_saldo`,`bodega`.`codigo_bodega` AS `codigo_bodega`,`bodega`.`descripcion_bodega` AS `descripcion_bodega`,`articulo_kardex`.`codigo_articulo` AS `codigo_articulo`,`articulo_kardex`.`cantidad_maxima` AS `cantidad_maxima`,`articulo_kardex`.`cantidad_minima` AS `cantidad_minima`,`articulo_kardex`.`metodo` AS `metodo` from ((((((`articulo_kardex` join `detalle_movimiento_kardex` on((`articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`))) join `movimiento_kardex` `saldos` on(((`detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`) and (`saldos`.`codigo_tipo_movimiento` = 3)))) join `articulo` on((`articulo`.`codigo_articulo` = `articulo_kardex`.`codigo_articulo`))) join `bodega` on((`bodega`.`codigo_bodega` = `articulo_kardex`.`codigo_bodega`))) left join `movimiento_kardex` `entradas` on(((`detalle_movimiento_kardex`.`codigo_movimiento` = `entradas`.`codigo_movimiento`) and (`entradas`.`codigo_tipo_movimiento` = 1)))) left join `movimiento_kardex` `salidas` on(((`detalle_movimiento_kardex`.`codigo_movimiento` = `salidas`.`codigo_movimiento`) and (`salidas`.`codigo_tipo_movimiento` = 2)))) where (`articulo`.`estado` = 1);

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_existencia` AS select `t1`.`cod` AS `cod`,`t1`.`codigo_movimiento` AS `codigo_movimiento`,`t1`.`codigo_marca` AS `codigo_marca`,`t1`.`fecha` AS `fecha`,`t1`.`descripcion` AS `descripcion`,`t1`.`no_documento` AS `no_documento`,`t1`.`can_saldo` AS `can_saldo`,`t1`.`precio_saldo` AS `precio_saldo`,`t1`.`total_saldo` AS `total_saldo`,`t1`.`codigo_bodega` AS `codigo_bodega`,`t1`.`descripcion_bodega` AS `descripcion_bodega`,`t1`.`cantidad_maxima` AS `cantidad_maxima`,`t1`.`cantidad_minima` AS `cantidad_minima`,`t1`.`metodo` AS `metodo`,`t1`.`codigo_articulo` AS `codigo_articulo`,`t1`.`articulo` AS `articulo`,now() AS `fecha_report` from (`v_kardex` `t1` join (select `dmk`.`codigo_kardex` AS `codigo_kardex`,max(`dmk`.`codigo_movimiento`) AS `ultimo_movimiento` from (`detalle_movimiento_kardex` `dmk` join `movimiento_kardex` `mk` on((`dmk`.`codigo_movimiento` = `mk`.`codigo_movimiento`))) where (`mk`.`codigo_tipo_movimiento` = 3) group by `dmk`.`codigo_kardex`) `ultimos_mov` on(((`t1`.`cod` = `ultimos_mov`.`codigo_kardex`) and (`t1`.`codigo_movimiento` = `ultimos_mov`.`ultimo_movimiento`))));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_existencia_alert` AS select `mk`.`cantidad` AS `can_saldo`,`ak`.`codigo_bodega` AS `codigo_bodega`,`ak`.`codigo_articulo` AS `codigo_articulo`,`dmk`.`codigo_movimiento` AS `codigo_movimiento`,`a`.`articulo` AS `articulo`,`ak`.`codigo_kardex` AS `cod`,`dmk`.`fecha` AS `fecha`,`dmk`.`descripcion` AS `descripcion`,`dmk`.`no_documento` AS `no_documento`,`mk`.`precio_unidad` AS `precio_saldo`,`mk`.`total` AS `total_saldo`,`b`.`descripcion_bodega` AS `descripcion_bodega`,`ak`.`cantidad_maxima` AS `cantidad_maxima`,`ak`.`cantidad_minima` AS `cantidad_minima`,`ak`.`metodo` AS `metodo` from (((((`articulo_kardex` `ak` join `detalle_movimiento_kardex` `dmk` on((`ak`.`codigo_kardex` = `dmk`.`codigo_kardex`))) join (select `detalle_movimiento_kardex`.`codigo_kardex` AS `codigo_kardex`,max(`detalle_movimiento_kardex`.`codigo_movimiento`) AS `ultimo_movimiento` from `detalle_movimiento_kardex` group by `detalle_movimiento_kardex`.`codigo_kardex`) `ultimos_mov` on(((`dmk`.`codigo_kardex` = `ultimos_mov`.`codigo_kardex`) and (`dmk`.`codigo_movimiento` = `ultimos_mov`.`ultimo_movimiento`)))) join `movimiento_kardex` `mk` on((`dmk`.`codigo_movimiento` = `mk`.`codigo_movimiento`))) join `articulo` `a` on((`ak`.`codigo_articulo` = `a`.`codigo_articulo`))) join `bodega` `b` on((`ak`.`codigo_bodega` = `b`.`codigo_bodega`))) where ((`mk`.`codigo_tipo_movimiento` = 3) and (`mk`.`cantidad` < 0) and (`mk`.`cantidad` < `ak`.`cantidad_minima`) and (`a`.`estado` = 1));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_existencia_alerta` AS select `vk`.`cod` AS `cod`,`vk`.`codigo_movimiento` AS `codigo_movimiento`,`vk`.`fecha` AS `fecha`,`vk`.`descripcion` AS `descripcion`,`vk`.`no_documento` AS `no_documento`,`vk`.`can_ingreso` AS `can_ingreso`,`vk`.`precio_ingreso` AS `precio_ingreso`,`vk`.`total_ingreso` AS `total_ingreso`,`vk`.`can_salida` AS `can_salida`,`vk`.`precio_salida` AS `precio_salida`,`vk`.`total_salida` AS `total_salida`,`vk`.`can_saldo` AS `can_saldo`,`vk`.`precio_saldo` AS `precio_saldo`,`vk`.`total_saldo` AS `total_saldo`,`vk`.`codigo_bodega` AS `codigo_bodega`,`vk`.`descripcion_bodega` AS `descripcion_bodega`,`vk`.`cantidad_maxima` AS `cantidad_maxima`,`vk`.`cantidad_minima` AS `cantidad_minima`,`vk`.`metodo` AS `metodo`,`vk`.`codigo_articulo` AS `codigo_articulo`,`vk`.`articulo` AS `articulo` from (`v_kardex` `vk` join (select `detalle_movimiento_kardex`.`codigo_kardex` AS `codigo_kardex`,max(`detalle_movimiento_kardex`.`codigo_movimiento`) AS `ultimo_movimiento` from `detalle_movimiento_kardex` group by `detalle_movimiento_kardex`.`codigo_kardex`) `ultimos_mov` on((`vk`.`codigo_movimiento` = `ultimos_mov`.`ultimo_movimiento`))) where ((`vk`.`can_saldo` <= `vk`.`cantidad_minima`) and (`vk`.`can_saldo` < 0)) order by `vk`.`codigo_movimiento` desc;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_factura` AS select `cliente`.`nombre_cliente` AS `nombre_cliente`,`encabezado_factura`.`numero_factura` AS `numero_factura`,`encabezado_factura`.`fecha` AS `fecha`,`encabezado_factura`.`subtotal` AS `subtotal_Total`,`encabezado_factura`.`impuesto` AS `Impuesto_Total`,`encabezado_factura`.`total` AS `total`,`encabezado_factura`.`estado_factura` AS `estado_factura`,`encabezado_factura`.`isv18` AS `isv18`,`articulo`.`articulo` AS `articulo`,`detalle_factura`.`precio` AS `precio`,`detalle_factura`.`cantidad` AS `cantidad`,`detalle_factura`.`impuesto` AS `impuesto`,`detalle_factura`.`subtotal` AS `subtotal`,`encabezado_factura`.`observacion` AS `observacion` from (((`articulo` join `detalle_factura` on((`detalle_factura`.`codigo_articulo` = `articulo`.`codigo_articulo`))) join `encabezado_factura` on((`encabezado_factura`.`numero_factura` = `detalle_factura`.`numero_factura`))) join `cliente` on((`encabezado_factura`.`codigo_cliente` = `cliente`.`codigo_cliente`)));

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_facturas` AS select `v_encabezado_factura`.`numero_factura` AS `numero_factura`,`v_encabezado_factura`.`fecha` AS `fecha`,`v_encabezado_factura`.`fecha2` AS `fecha2`,`v_encabezado_factura`.`fecha1` AS `fecha1`,`v_encabezado_factura`.`codigo_cliente` AS `codigo_cliente`,`v_encabezado_factura`.`nombre_cliente` AS `nombre_cliente`,`v_encabezado_factura`.`direccion` AS `direccion`,`v_encabezado_factura`.`telefono` AS `telefono`,`v_encabezado_factura`.`movil` AS `movil`,`v_encabezado_factura`.`pago` AS `pago`,`v_encabezado_factura`.`usuario` AS `usuario`,`v_encabezado_factura`.`isv18` AS `isv18`,`v_encabezado_factura`.`estado_factura` AS `estado_factura`,`v_encabezado_factura`.`codigo` AS `codigo`,`v_encabezado_factura`.`total` AS `total`,`v_encabezado_factura`.`impuesto` AS `impuesto`,`v_encabezado_factura`.`subtotal` AS `subtotal`,`v_encabezado_factura`.`rtn` AS `rtn`,`v_detalle_factura`.`articulo` AS `articulo`,`v_detalle_factura`.`precio_detalle` AS `precio_detalle`,`v_detalle_factura`.`cantidad_detalle` AS `cantidad_detalle`,`v_detalle_factura`.`impuesto_detalle` AS `impuesto_detalle`,`v_detalle_factura`.`descuento_detalle` AS `descuento_detalle`,`v_detalle_factura`.`subtotal_detalle` AS `subtotal_detalle`,`v_detalle_factura`.`total_detalle` AS `total_detalle`,`v_detalle_factura`.`codigo_articulo` AS `codigo_articulo`,`v_encabezado_factura`.`descuento` AS `descuento`,`v_encabezado_factura`.`tipo_factura` AS `tipo_factura`,`v_encabezado_factura`.`cambio` AS `cambio`,`v_encabezado_factura`.`total_letras` AS `total_letras`,`v_encabezado_factura`.`tipo_pago` AS `tipo_pago`,`v_encabezado_factura`.`vendedor` AS `vendedor`,`v_encabezado_factura`.`id_tipo_factura` AS `id_tipo_factura`,`v_encabezado_factura`.`agrega_kardex` AS `agrega_kardex`,`v_detalle_factura`.`numero_factura_detalle` AS `numero_factura_detalle`,`v_encabezado_factura`.`subtotal_excento` AS `subtotal_excento`,`v_encabezado_factura`.`subtotal15` AS `subtotal15`,`v_encabezado_factura`.`subtotal18` AS `subtotal18`,`v_encabezado_factura`.`isvOtros` AS `isvOtros`,`v_encabezado_factura`.`cod_rango` AS `cod_rango`,`v_encabezado_factura`.`nombre_vendedor` AS `nombre_vendedor`,`v_encabezado_factura`.`apellido_vendedor` AS `apellido_vendedor`,`v_encabezado_factura`.`codigo_vendedor` AS `codigo_vendedor`,`v_encabezado_factura`.`numero_factura2` AS `numero_factura2` from (`v_detalle_factura` join `v_encabezado_factura` on((`v_detalle_factura`.`numero_factura_detalle` = `v_encabezado_factura`.`numero_factura`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_precios` AS select `precios`.`codigo_precio` AS `codigo_precio`,`precios`.`descripcion` AS `descripcion`,`precios_articulos`.`codigo_articulo` AS `codigo_articulo`,`precios_articulos`.`precio_articulo` AS `precio_articulo` from (`precios` join `precios_articulos` on((`precios`.`codigo_precio` = `precios_articulos`.`codigo_precio`))) order by `precios`.`codigo_precio`;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_precios_costos` AS select `precios_articulos`.`precio_articulo` AS `precio_articulo`,`precios_articulos`.`codigo_precio` AS `codigo_precio`,`precios_articulos`.`codigo_articulo` AS `codigo_articulo` from `precios_articulos` where (`precios_articulos`.`codigo_precio` = 4) order by `precios_articulos`.`codigo_articulo`;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_precios_publico` AS select `precios_articulos`.`precio_articulo` AS `precio_articulo`,`precios_articulos`.`codigo_precio` AS `codigo_precio`,`precios_articulos`.`codigo_articulo` AS `codigo_articulo` from `precios_articulos` where (`precios_articulos`.`codigo_precio` = 1) order by `precios_articulos`.`codigo_articulo`;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_proveedores` AS select `f_saldo_proveedor`(`proveedor`.`codigo_proveedor`) AS `saldo`,`proveedor`.`codigo_proveedor` AS `codigo_proveedor`,`proveedor`.`nombre_proveedor` AS `nombre_proveedor`,`proveedor`.`telefono` AS `telefono`,`proveedor`.`celular` AS `celular`,`proveedor`.`direccion` AS `direccion` from `proveedor`;

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_reporte_contador` AS select `encabezado_factura`.`fecha` AS `fecha`,`articulo`.`articulo` AS `articulo`,`impuesto`.`porcentaje` AS `porcentaje`,`detalle_factura`.`subtotal` AS `subtotal`,`detalle_factura`.`impuesto` AS `impuesto`,`detalle_factura`.`total` AS `total`,`impuesto`.`codigo_impuesto` AS `codigo_impuesto`,`encabezado_factura`.`numero_factura` AS `numero_factura` from (((`detalle_factura` join `encabezado_factura` on((`detalle_factura`.`numero_factura` = `encabezado_factura`.`numero_factura`))) join `articulo` on((`articulo`.`codigo_articulo` = `detalle_factura`.`codigo_articulo`))) join `impuesto` on((`articulo`.`codigo_impuesto` = `impuesto`.`codigo_impuesto`)));

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_rangos_facturas` AS select `v_reporte_contador`.`fecha` AS `fecha`,min(`v_reporte_contador`.`numero_factura`) AS `no_factura_inicio`,max(`v_reporte_contador`.`numero_factura`) AS `no_factura_final`,sum(`v_reporte_contador`.`total`) AS `total_venta_dia` from `v_reporte_contador` group by `v_reporte_contador`.`fecha`;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_recibo_pago_cuenta` AS select `recibo_pago`.`no_recibo` AS `no_recibo`,`recibo_pago`.`fecha` AS `fecha`,`recibo_pago`.`codigo_cliente` AS `codigo_cliente`,`recibo_pago`.`total_letras` AS `total_letras`,`recibo_pago`.`total` AS `total`,`recibo_pago`.`saldo_anterio` AS `saldo_anterio`,`recibo_pago`.`saldo` AS `saldo`,`recibo_pago`.`concepto` AS `concepto`,`recibo_pago`.`usuario` AS `usuario`,`cliente`.`nombre_cliente` AS `nombre_cliente` from (`recibo_pago` join `cliente` on((`recibo_pago`.`codigo_cliente` = `cliente`.`codigo_cliente`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_recibo_pago_proveedor` AS select `recibo_pago_proveedores`.`no_recibo` AS `no_recibo`,`recibo_pago_proveedores`.`fecha` AS `fecha`,`recibo_pago_proveedores`.`total_letras` AS `total_letras`,`recibo_pago_proveedores`.`total` AS `total`,`recibo_pago_proveedores`.`saldo_anterio` AS `saldo_anterio`,`recibo_pago_proveedores`.`saldo` AS `saldo`,`recibo_pago_proveedores`.`concepto` AS `concepto`,`recibo_pago_proveedores`.`usuario` AS `usuario`,`recibo_pago_proveedores`.`codigo_proveedor` AS `codigo_proveedor`,`proveedor`.`nombre_proveedor` AS `nombre_proveedor`,`proveedor`.`telefono` AS `telefono`,`proveedor`.`celular` AS `celular`,`proveedor`.`direccion` AS `direccion`,`recibo_pago_proveedores`.`codigo_tipo_pago` AS `codigo_tipo_pago`,`bancos`.`nombre` AS `forma_pago`,`bancos`.`no_cuenta` AS `no_cuenta`,`tipo_cuenta_bancos`.`tipo_cuenta` AS `tipo_cuenta`,`tipo_cuenta_bancos`.`observaciones` AS `observaciones`,`bancos`.`id_tipo_cuenta` AS `id_tipo_cuenta` from (((`recibo_pago_proveedores` join `proveedor` on((`recibo_pago_proveedores`.`codigo_proveedor` = `proveedor`.`codigo_proveedor`))) join `bancos` on((`recibo_pago_proveedores`.`codigo_tipo_pago` = `bancos`.`id`))) join `tipo_cuenta_bancos` on((`bancos`.`id_tipo_cuenta` = `tipo_cuenta_bancos`.`id`)));

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_requisiciones` AS select `encabezado_requisicion`.`codigo_requisicion` AS `codigo_requisicion`,date_format(`encabezado_requisicion`.`fecha`,'%d/%m/%Y') AS `fecha`,`encabezado_requisicion`.`total` AS `total`,`encabezado_requisicion`.`usuario` AS `usuario`,`encabezado_requisicion`.`agrega_kardex` AS `agrega_kardex`,`encabezado_requisicion`.`estado_requisicion` AS `estado_requisicion`,`encabezado_requisicion`.`codigo_depart_destino` AS `idDestino`,`encabezado_requisicion`.`codigo_depart_origen` AS `idOrigen`,`encabezado_requisicion`.`fecha` AS `fecha2`,`bodega`.`descripcion_bodega` AS `destino`,`v_bodega`.`descripcion_bodega` AS `origen` from ((`encabezado_requisicion` join `bodega` on((`encabezado_requisicion`.`codigo_depart_destino` = `bodega`.`codigo_bodega`))) join `v_bodega` on((`encabezado_requisicion`.`codigo_depart_origen` = `v_bodega`.`codigo_bodega`))) order by `encabezado_requisicion`.`codigo_requisicion` desc;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_saldo_cliente` AS select `cuentas_por_cobrar`.`saldo` AS `saldo`,`cuentas_por_cobrar`.`codigo_cliente` AS `codigo_cliente`,`cuentas_por_cobrar`.`codigo_reguistro` AS `codigo_reguistro`,`cuentas_por_cobrar`.`fecha` AS `fecha`,`cuentas_por_cobrar`.`debito` AS `debito`,`cuentas_por_cobrar`.`credito` AS `credito`,`cuentas_por_cobrar`.`descripcion` AS `descripcion`,`cliente`.`nombre_cliente` AS `nombre_cliente`,`cliente`.`direccion` AS `direccion`,`cliente`.`rtn` AS `rtn`,`cliente`.`telefono` AS `telefono`,`cliente`.`movil` AS `movil` from (`cuentas_por_cobrar` join `cliente` on((`cuentas_por_cobrar`.`codigo_cliente` = `cliente`.`codigo_cliente`))) order by `cuentas_por_cobrar`.`codigo_reguistro` desc;

CREATE ALGORITHM=MERGE SQL SECURITY DEFINER VIEW `v_saldo_proveedor` AS select `cuentas_por_pagar`.`codigo_reguistro` AS `codigo_reguistro`,`cuentas_por_pagar`.`fecha` AS `fecha`,`cuentas_por_pagar`.`codigo_proveedor` AS `codigo_proveedor`,`cuentas_por_pagar`.`descripcion` AS `descripcion`,`cuentas_por_pagar`.`debito` AS `debito`,`cuentas_por_pagar`.`credito` AS `credito`,`cuentas_por_pagar`.`saldo` AS `saldo`,`proveedor`.`nombre_proveedor` AS `nombre_proveedor`,`proveedor`.`telefono` AS `telefono`,`proveedor`.`celular` AS `celular`,`proveedor`.`direccion` AS `direccion` from (`cuentas_por_pagar` join `proveedor` on((`cuentas_por_pagar`.`codigo_proveedor` = `proveedor`.`codigo_proveedor`))) order by `cuentas_por_pagar`.`codigo_reguistro` desc;

CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_salidas` AS select `salidas_caja`.`codigo_salida` AS `codigo_salida`,`salidas_caja`.`concepto` AS `concepto`,`salidas_caja`.`cantidad` AS `cantidad`,`salidas_caja`.`usuario` AS `usuario`,`empleados`.`codigo_empleado` AS `codigo_empleado`,`empleados`.`nombre` AS `nombre`,`empleados`.`apellido` AS `apellido`,date_format(`salidas_caja`.`fecha`,'%d/%m/%Y') AS `fecha`,`salidas_caja`.`fecha` AS `fecha1` from (`salidas_caja` join `empleados` on((`salidas_caja`.`codigo_empleado` = `empleados`.`codigo_empleado`)));

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_total_excentos` AS select `v_reporte_contador`.`fecha` AS `fecha`,sum(`v_reporte_contador`.`total`) AS `t_exectos` from `v_reporte_contador` where (`v_reporte_contador`.`codigo_impuesto` = 1) group by `v_reporte_contador`.`fecha`;

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_total_isv15` AS select `v_reporte_contador`.`fecha` AS `fecha`,sum(`v_reporte_contador`.`total`) AS `total_isv15`,(sum(`v_reporte_contador`.`total`) * 0.15) AS `isv15` from `v_reporte_contador` where (`v_reporte_contador`.`codigo_impuesto` = 2) group by `v_reporte_contador`.`fecha`;

CREATE ALGORITHM=TEMPTABLE SQL SECURITY DEFINER VIEW `v_total_isv18` AS select `v_reporte_contador`.`fecha` AS `fecha`,sum(`v_reporte_contador`.`total`) AS `total_isv18`,(sum(`v_reporte_contador`.`total`) * 0.18) AS `isv18` from `v_reporte_contador` where (`v_reporte_contador`.`codigo_impuesto` = 3) group by `v_reporte_contador`.`fecha`;

-- ============================================
-- FUNCTIONS (23)
-- ============================================

DELIMITER $$
CREATE FUNCTION `f_can_saldo_kardex`(p_cod_articulo int(11), p_cod_bodega int(11)) RETURNS double(11,2)
BEGIN

#return (SELECT
#	movimiento_kardex.cantidad
#FROM
#	articulo_kardex
#INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
#INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

#WHERE	
#	movimiento_kardex.codigo_tipo_movimiento=3
#	AND
#	articulo_kardex.codigo_kardex=p_cod_articulo
#	AND
#	articulo_kardex.codigo_bodega=p_cod_bodega

#	ORDER BY
#		movimiento_kardex.codigo_movimiento DESC
#	limit 1);

return (SELECT
	`saldos`.`cantidad` AS `can_saldo`
FROM
	
		
			`articulo_kardex`
			JOIN `detalle_movimiento_kardex` ON(
				
					`articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`
				
			)
	
		JOIN `movimiento_kardex` `saldos` ON(
			
				
					`detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`
				
				AND
					`saldos`.`codigo_tipo_movimiento` = 3
				
			
		)
	
WHERE
	`articulo_kardex`.`codigo_articulo` = p_cod_articulo and `articulo_kardex`.`codigo_bodega`=p_cod_bodega
ORDER BY detalle_movimiento_kardex.codigo_movimiento desc limit 1);
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_costo_dev`(p_numero_factura int(11), p_codigo int(11)) RETURNS double(11,2)
BEGIN
	return (SELECT

	SUM(
		detalle_devoluciones.cantidad * precios_articulos.precio_articulo
	) AS total_dev_costo
FROM
	detalle_devoluciones
INNER JOIN precios_articulos ON (
	detalle_devoluciones.codigo_articulo = precios_articulos.codigo_articulo
	AND precios_articulos.codigo_precio = 4
)
WHERE
	detalle_devoluciones.codigo_articulo= p_codigo and detalle_devoluciones.numero_factura=p_numero_factura) ;
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_descripcion_saldo_kardex`(p_cod_kardex int(11)) RETURNS varchar(150) CHARSET utf8mb3
BEGIN

return (SELECT
	detalle_movimiento_kardex.descripcion
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

WHERE	
	movimiento_kardex.codigo_tipo_movimiento=3
	AND
	articulo_kardex.codigo_kardex=p_cod_kardex

	ORDER BY
		movimiento_kardex.codigo_movimiento DESC
	limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_detalle_cuenta_factura`(p_id_cuenta int(11)) RETURNS varchar(260) CHARSET utf8mb3
BEGIN

	return (select descripcion from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and tipo_movimiento=1  ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro ASC LIMIT 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_existencia_articulo`(p_codigo_articulo int(11), p_codigo_bodega int(11)) RETURNS double(11,2)
    NO SQL
BEGIN
	return (SELECT
	movimiento_kardex.cantidad AS can_saldo
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
WHERE
	(
		`movimiento_kardex`.`codigo_tipo_movimiento` = 3
		AND articulo_kardex.codigo_articulo = p_codigo_articulo
		AND articulo_kardex.codigo_bodega = p_codigo_bodega	) ORDER BY movimiento_kardex.codigo_movimiento desc limit 1) ;
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_existencia_y_ordenes`(p_cod_articulo int(11), p_cod_bodega int(11)) RETURNS decimal(10,2)
    DETERMINISTIC
BEGIN

 DECLARE can_saldo DECIMAL(10,2) DEFAULT 0;
    DECLARE total_pedidos DECIMAL(10,2) DEFAULT 0;
    DECLARE resultado_total DECIMAL(10,2);

    -- Consulta 1: Obtener el saldo
    SELECT `saldos`.`cantidad` INTO can_saldo
    FROM `articulo_kardex`
    JOIN `detalle_movimiento_kardex` 
        ON `articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`
    JOIN `movimiento_kardex` `saldos` 
        ON `detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`
        AND `saldos`.`codigo_tipo_movimiento` = 3
    WHERE `articulo_kardex`.`codigo_articulo` = p_cod_articulo
      AND `articulo_kardex`.`codigo_bodega` = p_cod_bodega
    ORDER BY `detalle_movimiento_kardex`.`codigo_movimiento` DESC
    LIMIT 1;
		
		
		-- Consulta 2: Obtener el total de pedidos
    SELECT SUM(detalle_factura_temp.cantidad) INTO total_pedidos
    FROM
	detalle_factura_temp
	INNER JOIN
	encabezado_factura_temp
	ON 
		detalle_factura_temp.numero_factura = encabezado_factura_temp.numero_factura
	INNER JOIN
	cajas
	ON 
		encabezado_factura_temp.codigo_caja = cajas.codigo
WHERE
	detalle_factura_temp.codigo_articulo = p_cod_articulo AND
	cajas.codigo_bodega = p_cod_bodega AND	encabezado_factura_temp.estado<3;

    -- Sumar los resultados
    SET resultado_total = IFNULL(can_saldo, 0) - IFNULL(total_pedidos, 0);

    RETURN resultado_total;
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_fecha_saldo_kardex`(p_cod_kardex int(11)) RETURNS date
BEGIN

return (SELECT
	detalle_movimiento_kardex.fecha
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

WHERE	
	movimiento_kardex.codigo_tipo_movimiento=3
	AND
	articulo_kardex.codigo_kardex=p_cod_kardex

	ORDER BY
		movimiento_kardex.codigo_movimiento DESC
	limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_fecha_ultimo_pago_factura`(p_id_cuenta int(11)) RETURNS varchar(50) CHARSET utf8mb3
BEGIN

	return (select fecha from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and tipo_movimiento=2  ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_get_botton_id_pag`(p_cod_articulo int(11), `p_table_name` varchar(150), `p_id_name` varchar(150)) RETURNS int
BEGIN
	
	declare contador int;
	declare top_id int;
	declare top_id_net int;
	declare resul_id int;
	#return (select p_id_name from marcas where p_id_name=(p_cod_articulo));
	
	set top_id_net = p_cod_articulo-1;
	set contador=0;
	set top_id=0;
	
	while contador<=20 do
			set resul_id= (select codigo_marca from marcas where codigo_marca=(top_id_net) );

			IF  (resul_id>0) then
				set contador=contador+1;
				set top_id=resul_id;
				
			end IF;
			set top_id_net=top_id_net-1;
  
    end while;

	return top_id;
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `get_cod_kardex`(p_cod_articulo int, p_cod_bodega int) RETURNS int
begin

	declare cod_kardex int;

	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = p_cod_articulo AND	codigo_bodega = p_cod_bodega) limit 1);

	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		RETURN cod_kardex;
	ELSE
		RETURN cod_kardex;
	end if;

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_kardex`(p_nomero_fact int, p_cod_articulo int, p_catidad double, p_cod_bodega int) RETURNS tinyint(1)
BEGIN
	declare cod_kardex int;
	declare tipo_articulo int;

	set cod_kardex=(select get_cod_kardex(p_cod_articulo,1));
	set tipo_articulo=(SELECT * from articulo WHERE codigo_articulo=p_cod_articulo);

	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		return false;
	ELSE
		return true;
	end if; 


END$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_no_dias_del_ultimo_pago`(p_id_cuenta int(11)) RETURNS int
BEGIN

	return (SELECT DATEDIFF( CURDATE(), cuentas_por_cobrar_facturas.fecha) as dias from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and (tipo_movimiento=2 or tipo_movimiento=1) ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_no_documento_saldo_kardex`(p_cod_kardex int(11)) RETURNS varchar(150) CHARSET utf8mb3
BEGIN

return (SELECT
	detalle_movimiento_kardex.no_documento
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

WHERE	
	movimiento_kardex.codigo_tipo_movimiento=3
	AND
	articulo_kardex.codigo_kardex=p_cod_kardex

	ORDER BY
		movimiento_kardex.codigo_movimiento DESC
	limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_no_factor`() RETURNS int
begin
	DECLARE factura1 int;
	DECLARE factura2 int;
	declare mas_factura int;
	declare porcentaje double;

	set factura1=(SELECT min(numero_factura) from categorias where DATE_FORMAT(fecha, '%Y-%m-%d') = CURDATE());
	
	if(factura1 is null) THEN
		set factura1=(select max(numero_factura) from categorias);
	end if;

	set factura2=(SELECT min(numero_factura) from encabezado_factura where DATE_FORMAT(fecha, '%Y-%m-%d') = CURDATE());
	
	if(factura2 is null) THEN
		set factura2=(select max(numero_factura) from encabezado_factura);
	end if;

	set porcentaje=(1-( factura1/factura2));

	set mas_factura=(factura2*porcentaje);


	return mas_factura;

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_pasar_numero_letra`(p_numero int(3)) RETURNS varchar(3) CHARSET utf8mb3
BEGIN

	CASE p_numero
		WHEN 1 THEN return "A";
		WHEN 2 THEN return "B";
		WHEN 3 THEN return "C";
		WHEN 4 THEN return "D";
		WHEN 5 THEN return "E";
		WHEN 6 THEN return "F";
		WHEN 7 THEN return "G";
		WHEN 8 THEN return "H";
		WHEN 9 THEN return "I";
		WHEN 10 THEN return "J";
		WHEN 11 THEN return "K";
		WHEN 12 THEN return "L";
		WHEN 13 THEN return "M";
		WHEN 14 THEN return "N";
		WHEN 15 THEN return "O";
		ELSE return "NA";
		
	end case;

	#return (ifnull ((SELECT saldo FROM cuentas_por_pagar where codigo_proveedor=p_codigo_proveedor ORDER BY codigo_reguistro DESC limit 1),0));

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_precio_articulo`(p_codigo_articulo int(11)) RETURNS double(11,2)
BEGIN
	return (SELECT precios_articulos.precio_articulo
FROM precios_articulos
WHERE `precios_articulos`.`codigo_precio` = 1
AND `precios_articulos`.`codigo_articulo` = p_codigo_articulo) ;
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_precio_general_articulo`(p_codigo_articulo int(11)) RETURNS double(11,2)
BEGIN

return (SELECT `precios_articulos`.`precio_articulo` FROM `precios_articulos` WHERE `codigo_precio`= 1 and codigo_articulo=p_codigo_articulo);
	
end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_precio_saldo_kardex`(p_cod_kardex int(11)) RETURNS double(11,2)
BEGIN

return (SELECT
	movimiento_kardex.precio_unidad
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

WHERE	
	movimiento_kardex.codigo_tipo_movimiento=3
	AND
	articulo_kardex.codigo_kardex=p_cod_kardex

	ORDER BY
		movimiento_kardex.codigo_movimiento DESC
	limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_saldo_cliente`(p_id_cliente int(11)) RETURNS double(11,2)
BEGIN

	return (SELECT saldo FROM cuentas_por_cobrar where codigo_cliente=p_id_cliente ORDER BY codigo_reguistro DESC limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_saldo_factura_cliente`(p_no_cuenta int(11)) RETURNS double(11,2)
BEGIN

	return (SELECT saldo FROM cuentas_por_cobrar_facturas where codigo_cuenta=p_no_cuenta ORDER BY codigo_reguistro DESC limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_saldo_proveedor`(p_codigo_proveedor int(11)) RETURNS double(11,2)
BEGIN

	return (ifnull ((SELECT saldo FROM cuentas_por_pagar where codigo_proveedor=p_codigo_proveedor ORDER BY codigo_reguistro DESC limit 1),0));

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `f_total_saldo_kardex`(p_cod_kardex int(11)) RETURNS double(11,2)
BEGIN

return (SELECT
	movimiento_kardex.total
FROM
	articulo_kardex
INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento

WHERE	
	movimiento_kardex.codigo_tipo_movimiento=3
	AND
	articulo_kardex.codigo_kardex=p_cod_kardex

	ORDER BY
		movimiento_kardex.codigo_movimiento DESC
	limit 1);

end$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `sin_usar_verificar_kardex`(p_cod_articulo int, p_cod_bodega int) RETURNS tinyint(1)
begin

	declare cod_kardex int;

	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = p_cod_articulo AND	codigo_bodega = p_cod_bodega) limit 1);

	#set cod_kardex=9;
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		RETURN false;
	ELSE
		RETURN true;
	end if;

end$$
DELIMITER ;

-- ============================================
-- PROCEDURES (17)
-- ============================================

DELIMITER $$
CREATE PROCEDURE `agregar_entrada_kardex`()
BEGIN
	declare fin int default 0;
	declare no_factura int;
	declare no_factura_compra varchar(50);
	declare cod_articulo int;
	declare cantidad2 float;
	declare tipo_articulo int;
	declare cod_kardex int;
	declare cod_movimiento int;
	declare existencia int;
	declare precio float;
	declare precio_compra float;
	declare total float;
	declare newSaldo float;
	declare newExistencia float;
	
	DECLARE listaFacturas  CURSOR FOR SELECT
	encabezado_factura_compra.numero_compra,
	detalle_factura_compra.codigo_articulo,
	detalle_factura_compra.cantidad as cantidad_compra,
	articulo.tipo_articulo,
	encabezado_factura_compra.no_factura_compra,
	detalle_factura_compra.precio
	FROM
	encabezado_factura_compra
	INNER JOIN detalle_factura_compra ON encabezado_factura_compra.numero_compra = detalle_factura_compra.numero_compra
	INNER JOIN articulo ON detalle_factura_compra.codigo_articulo = articulo.codigo_articulo
	WHERE
	detalle_factura_compra.agrega_kardex = 0 AND
	encabezado_factura_compra.estado_factura = 'ACT';
	
	
	DECLARE CONTINUE HANDLER FOR  NOT FOUND SET fin=1;
	
	

	open listaFacturas;

	ciclo_loop: LOOP

		fetch listaFacturas into no_factura,cod_articulo,cantidad2,tipo_articulo,no_factura_compra,precio_compra;

		if ( fin=1) then
			#Termina el loop es decir cierra el cursor
			leave ciclo_loop;
		end if;

		

		#si lo facturado es un bien
		if(tipo_articulo=1) then

			set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex
			WHERE (codigo_articulo = cod_articulo AND	codigo_bodega = 1) LIMIT 1);
			
			#sino existe el kardex se crea por defecto
			if(cod_kardex is null) then
				#se crea el kardex
				INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (cod_articulo,1);
				
				#se coge el codigo de kardex
				set cod_kardex=(select last_insert_id());
				
				#se ingresa el detalle por kardex
				insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
				values (cod_kardex, now(),'Inventario inicial', no_factura_compra);

				set cod_movimiento=(select last_insert_id());

				#se agrega la entrada pero solo el saldo
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (cod_movimiento,3,cantidad2,precio_compra,(cantidad2*precio_compra));
				
				#se actualiza el detalle factura estableciento que ya esta en el kardex			
				update detalle_factura_compra set  agrega_kardex=1 where numero_compra=no_factura;

			ELSE
				insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
				values (cod_kardex, now(),'Compra de productos', no_factura_compra);
				
				set cod_movimiento=(select last_insert_id());
				
				set existencia=(
										SELECT movimiento_kardex.cantidad FROM articulo_kardex
										INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
										INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
										WHERE
										articulo_kardex.codigo_kardex = cod_kardex AND
										movimiento_kardex.codigo_tipo_movimiento = 3
										ORDER BY
										detalle_movimiento_kardex.codigo_movimiento DESC
										LIMIT 1
						);
				
				set total=(
								SELECT movimiento_kardex.total FROM articulo_kardex
								INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
								INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
								WHERE
								articulo_kardex.codigo_kardex = cod_kardex AND
								movimiento_kardex.codigo_tipo_movimiento = 3
								ORDER BY
								detalle_movimiento_kardex.codigo_movimiento DESC
								LIMIT 1
				
				
				
				);
				
				set precio=(
								SELECT movimiento_kardex.precio_unidad FROM articulo_kardex
								INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
								INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
								WHERE
								articulo_kardex.codigo_kardex = cod_kardex AND
								movimiento_kardex.codigo_tipo_movimiento = 3
								ORDER BY
								detalle_movimiento_kardex.codigo_movimiento DESC
								LIMIT 1
				
				

				);
				#se agrega la entrada
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (cod_movimiento,1,cantidad2,precio_compra,(cantidad2*precio_compra));
				
				 
				set newSaldo  =(total+ (cantidad2*precio_compra));
				
				set newExistencia=(existencia+cantidad2);



				if(newExistencia>0) then
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										(newSaldo/newExistencia),
										newSaldo
						);
					ELSEIF(newExistencia=0) then
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										precio,
										0
						);
					ELSE
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										(newSaldo/newExistencia),
										newSaldo
						);


					end if;




				
				
				#se agrega el saldo
				#insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total)
				#values (
				#				cod_movimiento,
				#				3,
				#				newExistencia,
				#				(newSaldo/newExistencia),
				#				newSaldo
				#);
				
				#se actualiza la factura estableciento que ya esta en el kardex			
				update detalle_factura_compra set  agrega_kardex=1 where numero_compra=no_factura;
				
			end if;

			
			
			


		end if;

	end LOOP ciclo_loop ;
	CLOSE listaFacturas;

END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `agregar_salida_fact_kardex`()
BEGIN
	declare fin int default 0;
	declare no_factura int;
	declare cod_articulo int;
	declare cantidad float;
	declare tipo_articulo int;
	declare cod_kardex int;
	declare cod_movimiento int;
	declare existencia int;
	declare precio float;
	declare total float;
	declare newSaldo float;
	declare newExistencia float;
	


	DECLARE listaFacturas  CURSOR FOR SELECT
	encabezado_factura.numero_factura,
	detalle_factura.codigo_articulo,
	detalle_factura.cantidad,
	articulo.tipo_articulo
	FROM
	encabezado_factura
	INNER JOIN detalle_factura ON encabezado_factura.numero_factura = detalle_factura.numero_factura
	INNER JOIN articulo ON detalle_factura.codigo_articulo = articulo.codigo_articulo
	WHERE
	detalle_factura.agrega_kardex = 0 AND
	encabezado_factura.estado_factura = 'ACT';
		
		

	DECLARE CONTINUE HANDLER FOR  NOT FOUND SET fin=1;

	open listaFacturas;

	ciclo_loop: LOOP
	
		fetch listaFacturas into no_factura,cod_articulo,cantidad,tipo_articulo;
	
		if ( fin=1) then
			#Termina el loop es decir cierra el cursor
			leave ciclo_loop;
		end if;

		#si lo facturado es un bien 
		if(tipo_articulo=1) then
		
			set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = cod_articulo AND	codigo_bodega = 1) limit 1);

			#sino existe el kardex se crea por defecto
			if(cod_kardex is null) then
				#se crea el kardex
				INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (cod_articulo,1);
				
				#se coge el codigo de kardex
				set cod_kardex=(select last_insert_id());
				
				#se crea el movimiento
				insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
				values (cod_kardex, now(),'Ajuste de inventario','NA');

				#se agrega la entrada
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (cod_movimiento,3,cantidad,precio,(cantidad*precio));

				#se agrega la salida
				insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
				values (cod_kardex, now(),'Venta de productos', no_factura);
			
				set cod_movimiento=(select last_insert_id());

				#se agrega la entrada
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (cod_movimiento,2,cantidad,precio,(cantidad*precio));


				#se agrega el saldo
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (
								cod_movimiento,
								3,
								0,
								precio,
								0
				);

				#se actualiza la factura estableciento que ya esta en el kardex			
				update detalle_factura set  agrega_kardex=1 where numero_factura=no_factura;




				ELSE

			
					insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (cod_kardex, now(),'Venta de productos', no_factura);
					
					set cod_movimiento=(select last_insert_id());
					
					set existencia=(
											SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
					set total=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
					
					set precio=(
									SELECT movimiento_kardex.precio_unidad FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
					#se agrega la entrada
					insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,2,cantidad,precio,(cantidad*precio));
					
					 
					set newSaldo  =(total- (cantidad*precio));
					
					set newExistencia=(existencia-cantidad);
					
					if(newExistencia>0) then
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										(newSaldo/newExistencia),
										newSaldo
						);
					ELSEIF(newExistencia=0) then
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										precio,
										0
						);
					ELSE
						#se agrega el saldo
						insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
						values (
										cod_movimiento,
										3,
										newExistencia,
										(newSaldo/newExistencia),
										newSaldo
						);


					end if;

					
					#se actualiza la factura estableciento que ya esta en el kardex			
					update detalle_factura set  agrega_kardex=1 where numero_factura=no_factura;
			end if;#fin del fi donde se asegura que exista el kardex

	
		end if; 

	end LOOP ciclo_loop ;
	CLOSE listaFacturas;

END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_ajuste_inventario_kardex`(p_cod_kardex int(10), p_cantidad float, p_precio_comp float, p_referencia varchar(100))
BEGIN

	declare cod_movimiento int;
	declare	valor_total_saldo float;

	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Ajuste inventario', p_referencia);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la entrada del producto al registro codigo movimiento #1
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,1,p_cantidad,p_precio_comp,(p_cantidad*p_precio_comp));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(p_cantidad*p_precio_comp);
	

		
		#si la nueva existencia en positiva se agrega el registro de saldo normalmente
		if(p_cantidad>0) then
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
								cod_movimiento,
								3,
								p_cantidad,
								(valor_total_saldo/p_cantidad),
								valor_total_saldo
				);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(p_cantidad=0) then
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
								cod_movimiento,
								3,
								0,
								0,
								0
				);
		end if;
		
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_inventario_inicial_kardex`(p_cod_kardex int(10), p_cantidad float, p_precio_comp float, p_referencia varchar(20))
BEGIN

	declare cod_movimiento int;
	declare	valor_total_saldo float;

	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Inventario inicial', p_referencia);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la entrada del producto al registro codigo movimiento #1
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,1,p_cantidad,p_precio_comp,(p_cantidad*p_precio_comp));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(p_cantidad*p_precio_comp);
	

		#si la nueva existencia en positiva se agrega el registro de saldo normalmente
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
		values (
						cod_movimiento,
						3,
						p_cantidad,
						(valor_total_saldo/p_cantidad),
						valor_total_saldo
			);
		
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_ajuste_inventario_movil`(p_codigo_articulo int(10), p_codigo_bodega int(10), p_cantidad float(10), p_precio float(10), p_referencia varchar(100))
BEGIN
	declare cod_kardex int;
	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = p_codigo_articulo AND	codigo_bodega = p_codigo_bodega) limit 1);
	
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		#se crea el kardex
		INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (p_codigo_articulo,p_codigo_bodega);
				
		#se coge el codigo de kardex
		set cod_kardex=(select last_insert_id());
	
		call crear_inventario_inicial_kardex(cod_kardex,p_cantidad,p_precio,p_referencia);

	ELSE
		call crear_ajuste_inventario_kardex( cod_kardex,p_cantidad,p_precio,p_referencia);

	end if;

	
end$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_compa_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float, p_precio_comp float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Compra de productos', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro codigo movimiento #1
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,1,p_cantidad,p_precio_comp,(p_cantidad*p_precio_comp));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old+(p_cantidad*p_precio_comp));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old+p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							valor_total_saldo
			);
		
		end if;	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_dev_compa_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float, p_precio_comp float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(					SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Devolucion sobre compra', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro codigo movimiento #1
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,1,p_cantidad,p_precio_comp,(p_cantidad*p_precio_comp*-1));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old+(p_cantidad*p_precio_comp*-1));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old-p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							0
			);
		
		end if;
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_dev_venta_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float, p_precio_fact float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(					SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);

	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Devolucion sobre venta', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,2,p_cantidad,p_precio_fact,(p_cantidad*p_precio_fact*-1));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old- (p_cantidad*p_precio_fact*-1));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old+p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							0
			);
		
		end if;	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_interes_facturas`()
BEGIN
	declare fin int default 0;
	declare contador int default 0;
	declare no_dia_vencimiento int;
	declare no_dia_venc_factura int;
	declare cantidad_interes_now int;
	declare cantidad_interes_debe_tener int;
	declare cantidad_interes_aplicar int;
	declare	interes_factura float;
	declare	interes_facturas float;

	declare cod_cuenta int;
	declare fecha_factura date;
	declare id_factura int;
	declare cod_caja int;
	declare cod_cliente int;
	declare nombre_cliente VARCHAR(150);
	declare rtn_cliente VARCHAR(100);
	declare saldo_factura float;

	

	
	declare facturasVencidas  CURSOR FOR SELECT cuentas_facturas.codigo_cuenta, 
											 cuentas_facturas.fecha,
											 cuentas_facturas.no_factura,
											 cuentas_facturas.codigo_caja,
											 cuentas_facturas.codigo_cliente,
											 cliente.nombre_cliente,
											 cliente.rtn,
											 f_saldo_factura_cliente( cuentas_facturas.codigo_cuenta ) AS saldo
										FROM
											cuentas_facturas
										JOIN cliente 
													ON(cliente.codigo_cliente = cuentas_facturas.codigo_cliente) 
										JOIN(
												SELECT
														codigo_cuenta,
														ifnull(
															f_saldo_factura_cliente(codigo_cuenta),
															0.00
														)saldo
													FROM
														cuentas_facturas
												)cuenta2 
													ON(cuenta2.codigo_cuenta = cuentas_facturas.codigo_cuenta and cuenta2.saldo<>0);
									#	where 
									#		CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY);




	DECLARE CONTINUE HANDLER FOR  NOT FOUND SET fin=1;
	
	set no_dia_vencimiento=(select dia_vencimiento_factura from config_app limit 1);
	set interes_facturas=(select interes_para_facturas_venc from config_app limit 1);

		

	open facturasVencidas;

	ciclo_loop: LOOP
		#se recogen los field de cada factura
		fetch facturasVencidas into cod_cuenta, fecha_factura, id_factura, cod_caja, cod_cliente, nombre_cliente, rtn_cliente, saldo_factura;
		
		if ( fin=1) then
			#Termina el loop es decir cierra el cursor
			leave ciclo_loop;
		end if;

		if( CURDATE() > DATE_ADD(fecha_factura, INTERVAL no_dia_vencimiento DAY) ) then

				
				#se encuentra el numero de registros de intereses que tiene la factura
				set cantidad_interes_now=(SELECT COUNT( cuentas_por_cobrar_facturas.codigo_reguistro) FROM cuentas_por_cobrar_facturas where tipo_movimiento=3 and codigo_cuenta=cod_cuenta);
				#select cantidad_interes_now;
				
				#se encuentran la cantidad de dias vencidos de la fatura
				set no_dia_venc_factura=(SELECT DATEDIFF( CURDATE(), cuentas_facturas.fecha) as dias FROM cuentas_facturas WHERE codigo_cuenta=cod_cuenta);
				#select no_dia_venc_factura;
				
				#se encuenta el numero de registro de intereses que debe tener la factura
				set cantidad_interes_debe_tener=( no_dia_venc_factura div no_dia_vencimiento);
				#select cantidad_interes_debe_tener;
				
				#se encuentra el numero de resitro de interses que le falta a la factura
				set cantidad_interes_aplicar=(cantidad_interes_debe_tener - cantidad_interes_now);
				
				
				#select cod_cuenta;
				#insert into test(cod_cuenta, cantidad_interes_now,no_dia_venc_factura,cantidad_interes_debe_tener,cantidad_interes_aplicar) values (cod_cuenta, cantidad_interes_now,no_dia_venc_factura,cantidad_interes_debe_tener,cantidad_interes_aplicar) ;

				#se aplican los interes que faltan 
				WHILE contador < cantidad_interes_aplicar DO

					set interes_factura=(f_saldo_factura_cliente( cod_cuenta ) * (interes_facturas/100));
					insert into cuentas_por_cobrar_facturas(codigo_cuenta,fecha,descripcion,credito,saldo,usuario,tipo_movimiento) VALUES (cod_cuenta,now(),'Interes por factura vencida',interes_factura,(interes_factura + f_saldo_factura_cliente( cod_cuenta )),"system",3); 
					insert into cuentas_por_cobrar(fecha,codigo_cliente,descripcion,credito,saldo) VALUES(now(),cod_cliente,CONCAT('Interes por factura vencida #',id_factura),interes_factura,(interes_factura + f_saldo_cliente(cod_cliente)));
					set contador=contador+1;
				END WHILE;
				
				set contador=0;
				set cantidad_interes_aplicar=0;
		
		end if;

		
	end LOOP ciclo_loop ;
	CLOSE facturasVencidas;
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_interes_facturas_optimizada`()
BEGIN
    DECLARE fin INT DEFAULT 0;
    DECLARE contador INT DEFAULT 0;
    DECLARE no_dia_vencimiento INT;
    DECLARE no_dia_venc_factura INT;
    DECLARE cantidad_interes_now INT;
    DECLARE cantidad_interes_debe_tener INT;
    DECLARE cantidad_interes_aplicar INT;
    DECLARE interes_factura FLOAT;
    DECLARE interes_facturas FLOAT;

    DECLARE cod_cuenta INT;
    DECLARE fecha_factura DATE;
    DECLARE id_factura INT;
    DECLARE cod_cliente INT;
    DECLARE saldo_factura FLOAT;

    -- CURSOR OPTIMIZADO: Filtra igual que el original, pero sin llamadas repetitivas a la función
    -- y omitiendo las columnas de cliente (nombre, rtn, cod_caja) que no se usaban en los inserts.
    DECLARE facturasVencidas CURSOR FOR 
        SELECT 
            cf.codigo_cuenta, 
            cf.fecha, 
            cf.no_factura, 
            cf.codigo_cliente, 
            t_saldo.saldo
        FROM cuentas_facturas cf
        INNER JOIN (
            SELECT s1.codigo_cuenta, s1.saldo
            FROM cuentas_por_cobrar_facturas s1
            WHERE s1.codigo_reguistro = (
                SELECT MAX(s2.codigo_reguistro) 
                FROM cuentas_por_cobrar_facturas s2 
                WHERE s2.codigo_cuenta = s1.codigo_cuenta
            )
        ) t_saldo ON cf.codigo_cuenta = t_saldo.codigo_cuenta
        WHERE t_saldo.saldo > 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET fin = 1;
    
    SET no_dia_vencimiento = (SELECT dia_vencimiento_factura FROM config_app LIMIT 1);
    SET interes_facturas = (SELECT interes_para_facturas_venc FROM config_app LIMIT 1);

    OPEN facturasVencidas;

    ciclo_loop: LOOP
        -- Fetch alineado con las variables necesarias
        FETCH facturasVencidas INTO cod_cuenta, fecha_factura, id_factura, cod_cliente, saldo_factura;
        
        IF (fin = 1) THEN
            LEAVE ciclo_loop;
        END IF;

        -- REGLA DE NEGOCIO ORIGINAL INTACTA
        IF (CURDATE() > DATE_ADD(fecha_factura, INTERVAL no_dia_vencimiento DAY)) THEN
                
            -- Se encuentra el numero de registros de intereses que tiene la factura
            SET cantidad_interes_now = (SELECT COUNT(codigo_reguistro) FROM cuentas_por_cobrar_facturas WHERE tipo_movimiento = 3 AND codigo_cuenta = cod_cuenta);
            
            -- Se encuentra la cantidad de dias vencidos usando la variable local (Optimizado)
            SET no_dia_venc_factura = DATEDIFF(CURDATE(), fecha_factura);
            
            -- Intereses que debe tener
            SET cantidad_interes_debe_tener = (no_dia_venc_factura DIV no_dia_vencimiento);
            
            -- Intereses que le faltan a la factura
            SET cantidad_interes_aplicar = (cantidad_interes_debe_tener - cantidad_interes_now);

            -- Se aplican los interes que faltan (Logica de interes compuesto conservada en memoria)
            WHILE contador < cantidad_interes_aplicar DO

                -- Cálculo basado en el saldo de la memoria
                SET interes_factura = (saldo_factura * (interes_facturas / 100));
                
                -- Insert en detalle
                INSERT INTO cuentas_por_cobrar_facturas(codigo_cuenta, fecha, descripcion, credito, saldo, usuario, tipo_movimiento) 
                VALUES (cod_cuenta, NOW(), 'Interes por factura vencida', interes_factura, (interes_factura + saldo_factura), "system", 3); 
                
                -- Insert en maestro (Mantenemos f_saldo_cliente intacto)
                INSERT INTO cuentas_por_cobrar(fecha, codigo_cliente, descripcion, credito, saldo) 
                VALUES (NOW(), cod_cliente, CONCAT('Interes por factura vencida #', id_factura), interes_factura, (interes_factura + f_saldo_cliente(cod_cliente)));
                
                -- CRÍTICO: Actualiza el saldo en la variable para la próxima vuelta del loop
                SET saldo_factura = saldo_factura + interes_factura;
                
                SET contador = contador + 1;
            END WHILE;
            
            SET contador = 0;
            SET cantidad_interes_aplicar = 0;
    
        END IF;
        
    END LOOP ciclo_loop;
    CLOSE facturasVencidas;
    
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_requisicion_entrada_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float, p_precio_comp float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'requisicion de producto', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro codigo movimiento #1
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,1,p_cantidad,p_precio_comp,(p_cantidad*p_precio_comp));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old+(p_cantidad*p_precio_comp));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old+p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							valor_total_saldo
			);
		
		end if;	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_requisicion_salida_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare precio_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(					SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
					
	set precio_old=(
									SELECT movimiento_kardex.precio_unidad FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'requisicion de articulo', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,2,p_cantidad,precio_old,(p_cantidad*precio_old));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old- (p_cantidad*precio_old));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old-p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							valor_total_saldo
			);
		
		end if;
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_salida_venta_kardex_viejo`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float, p_precio_fact float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare precio_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(					SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
					
	set precio_old=(
									SELECT movimiento_kardex.precio_unidad FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Venta de productos', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,2,p_cantidad,precio_old,(p_cantidad*precio_old));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old- (p_cantidad*precio_old));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old-p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(newSaldo/newExistencia),
							newSaldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							newSaldo
			);
		
		end if;
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_venta_kardex`(p_cod_kardex int(10), p_no_factura int(10), p_cantidad float)
BEGIN

	declare cod_movimiento int;
	declare existencia_old float;
	declare total_old float;
	declare precio_old float;
	declare	valor_total_saldo float;
	declare newExistencia float;



	set existencia_old=(					SELECT movimiento_kardex.cantidad FROM articulo_kardex
											INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
											INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
											WHERE
											articulo_kardex.codigo_kardex = p_cod_kardex AND
											movimiento_kardex.codigo_tipo_movimiento = 3
											ORDER BY
											detalle_movimiento_kardex.codigo_movimiento DESC
											LIMIT 1
							);
					
	set total_old=(
									SELECT movimiento_kardex.total FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
					
	set precio_old=(
									SELECT movimiento_kardex.precio_unidad FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = p_cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					
					
					
					);
	
	



		#se agrega el detalle al registro del kardex
		insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
					values (p_cod_kardex, now(),'Venta de productos', p_no_factura);	
		set cod_movimiento=(select last_insert_id());

		#se agrega la salida del producto al registro
		insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (cod_movimiento,2,p_cantidad,precio_old,(p_cantidad*precio_old));
		
		#se calcula el nuevo total para el saldo
		set valor_total_saldo  =(total_old- (p_cantidad*precio_old));
		
		#se calcula la nueva existencia para el saldo
		set newExistencia=(existencia_old-p_cantidad);

		#se verifica la nueva existencia
		if(newExistencia>0) then
			#si la nueva existencia en positiva se agrega el registro de saldo normalmente
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							(valor_total_saldo/newExistencia),
							valor_total_saldo
			);
		#si la nueva existencia es cero se crea el registro de saldo con valores 0
		ELSEIF(newExistencia=0) then
			#s
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							0,
							0,
							0
			);
		#si el valor de la existencia es negativo se coloca el precio del saldo en 0
		ELSE
			#se agrega el saldo
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (
							cod_movimiento,
							3,
							newExistencia,
							0,
							valor_total_saldo
			);
		
		end if;
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `crear_venta_insumo_kardex`(p_cod_bodega int(10), p_cod_articulo int(10), p_cantidad float, p_observaciones varchar(200), p_no_factura int(11))
BEGIN
	declare fin int default 0;
	declare id_insu int;
	declare cod_servicio int;
	declare cod_articulo int;
	declare cant float;
	declare precio_costo float;
	DECLARE cod_kardex INT;

	declare insumos_articulo  CURSOR FOR SELECT insumos.id_insumo, insumos.codigo_servicio, insumos.codigo_articulo, insumos.cantidad FROM insumos where insumos.codigo_servicio=p_cod_articulo;

	DECLARE CONTINUE HANDLER FOR  NOT FOUND SET fin=1;

	open insumos_articulo;

	ciclo_loop: LOOP
		#se recogen los field de cada factura
		fetch insumos_articulo into id_insu, cod_servicio, cod_articulo, cant;
		
		
		if ( fin=1) then
			#Termina el loop es decir cierra el cursor
			leave ciclo_loop;
		end if;
		
		SET cod_kardex =(SELECT codigo_kardex FROM admin_tools.articulo_kardex WHERE ( codigo_articulo = cod_articulo AND codigo_bodega = p_cod_bodega) LIMIT 1);
		
		IF(cod_kardex IS NULL) THEN
			INSERT INTO admin_tools.articulo_kardex(codigo_articulo, codigo_bodega) VALUES (cod_articulo, p_cod_bodega);
			set precio_costo=(SELECT `precios_articulos`.`precio_articulo` FROM `precios_articulos` WHERE `codigo_precio`= 1 and codigo_articulo=cod_articulo);
			SET cod_kardex =(SELECT last_insert_id());
			CALL admin_tools.crear_ajuste_inventario_kardex(cod_kardex,p_cantidad*cant,precio_costo,p_observaciones);
			CALL admin_tools.crear_venta_kardex(cod_kardex, p_no_factura, p_cantidad*cant);
		ELSE
			set precio_costo=(select IFNULL(f_precio_saldo_kardex(cod_kardex),0));
			CALL admin_tools.crear_venta_kardex(cod_kardex,p_no_factura,p_cantidad*cant);
		
		end if;


		
	end LOOP ciclo_loop ;
	CLOSE insumos_articulo;
	
	
	
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `devolucion_sobre_venta`()
BEGIN
	declare fin int default 0;
	declare no_factura int;
	declare cod_devolucion int;
	declare cod_articulo int;
	declare cantidad float;
	declare tipo_articulo int;
	declare cod_kardex int;
	declare cod_movimiento int;
	declare existencia int;
	declare precio float;
	declare total float;
	declare newSaldo float;
	declare newExistencia float;
	


	DECLARE listaFacturas  CURSOR FOR SELECT 
	detalle_devoluciones.codigo_devolucion,
	detalle_devoluciones.numero_factura, 
	detalle_devoluciones.codigo_articulo, 
	detalle_devoluciones.cantidad, 
	detalle_devoluciones.precio, 
	articulo.tipo_articulo
	FROM detalle_devoluciones INNER JOIN articulo ON detalle_devoluciones.codigo_articulo = articulo.codigo_articulo
	WHERE agrega_kardex=0;
		
		

	DECLARE CONTINUE HANDLER FOR  NOT FOUND SET fin=1;

	open listaFacturas;

	ciclo_loop: LOOP
	
		fetch listaFacturas into cod_devolucion,no_factura,cod_articulo,cantidad,precio,tipo_articulo;
	
		if ( fin=1) then
			#Termina el loop es decir cierra el cursor
			leave ciclo_loop;
		end if;

		#si lo facturado es un bien 
		if(tipo_articulo=1) then
		
			set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = cod_articulo AND	codigo_bodega = 1) limit 1);

			
			insert into detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento)
			values (cod_kardex, now(),'Devolucion sobre venta', no_factura);
					
			set cod_movimiento=(select last_insert_id());
					
			set existencia=(
									SELECT movimiento_kardex.cantidad FROM articulo_kardex
									INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
									INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
									WHERE
									articulo_kardex.codigo_kardex = cod_kardex AND
									movimiento_kardex.codigo_tipo_movimiento = 3
									ORDER BY
									detalle_movimiento_kardex.codigo_movimiento DESC
									LIMIT 1
					);
					
			set total=(
							SELECT movimiento_kardex.total FROM articulo_kardex
							INNER JOIN detalle_movimiento_kardex ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex
							INNER JOIN movimiento_kardex ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento
							WHERE
							articulo_kardex.codigo_kardex = cod_kardex AND
							movimiento_kardex.codigo_tipo_movimiento = 3
							ORDER BY
							detalle_movimiento_kardex.codigo_movimiento DESC
							LIMIT 1
			
			
			
			);
					
			#se agrega la salida
			insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
			values (cod_movimiento,2,cantidad,precio,(cantidad*precio*-1));
					
					 
			set newSaldo  =(total+ (cantidad*precio));
					
			set newExistencia=(existencia+cantidad);
					
			if(newExistencia>0) then
				#se agrega el saldo
				insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
				values (
								cod_movimiento,
								3,
								newExistencia,
								(newSaldo/newExistencia),
								newSaldo
				);
			ELSEIF(newExistencia=0) then
					#se agrega el saldo
					insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (
									cod_movimiento,
									3,
									newExistencia,
									precio,
									0
					);
				ELSE
					#se agrega el saldo
					insert into movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) 
					values (
									cod_movimiento,
									3,
									newExistencia,
									(newSaldo/newExistencia),
									newSaldo
					);


				end if;

					
				#se actualiza la factura estableciento que ya esta en el kardex			
				update detalle_devoluciones set  agrega_kardex=1 where codigo_devolucion=cod_devolucion;
		

		#fin del if donde se comprueba que el articulo es un bien y no un servicio
		end if; 

	end LOOP ciclo_loop ;
	CLOSE listaFacturas;

END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `p_prueba_return_table`()
BEGIN

      SELECT * FROM cajas where codigo=1;
END$$
DELIMITER ;

-- ============================================
-- TRIGGERS (8)
-- ============================================

DELIMITER $$
CREATE TRIGGER `detalle_compra_b_inset` BEFORE INSERT ON `detalle_factura_compra` FOR EACH ROW BEGIN
	declare cod_kardex int;
	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = NEW.codigo_articulo AND	codigo_bodega = NEW.codigo_bodega) limit 1);
	
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		#se crea el kardex
		INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,NEW.codigo_bodega);
				
		#se coge el codigo de kardex
		set cod_kardex=(select last_insert_id());
	
		call crear_inventario_inicial_kardex(cod_kardex,NEW.cantidad,NEW.precio,NEW.numero_compra);
		set NEW.agrega_kardex=1;

	ELSE
		call crear_compa_kardex(cod_kardex,NEW.numero_compra,NEW.cantidad,NEW.precio);
		set NEW.agrega_kardex=1;
		#set cod_kardex=22;

	end if;

	
end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `detalle_devolucion_b_inset` BEFORE INSERT ON `detalle_devoluciones` FOR EACH ROW BEGIN
	declare cod_kardex int;
	declare cod_bodega int;

	#se encuentra el codigo de la bodega que esta asignado para la caja en donde se facturo
	set cod_bodega=(SELECT cajas.codigo_bodega FROM cajas where codigo=NEW.codigo_caja  limit 1);
	
	#se encuentra el codigo de kardex
	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = NEW.codigo_articulo AND	codigo_bodega = cod_bodega) limit 1);
	
	
	
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		#se crea el kardex
		INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,cod_bodega);
				
		#se coge el codigo de kardex
		set cod_kardex=(select last_insert_id());
	
		CALL crear_ajuste_inventario_kardex(cod_kardex,NEW.cantidad,NEW.precio,'Ajuste facturado devolucion');
		call crear_dev_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad,NEW.precio);
		set NEW.agrega_kardex=1;

	ELSE
		call crear_dev_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad,NEW.precio);

		set NEW.agrega_kardex=1;
		

	end if;

end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `detalle_devolucion_compra_b_i` BEFORE INSERT ON `detalle_devoluciones_compra` FOR EACH ROW BEGIN
	declare cod_kardex int;
	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = NEW.codigo_articulo AND	codigo_bodega = NEW.codigo_bodega) limit 1);
	
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		#se crea el kardex
		INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,1);
				
		#se coge el codigo de kardex
		set cod_kardex=(select last_insert_id());
	
		CALL crear_ajuste_inventario_kardex(cod_kardex,NEW.cantidad,NEW.precio,'Ajuste facturado Tienda Principal');
		call crear_dev_compa_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad,NEW.precio);
		set NEW.agrega_kardex=1;

	ELSE
		call crear_dev_compa_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad,NEW.precio);

		set NEW.agrega_kardex=1;
		

	end if;

end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `detalle_factura_b_insert` BEFORE INSERT ON `detalle_factura` FOR EACH ROW BEGIN
	declare cod_kardex int;
	set cod_kardex =(SELECT codigo_kardex FROM articulo_kardex 
			WHERE (codigo_articulo = NEW.codigo_articulo AND	codigo_bodega = 1) limit 1);
	
	#sino existe el kardex se crea por defecto
	if(cod_kardex is null) then
		#se crea el kardex
		INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,1);
				
		#se coge el codigo de kardex
		set cod_kardex=(select last_insert_id());
	
		CALL crear_ajuste_inventario_kardex(cod_kardex,NEW.cantidad,NEW.precio,'facturado Tienda Principal');
		call crear_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad);
		set NEW.agrega_kardex=1;

	ELSE
		call crear_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad);

		set NEW.agrega_kardex=1;
		

	end if;

end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `d_requisicion_b_insert` BEFORE INSERT ON `detalle_requisicion` FOR EACH ROW BEGIN
			declare cod_kardex int; 
            declare cod_kardex2 int; 
			set cod_kardex =(SELECT codigo_kardex FROM admin_tools.articulo_kardex WHERE (codigo_articulo = NEW.codigo_articulo AND codigo_bodega = NEW.codigo_depart_destino) limit 1);
            set cod_kardex2 =(SELECT codigo_kardex FROM admin_tools.articulo_kardex WHERE (codigo_articulo = NEW.codigo_articulo AND codigo_bodega = NEW.codigo_depart_origen) limit 1);
			
            if(cod_kardex is null) then 

						INSERT INTO admin_tools.articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,NEW.codigo_depart_destino);
					
						set cod_kardex=(select last_insert_id());
						call admin_tools.crear_inventario_inicial_kardex(cod_kardex,NEW.cantidad,NEW.precio_unidad,NEW.codigo_requisicion);
                        call admin_tools.crear_requisicion_salida_kardex(cod_kardex2,NEW.codigo_requisicion,NEW.cantidad);

						set NEW.agrega_kardex=1;
			ELSE
					call admin_tools.crear_requisicion_entrada_kardex(cod_kardex,NEW.codigo_requisicion,NEW.cantidad,NEW.precio_unidad);
                    call admin_tools.crear_requisicion_salida_kardex(cod_kardex2,NEW.codigo_requisicion,NEW.cantidad);
					set NEW.agrega_kardex=1;
			end if; 
end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `order_b_delete` BEFORE DELETE ON `encabezado_factura_temp` FOR EACH ROW BEGIN
		DELETE FROM detalle_factura_temp WHERE numero_factura = OLD.numero_factura;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `turno_bi` BEFORE INSERT ON `cierre_caja` FOR EACH ROW BEGIN
	DECLARE num_turno int default 0;
	
	IF  (SELECT estado FROM cierre_caja WHERE usuario = NEW.usuario AND estado = 1) = 1 THEN
               SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'Ya existe un cierre de caja activo para este usuario';
	ELSE
				set num_turno = (SELECT COUNT( cierre_caja.idCierre) FROM cierre_caja WHERE (MONTH(cierre_caja.fecha) = MONTH (now()) and YEAR (cierre_caja.fecha) =YEAR(now()) and  DAY (cierre_caja.fecha) =DAY(now())));
				
				set  NEW.turno = ( select f_pasar_numero_letra(num_turno + 1));
  END IF;
end$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER `usuario_a_insert` AFTER INSERT ON `usuario` FOR EACH ROW begin
insert into config_user_facturacion(usuario,formato_factura) VALUES (NEW.usuario,"tiket");
end$$
DELIMITER ;

SET GLOBAL log_bin_trust_function_creators = @ADMIN_TOOLS_ORIG_LBT;
SET FOREIGN_KEY_CHECKS=1;
