package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReciboPagoDao extends ModeloDaoBasic {
	
	
	private CuentaPorCobrarDao myCuentaCobrarDao=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;
	private ClienteDao myClienteDao=null;
	public int idUltimoRecibo=0;
	private String sqlBaseJoin;
	

	public ReciboPagoDao() {
		
		super("recibo_pago","no_recibo");
		
		myClienteDao=new ClienteDao();
		myCuentaCobrarDao=new CuentaPorCobrarDao();
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();
		
		sqlBaseJoin="SELECT recibo_pago.no_recibo, "
							+ " DATE_FORMAT(recibo_pago.fecha,'%d/%m/%Y')  AS fecha , "
							+ " recibo_pago.codigo_cliente, "
							+ " recibo_pago.total_letras, "
							+ " recibo_pago.total, "
							+ " recibo_pago.ref, "
							+ " recibo_pago.saldo_anterio, "
							+ " recibo_pago.saldo, "
							+ " recibo_pago.concepto, "
							+ " recibo_pago.usuario, "
							+ " cliente.nombre_cliente "
					+ " FROM "
							+ super.DbName+ ". recibo_pago  "
							+ "INNER JOIN "+super.DbName+ ". cuentas_facturas  "
								+ "ON( recibo_pago.codigo_cliente  =  cuentas_facturas.codigo_cuenta ) "
							+ "INNER JOIN "+super.DbName+ ". cliente  "
										+ "ON( cuentas_facturas.codigo_cliente = cliente.codigo_cliente )";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		ReciboPago myRecibo=(ReciboPago)c;
		//JOptionPane.showConfirmDialog(null, myCliente);
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;

		String fecha= myRecibo.getFecha()==null? " now(), ":"'"+myRecibo.getFecha()+" 00:00:00', ";
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			myRecibo.setCliente(this.myClienteDao.buscarPorId(myRecibo.getCliente().getId()));
			
			//se establece los saldo en 0
			myRecibo.setSaldos0();
			
			//el salado anterio
			myRecibo.setSaldoAnterior(myClienteDao.getSaldoCliente(myRecibo.getCliente().getId()));
			
			//el saldo actural
			myRecibo.setSaldo(myRecibo.getSaldoAnterior().subtract(myRecibo.getTotal()));
			
	
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cliente,total_letras,total,concepto,usuario,saldo_anterio,saldo,ref) VALUES ("+fecha+"?,?,?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setInt(1, myRecibo.getCliente().getId());
			psConsultas.setString(2, myRecibo.getTotalLetras());
			psConsultas.setBigDecimal(3, myRecibo.getTotal());
			psConsultas.setString(4, myRecibo.getConcepto());
			psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setBigDecimal(6, myRecibo.getSaldoAnterior().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setBigDecimal(7, myRecibo.getSaldo().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setString(8,myRecibo.getRef());
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				//this.setIdClienteRegistrado(rs.getInt(1));
				myRecibo.setNoRecibo(rs.getInt(1));
				this.idUltimoRecibo=rs.getInt(1);
			}
			
			//se establece en el concepto en numero de recibo con que se pago
			String concepto=myRecibo.getConcepto();
			concepto=concepto+" con recibo no. "+myRecibo.getNoRecibo();
			myRecibo.setConcepto(concepto);
			
			this.myCuentaCobrarDao.reguistrarDebito(myRecibo);
			
			
			//for(int x=0;x<myRecibo.getFacturas().size();x++){
		//		this.registrarFacturasPagadas(myRecibo.getFacturas().get(x).getIdFactura(), myRecibo.getNoRecibo());
			//}
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
            return false;
		}
		finally
		{
			try{
				if(rs!=null)rs.close();
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



	public boolean registrar(Object c, CuentaFactura cuenta)
	{
		ReciboPago myRecibo=(ReciboPago)c;


		int resultado=0;
		ResultSet rs=null;
		Connection con = null;

		String fecha= myRecibo.getFecha()==null? " now(), ":"'"+myRecibo.getFecha()+" 00:00:00', ";

		try
		{
			con = ConexionStatic.getPoolConexion().getConnection();


			//se establece los saldo en 0
			myRecibo.setSaldos0();


			//el salado anterio
			myRecibo.setSaldoAnterior(cuentaXCobrarFacturaDao.getSaldoFactura(cuenta).getSaldo());

			//el saldo actural
			myRecibo.setSaldo(myRecibo.getSaldoAnterior().subtract(myRecibo.getTotal()));


			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cliente,total_letras,total,concepto,usuario,saldo_anterio,saldo,ref) VALUES ("+fecha+"?,?,?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);

			psConsultas.setInt(1, cuenta.getCodigoCuenta());
			psConsultas.setString(2, myRecibo.getTotalLetras());
			psConsultas.setBigDecimal(3, myRecibo.getTotal());
			psConsultas.setString(4, myRecibo.getConcepto());
			psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setBigDecimal(6, myRecibo.getSaldoAnterior().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setBigDecimal(7, myRecibo.getSaldo().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setString(8,myRecibo.getRef());

			resultado=psConsultas.executeUpdate();

			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				//this.setIdClienteRegistrado(rs.getInt(1));
				myRecibo.setNoRecibo(rs.getInt(1));
				this.idUltimoRecibo=rs.getInt(1);
			}

			/*** se crea el registro el debito de la cuenta del cliente con el recibo generado ***/
			String concepto=myRecibo.getConcepto();
			concepto=concepto+" con recibo no. "+myRecibo.getNoRecibo()+",  ref "+myRecibo.getRef();
			myRecibo.setConcepto(concepto);
			myCuentaCobrarDao.reguistrarDebito(myRecibo);


			/*** se crea el registro el debito de la cuentaXcobrarFactura con el recibo generado ***/
			CuentaXCobrarFactura cuentaAregisrar=new CuentaXCobrarFactura();
			if (myRecibo.getFecha() == null) {
				cuentaAregisrar.setFecha1(null);
			} else {
				cuentaAregisrar.setFecha1(myRecibo.getFecha());
			}
			cuentaAregisrar.setCodigoCuenta(cuenta.getCodigoCuenta());
			cuentaAregisrar.setDebito(myRecibo.getTotal());
			cuentaAregisrar.setDescripcion("Pago con recibo # "+myRecibo.getNoRecibo()+",  ref "+myRecibo.getRef());
			cuentaXCobrarFacturaDao.reguistrarDebito(cuentaAregisrar);


			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		finally
		{
			try{
				if(rs!=null)rs.close();
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


	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean registrarFacturasPagadas(int idFactura, int idRecibo)
	{
		//JOptionPane.showConfirmDialog(null, myCliente);
		int resultado=0;
		//ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( "INSERT INTO detalle_pago(no_recibo_pago,no_factura_pagada) VALUES (?,?)");
			
			psConsultas.setInt(1,idRecibo );
			psConsultas.setInt(2,idFactura);
			
						
			resultado=psConsultas.executeUpdate();
			
					
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
            return false;
		}
		finally
		{
			try{
				//if(rs!=null)rs.close();
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
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public List<ReciboPago> todos(int limInf,int limSupe){
		
        Connection con = null;
      
       	List<ReciboPago> pagos=new ArrayList<ReciboPago>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				ReciboPago un=new ReciboPago();
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				un.setRef(res.getString("ref"));
				
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				
				un.setCliente(unCliente);
				
				
				pagos.add(un);
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
				return pagos;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public ReciboPago buscarPorId(int noRecibo){
	
		
        Connection con = null;
        
   
      
       	ReciboPago un=new ReciboPago();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("no_recibo", "="));
			
			psConsultas.setInt(1, noRecibo);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				un.setRef(res.getString("ref"));
				
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				un.setCliente(unCliente);
				
				
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
				return un;
			}
			else return null;
		
	}


	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar los recibo por fecha>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<ReciboPago>  buscarPorRef(String busqueda,int limitInferio, int canItemPag){



		Connection con = null;


		List<ReciboPago> pagos=new ArrayList<ReciboPago>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas=con.prepareStatement(super.getQuerySearch("ref", "like"));
			psConsultas.setString(1,  busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				ReciboPago un=new ReciboPago();
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				un.setRef(res.getString("ref"));

				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));

				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				un.setCliente(unCliente);


				pagos.add(un);
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
			return pagos;
		}
		else return null;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar los recibo por fecha>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<ReciboPago>  reciboPorFecha(String fecha1, String fecha2,int limitInferio, int canItemPag){
		
		
		
        Connection con = null;
        
        
       	List<ReciboPago> pagos=new ArrayList<ReciboPago>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			//clave contador=4475226
			psConsultas = con.prepareStatement(super.getQuerySearch("fecha", "BETWEEN ? and "));
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				ReciboPago un=new ReciboPago();
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				un.setRef(res.getString("ref"));
				
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				un.setCliente(unCliente);
				
				
				pagos.add(un);
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
				return pagos;
			}
			else return null;
		}
	
	public void calcularTotalCierre(CierreCaja unaCierre) {
		
		Connection con = null;
		ResultSet res=null;
		
		//se consiguie el ultima salida que realizo el usuario
		ReciboPago ultimo=this.getCobroUltimoUser();
		//se establece la ultima salida del usuaurio
		unaCierre.setNoCobroFinal(ultimo.getNoRecibo());
		
	
		
		
		String sql="SELECT	ifnull(sum(recibo_pago.total), 0) AS cantidad FROM "+ super.DbName+".recibo_pago WHERE recibo_pago.no_recibo >= ? AND recibo_pago.no_recibo <= ?  AND recibo_pago.usuario =?";
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql);
			
			psConsultas.setInt(1, unaCierre.getNoCobroInicial());
			psConsultas.setInt(2, unaCierre.getNoCobroFinal());
			psConsultas.setString(3, unaCierre.getUsuario());
			
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				
				
				//si existe alguna salida se suma y se establecen en el cierre
				unaCierre.setTotalCobro(res.getBigDecimal("cantidad"));
	
			
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
	
		
	
		
	}

	private ReciboPago getCobroUltimoUser() {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE usuario=? ORDER BY no_recibo DESC LIMIT 1";
    	//dfsafa
        //Statement stmt = null;
    	ReciboPago unRecibo=new ReciboPago();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				
				unRecibo.setFecha(res.getString("fecha"));
				unRecibo.setConcepto(res.getString("concepto"));
				unRecibo.setNoRecibo(res.getInt("no_recibo"));
				unRecibo.setTotal(res.getBigDecimal("total"));
				unRecibo.setSaldoAnterior(res.getBigDecimal("saldo_anterio"));
				unRecibo.setSaldo(res.getBigDecimal("saldo"));
				
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				
				unRecibo.setCliente(unCliente);
				
				//unRecibo.setCliente(unCliente);
			
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
		
		return unRecibo;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
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

}
