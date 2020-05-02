package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.view.ViewListaCajas;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlCajasBuscar extends CtlGenerico implements ActionListener, MouseListener,WindowListener, ItemListener {
	
	private ViewListaCajas view;

	private CajaDao myDao;
	
	private Caja myCaja;
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	
	private boolean resultadoBuscar=false;
	
	public CtlCajasBuscar(ViewListaCajas v){
		view=v;
		
		
		myDao=new CajaDao();
		
		view.conectarCtlBuscar(this);
		
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior()));
		//view.setVisible(true);
		
	}
	public boolean buscarCaja(){
		view.setVisible(true);
		return this.resultadoBuscar;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
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
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		filaPulsada = this.view.getTabla().getSelectedRow();
		 
		 //si seleccion una fila
	        if(filaPulsada>=0){
	        	
	        	//si fue doble click mostrar modificar
	        	if (e.getClickCount() == 2) {
	        		
	        		myCaja=view.getModelo().getCaja(filaPulsada);
	        		
	        		this.resultadoBuscar=true;
	        		
	        		this.view.setVisible(false);
	    			//JOptionPane.showMessageDialog(null,myMarca);
	    			this.view.dispose();
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
				
			case "ESCRIBIR":
				view.setTamanioVentana(1);
				break;
			case "BUSCAR":
				
				view.getModelo().setPaginacion();
				
				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior()));
				}
				
				if(this.view.getRdbtnId().isSelected()){
					Caja cajaBusca=myDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
					if(cajaBusca!=null){												
						this.view.getModelo().limpiar();
						this.view.getModelo().add(cajaBusca);
					}else{
						this.view.getModelo().limpiar();
						JOptionPane.showMessageDialog(view, "No se encuentro ningun registro","Error",JOptionPane.ERROR_MESSAGE);
					}
					
				}
				
				if(view.getRdbtnDescripcion().isSelected()){
					this.cargarTabla(myDao.buscarPorDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
			case "NEXT":
				view.getModelo().netPag();
				
				if(view.getRdbtnDescripcion().isSelected()){
					this.cargarTabla(myDao.buscarPorDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				
				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior()));
				}
				
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
				
			case "LAST":
				view.getModelo().lastPag();
				
				if(view.getRdbtnDescripcion().isSelected()){
					this.cargarTabla(myDao.buscarPorDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				
				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior()));
				}
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
		}
		
	}
	
	public void cargarTabla(List<Caja> cajas) {
		// TODO Auto-generated method stub
		this.view.getModelo().limpiar();
		if(cajas!=null){
			for(int c=0;c<cajas.size();c++)
				this.view.getModelo().agregar(cajas.get(c));
		}
		
	}
	public Caja getMyCaja() {
		return myCaja;
	}
}
