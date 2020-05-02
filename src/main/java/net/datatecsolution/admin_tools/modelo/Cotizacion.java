package net.datatecsolution.admin_tools.modelo;

public class Cotizacion extends Factura {
	private boolean hechaFactura=false;

	public Cotizacion() {
		// TODO Auto-generated constructor stub
	}

	public boolean isHechaFactura() {
		return hechaFactura;
	}

	public void setHechaFactura(boolean hechaFactura) {
		this.hechaFactura = hechaFactura;
	}

}
