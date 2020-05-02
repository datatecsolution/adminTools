package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaDao extends ModeloDaoBasic {
	
	
	

	private String sqlBaseJoin=null;
	
	public DetalleFacturaDao(){
		super("detalle_factura","id");
		
			setSqlQueryJoin();
		
		
	}
	public void setSqlQueryJoin(){
		sqlBaseJoin="SELECT detalle_factura.numero_factura  AS  numero_factura_detalle , "
				+ " articulo.articulo, "
				+ " detalle_factura.precio  AS  precio_detalle , "
				+ " detalle_factura.cantidad  AS  cantidad_detalle , "
				+ " detalle_factura.impuesto  AS  impuesto_detalle , "
				+ " detalle_factura.descuento  AS  descuento_detalle , "
				+ " detalle_factura.subtotal  AS  subtotal_detalle , "
				+ " detalle_factura.total  AS  total_detalle , "
				+ " articulo.codigo_articulo, "
				+ " detalle_factura.id, "
				+ " detalle_factura.agrega_kardex, "
				+ " detalle_factura.codigo_barra,"
				+ " IFNULL(precios_articulos.precio_articulo,0) AS precio_costo"
			+ " FROM "
				+ super.DbName+".detalle_factura  "
					+ "JOIN "
						+ DbNameBase+". articulo  "
								+ "	ON ( articulo . codigo_articulo  =  detalle_factura . codigo_articulo )"
					+ "JOIN "
						+ super.DbName+". encabezado_factura "
							+ "	ON ( detalle_factura . numero_factura  =  encabezado_factura . numero_factura )"
						
							+ " LEFT JOIN "+ DbNameBase+".precios_articulos "
							+ " on(precios_articulos.codigo_articulo =articulo.codigo_articulo and precios_articulos.codigo_precio = 4)  ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean agregarDetalle(DetalleFactura detalle, int idFactura) {
		boolean resultado=false;
		
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();
		setSqlQueryJoin();
		String sql=super.getQueryInsert()+" ("
				+ "numero_factura,"
				+ "codigo_articulo,"
				+ "precio,"
				+ "cantidad,"
				+ "impuesto,"
				+ "subtotal,"
				+ "descuento,"
				+ "total,"
				+ "codigo_barra"
				+ ") VALUES (?,?,?,?,?,?,?,?,?)";
		Connection conn=null;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( sql);
			
			psConsultas.setInt(1, idFactura);
			psConsultas.setInt(2, detalle.getArticulo().getId());
			psConsultas.setDouble(3, detalle.getArticulo().getPrecioVenta());
			psConsultas.setBigDecimal(4, detalle.getCantidad());
			psConsultas.setBigDecimal(5, detalle.getImpuesto());
			psConsultas.setBigDecimal(6, detalle.getSubTotal());
			psConsultas.setBigDecimal(7, detalle.getDescuentoItem());
			psConsultas.setBigDecimal(8, detalle.getTotal());
			if(detalle.getArticulo().getCodigoBarra()!=null)
				psConsultas.setString(9, detalle.getArticulo().getCodigoBarra());
			else
				psConsultas.setString(9,"NA");
				
			psConsultas.executeUpdate();
			
			resultado=true;
		}catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			//conexion.desconectar();
			resultado= false;
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
			
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
		return resultado;
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean verificarArticuloEnDetalle2( int idArticulo) {
		boolean resultado=false;
		ResultSet res=null;
		String sql=super.getQuerySelect()+" where codigo_articulo=?";
		Connection conn=null;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( sql);
			
			psConsultas.setInt(1, idArticulo);
			res = psConsultas.executeQuery();
			while(res.next()){
				resultado=true;
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado= false;
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
		return resultado;
	}
	
	
public void getDetallesFactura(Integer noFacturaIncial, Integer noFacturaFinal,String usuario,Caja caja,List<DetalleFactura> detalles) {
		
		
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=caja.getNombreBd();
		setSqlQueryJoin();
		
		
		

        Connection con = null;
  
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			//dfd
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where detalle_factura.numero_factura>=? and detalle_factura.numero_factura<=? AND encabezado_factura.estado_factura = 'ACT' AND encabezado_factura.usuario = ? ORDER BY  articulo.articulo ");
			psConsultas.setInt(1, noFacturaIncial);
			psConsultas.setInt(2, noFacturaFinal);
			psConsultas.setString(3, usuario);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				DetalleFactura unDetalle=new DetalleFactura();
				existe=true;
				unDetalle.setId(res.getInt("id"));
				unDetalle.setIdFactura(res.getInt("numero_factura_detalle"));
				
				
				//se consigue el articulo del detalle
				Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
				articuloDetalle.setId(res.getInt("codigo_articulo"));
				articuloDetalle.setArticulo(res.getString("articulo"));
				articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
				unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
				
				unDetalle.setArt(res.getString("articulo"));
				unDetalle.setCodigoArt(res.getInt("codigo_articulo"));
				unDetalle.setPrecioVentaItem(res.getDouble("precio_detalle"));
				
				//se recoge el precio costo del articulo
				BigDecimal precioCostoArticulo=new BigDecimal(res.getDouble("precio_costo"));
				
				//se mutimplica el cantidad del item por el precio costo
				BigDecimal precioCosto=precioCostoArticulo.multiply(res.getBigDecimal("cantidad_detalle"));
				
				//se calcula la ganancia
				BigDecimal ganacia=res.getBigDecimal("total_detalle").subtract(precioCosto);
				
				//se establece el total del costo
				unDetalle.setTotalVentasCosto(precioCosto.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				
				//se establece la ganancia
				unDetalle.setGanancia(ganacia.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				
				//unDetalle.setTotalVentasCosto(totalVentasCosto);
				unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
				unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
				unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
				unDetalle.setDescuentoItem(res.getBigDecimal("descuento_detalle"));
				unDetalle.setTotal(res.getBigDecimal("total_detalle"));
				
				
				
				
				detalles.add(unDetalle);
			 }
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
		finally
		{
			
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
		
	}
	

public void getDetallesFactura(Caja caja,List<DetalleFactura> detalles,Articulo articulo,String fecha1, String fecha2) {
	
	
	//se cambia la base de datos para las facturas de la caja seleccionada
	super.DbName=caja.getNombreBd();
	setSqlQueryJoin();
	
	
	

    Connection con = null;

	
	ResultSet res=null;
	
	boolean existe=false;
	try {
		con = ConexionStatic.getPoolConexion().getConnection();
		//dfd
		psConsultas = con.prepareStatement(super.getQuerySelect()+" where CAST(encabezado_factura.fecha AS DATE) BETWEEN ? AND ? and  detalle_factura.codigo_articulo=? and encabezado_factura.estado_factura = 'ACT'");
		
		psConsultas.setString(1, fecha1);
		psConsultas.setString(2, fecha2);
		psConsultas.setInt(3, articulo.getId());
		
		//System.out.println(psConsultas);
		res = psConsultas.executeQuery();
		while(res.next()){
			DetalleFactura unDetalle=new DetalleFactura();
			existe=true;
			unDetalle.setId(res.getInt("id"));
			unDetalle.setIdFactura(res.getInt("numero_factura_detalle"));
			
			
			//se consigue el articulo del detalle
			Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
			articuloDetalle.setId(res.getInt("codigo_articulo"));
			articuloDetalle.setArticulo(res.getString("articulo"));
			articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
			unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
			
			unDetalle.setArt(res.getString("articulo"));
			unDetalle.setCodigoArt(res.getInt("codigo_articulo"));
			unDetalle.setPrecioVentaItem(res.getDouble("precio_detalle"));
			
			//se recoge el precio costo del articulo
			BigDecimal precioCostoArticulo=new BigDecimal(res.getDouble("precio_costo"));
			
			//se mutimplica el cantidad del item por el precio costo
			BigDecimal precioCosto=precioCostoArticulo.multiply(res.getBigDecimal("cantidad_detalle"));
			
			//se calcula la ganancia
			BigDecimal ganacia=res.getBigDecimal("total_detalle").subtract(precioCosto);
			
			//se establece el total del costo
			unDetalle.setTotalVentasCosto(precioCosto.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
			
			//se establece la ganancia
			unDetalle.setGanancia(ganacia.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
			
			//unDetalle.setTotalVentasCosto(totalVentasCosto);
			unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
			unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
			unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
			unDetalle.setDescuentoItem(res.getBigDecimal("descuento_detalle"));
			unDetalle.setTotal(res.getBigDecimal("total_detalle"));
			
			
			
			
			detalles.add(unDetalle);
		 }
				
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
		}
	finally
	{
		
		//se restablece el nombre de la base de datos por defecto
		super.DbName= DbNameBase;
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
	
}

public List<DetalleFactura> getDetallesFactura(Integer idFactura, Caja caja) {
	
	
	//se cambia la base de datos para las facturas de la caja seleccionada
	super.DbName=caja.getNombreBd();
	setSqlQueryJoin();
	
	List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
	

    Connection con = null;

	
	ResultSet res=null;
	
	boolean existe=false;
	try {
		con = ConexionStatic.getPoolConexion().getConnection();
		
		psConsultas = con.prepareStatement(super.getQuerySelect()+" where detalle_factura.numero_factura=?;");
		psConsultas.setInt(1, idFactura);
		//System.out.println(psConsultas);
		res = psConsultas.executeQuery();
		while(res.next()){
			DetalleFactura unDetalle=new DetalleFactura();
			existe=true;
			unDetalle.setId(res.getInt("id"));
			unDetalle.setIdFactura(res.getInt("numero_factura_detalle"));
			
			
			//se consigue el articulo del detalle
			Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
			articuloDetalle.setId(res.getInt("codigo_articulo"));
			articuloDetalle.setArticulo(res.getString("articulo"));
			articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
			unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
			
			unDetalle.setArt(res.getString("articulo"));
			unDetalle.setCodigoArt(res.getInt("codigo_articulo"));
			unDetalle.setPrecioVentaItem(res.getDouble("precio_detalle"));
			
			//se recoge el precio costo del articulo
			BigDecimal precioCostoArticulo=new BigDecimal(res.getDouble("precio_costo"));
			
			//se mutimplica el cantidad del item por el precio costo
			BigDecimal precioCosto=precioCostoArticulo.multiply(res.getBigDecimal("cantidad_detalle"));
			
			//se calcula la ganancia
			BigDecimal ganacia=res.getBigDecimal("total_detalle").subtract(precioCosto);
			
			//se establece el total del costo
			unDetalle.setTotalVentasCosto(precioCosto.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
			
			//se establece la ganancia
			unDetalle.setGanancia(ganacia.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
			
			//unDetalle.setTotalVentasCosto(totalVentasCosto);
			unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
			unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
			unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
			unDetalle.setDescuentoItem(res.getBigDecimal("descuento_detalle"));
			unDetalle.setTotal(res.getBigDecimal("total_detalle"));
			
			
			
			
			detalles.add(unDetalle);
			
			
		 }
				
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
		}
	finally
	{
		
		//se restablece el nombre de la base de datos por defecto
		super.DbName= DbNameBase;
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
			return detalles;
		}
		else return null;
}

	public List<DetalleFactura> getDetallesFactura(int idFactura) {
		
		
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();
		setSqlQueryJoin();
		
		List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
		

        Connection con = null;
  
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where detalle_factura.numero_factura=?;");
			psConsultas.setInt(1, idFactura);
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				DetalleFactura unDetalle=new DetalleFactura();
				existe=true;
				unDetalle.setId(res.getInt("id"));
				//se consigue el articulo del detalle
				Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
				articuloDetalle.setId(res.getInt("codigo_articulo"));
				articuloDetalle.setArticulo(res.getString("articulo"));
				articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
				unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
				unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
				unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
				unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
				unDetalle.setDescuentoItem(res.getBigDecimal("descuento_detalle"));
				unDetalle.setTotal(res.getBigDecimal("total_detalle"));
				
				
				
				
				detalles.add(unDetalle);
			 }
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
			}
		finally
		{
			
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
				return detalles;
			}
			else return null;
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	public void getDetallesFacturaCategoria(CierreFacturacion cierreFacturacion, String usuario, Categoria myCategoria,
			List<DetalleFactura> detalles) {
		
		//se cambia la base de datos para las facturas de la caja seleccionada
				super.DbName=cierreFacturacion.getCaja().getNombreBd();//caja.getNombreBd();
				setSqlQueryJoin();
				
				
				//fsdf

		        Connection con = null;
		  
				
				ResultSet res=null;
				
				boolean existe=false;
				try {
					con = ConexionStatic.getPoolConexion().getConnection();
					//dfd
					psConsultas = con.prepareStatement(super.getQuerySelect()+" where detalle_factura.numero_factura>=? and detalle_factura.numero_factura<=? AND encabezado_factura.estado_factura = 'ACT' AND encabezado_factura.usuario = ? and articulo.codigo_marca=? ORDER BY  articulo.articulo ");
					psConsultas.setInt(1,cierreFacturacion.getNoFacturaInicio());
					psConsultas.setInt(2,cierreFacturacion.getNoFacturaFinal());
					psConsultas.setString(3, usuario);
					psConsultas.setInt(4, myCategoria.getId());
					
					res = psConsultas.executeQuery();
					while(res.next()){
						DetalleFactura unDetalle=new DetalleFactura();
						existe=true;
						unDetalle.setId(res.getInt("id"));
						unDetalle.setIdFactura(res.getInt("numero_factura_detalle"));
						
						
						//se consigue el articulo del detalle
						Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
						articuloDetalle.setId(res.getInt("codigo_articulo"));
						articuloDetalle.setArticulo(res.getString("articulo"));
						articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
						unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
						
						unDetalle.setArt(res.getString("articulo"));
						unDetalle.setCodigoArt(res.getInt("codigo_articulo"));
						unDetalle.setPrecioVentaItem(res.getDouble("precio_detalle"));
						
						//se recoge el precio costo del articulo
						BigDecimal precioCostoArticulo=new BigDecimal(res.getDouble("precio_costo"));
						
						//se mutimplica el cantidad del item por el precio costo
						BigDecimal precioCosto=precioCostoArticulo.multiply(res.getBigDecimal("cantidad_detalle"));
						
						//se calcula la ganancia
						BigDecimal ganacia=res.getBigDecimal("total_detalle").subtract(precioCosto);
						
						//se establece el total del costo
						unDetalle.setTotalVentasCosto(precioCosto.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
						
						//se establece la ganancia
						unDetalle.setGanancia(ganacia.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
						
						//unDetalle.setTotalVentasCosto(totalVentasCosto);
						unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
						unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
						unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
						unDetalle.setDescuentoItem(res.getBigDecimal("descuento_detalle"));
						unDetalle.setTotal(res.getBigDecimal("total_detalle"));
						
						
						
						
						detalles.add(unDetalle);
					 }
							
					} catch (SQLException e) {
							JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
							System.out.println(e);
					}
				finally
				{
					
					//se restablece el nombre de la base de datos por defecto
					super.DbName= DbNameBase;
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
		
	}

	

}
