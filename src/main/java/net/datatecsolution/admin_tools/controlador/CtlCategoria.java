package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;
import net.datatecsolution.admin_tools.view.ViewCrearCategoria;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlCategoria implements ActionListener {
	
	private ViewCrearCategoria viewMarca;
	private CategoriaDao myMarcaDao;
	private Categoria myMarca;
	
	boolean resultaOperacion=false;
	
	
	public CtlCategoria(ViewCrearCategoria view,CategoriaDao m){
		this.viewMarca=view;
		this.myMarcaDao=m;
		
	}
	public void setMyMarcaDao(CategoriaDao m){
		myMarcaDao=m;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		myMarca=new Categoria();
		myMarca.setDescripcion(viewMarca.getTxtMarca().getText());
		myMarca.setObservacion(viewMarca.getTxtObservacion().getText());
		
		switch (comando){
		
		
			case "GUARDAR":
				
				if(myMarcaDao.registrar(myMarca)){//se ejecuta la accion de guardar
					JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
					myMarca.setId(myMarcaDao.getIdCategoriaRegistrada());//se completa el proveedor guardado con el ID asignado por la BD
					resultaOperacion=true;
					viewMarca.setVisible(false);
					this.viewMarca.dispose(); 
				}
				else{
					JOptionPane.showMessageDialog(null, "No se Registro");
					resultaOperacion=false;
					viewMarca.setVisible(false);
					this.viewMarca.dispose(); 
				}
				
				break;
				
			case "ACTUALIZAR":
				
				if(myMarcaDao.actualizar(this.viewMarca.getMarca())){//ejecuta el metodo actualize en el Dao y espera que devuelva true o false
					JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
					viewMarca.setVisible(false);
					resultaOperacion=true;
				}
				else{
					JOptionPane.showMessageDialog(null, "No se Registro");
					resultaOperacion=false;
				}
				break;
		}
		
	}
	public Categoria getMarca(){
		
		myMarca.setDescripcion(this.viewMarca.getTxtMarca().getText());
		myMarca.setObservacion(this.viewMarca.getTxtObservacion().getText());
		return myMarca;
	}
	public boolean agregarMarca(){
		this.viewMarca.setVisible(true);
		return resultaOperacion;
	}
	
	public boolean actualizarMarca(){
		this.viewMarca.setVisible(true);
		return resultaOperacion;
	}

}
