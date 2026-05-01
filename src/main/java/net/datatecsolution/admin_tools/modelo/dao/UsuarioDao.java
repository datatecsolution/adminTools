package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.config.PasswordHasher;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
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
	
	private final CajaDao cajasDao;
	
	public UsuarioDao(){
		super( "usuario","id");
		cajasDao=new CajaDao();
	}
	
	public boolean comprobarAdmin(String pwd){

		boolean resultado=false;

        Connection con = null;

    	String sql=super.getQuerySelect()+" where (tipo_permiso=1 or tipo_permiso=4)";

		ResultSet res=null;

		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			psConsultas = con.prepareStatement(sql);
			res = psConsultas.executeQuery();
			while(res.next()){
				String claveAlmacenada = res.getString("clave");
				if (PasswordHasher.verify(pwd, claveAlmacenada)) {
					resultado = true;
					if (!PasswordHasher.isHashed(claveAlmacenada)) {
						migrarPassword(con, res.getString("usuario"), claveAlmacenada, pwd);
					}
					break;
				}
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
			buscarUser=conn.prepareStatement(super.getQuerySelect()+" WHERE usuario = ?");
			buscarUser.setString(1, user.getUser());
			res = buscarUser.executeQuery();
			while(res.next()){
				String claveAlmacenada = res.getString("clave");

				if (!PasswordHasher.verify(user.getPwd(), claveAlmacenada)) {
					continue;
				}

				existe=true;
				unUsuario.setNombre(res.getString("nombre_completo"));
				unUsuario.setUser(res.getString("usuario"));
				unUsuario.setPwd(claveAlmacenada);
				unUsuario.setPermiso(res.getString("permiso"));
				unUsuario.setTipoPermiso(res.getInt("tipo_permiso"));

				unUsuario.setCajas(cajasDao.getCajasUsuario(unUsuario));

				ConexionStatic.setUsuarioLogin(unUsuario);

				if (!PasswordHasher.isHashed(claveAlmacenada)) {
					migrarPassword(conn, user.getUser(), claveAlmacenada, user.getPwd());
				}
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
			String pwdToStore = PasswordHasher.isHashed(myUsuario.getPwd())
				? myUsuario.getPwd()
				: PasswordHasher.hash(myUsuario.getPwd());
			psConsultas.setString( 3, pwdToStore);
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

			persistirAsignaciones(myUsuario);

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
			psConsultas.setString( 1, myUsuario.getUser() );
			psConsultas.setString( 2, myUsuario.getNombre()+" "+myUsuario.getApellido() );
			String pwdToStore = PasswordHasher.isHashed(myUsuario.getPwd())
				? myUsuario.getPwd()
				: PasswordHasher.hash(myUsuario.getPwd());
			psConsultas.setString( 3, pwdToStore);
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

			if (myUsuario.getUserOld() != null && !myUsuario.getUserOld().equals(myUsuario.getUser())) {
				new EmpleadoDao().desasignarUsuariosDe(myUsuario.getUserOld());
				new UsuarioPrecioDao().desasignarPrecios(myUsuario.getUserOld());
			}
			persistirAsignaciones(myUsuario);

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

	private void persistirAsignaciones(Usuario u) {
		EmpleadoDao empleadoDao = new EmpleadoDao();
		UsuarioPrecioDao precioDao = new UsuarioPrecioDao();

		empleadoDao.desasignarUsuariosDe(u.getUser());
		precioDao.desasignarPrecios(u.getUser());

		boolean esMovil = u.getCodigoEmpleado() > 0;

		if (esMovil) {
			actualizarCodigoEmpleado(u.getUser(), u.getCodigoEmpleado());
			empleadoDao.asignarUsuario(u.getUser(), u.getCodigoEmpleado());
			if (u.getPreciosAsignados() != null) {
				for (PrecioArticulo p : u.getPreciosAsignados()) {
					precioDao.asignarPrecio(u.getUser(), p.getCodigoPrecio());
				}
			}
		} else {
			actualizarCodigoEmpleado(u.getUser(), 0);
			if (u.getVendedoresAsignados() != null) {
				for (Empleado e : u.getVendedoresAsignados()) {
					empleadoDao.asignarUsuario(u.getUser(), e.getCodigo());
				}
			}
		}
	}

	private void actualizarCodigoEmpleado(String usuario, int codigoEmpleado) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			ps = conn.prepareStatement("UPDATE " + DbName + ".usuario SET codigo_empleado = ? WHERE usuario = ?");
			if (codigoEmpleado > 0) {
				ps.setInt(1, codigoEmpleado);
			} else {
				ps.setNull(1, java.sql.Types.INTEGER);
			}
			ps.setString(2, usuario);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) ps.close();
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void cargarAsignaciones(Usuario u) {
		u.setVendedoresAsignados(new EmpleadoDao().getEmpleadosAsignadosA(u.getUser()));
		u.setPreciosAsignados(new UsuarioPrecioDao().getPreciosPorUsuario(u.getUser()));
		u.setCodigoEmpleado(consultarCodigoEmpleado(u.getUser()));
	}

	private int consultarCodigoEmpleado(String usuario) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		int codigo = 0;
		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			ps = conn.prepareStatement("SELECT codigo_empleado FROM " + DbName + ".usuario WHERE usuario = ?");
			ps.setString(1, usuario);
			res = ps.executeQuery();
			if (res.next()) {
				codigo = res.getInt("codigo_empleado");
				if (res.wasNull()) codigo = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (res != null) res.close();
				if (ps != null) ps.close();
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return codigo;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	private void migrarPassword(Connection conn, String usuario, String claveActual, String passwordPlano) {
		PreparedStatement ps = null;
		try {
			String hashed = PasswordHasher.hash(passwordPlano);
			ps = conn.prepareStatement("UPDATE usuario SET clave = ? WHERE usuario = ? AND clave = ?");
			ps.setString(1, hashed);
			ps.setString(2, usuario);
			ps.setString(3, claveActual);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
