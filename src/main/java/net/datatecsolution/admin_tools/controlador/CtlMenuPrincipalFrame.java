package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Conexion;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.modelo.dao.CierreCajaDao;
import net.datatecsolution.admin_tools.view.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class CtlMenuPrincipalFrame implements ActionListener {
	
	public ViewMenuPrincipalFrame view;
	public Conexion conexion=null;
	private ArticuloDao myArticuloDao=null;
	
	
	
	public CtlMenuPrincipalFrame(ViewMenuPrincipalFrame view, Conexion conn){
		conexion=conn;
		this.view=view;
		myArticuloDao=new ArticuloDao();
		
		setAlertaExistencia();
		
		view.getLblUserName().setText(conexion.getUsuarioLogin().getUser());
		//view.setMaximumSize(maximumSize);
		
		
	}
	private void setAlertaExistencia() {
		// TODO Auto-generated method stub
		view.getBtnAlertaExistencia().setText(myArticuloDao.getAlertaExistencia()+" articulos con poca existencia");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame.setDefaultLookAndFeelDecorated(true);
		switch(comando){
		
		
		
		case "ALERTAEXISTENCIAS":
			try {
				//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
				AbstractJasperReports.createReportAlertaExistencia(conexion.getPoolConexion().getConnection(), 4);
				
				//this.view.setModal(false);
				//AbstractJasperReports.imprimierFactura();
				AbstractJasperReports.showViewer(this.view);
				
				
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
			break;
	
		
			case "CUENTASBANCOS":
				ViewListaCuentaBancos vCuentasBancos=new ViewListaCuentaBancos(view);
				CtlBancosLista cCuentasBancos=new CtlBancosLista(vCuentasBancos);
				vCuentasBancos.setVisible(true);
				break;
		
			case "COTIZACIONES":
					ViewListaCotizacion vCotizaciones=new ViewListaCotizacion(view);
					CtlCotizacionLista cCotizaciones=new CtlCotizacionLista(vCotizaciones);
					vCotizaciones.setVisible(true);
					break;
		
		
			case "PAGOPROVEEDORES":
				ViewListaPagosProveedores vPagosProveedores= new ViewListaPagosProveedores(view);
				CtlPagosProveedoresLista cPagoProveedores=new CtlPagosProveedoresLista(vPagosProveedores);
				
				vPagosProveedores.dispose();
				cPagoProveedores=null;
				/*
					ViewPagoProveedor vPagoProveedores=new ViewPagoProveedor(view);
					CtlPagoProveedor cPagoProveedores=new CtlPagoProveedor(vPagoProveedores,conexion);
					vPagoProveedores.dispose();
					cPagoProveedores=null;*/
				break;
				
			case "REQUISICIONES":
				ViewListaRequisiciones viewRequiLista=new ViewListaRequisiciones(view);
				CtlRequisicionesLista ctlRequiLista=new CtlRequisicionesLista(viewRequiLista);
				viewRequiLista.dispose();
				ctlRequiLista=null;
			break;
	
			case "CERRARFACTURACION":
				try {
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
					AbstractJasperReports.createReport(conexion.getPoolConexion().getConnection(), 4, 0);
					
					//this.view.setModal(false);
					//AbstractJasperReports.imprimierFactura();
					AbstractJasperReports.showViewer(this.view);
					
					CierreCajaDao cierre=new CierreCajaDao();
					
					if(cierre.registrar(new Object())){
						JOptionPane.showMessageDialog(view, "Se creo el cierre de caja");
					}
					
				} catch (SQLException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
				break;
			case "PROVEEDORES":
				ViewListaProveedor viewListaProveedor=new ViewListaProveedor(view);
				CtlProveedorLista ctlProveedor=new CtlProveedorLista(viewListaProveedor);
				viewListaProveedor.dispose();
				ctlProveedor=null;
				break;
			case "ARTICULOS":
				ViewListaArticulo viewListaArticulo=new ViewListaArticulo(view);
				CtlArticuloLista ctlArticulo =new CtlArticuloLista(viewListaArticulo);
				viewListaArticulo.dispose();
				ctlArticulo=null;
				
				break;
			case "AGREGARCOMPRAS":
				ViewAgregarCompras viewAgregarCompras= new ViewAgregarCompras(this.view);
				CtlCompras ctlAgregarCompra=new CtlCompras(viewAgregarCompras);
				
				
				viewAgregarCompras.dispose();
				viewAgregarCompras=null;
				ctlAgregarCompra=null;
				break;
			case "FACTURAR":
				
				/*ViewListaFactura vistaFacturars=new ViewListaFactura(this.view);
				CtlFacturaLista ctlFacturas=new CtlFacturaLista(vistaFacturars,conexion );
				vistaFacturars.dispose();
				ctlFacturas=null;*/
			
				ViewFacturar vistaFacturar=new ViewFacturar(this.view);
				vistaFacturar.pack();
				CtlFacturar ctlFacturar=new CtlFacturar(vistaFacturar );
				vistaFacturar.setVisible(true);
		
				
				break;
			case "CATEGORIAS":
				//se crea la lista de categorias
				ViewListaCategorias vlCategorias=new ViewListaCategorias(this.view);
				
				// se crea el control para la view lista marcas
				CtlCategoriaLista ctlListaCategorias =new CtlCategoriaLista(vlCategorias); 
				
				vlCategorias.dispose();
				ctlListaCategorias=null;
				
				break;
			case "CLIENTES":
				ViewListaClientes viewClientes=new ViewListaClientes(view);
				CtlClienteLista  ctlClientes=new CtlClienteLista(viewClientes);
				viewClientes.dispose();
				ctlClientes=null;
				break;
			case "BUSCARFACTURAS":
				
				ViewFacturas viewBuscarFacturas = new ViewFacturas(this.view);
				CtlFacturas cltBuscarFacturas= new CtlFacturas(viewBuscarFacturas);
				viewBuscarFacturas.dispose();
				cltBuscarFacturas=null;
				break;
				
			case "LISTAFACTURASCOMPRA":
				ViewListaFacturasCompra viewFacturasCompra=new ViewListaFacturasCompra(this.view);
				CtlFacturasCompra ctlFacturasCompra=new CtlFacturasCompra(viewFacturasCompra);
				viewFacturasCompra.dispose();
				viewFacturasCompra=null;
				ctlFacturasCompra=null;
				break;
				
			case "PAGOCLIENTES":
				ViewCxCPagos viewPagoClientes=new ViewCxCPagos(view);
				CtlFacturaPagos ctlPagoCleintes=new CtlFacturaPagos(viewPagoClientes);
				viewPagoClientes.dispose();
				viewPagoClientes=null;
				ctlPagoCleintes=null;
				break;
			case "LISTAPAGOS":
				
				ViewListaPagos viewListaPagos=new ViewListaPagos(view);
				CtlPagoLista ctlPagoLista =new CtlPagoLista(viewListaPagos);
				viewListaPagos.dispose();
				//viewListaPagos=null;
				//ctlPagoLista=null;
				break;
			case "PROGRAMARPRECIOS":
				ViewListaPrecioProgramar viewProgramarPrecio=new ViewListaPrecioProgramar(view);
				CtlProgramarPrecios ctlProgramarPrecio=new CtlProgramarPrecios(viewProgramarPrecio, conexion);
				viewProgramarPrecio.dispose();
				viewProgramarPrecio=null;
				ctlProgramarPrecio=null;
				break;
			case "R_DEI":
				ViewFiltroRepSarVentas viewFiltroDei=new ViewFiltroRepSarVentas(view);
				CtlFiltroRepSarVentas ctlFiltroDei=new CtlFiltroRepSarVentas(viewFiltroDei);
				break;
				
			case "USUARIOS":
				
				ViewListaUsuarios viewListaUsuarios=new ViewListaUsuarios(view);
				CtlUsuariosLista ctlUsuarios=new CtlUsuariosLista(viewListaUsuarios);
				
				/*ViewCrearUsuario viewCrearUsuario=new ViewCrearUsuario(view);
				CtlUsuario ctlUsuario=new CtlUsuario(viewCrearUsuario, conexion);
				viewCrearUsuario.setVisible(true);*/
				
				break;
				
			case "INVENTARIO":
				try {
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
					AbstractJasperReports.createReportInventario(conexion.getPoolConexion().getConnection(), conexion.getUsuarioLogin().getUser());
					
					//this.view.setModal(false);
					//AbstractJasperReports.imprimierFactura();
					AbstractJasperReports.showViewer(this.view);
					
					
				} catch (SQLException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
				break;
				
				
			case "CIERRES_CAJA":
				
				ViewListaCierresCaja viewCierres=new ViewListaCierresCaja(view);
				CtlCierresCajaLista ctlCierres=new CtlCierresCajaLista(viewCierres);
				
				viewCierres.dispose();
				viewCierres=null;
				ctlCierres=null;
				break;
				
			case "EMPLEADOS":
				
				ViewListaEmpleados viewEmpleados=new ViewListaEmpleados(view);
				CtlEmpleadosLista ctlListaEmpleados=new CtlEmpleadosLista(viewEmpleados);
				
				viewEmpleados.dispose();
				viewEmpleados=null;
				ctlListaEmpleados=null;
				
				break;
			case "COMISIONES":
				ViewFiltroComisiones viewComisiones=new ViewFiltroComisiones(view);
				CtlFiltroRepComisiones ctlComisiones= new CtlFiltroRepComisiones(viewComisiones);
				viewComisiones.dispose();
				viewComisiones=null;
				ctlComisiones=null;
				
				break;
				
			case "SALIDASCAJAS":
				
				ViewListaSalidas viewSalidas=new ViewListaSalidas(view);
				CtlSalidasListas ctlSalidas=new CtlSalidasListas(viewSalidas);
				break;
		}
		
	}

}
