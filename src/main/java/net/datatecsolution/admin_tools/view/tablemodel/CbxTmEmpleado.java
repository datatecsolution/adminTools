package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.Empleado;

import javax.swing.*;
import java.util.Vector;


public class CbxTmEmpleado extends DefaultComboBoxModel{
	
	private Vector<Empleado> empleados=new Vector<Empleado>();
	
	public void agregar(Empleado c){
		empleados.addElement(c);
	}
	
	public Empleado getEmpleado(int position){
		return empleados.get(position);
	}

	@Override
	public int getSize() {
		  return empleados.size();
		 }
	
	@Override
	public Object getElementAt(int index) {
		  return empleados.get(index);
		 }
	
	public void setLista(Vector<Empleado> im){

		empleados.clear();

		//se agregar un selecion por defecto
		Empleado unEmpleado=new Empleado();
		unEmpleado.setCodigo(0);
		unEmpleado.setNombre("Todos");


		empleados.add(unEmpleado);

		for (Empleado emp:im
		) {
			empleados.add(emp);

		}
	}
	public void addEmpleado(Empleado m){
		empleados.addElement(m);
		//this.f

	}
	
	public CbxTmEmpleado(){
		
	}
	
	public int buscarEmpleado(Empleado m){
		int index=-1;
		
		for(int c=0;c<empleados.size();c++){
			
			if(empleados.get(c).getCodigo()==m.getCodigo()){
				
				index=c;
			}
		}
		
		return index;
	}

}
