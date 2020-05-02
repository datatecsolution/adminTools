package net.datatecsolution.admin_tools.modelo;


public class Requisicion  extends FacturaCompra{
	private int noRequisicion;
	private Departamento departamentoOrigen=new Departamento();
	private Departamento departamentoDestino=new Departamento();
	
	public void setDepartamentoOrigen(Departamento d){
		departamentoOrigen=d;
	}
	public Departamento getDepartamentoOrigen(){
		return departamentoOrigen;
	}
	
	public void setDepartamentoDestino(Departamento d){
		departamentoDestino=d;
	}
	public Departamento getDepartamentoDestino(){
		return departamentoDestino;
	}
	
	public void setNoRequisicion(int n){
		noRequisicion=n;
	}
	public int getNoRequisicion(){
		return noRequisicion;
	}

}
