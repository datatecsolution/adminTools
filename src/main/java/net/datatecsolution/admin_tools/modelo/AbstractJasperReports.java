package net.datatecsolution.admin_tools.modelo;

import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.modelo.dao.SalidaCajaDao;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@SuppressWarnings({ "deprecation", "unused" })
public abstract class AbstractJasperReports implements Runnable
{
	private static JasperReport report;
	private static JasperPrint reportFilled;
	private static JasperViewer viewer;
	
	private static InputStream factura=null;
	private static InputStream factura2=null;
	private static InputStream facturaCarta=null;
	private static InputStream facturaTiketCredito=null;
	private static InputStream facturaCartaCredito=null;
	private static InputStream facturaCompra=null;
	private static InputStream facturaReimpresion=null;
	private static InputStream cierreCaja=null;
	private static InputStream reciboPago=null;
	private static InputStream SarVentas=null;
	private static InputStream codigoBarra=null;
	private static InputStream kardex=null;
	private static InputStream Devolucion=null;
	private static InputStream inventario=null;
	private static InputStream cierresCaja;
	private static InputStream cotizacion;
	private static InputStream comisiones;
	private static InputStream salidaCaja=null;
	private static InputStream cobroCaja=null;
	private static InputStream salidasEmpleados=null;
	private static InputStream cuentaCliente=null;
	private static InputStream saldosClientes=null;
	private static InputStream pagoCaja=null;
	private static InputStream saldosProveedores=null;
	private static InputStream cuentaProveedor=null;
	private static InputStream alertaExistencia=null;
	private static InputStream existenciaBodega=null;
	private static InputStream existenciaBodegaCategoria=null;
	private static InputStream pagosClientes=null;
	private static InputStream SarCompras=null;
	private static InputStream ventasUsuarios=null;
	private static InputStream ordenVenta=null;
	private static InputStream entradaCaja=null;
	private static InputStream movimientoBanco=null;
	private static InputStream saldoBanco=null;
	private static InputStream ventasCategoria=null;
	private static InputStream ventasDetalleTurno=null;
	private static InputStream ventasArticulo=null;
	private static InputStream cierreVentasDetalleCateg=null;
	private static InputStream cuentaFactura=null;
	private static InputStream ruta=null;
	private static InputStream articulosPrecios=null;
	private static InputStream entradasBanco=null;

	
	
	private static JasperReport	reportFactura;
	private static JasperReport	reportCotizacion;

	private static JasperReport	reportFactura2;
	private static JasperReport	reportFacturaCarta;
	private static JasperReport	reportFacturaTiketCredito;
	private static JasperReport reportFacturaCartaCredito;
	private static JasperReport	reportFacturaCompra;
	private static JasperReport	reportFacturaReimpresion;
	private static JasperReport	reportFacturaCierreCaja;
	private static JasperReport	reportReciboPago;
	private static JasperReport	reportSarVentas;
	private static JasperReport	reportDevolucion;
	private static JasperReport	reportCodigoBarra;
	private static JasperReport	reportKardex;
	private static JasperReport	reportInventario;
	private static JasperReport reportCierresCaja;
	private static JasperReport reportComisiones;
	private static JasperReport	reportSalidaCaja;
	private static JasperReport	reportCobroCaja;
	private static JasperReport	reportSalidasEmpleados;
	private static JasperReport	reportCuentaCliente;
	private static JasperReport	reportSaldosClientes;
	private static JasperReport	reportPagoCaja;
	private static JasperReport	reportSaldosProveedores;
	private static JasperReport reportCuentaProveedor;
	private static JasperReport	reportAlertaExistencia;
	private static JasperReport	reportExistenciaBodega;
	private static JasperReport reportExistenciaBodegaCategoria;
	private static JasperReport	reportPagosCliente;
	private static JasperReport	reportSarCompras;
	private static JasperReport reportVentasUsuarios;
	private static JasperReport reportOrdenVenta;
	private static JasperReport	reportEntradaCaja;
	private static JasperReport	reportMovimientoBanco;
	private static JasperReport reportSaldoBanco;
	private static JasperReport reportVentasCategoria;
	private static JasperReport reportVentasDetalleTurno;
	private static JasperReport reportVentasArticulo;
	private static JasperReport reportCierreVentasDetalleCateg;
	private static JasperReport	reportCuentaFactura;
	private static JasperReport reportRuta;
	private static JasperReport reportArticulosPrecios;
	private static JasperReport reportEntradasBanco;
	
	
	private static final Pattern numberPattern=Pattern.compile("-?\\d+");
	private static final Pattern numberPatternReal=Pattern.compile("\\d+([.]\\d+)?");
	private static final FacturaDao facturaDao=new FacturaDao();
	
	
	public static boolean isNumber(String string){
		return string !=null && numberPattern.matcher(string).matches();
	}
	
