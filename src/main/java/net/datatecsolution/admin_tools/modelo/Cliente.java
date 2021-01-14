package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class Cliente {
	
	private int id=-1;
	private String nombre="NA";
	private String direccion="NA";
	private String telefono="NA";
	private String celular="NA";
	private String rtn="CF";
	private BigDecimal limiteCredito=new BigDecimal(0.0);
	private BigDecimal saldoCuenta=new BigDecimal(0.0);
	private Integer tipoCliente=0;
	private Empleado vendedor=new Empleado();
	private int idVendedor=-1;
	private RutaCobro rutaCobro=new RutaCobro();
	private int idRutaCobro=-1;
	
	public Cliente(){
		
	}
	public Cliente(String n,String d){
		nombre=n;
		direccion=d;
		
	}
	
	
	public void setLimiteCredito(BigDecimal t){
		limiteCredito=limiteCredito.add(t);
	}
	public BigDecimal getLimiteCredito(){
		return limiteCredito;
	}
	public void setSaldoCuenta(BigDecimal t){
		saldoCuenta=saldoCuenta.add(t);
	}
	public BigDecimal getSaldoCuenta(){
		return saldoCuenta;
	}
	
	public void resetTotales(){
		
		limiteCredito=BigDecimal.ZERO;
		saldoCuenta=BigDecimal.ZERO;
	}
	
	public String getRtn(){
		return rtn;
	}
	public void setRtn(String r){
		rtn=r;
	}
	public String getCelular(){
		return celular;
	}
	public void setCelular(String c){
		celular=c;
	}
	public String getTelefono(){
		return telefono;
	}
	public void setTelefono(String t){
		telefono=t;
	}
	
	public String getDereccion(){
		return direccion;
	}
	public void setDireccion(String d){
		direccion=d;
	}
	public String getNombre(){
		return nombre;
	}
	public void setNombre(String n){
		nombre=n;
	}
	
	public int getId(){
		return id;
	}
	public void setId(int i){
		id=i;
	}
	@Override
	public String toString(){
		return "Id:"+id+", rtn:"+rtn+", nombre:"+nombre+", direccion:"+direccion+", telefono:"+telefono+", celular:"+celular;
	}
	public Integer getTipoCliente() {
		return tipoCliente;
	}
	public void setTipoCliente(Integer tipoCliente) {
		this.tipoCliente = tipoCliente;
	}
	public Empleado getVendedor() {
		return vendedor;
	}
	public void setVendedor(Empleado v) {
		this.idVendedor=v.getCodigo();
		this.vendedor = v;
	}
	public RutaCobro getRutaCobro(){return rutaCobro;}
	public void setRutaCobro(RutaCobro v) {
		this.idRutaCobro=v.getCodigo();
		this.rutaCobro = v;
	}
}
