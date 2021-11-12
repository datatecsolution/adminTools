package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

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

	public void getDevolucionesParciales(String fecha1, String fecha2, Caja caja, List<Comision> comisiones, int comisionPorcentaje) {

		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName = caja.getNombreBd();


		Connection con = null;

		String sql = "SELECT empleados.nombre, " +
				" empleados.apellido," +
				" encabezado_factura.codigo_vendedor, " +
				" max(encabezado_factura.fecha) AS fecha_fact, "+
				" sum(detalle_devoluciones.total) AS total_ventas," +
				" sum("+DbNameBase+".f_costo_dev(detalle_devoluciones.numero_factura,detalle_devoluciones.codigo_articulo)) AS costo_dev "+
				" FROM " + super.DbName + ".encabezado_factura " +
				" INNER JOIN " + DbNameBase + ".empleados " +
				"	ON encabezado_factura.codigo_vendedor = empleados.codigo_empleado " +
				"INNER JOIN  " + DbNameBase + ".detalle_devoluciones " +
				"ON encabezado_factura.numero_factura = detalle_devoluciones.numero_factura " +
				"where encabezado_factura.estado_factura = 'ACT'" +
				"and DATE_FORMAT(encabezado_factura.fecha,'%y-%m-%d') BETWEEN CAST(? AS DATE) and CAST(? AS DATE) " +
				"and detalle_devoluciones.codigo_caja = ? " +
				"GROUP BY encabezado_factura.codigo_vendedor;";


		ResultSet res = null;


		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			psConsultas.setInt(3, caja.getCodigo());

			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();

			while (res.next()) {
				int itemEmpleado = -1;
				//se revisa la lista de los empleados cargados previos establecer a quien corresponde el resultado obtenido
				for (int x = 0; x < comisiones.size(); x++) {
					if (comisiones.get(x).getCodigoVendedor() == res.getInt("codigo_vendedor")) {
						itemEmpleado = x;
					}
				}
				if (itemEmpleado >= 0) {
					comisiones.get(itemEmpleado).setDevoluciones(res.getDouble("total_ventas"));
					comisiones.get(itemEmpleado).setTotalCostoDev(res.getDouble("costo_dev"));
					//comisiones.get(itemEmpleado).setPorcentaje(comisionPorcentaje);
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
		} finally {
			//se restablece el nombre de la base de datos por defecto
			super.DbName = DbNameBase;
			try {

				if (res != null) res.close();
				if (psConsultas != null) psConsultas.close();
				if (con != null) con.close();


			} // fin de try
			catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally

	}
}
