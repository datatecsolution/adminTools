package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import net.datatecsolution.admin_tools.modelo.dao.PrecioArticuloDao;
import net.datatecsolution.admin_tools.view.ViewSelectPrecio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CtlSelectPrecio implements ActionListener, KeyListener {
	
	private ViewSelectPrecio view;
	private PrecioArticuloDao preciosDao=null;
	private PrecioArticulo myPrecio=new PrecioArticulo();
	
	private boolean resultaOperacion=false;
	private boolean aplicarTodo=false;
	
	public CtlSelectPrecio(ViewSelectPrecio v){
		view=v;
		preciosDao=new PrecioArticuloDao();
		
		//modeloPrecioCb.setLista();
		view.conectarControlador(this);
		cargarComboBox();
		
	}
	
	public boolean agregar(){
		view.setVisible(true);
		return resultaOperacion;
	}
	
	

	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		view.getModeloPrecioCb().setLista(preciosDao.getTipoPrecios());
		
		
		//se remueve la lista por defecto
		//this.view.getCbxDepart().removeAllItems();
	
		this.view.getCbPrecios().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GUARDAR":
			//Se establece el departamento seleccionado desde la view
			this.setMyPrecio(view.getModeloPrecioCb().getElementAt( view.getCbPrecios().getSelectedIndex()));
			
			//JOptionPane.showMessageDialog(view,this.myPrecio.getDescripcion());
			this.aplicarTodo=view.getChckbxAplicarAToda().isSelected();
			this.resultaOperacion=true;
			view.setVisible(false);
			break;
	
		case "CANCELAR":
			this.resultaOperacion=false;
			view.setVisible(false);
			break;
		
		}
		
	}

	

	private void setMyPrecio(PrecioArticulo selectedItem) {
		// TODO Auto-generated method stub
		this.myPrecio=selectedItem;
		
	}

	private boolean validad() {
		// TODO Auto-generated method stub
		/*
		if(view.getTxtAreaDescripcion().getText().isEmpty()){
			JOptionPane.showMessageDialog(view, "Debe incluir una descripcion de la caja","Error de validacion",JOptionPane.ERROR_MESSAGE);
			return false;
		}*/
		return true;
		
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
		
	}

	/**
	 * @return the aplicarTodo
	 */
	public boolean isAplicarTodo() {
		return aplicarTodo;
	}

	public PrecioArticulo getPrecioSelect() {
		return myPrecio;
	}



}
