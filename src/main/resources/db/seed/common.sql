-- Seed de datos base para `admin_tools`.
-- Generado por SeedDumper desde la conexion activa de ConexionStatic.
-- NO aplicar contra clientes en produccion: lo consume DatabaseBootstrap
-- unicamente en instalaciones nuevas, despues de common/V1__baseline.sql.

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- ============================================
-- bancos (limit 2)
-- ============================================
INSERT INTO `bancos` (`id`,`nombre`,`no_cuenta`,`id_tipo_cuenta`) VALUES (1,'Efectivo','NA',4);
INSERT INTO `bancos` (`id`,`nombre`,`no_cuenta`,`id_tipo_cuenta`) VALUES (3,'Occidente','NA',1);

-- ============================================
-- bodega (todas las filas)
-- ============================================
INSERT INTO `bodega` (`codigo_bodega`,`descripcion_bodega`) VALUES (1,'Tienda Principal');
INSERT INTO `bodega` (`codigo_bodega`,`descripcion_bodega`) VALUES (2,'Bodega 1');

-- ============================================
-- cliente (limit 1)
-- ============================================
INSERT INTO `cliente` (`codigo_cliente`,`nombre_cliente`,`direccion`,`telefono`,`movil`,`rtn`,`zona_procedencia`,`codigo_postal`,`codiciones_pago`,`limite_credito`,`saldo`,`clasificacion`,`estado`,`tipo_cliente`,`id_vendedor`,`id_ruta_cobro`) VALUES (1,'Consumidor final','NA','NA','NA','CF','NA','NA','NA',0.0,0.0,'NA',0,1,1,1);

-- ============================================
-- config_app (todas las filas)
-- ============================================
INSERT INTO `config_app` (`dia_vencimiento_factura`,`interes_para_facturas_venc`) VALUES (365,3);

-- ============================================
-- config_user_facturacion (todas las filas)
-- ============================================
INSERT INTO `config_user_facturacion` (`id`,`usuario`,`formato_factura`,`ventana_vendedor`,`pwd_descuento`,`pwd_precio`,`descuento_porcentaje`,`ventana_observaciones`,`precio_redondiar`,`facturar_sin_inventario`,`impr_report_categ_cierre`,`impr_report_salida`,`show_report_salida`,`impr_report_entrada`,`show_report_entrada`,`activar_busqueda_facturacion`,`agregar_cliente_credito`,`formato_factura_credito`,`pwd_entre_precio`,`imp_report_order`,`unir_can_item`,`delete_item_fact`) VALUES (1,'tecnico','tiket',0,0,1,0,0,0,1,1,0,1,0,0,1,1,'tiket',0,0,1,1);
INSERT INTO `config_user_facturacion` (`id`,`usuario`,`formato_factura`,`ventana_vendedor`,`pwd_descuento`,`pwd_precio`,`descuento_porcentaje`,`ventana_observaciones`,`precio_redondiar`,`facturar_sin_inventario`,`impr_report_categ_cierre`,`impr_report_salida`,`show_report_salida`,`impr_report_entrada`,`show_report_entrada`,`activar_busqueda_facturacion`,`agregar_cliente_credito`,`formato_factura_credito`,`pwd_entre_precio`,`imp_report_order`,`unir_can_item`,`delete_item_fact`) VALUES (2,'admin','tiket',0,0,0,0,0,0,1,0,0,0,0,0,1,0,'tiket',0,0,0,0);
INSERT INTO `config_user_facturacion` (`id`,`usuario`,`formato_factura`,`ventana_vendedor`,`pwd_descuento`,`pwd_precio`,`descuento_porcentaje`,`ventana_observaciones`,`precio_redondiar`,`facturar_sin_inventario`,`impr_report_categ_cierre`,`impr_report_salida`,`show_report_salida`,`impr_report_entrada`,`show_report_entrada`,`activar_busqueda_facturacion`,`agregar_cliente_credito`,`formato_factura_credito`,`pwd_entre_precio`,`imp_report_order`,`unir_can_item`,`delete_item_fact`) VALUES (3,'ventas','tiket',0,1,1,1,0,0,1,0,1,1,1,1,1,1,'tiket',1,1,1,0);

-- ============================================
-- datos_empresa (todas las filas)
-- ============================================
INSERT INTO `datos_empresa` (`nombre`,`rtn`,`telefono`,`correo`,`propietario`,`direccion`,`id`) VALUES ('DATA TEC SOLUTION S. DE R. L.','01019015756376','(504) 2436-3070','jdmayorga82@yahoo.com','','Bo Suyapa,San Juan Pueblo, Atlantida',1);

-- ============================================
-- departamento (todas las filas)
-- ============================================
INSERT INTO `departamento` (`codigo_departamento`,`nombre`) VALUES (1,'Tienda Principal');
INSERT INTO `departamento` (`codigo_departamento`,`nombre`) VALUES (2,'Bodega 1');

-- ============================================
-- empleados (limit 1)
-- ============================================
INSERT INTO `empleados` (`codigo_empleado`,`nombre`,`apellido`,`telefono`,`correo`,`direccion`,`sueldo_base`,`codigo_tipo_empleado`,`usuario`) VALUES (1,'system','system','NA','NA','NA',0.0,1,'system');

