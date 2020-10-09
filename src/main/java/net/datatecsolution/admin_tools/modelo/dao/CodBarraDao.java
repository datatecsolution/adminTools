package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CodBarraDao extends ModeloDaoBasic {
	
	
	
	public CodBarraDao(){
		super("codigos_articulos","id_codigo");
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< selecciona de la Bd todas las MArcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<CodBarra> getCodsArticulo(int codArticulo){
		List<CodBarra> cods=new ArrayList<CodBarra>();
		ResultSet res=null;
		Connection conn=null;
		
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where codigo_articulo =  ?");
			
			psConsultas.setInt(1, codArticulo);
			res = psConsultas.executeQuery();
			while(res.next()){
				CodBarra unCod=new CodBarra();
				existe=true;

				unCod.setCodArticulo(res.getInt("codigo_articulo"));
				unCod.setCodigoBarra(res.getString("codigo_barra"));
				
				cods.add(unCod);
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
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return cods;
			}
			else return null;
			
		}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< selecciona de la Bd todas las MArcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public String getCodArticulo(int codArticulo){
		
		ResultSet res=null;
		Connection conn=null;
		String codigo="";
		
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where codigo_articulo =  ?");
			
			psConsultas.setInt(1, codArticulo);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				codigo=res.getString("codigo_barra");

				
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
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return codigo;
			}
			else return null;
			
		}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar de articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean eliminarCodigo(int idArticulo){
		int resultado=0;
		
		Connection conn=null;
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_articulo = ?");
				
				psConsultas.setInt( 1, idArticulo );
				resultado=psConsultas.executeUpdate();
				return true;
			
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		finally
		{
			try{
					
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar de articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean eliminar(Object c){
		CodBarra cod=(CodBarra)c;
		int resultado=0;
		Connection conn=null;
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_barra = ? and codigo_articulo= ?");
			
				psConsultas.setString( 1, cod.getCodigoBarra() );
				psConsultas.setInt( 2, cod.getCodArticulo() );
				
				
				resultado=psConsultas.executeUpdate();
		
				return true;
			
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		finally
		{
			try{
					
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar de codigos de barra>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean registrarCodsBarra(List<CodBarra> codsBarras){
		
		Connection conn=null;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( super.getQueryInsert()+" (codigo_articulo,codigo_barra) VALUES (?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			for(int x=0;x<codsBarras.size();x++){
				psConsultas.setInt(1, codsBarras.get(x).getCodArticulo());
				psConsultas.setString(2,codsBarras.get(x).getCodigoBarra() );
				psConsultas.executeUpdate();
				
			}
			return true;
		}catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
	            return false;
			}
		finally
		{
			try{
					
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
		
			
	

	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para registrar de codigos de barra>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c){
		CodBarra codBarra=(CodBarra)c;
		Connection conn=null;
		try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryInsert()+ " (codigo_articulo,codigo_barra) VALUES (?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
				psConsultas.setInt(1, codBarra.getCodArticulo());
				psConsultas.setString(2,codBarra.getCodigoBarra() );
				psConsultas.executeUpdate();
				
			
			return true;
		}catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
	            return false;
			}
		finally
		{
			try{
					
	                if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		} // fin de finally
		
			
	

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
