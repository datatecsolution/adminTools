package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.FacturaCompra;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import net.datatecsolution.admin_tools.modelo.Proveedor;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FacturaCompraDao extends ModeloDaoBasic{
	
	//private ProveedorDao myProveedorDao=null;
	//private KardexDao kardexDao=null;
	private CuentaPorPagarDao myCuentaPagarDao=null;
	private PrecioArticuloDao preciosDao=null;
	private DetalleFacturaProveedorDao detallesDao;
	private String sqlBaseJoin=null;
	public FacturaCompraDao(){
		super("encabezado_factura_compra","numero_compra");
		detallesDao=new DetalleFacturaProveedorDao();
		//myProveedorDao=new ProveedorDao();
		preciosDao=new PrecioArticuloDao();
		myCuentaPagarDao=new CuentaPorPagarDao();
		
		sqlBaseJoin="SELECT encabezado_factura_compra.numero_compra, "
							+ " encabezado_factura_compra.no_factura_compra, "
							+ " date_format( "
										+ " encabezado_factura_compra.fecha, "
										+ " '%d/%m/%Y' "
										+ " ) AS fecha, "
							+ " proveedor.codigo_proveedor,"
							+ " proveedor.nombre_proveedor, "
							+ " proveedor.direccion, "
							+ " proveedor.telefono, "
							+ " proveedor.celular, "
							+ " encabezado_factura_compra.subtotal, "
							+ " encabezado_factura_compra.impuesto, "
							+ " encabezado_factura_compra.total, "
							+ " encabezado_factura_compra.estado_factura, "
							+ " encabezado_factura_compra.isv18, "
							+ " encabezado_factura_compra.usuario, "
							+ " encabezado_factura_compra.pago, "
							+ " encabezado_factura_compra.tipo_factura, "
							+ " tipo_factura.tipo_factura AS tipo_factura_descripcion,"
							+ " date_format( "
											+ " encabezado_factura_compra.fecha_ingreso,"
											+ " '%d/%m/%Y'"
											+ " ) AS fecha_ingreso, "
							+ "date_format( "
											+ " encabezado_factura_compra.fecha_vencimiento, "
											+ " '%d/%m/%Y'"
											+ ") AS fecha_vencimiento, "
							+ "encabezado_factura_compra.agrega_kardex "
				+ " FROM "
						+ super.DbName+". encabezado_factura_compra "
								+ " JOIN "
									+ super.DbName+".proveedor "
											+ " ON(proveedor.codigo_proveedor = encabezado_factura_compra.codigo_proveedor)"
								+ " JOIN "
									+ super.DbName+".tipo_factura "
											+ " ON(tipo_factura.id_tipo_factura = encabezado_factura_compra.tipo_factura)";
		super.setSqlQuerySelectJoin(sqlBaseJoin);	
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para agreagar facturas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	@Override
	public boolean registrar(Object c)
	{
		FacturaCompra fac=(FacturaCompra)c;
		boolean resultado;
		ResultSet res=null;
		Connection conn=null;
		int idFactura=0;
		
		try 
		{
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement( super.getQueryInsert()+" ("
					+ "fecha,"
					+ "subtotal,"
					+ "impuesto,"
					+ "total,"
					+ "codigo_proveedor,"
					+ "no_factura_compra,"
					+ "tipo_factura,"
					+ "fecha_vencimiento,"
					+ "estado_factura,"
					+ "subtotal_excento,"
					+ "subtotal15,"
					+ "subtotal18,"
					+ "isv18,"
					+ "fecha_ingreso) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,now())",java.sql.Statement.RETURN_GENERATED_KEYS);
			psConsultas.setString(1, fac.getFechaCompra());
			psConsultas.setBigDecimal(2, fac.getSubTotal());
			psConsultas.setBigDecimal(3, fac.getTotalImpuesto15());
			psConsultas.setBigDecimal(4, fac.getTotal());
			psConsultas.setInt(5, fac.getProveedor().getId());
			psConsultas.setString(6, fac.getIdFactura());
			psConsultas.setInt(7, fac.getTipoFactura());
			psConsultas.setString(8, fac.getFechaVencimento());
			psConsultas.setString(9, "ACT");
			psConsultas.setBigDecimal(10, fac.getSubTotalExcento());
			psConsultas.setBigDecimal(11, fac.getSubTotal15());
			psConsultas.setBigDecimal(12, fac.getSubTotal18());
			psConsultas.setBigDecimal(13, fac.getTotalImpuesto18());
			
			
			psConsultas.executeUpdate();
			res=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(res.next()){
				idFactura=res.getInt(1);
				fac.setNoCompra(idFactura);
			}
			
			//se recorren los detalles para guardalos 
			for(int x=0;x<fac.getDetalles().size();x++){
				//solo los articulos validos que no tengan id nulos
				if(fac.getDetalles().get(x).getArticulo().getId()>0){
					boolean resul=detallesDao.registrar(fac.getDetalles().get(x), idFactura,fac.getDepartamento().getId());
					
					//se crea el objeto precio venta para poder actualizarlo en la base de datos
					PrecioArticulo precioVenta=new PrecioArticulo();
					precioVenta.setCodigoArticulo(fac.getDetalles().get(x).getArticulo().getId());//se le asigna el codigo del articulo
					precioVenta.setPrecio(new BigDecimal(fac.getDetalles().get(x).getArticulo().getPrecioVenta()));//se le asigna el precio
					precioVenta.setCodigoPrecio(1);//se establece 1 porque es el codigo del precio del publico general
					
					this.preciosDao.actualizar(precioVenta);//se actualiza el precio
					
					PrecioArticulo precioCosto=null;
					for(int aa=0;aa<fac.getDetalles().get(x).getArticulo().getPreciosVenta().size();aa++){
		        		if(fac.getDetalles().get(x).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==4){
		        			precioCosto=fac.getDetalles().get(x).getArticulo().getPreciosVenta().get(aa);
		        		}
		        	}
					
					//se actualiza el precio costo en la base de datos
					if(fac.getDetalles().get(x).getArticulo().getPreciosVenta()!=null && precioCosto!=null){
						//el en la tabla el codigo tres es la base de datos
						//PrecioArticulo preciocosto=precioCosto
						this.preciosDao.actualizar(precioCosto);//se actualiza el precio
					}
				}
	
			}
			
			//si la factura es al credito se guarda el debito del cliente
			if(fac.getTipoFactura()==2){
				boolean resultado2=this.myCuentaPagarDao.reguistrarCredito(fac);
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
		
		return resultado;
	}
	
	public String getFechaSistema(){
		String fecha="";
		
		Statement instrucions=null;
		Connection conn=null;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			instrucions = conn.createStatement();
			ResultSet res=null;
			//res=instrucions.executeQuery(" SELECT DATE_FORMAT(now(), '%d/%m/%Y %h:%i:%s %p') as fecha;");
			res=instrucions.executeQuery(" SELECT DATE_FORMAT(now(), '%d/%m/%Y') as fecha;");
			while(res.next()){
				fecha=res.getString("fecha");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			try{
				//if(res != null) res.close();
                if(instrucions != null)instrucions.close();
                if(conn != null) conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
				//conexion.desconectar();
			} // fin de catch
		} // fin de finally
		
		
		
		
		return fecha;
	}
	
	
public List<FacturaCompra> buscarPorProveedor(String proveedor,int limitInferio, int canItemPag) {
		
		
		
		
        Connection con = null;
        
    	//String sql="SELECT * FROM encabezado_factura_compra INNER JOIN proveedor ON encabezado_factura_compra.codigo_proveedor = proveedor.codigo_proveedor where nombre_proveedor LIKE ? ";
     
       	List<FacturaCompra> facturas=new ArrayList<FacturaCompra>();
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearchJoin("nombre_proveedor", "LIKE", "proveedor", "codigo_proveedor", "codigo_proveedor"));
			psConsultas.setString(1,  "%" + proveedor + "%");
			psConsultas.setInt(2, limitInferio);
			psConsultas.setInt(3, canItemPag);
			
			
			res = psConsultas.executeQuery();
			while(res.next()){
				FacturaCompra unaFactura=new FacturaCompra();
				existe=true;
				unaFactura.setNoCompra(res.getInt("numero_compra"));
				unaFactura.setFechaCompra(res.getString("fecha"));
				
				unaFactura.setIdFactura(res.getString("no_factura_compra"));
				
				Proveedor unProveedor=new Proveedor();
				unProveedor.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				unProveedor.setTelefono(res.getString("telefono"));
				unProveedor.setCelular(res.getString("celular"));
				unProveedor.setDireccion(res.getString("direccion"));
				
				unaFactura.setProveedor(unProveedor);
				
				
				unaFactura.addSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.addTotalImpuesto15(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				//unaFactura.setTotalDescuento(res.getDouble("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				unaFactura.setDetalles(detallesDao.buscarPorId(res.getInt("numero_compra"))); //para cargar en la view los detalles de la factura
				
				
				facturas.add(unaFactura);
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
		
		
			if (existe) {
				return facturas;
			}
			else return null;
		
	}

	@Override
	public List<FacturaCompra> todos(int limInf,int limSupe) {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
       	List<FacturaCompra> facturas=new ArrayList<FacturaCompra>();
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQueryRecord());
			
			psConsultas.setInt(1, limSupe);
			psConsultas.setInt(2, limInf);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				FacturaCompra unaFactura=new FacturaCompra();
				existe=true;
				unaFactura.setNoCompra(res.getInt("numero_compra"));
				unaFactura.setFechaCompra(res.getString("fecha"));
				
				unaFactura.setIdFactura(res.getString("no_factura_compra"));
				
				Proveedor unProveedor=new Proveedor();
				unProveedor.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				unProveedor.setTelefono(res.getString("telefono"));
				unProveedor.setCelular(res.getString("celular"));
				unProveedor.setDireccion(res.getString("direccion"));
				
				unaFactura.setProveedor(unProveedor);
				
				
				unaFactura.addSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.addTotalImpuesto15(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				//unaFactura.setTotalDescuento(res.getDouble("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				unaFactura.setDetalles(detallesDao.buscarPorId(res.getInt("numero_compra"))); //para cargar en la view los detalles de la factura
				
				
				facturas.add(unaFactura);
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
		
		
			if (existe) {
				return facturas;
			}
			else return null;
		
	}

	public boolean anularFactura(FacturaCompra f) {
		// TODO Auto-generated method stub
		boolean resultado=false;
		Connection conn=null;
		String sql=super.getQueryUpdate()+" SET estado_factura=? WHERE numero_compra = ?";
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas=conn.prepareStatement(sql);
			
			
			psConsultas.setString(1, "NULA");
			
			psConsultas.setInt(2, f.getNoCompra());
			psConsultas.executeUpdate();
						
			
			resultado= true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			resultado=false;
		}
		return resultado;
	}

	public FacturaCompra buscarPorId(int id) {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
       
    	FacturaCompra unaFactura=new FacturaCompra();
		
		ResultSet res=null;
		
		boolean existe=false;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			
			psConsultas = con.prepareStatement(super.getQuerySearch("numero_compra", "="));
			psConsultas.setInt(1, id);
			psConsultas.setInt(2, 0);
			psConsultas.setInt(3, 1);
			
			res = psConsultas.executeQuery();
			while(res.next()){
				
				existe=true;
				unaFactura.setNoCompra(res.getInt("numero_compra"));
				unaFactura.setFechaCompra(res.getString("fecha"));
				
				unaFactura.setIdFactura(res.getString("no_factura_compra"));
				
				Proveedor unProveedor=new Proveedor();
				unProveedor.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				unProveedor.setTelefono(res.getString("telefono"));
				unProveedor.setCelular(res.getString("celular"));
				unProveedor.setDireccion(res.getString("direccion"));

				unaFactura.setProveedor(unProveedor);
				
				
				unaFactura.addSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.addTotalImpuesto15(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				//unaFactura.setTotalDescuento(res.getDouble("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				unaFactura.setDetalles(detallesDao.buscarPorId(res.getInt("numero_compra")));
				
				
			
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
		
		
			if (existe) {
				return unaFactura;
			}
			else return null;
		
	}

	public List<FacturaCompra> buscarPorFechas(String fecha1, String fecha2,int limitInferio, int canItemPag) {
		
		//se crear un referencia al pool de conexiones
		//DataSource ds = DBCPDataSourceFactory.getDataSource("mysql");
		
		
        Connection con = null;
        
    	
    	List<FacturaCompra> facturas=new ArrayList<FacturaCompra>();
		
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
				
				
				FacturaCompra unaFactura=new FacturaCompra();
				existe=true;
				unaFactura.setNoCompra(res.getInt("numero_compra"));
				unaFactura.setFechaCompra(res.getString("fecha"));
				unaFactura.setIdFactura(res.getString("no_factura_compra"));
				
				Proveedor unProveedor=new Proveedor();
				unProveedor.setId(Integer.parseInt(res.getString("codigo_proveedor")));
				unProveedor.setNombre(res.getString("nombre_proveedor"));
				unProveedor.setTelefono(res.getString("telefono"));
				unProveedor.setCelular(res.getString("celular"));
				unProveedor.setDireccion(res.getString("direccion"));
				
				unaFactura.setProveedor(unProveedor);
				
				
				unaFactura.addSubTotal(res.getBigDecimal("subtotal"));
				unaFactura.addTotalImpuesto15(res.getBigDecimal("impuesto"));
				unaFactura.setTotal(res.getBigDecimal("total"));
				//unaFactura.setEstado(res.getInt("estado_factura"));
				//unaFactura.setTotalDescuento(res.getDouble("descuento"));
				
				unaFactura.setEstado(res.getString("estado_factura"));
				unaFactura.setTipoFactura(res.getInt("tipo_factura"));
				unaFactura.setAgregadoAkardex(res.getInt("agrega_kardex"));
				
				unaFactura.setDetalles(detallesDao.buscarPorId(res.getInt("numero_compra")));
				
				
				facturas.add(unaFactura);
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
		
		
			if (existe) {
				return facturas;
			}
			else return null;
		
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
