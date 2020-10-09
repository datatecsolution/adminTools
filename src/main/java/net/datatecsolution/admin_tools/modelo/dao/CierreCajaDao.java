package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.CierreCaja;
import net.datatecsolution.admin_tools.modelo.CierreFacturacion;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Factura;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CierreCajaDao extends ModeloDaoBasic {
	
	
	public int idUltimoRequistro=0;
	private FacturaDao myFacturaDao;
	private String sqlJoin;
	private CierreFacturacionDao cierreFacturasDao;
	public CierreCajaDao(){
		super("cierre_caja","idCierre");
		
		sqlJoin="SELECT cierre_caja.idCierre, "
					+ " date_format( cierre_caja.fecha, '%d/%m/%Y') AS fecha, "
					+ " cierre_caja.fecha AS fecha2, "
					+ " cierre_caja.factura_inicial, "
					+ " cierre_caja.factura_final, "
					+ " cierre_caja.efectivo, "
					+ " cierre_caja.estado, "
					+ " cierre_caja.efectivo_inicial, "
					+ " cierre_caja.no_salida_final, "
					+ " cierre_caja.no_salida_inicial, "
					+ " cierre_caja.no_entrada_final, "
					+ " cierre_caja.no_entrada_inicial, "
					+ " cierre_caja.no_cobro_inicial, "
					+ " cierre_caja.no_cobro_final, "
					+ " cierre_caja.no_pago_inicial, "
					+ " cierre_caja.no_pago_final, "
					+ " ifnull(cierre_caja.creditos, 0)AS creditos, "
					+ " cierre_caja.isv15, "
					+ " cierre_caja.isv18, "
					+ " cierre_caja.totalventa, "
					+ " cierre_caja.turno, "
					+ " ifnull(cierre_caja.totalimpuesto,0)AS totalimpuesto, "
					+ " ifnull(cierre_caja.tarjeta, 0)AS tarjeta, "
					+ " ifnull(cierre_caja.usuario, ' ')AS usuario "
				+ " FROM "
					+ super.DbName+".cierre_caja ";
					//		+ " Join "+super.DbNameBase+".cierre_facturacion "
						//			+ "on cierre_caja.idCierre=cierre_facturacion.codigo_cierre";
		super.setSqlQuerySelectJoin(sqlJoin);
	
		myFacturaDao=new FacturaDao();
		cierreFacturasDao=new CierreFacturacionDao();
	}
	@Override
	public boolean registrar(Object c){
		boolean resultado=false;
		 Connection con = null;
		 
		 ResultSet rs=null;
		 
		 //SE CONSIGUE EL ITEM PARA EL CIERRE DE CAJA
		 CierreCaja unCierre=this.getCierre();
		 String sql= "INSERT INTO cierre_caja("
					+ "fecha,"
					+ "factura_inicial,"
					+ "factura_final,"
					+ "efectivo,"
					+ "creditos,"
					+ "totalventa,"
					+ "tarjeta,"
					+ "usuario,"
					+ "isv15,"
					+ "isv18)"
					+ " VALUES (now(),?,?,?,?,?,?,?,?,?)";
			 if(unCierre!=null&&unCierre.getNoFacturaFinal()!=0){
				 try {
						con = ConexionStatic.getPoolConexion().getConnection();
						psConsultas=
								con.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
						
						psConsultas.setInt(1,unCierre.getNoFacturaInicio() );
						psConsultas.setInt(2,unCierre.getNoFacturaFinal() );
						psConsultas.setBigDecimal(3, unCierre.getEfectivo());
						psConsultas.setBigDecimal(4, unCierre.getCredito());
						psConsultas.setBigDecimal(5, unCierre.getTotal());
						psConsultas.setBigDecimal(6, unCierre.getTarjeta());
						psConsultas.setString(7, unCierre.getUsuario());
						psConsultas.setBigDecimal(8, unCierre.getIsv15());
						psConsultas.setBigDecimal(9, unCierre.getIsv18());
						
						
						
						
						psConsultas.executeUpdate();//se guarda el encabezado de la factura
						
						
						rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
						while(rs.next()){
							this.idUltimoRequistro=rs.getInt(1);
							//this.setIdArticuloRegistrado(rs.getInt(1));
						}
						resultado=true;
						
				 }catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
						resultado=false;
					}
				finally
				{
					try{
						
						if(rs != null) rs.close();
						if(psConsultas != null)psConsultas.close();
						if(con != null) con.close();
		             
						
						} // fin de try
						catch ( SQLException excepcionSql )
						{
							excepcionSql.printStackTrace();
							//conexion.desconectar();
						} // fin de catch
				} // fin de finally
		
		 }else{
			 resultado=false;
		 }
		
		return resultado;
	}
	public boolean actualizarCierre(BigDecimal total){
		
		
		boolean resultado=false;
		 Connection con = null;
		 
		 ResultSet rs=null;
		 
		 
		 //SE calculan todos los datos para el actualizar el cierre
		 //se crea este objeto en donde se volcaran todos los datos
		 CierreCaja newCierre=new CierreCaja();
		 
		//SE consigue el ultimo cierre de caja activo del usuario
		 CierreCaja ultimoCierreUser=this.getCierreUltimoUser();
		 
		//se llenan el nuevo objeto cierre para la actualizacion
	 	newCierre.setId(ultimoCierreUser.getId());
		newCierre.setNoSalidaInicial(ultimoCierreUser.getNoSalidaInicial());/// este es el error
		newCierre.setNoEntradaInicial(ultimoCierreUser.getNoEntradaInicial());
		newCierre.setNoCobroInicial(ultimoCierreUser.getNoCobroInicial());
		newCierre.setNoPagoInicial(ultimoCierreUser.getNoPagoInicial());
		newCierre.setEfectivoInicial(ultimoCierreUser.getEfectivoInicial());
	
		 
		 //se recorre las cajas asignadas al usuario para calcular en total de la facturacion
		 for(int x=0;x<ConexionStatic.getUsuarioLogin().getCajas().size();x++){
			 //se consigue la ultima factura realiza en la caja
			 Factura ultimaFactura=myFacturaDao.getUltimaFacturaUser(ConexionStatic.getUsuarioLogin().getUser(), ConexionStatic.getUsuarioLogin().getCajas().get(x));
			 
			 CierreFacturacion registroFacturasCaja=null;
			 
			 if(ultimaFactura!=null)
			 {
					 //se extren los registros de las caja(numero factura inicial)
					registroFacturasCaja=cierreFacturasDao.buscarPorCajaUsuario( ConexionStatic.getUsuarioLogin().getCajas().get(x), 
							 																		ConexionStatic.getUsuarioLogin().getUser(), 
							 																		ultimoCierreUser.getId());
			 }
			 //sin los registro de la caja no es null se calculan los otros elementos
			 if(registroFacturasCaja!=null)
			 {
				 //se registra el numero de factura final
				 registroFacturasCaja.setNoFacturaFinal(ultimaFactura.getIdFactura());
				
				 //se calcula el total de la facturacion de la caja
				 myFacturaDao.calcularCierre(ConexionStatic.getUsuarioLogin().getCajas().get(x),registroFacturasCaja,ConexionStatic.getUsuarioLogin().getUser(),newCierre);
				 
				 //se le asigna al cierre los datos de las facturas que se calculo el todal
				 newCierre.getCierreFacturas().add(registroFacturasCaja);
				 
				 //se consigue de la base de datos el total de en creditos de un terminando grupos de facturas para un usuario
				 BigDecimal totalCredito=myFacturaDao.getTotalCredito(ConexionStatic.getUsuarioLogin().getCajas().get(x),registroFacturasCaja.getNoFacturaInicio(),registroFacturasCaja.getNoFacturaFinal());
				 
				 //se agrega el total de credito
				 newCierre.setCredito(newCierre.getCredito().add(totalCredito));
			 }
			 
		 }
		
		 
		 newCierre.setEfectivoCaja(total);
		 
		 
		 String sql=super.getQueryUpdate()+ " set "
		 		//+ "fecha=now(),"
		 		+ "fecha_final=now(), "
		 		+ "factura_final=?, "
		 		+ "efectivo=?, "
		 		+ "creditos=?, "
		 		+ "tarjeta=?, "
		 		+ "isv15=?, "
		 		+ "isv18=?, "
		 		+ "total_isv15=?, "
		 		+ "total_isv18=?, "
		 		+ "total_excento=?, "
		 		+ "estado=?, "
		 		+ "total_efectivo=?, "
		 		+ "totalventa=?, "
		 		+ "no_salida_final=?, "
		 		+ "total_salida=?, "
		 		+ "no_entrada_final=?, "
		 		+ "total_entrada=?, "
		 		+ "no_cobro_final=?, "
		 		+ "total_cobro=?, "
		 		+ "efectivo_caja =?, "
		 		+ "no_pago_final=?, "
		 		+ "total_pago=? "
		 		+ "where "
		 		+ "idCierre=?";
		 		//+ "
		
			 if(newCierre!=null){
				 
				
				 try {
					 	con = ConexionStatic.getPoolConexion().getConnection();
						psConsultas=con.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
						
						//se suma el efectivo inicial y la venta con efectivo
						BigDecimal t_efectivo=newCierre.getEfectivoInicial().add(newCierre.getEfectivo());
					 	newCierre.setTotalEfectivo(t_efectivo);
					 	
					 	//se consigue el total de las salidas asi como la ultima
					 	SalidaCajaDao salidaDao=new SalidaCajaDao();
					 	
					 	//se manda el cierre para que sea modificado con las salidas
					 	salidaDao.calcularTotal(newCierre);
				
					 	//unCierre.setTotalSalida(salidaDao.calcularTotal1(unCierre));
					 	newCierre.setTotalEfectivo(newCierre.getTotalEfectivo().subtract(newCierre.getTotalSalida()));
					 	
					 	
					 	//se consigue el total de las entradas asi como la ultima
					 	EntradaCajaDao entradaDao=new EntradaCajaDao();
					 	
					 	//se manda el cierre para que sea modificado con las entradas
					 	entradaDao.calcularTotal(newCierre);
				
					 	//unCierre.setTotalSalida(salidaDao.calcularTotal1(unCierre));
					 	newCierre.setTotalEfectivo(newCierre.getTotalEfectivo().add(newCierre.getTotalEntrada()));
					 	
					 	
					 	
					 	//se crea el objeto que para consultar los cobros
					 	ReciboPagoDao reciboDao=new ReciboPagoDao();
					 	
					 	//se manda el cierre para que sea modificado con las salidas y el total
					 	reciboDao.calcularTotalCierre(newCierre);
					 	//se calcula el total en efectivo sumando los cobros
					 	newCierre.setTotalEfectivo(newCierre.getTotalEfectivo().add(newCierre.getTotalCobro()));
					 	
					 	//se crea el objeto que para consultar los cobros
					 	ReciboPagoProveedoresDao reciboProveedoresDao=new ReciboPagoProveedoresDao();
					 	
					 	//se manda el cierre para que sea modificado con los pagos y el total
					 	reciboProveedoresDao.calcularTotalPagosCierre(newCierre);
					 	
					 	//se calcula el total en efectivo restandoles los pagos
					 	newCierre.setTotalEfectivo(newCierre.getTotalEfectivo().subtract(newCierre.getTotalPago()));
					 	
						
						
						psConsultas.setInt(1,newCierre.getNoFacturaFinal() );
						psConsultas.setDouble(2,newCierre.getEfectivo().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
						psConsultas.setBigDecimal(3, newCierre.getCredito().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						psConsultas.setBigDecimal(4, newCierre.getTarjeta().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setBigDecimal(5, newCierre.getIsv15().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setBigDecimal(6, newCierre.getIsv18().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setBigDecimal(7, newCierre.getTotalIsv15().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						psConsultas.setBigDecimal(8, newCierre.getTotalIsv18().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setBigDecimal(9, newCierre.getTotalExcento().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(10, 0);
						psConsultas.setBigDecimal(11, newCierre.getTotalEfectivo().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						psConsultas.setBigDecimal(12, newCierre.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(13, newCierre.getNoSalidaFinal());
						psConsultas.setBigDecimal(14, newCierre.getTotalSalida().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(15, newCierre.getNoEntradaFinal());
						psConsultas.setBigDecimal(16, newCierre.getTotalEntrada().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(17, newCierre.getNoCobroFinal());
						
						psConsultas.setBigDecimal(18, newCierre.getTotalCobro().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setBigDecimal(19, newCierre.getEfectivoCaja().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(20, newCierre.getNoPagoFinal());
						
						psConsultas.setBigDecimal(21, newCierre.getTotalPago().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						psConsultas.setInt(22, newCierre.getId());
						
						
						
						
						
						
						
						
						psConsultas.executeUpdate();//se guarda el encabezado de la factura
						
						
						rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
						while(rs.next()){
							this.idUltimoRequistro=rs.getInt(1);
							//this.setIdArticuloRegistrado(rs.getInt(1));
						}
						this.idUltimoRequistro=newCierre.getId();
						resultado=true;
						
						//se recorre los registros de las facturas procesadas para actualizarlo
						for(int y=0;y<newCierre.getCierreFacturas().size();y++){
							//se actualiza el rango de factuas procesas por caja
							cierreFacturasDao.actualizar(newCierre.getCierreFacturas().get(y));
						}
						
				 }catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
						resultado=false;
					}
				finally
				{
					try{
						
						if(rs != null) rs.close();
						if(psConsultas != null)psConsultas.close();
						if(con != null) con.close();
		             
						
						} // fin de try
						catch ( SQLException excepcionSql )
						{
							excepcionSql.printStackTrace();
							//conexion.desconectar();
						} // fin de catch
				} // fin de finally
		
		 }else{
			 
			 resultado=false;
			 
			 
		 }
		
		return resultado;
	}
	
	public boolean registrarCierre(CierreCaja unCierre){
		boolean resultado=false;
		 Connection con = null;
		 
		 ResultSet rs=null;
		 
		 
		 String sql= super.getQueryInsert()+ " ("
					+ "fecha,"
					+ "fecha_inicio,"
					+ "fecha_final,"
					+ "factura_inicial,"
					+ "factura_final,"
					+ "efectivo,"
					+ "creditos,"
					+ "totalventa,"
					+ "tarjeta,"
					+ "usuario,"
					+ "isv15,"
					+ "isv18,"
					+ "efectivo_inicial,"
					+ "no_salida_inicial,"
					+ "no_entrada_inicial,"
					+ "no_cobro_inicial,"
					+ "no_pago_inicial)"
					+ " VALUES (now(),now(),now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 if(unCierre!=null){
				 try {
						con = ConexionStatic.getPoolConexion().getConnection();
						psConsultas=
								con.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
						
						psConsultas.setInt(1,unCierre.getNoFacturaInicio() );
						psConsultas.setInt(2,0 );
						psConsultas.setBigDecimal(3, unCierre.getEfectivo());
						psConsultas.setBigDecimal(4, unCierre.getCredito());
						psConsultas.setBigDecimal(5, unCierre.getTotal());
						psConsultas.setBigDecimal(6, unCierre.getTarjeta());
						psConsultas.setString(7, unCierre.getUsuario());
						psConsultas.setBigDecimal(8, unCierre.getIsv15());
						psConsultas.setBigDecimal(9, unCierre.getIsv18());
						psConsultas.setBigDecimal(10,unCierre.getEfectivoInicial());
						psConsultas.setInt(11, unCierre.getNoSalidaInicial());
						psConsultas.setInt(12, unCierre.getNoEntradaInicial());
						psConsultas.setInt(13, unCierre.getNoCobroInicial());
						psConsultas.setInt(14, unCierre.getNoPagoInicial());
						
						
						
						
						psConsultas.executeUpdate();//se guarda el encabezado de la factura
						
						
						rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
						while(rs.next()){
							this.idUltimoRequistro=rs.getInt(1);
							//this.setIdArticuloRegistrado(rs.getInt(1));
						}
						//se completa el cierre con el id
						unCierre.setId(idUltimoRequistro);
						
						//se registran los nuevos registros de numero de facturas del cierre
						CierreFacturacionDao cierreFacturasDao=new CierreFacturacionDao();
						
						for(int y=0;y<unCierre.getCierreFacturas().size();y++){
							//se estable el codigo de cierre recien creado
							unCierre.getCierreFacturas().get(y).setCodigoCierre(idUltimoRequistro);
							
							//se registra los datos de los numero de facturas
							cierreFacturasDao.registrar(unCierre.getCierreFacturas().get(y));
							
						}
						
						resultado=true;
						
				 }catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
						resultado=false;
					}
				finally
				{
					try{
						
						if(rs != null) rs.close();
						if(psConsultas != null)psConsultas.close();
						if(con != null) con.close();
		             
						
						} // fin de try
						catch ( SQLException excepcionSql )
						{
							excepcionSql.printStackTrace();
							//conexion.desconectar();
						} // fin de catch
				} // fin de finally
		
		 }else{
			 resultado=false;
		 }
		
		return resultado;
	}
	
	
	
	
	
public CierreCaja getCierreUltimoUser(){
		
		
        Connection con = null;
        
    	
    	String sql2=super.getQuerySelect()+" WHERE cierre_caja.usuario = ? ORDER BY cierre_caja.idCierre DESC LIMIT 1";
     
    	CierreCaja unaCierre=new CierreCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			//el segundo parametro de busqueda es para el ultimo cierre que este activo(1)
			//psConsultas.setInt(2, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaCierre.setId(res.getInt("idCierre"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setEfectivo(res.getBigDecimal("efectivo"));
				unaCierre.setCredito(res.getBigDecimal("creditos"));
				unaCierre.setTarjeta(res.getBigDecimal("tarjeta"));
				
				unaCierre.setIsv15(res.getBigDecimal("isv15"));
				unaCierre.setIsv18(res.getBigDecimal("isv18"));
				
				unaCierre.setTotal(res.getBigDecimal("totalventa"));
				unaCierre.setUsuario(res.getString("usuario"));
				unaCierre.setEstado(res.getBoolean("estado"));
				unaCierre.setEfectivoInicial(res.getBigDecimal("efectivo_inicial"));
				
				unaCierre.setNoSalidaFinal(res.getInt("no_salida_final"));
				unaCierre.setNoSalidaInicial(res.getInt("no_salida_inicial"));
				
				unaCierre.setNoEntradaFinal(res.getInt("no_entrada_final"));
				unaCierre.setNoEntradaInicial(res.getInt("no_entrada_inicial"));
				
				unaCierre.setNoCobroInicial(res.getInt("no_cobro_inicial"));
				unaCierre.setNoCobroFinal(res.getInt("no_cobro_final"));
				
				unaCierre.setNoPagoInicial(res.getInt("no_pago_inicial"));
				unaCierre.setNoPagoFinal(res.getInt("no_pago_final"));
				unaCierre.setTurno(res.getString("turno"));
				
				//CierreFacturacionDao cierreFactura=new CierreFacturacionDao();
				
				//unaCierre.setCierreFacturas(cierreFactura.buscarUltimoUser(ConexionStatic.getUsuarioLogin()));
				
				
			
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
		
		return unaCierre;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}

/**
 * @return the total en efectivo en un rago de facturas para un usuario
 * @param factInicial numero de factura inicial
 * @param factFinal numero de factura final
 */
	/*private BigDecimal getTotalEfectivo2(int factInicial, int factFinal){
		
		BigDecimal total=null;
		
		//total=total.add(t);
		String sql="select sum(cobro_efectivo) AS total_efectivo from encabezado_factura where tipo_factura = 1 and estado_factura = 'ACT' and numero_factura >= ? and numero_factura <= ? and usuario =? ";
		Connection con = null;
        
        
		//String tot="";
		ResultSet res=null;
		
	//	String user=conexion.getUsuarioLogin().getUser();
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas= con.prepareStatement(sql);
			psConsultas.setInt(1, factInicial);
			psConsultas.setInt(2, factFinal);
			psConsultas.setString(3, ConexionStatic.getUsuarioLogin().getUser());
			
			res = psConsultas.executeQuery();
			
			while(res.next()){
				
				existe=true;
				//total=total.add(res.getBigDecimal("total_efectivo"));
				total=new BigDecimal(res.getDouble("total_efectivo"));
				//String dato=res.getString("total_efectivo");
				
				//JOptionPane.showMessageDialog(null, dato);
				
				
			
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
				return total;
			}
			else return null;
	}*/
	
	
	/**
	 * @return the total en tarjeta en un rago de facturas para un usuario
	 * @param factInicial numero de factura inicial
	 * @param factFinal numero de factura final
	 *//*
		private BigDecimal getTotalTarjeta2(int factInicial, int factFinal){
			
			BigDecimal total=null;
			
			
			String sql="select "
	    			+ "			sum("
	    			+ "				`encabezado_factura`.`cobro_tarjeta` "
	    			+ "			) AS `total_tarjeta` "
	    			+ "		from "
	    			+ "			`encabezado_factura` "
	    			+ "		where "
	    			+ "					`encabezado_factura`.`tipo_factura` = 1 "
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
				
				res = psConsultas.executeQuery();
				while(res.next()){
					
					existe=true;
					total=new BigDecimal(res.getDouble("total_tarjeta"));
					
					
					
				
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
					return total;
				}
				else return null;
		}*/

	public CierreCaja getCierre(){
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        //SE CONSIGUE EL ITEM PARA EL CIERRE DE CAJA
		 CierreCaja ultimoCierreUser=this.getCierreUltimoUser();
        
  
    	
    	String sql2=""
    			+ " select "
    			+ " date_format(now(),'%d/%m/%Y %h:%i:%s') AS `fecha`, "
    			+ "	("
    			+ "		select "
    			+ "			`encabezado_factura`.`numero_factura` "
    			+ "		from "
    			+ "			`encabezado_factura` "
    			+ "		WHERE "
    			+ "			encabezado_factura.usuario ='"
    			+ 		ConexionStatic.getUsuarioLogin().getUser()+"'"
    			+ "		order by "
    			+ "			`encabezado_factura`.`numero_factura` desc "
    			+ "		limit 1"
    			+ "	) AS `factura_ultima`,"
    			+ "	("
    			+ "		select "
    			+ "			sum("
    			+ "				`encabezado_factura`.`total` "
    			+ "			) AS `total_efectivo` "
    			+ "		from "
    			+ "			`encabezado_factura` "
    			+ "		where "
    			+ "			("
    			+ "				("
    			+ "					`encabezado_factura`.`tipo_factura` = 1"
    			+ "				) "
    			+ "				and "
    			+ "				("
    			+ "					`encabezado_factura`.`estado_factura` = 'ACT'"
    			+ "				) "
    			+ "				and "
    			+ "				("
    			+ "					`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
    			+ "				) "
    			+ "				and "
    			+ "				("
    			+ "					`encabezado_factura`.`numero_factura` <= `factura_ultima`"
    			+ "				) "
    			+ "				and "
    			+ "				("
    			+ "					`encabezado_factura`.`tipo_pago` = 1"
    			+ "				)"
    			+ "				and "
    			+ "				( "
    			+ "					encabezado_factura.usuario ='"
    			+ 					ConexionStatic.getUsuarioLogin().getUser()+"'"
    			+ "				)"
    			+ "			)"
    			+ "	) AS `total_efectivo`,"
    			+ "	("
    			+ "		select "
    			+ "			sum("
    			+ "				`encabezado_factura`.`total` "
    			+ "			) AS `total_efectivo` "
    			+ "		from "
    			+ "			`encabezado_factura` "
    			+ "		where "
    			+ "			("
    			+ "				("
    			+ "					`encabezado_factura`.`tipo_factura` = 1"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`estado_factura` = 'ACT'"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` <= `factura_ultima`"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`tipo_pago` = 2"
    			+ "				) "
    			+ "				and("
    			+ "					encabezado_factura.usuario ='"
    			+ 				ConexionStatic.getUsuarioLogin().getUser()+"'"
    			+ "				)"
    			+ "			)"
    			+ "	) AS `total_tarjeta`,"
    			+ "	("
    			+ "		select "
    			+ "			sum("
    			+ "				`encabezado_factura`.`total` "
    			+ "			) AS `total_efectivo` "
    			+ "		from "
    			+ "			`encabezado_factura` "
    			+ "		where "
    			+ "			("
    			+ "				("
    			+ "					`encabezado_factura`.`tipo_factura` = 2"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`estado_factura` = 'ACT'"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` <= `factura_ultima`"
    			+ "				) "
    			+ "				and( "
    			+ "					encabezado_factura.usuario = '"
    			+ 						ConexionStatic.getUsuarioLogin().getUser()+"'"
    			+ "				)"
    			+ "			)"
    			+ ") AS `total_credito`,"
    			+ "("
    			+ "		SELECT "
    			+ "			sum( "
				+ "				`encabezado_factura`.`impuesto` "
				+"			) AS `total_isv15` "
				+"		FROM "
				+"			`encabezado_factura` "
				+"		WHERE "
				+"			( "
				+"				( "
				+"					`encabezado_factura`.`tipo_factura` = 1 "
				+"				) "
				+"				AND ( "
				+"					`encabezado_factura`.`estado_factura` = 'ACT' "
				+"				) "
				+"				AND ( "
				+"					`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
				+"				) "
				+"				AND ( "
				+"					`encabezado_factura`.`numero_factura` <= `factura_ultima` "
				+				")"
				+"				AND ( "
				+"					encabezado_factura.usuario = '"
				+ 					ConexionStatic.getUsuarioLogin().getUser()+"'"
				+"				) "
				+"			) "
				+") AS `total_isv15`, "
    			+ "("
    			+"		SELECT "
    			+"			sum( "
				+"				`encabezado_factura`.`isv18` "
				+"			) AS `total_isv18` "
				+"		FROM "
				+"			`encabezado_factura` "
				+"		WHERE  "
				+"		( "
				+"				( "
				+"					`encabezado_factura`.`tipo_factura` = 1 "
				+"				) "
				+"			AND ( "
				+"				`encabezado_factura`.`estado_factura` = 'ACT' "
				+"			) "
				+"			AND ( "
				+"				`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
				+"			) "
				+"			AND ( "
				+"				`encabezado_factura`.`numero_factura` <= `factura_ultima` "
				+"			) "
				+"			AND ( "
				+"					encabezado_factura.usuario = '"
				+ 				ConexionStatic.getUsuarioLogin().getUser()+"'"
				+"			) "
				+"		) "
				+") AS `total_isv18`, "
    			+ "	("
    			+ "		select "
    			+ "			sum("
    			+ "				`encabezado_factura`.`total` "
    			+ "			) AS `total_efectivo` "
    			+ "			from "
    			+ "				`encabezado_factura` "
    			+ "			where "
    			+ "			("
    			+ "				("
    			+ "					`encabezado_factura`.`estado_factura` = 'ACT'"
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` > "+ultimoCierreUser.getNoFacturaFinal()
    			+ "				) "
    			+ "				and ("
    			+ "					`encabezado_factura`.`numero_factura` <= `factura_ultima`"
    			+ "				) "
    			+ "				and( "
    			+ "					encabezado_factura.usuario = '"
    			+ 					ConexionStatic.getUsuarioLogin().getUser()+"'"
    			+ "				)"
    			+ "			)"
    			+ "	) AS `total` "
    			+ "	from "
    			+ "		`encabezado_factura` "
    			+ "	where ((`encabezado_factura`.`numero_factura` > (select `cierre_caja`.`factura_final` from `cierre_caja` order by `cierre_caja`.`idCierre` desc limit 1)) and (`encabezado_factura`.`numero_factura` <= (select `encabezado_factura`.`numero_factura` from `encabezado_factura` order by `encabezado_factura`.`numero_factura` desc limit 1))) limit 1; ";
        //Statement stmt = null;
    	CierreCaja unaCierre=new CierreCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				
				unaCierre.setNoFacturaInicio(ultimoCierreUser.getNoFacturaFinal());
				unaCierre.setNoFacturaFinal(res.getInt("factura_ultima"));
				unaCierre.setEfectivo(res.getBigDecimal("total_efectivo"));
				unaCierre.setCredito(res.getBigDecimal("total_credito"));
				unaCierre.setTarjeta(res.getBigDecimal("total_tarjeta"));
				
				unaCierre.setIsv15(res.getBigDecimal("total_isv15"));
				unaCierre.setIsv18(res.getBigDecimal("total_isv18"));
				
				unaCierre.setTotal(res.getBigDecimal("total"));
				unaCierre.setUsuario(ConexionStatic.getUsuarioLogin().getUser());//.setTotal(res.getBigDecimal("total"));
			
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
		
		return unaCierre;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
/*
private CierreCaja getCalcularCierre2(){
		
	
		//el cierre donde se volcaran todos los datos para regresarlo
		CierreCaja unaCierre=new CierreCaja();
		
		
        Connection con = null;
        
        
        //SE consigue el ultimo cierre de caja activo del usuario
		 CierreCaja ultimoCierreUser=this.getCierreUltimoUser();
		 
		 
		 
		 
		 
		 String sql="SELECT "
		 			+ "sum(subtotal15)AS subtotal15,"
		 			+ "SUM(subtotal18)AS subtotal18,"
		 			+ "SUM(cobro_tarjeta)AS cobro_tarjeta,"
		 			+ "SUM(cobro_efectivo)AS cobro_efectivo,"
		 			+ "SUM(subtotal_excento)AS subtotal_excento,"
		 			+ "SUM(total)AS total,"
		 			+ "sum(impuesto) as impuesto,"
		 			+ "sum(isv18) as isv18 "
		 		+ "from encabezado_factura "
		 			+ "where usuario=? "
		 			+ "and "
		 			+ "numero_factura >= ? "
		 			+ "and numero_factura <=? and estado_factura='ACT'";
        
		 
		 //se consigue de la base de datos la ultima factura realizada por el usuario
		 Factura ultimaFacturaUsuario=myFacturaDao.getUltimaFacturaUser(ConexionStatic.getUsuarioLogin().getUser(),new Caja());
		
		
		//se consigue de la base de datos el total de en creditos de un terminando grupos de facturas para un usuario
		 BigDecimal totalCredito=this.getTotalCredito(ultimoCierreUser.getNoFacturaInicio(), ultimaFacturaUsuario.getIdFactura());
		
    	
        
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas = con.prepareStatement(sql);
			
			// se consigue se establece el nombre del usuario
			psConsultas.setString(1,ultimoCierreUser.getUsuario() );
			
			//se establece el numero inicial de factura
			psConsultas.setInt(2, ultimoCierreUser.getNoFacturaInicio());
			
			//se establece el numero final de la factura
			psConsultas.setInt(3,ultimaFacturaUsuario.getIdFactura() );
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				
				unaCierre.setId(ultimoCierreUser.getId());
				
				unaCierre.setNoFacturaInicio(ultimoCierreUser.getNoFacturaInicio());
				unaCierre.setNoSalidaInicial(ultimoCierreUser.getNoSalidaInicial());/// este es el error
				unaCierre.setNoCobroInicial(ultimoCierreUser.getNoCobroInicial());
				unaCierre.setNoPagoInicial(ultimoCierreUser.getNoPagoInicial());
				
				unaCierre.setEfectivo(res.getBigDecimal("cobro_efectivo"));
				unaCierre.setCredito(totalCredito);
				unaCierre.setTarjeta(res.getBigDecimal("cobro_tarjeta"));
				
				unaCierre.setTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaCierre.setTotalIsv15(res.getBigDecimal("subtotal15"));
				unaCierre.setTotalIsv18(res.getBigDecimal("subtotal18"));
				unaCierre.setNoFacturaFinal(ultimaFacturaUsuario.getIdFactura());
				
				unaCierre.setIsv15(res.getBigDecimal("impuesto"));
				unaCierre.setIsv18(res.getBigDecimal("isv18"));
				unaCierre.setEfectivoInicial(ultimoCierreUser.getEfectivoInicial());
				//unaCierre.setNoCobroInicial(noCobro);
				
				//BigDecimal t_efectivo=ultimoCierreUser.getEfectivoInicial().add(res.getBigDecimal("total_efectivo"));//se suma el efectivo inicial y la venta con efectivo
				
				//unaCierre.setTotalEfectivo(t_efectivo);
				
				unaCierre.setTotal(res.getBigDecimal("total"));
				unaCierre.setUsuario(ConexionStatic.getUsuarioLogin().getUser());//.setTotal(res.getBigDecimal("total"));
			
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
		
		return unaCierre;
			
		
	}
*/
	@Override
	public List<CierreCaja> todos(int limInf,int limSupe) {
		List<CierreCaja> cierres =new ArrayList<CierreCaja>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				CierreCaja unaCierre=new CierreCaja();
				unaCierre.setFecha(res.getString("fecha"));
				unaCierre.setId(res.getInt("idCierre"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setEfectivo(res.getBigDecimal("efectivo"));
				unaCierre.setCredito(res.getBigDecimal("creditos"));
				unaCierre.setTarjeta(res.getBigDecimal("tarjeta"));
				
				unaCierre.setIsv15(res.getBigDecimal("isv15"));
				unaCierre.setIsv18(res.getBigDecimal("isv18"));
				
				unaCierre.setTotal(res.getBigDecimal("totalventa"));
				unaCierre.setUsuario(res.getString("usuario"));
				unaCierre.setTurno(res.getString("turno"));
				
				
				cierres.add(unaCierre);
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return cierres;
		}
		else return null;
	}

	public List<CierreCaja> buscarPorFecha(String date, String date2,int limitInferio, int canItemPag) {
		List<CierreCaja> cierres =new ArrayList<CierreCaja>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("fecha", "BETWEEN ? and "));
			psConsultas.setString(1, date);
			psConsultas.setString(2, date2);
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				CierreCaja unaCierre=new CierreCaja();
				unaCierre.setFecha(res.getString("fecha"));
				unaCierre.setId(res.getInt("idCierre"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setEfectivo(res.getBigDecimal("efectivo"));
				unaCierre.setCredito(res.getBigDecimal("creditos"));
				unaCierre.setTarjeta(res.getBigDecimal("tarjeta"));
				
				unaCierre.setIsv15(res.getBigDecimal("isv15"));
				unaCierre.setIsv18(res.getBigDecimal("isv18"));
				
				unaCierre.setTotal(res.getBigDecimal("totalventa"));
				unaCierre.setUsuario(res.getString("usuario"));
				unaCierre.setTurno(res.getString("turno"));
				
				
				cierres.add(unaCierre);
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return cierres;
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
	@Override
	public CierreCaja buscarPorId(int id) {
		// TODO Auto-generated method stub
		ResultSet res=null;
		CierreCaja unaCierre=new CierreCaja();
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+ " where idCierre=?");
			
			psConsultas.setInt(1, id);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				
				unaCierre.setFecha(res.getString("fecha"));
				unaCierre.setId(res.getInt("idCierre"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setEfectivo(res.getBigDecimal("efectivo"));
				unaCierre.setCredito(res.getBigDecimal("creditos"));
				unaCierre.setTarjeta(res.getBigDecimal("tarjeta"));
				
				unaCierre.setIsv15(res.getBigDecimal("isv15"));
				unaCierre.setIsv18(res.getBigDecimal("isv18"));
				
				unaCierre.setTotal(res.getBigDecimal("totalventa"));
				unaCierre.setUsuario(res.getString("usuario"));
				unaCierre.setTurno(res.getString("turno"));
				
			
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return unaCierre;
		}
		else return null;
	}
	public boolean addEfectivo(BigDecimal addEfectivo) {
		// TODO Auto-generated method stub
		
		//SE consigue el ultimo cierre de caja activo del usuario
		 CierreCaja ultimoCierreUser=this.getCierreUltimoUser();
		 
		 BigDecimal newEfectivo= addEfectivo.add(ultimoCierreUser.getEfectivoInicial());
		 
		//int resultado=0;
			Connection conn=null;
			
			try {
				
				//JOptionPane.showMessageDialog(null, "Datos Caja en modelo dao:"+caja.toString(),"Exito",JOptionPane.INFORMATION_MESSAGE);
				conn=ConexionStatic.getPoolConexion().getConnection();
				super.psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET efectivo_inicial=? WHERE idCierre=?");
				super.psConsultas.setDouble( 1,newEfectivo.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				super.psConsultas.setInt(2, ultimoCierreUser.getId());
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
	
	
	

}
