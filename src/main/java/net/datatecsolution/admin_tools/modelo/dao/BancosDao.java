package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BancosDao extends ModeloDaoBasic{
	
	private int idRegistrado=-1;
	
	private String sqlBaseJoin=null;
	
	public BancosDao(){
		super("bancos","id");
		
		sqlBaseJoin="SELECT bancos.nombre, "
							+ " bancos.no_cuenta, "
							+ " tipo_cuenta_bancos.tipo_cuenta, "
							+ " bancos.id, "
							+ " tipo_cuenta_bancos.observaciones, "
							+ " bancos.id_tipo_cuenta  "
				+ " FROM "
						+ super.DbName+".bancos "
							+ "INNER JOIN "+super.DbName+".tipo_cuenta_bancos "
									+ "ON (bancos.id_tipo_cuenta = tipo_cuenta_bancos.id)";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	public Vector<Banco> getCuentas(){
		Vector<Banco> cuentas=new Vector<Banco>();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect());
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				Banco una=new Banco();
				existe=true;
				una.setId(res.getInt("id"));
				una.setNombre(res.getString("nombre"));
				una.setNoCuenta(res.getString("no_cuenta"));
				una.setTipoCuenta(res.getString("tipo_cuenta"));
				una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
				cuentas.add(una);
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
	
	
	public Vector<Banco> getCuentasExectoEfectivo(){
		Vector<Banco> cuentas=new Vector<Banco>();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where bancos.id<>1");
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				Banco una=new Banco();
				existe=true;
				una.setId(res.getInt("id"));
				una.setNombre(res.getString("nombre"));
				una.setNoCuenta(res.getString("no_cuenta"));
				una.setTipoCuenta(res.getString("tipo_cuenta"));
				una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
				cuentas.add(una);
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
	
	public void setIdRegistrado(int i){
		idRegistrado=i;
	}
	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		Banco myCuenta=(Banco)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryUpdate()+" SET nombre=?,no_cuenta=?,id_tipo_cuenta=? where id=?");
			
			psConsultas.setString( 1, myCuenta.getNombre() );
			psConsultas.setString( 2,myCuenta.getNoCuenta() );
			
			
			psConsultas.setInt( 3, myCuenta.getIdTipoCuenta());
			
			
			psConsultas.setInt(4, myCuenta.getId());
			
			
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
	}
	@Override
	public List<Banco> todos(int limiteInferior, int limiteSuperior) {
		// TODO Auto-generated method stub
		
		List<Banco> cuentas=new ArrayList<Banco>();
				ResultSet res=null;
				
				Connection conn=null;
				
				boolean existe=false;
				
				try {
					
					conn=ConexionStatic.getPoolConexion().getConnection();
					psConsultas=conn.prepareStatement( super.getQueryRecord());
					
					psConsultas.setInt(1, limiteSuperior);
					psConsultas.setInt(2, limiteInferior);
					
					res = psConsultas.executeQuery();
					while(res.next()){
						
						Banco una=new Banco();
						existe=true;
						una.setId(res.getInt("id"));
						una.setNombre(res.getString("nombre"));
						una.setNoCuenta(res.getString("no_cuenta"));
						una.setTipoCuenta(res.getString("tipo_cuenta"));
						una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
						cuentas.add(una);
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
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		Banco myCuenta=(Banco)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+"(nombre,no_cuenta,id_tipo_cuenta) VALUES (?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myCuenta.getNombre() );
			psConsultas.setString( 2,myCuenta.getNoCuenta() );
			psConsultas.setInt( 3, myCuenta.getIdTipoCuenta());
			
				
			
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
			}
			
			
			
			

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
	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

}
