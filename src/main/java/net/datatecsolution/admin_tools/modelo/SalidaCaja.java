package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class SalidaCaja {
	
	private int codigoSalida=-1;
	private String concepto="";
	private BigDecimal cantidad= new BigDecimal(0.00); 
	private String fecha="";
	private String usuario="";
	private Empleado empleado=new Empleado();
	private String estado="";
	private Banco banco=new Banco();
	
	private int codigoCuenta=-1;
	
	
	public void setCodigoSalida(int c){
		codigoSalida=c;
	}
	public int getCodigoSalida(){
		return codigoSalida;
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
				+ this.codigoSalida+", Concepto="
						+ this.concepto+", Cantidad="
								+ this.cantidad+", Fecha="
										+ this.fecha+", Usuario="
												+ this.usuario;
	}

	public SalidaCaja() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the empleado
	 */
	public Empleado getEmpleado() {
		return empleado;
	}
	/**
	 * @param empleado the empleado to set
	 */
	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
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
	public void setBanco(Banco banco) {
		this.banco = banco;
	}

}
