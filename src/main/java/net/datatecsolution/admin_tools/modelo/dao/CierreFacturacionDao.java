package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.CierreFacturacion;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Usuario;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CierreFacturacionDao extends ModeloDaoBasic{

	private String sqlJoin;
	public CierreFacturacionDao() {
		super("cierre_facturacion", "id");
		// TODO Auto-generated constructor stub
		sqlJoin="SELECT cierre_facturacion.factura_inicial, "
					+ " cierre_facturacion.factura_final, "
					+ " cierre_facturacion.codigo_caja, "
					+ " cierre_facturacion.id, "
					+ " usuario.usuario, "
					+ " usuario.nombre_completo, "
					+ " usuario.clave, "
					+ " usuario.permiso, "
					+ " usuario.tipo_permiso, "
					+ " usuario.codigo_caja, "
					+ " cierre_facturacion.codigo_cierre, "
					+ " cajas.descripcion, "
					+ " cajas.codigo_bodega, "
					+ " cajas.nombre_db "
			+ " FROM "
					+ DbNameBase+".cierre_facturacion "
						+ " INNER JOIN "+ DbNameBase+".usuario "
							+ "	ON cierre_facturacion.usuario = usuario.usuario"
						+ " INNER JOIN  "+ DbNameBase+".cajas "
							+ " ON cierre_facturacion.codigo_caja = cajas.codigo";//incluir en el join el la table cierre de caja
																				//para evitar que coja registro guerfacnos
																				//OTRA opcion es a nivel de base de datos enlace por cascada
		
		super.setSqlQuerySelectJoin(sqlJoin);
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		CierreFacturacion cierreFactura=(CierreFacturacion)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryInsert()+"(codigo_cierre,codigo_caja,usuario,factura_inicial,factura_final) VALUES (?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setInt(1,cierreFactura.getCodigoCierre());
			super.psConsultas.setInt( 2, cierreFactura.getCaja().getCodigo());
			super.psConsultas.setString(3,cierreFactura.getUsuario());
			super.psConsultas.setInt( 4, cierreFactura.getNoFacturaInicio());
			super.psConsultas.setInt( 5, cierreFactura.getNoFacturaFinal());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				cierreFactura.setId(rs.getInt(1));
			}
					
				
			
			
			return true;
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
		}
		finally{
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
		}
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		
		CierreFacturacion cierreFactura=(CierreFacturacion)c;
		int resultado;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET factura_final = ? WHERE id = ?");
			psConsultas.setInt( 1, cierreFactura.getNoFacturaFinal() );
			psConsultas.setInt(2,cierreFactura.getId());
			resultado=psConsultas.executeUpdate();
			return true;
		}catch (SQLException e) {
			System.out.println(e.getMessage());
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			return false;
        }
		finally
		{
			try{
				
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
	public List todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
public List<CierreFacturacion> buscarIdCierre(Integer id){
		
		List<CierreFacturacion> cierresFacturas=new ArrayList<CierreFacturacion>();
		

		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		//fdfs
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where cierre_facturacion.codigo_cierre=?");
			//sdfsd
			psConsultas.setInt(1, id);
			
			//System.out.println(psConsultas);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				CierreFacturacion unaCierre=new CierreFacturacion();
			
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setId(res.getInt("id"));
				
				Caja unaCaja=new Caja();
				//sdfsd
				unaCaja.setCodigo(res.getInt("codigo_caja"));
				unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
				unaCaja.setDescripcion(res.getString("descripcion"));
				unaCaja.setNombreBd(res.getString("nombre_db"));
				
				unaCierre.setCaja(unaCaja);
				cierresFacturas.add(unaCierre);
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return cierresFacturas;
		}
		else return null;
	}
	
	public List<CierreFacturacion> buscarUltimoUser(Usuario user){
		
		List<CierreFacturacion> cierresFacturas=new ArrayList<CierreFacturacion>();
		

		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("usuario", "="));
			
			psConsultas.setString(1, user.getUser());
			
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				CierreFacturacion unaCierre=new CierreFacturacion();
				
				unaCierre.setNoFacturaFinal(res.getInt("factura_inicial"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setId(res.getInt("id"));
				
				Caja unaCaja=new Caja();
				
				unaCaja.setCodigo(res.getInt("codigo"));
				unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
				unaCaja.setDescripcion(res.getString("descripcion"));
				unaCaja.setNombreBd(res.getString("nombre_db"));
				
				unaCierre.setCaja(unaCaja);
				cierresFacturas.add(unaCierre);
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return cierresFacturas;
		}
		else return null;
	}
	public CierreFacturacion buscarPorCajaUsuario(Caja caja, String user) {
		// TODO Auto-generated method stub
		
		CierreFacturacion unaCierre=new CierreFacturacion();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("usuario=? and codigo_caja", "="));
			
			psConsultas.setString(1, user);
			psConsultas.setInt(2,caja.getCodigo());
			psConsultas.setInt(3, 0);
			psConsultas.setInt(4, 1);
			
			res = psConsultas.executeQuery();
			
			while(res.next()){
				existe=true;
				
				
				unaCierre.setNoFacturaFinal(res.getInt("factura_final"));
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setId(res.getInt("id"));
				
				Caja unaCaja=new Caja();
				
				unaCaja.setCodigo(res.getInt("codigo_caja"));
				unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
				unaCaja.setDescripcion(res.getString("descripcion"));
				unaCaja.setNombreBd(res.getString("nombre_db"));
				
				unaCierre.setCaja(unaCaja);
				
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return unaCierre;
		}
		else return null;
	}

	public CierreFacturacion buscarPorCajaUsuario(Caja caja, String user,int idCierre) {
		// TODO Auto-generated method stub
		
		CierreFacturacion unaCierre=new CierreFacturacion();
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySearch("usuario=? and codigo_cierre=? and codigo_caja", "="));
			
			psConsultas.setString(1, user);
			psConsultas.setInt(2, idCierre);
			psConsultas.setInt(3,caja.getCodigo());
			psConsultas.setInt(4, 0);
			psConsultas.setInt(5, 1);
			
			res = psConsultas.executeQuery();
			
			while(res.next()){
				existe=true;
				
				unaCierre.setNoFacturaInicio(res.getInt("factura_inicial"));
				unaCierre.setId(res.getInt("id"));
				
				Caja unaCaja=new Caja();
				
				unaCaja.setCodigo(res.getInt("codigo_caja"));
				unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
				unaCaja.setDescripcion(res.getString("descripcion"));
				unaCaja.setNombreBd(res.getString("nombre_db"));
				
				unaCierre.setCaja(unaCaja);
				
				
				
			}
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
	}
	finally
	{
		try{
			if(res != null) res.close();
	        if(psConsultas != null)psConsultas.close();
	        if(conn != null) conn.close();
			
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
	} // fin de finally
		
		
		if (existe) {
			return unaCierre;
		}
		else{
			return null;
		}
	}

}
