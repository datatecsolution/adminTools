package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import net.datatecsolution.admin_tools.modelo.MovimientoCartera;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * US-127 — reglas de la transferencia de cartera de clientes entre vendedores.
 *
 * Toda la logica que decide QUE se mueve vive aca, sin tocar la base de datos,
 * para que sea testeable (el repo no mockea JDBC). El DAO solo ejecuta el
 * UPDATE y el controlador solo arma la pantalla.
 *
 * Contexto del modelo: `cliente` tiene DOS asignaciones de empleado, separadas
 * por la migracion V9 — `id_vendedor` (comision de venta) e `id_cobrador`
 * (cartera de credito). V9 las separo porque pueden ser PERSONAS DISTINTAS,
 * asi que la transferencia trata cada rol como un {@link MovimientoCartera}
 * independiente, con su propio origen y su propio destino. Un cliente que
 * vende Ana y cobra Beto puede pasar su venta a Carlos y su cobranza a Dora
 * en la misma corrida, sin que un rol arrastre al otro.
 *
 * @author jdmayorga
 */
public class TransferenciaCarteraService {

	/** {@code usuario.tipo_permiso} del Administrador (1=Supervisor, 2=Cajero, 3=Vendedor, 4=Admin). */
	public static final int TIPO_ADMIN = 4;

	/**
	 * Unico lugar donde se decide quien puede transferir cartera: SOLO el
	 * administrador (tipo_permiso 4).
	 *
	 * El supervisor (1) queda afuera a proposito, aunque para otras pantallas
	 * de clientes se lo trate como equivalente al admin. Mover cartera
	 * reasigna comisiones de venta y responsabilidad de cobro de miles de
	 * clientes de una sola vez y no tiene deshacer.
	 *
	 * Lo usan el menu principal (para no abrir la pantalla) y el controlador
	 * de la pantalla (por si alguna vez se agrega otra via de entrada).
	 */
	public boolean puedeTransferir(Integer tipoPermiso) {
		return tipoPermiso != null && tipoPermiso == TIPO_ADMIN;
	}

	/**
	 * Valida los parametros de la transferencia.
	 *
	 * @return {@code null} si todo esta bien, o el mensaje a mostrarle al
	 *         usuario explicando por que no se puede continuar.
	 */
	public String validar(MovimientoCartera venta, MovimientoCartera cobro,
			List<ClienteCartera> seleccionados) {

		if (!venta.isActivo() && !cobro.isActivo()) {
			return "Marque al menos que transferir: la asignacion de venta, la cartera de cobro o ambas.";
		}

		String errorVenta = validarMovimiento(venta, "venta");
		if (errorVenta != null) {
			return errorVenta;
		}
		String errorCobro = validarMovimiento(cobro, "cobro");
		if (errorCobro != null) {
			return errorCobro;
		}

		if (seleccionados == null || seleccionados.isEmpty()) {
			return "Seleccione al menos un cliente para transferir.";
		}
		if (afectados(seleccionados, venta, cobro).isEmpty()) {
			return "Ninguno de los clientes seleccionados cambia con los movimientos indicados.\n"
					+ "Revise que los empleados de origen sean los que hoy tienen esos clientes.";
		}
		return null;
	}

	private String validarMovimiento(MovimientoCartera m, String rol) {
		if (!m.isActivo()) {
			return null;
		}
		if (m.getOrigen() <= 0) {
			return "Seleccione el empleado de origen de la " + rol + ".";
		}
		if (m.getDestino() <= 0) {
			return "Seleccione el empleado de destino de la " + rol + ".";
		}
		if (m.getOrigen() == m.getDestino()) {
			return "En la " + rol + ", el empleado de origen y el de destino son el mismo.";
		}
		return null;
	}

