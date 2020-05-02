package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CierreCaja {
	private Integer id;
	private String fecha;
	private Integer noFacturaInicio=0;
	private Integer noFacturaFinal=0;
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal totalEfectivo=new BigDecimal(0.0);
	private BigDecimal efectivoInicial=new BigDecimal(0.0);
	private BigDecimal efectivo=new BigDecimal(0.0);
	private BigDecimal tarjeta=new BigDecimal(0.0);
	private BigDecimal credito=new BigDecimal(0.0);
	
	private BigDecimal isv15=new BigDecimal(0.0);
	private BigDecimal totalIsv15=new BigDecimal(0.0);
	private BigDecimal totalExcento=new BigDecimal(0.0);
	private BigDecimal apertura=new BigDecimal(0.0);
	private BigDecimal isv18=new BigDecimal(0.0);
	private BigDecimal totalIsv18=new BigDecimal(0.0);
	private BigDecimal totalSalida=new BigDecimal(0.0);
	private BigDecimal totalEntrada=new BigDecimal(0.0);
	private BigDecimal totalCobro=new BigDecimal(0.0);
	private BigDecimal totalPago=new BigDecimal(0.0);
	private BigDecimal totalEfectivoCaja=new BigDecimal(0.0);
	
	private int noSalidaInicial=0;
	private int noSalidaFinal=0;
	
	private int noEntradaInicial=0;
	private int noEntradaFinal=0;
	
	private int noCobroInicial=0;
	private int noCobroFinal=0;
	
	private int noPagoInicial=0;
	private int noPagoFinal=0;

	
	private boolean estado=false;
	
	private String usuario="";
	private String turno="NA";
	private List<CierreFacturacion> cierreFacturas=new ArrayList<CierreFacturacion>();
	
	public void setEfectivoCaja(BigDecimal t){
		totalEfectivoCaja=t;
	}
	public BigDecimal getEfectivoCaja(){
		return totalEfectivoCaja;
	}
	
	
	public void setTotalCobro(BigDecimal t){
		totalCobro=t;
	}
	public BigDecimal getTotalCobro(){
		return totalCobro;
	}
	
	public void setNoCobroInicial(int noCobro){
		noCobroInicial=noCobro;
	}
	public int getNoCobroInicial(){
		return noCobroInicial;
	}
	
	public void setNoCobroFinal(int noCobro){
		noCobroFinal=noCobro;
	}
	public int getNoCobroFinal(){
		return noCobroFinal;
	}
	
	public void setTotalSalida(BigDecimal t){
		totalSalida=t;
	}
	public BigDecimal getTotalSalida(){
		return totalSalida;
	}
	
	public void setNoSalidaInicial(int noSalida){
		noSalidaInicial=noSalida;
	}
	public int getNoSalidaInicial(){
		return noSalidaInicial;
	}
	
	public void setNoSalidaFinal(int nSalida){
		noSalidaFinal=nSalida;
	}
	public int getNoSalidaFinal(){
		return noSalidaFinal;
	}
	
	public void setTotalEfectivo(BigDecimal c){
		totalEfectivo=c;
	}
	public BigDecimal getTotalEfectivo(){
		return totalEfectivo;
	}
	public void setTotalExcento(BigDecimal c){
		totalExcento=c;
	}
	public BigDecimal getTotalExcento(){
		return totalExcento;
	}
	public void setTotalIsv18(BigDecimal c){
		totalIsv18=c;
	}
	public BigDecimal getTotalIsv18(){
		return totalIsv18;
	}
	public void setTotalIsv15(BigDecimal c){
		totalIsv15=c;
	}
	public BigDecimal getTotalIsv15(){
		return totalIsv15;
	}
	public void setUsuario(String u){
		usuario=u;
	}
	public String getUsuario(){
		return usuario;
	}
	
	public void setId(Integer i){
		id=i;
	}
	public Integer getId(){
		return id;
	}
	public void setEstado(boolean e){
		estado=e;
	}
	public boolean getEstado(){
		return estado;
	}
	
	
	public void setCredito(BigDecimal c){
		credito=c;
	}
	public BigDecimal getCredito(){
		return credito;
	}
	
	public void setApertura(BigDecimal a){
		apertura=a;
	}
	public BigDecimal getApertura(){
		return apertura;
	}
	
	
	public void setIsv15(BigDecimal i){
		isv15=i;
	}
	public BigDecimal getIsv15(){
		return isv15;
	}
	
	public void setIsv18(BigDecimal i){
		isv18=i;
	}
	public BigDecimal getIsv18(){
		return isv18;
	}
	
	
	public void setEfectivo(BigDecimal e){
		efectivo=e;
	}
	public BigDecimal getEfectivo(){
		return efectivo;
	}
	public void setEfectivoInicial(BigDecimal e){
		efectivoInicial=e;
	}
	public BigDecimal getEfectivoInicial(){
		return efectivoInicial;
	}
	
	
	public void setTarjeta(BigDecimal t){
		tarjeta=t;
	}
	public BigDecimal getTarjeta(){
		return tarjeta;
	}
	
	
	
	
	public void setTotal(BigDecimal t){
		total=t;
	}
	public BigDecimal getTotal(){
		return total;
	}
	
	public void setNoFacturaFinal(Integer i){
		noFacturaFinal=i;
	}
	public Integer getNoFacturaFinal(){
		return noFacturaFinal;
	}
	
	public void setNoFacturaInicio(Integer i){
		noFacturaInicio=i;
	}
	public Integer getNoFacturaInicio(){
		return noFacturaInicio;
	}
	
	public void setFecha(String f){
		fecha=f;
	}
	public String getFecha(){
		return fecha;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CierreCaja [id=" + id + ", fecha=" + fecha + ", noFacturaInicio=" + noFacturaInicio
				+ ", noFacturaFinal=" + noFacturaFinal + ", total=" + total + ", totalEfectivo=" + totalEfectivo
				+ ", efectivoInicial=" + efectivoInicial + ", efectivo=" + efectivo + ", tarjeta=" + tarjeta
				+ ", credito=" + credito + ", isv15=" + isv15 + ", totalIsv15=" + totalIsv15 + ", totalExcento="
				+ totalExcento + ", apertura=" + apertura + ", isv18=" + isv18 + ", totalIsv18=" + totalIsv18
				+ ", totalSalida=" + totalSalida + ", totalCobro=" + totalCobro + ", totalEfectivoCaja="
				+ totalEfectivoCaja + ", noSalidaInicial=" + noSalidaInicial + ", noSalidaFinal=" + noSalidaFinal
				+ ", noCobroInicial=" + noCobroInicial + ", noCobroFinal=" + noCobroFinal + ", estado=" + estado
				+ ", usuario=" + usuario + "]";
	}
	/**
	 * @return the noPagoInicial
	 */
	public int getNoPagoInicial() {
		return noPagoInicial;
	}
	/**
	 * @param noPagoInicial the noPagoInicial to set
	 */
	public void setNoPagoInicial(int noPagoInicial) {
		this.noPagoInicial = noPagoInicial;
	}
	/**
	 * @return the noPagoFinal
	 */
	public int getNoPagoFinal() {
		return noPagoFinal;
	}
	/**
	 * @param noPagoFinal the noPagoFinal to set
	 */
	public void setNoPagoFinal(int noPagoFinal) {
		this.noPagoFinal = noPagoFinal;
	}
	/**
	 * @return the totalPago
	 */
	public BigDecimal getTotalPago() {
		return totalPago;
	}
	/**
	 * @param totalPago the totalPago to set
	 */
	public void setTotalPago(BigDecimal totalPago) {
		this.totalPago = totalPago;
	}
	public List<CierreFacturacion> getCierreFacturas() {
		return cierreFacturas;
	}
	public void setCierreFacturas(List<CierreFacturacion> cierreFacturas) {
		this.cierreFacturas = cierreFacturas;
	}
	/**
	 * @return the noEntradaInicial
	 */
	public int getNoEntradaInicial() {
		return noEntradaInicial;
	}
	/**
	 * @param noEntradaInicial the noEntradaInicial to set
	 */
	public void setNoEntradaInicial(int noEntradaInicial) {
		this.noEntradaInicial = noEntradaInicial;
	}
	/**
	 * @return the noEntradaFinal
	 */
	public int getNoEntradaFinal() {
		return noEntradaFinal;
	}
	/**
	 * @param noEntradaFinal the noEntradaFinal to set
	 */
	public void setNoEntradaFinal(int noEntradaFinal) {
		this.noEntradaFinal = noEntradaFinal;
	}
	public BigDecimal getTotalEntrada() {
		return totalEntrada;
	}
	public void setTotalEntrada(BigDecimal totalEntrada) {
		this.totalEntrada = totalEntrada;
	}
	
	public String getTurno() {
		
		return turno;
	}
	public void setTurno(String turno) {
		this.turno = turno;
	}
	

}
