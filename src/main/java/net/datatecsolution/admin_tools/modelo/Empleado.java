package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

/**

* Esta clase representa a un empleado de la empresa

* @author: jdmayorga

* @version: 16/07/2015

* @see <a href = "https://www.github.com/jdmayorga" /> direccion del creador </a>

*/
public class Empleado extends Persona {
	
	private int codigo=0;	
	private BigDecimal sueldoBase=new BigDecimal(0.00);
	private BigDecimal tComisionVenta=new BigDecimal(0.00);
	private BigDecimal tVenta=new BigDecimal(0.00);
	private BigDecimal porcetajeComision=new BigDecimal(0.00);

	public Empleado() {
		// TODO Auto-generated constructor stub
	}
	public Empleado(String n, String a) {
		// TODO Auto-generated constructor stub
		//super(nombre,apellido);
		super.setApellido(a);
		super.setNombre(n);
	}
	/**

     * metodo para establecer el codigo empleado

     * @param c establece el codigo del empleado 

     */
	public void setCodigo(int c){
		codigo=c;
	}
	/**

     * M�todo que devuelve el codigo del empleado
     * @return codigo empleado

     */
	public int getCodigo(){
		return codigo;
	}
	/**

     * metodo para establecer el sueldo base del empleado

     * @param sueldo establece el sueldo del empleado 

     */
	public void setSueldoBase(BigDecimal sueldo){
		sueldoBase=sueldo;
	}
	
	/**

     * M�todo que devuelve el sueldo base del empleado
     * @return sueldo base

     */
	public BigDecimal getSueldoBase(){
		return sueldoBase;
	}
	
	/**

     * metodo para establecer el total de la comision del empleado

     * @param comision establece el total de la comision 

     */
	public void setTotalComisionVenta(BigDecimal comision){
		tComisionVenta=comision;
	}
	
	/**

     * M�todo que devuelve el total de la comision empleado
     * @return total comision empleado

     */
	public BigDecimal getTotalComisionVenta(){
		return tComisionVenta;
	}
	
	/**

     * metodo para establecer el total de ventas del empleado

     * @param ventas establece el total de las ventas 

     */
	public void setTotalVentas(BigDecimal ventas){
		this.tVenta=ventas;
	}
	
	
	/**

     * M�todo que devuelve el total de ventas del empleado
     * @return total ventas del empleado

     */
	public BigDecimal getTotalVentas(){
		return this.tVenta;
	}
	
	/**

     * metodo para establecer el %  de la comision de ventas del empleado

     * @param comision  establece % de la comision de las ventas 

     */
	public void setPorcentajeComision(BigDecimal comision){
		this.porcetajeComision=comision;
	}
	/**

     * M�todo que devuelve el % las comision de ventas del empleado
     * @return porcentaje de comision ventas del empleado

     */
	public BigDecimal getPorcentajeComision(){
		return this.porcetajeComision;
	}
	
	@Override
	public String toString(){
		return this.nombre+" "+this.apellido;
	}
	

}
