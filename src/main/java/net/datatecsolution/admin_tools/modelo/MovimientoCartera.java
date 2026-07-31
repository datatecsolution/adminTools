package net.datatecsolution.admin_tools.modelo;

/**
 * US-127 — un movimiento de cartera para UN rol: de que empleado sale y a
 * cual entra.
 *
 * La transferencia maneja dos de estos, uno para la asignacion de venta
 * ({@code cliente.id_vendedor}) y otro para la cartera de cobro
 * ({@code id_cobrador}), completamente independientes entre si. La migracion
 * V9 separo los dos roles justamente porque pueden ser personas distintas:
 * un cliente puede venderlo Ana y cobrarlo Beto, y mover uno no debe arrastrar
 * al otro.
 *
 * @author jdmayorga
 */
public class MovimientoCartera {

	private final boolean activo;
	private final int origen;
	private final int destino;

	public MovimientoCartera(boolean activo, int origen, int destino) {
		this.activo = activo;
		this.origen = origen;
		this.destino = destino;
	}

	/** Movimiento apagado; util para "no tocar este rol". */
	public static MovimientoCartera inactivo() {
		return new MovimientoCartera(false, 0, 0);
	}

	public boolean isActivo() { return activo; }

	public int getOrigen() { return origen; }

	public int getDestino() { return destino; }

	/**
	 * Codigo de empleado por el que hay que buscar clientes de este rol, o 0
	 * si el rol esta apagado.
	 *
	 * Se usa como criterio de carga y como valor de la columna "Rol actual".
	 * Los codigos de empleado son AUTO_INCREMENT desde 1, asi que 0 nunca
	 * coincide con nadie.
	 */
	public int origenBuscable() {
		return activo ? origen : 0;
	}

	/** Un movimiento activo necesita origen, destino y que no sean el mismo. */
	public boolean esUtilizable() {
		return activo && origen > 0 && destino > 0 && origen != destino;
	}

	@Override
	public String toString() {
		return activo ? (origen + " -> " + destino) : "(inactivo)";
	}
}
