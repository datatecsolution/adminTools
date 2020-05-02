package net.datatecsolution.admin_tools.modelo;

public class Inventario {
	private Departamento bodega=new Departamento();
    private Articulo articulo=new Articulo();
    private double existencia=0.0;
	//private double precioVenta=0.0;
	
	public Inventario(){
	}
	
	public void setBodega(Departamento b){
		bodega=b;
	}
	public Departamento getBodega(){
		return bodega;
	}
	
	public void setExistencia(double e){
		existencia=e;
	}
	public double getExistencia(){
		return existencia;
	}
	
	/*public void setPrecioVenta(double p){
		precioVenta=p;
	}
	public double getPrecioVenta(){
		return precioVenta;
	}*/
	public Articulo getArticulo(){
		return articulo;
	}
	public void setArticulo(Articulo a){
		articulo=a;
	}
	public void incremetarExistencia(double i){
		existencia+=i;
	}
	public void decrementarExistencia(double d){
		existencia-=d;
	}
	
	@Override
	public String toString(){
		return "Articulo["+articulo+"], Bodega["+bodega+"], Existencia:"+existencia;
	}
}
