package net.datatecsolution.admin_tools.modelo;

public class CodBarra {
	private int codArticulo;
	private String codigoBarra;
	
	public CodBarra(){
		
	}
	public CodBarra(int c,String cb){
		codArticulo=c;
		codigoBarra=cb;
	}
	
	public void setCodArticulo(int c){
		codArticulo=c;
	}
	
	public int getCodArticulo(){
		return codArticulo;
	}
	
	public void setCodigoBarra(String cb){
		codigoBarra=cb;
	}
	
	public String getCodigoBarra(){
		return codigoBarra;
	}
	
	@Override
	public String toString(){
		return codigoBarra;
	}

}
