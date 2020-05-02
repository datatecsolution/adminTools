package net.datatecsolution.admin_tools.modelo;



public class VentasCategoria {
	private int codigo=0;
	private int codigoCategoria=0;
	private String categoria="";
	private double totalVentas=0.0;
	
	/**
	 * @return the codigo
	 */
	public int getCodigo() {
		return codigo;
	}
	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	
	
	/**
	 * @return the totalVentas
	 */
	public double getTotalVentas() {
		return totalVentas;
	}
	/**
	 * @param totalVentas the totalVentas to set
	 */
	public void setTotalVentas(double t) {
		this.totalVentas = totalVentas + t;
	}
	
	public void resetTotales(){
		
		this.totalVentas=0;

	}
	
	
	
	public int getCodigoCategoria() {
		return codigoCategoria;
	}
	public void setCodigoCategoria(int codigoCategoria) {
		this.codigoCategoria = codigoCategoria;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String c) {
		categoria = c;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VentasCategoria [codigo=" + codigo + ", codigoCategoria=" + codigoCategoria + ", Categoria=" + categoria
				+ ", totalVentas=" + totalVentas + "]";
	}
	

}
