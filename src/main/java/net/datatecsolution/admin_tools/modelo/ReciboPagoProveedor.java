package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReciboPagoProveedor {
	
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal saldo=new BigDecimal(0.0);
	private BigDecimal saldoAnterior=new BigDecimal(0.0);
	private Proveedor proveedor=null;
	private String concepto="";
	private String totalLetras="";
	private String fecha; 
	private int noRecibo=0;
	private List<Factura> facturas=new ArrayList<Factura>();
	private Banco formaPago=new Banco();
	

	public ReciboPagoProveedor() {
		// TODO Auto-generated constructor stub
		proveedor=new Proveedor();
	}
	public List<Factura> getFacturas(){
		return facturas;
	}
	public void setFacturas(List<Factura> d){
		facturas=d;
	}
	public void setNoRecibo(int n){
		noRecibo=n;
	}
	public int getNoRecibo(){
		return noRecibo;
	}
	public void setFecha(String f){
		fecha=f;
	}
	public String getFecha(){
		return fecha;
	}
	public void setTotalLetras(String t){
		totalLetras=t;
	}
	public String getTotalLetras(){
		return totalLetras;
	}
	public void setConcepto(String c){
		concepto=c;
	}
	public String getConcepto(){
		if(concepto.length()>250)
			return StringUtils.abbreviate(concepto,250);
		else
			return concepto;
	}
	
	public void setTotal(BigDecimal t){
		total=total.add(t);
	}
	public BigDecimal getTotal(){
		return total;
	}
	
	public void setSaldo(BigDecimal t){
		saldo=saldo.add(t);
	}
	public BigDecimal getSaldo(){
		return saldo;
	}
	public void setSaldoAnterior(BigDecimal t){
		saldoAnterior=saldoAnterior.add(t);
	}
	public BigDecimal getSaldoAnterior(){
		return saldoAnterior;
	}
	
	public void resetTotales(){
		
		total=BigDecimal.ZERO;
		saldo=BigDecimal.ZERO;
		saldoAnterior=BigDecimal.ZERO;
	}
	public void setSaldos0(){
		saldo=BigDecimal.ZERO;
		saldoAnterior=BigDecimal.ZERO;
	}
	public Proveedor getProveedor() {
		// TODO Auto-generated method stub
		return proveedor;
	}
	/**
	 * @param proveedor the proveedor to set
	 */
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	/**
	 * @return the formaPago
	 */
	public Banco getFormaPago() {
		return formaPago;
	}
	/**
	 * @param formaPago the formaPago to set
	 */
	public void setFormaPago(Banco formaPago) {
		this.formaPago = formaPago;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReciboPagoProveedor [total=" + total + ", saldo=" + saldo + ", saldoAnterior=" + saldoAnterior
				+ ", proveedor=" + proveedor + ", concepto=" + concepto + ", totalLetras=" + totalLetras + ", fecha="
				+ fecha + ", noRecibo=" + noRecibo + ", facturas=" + facturas + ", formaPago=" + formaPago + "]";
	}

}
