package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CategoriaDao extends ModeloDaoBasic{
	
	private int idCategoriaRegistrada;
	
	
	
	public CategoriaDao(){
		super("marcas","codigo_marca");
		
	}
	
	public Vector<Categoria> todosVector() {
		
		Vector<Categoria> categorias=new Vector<Categoria>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect());
			
			
			
			res=psConsultas.executeQuery();
			while(res.next()){
				Categoria unaCategoria=new Categoria();
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				categorias.add(unaCategoria);
				existe=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res!=null)res.close();
				if(psConsultas!=null)psConsultas.close();
				if(conn!=null)conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
			} // fin de catch
		} // fin de finally
		
		if(existe)
			return categorias;
		else
			return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eleminar marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para eliminar categorias
	 * @param id de categoria
	 * @return true=se elemino correctamen o false=no se puedo eliminar
	 */
	@Override
	public boolean eliminar(Object c){
		Categoria myCategoria=(Categoria)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_marca = ?");
			psConsultas.setInt( 1, myCategoria.getId() );
			resultado=psConsultas.executeUpdate();
			return true;
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e.getMessage());
				return false;
		}
		finally
		{
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
		} // fin de finally
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Actualizar los marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para actualizar categorias
	 * @param myCategoria
	 * @return true= se actualizo,  o false= no se pudo actualizar
	 */
	@Override
	public boolean actualizar(Object c){
		Categoria myCategoria=(Categoria)c;
		Connection conn=null;
		
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET descripcion = ?, observacion = ? WHERE codigo_marca = ?");
				psConsultas.setString(1, myCategoria.getDescripcion());
				psConsultas.setString(2, myCategoria.getObservacion());
				
				psConsultas.setInt(3, myCategoria.getId());
				
				
				psConsultas.executeUpdate();
				return true;
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getMessage());
            return false;
            
			
		}
		finally
		{
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
		} // fin de finally
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< selecciona de la Bd todas las MArcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Selecciona de la base de tados todas la categorias y las pagina de 20 en 20
	 * @param limite inferior para la paginacion
	 * @param limite superio para la paginacion
	 * @return Lista de categorias
	 */
	@Override
	public List<Categoria> todos(int canItemPag,int limSupe){
		
		List<Categoria> categorias=new ArrayList<Categoria>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			//System.out.println(super.getQuerySelect()+"<<<<===QUERY");
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas = conn.prepareStatement(super.getQuerySelect()+ " where "+ super.DbName+".marcas.codigo_marca<=((SELECT max("+ super.DbName+".marcas.codigo_marca) from "+ super.DbName+".marcas)-?) and "+ super.DbName+".marcas.codigo_marca> ((SELECT max("+ super.DbName+".marcas.codigo_marca) from "+ super.DbName+".empleados)-?) ORDER BY "+ super.DbName+".marcas.codigo_marca DESC;");
			psConsultas = conn.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, canItemPag);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				Categoria unaCategoria=new Categoria();
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				categorias.add(unaCategoria);
			 }
			//res.close();
			//conexion.desconectar();
					
					
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
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return categorias;
			}
			else return null;
			
		}
	
	
public List<Categoria> todos(){
		
		List<Categoria> categorias=new ArrayList<Categoria>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			//System.out.println(super.getQuerySelect()+"<<<<===QUERY");
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(super.getQuerySelect());
			
	
			res = psConsultas.executeQuery();
			while(res.next()){
				Categoria unaCategoria=new Categoria();
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				categorias.add(unaCategoria);
			 }
			//res.close();
			//conexion.desconectar();
					
					
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
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return categorias;
			}
			else return null;
			
		}
	
	
	/**
	 * @param id de la categoria registrada
	 */
	public void setIdCategoriaRegistrada(int i){
		idCategoriaRegistrada=i;
	}
	/**
	 * @return id de la categoria registrada
	 */
	public int getIdCategoriaRegistrada(){
		return idCategoriaRegistrada;
	} 
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo paa agregar categorias
	 * @param myCategoria
	 * @return true=se registro o false=no se registro
	 */
	@Override
	public boolean registrar(Object c)
	{
		Categoria myCategoria=(Categoria)c;
		int resultado=0;
		ResultSet res=null;
		Connection conn=null;
		
		try 
		{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( super.getQueryInsert()+"(descripcion,observacion) VALUES (?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setString( 1, myCategoria.getDescripcion() );
			psConsultas.setString( 2, myCategoria.getObservacion() );
			
			resultado=psConsultas.executeUpdate();
			
			res=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(res.next()){
				this.setIdCategoriaRegistrada(res.getInt(1));
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
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar marca por ID>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para buscar la categoria por id
	 * @param id de la categoria
	 * @return categoria entrada sino null
	 */
	public Categoria buscarPorId(int i){
		
		Categoria unaCategoria=new Categoria();
		
		ResultSet res=null;
		
		boolean existe=false;
		
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(super.getQuerySearch("codigo_marca", "="));
			psConsultas.setInt(1, i);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				
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
				//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return unaCategoria;
			}
			else return null;
		
		
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los marcas por observacion>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para buscar la categorias por la observacion
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de categorias encontradas sino null
	 */
	public List<Categoria> buscarPorObservacion(String busqueda,int limitInferio, int canItemPag){
		List<Categoria> categorias=new ArrayList<Categoria>();
		ResultSet res=null;
		 
		boolean existe=false;
		
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("observacion", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Categoria unaCategoria=new Categoria();
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				categorias.add(unaCategoria);
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
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
				return categorias;
			}
			else return null;
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los marcas por observacion>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para buscar las categorias por nombre
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de categorias encontradas sino null
	 */
	public List<Categoria> buscarPorNombre(String busqueda,int limitInferio, int canItemPag){
		List<Categoria> categorias=new ArrayList<Categoria>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("descripcion", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Categoria unaCategoria=new Categoria();
				existe=true;
				unaCategoria.setId(Integer.parseInt(res.getString("codigo_marca")));
				unaCategoria.setDescripcion(res.getString("descripcion"));
				unaCategoria.setObservacion(res.getString("observacion"));
				categorias.add(unaCategoria);
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
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
				return categorias;
			}
			else return null;
		
	}
	
	
}
