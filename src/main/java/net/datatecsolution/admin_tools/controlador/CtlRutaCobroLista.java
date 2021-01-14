package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.modelo.dao.RutaCobroDao;
import net.datatecsolution.admin_tools.view.ViewCrearRutaCobro;
import net.datatecsolution.admin_tools.view.ViewListaRutasCobro;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlRutaCobroLista implements ActionListener, MouseListener,WindowListener, ItemListener {

	//formulario para Modificar, insertar marcas
	//public ViewCrearCategoria viewCategoria;
	//lista de marcas
	public ViewListaRutasCobro view;

	//modelo para consultar la base de datos
	public RutaCobroDao myRutasCobroDao;

	//modelo de datos
	public RutaCobro myRuta;

	//fila selecciona enla lista
	private int filaPulsada;


	public CtlRutaCobroLista(ViewListaRutasCobro v){
		
		this.view=v;
		myRuta =new RutaCobro();
		myRutasCobroDao =new RutaCobroDao();
		cargarTabla(myRutasCobroDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
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

				ViewCrearRutaCobro viewCrearRutaCobro=new ViewCrearRutaCobro(this.view);
				CtlRutaCobro ctlRuta=new CtlRutaCobro(viewCrearRutaCobro);

				//se llama el metodo agregar proveedor que devuelve un resultado
				boolean resul=ctlRuta.agregarRuta();
				
				if(resul){//se procesa el resultado de agregar proveeros
					
					view.getModelo().setPaginacion();
					cargarTabla(myRutasCobroDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			
				}
				viewCrearRutaCobro.dispose();
				viewCrearRutaCobro=null;
				ctlRuta=null;


			break;
		case "ELIMINAR":
			if(myRutasCobroDao.eliminar(myRuta.getCodigo())){//llamamos al metodo para agregar
				JOptionPane.showMessageDialog(this.view, "Se elimino exitosamente","Informaci�n",JOptionPane.INFORMATION_MESSAGE);
				this.view.getModelo().eliminar(filaPulsada);
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
				myRuta = myRutasCobroDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myRuta !=null){
					this.view.getModelo().limpiar();
					this.view.getModelo().agregar(myRuta);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
				}
			} 
			
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myRutasCobroDao.porObser(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDescripcion().isSelected()){
				cargarTabla(myRutasCobroDao.porDescripcion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myRutasCobroDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myRutasCobroDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myRutasCobroDao.porObser(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDescripcion().isSelected()){
				cargarTabla(myRutasCobroDao.porDescripcion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myRutasCobroDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnObservacion().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myRutasCobroDao.porObser(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDescripcion().isSelected()){
				cargarTabla(myRutasCobroDao.porDescripcion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
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
            myRuta =this.view.getModelo().getRuta(filaPulsada);//se consigue el proveedore de la fila seleccionada
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
	        	//myProveedor=myProveedorDao.buscarPro(identificador);
	        	myRuta =this.view.getModelo().getRuta(filaPulsada);//se consigue el Marca de la fila seleccionada


	        	//se crea la vista para modificar proveedor con un proveedor ya hecho
	        	ViewCrearRutaCobro viewCategoria= new ViewCrearRutaCobro(this.view);
	        	
	        	//se crea el controlador para la vista modificar
				CtlRutaCobro ctlMarcaActualizar=new CtlRutaCobro(viewCategoria);
				
				//se asigna el contrador a los elementos de la vista
				viewCategoria.conectarControlador(ctlMarcaActualizar);
				
				//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
				boolean resultado=ctlMarcaActualizar.actualizarRuta(myRuta);
				
				//se proceso el resultado de modificar la marca
				if(resultado){
					this.view.getModelo().cambiarRuta(filaPulsada, ctlMarcaActualizar.getRuta());//se cambia en la vista
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
	
	public void cargarTabla(List<RutaCobro> rutas){
		
		this.view.getModelo().limpiar();
		if(rutas!=null){
			for(int c=0;c<rutas.size();c++)
				this.view.getModelo().agregar(rutas.get(c));
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
