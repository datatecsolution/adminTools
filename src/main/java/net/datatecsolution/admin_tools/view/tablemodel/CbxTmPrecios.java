package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.*;
import java.util.List;
import java.util.Vector;
public class CbxTmPrecios extends DefaultComboBoxModel<PrecioArticulo> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	private Vector<PrecioArticulo> precios=new Vector<PrecioArticulo>();

	public CbxTmPrecios() {
		// TODO Auto-generated constructor stub
		PrecioArticulo una=new PrecioArticulo();
		precios.add(una);
	}
	public PrecioArticulo getPrecio(int position){
		return precios.get(position);
	}
	public void agregar(PrecioArticulo c){
		precios.addElement(c);
	}
	
	@Override
	public int getSize() {
		  return precios.size();
		 }
	
	@Override
	public PrecioArticulo getElementAt(int index) {
		  return precios.get(index);
		 }
	
	public void setLista(Vector<PrecioArticulo> im){
		precios=im;
	}
	public void setLista(List<PrecioArticulo> im){
		
		if(im!=null){
			this.precios.clear();
			for(int x=0;x<im.size();x++){
				
				
				precios.add(im.get(x));
				
			}
		}
		
	}
	
	public int buscarPrecio(PrecioArticulo m){
		int index=-1;
		
		for(int c=0;c<precios.size();c++){
			
			if(precios.get(c).getCodigoPrecio()==m.getCodigoPrecio()){
				
				index=c;
			}
		}
		
		return index;
	}

}
