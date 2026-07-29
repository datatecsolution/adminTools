package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.*;

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
	private final String sqlJoin;
	
	
	public FacturaOrdenVentaDao(){
		
		super("encabezado_factura_temp","numero_factura");
		
		sqlJoin= " SELECT encabezado_factura_temp.numero_factura AS numero_factura, "
							+ " encabezado_factura_temp.fecha AS fecha1, "
							+ " date_format( encabezado_factura_temp.fecha, '%d/%m/%Y' )AS fecha, "
							+ " encabezado_factura_temp.subtotal_excento AS subtotal_excento, "
							+ " encabezado_factura_temp.subtotal15 AS subtotal15, "
							+ " encabezado_factura_temp.subtotal18 AS subtotal18, "
							+ " encabezado_factura_temp.subtotal AS subtotal, "
							+ " encabezado_factura_temp.codigo_vendedor AS codigo_vendedor, "
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
							+ " encabezado_factura_temp.observacion AS observacion, "
							+ " encabezado_factura_temp.estado AS estado, "
							+ " encabezado_factura_temp.tipo_factura AS tipo_factura, "
							+ " cliente.rtn AS rtn "
							+ " FROM "
								+ super.DbName+".encabezado_factura_temp "
									+ " JOIN "
									+ super.DbName+".cliente "
											+ " ON ( encabezado_factura_temp.codigo_cliente = cliente.codigo_cliente) ";
//									" INNER JOIN "
//									+ super.DbName+".empleados " +
//											" ON encabezado_factura_temp.codigo_vendedor = empleados.codigo_empleado";
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
				+ "codigo_vendedor,"
				+ "subtotal_excento,"
				+ "subtotal15,"
				+ "subtotal18,"
				+ "isvOtros,"
				+ "codigo_caja)"
				+ " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
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
			psConsultas.setInt(9, myFactura.getVendedor().getCodigo());
			psConsultas.setBigDecimal(10, myFactura.getSubTotalExcento());
			psConsultas.setBigDecimal(11, myFactura.getSubTotal15());
			psConsultas.setBigDecimal(12, myFactura.getSubTotal18());
			psConsultas.setBigDecimal(13, myFactura.getTotalOtrosImpuesto1());
			psConsultas.setInt(14, myFactura.getCodigoCaja());
			
			
			
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

			System.out.println(psConsultas);
			
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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);
				
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


	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> ordenesPorEmpleadosUsuarios(){




		Connection con = null;

		List<Factura> facturas=new ArrayList<Factura>();

		ResultSet res=null;

		boolean existe=false;

		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			String sqlOrdenes = super.getQuerySelect()
				+ " JOIN (SELECT encabezado_factura_temp.numero_factura FROM "
				+ super.DbName + ".encabezado_factura_temp LEFT JOIN "
				+ DbNameBase + ".empleados ON (encabezado_factura_temp.codigo_vendedor = empleados.codigo_empleado) "
				+ "WHERE encabezado_factura_temp.estado < 3 "
				+ "AND (empleados.usuario = ? OR encabezado_factura_temp.usuario = ?) "
				+ "ORDER BY encabezado_factura_temp.numero_factura DESC LIMIT ?,?) tabla2 "
				+ "ON (tabla2.numero_factura = encabezado_factura_temp.numero_factura) "
				+ "ORDER BY encabezado_factura_temp.numero_factura DESC";
			psConsultas = con.prepareStatement(sqlOrdenes);
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(2, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(3, 0);
			psConsultas.setInt(4, 20);

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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setEstado(res.getString("estado"));

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
	/**
	 * US-119 (Fase 3 stock reservado): actualizar la orden es ATÓMICO. El
	 * flujo histórico (UPDATE header + eliminar() + agregarDetalleTemp() en
	 * conexiones separadas con autocommit) dejaba una ventana sin detalles:
	 * la reserva de la orden desaparecía transitoriamente y, si el re-insert
	 * fallaba, la orden quedaba VACÍA. Ahora todo corre en UNA conexión con
	 * setAutoCommit(false): o queda el set nuevo completo, o rollback y los
	 * detalles previos quedan intactos.
	 */
	public boolean actualizar(Object c) {
		Factura factura=(Factura)c;
		boolean resultado=false;
		Connection conn=null;
		String sqlHeader=super.getQueryUpdate()+" "
				+ "SET fecha = now(),"
				+ " subtotal = ? , "
				+ "impuesto = ?, "
				+ "total=?, "
				+ "codigo_cliente=?,"
				+ "estado_factura=?,"
				+ "descuento=?,"
				+ "tipo_factura=?, "
				+ "estado=2 "
				+ " WHERE numero_factura = ?";
		String sqlDeleteDetalles="DELETE FROM "+super.DbName+".detalle_factura_temp WHERE numero_factura = ?";
		String sqlInsertDetalle="INSERT INTO "+super.DbName+".detalle_factura_temp ("
				+ "numero_factura,codigo_articulo,precio,cantidad,impuesto,subtotal,descuento,total"
				+ ") VALUES (?,?,?,?,?,?,?,?)";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			conn.setAutoCommit(false);

			try (java.sql.PreparedStatement psHeader=conn.prepareStatement(sqlHeader)) {
				psHeader.setBigDecimal(1,factura.getSubTotal());
				psHeader.setBigDecimal(2,factura.getTotalImpuesto());
				psHeader.setBigDecimal(3,factura.getTotal());
				psHeader.setInt(4, factura.getCliente().getId());
				psHeader.setString(5, "ACT");
				psHeader.setBigDecimal(6, factura.getTotalDescuento());
				psHeader.setInt(7, factura.getTipoFactura());
				psHeader.setInt(8, factura.getIdFactura());
				psHeader.executeUpdate();
			}

			try (java.sql.PreparedStatement psDelete=conn.prepareStatement(sqlDeleteDetalles)) {
				psDelete.setInt(1, factura.getIdFactura());
				psDelete.executeUpdate();
			}

			try (java.sql.PreparedStatement psInsert=conn.prepareStatement(sqlInsertDetalle)) {
				for(int x=0;x<factura.getDetalles().size();x++){
					if(factura.getDetalles().get(x).getArticulo().getId()==-1) continue;
					psInsert.setInt(1, factura.getIdFactura());
					psInsert.setInt(2, factura.getDetalles().get(x).getArticulo().getId());
					psInsert.setDouble(3, factura.getDetalles().get(x).getArticulo().getPrecioVenta());
					psInsert.setBigDecimal(4, factura.getDetalles().get(x).getCantidad());
					psInsert.setBigDecimal(5, factura.getDetalles().get(x).getImpuesto());
					psInsert.setBigDecimal(6, factura.getDetalles().get(x).getSubTotal());
					psInsert.setBigDecimal(7, factura.getDetalles().get(x).getDescuentoItem());
					psInsert.setBigDecimal(8, factura.getDetalles().get(x).getTotal());
					psInsert.addBatch();
				}
				psInsert.executeBatch();
			}

			conn.commit();
			resultado= true;
		} catch (SQLException e) {
			e.printStackTrace();
			if(conn!=null){
				try{ conn.rollback(); }catch(SQLException ex){ ex.printStackTrace(); }
			}
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}
		finally
		{
			try{
				if(conn != null){ conn.setAutoCommit(true); conn.close(); }
			}
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
			}
		}
		return resultado;
	}

	@Override
	public List<Factura> todos(int limInf, int limSupe) {
		Connection con = null;


		List<Factura> facturas=new ArrayList<Factura>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			String sqlTodos = super.getQuerySelect()
				+ " JOIN (SELECT encabezado_factura_temp.numero_factura FROM "
				+ super.DbName + ".encabezado_factura_temp LEFT JOIN "
				+ DbNameBase + ".empleados ON (encabezado_factura_temp.codigo_vendedor = empleados.codigo_empleado) "
				+ "WHERE encabezado_factura_temp.estado < 3 "
				+ "AND (empleados.usuario = ? OR encabezado_factura_temp.usuario = ?) "
				+ "ORDER BY encabezado_factura_temp.numero_factura DESC LIMIT ?,?) tabla2 "
				+ "ON (tabla2.numero_factura = encabezado_factura_temp.numero_factura) "
				+ "ORDER BY encabezado_factura_temp.numero_factura DESC";
			psConsultas = con.prepareStatement(sqlTodos);
			psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setString(2, ConexionStatic.getUsuarioLogin().getUser());
			psConsultas.setInt(3, limSupe);
			psConsultas.setInt(4, limInf);


			System.out.println(psConsultas);

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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));


				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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

	public List<Factura> todasSinFiltro(int limitInferior, int canItemPag) {
		Connection con = null;
		List<Factura> facturas = new ArrayList<Factura>();
		ResultSet res = null;
		boolean existe = false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			String sqlTodas = super.getQuerySelect()
				+ " ORDER BY encabezado_factura_temp.numero_factura DESC LIMIT ?,?";
			psConsultas = con.prepareStatement(sqlTodas);
			psConsultas.setInt(1, limitInferior);
			psConsultas.setInt(2, canItemPag);

			System.out.println("[DEBUG todasSinFiltro] SQL: " + psConsultas);

			res = psConsultas.executeQuery();

			while (res.next()) {
				Factura unaFactura = new Factura();
				existe = true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente = new Cliente();
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				unaFactura.setCliente(unCliente);

				Empleado unEmpleado = new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));
				unaFactura.setVendedor(unEmpleado);

				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				unaFactura.setCodigoCaja(res.getInt("codigo_caja"));
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));

				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
		} finally {
			super.DbName = DbNameBase;
			try {
				if (res != null) res.close();
				if (psConsultas != null) psConsultas.close();
				if (con != null) con.close();
			} catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
			}
		}

		if (existe) {
			return facturas;
		} else return null;
	}

	public List<Factura> buscarPorClienteSinFiltroVendedor(String nombre, int limitInferior, int canItemPag) {
		Connection con = null;
		List<Factura> facturas = new ArrayList<Factura>();
		ResultSet res = null;
		boolean existe = false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();

			String sql = super.getQuerySelect()
				+ " WHERE cliente.nombre_cliente LIKE ? "
				+ "ORDER BY encabezado_factura_temp.numero_factura DESC LIMIT ?,?";
			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, "%" + nombre + "%");
			psConsultas.setInt(2, limitInferior);
			psConsultas.setInt(3, canItemPag);

			System.out.println("[DEBUG buscarPorClienteSinFiltroVendedor] SQL: " + psConsultas);

			res = psConsultas.executeQuery();

			while (res.next()) {
				Factura unaFactura = new Factura();
				existe = true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente = new Cliente();
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));
				unaFactura.setCliente(unCliente);

				Empleado unEmpleado = new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));
				unaFactura.setVendedor(unEmpleado);

				unaFactura.setFecha(res.getString("fecha"));
				unaFactura.setSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.setTotalImpuesto(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				unaFactura.setTotalDescuento(res.getBigDecimal("descuento"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setSubTotalExcento(res.getBigDecimal("subtotal_excento"));
				unaFactura.setSubTotal15(res.getBigDecimal("subtotal15"));
				unaFactura.setSubTotal18(res.getBigDecimal("subtotal18"));
				unaFactura.setTotalOtrosImpuesto(res.getBigDecimal("isvOtros"));
				unaFactura.setCodigoCaja(res.getInt("codigo_caja"));
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));

				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
		} finally {
			super.DbName = DbNameBase;
			try {
				if (res != null) res.close();
				if (psConsultas != null) psConsultas.close();
				if (con != null) con.close();
			} catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
			}
		}

		if (existe) {
			return facturas;
		} else return null;
	}

	public List<Factura> todosConEliminados(String nombre,int limitInferio, int canItemPag) {


		Connection con = null;


		List<Factura> facturas=new ArrayList<Factura>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			psConsultas = con.prepareStatement(super.getQuerySearchJoin("encabezado_factura_temp.estado<3 and codigo_vendedor=? and nombre_cliente", "LIKE", "cliente", "codigo_cliente", "codigo_cliente"));

			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			psConsultas.setString(2, "%" + nombre + "%");
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);

			System.out.println(psConsultas);

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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));


				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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

	@Override
	public Factura buscarPorId(int id) {


		Connection con = null;

		Factura unaFactura=new Factura();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			psConsultas = con.prepareStatement(super.getQuerySearch("numero_factura", "="));

			//psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);

			System.out.println(psConsultas);

			res = psConsultas.executeQuery();

			while(res.next()){

				existe=true;
				unaFactura.setIdFactura(res.getInt("numero_factura"));
				Cliente unCliente=new Cliente();//myClienteDao.buscarCliente(res.getInt("codigo_cliente"));
				unCliente.setId(res.getInt("codigo_cliente"));
				unCliente.setNombre(res.getString("nombre_cliente"));
				unCliente.setTipoCliente(res.getInt("tipo_cliente"));
				unCliente.setRtn(res.getString("rtn"));

				unaFactura.setCliente(unCliente);

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));

			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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
			return unaFactura;
		}
		else return null;

	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> buscarPorNombreCliente(String nombre,int limitInferio, int canItemPag){


		Connection con = null;


		List<Factura> facturas=new ArrayList<Factura>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			int codigoVendedor = ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo();

			if (codigoVendedor == 0) {
				String sqlBuscarTodos = super.getQuerySelect()
					+ " JOIN (SELECT encabezado_factura_temp.numero_factura FROM "
					+ super.DbName + ".encabezado_factura_temp "
					+ "LEFT JOIN " + DbNameBase + ".empleados ON (encabezado_factura_temp.codigo_vendedor = empleados.codigo_empleado) "
					+ "JOIN " + super.DbName + ".cliente ON (encabezado_factura_temp.codigo_cliente = cliente.codigo_cliente) "
					+ "WHERE encabezado_factura_temp.estado < 3 "
					+ "AND (empleados.usuario = ? OR encabezado_factura_temp.usuario = ?) "
					+ "AND cliente.nombre_cliente LIKE ? "
					+ "ORDER BY encabezado_factura_temp.numero_factura DESC LIMIT ?,?) tabla2 "
					+ "ON (tabla2.numero_factura = encabezado_factura_temp.numero_factura) "
					+ "ORDER BY encabezado_factura_temp.numero_factura DESC";
				psConsultas = con.prepareStatement(sqlBuscarTodos);
				psConsultas.setString(1, ConexionStatic.getUsuarioLogin().getUser());
				psConsultas.setString(2, ConexionStatic.getUsuarioLogin().getUser());
				psConsultas.setString(3, "%" + nombre + "%");
				psConsultas.setInt(4, limitInferio);
				psConsultas.setInt(5, canItemPag);
			} else {
				psConsultas = con.prepareStatement(super.getQuerySearchJoin("encabezado_factura_temp.estado<3 and codigo_vendedor=? and nombre_cliente", "LIKE", "cliente", "codigo_cliente", "codigo_cliente"));
				psConsultas.setInt(1, codigoVendedor);
				psConsultas.setString(2, "%" + nombre + "%");
				psConsultas.setInt(3, limitInferio);
				psConsultas.setInt(4, canItemPag);
			}

			System.out.println(psConsultas);

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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));


				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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


	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public List<Factura> buscarPorVendedorCliente(String nombre,int limitInferio, int canItemPag){


		Connection con = null;


		List<Factura> facturas=new ArrayList<Factura>();

		ResultSet res=null;

		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();


			psConsultas = con.prepareStatement(super.getQuerySearchJoin("codigo_vendedor=? and nombre_cliente", "LIKE", "cliente", "codigo_cliente", "codigo_cliente"));

			psConsultas.setInt(1, ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo());
			psConsultas.setString(2, "%" + nombre + "%");
			psConsultas.setInt(3, limitInferio);
			psConsultas.setInt(4, canItemPag);

			System.out.println(psConsultas);

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

				Empleado unEmpleado=new Empleado();
				unEmpleado.setCodigo(res.getInt("codigo_vendedor"));

				unaFactura.setVendedor(unEmpleado);

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
				unaFactura.setObservacion(res.getString("observacion"));
				unaFactura.setEstado(res.getString("estado"));


				facturas.add(unaFactura);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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

	public boolean cambiarEstado(Factura f,int estado) {
		// TODO Auto-generated method stub

		boolean resultado=false;
		Connection conn=null;

		String sql=super.getQueryUpdate()+" SET "


				+ "estado=?"

				+ " WHERE numero_factura = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();

			psConsultas=conn.prepareStatement(sql);


			psConsultas.setInt(1, estado);

			psConsultas.setInt(2, f.getIdFactura());
			psConsultas.executeUpdate();




			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}
		finally
		{
			//se restablece el nombre de la base de datos por defecto
			super.DbName= DbNameBase;
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

	

}
