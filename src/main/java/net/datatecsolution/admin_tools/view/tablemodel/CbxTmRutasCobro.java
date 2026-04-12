package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.RutaCobro;

import javax.swing.*;
import java.util.Vector;


public class CbxTmRutasCobro extends DefaultComboBoxModel{

	private Vector<RutaCobro> rutaCobros =new Vector<RutaCobro>();

	public void agregar(RutaCobro c){
		rutaCobros.addElement(c);
	}

	public RutaCobro getRuta(int position){
		return rutaCobros.get(position);
	}

	@Override
	public int getSize() {
		  return rutaCobros.size();
		 }

	@Override
	public Object getElementAt(int index) {
		  return rutaCobros.get(index);
		 }

	public void setLista(Vector<RutaCobro> im){

		rutaCobros.clear();

		//se agregar un selecion por defecto
		RutaCobro unaRuta=new RutaCobro();
		unaRuta.setCodigo(0);
		unaRuta.setDescripcion("Todos");


		rutaCobros.add(unaRuta);

		for (RutaCobro emp:im
		) {
			rutaCobros.add(emp);

		}
	}
	public void addRuta(RutaCobro m){
		rutaCobros.addElement(m);
		//this.f

	}

	public CbxTmRutasCobro(){
		
	}
	
	public int buscarRuta(RutaCobro m){
		int index=-1;
		
		for(int c = 0; c< rutaCobros.size(); c++){
			
			if(rutaCobros.get(c).getCodigo()==m.getCodigo()){
				
				index=c;
			}
		}
		
		return index;
	}

}
