package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ReciboPagoProveedor;
import net.datatecsolution.admin_tools.modelo.dao.ReciboPagoProveedoresDao;
import net.datatecsolution.admin_tools.view.ViewListaPagosProveedores;
import net.datatecsolution.admin_tools.view.ViewPagoProveedor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class CtlPagosProveedoresLista implements ActionListener, MouseListener, ChangeListener, WindowListener {
	
	private ViewListaPagosProveedores view;
	
	private ReciboPagoProveedor myRecibo=null;
	private ReciboPagoProveedoresDao reciboDao=null;
	
	public CtlPagosProveedoresLista(ViewListaPagosProveedores v){
		view =v;
		view.conectarControlador(this);
		myRecibo=new ReciboPagoProveedor();
		reciboDao=new ReciboPagoProveedoresDao();
		cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
	}
	
	public void cargarTabla(List<ReciboPagoProveedor> pagos){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiar();
		
		if(pagos!=null){
			for(int c=0;c<pagos.size();c++){
				this.view.getModelo().agregarPago(pagos.get(c));
				
			}
		}
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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

		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch (comando){
		
		case "INSERTAR":
			ViewPagoProveedor vPagoProveedores=new ViewPagoProveedor(view);
			CtlPagoProveedor cPagoProveedores=new CtlPagoProveedor(vPagoProveedores);
			vPagoProveedores.dispose();
			cPagoProveedores=null;
			view.getModelo().setPaginacion();
			cargarTabla(reciboDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			break;
		case "FECHA":
			
			
		case "TODAS":
			
			
			break;
		case "ID":
			
			
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
				
				cargarTabla(reciboDao.reciboProveedorPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
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
				
				cargarTabla(reciboDao.reciboProveedorPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
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
				
				cargarTabla(reciboDao.reciboProveedorPorFecha(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}

	
		
	}

}
