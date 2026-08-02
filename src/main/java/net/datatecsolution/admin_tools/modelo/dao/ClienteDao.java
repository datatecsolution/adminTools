package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.MovimientoCartera;
import net.datatecsolution.admin_tools.modelo.RutaCobro;

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
	
	private final EmpleadoDao empleadoDao=new EmpleadoDao();

	private final RutaCobroDao rutaCobroDao=new RutaCobroDao();
	
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
				+ " cliente.id_cobrador, "
				+ " cliente.id_ruta_cobro, "
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

		if(ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_cobrador>=?";
		}else{
			whereBusqueda="id_cobrador=?";
		}

		try {
			conn = ConexionStatic.getPoolConexion().getConnection();

			psConsultas = conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente", "="));

			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo());

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

				Empleado unCobrador=empleadoDao.buscarPorId(res.getInt("id_cobrador"));
				unCliente.setCobrador(unCobrador);

				RutaCobro unaRuta=rutaCobroDao.buscarPorId(res.getInt("id_ruta_cobro"));
				unCliente.setRutaCobro(unaRuta);

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

		if(ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_cobrador>=?";
		}else{
			whereBusqueda="id_cobrador=?";
		}

		try {
			conn=ConexionStatic.getPoolConexion().getConnection();

			psConsultas=conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente=2 and rtn", "LIKE"));

			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo());

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

				Empleado unCobrador=empleadoDao.buscarPorId(res.getInt("id_cobrador"));
				unCliente.setCobrador(unCobrador);

				RutaCobro unaRuta=rutaCobroDao.buscarPorId(res.getInt("id_ruta_cobro"));
				unCliente.setRutaCobro(unaRuta);
				
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
	public List<Cliente> buscarPorNombreTodosLosCobradores(String busqueda,int limitInferio, int canItemPag){
		List<Cliente> clientes=new ArrayList<Cliente>();

		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch(" tipo_cliente=2 and nombre_cliente", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
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

				Empleado unCobrador=empleadoDao.buscarPorId(res.getInt("id_cobrador"));
				unCliente.setCobrador(unCobrador);

				RutaCobro unaRuta=rutaCobroDao.buscarPorId(res.getInt("id_ruta_cobro"));
				unCliente.setRutaCobro(unaRuta);

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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los cliente  por nombre>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Cliente> buscarPorNombre(String busqueda,int limitInferio, int canItemPag){
		List<Cliente> clientes=new ArrayList<Cliente>();
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;

		String whereBusqueda="";

		if(ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo()==0){
			whereBusqueda="id_cobrador>=?";
		}else{
			whereBusqueda="id_cobrador=?";
		}
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch(whereBusqueda+" and tipo_cliente=2 and nombre_cliente", "LIKE"));
			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo());
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

				Empleado unCobrador=empleadoDao.buscarPorId(res.getInt("id_cobrador"));
				unCliente.setCobrador(unCobrador);

				RutaCobro unaRuta=rutaCobroDao.buscarPorId(res.getInt("id_ruta_cobro"));
				unCliente.setRutaCobro(unaRuta);

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
		
			String filtroCobrador;
			int valorCobrador = ConexionStatic.getUsuarioLogin().getConfig().getCobradorEnBusqueda().getCodigo();
			if (valorCobrador == 0) {
				filtroCobrador = "id_cobrador>=?";
			} else {
				filtroCobrador = "id_cobrador=?";
			}
			psConsultas=con.prepareStatement(super.getQuerySearch("tipo_cliente=2 and "+filtroCobrador+" and codigo_cliente", "="));
			psConsultas.setInt(1, valorCobrador);
			psConsultas.setInt(2, id);
			psConsultas.setInt(3, 0);
			psConsultas.setInt(4, 1);
			res=psConsultas.executeQuery();
			while(res.next()){
				myCliente.setId(res.getInt("codigo_cliente"));
				myCliente.setNombre(res.getString("nombre_cliente"));
				myCliente.setTelefono(res.getString("telefono"));
				myCliente.setCelular(res.getString("movil"));
				myCliente.setTipoCliente(res.getInt("tipo_cliente"));



				myCliente.setDireccion(res.getString("direccion"));


				myCliente.setRtn(res.getString("rtn"));

				Empleado unVendedor=empleadoDao.buscarPorId(res.getInt("id_vendedor"));
				myCliente.setVendedor(unVendedor);

				Empleado unCobrador=empleadoDao.buscarPorId(res.getInt("id_cobrador"));
				myCliente.setCobrador(unCobrador);

				RutaCobro unaRuta=rutaCobroDao.buscarPorId(res.getInt("id_ruta_cobro"));
				myCliente.setRutaCobro(unaRuta);
				
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
			
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET nombre_cliente = ?, direccion = ? ,telefono = ?, movil=?, rtn=?,limite_credito=?,id_cobrador=?, id_vendedor=?, id_ruta_cobro=? WHERE codigo_cliente = ?");
			psConsultas.setString(1,cliente.getNombre());
			psConsultas.setString(2, cliente.getDereccion());
			psConsultas.setString(3, cliente.getTelefono());
			psConsultas.setString(4, cliente.getCelular());
			psConsultas.setString(5,cliente.getRtn());
			psConsultas.setBigDecimal(6, cliente.getLimiteCredito());

			int codigoCobrador = cliente.getCobrador() != null ? cliente.getCobrador().getCodigo() : 0;
			psConsultas.setInt(7, codigoCobrador);
			int codigoVendedor = cliente.getVendedor() != null ? cliente.getVendedor().getCodigo() : 0;
			psConsultas.setInt(8, codigoVendedor);
			psConsultas.setInt(9, cliente.getRutaCobro().getCodigo());
			psConsultas.setInt(10,cliente.getId());
			
			
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
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (nombre_cliente,direccion,telefono,movil,rtn,limite_credito,tipo_cliente,id_cobrador,id_vendedor,id_ruta_cobro) VALUES (?,?,?,?,?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);


			psConsultas.setString( 1, myCliente.getNombre() );
			psConsultas.setString( 2, myCliente.getDereccion() );
			psConsultas.setString( 3, myCliente.getTelefono());
			psConsultas.setString(4, myCliente.getCelular());
			psConsultas.setString(5, myCliente.getRtn());
			psConsultas.setBigDecimal(6, myCliente.getLimiteCredito());
			psConsultas.setInt(7, 2);
			int codigoCobradorIns = myCliente.getCobrador() != null ? myCliente.getCobrador().getCodigo() : 0;
			psConsultas.setInt(8, codigoCobradorIns);
			int codigoVendedorIns = myCliente.getVendedor() != null ? myCliente.getVendedor().getCodigo() : 0;
			psConsultas.setInt(9, codigoVendedorIns);
			psConsultas.setInt(10, myCliente.getRutaCobro().getCodigo());
			
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

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< US-127: transferencia de cartera entre vendedores >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

	/**
	 * US-127 — devuelve los clientes candidatos a la transferencia: la UNION
	 * de los que vende {@code origenVenta} y los que cobra {@code origenCobro}.
	 *
	 * Los dos codigos pueden ser empleados DISTINTOS (o el mismo), porque la
	 * venta y la cobranza se transfieren por separado. Pasar 0 en cualquiera
	 * de los dos significa "ese rol no se esta moviendo": esa condicion NO se
	 * agrega al WHERE.
	 *
	 * OJO con el 0: `id_cobrador` es NOT NULL DEFAULT 0 (V9), asi que TODOS
	 * los clientes de contado lo tienen en 0. Por eso el rol apagado se
	 * resuelve omitiendo la condicion y no pasando 0 como comodin — eso
	 * traeria el padron entero.
	 *
	 * Consulta plana a proposito. Los otros listados de este DAO resuelven
	 * vendedor, cobrador y ruta con un buscarPorId() por fila (N+1); para una
	 * cartera entera eso son miles de round-trips. Aca solo se traen las
	 * columnas que la pantalla de transferencia necesita mostrar.
	 *
	 * @param origenVenta empleado cuya asignacion de venta se mueve, o 0
	 * @param origenCobro empleado cuya cartera de cobro se mueve, o 0
	 * @return los clientes ordenados por nombre, o lista vacia
	 */
	public List<ClienteCartera> carteraParaTransferencia(int origenVenta, int origenCobro) {

		List<ClienteCartera> clientes = new ArrayList<ClienteCartera>();
		if (origenVenta <= 0 && origenCobro <= 0) {
			return clientes;
		}

		Connection con = null;
		ResultSet rs = null;

		StringBuilder where = new StringBuilder();
		if (origenVenta > 0) {
			where.append("id_vendedor = ?");
		}
		if (origenCobro > 0) {
			if (where.length() > 0) where.append(" OR ");
			where.append("id_cobrador = ?");
		}

		String sql = "SELECT codigo_cliente, nombre_cliente, rtn, tipo_cliente, id_vendedor, id_cobrador, "
				+ " ifnull(" + super.DbName + ".`f_saldo_cliente`(`codigo_cliente`),0) AS saldo "
				+ "FROM " + super.DbName + ".cliente "
				+ "WHERE " + where + " "
				+ "ORDER BY nombre_cliente";

		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			psConsultas = con.prepareStatement(sql);
			int idx = 1;
			if (origenVenta > 0) {
				psConsultas.setInt(idx++, origenVenta);
			}
			if (origenCobro > 0) {
				psConsultas.setInt(idx, origenCobro);
			}
			rs = psConsultas.executeQuery();

			while (rs.next()) {
				ClienteCartera c = new ClienteCartera();
				c.setCodigo(rs.getInt("codigo_cliente"));
				c.setNombre(rs.getString("nombre_cliente"));
				c.setRtn(rs.getString("rtn"));
				c.setTipoCliente(rs.getInt("tipo_cliente"));
				c.setIdVendedor(rs.getInt("id_vendedor"));
				c.setIdCobrador(rs.getInt("id_cobrador"));
				c.setSaldo(rs.getBigDecimal("saldo"));
				clientes.add(c);
			}
			return clientes;

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
			return clientes;
		} finally {
			try {
				if (rs != null) rs.close();
				if (psConsultas != null) psConsultas.close();
				if (con != null) con.close();
			} catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
			}
		}
	}

	/** Clientes por sentencia en la transferencia de cartera (ver transferirCartera). */
	private static final int LOTE_TRANSFERENCIA = 500;

	/**
	 * US-127 — mueve la cartera de forma ATOMICA, tratando la venta y la
	 * cobranza como movimientos INDEPENDIENTES.
	 *
	 * Cada rol tiene su propio origen y su propio destino porque V9 los separo
	 * justamente para que puedan ser personas distintas: un cliente que vende
	 * Ana y cobra Beto puede pasar su venta a Carlos y su cobranza a Dora en
	 * la misma corrida.
	 *
	 * Los dos UPDATE van en UNA transaccion: o se mueve todo el lote o no se
	 * mueve nada. Sin esto, un fallo entre ambos dejaria clientes con el
	 * vendedor nuevo y el cobrador viejo, un estado que nadie puede deshacer a
	 * mano sin conocer el corte.
	 *
	 * Los codigos se agrupan de a {@value #LOTE_TRANSFERENCIA} en un IN en vez
	 * de mandar un UPDATE por cliente. No es una micro-optimizacion: en los
	 * clientes reales medidos (Ferro 29.396, Wendy 44.244) casi TODO el padron
	 * cuelga del id_vendedor=1 —el DEFAULT del baseline, o sea clientes que
	 * nunca se asignaron—, asi que una sola transferencia puede ser de 44.000
	 * clientes. Fila por fila serian 44.000 viajes al servidor manteniendo la
	 * transaccion abierta; de a 500 son 89 sentencias.
	 *
	 * Cada UPDATE lleva {@code AND id_... = origen} ademas de los codigos: eso
	 * hace la operacion idempotente y garantiza que no se pise la asignacion
	 * de un tercer empleado si la pantalla quedo desactualizada.
	 *
	 * @param codigosVenta clientes que cambian de vendedor (vacio = no mover venta)
	 * @param venta        movimiento de la asignacion de venta
	 * @param codigosCobro clientes que cambian de cobrador (vacio = no mover cobro)
	 * @param cobro        movimiento de la cartera de cobro
	 * @return true si la transaccion se confirmo
	 */
	public boolean transferirCartera(List<Integer> codigosVenta, MovimientoCartera venta,
			List<Integer> codigosCobro, MovimientoCartera cobro) {

		boolean hayVenta = venta != null && venta.esUtilizable()
				&& codigosVenta != null && !codigosVenta.isEmpty();
		boolean hayCobro = cobro != null && cobro.esUtilizable()
				&& codigosCobro != null && !codigosCobro.isEmpty();

		if (!hayVenta && !hayCobro) {
			return false;
		}

		boolean resultado = false;
		Connection conn = null;

		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			conn.setAutoCommit(false);

			if (hayVenta) {
				actualizarEnLotes(conn, "id_vendedor", codigosVenta, venta.getOrigen(), venta.getDestino());
			}

			if (hayCobro) {
				actualizarEnLotes(conn, "id_cobrador", codigosCobro, cobro.getOrigen(), cobro.getDestino());
			}

			conn.commit();
			resultado = true;
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
			}
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
			resultado = false;
		} finally {
			try {
				if (conn != null) { conn.setAutoCommit(true); conn.close(); }
			} catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
			}
		}
		return resultado;
	}

	/**
	 * Reasigna un campo de empleado en lotes de {@value #LOTE_TRANSFERENCIA}
	 * clientes, dentro de la conexion/transaccion que le pasen.
	 *
	 * El nombre del campo se concatena porque un placeholder no puede ocupar
	 * el lugar de una columna; los unicos valores posibles son las constantes
	 * "id_vendedor" e "id_cobrador" que pasa transferirCartera, nunca entrada
	 * del usuario. Los codigos de cliente si van parametrizados.
	 */
	private void actualizarEnLotes(Connection conn, String campo, List<Integer> codigos,
			int origen, int destino) throws SQLException {

		for (int inicio = 0; inicio < codigos.size(); inicio += LOTE_TRANSFERENCIA) {
			List<Integer> lote = codigos.subList(inicio,
					Math.min(inicio + LOTE_TRANSFERENCIA, codigos.size()));

			StringBuilder sql = new StringBuilder(super.getQueryUpdate())
					.append(" SET ").append(campo).append(" = ? ")
					.append("WHERE ").append(campo).append(" = ? AND codigo_cliente IN (");
			for (int i = 0; i < lote.size(); i++) {
				sql.append(i == 0 ? "?" : ",?");
			}
			sql.append(")");

			try (java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
				ps.setInt(1, destino);
				ps.setInt(2, origen);
				for (int i = 0; i < lote.size(); i++) {
					ps.setInt(3 + i, lote.get(i));
				}
				ps.executeUpdate();
			}
		}
	}

}
