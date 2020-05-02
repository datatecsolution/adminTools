package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CuentaPorPagarDao extends ModeloDaoBasic {
	


	public CuentaPorPagarDao() {
		super("cuentas_por_pagar","codigo_reguistro");
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaPorPagar getSaldoProveedor(Proveedor myProveedor){
		CuentaPorPagar un=new CuentaPorPagar();
		
		Connection con = null;
        
 
    	
    	String sql2=super.getQuerySelect()+" WHERE codigo_proveedor = ? ORDER BY codigo_reguistro DESC LIMIT 1";
    	ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setInt(1, myProveedor.getId());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				un.setProveedor(myProveedor);
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getString("fecha"));
				un.setCredito(res.getBigDecimal("credito"));
				un.setDebito(res.getBigDecimal("debito"));
				un.setSaldo(res.getBigDecimal("saldo"));
			
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
		return un;
	}
	
	
	
	public boolean reguistrarDebito(ReciboPagoProveedor myRecibo) {
		// TODO Auto-generated method stub
		CuentaPorPagar aRegistrar=new CuentaPorPagar();
		CuentaPorPagar ultima=this.getSaldoProveedor(myRecibo.getProveedor());
		
		
		aRegistrar.setProveedor(myRecibo.getProveedor());
		aRegistrar.setDescripcion(myRecibo.getConcepto());
		
		aRegistrar.setDebito(myRecibo.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().subtract(myRecibo.getTotal());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_proveedor,descripcion,credito,saldo) VALUES (now(),?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getProveedor().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(3, aRegistrar.getDebito());
					psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
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
	
	public boolean reguistrarCredito(FacturaCompra myFactura) {
		// TODO Auto-generated method stub
		CuentaPorPagar aRegistrar=new CuentaPorPagar();
		CuentaPorPagar ultima=this.getSaldoProveedor(myFactura.getProveedor());
		
		
		aRegistrar.setProveedor(myFactura.getProveedor());
		aRegistrar.setDescripcion("compra segun factura no. "+myFactura.getIdFactura());
		aRegistrar.setCredito(myFactura.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_proveedor,descripcion,credito,saldo) VALUES (now(),?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getProveedor().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(3, aRegistrar.getCredito());
					psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
					resultado=psConsultas.executeUpdate();
					
					return true;
					
				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
