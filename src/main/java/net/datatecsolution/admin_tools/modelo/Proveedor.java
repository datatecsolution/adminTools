package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class Proveedor extends Persona {
	private int id;
	private String celular;
	private BigDecimal saldo=new BigDecimal(0);
	//Conexion conex= new Conexion();
	
	//contructor vacio
	public Proveedor(){
		
	}
	
	//contructor con todos los valores del proveedor
	public Proveedor(int i,String n,String t,String c,String d){
		nombre=n;
		id=i;
		celular=t;
		telefono=t;
		direccion=d;
	}
		
	public void setId(int i){
		id=i;
	}
	public int getId(){
		return id;
	}
	
	
	public void setCelular(String c){
		
		celular=c;
	}
	public String getCelular(){
		return celular;
	}
	
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Proveedor [id=" + id+super.toString() + ", celular=" + celular + ", saldo=" + saldo + "]";
	}

	/**
	 * @return the saldo
	 */
	public BigDecimal getSaldo() {
		return saldo;
	}

	/**
	 * @param saldo the saldo to set
	 */
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	
	
}
