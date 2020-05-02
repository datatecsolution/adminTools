package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.CierreCaja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.EntradaCaja;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;



public class EntradaCajaDao extends ModeloDaoBasic {

	
	public int idUltimoRequistro=0;
	private int idRegistrado;
	private String sqlBaseJoin=null;
	private CuentaBancosDao myCuentaBancosDao=null;
	
	public EntradaCajaDao(){
		//Class(Conexion);
		super( "entradas_caja","codigo_entrada");
		
		 myCuentaBancosDao=new CuentaBancosDao();
		
		sqlBaseJoin="SELECT `entradas_caja`.`codigo_entrada`,  "
							+ " `entradas_caja`.`concepto`,  "
							+ " `entradas_caja`.`cantidad`, "
							+ " `entradas_caja`.`usuario` , "
							+ " `entradas_caja`.`estado` , "
							+ " date_format("
												+ "`entradas_caja`.`fecha`, '%d/%m/%Y'"
											+ ") AS `fecha`, "
							+ " entradas_caja.`fecha` AS `fecha1` "
					+ " FROM "+super.DbName+ ".`entradas_caja` ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	
	public EntradaCaja getEntradaUltimoUser(){
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2="SELECT * FROM "+super.DbName+".entradas_caja WHERE entradas_caja.usuario=? ORDER BY entradas_caja.codigo_entrada DESC LIMIT 1";
        //Statement stmt = null;
    	EntradaCaja unaEntrada=new EntradaCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaEntrada.setCodigoEntrada(res.getInt("codigo_entrada"));
				unaEntrada.setConcepto(res.getString("concepto"));
				unaEntrada.setCantidad(res.getBigDecimal("cantidad"));
				unaEntrada.setFecha(res.getString("fecha"));
				unaEntrada.setUsuario(res.getString("usuario"));
				unaEntrada.setEstado(res.getString("estado"));
			
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
		
		return unaEntrada;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
	@Override
	public List<EntradaCaja> todos(int limInf,int limSupe) {
		List<EntradaCaja> entradas =new ArrayList<EntradaCaja>();
		
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
				EntradaCaja unaEntrada=new EntradaCaja();
				
				unaEntrada.setCodigoEntrada(res.getInt("codigo_entrada"));
				unaEntrada.setConcepto(res.getString("concepto"));
				unaEntrada.setCantidad(res.getBigDecimal("cantidad"));
				unaEntrada.setFecha(res.getString("fecha"));
				unaEntrada.setUsuario(res.getString("usuario"));
				unaEntrada.setEstado(res.getString("estado"));
				
				
				
				entradas.add(unaEntrada);
				
				
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
			return entradas;
		}
		else return null;
	}

	public void calcularTotal(CierreCaja unaCierre) {
		// TODO Auto-generated method stub
		
		
		Connection con = null;
		ResultSet res=null;
		
		//se consiguie el ultima salida que realizo el usuario
		EntradaCaja ultimo=this.getEntradaUltimoUser();
		
		//se establece la ultima salida del usuaurio
		unaCierre.setNoEntradaFinal(ultimo.getCodigoEntrada());
		
		
		
		
		//corregir error poner ifnull a la sumAa
		String sql="SELECT ifnull(sum(entradas_caja.cantidad),0)AS cantidad FROM "+super.DbName+ ".entradas_caja WHERE entradas_caja.codigo_entrada >= ? AND entradas_caja.codigo_entrada <= ? and entradas_caja.usuario=? and estado='ACT'";
		
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql);
			
			psConsultas.setInt(1, unaCierre.getNoEntradaInicial());
			psConsultas.setInt(2, unaCierre.getNoEntradaFinal());
			psConsultas.setString(3, unaCierre.getUsuario());
			
			
			//seleccionarCierre.setString(1, conexion.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				
				
				//si existe alguna salida se suma y se establecen en el cierre
				unaCierre.setTotalEntrada(res.getBigDecimal("cantidad"));
				
	
			
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
		
		EntradaCaja myEntrada=(EntradaCaja)c;
		// TODO Auto-generated method stub
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
	
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+ " (concepto,cantidad,usuario,fecha,estado,codigo_cuenta) VALUES (?,?,?,now(),?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myEntrada.getConcepto());
			psConsultas.setBigDecimal( 2, myEntrada.getCantidad().setScale(2, BigDecimal.ROUND_HALF_EVEN) );
			psConsultas.setString( 3, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(4, "ACT");
			psConsultas.setInt(5, myEntrada.getCodigoCuenta());
			
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
				myEntrada.setCodigoEntrada(this.getIdRegistrado());
			}
			
			//se registra la salida de la cuenta selecciona
			myCuentaBancosDao.reguistrarCreditoEntradaCaja(myEntrada);
			
			//se registra el aumento de la cuenta efectivo
			myCuentaBancosDao.reguistrarDebitoEntradaCajaEfectivo(myEntrada);

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
	
	@Override
	public EntradaCaja buscarPorId(int id){
		
		
		
		
        Connection con = null;
        
    	
        EntradaCaja unaEntrada=new EntradaCaja();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("codigo_entrada", "="));
			
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaEntrada.setCodigoEntrada(res.getInt("codigo_entrada"));
				unaEntrada.setConcepto(res.getString("concepto"));
				unaEntrada.setCantidad(res.getBigDecimal("cantidad"));
				unaEntrada.setFecha(res.getString("fecha"));
				unaEntrada.setUsuario(res.getString("usuario"));
				unaEntrada.setEstado(res.getString("estado"));
			
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
		
		return unaEntrada;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
	
	public boolean actualizarEstado(EntradaCaja entrada){
		int resultado;
		
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET estado = ? WHERE codigo_entrada = ?");
			
			psConsultas.setString(1,entrada.getEstado());
			
			psConsultas.setInt(2, entrada.getCodigoEntrada());
			
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
