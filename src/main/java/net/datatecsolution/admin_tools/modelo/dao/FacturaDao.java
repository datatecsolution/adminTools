package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacturaDao extends ModeloDaoBasic {
	
	
	
	private DetalleFacturaDao detallesDao=null;
	private ClienteDao myClienteDao=null;
	private CuentaPorCobrarDao myCuentaCobrarDao=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;
	private CuentaFacturaDao cuentaFacturaDao=null;
	private Integer idFacturaGuardada=null;
	
	
	
	public FacturaDao(){
		
		super("encabezado_factura","numero_factura");
		
		
		
		super.setSqlQuerySelectJoin(getQueryJoin());
		
		detallesDao=new DetalleFacturaDao();
		myClienteDao=new ClienteDao();
		myCuentaCobrarDao=new CuentaPorCobrarDao();
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();
		cuentaFacturaDao=new CuentaFacturaDao();
	}
	
	private String getQueryJoin(){
		return "SELECT encabezado_factura.fecha AS fecha1, "
				+ " encabezado_factura.fecha AS fecha2, "
				+ " lpad(encabezado_factura.numero_factura,8,'0')AS numero_factura,"
				+ " lpad(encabezado_factura.numero_factura,8,'0')AS numero_factura2, "
				+ " date_format( encabezado_factura.fecha, '%d/%m/%Y' )AS fecha, "
				+ " cliente.codigo_cliente AS codigo_cliente, "
				+ " cliente.nombre_cliente AS nombre_cliente, "
				+ " cliente.direccion AS direccion, "
				+ " cliente.telefono AS telefono, "
				+ " cliente.movil AS movil, "
				+ " cliente.rtn AS rtn, "
				+ " encabezado_factura.subtotal, "
				+ " encabezado_factura.tipo_factura, "
				+ " encabezado_factura.impuesto AS impuesto, "
				+ " encabezado_factura.total AS total, "
				+ " encabezado_factura.codigo AS codigo, "
				+ " encabezado_factura.estado_factura AS estado_factura, "
				+ " encabezado_factura.isv18 AS isv18, "
				+ " encabezado_factura.usuario AS usuario, "
				+ " encabezado_factura.pago AS pago, "
				+ " encabezado_factura.descuento AS descuento, "
				+ " tipo_factura.tipo_factura AS tipo_factura_descripcion, "
				+ " ( encabezado_factura.pago - encabezado_factura.total )AS cambio, "
				+ " encabezado_factura.total_letras AS total_letras, "
				+ " tipo_pago.descripcion AS tipo_pago, "
				+ " concat( empleados.nombre,' ',empleados.apellido )AS vendedor, "
				+ " tipo_factura.id_tipo_factura AS id_tipo_factura, "
				+ " encabezado_factura.agrega_kardex AS agrega_kardex, "
				+ " encabezado_factura.subtotal_excento AS subtotal_excento, "
				+ " encabezado_factura.subtotal15 AS subtotal15, "
				+ " encabezado_factura.subtotal18 AS subtotal18, "
				+ " encabezado_factura.isvOtros AS isvOtros, "
				+ " encabezado_factura.cod_rango AS cod_rango, "
				+ " empleados.nombre AS nombre_vendedor, "
				+ " empleados.apellido AS apellido_vendedor, "
				+ " encabezado_factura.codigo_vendedor AS codigo_vendedor, "
				+ " encabezado_factura.cobro_tarjeta AS cobro_tarjeta,"
				+ " encabezado_factura.cobro_efectivo AS cobro_efectivo "
	+ " FROM "
			+ super.DbName+". encabezado_factura "
					+ " JOIN "+ DbNameBase+".cliente "
							+ " ON( cliente.codigo_cliente = encabezado_factura.codigo_cliente ) "
					+ " JOIN "+ DbNameBase+".tipo_factura "
							+ " ON( tipo_factura.id_tipo_factura = encabezado_factura.tipo_factura ) "
					+ " JOIN "+ DbNameBase+".tipo_pago "
							+ " ON( encabezado_factura.tipo_pago = tipo_pago.codigo_tipo_pago) "
					+ " JOIN "+ DbNameBase+".empleados "
							+ " ON( encabezado_factura.codigo_vendedor = empleados.codigo_empleado)";
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para conseguir la fecha>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public String getFechaSistema(){
		String fecha="";
		//DataSource ds=DBCPDataSourceFactory.getDataSource("mysql");
		Connection conn=null;
		ResultSet res=null;
		//Statement instrucions=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement("SELECT DATE_FORMAT(now(), '%d/%m/%Y') as fecha;");
			
			//res=getFecha.executeQuery(" SELECT DATE_FORMAT(now(), '%d/%m/%Y %h:%i:%s %p') as fecha;");
			res=psConsultas.executeQuery();
			while(res.next()){
				fecha=res.getString("fecha");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally{
			try{
				
				if(res != null) res.close();
	            if(psConsultas != null)psConsultas.close();
	            if(conn != null) conn.close();
	            
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		}
		
		
		return fecha;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c){
		Factura myFactura=(Factura)c;
		boolean resultado=false;
		ResultSet rs=null;
		
		Connection conn=null;
		//int idFactura=0;
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();

		String fecha= myFactura.getFecha()==null? " now(), ":"'"+myFactura.getFecha()+" 00:00:00', ";
		
		//se coloca la base de datos donde se guardara la factura, 
		String sql=super.getQueryInsert()+" ("
				+ "fecha,"
				+ "fecha_vencimiento, "
				+ "subtotal,"
				+ "impuesto,"
				+ "total,"
				+ "codigo_cliente,"
				+ "descuento,"
				+ "estado_factura,"
				+ "tipo_factura,"
				+ "tipo_pago,"
				+ "observacion,"
				+ "isv18,"
				+ "pago,"
				+ "usuario,"
				+ "total_letras,"
				+ "codigo_vendedor,"
				+ "codigo,"
				+ "estado_pago,"
				+ "subtotal_excento,"
				+ "subtotal15,"
				+ "subtotal18,"
				+ "isvOtros,"
				+ "cod_rango,"
				+ "cobro_tarjeta,"
				+ "cobro_efectivo) "
				+ " VALUES ("
					//			+ " now(), "
				+fecha
								+ " DATE_ADD(now(), INTERVAL (SELECT dia_vencimiento_factura from config_app LIMIT 1) DAY), "
								+ " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? "
						+ ")";

		
		
		
		try 
		{
			
			String nombreCliente=myFactura.getCliente().getNombre();//"Consumidor final";
			
			//si el cliente en escrito por el usuario
			if(myFactura.getCliente().getId()<0){
				
				myClienteDao.registrarClienteContado(myFactura.getCliente());
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
			psConsultas.setInt(7, myFactura.getTipoFactura());
			psConsultas.setInt(8, myFactura.getTipoPago());
			psConsultas.setString(9, myFactura.getObservacion());
			psConsultas.setBigDecimal(10, myFactura.getTotalImpuesto18());
			psConsultas.setBigDecimal(11, myFactura.getPago());
			psConsultas.setString(12, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(13, NumberToLetterConverter.convertNumberToLetter(myFactura.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
			psConsultas.setInt(14, myFactura.getVendedor().getCodigo());
			psConsultas.setInt(15, 1);
			psConsultas.setInt(16,myFactura.getEstadoPago());
			psConsultas.setBigDecimal(17, myFactura.getSubTotalExcento());
			psConsultas.setBigDecimal(18, myFactura.getSubTotal15());
			psConsultas.setBigDecimal(19, myFactura.getSubTotal18());
			psConsultas.setBigDecimal(20, myFactura.getTotalOtrosImpuesto1());
			
			
			
			//para relacionar el codigo de cai con cada factura
			DatosFacturacionDao caja=new DatosFacturacionDao();
			psConsultas.setInt(21, caja.getIdCaiAct(ConexionStatic.getUsuarioLogin().getCajaActiva()));
			
			psConsultas.setBigDecimal(22, myFactura.getCobroTarjeta());
			psConsultas.setBigDecimal(23, myFactura.getCobroEfectivo());
			
			
			
			
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
					detallesDao.agregarDetalle(myFactura.getDetalles().get(x), idFacturaGuardada);
			}
			
			
			//si la factura es al credito se guarda el credito del cliente
			if(myFactura.getTipoFactura()==2){
				myCuentaCobrarDao.reguistrarCredito(myFactura);






				//se crea la cuenta de la factura en la bd
				CuentaFactura unaCuentaFactura=new CuentaFactura();
				unaCuentaFactura.setCaja(ConexionStatic.getUsuarioLogin().getCajaActiva());
				myFactura.setCodigoCaja(ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
				unaCuentaFactura.setCliente(myFactura.getCliente());
				unaCuentaFactura.setFactura(myFactura);
				
				cuentaFacturaDao.registrar(unaCuentaFactura);
				
				cuentaXCobrarFacturaDao.reguistrarCredito(unaCuentaFactura);
			}
			
			resultado= true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado= false;
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
	public String getDbNameDefault(){
		return DbNameBase;
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar las facturas por id>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	
	public Factura buscarPorId(int id, Object c){
		
		Caja caja=(Caja)c;
		
		//se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
		
        Connection con = null;
        
    	Factura unaFactura=new Factura();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("numero_factura", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			//System.out.println(psConsultas);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				
				
				Cliente unCliente= new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setTelefono(res.getString("telefono"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				//unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
				
				
			
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

	public List<Factura> todos(int limInf,int limSupe,Object c){
		
		Caja caja=(Caja)c;
        Connection con = null;
        
        //se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
        List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
		
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				
				Cliente unCliente=new Cliente();
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
			
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				//unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
				
				
				facturas.add(unaFactura);
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
	
	public Integer getIdFacturaGuardada() {
		// TODO Auto-generated method stub
		return idFacturaGuardada;
	}
	
	
	
	
	public List<Factura> buscarPorFecha(String fecha1, String fecha2,int limitInferio, int canItemPag,Object c) {
		
		
		Caja caja=(Caja)c;

		//se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
		
        Connection con = null;
        
    	
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
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente=new Cliente();
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				//unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
				
				
				facturas.add(unaFactura);
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
	
	public boolean aplicarInteresVenc() {
		// TODO Auto-generated method stub
		
		
		int resultado;
		//boolean resultado=false;
		Connection conn=null;
		//fsdf
		String sql="{call crear_interes_facturas()}";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(sql);
			
			resultado=psConsultas.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		finally
		{
			try{
				
				//if(res != null) res.close();
                if(psConsultas != null)psConsultas.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
	//	return resultado;
	}

	public boolean anularFactura(Factura f) {
		// TODO Auto-generated method stub
		
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();
	
		boolean resultado=false;
		Connection conn=null;
		
		String sql=super.getQueryUpdate()+" SET "
				
				
				+ "estado_factura=?"
				
				+ " WHERE numero_factura = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(sql);
			
			
			psConsultas.setString(1, "NULA");
			
			psConsultas.setInt(2, f.getIdFactura());
			psConsultas.executeUpdate();
			
			
						
			
			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
			try{
				
				//if(res != null) res.close();
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
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Eliminar los proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> sinPagarCliente(Cliente myCliente) {
        Connection con = null;
        
    	/*String sql="SELECT "
				+ "encabezado_factura.numero_factura, "
				+ "DATE_FORMAT(encabezado_factura.fecha, '%d/%m/%Y') as fecha,"
				+ " encabezado_factura.subtotal, "
				+ "encabezado_factura.impuesto, "
				+ "encabezado_factura.total, "
				+ "encabezado_factura.codigo_cliente,"
				+ "encabezado_factura.codigo, "
				+ "encabezado_factura.estado_factura, "
				+ "encabezado_factura.isv18, "
				+ "encabezado_factura.tipo_factura, "
				+ "encabezado_factura.descuento,"
				+ "encabezado_factura.pago, "
				+ "encabezado_factura.usuario,"
				+ "encabezado_factura.estado_factura, "
				+ "encabezado_factura.agrega_kardex "
				+ " FROM encabezado_factura "
				+ "where encabezado_factura.codigo_cliente=? "
				+ " and "
				+ " encabezado_factura.estado_factura = 'ACT' "
				+ " and "
				+ " encabezado_factura.estado_pago = 0;";*/
        
       	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where encabezado_factura.codigo_cliente=? and encabezado_factura.estado_factura = 'ACT' and encabezado_factura.estado_pago = 0");
			
			psConsultas.setInt(1, myCliente.getId());
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente=myClienteDao.buscarPorId(res.getInt("codigo_cliente"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
				
				
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

	public boolean cambiarEstadoPago(Integer idFactura) {
		boolean resultado=false;
		Connection conn=null;
		String sql=super.getQuerySelect()+" SET "
				
				
				+ "estado_pago=?"
				
				+ " WHERE numero_factura = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(sql);
			
			
			psConsultas.setInt(1, 1);
			
			psConsultas.setInt(2, idFactura);
			psConsultas.executeUpdate();
						
			
			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}finally
		{
			try{
				
				//if(res != null) res.close();
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
	
	
	
	
	/**
	 * @return the ultimaFacturaUser
	 * @param user el nombre del usuario
	 */
	public Factura getUltimaFacturaUser(String user,Caja caja) {
		
	
				//se cambia la base de datos para las facturas de la caja seleccionada
		        super.DbName=caja.getNombreBd();
		        super.setSqlQuerySelectJoin(getQueryJoin());
	
		        Connection con = null;
		        
		    	String sql=super.getQuerySelect()+ " where usuario=? ORDER BY numero_factura DESC limit 1";
		        //Statement stmt = null;
		    	Factura unaFactura=new Factura();
				
				ResultSet res=null;
				
				boolean existe=false;
				try {
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas = con.prepareStatement(sql);
					psConsultas.setString(1, user);
					
					res = psConsultas.executeQuery();
					while(res.next()){
						
						existe=true;
						unaFactura.setIdFactura(res.getInt("numero_factura"));
						Cliente unCliente=myClienteDao.buscarPorId(res.getInt("codigo_cliente"));
						
						unaFactura.setCliente(unCliente);
						
						unaFactura.setFecha(res.getString("fecha"));
						unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
						unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
						unaFactura.setTotal(res.getBigDecimal("total"));
						//unaFactura.setEstado(res.getInt("estado_factura"));
						unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
						
						unaFactura.setEstado(res.getString("estado_factura"));
						unaFactura.setTipoFactura(res.getInt("tipo_factura"));
						unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
						
						unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
						
						
					
					 }
							
					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					}
				finally
				{
					//se restablece el nombre de la base de datos por defecto
					super.DbName= DbNameBase;
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
		//return ultimaFacturaUser;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> buscarPorNombreCliente(String nombre,int limitInferio, int canItemPag,Object c){
		
		
		Caja caja=(Caja)c;

		//se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
        
        Connection con = null;
        
    	
       	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(super.getQuerySearchJoin("nombre_cliente", "LIKE", "cliente", "codigo_cliente", "codigo_cliente"));
			
			psConsultas.setString(1, "%" + nombre + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			
			System.out.println(psConsultas);
			
			res = psConsultas.executeQuery();
			
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				//Cliente unCliente=myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				Cliente unCliente=new Cliente();
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				//unaFactura.setDetalles(detallesDao.getDetallesFactura(res.getInt("numero_factura")));
				
				
				facturas.add(unaFactura);
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
	
	public boolean verificarCierre(Caja caja,CierreFacturacion registroFacturasCaja) {
		// TODO Auto-generated method stub
		
		
		//se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
				
		        Connection con = null;
		        
		    	String sql=super.getQuerySelect()+" WHERE	numero_factura >= ? AND usuario = ? ";
				
				ResultSet res=null;
				
				boolean existe=false;
				try {
					con = ConexionStatic.getPoolConexion().getConnection();
					
					
					psConsultas = con.prepareStatement(sql);
					psConsultas.setInt(1,registroFacturasCaja.getNoFacturaInicio() );
					
					psConsultas.setString(2,ConexionStatic.getUsuarioLogin().getUser());
					
					res = psConsultas.executeQuery();
					while(res.next()){
						
						existe=true;
						
					
					 }
							
					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					}
				finally
				{
					//se restablece el nombre de la base de datos por defecto
					super.DbName= DbNameBase;
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
		return existe;
	}

	@Override
	public List todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void calcularCierre(Caja caja,CierreFacturacion registroFacturas,String usuario ,CierreCaja unaCierre) {
		// TODO Auto-generated method stub
		Connection con = null;
        
        //se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
		
		ResultSet res=null;
		
		boolean existe=false;
		
		 String sql="SELECT "
		 			+ "ifnull(sum(subtotal15),0) AS subtotal15,"
		 			+ "ifnull(SUM(subtotal18),0) AS subtotal18,"
		 			+ "ifnull(SUM(cobro_tarjeta),0) AS cobro_tarjeta,"
		 			+ "ifnull(SUM(cobro_efectivo),0) AS cobro_efectivo,"
		 			+ "ifnull(SUM(subtotal_excento),0) AS subtotal_excento,"
		 			+ "ifnull(SUM(total),0) AS total,"
		 			+ "ifnull(sum(impuesto),0) as impuesto,"
		 			+ "ifnull(sum(isv18),0) as isv18 "
		 		+ "from "
		 		 +super.DbName+".encabezado_factura "
		 			+ "where usuario=? "
		 			+ "and "
		 			+ "numero_factura >= ? "
		 			+ "and numero_factura <=? and estado_factura='ACT'";
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(sql);
			// se consigue se establece el nombre del usuario
			psConsultas.setString(1,usuario);
			
			//se establece el numero inicial de factura
			psConsultas.setInt(2, registroFacturas.getNoFacturaInicio());
			
			//se establece el numero final de la factura
			psConsultas.setInt(3,registroFacturas.getNoFacturaFinal() );
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				
				//unaCierre.setId(ultimoCierreUser.getId());
				
				//unaCierre.setNoFacturaInicio(ultimoCierreUser.getNoFacturaInicio());
				//unaCierre.setNoSalidaInicial(ultimoCierreUser.getNoSalidaInicial());/// este es el error
				//unaCierre.setNoCobroInicial(ultimoCierreUser.getNoCobroInicial());
				//unaCierre.setNoPagoInicial(ultimoCierreUser.getNoPagoInicial());
				
				
				unaCierre.setEfectivo(unaCierre.getEfectivo().add(res.getBigDecimal("cobro_efectivo")));
				//unaCierre.setEfectivo(res.getBigDecimal("cobro_efectivo"));
				
				//unaCierre.setCredito(totalCredito);Ojo
				unaCierre.setTarjeta(unaCierre.getTarjeta().add(res.getBigDecimal("cobro_tarjeta")));
				//unaCierre.setTarjeta(res.getBigDecimal("cobro_tarjeta"));
				
				unaCierre.setTotalExcento(unaCierre.getTotalExcento().add(res.getBigDecimal("subtotal_excento")));
				//unaCierre.setTotalExcento(res.getBigDecimal("subtotal_excento"));
				
				unaCierre.setTotalIsv15(unaCierre.getTotalIsv15().add(res.getBigDecimal("subtotal15")));
				//unaCierre.setTotalIsv15(res.getBigDecimal("subtotal15"));
				
				unaCierre.setTotalIsv18(unaCierre.getTotalIsv18().add(res.getBigDecimal("subtotal18")));
				//unaCierre.setTotalIsv18(res.getBigDecimal("subtotal18"));
				//unaCierre.setNoFacturaFinal(ultimaFacturaUsuario.getIdFactura());
				
				unaCierre.setIsv15(unaCierre.getIsv15().add(res.getBigDecimal("impuesto")));
				//unaCierre.setIsv15(res.getBigDecimal("impuesto"));
				
				unaCierre.setIsv18(unaCierre.getIsv18().add(res.getBigDecimal("isv18")));
				//unaCierre.setIsv18(res.getBigDecimal("isv18"));
				//unaCierre.setEfectivoInicial(ultimoCierreUser.getEfectivoInicial());
				//unaCierre.setNoCobroInicial(noCobro);
				
				//BigDecimal t_efectivo=ultimoCierreUser.getEfectivoInicial().add(res.getBigDecimal("total_efectivo"));//se suma el efectivo inicial y la venta con efectivo
				
				//unaCierre.setTotalEfectivo(t_efectivo);
				
				unaCierre.setTotal(unaCierre.getTotal().add(res.getBigDecimal("total")));
				//unaCierre.setTotal(res.getBigDecimal("total"));
				unaCierre.setUsuario(usuario);//.setTotal(res.getBigDecimal("total"));
			
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
		
		
		
	}
	
	
	/**
	 * @return the total en creditos en un rago de facturas para un usuario
	 * @param factInicial numero de factura inicial
	 * @param factFinal numero de factura final
	 */
		public BigDecimal getTotalCredito(Caja caja,int factInicial, int factFinal){
			
			//se cambia la base de datos para las facturas de la caja seleccionada
	        super.DbName=caja.getNombreBd();
	        super.setSqlQuerySelectJoin(getQueryJoin());
	        
			BigDecimal total=null;
			
			String sql="select "
	    			+ "			sum("
	    			+ "				`encabezado_factura`.`total` "
	    			+ "			) AS `total_efectivo` "
	    			+ "		from "
	    			+ super.DbName+".`encabezado_factura` "
	    			+ "		where "
	    			+ "					`encabezado_factura`.`tipo_factura` = 2 "
	    			+ "				and "
	    			+ "					`encabezado_factura`.`estado_factura` = 'ACT' "
	    			+ "				and "
	    			+ "					`encabezado_factura`.`numero_factura` >= ?"
	    			+ "				and "
	    			+ "					`encabezado_factura`.`numero_factura` <= ?"
	    			+ "				and "
	    			+ "					encabezado_factura.usuario =?";
			Connection con = null;
	        
	        
			
			ResultSet res=null;
			
			boolean existe=false;
			try {
				con = ConexionStatic.getPoolConexion().getConnection();
				
				this.psConsultas= con.prepareStatement(sql);
				psConsultas.setInt(1, factInicial);
				psConsultas.setInt(2, factFinal);
				psConsultas.setString(3, ConexionStatic.getUsuarioLogin().getUser());
				
				//System.out.println(psConsultas);
				res = psConsultas.executeQuery();
				while(res.next()){
					
					existe=true;
					total=new BigDecimal(res.getDouble("total_efectivo"));
					
					
					
				
				 }
						
				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				}
			finally
			{
				//se restablece el nombre de la base de datos por defecto
				super.DbName= DbNameBase;
				
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
					return total;
				}
				else return null;
		}

	public boolean verificarCierre(List<Caja> cajas) {
		// TODO Auto-generated method stub
		boolean resultado=false;
		CierreFacturacionDao cierreFacturasDao=new CierreFacturacionDao();
		
		CierreCajaDao cierreCajaDao=new CierreCajaDao();
		
		CierreCaja ultimoCierreUser=cierreCajaDao.getCierreUltimoUser();
		
		//se recorren las caja en busca de facturas para realizar el cierre
		for(int x=0;x<cajas.size();x++){
			
			//se extren los registros de las caja(numero factura inicial)
			CierreFacturacion registroFacturasCaja=cierreFacturasDao.buscarPorCajaUsuario( ConexionStatic.getUsuarioLogin().getCajas().get(x), 
					 																		ConexionStatic.getUsuarioLogin().getUser(), 
					 																		ultimoCierreUser.getId());
			//se verifica que el registro de la factura no este vacio
			if(registroFacturasCaja!=null){	
				
				//se busca la existencia de facturas para esa caja
				resultado=verificarCierre(cajas.get(x),registroFacturasCaja);
				//si encontro un factua para realizar el cierre sale del for
				if(resultado){
					break;
				}
				
			}else{//si esta vacio se se verifica que hay facturas hechas en esa caja para crear el registro
				//se crea un registro vacio
				CierreFacturacion newFacturasCaja=new CierreFacturacion();
				//se verifica si hay factuas en esa caja
				boolean result2=verificarCierre(cajas.get(x),newFacturasCaja);
				if(result2){//si hay factura se crea el registro
					newFacturasCaja.setCodigoCierre(ultimoCierreUser.getId());
					newFacturasCaja.setUsuario(ConexionStatic.getUsuarioLogin().getUser());
					newFacturasCaja.setCaja(cajas.get(x));
					cierreFacturasDao.registrar(newFacturasCaja);
					//resultado=true;
					
				}
			}
		}
		
		return resultado;
	}
	public void getComiciones(String fecha1, String fecha2,Caja caja,List<Comision> comisiones, int comisionPorcentaje){
		
		
		Connection con = null;
		boolean existe=false;
		
		 //se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
       
        String sql="SELECT empleados.nombre, "
        		+ " empleados.apellido,"
        		+ "	encabezado_factura.codigo_vendedor, "
        		+ " max(encabezado_factura.fecha) AS fecha, "
        		+ " sum(encabezado_factura.total) AS total_ventas, "
        		+ " sum("+super.DbName+".f_costo_factura(encabezado_factura.numero_factura)) AS total_costos, "
        		+ " sum(if(encabezado_factura.tipo_factura=2,encabezado_factura.total,0))  total_credito,"
        		+ " sum(if(encabezado_factura.tipo_factura=1,encabezado_factura.total,0))  total_contado,"
        		+ " COUNT(encabezado_factura.numero_factura) AS no_clientes "
        		+ " FROM "+super.DbName+".encabezado_factura "
        				+ " INNER JOIN "+ DbNameBase+".empleados "
        						+ " ON encabezado_factura.codigo_vendedor = empleados.codigo_empleado "
        						+ " where DATE_FORMAT(fecha,'%y-%m-%d') BETWEEN CAST(? AS DATE) and CAST(? AS DATE) "
        						+ " and encabezado_factura.estado_factura = 'ACT'"
        						+ "GROUP BY encabezado_factura.codigo_vendedor";
        
		
		ResultSet res=null;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
		
			while(res.next()){
				int itemEmpleado=-1;
				for(int x=0;x<comisiones.size();x++){
					if(comisiones.get(x).getCodigoVendedor()==res.getInt("codigo_vendedor")){
						itemEmpleado=x;
					}
				}
				if(itemEmpleado>=0){
					comisiones.get(itemEmpleado).setClienteAtendidos(res.getInt("no_clientes"));
					comisiones.get(itemEmpleado).setTotalVentas(res.getDouble("total_ventas"));
					comisiones.get(itemEmpleado).setTotalCredito(res.getDouble("total_credito"));
					comisiones.get(itemEmpleado).setTotalContado(res.getDouble("total_contado"));
					comisiones.get(itemEmpleado).setTotalCosto(res.getDouble("total_costos"));
					comisiones.get(itemEmpleado).setPorcentaje(comisionPorcentaje);
					comisiones.get(itemEmpleado).calcularComision();
					comisiones.get(itemEmpleado).setPedidasGanacias();
					//JOptionPane.showMessageDialog(null, comisiones.get(itemEmpleado).toString());
				}
				
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
		
		
		
	}
	
	public void getVentasUsuarios(String fecha1, String fecha2,Caja caja,List<Comision> comisiones, int comisionPorcentaje){
		
		
		Connection con = null;
		boolean existe=false;
		
		 //se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
       
        String sql="SELECT  "
        		+ "	usuario.id, "
        		+ " max(encabezado_factura.fecha) AS fecha, "
        		+ " sum(encabezado_factura.total) AS total_ventas, "
        		+ " COUNT(encabezado_factura.numero_factura) AS no_clientes "
        		+ " FROM "+super.DbName+".encabezado_factura "
        				+ " INNER JOIN "+ DbNameBase+".usuario "
        						+ " ON encabezado_factura.usuario = usuario.usuario "
        						+ " where fecha BETWEEN ? and ? "
        						+ " and encabezado_factura.estado_factura = 'ACT'"
        						+ "GROUP BY usuario.id";
        
		
		ResultSet res=null;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
		
			while(res.next()){
				
				int itemPosicion=-1;
				//se recorren la comisiones en busquedas de los usuarios
				for(int x=0;x<comisiones.size();x++){
					//si el usuario es igual a lo devuelto por la bd
					if(comisiones.get(x).getCodigoVendedor()==res.getInt("id")){
						//se estable el item con el cual se va trabajar
						itemPosicion=x;
						
						//System.out.println(res.getString("usuario"));
					}
				}
				//si se encotro el item de la consulta con los item la lista
				if(itemPosicion>=0){
					comisiones.get(itemPosicion).setClienteAtendidos(res.getInt("no_clientes"));
					comisiones.get(itemPosicion).setTotalVentas(res.getDouble("total_ventas"));
					comisiones.get(itemPosicion).setPorcentaje(comisionPorcentaje);
					comisiones.get(itemPosicion).calcularComision();
					//JOptionPane.showMessageDialog(null, comisiones.get(itemPosicion).toString());
				}
				
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				//JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
		
		
		
	}
	
	
public void getVentasCategorias(Integer noFacturaIncial, Integer noFacturaFinal,String usuario,Caja caja,List<VentasCategoria> ventas){
		
		
		Connection con = null;
		boolean existe=false;
		
		
		 //se cambia la base de datos para las facturas de la caja seleccionada
        super.DbName=caja.getNombreBd();
        super.setSqlQuerySelectJoin(getQueryJoin());
       
        String sql="SELECT COUNT(detalle_factura.id) as cantidad, "
        				+ " marcas.codigo_marca as codigo_marca, "
        				+ " marcas.descripcion as descripcion, "
        				+ " sum( detalle_factura.total) as total_item "
        		+ " FROM "+super.DbName+".encabezado_factura "
        						+ " INNER JOIN "+super.DbName+".detalle_factura "
        								+ "	ON encabezado_factura.numero_factura = detalle_factura.numero_factura "
								+ " INNER JOIN "+ DbNameBase+".articulo "
										+ " ON detalle_factura.codigo_articulo = articulo.codigo_articulo"
								+ " INNER JOIN "+ DbNameBase+".marcas "
										+ " ON marcas.codigo_marca = articulo.codigo_marca "
				+ " WHERE "
						+ " encabezado_factura.numero_factura >= ?  "
						+ " AND "
						+ " encabezado_factura.numero_factura <= ? "
						+ " AND "
						+ " encabezado_factura.estado_factura = 'ACT' "
						+ " AND "
						+ " encabezado_factura.usuario = ? "
						+ " GROUP BY marcas.codigo_marca";
        
		
		ResultSet res=null;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(sql);
			psConsultas.setInt(1, noFacturaIncial);
			psConsultas.setInt(2, noFacturaFinal);
			psConsultas.setString(3, usuario);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
		
			while(res.next()){
				
				int itemPosicion=-1;
				//se recorren la comisiones en busquedas de los usuarios
				for(int x=0;x<ventas.size();x++){
					//si el usuario es igual a lo devuelto por la bd
					if(ventas.get(x).getCodigoCategoria()==res.getInt("codigo_marca")){
						//se estable el item con el cual se va trabajar
						itemPosicion=x;
					}
				}
				//si se encotro el item de la consulta con los item la lista
				if(itemPosicion>=0){
				
					ventas.get(itemPosicion).setTotalVentas(res.getDouble("total_item"));
					
				}
				
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				//JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
		
		
		
	}

	

}
