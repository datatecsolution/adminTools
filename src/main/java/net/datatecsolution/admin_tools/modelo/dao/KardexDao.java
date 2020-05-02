package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Kardex;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KardexDao {

	private PreparedStatement buscarArticulo=null;
	private PreparedStatement insertarNuevoMovimiento=null;
	private PreparedStatement insertarNuevo=null;
	
	private String ultimoRistro="SELECT "
			+ " v_kardex.can_saldo, "
			+ "v_kardex.precio_saldo, "
			+ "v_kardex.total_saldo,"
			+ "v_kardex.cod,"
			+ "v_kardex.codigo_bodega,"
			+ "v_kardex.codigo_articulo "
			+ " FROM v_kardex "
			+ " WHERE "
			+ " v_kardex.codigo_articulo = ? AND "
			+ " v_kardex.codigo_bodega = ? "
			+ " ORDER BY "
			+ " v_kardex.cod DESC LIMIT 1";
	
	
	private String ultimoReee="SELECT"
			+ "`articulo_kardex`.`codigo_kardex` AS `codigo_kardex`,"
			+ "`articulo_kardex`.`codigo_articulo` AS `codigo_articulo`,"
			+ "`articulo_kardex`.`codigo_bodega` AS `codigo_bodega`,"
			+ "`articulo_kardex`.`cantidad_maxima` AS `cantidad_maxima`,"
			+ "`articulo_kardex`.`cantidad_minima` AS `cantidad_minima`,"
			+ "`articulo_kardex`.`metodo` AS `metodo`"
			+ " FROM "
			+ "`articulo_kardex` "
			+ " WHERE"
			+ "	("
			+ "		("
			+ "			`articulo_kardex`.`codigo_articulo` = ?"
			+ "		)"
			+ "		AND ("
			+ "			`articulo_kardex`.`codigo_bodega` = ?"
			+ "		)"
			+ "	)";
	
	public KardexDao(){
		
	}
	
	public boolean agregarEntrada(Kardex kar){
		boolean resultado=false;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			insertarNuevoMovimiento=conn.prepareStatement( "INSERT INTO kardex(no_documento,codigo_articulo,codigo_bodega,entrada,salida,fecha) VALUES (?,?,?,?,?,now())");
			//insertarNuevoMovimiento=conn.prepareStatement(sql);
			insertarNuevoMovimiento.setString(1, kar.getNoDocumento());
			insertarNuevoMovimiento.setInt(2, kar.getArticulo().getId());
			insertarNuevoMovimiento.setInt(3, kar.getBodega().getId());
			insertarNuevoMovimiento.setDouble(4, kar.getEntrada());
			insertarNuevoMovimiento.setDouble(5, kar.getSalida());
			insertarNuevoMovimiento.executeUpdate();
			resultado=true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				//if(res != null) res.close();
                if(insertarNuevoMovimiento != null)insertarNuevoMovimiento.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		return resultado;
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo en el kardex por por id articulo y id bodega>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public BigDecimal buscarExistencia(int idArticulo,int idBodega){
		
		BigDecimal existencia=new BigDecimal(0.00);
		boolean existe=false;
		Connection conn=null;
		ResultSet res=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			buscarArticulo=conn.prepareStatement("SELECT movimiento_kardex.cantidad AS can_saldo "
					+ " FROM articulo_kardex "
					+ " INNER JOIN detalle_movimiento_kardex "
							+ " ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex "
					+ " INNER JOIN movimiento_kardex "
						+ " ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento "
					+ " WHERE movimiento_kardex.codigo_tipo_movimiento = 3 AND articulo_kardex.codigo_articulo = ? AND articulo_kardex.codigo_bodega = ?	 "
					+ " ORDER BY movimiento_kardex.codigo_movimiento desc limit 1");
			buscarArticulo.setInt(1, idArticulo);
			buscarArticulo.setInt(2, idBodega);
			res=buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				existencia=res.getBigDecimal("can_saldo");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(buscarArticulo != null)buscarArticulo.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		if(existe){
			return existencia;
			
		}else{
			return null;
		}
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo en el kardex por por id articulo y id bodega>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Kardex buscarKardex(int idArticulo,int idBodega){
		Kardex myKardex=new Kardex();
		boolean existe=false;
		Connection conn=null;
		ResultSet res=null;
		ArticuloDao articuloDao=new ArticuloDao();
		DepartamentoDao bodegaDao=new DepartamentoDao();
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			buscarArticulo=conn.prepareStatement("SELECT * FROM kardex where codigo_articulo=? and codigo_bodega=? LIMIT 1,1");
			buscarArticulo.setInt(1, idArticulo);
			buscarArticulo.setInt(2, idBodega);
			res=buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				myKardex.setArticulo(articuloDao.buscarArticulo(idArticulo));
				myKardex.setBodega(bodegaDao.buscarBodega(idBodega));
				myKardex.setEntrada(res.getDouble("entrada"));
				myKardex.setSalida(res.getDouble("salida"));
				myKardex.setId(res.getInt("idKardex"));
				myKardex.setFecha(res.getString("fecha"));
				myKardex.setNoDocumento(res.getString("no_documento"));
				myKardex.setExistencia(res.getDouble("existencia"));
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(buscarArticulo != null)buscarArticulo.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		if(existe){
			return myKardex;
			
		}else{
			return null;
		}
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo en el kardex por por id articulo y id bodega>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public BigDecimal buscarCantidadSaldo(int idArticulo,int idBodega){
		
		boolean existe=false;
		Connection conn=null;
		ResultSet res=null;
		BigDecimal precioCompra=new BigDecimal(00);
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			buscarArticulo=conn.prepareStatement(ultimoReee);
			buscarArticulo.setInt(1, idArticulo);
			buscarArticulo.setInt(2, idBodega);
			res=buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				
				precioCompra=res.getBigDecimal("can_saldo");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(buscarArticulo != null)buscarArticulo.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		if(existe){
			return precioCompra;
			
		}else{
			return null;
		}
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo en el kardex por por id articulo y id bodega>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public BigDecimal buscarKardexPrecio(int idArticulo,int idBodega){
		
		boolean existe=false;
		Connection conn=null;
		ResultSet res=null;
		BigDecimal precioCompra=new BigDecimal(00);
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			buscarArticulo=conn.prepareStatement("SELECT movimiento_kardex.precio_unidad AS precio_saldo "
					+ " FROM articulo_kardex "
					+ " INNER JOIN detalle_movimiento_kardex "
							+ " ON articulo_kardex.codigo_kardex = detalle_movimiento_kardex.codigo_kardex "
					+ " INNER JOIN movimiento_kardex "
						+ " ON detalle_movimiento_kardex.codigo_movimiento = movimiento_kardex.codigo_movimiento "
					+ " WHERE movimiento_kardex.codigo_tipo_movimiento = 3 AND articulo_kardex.codigo_articulo = ? AND articulo_kardex.codigo_bodega = ?	 "
					+ " ORDER BY movimiento_kardex.codigo_movimiento desc limit 1");
			buscarArticulo.setInt(1, idArticulo);
			buscarArticulo.setInt(2, idBodega);
			res=buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				
				precioCompra=res.getBigDecimal("precio_saldo");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(buscarArticulo != null)buscarArticulo.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		if(existe){
			return precioCompra;
			
		}else{
			return null;
		}
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo en el kardex por por id articulo y id bodega>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean comprobarKardex(int idArticulo,int idBodega){
		
		boolean existe=false;
		Connection conn=null;
		ResultSet res=null;
		BigDecimal precioCompra=new BigDecimal(00);
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			buscarArticulo=conn.prepareStatement(ultimoReee);
			buscarArticulo.setInt(1, idArticulo);
			buscarArticulo.setInt(2, idBodega);
			res=buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				
				//precioCompra=res.getBigDecimal("precio_saldo");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				
				if(res != null) res.close();
                if(buscarArticulo != null)buscarArticulo.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		
		return existe;
		/*if(existe){
			return precioCompra;
			
		}else{
			return null;
		}**/
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar encabezado Kardex>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public int registrarKardex(int idArticulo,int idBodega)
	{
		
		int resultado=-1;
		ResultSet rs=null;
		Connection con = null;
		boolean resul=false;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			insertarNuevo=con.prepareStatement( "INSERT INTO articulo_kardex(codigo_articulo,codigo_bodega) VALUES (?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			insertarNuevo.setInt( 1, idArticulo );
			insertarNuevo.setInt( 2, idBodega );
			
			
			insertarNuevo.executeUpdate();
			
			rs=insertarNuevo.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				resultado=rs.getInt(1);
			}
			
			
			
			resul= true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			//conexion.desconectar();
            resul= false;
		}
		finally
		{
			try{
				if(rs!=null)rs.close();
				 if(insertarNuevo != null)insertarNuevo.close();
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
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalle Kardex>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public int agregarDetalle(int codigoKardex, String idFactura) {
		// TODO Auto-generated method stub
		int resultado=-1;
		ResultSet rs=null;
		Connection con = null;
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			insertarNuevo=con.prepareStatement( "INSERT INTO detalle_movimiento_kardex(fecha,codigo_kardex,descripcion,no_documento) VALUES (now(),?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			insertarNuevo.setInt( 1, codigoKardex );
			insertarNuevo.setString( 2, "Inventario Inicial" );
			insertarNuevo.setString( 3, idFactura );
			
			insertarNuevo.executeUpdate();
			
			rs=insertarNuevo.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				resultado=rs.getInt(1);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally
		{
			try{
				if(rs!=null)rs.close();
				 if(insertarNuevo != null)insertarNuevo.close();
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

	public boolean agregarMovimiento(int idMov, BigDecimal cantidad,
			BigDecimal precioCompra) {
		// TODO Auto-generated method stub
		boolean resul=false;
		//ResultSet rs=null;
		Connection con = null;
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			insertarNuevo=con.prepareStatement( "INSERT INTO movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) VALUES (?,?,?,?,?)");
			insertarNuevo.setInt( 1, idMov );
			insertarNuevo.setInt( 2, 3 );
			insertarNuevo.setDouble( 3, cantidad.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() );
			insertarNuevo.setDouble( 4, precioCompra.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() );
			
			BigDecimal total=cantidad.multiply(precioCompra);
			insertarNuevo.setDouble( 5, total.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() );
			insertarNuevo.executeUpdate();
			resul=true;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally
		{
			try{
				//if(rs!=null)rs.close();
				 if(insertarNuevo != null)insertarNuevo.close();
	              if(con != null) con.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally
		
		return resul;
		
	}

}
