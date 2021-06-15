package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Factura {
	
	private String fecha=null;
	private Integer idFactura=0;
	private Integer tipoFactura=1;
	private Cliente cliente;
	private List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
	private BigDecimal totalImpuesto=new BigDecimal(0.0);
	private BigDecimal totalImpuesto18=new BigDecimal(0.0);
	private BigDecimal totalOtrosImpuesto=new BigDecimal(0.0);
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal subTotal=new BigDecimal(0.0);
	private BigDecimal subTotal15=new BigDecimal(0.0);
	private BigDecimal subTotal18=new BigDecimal(0.0);
	private BigDecimal subTotalExcento=new BigDecimal(0.0);
	private BigDecimal totalDescuento=new BigDecimal(0.0);
	private String fechaVencimento=null;
	private String estado;
	private int agregadoAkardex;
	private int tipoPago;
	private BigDecimal pago=new BigDecimal(0.0);
	private BigDecimal cambio=new BigDecimal(0.0);
	private BigDecimal cobroEfectivo=new BigDecimal(0.0);
	private BigDecimal cobroTarjeta=new BigDecimal(0.0);
	private String observacion="";
	private Empleado vendedor=new Empleado();
	private boolean deseaPagar=false;
	private int estadoPago=0;
	private int codigo2=0;
	private int codigoCaja=1;
	private BigDecimal saldo=new BigDecimal(0.0);
	


	
	
	public void setCodigoAlter(int c){
		codigo2=c;
	}
	public int getCodigoAlter(){
		return codigo2;
	}
	
	public void setEstadoPago(int e){
		estadoPago=e;
	}
	public int getEstadoPago(){
		return estadoPago;
	}
	public void setDeseaPagar(boolean d){
		deseaPagar=d;
	}
	public boolean getDeseaPagar(){
		return deseaPagar;
	}
	public void setVendedor(Empleado v){
		vendedor=v;
	}
	public Empleado getVendedor(){
		return vendedor;
	}
	public void setObservacion(String o){
		observacion=o;
	}
	public String getObservacion(){
		return observacion;
	}
	
	public int getTipoPago(){
		return tipoPago;
	}
	public void setTipoPago(int p){
		tipoPago=p;
	}
	
	public int getAgregadoAkardex(){
		return agregadoAkardex;
	}
	public void setAgregadoAkardex(int a){
		agregadoAkardex=a;
	}
	
	public void setPago(BigDecimal p){
		pago=p;
	}
	public BigDecimal getPago(){
		return pago;
	}
	
	public void setCambio(BigDecimal c){
		cambio=c;
	}
	public BigDecimal getCambio(){
		return cambio;
	}
	
	public String getEstado(){
		return estado;
	}
	public void setEstado(String e){
		estado=e;
	}
	public String getFechaVencimento(){
		return fechaVencimento;
	}
	public void setFechaVencimento(String f){
			fechaVencimento=f;
	}
	
	public void setCliente(Cliente c){
		cliente=c;
	}
	public Cliente getCliente(){
		return cliente;
	}
	
	public void setFecha(String f){
		fecha=f;
	}
	public String getFecha(){
		return fecha;
	}
	
	public void setIdFactura(Integer idF){
		idFactura=idF;
	}
	public Integer getIdFactura(){
		return idFactura;
	}
	
	public void setTipoFactura(Integer t){
		tipoFactura=t;
	}
	public Integer getTipoFactura(){
		return tipoFactura;
	}
	
	public void setDetalles(List<DetalleFactura> d){
		/*List<DetalleFactura> nuevaDetalle= new ArrayList<DetalleFactura>();
		nuevaDetalle=nuevaDetalle;
		detalles.clear();*/
		detalles=d;
	}
	public List<DetalleFactura> getDetalles(){
		return detalles;
	}
	
	public void setTotalImpuesto(BigDecimal tImp){
		totalImpuesto=totalImpuesto.add(tImp);
	}
	public BigDecimal getTotalImpuesto(){
		return totalImpuesto;
	}
	public void setTotalOtrosImpuesto(BigDecimal tImp){
		totalOtrosImpuesto=totalOtrosImpuesto.add(tImp);
	}
	public BigDecimal getTotalOtrosImpuesto1(){
		return totalOtrosImpuesto;
	}
	public void setTotalImpuesto18(BigDecimal tImp){
		totalImpuesto18=totalImpuesto18.add(tImp);
	}
	public BigDecimal getTotalImpuesto18(){
		return totalImpuesto18;
	}
	
	public void setTotal(BigDecimal t){
		total=total.add(t);
	}
	public BigDecimal getTotal(){
		return total;
	}
	
	public void setSubTotal(BigDecimal s){
		subTotal=subTotal.add(s);
	}
	public BigDecimal getSubTotal(){
		return subTotal;
	}
	
	public void setSubTotal15(BigDecimal s){
		subTotal15=subTotal15.add(s);
	}
	public BigDecimal getSubTotal15(){
		return subTotal15;
	}
	public void setSubTotal18(BigDecimal s){
		subTotal18=subTotal18.add(s);
	}
	public BigDecimal getSubTotal18(){
		return subTotal18;
	}
	public void setSubTotalExcento(BigDecimal s){
		subTotalExcento=subTotalExcento.add(s);
	}
	public BigDecimal getSubTotalExcento(){
		return subTotalExcento;
	}
	
	
	
	public void setTotalDescuento(BigDecimal s){
		totalDescuento=totalDescuento.add(s);
	}
	public BigDecimal getTotalDescuento(){
		return totalDescuento;
	}
	
	public void resetTotales(){
		totalImpuesto=BigDecimal.ZERO;
		total=BigDecimal.ZERO;
		subTotal=BigDecimal.ZERO;
		subTotal18=BigDecimal.ZERO;
		subTotal15=BigDecimal.ZERO;
		subTotalExcento=BigDecimal.ZERO;
		totalDescuento=BigDecimal.ZERO;
		totalImpuesto18=BigDecimal.ZERO;
		totalOtrosImpuesto=BigDecimal.ZERO;
		cobroEfectivo=BigDecimal.ZERO;
		cobroTarjeta=BigDecimal.ZERO;
		pago=BigDecimal.ZERO;
		cambio=BigDecimal.ZERO;
	
	}
	@Override
	public String toString(){
		return "Fecha: "+fecha
				+", Id factura: "+idFactura
				+", tipo factura: "+tipoFactura
				+", Cliente"+cliente.getNombre()
				+", subtotal:"+subTotal
				+", Impuesto:"+totalImpuesto
				+", descuento:"+totalDescuento
				+", total:"+total;
	}
	/**
	 * @return the cobroEfectivo
	 */
	public BigDecimal getCobroEfectivo() {
		return cobroEfectivo;
	}
	/**
	 * @param cobroEfectivo the cobroEfectivo to set
	 */
	public void setCobroEfectivo(BigDecimal cobroEfectivo) {
		this.cobroEfectivo = cobroEfectivo;
	}
	/**
	 * @return the cobroTarjeta
	 */
	public BigDecimal getCobroTarjeta() {
		return cobroTarjeta;
	}
	/**
	 * @param cobroTarjeta the cobroTarjeta to set
	 */
	public void setCobroTarjeta(BigDecimal cobroTarjeta) {
		this.cobroTarjeta = cobroTarjeta;
	}
	public int getCodigoCaja() {
		return codigoCaja;
	}
	public void setCodigoCaja(int codigoCaja) {
		this.codigoCaja = codigoCaja;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

}
