package net.datatecsolution.admin_tools.view.dto;

public class FacturaCabeceraData {

	public static final int TIPO_CONTADO = 1;
	public static final int TIPO_CREDITO = 2;

	private int tipoFactura;
	private String fecha;

	public FacturaCabeceraData() {
	}

	public FacturaCabeceraData(int tipoFactura, String fecha) {
		this.tipoFactura = tipoFactura;
		this.fecha = fecha;
	}

	public int getTipoFactura() {
		return tipoFactura;
	}

	public void setTipoFactura(int tipoFactura) {
		this.tipoFactura = tipoFactura;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public int getEstadoPago() {
		return tipoFactura == TIPO_CONTADO ? 1 : 0;
	}

	public boolean esContado() {
		return tipoFactura == TIPO_CONTADO;
	}

	public boolean esCredito() {
		return tipoFactura == TIPO_CREDITO;
	}
}
