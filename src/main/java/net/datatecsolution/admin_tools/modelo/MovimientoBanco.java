package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class MovimientoBanco {
	
	private int codigo=-1;
	private String descripcion="";
	private BigDecimal cantidad= new BigDecimal(0.00); 
	private String fecha="";
	private String usuario="";
	
	private Banco banco=new Banco();
	
	private int codigoCuenta=-1;
	private int codigoTipoMovimiento=-1;
	
	
	
	public void setCodigo(int c){
		codigo=c;
	}
	public int getCodigo(){
		return codigo;
	}
	
	public void setDescripcion(String c){
		descripcion=c;
	}
	public String getDescripcion(){
		return descripcion;
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
				+ this.codigo+", Concepto="
						+ this.descripcion+", Cantidad="
								+ this.cantidad+", Fecha="
										+ this.fecha+", Usuario="
												+ this.usuario;
	}

	public MovimientoBanco() {
		// TODO Auto-generated constructor stub
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
	public int getCodigoTipoMovimiento() {
		return codigoTipoMovimiento;
	}
	public void setCodigoTipoMovimiento(int codigoTipoMovimiento) {
		this.codigoTipoMovimiento = codigoTipoMovimiento;
	}

}
