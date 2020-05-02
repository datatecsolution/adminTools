package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

public class Insumo {
	private BigDecimal cantidad=new BigDecimal(1);
	private BigDecimal total=new BigDecimal(0);
	private int codigoInsumo=-1;
	private int codigoServicio=-1;
	private Articulo servicio=null;
	private Articulo articulo=null;

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public int getCodigoInsumo() {
		return codigoInsumo;
	}

	public void setCodigoInsumo(int codigoInsumo) {
		this.codigoInsumo = codigoInsumo;
	}

	public int getCodigoServicio() {
		return codigoServicio;
	}

	public void setCodigoServicio(int codigoServicio) {
		this.codigoServicio = codigoServicio;
	}

	public Articulo getServicio() {
		return servicio;
	}

	public void setServicio(Articulo servicio) {
		this.servicio = servicio;
	}

	public Articulo getArticulo() {
		return articulo;
	}

	public void setArticulo(Articulo articulo) {
		this.articulo = articulo;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

}
