package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDao extends ModeloDaoBasic {
	
	private int idClienteRegistrado;
	
	private String sqlBaseJoin=null;
	
	private EmpleadoDao empleadoDao=new EmpleadoDao();
	
	public ClienteDao(){
		
		super( "cliente","codigo_cliente");
		
		sqlBaseJoin="SELECT cliente.codigo_cliente, "
				+ " cliente.nombre_cliente, "
				+ " cliente.direccion, "
				+ " cliente.telefono, "
				+ " cliente.movil, "
				+ " cliente.rtn, "
				+ " cliente.limite_credito, "
				+ " cliente.id_vendedor, "
				+ " cliente.estado, "
				+ " cliente.tipo_cliente, "
				+ " ifnull("+super.DbName+ ".`f_saldo_cliente`(`cliente`.`codigo_cliente`),0) AS `saldo2`,  "
				+ " cliente.clasificacion "
				+ "FROM "+super.DbName+ ".cliente";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	public void setIdClienteRegistrado(int i){
		idClienteRegistrado=i;
	}
	public int getIdClienteRegistrado(){
		return idClienteRegistrado;
	} 
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los clientes>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public List<Cliente> todos(int cantItemPag,int limSupe){
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection conn = null;
        
        
        //Statement stmt = null;
       	List<Cliente> clientes=new ArrayList<Cliente>();
		
		ResultSet res=null;
		
		boolean existe=false;

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_vendedor>?";
		}else{
			whereBusqueda="id_vendedor=?";
		}

		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente", "="));
			
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			
			psConsultas.setInt(2, 2);
			psConsultas.setInt(3, limSupe);
			psConsultas.setInt(4, cantItemPag);
			res = psConsultas.executeQuery();
			while(res.next()){
				Cliente unCliente=new Cliente();
				existe=true;
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setDireccion(res.getString("direccion"));
				unCliente.setTelefono(res.getString("telefono"));
				unCliente.setCelular(res.getString("movil"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				unCliente.setLimiteCredito(res.getBigDecimal("limite_credito"));
				
				unCliente.setSaldoCuenta(res.getBigDecimal("saldo2"));
				
				Empleado unVendedor=empleadoDao.buscarPorId(res.getInt("id_vendedor"));
				unCliente.setVendedor(unVendedor);
				
				clientes.add(unCliente);
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
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		
			if (existe) {
				return clientes;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los cliente  por rtn>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Cliente> buscarPorRtn(String busqueda,int limitInferio, int canItemPag){
		List<Cliente> clientes=new ArrayList<Cliente>();
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_vendedor>?";
		}else{
			whereBusqueda="id_vendedor=?";
		}

		try {
			conn=ConexionStatic.getPoolConexion().getConnection();

			psConsultas=conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente=2 and rtn", "LIKE"));
			
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
		
			psConsultas.setString(2, "%" + busqueda + "%");
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Cliente unCliente=new Cliente();
				existe=true;
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setDireccion(res.getString("direccion"));
				unCliente.setTelefono(res.getString("telefono"));
				unCliente.setCelular(res.getString("movil"));
				unCliente.setRtn(res.getString("rtn"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setLimiteCredito(res.getBigDecimal("limite_credito"));
				unCliente.setSaldoCuenta(res.getBigDecimal("saldo2"));
				
				Empleado unVendedor=empleadoDao.buscarPorId(res.getInt("id_vendedor"));
				unCliente.setVendedor(unVendedor);
				
				clientes.add(unCliente);
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
			{
				try{
					if(res!=null)res.close();
					if(conn!=null)conn.close();
					if(psConsultas!=null)psConsultas.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return clientes;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los cliente  por nombre>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Cliente> buscarPorNombre(String busqueda,int limitInferio, int canItemPag){
		List<Cliente> clientes=new ArrayList<Cliente>();
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_vendedor>?";
		}else{
			whereBusqueda="id_vendedor=?";
		}
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente=2 and nombre_cliente", "LIKE"));
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			psConsultas.setString(2, "%" + busqueda + "%");
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre); Tampa fl, Houston  //// Washinton internal
			while(res.next()){
				Cliente unCliente=new Cliente();
				existe=true;
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setDireccion(res.getString("direccion"));
				unCliente.setTelefono(res.getString("telefono"));
				unCliente.setCelular(res.getString("movil"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));	
				
				Empleado unVendedor=empleadoDao.buscarPorId(res.getInt("id_vendedor"));
				unCliente.setVendedor(unVendedor);
				
				unCliente.setLimiteCredito(res.getBigDecimal("limite_credito"));
				unCliente.setSaldoCuenta(res.getBigDecimal("saldo2"));
				
				clientes.add(unCliente);
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
			{
				try{
					if(res!=null)res.close();
					if(conn!=null)conn.close();
					if(psConsultas!=null)psConsultas.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return clientes;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar clientes por id>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Cliente buscarPorId(int id){
		Cliente myCliente=new Cliente();
		//se crear un referencia al pool de conexiones
		
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
       
		
		ResultSet res=null;
		
		boolean existe=false;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
		
			psConsultas=con.prepareStatement(super.getQuerySearch("tipo_cliente=2 and codigo_cliente", "="));
			//psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res=psConsultas.executeQuery();
			while(res.next()){
				myCliente.setId(res.getInt("codigo_cliente"));
				myCliente.setNombre(res.getString("nombre_cliente"));
				myCliente.setTelefono(res.getString("telefono"));
				myCliente.setCelular(res.getString("movil"));
				myCliente.setTipoCliente(res.getInt("tipo_cliente"));
				
				Empleado unVendedor=empleadoDao.buscarPorId(res.getInt("id_vendedor"));
				myCliente.setVendedor(unVendedor);
				
				myCliente.setRtn(res.getString("rtn"));
				myCliente.setLimiteCredito(res.getBigDecimal("limite_credito"));
				myCliente.setSaldoCuenta(res.getBigDecimal("saldo2"));
				existe=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
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
				return myCliente;
		}
		else
			return null;
		
	
		
	}
	
	public BigDecimal getSaldoCliente(int idCliente) {
		// TODO Auto-generated method stub

		BigDecimal saldo=new BigDecimal(0);
		//se crear un referencia al pool de conexiones
		
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
       
		
		ResultSet res=null;
		
		boolean existe=false;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement("SELECT saldo FROM cuentas_por_cobrar where codigo_cliente=? ORDER BY codigo_reguistro DESC limit 1;");
			
			psConsultas.setInt(1, idCliente);
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

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar un cliente>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean eliminar(Object c){
		Cliente cliente=(Cliente)c;
		int resultado=0;
		Connection conn=null;
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_cliente = ?");
				
				psConsultas.setInt( 1, cliente.getId() );
				resultado=psConsultas.executeUpdate();
				
				return true;
			
			} catch (SQLException e) {
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
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Actualizar cliente>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean actualizar(Object c){
		
		Cliente cliente=(Cliente)c;
		int resultado;
		
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET nombre_cliente = ?, direccion = ? ,telefono = ?, movil=?, rtn=?,limite_credito=?,id_vendedor=? WHERE codigo_cliente = ?");
			psConsultas.setString(1,cliente.getNombre());
			psConsultas.setString(2, cliente.getDereccion());
			psConsultas.setString(3, cliente.getTelefono());
			psConsultas.setString(4, cliente.getCelular());
			psConsultas.setString(5,cliente.getRtn());
			psConsultas.setBigDecimal(6, cliente.getLimiteCredito());
			
			psConsultas.setInt(7, cliente.getVendedor().getCodigo());
			psConsultas.setInt(8,cliente.getId());
			
			
			resultado=psConsultas.executeUpdate();
			//JOptionPane.showMessageDialog(null, a+","+resultado );
			
			
			return true;
		
		} catch (SQLException e) {
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean registrarClienteContado(Cliente myCliente)
	{
		//JOptionPane.showConfirmDialog(null, myCliente);
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (nombre_cliente,direccion,telefono,movil,rtn,limite_credito) VALUES (?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myCliente.getNombre() );
			psConsultas.setString( 2, myCliente.getDereccion() );
			psConsultas.setString( 3, myCliente.getTelefono());
			psConsultas.setString(4, myCliente.getCelular());
			psConsultas.setString(5, myCliente.getRtn());
			psConsultas.setBigDecimal(6, myCliente.getLimiteCredito());
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdClienteRegistrado(rs.getInt(1));
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		Cliente myCliente=(Cliente)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			//insertarNuevaCliente=con.prepareStatement( "INSERT INTO cliente(nombre_cliente,direccion,telefono,movil,rtn) VALUES (?,?,?,?,?)");
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (nombre_cliente,direccion,telefono,movil,rtn,limite_credito,tipo_cliente,id_vendedor) VALUES (?,?,?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			
			psConsultas.setString( 1, myCliente.getNombre() );
			psConsultas.setString( 2, myCliente.getDereccion() );
			psConsultas.setString( 3, myCliente.getTelefono());
			psConsultas.setString(4, myCliente.getCelular());
			psConsultas.setString(5, myCliente.getRtn());
			psConsultas.setBigDecimal(6, myCliente.getLimiteCredito());
			psConsultas.setInt(7, 2);
			psConsultas.setInt(8, myCliente.getVendedor().getCodigo());
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdClienteRegistrado(rs.getInt(1));
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
	
}
