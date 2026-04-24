package net.datatecsolution.admin_tools.modelo;

public final class RotacionCajas {

	public static final int CLIENTE_CONSUMIDOR_FINAL = 1;

	private RotacionCajas() {
	}

	public static boolean debeRotarPreGuardar(boolean rotacionAutomatica,
	                                          boolean rotacionManual,
	                                          int clienteId,
	                                          int bandera) {
		return rotacionAutomatica
				&& !rotacionManual
				&& clienteId == CLIENTE_CONSUMIDOR_FINAL
				&& bandera < 1;
	}

	public static int banderaPostGuardar(boolean rotacionAutomatica,
	                                     int clienteId,
	                                     int bandera) {
		if (!rotacionAutomatica) {
			return bandera;
		}
		if (clienteId != CLIENTE_CONSUMIDOR_FINAL) {
			return bandera;
		}
		if (bandera > 1) {
			return 0;
		}
		return bandera + 1;
	}
}
