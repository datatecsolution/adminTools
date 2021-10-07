package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
 
public class Articulo {
	
	private int codigo=-1;
	private String articulo;
	
	//private String marca;
	//private double impuesto;
	private Categoria mar=new Categoria();
	private Impuesto imp=new Impuesto();
	private List<CodBarra> codigos=new ArrayList<CodBarra>();
	private double precioVenta=0;
	private double precioCompra=0;
	private int tipoArticulo=0;
	private List<PrecioArticulo> preciosVenta=new ArrayList<PrecioArticulo>();
	private int  posicionPrecio=0;
	private String codigoBarra="";
	private double existencia;
	private boolean estado=true;
	public Articulo(){
		
	}
	
	public Articulo(int c, String a,Impuesto i, Vector<CodBarra> cods, double p){
		codigo=c;
		articulo=a;
		
		imp=i;
		codigos=cods;
		precioVenta=p;
	}
	public void setCodigoBarra(String c){
		codigoBarra=c;
	}
	public String getCodigoBarra(){
		return codigoBarra;
	}
	public void setPreciosVenta(List<PrecioArticulo> precios){
		preciosVenta=precios;
	}
	
	public List<PrecioArticulo> getPreciosVenta(){
		return preciosVenta;
	}
	public void netPrecio(){
		posicionPrecio++;
		if(posicionPrecio==preciosVenta.size()-1){
			precioVenta=preciosVenta.get(preciosVenta.size()-1).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			//posicionPrecio--;
		}
		else{
			if(posicionPrecio>=preciosVenta.size()){
				posicionPrecio=preciosVenta.size()-1;
				precioVenta=preciosVenta.get(posicionPrecio).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			}else
				precioVenta=preciosVenta.get(posicionPrecio).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		}
	}
	public void setPrecio(PrecioArticulo pr){
		int index=-1;
		
		for(int c=0;c<preciosVenta.size();c++){
			
			if(preciosVenta.get(c).getCodigoPrecio()==pr.getCodigoPrecio()){
				
				index=c;
			}
		}
		if(index!=-1)
			precioVenta=preciosVenta.get(index).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}
	public void lastPrecio(){
		posicionPrecio--;
		if(posicionPrecio<=0){
			precioVenta=preciosVenta.get(0).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			posicionPrecio=0;
		}
		else{
			//posicionPrecio--;
			precioVenta=preciosVenta.get(posicionPrecio).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		}
	}
	public void setTipoArticulo(int t){
		tipoArticulo=t;
	}
	public int getTipoArticulo(){
		return tipoArticulo;
	}
	public void setPrecioCompra(double pc){
		precioCompra=pc;
	}
	public double getPrecioCompra(){
		return precioCompra;
	}
	public void setPrecioVenta(double p){
		precioVenta=p;
	}
	public double getPrecioVenta(){
		return precioVenta;
	}
	
	public void setCodBarra(Vector<CodBarra> cods){
		codigos=cods;
		
	}
	public List<CodBarra> getCodBarra(){
		return codigos;
	}
	public void setCodBarras(List<CodBarra> cods){
		codigos=cods;
	}
	public void setId(int c){
		codigo=c;
	}
	public int getId(){
		return codigo;
	}
	
	public Categoria getCategoria(){
		return mar;
	}
	public void setCategoria(Categoria m){
		mar=m;
		
	}
	
	public Impuesto getImpuestoObj(){
		return imp;
	}
	public void setImpuestoObj(Impuesto i){
		imp=i;
		
	}
	
	public void setArticulo(String a){
		articulo=a;
	}
	public String getArticulo(){
		return articulo;
	}
	
	
	
	/*public void setMarca(String m){
		marca=m;
	}
	public String getMarca(){
		return marca;
	}*/
	
	@Override
	public String toString(){
		return "Id Articulo:"+codigo+", Articulo:"+articulo+", Precio Venta:"+ this.precioVenta+"Marca["+mar.toString()+"]"+", Impueso:"+imp.getPorcentaje()+"%"+
	", Codigos Barra["+codigos+"] , Tipo Articulo:"+tipoArticulo;
	}
	
	/**
	 * @return the existencia
	 */
	public double getExistencia() {
		return existencia;
	}

	/**
	 * @param existencia the existencia to set
	 */
	public void setExistencia(double existencia) {
		this.existencia = existencia;
	}

	public boolean isEstado() {
		return estado;
	}
	
	/**
	 * @param estado to set
	 */
	public void setEstado(boolean estado) {
		this.estado = estado;
	}


}
