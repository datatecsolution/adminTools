package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.CuentaFactura;
import net.datatecsolution.admin_tools.modelo.CuentaXCobrarFactura;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaXCobrarFacturaDao extends ModeloDaoBasic {
	
	private String sqlBaseJoin=null;

	public CuentaXCobrarFacturaDao() {
		super("cuentas_por_cobrar_facturas","codigo_reguistro");
		
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaXCobrarFactura getUltimoPago(CuentaFactura cuentaFactura){
		
		CuentaXCobrarFactura un=new CuentaXCobrarFactura();
		
		Connection con = null;
      
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE cuentas_por_cobrar_facturas.codigo_cuenta = ? and tipo_movimiento=2  ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1";
    	ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setInt(1, cuentaFactura.getCodigoCuenta());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				//un.setFactura(myFactura);
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getDate("fecha"));
				un.setCredito(res.getBigDecimal("credito"));
				un.setDebito(res.getBigDecimal("debito"));
				un.setSaldo(res.getBigDecimal("saldo"));
			
			 }
					
			} catch (SQLException e) {
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
		
		if(existe)
			return un;
		else
			return null;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaXCobrarFactura getSaldoFactura(CuentaFactura cuentaFactura){
		CuentaXCobrarFactura un=new CuentaXCobrarFactura();
		
		Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE cuentas_por_cobrar_facturas.codigo_cuenta = ? ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1";
    	ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setInt(1, cuentaFactura.getCodigoCuenta());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				//un.setFactura(myFactura);
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getDate("fecha"));
				un.setCredito(res.getBigDecimal("credito"));
				un.setDebito(res.getBigDecimal("debito"));
				un.setSaldo(res.getBigDecimal("saldo"));
			
			 }
					
			} catch (SQLException e) {
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
		return un;
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaXCobrarFactura getSaldoFactura(CuentaXCobrarFactura cuentaFactura){
		CuentaXCobrarFactura un=new CuentaXCobrarFactura();

		Connection con = null;

		String sql2=super.getQuerySelect()+" WHERE cuentas_por_cobrar_facturas.codigo_cuenta = ? ORDER BY cuentas_por_cobrar_facturas.codigo_reguistro DESC LIMIT 1";
		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas = con.prepareStatement(sql2);

			psConsultas.setInt(1, cuentaFactura.getCodigoCuenta());
			res = psConsultas.executeQuery();
			while(res.next()){

				existe=true;
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getDate("fecha"));
				un.setCredito(res.getBigDecimal("credito"));
				un.setDebito(res.getBigDecimal("debito"));
				un.setSaldo(res.getBigDecimal("saldo"));

			}

		} catch (SQLException e) {
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
		return un;
	}
	
	public List<CuentaXCobrarFactura> getFacturasClienteSaldo(Cliente cliente) {
		// TODO Auto-generated method stub
		List<CuentaXCobrarFactura> cuentas=new ArrayList<CuentaXCobrarFactura>();
		
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
				CuentaXCobrarFactura un=new CuentaXCobrarFactura();
				
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				un.setCodigoCuenta(res.getInt("codigo_cuenta"));
				
				
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getDate("fecha"));
				un.setCredito(res.getBigDecimal("credito"));
				un.setDebito(res.getBigDecimal("debito"));
				un.setSaldo(res.getBigDecimal("saldo"));
				cuentas.add(un);
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
				return cuentas;
			}
			else return null;
	}
	
	
	
	public boolean reguistrarDebito(CuentaXCobrarFactura aRegistrar) {
		// TODO Auto-generated method stub

		CuentaXCobrarFactura ultima=this.getSaldoFactura(aRegistrar);
		BigDecimal newSaldo=ultima.getSaldo().subtract(aRegistrar.getDebito());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);

				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				String fecha= aRegistrar.getFecha1()==null? " now(), ":"'"+aRegistrar.getFecha1()+" 00:00:00', ";
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cuenta,descripcion,debito,saldo,usuario,tipo_movimiento) VALUES ("+fecha+"?,?,?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getCodigoCuenta());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(3, aRegistrar.getDebito());
					psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
					psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
					psConsultas.setInt(6, 2);//tipo de movimiento 2= pago,1=saldo inicial(defaul), 3 interes
					resultado=psConsultas.executeUpdate();
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
		
		//return false;
	}
	public boolean reguistrarDebitoYaProcesado(CuentaXCobrarFactura aRegistrar) {
		// TODO Auto-generated method stub
		//CuentaXCobrarFactura aRegistrar=new CuentaXCobrarFactura();
		//CuentaXCobrarFactura ultima=null;//this.getSaldoFactura(myRecibo);
		//hay que modificar aqui para poder hacer el registro

		//aRegistrar.setFactura(myRecibo);

		//aRegistrar.setDebito(myRecibo.getTotal());//.setCredito(myFactura.getTotal());
		//BigDecimal newSaldo=ultima.getSaldo().subtract(myRecibo.getTotal());//.add(myFactura.getTotal());
		//aRegistrar.setSaldo(newSaldo);

		//JOptionPane.showConfirmDialog(null, myCliente);
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;

		try
		{
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cuenta,descripcion,debito,saldo,usuario,tipo_movimiento) VALUES (now(),?,?,?,?,?,?)");
			psConsultas.setInt(1, aRegistrar.getCodigoCuenta());
			psConsultas.setString(2, aRegistrar.getDescripcion());
			psConsultas.setBigDecimal(3, aRegistrar.getDebito());
			psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
			psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(6, 2);//tipo de movimiento 2= pago,1=saldo inicial(defaul), 3 interes
			resultado=psConsultas.executeUpdate();
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

		//return false;
	}
	
	public boolean reguistrarCredito(CuentaFactura cuentaFactura) {
		// TODO Auto-generated method stub
		CuentaXCobrarFactura aRegistrar=new CuentaXCobrarFactura();
		CuentaXCobrarFactura ultima=this.getSaldoFactura(cuentaFactura);

		//String fecha= cuentaFactura.getFactura().getFecha()==null? " now(), ":"'"+cuentaFactura.getFactura().getFecha()+" 00:00:00', ";

		/** se construlle la descripcion de la cuenta segun la factura ***/
		String descripcion="venta de ";
		for(int x=0;x<cuentaFactura.getFactura().getDetalles().size();x++){

			if(cuentaFactura.getFactura().getDetalles().get(x).getArticulo().getId()!=-1)
				descripcion=descripcion+cuentaFactura.getFactura().getDetalles().get(x).getArticulo().getArticulo()+" , ";
		}
		descripcion=descripcion+" fact # "+ cuentaFactura.getFactura().getIdFactura();
		aRegistrar.setDescripcion(descripcion);


		aRegistrar.setCredito(cuentaFactura.getFactura().getTotal());
		BigDecimal newSaldo=ultima.getSaldo().add(cuentaFactura.getFactura().getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,descripcion,credito,saldo,usuario,codigo_cuenta) VALUES (now(),?,?,?,?,?)");
					psConsultas.setString(1, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(2, aRegistrar.getCredito());
					psConsultas.setBigDecimal(3, aRegistrar.getSaldo());
					psConsultas.setString(4, ConexionStatic.getUsuarioLogin().getUser());
					psConsultas.setInt(5, cuentaFactura.getCodigoCuenta());
					resultado=psConsultas.executeUpdate();
					
					return true;
					
				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage());
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
		
		//return false;
	}

	public boolean reguistrarCreditoSinFactura(CuentaFactura cuentaFactura) {
		// TODO Auto-generated method stub
		CuentaXCobrarFactura aRegistrar=new CuentaXCobrarFactura();
		//se busca el ultimo reg de la cuenta
		CuentaXCobrarFactura ultima=this.getSaldoFactura(cuentaFactura);

		//se define el nuevo reg
		aRegistrar.setDescripcion(cuentaFactura.getDetalleCredito());
		aRegistrar.setCredito(cuentaFactura.getSaldo());
		BigDecimal newSaldo=ultima.getSaldo().add(cuentaFactura.getSaldo());
		aRegistrar.setSaldo(newSaldo);

		int resultado=0;
		ResultSet rs=null;
		Connection con = null;

		try
		{
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,descripcion,credito,saldo,usuario,codigo_cuenta) VALUES (now(), ?,?,?,?,?)");
			psConsultas.setString(1, aRegistrar.getDescripcion());
			psConsultas.setBigDecimal(2, aRegistrar.getCredito());
			psConsultas.setBigDecimal(3, aRegistrar.getSaldo());
			psConsultas.setString(4, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(5, cuentaFactura.getCodigoCuenta());
			resultado=psConsultas.executeUpdate();

			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
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

		//return false;
	}
	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
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

}
