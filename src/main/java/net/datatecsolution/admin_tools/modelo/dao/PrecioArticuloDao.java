package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrecioArticuloDao extends ModeloDaoBasic {
	
	
	public PrecioArticuloDao() {
		// TODO Auto-generated constructor stub
		super("precios_articulos","id");
		
		String sqlBaseJoin="SELECT "
				+ "`precios`.`codigo_precio` AS `codigo_precio`,"
				+ "`precios`.`descripcion` AS `descripcion`,"
				+ "`precios_articulos`.`codigo_articulo` AS `codigo_articulo`,"
				+ "`precios_articulos`.`precio_articulo` AS `precio_articulo`"
				+ " FROM "
				+ super.DbName+".`precios` "
				+ "JOIN "
				+ super.DbName+".`precios_articulos` ON( `precios`.`codigo_precio` = `precios_articulos`.`codigo_precio`) ";
			//	+ "ORDER BY `precios`.`codigo_precio`";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los precios de un articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<PrecioArticulo> getTipoPrecios(){
		
		
		
        Connection con = null;
        
        
      
       	List<PrecioArticulo> precios=new ArrayList<PrecioArticulo>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement("SELECT * FROM "+super.DbName+".precios ;");
			//buscarPorArticulo.setInt(1,id);
			res = psConsultas.executeQuery();
			while(res.next()){
				PrecioArticulo unPrecio=new PrecioArticulo();
				existe=true;
				
				//unPrecio.setCodigoArticulo(res.getInt("codigo_articulo"));
				unPrecio.setCodigoPrecio(res.getInt("codigo_precio"));
				unPrecio.setDecripcion(res.getString("descripcion"));
				//unPrecio.setPrecio(res.getBigDecimal("precio_articulo"));
				
				
				precios.add(unPrecio);
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
                if(con != null) con.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		
			if (existe) {
				return precios;
			}
			else return null;
		
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los precios de un articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<PrecioArticulo> getPreciosArticulo(int id){
		
		
		
        Connection con = null;
        
        
      
       	List<PrecioArticulo> precios=new ArrayList<PrecioArticulo>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" WHERE precios_articulos.codigo_articulo = ?;");
			psConsultas.setInt(1,id);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				PrecioArticulo unPrecio=new PrecioArticulo();
				existe=true;
				
				unPrecio.setCodigoArticulo(res.getInt("codigo_articulo"));
				unPrecio.setCodigoPrecio(res.getInt("codigo_precio"));
				unPrecio.setDecripcion(res.getString("descripcion"));
				unPrecio.setPrecio(res.getBigDecimal("precio_articulo"));
				
				
				precios.add(unPrecio);
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
                if(con != null) con.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		
			if (existe) {
				return precios;
			}
			else return null;
		
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los precios de un articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<PrecioArticulo> getPreciosArticuloSinCosto(int id){



		Connection con = null;



		List<PrecioArticulo> precios=new ArrayList<PrecioArticulo>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas = con.prepareStatement(super.getQuerySelect()+" WHERE precios_articulos.codigo_articulo = ? and precios_articulos.codigo_precio<4;");
			psConsultas.setInt(1,id);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				PrecioArticulo unPrecio=new PrecioArticulo();
				existe=true;

				unPrecio.setCodigoArticulo(res.getInt("codigo_articulo"));
				unPrecio.setCodigoPrecio(res.getInt("codigo_precio"));
				unPrecio.setDecripcion(res.getString("descripcion"));
				unPrecio.setPrecio(res.getBigDecimal("precio_articulo"));


				precios.add(unPrecio);
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
				if(con != null) con.close();


			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally


		if (existe) {
			return precios;
		}
		else return null;

	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar de articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean eliminar(Object c){
		PrecioArticulo precios=(PrecioArticulo)c;
		int resultado=0;
		Connection conn=null;
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_articulo = ? and codigo_precio= ?");
			
				psConsultas.setInt( 1, precios.getCodigoArticulo());
				psConsultas.setInt( 2, precios.getCodigoPrecio() );
				
				
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
	public boolean actualizar(Object c){
		PrecioArticulo precios=(PrecioArticulo)c;
		int resultado=0;
		Connection conn=null;
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryUpdate()+" set precio_articulo=?  WHERE codigo_articulo = ? and codigo_precio= ?");
				psConsultas.setFloat(1, precios.getPrecio().floatValue());
				psConsultas.setInt( 2, precios.getCodigoArticulo());
				psConsultas.setInt( 3, precios.getCodigoPrecio() );
				
				
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		PrecioArticulo precio=(PrecioArticulo)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+"(codigo_articulo,precio_articulo,codigo_precio) VALUES (?,?,?)");
			
			psConsultas.setInt(1, precio.getCodigoArticulo());
			psConsultas.setFloat(2, precio.getPrecio().floatValue());
			psConsultas.setInt(3, precio.getCodigoPrecio());
			
			
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
