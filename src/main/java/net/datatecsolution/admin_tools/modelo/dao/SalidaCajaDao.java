package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.CierreCaja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.SalidaCaja;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalidaCajaDao extends ModeloDaoBasic {

	
	public int idUltimoRequistro=0;
	private int idRegistrado;
	private String sqlBaseJoin=null;
	private CuentaBancosDao myCuentaBancosDao=null;
	public SalidaCajaDao(){
		//Class(Conexion);
		super( "salidas_caja","codigo_salida");
		
		myCuentaBancosDao=new CuentaBancosDao();
		
		sqlBaseJoin="SELECT `salidas_caja`.`codigo_salida`,  "
							+ " `salidas_caja`.`concepto`,  "
							+ " `salidas_caja`.`cantidad`, "
							+ " `salidas_caja`.`usuario` , "
							+ " `salidas_caja`.`estado` , "
							+ " `empleados`.`codigo_empleado`, "
							+ " `empleados`.`nombre`, "
							+ " `empleados`.`apellido`,"
							+ " date_format("
												+ "`salidas_caja`.`fecha`, '%d/%m/%Y'"
											+ ") AS `fecha`, "
							+ " salidas_caja.`fecha` AS `fecha1` "
					+ " FROM "+super.DbName+ ".`salidas_caja` "
						+ "JOIN "+super.DbName+ ".`empleados` "
							+ "ON(`salidas_caja`.`codigo_empleado` = `empleados`.`codigo_empleado`) ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	
	public SalidaCaja getSalidaUltimoUser(){
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2="SELECT * FROM "+super.DbName+".salidas_caja WHERE salidas_caja.usuario=? ORDER BY salidas_caja.codigo_salida DESC LIMIT 1";
        //Statement stmt = null;
    	SalidaCaja unaSalida=new SalidaCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaSalida.setCodigoSalida(res.getInt("codigo_salida"));
				unaSalida.setConcepto(res.getString("concepto"));
				unaSalida.setCantidad(res.getBigDecimal("cantidad"));
				unaSalida.setFecha(res.getString("fecha"));
				unaSalida.setUsuario(res.getString("usuario"));
				unaSalida.setEstado(res.getString("estado"));
			
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
		
		return unaSalida;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
	@Override
	public List<SalidaCaja> todos(int limInf,int limSupe) {
		List<SalidaCaja> salidas =new ArrayList<SalidaCaja>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryRecord());
			//System.out.println(psConsultas);
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				SalidaCaja unaSalida=new SalidaCaja();
				
				unaSalida.setCodigoSalida(res.getInt("codigo_salida"));
				unaSalida.setConcepto(res.getString("concepto"));
				unaSalida.setCantidad(res.getBigDecimal("cantidad"));
				unaSalida.setFecha(res.getString("fecha"));
				unaSalida.setUsuario(res.getString("usuario"));
				unaSalida.setEstado(res.getString("estado"));
				
				Empleado unEmpleado=new Empleado();
				
				unEmpleado.setCodigo(res.getInt("codigo_empleado"));
				unEmpleado.setNombre(res.getString("nombre"));
				unEmpleado.setApellido(res.getString("apellido"));
				
				unaSalida.setEmpleado(unEmpleado);
				
				
				salidas.add(unaSalida);
				
				
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
			return salidas;
		}
		else return null;
	}

	public void calcularTotal(CierreCaja unaCierre) {
		// TODO Auto-generated method stub
		
		
		Connection con = null;
		ResultSet res=null;
		
		//se consiguie el ultima salida que realizo el usuario
		SalidaCaja ultimo=this.getSalidaUltimoUser();
		//se establece la ultima salida del usuaurio
		unaCierre.setNoSalidaFinal(ultimo.getCodigoSalida());
		
		
		
		
		//corregir error poner ifnull a la sumAa
		String sql="SELECT ifnull(sum(salidas_caja.cantidad),0)AS cantidad FROM "+super.DbName+ ".salidas_caja WHERE salidas_caja.codigo_salida >= ? AND salidas_caja.codigo_salida <= ? and salidas_caja.usuario=? and estado='ACT'";
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql);
			
			psConsultas.setInt(1, unaCierre.getNoSalidaInicial());
			psConsultas.setInt(2, unaCierre.getNoSalidaFinal());
			psConsultas.setString(3, unaCierre.getUsuario());
			
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				
				
				//si existe alguna salida se suma y se establecen en el cierre
				unaCierre.setTotalSalida(res.getBigDecimal("cantidad"));
				
	
			
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
	
		
	}
	@Override
	public boolean registrar(Object c) {
		
		SalidaCaja mySalida=(SalidaCaja)c;
		// TODO Auto-generated method stub
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
	
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+ " (concepto,cantidad,usuario,fecha,codigo_empleado,estado,codigo_cuenta) VALUES (?,?,?,now(),?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, mySalida.getConcepto());
			psConsultas.setBigDecimal( 2, mySalida.getCantidad().setScale(2, BigDecimal.ROUND_HALF_EVEN) );
			psConsultas.setString( 3, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt( 4,mySalida.getEmpleado().getCodigo() );
			psConsultas.setString(5, "ACT");
			psConsultas.setInt(6, mySalida.getCodigoCuenta());
			
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
				mySalida.setCodigoSalida(this.getIdRegistrado());
			}
			
			//ser registra el aumento a la cuenta
			myCuentaBancosDao.reguistrarDebitoSalidaCaja(mySalida);
			
			//se registra la disminucion de la cuenta efectivo
			myCuentaBancosDao.reguistrarCreditoSalidaCajaEfectivo(mySalida);
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

	private void setIdRegistrado(int i) {
		// TODO Auto-generated method stub
		idRegistrado=i;
	}
	public int getIdRegistrado(){
		return idRegistrado;
	} 
	
	public List<SalidaCaja> buscarPorNombreEmpleado(String busqueda,int limitInferio, int canItemPag) {
		List<SalidaCaja> salidas =new ArrayList<SalidaCaja>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try{
			conn=ConexionStatic.getPoolConexion().getConnection();
			//psConsultas=conn.prepareStatement("SELECT * FROM v_salidas where nombre LIKE ? ORDER BY codigo_salida Desc;");
			psConsultas=conn.prepareStatement(super.getQuerySearchJoin("nombre", "LIKE", "empleados", "codigo_empleado", "codigo_empleado"));
			
			psConsultas.setString(1, busqueda + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			System.out.println(psConsultas);
			res = psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				SalidaCaja unaSalida=new SalidaCaja();
				
				unaSalida.setCodigoSalida(res.getInt("codigo_salida"));
				unaSalida.setConcepto(res.getString("concepto"));
				unaSalida.setCantidad(res.getBigDecimal("cantidad"));
				unaSalida.setFecha(res.getString("fecha"));
				unaSalida.setUsuario(res.getString("usuario"));
				unaSalida.setEstado(res.getString("estado"));
				
				Empleado unEmpleado=new Empleado();
				
				unEmpleado.setCodigo(res.getInt("codigo_empleado"));
				unEmpleado.setNombre(res.getString("nombre"));
				unEmpleado.setApellido(res.getString("apellido"));
				
				unaSalida.setEmpleado(unEmpleado);
				
				
				salidas.add(unaSalida);
				
				
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
			return salidas;
		}
		else return null;
	} 
	@Override
	public SalidaCaja buscarPorId(int id){
		
		
		
		
        Connection con = null;
        
    	
    	SalidaCaja unaSalida=new SalidaCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("codigo_salida", "="));
			
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaSalida.setCodigoSalida(res.getInt("codigo_salida"));
				unaSalida.setConcepto(res.getString("concepto"));
				unaSalida.setCantidad(res.getBigDecimal("cantidad"));
				unaSalida.setFecha(res.getString("fecha"));
				unaSalida.setUsuario(res.getString("usuario"));
				unaSalida.setEstado(res.getString("estado"));
			
			 }
					
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
		
		return unaSalida;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
	
	public boolean actualizarEstado(SalidaCaja salida){
		int resultado;
		
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET estado = ? WHERE codigo_salida = ?");
			
			psConsultas.setString(1,salida.getEstado());
			
			psConsultas.setInt(2, salida.getCodigoSalida());
			
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
				
				if(psConsultas != null)psConsultas.close();
	            if(conn != null) conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
			excepcionSql.printStackTrace();
			
			} // fin de catch
		} // fin de finally
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
