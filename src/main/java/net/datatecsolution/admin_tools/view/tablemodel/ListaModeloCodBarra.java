package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.CodBarra;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

//import javax.swing.DefaultComboBoxModel;




public class ListaModeloCodBarra extends AbstractListModel {
	
private List<CodBarra> codsBarras=new ArrayList<CodBarra>();

private List<CodBarra> codsBarrasElimniar=new ArrayList<CodBarra>();

@Override
public int getSize() {
	// TODO Auto-generated method stub
	return codsBarras.size();
	
	//codsBarras.set(index, element)
}

@Override
public Object getElementAt(int index) {
	// TODO Auto-generated method stub
	CodBarra cod=codsBarras.get(index);
	return cod.getCodigoBarra();
}

public void setCodBarra(int index,CodBarra cods){
	codsBarras.set(index, cods);
}

public void setCodsBarra(List<CodBarra> cods){
	codsBarras=cods;
}
public void addCodBarra(CodBarra cod){
	codsBarras.add(cod);
	this.fireIntervalAdded(this, getSize(), getSize()+1);
}

	
public void eliminarCodBarra(int index0){
	codsBarrasElimniar.add(codsBarras.get(index0));
	codsBarras.remove(index0);
	this.fireIntervalRemoved(index0, getSize(), getSize()+1);
}

public List<CodBarra> getCodsElimnar(){
	return codsBarrasElimniar;
}
public CodBarra getCodBarra(int index){
	  return codsBarras.get(index);
	}

public List<CodBarra> getCodsBarras(){
	return codsBarras;
}



}