-- ============================================
-- impuesto (todas las filas)
-- ============================================
INSERT INTO `impuesto` (`codigo_impuesto`,`descripcion_impuesto`,`porcentaje`) VALUES (1,'Exectos',0.0);
INSERT INTO `impuesto` (`codigo_impuesto`,`descripcion_impuesto`,`porcentaje`) VALUES (2,'Basicos',15.0);
INSERT INTO `impuesto` (`codigo_impuesto`,`descripcion_impuesto`,`porcentaje`) VALUES (3,'Lujo',18.0);

-- ============================================
-- marcas (limit 1)
-- ============================================
INSERT INTO `marcas` (`codigo_marca`,`descripcion`,`observacion`) VALUES (1,'Varios','');

-- ============================================
-- precios (todas las filas)
-- ============================================
INSERT INTO `precios` (`codigo_precio`,`descripcion`) VALUES (1,'Publico General');
INSERT INTO `precios` (`codigo_precio`,`descripcion`) VALUES (2,'Clientes Especiales');
INSERT INTO `precios` (`codigo_precio`,`descripcion`) VALUES (3,'Mayoristas');
INSERT INTO `precios` (`codigo_precio`,`descripcion`) VALUES (4,'Costos');

-- ============================================
-- proveedor (limit 1)
-- ============================================
INSERT INTO `proveedor` (`codigo_proveedor`,`nombre_proveedor`,`telefono`,`celular`,`direccion`) VALUES (1,'Inventario inicial','na','na','na');

-- ============================================
-- tipo_articulo (todas las filas)
-- ============================================
INSERT INTO `tipo_articulo` (`codigo_tipo_articulo`,`descripcion`) VALUES (1,'Bienes');
INSERT INTO `tipo_articulo` (`codigo_tipo_articulo`,`descripcion`) VALUES (2,'Servicios');

-- ============================================
-- tipo_cuenta_bancos (todas las filas)
-- ============================================
INSERT INTO `tipo_cuenta_bancos` (`id`,`tipo_cuenta`,`observaciones`) VALUES (1,'Ahorro','NA');
INSERT INTO `tipo_cuenta_bancos` (`id`,`tipo_cuenta`,`observaciones`) VALUES (2,'Cheque','NA');
INSERT INTO `tipo_cuenta_bancos` (`id`,`tipo_cuenta`,`observaciones`) VALUES (3,'Monena extanjera','NA');
INSERT INTO `tipo_cuenta_bancos` (`id`,`tipo_cuenta`,`observaciones`) VALUES (4,'NA','NA');

-- ============================================
-- tipo_empleado (todas las filas)
-- ============================================
INSERT INTO `tipo_empleado` (`codigo_tipo`,`descripcion`) VALUES (1,'Vendedor');

-- ============================================
-- tipo_factura (todas las filas)
-- ============================================
INSERT INTO `tipo_factura` (`id_tipo_factura`,`tipo_factura`) VALUES (1,'Contado');
INSERT INTO `tipo_factura` (`id_tipo_factura`,`tipo_factura`) VALUES (2,'Credito');

-- ============================================
-- tipo_movimiento_bancos (todas las filas)
-- ============================================
INSERT INTO `tipo_movimiento_bancos` (`id`,`tipo_movimiento`,`observaciones`) VALUES (1,'Depositos','NA');
INSERT INTO `tipo_movimiento_bancos` (`id`,`tipo_movimiento`,`observaciones`) VALUES (2,'Retiros','NA');

-- ============================================
-- tipo_movimiento_kardex (todas las filas)
-- ============================================
INSERT INTO `tipo_movimiento_kardex` (`codigo_tipo_moviemiento`,`movimiento`) VALUES (1,'Entrada');
INSERT INTO `tipo_movimiento_kardex` (`codigo_tipo_moviemiento`,`movimiento`) VALUES (2,'Salida');
INSERT INTO `tipo_movimiento_kardex` (`codigo_tipo_moviemiento`,`movimiento`) VALUES (3,'Saldos');

-- ============================================
-- tipo_pago (todas las filas)
-- ============================================
INSERT INTO `tipo_pago` (`codigo_tipo_pago`,`descripcion`) VALUES (1,'Efectivo');
INSERT INTO `tipo_pago` (`codigo_tipo_pago`,`descripcion`) VALUES (2,'Tarjeta');
INSERT INTO `tipo_pago` (`codigo_tipo_pago`,`descripcion`) VALUES (3,'Credito');

-- ============================================
-- usuario (todas las filas)
-- ============================================
INSERT INTO `usuario` (`id`,`usuario`,`nombre_completo`,`clave`,`permiso`,`tipo_permiso`,`codigo_caja`,`api_token`,`created_at`,`updated_at`) VALUES (1,'tecnico','system system','abc','Cajero',2,0,'na',NULL,'2020-05-11 18:09:56.0');
INSERT INTO `usuario` (`id`,`usuario`,`nombre_completo`,`clave`,`permiso`,`tipo_permiso`,`codigo_caja`,`api_token`,`created_at`,`updated_at`) VALUES (2,'admin','NA','4321','administrador',4,0,'',NULL,NULL);
INSERT INTO `usuario` (`id`,`usuario`,`nombre_completo`,`clave`,`permiso`,`tipo_permiso`,`codigo_caja`,`api_token`,`created_at`,`updated_at`) VALUES (3,'ventas','NA','123','Ventas',3,0,NULL,NULL,NULL);

SET FOREIGN_KEY_CHECKS=1;
