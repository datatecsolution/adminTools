package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Caja;

import javax.swing.*;
import java.util.Vector;
public class CbxTmCajas extends DefaultComboBoxModel<Caja> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6125209848283370605L;
	private Vector<Caja> cajas=new Vector<Caja>();

	public CbxTmCajas() {
		// TODO Auto-generated constructor stub
		Caja una=new Caja();
		cajas.add(una);
	}
	public Caja getCaja(int position){
		return cajas.get(position);
	}
	public void agregar(Caja c){
		cajas.addElement(c);
	}
	
	@Override
	public int getSize() {
		  return cajas.size();
		 }
	
	@Override
	public Caja getElementAt(int index) {
		  return cajas.get(index);
		 }
	
	public void setLista(Vector<Caja> im){
		cajas=im;
	}
	
	public int buscarCaja(Caja m){
		int index=-1;
		
		for(int c=0;c<cajas.size();c++){
			
			if(cajas.get(c).getCodigo()==m.getCodigo()){
				
				index=c;
			}
		}
		
		return index;
	}

}
