package net.datatecsolution.admin_tools.controlador;


import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.modelo.dao.RutaCobroDao;
import net.datatecsolution.admin_tools.view.ViewCrearRutaCobro;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlRutaCobro implements ActionListener {

	private ViewCrearRutaCobro view;
	private RutaCobroDao myRutaDao;
	private RutaCobro myRuta=new RutaCobro();

	boolean resultaOperacion=false;


	public CtlRutaCobro(ViewCrearRutaCobro v){
		this.view =v;
		this.myRutaDao =new RutaCobroDao();
		view.conectarControlador(this);
		
	}
	public void setMyRutaDao(RutaCobroDao m){
		myRutaDao =m;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		

		
		switch (comando){
		
		
			case "GUARDAR":

				if(validacion()) {
					setRuta();

					if (myRutaDao.registrar(myRuta)) {//se ejecuta la accion de guardar
						JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente", "Informacion", JOptionPane.INFORMATION_MESSAGE);
						myRuta.setCodigo(myRutaDao.getIdRegistrado());//se completa el proveedor guardado con el ID asignado por la BD
						resultaOperacion = true;
						view.setVisible(false);
						this.view.dispose();
					} else {
						JOptionPane.showMessageDialog(null, "No se Registro");
						resultaOperacion = false;
						view.setVisible(false);
						this.view.dispose();
					}
				}
				
				break;
				
			case "ACTUALIZAR":

				if(validacion()) {

					setRuta();

					if (myRutaDao.actualizar(myRuta)) {//ejecuta el metodo actualize en el Dao y espera que devuelva true o false
						JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente", "Informacion", JOptionPane.INFORMATION_MESSAGE);
						view.setVisible(false);
						resultaOperacion = true;
					} else {
						JOptionPane.showMessageDialog(null, "No se Registro");
						resultaOperacion = false;
					}
				}
				break;
		}
		
	}

	private void setRuta() {
		myRuta.setDescripcion(view.getTxtDescripcion().getText());
		myRuta.setObser(view.getTxtObservacion().getText());
	}

	private boolean validacion() {
		boolean resul=false;
		if(view.getTxtDescripcion().getText().trim().length()==0){
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getTxtDescripcion().requestFocusInWindow();
		}else
			resul=true;
		return resul;
	}

	public RutaCobro getRuta(){
		return myRuta;
	}
	public boolean agregarRuta(){
		this.view.setVisible(true);
		return resultaOperacion;
	}

	private void cargarDatos(){


		view.getTxtObservacion().setText(myRuta.getObser());
		view.getTxtDescripcion().setText(myRuta.getDescripcion());

	}
	
	public boolean actualizarRuta(RutaCobro r){
		myRuta =r;
		cargarDatos();
		view.getBtnGuardar().setVisible(false);
		view.getBtnActualizar().setVisible(true);

		this.view.setVisible(true);
		return resultaOperacion;
	}

}
