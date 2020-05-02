package net.datatecsolution.admin_tools.view.tablemodel;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import net.datatecsolution.admin_tools.modelo.Categoria;
public class CbxTmCategoria extends DefaultComboBoxModel<Categoria> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	private Vector<Categoria> categorias=new Vector<Categoria>();

	public CbxTmCategoria() {
		// TODO Auto-generated constructor stub
		Categoria una=new Categoria();
		categorias.add(una);
	}
	public Categoria getCategoria(int position){
		return categorias.get(position);
	}
	public void agregar(Categoria c){
		categorias.addElement(c);
	}
	
	@Override
	public int getSize() {
		  return categorias.size();
		 }
	
	@Override
	public Categoria getElementAt(int index) {
		  return categorias.get(index);
		 }
	
	public void setLista(Vector<Categoria> im){
		categorias=im;
	}
	
	public int buscarCategoria(Categoria m){
		int index=-1;
		
		for(int c=0;c<categorias.size();c++){
			
			if(categorias.get(c).getId()==m.getId()){
				
				index=c;
			}
		}
		
		return index;
	}

}
