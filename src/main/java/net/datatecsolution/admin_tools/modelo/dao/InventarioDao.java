package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Conexion;
import net.datatecsolution.admin_tools.modelo.Inventario;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventarioDao {
	private Conexion conexion;
	private PreparedStatement buscarArticulo=null;
	private PreparedStatement actualizarInventario=null;
	private PreparedStatement insertarNuevaInventario=null;
	
	public InventarioDao(Conexion conn){
		conexion=conn;
		
		/*try {
			buscarArticulo=conexion.getConnection().prepareStatement("SELECT * FROM v_existencia where codigo_articulo=? ;");
			//actualizarInventario=conexion.getConnection().prepareStatement("UPDATE articulo_bodega SET existencia = ? WHERE codigo_articulo = ? and codigo_bodega=?");
			//insertarNuevaInventario=conexion.getConnection().prepareStatement( "INSERT INTO articulo_bodega(codigo_bodega,codigo_articulo,existencia) VALUES (?,?,?)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar inventario>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean agregarInventario(Inventario inv){
		boolean resultado=false;
		Connection conn=null;
		try{
			conn=conexion.getPoolConexion().getConnection();
			insertarNuevaInventario=conn.prepareStatement( "INSERT INTO articulo_bodega(codigo_bodega,codigo_articulo,existencia) VALUES (?,?,?)");
			insertarNuevaInventario.setInt(1,inv.getBodega().getId());
			insertarNuevaInventario.setInt(2, inv.getArticulo().getId());
			insertarNuevaInventario.setDouble(3,inv.getExistencia());
			insertarNuevaInventario.executeUpdate();
			
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
                if(insertarNuevaInventario != null)insertarNuevaInventario.close();
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para Actualizar los Inventario>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean actualizarInventario(Inventario inv){
		int resultado;
		Connection conn=null;
		try {
			
			conn=conexion.getPoolConexion().getConnection();
			actualizarInventario=conn.prepareStatement("UPDATE articulo_bodega SET existencia = ? WHERE codigo_articulo = ? and codigo_bodega=?");
			actualizarInventario.setDouble(1,inv.getExistencia());
			actualizarInventario.setInt(2, inv.getArticulo().getId());
			actualizarInventario.setInt(3,inv.getBodega().getId());
			
			resultado=actualizarInventario.executeUpdate();
			//JOptionPane.showMessageDialog(null, a+","+resultado );
			
			return true;
		
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
        }
		finally
		{
			try{
				
				//if(res != null) res.close();
                if(actualizarInventario != null)actualizarInventario.close();
                if(conn != null) conn.close();
                
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//Sconexion.desconectar();
				} // fin de catch
		} // fin de finally
	}
	
	
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para buscar articulo del inventario por ID>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Inventario buscarArticulo(int i){
		Inventario unArticulo=new Inventario();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=conexion.getPoolConexion().getConnection();
			buscarArticulo=conn.prepareStatement("SELECT * FROM v_existencia where codigo_articulo=? ;");
			buscarArticulo.setInt(1, i);
			res = buscarArticulo.executeQuery();
			while(res.next()){
				existe=true;
				
				unArticulo.setExistencia(res.getDouble("existencia"));
				//unArticulo.setPrecioVenta(res.getDouble("Precio"));
				unArticulo.getBodega().setId(res.getInt("codigo_bodega"));
				unArticulo.getBodega().setDescripcion(res.getString("descripcion_bodega"));
				unArticulo.getArticulo().setId(res.getInt("codigo_articulo"));
				unArticulo.getArticulo().setArticulo(res.getString("articulo"));
				unArticulo.getArticulo().setPrecioVenta(res.getDouble("precio_articulo"));
				
				
			 }
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Error, no se conecto");
					System.out.println(e);
					existe=false;
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
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return unArticulo;
			}
			else return null;
		
		
		
	}

}
