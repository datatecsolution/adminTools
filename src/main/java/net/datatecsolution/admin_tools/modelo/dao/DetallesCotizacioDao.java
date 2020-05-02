package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetallesCotizacioDao extends ModeloDaoBasic{
	
	
	private String sqlBaseJoin=null;

	public DetallesCotizacioDao() {
		// TODO Auto-generated constructor stub
		super("detalle_cotizacion","id");
		
		
		sqlBaseJoin="SELECT detalle_cotizacion.numero_cotizacion  AS  numero_cotizacion_detalle , "
							+ " articulo . articulo, "
							+ " detalle_cotizacion . precio  AS  precio_detalle , "
							+ " detalle_cotizacion . cantidad  AS  cantidad_detalle , "
							+ " detalle_cotizacion . impuesto  AS  impuesto_detalle , "
							+ " detalle_cotizacion . descuento  AS  descuento_detalle , "
							+ " detalle_cotizacion . subtotal  AS  subtotal_detalle , "
							+ " detalle_cotizacion . total  AS  total_detalle , "
							+ " articulo . codigo_articulo, "
							+ " detalle_cotizacion . id  AS  id , "
							+ " detalle_cotizacion . codigo_barra "
							+ " FROM "
							+ super.DbName+ ".detalle_cotizacion "
								+ " JOIN  "
									+super.DbName+ ". articulo  ON( articulo . codigo_articulo  =  detalle_cotizacion . codigo_articulo )";
		
	super.setSqlQuerySelectJoin(sqlBaseJoin);
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean resgistrar(DetalleFactura detalle, int idFactura) {
		boolean resultado=false;
		
		String sql=super.getQueryInsert()+" ("
				+ "numero_cotizacion,"
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
			psConsultas.setString(9, detalle.getArticulo().getCodigoBarra());
			psConsultas.executeUpdate();
			
			resultado=true;
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

	public List<DetalleFactura> buscarPorIdCotizacion(int idFactura) {
		
		
		List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
		
	
	    Connection con = null;
	
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where numero_cotizacion=? order by id ASC;");
			//psConsultas = con.prepareStatement(super.getQuerySearch("numero_cotizacion", "="));
			psConsultas.setInt(1, idFactura);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				DetalleFactura unDetalle=new DetalleFactura();
				existe=true;
				//se consigue el articulo del detalles
				
				Articulo articuloDetalle=new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
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

}
