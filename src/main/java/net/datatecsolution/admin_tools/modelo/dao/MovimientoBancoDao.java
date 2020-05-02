package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.CierreCaja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.MovimientoBanco;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovimientoBancoDao extends ModeloDaoBasic {

	
	public int idUltimoRequistro=0;
	private int idRegistrado;
	private String sqlBaseJoin=null;
	private CuentaBancosDao myCuentaBancosDao=null;
	
	public MovimientoBancoDao(){
		//Class(Conexion);
		super( "movimientos_bancos","codigo_movimiento");
		
		 myCuentaBancosDao=new CuentaBancosDao();
		
		sqlBaseJoin="SELECT `movimientos_bancos`.`codigo_movimiento`,  "
							+ " `movimientos_bancos`.`descripcion`,  "
							+ " `movimientos_bancos`.`id_tipo_movimiento`,  "
							+ " `movimientos_bancos`.`cantidad`, "
							+ " `movimientos_bancos`.`usuario` , "
							+ " `tipo_movimiento_bancos`.`tipo_movimiento` , "
							+ " date_format("
												+ "`movimientos_bancos`.`fecha`, '%d/%m/%Y'"
											+ ") AS `fecha`, "
							+ " entradas_caja.`fecha` AS `fecha1` "
					+ " FROM "+super.DbName+ ".`movimientos_bancos` "
						+ " JOIN "+super.DbName+".proveedor "
							+ " ON ( movimientos_bancos . id_tipo_movimiento = tipo_movimiento_bancos . id ) ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}
	
	
	public MovimientoBanco getMovimientoUltimoUser(){
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	//String sql="select * from cierre where usuario = ?";
    	
    	String sql2=super.sqlQuerySelectJoin+" WHERE movimientos_bancos.usuario=? ORDER BY movimientos_bancos.codigo_movimiento DESC LIMIT 1";
        //Statement stmt = null;
    	MovimientoBanco unaMovimiento=new MovimientoBanco();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(sql2);
			
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaMovimiento.setCodigo(res.getInt("codigo_entrada"));
				unaMovimiento.setDescripcion(res.getString("descripcion"));
				unaMovimiento.setCantidad(res.getBigDecimal("cantidad"));
				unaMovimiento.setFecha(res.getString("fecha"));
				unaMovimiento.setUsuario(res.getString("usuario"));
				unaMovimiento.setCodigoTipoMovimiento(res.getInt("id_tipo_movimiento"));
				
			
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
		
		return unaMovimiento;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
	}
	@Override
	public List<MovimientoBanco> todos(int limInf,int limSupe) {
		List<MovimientoBanco> movimientos =new ArrayList<MovimientoBanco>();
		
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
				MovimientoBanco unaMovimiento=new MovimientoBanco();
				
				unaMovimiento.setCodigo(res.getInt("codigo_entrada"));
				unaMovimiento.setDescripcion(res.getString("descripcion"));
				unaMovimiento.setCantidad(res.getBigDecimal("cantidad"));
				unaMovimiento.setFecha(res.getString("fecha"));
				unaMovimiento.setUsuario(res.getString("usuario"));
				unaMovimiento.setCodigoTipoMovimiento(res.getInt("id_tipo_movimiento"));
				
				
				movimientos.add(unaMovimiento);
				
				
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
			return movimientos;
		}
		else return null;
	}

	public void calcularTotal(CierreCaja unaCierre) {
		// TODO Auto-generated method stub
		
		
		Connection con = null;
		ResultSet res=null;
		
		//se consiguie el ultima salida que realizo el usuario
		MovimientoBanco ultimo=this.getMovimientoUltimoUser();
		
		//se establece la ultima salida del usuaurio
		//unaCierre.setNoEntradaFinal(ultimo.getCodigoEntrada());
		
		
		
		
		//corregir error poner ifnull a la sumAa
		String sql="SELECT ifnull(sum(movimientos_bancos.cantidad),0)AS cantidad FROM "+super.DbName+ ".movimientos_bancos WHERE movimientos_bancos.codigo_movimiento >= ? AND movimientos_bancos.codigo_movimiento <= ? and movimientos_bancos.usuario=? ";
		
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
				//unaCierre.setTotalEntrada(res.getBigDecimal("cantidad"));
				
	
			
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
		
		MovimientoBanco myMovimiento=(MovimientoBanco)c;
		// TODO Auto-generated method stub
		int resultado=0;
		ResultSet rs=null;
		Connection con = null;
	
		try 
		{
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=con.prepareStatement( super.getQueryInsert()+ " (descripcion,cantidad,usuario,fecha,codigo_cuenta,id_tipo_movimiento) VALUES (?,?,?,now(),?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			
			psConsultas.setString( 1, myMovimiento.getDescripcion());
			psConsultas.setBigDecimal( 2, myMovimiento.getCantidad().setScale(2, BigDecimal.ROUND_HALF_EVEN) );
			psConsultas.setString( 3, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(4, myMovimiento.getCodigoCuenta());
			psConsultas.setInt(5, myMovimiento.getCodigoTipoMovimiento());
			
			
			resultado=psConsultas.executeUpdate();
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				this.setIdRegistrado(rs.getInt(1));
				myMovimiento.setCodigo(this.getIdRegistrado());
			}
			
			if(myMovimiento.getCodigoTipoMovimiento()==1){
				//debito al cuenta de banco
				//o deposito de la cuenta
				myCuentaBancosDao.reguistrarDebitoMovimiento(myMovimiento);
				
			}
			if(myMovimiento.getCodigoTipoMovimiento()==2){
				//credito a la cuenta de banco
				//o retiro de la cuenta
				myCuentaBancosDao.reguistrarCreditoMovimiento(myMovimiento);
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

	private void setIdRegistrado(int i) {
		// TODO Auto-generated method stub
		idRegistrado=i;
	}
	public int getIdRegistrado(){
		return idRegistrado;
	} 
	
	@Override
	public MovimientoBanco buscarPorId(int id){
		
		
		
		
        Connection con = null;
        
    	
        MovimientoBanco unMovimiento=new MovimientoBanco();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("codigo_movimiento", "="));
			
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unMovimiento.setCodigo(res.getInt("codigo_movimiento"));
				unMovimiento.setDescripcion(res.getString("descripcion"));
				unMovimiento.setCantidad(res.getBigDecimal("cantidad"));
				unMovimiento.setFecha(res.getString("fecha"));
				unMovimiento.setUsuario(res.getString("usuario"));
				unMovimiento.setCodigoTipoMovimiento(res.getInt("id_tipo_movimiento"));
				
			
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
		
		return unMovimiento;
			/*if (existe) {
				return unaCierre;
			}
			else return null;*/
		
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
