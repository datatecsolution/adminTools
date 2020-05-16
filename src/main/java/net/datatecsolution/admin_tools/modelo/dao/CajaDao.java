package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.Usuario;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CajaDao extends ModeloDaoBasic {
	private String sqlBaseJoin=null;

	public CajaDao() {
		super("cajas", "codigo");
		
		sqlBaseJoin="SELECT "
							+ " cajas.codigo, "
							+ " cajas.codigo_bodega, "
							+ " cajas.descripcion, "
							+ " cajas.nombre_db, "
							+ " bodega.descripcion_bodega "
					+ " FROM "
							+super.DbName+ "."+super.tableName
						+ " JOIN "+super.DbName+".bodega "
						+ " on (bodega.codigo_bodega="+ super.tableName+".codigo_bodega) ";
		super.setSqlQuerySelectJoin(sqlBaseJoin);
	}

	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		Caja caja=(Caja)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryDelete()+" WHERE codigo= ?");
			super.psConsultas.setInt( 1, caja.getCodigo() );
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
		Caja caja=(Caja)c;
		ResultSet rs=null;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryInsert()+"(descripcion,codigo_bodega,nombre_db) VALUES (?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			super.psConsultas.setString(1, caja.getDescripcion());
			super.psConsultas.setInt( 2, caja.getCodigoBodega());
			super.psConsultas.setString(3, caja.getNombreBd());
			psConsultas.executeUpdate();
			
			
			rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
			while(rs.next()){
				caja.setCodigo(rs.getInt(1));
				
				//se establece el nombre de la base de datos[prefijo + codigo de registro]
				caja.setNombreBd(dbNameCaja+caja.getCodigo());
				
				//se actualiza la caja con el numero nombre de la base de datos
				this.actualizar(caja);
				
				//se crea la base de datos para la facturacion
				boolean resul=crearDataBase(caja.getCodigo());
				
				//si se creo correctamente la base de datos se crean las tablas y sus triger correpondientes
				if(resul){
						
					//se crea la tabla encabezado factura
					crearEncabezadoTabla(caja.getNombreBd());
					
					//se crea la tabla detalle de factura
					this.crearDetalleTabla(caja.getNombreBd());
					
					//se crea el trigger para la tabla detalle que maneja el inventario
					crearTriggerDetalle(caja);
					
					//se crea la tabla encabezado fatura temporal
					//this.crearEncabezadoTemTabla(caja.getNombreBd());
					
					//se crea la tabla detalle de factura temporar
					//this.crearDetalleTemTabla(caja.getNombreBd());
					
					
					
					//se crea la tabla sobre los datos de la facturacion
					this.crearDatosFacturaTabla(caja.getNombreBd());
					
					
				}
			}
			//para que actualice el nombre de la base de datos
			this.actualizar(caja);
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
		Caja caja=(Caja)c;
		
		//int resultado=0;
		Connection conn=null;
		
		try {
			
			//JOptionPane.showMessageDialog(null, "Datos Caja en modelo dao:"+caja.toString(),"Exito",JOptionPane.INFORMATION_MESSAGE);
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET descripcion=?,codigo_bodega=?, nombre_db=? WHERE codigo=?");
			super.psConsultas.setString(1, caja.getDescripcion());
			super.psConsultas.setInt( 2, caja.getCodigoBodega());
			super.psConsultas.setString( 3, caja.getNombreBd());
			super.psConsultas.setInt( 4, caja.getCodigo());
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
	@Override
	public List<Caja> todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		List<Caja> cajas=new ArrayList<Caja>();
		
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
				Caja unaCaja=new Caja();
				
				unaCaja.setCodigo(res.getInt("codigo"));
				unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
				unaCaja.setDescripcion(res.getString("descripcion"));
				unaCaja.setNombreBd(res.getString("nombre_db"));
				
				Departamento dept=new Departamento();
				
				dept.setId((res.getInt("codigo_bodega")));
				dept.setDescripcion(res.getString("descripcion_bodega"));
				
				unaCaja.setDetartamento(dept);
				cajas.add(unaCaja);
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

	public boolean crearDataBase(int codigoCaja){
		
		//int resultado=0;
				Connection conn=null;
				
				try {
					conn=ConexionStatic.getPoolConexion().getConnection();
					super.psConsultas=conn.prepareStatement("CREATE DATABASE " +dbNameCaja+codigoCaja);
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

	public boolean crearEncabezadoTabla(String nombreBd){
		
		
		
		Connection conn=null;
		String sql="Create table if not exists "+nombreBd+".`encabezado_factura` ( "
				+ "`numero_factura` int(11) NOT NULL AUTO_INCREMENT,"
				+ " `fecha` date NOT NULL,"
				+ "`subtotal_excento` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`subtotal15` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`subtotal18` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`subtotal` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`impuesto` float(8,2) NOT NULL,"
				+ "`total` float(10,2) NOT NULL,"
				+ "`codigo_cliente` int(11) NOT NULL,"
				+ "`codigo` varchar(11) NOT NULL DEFAULT '-1',"
				+ "`estado_factura` varchar(25) NOT NULL DEFAULT 'NA',"
				+ "`isvOtros` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`isv18` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',"
				+ "`pago` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`descuento` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`tipo_factura` int(11) NOT NULL DEFAULT '1',"
				+ "`agrega_kardex` int(11) NOT NULL DEFAULT '0',"
				+ "`tipo_pago` int(11) NOT NULL,"
				+ "`observacion` varchar(255) NOT NULL DEFAULT 'NA',"
				+ "`total_letras` varchar(500) NOT NULL DEFAULT 'NA',"
				+ "`codigo_vendedor` int(11) NOT NULL DEFAULT '1',"
				+ "`estado_pago` int(1) NOT NULL DEFAULT '0',"
				+ "`cod_rango` int(2) NOT NULL DEFAULT '1',"
				+ "`cobro_tarjeta` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`cobro_efectivo` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`fecha_vencimiento` date NOT NULL DEFAULT '1990-01-01',"
				+ "PRIMARY KEY (`numero_factura`),"
				+ "UNIQUE KEY `numero_factura` (`numero_factura`) USING BTREE,"
				+ " KEY `codigo_cliente` (`codigo_cliente`) USING BTREE,"
				+ "KEY `tipo_factura` (`tipo_factura`) USING BTREE,"
				+ "KEY `usuario` (`usuario`) USING BTREE ) "
				+ "ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(sql);
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
	
	public boolean crearDetalleTabla(String nombreBd){
		
		
		
		Connection conn=null;
		String sql="Create table if not exists "+nombreBd+".`detalle_factura` ("
				+ "`numero_factura` int(11) NOT NULL,"
				+ "`codigo_articulo` int(11) NOT NULL,"
				+ "`precio` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`cantidad` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`impuesto` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`subtotal` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`descuento` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`total` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
				+ "`codigo_barra` varchar(255) NOT NULL DEFAULT 'NA',"
				+ "`agrega_kardex` int(2) NOT NULL DEFAULT '0',"
				+ " PRIMARY KEY (`id`),"
				+ "KEY `codigo_articulo` (`codigo_articulo`) USING BTREE,"
				+ "KEY `numero_factura` (`numero_factura`) USING BTREE"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(sql);
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
	
	public boolean crearTriggerDetalle(Caja caja){
		
		
		
		Connection conn=null;
		Statement stmt =null;
		String sql=" CREATE TRIGGER "+caja.getNombreBd()+".detalle_factura_b_insert BEFORE INSERT ON "+caja.getNombreBd()+".detalle_factura FOR EACH ROW"
				+ " BEGIN "
				+ " declare cod_kardex int; "
				+ " declare tipo_articulo int; "
				
				+ " set cod_kardex =(SELECT codigo_kardex FROM admin_tools.articulo_kardex WHERE (codigo_articulo = NEW.codigo_articulo AND	codigo_bodega = "+caja.getCodigoBodega()+") limit 1); "
				
				+ "set tipo_articulo=(SELECT t1.tipo_articulo from admin_tools.articulo t1 where t1.codigo_articulo=NEW.codigo_articulo LIMIT 1);"

				+ " if(cod_kardex is null) then  "
						
						+ " if( tipo_articulo =1) then"

								+ " INSERT INTO admin_tools.articulo_kardex(codigo_articulo,codigo_bodega) VALUES (NEW.codigo_articulo,"+caja.getCodigoBodega()+");"
							
								+ " set cod_kardex=(select last_insert_id());"
								+ " CALL admin_tools.crear_ajuste_inventario_kardex(cod_kardex,NEW.cantidad,NEW.precio,\'facturado en "+caja.getNombreBd()+"\');"
								+ " call admin_tools.crear_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad);"
								+ " set NEW.agrega_kardex=1; "
						+ " end if; "
						+ " if( tipo_articulo =2) then "
								+ " CALL admin_tools.crear_venta_insumo_kardex("+caja.getCodigoBodega()+",NEW.codigo_articulo,NEW.cantidad,\'facturado en "+caja.getNombreBd()+"\',NEW.numero_factura); "
								+ " set NEW.agrega_kardex=1; "
						+ " end if;"
				+ " ELSE "
						+ " if( tipo_articulo =1) then"
								+ " call admin_tools.crear_venta_kardex(cod_kardex,NEW.numero_factura,NEW.cantidad);"
								+ " set NEW.agrega_kardex=1; "
						+ " end if; "
						+ " if( tipo_articulo =2) then "
								+ " CALL admin_tools.crear_venta_insumo_kardex("+caja.getCodigoBodega()+",NEW.codigo_articulo,NEW.cantidad,\'facturado en "+caja.getNombreBd()+"\',NEW.numero_factura); "
								+ " set NEW.agrega_kardex=1; "
						+ " end if; "
				+ " end if; "
				+ " end;";
		
		System.out.println(sql);
		
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			stmt = conn.createStatement();
			//super.psConsultas=conn.prepareStatement(sql);
			//psConsultas.executeUpdate();
			
			stmt.execute(sql);
			return true;
			
		} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
		}
		finally{
				try{
								
					//if(res != null) res.close();
	                if(stmt != null)stmt.close();
	                if(conn != null) conn.close();
	                
					
					} // fin de try
					catch ( SQLException excepcionSql )
					{
						excepcionSql.printStackTrace();
						//Sconexion.desconectar();
					} // fin de catch
		}
		
	}
	
public boolean crearEncabezadoTemTabla(String nombreBd){
		
		
		
		Connection conn=null;
		String sql="Create table if not exists "+nombreBd+".`encabezado_factura_temp` ("
				+ "`numero_factura` int(11) NOT NULL AUTO_INCREMENT,"
				+ "`fecha` date NOT NULL,"
				+ "`subtotal_excento` float(8,0) NOT NULL DEFAULT '0',"
				+ "`subtotal15` float(8,0) NOT NULL DEFAULT '0',"
				+ "`subtotal18` float(8,0) NOT NULL DEFAULT '0',"
				+ "`subtotal` float(8,2) NOT NULL DEFAULT '0.00',"
				+ "`impuesto` float(8,2) NOT NULL,"
				+ "`total` float(10,2) NOT NULL DEFAULT '0.00',"
				+ "`codigo_cliente` int(18) NOT NULL DEFAULT '1',"
				+ "`codigo` varchar(5) NOT NULL DEFAULT 'NA',"
				+ "`estado_factura` varchar(25) NOT NULL DEFAULT 'NA',"
				+ "`isvOtros` float(8,0) NOT NULL DEFAULT '0',"
				+ "`isv18` float(255,2) NOT NULL DEFAULT '0.00',"
				+ "`usuario` varchar(255) NOT NULL DEFAULT 'SYSTEM',"
				+ "`pago` float NOT NULL DEFAULT '0',"
				+ "`descuento` float(8,0) NOT NULL DEFAULT '0',"
				+ "`tipo_factura` int(11) NOT NULL,"
				+ " PRIMARY KEY (`numero_factura`),"
				+ "UNIQUE KEY `numero_factura` (`numero_factura`) USING BTREE,"
				+ " KEY `codigo_cliente` (`codigo_cliente`) USING BTREE ) "
				+ "ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(sql);
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
	
	
public boolean crearDetalleTemTabla(String nombreBd){
		
		
		
		Connection conn=null;
		String sql="Create table if not exists "+nombreBd+".`detalle_factura_temp` ("
				+ "`numero_factura` int(11) NOT NULL,"
				+ "`codigo_articulo` int(11) NOT NULL,"
				+ "`precio` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`cantidad` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`impuesto` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`subtotal` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`descuento` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`total` float(11,2) NOT NULL DEFAULT '0.00',"
				+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
				+ " PRIMARY KEY (`id`),"
				+ "KEY `codigo_articulo` (`codigo_articulo`) USING BTREE,"
				+ "KEY `numero_factura` (`numero_factura`) USING BTREE"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			super.psConsultas=conn.prepareStatement(sql);
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


		public boolean crearDatosFacturaTabla(String nombreBd){
			
			
			
			Connection conn=null;
			String sql="Create table if not exists "+nombreBd+".`datos_factura` ("
					+ "`codigo_rango` int(11) NOT NULL AUTO_INCREMENT,"
					+ "`CAI` varchar(300) NOT NULL DEFAULT 'NA',"
					+ "`factura_inicial` varchar(11) NOT NULL DEFAULT 'NA',"
					+ "`factura_final` varchar(11) NOT NULL DEFAULT 'NA',"
					+ "`codigo_tipo_facturacion` varchar(50) NOT NULL DEFAULT 'NA',"
					+ "`cantida_solicitada` int(11) NOT NULL DEFAULT '0',"
					+ "`fecha_limite_emision` date NOT NULL DEFAULT '1990-01-01',"
					+ "PRIMARY KEY (`codigo_rango`)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
			
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				super.psConsultas=conn.prepareStatement(sql);
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

		public boolean asignarCajas(String user,Caja caja) {
			// TODO Auto-generated method stub
			
			Connection conn=null;
			
			try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				
				psConsultas=conn.prepareStatement( "INSERT INTO cajas_usuarios(codigo_caja,usuario,por_defecto) VALUES (?,?,?)");
					
				psConsultas.setInt(1, caja.getCodigo());
				psConsultas.setString(2,user);
				psConsultas.setInt(3,caja.isActiva()==true?1:0 );
				psConsultas.executeUpdate();
					
				
				return true;
			}catch (SQLException e) {
					e.printStackTrace();
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
						JOptionPane.showMessageDialog(null, excepcionSql.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					} // fin de catch
			} // fin de finally
			
		}

		public boolean desAsignarCaja(Usuario myUsuario) {
			// TODO Auto-generated method stub
			
			Connection conn=null;
			try {
					conn=ConexionStatic.getPoolConexion().getConnection();
					psConsultas=conn.prepareStatement("DELETE FROM cajas_usuarios WHERE usuario = ?");
				
					psConsultas.setString( 1, myUsuario.getUser()  );
					
					
					psConsultas.executeUpdate();
			
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
						JOptionPane.showMessageDialog(null, excepcionSql.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
					} // fin de catch
			} // fin de finally
			
		}

		public List<Caja> getCajasUsuario(Usuario myUsuario) {
			// TODO Auto-generated method stub
			
			
			List<Caja> cajas=new ArrayList<Caja>();
			ResultSet res=null;
			Connection conn=null;
			
			String sql="SELECT cajas.codigo, "
							+ " cajas.descripcion, "
							+ " cajas.codigo_bodega, "
							+ " cajas.nombre_db, "
							+ " cajas_usuarios.usuario, "
							+ " cajas_usuarios.por_defecto, "
							+ " bodega.codigo_bodega, "
							+ " bodega.descripcion_bodega "
						+ " FROM "
							+ super.DbName+".cajas "
							+ " INNER JOIN "+super.DbName+".cajas_usuarios "
								+ " ON cajas.codigo = cajas_usuarios.codigo_caja "
							+ " INNER JOIN "+super.DbName+".bodega "
								+ " ON cajas.codigo_bodega = bodega.codigo_bodega "
								+ " where cajas_usuarios.usuario=?";
			
			boolean existe=false;
			
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(sql);
				
				psConsultas.setString(1, myUsuario.getUser());
				res = psConsultas.executeQuery();
				while(res.next()){
					Caja unaCaja=new Caja();
					existe=true;
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					unaCaja.setActiva(res.getBoolean("por_defecto"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);

					
					
					cajas.add(unaCaja);
				 }
						
						
				} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, "Error, no se conecto");
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
					} // fin de catch
			} // fin de finally
			
				if (existe) {
					return cajas;
				}
				else return null;
			
		}

		public Vector<Caja> todosVector() {
			// TODO Auto-generated method stub
			Vector<Caja> cajas=new Vector<Caja>();
			ResultSet res=null;
			Connection conn=null;
			boolean existe=false;
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQuerySelect());
				
				
				
				res=psConsultas.executeQuery();
				while(res.next()){
					Caja unaCaja=new Caja();
					
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);
					cajas.add(unaCaja);
					existe=true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try{
					if(res!=null)res.close();
					if(psConsultas!=null)psConsultas.close();
					if(conn!=null)conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
			} // fin de finally
			
			if(existe)
				return cajas;
			else
				return null;
		}
		
		public List<Caja> todosList() {
			// TODO Auto-generated method stub
			List<Caja> cajas=new ArrayList<Caja>();
			ResultSet res=null;
			Connection conn=null;
			boolean existe=false;
			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQuerySelect());
				
				
				
				res=psConsultas.executeQuery();
				while(res.next()){
					Caja unaCaja=new Caja();
					
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);
					cajas.add(unaCaja);
					existe=true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try{
					if(res!=null)res.close();
					if(psConsultas!=null)psConsultas.close();
					if(conn!=null)conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
				} // fin de catch
			} // fin de finally
			
			if(existe)
				return cajas;
			else
				return null;
		}

		public Caja buscarPorId(int id) {
			// TODO Auto-generated method stub
			Caja unaCaja=new Caja();
			Connection con = null;
			ResultSet res=null;
			
			boolean existe=false;
			
			try {
				con = ConexionStatic.getPoolConexion().getConnection();
				
				//psConsultas = con.prepareStatement(super.getQuerySelect()+" where v_requisiciones.codigo_requisicion=?;");
				psConsultas = con.prepareStatement(super.getQuerySearch("codigo", "="));
				psConsultas.setInt(1, id);
				psConsultas.setInt(2, 0);
				psConsultas.setInt(3, 1);
				res = psConsultas.executeQuery();
				while(res.next()){
					existe=true;
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);
					
					
					
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
				return unaCaja;
			}
			else return null;
		}

		public List<Caja> buscarPorDescripcion(String busqueda, int limitInferio, int canItemPag) {
			List<Caja> cajas =new ArrayList<Caja>();
			
			ResultSet res=null;
			
			Connection conn=null;
			
			boolean existe=false;
			
			try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQuerySearch("descripcion", "like"));
				psConsultas.setString(1, "%" + busqueda + "%");
				psConsultas.setInt(2, limitInferio);
				psConsultas.setInt(3, canItemPag);
				res = psConsultas.executeQuery();
				while(res.next()){
					Caja unaCaja=new Caja();
					
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);
					cajas.add(unaCaja);
					existe=true;
					
					
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
				return cajas;
			}
			else return null;
		}
		
		public List<Caja> buscarPorUbicacion(String busqueda, int limitInferio, int canItemPag) {
			List<Caja> cajas =new ArrayList<Caja>();
			
			ResultSet res=null;
			
			Connection conn=null;
			
			boolean existe=false;
			
			try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQuerySelect()+" where descripcion_bodega like ? order by codigo  desc Limit ?, ?");
				psConsultas.setString(1, "%" + busqueda + "%");
				psConsultas.setInt(2, limitInferio);
				psConsultas.setInt(3, canItemPag);
				res = psConsultas.executeQuery();
				while(res.next()){
					Caja unaCaja=new Caja();
					
					unaCaja.setCodigo(res.getInt("codigo"));
					unaCaja.setCodigoBodega(res.getInt("codigo_bodega"));
					unaCaja.setDescripcion(res.getString("descripcion"));
					unaCaja.setNombreBd(res.getString("nombre_db"));
					
					Departamento dept=new Departamento();
					
					dept.setId((res.getInt("codigo_bodega")));
					dept.setDescripcion(res.getString("descripcion_bodega"));
					
					unaCaja.setDetartamento(dept);
					cajas.add(unaCaja);
					existe=true;
					
					
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
				return cajas;
			}
			else return null;
		}

		

}
