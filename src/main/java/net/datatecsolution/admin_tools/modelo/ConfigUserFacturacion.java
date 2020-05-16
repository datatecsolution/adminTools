package net.datatecsolution.admin_tools.modelo;

public class ConfigUserFacturacion {
	private int id=0;
	private String usuario="";
	private String formatoFactura="tiket";
	private boolean pwdDescuento=false;
	private boolean pwdPrecio=false;
	private boolean ventanaVendedor=false;
	private boolean descPorcentaje=false;
	private boolean ventanaObservaciones=false;
	private boolean precioRedondear=false;
	private boolean facturarSinInventario=false;
	private boolean imprReportCategCierre=false;
	private boolean imprReportSalida=false;
	private boolean showReportSalida=false;
	private boolean imprReportEntrada=false;
	private boolean showReportEntrada=false;
	private boolean activarBusquedaFacturacion=false;
	private boolean agregarClienteCredito=false;
	private String formatoFacturaCredito="tiket";
	private Usuario usuario2=null;
	private Empleado vendedorEnBusqueda;

	public Departamento getDepartEnBusqueda() {
		return departEnBusqueda;
	}

	public void setDepartEnBusqueda(Departamento departEnBusqueda) {
		this.departEnBusqueda = departEnBusqueda;
	}

	private Departamento departEnBusqueda;
	
