package net.datatecsolution.admin_tools.view.tablemodel;


import net.datatecsolution.admin_tools.modelo.Impuesto;

import javax.swing.*;
import java.util.Vector;

public class ComboBoxImpuesto extends DefaultComboBoxModel {
	
	private Vector<Impuesto> impuestos=new Vector<Impuesto>();
	
	public void agregar(Impuesto i){
		impuestos.add(i);
	}

	@Override
	public int getSize() {
		  return impuestos.size();
		 }
	
	@Override
	public Object getElementAt(int index) {
		  return impuestos.get(index);
		 }
	
	public void setLista(Vector<Impuesto> im){
		impuestos=im;
	}
	
	public ComboBoxImpuesto(){
		
	}
	
	public int buscarImpuesto(Impuesto m){
		int index=-1;
		
		for(int c=0;c<impuestos.size();c++){
			
			if(impuestos.get(c).getId()==m.getId()){
				
				index=c;
			}
		}
		
		return index;
	}
	
	
	
	
	
}
