package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;
import net.datatecsolution.admin_tools.view.ViewCrearCategoria;
import net.datatecsolution.admin_tools.view.ViewListaCategorias;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlCategoriaLista implements ActionListener, MouseListener,WindowListener, ItemListener {
	
	//formulario para Modificar, insertar marcas
	//public ViewCrearCategoria viewCategoria;
	//lista de marcas
	public ViewListaCategorias view;
	
	//modelo para consultar la base de datos
	public CategoriaDao myCategoriaDao;
	
	//modelo de datos
	public Categoria myCategoria;
	
	//fila selecciona enla lista
	private int filaPulsada;
	
	
	public CtlCategoriaLista(ViewListaCategorias v){
		
		this.view=v;
		myCategoria=new Categoria();
		myCategoriaDao=new CategoriaDao();
		cargarTabla(myCategoriaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		//conectar los controles al objeto que ctlMarca
		view.conectarControlador(this);
		
		//hacer visible la view lista de marcas
		view.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
			
		case "INSERTAR":
				ViewCrearCategoria viewCategoria=new ViewCrearCategoria(this.view);
				CtlCategoria ctlMarca=new CtlCategoria(viewCategoria,myCategoriaDao);
				viewCategoria.conectarControlador(ctlMarca);
				//ctlMarca.setMyMarcaDao(myMarcaDao);
				
				boolean resul=ctlMarca.agregarMarca();//se llama el metodo agregar proveedor que devuelve un resultado
				
				if(resul){//se procesa el resultado de agregar proveeros
					
					view.getModelo().setPaginacion();
					cargarTabla(myCategoriaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			
				}
				viewCategoria.dispose();
				viewCategoria=null;
				ctlMarca=null;
			break;
		case "ELIMINAR":
			if(myCategoriaDao.eliminar(myCategoria.getId())){//llamamos al metodo para agregar 
				JOptionPane.showMessageDialog(this.view, "Se elimino exitosamente","Informaci�n",JOptionPane.INFORMATION_MESSAGE);
				this.view.getModelo().eliminarMarca(filaPulsada);
				this.view.getBtnEliminar().setEnabled(false);
				
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
			}
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){  
				myCategoria=myCategoriaDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myCategoria!=null){
					this.view.getModelo().limpiarMarcas();
					this.view.getModelo().agregarMarca(myCategoria);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
				}
			} 
			
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myCategoriaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myCategoriaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myCategoriaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myCategoriaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myCategoriaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myCategoriaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myCategoriaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myCategoriaDao.buscarPorObservacion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnCategoria().isSelected()){  
				cargarTabla(myCategoriaDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//Se recoge el id de la fila marcada
            int identificador= (int)this.view.getModelo().getValueAt(filaPulsada, 0);
            myCategoria=this.view.getModelo().getMarca(filaPulsada);//se consigue el proveedore de la fila seleccionada
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
	        	//myProveedor=myProveedorDao.buscarPro(identificador);
	        	myCategoria=this.view.getModelo().getMarca(filaPulsada);//se consigue el Marca de la fila seleccionada
	           
	        	//se crea la vista para modificar proveedor con un proveedor ya hecho
	        	ViewCrearCategoria viewCategoria= new ViewCrearCategoria(myCategoria,this.view);
	        	
	        	//se crea el controlador para la vista modificar
				CtlCategoria ctlMarcaActualizar=new CtlCategoria(viewCategoria,myCategoriaDao);
				
				//se asigna el contrador a los elementos de la vista
				viewCategoria.conectarControlador(ctlMarcaActualizar);
				
				//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
				boolean resultado=ctlMarcaActualizar.actualizarMarca();
				
				//se proceso el resultado de modificar la marca
				if(resultado){
					this.view.getModelo().cambiarMarca(filaPulsada, ctlMarcaActualizar.getMarca());//se cambia en la vista
					this.view.getModelo().fireTableDataChanged();//se refrescan los cambios
					this.view.getTabla().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
				}	
				
				viewCategoria.dispose();
				viewCategoria=null;
				ctlMarcaActualizar=null;
				
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		this.view.getBtnEliminar().setEnabled(true);
        		/*idProveedor=identificador;
        		filaTabla=filaPulsada;*/
        		
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
	
	public void cargarTabla(List<Categoria> marcas){
		
		this.view.getModelo().limpiarMarcas();
		if(marcas!=null){
			for(int c=0;c<marcas.size();c++)
				this.view.getModelo().agregarMarca(marcas.get(c));
		}
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
		this.view.setVisible(false);
		
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
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		this.view.getTxtBuscar().setText("");
		
	}

}
