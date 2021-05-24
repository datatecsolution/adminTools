package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.CuentaFactura;
import net.datatecsolution.admin_tools.modelo.Empleado;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaFacturaDao extends ModeloDaoBasic {
	private String sqlBaseJoin=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;

	private Integer rowCount=0;



	private boolean todoReg=false;

	public CuentaFacturaDao() {
		super("cuentas_facturas", "codigo_cuenta");
		
		sqlBaseJoin="SELECT "
				+ super.tableName+".codigo_cuenta, "
				+ super.tableName+".fecha, "
				+ super.tableName+".fecha_vencimiento, "
				+ super.tableName+".no_factura, "
				+ super.tableName+".codigo_caja, "
				+ super.tableName+ ".codigo_cliente, "
				+ " cliente.nombre_cliente, "
				+ " cliente.telefono, "
				+ " cliente.direccion, "
				+ " cliente.rtn, "
				+ " empleados.codigo_empleado,"
				+ " empleados.nombre as nombre_vendedor,"
				+ " empleados.apellido as apellido_vendedor,"
				+ " cuenta2.ultimo_pago, "
				+ " cuenta2.saldo2, "
				+ " cuenta2.no_dias, "
				+ " cuenta2.detalle_venta, "
				+ " cuenta2.saldo2 as saldo"
		+ " FROM "
				+super.DbName+ "."+super.tableName
			+ " JOIN "+super.DbName+".cliente "
					+ " on (cliente.codigo_cliente="+ super.tableName+".codigo_cliente) "
			+ " JOIN "+super.DbName+".empleados "
				+ " on (cliente.id_vendedor=empleados.codigo_empleado) "
			+ " JOIN ( " +
						"SELECT "
									+super.tableName+".codigo_cuenta as cod2, "
									+" ifnull("+super.DbName+ "."+"f_saldo_factura_cliente("+super.tableName+".codigo_cuenta),0.00 ) saldo2, "
									+super.tableName+".codigo_cuenta, "
									+super.DbName+ "."+"f_detalle_cuenta_factura("+super.tableName+".codigo_cuenta) as detalle_venta, "
									+super.DbName+ "."+"f_fecha_ultimo_pago_factura("+super.tableName+".codigo_cuenta) as ultimo_pago, "
									+" ifnull("+super.DbName+ "."+"f_no_dias_del_ultimo_pago("+super.tableName+".codigo_cuenta),0.00 ) as no_dias"
					+  " FROM "+super.DbName+ "."+super.tableName+") cuenta2 "
				+ " on( cuenta2.codigo_cuenta="+super.tableName+".codigo_cuenta)";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		CuentaFactura cuenta=(CuentaFactura)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_cuenta= ?");
			super.psConsultas.setInt( 1, cuenta.getCodigoCuenta() );
			psConsultas.executeUpdate();
			return true;
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
		}
		finally{
				try{
								
					//if(res != null) res.close();
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
	                
					
					} // fin de try
					catch ( SQLException excepcionSql )
					{
						excepcionSql.printStackTrace();
						//Sconexion.desconectar();
					} // fin de catch
		}
		//return false;
	}

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		CuentaFactura cuentaFactura=(CuentaFactura)c;
		ResultSet rs=null;
		String fecha= cuentaFactura.getFactura().getFecha()==null? " now(), ":"'"+cuentaFactura.getFactura().getFecha()+" 00:00:00', ";
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			//fsfsf
			super.psConsultas=conn.prepareStatement(super.getQueryInsert()+"(fecha,fecha_vencimiento,codigo_cliente,no_factura,codigo_caja) VALUES ("+fecha+" DATE_ADD(now(), INTERVAL (SELECT dia_vencimiento_factura from config_app LIMIT 1) DAY),?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setInt(1, cuentaFactura.getCodigoCliente());
			super.psConsultas.setInt( 2, cuentaFactura.getNoFactura());
			super.psConsultas.setInt(3, cuentaFactura.getCodigoCaja());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				cuentaFactura.setCodigoCuenta(rs.getInt(1));
	
					
					
				}
			return true;
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
		}
		finally{
				try{
								
					//if(res != null) res.close();
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
	                
					
					} // fin de try
					catch ( SQLException excepcionSql )
					{
						excepcionSql.printStackTrace();
						//Sconexion.desconectar();
					} // fin de catch
		}
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public List<CuentaFactura> todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQueryRecord());
			super.psConsultas.setInt(1, limSupe);
			super.psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();


			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente2);
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}

	@Override
	public List<CuentaFactura> buscarPorId(int codigo) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		
		try {

			String conSaldo="";
			if(todoReg){
				conSaldo=" cuenta2.saldo2>0 ";
			}else {
				conSaldo=" (cuenta2.saldo2<>0 or  cuenta2.saldo2=0 )";
			}

			String whereBusqueda="";

			if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
				whereBusqueda=" and id_vendedor>?";
			}else{
				whereBusqueda=" and id_vendedor=?";
			}

			String whereBusquedaRuta="";

			if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
				whereBusquedaRuta=" and id_ruta_cobro>0";
			}else{
				whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
			}





			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cuentas_facturas.codigo_cuenta=?  and"+  conSaldo+whereBusqueda+whereBusquedaRuta +" order by cuentas_facturas.codigo_cuenta asc");
			super.psConsultas.setInt(1, codigo);
			super.psConsultas.setInt(2, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();


			
			while(res.next()){
				rowCount++;
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente=new Cliente();
				
				cliente.setId(res.getInt("codigo_cliente"));
				cliente.setNombre(res.getString("nombre_cliente"));
				cliente.setRtn(res.getString("rtn"));
				cliente.setTelefono(res.getString("telefono"));
				cliente.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente);
				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));
				
				cajas.add(unaCuenta);
				existe=true;
			 }

			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}



	public CuentaFactura buscarPorIdCuenta(int codigo) {
		// TODO Auto-generated method stub

		CuentaFactura unaCuenta=new CuentaFactura();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		try {


			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cuentas_facturas.codigo_cuenta=?  "+" order by cuentas_facturas.codigo_cuenta asc");
			super.psConsultas.setInt(1, codigo);

			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();


			while(res.next()){




				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));


				Cliente cliente=new Cliente();

				cliente.setId(res.getInt("codigo_cliente"));
				cliente.setNombre(res.getString("nombre_cliente"));
				cliente.setRtn(res.getString("rtn"));
				cliente.setTelefono(res.getString("telefono"));
				cliente.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente.setVendedor(empleado);

				unaCuenta.setCliente(cliente);
				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));

				existe=true;
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res != null) res.close();
				if(super.psConsultas != null)super.psConsultas.close();
				if(conn != null) conn.close();

			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally

		if (existe) {
			return unaCuenta;
		}
		else return null;
	}
	
	public CuentaFactura buscarPorId(int codigoCliente, int codigoCaja, int nofactura) {
		// TODO Auto-generated method stub
		CuentaFactura unaCuenta=new CuentaFactura();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cuentas_facturas.codigo_cliente=? and cuenta2.saldo2<>0 order by cuentas_facturas.codigo_cuenta asc");
			super.psConsultas.setInt(1, codigoCliente);
			
			System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente=new Cliente();
				
				cliente.setId(res.getInt("codigo_cliente"));
				cliente.setNombre(res.getString("nombre_cliente"));
				cliente.setRtn(res.getString("rtn"));
				cliente.setTelefono(res.getString("telefono"));
				cliente.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));
				
	
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return unaCuenta;
			}
			else return null;
	}


	public List<CuentaFactura> buscarConSaldoReporte(int diasRetrazados) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;


		String conSaldo="";
		if(todoReg){
			conSaldo=" cuenta2.saldo2>0 ";
		}else {
			conSaldo=" (cuenta2.saldo2<>0 or  cuenta2.saldo2=0 )";
		}

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda=" and id_vendedor>?";
		}else{
			whereBusqueda=" and id_vendedor=?";
		}

		String whereBusquedaRuta="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
			whereBusquedaRuta=" and id_ruta_cobro>0";
		}else{
			whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
		}



		try {

			//dsfa
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where "+conSaldo+whereBusqueda+whereBusquedaRuta+" and  no_dias >=30 and no_dias<="+diasRetrazados);
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());

			res = super.psConsultas.executeQuery();

			while(res.next()){
				rowCount++;
				CuentaFactura unaCuenta=new CuentaFactura();


				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));


				Cliente cliente2=new Cliente();

				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);

				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));
				unaCuenta.setDetalleCredito(res.getString("detalle_venta"));

				cajas.add(unaCuenta);
				existe=true;
			}


		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res != null) res.close();
				if(super.psConsultas != null)super.psConsultas.close();
				if(conn != null) conn.close();

			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally

		if (existe) {
			return cajas;
		}
		else return null;
	}



	
	
	public List<CuentaFactura> buscarConSaldo(int limInf, int limSupe) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;


		String conSaldo="";
		if(todoReg){
			conSaldo=" cuenta2.saldo2>0 ";
		}else {
			conSaldo=" (cuenta2.saldo2<>0 or  cuenta2.saldo2=0 )";
		}

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda=" and id_vendedor>?";
		}else{
			whereBusqueda=" and id_vendedor=?";
		}

		String whereBusquedaRuta="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
			whereBusquedaRuta=" and id_ruta_cobro>0";
		}else{
			whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
		}


		
		try {
			
			//dsfa
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where "+conSaldo+whereBusqueda+whereBusquedaRuta);
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());

			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				rowCount++;
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXidCliente(int codigo) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where saldo2>0 and cliente.codigo_cliente=? and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			super.psConsultas.setInt(1, codigo);
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXnombreCliente(String nombre) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;


		String conSaldo="";
		if(todoReg){
			conSaldo=" and  cuenta2.saldo2>0 ";
		}else {
			conSaldo=" and  (cuenta2.saldo2<>0 or  cuenta2.saldo2=0) ";
		}

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda=" and id_vendedor>?";
		}else{
			whereBusqueda=" and id_vendedor=?";
		}

		String whereBusquedaRuta="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
			whereBusquedaRuta=" and id_ruta_cobro>0";
		}else{
			whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
		}
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cliente.nombre_cliente like ? "+conSaldo+whereBusqueda+whereBusquedaRuta);
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			psConsultas.setString(1, "%" + nombre + "%");
			psConsultas.setInt(2, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				rowCount++;
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));

				cajas.add(unaCuenta);
				existe=true;
			 }
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXfecha(String fecha1,String fecha2) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		String conSaldo="";
		if(todoReg){
			conSaldo=" and  cuenta2.saldo2>0 ";
		}else {
			conSaldo=" and  (cuenta2.saldo2<>0 or  cuenta2.saldo2=0 ) ";
		}

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda=" and id_vendedor>?";
		}else{
			whereBusqueda=" and id_vendedor=?";
		}

		String whereBusquedaRuta="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
			whereBusquedaRuta=" and id_ruta_cobro>0";
		}else{
			whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
		}



		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where fecha BETWEEN ? and ? "+conSaldo+whereBusqueda+whereBusquedaRuta);
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();

			
			while(res.next()){
				rowCount++;
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));


				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);
				
				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));

				cajas.add(unaCuenta);
				existe=true;
			 }
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cajas;
			}
			else return null;
	}

	public boolean isTodoReg() {
		return todoReg;
	}

	public void setTodoReg(boolean todoReg) {
		this.todoReg = todoReg;
	}


	public List<CuentaFactura> buscarConSaldoXrtnCliente(String rtn) {
		// TODO Auto-generated method stub

		rowCount=0;
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;


		String conSaldo="";
		if(todoReg){
			conSaldo=" and  cuenta2.saldo2>0 ";
		}else {
			conSaldo=" and  cuenta2.saldo2<>0 ";
		}

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda=" and id_vendedor>?";
		}else{
			whereBusqueda=" and id_vendedor=?";
		}

		String whereBusquedaRuta="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo()==0){
			whereBusquedaRuta=" and id_ruta_cobro>0";
		}else{
			whereBusquedaRuta=" and id_ruta_cobro="+ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda().getCodigo();
		}




		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cliente.rtn like ? "+conSaldo+whereBusqueda+whereBusquedaRuta);
			psConsultas.setString(1, rtn + "%");
			psConsultas.setInt(2, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());

			res = super.psConsultas.executeQuery();

			while(res.next()){
				rowCount++;

				CuentaFactura unaCuenta=new CuentaFactura();


				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));


				Cliente cliente2=new Cliente();

				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				cliente2.setTelefono(res.getString("telefono"));
				cliente2.setDireccion(res.getString("direccion"));

				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setNombre(res.getString("nombre_vendedor"));
				empleado.setApellido(res.getString("apellido_vendedor"));

				cliente2.setVendedor(empleado);

				unaCuenta.setCliente(cliente2);

				//unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				unaCuenta.setFechaUltimoPago(res.getDate("ultimo_pago"));
				unaCuenta.setNoDiasUltimoPago(res.getInt("no_dias"));

				cajas.add(unaCuenta);
				existe=true;
			}


		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res != null) res.close();
				if(super.psConsultas != null)super.psConsultas.close();
				if(conn != null) conn.close();

			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally

		if (existe) {
			return cajas;
		}
		else return null;
	}

	public int getRows(ResultSet res){

		int totalRows = 0;
		try {
			//res.beforeFirst();
			res.last();
			totalRows = res.getRow();
			rowCount=totalRows;
			res.beforeFirst();
		}
		catch(Exception ex)  {
			return 0;
		}
		return totalRows ;
	}
	public Integer getRowCount(){
		return rowCount;
	}
}
