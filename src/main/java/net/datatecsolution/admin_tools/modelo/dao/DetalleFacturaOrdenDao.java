package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import net.datatecsolution.admin_tools.modelo.Factura;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaOrdenDao extends ModeloDaoBasic {
	
	
	
	private String sqlBaseJoin;
	private PrecioArticuloDao preciosDao=new PrecioArticuloDao();
	
	public DetalleFacturaOrdenDao(){
		super("detalle_factura_temp","id");
		
		sqlBaseJoin="SELECT detalle_factura_temp.numero_factura, "
							+ " articulo.articulo, "
							+ " impuesto.codigo_impuesto, "
							+ " impuesto.porcentaje AS impuesto, "
							+ " impuesto.descripcion_impuesto, "
							+ " detalle_factura_temp.precio, "
							+ " detalle_factura_temp.cantidad, "
							+ " detalle_factura_temp.impuesto, "
							+ " detalle_factura_temp.descuento, "
							+ " detalle_factura_temp.subtotal, "
							+ " detalle_factura_temp.total, "
							+ " articulo.codigo_articulo, "
							+ " detalle_factura_temp.id "
					+ " FROM "
						+ super.DbName+".detalle_factura_temp  "
								+ "JOIN "
									+super.DbName+". articulo  "
										+ "	ON ( articulo . codigo_articulo  =  detalle_factura_temp . codigo_articulo )"
								+ " join "+super.DbName+ ".impuesto "
										+ " on(impuesto.codigo_impuesto =articulo.codigo_impuesto) "
								+ "join "+ super.DbName+".precios_articulos "
										+ " on(precios_articulos.codigo_articulo =articulo.codigo_articulo and precios_articulos.codigo_precio = 1) ";



			super.setSqlQuerySelectJoin(sqlBaseJoin);
		
		
	}
	
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas temp>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean agregarDetalleTemp(DetalleFactura detalle, int idFactura) {
		boolean resultado=false;
		
		String sql=super.getQueryInsert()+"("
				+ "numero_factura,"
				+ "codigo_articulo,"
				+ "precio,"
				+ "cantidad,"
				+ "impuesto,"
				+ "subtotal,"
				+ "descuento,"
				+ "total"
				+ ") VALUES (?,?,?,?,?,?,?,?)";
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
	
	public List<DetalleFactura> detallesFacturaPendiente(int idFactura) {
		List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
		

        Connection con = null;
  
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where numero_factura=? order by id ASC;");
			//psConsultas = con.prepareStatement(super.getquer+" where numero_factura=? order by id ASC;");
			psConsultas.setInt(1, idFactura);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				DetalleFactura unDetalle=new DetalleFactura();
				existe=true;
				//se consigue el articulo del detalle
				Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
				
				
				
				articuloDetalle.setId(res.getInt("codigo_articulo"));
				articuloDetalle.setArticulo(res.getString("articulo"));
				articuloDetalle.setPrecioVenta(res.getDouble("precio"));//se estable el precio del articulo
				articuloDetalle.getImpuestoObj().setPorcentaje(res.getString("impuesto"));
				articuloDetalle.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				
				//conseguir los precios del producto
				articuloDetalle.setPreciosVenta(this.preciosDao.getPreciosArticulo(articuloDetalle.getId()));
				
				unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
				unDetalle.setCantidad(res.getBigDecimal("cantidad"));
				unDetalle.setImpuesto(res.getBigDecimal("impuesto"));
				unDetalle.setSubTotal(res.getBigDecimal("subtotal"));
				unDetalle.setDescuentoItem(res.getBigDecimal("descuento"));
				unDetalle.setTotal(res.getBigDecimal("total"));
				
				
				
				
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
		Factura myFactura=(Factura)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE numero_factura = ?");
			psConsultas.setInt( 1, myFactura.getIdFactura() );
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
