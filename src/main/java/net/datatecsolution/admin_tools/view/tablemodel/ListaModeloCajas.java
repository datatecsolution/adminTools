package net.datatecsolution.admin_tools.view.tablemodel;

import java.util.ArrayList;
import java.util.List;









import javax.swing.AbstractListModel;
import net.datatecsolution.admin_tools.modelo.Caja;




public class ListaModeloCajas extends AbstractListModel {
	
private List<Caja> cajas=new ArrayList<Caja>();


@Override
public int getSize() {
	// TODO Auto-generated method stub
	return cajas.size();
	
	//codsBarras.set(index, element)
}

@Override
public Object getElementAt(int index) {
	// TODO Auto-generated method stub
	Caja c=cajas.get(index);
	return c.toString();
}

public void setCaja(int index,Caja c){
	cajas.set(index, c);
}
public void setCajaDefault(int index){
	
	for(int x=0;x<cajas.size();x++){
		if(x==index){
			cajas.get(x).setActiva(true);
		}else{
			cajas.get(x).setActiva(false);
		}
		this.fireContentsChanged(cajas, x, x);
			
	}
	
	
}

public void setCajas(List<Caja> cs){
	cajas=cs;
}
public void addCaja(Caja c){
	cajas.add(c);
	this.fireIntervalAdded(this, getSize(), getSize()+1);
}

	
public void eliminarCaja(int index0){
	
	cajas.remove(index0);
	this.fireIntervalRemoved(index0, getSize(), getSize()+1);
}

public Caja getCaja(int index){
	  return cajas.get(index);
	}

public List<Caja> getCajas(){
	return cajas;
}

public boolean verificarDuplicado(Caja myCaja) {
	
	boolean resul=false;
	for(int x=0;x<cajas.size();x++){
		if(cajas.get(x).getCodigo()==myCaja.getCodigo())
			resul=true;
	}
	return resul;
}



}
