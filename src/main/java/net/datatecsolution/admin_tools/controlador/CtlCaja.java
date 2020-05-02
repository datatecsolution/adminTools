package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DepartamentoDao;
import net.datatecsolution.admin_tools.view.ViewCrearCaja;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CtlCaja implements ActionListener, KeyListener {
	
	private ViewCrearCaja view;
	private CajaDao myDao;
	private Caja myCaja=new Caja();
	private DepartamentoDao deptDao=null;
	boolean resultaOperacion=false;
	
	public CtlCaja(ViewCrearCaja v){
		view=v;
		myDao=new CajaDao();
		deptDao=new DepartamentoDao();
		view.conectarControlador(this);
		cargarComboBox();
		
	}
	
	public boolean agregar(){
		view.setVisible(true);
		return resultaOperacion;
	}
	public boolean actualizar(Caja c){
		myCaja=c;
		view.getBtnGuardar().setVisible(false);
		view.getBtnActualizar().setVisible(true);
		cargarDatosView();
		view.setVisible(true);
		return resultaOperacion;
	}
	
	private void cargarDatosView() {
		// TODO Auto-generated method stub
		view.getTxtAreaDescripcion().setText(this.myCaja.getDescripcion());
		
		view.getCbxDepart().setSelectedIndex(view.getModeloCbx().buscarImpuesto(myCaja.getDetartamento()));
	}

	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloCbx().setLista(this.deptDao.todos());
		
		
		//se remueve la lista por defecto
		this.view.getCbxDepart().removeAllItems();
	
		this.view.getCbxDepart().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GUARDAR":
				if(validad()){
					rellenarModelo();
					boolean resul=myDao.registrar(myCaja);
					if(resul){
						JOptionPane.showMessageDialog(view, "Se registro la caja","Exito",JOptionPane.INFORMATION_MESSAGE);
						
						resultaOperacion=true;	
						view.setVisible(false);
					}else{
						resultaOperacion=false;
					}
				}
			break;
		case "ACTUALIZAR":
			if(validad()){
				rellenarModelo();
				
				boolean resul=myDao.actualizar(myCaja);
				
				if(resul){
					JOptionPane.showMessageDialog(view, "Se actualizo la caja","Exito",JOptionPane.INFORMATION_MESSAGE);
					
					resultaOperacion=true;	
					view.setVisible(false);
				}else{
					resultaOperacion=false;
				}
			}
			
			break;
		case "CANCELAR":
			view.setVisible(false);
			break;
		
		}
		
	}

	private void rellenarModelo() {
		// TODO Auto-generated method stub
		
		
		//se establece la descripcion desde la view
		myCaja.setDescripcion(view.getTxtAreaDescripcion().getText());
		
		
		//Se establece el departamento seleccionado desde la view
		Departamento depart= (Departamento) this.view.getCbxDepart().getSelectedItem();
		myCaja.setDetartamento(depart);
		
		myCaja.setCodigoBodega(depart.getId());
		

		
	}

	private boolean validad() {
		// TODO Auto-generated method stub
		
		if(view.getTxtAreaDescripcion().getText().isEmpty()){
			JOptionPane.showMessageDialog(view, "Debe incluir una descripcion de la caja","Error de validacion",JOptionPane.ERROR_MESSAGE);
			return false;
		}
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

}
