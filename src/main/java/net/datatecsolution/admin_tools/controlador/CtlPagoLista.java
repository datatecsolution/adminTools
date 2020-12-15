package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.ReciboPago;
import net.datatecsolution.admin_tools.modelo.dao.ReciboPagoDao;
import net.datatecsolution.admin_tools.view.ViewFiltroPagos;
import net.datatecsolution.admin_tools.view.ViewListaPagos;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CtlPagoLista implements ActionListener, MouseListener, ChangeListener, WindowListener  {
	
	
	private ViewListaPagos view;
	private ReciboPagoDao reciboDao=null;
	private ReciboPago myRecibo=null;
	
	//fila selecciona enla lista
	private int filaPulsada;

	public CtlPagoLista(ViewListaPagos v) {
		// TODO Auto-generated constructor stub
		view =v;
		
		view.conectarControlador(this);
		reciboDao=new ReciboPagoDao();

		myRecibo=new ReciboPago();
		cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
	}
	public void cargarTabla(List<ReciboPago> pagos){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiar();
		
		if(pagos!=null){
			for(int c=0;c<pagos.size();c++){
				this.view.getModelo().agregarPago(pagos.get(c));
				
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){

            this.myRecibo=this.view.getModelo().getRecibo(filaPulsada);
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		
        		
        		try {

					AbstractJasperReports.createReportReciboCobroCajaFactura(ConexionStatic.getPoolConexion().getConnection(), myRecibo.getNoRecibo());
					AbstractJasperReports.showViewer(view);
					//AbstractJasperReports.imprimierFactura();

    				this.view.getBtnCobrador().setEnabled(false);
    				myRecibo=null;
    			} catch (SQLException ee) {
    				// TODO Auto-generated catch block
    				ee.printStackTrace();
    			}
			
				
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		this.view.getBtnEliminar().setEnabled(true);
        		
        		
        	}
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		
		switch (comando){

			case "IMPRIMIR":
				ViewFiltroPagos vFiltroPagos2=new ViewFiltroPagos(null);
				CtlFiltroRepPagosVendedor ctlFiltroRepPagosVendedor=new CtlFiltroRepPagosVendedor(vFiltroPagos2);

				break;
		
		
			case "REPORTE":
				ViewFiltroPagos vFiltroPagos=new ViewFiltroPagos(null);
				CtlFiltroRepPagos cFiltroPagos=new CtlFiltroRepPagos(vFiltroPagos);

			break;

			case "ESCRIBIR":
				view.setTamanioVentana(1);
				break;
		
			case "INSERTAR":
				/*
				ViewCobro viewCobro=new ViewCobro(view);
				Conexion conexion=null;
				CtlCobro ctlCobro=new CtlCobro(viewCobro);

				viewCobro.dispose();
				viewCobro=null;
				ctlCobro=null;
				view.getModelo().setPaginacion();
				cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));

				 */
				break;
			case "BUSCAR":
			
				//si la busqueda es por id
				if(this.view.getRdbtnId().isSelected()){
					myRecibo=reciboDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
					if(myRecibo!=null){
						this.view.getModelo().limpiar();
						this.view.getModelo().agregarPago(myRecibo);
					}else{
						JOptionPane.showMessageDialog(view, "No se encuentro la factura");
					}

				}
				//si la busqueda es por fecha
				if(this.view.getRdbtnFecha().isSelected()){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date1 = sdf.format(this.view.getDcFecha1().getDate());
					String date2 = sdf.format(this.view.getDcFecha2().getDate());

					cargarTabla(reciboDao.reciboPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));

					}

				//si la busqueda son tadas
				if(this.view.getRdbtnRef().isSelected()){
					cargarTabla(reciboDao.buscarPorRef(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}


				//si la busqueda son tadas
				if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					this.view.getTxtBuscar().setText("");
					}
				break;
			
			case "NEXT":
				view.getModelo().netPag();
				if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					this.view.getTxtBuscar().setText("");
					}
				//si la busqueda es por fecha
				if(this.view.getRdbtnFecha().isSelected()){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date1 = sdf.format(this.view.getDcFecha1().getDate());
					String date2 = sdf.format(this.view.getDcFecha2().getDate());

					cargarTabla(reciboDao.reciboPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));

					}
				//si la busqueda son tadas
				if(this.view.getRdbtnRef().isSelected()){
					cargarTabla(reciboDao.buscarPorRef(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
			case "LAST":
				view.getModelo().lastPag();
				if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					this.view.getTxtBuscar().setText("");
					}
				//si la busqueda es por fecha
				if(this.view.getRdbtnFecha().isSelected()){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date1 = sdf.format(this.view.getDcFecha1().getDate());
					String date2 = sdf.format(this.view.getDcFecha2().getDate());

					cargarTabla(reciboDao.reciboPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));

					}
				//si la busqueda son tadas
				if(this.view.getRdbtnRef().isSelected()){
					cargarTabla(reciboDao.buscarPorRef(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
		}

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		view.setVisible(false);
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
