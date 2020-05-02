package net.datatecsolution.admin_tools.modelo;

public class Impuesto {
	private int  id;
	private String  porcentaje;
	
	public Impuesto(){
		
	}
	
	public Impuesto(int  i,String  p){
		id=i;
		porcentaje=p;
	}
	
	public void setId(int  i){
		id=i;
	}
	public int  getId(){
		return id;
	}
	
	public void setPorcentaje(String  p){
		porcentaje=p;
	}
	public String  getPorcentaje(){
		return porcentaje;
	}
	
	@Override
	public String toString(){
		return porcentaje;
	}

}
