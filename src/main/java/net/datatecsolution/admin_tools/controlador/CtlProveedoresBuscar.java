package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Proveedor;
import net.datatecsolution.admin_tools.modelo.dao.ProveedorDao;
import net.datatecsolution.admin_tools.view.ViewListaProveedor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CtlProveedoresBuscar implements ActionListener ,MouseListener, WindowListener,KeyListener {
	
	
	private Proveedor myProveedor=null;
	private ProveedorDao myDao=null;
	private ViewListaProveedor view;

	private boolean resultado=false;
	private int filaPulsada=-1;
	
	public CtlProveedoresBuscar(ViewListaProveedor v){
		
		view=v;
		view.conectarControladorBuscar(this);
		myDao=new ProveedorDao();
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior()));
		
		view.getRdbtnNombre().setSelected(true);
		view.getTxtBuscar().requestFocusInWindow();
		
		
	}
	
	public void cargarTabla(List<Proveedor> proveedores){
		view.getModelo().limpiarProveedores();
		
		if(proveedores!=null){
			for(int c=0;c<proveedores.size();c++){
				this.view.getModelo().agregarProveedor(proveedores.get(c));
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
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
		if (e.getClickCount() == 2){
			myProveedor=view.getModelo().getProveedor(filaPulsada);
			resultado=true;
			
			this.view.setVisible(false);
			this.view.dispose();
			
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
		
		
		
			case "NEXT":
				view.getModelo().netPag();
				if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
					
					cargarTabla(myDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			        
					}
				if(this.view.getRdbtnDireccion().isSelected()){  
					cargarTabla(myDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
			case "LAST":
				view.getModelo().lastPag();
				if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
					
					cargarTabla(myDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			        
					}
				if(this.view.getRdbtnDireccion().isSelected()){  
					cargarTabla(myDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
				
			case "BUSCAR":
				view.getModelo().setPaginacion();
				//si se seleciono el boton ID
				if(this.view.getRdbtnId().isSelected()){  
					myProveedor=myDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
					if(myProveedor!=null){
						this.view.modelo.limpiarProveedores();
						this.view.modelo.agregarProveedor(myProveedor);
					}else{
						JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
					}
				} 
				
				if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
					
					cargarTabla(myDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			        
					}
				if(this.view.getRdbtnDireccion().isSelected()){  
					cargarTabla(myDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
				
				if(this.view.getRdbtnTodos().isSelected()){  
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					this.view.getTxtBuscar().setText("");
					}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
			case "ESCRIBIR":
				view.setTamanioVentana(1);
				
				break;
				
				
			
		}
		
	}
	
	public boolean buscarProveedores(Window v){
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		this.view.setLocationRelativeTo(v);
		this.view.setModal(true);
		this.view.setVisible(true);
		return resultado;
		
	}

	/**
	 * @return the myProveedor
	 */
	public Proveedor getProveedor() {
		return myProveedor;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			resultado=false;
			this.myProveedor=null;
	         view.setVisible(false);
	      }
		
	}


}
