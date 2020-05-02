package net.datatecsolution.admin_tools.modelo;

public class CierreFacturacion {
	
	
	private Integer noFacturaInicio=0;
	private Integer noFacturaFinal=0;
	private Caja caja=new Caja();
	private Integer codigoCierre=0;
	private String usuario="";
	private Integer id=-1;
	
	
	public CierreFacturacion(){
		
	}
	
	public CierreFacturacion(Integer noFacturaInicio, Integer noFacturaFinal, Caja caja, Integer codigoCierre) {
		super();
		this.noFacturaInicio = noFacturaInicio;
		this.noFacturaFinal = noFacturaFinal;
		this.caja = caja;
		this.codigoCierre = codigoCierre;
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
	public Caja getCaja() {
		return caja;
	}
	public void setCaja(Caja caja) {
		this.caja = caja;
	}
	public Integer getCodigoCierre() {
		return codigoCierre;
	}
	public void setCodigoCierre(Integer codigoCierre) {
		this.codigoCierre = codigoCierre;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CierreFacturacion [noFacturaInicio=" + noFacturaInicio + ", noFacturaFinal=" + noFacturaFinal
				+ ", caja=" + caja + ", codigoCierre=" + codigoCierre + ", usuario=" + usuario + ", id=" + id + "]";
	}

}
