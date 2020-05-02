package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EmpleadoDao extends ModeloDaoBasic {
	
	
	private int idRegistrado;

	public EmpleadoDao() {
		// TODO Auto-generated constructor stub
		super("empleados","codigo_empleado");
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Vector<Empleado> todoEmpleadosVendedores(){
		
		
		
        Connection con = null;
        
        
      
        Vector<Empleado> vendedores=new Vector<Empleado>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement(super.getQuerySelect());
			//psConsultas.setInt(1,1);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			while(res.next()){
				Empleado unEmpleado=new Empleado();
				existe=true;
				unEmpleado.setCodigo(res.getInt("codigo_empleado"));
				unEmpleado.setNombre(res.getString("nombre"));
				unEmpleado.setApellido(res.getString("apellido"));
				
				
				vendedores.add(unEmpleado);
			 }
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(super.psConsultas != null)psConsultas.close();
                if(con != null) con.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		
			if (existe) {
				return vendedores;
			}
			else return null;
		
	}
	
	/**
	 * Metodo para buscar un empleado en la base de datos
	 * @param id
	 * @return Empleado encontrado
	 */
	@Override
	public Empleado buscarPorId(int id) {
		// TODO Auto-generated method stub
		
		Empleado myEmpleado=new Empleado();
		
	
		ResultSet res=null;
		boolean existe=false;
		Connection con = null;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();			
			
			psConsultas=con.prepareStatement(super.getQuerySearch("codigo_empleado", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				myEmpleado.setCodigo(res.getInt("codigo_empleado"));
				myEmpleado.setNombre(res.getString("nombre"));
				myEmpleado.setApellido(res.getString("apellido"));
				myEmpleado.setTelefono(res.getString("telefono"));
				myEmpleado.setCorreo(res.getString("correo"));
				myEmpleado.setDireccion(res.getString("direccion"));
				myEmpleado.setSueldoBase(res.getBigDecimal("sueldo_base"));
				
				
			
			 }
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
		
		
			if (existe) {
				return myEmpleado;
			}
			else return null;
	}
	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		
		Empleado myEmpleado=(Empleado)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (nombre,apellido,telefono,correo) VALUES (?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString(1, myEmpleado.getNombre());
			psConsultas.setString(2, myEmpleado.getApellido());
			psConsultas.setString(3, myEmpleado.getTelefono());
			psConsultas.setString(4, myEmpleado.getCorreo());
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
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
	}//fin de registrar
	
	@Override
	public List<Empleado> todos(int limInf,int limSupe) {
		List<Empleado> empleados =new ArrayList<Empleado>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement(super.getQuerySelect()+ "join (select codigo_empleado from "+super.DbName+"."+super.tableName+" where codigo_empleado<=((SELECT max(codigo_empleado) from "+ super.DbName+"."+super.tableName+")-?)  ORDER BY codigo_empleado DESC LIMIT ? ) empleados2 on(empleados2.codigo_empleado="+super.tableName+".codigo_empleado ) ORDER BY "+super.tableName+".codigo_empleado DESC");
			psConsultas=conn.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Empleado un=new Empleado();
				un.setCodigo(res.getInt("codigo_empleado"));
				un.setNombre(res.getString("nombre"));
				un.setApellido(res.getString("apellido"));
				un.setTelefono(res.getString("telefono"));
				un.setCorreo(res.getString("correo"));
				empleados.add(un);
				
				
			}
		}catch (SQLException e) {
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
			return empleados;
		}
		else return null;
	}
	@Override
	public boolean actualizar(Object c) {
		Empleado myEmpleado=(Empleado)c;
		int resultado;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET nombre = ?, apellido = ?,telefono=? ,correo = ? WHERE codigo_empleado = ?");
			//nuevo=con.prepareStatement( "INSERT INTO usuario(usuario,nombre_completo,clave,permiso,tipo_permiso) VALUES (?,?,?,?,?)");
			psConsultas.setString( 1, myEmpleado.getNombre() );
			psConsultas.setString( 2, myEmpleado.getApellido() );
			psConsultas.setString( 3, myEmpleado.getTelefono());
			psConsultas.setString(4, myEmpleado.getCorreo());
			psConsultas.setInt(5,myEmpleado.getCodigo());
			resultado=psConsultas.executeUpdate();
			return true;
		}catch (SQLException e) {
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
			//conexion.desconectar();
			} // fin de catch
		} // fin de finally
		
	}

	/**
	 * Metodo para buscar los empleados por el nombre 
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de empleados encontrados
	 */
	public List<Empleado> porNombre(String busqueda,int limitInferio, int canItemPag) {
		List<Empleado> empleados =new ArrayList<Empleado>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("nombre", "like"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Empleado un=new Empleado();
				un.setCodigo(res.getInt("codigo_empleado"));
				un.setNombre(res.getString("nombre"));
				un.setApellido(res.getString("apellido"));
				un.setTelefono(res.getString("telefono"));
				un.setCorreo(res.getString("correo"));
				empleados.add(un);
				
				
			}
		}catch (SQLException e) {
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
			return empleados;
		}
		else return null;
	}

	/**
	 * Metodo para buscar los empleados por el apellido 
	 * @param busqueda
	 * @param limite de paginacion 
	 * @param cantidad de item por pagina
	 * @return lista de paginas
	 */
	public List<Empleado> porApellido(String busqueda,int limitInferio, int canItemPag) {
		List<Empleado> empleados = new ArrayList<Empleado>();

		ResultSet res = null;

		Connection conn = null;

		boolean existe = false;

		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(super.getQuerySearch("apellido", "like"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			while (res.next()) {
				existe = true;
				Empleado un = new Empleado();
				un.setCodigo(res.getInt("codigo_empleado"));
				un.setNombre(res.getString("nombre"));
				un.setApellido(res.getString("apellido"));
				un.setTelefono(res.getString("telefono"));
				un.setCorreo(res.getString("correo"));
				empleados.add(un);

			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
		} finally {
			try {
				if (res != null)
					res.close();
				if (psConsultas != null)
					psConsultas.close();
				if (conn != null)
					conn.close();

			} // fin de try
			catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
				// conexion.desconectar();
			} // fin de catch
		} // fin de finally

		if (existe) {
			return empleados;
		} else
			return null;
	}
	public void setIdRegistrado(int i){
		idRegistrado=i;
	}
	public int getIdRegistrado(){
		return idRegistrado;
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}


}
