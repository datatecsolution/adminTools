package net.datatecsolution.admin_tools.modelo;

public class FilaReporteInventario {

	private int codigoArticulo;
	private String articulo = "";
	private String codArticulo = "";
	private double totalIngreso = 0;
	private double totalSalida = 0;
	private double saldo = 0;
	private double costo = 0;

	public int getCodigoArticulo() {
		return codigoArticulo;
	}

	public void setCodigoArticulo(int codigoArticulo) {
		this.codigoArticulo = codigoArticulo;
	}

	public String getArticulo() {
		return articulo;
	}

	public void setArticulo(String articulo) {
		this.articulo = articulo;
	}

	public String getCodArticulo() {
		return codArticulo;
	}

	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}

	public double getTotalIngreso() {
		return totalIngreso;
	}

	public void setTotalIngreso(double totalIngreso) {
		this.totalIngreso = totalIngreso;
	}

	public double getTotalSalida() {
		return totalSalida;
	}

	public void setTotalSalida(double totalSalida) {
		this.totalSalida = totalSalida;
	}

	public double getSaldo() {
		return saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	public double getCosto() {
		return costo;
	}

	public void setCosto(double costo) {
		this.costo = costo;
	}

	public double getEfectivoSaldo() {
		return saldo * costo;
	}
}
