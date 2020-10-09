package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Factura;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class FacturaOrdenVentaDao extends ModeloDaoBasic {
	
	
	
	private DetalleFacturaOrdenDao detallesDao=null;
	private ClienteDao myClienteDao=null;
	
	private Integer idFacturaGuardada=null;
	private String sqlJoin;
	
	
	public FacturaOrdenVentaDao(){
		
		super("encabezado_factura_temp","numero_factura");
		
		sqlJoin= " SELECT encabezado_factura_temp.numero_factura AS numero_factura, "
							+ " encabezado_factura_temp.fecha AS fecha1, "
							+ " date_format( encabezado_factura_temp.fecha, '%d/%m/%Y' )AS fecha, "
							+ " encabezado_factura_temp.subtotal_excento AS subtotal_excento, "
							+ " encabezado_factura_temp.subtotal15 AS subtotal15, "
							+ " encabezado_factura_temp.subtotal18 AS subtotal18, "
							+ " encabezado_factura_temp.subtotal AS subtotal, "
							+ " encabezado_factura_temp.impuesto AS impuesto, "
							+ " encabezado_factura_temp.total AS total, "
							+ " cliente.codigo_cliente AS codigo_cliente, "
							+ " cliente.nombre_cliente AS nombre_cliente, "
							+ " cliente.tipo_cliente AS tipo_cliente, "
							+ " encabezado_factura_temp.codigo AS codigo, "
							+ " encabezado_factura_temp.estado_factura AS estado_factura, "
							+ " encabezado_factura_temp.isvOtros AS isvOtros, "
							+ " encabezado_factura_temp.isv18 AS isv18, "
							+ " encabezado_factura_temp.usuario AS usuario, "
							+ " encabezado_factura_temp.codigo_caja AS codigo_caja, "
							+ " encabezado_factura_temp.pago AS pago, "
							+ " encabezado_factura_temp.descuento AS descuento, "
							+ " encabezado_factura_temp.tipo_factura AS tipo_factura, "
							+ " cliente.rtn AS rtn "
							+ " FROM "
								+ super.DbName+".encabezado_factura_temp "
									+ " JOIN "
									+ super.DbName+".cliente "
											+ " ON ( encabezado_factura_temp.codigo_cliente = cliente.codigo_cliente)";
		super.setSqlQuerySelectJoin(sqlJoin);
		
		detallesDao=new DetalleFacturaOrdenDao();
		myClienteDao=new ClienteDao();
	}
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar facturas temporal>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c){
		Factura myFactura=(Factura)c;
		boolean resultado=false;
		ResultSet rs=null;
		
		Connection conn=null;
		//int idFactura=0;
		
		String sql= super.getQueryInsert()+" ("
				+ "fecha,"
				+ "subtotal,"
				+ "impuesto,"
				+ "total,"
				+ "codigo_cliente,"
				+ "descuento,"
				+ "estado_factura,"
				+ "tipo_factura,"
				+ "usuario,"
				+ "subtotal_excento,"
				+ "subtotal15,"
				+ "subtotal18,"
				+ "isvOtros,"
				+ "codigo_caja)"
				+ " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		try 
		{
			String nombreCliente=myFactura.getCliente().getNombre();//"Consumidor final";
			
			//si el cliente en escrito por el usuario
			if(myFactura.getCliente().getId()<0){
				myClienteDao.registrarClienteContado(myFactura.getCliente());
				myFactura.getCliente().setId(myClienteDao.getIdClienteRegistrado());
				//JOptionPane.showMessageDialog(null,myClienteDao.getIdClienteRegistrado());
			}
			
			conn= ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setBigDecimal(1,myFactura.getSubTotal() );
			psConsultas.setBigDecimal(2, myFactura.getTotalImpuesto());
			psConsultas.setBigDecimal(3, myFactura.getTotal());
			psConsultas.setInt(4, myFactura.getCliente().getId());
			psConsultas.setBigDecimal(5, myFactura.getTotalDescuento());
			psConsultas.setString(6, "ACT");
			psConsultas.setInt(7, myFactura.getTipoFactura());
			psConsultas.setString(8, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setBigDecimal(9, myFactura.getSubTotalExcento());
			psConsultas.setBigDecimal(10, myFactura.getSubTotal15());
			psConsultas.setBigDecimal(11, myFactura.getSubTotal18());
			psConsultas.setBigDecimal(12, myFactura.getTotalOtrosImpuesto1());
			psConsultas.setInt(13, myFactura.getCodigoCaja());
			
			
			
			psConsultas.executeUpdate();//se guarda el encabezado de la factura
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				//idFactura=rs.getInt(1);
				idFacturaGuardada=rs.getInt(1);
				
			}
			
			//se guardan los detalles de la fatura
			for(int x=0;x<myFactura.getDetalles().size();x++){
				
				if(myFactura.getDetalles().get(x).getArticulo().getId()!=-1)
					detallesDao.agregarDetalleTemp(myFactura.getDetalles().get(x), idFacturaGuardada);
			}
			
			resultado= true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado= false;
		}
		finally
		{
			try{
				if(rs != null) rs.close();
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
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> facturasEnProceso(){
		
		
		
		
        Connection con = null;
       
       	List<Factura> facturas=new ArrayList<Factura>();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			//psConsultas = con.prepareStatement(super.getQuerySelect()+" where usuario=?;");
			//psConsultas = con.prepareStatement(super.getQuerySearch("usuario", "="));
			
			psConsultas = con.prepareStatement(super.getQuerySearchJoin("tipo_permiso=3 or usuario.usuario", "=", "usuario", "usuario", "usuario"));
			//psConsultas = con.prepareStatement(super.getQuerySearchJoin(campo, operador, tableJoin, campoTableJoin, campoJoin)
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 20);
			res = psConsultas.executeQuery();
			
			while(res.next()){
				Factura unaFactura=new Factura();
				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				
				unaFactura.setCliente(unCliente);
				
				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				unaFactura.setCodigoCaja(res.getInt("codigo_caja"));
				
				//unaFactura.setDetalles(detallesDao.detallesFacturaPendiente(unaFactura.getIdFactura()));
				
				
				facturas.add(unaFactura);
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
				return facturas;
			}
			else return null;
		
	}
	
	

	public Integer getIdFacturaGuardada() {
		// TODO Auto-generated method stub
		return idFacturaGuardada;
	}
	
	
	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		Factura fact=(Factura)c;
		int resultado=0;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement("DELETE FROM encabezado_factura_temp WHERE numero_factura = ?");
			psConsultas.setInt( 1, fact.getIdFactura() );
			resultado=psConsultas.executeUpdate();
			
			this.detallesDao.eliminar(fact);
			return true;
			
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				return false;
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
	}

	
	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		Factura factura=(Factura)c;
		
		boolean resultado=false;
		Connection conn=null;
		String sql=super.getQueryUpdate()+" "
				+ "SET fecha = now(),"
				+ " subtotal = ? , "
				+ "impuesto = ?, "
				+ "total=?, "
				+ "codigo_cliente=?,"
				
				+ "estado_factura=?,"
				+ "descuento=?,"
				+ "tipo_factura=?,"
				+ "usuario=?"
				+ " WHERE numero_factura = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(sql);
			
			//JOptionPane.showMessageDialog(null, f);
			psConsultas.setBigDecimal(1,factura.getSubTotal());
			psConsultas.setBigDecimal(2,factura.getTotalImpuesto());
			psConsultas.setBigDecimal(3,factura.getTotal());
			psConsultas.setInt(4, factura.getCliente().getId());
			psConsultas.setString(5, "ACT");
			
			psConsultas.setBigDecimal(6, factura.getTotalDescuento());
			
			psConsultas.setInt(7, factura.getTipoFactura());
			psConsultas.setString(8, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(9, factura.getIdFactura());
			psConsultas.executeUpdate();
			
			detallesDao.eliminar(factura);
			//se guardan los detalles de la fatura
			for(int x=0;x<factura.getDetalles().size();x++){
				
				if(factura.getDetalles().get(x).getArticulo().getId()!=-1)
					detallesDao.agregarDetalleTemp(factura.getDetalles().get(x),factura.getIdFactura());
			}
			
			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
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
	public List todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
