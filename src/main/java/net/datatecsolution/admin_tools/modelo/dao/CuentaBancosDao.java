package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaBancosDao extends ModeloDaoBasic {
	
	
	private Banco myBanco=new Banco();
	
	private String sqlBaseJoin=null;
	
	public CuentaBancosDao() {
		super("cuentas_bancos","codigo");
		
		
		sqlBaseJoin="SELECT cuentas_bancos.codigo, "
				+ " date_format("
				+ "`cuentas_bancos`.`fecha`, '%d/%m/%Y %h:%i:%s %p'"
				+ ") AS `fecha`, "
				+ " cuentas_bancos.descripcion, "
				+ " cuentas_bancos.referencia, "
				+ " cuentas_bancos.codigo_banco, "
				+ " cuentas_bancos.debito, "
				+ " cuentas_bancos.credito, "
				+ " cuentas_bancos.saldo, "
				+ " bancos.nombre,  "
				+ " bancos.id_tipo_cuenta,  "
				+ " bancos.no_cuenta  "
	+ " FROM "
			+ super.DbName+".cuentas_bancos "
				+ "INNER JOIN "+super.DbName+".bancos "
						+ "ON (cuentas_bancos.codigo_banco = bancos.id)";
super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaBanco getSaldoBanco(Banco myBanco){
		CuentaBanco un=new CuentaBanco();
		
		Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE cuentas_bancos.codigo_banco = ? ORDER BY cuentas_bancos.codigo DESC LIMIT 1";
    	ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setInt(1, myBanco.getId());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setNoReguistro(res.getInt("codigo"));
				un.setBanco(myBanco);
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getString("fecha"));
				un.setReferencia(res.getString("referencia"));
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
	
	
	public boolean reguistrarDebitoMovimiento(MovimientoBanco myMovimiento) {
		// TODO Auto-generated method stub
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(myMovimiento.getBanco());
		
		
		aRegistrar.setBanco(myMovimiento.getBanco());
		aRegistrar.setDescripcion(myMovimiento.getDescripcion());
		aRegistrar.setDebito(myMovimiento.getCantidad());//.setCredito(myFactura.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().add(myMovimiento.getCantidad());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,debito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getBanco().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, myMovimiento.getCodigo()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getDebito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
	
	
	
	
	public boolean reguistrarDebitoSalidaCaja(SalidaCaja mySalida) {
		// TODO Auto-generated method stub
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(mySalida.getBanco());
		
		
		aRegistrar.setBanco(mySalida.getBanco());
		aRegistrar.setDescripcion(mySalida.getConcepto());
		aRegistrar.setDebito(mySalida.getCantidad());//.setCredito(myFactura.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().add(mySalida.getCantidad());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,debito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getBanco().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, mySalida.getCodigoSalida()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getDebito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
	
	//solo para la cuenta de efectivo o caja
	public boolean reguistrarDebitoEntradaCajaEfectivo(EntradaCaja myEntrada) {
		// TODO Auto-generated method stub
		//se establece el efectivo como la cuenta
		Banco cuentaEfectivo=new Banco();
		cuentaEfectivo.setId(1);
		
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(cuentaEfectivo);
		
		
		aRegistrar.setBanco(myEntrada.getBanco());
		aRegistrar.setDescripcion(myEntrada.getConcepto());
		aRegistrar.setDebito(myEntrada.getCantidad());//.setCredito(myFactura.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().add(myEntrada.getCantidad());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,debito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, cuentaEfectivo.getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, myEntrada.getCodigoEntrada()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getDebito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
	
	public boolean reguistrarCreditoEntradaCaja(EntradaCaja myEntrada) {
		// TODO Auto-generated method stub
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(myEntrada.getBanco());
		
		
		aRegistrar.setBanco(myEntrada.getBanco());
		aRegistrar.setDescripcion(myEntrada.getConcepto());
		aRegistrar.setCredito(myEntrada.getCantidad());
		BigDecimal newSaldo=ultima.getSaldo().subtract(myEntrada.getCantidad());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,credito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getBanco().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, myEntrada.getCodigoEntrada()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getCredito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
		
	}
	
	public boolean reguistrarCreditoMovimiento(MovimientoBanco myMovimiento) {
		// TODO Auto-generated method stub
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(myMovimiento.getBanco());
		
		
		aRegistrar.setBanco(myMovimiento.getBanco());
		aRegistrar.setDescripcion(myMovimiento.getDescripcion());
		aRegistrar.setCredito(myMovimiento.getCantidad());
		BigDecimal newSaldo=ultima.getSaldo().subtract(myMovimiento.getCantidad());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,credito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getBanco().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, myMovimiento.getCodigo()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getCredito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
		
	}
	
	//solo para la cuenta de efectivo o caja
	public boolean reguistrarCreditoSalidaCajaEfectivo(SalidaCaja mySalida) {
		// TODO Auto-generated method stub
		
		
		Banco cuentaEfectivo=new Banco();
		cuentaEfectivo.setId(1);
		
		CuentaBanco aRegistrar=new CuentaBanco();
		CuentaBanco ultima=this.getSaldoBanco(cuentaEfectivo);
		
		
		aRegistrar.setBanco(mySalida.getBanco());
		aRegistrar.setDescripcion(mySalida.getConcepto());
		aRegistrar.setCredito(mySalida.getCantidad());
		BigDecimal newSaldo=ultima.getSaldo().subtract(mySalida.getCantidad());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_banco,descripcion,referencia,credito,saldo) VALUES (now(),?,?,?,?,?)");
					psConsultas.setInt(1, cuentaEfectivo.getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setString(3, mySalida.getCodigoSalida()+"");
					psConsultas.setBigDecimal(4, aRegistrar.getCredito());
					psConsultas.setBigDecimal(5, aRegistrar.getSaldo());
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
	public List<CuentaBanco> todos(int canItemPag, int limSupe) {
		// TODO Auto-generated method stub
		
				List<CuentaBanco> cuentas=new ArrayList<CuentaBanco>();
				ResultSet res=null;
				
				Connection conn=null;
				
				boolean existe=false;
				
				try {
					
					conn=ConexionStatic.getPoolConexion().getConnection();
					psConsultas=conn.prepareStatement( super.getQuerySearchJoin("codigo_banco", "=", "bancos", "id", "codigo_banco"));
					
					psConsultas.setInt(1, this.myBanco.getId());
					
					psConsultas.setInt(2, limSupe);
					psConsultas.setInt(3, canItemPag);
					
					//System.out.println(psConsultas);
					res = psConsultas.executeQuery();
					while(res.next()){
						
						CuentaBanco un=new CuentaBanco();
						existe=true;
						un.setNoReguistro(res.getInt("codigo"));
						un.setDescripcion(res.getString("descripcion"));
						un.setFecha(res.getString("fecha"));
						un.setReferencia(res.getString("referencia"));
						un.setCredito(res.getBigDecimal("credito"));
						un.setDebito(res.getBigDecimal("debito"));
						un.setSaldo(res.getBigDecimal("saldo"));
						cuentas.add(un);
					 }
					
				}catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
			{
				try{
					if(res!=null)res.close();
					if(psConsultas!=null)psConsultas.close();
					if(conn!=null)conn.close();
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
	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	public Banco getMyBanco() {
		return myBanco;
	}
	public void setMyBanco(Banco myBanco) {
		this.myBanco = myBanco;
	}

}
