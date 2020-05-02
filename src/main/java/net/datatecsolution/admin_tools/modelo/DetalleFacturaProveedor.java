package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;




public class DetalleFacturaProveedor {
	private Articulo articulo;
	private BigDecimal cantidad=new BigDecimal(1);
	private BigDecimal impuesto=new BigDecimal(0.0);
	private BigDecimal subTotal=new BigDecimal(0.0);
	private BigDecimal precioCompra=new BigDecimal(0.0);
	private BigDecimal total=new BigDecimal(0.0);
	private int codigoBodega=-1;
	
	private Departamento departamentoOrigen=new Departamento();
	private Departamento departamentoDestino=new Departamento();
	
	private boolean ivaIncludo=true;
	
	

	
	public DetalleFacturaProveedor(){
		articulo=new Articulo();
	}
	public DetalleFacturaProveedor(Articulo a,double c,double i,double t){
		
	}
	
	public void setPrecioCompra(BigDecimal p){
		precioCompra=p;
	}
	public BigDecimal getPrecioCompra(){
		return precioCompra;
	}
	public void setListArticulos(Articulo a){
		articulo=a;
	}
	public Articulo getArticulo(){
		return articulo;
	}
	
	public void setCantidad(BigDecimal c){
		cantidad=c;
	}
	public BigDecimal getCantidad(){
		return cantidad;
	}
	
	public void setImpuesto(BigDecimal i){
		impuesto=i;
	}
	public BigDecimal getImpuesto(){
		return impuesto;
	}
	
	public void setSubTotal(BigDecimal t){
		subTotal=t;
	}
	public BigDecimal getSubTotal(){
		return subTotal;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public Departamento getDepartamentoOrigen() {
		return departamentoOrigen;
	}
	public void setDepartamentoOrigen(Departamento departamentoOrigen) {
		this.departamentoOrigen = departamentoOrigen;
	}
	public Departamento getDepartamentoDestino() {
		return departamentoDestino;
	}
	public void setDepartamentoDestino(Departamento departamentoDestino) {
		this.departamentoDestino = departamentoDestino;
	}
	/**
	 * @return the ivaIncludo
	 */
	public boolean isIvaIncludo() {
		return ivaIncludo;
	}
	/**
	 * @param ivaIncludo the ivaIncludo to set
	 */
	public void setIvaIncludo(boolean ivaIncludo) {
		this.ivaIncludo = ivaIncludo;
	}
	public int getCodigoBodega() {
		return codigoBodega;
	}
	public void setCodigoBodega(int codigoBodega) {
		this.codigoBodega = codigoBodega;
	}
}
