package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;
import java.sql.Date;

public class CuentaFactura {
	private int codigoCuenta=0;
	//private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	private int noFactura=-1;
	private Factura factura;
	
	private int codigoCliente=-1;
	private Cliente cliente=null;
	
	private int codigoCaja=-1;
	private Caja caja=null;
	
	private Date fecha;
	private Date fechaVenc;
	//private String fechaVenc;
	private BigDecimal saldo=new BigDecimal(0.0);
	
	private BigDecimal pago=new BigDecimal(0.0);
	private BigDecimal newSaldo=new BigDecimal(0.0);
	
	private CuentaXCobrarFactura ultimoPago;

	public int getCodigoCuenta() {
		
		return codigoCuenta;
	}

	public void setCodigoCuenta(int codigoCuenta) {
		this.codigoCuenta = codigoCuenta;
	}

	public int getCodigoCliente() {
		return codigoCliente;
	}

	public void setCodigoCliente(int codigoCliente) {
		this.codigoCliente = codigoCliente;
	}

	public int getNoFactura() {
		return noFactura;
	}

	public void setNoFactura(int noFactura) {
		this.noFactura = noFactura;
	}

	public Factura getFactura() {
		return factura;
	}

	public void setFactura(Factura f) {
		
		this.setNoFactura(f.getIdFactura());
		this.factura = f;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente c) {
		this.setCodigoCliente(c.getId());
		this.cliente = c;
	}

	public int getCodigoCaja() {
		return codigoCaja;
	}

	public void setCodigoCaja(int codigoCaja) {
		this.codigoCaja = codigoCaja;
	}

	public Caja getCaja() {
		return caja;
	}

	public void setCaja(Caja c) {
		
		this.setCodigoCaja(c.getCodigo());
		this.caja = c;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getPago() {
		return pago;
	}

	public void setPago(BigDecimal pago) {
		this.pago = pago;
	}

	public BigDecimal getNewSaldo() {
		return newSaldo;
	}

	public void setNewSaldo(BigDecimal newSaldo) {
		this.newSaldo = newSaldo;
	}

	public Date getFechaVenc() {
		return fechaVenc;
	}

	public void setFechaVenc(Date fechaVenc) {
		this.fechaVenc = fechaVenc;
	}

	/**
	 * @return the ultimoPago
	 */
	public CuentaXCobrarFactura getUltimoPago() {
		return ultimoPago;
	}

	/**
	 * @param ultimoPago the ultimoPago to set
	 */
	public void setUltimoPago(CuentaXCobrarFactura ultimoPago) {
		this.ultimoPago = ultimoPago;
	}
	

}
