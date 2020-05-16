package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DatosFacturacion;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatosFacturacionDao extends ModeloDaoBasic {

	public DatosFacturacionDao() {
		super("datos_factura","codigo_rango");
		
	}

	@Override
	public boolean eliminar(Object c) {
		
		
		DatosFacturacion datosF=(DatosFacturacion)c;
		
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement("DELETE FROM "+datosF.getCaja().getNombreBd()+".datos_factura WHERE datos_factura.codigo_rango=?;");
			
			super.psConsultas.setInt( 1, datosF.getCodigo());
			
			psConsultas.executeUpdate();
		
			
			
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
	
	public int getIdCaiAct(Caja caja){
		
		//se cambia la base de datos para las facturas de la caja seleccionada
		super.DbName=ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();
		
		
		Integer codCai=0;
		ResultSet res=null;
		PreparedStatement buscarArt=null;
		Connection conn=null;
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			//JOptionPane.showMessageDialog(null, "Pasamossssssss","ERORR",JOptionPane.ERROR_MESSAGE);
			buscarArt=conn.prepareStatement(super.getQuerySelect()+" ORDER BY codigo_rango desc limit 1");
			
			res = buscarArt.executeQuery();
			while(res.next()){
				existe=true;
				codCai=res.getInt("codigo_rango");
				//unArticulo.setPreciosVenta(preciosDao.getPreciosArticulo(unArticulo.getId()));
				
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
	                if(buscarArt != null)buscarArt.close();
	                if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		
			if (existe) {
				return codCai;
			}
			else return  -1;
	}

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		DatosFacturacion datosF=(DatosFacturacion)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement("INSERT INTO "+datosF.getCaja().getNombreBd()+".datos_factura(CAI,factura_inicial,factura_final,codigo_tipo_facturacion,cantida_solicitada,fecha_limite_emision) VALUES (?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setString(1,datosF.getCAI());
			super.psConsultas.setInt( 2, datosF.getFacturaInicial());
			super.psConsultas.setInt(3, datosF.getFacturaFinal());
			super.psConsultas.setString(4,datosF.getCodigoFacturas());
			super.psConsultas.setInt(5, datosF.getCantOtorgada());
			super.psConsultas.setString(6,datosF.getFechaLimite());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				datosF.setCodigo(rs.getInt(1));
					
					
				}
			
			//se estable el nuevo numeracion de la facturacion
			this.setNumeroFact(datosF);
			
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
		DatosFacturacion datosF=(DatosFacturacion)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement("UPDATE "+datosF.getCaja().getNombreBd()+".datos_factura SET CAI=?,factura_inicial=?,factura_final=?,codigo_tipo_facturacion=?,cantida_solicitada=?,fecha_limite_emision=? WHERE datos_factura.codigo_rango=?;",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setString(1,datosF.getCAI());
			super.psConsultas.setInt( 2, datosF.getFacturaInicial());
			super.psConsultas.setInt(3, datosF.getFacturaFinal());
			super.psConsultas.setString(4,datosF.getCodigoFacturas());
			super.psConsultas.setInt(5, datosF.getCantOtorgada());
			super.psConsultas.setString(6,datosF.getFechaLimite());
			super.psConsultas.setInt(7,datosF.getCodigo());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				datosF.setCodigo(rs.getInt(1));
					
					
				}
			
			//se estable el nuevo numeracion de la facturacion
			this.setNumeroFact(datosF);
			
			
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
	public List<DatosFacturacion> todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<DatosFacturacion> todos(int limInf, int limSupe, Object c) {
		// TODO Auto-generated method stub
		Caja caja=(Caja)c;
		List<DatosFacturacion> datosFs=new ArrayList<DatosFacturacion>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		super.DbName=caja.getNombreBd();
		//JOptionPane.showMessageDialog(null, caja.getNombreBd());
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQueryRecord());
			
			super.psConsultas.setInt(1, limSupe);
			super.psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				DatosFacturacion unaDato=new DatosFacturacion();
				
				unaDato.setCodigo(res.getInt("codigo_rango"));
				unaDato.setCAI(res.getString("CAI"));
				unaDato.setFacturaInicial(res.getInt("factura_inicial"));
				unaDato.setFacturaFinal(res.getInt("factura_final"));
				unaDato.setCodigoFacturas(res.getString("codigo_tipo_facturacion"));
				unaDato.setCantOtorgada(res.getInt("cantida_solicitada"));
				
				//se crear el formato para la fecha
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
				
				Date date2=sdf2.parse(res.getString("fecha_limite_emision"));
				
				//se recoge la fecha de compra de la view
				String date = sdf.format(date2);
				
				//myDatosF.setFechaLimite(date);
				unaDato.setFechaLimite(date);
				
				
				unaDato.setCaja(caja);
				
				datosFs.add(unaDato);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en las fechas",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		finally
		{
			try{
				if(res != null) res.close();
	            if(super.psConsultas != null)super.psConsultas.close();
	            if(conn != null) conn.close();
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
		} // fin de finally
		
			if (existe) {
				return datosFs;
			}
			else return null;
	}
	
	public boolean setNumeroFact(DatosFacturacion datosF){
		
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement("ALTER TABLE "+datosF.getCaja().getNombreBd()+".encabezado_factura AUTO_INCREMENT = ?");
			super.psConsultas.setInt(1,datosF.getFacturaInicial());
			
			
			psConsultas.executeUpdate();

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
	
	public boolean verificarFacturacionFactInicial(DatosFacturacion datosF){
		
		ResultSet rs=null;
		
		int ultimoNoFactura=0;
		
		//int resultado=0;
		Connection conn=null;
		boolean existe=false;
		String sql="SELECT encabezado_factura.numero_factura FROM "+datosF.getCaja().getNombreBd()+".encabezado_factura ORDER BY encabezado_factura.numero_factura desc limit 1";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			super.psConsultas=conn.prepareStatement(sql);
			
			
			
			rs=super.psConsultas.executeQuery();

			while(rs.next()){
				ultimoNoFactura=rs.getInt("numero_factura");
				existe=true;
			 }
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				existe=false;
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

        existe= datosF.getFacturaInicial() > ultimoNoFactura;
		

		return existe;
		
	}
	
	public boolean verificarFacturacionEliminacion(DatosFacturacion datosF){
			
			ResultSet rs=null;
			
			int ultimoNoFactura=0;
			
			//int resultado=0;
			Connection conn=null;
			boolean existe=false;
			String sql="SELECT encabezado_factura.numero_factura FROM "+datosF.getCaja().getNombreBd()+".encabezado_factura where encabezado_factura.cod_rango=?";
			
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				
				super.psConsultas=conn.prepareStatement(sql);
				super.psConsultas.setInt(1, datosF.getCodigo());
				
				rs=super.psConsultas.executeQuery();
	
				while(rs.next()){
					ultimoNoFactura=rs.getInt("numero_factura");
					existe=true;
				 }
				
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					existe=true;
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
			
	
			return existe;
			
		}

	public DatosFacturacion buscarPorId(Integer id, Object c) {
		// TODO Auto-generated method stub
		Caja caja=(Caja)c;
		DatosFacturacion unaDato=new DatosFacturacion();
		ResultSet res=null;
		//se cambia el nombre de la base de datos 
		super.DbName=caja.getNombreBd();
		boolean existe=false;
		Connection con = null;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();			
			
			psConsultas=con.prepareStatement(super.getQuerySearch("codigo_rango", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			//System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				unaDato.setCodigo(res.getInt("codigo_rango"));
				unaDato.setCAI(res.getString("CAI"));
				unaDato.setFacturaInicial(res.getInt("factura_inicial"));
				unaDato.setFacturaFinal(res.getInt("factura_final"));
				unaDato.setCodigoFacturas(res.getString("codigo_tipo_facturacion"));
				unaDato.setCantOtorgada(res.getInt("cantida_solicitada"));
				
				//se crear el formato para la fecha
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
				
				Date date2=sdf2.parse(res.getString("fecha_limite_emision"));
				
				//se recoge la fecha de compra de la view
				String date = sdf.format(date2);
				
				//myDatosF.setFechaLimite(date);
				unaDato.setFechaLimite(date);
				
				
				unaDato.setCaja(caja);
				
				existe=true;
		
				
				
			
			 }
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en las fechas",JOptionPane.ERROR_MESSAGE);
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
				return unaDato;
			}
			else return null;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}


}
