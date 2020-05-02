package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFacturaProveedor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;



public class DetalleRequisicionDao extends ModeloDaoBasic {
	
	
	
	private String sqlBaseJoin=null;
	
	public DetalleRequisicionDao(){
		super("detalle_factura","id");
		
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
								+ " detalle_factura.codigo_barra "
				+ " FROM "
					+ super.DbName+".detalle_factura  "
					+ "JOIN "+super.DbName+". articulo  "
						+ "	ON ( articulo . codigo_articulo  =  detalle_factura . codigo_articulo )";
		
			super.setSqlQuerySelectJoin(sqlBaseJoin);
		
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean agregarDetalleRequi(DetalleFacturaProveedor detalle, int idRequi) {
		boolean resultado=false;
		
		String sql="INSERT INTO detalle_requisicion("
				+ "codigo_requisicion,"
				+ "codigo_articulo,"
				+ "precio_unidad,"
				+ "cantidad,"
				+ "codigo_depart_origen,"
				+ "codigo_depart_destino,"
				+ "total"
				+ ") VALUES (?,?,?,?,?,?,?)";
		Connection conn=null;
		
		try{
			conn= ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( sql);
			
			psConsultas.setInt(1, idRequi);
			psConsultas.setInt(2, detalle.getArticulo().getId());
			psConsultas.setBigDecimal(3, detalle.getPrecioCompra());
			psConsultas.setBigDecimal(4, detalle.getCantidad());
			psConsultas.setInt( 5, detalle.getDepartamentoOrigen().getId());
			psConsultas.setInt( 6, detalle.getDepartamentoDestino().getId());
			psConsultas.setBigDecimal(7, detalle.getSubTotal());
			psConsultas.executeUpdate();
			
			resultado=true;
		}catch (SQLException e) {
			e.printStackTrace();
			//conexion.desconectar();
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
