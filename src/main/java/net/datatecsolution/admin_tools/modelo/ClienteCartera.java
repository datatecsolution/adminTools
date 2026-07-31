package net.datatecsolution.admin_tools.modelo;

import java.math.BigDecimal;

/**
 * US-127 — vista liviana de un cliente para la pantalla de transferencia de
 * cartera entre vendedores.
 *
 * No se reutiliza {@link Cliente} a proposito: ese modelo trae objetos
 * {@link Empleado} y {@link RutaCobro} completos, y {@code ClienteDao} los
 * resuelve con una consulta por fila (N+1). Para listar la cartera entera de
 * un vendedor —que en clientes grandes son miles de filas— eso es inviable.
 * Aca solo viajan los codigos crudos y lo que la tabla necesita mostrar.
 *
 * @author jdmayorga
 */
public class ClienteCartera {

	private int codigo;
	private String nombre;
	private String rtn;
	private int tipoCliente;
	private int idVendedor;
	private int idCobrador;
	private BigDecimal saldo = BigDecimal.ZERO;
	private boolean seleccionado = true;

	public int getCodigo() { return codigo; }
	public void setCodigo(int codigo) { this.codigo = codigo; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public String getRtn() { return rtn; }
	public void setRtn(String rtn) { this.rtn = rtn; }

	/** 1 = contado, 2 = credito (ver {@code cliente.tipo_cliente}). */
	public int getTipoCliente() { return tipoCliente; }
	public void setTipoCliente(int tipoCliente) { this.tipoCliente = tipoCliente; }

	public int getIdVendedor() { return idVendedor; }
	public void setIdVendedor(int idVendedor) { this.idVendedor = idVendedor; }

	public int getIdCobrador() { return idCobrador; }
	public void setIdCobrador(int idCobrador) { this.idCobrador = idCobrador; }

	public BigDecimal getSaldo() { return saldo; }
	public void setSaldo(BigDecimal saldo) { this.saldo = saldo == null ? BigDecimal.ZERO : saldo; }

	/** Marca de la casilla en la tabla de transferencia. */
	public boolean isSeleccionado() { return seleccionado; }
	public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }

	/**
	 * Describe con que rol figura el empleado consultado en este cliente.
	 * Es lo que ve el usuario en la columna "Rol actual" de la tabla.
	 */
	public String rolRespectoA(int codigoEmpleado) {
		boolean venta = idVendedor == codigoEmpleado;
		boolean cobro = idCobrador == codigoEmpleado;
		if (venta && cobro) return "Venta y cobro";
		if (venta) return "Venta";
		if (cobro) return "Cobro";
		return "";
	}

	@Override
	public String toString() {
		return codigo + " - " + nombre;
	}
}
