package net.datatecsolution.admin_tools.controlador;


import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;
import net.datatecsolution.admin_tools.view.ViewCrearCategoria;
import net.datatecsolution.admin_tools.view.ViewListaCategorias;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlCategoriaBuscar implements ActionListener, MouseListener,WindowListener, ItemListener{
	
	//formulario para Modificar, insertar marcas
		public ViewCrearCategoria viewMarca;
		//lista de marcas
		public ViewListaCategorias view;
		
		//modelo para consultar la base de datos
		public CategoriaDao myMarcaDao;
		
		//modelo de datos
		public Categoria myMarca=null;
		
		//fila selecciona enla lista
		private int filaPulsada;
		
		
		public CtlCategoriaBuscar(ViewListaCategorias view){
			
			this.view=view;
			myMarca=new Categoria();
			myMarcaDao=new CategoriaDao();
			cargarTabla(myMarcaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			
		}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		this.view.getTxtBuscar().setText("");
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
		this.view.setVisible(false);
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
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
		if (e.getClickCount() == 2){
			myMarca=this.view.getModelo().getMarca(filaPulsada);
			this.view.setVisible(false);
			//JOptionPane.showMessageDialog(null,myMarca);
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

		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){  
				myMarca=myMarcaDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myMarca!=null){
					this.view.getModelo().limpiarMarcas();
                    this.view.getModelo().agregarMarca(myMarca);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
				}
			} 
			
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myMarcaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myMarcaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myMarcaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myMarcaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myMarcaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myMarcaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myMarcaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myMarcaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myMarcaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	
		
	}
	public void cargarTabla(List<Categoria> marcas){
		
		this.view.getModelo().limpiarMarcas();
		for(int c=0;c<marcas.size();c++)
			this.view.getModelo().agregarMarca(marcas.get(c));
		
	}
	
	public Categoria buscarMarca(){
		
		this.view.setVisible(true);
		return myMarca;
	}

}
