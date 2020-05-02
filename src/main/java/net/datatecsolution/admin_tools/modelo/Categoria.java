package net.datatecsolution.admin_tools.modelo;

public class Categoria {
	private int id;
	private String descripcion;
	private String observacion;
	
	public Categoria(){
		
	}
	public void setId(int i){
		id=i;
	}
	public int getId(){
		return id;
	}
	public void setDescripcion(String m){
		descripcion=m;
	}
	public String getDescripcion(){
		return descripcion;
	}
	public void setObservacion(String o){
		observacion=o;
	}
	public String getObservacion(){
		return observacion;
	}
	
	@Override
	public String toString(){
		return getDescripcion();//"Codigo Marca: "+getId()+", Marca: "+getMarca()+", Observacion: "+getObservacion();
	}

}