	public ConfigUserFacturacion(){
		vendedorEnBusqueda =new Empleado();
		vendedorEnBusqueda.setCodigo(1);

		departEnBusqueda=new Departamento();
		departEnBusqueda.setId(1);
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the usuario
	 */
	public String getUsuario() {
		return usuario;
	}
	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	/**
	 * @return the formatoFactura
	 */
	public String getFormatoFactura() {
		return formatoFactura;
	}
	/**
	 * @param formatoFactura the formatoFactura to set
	 */
	public void setFormatoFactura(String formatoFactura) {
		this.formatoFactura = formatoFactura;
	}
	/**
	 * @return the pwdDescuento
	 */
	public boolean isPwdDescuento() {
		return pwdDescuento;
	}
	/**
	 * @param pwdDescuento the pwdDescuento to set
	 */
	public void setPwdDescuento(boolean pwdDescuento) {
		this.pwdDescuento = pwdDescuento;
	}
	/**
	 * @return the pwdPrecio
	 */
	public boolean isPwdPrecio() {
		return pwdPrecio;
	}
	/**
	 * @param pwdPrecio the pwdPrecio to set
	 */
	public void setPwdPrecio(boolean pwdPrecio) {
		this.pwdPrecio = pwdPrecio;
	}
	/**
	 * @return the pwdVendedor
	 */
	public boolean isVentanaVendedor() {
		return ventanaVendedor;
	}
	/**
	 * @param pwdVendedor the pwdVendedor to set
	 */
	public void setVentanaVendedor(boolean pwdVendedor) {
		this.ventanaVendedor = pwdVendedor;
	}
	/**
	 * @return the descPorcentaje
	 */
	public boolean isDescPorcentaje() {
		return descPorcentaje;
	}
	/**
	 * @param descPorcentaje the descPorcentaje to set
	 */
	public void setDescPorcentaje(boolean descPorcentaje) {
		this.descPorcentaje = descPorcentaje;
	}
	public boolean isVentanaObservaciones() {
		return ventanaObservaciones;
	}
	public void setVentanaObservaciones(boolean ventanaObservaciones) {
		this.ventanaObservaciones = ventanaObservaciones;
	}
	public boolean isPrecioRedondear() {
		return precioRedondear;
	}
	public void setPrecioRedondear(boolean precioRedondear) {
		this.precioRedondear = precioRedondear;
	}
	/**
	 * @return the facturarSinInventario
	 */
	public boolean isFacturarSinInventario() {
		return facturarSinInventario;
	}
	/**
	 * @param facturarSinInventario the facturarSinInventario to set
	 */
	public void setFacturarSinInventario(boolean facturarSinInventario) {
		this.facturarSinInventario = facturarSinInventario;
	}
	/**
	 * @return the imprReportCategCierre
	 */
	public boolean isImprReportCategCierre() {
		return imprReportCategCierre;
	}
	/**
	 * @param imprReportCategCierre the imprReportCategCierre to set
	 */
	public void setImprReportCategCierre(boolean imprReportCategCierre) {
		this.imprReportCategCierre = imprReportCategCierre;
	}
	/**
	 * @return the imprReportSalida
	 */
	public boolean isImprReportSalida() {
		return imprReportSalida;
	}
	/**
	 * @param imprReportSalida the imprReportSalida to set
	 */
	public void setImprReportSalida(boolean imprReportSalida) {
		this.imprReportSalida = imprReportSalida;
	}
	/**
	 * @return the showReportSalida
	 */
	public boolean isShowReportSalida() {
		return showReportSalida;
	}
	/**
	 * @param showReportSalida the showReportSalida to set
	 */
	public void setShowReportSalida(boolean showReportSalida) {
		this.showReportSalida = showReportSalida;
	}
	/**
	 * @return the imprReportEntrada
	 */
	public boolean isImprReportEntrada() {
		return imprReportEntrada;
	}
	/**
	 * @param imprReportEntrada the imprReportEntrada to set
	 */
	public void setImprReportEntrada(boolean imprReportEntrada) {
		this.imprReportEntrada = imprReportEntrada;
	}
	/**
	 * @return the showReportEntrada
	 */
	public boolean isShowReportEntrada() {
		return showReportEntrada;
	}
	/**
	 * @param showReportEntrada the showReportEntrada to set
	 */
	public void setShowReportEntrada(boolean showReportEntrada) {
		this.showReportEntrada = showReportEntrada;
	}
	/**
	 * @return the activarBusquedaFacturacion
	 */
	public boolean isActivarBusquedaFacturacion() {
		return activarBusquedaFacturacion;
	}
	/**
	 * @param activarBusquedaFacturacion the activarBusquedaFacturacion to set
	 */
	public void setActivarBusquedaFacturacion(boolean activarBusquedaFacturacion) {
		this.activarBusquedaFacturacion = activarBusquedaFacturacion;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConfigUserFacturacion [id=" + id + ", usuario=" + usuario + ", formatoFactura=" + formatoFactura
				+ ", pwdDescuento=" + pwdDescuento + ", pwdPrecio=" + pwdPrecio + ", ventanaVendedor=" + ventanaVendedor
				+ ", descPorcentaje=" + descPorcentaje + ", ventanaObservaciones=" + ventanaObservaciones
				+ ", precioRedondear=" + precioRedondear + ", facturarSinInventario=" + facturarSinInventario
				+ ", imprReportCategCierre=" + imprReportCategCierre + ", imprReportSalida=" + imprReportSalida
				+ ", showReportSalida=" + showReportSalida + ", imprReportEntrada=" + imprReportEntrada
				+ ", showReportEntrada=" + showReportEntrada + ", activarBusquedaFacturacion="
				+ activarBusquedaFacturacion + "]";
	}
	public boolean isAgregarClienteCredito() {
		return agregarClienteCredito;
	}
	public void setAgregarClienteCredito(boolean agregarClienteCredito) {
		this.agregarClienteCredito = agregarClienteCredito;
	}
	public String getFormatoFacturaCredito() {
		return formatoFacturaCredito;
	}
	public void setFormatoFacturaCredito(String formatoFacturaCredito) {
		this.formatoFacturaCredito = formatoFacturaCredito;
	}
	public Usuario getUser() {
		return usuario2;
	}
	public void setUser(Usuario usuario2) {
		this.usuario2 = usuario2;
	}
	/**
	 * @return the vendedorBusqueda
	 */
	public Empleado getVendedorEnBusqueda() {
		return vendedorEnBusqueda;
	}
	/**
	 * @param vendedorEnBusqueda the vendedorBusqueda to set
	 */
	public void setVendedorEnBusqueda(Empleado vendedorEnBusqueda) {
		this.vendedorEnBusqueda = vendedorEnBusqueda;
	}
	

}
