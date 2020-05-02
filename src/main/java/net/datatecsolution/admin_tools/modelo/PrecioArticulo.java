package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class PrecioArticulo {
	
	private int codigoPrecio=-1;
	private String descripcion="";
	private BigDecimal precio=new BigDecimal(-1.00);
	private int codigoArticulo=-1;
	

	public PrecioArticulo() {
		// TODO Auto-generated constructor stub
	}
	
	public void setCodigoPrecio(int c){
		codigoPrecio=c;
	}
	public int getCodigoPrecio(){
		return codigoPrecio;
	}
	
	public void setDecripcion(String d){
		descripcion=d;
	}
	public String getDescripcion(){
		return descripcion;
	}
	
	public void setPrecio(BigDecimal p){
		precio=p;
	}
	public BigDecimal getPrecio(){
		return precio;
	}
	
	public void setCodigoArticulo(int codArticulo){
		codigoArticulo=codArticulo;
	}
	public int getCodigoArticulo(){
		return codigoArticulo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return  descripcion;
	}

}