	public static boolean isNumberReal(String string){
		return string !=null && numberPatternReal.matcher(string).matches();
	}
	
	
	public static void loadFileReport(){



		factura= AbstractJasperReports.class.getResourceAsStream("/reportes/factura_tiket.jasper");
		factura2=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_tiket.jasper");
		facturaCarta=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_carta.jasper");

		facturaTiketCredito=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_tiket_credito.jasper");
		facturaCartaCredito=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_carta_credito.jasper");
		//factura2=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_carta.jasper");

		facturaCompra=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_compra_carta.jasper");
		facturaReimpresion=AbstractJasperReports.class.getResourceAsStream("/reportes/factura_tiket_la_copia.jasper");
		cierreCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/cierre_caja.jasper");
		reciboPago=AbstractJasperReports.class.getResourceAsStream("/reportes/recibo_pago.jasper");
		SarVentas=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteSarVentas.jasper");
		Devolucion=AbstractJasperReports.class.getResourceAsStream("/reportes/devoluciones_venta.jasper");
		codigoBarra=AbstractJasperReports.class.getResourceAsStream("/reportes/codigo_barra.jasper");
		kardex=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteKardex.jasper");
		inventario=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteExistencia.jasper");
		cierresCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/cierres_caja.jasper");
		cotizacion=AbstractJasperReports.class.getResourceAsStream("/reportes/cotizacion.jasper");
		comisiones=AbstractJasperReports.class.getResourceAsStream("/reportes/comisiones2.jasper");
		salidaCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/salida_caja.jasper");
		cobroCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/cobro_caja.jasper");
		
		salidasEmpleados=AbstractJasperReports.class.getResourceAsStream("/reportes/salidas_empleados.jasper");
		
		
		cuentaCliente=AbstractJasperReports.class.getResourceAsStream("/reportes/cliente_cuenta.jasper");
		saldosClientes=AbstractJasperReports.class.getResourceAsStream("/reportes/clientes_cuentas.jasper");
		pagoCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/pago_caja.jasper");
		saldosProveedores=AbstractJasperReports.class.getResourceAsStream("/reportes/proveedores_cuentas.jasper");
		cuentaProveedor=AbstractJasperReports.class.getResourceAsStream("/reportes/proveedor_cuenta.jasper");
		alertaExistencia=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteAlertaExistencia.jasper");
		existenciaBodega=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteExistenciaBodega.jasper");
		existenciaBodegaCategoria=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteExistenciaBodegaCategoria.jasper");
		pagosClientes=AbstractJasperReports.class.getResourceAsStream("/reportes/pagos_clientes.jasper");
		SarCompras=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteSarCompras.jasper");
		
		ventasUsuarios=AbstractJasperReports.class.getResourceAsStream("/reportes/ventas_usuarios.jasper");
		ordenVenta=AbstractJasperReports.class.getResourceAsStream("/reportes/orden.jasper");
		
		entradaCaja=AbstractJasperReports.class.getResourceAsStream("/reportes/entrada_caja.jasper");
		
		movimientoBanco=AbstractJasperReports.class.getResourceAsStream("/reportes/movimiento_banco.jasper");
		
		saldoBanco=AbstractJasperReports.class.getResourceAsStream("/reportes/saldo_cuentas.jasper");
		
		ventasCategoria=AbstractJasperReports.class.getResourceAsStream("/reportes/ventas_categorias.jasper");
		
		ventasDetalleTurno=AbstractJasperReports.class.getResourceAsStream("/reportes/ventas_detalle_turno.jasper");
		
		ventasArticulo=AbstractJasperReports.class.getResourceAsStream("/reportes/ventas_detalle_articulo.jasper");
		
		
		cierreVentasDetalleCateg=AbstractJasperReports.class.getResourceAsStream("/reportes/cierre_ventas_detalle_categ.jasper");
		
		cuentaFactura=AbstractJasperReports.class.getResourceAsStream("/reportes/cliente_cuenta_factura.jasper");
		
		ruta=AbstractJasperReports.class.getResourceAsStream("/reportes/reporte_rutas.jasper");

		articulosPrecios=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteArticuloPrecio.jasper");

		entradasBanco=AbstractJasperReports.class.getResourceAsStream("/reportes/entradas_bancos.jasper");
		
		try {
			reportFactura = (JasperReport) JRLoader.loadObject( factura );
			reportFactura2 = (JasperReport) JRLoader.loadObject( factura2 );
			reportFacturaCarta = (JasperReport) JRLoader.loadObject( facturaCarta );
			reportFacturaTiketCredito = (JasperReport) JRLoader.loadObject( facturaTiketCredito );
			reportFacturaCartaCredito = (JasperReport) JRLoader.loadObject( facturaCartaCredito );
			//reportFacturaCredito = (JasperReport) JRLoader.loadObject( facturaCredito );
			//reportFacturaCredito2 = (JasperReport) JRLoader.loadObject( facturaCredito2 );
			reportFacturaCompra = (JasperReport) JRLoader.loadObject( facturaCompra );
			reportFacturaReimpresion= (JasperReport) JRLoader.loadObject( facturaReimpresion );
			reportFacturaCierreCaja= (JasperReport) JRLoader.loadObject( cierreCaja );
			reportReciboPago= (JasperReport) JRLoader.loadObject( reciboPago );
			reportSarVentas= (JasperReport) JRLoader.loadObject( SarVentas );
			reportDevolucion= (JasperReport) JRLoader.loadObject( Devolucion );
			reportCodigoBarra= (JasperReport) JRLoader.loadObject( codigoBarra );
			reportKardex= (JasperReport) JRLoader.loadObject( kardex );
			reportInventario= (JasperReport) JRLoader.loadObject( inventario );
			reportCierresCaja= (JasperReport) JRLoader.loadObject( cierresCaja );
			reportCotizacion= (JasperReport) JRLoader.loadObject( cotizacion );
			reportComisiones= (JasperReport) JRLoader.loadObject( comisiones );
			reportCobroCaja= (JasperReport) JRLoader.loadObject( cobroCaja );
			reportSalidasEmpleados= (JasperReport) JRLoader.loadObject( salidasEmpleados );
			//Dei=AbstractJasperReports.class.getResourceAsStream("/reportes/ReporteDEI.jasper");
			
			reportSalidaCaja= (JasperReport) JRLoader.loadObject( salidaCaja );
			reportCuentaCliente= (JasperReport) JRLoader.loadObject( cuentaCliente );
			reportSaldosClientes= (JasperReport) JRLoader.loadObject( saldosClientes );
			reportPagoCaja= (JasperReport) JRLoader.loadObject( pagoCaja );
			reportSaldosProveedores=(JasperReport) JRLoader.loadObject( saldosProveedores );
			reportCuentaProveedor=(JasperReport) JRLoader.loadObject( cuentaProveedor );
			reportAlertaExistencia= (JasperReport) JRLoader.loadObject( alertaExistencia );
			reportExistenciaBodega= (JasperReport) JRLoader.loadObject( existenciaBodega );
			reportExistenciaBodegaCategoria= (JasperReport) JRLoader.loadObject( existenciaBodegaCategoria );
			reportPagosCliente= (JasperReport) JRLoader.loadObject( pagosClientes );
			reportSarCompras= (JasperReport) JRLoader.loadObject( SarCompras );
			
			reportVentasUsuarios= (JasperReport) JRLoader.loadObject( ventasUsuarios );
			reportOrdenVenta= (JasperReport) JRLoader.loadObject( ordenVenta );
			reportEntradaCaja= (JasperReport) JRLoader.loadObject( entradaCaja );
			reportMovimientoBanco = (JasperReport) JRLoader.loadObject( movimientoBanco );
			
			reportSaldoBanco= (JasperReport) JRLoader.loadObject( saldoBanco );
			
			reportVentasCategoria=(JasperReport) JRLoader.loadObject( ventasCategoria );
			
			reportVentasDetalleTurno=(JasperReport) JRLoader.loadObject( ventasDetalleTurno );
			
			reportVentasArticulo=(JasperReport) JRLoader.loadObject( ventasArticulo );
			
			reportCierreVentasDetalleCateg=(JasperReport) JRLoader.loadObject( cierreVentasDetalleCateg );
			
			reportCuentaFactura=(JasperReport) JRLoader.loadObject(cuentaFactura  );
			
			reportRuta=(JasperReport) JRLoader.loadObject(ruta  );

			reportArticulosPrecios=(JasperReport) JRLoader.loadObject(articulosPrecios  );

			reportEntradasBanco=(JasperReport) JRLoader.loadObject(entradasBanco);
			
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void createReportCodBarra(Connection conn,int id){
		 Map parametros = new HashMap();
		 parametros.put("id_articulo",id);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportCodigoBarra, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportAlertaExistencia(Connection conn,int id){
		 Map parametros = new HashMap();
		 parametros.put("id_articulo",id);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportAlertaExistencia, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportSalidaCaja(Connection conn,int codigo){
			SalidaCajaDao salida=new SalidaCajaDao();
		 Map parametros = new HashMap();
		 parametros.put("codigo_salida",codigo);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSalidaCaja, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportEntradaCaja(Connection conn,int codigo){
		SalidaCajaDao salida=new SalidaCajaDao();
	 Map parametros = new HashMap();
	 parametros.put("codigo_entrada",codigo);
	 parametros.put("bD_admin",facturaDao.getDbNameDefault());
	 
	 
	 try {
		reportFilled = JasperFillManager.fillReport( reportEntradaCaja, parametros, conn );
	} catch (JRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 try {
			conn.close();
		} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
		}
	}
	
	public static void createReportMovimientoBanco(Connection conn,int codigo){
		SalidaCajaDao salida=new SalidaCajaDao();
	 Map parametros = new HashMap();
	 parametros.put("codigo_movimiento",codigo);
	 parametros.put("bD_admin",facturaDao.getDbNameDefault());
	 
	 
	 try {
		reportFilled = JasperFillManager.fillReport( reportMovimientoBanco, parametros, conn );
	} catch (JRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 try {
			conn.close();
		} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
		}
	}
	
	public static void createReportVentasUsuarios(Connection conn,Date fechaMin,Date fechaMax,List<Comision> comisiones,int porcentaje){
		 
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("fecha_min",fechaMin);
		 parametros.put("fecha_max", fechaMax);
		 
		 parametros.put("porcentaje", porcentaje);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 
		 
		 
		 
		 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(comisiones);
		 parametros.put("comisionDataSource",comisionesJRBean);
		 
		 
		 
		 try {
			 if(reportVentasUsuarios!=null)
				 reportFilled = JasperFillManager.fillReport( reportVentasUsuarios, parametros, conn );
			 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
			 else
				 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
public static void createReportVentasArticulo(Connection conn,List<DetalleFactura> ventas,Articulo articulo,String fecha1,String fecha2){
		 
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("fecha1",fecha1);
		 parametros.put("fecha2", fecha2);
		 parametros.put("articulo", articulo.getArticulo());
		 
		 parametros.put("codigo_articulo", articulo.getId());
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		
		 
		 
		 
		 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(ventas);
		 parametros.put("ventasDataSource",comisionesJRBean);
		 
		 
		 try {
			 if(reportVentasArticulo!=null)
				 reportFilled = JasperFillManager.fillReport( reportVentasArticulo, parametros, conn );
			 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
			 else
				 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}

public static void createReportRuta(Connection conn,RutaEntrega ruta,List<DetalleFactura> ventas){
	 
	
	Map<String, Object> parametros = new HashMap<String, Object>();
	 parametros.put("fecha",ruta.getFecha());
	 parametros.put("usuario", ConexionStatic.getUsuarioLogin().getUser());
	 
	 parametros.put("vendedor", ruta.getVendedor().getCodigo()+" | "+ruta.getVendedor().getNombre()+" "+ruta.getVendedor().getApellido());
	 
	 parametros.put("codigo_ruta", ruta.getIdRuta());
	 parametros.put("bD_admin",facturaDao.getDbNameDefault());
	 
	 
	 
	
	 
	 
	 
	 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(ventas);
	 parametros.put("ventasDataSource",comisionesJRBean);
	 
	 
	 try {
		 if(reportRuta!=null)
			 reportFilled = JasperFillManager.fillReport( reportRuta, parametros, conn );
		 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
		 else
			 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
	} catch (JRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 try {
			conn.close();
		} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
		}
}
		
public static void createReportVentasDetalleTurno(Connection conn,CierreCaja cierre,List<DetalleFactura> ventas,Caja myCaja){
		 
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("fecha",cierre.getFecha());
		 parametros.put("usuario", cierre.getUsuario());
		 parametros.put("caja", myCaja.getDescripcion());
		 
		 parametros.put("codigo_cierre", cierre.getId());
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		
		 
		 
		 
		 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(ventas);
		 parametros.put("ventasDataSource",comisionesJRBean);
		 
		 
		 try {
			 if(reportVentasDetalleTurno!=null)
				 reportFilled = JasperFillManager.fillReport( reportVentasDetalleTurno, parametros, conn );
			 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
			 else
				 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}

public static void createRepCierreVentasDetallesCateg(Connection conn,CierreCaja cierre,List<DetalleFactura> ventas,Categoria myCategoria){
	 
	
	Map<String, Object> parametros = new HashMap<String, Object>();
	 parametros.put("fecha",cierre.getFecha());
	 parametros.put("usuario", cierre.getUsuario());
	 parametros.put("categoria", myCategoria.getDescripcion());
	 
	 parametros.put("codigo_cierre", cierre.getId());
	 parametros.put("bD_admin",facturaDao.getDbNameDefault());
	 
	 
	 
	
	 
	 
	 
	 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(ventas);
	 parametros.put("ventasDataSource",comisionesJRBean);
	 
	 
	 try {
		 if(reportVentasDetalleTurno!=null)
			 reportFilled = JasperFillManager.fillReport( reportCierreVentasDetalleCateg, parametros, conn );
		 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
		 else
			 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
	} catch (JRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 try {
			conn.close();
		} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
		}
}
	
public static void createReportVentasCategoria(Connection conn,CierreCaja cierre,List<VentasCategoria> ventas){
		 
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("fecha",cierre.getFecha());
		 parametros.put("usuario", cierre.getUsuario());
		 
		 parametros.put("codigo_cierre", cierre.getId());
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		
		 
		 
		 
		 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(ventas);
		 parametros.put("ventasDataSource",comisionesJRBean);
		 
		 
		 
		 try {
			 if(reportVentasCategoria!=null)
				 reportFilled = JasperFillManager.fillReport( reportVentasCategoria, parametros, conn );
			 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
			 else
				 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportComisiones(Connection conn,Date fechaMin,Date fechaMax,List<Comision> comisiones,int porcentaje){
		 
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("fecha_min",fechaMin);
		 parametros.put("fecha_max", fechaMax);
		 
		 parametros.put("porcentaje", porcentaje);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 
		 
		 
		 
		 JRBeanCollectionDataSource comisionesJRBean =new JRBeanCollectionDataSource(comisiones);
		 parametros.put("comisionDataSource",comisionesJRBean);
		 
		 
		 
		 try {
			 if(reportComisiones!=null)
				 reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, conn );
			 	//reportFilled = JasperFillManager.fillReport( reportComisiones, parametros, new JREmptyDataSource() );
			 else
				 JOptionPane.showMessageDialog(null, "No se encontro el reportes");
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportSalidasEmpleado(Connection conn,Date fechaMin,Date fechaMax,int codigoEmpleado ){
		 Map parametros = new HashMap();
		 parametros.put("fecha_min",fechaMin);
		 parametros.put("fecha_max", fechaMax);
		 parametros.put("codigo_empleado", codigoEmpleado);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSalidasEmpleados, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}

	public static void createReportEntradasBanco(Connection conn,Date fechaMin,Date fechaMax,int codigoCuenta ){
		Map parametros = new HashMap();
		parametros.put("fecha_min",fechaMin);
		parametros.put("fecha_max", fechaMax);
		parametros.put("codigo_cuenta", codigoCuenta);
		parametros.put("bD_admin",facturaDao.getDbNameDefault());


		try {
			reportFilled = JasperFillManager.fillReport( reportEntradasBanco, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void createReportSaldoBancos(Connection conn,Date fechaMin,Date fechaMax,int codigoBanco ){
		 Map parametros = new HashMap();
		 parametros.put("fecha_min",fechaMin);
		 parametros.put("fecha_max", fechaMax);
		 parametros.put("codigo_banco", codigoBanco);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
	
		 try {
			reportFilled = JasperFillManager.fillReport( reportSaldoBanco, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	
	public static void createReportPagoCliente(Connection conn,Date fechaMin,Date fechaMax,int codigoCliente ){
		 Map parametros = new HashMap();
		 parametros.put("fecha_min",fechaMin);
		 parametros.put("fecha_max", fechaMax);
		 parametros.put("codigo_cliente", codigoCliente);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportPagosCliente, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	
	public static void createReportSarVentas(Connection conn,int mes,int anio,String usuario, String bD_facturacion){
		 
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("Mes",mes);
		 parametros.put("Anio",anio);
		 parametros.put("usuario",usuario);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 parametros.put("bD_facturacion",bD_facturacion);
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSarVentas, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportSarCompras(Connection conn,int mes,int anio,String usuario){
		 
		Map<String, Object> parametros = new HashMap<String, Object>();
		 parametros.put("Mes",mes);
		 parametros.put("Anio",anio);
		 parametros.put("usuario",usuario);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSarCompras, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportInventario(Connection conn,String user){
		 Map parametros = new HashMap();
		 parametros.put("usuario",user);
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportInventario, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportArticulosPreios(Connection conn,String user, Integer codigoCategoria){
		 Map parametros = new HashMap();
		 parametros.put("usuario",user);
		 parametros.put("codigo_categoria",codigoCategoria);
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportArticulosPrecios, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportInventarioBodega(Connection conn,String user,Integer idBodega){
		 Map parametros = new HashMap();
		 parametros.put("usuario",user);
		 parametros.put("codigo_bodega",idBodega);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportExistenciaBodega, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportInventarioBodegaCategoria(Connection conn,String user,Integer idBodega,Integer idCategoria){
		 Map parametros = new HashMap();
		 parametros.put("usuario",user);
		 parametros.put("codigo_bodega",idBodega);
		 parametros.put("codigo_categoria",idCategoria);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportExistenciaBodegaCategoria, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void crearReporteCotizacion(Connection conn,Integer idCotizacion){
		
		Map parametros = new HashMap();
		 parametros.put("numero_factura",idCotizacion);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 try {
				
					reportFilled = JasperFillManager.fillReport( reportCotizacion, parametros, conn );
			}catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
		
	}
	
	
	public static void createReportReciboPagoCaja(Connection conn,int id){
		 Map parametros = new HashMap();
		 parametros.put("no_recibo",id);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportPagoCaja, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	
	public static void createReportReciboCobroCaja(Connection conn,int id){
		 Map parametros = new HashMap();
		 parametros.put("no_recibo",id);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportCobroCaja, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	
	public static void createReportSaltosClientes(Connection conn){
		
		 Map parametros = new HashMap();
		 parametros.put("codigo_cliente",1);//no se necesario por ahora
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSaldosClientes, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportSaltosProveedores(Connection conn){
		
		 Map parametros = new HashMap();
		 parametros.put("codigo_cliente",1);//no se necesario por ahora
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportSaldosProveedores, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportCuentaCliente(Connection conn,int codigo){
		
		 Map parametros = new HashMap();
		 parametros.put("codigo_cliente",codigo);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportCuentaCliente, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportCuentaFactura(Connection conn,int codigo){
		
		 Map parametros = new HashMap();
		 parametros.put("codigo_cuenta",codigo);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		// dsd
		 try {
			reportFilled = JasperFillManager.fillReport( reportCuentaFactura, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportCuentaProveedor(Connection conn,int codigo){
		
		 Map parametros = new HashMap();
		 parametros.put("codigo_proveedor",codigo);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportCuentaProveedor, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	public static void createReportDevolucionVenta(Connection conn,Integer idFactura){
		
		Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			 parametros.put("cod_caja",ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 try {
			 reportFilled = JasperFillManager.fillReport( reportDevolucion, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
	}
	
	public static void createReportFacturaTiketCredito( Connection conn,Integer idFactura ){
		
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			// JOptionPane.showMessageDialog(null,ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 

		 
		try {
			//fsdfs
			
				reportFilled = JasperFillManager.fillReport( reportFacturaTiketCredito, parametros, conn );
			
			
			
			
			
			}
			catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
	}
	
	public static void createReportFacturaCarta( Connection conn,Integer idFactura, String  obrs )
	{
		
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 parametros.put("observacion",obrs);
		 
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			// JOptionPane.showMessageDialog(null,ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		  
		try {
		
				reportFilled = JasperFillManager.fillReport( reportFacturaCarta, parametros, conn );
			
			}
			catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
		
	}
	public static void createReportFacturaCartaCredito( Connection conn,Integer idFactura , String obrs )
	{
		
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 parametros.put("observacion",obrs);
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			// JOptionPane.showMessageDialog(null,ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		  
		try {
		
				reportFilled = JasperFillManager.fillReport( reportFacturaCartaCredito, parametros, conn );
			
			}
			catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
		
	}
	
	
	
	
	public static void createReport( Connection conn, int tipoReporte,Integer idFactura )
	{
		
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			// JOptionPane.showMessageDialog(null,ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		// JOptionPane.showMessageDialog(null,facturaDao.getDbNameDefault());
		// JOptionPane.showMessageDialog(null,idFactura);

		 
		try {
			
			if(tipoReporte==1){
				reportFilled = JasperFillManager.fillReport( reportFactura, parametros, conn );
			}
			if(tipoReporte==2){
				reportFilled = JasperFillManager.fillReport( reportFacturaCompra, parametros, conn );
			}
			if(tipoReporte==3){
				reportFilled = JasperFillManager.fillReport( reportFacturaReimpresion, parametros, conn );
			}
			if(tipoReporte==4){
				reportFilled = JasperFillManager.fillReport( reportFacturaCierreCaja, parametros, conn );
			}
			if(tipoReporte==5){
				reportFilled = JasperFillManager.fillReport( reportReciboPago, parametros, conn );
			}
			if(tipoReporte==6){
				reportFilled = JasperFillManager.fillReport( reportFactura2, parametros, conn );
			}
			if(tipoReporte==7){
				reportFilled = JasperFillManager.fillReport( reportDevolucion, parametros, conn );
			}
			/*if(tipoReporte==8){
				reportFilled = JasperFillManager.fillReport( reportFacturaCredito2, parametros, conn );
			}*/
			
			
			
			
			
			
			}
			catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
		
	}
	
	public static void createReportOrden( Connection conn,Integer idFactura )
	{
		
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 if(ConexionStatic.getUsuarioLogin().getCajaActiva()!=null){
			 parametros.put("bD_facturacion",ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
			// JOptionPane.showMessageDialog(null,ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd());
		 }
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		// JOptionPane.showMessageDialog(null,facturaDao.getDbNameDefault());
		// JOptionPane.showMessageDialog(null,idFactura);

		 
		try {
			
			
				reportFilled = JasperFillManager.fillReport( reportOrdenVenta, parametros, conn );
			
			
			
			
			
			
			
			}
			catch( JRException ex ) {
				ex.printStackTrace();
			}
			try {
					conn.close();
				} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
				}
		
	}
	
	public static void createReportCierresCaja(Connection conn,String user){
		 Map parametros = new HashMap();
		 parametros.put("usuario",user);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportCierresCaja, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
		
	}
	
	public static void createReportKardex(Connection conn,Integer idArticulo,Integer idBodega,String user){
		 Map parametros = new HashMap();
		 parametros.put("cod_articulo",idArticulo);
		 parametros.put("cod_bodega",idBodega);
		 parametros.put("usuario",user);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 try {
			reportFilled = JasperFillManager.fillReport( reportKardex, parametros, conn );
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
		
	}
	

	public static void createReportRequisicion( Connection conn, String path,Integer idFactura )
	{
		 Map parametros = new HashMap();
		 parametros.put("numero_factura",idFactura);
		 parametros.put("bD_admin",facturaDao.getDbNameDefault());
		 
		 
		 
		 InputStream ticketReportStream=null;
		 
		 
		//fsdf
		try {
			
			ticketReportStream=AbstractJasperReports.class.getResourceAsStream("/reportes/"+path);
			//report = (JasperReport) JRLoader.loadObjectFromFile( path );
			report = (JasperReport) JRLoader.loadObject( ticketReportStream );
			reportFilled = JasperFillManager.fillReport( report, parametros, conn );
			
		}
		catch( JRException ex ) {
			ex.printStackTrace();
		}
		try {
				conn.close();
			} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
			}
		
	}
	public static void imprimierFactura(){
		try {
			JasperPrintManager.printReport(reportFilled, false);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static void showViewer(Window view)
	{
		JDialog viewer2 = new JDialog(view,"Vista previa del reporte", Dialog.ModalityType.DOCUMENT_MODAL);
		viewer2.setSize(1000,800);
		viewer2.setLocationRelativeTo(null);
		
		
		JasperViewer viewer3 = new JasperViewer( reportFilled );
		//viewer2.setTitle("Factura");
		viewer2.getContentPane().add(viewer3.getContentPane());
		viewer2.setVisible( true );
		//viewer.setVisible( true );
		
		
	}
	public static void showViewer(JInternalFrame view)
	{
		JDialog viewer2 = new JDialog();
		viewer2.setSize(1000,800);
		viewer2.setLocationRelativeTo(null);
		
		
		JasperViewer viewer3 = new JasperViewer( reportFilled );
		//viewer2.setTitle("Factura");
		viewer2.getContentPane().add(viewer3.getContentPane());
		viewer2.setVisible( true );
		//viewer.setVisible( true );
		
		
	}
	
 public static void Imprimir2(){  
		 
		 
		 
		 
		 try {


		        //String report = JasperCompileManager.compileReportToFile(sourceFileName);


		        //JasperPrint jasperPrint = JasperFillManager.fillReport(report, para, ds);


		        PrinterJob printerJob = PrinterJob.getPrinterJob();


		        PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
		        printerJob.defaultPage(pageFormat);

		        int selectedService = 0;


		        AttributeSet attributeSet = new HashPrintServiceAttributeSet(new PrinterName("\\\\TEXACOADMIN\\EPSON L210 Series", null));
		        
		        //AttributeSet attributeSet = new HashPrintServiceAttributeSet(new PrinterName("\\\\CONTABILIDAD-PC\\EPSON LX-300+ /II (Copiar 1)", null));


		        PrintService[] printService = PrintServiceLookup.lookupPrintServices(null, attributeSet);

		        try {
		            printerJob.setPrintService(printService[selectedService]);

		        } catch (Exception e) {

		            System.out.println(e);
		        }
		        JRPrintServiceExporter exporter;
		        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
		        printRequestAttributeSet.add(MediaSizeName.NA_LETTER);
		        printRequestAttributeSet.add(new Copies(1));

		        // these are deprecated
		        exporter = new JRPrintServiceExporter();
		        exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportFilled);
		        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, printService[selectedService]);
		        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, printService[selectedService].getAttributes());
		        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
		        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
		        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
		        exporter.exportReport();

		    } catch (JRException e) {
		        e.printStackTrace();
		    }
		//}   
		         
		         
		         
		}

	public static void exportToPDF( String destination )
	{
		try { 
			JasperExportManager.exportReportToPdfFile( reportFilled, destination );
		}
		catch( JRException ex ) {
			ex.printStackTrace();
		}
	}
	
}
