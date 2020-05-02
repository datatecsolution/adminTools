package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Conexion;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.dao.CotizacionDao;
import net.datatecsolution.admin_tools.view.ViewFacturar;
import net.datatecsolution.admin_tools.view.ViewListaCotizacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CtlCotizacionLista implements ActionListener, MouseListener {
	
	private Conexion conexion=null;
	private ViewListaCotizacion view=null;
	private CotizacionDao myFacturaDao=null;
	private int filaPulsada;
	private Factura myFactura;
	private boolean resultado;
	
	public CtlCotizacionLista(){
		
	}

	public CtlCotizacionLista(ViewListaCotizacion v) {
		// TODO Auto-generated constructor stub
		view=v;
		view.conectarControlador(this);
		myFacturaDao=new CotizacionDao();
		cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		//setMyFactura(new Factura());
		
		//view.setVisible(true);
	}
	
	public void cargarTabla(List<Factura> facturas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiarFacturas();
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				this.view.getModelo().agregarFactura(facturas.get(c));
				
			}
		}else{
			//JOptionPane.showMessageDialog(view, "No se encontro ninguna cotizacion. Escriba otra busqueda", "Error al buscar", JOptionPane.ERROR_MESSAGE);
			//view.getTxtBuscar().requestFocusInWindow();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		filaPulsada=this.view.getTabla().getSelectedRow();
		
		 //si seleccion una fila
        if(filaPulsada>=0){
        	
        	
        	//se consigue el proveedore de la fila seleccionada
        	setMyFactura(this.view.getModelo().getFactura(filaPulsada));
        	
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		//si el boton de agregar esta desactividado se esconde la venta porque se llamo desde facturacion
        		if(!view.getBtnAgregar().isEnabled()){
		        		setResultado(true);
		    			//myArticuloDao.desconectarBD();
		    			this.view.setVisible(false);
		    			//JOptionPane.showMessageDialog(null,myMarca);
		    			this.view.dispose();
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
		
		switch(comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
			
		case "BUSCAR":
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){ 
				
	
				myFactura=myFacturaDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myFactura!=null){
					resultado=true;
					this.view.getModelo().limpiarFacturas();
					this.view.getModelo().agregarFactura(myFactura);//.agregarArticulo(myArticulo);
					//view.setVisible(false);
				}else{
					JOptionPane.showMessageDialog(view, "No se encontro ninguna cotizacion. Escriba otra busqueda", "Error al buscar", JOptionPane.ERROR_MESSAGE);
					this.view.getTxtBuscar().setText("");
				}
			} 
			
			if(this.view.getRdbtnCliente().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myFacturaDao.buscarPorCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			if(view.getRdbtnFecha().isSelected()){ 
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(myFacturaDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
			
				}
			
			if(view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				}
			break;
		case "INSERTAR":
			
			ViewFacturar vistaFacturar=new ViewFacturar(this.view);
			vistaFacturar.pack();
			CtlFacturar ctlFacturar=new CtlFacturar(vistaFacturar );
				
			vistaFacturar.getTxtBuscar().requestFocusInWindow();
			
			//myFac=ctlFacturar.getAccion();
			boolean resul=ctlFacturar.getAccion();
			//JOptionPane.showMessageDialog(view, myFac);
			
			if(resul){
				view.getModelo().netPag();
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			}
			
			vistaFacturar.dispose();
			vistaFacturar=null;
			ctlFacturar=null;
			break;
		case "IMPRIMIR":
			
			//se comprueba que este selecciona una fila de la tabla
			if(view.getTabla().getSelectedRow()>=0){
					try {
						myFactura=view.getModelo().getFactura(view.getTabla().getSelectedRow());
						
						AbstractJasperReports.crearReporteCotizacion(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
						AbstractJasperReports.showViewer(view);
						//AbstractJasperReports.imprimierFactura();
						
						
					
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(null, ee.getMessage(),"Error al generar el reporte",JOptionPane.ERROR_MESSAGE);
						ee.printStackTrace();
					}
			}else{
				JOptionPane.showMessageDialog(null, "Debe selecionar una fila de la tabla.","Error al generar el reporte",JOptionPane.ERROR_MESSAGE);
			}
		
		
			break;
		case "ELIMINAR":

		break;
		
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				}
			
			if(view.getRdbtnFecha().isSelected()){ 
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(myFacturaDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
			
				}
			if(this.view.getRdbtnCliente().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myFacturaDao.buscarPorCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				}
			
			if(view.getRdbtnFecha().isSelected()){ 
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(myFacturaDao.buscarPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
			
				}
			if(this.view.getRdbtnCliente().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myFacturaDao.buscarPorCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}

	}

	public boolean isResultado() {
		return resultado;
	}

	public void setResultado(boolean resultado) {
		this.resultado = resultado;
	}

	public Factura getMyFactura() {
		return myFactura;
	}

	public void setMyFactura(Factura myFactura) {
		this.myFactura = myFactura;
	}
	
public boolean buscarCotizaciones(Window v){
		
		//this.myArticuloDao.cargarInstrucciones();
		//cargarTabla(myArticuloDao.todoArticulos());
		myFactura=null;
		this.view.getRdbtnCliente().setSelected(true);
		this.view.getBtnEliminar().setEnabled(false);
		this.view.getBtnAgregar().setEnabled(false);
		this.view.setLocationRelativeTo(v);
		this.view.setModal(true);
		view.getTxtBuscar().requestFocusInWindow();
		
		//view.setFocusable(true);
		this.view.setVisible(true);
		//
		return resultado;
	}

}
