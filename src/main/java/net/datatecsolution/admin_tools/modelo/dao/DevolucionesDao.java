package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;

import javax.swing.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DevolucionesDao extends ModeloDaoBasic {
	
	
	
	

	public DevolucionesDao() {
		// TODO Auto-generated constructor stub
		super("detalle_devoluciones","codigo_devolucion");
	}
	
	public boolean registraDevulucion(DetalleFactura detalle,int idFactura,int codigoKardex) {
		boolean resultado=false;
		
		Connection conn=null;
		
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			CallableStatement procedimiento=conn.prepareCall("{call crear_dev_venta_kardex(?,?,?,?,?)}");
			
			procedimiento.setInt(1, codigoKardex);
			procedimiento.setInt(2, idFactura);
			procedimiento.setBigDecimal(3, detalle.getCantidad());
			procedimiento.setDouble(4, detalle.getArticulo().getPrecioVenta());
			procedimiento.setInt(5,detalle.getId());
			
			procedimiento.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		return resultado;
	}
	public List<DetalleFactura> getDevolucionArticulo(int idFactura,int idArticulo){


		List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
		


        Connection con = null;
  
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySelect()+" where numero_factura=? and codigo_articulo=? and codigo_caja=?;");
			psConsultas.setInt(1, idFactura);
			psConsultas.setInt(2, idArticulo);
			psConsultas.setInt(3, ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){

				DetalleFactura unDetalle=new DetalleFactura();
				
				existe=true;
				unDetalle.setId(res.getInt("codigo_devolucion"));
				//se consigue el articulo del detalle
				Articulo articuloDetalle= new Articulo();//articuloDao.buscarArticulo(res.getInt("codigo_articulo"));
				articuloDetalle.setId(res.getInt("codigo_articulo"));
				//articuloDetalle.setArticulo(res.getString("articulo"));
				articuloDetalle.setPrecioVenta(res.getDouble("precio"));//se estable el precio del articulo
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
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar detalles de facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean agregarDetalle(DetalleFactura detalle, int idFactura) {
		boolean resultado=false;
		
		String sql=super.getQueryInsert()+"("
				+ "numero_factura,"
				+ "codigo_articulo,"
				+ "precio,"
				+ "cantidad,"
				+ "impuesto,"
				+ "subtotal,"
				+ "descuento,"
				+ "total,"
				+ "fecha, "
				+ "codigo_caja"
				+ ") VALUES (?,?,?,?,?,?,?,?,now(),?)";
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
			psConsultas.setInt(9,ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
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
