package net.datatecsolution.admin_tools.modelo;

public class DatosFacturacion {
	
	
	private int codigo=0;
	private int codigoCaja=0;
	private Caja caja=new Caja();
	private int facturaInicial=0;
	private int facturaFinal=0;
	private String codigoFacturas="";
	private int cantOtorgada=0;
	private String fechaLimite="1990/01/01";
	private String CAI="NA";
	
	public DatosFacturacion(){
		
	}
	
	public DatosFacturacion(int codigo, int codigoCaja, Caja caja, int facturaInicial, int facturaFinal,
			String codigoFacturas, int cantOtorgada, String fechaLimite) {
		super();
		this.codigo = codigo;
		this.codigoCaja = codigoCaja;
		this.caja = caja;
		this.facturaInicial = facturaInicial;
		this.facturaFinal = facturaFinal;
		this.codigoFacturas = codigoFacturas;
		this.cantOtorgada = cantOtorgada;
		this.fechaLimite = fechaLimite;
	}
	
	/**
	 * @return the codigo
	 */
	public int getCodigo() {
		return codigo;
	}
	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	/**
	 * @return the codigoCaja
	 */
	public int getCodigoCaja() {
		return codigoCaja;
	}
	/**
	 * @param codigoCaja the codigoCaja to set
	 */
	public void setCodigoCaja(int codigoCaja) {
		this.codigoCaja = codigoCaja;
	}
	/**
	 * @return the caja
	 */
	public Caja getCaja() {
		
		return caja;
	}
	/**
	 * @param caja the caja to set
	 */
	public void setCaja(Caja caja) {
		
		setCodigoCaja(caja.getCodigo());
		this.caja = caja;
	}
	/**
	 * @return the facturaInicial
	 */
	public int getFacturaInicial() {
		return facturaInicial;
	}
	/**
	 * @param facturaInicial the facturaInicial to set
	 */
	public void setFacturaInicial(int facturaInicial) {
		this.facturaInicial = facturaInicial;
	}
	/**
	 * @return the facturaFinal
	 */
	public int getFacturaFinal() {
		return facturaFinal;
	}
	/**
	 * @param facturaFinal the facturaFinal to set
	 */
	public void setFacturaFinal(int facturaFinal) {
		this.facturaFinal = facturaFinal;
	}
	/**
	 * @return the codigoFacturas
	 */
	public String getCodigoFacturas() {
		return codigoFacturas;
	}
	/**
	 * @param codigoFacturas the codigoFacturas to set
	 */
	public void setCodigoFacturas(String codigoFacturas) {
		this.codigoFacturas = codigoFacturas;
	}
	/**
	 * @return the cantOtorgada
	 */
	public int getCantOtorgada() {
		return cantOtorgada;
	}
	/**
	 * @param cantOtorgada the cantOtorgada to set
	 */
	public void setCantOtorgada(int cantOtorgada) {
		this.cantOtorgada = cantOtorgada;
	}
	/**
	 * @return the fechaLimite
	 */
	public String getFechaLimite() {
		return fechaLimite;
	}
	/**
	 * @param fechaLimite the fechaLimite to set
	 */
	public void setFechaLimite(String fechaLimite) {
		this.fechaLimite = fechaLimite;
	}

	/**
	 * @return the cAI
	 */
	public String getCAI() {
		return CAI;
	}

	/**
	 * @param cAI the cAI to set
	 */
	public void setCAI(String cAI) {
		CAI = cAI;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Datos Facturacion [codigo=" + codigo + ", caja=" + caja
				+ ", factura inicial=" + facturaInicial + ", factura final=" + facturaFinal + ",\n codigo facturas="
				+ codigoFacturas + ", cant otorgada=" + cantOtorgada + ", fecha limite=" + fechaLimite + ",\n CAI=" + CAI
				+ "]";
	}
	

}
