package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.NumberToLetterConverter;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CotizacionDao extends ModeloDaoBasic {
	
	
	
	private DetallesCotizacioDao detallesDao=null;
	private ClienteDao myClienteDao=null;
	//private CuentaPorCobrarDao myCuentaCobrarDao=null;
	
	private Integer idFacturaGuardada=null;
	private String sqlBaseJoin=null;

	public CotizacionDao() {
		// TODO Auto-generated constructor stub
		super("encabezado_cotizacion","numero_cotizacion");
		detallesDao=new DetallesCotizacioDao();
		myClienteDao=new ClienteDao();
		//myCuentaCobrarDao=new CuentaPorCobrarDao();
		sqlBaseJoin="SELECT encabezado_cotizacion . fecha  AS  fecha1, "
							+ " encabezado_cotizacion . fecha  AS  fecha2 , "
							+ " lpad("
										+ "encabezado_cotizacion . numero_cotizacion , "
										+ "8, "
										+ "'0' "
								+ ")AS  numero_cotizacion,"
							+ " date_format("
											+ " encabezado_cotizacion . fecha , "
											+ " '%d/%m/%Y'"
										+ " )AS  fecha , "
							+ " cliente . codigo_cliente  AS  codigo_cliente , "
							+ " cliente . nombre_cliente  AS  nombre_cliente , "
							+ " cliente . direccion  AS  direccion , "
							+ " cliente . telefono  AS  telefono , "
							+ " cliente . movil  AS  movil , "
							+ " cliente . rtn  AS  rtn , "
							+ " encabezado_cotizacion . subtotal  AS  subtotal , "
							+ " encabezado_cotizacion . impuesto  AS  impuesto , "
							+ " encabezado_cotizacion . total  AS  total , "
							+ " encabezado_cotizacion . codigo  AS  codigo , "
							+ " encabezado_cotizacion . estado  AS  estado , "
							+ " encabezado_cotizacion . isv18  AS  isv18 , "
							+ " encabezado_cotizacion . usuario  AS  usuario , "
							+ " encabezado_cotizacion . descuento  AS  descuento , "
							+ " encabezado_cotizacion . total_letras  AS  total_letras , "
							+ " encabezado_cotizacion . subtotal_excento  AS  subtotal_excento , "
							+ " encabezado_cotizacion . subtotal15  AS  subtotal15 , "
							+ " encabezado_cotizacion . subtotal18  AS  subtotal18 , "
							+ " encabezado_cotizacion . isvOtros  AS  isvOtros  "
					+ " FROM "
						+super.DbName+ ".encabezado_cotizacion  "
							+ " JOIN  "+super.DbName+ ".cliente  "
									+ " ON (  cliente . codigo_cliente  =  encabezado_cotizacion . codigo_cliente )";
		
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c){
		Factura myFactura=(Factura)c;
		boolean resultado=false;
		ResultSet rs=null;
		
		Connection conn=null;
		//int idFactura=0;
		
		String sql= super.getQueryInsert()+" ("
				+ "fecha,"
				+ "subtotal,"
				+ "impuesto,"
				+ "total,"
				+ "codigo_cliente,"
				+ "descuento,"
				+ "estado,"
				+ "observacion,"
				+ "isv18,"
				+ "usuario,"
				+ "total_letras,"
				+ "codigo,"
				+ "subtotal_excento,"
				+ "subtotal15,"
				+ "subtotal18,"
				+ "isvOtros)"
				+ " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		try 
		{
			
			String nombreCliente=myFactura.getCliente().getNombre();//"Consumidor final";
			
			//si el cliente en escrito por el bombero
			if(myFactura.getCliente().getId()<0){
				myClienteDao.registrar(myFactura.getCliente());
				myFactura.getCliente().setId(myClienteDao.getIdClienteRegistrado());
			}
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setBigDecimal(1,myFactura.getSubTotal() );
			psConsultas.setBigDecimal(2, myFactura.getTotalImpuesto());
			psConsultas.setBigDecimal(3, myFactura.getTotal());
			psConsultas.setInt(4, myFactura.getCliente().getId());
			psConsultas.setBigDecimal(5, myFactura.getTotalDescuento());
			psConsultas.setString(6, "ACT");
			psConsultas.setString(7, myFactura.getObservacion());
			psConsultas.setBigDecimal(8, myFactura.getTotalImpuesto18());
			psConsultas.setString(9, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(10, NumberToLetterConverter.convertNumberToLetter(myFactura.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
			psConsultas.setInt(11, myFactura.getCodigoAlter());
			psConsultas.setBigDecimal(12, myFactura.getSubTotalExcento());
			psConsultas.setBigDecimal(13, myFactura.getSubTotal15());
			psConsultas.setBigDecimal(14, myFactura.getSubTotal18());
			psConsultas.setBigDecimal(15, myFactura.getTotalOtrosImpuesto1());
		
			
			
			
			psConsultas.executeUpdate();//se guarda el encabezado de la factura
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				//idFactura=rs.getInt(1);
				idFacturaGuardada=rs.getInt(1);
				myFactura.setIdFactura(idFacturaGuardada);
				
			}
			
			//se guardan los detalles de la fatura
			for(int x=0;x<myFactura.getDetalles().size();x++){
				
				if(myFactura.getDetalles().get(x).getArticulo().getId()!=-1)
					detallesDao.resgistrar(myFactura.getDetalles().get(x), idFacturaGuardada);
			}
			resultado= true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			//conexion.desconectar();
			resultado= false;
		}
		finally
		{
			try{
				if(rs != null) rs.close();
	            if(psConsultas != null)psConsultas.close();
	            if(conn != null) conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally
		
		
		
		
		return resultado;
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public List<Factura> todos(int limInf,int limSupe){
		
		
	    Connection con = null;
		
	    //Statement stmt = null;
	   	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			res = psConsultas.executeQuery();
			
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_cotizacion"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				//unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				
				if(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura())!=null)
					unaFactura.setDetalles(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura()));
				
				
				facturas.add(unaFactura);
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
				return facturas;
			}
			else return null;
		
	}

	public List<Factura> buscarPorFecha(String fecha1,String fecha2,int limitInferio, int canItemPag) {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
	    Connection con = null;
	    //String sql="select * from v_encabezado_cotizacion where fecha1 BETWEEN ? and ?;";
	   	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("fecha", "BETWEEN ? and "));
			
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_cotizacion"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				//unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				
				if(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura())!=null)
					unaFactura.setDetalles(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura()));
				
				
				facturas.add(unaFactura);
			 }
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
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
				return facturas;
			}
			else return null;
		
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar las facturas por id>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Factura buscarPorId(int id){
		
		
		
	    Connection con = null;
	    
	    //String sql="select * from v_encabezado_cotizacion where numero_cotizacion=?";
	
		Factura unaFactura=new Factura();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("numero_cotizacion", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_cotizacion"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				//unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				
				if(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura())!=null)
					unaFactura.setDetalles(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura()));
				
				
			
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return unaFactura;
			}
			else return null;
		
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Eliminar los proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> buscarPorCliente(String busqueda,int limitInferio, int canItemPag) {
	    Connection con = null;
	    
	    //String sql="select * from v_encabezado_cotizacion where nombre_cliente LIKE ?";
		  	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearchJoin("nombre_cliente", "LIKE", "cliente", "codigo_cliente", "codigo_cliente"));
			
			psConsultas.setString(1, "%" + busqueda+ "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_cotizacion"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				//unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				
				if(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura())!=null)
					unaFactura.setDetalles(detallesDao.buscarPorIdCotizacion(unaFactura.getIdFactura()));
				
				
				facturas.add(unaFactura);
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return facturas;
			}
			else return null;}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

}
