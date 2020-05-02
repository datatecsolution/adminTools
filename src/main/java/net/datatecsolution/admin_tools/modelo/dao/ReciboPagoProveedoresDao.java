package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReciboPagoProveedoresDao extends ModeloDaoBasic {
	
	
	
	private CuentaPorPagarDao myCuentaPagarDao=null;
	
	public int idUltimoRecibo=0;
	private String sqlBaseJoin;
	

	public ReciboPagoProveedoresDao() {
		
		super("recibo_pago_proveedores","no_recibo");
		
		
		myCuentaPagarDao=new CuentaPorPagarDao();
		
		sqlBaseJoin="SELECT recibo_pago_proveedores.no_recibo, "
							+ " DATE_FORMAT(recibo_pago_proveedores.fecha,'%d/%m/%Y') AS fecha , "
							+ " recibo_pago_proveedores.total_letras, "
							+ " recibo_pago_proveedores.total, "
							+ " recibo_pago_proveedores.saldo_anterio, "
							+ " recibo_pago_proveedores.saldo, "
							+ " recibo_pago_proveedores.concepto, "
							+ " recibo_pago_proveedores.usuario, "
							+ " recibo_pago_proveedores.codigo_proveedor, "
							+ " proveedor.nombre_proveedor, "
							+ " proveedor.telefono, "
							+ " proveedor.celular, "
							+ " proveedor.direccion, "
							+ " recibo_pago_proveedores . codigo_tipo_pago, "
							+ " bancos.nombre AS forma_pago , "
							+ " bancos.no_cuenta, "
							+ " tipo_cuenta_bancos.tipo_cuenta , "
							+ " tipo_cuenta_bancos.observaciones, "
							+ " bancos.id_tipo_cuenta "
				+ " FROM "
						+ super.DbName+". recibo_pago_proveedores "
							+ " JOIN "+super.DbName+".proveedor "
									+ " ON ( recibo_pago_proveedores . codigo_proveedor = proveedor . codigo_proveedor ) "
							+ " JOIN "+super.DbName+".bancos "
									+ " ON ( recibo_pago_proveedores . codigo_tipo_pago = bancos . id )"
							+ " JOIN "+ super.DbName+".tipo_cuenta_bancos "
									+ " ON ( bancos . id_tipo_cuenta = tipo_cuenta_bancos . id )";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	@Override
	public boolean registrar(Object c)
	{
		ReciboPagoProveedor myReciboPago=(ReciboPagoProveedor)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			//ClienteDao clienteDao= new ClienteDao(conexion);
			
			
			//se establece los saldo en 0
			myReciboPago.setSaldos0();
			
			//el salado anterio
			myReciboPago.setSaldoAnterior(myReciboPago.getProveedor().getSaldo());
			
			//el saldo actural
			myReciboPago.setSaldo(myReciboPago.getSaldoAnterior().subtract(myReciboPago.getTotal()));
			
	
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_proveedor,total_letras,total,concepto,usuario,saldo_anterio,saldo,codigo_tipo_pago) VALUES (now(),?,?,?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			
			psConsultas.setInt(1, myReciboPago.getProveedor().getId());
			psConsultas.setString(2, myReciboPago.getTotalLetras());
			psConsultas.setBigDecimal(3, myReciboPago.getTotal());
			psConsultas.setString(4, myReciboPago.getConcepto());
			psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setBigDecimal(6, myReciboPago.getSaldoAnterior().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setBigDecimal(7, myReciboPago.getSaldo().setScale(2, BigDecimal.ROUND_HALF_EVEN));
			psConsultas.setInt(8, myReciboPago.getFormaPago().getId());
						
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				//this.setIdClienteRegistrado(rs.getInt(1));
				myReciboPago.setNoRecibo(rs.getInt(1));
				this.idUltimoRecibo=rs.getInt(1);
			}
			
			//se establece en el concepto en numero de recibo con que se pago
			String concepto=myReciboPago.getConcepto();
			concepto=concepto+" con recibo no. "+myReciboPago.getNoRecibo();
			myReciboPago.setConcepto(concepto);
			
			//se registra el pago en la la tabla cuentas por pagar
			myCuentaPagarDao.reguistrarDebito(myReciboPago);//.reguistrarDebito(myRecibo);
			
	
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			//conexion.desconectar();
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
	public void calcularTotalPagosCierre(CierreCaja unaCierre) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		
		Connection con = null;
		ResultSet res=null;
		
		//se consiguie el ultima pago que realizo el usuario
		ReciboPagoProveedor ultimo=this.getPagoUltimoUser();
		//se establece la ultima pago del usuaurio
		unaCierre.setNoPagoFinal(ultimo.getNoRecibo());
		
		
		//JOptionPane.showMessageDialog(null,"aqui "+unaCierre.toString());
		
		
		String sql="SELECT	ifnull(sum(recibo_pago_proveedores.total), 0) AS cantidad FROM "+ super.DbName+".recibo_pago_proveedores WHERE recibo_pago_proveedores.no_recibo >= ? AND recibo_pago_proveedores.no_recibo <= ?  AND recibo_pago_proveedores.usuario =?";
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql);
			
			psConsultas.setInt(1, unaCierre.getNoPagoInicial());
			psConsultas.setInt(2, unaCierre.getNoPagoFinal());
			psConsultas.setString(3, unaCierre.getUsuario());
			
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				
				
				//si existe alguna salida se suma y se establecen en el cierre
				unaCierre.setTotalPago(res.getBigDecimal("cantidad"));
	
			
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
	
	private ReciboPagoProveedor getPagoUltimoUser() {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE usuario=? ORDER BY no_recibo DESC LIMIT 1";
    	//dfsafa
        //Statement stmt = null;
    	ReciboPagoProveedor unRecibo=new ReciboPagoProveedor();
		
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
				
				Proveedor unProveedor=new Proveedor();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unProveedor.setId(res.getInt("codigo_proveedor"));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				
				unRecibo.setProveedor(unProveedor);
				
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
	public List<ReciboPagoProveedor> todos(int limiteInferior, int limiteSuperior) {
		// TODO Auto-generated method stub

        Connection con = null;
       
       	List<ReciboPagoProveedor> pagos=new ArrayList<ReciboPagoProveedor>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limiteSuperior);
			psConsultas.setInt(2, limiteInferior);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				ReciboPagoProveedor un=new ReciboPagoProveedor();
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				
				Proveedor unProveedor=new Proveedor();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unProveedor.setId(res.getInt("codigo_proveedor"));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				
				un.setProveedor(unProveedor);
				
				Banco unCuenta=new Banco();
				unCuenta.setId(res.getInt("codigo_tipo_pago"));
				unCuenta.setNombre(res.getString("forma_pago"));
				unCuenta.setNoCuenta(res.getString("no_cuenta"));
				unCuenta.setTipoCuenta(res.getString("tipo_cuenta"));
				un.setFormaPago(unCuenta);
				
				
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
	@Override
	public ReciboPagoProveedor buscarPorId(int codigo) {
		// TODO Auto-generated method stub
		 Connection con = null;
	        
	    	ReciboPagoProveedor un=new ReciboPagoProveedor();
			
			ResultSet res=null;
			
			boolean existe=false;
			try {
				con = ConexionStatic.getPoolConexion().getConnection();
				
				psConsultas = con.prepareStatement(super.getQuerySearch("no_recibo", "="));
				psConsultas.setInt(1, codigo);
				psConsultas.setInt(2, 0);
				psConsultas.setInt(3, 1);
				
				
				res = psConsultas.executeQuery();
				while(res.next()){
					existe=true;
					un.setFecha(res.getString("fecha"));
					un.setConcepto(res.getString("concepto"));
					un.setNoRecibo(res.getInt("no_recibo"));
					un.setTotal(res.getBigDecimal("total"));
					
					Proveedor unProveedor=new Proveedor();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
					
					unProveedor.setId(res.getInt("codigo_proveedor"));
					unProveedor.setNombre(res.getString("nombre_proveedor"));
					
					un.setProveedor(unProveedor);
					
					Banco unCuenta=new Banco();
					unCuenta.setId(res.getInt("codigo_tipo_pago"));
					unCuenta.setNombre(res.getString("forma_pago"));
					unCuenta.setNoCuenta(res.getString("no_cuenta"));
					unCuenta.setTipoCuenta(res.getString("tipo_cuenta"));
					un.setFormaPago(unCuenta);
					
					

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

	public List<ReciboPagoProveedor> reciboProveedorPorFecha(String date1, String date2,int limitInferio, int canItemPag) {
		// TODO Auto-generated method stub
		Connection con = null;
        
       	List<ReciboPagoProveedor> pagos=new ArrayList<ReciboPagoProveedor>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("fecha", "BETWEEN ? and "));
			psConsultas.setString(1, date1);
			psConsultas.setString(2, date2);
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				ReciboPagoProveedor un=new ReciboPagoProveedor();
				existe=true;
				un.setFecha(res.getString("fecha"));
				un.setConcepto(res.getString("concepto"));
				un.setNoRecibo(res.getInt("no_recibo"));
				un.setTotal(res.getBigDecimal("total"));
				
				Proveedor unProveedor=new Proveedor();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				
				unProveedor.setId(res.getInt("codigo_proveedor"));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				
				un.setProveedor(unProveedor);
				
				Banco unCuenta=new Banco();
				unCuenta.setId(res.getInt("codigo_tipo_pago"));
				unCuenta.setNombre(res.getString("forma_pago"));
				unCuenta.setNoCuenta(res.getString("no_cuenta"));
				unCuenta.setTipoCuenta(res.getString("tipo_cuenta"));
				un.setFormaPago(unCuenta);
				
				
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
