package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.CuentaFactura;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaFacturaDao extends ModeloDaoBasic {
	private String sqlBaseJoin=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;

	public CuentaFacturaDao() {
		super("cuentas_facturas", "codigo_cuenta");
		
		sqlBaseJoin="SELECT "
				+ super.tableName+".codigo_cuenta, "
				+ super.tableName+".fecha, "
				+ super.tableName+".fecha_vencimiento, "
				+ super.tableName+".no_factura, "
				+ super.tableName+".codigo_caja, "
				+ super.tableName+ ".codigo_cliente, "
				+ " cliente.nombre_cliente, "
				+ " cliente.rtn,"
				+ " f_saldo_factura_cliente(cuentas_facturas.codigo_cuenta) as saldo ,"
				+ " cuenta2.saldo2 "
		+ " FROM "
				+super.DbName+ "."+super.tableName
			+ " JOIN "+super.DbName+".cliente "
					+ " on (cliente.codigo_cliente="+ super.tableName+".codigo_cliente) "
			+ " JOIN ( SELECT "+super.tableName+".codigo_cuenta, ifnull("+super.DbName+ "."+"f_saldo_factura_cliente("+super.tableName+".codigo_cuenta),0.00 ) saldo2 FROM "+super.DbName+ "."+super.tableName+") cuenta2 "
				+ " on( cuenta2.codigo_cuenta="+super.tableName+".codigo_cuenta)";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		CuentaFactura cuenta=(CuentaFactura)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo_cuenta= ?");
			super.psConsultas.setInt( 1, cuenta.getCodigoCuenta() );
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
		//return false;
	}

	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		CuentaFactura cuentaFactura=(CuentaFactura)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			//fsfsf
			super.psConsultas=conn.prepareStatement(super.getQueryInsert()+"(fecha,fecha_vencimiento,codigo_cliente,no_factura,codigo_caja) VALUES (now(),DATE_ADD(now(), INTERVAL (SELECT dia_vencimiento_factura from config_app LIMIT 1) DAY),?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setInt(1, cuentaFactura.getCodigoCliente());
			super.psConsultas.setInt( 2, cuentaFactura.getNoFactura());
			super.psConsultas.setInt(3, cuentaFactura.getCodigoCaja());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				cuentaFactura.setCodigoCuenta(rs.getInt(1));
	
					
					
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
		return false;
	}
	@Override
	public List<CuentaFactura> todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQueryRecord());
			super.psConsultas.setInt(1, limSupe);
			super.psConsultas.setInt(2, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente2);
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}

	@Override
	public List<CuentaFactura> buscarPorId(int codigo) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cuentas_facturas.codigo_cuenta=? and cuenta2.saldo2<>0 order by cuentas_facturas.codigo_cuenta asc");
			super.psConsultas.setInt(1, codigo);
			
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente=new Cliente();
				
				cliente.setId(res.getInt("codigo_cliente"));
				cliente.setNombre(res.getString("nombre_cliente"));
				cliente.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente);
				unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();

			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}
	
	public CuentaFactura buscarPorId(int codigoCliente, int codigoCaja, int nofactura) {
		// TODO Auto-generated method stub
		CuentaFactura unaCuenta=new CuentaFactura();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where cuentas_facturas.codigo_cliente=? and cuenta2.saldo2<>0 order by cuentas_facturas.codigo_cuenta asc");
			super.psConsultas.setInt(1, codigoCliente);
			
			System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente=new Cliente();
				
				cliente.setId(res.getInt("codigo_cliente"));
				cliente.setNombre(res.getString("nombre_cliente"));
				cliente.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente);
				
	
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return unaCuenta;
			}
			else return null;
	}
	
	
	public List<CuentaFactura> buscarConSaldo(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			
			//dsfa
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			//super.psConsultas.setInt(1, 0);
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente2);
				
				unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXidCliente(int codigo) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where saldo2>0 and cliente.codigo_cliente=? and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			super.psConsultas.setInt(1, codigo);
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente2);
				
				unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXnombreCliente(String nombre) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where saldo2>0 and cliente.nombre_cliente like ? and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			psConsultas.setString(1, "%" + nombre + "%");
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente2);
				
				unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}
	
	public List<CuentaFactura> buscarConSaldoXfecha(String fecha1,String fecha2) {
		// TODO Auto-generated method stub
		List<CuentaFactura> cajas=new ArrayList<CuentaFactura>();
		
		ResultSet res=null;
		
		Connection conn=null;
		
		boolean existe=false;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas = conn.prepareStatement(super.getQuerySelect()+" where saldo2>0 and fecha BETWEEN ? and ? and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//super.psConsultas = conn.prepareStatement(super.getQuerySearch("saldo",">"));//+" where saldo2>0 and CURDATE() > DATE_ADD(cuentas_facturas.fecha, INTERVAL (select dia_vencimiento_factura from config_app limit 1) DAY) ");
			//dd
			psConsultas.setString(1, fecha1);
			psConsultas.setString(2, fecha2);
			//super.psConsultas.setInt(2, limSupe);
			//super.psConsultas.setInt(3, limInf);
			//System.out.println(psConsultas);
			res = super.psConsultas.executeQuery();
			
			while(res.next()){
				CuentaFactura unaCuenta=new CuentaFactura();
				
				
				unaCuenta.setCodigoCuenta(res.getInt("codigo_cuenta"));
				unaCuenta.setCodigoCaja(res.getInt("codigo_caja"));
				unaCuenta.setNoFactura(res.getInt("no_factura"));
				unaCuenta.setCodigoCliente(res.getInt("codigo_cliente"));
				unaCuenta.setFecha(res.getDate("fecha"));
				unaCuenta.setSaldo(res.getBigDecimal("saldo"));
				unaCuenta.setFechaVenc(res.getDate("fecha_vencimiento"));
				
				
				Cliente cliente2=new Cliente();
				
				cliente2.setId(res.getInt("codigo_cliente"));
				cliente2.setNombre(res.getString("nombre_cliente"));
				cliente2.setRtn(res.getString("rtn"));
				
				unaCuenta.setCliente(cliente2);
				
				unaCuenta.setUltimoPago(cuentaXCobrarFacturaDao.getUltimoPago(unaCuenta));
				
				cajas.add(unaCuenta);
				existe=true;
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
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
				return cajas;
			}
			else return null;
	}


		

}
