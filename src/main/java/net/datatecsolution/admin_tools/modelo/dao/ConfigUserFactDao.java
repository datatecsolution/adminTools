package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.Usuario;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConfigUserFactDao extends ModeloDaoBasic {
	private String sqlBaseJoin=null;

	public ConfigUserFactDao() {
		super("config_user_facturacion", "id");
		
		sqlBaseJoin="SELECT *, "
				+ " usuario.nombre_completo, "
				+ " usuario.permiso, "
				+ " usuario.tipo_permiso "
		+ " FROM "
				+super.DbName+ "."+super.tableName
			+ " JOIN "+super.DbName+".usuario "
			+ " on (usuario.usuario="+ super.tableName+".usuario) ";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
		
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		Caja caja=(Caja)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE id= ?");
			super.psConsultas.setInt( 1, caja.getCodigo() );
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
		ConfigUserFacturacion config=(ConfigUserFacturacion)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryInsert()+"(usuario,formato_factura,ventana_vendedor,pwd_descuento,pwd_precio,descuento_porcentaje,ventana_observaciones,precio_redondiar,facturar_sin_inventario,impr_report_categ_cierre,impr_report_salida,show_report_salida,impr_report_entrada,show_report_entrada,activar_busqueda_facturacion,pwd_entre_precio) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setString(1, config.getUsuario());
			super.psConsultas.setString(2, config.getFormatoFactura());
			super.psConsultas.setBoolean( 3, config.isVentanaVendedor());
			super.psConsultas.setBoolean( 4, config.isPwdDescuento());
			super.psConsultas.setBoolean( 5, config.isPwdPrecio());
			super.psConsultas.setBoolean( 6, config.isDescPorcentaje());
			super.psConsultas.setBoolean( 7, config.isVentanaObservaciones());
			super.psConsultas.setBoolean(8, config.isPrecioRedondear());
			
			super.psConsultas.setBoolean(9, config.isFacturarSinInventario());
			super.psConsultas.setBoolean(10, config.isImprReportCategCierre());
			super.psConsultas.setBoolean(11, config.isImprReportSalida());
			super.psConsultas.setBoolean(12, config.isShowReportSalida());
			super.psConsultas.setBoolean(13, config.isImprReportEntrada());
			super.psConsultas.setBoolean(14, config.isShowReportEntrada());
			super.psConsultas.setBoolean(15, config.isActivarBusquedaFacturacion());
			super.psConsultas.setBoolean(16, config.isPwdEntrePrecio());
			
			

		
			
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				config.setId(rs.getInt(1));
				
				
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
	public boolean actualizarFormatoContado(String formato,String usuario) {
		// TODO Auto-generated method stub


		//int resultado=0;
		Connection conn=null;

		try {

			//JOptionPane.showMessageDialog(null, "Datos Caja en modelo dao:"+caja.toString(),"Exito",JOptionPane.INFORMATION_MESSAGE);
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET formato_factura=? WHERE usuario=?");
			super.psConsultas.setString(1, formato);
			super.psConsultas.setString(2, usuario);
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
	}

	public boolean actualizarFormatoCredito(String formato,String usuario) {
		// TODO Auto-generated method stub


		//int resultado=0;
		Connection conn=null;

		try {

			//JOptionPane.showMessageDialog(null, "Datos Caja en modelo dao:"+caja.toString(),"Exito",JOptionPane.INFORMATION_MESSAGE);
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET formato_factura_credito=? WHERE usuario=?");
			super.psConsultas.setString(1, formato);
			super.psConsultas.setString(2, usuario);
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
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		ConfigUserFacturacion config=(ConfigUserFacturacion)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			
			//JOptionPane.showMessageDialog(null, "Datos Caja en modelo dao:"+caja.toString(),"Exito",JOptionPane.INFORMATION_MESSAGE);
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET ventana_vendedor=?,pwd_descuento=?,pwd_precio=?,descuento_porcentaje=?,ventana_observaciones=?, precio_redondiar=? ,facturar_sin_inventario=?,impr_report_categ_cierre=?,impr_report_salida=?,show_report_salida=?,impr_report_entrada=?,show_report_entrada=?,activar_busqueda_facturacion=?, agregar_cliente_credito=?, pwd_entre_precio=? WHERE usuario=?");
			super.psConsultas.setBoolean( 1, config.isVentanaVendedor());
			super.psConsultas.setBoolean( 2, config.isPwdDescuento());
			super.psConsultas.setBoolean( 3, config.isPwdPrecio());
			super.psConsultas.setBoolean( 4, config.isDescPorcentaje());
			super.psConsultas.setBoolean( 5, config.isVentanaObservaciones());
			
			super.psConsultas.setBoolean(6, config.isPrecioRedondear());
			
			super.psConsultas.setBoolean(7, config.isFacturarSinInventario());
			super.psConsultas.setBoolean(8, config.isImprReportCategCierre());
			super.psConsultas.setBoolean(9, config.isImprReportSalida());
			super.psConsultas.setBoolean(10, config.isShowReportSalida());
			super.psConsultas.setBoolean(11, config.isImprReportEntrada());
			super.psConsultas.setBoolean(12, config.isShowReportEntrada());
			super.psConsultas.setBoolean(13, config.isActivarBusquedaFacturacion());

			super.psConsultas.setBoolean(14, config.isAgregarClienteCredito());

			super.psConsultas.setBoolean(15, config.isPwdEntrePrecio());
			
			super.psConsultas.setString(16, config.getUsuario());
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
	}
	@Override
	public List<ConfigUserFacturacion> todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		List<ConfigUserFacturacion> configs=new ArrayList<ConfigUserFacturacion>();
		
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
				ConfigUserFacturacion config=new ConfigUserFacturacion();
				
				config.setId(res.getInt("id"));
				config.setUsuario(res.getString("usuario"));
				config.setFormatoFactura(res.getString("formato_factura"));
				config.setVentanaVendedor(res.getBoolean("ventana_vendedor"));
				config.setPwdDescuento(res.getBoolean("pwd_descuento"));
				config.setPwdPrecio(res.getBoolean("pwd_descuento"));
				config.setDescPorcentaje(res.getBoolean("descuento_porcentaje"));
				config.setVentanaObservaciones(res.getBoolean("ventana_observaciones"));
				config.setPrecioRedondear(res.getBoolean("precio_redondiar"));
				config.setFacturarSinInventario(res.getBoolean("facturar_sin_inventario"));
				config.setImprReportCategCierre(res.getBoolean("impr_report_categ_cierre"));
				config.setImprReportSalida(res.getBoolean("impr_report_salida"));
				config.setShowReportSalida(res.getBoolean("show_report_salida"));
				config.setImprReportEntrada(res.getBoolean("impr_report_entrada"));
				config.setShowReportEntrada(res.getBoolean("show_report_entrada"));
				config.setActivarBusquedaFacturacion(res.getBoolean("activar_busqueda_facturacion"));
				config.setPwdEntrePrecio(res.getBoolean("pwd_entre_precio"));
				config.setPwdPrecio(res.getBoolean("pwd_precio"));


				
				configs.add(config);
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
				return configs;
			}
			else return null;
	}
	
	
		public List<ConfigUserFacturacion> todos() {
		// TODO Auto-generated method stub
		List<ConfigUserFacturacion> configs=new ArrayList<ConfigUserFacturacion>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect());
			//super.psConsultas.setInt(1, limSupe);
			//super.psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				ConfigUserFacturacion config=new ConfigUserFacturacion();
				
				config.setId(res.getInt("id"));
				config.setUsuario(res.getString("usuario"));
				
				Usuario un=new Usuario();
				un.setUser(res.getString("usuario"));
				un.setNombre(res.getString("nombre_completo"));
				un.setTipoPermiso(res.getInt("tipo_permiso"));
				un.setPermiso(res.getString("permiso"));
				config.setUser(un);
				
				config.setFormatoFactura(res.getString("formato_factura"));
				config.setVentanaVendedor(res.getBoolean("ventana_vendedor"));
				config.setPwdDescuento(res.getBoolean("pwd_descuento"));
				config.setPwdPrecio(res.getBoolean("pwd_descuento"));
				config.setDescPorcentaje(res.getBoolean("descuento_porcentaje"));
				config.setVentanaObservaciones(res.getBoolean("ventana_observaciones"));
				config.setPrecioRedondear(res.getBoolean("precio_redondiar"));
				config.setFacturarSinInventario(res.getBoolean("facturar_sin_inventario"));
				config.setImprReportCategCierre(res.getBoolean("impr_report_categ_cierre"));
				config.setImprReportSalida(res.getBoolean("impr_report_salida"));
				config.setShowReportSalida(res.getBoolean("show_report_salida"));
				config.setImprReportEntrada(res.getBoolean("impr_report_entrada"));
				config.setShowReportEntrada(res.getBoolean("show_report_entrada"));
				config.setActivarBusquedaFacturacion(res.getBoolean("activar_busqueda_facturacion"));
				config.setAgregarClienteCredito(res.getBoolean("agregar_cliente_credito"));
				config.setFormatoFacturaCredito(res.getString("formato_factura_credito"));
				config.setPwdEntrePrecio(res.getBoolean("pwd_entre_precio"));
				config.setPwdPrecio(res.getBoolean("pwd_precio"));

				
				configs.add(config);
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
				return configs;
			}
			else return null;
	}


	

	


		public ConfigUserFacturacion buscarPorUser(String user) {
			// TODO Auto-generated method stub
			ConfigUserFacturacion config=new ConfigUserFacturacion();
			Connection con = null;
			ResultSet res=null;
			
			boolean existe=false;
			
			try {
				con = ConexionStatic.getPoolConexion().getConnection();
				
				//psConsultas = con.prepareStatement(super.getQuerySelect()+" where v_requisiciones.codigo_requisicion=?;");
				psConsultas = con.prepareStatement(super.getQuerySearch("usuario", "="));
				psConsultas.setString(1, user);
				psConsultas.setInt(2, 0);
				psConsultas.setInt(3, 1);
				res = psConsultas.executeQuery();
				while(res.next()){
					existe=true;
					
					config.setId(res.getInt("id"));
					config.setUsuario(res.getString("usuario"));
					config.setFormatoFactura(res.getString("formato_factura"));
					config.setVentanaVendedor(res.getBoolean("ventana_vendedor"));
					config.setPwdDescuento(res.getBoolean("pwd_descuento"));
					config.setPwdPrecio(res.getBoolean("pwd_descuento"));
					config.setDescPorcentaje(res.getBoolean("descuento_porcentaje"));
					config.setVentanaObservaciones(res.getBoolean("ventana_observaciones"));
					config.setPrecioRedondear(res.getBoolean("precio_redondiar"));
					config.setPwdPrecio(res.getBoolean("pwd_precio"));
					
					config.setFacturarSinInventario(res.getBoolean("facturar_sin_inventario"));
					config.setImprReportCategCierre(res.getBoolean("impr_report_categ_cierre"));
					config.setImprReportSalida(res.getBoolean("impr_report_salida"));
					config.setShowReportSalida(res.getBoolean("show_report_salida"));
					config.setImprReportEntrada(res.getBoolean("impr_report_entrada"));
					config.setShowReportEntrada(res.getBoolean("show_report_entrada"));
					config.setActivarBusquedaFacturacion(res.getBoolean("activar_busqueda_facturacion"));
					config.setAgregarClienteCredito(res.getBoolean("agregar_cliente_credito"));
					config.setFormatoFacturaCredito(res.getString("formato_factura_credito"));
					config.setPwdEntrePrecio(res.getBoolean("pwd_entre_precio"));
					
					
					
					
				 }
						
				} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
						System.out.println(e);
				}
			finally
			{
				try{
					
					if(res != null) res.close();
	                if(psConsultas != null)psConsultas.close();
	                if(con != null) con.close();
	                
					
					} // fin de try
					catch ( SQLException excepcionSql )
					{
						excepcionSql.printStackTrace();
						//conexion.desconectar();
					} // fin de catch
			} // fin de finally
			
			
			if (existe) {
				return config;
			}
			else return null;
		}

		@Override
		public Object buscarPorId(int id) {
			// TODO Auto-generated method stub
			return null;
		}

	

		

}
