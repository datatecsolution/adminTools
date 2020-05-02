package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FacturaCompra {
	private String fechaCompra="1990/01/01";
	private int noCompra=-1;
	private String idFactura="1";
	private Integer tipoFactura=1;
	private Proveedor proveedor;
	private List<DetalleFacturaProveedor> detalles=new ArrayList<DetalleFacturaProveedor>();
	private BigDecimal totalImpuesto=new BigDecimal(0.0);
	private BigDecimal totalImpuesto18=new BigDecimal(0.0);
	private BigDecimal totalOtrosImpuesto=new BigDecimal(0.0);
	private BigDecimal total=new BigDecimal(0.0);
	private BigDecimal subTotal=new BigDecimal(0.0);
	private String fechaVencimento="1990/01/01";
	private String estado="ACT";
	private int agregadoAkardex=0;
	private Departamento depart;
	
	private BigDecimal subTotal15=new BigDecimal(0.0);
	private BigDecimal subTotal18=new BigDecimal(0.0);
	private BigDecimal subTotalExcento=new BigDecimal(0.0);
	
	public FacturaCompra(){
		
	}
	public void setDepartamento(Departamento d){
		depart=d;
	}
	public Departamento getDepartamento(){
		return depart;
	}
	public int getAgregadoAkardex(){
		return agregadoAkardex;
	}
	public void setAgregadoAkardex(int a){
		agregadoAkardex=a;
	}
	
	public void setNoCompra(int i){
		noCompra=i;
	}
	public int getNoCompra(){
		return noCompra;
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
	
	public void setProveedor(Proveedor p){
		proveedor=p;
	}
	public Proveedor getProveedor(){
		return proveedor;
	}
	
	public void setFechaCompra(String f){
		fechaCompra=f;
	}
	public String getFechaCompra(){
		return fechaCompra;
	}
	
	public void setIdFactura(String idF){
		idFactura=idF;
	}
	public String getIdFactura(){
		return idFactura;
	}
	
	public void setTipoFactura(Integer t){
		tipoFactura=t;
	}
	public Integer getTipoFactura(){
		return tipoFactura;
	}
	
	public void setDetalles(List<DetalleFacturaProveedor> d){
		detalles=d;
	}
	public List<DetalleFacturaProveedor> getDetalles(){
		return detalles;
	}
	
	public void addTotalImpuesto15(BigDecimal tImp){
		totalImpuesto=totalImpuesto.add(tImp);
	}
	public BigDecimal getTotalImpuesto15(){
		return totalImpuesto;
	}
	
	public void setTotal(BigDecimal t){
		total=total.add(t);
	}
	public BigDecimal getTotal(){
		return total;
	}
	public void addSubTotal(BigDecimal s){
		subTotal=subTotal.add(s);
	}
	public BigDecimal getSubTotal(){
		return subTotal;
	}
	
	
	public void resetTotales() {
		totalImpuesto=BigDecimal.ZERO;
		total=BigDecimal.ZERO;
		subTotal=BigDecimal.ZERO;
		subTotal15=BigDecimal.ZERO;
		subTotal18=BigDecimal.ZERO;
		subTotalExcento=BigDecimal.ZERO;
		totalImpuesto18=BigDecimal.ZERO;
		totalOtrosImpuesto=BigDecimal.ZERO;
	
		//totalDescuento=BigDecimal.ZERO;
		
	}
	public BigDecimal getTotalImpuesto18() {
		return totalImpuesto18;
	}
	public void addTotalImpuesto18(BigDecimal totalImpuesto18) {
		this.totalImpuesto18=this.totalImpuesto18.add(totalImpuesto18);
	}
	public BigDecimal getTotalOtrosImpuesto() {
		return totalOtrosImpuesto;
	}
	public void addTotalOtrosImpuesto(BigDecimal totalOtrosImpuesto) {
		this.totalOtrosImpuesto = totalOtrosImpuesto;
	}
	public BigDecimal getSubTotal15() {
		return subTotal15;
	}
	public void addSubTotal15(BigDecimal subTotal15) {
		this.subTotal15=this.subTotal15.add(subTotal15);
	}
	public BigDecimal getSubTotal18() {
		return subTotal18;
	}
	public void addSubTotal18(BigDecimal subTotal18) {
		this.subTotal18=this.subTotal18.add(subTotal18);
	}
	public BigDecimal getSubTotalExcento() {
		return subTotalExcento;
	}
	public void addSubTotalExcento(BigDecimal subTotalExcento) {
		this.subTotalExcento=this.subTotalExcento.add(subTotalExcento);
	}

}
