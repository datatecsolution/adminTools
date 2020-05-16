package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Banco;

import javax.swing.*;
import java.util.Vector;



public class CbxTmCuentasBancos extends DefaultComboBoxModel<Banco>{
	
	private Vector<Banco> cuentas=new Vector<Banco>();

	@Override
	public int getSize() {
		  return cuentas.size();
		 }
	
	@Override
	public Banco getElementAt(int index) {
		  return cuentas.get(index);
		 }
	
	public void setLista(Vector<Banco> im){
		cuentas=im;
	}
	public void addEmpleado(Banco m){
		cuentas.addElement(m);
		//this.f
	

	}
	
	public Banco getBanco(int index){
		return cuentas.get(index);
	}

	
	public CbxTmCuentasBancos(){
		
	}
	
	public int buscarImpuesto(Banco m){
		int index=-1;
		
		for(int c=0;c<cuentas.size();c++){
			
			if(cuentas.get(c).getId()==m.getId()){
				
				index=c;
			}
		}
		
		return index;
	}

}
