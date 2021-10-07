package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFacturaProveedor;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaProveedorDao extends ModeloDaoBasic{
	
	
	private ArticuloDao articuloDao=null;
	private String sqlBaseJoin=null;
	
	public DetalleFacturaProveedorDao(){
		
		super("detalle_factura_compra","id_detalle_compra");
		articuloDao=new ArticuloDao();
		
		sqlBaseJoin="SELECT detalle_factura_compra . numero_compra  AS  numero_compra_detalle , "
							+ " articulo . articulo  AS  articulo , "
							+ " ifnull( "
										+ " detalle_factura_compra . precio , 0 "
									+ " )AS  precio_detalle , "
							+ " ifnull("
										+ " detalle_factura_compra . cantidad ,0 "
									+ " )AS  cantidad_detalle , "
							+ " ifnull( "
										+ " detalle_factura_compra . impuesto ,0 "
									+ " )AS  impuesto_detalle , "
							+ " ifnull( "
										+ " detalle_factura_compra . subtotal ,0 "
										+ " )AS  subtotal_detalle , "
							+ " detalle_factura_compra . codigo_articulo, "
							+ " detalle_factura_compra . id_detalle_compra, "
							+ " detalle_factura_compra . precio, "
							+ " detalle_factura_compra . cantidad, "
							+ " detalle_factura_compra . impuesto, "
							+ " detalle_factura_compra . subtotal, "
							+ " detalle_factura_compra . agrega_kardex, "
							+ " detalle_factura_compra . codigo_bodega, "
							+" `articulo`.`codigo_articulo` AS `codigo_articulo`, "
							+ " `articulo`.`articulo` AS `articulo`, "
							+ " `articulo`.`cod_articulo` AS `cod_articulo`, "
							+ " `articulo`.`tipo_articulo` AS `tipo_articulo`, "
							+ " `articulo`.`codigo_marca`, "
							+ " `articulo`.`codigo_impuesto`"
				+ " FROM "
						+ super.DbName+". detalle_factura_compra "
								+ " JOIN  "
									+ super.DbName+". articulo  ON( articulo . codigo_articulo  =  detalle_factura_compra . codigo_articulo )";
		super.setSqlQuerySelectJoin(sqlBaseJoin);	
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalle>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean registrar(DetalleFacturaProveedor detalle,int noCompra,int codBodega){
		boolean resultado=false;
		Connection conn=null;
		
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryInsert() +" (numero_compra,codigo_articulo,precio,cantidad,impuesto,subtotal,codigo_bodega) VALUES (?,?,?,?,?,?,?)");
			psConsultas.setInt(1, noCompra);
			psConsultas.setInt(2, detalle.getArticulo().getId());
			psConsultas.setBigDecimal(3, detalle.getPrecioCompra());
			psConsultas.setBigDecimal(4, detalle.getCantidad());
			psConsultas.setBigDecimal(5, detalle.getImpuesto());
			psConsultas.setBigDecimal(6, detalle.getSubTotal());
			psConsultas.setInt(7, codBodega);
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
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
		return resultado;
	}
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para cargar los detalles>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<DetalleFacturaProveedor> buscarPorId(int idCompra) {
		
		
		List<DetalleFacturaProveedor> detalles=new ArrayList<DetalleFacturaProveedor>();
		

        Connection con = null;
  
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where numero_compra=?;");
			psConsultas.setInt(1, idCompra);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				DetalleFacturaProveedor unDetalle=new DetalleFacturaProveedor();
				existe=true;
				//se consigue el articulo del detalle
				Articulo articuloDetalle=new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
				articuloDetalle.setId(Integer.parseInt(res.getString("codigo_articulo")));
				articuloDetalle.setArticulo(res.getString("articulo"));
				articuloDetalle.getCategoria().setId(res.getInt("codigo_marca"));
				articuloDetalle.getImpuestoObj().setId(res.getInt("codigo_impuesto"));
				articuloDetalle.setTipoArticulo(res.getInt("tipo_articulo"));
				articuloDetalle.setPrecioVenta(res.getDouble("precio_detalle"));//se estable el precio del articulo
				
				unDetalle.setListArticulos(articuloDetalle);//se agrega el articulo al 
				
				unDetalle.setCantidad(res.getBigDecimal("cantidad_detalle"));
				unDetalle.setImpuesto(res.getBigDecimal("impuesto_detalle"));
				//unDetalle.setSubTotal(res.getDouble("subtotal"));
				unDetalle.setSubTotal(res.getBigDecimal("subtotal_detalle"));
				unDetalle.setCodigoBodega(res.getInt("codigo_bodega"));
				
				
				
				
				detalles.add(unDetalle);
			 }
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
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

}
