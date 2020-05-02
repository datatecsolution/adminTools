package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class EntradaCaja {
	
	private int codigoEntrada=-1;
	private String concepto="";
	private BigDecimal cantidad= new BigDecimal(0.00); 
	private String fecha="";
	private String usuario="";
	private String estado="";
	private Banco banco=new Banco();
	
	private int codigoCuenta=-1;
	
	
	public void setCodigoEntrada(int c){
		codigoEntrada=c;
	}
	public int getCodigoEntrada(){
		return codigoEntrada;
	}
	
	public void setConcepto(String c){
		concepto=c;
	}
	public String getConcepto(){
		return concepto;
	}
	
	public void setCantidad(BigDecimal c){
		cantidad=c;
	}
	public BigDecimal getCantidad(){
		return cantidad;
	}
	
	public void setFecha(String f){
		fecha=f;
	}
	public String getFecha(){
		return fecha;
	}
	
	public void setUsuario(String u){
		usuario=u;
	}
	public String getUsuario(){
		return usuario;
	}
	
	@Override
	public String toString(){
		return "Codigo="
				+ this.codigoEntrada+", Concepto="
						+ this.concepto+", Cantidad="
								+ this.cantidad+", Fecha="
										+ this.fecha+", Usuario="
												+ this.usuario;
	}

	public EntradaCaja() {
		// TODO Auto-generated constructor stub
	}
	
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public int getCodigoCuenta() {
		return codigoCuenta;
	}
	public void setCodigoCuenta(int codigoCuenta) {
		this.codigoCuenta = codigoCuenta;
	}
	public Banco getBanco() {
		return banco;
	}
	public void setBanco(Banco cuentaBanco) {
		this.banco = cuentaBanco;
	}

}
