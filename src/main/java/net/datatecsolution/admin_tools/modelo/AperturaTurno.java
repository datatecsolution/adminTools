package net.datatecsolution.admin_tools.modelo;

/**
 * US-149: reglas puras para el rango de facturas de un turno.
 */
public final class AperturaTurno {

	private AperturaTurno() {
	}

	/**
	 * true si la fila del turno anterior trae un numero final valido para
	 * continuar la numeracion (final > 0; 0 o -1 son filas sin completar).
	 */
	public static boolean tieneFinalUsable(CierreFacturacion previa) {
		return previa != null
				&& previa.getNoFacturaFinal() != null
				&& previa.getNoFacturaFinal() > 0;
	}

	/**
	 * Numero de factura inicial para la apertura: continua el final del turno
	 * anterior; sin turno anterior usable, continua la ultima factura emitida
	 * por el usuario en la caja; 1 solo cuando no existe ninguna de las dos
	 * (primera vez real).
	 */
	public static int calcularFacturaInicial(CierreFacturacion previa, Factura ultimaFactura) {
		if (tieneFinalUsable(previa)) {
			return previa.getNoFacturaFinal() + 1;
		}
		if (ultimaFactura != null && ultimaFactura.getIdFactura() != null
				&& ultimaFactura.getIdFactura() > 0) {
			return ultimaFactura.getIdFactura() + 1;
		}
		return 1;
	}

	/**
	 * true si el rango registrado en la apertura quedo envenenado: dice
	 * "primera vez" (inicial <= 1) pero el usuario ya tiene un turno anterior
	 * cerrado en esa caja. Cerrar con ese rango sumaria toda la historia.
	 */
	public static boolean esRangoEnvenenado(Integer facturaInicialRegistrada, Integer finalTurnoAnterior) {
		return facturaInicialRegistrada != null && facturaInicialRegistrada <= 1
				&& finalTurnoAnterior != null && finalTurnoAnterior > 0;
	}
}
