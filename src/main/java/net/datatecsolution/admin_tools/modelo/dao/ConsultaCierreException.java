package net.datatecsolution.admin_tools.modelo.dao;

/**
 * US-149: error al consultar los datos del turno (cierre_facturacion / facturas).
 * Un fallo de consulta NO debe tratarse como "no hay registro previo": si se
 * confunden, la apertura registra factura_inicial=1 y el cierre suma toda la
 * historia de la caja (incidente venecia, cierre 7175).
 */
public class ConsultaCierreException extends RuntimeException {

	public ConsultaCierreException(Throwable causa) {
		super(causa);
	}

	public ConsultaCierreException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
