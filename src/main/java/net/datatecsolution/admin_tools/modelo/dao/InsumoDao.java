package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Insumo;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;



public class InsumoDao extends ModeloDaoBasic{
	
	private int idRegistrado=-1;
	
	private String sqlBaseJoin=null;
	
	public InsumoDao(){
		super("insumos","id_insumo");
		
		sqlBaseJoin="SELECT insumos.id_insumo, "
							+ " insumos.codigo_servicio, "
							+ " insumos.codigo_articulo, "
							+ " insumos.cantidad, "
							+ " articulo.articulo, "
							+ " insumos.codigo_articulo, "
							+ " precios_articulos.precio_articulo "
					//		+ " tipo_cuenta_bancos.observaciones, "
					//		+ " bancos.id_tipo_cuenta  "
				+ " FROM "
						+ super.DbName+".insumos "
							+ "INNER JOIN "+super.DbName+".articulo "
									+ "ON (insumos.codigo_articulo = articulo.codigo_articulo)"
									+ " join "+ super.DbName+".precios_articulos "
									+ " on(precios_articulos.codigo_articulo =articulo.codigo_articulo and precios_articulos.codigo_precio = 1) ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	public Vector<Banco> getCuentas(){
		Vector<Banco> cuentas=new Vector<Banco>();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect());
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				Banco una=new Banco();
				existe=true;
				una.setId(res.getInt("id"));
				una.setNombre(res.getString("nombre"));
				una.setNoCuenta(res.getString("no_cuenta"));
				una.setTipoCuenta(res.getString("tipo_cuenta"));
				una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
				cuentas.add(una);
			 }
			
		}catch (SQLException e) {
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
			return cuentas;
		}
		else return null;
		
	}
	
	
	public Vector<Banco> getCuentasExectoEfectivo(){
		Vector<Banco> cuentas=new Vector<Banco>();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where bancos.id<>1");
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				Banco una=new Banco();
				existe=true;
				una.setId(res.getInt("id"));
				una.setNombre(res.getString("nombre"));
				una.setNoCuenta(res.getString("no_cuenta"));
				una.setTipoCuenta(res.getString("tipo_cuenta"));
				una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
				cuentas.add(una);
			 }
			
		}catch (SQLException e) {
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
			return cuentas;
		}
		else return null;
		
	}
	
	
	public void setIdRegistrado(int i){
		idRegistrado=i;
	}
	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		Banco myCuenta=(Banco)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryUpdate()+" SET nombre=?,no_cuenta=?,id_tipo_cuenta=? where id=?");
			
			psConsultas.setString( 1, myCuenta.getNombre() );
			psConsultas.setString( 2,myCuenta.getNoCuenta() );
			
			
			psConsultas.setInt( 3, myCuenta.getIdTipoCuenta());
			
			
			psConsultas.setInt(4, myCuenta.getId());
			
			
			resultado=psConsultas.executeUpdate();
			


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
	@Override
	public List<Banco> todos(int limiteInferior, int limiteSuperior) {
		// TODO Auto-generated method stub
		
		List<Banco> cuentas=new ArrayList<Banco>();
				ResultSet res=null;
				
				Connection conn=null;
				
				boolean existe=false;
				
				try {
					
					conn=ConexionStatic.getPoolConexion().getConnection();
					psConsultas=conn.prepareStatement( super.getQueryRecord());
					
					psConsultas.setInt(1, limiteSuperior);
					psConsultas.setInt(2, limiteInferior);
					
					res = psConsultas.executeQuery();
					while(res.next()){
						
						Banco una=new Banco();
						existe=true;
						una.setId(res.getInt("id"));
						una.setNombre(res.getString("nombre"));
						una.setNoCuenta(res.getString("no_cuenta"));
						una.setTipoCuenta(res.getString("tipo_cuenta"));
						una.setIdTipoCuenta(res.getInt("id_tipo_cuenta"));
						cuentas.add(una);
					 }
					
				}catch (SQLException e) {
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
					return cuentas;
				}
				else return null;
			}
	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		Insumo myInsumo=(Insumo)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement(super.getQueryDelete()+" WHERE id_insumo = ?");
			psConsultas.setInt( 1, myInsumo.getCodigoInsumo() );
	
			resultado=psConsultas.executeUpdate();
			
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
	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		Insumo myInsumo=(Insumo)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+"(codigo_servicio,codigo_articulo,cantidad) VALUES (?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setInt( 1, myInsumo.getServicio().getId());
			psConsultas.setInt( 2, myInsumo.getArticulo().getId());
			psConsultas.setBigDecimal( 3,myInsumo.getCantidad() );
	
			
				
			
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
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
	@Override
	public List<Insumo> buscarPorId(int id) {
		// TODO Auto-generated method stub
		List<Insumo> isumos=new ArrayList<Insumo>();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( super.getQuerySelect()+" where insumos.codigo_servicio=?");
			
			psConsultas.setInt(1, id);
			
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				Insumo isum=new Insumo();
				isum.setCodigoInsumo(res.getInt("id_insumo"));
				isum.setCodigoServicio(res.getInt("codigo_servicio"));
				
				Articulo articulo=new Articulo();
				
				articulo.setId(res.getInt("codigo_articulo"));
				articulo.setArticulo(res.getString("articulo.articulo"));
				articulo.setPrecioVenta(res.getDouble("precio_articulo"));
				isum.setArticulo(articulo);
				
				isum.setCantidad(res.getBigDecimal("cantidad"));
				existe=true;
				isumos.add(isum);
			 }
			
		}catch (SQLException e) {
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
			return isumos;
		}
		else return null;
	}
	
	
	public boolean eliminarTodosArticulo(int myInsumo) {
		// TODO Auto-generated method stub
		//Insumo myInsumo=(Insumo)c;
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement(super.getQueryDelete()+" WHERE codigo_servicio = ?");
			psConsultas.setInt( 1, myInsumo);
	
			resultado=psConsultas.executeUpdate();
			
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

}
