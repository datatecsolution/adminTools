package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.Caja;

import java.util.List;

/**
 * US-128 — reglas de completitud y coherencia al dar de alta o editar un
 * usuario.
 *
 * Nace de un incidente real en Distribuidora Sharon (2026-08-01): el usuario
 * WACAESTRA se creo como Vendedor y la pantalla mostro "El Usuario se guardo
 * correctamente", pero quedo SIN CAJA. La API rechaza cada pedido suyo con
 * 409 ("El vendedor no tiene caja asignada") y el vendedor perdio dos dias
 * creyendo que guardaba.
 *
 * La causa era estructural, no un descuido: la pantalla tiene dos modos y
 * cada uno pide solo la mitad de lo que un vendedor necesita.
 *
 *   - Modo MOVIL      -> pide el empleado, pero NO ofrecia caja (WACAESTRA).
 *   - Modo ESCRITORIO -> ofrece cajas, pero fuerza codigo_empleado = 0, asi
 *                        que el vendedor queda sin empleado y ningun cajero
 *                        puede ver ni facturar sus pedidos (MYMEDDY).
 *
 * La caja no es un permiso mas: define la BODEGA de la que se descuenta el
 * inventario. Por eso cada rol tiene una regla distinta y ninguna es
 * cosmetica.
 *
 * @author jdmayorga
 */
public class ConfiguracionUsuarioService {

	/** {@code usuario.tipo_permiso}: 1=Supervisor, 2=Cajero, 3=Vendedor, 4=Admin. */
	public static final int TIPO_SUPERVISOR = 1;
	public static final int TIPO_CAJERO = 2;
	public static final int TIPO_VENDEDOR = 3;
	public static final int TIPO_ADMIN = 4;

	/**
	 * Valida que la configuracion de cajas y empleado sea coherente con el
	 * rol del usuario.
	 *
	 * @param tipoPermiso     rol del usuario que se esta guardando
	 * @param codigoEmpleado  empleado que representa (0 o negativo = ninguno)
	 * @param cajas           cajas asignadas en la pantalla
	 * @return {@code null} si la configuracion es correcta, o el mensaje a
	 *         mostrarle al operador
	 */
	public String validar(int tipoPermiso, int codigoEmpleado, List<Caja> cajas) {
		int cuantas = cajas == null ? 0 : cajas.size();

		if (tipoPermiso == TIPO_VENDEDOR) {
			return validarVendedor(codigoEmpleado, cuantas);
		}
		if (!admiteCaja(tipoPermiso)) {
			return validarSinCaja(tipoPermiso, cuantas);
		}
		return null;
	}

	/**
	 * Un vendedor necesita DOS cosas para operar, y ninguna es opcional:
	 *
	 *   1. Un empleado ({@code usuario.codigo_empleado}) — sin el, la orden se
	 *      graba sin vendedor y el cajero no la ve en su panel.
	 *   2. Exactamente UNA caja — sin ella la API responde 409 y no guarda
	 *      nada; con mas de una se viola US-110 (vendedor mono-caja) y la
	 *      bodega a la que descuenta el inventario queda indefinida.
	 */
	private String validarVendedor(int codigoEmpleado, int cuantasCajas) {
		if (codigoEmpleado <= 0) {
			return "Un usuario Vendedor debe tener un empleado asignado.\n"
					+ "Seleccione el tipo Movil y elija el vendedor en la lista.";
		}
		if (cuantasCajas == 0) {
			return "Un usuario Vendedor debe tener una caja asignada.\n"
					+ "Sin caja, la aplicacion de pedidos rechaza todo lo que intente guardar.";
		}
		if (cuantasCajas > 1) {
			return "Un usuario Vendedor solo puede tener UNA caja asignada (tiene " + cuantasCajas + ").\n"
					+ "La caja define la bodega de la que se descuenta el inventario.";
		}
		return null;
	}

	/**
	 * Supervisores y administradores NO facturan: supervisan la operacion y
	 * configuran el sistema. Asignarles caja no les habilita nada y ademas
	 * los mete en reportes y cierres de caja donde no corresponden.
	 *
	 * Verificado con los datos de Sharon antes de imponer la regla: de 15
	 * usuarios de estos dos roles, 6 tenian caja y NINGUNO la usaba — cero
	 * facturas emitidas entre todos, y el unico con pedidos (MARIOH) no
	 * registra actividad desde enero.
	 */
	private String validarSinCaja(int tipoPermiso, int cuantasCajas) {
		if (cuantasCajas == 0) {
			return null;
		}
		return "Un usuario " + nombreRol(tipoPermiso) + " no debe tener cajas asignadas"
				+ " (tiene " + cuantasCajas + ").\n"
				+ "Este rol no esta disenado para facturar; quite las cajas de la lista.";
	}

	/**
	 * ¿Este rol puede tener cajas?
	 *
	 * Cajero y Vendedor si (facturan o levantan pedidos). Supervisor y
	 * Administrador no.
	 */
	public boolean admiteCaja(int tipoPermiso) {
		return tipoPermiso == TIPO_CAJERO || tipoPermiso == TIPO_VENDEDOR;
	}

	/**
	 * ¿Hay que exigirle caja a este rol al guardarlo?
	 *
	 * Solo al vendedor: sin caja la API le rechaza los pedidos con 409. Al
	 * cajero no se le exige aca para no bloquear la edicion de cuentas
	 * existentes que hoy no la tienen.
	 */
	public boolean requiereCaja(int tipoPermiso) {
		return tipoPermiso == TIPO_VENDEDOR;
	}

	public String nombreRol(int tipoPermiso) {
		switch (tipoPermiso) {
		case TIPO_SUPERVISOR: return "Supervisor";
		case TIPO_CAJERO:     return "Cajero";
		case TIPO_VENDEDOR:   return "Vendedor";
		case TIPO_ADMIN:      return "Administrador";
		default:              return "desconocido";
		}
	}
}
