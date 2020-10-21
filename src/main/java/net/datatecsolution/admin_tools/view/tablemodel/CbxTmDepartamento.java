package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Departamento;

import javax.swing.*;
import java.util.Vector;
public class CbxTmDepartamento extends DefaultComboBoxModel {
	
	private Vector<Departamento> depts=new Vector<Departamento>();

	public CbxTmDepartamento() {
		// TODO Auto-generated constructor stub
		Departamento prueba=new Departamento();
		depts.add(prueba);
	}
	public Departamento getDepartamento(int position){
		return depts.get(position);
	}
	
	@Override
	public int getSize() {
		  return depts.size();
		 }
	
	@Override
	public Object getElementAt(int index) {
		  return depts.get(index);
		 }
	
	public void setLista(Vector<Departamento> im){

		depts.clear();


		//se agregar un selecion por defecto
		Departamento unDept=new Departamento();
		unDept.setId(0);
		unDept.setDescripcion("Seleccione una opcion");

		depts.add(unDept);

		for (Departamento dep:im
			 ) {
			depts.add(dep);

		}
		//depts=im;
	}
	
	public int buscarDepartamento(Departamento m){
		int index=-1;
		
		for(int c=0;c<depts.size();c++){
			
			if(depts.get(c).getId()==m.getId()){
				
				index=c;
			}
		}
		
		return index;
	}

}
