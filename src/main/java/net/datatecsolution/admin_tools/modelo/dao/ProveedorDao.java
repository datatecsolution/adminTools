package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Proveedor;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDao extends ModeloDaoBasic {
	private int idProveedorRegistrado;
	
	
	
	public ProveedorDao(){
		super("proveedor","codigo_proveedor");
		super.setSqlQuerySelectJoin("Select *,"+ super.DbName+ ".f_saldo_proveedor("+super.tableName+"."+super.idColumnName+") as saldo from "+super.DbName +"."+super.tableName);
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agregar un proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		Proveedor myProveedor=(Proveedor)c;
		
		int resultado=0;
		ResultSet res=null;
		Connection conn=null;
		try 
		{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryInsert()+ "(nombre_proveedor,telefono,celular,direccion) VALUES (?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setString( 1, myProveedor.getNombre() );
			psConsultas.setString( 2, myProveedor.getTelefono() );
			psConsultas.setString( 3, myProveedor.getCelular() );
			psConsultas.setString( 4, myProveedor.getDireccion() );
			
			resultado=psConsultas.executeUpdate();
			res=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(res.next()){
				this.setIdProveedorRegistrado(res.getInt(1));
				//int clave=rs.getInt(1);
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para el atributo de ID>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public int getIdProveedorRegistrado(){
		return idProveedorRegistrado;
	}
	
	public void setIdProveedorRegistrado(int id){
		idProveedorRegistrado=id;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Actualizar los proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean actualizar(Object c){
		Proveedor myProveedor=(Proveedor)c;
		Connection conn=null;
		
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET nombre_proveedor = ?, telefono =?, celular = ?, direccion = ?  WHERE codigo_proveedor = ?");
				psConsultas.setString(1, myProveedor.getNombre());
				psConsultas.setString(2, myProveedor.getTelefono());
				psConsultas.setString(3, myProveedor.getCelular());
				psConsultas.setString(4, myProveedor.getDireccion());
				psConsultas.setInt(5, myProveedor.getId());
				
				psConsultas.executeUpdate();
				/*Statement estatuto = conexion.getConnection().createStatement();
				estatuto.executeUpdate("UPDATE proveedor SET nombre_proveedor ='"+myProveedor.getNombre()+"', telefono ='"
					+myProveedor.getTelefono()+"', celular = '"+myProveedor.getCelular()+"', direccion = '"+myProveedor.getDireccion()+"'  WHERE codigo_proveedor ="+myProveedor.getId());
				estatuto.close();
				conexion.desconectar();*/
				return true;
			
		} catch (SQLException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally
		
	}


	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Eliminar los proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean eliminar(Object c){
		Proveedor myProveedor=(Proveedor)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_proveedor = ?");
				psConsultas.setInt( 1, myProveedor.getId() );
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
				//if(res != null) res.close();
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar proveedor por ID>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Proveedor buscarPorId(int i){
		//Conexion conex= new Conexion();
		Proveedor unoPro=new Proveedor();
		ResultSet res=null;
		boolean existe=false;
		
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(super.getQuerySearch("codigo_proveedor", "="));
			psConsultas.setInt(1, i);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unoPro.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unoPro.setNombre(res.getString("nombre_proveedor"));
				unoPro.setTelefono(res.getString("telefono"));
				unoPro.setCelular(res.getString("celular"));
				unoPro.setDireccion(res.getString("direccion"));
				unoPro.setSaldo(res.getBigDecimal("saldo"));
				
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
				return unoPro;
			}
			else return null;
		
		
		
	}

	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los proveedores por nombre>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 *Metodo para busca los proveedores por nombre
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de proveedores encontrados
	 */
	public List<Proveedor> buscarPorNombre(String busqueda,int limite, int cantidad){
		List<Proveedor> proveedores=new ArrayList<Proveedor>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("nombre_proveedor", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limite);
			psConsultas.setInt(3, cantidad);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Proveedor unoPro=new Proveedor();
				existe=true;
				unoPro.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unoPro.setNombre(res.getString("nombre_proveedor"));
				unoPro.setTelefono(res.getString("telefono"));
				unoPro.setCelular(res.getString("celular"));
				unoPro.setDireccion(res.getString("direccion"));
				unoPro.setSaldo(res.getBigDecimal("saldo"));
				proveedores.add(unoPro);
				//System.out.println(unoPro);
			 }
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
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
				return proveedores;
			}
			else return null;
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar los proveedores por direccion>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 *Metodo para busca los proveedores por direccion
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de proveedores encontrados
	 */
	public List<Proveedor> buscarPorDireccion(String busqueda,int limite, int cantidad){
		List<Proveedor> proveedores=new ArrayList<Proveedor>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("direccion", "LIKE"));
		
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limite);
			psConsultas.setInt(3, cantidad);
			res = psConsultas.executeQuery();
			while(res.next()){
				Proveedor unoPro=new Proveedor();
				existe=true;
				unoPro.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unoPro.setNombre(res.getString("nombre_proveedor"));
				unoPro.setTelefono(res.getString("telefono"));
				unoPro.setCelular(res.getString("celular"));
				unoPro.setDireccion(res.getString("direccion"));
				unoPro.setSaldo(res.getBigDecimal("saldo"));
				proveedores.add(unoPro);
				//System.out.println(unoPro);
			 }
					
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return proveedores;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca todos los proveedores>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/	
	@Override
	public List<Proveedor> todos(int canItemPag,int limSupe){
		List<Proveedor> proveedores=new ArrayList<Proveedor>();
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				Proveedor unoPro=new Proveedor();
				existe=true;
				unoPro.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unoPro.setNombre(res.getString("nombre_proveedor"));
				unoPro.setTelefono(res.getString("telefono"));
				unoPro.setCelular(res.getString("celular"));
				unoPro.setDireccion(res.getString("direccion"));
				unoPro.setSaldo(res.getBigDecimal("saldo"));
				proveedores.add(unoPro);
			 }					
					
			} catch (SQLException e) {
				e.printStackTrace();
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
				return proveedores;
			}
			else return null;
		
		
		//return  proveedores;
	}
	
	public ProveedorDao getProveedorDao(){
		return this;
	}
	
	public BigDecimal getSaldoProveedor(int idProveedor) {
		// TODO Auto-generated method stub

		BigDecimal saldo=new BigDecimal(0);
		//se crear un referencia al pool de conexiones
		
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
       
		
		ResultSet res=null;
		
		boolean existe=false;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement("select "+super.DbName+".f_saldo_proveedor(?) as saldo;");
			
			psConsultas.setInt(1, idProveedor);
			res=psConsultas.executeQuery();
			while(res.next()){
				saldo=res.getBigDecimal("saldo");
				existe=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		try{
			
			if(res != null) res.close();
            if(psConsultas != null)psConsultas.close();
            if(con != null) con.close();
            
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();

			} // fin de catch
		
		if(existe){
				return saldo;
		}
		else
			return new BigDecimal(0);
		
	
		
	}

}
