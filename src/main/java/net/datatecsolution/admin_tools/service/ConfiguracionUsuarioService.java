package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.Caja;

import java.util.List;

/**
 * US-128 — reglas de completitud al dar de alta o editar un usuario.
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
 *   - Modo MOVIL     -> pide el empleado, pero NO ofrece caja (WACAESTRA).
 *   - Modo ESCRITORIO -> ofrece cajas, pero fuerza codigo_empleado = 0, asi
 *                        que el vendedor queda sin empleado y ningun cajero
 *                        puede ver ni facturar sus pedidos (MYMEDDY).
 *
 * Por cualquiera de los dos caminos el vendedor nacia roto. Estas reglas
 * viven aca, sin Swing ni JDBC, para poder probarlas.
 *
 * @author jdmayorga
 */
public class ConfiguracionUsuarioService {

	/** {@code usuario.tipo_permiso}: 1=Supervisor, 2=Cajero, 3=Vendedor, 4=Admin. */
	public static final int TIPO_VENDEDOR = 3;

	/**
	 * Un vendedor necesita DOS cosas para operar, y ninguna es opcional:
	 *
	 *   1. Un empleado ({@code usuario.codigo_empleado}) — sin el, la orden se
	 *      graba sin vendedor y el cajero no la ve en su panel.
	 *   2. Exactamente UNA caja — sin ella la API responde 409 y no guarda
	 *      nada; con mas de una se viola US-110 (vendedor mono-caja) y la
	 *      bodega a la que descuenta el inventario queda indefinida.
	 *
	 * @param tipoPermiso     tipo del usuario que se esta guardando
	 * @param codigoEmpleado  empleado que representa (0 o negativo = ninguno)
	 * @param cajas           cajas asignadas en la pantalla
	 * @return {@code null} si la configuracion esta completa, o el mensaje a
	 *         mostrarle al operador explicando que falta
	 */
	public String validarVendedor(int tipoPermiso, int codigoEmpleado, List<Caja> cajas) {

		if (tipoPermiso != TIPO_VENDEDOR) {
			return null;
		}

		if (codigoEmpleado <= 0) {
			return "Un usuario Vendedor debe tener un empleado asignado.\n"
					+ "Seleccione el tipo Movil y elija el vendedor en la lista.";
		}

		int cuantas = cajas == null ? 0 : cajas.size();

		if (cuantas == 0) {
			return "Un usuario Vendedor debe tener una caja asignada.\n"
					+ "Sin caja, la aplicacion de pedidos rechaza todo lo que intente guardar.";
		}

		if (cuantas > 1) {
			return "Un usuario Vendedor solo puede tener UNA caja asignada (tiene " + cuantas + ").\n"
					+ "La caja define la bodega de la que se descuenta el inventario.";
		}

		return null;
	}

	/**
	 * ¿Hay que exigirle caja a este tipo de usuario al guardarlo?
	 *
	 * Solo al vendedor. Supervisores y administradores operan sin caja
	 * (en Sharon hay diez asi, trabajando sin problema) y exigirsela
	 * bloquearia editarlos.
	 */
	public boolean requiereCaja(int tipoPermiso) {
		return tipoPermiso == TIPO_VENDEDOR;
	}
}
