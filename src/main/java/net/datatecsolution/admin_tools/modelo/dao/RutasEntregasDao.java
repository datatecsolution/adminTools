package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class RutasEntregasDao extends ModeloDaoBasic{
	
	private int idCategoriaRegistrada;
	
	private String sqlBaseJoin=null;
	
	public RutasEntregasDao(){
		super("rutas_entregas","id_entrega");
		
		sqlBaseJoin="SELECT rutas_entregas.id_entrega, "
				+ " rutas_entregas.id_vendedor, "
				+ " rutas_entregas.fecha, "
				+ " rutas_entregas.estado, "
				+ " empleados.codigo_empleado, "
				+ " empleados.nombre, "
				+ " empleados.telefono, "
				+ " empleados.apellido  "
		+ " FROM "
			+ super.DbName+".rutas_entregas "
				+ "INNER JOIN "+super.DbName+".empleados "
						+ "ON (rutas_entregas.id_vendedor = empleados.codigo_empleado)";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
		
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
	public boolean eliminarFacturas(Object c){
		
		RutaEntrega ruta=(RutaEntrega)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement("DELETE FROM entregas_facturas WHERE id_entrega = ?");
			psConsultas.setInt( 1, ruta.getIdRuta() );
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eleminar marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo para eliminar rutas
	 * @param c objeto a eliminar
	 * @return true=se elemino correctamen o false=no se puedo eliminar
	 */
	@Override
	public boolean eliminar(Object c){
		RutaEntrega ruta=(RutaEntrega)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE id_entrega = ?");
			psConsultas.setInt( 1, ruta.getIdRuta() );
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
	 * Metodo para actualizar rutas de entregas
	 * @param c RutaEntrega
	 * @return true= se actualizo,  o false= no se pudo actualizar
	 */
	@Override
	public boolean actualizar(Object c){
		
		
		RutaEntrega ruta=(RutaEntrega)c;
		Connection conn=null;
		
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET id_vendedor = ?, fecha = ?,estado=? WHERE id_entrega = ?");
				psConsultas.setInt(1, ruta.getIdVendedor());
				psConsultas.setString(2, ruta.getFecha());
				
				psConsultas.setString(3, ruta.getEstado());
				
				psConsultas.setInt(4, ruta.getIdRuta());
				
				
				psConsultas.executeUpdate();
				
				this.eliminarFacturas(ruta);
				
				//se registran las facturas del pedido
				for(int x=0;x<ruta.getFacturas().size();x++){
					this.registraFacturaRuta(ruta.getIdRuta(), ruta.getFacturas().get(x));
				}
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
	 * @param canItemPag inferior para la paginacion
	 * @param limSupe superio para la paginacion
	 * @return Lista de categorias
	 */
	@Override
	public List<RutaEntrega> todos(int canItemPag,int limSupe){
		
		List<RutaEntrega> rutas=new ArrayList<RutaEntrega>();
		
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
				RutaEntrega unaRuta=new RutaEntrega();
				existe=true;
				unaRuta.setIdRuta(res.getInt("id_entrega"));
				unaRuta.setEstado(res.getString("estado"));
				
				try {
					SimpleDateFormat formato = new SimpleDateFormat("yyy-MM-dd");
					Date fechaDate = formato.parse(res.getString("fecha"));
					
					SimpleDateFormat formato2 = new SimpleDateFormat("dd-MM-yyy");
					String fecha=formato2.format(fechaDate);
					unaRuta.setFecha(fecha);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setApellido(res.getString("apellido"));
				empleado.setNombre((res.getString("nombre")));
				empleado.setTelefono((res.getString("telefono")));
				
				unaRuta.setVendedor(empleado);
				
				rutas.add(unaRuta);
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
				return rutas;
			}
			else return null;
			
		}
	
	
public List<RutaEntrega> todos(){
		
		List<RutaEntrega> rutas=new ArrayList<RutaEntrega>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			//System.out.println(super.getQuerySelect()+"<<<<===QUERY");
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(super.getQuerySelect());
			
	
			res = psConsultas.executeQuery();
			while(res.next()){
				RutaEntrega unaRuta=new RutaEntrega();
				existe=true;
				unaRuta.setIdRuta(res.getInt("id_entrega"));
				unaRuta.setEstado(res.getString("estado"));
				unaRuta.setFecha(res.getString("fecha"));
				
				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setApellido(res.getString("apellido"));
				empleado.setNombre((res.getString("nombre")));
				empleado.setTelefono((res.getString("telefono")));
				
				unaRuta.setVendedor(empleado);
				
				rutas.add(unaRuta);
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
				return rutas;
			}
			else return null;
			
		}
	
	
	/**
	 * @param i de la categoria registrada
	 */
	public void setIdRegistrada(int i){
		idCategoriaRegistrada=i;
	}
	/**
	 * @return id de la categoria registrada
	 */
	public int getIdRegistrada(){
		return idCategoriaRegistrada;
	} 
	
	public boolean registraFacturaRuta(int idRuta,Factura factura){
		
		ResultSet res=null;
		Connection conn=null;
		
		try 
		{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( "insert into entregas_facturas(id_entrega,id_factura,id_caja) VALUES (?,?,?)");
			psConsultas.setInt( 1,idRuta );
			psConsultas.setInt( 2, factura.getIdFactura());
			psConsultas.setInt( 3, factura.getCodigoCaja() );
		
			psConsultas.executeUpdate();
			
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
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	/**
	 * Metodo paa agregar categorias
	 * @param c objeto rutasEntregas
	 * @return true=se registro o false=no se registro
	 */
	@Override
	public boolean registrar(Object c)
	{
		RutaEntrega myRuta=(RutaEntrega)c;
		int resultado=0;
		ResultSet res=null;
		Connection conn=null;
		
		try 
		{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( super.getQueryInsert()+"(id_vendedor,fecha,estado) VALUES (?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setInt( 1, myRuta.getIdVendedor());
			psConsultas.setString( 2, myRuta.getFecha() );
			psConsultas.setString( 3, myRuta.getEstado() );
			
			resultado=psConsultas.executeUpdate();
			
			res=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(res.next()){
				this.setIdRegistrada(res.getInt(1));	
			}
			myRuta.setIdRuta(getIdRegistrada());
			
			//se registran las facturas del pedido
			for(int x=0;x<myRuta.getFacturas().size();x++){
				this.registraFacturaRuta(getIdRegistrada(), myRuta.getFacturas().get(x));
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
	 * Metodo para buscar la rutas de entregas por id
	 * @param i de la categoria
	 * @return categoria entrada sino null
	 */
	public RutaEntrega buscarPorId(int i){
		
		RutaEntrega unaRuta=new RutaEntrega();
		
		ResultSet res=null;
		
		boolean existe=false;
		
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(super.getQuerySearch("id_entrega", "="));
			psConsultas.setInt(1, i);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unaRuta.setIdRuta(res.getInt("id_entrega"));
				unaRuta.setEstado(res.getString("estado"));
				unaRuta.setFecha(res.getString("fecha"));
				
				Empleado empleado=new Empleado();
				empleado.setCodigo(res.getInt("codigo_empleado"));
				empleado.setApellido(res.getString("apellido"));
				empleado.setNombre((res.getString("nombre")));
				empleado.setTelefono((res.getString("telefono")));
				
				unaRuta.setVendedor(empleado);
				
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
				return unaRuta;
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
	public List<Categoria> buscarPorObservacion(String busqueda,int limite, int cantidad){
		List<Categoria> categorias=new ArrayList<Categoria>();
		ResultSet res=null;
		 
		boolean existe=false;
		
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("observacion", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limite);
			psConsultas.setInt(3, cantidad);
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
	public List<Categoria> buscarPorNombre(String busqueda,int limite, int cantidad){
		List<Categoria> categorias=new ArrayList<Categoria>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("descripcion", "LIKE"));
			psConsultas.setString(1, "%" + busqueda + "%");
			psConsultas.setInt(2, limite);
			psConsultas.setInt(3, cantidad);
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

	public void getFacturas(RutaEntrega unRuta) {
		// TODO Auto-generated method stub
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement("select id_factura,id_caja from entregas_facturas where id_entrega=? order by id_factura ");
			psConsultas.setInt(1, unRuta.getIdRuta());
			res = psConsultas.executeQuery();
			//System.out.println(psConsultas);
			while(res.next()){
				Factura unaFactura=new Factura();
				unaFactura.setIdFactura(res.getInt("id_factura"));
				unaFactura.setCodigoCaja(res.getInt("id_caja"));
				unRuta.getFacturas().add(unaFactura);
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
		}

	public boolean verificarExistenciaEnRuta(Factura myFactura, Caja caja) {
		// TODO Auto-generated method stub

				
		        Connection con = null;
		        
		    	String sql="SELECT * FROM entregas_facturas WHERE	id_factura = ? AND id_caja = ? ";
				
				ResultSet res=null;
				
				boolean existe=false;
				try {
					con = ConexionStatic.getPoolConexion().getConnection();
					
					
					psConsultas = con.prepareStatement(sql);
					psConsultas.setInt(1, myFactura.getIdFactura());
					
					psConsultas.setInt(2,caja.getCodigo());
					
					res = psConsultas.executeQuery();
					while(res.next()){
						
						existe=true;
						
					
					 }
							
					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
		return existe;
	
	}
	
	
}
