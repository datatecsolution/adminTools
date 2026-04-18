-- Recrea todas las funciones, procedures y triggers de admin_tools.
-- Necesario para clientes existentes donde V1 fue baselineada sin ejecutar.
SET @ADMIN_TOOLS_ORIG_LBT = @@GLOBAL.log_bin_trust_function_creators;
SET GLOBAL log_bin_trust_function_creators = 1;

-- ============================================
-- FUNCTIONS (23)
-- ============================================

DROP FUNCTION IF EXISTS `f_can_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `f_costo_dev`;

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

DROP FUNCTION IF EXISTS `f_descripcion_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `f_detalle_cuenta_factura`;

DELIMITER $$
CREATE FUNCTION `f_detalle_cuenta_factura`(p_id_cuenta int(11)) RETURNS varchar(260) CHARSET utf8mb3
BEGIN

	return (select descripcion from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and tipo_movimiento=1  ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro ASC LIMIT 1);

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_existencia_articulo`;

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

DROP FUNCTION IF EXISTS `f_existencia_y_ordenes`;

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

DROP FUNCTION IF EXISTS `f_fecha_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `f_fecha_ultimo_pago_factura`;

DELIMITER $$
CREATE FUNCTION `f_fecha_ultimo_pago_factura`(p_id_cuenta int(11)) RETURNS varchar(50) CHARSET utf8mb3
BEGIN

	return (select fecha from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and tipo_movimiento=2  ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1);

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_get_botton_id_pag`;

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

DROP FUNCTION IF EXISTS `get_cod_kardex`;

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

DROP FUNCTION IF EXISTS `f_kardex`;

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

DROP FUNCTION IF EXISTS `f_no_dias_del_ultimo_pago`;

DELIMITER $$
CREATE FUNCTION `f_no_dias_del_ultimo_pago`(p_id_cuenta int(11)) RETURNS int
BEGIN

	return (SELECT DATEDIFF( CURDATE(), cuentas_por_cobrar_facturas.fecha) as dias from cuentas_por_cobrar_facturas WHERE cuentas_por_cobrar_facturas.codigo_cuenta = p_id_cuenta and (tipo_movimiento=2 or tipo_movimiento=1) ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1);

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_no_documento_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `f_no_factor`;

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

DROP FUNCTION IF EXISTS `f_pasar_numero_letra`;

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

DROP FUNCTION IF EXISTS `f_precio_articulo`;

DELIMITER $$
CREATE FUNCTION `f_precio_articulo`(p_codigo_articulo int(11)) RETURNS double(11,2)
BEGIN
	return (SELECT precios_articulos.precio_articulo
FROM precios_articulos
WHERE `precios_articulos`.`codigo_precio` = 1
AND `precios_articulos`.`codigo_articulo` = p_codigo_articulo) ;
end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_precio_general_articulo`;

DELIMITER $$
CREATE FUNCTION `f_precio_general_articulo`(p_codigo_articulo int(11)) RETURNS double(11,2)
BEGIN

return (SELECT `precios_articulos`.`precio_articulo` FROM `precios_articulos` WHERE `codigo_precio`= 1 and codigo_articulo=p_codigo_articulo);
	
end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_precio_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `f_saldo_cliente`;

DELIMITER $$
CREATE FUNCTION `f_saldo_cliente`(p_id_cliente int(11)) RETURNS double(11,2)
BEGIN

	return (SELECT saldo FROM cuentas_por_cobrar where codigo_cliente=p_id_cliente ORDER BY codigo_reguistro DESC limit 1);

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_saldo_factura_cliente`;

DELIMITER $$
CREATE FUNCTION `f_saldo_factura_cliente`(p_no_cuenta int(11)) RETURNS double(11,2)
BEGIN

	return (SELECT saldo FROM cuentas_por_cobrar_facturas where codigo_cuenta=p_no_cuenta ORDER BY codigo_reguistro DESC limit 1);

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_saldo_proveedor`;

DELIMITER $$
CREATE FUNCTION `f_saldo_proveedor`(p_codigo_proveedor int(11)) RETURNS double(11,2)
BEGIN

	return (ifnull ((SELECT saldo FROM cuentas_por_pagar where codigo_proveedor=p_codigo_proveedor ORDER BY codigo_reguistro DESC limit 1),0));

end$$
DELIMITER ;

DROP FUNCTION IF EXISTS `f_total_saldo_kardex`;

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

DROP FUNCTION IF EXISTS `sin_usar_verificar_kardex`;

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

DROP PROCEDURE IF EXISTS `agregar_entrada_kardex`;

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

DROP PROCEDURE IF EXISTS `agregar_salida_fact_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_ajuste_inventario_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_inventario_inicial_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_ajuste_inventario_movil`;

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

DROP PROCEDURE IF EXISTS `crear_compa_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_dev_compa_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_dev_venta_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_interes_facturas`;

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

DROP PROCEDURE IF EXISTS `crear_interes_facturas_optimizada`;

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

DROP PROCEDURE IF EXISTS `crear_requisicion_entrada_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_requisicion_salida_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_salida_venta_kardex_viejo`;

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

DROP PROCEDURE IF EXISTS `crear_venta_kardex`;

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

DROP PROCEDURE IF EXISTS `crear_venta_insumo_kardex`;

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

DROP PROCEDURE IF EXISTS `devolucion_sobre_venta`;

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

DROP PROCEDURE IF EXISTS `p_prueba_return_table`;

DELIMITER $$
CREATE PROCEDURE `p_prueba_return_table`()
BEGIN

      SELECT * FROM cajas where codigo=1;
END$$
DELIMITER ;

-- ============================================
-- TRIGGERS (8)
-- ============================================

DROP TRIGGER IF EXISTS `detalle_compra_b_inset`;

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

DROP TRIGGER IF EXISTS `detalle_devolucion_b_inset`;

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

DROP TRIGGER IF EXISTS `detalle_devolucion_compra_b_i`;

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

DROP TRIGGER IF EXISTS `detalle_factura_b_insert`;

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

DROP TRIGGER IF EXISTS `d_requisicion_b_insert`;

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

DROP TRIGGER IF EXISTS `order_b_delete`;

DELIMITER $$
CREATE TRIGGER `order_b_delete` BEFORE DELETE ON `encabezado_factura_temp` FOR EACH ROW BEGIN
		DELETE FROM detalle_factura_temp WHERE numero_factura = OLD.numero_factura;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `turno_bi`;

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

DROP TRIGGER IF EXISTS `usuario_a_insert`;

DELIMITER $$
CREATE TRIGGER `usuario_a_insert` AFTER INSERT ON `usuario` FOR EACH ROW begin
insert into config_user_facturacion(usuario,formato_factura) VALUES (NEW.usuario,"tiket");
end$$
DELIMITER ;

SET GLOBAL log_bin_trust_function_creators = @ADMIN_TOOLS_ORIG_LBT;
SET FOREIGN_KEY_CHECKS=1;
