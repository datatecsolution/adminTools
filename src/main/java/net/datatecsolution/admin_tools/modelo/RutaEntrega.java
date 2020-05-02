package net.datatecsolution.admin_tools.modelo;

import java.util.ArrayList;
import java.util.List;

public class RutaEntrega {
	private int idRuta=0;
	private int idVendedor=0;
	private Empleado vendedor;
	private String fecha;
	private String estado;
	private List<Factura> facturas=new ArrayList<Factura>();
	public int getIdRuta() {
		return idRuta;
	}
	public void setIdRuta(int id_rutas) {
		this.idRuta = id_rutas;
	}
	public int getIdVendedor() {
		return idVendedor;
	}
	public void setIdVendedor(int id_vendedor) {
		this.idVendedor = id_vendedor;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public Empleado getVendedor() {
		return vendedor;
	}
	public void setVendedor(Empleado v) {
		idVendedor=v.getCodigo();
		vendedor = v;
	}
	public List<Factura> getFacturas() {
		return facturas;
	}
	public void setFacturas(List<Factura> facturas) {
		this.facturas = facturas;
	}

}
