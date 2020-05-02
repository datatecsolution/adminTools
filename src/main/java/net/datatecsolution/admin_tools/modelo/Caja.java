package net.datatecsolution.admin_tools.modelo;

public class Caja {
	private int codigo=0;
	private String descripcion="";
	private int codigoBodega=0;
	private Departamento departamento=new Departamento();
	private String nombreBd="";
	private boolean activa=false;
	
	public Caja(){
		
	}
	public Caja(Caja cajaActiva) {
		// TODO Auto-generated constructor stub
		codigo=new Integer(cajaActiva.getCodigo());
		descripcion= cajaActiva.getDescripcion();
		codigoBodega=new Integer(cajaActiva.codigoBodega);
		this.nombreBd= cajaActiva.nombreBd;
		this.activa=new Boolean(cajaActiva.isActiva());
		departamento=cajaActiva.getDetartamento();
	}
	//private List<Categoria> categoriasFactura=
	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public Departamento getDetartamento() {
		return departamento;
	}
	public void setDetartamento(Departamento dept) {
		
		this.departamento = dept;
		setCodigoBodega(dept.getId());
	}
	public String getNombreBd() {
		return nombreBd;
	}
	public void setNombreBd(String nombreBd) {
		this.nombreBd = nombreBd;
	}
	/**
	 * @return the codigoBodega
	 */
	public int getCodigoBodega() {
		return codigoBodega;
	}
	/**
	 * @param codigoBodega the codigoBodega to set
	 */
	public void setCodigoBodega(int codigoBodega) {
		this.codigoBodega = codigoBodega;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.descripcion+"  "+this.departamento.getDescripcion()+(isActiva()==true?" Default":"");
	}
	/**
	 * @return the activa
	 */
	public boolean isActiva() {
		return activa;
	}
	/**
	 * @param activa the activa to set
	 */
	public void setActiva(boolean activa) {
		this.activa = activa;
	}

}
