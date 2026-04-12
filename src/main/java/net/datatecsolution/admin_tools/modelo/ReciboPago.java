package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReciboPago {
	
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal saldo=new BigDecimal(0.0);
	private BigDecimal saldoAnterior=new BigDecimal(0.0);
	private Cliente myCliente=null;
	private String concepto="";
	private String totalLetras="";
	private String fecha=null;
	private int noRecibo=0;

	private String ref="NA";
	private List<Factura> facturas=new ArrayList<Factura>();
	

	public ReciboPago() {
		// TODO Auto-generated constructor stub
		myCliente=new Cliente();
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
	public void setCliente(Cliente c){
		myCliente=c;
	}
	public Cliente getCliente(){
		return myCliente;
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
		return null;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}


}
