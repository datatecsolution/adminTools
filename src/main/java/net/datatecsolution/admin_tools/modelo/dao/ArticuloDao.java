package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Departamento;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticuloDao extends ModeloDaoBasic implements Runnable {
	

	private int idArticuloRegistrado;
	
	private Departamento myBodega=new Departamento() ;
	
	private double existenciaAler=0;

	private CodBarraDao myCodBarraDao=null;
	
	private PrecioArticuloDao preciosDao=null;
	
	private String sqlBaseJoin=null;
	private String sqlBaseJoin2=null;
	
	
	public ArticuloDao(){
		
		super("articulo","codigo_articulo");
		
		//se activa la opcion de los registros activos y inactivos 
		super.estado=1;
		
		//se crea el query para la consulta de los articulos
		sqlBaseJoin="select articulo.codigo_articulo, "
							+ "articulo.articulo, "
							+ "articulo.cod_articulo, "
							+" articulo.tipo_articulo, "
							+" articulo.estado, "
							+" articulo.medida, "
							+" marcas.codigo_marca, "
							+" marcas.descripcion AS marca, "
							+" impuesto.codigo_impuesto, "
							+" impuesto.porcentaje AS impuesto, "
							+" impuesto.descripcion_impuesto, "
							+ super.DbName+ ".f_can_saldo_kardex("+ super.DbName+".articulo.codigo_articulo,?) AS existencia,  "
							+ " precios_articulos.precio_articulo"
					+ " from "
							+super.DbName+".articulo"
									+ " join "+ super.DbName+".marcas "
											+ " on(marcas.codigo_marca =articulo.codigo_marca) "
									+ " join "+super.DbName+ ".impuesto "
											+ " on(impuesto.codigo_impuesto =articulo.codigo_impuesto) "
									+ " join "+ super.DbName+".precios_articulos "
											+ " on(precios_articulos.codigo_articulo =articulo.codigo_articulo and precios_articulos.codigo_precio = 1) ";
		
		super.setSqlQuerySelectJoin(sqlBaseJoin);
		
		//se crea el query para la consulta de los articulos con codigo de barra W&cSjp123. jdmm->Jdmm1113?
		sqlBaseJoin2="select articulo.codigo_articulo, "
							+" articulo.articulo, "
							+" articulo.cod_articulo, "
							+" articulo.tipo_articulo, "
							+" articulo.estado, "
							+" marcas.codigo_marca, "
							+" articulo.medida, "
							+" marcas.descripcion AS marca, "
							+" impuesto.codigo_impuesto, "
							+" impuesto.porcentaje AS impuesto, "
							+" impuesto.descripcion_impuesto, "
							+ super.DbName+ ".f_can_saldo_kardex("+ super.DbName+".articulo.codigo_articulo,?) AS existencia,  "
							+" codigos_articulos.codigo_barra,"
							+" precios_articulos.precio_articulo  "
					+ " from "
							+super.DbName+".articulo "
									+ " join "+ super.DbName+".marcas "
											+ " on(marcas.codigo_marca =articulo.codigo_marca) " 
									+ "join " +super.DbName+ ".impuesto "
											+ " on(impuesto.codigo_impuesto =articulo.codigo_impuesto) "
									+ "join "+ super.DbName+".precios_articulos  "
											+ " on(precios_articulos.codigo_articulo =articulo.codigo_articulo and precios_articulos.codigo_precio = 1) "
									+ "JOIN "+ super.DbName+".codigos_articulos "
											+ " ON(articulo.codigo_articulo =codigos_articulos.codigo_articulo) ";
		
		myCodBarraDao=new CodBarraDao();
		preciosDao=new PrecioArticuloDao();
		
	}
	
	public double getAlertaExistencia(){
		
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		double existencia=0;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement("SELECT COUNT(cod) as cantidad from v_existencia_alert;");
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				existencia=res.getDouble("cantidad");
				
			 }
			//JOptionPane.showMessageDialog(null, unArticulo);		
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
			finally
			{
				try{
					if(res!=null)res.close();
					if(psConsultas != null)psConsultas.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return existencia;
			}
			else{

				return existencia;
			}
		
	}
	
	

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los articulos por marcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Articulo> buscarArticuloMarca(String busqueda, int limitInferior, int canItemPag){
		List<Articulo> articulos=new ArrayList<Articulo>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			//psConsultas=conn.prepareStatement(super.getQuerySelect()+"join (select codigo_articulo from  "+super.DbName+"."+super.tableName+" join "+super.DbName+".marcas on (articulo.codigo_marca=marcas.codigo_marca)  where descripcion LIKE ? ORDER BY articulo.codigo_articulo DESC limit ? ,?) articulo2 on( articulo2.codigo_articulo= `articulo`.`codigo_articulo`) ORDER BY articulo.codigo_articulo DESC ");
			psConsultas=conn.prepareStatement(super.getQuerySearchJoin("descripcion", "LIKE", "marcas", "codigo_marca", "codigo_marca"));
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setString(2, "%" + busqueda + "%");
			if(estado!=2){
				psConsultas.setInt(3, estado);
				psConsultas.setInt(4, limitInferior);
				psConsultas.setInt(5, canItemPag);
			}
			else{
				psConsultas.setInt(3, limitInferior);
				psConsultas.setInt(4, canItemPag);
			}
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Articulo unArticulo=new Articulo();
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				unArticulo.setEstado(res.getBoolean("estado"));
				unArticulo.setMedida(res.getInt("medida"));
				articulos.add(unArticulo);
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}finally
			{
				try{
					if(res!=null)res.close();
					if(psConsultas!=null)psConsultas.close();
					if(conn!=null)conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return articulos;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para busca los articulo  por nombre>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Articulo> buscarArticulo(String busqueda,int limitInferio, int canItemPag){
		List<Articulo> articulos=new ArrayList<Articulo>();
	
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		
		//String sql= super.getQuerySelect()+" join (select codigo_articulo from "+super.DbName+"."+super.tableName+" where articulo LIKE ? ORDER BY articulo.codigo_articulo desc LIMIT ?,?) articulo2 on( articulo2.codigo_articulo= `articulo`.`codigo_articulo`) ORDER BY articulo.codigo_articulo DESC";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			
			psConsultas=conn.prepareStatement(super.getQuerySearch("articulo", "like"));
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setString(2, "%" + busqueda + "%");
			if(estado!=2){
				psConsultas.setInt(3, estado);
				psConsultas.setInt(4, limitInferio);
				psConsultas.setInt(5, canItemPag);
			}else{
				psConsultas.setInt(3, limitInferio);
				psConsultas.setInt(4, canItemPag);
			}
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			//System.out.println(buscarProveedorNombre);
			while(res.next()){
				Articulo unArticulo=new Articulo();
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				//unArticulo.setExistencia(this.getExistencia(unArticulo.getId(), 1));
				//se estable los codigos de bara encontrados al objeto myArticulo;
				unArticulo.setCodBarras(myCodBarraDao.getCodsArticulo(unArticulo.getId()));
				unArticulo.setEstado(res.getBoolean("estado"));
				unArticulo.setMedida(res.getInt("medida"));
				articulos.add(unArticulo);
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
				return articulos;
			}
			else return null;
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo por por ID pool>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public Articulo buscarPorId(int i){
		Articulo unArticulo=new Articulo();
		ResultSet res=null;
		//PreparedStatement buscarArt=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("codigo_articulo", "="));
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setInt(2, i);
			psConsultas.setInt(3, 0);
			psConsultas.setInt(4, 1);
			
			if(estado!=2){
				psConsultas.setInt(3, estado);
				psConsultas.setInt(4, 0);
				psConsultas.setInt(5, 1);
			}else{
				psConsultas.setInt(3, 0);
				psConsultas.setInt(4, 1);
			}
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				unArticulo.setMedida(res.getInt("medida"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
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
				return unArticulo;
			}
			else return null;
		
		
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo por por ID pool>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Articulo buscarArticuloNombre(String i){
		Articulo unArticulo=new Articulo();
		ResultSet res=null;
		//PreparedStatement buscarArt=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where v_articulos.articulo LIKE ? LIMIT 1");
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setString(2,  i + "%");
			res = psConsultas.executeQuery();
			
			while(res.next())
			//for(int x=0;x>)
			{
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				unArticulo.setMedida(res.getInt("medida"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
				
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
				return unArticulo;
			}
			else return null;
		
		
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo por por ID pool>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Articulo buscarArticuloNombre(String i, int posicion){
		Articulo unArticulo=new Articulo();
		ResultSet res=null;
		//PreparedStatement buscarArt=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where articulo LIKE ? LIMIT "+posicion+","+posicion);
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setString(2,  i + "%");
			res = psConsultas.executeQuery();
			
			while(res.next())
			//for(int x=0;x>)
			{
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				unArticulo.setMedida(res.getInt("medida"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
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
				return unArticulo;
			}
			else return null;
		
		
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo por por ID>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Articulo buscarArticulo(int i){
		Articulo unArticulo=new Articulo();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("codigo_articulo", "="));
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setInt(2, i);
			psConsultas.setInt(3, 0);
			psConsultas.setInt(4, 1);
			
			System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				unArticulo.setMedida(res.getInt("medida"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
			 }
			//JOptionPane.showMessageDialog(null, unArticulo);		
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
			finally
			{
				try{
					if(res!=null)res.close();
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
				return unArticulo;
			}
			else{
				
				/*unArticulo=buscarArticuloBarraCod(i+"");
				if(unArticulo!=null){
					return unArticulo;
				}else*/
					return null;
			}
		
		
		
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo por codigo de barra>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Articulo buscarArticuloBarraCod(String i){
		//se cambia el query al segundo donde incluye la tabla de codigos
		super.setSqlQuerySelectJoin(sqlBaseJoin2);
		Articulo unArticulo=new Articulo();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement(super.sqlQuerySelectJoin+" WHERE codigo_barra = ?");
			psConsultas=conn.prepareStatement(super.getQuerySearchJoin("codigo_barra", "=", "codigos_articulos", "codigo_articulo", "codigo_articulo"));
			psConsultas.setInt(1, myBodega.getId());
			psConsultas.setString(2, i);
			if(estado!=2){
				psConsultas.setInt(3, estado);
				psConsultas.setInt(4, 0);
				psConsultas.setInt(5, 1);
				
			}
			else{
				psConsultas.setInt(3, 0);
				psConsultas.setInt(4, 1);
			}
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				
				unArticulo.setCodigoBarra(res.getString("codigo_barra"));
				
				unArticulo.setCodBarras(myCodBarraDao.getCodsArticulo(unArticulo.getId()));
				unArticulo.setExistencia(res.getInt("existencia"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				unArticulo.setEstado(res.getBoolean("estado"));
				unArticulo.setMedida(res.getInt("medida"));
				
			 }
			//JOptionPane.showMessageDialog(null, unArticulo);		
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
			finally
			{
				//se regresa el al query join sin la tabla codigo de barra
				super.setSqlQuerySelectJoin(sqlBaseJoin);
				try{
					if(res!=null)res.close();
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
				return unArticulo;
			}
			else{
				
				
				return null;
			}
		
		
		
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para eliminar un articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	
	@Override
	public boolean eliminar(Object c){
		Articulo myArticulo=(Articulo)c;
		
		int resultado=0;
		Connection conn=null;
		
		try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				
				psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_articulo = ?");
				
				psConsultas.setInt( 1, myArticulo.getId() );
				resultado=psConsultas.executeUpdate();
				myCodBarraDao.eliminarCodigo(myArticulo.getId());
				return true;
			
			} catch (SQLException e) {
				System.out.println(e.getMessage());
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
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Actualizar los articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean actualizarArticulo(Articulo articulo, List<CodBarra> codsElimniar){
		int resultado;
		
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET articulo = ?, codigo_marca = ? ,codigo_impuesto = ?, precio_articulo=?,tipo_articulo=?,medida=? WHERE codigo_articulo = ?");
			psConsultas.setString(1,articulo.getArticulo());
			psConsultas.setInt(2, articulo.getMarcaObj().getId());
			psConsultas.setInt(3, articulo.getImpuestoObj().getId());
			psConsultas.setDouble(4, articulo.getPrecioVenta());
			psConsultas.setDouble(5, articulo.getTipoArticulo());
			psConsultas.setInt(6, articulo.getMedida());
			psConsultas.setInt(7, articulo.getId());
			
			resultado=psConsultas.executeUpdate();
			//JOptionPane.showMessageDialog(null, a+","+resultado );
			
			//se elimina los codigos de barra selecionados
			for(int c=0;c<codsElimniar.size();c++){
				myCodBarraDao.eliminar(codsElimniar.get(c));
			}
			
			//se agregan los codigos nuevos registrados
			for(int x=0;x<articulo.getCodBarra().size();x++){
				if(articulo.getCodBarra().get(x).getCodArticulo()==0){
					articulo.getCodBarra().get(x).setCodArticulo(articulo.getId());
					myCodBarraDao.registrar(articulo.getCodBarra().get(x));
				}
				
			}
			
			//se eleminia los precios
			//se recorren los precios de los articulos para eliminarlos
			for(int y=0; y<articulo.getPreciosVenta().size();y++){
					//se completa el codigo de articulo para el precios
					articulo.getPreciosVenta().get(y).setCodigoArticulo(articulo.getId());
					this.preciosDao.eliminar(articulo.getPreciosVenta().get(y));
				
			}
			
			
			//se recorren los precios de los articulos para guardarlos
			for(int y=0; y<articulo.getPreciosVenta().size();y++){
				//se comprueba que el precio del articulo sea valido   
				if(articulo.getPreciosVenta().get(y).getPrecio().doubleValue()>0){
					//se completa el codigo de articulo para el precios
					articulo.getPreciosVenta().get(y).setCodigoArticulo(articulo.getId());
					this.preciosDao.registrar(articulo.getPreciosVenta().get(y));
					
					//se establece el precio de venta por defecto
					if(articulo.getPreciosVenta().get(y).getCodigoPrecio()==1){
						articulo.setPrecioVenta(articulo.getPreciosVenta().get(y).getPrecio().doubleValue());
					}
				}
			}
			
			
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Articulo> todoArticulos(){
		
		
		
        Connection con = null;
        
        
      
       	List<Articulo> articulos=new ArrayList<Articulo>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect());
			
			
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Articulo unArticulo=new Articulo();
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				unArticulo.setExistencia(res.getInt("existencia"));
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
				
				articulos.add(unArticulo);
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
				return articulos;
			}
			else return null;
		
	}
	

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public List<Articulo> todos(int limInf,int limSupe){
		
		
		
        Connection con = null;
        
        
      
       	List<Articulo> articulos=new ArrayList<Articulo>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			//System.out.println(myBodega);

			psConsultas = con.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, myBodega.getId());
			
			psConsultas.setInt(2, limSupe);
			
			//sino ocupamos el parametro del estado
			if(estado!=2){
				psConsultas.setInt(3, super.estado);
				psConsultas.setInt(4, limInf);
			}else{
				psConsultas.setInt(3, limInf);
			}
					
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				Articulo unArticulo=new Articulo();
				existe=true;
				unArticulo.setId(Integer.parseInt(res.getString("codigo_articulo")));
				unArticulo.setArticulo(res.getString("articulo"));
				unArticulo.getMarcaObj().setDescripcion(res.getString("marca"));
				unArticulo.getMarcaObj().setId(res.getInt("codigo_marca"));
				unArticulo.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				unArticulo.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				unArticulo.setPrecioVenta(res.getDouble("precio_articulo"));
				unArticulo.setTipoArticulo(res.getInt("tipo_articulo"));
				
				unArticulo.setExistencia(res.getDouble("existencia"));
				unArticulo.setEstado(res.getBoolean("estado"));
				
				articulos.add(unArticulo);
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
				return articulos;
			}
			else return null;
		
	}
	
	
	public void setIdArticuloRegistrado(int i){
		idArticuloRegistrado=i;
	}
	public int getIdArticuloRegistrado(){
		return idArticuloRegistrado;
	} 
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar Articulo>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		Articulo myArticulo=(Articulo)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+" (articulo,codigo_marca,codigo_impuesto,precio_articulo,tipo_articulo,medida) VALUES (?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myArticulo.getArticulo() );
			psConsultas.setInt( 2, myArticulo.getMarcaObj().getId() );
			psConsultas.setDouble( 3, myArticulo.getImpuestoObj().getId());
			psConsultas.setDouble(4, myArticulo.getPrecioVenta());
			psConsultas.setInt(5, myArticulo.getTipoArticulo());
			psConsultas.setInt(6, myArticulo.getMedida());
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdArticuloRegistrado(rs.getInt(1));
				myArticulo.setId(this.getIdArticuloRegistrado());
			}
			
			//se completa el listado de barras de codigos con el codigo del articulo creado
			for(int x=0;x<myArticulo.getCodBarra().size();x++){
				myArticulo.getCodBarra().get(x).setCodArticulo(idArticuloRegistrado);
			}
			
			myCodBarraDao.registrarCodsBarra(myArticulo.getCodBarra());
			
			//se recorren los precios de los articulos para guardarlos
			for(int y=0; y<myArticulo.getPreciosVenta().size();y++){
				//se comprueba que el precio del articulo sea valido
				if(myArticulo.getPreciosVenta().get(y).getPrecio().doubleValue()>0){
					//se completa el codigo de articulo para el precios
					myArticulo.getPreciosVenta().get(y).setCodigoArticulo(idArticuloRegistrado);
					this.preciosDao.registrar(myArticulo.getPreciosVenta().get(y));
					
					//se establece el precio de venta por defecto
					if(myArticulo.getPreciosVenta().get(y).getCodigoPrecio()==1){
						myArticulo.setPrecioVenta(myArticulo.getPreciosVenta().get(y).getPrecio().doubleValue());
					}
				}
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
	public boolean registraPrecioProgramar(Articulo myArticulo) {
		// TODO Auto-generated method stub
		int resultado=0;
		
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( "INSERT INTO precios_programados(codigo_articulo,nuevo_precio) VALUES (?,?)");
			
			psConsultas.setInt(1, myArticulo.getId());
			psConsultas.setDouble(2, myArticulo.getPrecioVenta());
			
			
			resultado=psConsultas.executeUpdate();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			//conexion.desconectar();
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
	
public double getExistencia(int codigoArticulo,int codigoBodega){
		
		
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		double existencia=0;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement("SELECT can_saldo FROM v_kardex_saldos where codigo_articulo=? and codigo_bodega=? ORDER BY codigo_movimiento desc limit 1");
			psConsultas=conn.prepareStatement("select f_can_saldo_kardex(?,?) as can_saldo");
			psConsultas.setInt(1, codigoArticulo);
			psConsultas.setInt(2, codigoBodega);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				existencia=res.getDouble("can_saldo");
				
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
			 }
			//JOptionPane.showMessageDialog(null, unArticulo);		
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
			finally
			{
				try{
					if(res!=null)res.close();
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
				return existencia;
			}
			else{
				
				/*unArticulo=buscarArticuloBarraCod(i+"");
				if(unArticulo!=null){
					return unArticulo;
				}else*/
					return existencia;
			}
		
	}

public boolean actualizarEstado(Articulo articulo){
	int resultado;
	
	Connection conn=null;
	
	try {
		conn=ConexionStatic.getPoolConexion().getConnection();
		psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET estado = ? WHERE codigo_articulo = ?");
		
		psConsultas.setInt(1,(articulo.isEstado()) ? 1 : 0);
		
		psConsultas.setInt(2, articulo.getId());
		
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
@Override
public boolean actualizar(Object c) {
	// TODO Auto-generated method stub
	return false;
}

public Departamento getMyBodega() {
	return myBodega;
}

public void setMyBodega(Departamento myBodega) {
	this.myBodega = myBodega;
}

@Override
public void run() {
	// TODO Auto-generated method stub
	
	this.existenciaAler=this.getAlertaExistencia();

	
}

public double getExistenciaAler() {
	return existenciaAler;
}

public void setExistenciaAler(double existenciaAler) {
	this.existenciaAler = existenciaAler;
	
}

		public boolean actualizarExistencia(Articulo articulo,Departamento bodega){
			int resultado;
			
			Connection conn=null;
			
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement("{call crear_ajuste_inventario_movil(?,?,?,?,?)}");
				
				psConsultas.setInt(1,articulo.getId());
				
				psConsultas.setInt(2, bodega.getId());
				
				psConsultas.setDouble(3, articulo.getExistencia());
				psConsultas.setDouble(4, articulo.getPrecioVenta());
				psConsultas.setString(5,ConexionStatic.getUsuarioLogin().getUser());
				
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

}
