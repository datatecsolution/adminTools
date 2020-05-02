package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class CuentaBanco {
	
	private int noReguistro=-1;
	private String fecha="";
	private Banco banco;
	private String descripcion="";
	private String referencia="";
	private BigDecimal saldo=new BigDecimal(0.0);
	private BigDecimal credito=new BigDecimal(0.0);
	private BigDecimal debito=new BigDecimal(0.0);

	public CuentaBanco() {
		// TODO Auto-generated constructor stub
	}
	public void resetTotales(){
		saldo=BigDecimal.ZERO;
		credito=BigDecimal.ZERO;
		debito=BigDecimal.ZERO;
	}
	
	public void setCredito(BigDecimal t){
		credito=credito.add(t);
	}
	public BigDecimal getCredito(){
		return credito;
	}
	
	public void setDebito(BigDecimal t){
		debito=debito.add(t);
	}
	public BigDecimal getDebito(){
		return debito;
	}
	
	public void setSaldo(BigDecimal t){
		saldo=saldo.add(t);
	}
	public BigDecimal getSaldo(){
		return saldo;
	}
	
	public void setDescripcion(String d){
		descripcion=d;
	}
	public String getDescripcion(){
		return descripcion;
	}
	public void setNoReguistro(int n){
		noReguistro=n;
	}
	public int getNoReguistro(){
		return noReguistro;
	}
	public void setFecha(String f){
		fecha=f;
	}
	public String getFecha(){
		return fecha;
	}
	/**
	 * @return the proveedor
	 */
	public Banco getBanco() {
		return banco;
	}
	/**
	 * @param proveedor the proveedor to set
	 */
	public void setBanco(Banco b) {
		this.banco = b;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CuentaBanco [noReguistro=" + noReguistro + ", fecha=" + fecha + ", banco=" + banco
				+ ", descripcion=" + descripcion + ", saldo=" + saldo + ", credito=" + credito + ", debito=" + debito
				+ "]";
	}
	public String getReferencia() {
		return referencia;
	}
	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

}
