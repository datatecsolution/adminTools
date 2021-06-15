package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CuentaPorCobrarDao extends ModeloDaoBasic {
	
	

	public CuentaPorCobrarDao() {
		super("cuentas_por_cobrar","codigo_reguistro");
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<      se obtiene el ultimo registro        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public CuentaPorCobrar getSaldoCliente(Cliente myCliente){
		CuentaPorCobrar un=new CuentaPorCobrar();
		
		Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.getQuerySelect()+" WHERE cuentas_por_cobrar.codigo_cliente = ? ORDER BY cuentas_por_cobrar.codigo_reguistro DESC LIMIT 1";
    	ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setInt(1, myCliente.getId());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				un.setNoReguistro(res.getInt("codigo_reguistro"));
				un.setCliente(myCliente);
				un.setDescripcion(res.getString("descripcion"));
				un.setFecha(res.getString("fecha"));
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
	
	
	
	public boolean reguistrarDebito(ReciboPago myRecibo) {
		// TODO Auto-generated method stub
		CuentaPorCobrar aRegistrar=new CuentaPorCobrar();
		CuentaPorCobrar ultima=this.getSaldoCliente(myRecibo.getCliente());
		
		
		aRegistrar.setCliente(myRecibo.getCliente());
		aRegistrar.setDescripcion(myRecibo.getConcepto());
		//aRegistrar.setCredito(myFactura.getTotal());
		aRegistrar.setDebito(myRecibo.getTotal());//.setCredito(myFactura.getTotal());
		BigDecimal newSaldo=ultima.getSaldo().subtract(myRecibo.getTotal());//.add(myFactura.getTotal());
		aRegistrar.setSaldo(newSaldo);
		
		//JOptionPane.showConfirmDialog(null, myCliente);
				int resultado=0;
				ResultSet rs=null;
				Connection con = null;
				
				try 
				{
					con = ConexionStatic.getPoolConexion().getConnection();
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cliente,descripcion,debito,saldo) VALUES (now(),?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
					psConsultas.setInt(1, aRegistrar.getCliente().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(3, aRegistrar.getDebito());
					psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
					resultado=psConsultas.executeUpdate();
					/*insertarNuevaCliente.setString( 1, myCliente.getNombre() );
					insertarNuevaCliente.setString( 2, myCliente.getDereccion() );
					insertarNuevaCliente.setString( 3, myCliente.getTelefono());
					insertarNuevaCliente.setString(4, myCliente.getCelular());
					insertarNuevaCliente.setString(5, myCliente.getRtn());
					
					
					
					rs=insertarNuevaCliente.getGeneratedKeys(); //obtengo las ultimas llaves generadas
					while(rs.next()){
						this.setIdClienteRegistrado(rs.getInt(1));
					}
					*/
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
	
	public boolean reguistrarCredito(Factura myFactura) {
		// TODO Auto-generated method stub
		CuentaPorCobrar aRegistrar=new CuentaPorCobrar();
		CuentaPorCobrar ultima=this.getSaldoCliente(myFactura.getCliente());

		String fecha= myFactura.getFecha()==null? " now(), ":"'"+myFactura.getFecha()+" 00:00:00', ";

		/** se construlle la descripcion de la cuenta segun la factura ***/
		String descripcion="venta de ";
		for(int x=0;x<myFactura.getDetalles().size();x++){

			if(myFactura.getDetalles().get(x).getArticulo().getId()!=-1)
				descripcion=descripcion+myFactura.getDetalles().get(x).getArticulo().getArticulo()+" , ";
		}
		descripcion=descripcion+" fact # "+ myFactura.getIdFactura();


		aRegistrar.setDescripcion(descripcion);
		aRegistrar.setCliente(myFactura.getCliente());
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
					
					psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cliente,descripcion,credito,saldo) VALUES ("+fecha+" ?,?,?,?)");
					psConsultas.setInt(1, aRegistrar.getCliente().getId());
					psConsultas.setString(2, aRegistrar.getDescripcion());
					psConsultas.setBigDecimal(3, aRegistrar.getCredito());
					psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
					//psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
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

	public boolean reguistrarCredito(CuentaPorCobrar aRegistrar) {
		// TODO Auto-generated method stub

		CuentaPorCobrar ultima=this.getSaldoCliente(aRegistrar.getCliente());


		BigDecimal newSaldo=ultima.getSaldo().add(aRegistrar.getSaldo());
		aRegistrar.setSaldo(newSaldo);

		//JOptionPane.showConfirmDialog(null, myCliente);
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;

		try
		{
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas=con.prepareStatement( super.getQueryInsert()+" (fecha,codigo_cliente,descripcion,credito,saldo) VALUES (now(), ?,?,?,?)");
			psConsultas.setInt(1, aRegistrar.getCliente().getId());
			psConsultas.setString(2, aRegistrar.getDescripcion());
			psConsultas.setBigDecimal(3, aRegistrar.getCredito());
			psConsultas.setBigDecimal(4, aRegistrar.getSaldo());
			//psConsultas.setString(5, ConexionStatic.getUsuarioLogin().getUser());
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
