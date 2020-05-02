package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.Usuario;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**

* Esta clase representa a un empleado de la empresa

* @author: jdmayorga

* @version: 16/07/2015

* @see <a href = "https://www.github.com/jdmayorga" /> direccion del creador </a>

*/
public class UsuarioDao  extends ModeloDaoBasic {
	private int idRegistrado=-1;
	
	private CajaDao cajasDao;
	
	public UsuarioDao(){
		super( "usuario","id");
		cajasDao=new CajaDao();
	}
	
	public boolean comprobarAdmin(String pwd){
		
		boolean resultado=false;
		
        Connection con = null;
        
    	String sql=super.getQuerySelect()+" where permiso='administrador' and clave=?";
        //Statement stmt = null;
       	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, pwd);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				resultado=true;
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				resultado=false;
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
		
		
		
	
		
		return resultado;
	}
	
	public void setIdRegistrado(int i){
		idRegistrado=i;
	}
	public int getIdRegistrado(){
		return idRegistrado;
	} 
	
	/**

     * Metodo para conseguir todos los usuarios del sistema
     * 

     * @return lista de usuarios del sistema

     */
	@Override
	public List<Usuario> todos(int limInf,int limSupe){
		
		List<Usuario> usuarios =new ArrayList<Usuario>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement(super.getQuerySelect()+ "join (select id from "+super.DbName+"."+super.tableName+" where usuario.id<=((SELECT max(usuario.id) from "+ super.DbName+".usuario)-?) ORDER BY usuario.id DESC LIMIT ?) usuario2 on ( usuario2.id=usuario.id) ORDER BY usuario.id DESC");
			psConsultas=conn.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Usuario un=new Usuario();
				un.setUser(res.getString("usuario"));
				un.setNombre(res.getString("nombre_completo"));
				un.setPwd(res.getString("clave"));
				un.setTipoPermiso(res.getInt("tipo_permiso"));
				un.setPermiso(res.getString("permiso"));
				usuarios.add(un);
				
				
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
			return usuarios;
		}
		else return null;
		
	}
	
	
	/**

     * Metodo para conseguir todos los usuarios del sistema
     * 

     * @return lista de usuarios del sistema

     */
	public List<Usuario> todos(){
		
		List<Usuario> usuarios =new ArrayList<Usuario>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement(super.getQuerySelect()+ "join (select id from "+super.DbName+"."+super.tableName+" where usuario.id<=((SELECT max(usuario.id) from "+ super.DbName+".usuario)-?) ORDER BY usuario.id DESC LIMIT ?) usuario2 on ( usuario2.id=usuario.id) ORDER BY usuario.id DESC");
			psConsultas=conn.prepareStatement(super.getQuerySelect());
			//psConsultas.setInt(1, limSupe);
			//psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Usuario un=new Usuario();
				un.setCodigo(res.getInt("id"));
				un.setUser(res.getString("usuario"));
				un.setNombre(res.getString("nombre_completo"));
				un.setPwd(res.getString("clave"));
				un.setTipoPermiso(res.getInt("tipo_permiso"));
				un.setPermiso(res.getString("permiso"));
				usuarios.add(un);
				
				
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
			return usuarios;
		}
		else return null;
		
	}
	
	
	
	public boolean setLogin(Usuario user) {
		
		Usuario unUsuario=new Usuario();
		ResultSet res=null;
		PreparedStatement buscarUser=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			buscarUser=conn.prepareStatement(super.getQuerySelect()+" WHERE usuario = ? AND clave = ?");
			buscarUser.setString(1, user.getUser());
			buscarUser.setString(2, user.getPwd());
			res = buscarUser.executeQuery();
			while(res.next()){
				existe=true;
				unUsuario.setNombre(res.getString("nombre_completo"));
				unUsuario.setUser(res.getString("usuario"));
				unUsuario.setPwd(res.getString("clave"));
				unUsuario.setPermiso(res.getString("permiso"));
				unUsuario.setTipoPermiso(res.getInt("tipo_permiso"));
				
				unUsuario.setCajas(cajasDao.getCajasUsuario(unUsuario));
				
				ConexionStatic.setUsuarioLogin(unUsuario);	
				
			 }
				
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try{
					if(res != null) res.close();
	                if(buscarUser != null)buscarUser.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
				excepcionSql.printStackTrace();
			//	conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			
		
		return existe;
			
		
		
		
	}
	
	



	
public List<Usuario> porNombre(String busqueda,int limitInferio, int canItemPag) {
		
		List<Usuario> usuarios =new ArrayList<Usuario>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("nombre_completo", "like") );
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Usuario un=new Usuario();
				un.setUser(res.getString("usuario"));
				un.setNombre(res.getString("nombre_completo"));
				un.setPwd(res.getString("clave"));
				un.setTipoPermiso(res.getInt("tipo_permiso"));
				un.setPermiso(res.getString("permiso"));
				usuarios.add(un);
				
				
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
			return usuarios;
		}
		else return null;
		
	}

	public List<Usuario> porUser(String busqueda,int limitInferio, int canItemPag) {
		
		List<Usuario> usuarios =new ArrayList<Usuario>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("usuario", "like") );
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				Usuario un=new Usuario();
				un.setUser(res.getString("usuario"));
				un.setNombre(res.getString("nombre_completo"));
				un.setPwd(res.getString("clave"));
				un.setTipoPermiso(res.getInt("tipo_permiso"));
				un.setPermiso(res.getString("permiso"));
				usuarios.add(un);
				
				
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
			return usuarios;
		}
		else return null;
		
	}

	
	/**

     * metodo para eliminar un usuario
     * 

     * @return falso o true segun el estado del accion

     */

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		Usuario myUsuario=(Usuario)c;
		int resultado=0;
		Connection conn=null;
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE usuario = ?");
				
				psConsultas.setString( 1, myUsuario.getUser() );
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
			//conexion.desconectar();
			} // fin de catch
		} // fin de finally
	}
	/**

     * metodo para guardar el usuario
     * 
     * @param c que se guardara

     * @return falso o true segun el estado del accion

     */

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		Usuario myUsuario=(Usuario)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		CajaDao cajaDao=new CajaDao();
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+"(usuario,nombre_completo,clave,permiso,tipo_permiso) VALUES (?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myUsuario.getUser() );
			psConsultas.setString( 2, myUsuario.getNombre()+" "+myUsuario.getApellido() );
			psConsultas.setString( 3, myUsuario.getPwd());
			psConsultas.setString(4, myUsuario.getPermiso());
			psConsultas.setInt(5,myUsuario.getTipoPermiso());
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
				
			}
			
		
			//se vuelven asignar las cajas que estan en la 
			for(int x=0;x<myUsuario.getCajas().size();x++){
				cajaDao.asignarCajas(myUsuario.getUser(),myUsuario.getCajas().get(x));
				//JOptionPane.showMessageDialog(null, myUsuario.getCajas().get(x).toString());
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
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		Usuario myUsuario=(Usuario)c;
		
		int resultado;
		Connection conn=null;
		CajaDao cajaDao=new CajaDao();
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET usuario = ?, nombre_completo = ?,clave=? ,permiso = ?, tipo_permiso=? WHERE usuario = ?");
			//nuevo=con.prepareStatement( "INSERT INTO usuario(usuario,nombre_completo,clave,permiso,tipo_permiso) VALUES (?,?,?,?,?)");
			psConsultas.setString( 1, myUsuario.getUser() );
			psConsultas.setString( 2, myUsuario.getNombre()+" "+myUsuario.getApellido() );
			psConsultas.setString( 3, myUsuario.getPwd());
			psConsultas.setString(4, myUsuario.getPermiso());
			psConsultas.setInt(5,myUsuario.getTipoPermiso());
			psConsultas.setString( 6, myUsuario.getUserOld() );
			resultado=psConsultas.executeUpdate();
			
			//JOptionPane.showMessageDialog(null, myUsuario.getCajas().size());
			
			//se desasignan las cajas del usuario
			cajaDao.desAsignarCaja(myUsuario);
			
			//se vuelven asignar las cajas que estan en la 
			for(int x=0;x<myUsuario.getCajas().size();x++){
				cajaDao.asignarCajas(myUsuario.getUser(),myUsuario.getCajas().get(x));
			}
			
			
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

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}
		

}
