package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class DetalleFactura {
	private Articulo articulo=new Articulo();
    private BigDecimal cantidad=new BigDecimal(1);
	private BigDecimal impuesto=new BigDecimal(0.0);
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal subTotal=new BigDecimal(0.0);
	private BigDecimal descuentoItem=new BigDecimal(0.0);
	private double descuento=0;
	private int idFactura=1;
	private boolean accion=false;
	
	//para el reporte detalle
	private int id=0;
	private int codigoArt=0;
	private String art="";
	private double precioVentaItem=0;
	private double totalVentasCosto=0.0;
	private double ganancia=0.0;

	
	public void setAccion(boolean d){
		accion=d;
	}
	public boolean getAccion(){
		return accion;
	}
	
	public BigDecimal getDescuentoItem(){
		return descuentoItem;
	}
	public void setDescuentoItem(BigDecimal d){
		descuentoItem=d;
	}
	
	public BigDecimal getSubTotal(){
		return subTotal;
	}
	public void setSubTotal(BigDecimal s){
		subTotal=s;
	}
	public void setDescuento(double d){
		descuento=d;
	}
	public double getDescuento(){
		return descuento;
	}
	public void setIdFactura(int id){
		idFactura=id;
	}
	public int getIdFactura(){
		return idFactura;
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
	
	public void setTotal(BigDecimal t){
		total=t;
	}
	public BigDecimal getTotal(){
		return total;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	public int getCodigoArt() {
		return codigoArt;
	}
	public void
	setCodigoArt(int codigoArt) {
		this.codigoArt = codigoArt;
	}
	public String getArt() {
		return art;
	}
	public void setArt(String art) {
		this.art = art;
	}
	public double getPrecioVentaItem() {
		return precioVentaItem;
	}
	public void setPrecioVentaItem(double precioVentaItem) {
		this.precioVentaItem = precioVentaItem;
	}
	public double getTotalVentasCosto() {
		return totalVentasCosto;
	}
	public void setTotalVentasCosto(double totalVentasCosto) {
		this.totalVentasCosto = totalVentasCosto;
	}
	public double getGanancia() {
		return ganancia;
	}
	public void setGanancia(double ganancia) {
		this.ganancia = ganancia;
	}

}
