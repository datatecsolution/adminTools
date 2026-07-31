package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;

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
 * (cartera de credito). La transferencia puede mover una, la otra o las dos, y
 * un cliente puede figurar con el vendedor origen en un solo rol.
 *
 * @author jdmayorga
 */
public class TransferenciaCarteraService {

	/**
	 * Valida los parametros de la transferencia.
	 *
	 * @return {@code null} si todo esta bien, o el mensaje a mostrarle al
	 *         usuario explicando por que no se puede continuar.
	 */
	public String validar(int origen, int destino, boolean moverVenta, boolean moverCobro,
			List<ClienteCartera> seleccionados) {

		if (origen <= 0) {
			return "Seleccione el vendedor de origen.";
		}
		if (destino <= 0) {
			return "Seleccione el vendedor de destino.";
		}
		if (origen == destino) {
			return "El vendedor de origen y el de destino son el mismo.";
		}
		if (!moverVenta && !moverCobro) {
			return "Marque al menos que transferir: la asignacion de venta, la cartera de cobro o ambas.";
		}
		if (seleccionados == null || seleccionados.isEmpty()) {
			return "Seleccione al menos un cliente para transferir.";
		}
		if (afectados(seleccionados, origen, moverVenta, moverCobro).isEmpty()) {
			return "Ninguno de los clientes seleccionados cambia con las opciones marcadas.\n"
					+ "Revise si el rol que marco (venta o cobro) corresponde al vendedor de origen.";
		}
		return null;
	}

	/**
	 * Filtra los clientes que REALMENTE cambian.
	 *
	 * La lista de la pantalla trae todo cliente donde el origen figure como
	 * vendedor O como cobrador. Si el usuario destilda uno de los dos roles,
	 * los clientes que solo figuran en el rol destildado quedan intactos: no
	 * tiene sentido contarlos en el resumen ni mandarlos al UPDATE.
	 */
	public List<ClienteCartera> afectados(List<ClienteCartera> seleccionados, int origen,
			boolean moverVenta, boolean moverCobro) {

		List<ClienteCartera> resultado = new ArrayList<ClienteCartera>();
		if (seleccionados == null) {
			return resultado;
		}
		for (ClienteCartera c : seleccionados) {
			boolean cambiaVenta = moverVenta && c.getIdVendedor() == origen;
			boolean cambiaCobro = moverCobro && c.getIdCobrador() == origen;
			if (cambiaVenta || cambiaCobro) {
				resultado.add(c);
			}
		}
		return resultado;
	}

	/** Solo los que cambian de vendedor de venta. */
	public List<ClienteCartera> afectadosVenta(List<ClienteCartera> seleccionados, int origen) {
		return afectados(seleccionados, origen, true, false);
	}

	/** Solo los que cambian de cobrador. */
	public List<ClienteCartera> afectadosCobro(List<ClienteCartera> seleccionados, int origen) {
		return afectados(seleccionados, origen, false, true);
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
	 * Arma la descripcion de que se va a mover, para el dialogo de confirmacion.
	 */
	public String descripcionRoles(boolean moverVenta, boolean moverCobro) {
		if (moverVenta && moverCobro) return "la asignacion de venta y la cartera de cobro";
		if (moverVenta) return "la asignacion de venta";
		if (moverCobro) return "la cartera de cobro";
		return "nada";
	}

	/**
	 * Texto de una linea con el estado de la seleccion, para el pie de la
	 * pantalla ("12 de 40 clientes seleccionados - 9 cambian").
	 */
	public String resumenSeleccion(List<ClienteCartera> todos, int origen, boolean moverVenta, boolean moverCobro) {
		int total = todos == null ? 0 : todos.size();
		List<ClienteCartera> marcados = seleccionados(todos);
		int cambian = afectados(marcados, origen, moverVenta, moverCobro).size();
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
