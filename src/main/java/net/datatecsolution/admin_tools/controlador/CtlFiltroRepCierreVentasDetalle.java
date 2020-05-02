package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.CierreFacturacionDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepCierreVentaDetalle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtlFiltroRepCierreVentasDetalle implements ActionListener {
	
	private ViewFiltroRepCierreVentaDetalle view;
	private CajaDao cajaDao;
	private CierreCaja elCierre=null;
	private DetalleFacturaDao detalleFacturaDao=new DetalleFacturaDao();

	public CtlFiltroRepCierreVentasDetalle(ViewFiltroRepCierreVentaDetalle v,CierreCaja c) {
		// TODO Auto-generated constructor stub
		
		view =v;
		elCierre=c;
		view.conectarCtl(this);
		
		cajaDao=new CajaDao();
		cargarComboBox();
		
		
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCajas().setLista(this.cajaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbxCajas().removeAllItems();
	
		this.view.getCbxCajas().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GENERAR":
			
			Caja myCaja=(Caja)view.getCbxCajas().getSelectedItem();
			
		
	        
	        
	        //si seleccion una fila
	        if(elCierre!=null){
	        	
	        	
	        	
				
				//se crea el informe de ventas por categorias
				List<DetalleFactura> ventas= new ArrayList<DetalleFactura>();
				
				
				//se consigue los registros de las facturas de los cierre
				CierreFacturacionDao cierreFacturacioDao=new CierreFacturacionDao();
				elCierre.setCierreFacturas(cierreFacturacioDao.buscarIdCierre(elCierre.getId()));
				
				//si existe los registros de los cierres se recororen en busca los totales en las facturas
				if(elCierre.getCierreFacturas()!=null){
					
					//se recorren los registros para sacar los totales de la facturas
					for(int xx=0;xx<elCierre.getCierreFacturas().size();xx++){
						
						//solo se consegiran los datos de la caja seleccionado
						if(elCierre.getCierreFacturas().get(xx).getCaja().getCodigo()==myCaja.getCodigo())
						{
							detalleFacturaDao.getDetallesFactura(elCierre.getCierreFacturas().get(xx).getNoFacturaInicio(),
															elCierre.getCierreFacturas().get(xx).getNoFacturaFinal(), 
															elCierre.getUsuario(), 
															elCierre.getCierreFacturas().get(xx).getCaja(), ventas);
						}
						
						
				
					}
					
					try {
					
						AbstractJasperReports.createReportVentasDetalleTurno(ConexionStatic.getPoolConexion().getConnection(),elCierre,ventas,myCaja);
						
						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
					} catch (SQLException eee) {
						// TODO Auto-generated catch block
						eee.printStackTrace();
					}
				}
	        }

			
			break;
		}
	}

}