	/**
	 * Filtra los clientes que REALMENTE cambian con los movimientos indicados.
	 *
	 * La lista de la pantalla es la UNION de dos criterios distintos: los
	 * clientes que vende el origen de venta MAS los que cobra el origen de
	 * cobro. Un cliente puede entrar por uno solo de los dos, y en ese caso
	 * solo cambia ese rol.
	 */
	public List<ClienteCartera> afectados(List<ClienteCartera> seleccionados,
			MovimientoCartera venta, MovimientoCartera cobro) {

		List<ClienteCartera> resultado = new ArrayList<ClienteCartera>();
		if (seleccionados == null) {
			return resultado;
		}
		for (ClienteCartera c : seleccionados) {
			if (cambiaVenta(c, venta) || cambiaCobro(c, cobro)) {
				resultado.add(c);
			}
		}
		return resultado;
	}

	/** Los que cambian de vendedor de venta. Es la lista que va al UPDATE de id_vendedor. */
	public List<ClienteCartera> afectadosVenta(List<ClienteCartera> seleccionados, MovimientoCartera venta) {
		List<ClienteCartera> resultado = new ArrayList<ClienteCartera>();
		if (seleccionados == null) {
			return resultado;
		}
		for (ClienteCartera c : seleccionados) {
			if (cambiaVenta(c, venta)) {
				resultado.add(c);
			}
		}
		return resultado;
	}

	/** Los que cambian de cobrador. Es la lista que va al UPDATE de id_cobrador. */
	public List<ClienteCartera> afectadosCobro(List<ClienteCartera> seleccionados, MovimientoCartera cobro) {
		List<ClienteCartera> resultado = new ArrayList<ClienteCartera>();
		if (seleccionados == null) {
			return resultado;
		}
		for (ClienteCartera c : seleccionados) {
			if (cambiaCobro(c, cobro)) {
				resultado.add(c);
			}
		}
		return resultado;
	}

	private boolean cambiaVenta(ClienteCartera c, MovimientoCartera venta) {
		return venta.esUtilizable() && c.getIdVendedor() == venta.getOrigen();
	}

	private boolean cambiaCobro(ClienteCartera c, MovimientoCartera cobro) {
		return cobro.esUtilizable() && c.getIdCobrador() == cobro.getOrigen();
	}

	/** Suma de saldos de los clientes que se mueven; sirve para el aviso de confirmacion. */
	public BigDecimal saldoTotal(List<ClienteCartera> clientes) {
		BigDecimal total = BigDecimal.ZERO;
		if (clientes == null) {
			return total;
		}
		for (ClienteCartera c : clientes) {
			if (c.getSaldo() != null) {
				total = total.add(c.getSaldo());
			}
		}
		return total;
	}

	/**
	 * Texto de una linea con el estado de la seleccion, para el pie de la
	 * pantalla ("12 de 40 clientes seleccionados - 9 cambian").
	 */
	public String resumenSeleccion(List<ClienteCartera> todos, MovimientoCartera venta, MovimientoCartera cobro) {
		int total = todos == null ? 0 : todos.size();
		List<ClienteCartera> marcados = seleccionados(todos);
		int cambian = afectados(marcados, venta, cobro).size();
		return marcados.size() + " de " + total + " clientes seleccionados - " + cambian + " cambian";
	}

	/** Los que tienen la casilla marcada. */
	public List<ClienteCartera> seleccionados(List<ClienteCartera> todos) {
		List<ClienteCartera> resultado = new ArrayList<ClienteCartera>();
		if (todos == null) {
			return resultado;
		}
		for (ClienteCartera c : todos) {
			if (c.isSeleccionado()) {
				resultado.add(c);
			}
		}
		return resultado;
	}

	/** Codigos de cliente de una lista, listos para el DAO. */
	public List<Integer> codigos(List<ClienteCartera> clientes) {
		List<Integer> resultado = new ArrayList<Integer>();
		if (clientes == null) {
			return resultado;
		}
		for (ClienteCartera c : clientes) {
			resultado.add(c.getCodigo());
		}
		return resultado;
	}
}
