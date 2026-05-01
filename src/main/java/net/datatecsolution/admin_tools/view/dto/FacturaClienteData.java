package net.datatecsolution.admin_tools.view.dto;

public class FacturaClienteData {

	private int id;
	private String nombre;
	private String rtn;

	public FacturaClienteData() {
	}

	public FacturaClienteData(int id, String nombre, String rtn) {
		this.id = id;
		this.nombre = nombre;
		this.rtn = rtn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRtn() {
		return rtn;
	}

	public void setRtn(String rtn) {
		this.rtn = rtn;
	}
}
