package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.Requisicion;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequisicionDao extends ModeloDaoBasic {
	
	
	private String sqlBaseJoin=null;
	private DetalleRequisicionDao detallesDao;

	public RequisicionDao() {
		super("encabezado_requisicion","codigo_requisicion");
	
		sqlBaseJoin="SELECT `encabezado_requisicion`.`codigo_requisicion` AS `codigo_requisicion`,"
							+ "date_format("
											+ "`encabezado_requisicion`.`fecha`,"
											+ "'%d/%m/%Y' )AS `fecha`,"
							+ "`encabezado_requisicion`.`total` AS `total`,"
							+ "`encabezado_requisicion`.`usuario` AS `usuario`,"
							+ "`encabezado_requisicion`.`agrega_kardex` AS `agrega_kardex`,"
							+ "`encabezado_requisicion`.`estado_requisicion` AS `estado_requisicion`,"
							+ "`encabezado_requisicion`.`codigo_depart_destino` AS `idDestino`,"
							+ "`encabezado_requisicion`.`codigo_depart_origen` AS `idOrigen`,"
							+ "`encabezado_requisicion`.`fecha` AS `fecha2`,"
							+ "`bodega`.`descripcion_bodega` AS `destino`,"
							+ "`bodega2`.`descripcion_bodega` AS `origen`"
					+ "FROM "
							+ super.DbName+ ".`encabezado_requisicion` "
							+ "JOIN "
							+ super.DbName+ ".`bodega` ON( `encabezado_requisicion`.`codigo_depart_destino` = `bodega`.`codigo_bodega`) "
							+ "JOIN "
							+ super.DbName+ ".`bodega` bodega2 ON( `encabezado_requisicion`.`codigo_depart_origen` = `bodega2`.`codigo_bodega`)";
				super.setSqlQuerySelectJoin(sqlBaseJoin);
				detallesDao=new DetalleRequisicionDao ();
				
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para conseguir la fecha>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public String getFechaSistema(){
		String fecha="";
		Connection conn=null;
		ResultSet res=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement("SELECT DATE_FORMAT(now(), '%d/%m/%Y') as fecha;");
	
			res=psConsultas.executeQuery();
			while(res.next()){
				fecha=res.getString("fecha");
			}
		} catch (SQLException e) {
		
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally{
			try{
				
				if(res != null) res.close();
	            if(psConsultas != null)psConsultas.close();
	            if(conn != null) conn.close();
	            
				
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
		}
		
		
		return fecha;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar requisicion>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		Requisicion myRequisicion=(Requisicion)c;
		
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
		
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()
					+ "("
						+ "fecha,"
						+ "total,"
						+ "codigo_depart_origen,"
						+ "codigo_depart_destino,"
						+ "usuario,"
						+ "estado_requisicion"
					+ ") VALUES (now(),?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setBigDecimal( 1, myRequisicion.getTotal() );
			psConsultas.setInt( 2, myRequisicion.getDepartamentoOrigen().getId());
			psConsultas.setInt( 3, myRequisicion.getDepartamentoDestino().getId());
			psConsultas.setString(4, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(5, "ACT");
			
			//myRequisicion.get
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			
			int idRequiReguistrado=0;
			while(rs.next()){
				idRequiReguistrado=rs.getInt(1);
				myRequisicion.setNoRequisicion(rs.getInt(1));
			}
			
			//se guardan los detalles de la fatura
			for(int x=0;x<myRequisicion.getDetalles().size();x++){
				
				if(myRequisicion.getDetalles().get(x).getArticulo().getId()!=-1){
					myRequisicion.getDetalles().get(x).setDepartamentoOrigen(myRequisicion.getDepartamentoOrigen());
					myRequisicion.getDetalles().get(x).setDepartamentoDestino( myRequisicion.getDepartamentoDestino());
					detallesDao.agregarDetalleRequi(myRequisicion.getDetalles().get(x), idRequiReguistrado);
				}
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para extraer todas las requisiciones de un rango de fechas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
/**
 * Metodo para extraer todas las requisiciones de un rango de fechas
 * @param fecha1
 * @param fecha2
 * @param limite de paginacion 
 * @param cantidad de item por pagina
 * @return lista de requisiones por fechas y paginadas de 20 en 20
 */
public List<Requisicion> buscarPorFechas(String fecha1, String fecha2,int limitInferio, int canItemPag) {
		
		
		
		
        Connection con = null;
    
        //Statement stmt = null;
    	List<Requisicion> requisiciones=new ArrayList<Requisicion>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("fecha", "BETWEEN ? and "));
			
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Requisicion una=new Requisicion();
				existe=true;
				
				
				una.setNoRequisicion(res.getInt("codigo_requisicion"));
				una.setFechaCompra(res.getString("fecha"));
				una.getDepartamentoOrigen().setDescripcion(res.getString("origen"));
				una.getDepartamentoDestino().setDescripcion(res.getString("destino"));
				una.setTotal(res.getBigDecimal("total"));
				una.setEstado(res.getString("estado_requisicion"));
				
				
				requisiciones.add(una);
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
				return requisiciones;
			}
			else return null;
		
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para extraer todas las requisiciones>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
@Override	
public List<Requisicion> todos(int cantItemPag,int limSupe) {
		Connection con = null;
        
        
	      
       	List<Requisicion> requisiciones=new ArrayList<Requisicion>();
		
		ResultSet res=null;
		
		boolean existe=false;
		
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, cantItemPag);
			
			
			res = psConsultas.executeQuery();
			while(res.next()){
				Requisicion una=new Requisicion();
				existe=true;
				
				
				una.setNoRequisicion(res.getInt("codigo_requisicion"));
				una.setFechaCompra(res.getString("fecha"));
				
				Departamento origen=new Departamento();
				
				origen.setId(res.getInt("idOrigen"));
				origen.setDescripcion(res.getString("origen"));
				
				una.setDepartamentoOrigen(origen);
				
				Departamento destino=new Departamento();
				
				destino.setId(res.getInt("idDestino"));
				destino.setDescripcion(res.getString("destino"));	
				
				una.setDepartamentoDestino(destino);
				
				una.setTotal(res.getBigDecimal("total"));
				una.setEstado(res.getString("estado_requisicion"));
				
				
							
				
				requisiciones.add(una);
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
			return requisiciones;
		}
		else return null;
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para extraer una requi por id>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
@Override	
public Requisicion buscarPorId(int id) {
		// TODO Auto-generated method stub
		Requisicion una=new Requisicion();
		Connection con = null;
		ResultSet res=null;
		
		boolean existe=false;
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			//psConsultas = con.prepareStatement(super.getQuerySelect()+" where v_requisiciones.codigo_requisicion=?;");
			psConsultas = con.prepareStatement(super.getQuerySearch("codigo_requisicion", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				una.setNoRequisicion(res.getInt("codigo_requisicion"));
				una.setFechaCompra(res.getString("fecha"));
				una.getDepartamentoOrigen().setDescripcion(res.getString("origen"));
				una.getDepartamentoDestino().setDescripcion(res.getString("destino"));
				una.setTotal(res.getBigDecimal("total"));
				una.setEstado(res.getString("estado_requisicion"));
				
				
				
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
			return una;
		}
		else return null;
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para anular requisiones>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public boolean anularRequi(Requisicion r) {
		// TODO Auto-generated method stub
		boolean resultado=false;
		Connection conn=null;
		String sql="UPDATE encabezado_requisicion SET "
				
				
				+ "estado_requisicion=?"
				
				+ " WHERE codigo_requisicion = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(sql);
			
			
			psConsultas.setString(1, "NULA");
			
			psConsultas.setInt(2, r.getNoRequisicion());
			psConsultas.executeUpdate();
						
			
			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}finally
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
		return resultado;
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}

}
