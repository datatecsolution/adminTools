package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;
import net.datatecsolution.admin_tools.modelo.dao.CierreCajaDao;
import net.datatecsolution.admin_tools.modelo.dao.CierreFacturacionDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepCierreVentaDetalle;
import net.datatecsolution.admin_tools.view.ViewFiltroRepCierreVentaDetalleCateg;
import net.datatecsolution.admin_tools.view.ViewListaCierresCaja;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CtlCierresCajaLista extends MouseAdapter implements ActionListener, MouseListener {
	private ViewListaCierresCaja view;
	private CierreCajaDao myDao;
	private int filaPulsada;
	private CierreCaja myCierre;
	private FacturaDao facturaDao=new FacturaDao();
	

	public CtlCierresCajaLista(ViewListaCierresCaja v) {
		// TODO Auto-generated constructor stub
		view=v;
		myDao=new CierreCajaDao();
		myCierre=new CierreCaja();
		view.conectarCtl(this);
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
	}
	
	private void cargarTabla(List<CierreCaja> cierres) {
		// TODO Auto-generated method stub
		view.getModelo().limpiar();
		if(cierres!=null){
			for(int x=0;x<cierres.size();x++)
				view.getModelo().agregar(cierres.get(x));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	myCierre=view.getModelo().getCierreCaja(filaPulsada);
        	
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		try {
        			AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 4, myCierre.getId());
        			AbstractJasperReports.showViewer(this.view);
        			
        		}catch (SQLException ee) {
    				// TODO Auto-generated catch block
    				ee.printStackTrace();
    			}
        	}
        }
		
	}

	@Override
	public void mousePressed(MouseEvent evento) {
		// TODO Auto-generated method stub
		check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador
		
	}

	@Override
	public void mouseReleased(MouseEvent evento) {
		// TODO Auto-generated method stub
		
		int r = view.getTabla().rowAtPoint(evento.getPoint());
        if (r >= 0 && r < view.getTabla().getRowCount()) {
        	view.getTabla().setRowSelectionInterval(r, r);
        } else {
        	view.getTabla().clearSelection();
        }

        int rowindex = view.getTabla().getSelectedRow();
        if (rowindex < 0)
            return;
        
        check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador
		
	}
	
	// determina si el evento debe desencadenar el menu contextual
			private void checkForTriggerEvent( MouseEvent evento )
			{
				if ( evento.isPopupTrigger() )
					this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY() );
			} // fin del metodo checkForTriggerEvent
			
			public void check(MouseEvent e)
			{ 
				if (e.isPopupTrigger()) { //if the event shows the menu 
					//this.view.getListCodigos().setSelectedIndex(this.view.getListCodigos().locationToIndex(e.getPoint())); //select the item 
					//view.getTabla().setColumnSelectionInterval(index0, index1); 
					//JOptionPane.showMessageDialog(view, "Donde se dio clip "+e.getPoint());
				} 
				
				
			}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "REPORTE_DETALLE_VENTA_CATEGORIA":
			
			filaPulsada = this.view.getTabla().getSelectedRow();
	        
	        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        	
	        	
	        	//se consigue el cierre guardado
	        	CierreCaja elCierre=view.getModelo().getCierreCaja(filaPulsada);
	        	
	        	
	        	ViewFiltroRepCierreVentaDetalleCateg viewFiltroRepCierreVentaDetalleCateg=new ViewFiltroRepCierreVentaDetalleCateg(view);
	        	CtlFiltroRepCierreVentasDetalleCateg ctlFiltroRepCierreVentasDetalleCateg=new CtlFiltroRepCierreVentasDetalleCateg(viewFiltroRepCierreVentaDetalleCateg,elCierre);
				
	  
	        }
		
		break;
		
		case "REPORTE_VENTA_CATEGORIA":
			
			filaPulsada = this.view.getTabla().getSelectedRow();
	        
	        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        	
	        	
	        	//se consigue el cierre guardado
	        	CierreCaja elCierre=view.getModelo().getCierreCaja(filaPulsada);
				
				//se crea el informe de ventas por categorias
				List<VentasCategoria> ventas= new ArrayList<VentasCategoria>();
				
				//se extren todas las categorias
				CategoriaDao categoriaDao=new CategoriaDao();
				List<Categoria> categorias=categoriaDao.todos();
				
				//se recorren las categorias y se agregan a reporte de ventas
				for(int yy=0;yy<categorias.size();yy++){
					VentasCategoria una =new VentasCategoria();
					una.setCodigoCategoria(categorias.get(yy).getId());
					una.setCategoria(categorias.get(yy).getDescripcion());
					ventas.add(una);
				}
				
				//se consigue los registros de las facturas de los cierre
				CierreFacturacionDao cierreFacturacioDao=new CierreFacturacionDao();
				elCierre.setCierreFacturas(cierreFacturacioDao.buscarIdCierre(elCierre.getId()));
				
				//si existe los registros de los cierres se recororen en busca los totales en las facturas
				if(elCierre.getCierreFacturas()!=null){
					
					//se recorren los registros para sacar los totales de la facturas
					for(int xx=0;xx<elCierre.getCierreFacturas().size();xx++){
						
						facturaDao.getVentasCategorias(elCierre.getCierreFacturas().get(xx).getNoFacturaInicio(),
															elCierre.getCierreFacturas().get(xx).getNoFacturaFinal(), 
															elCierre.getUsuario(), 
															elCierre.getCierreFacturas().get(xx).getCaja(), ventas);
						
						
				
					}
					
					try {
					
						AbstractJasperReports.createReportVentasCategoria(ConexionStatic.getPoolConexion().getConnection(),elCierre,ventas);
						
						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
					} catch (SQLException eee) {
						// TODO Auto-generated catch block
						eee.printStackTrace();
					}
				}
	        }
			break;
		
		case "REPORTE_DETALLE_VENTA":
			
			filaPulsada = this.view.getTabla().getSelectedRow();
	        
	        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        	
	        	
	        	//se consigue el cierre guardado
	        	CierreCaja elCierre=view.getModelo().getCierreCaja(filaPulsada);
	        	
	        	
	        	ViewFiltroRepCierreVentaDetalle viewFiltroReportVentaDetalleCierre=new ViewFiltroRepCierreVentaDetalle(view);
	        	CtlFiltroRepCierreVentasDetalle ctlFiltroRepVentasDetalleCierre=new CtlFiltroRepCierreVentasDetalle(viewFiltroReportVentaDetalleCierre,elCierre);
				
	  
	        }
	        
			break;
		
		case "IMPRIMIR":
			
			//Recoger que fila se ha pulsadao en la tabla
	        filaPulsada = this.view.getTabla().getSelectedRow();
	        
	        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        	myCierre=view.getModelo().getCierreCaja(filaPulsada);
	        	
	        	try {
	        			AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 4, myCierre.getId());
	        			//AbstractJasperReports.showViewer(this.view);
	        			AbstractJasperReports.imprimierFactura();
	        			
	        		}catch (SQLException ee) {
	    				// TODO Auto-generated catch block
	    				ee.printStackTrace();
	    			}
	    
	        }
			
			
			

			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			if(view.getRdbtnId().isSelected()){
				this.myCierre=myDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText()));
				if(myCierre!=null){
					view.getModelo().limpiar();
					view.getModelo().agregar(myCierre);
				}else{
					JOptionPane.showMessageDialog(view, "No se encontro el registro.");
				}
			}
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			
			if(view.getRdbtnFecha().isSelected()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnFecha().isSelected()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnFecha().isSelected()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		}
		
	}

}
