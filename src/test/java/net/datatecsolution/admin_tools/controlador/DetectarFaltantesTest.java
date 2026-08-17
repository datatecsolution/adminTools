package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * US-143 — deteccion de productos sin existencia al cargar un pedido de
 * vendedor.
 *
 * El caso real: el vendedor levanta el pedido a las 9:51, el producto se
 * agota durante la mañana y el cajero lo cobra a las 11:35. Antes eso se
 * descubria al guardar (la linea se rechazaba y la factura quedaba cobrando
 * de mas); ahora se detecta al cargar.
 */
public class DetectarFaltantesTest {

	/** Existencias simuladas: sin BD, sin UI. */
	private static CtlFacturarFrame.Disponibilidad stock(final Map<Integer, Double> m) {
		return new CtlFacturarFrame.Disponibilidad() {
			@Override
			public double de(int codigoArticulo) {
				Double v = m.get(codigoArticulo);
				return v == null ? 0 : v;
			}
		};
	}

	private static DetalleFactura linea(int idArticulo, String nombre, int tipo, double cantidad) {
		Articulo a = new Articulo();
		a.setId(idArticulo);
		a.setArticulo(nombre);
		a.setTipoArticulo(tipo);
		DetalleFactura d = new DetalleFactura();
		d.setListArticulos(a);
		d.setCantidad(new BigDecimal(cantidad));
		return d;
	}

	@Test
	public void productoAgotado_seDetecta() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(511, 0.0);   // el Alka Seltzer del caso real

		List<DetalleFactura> faltantes = CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(511, "CAJA ALKA SELTZER AZUL", 1, 1)), stock(m));

		assertEquals(1, faltantes.size());
		assertEquals(511, faltantes.get(0).getArticulo().getId());
	}

	@Test
	public void conExistenciaSuficiente_noSeDetecta() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(1620, 502.0);

		assertTrue(CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(1620, "JABON DOÑA BLANCA", 1, 6)), stock(m)).isEmpty());
	}

	/** Pedir exactamente lo que hay SI alcanza: la condicion es pedida > disponible. */
	@Test
	public void cantidadExacta_alcanza() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(77, 3.0);

		assertTrue(CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(77, "EXACTO", 1, 3)), stock(m)).isEmpty());
	}

	@Test
	public void parcial_pideMasDeLoQueHay_seDetecta() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(77, 2.0);

		assertEquals(1, CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(77, "PARCIAL", 1, 5)), stock(m)).size());
	}

	/** Los insumos no llevan kardex propio: no deben marcarse como faltantes. */
	@Test
	public void insumo_noSeControla() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(900, 0.0);

		assertTrue(CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(900, "SERVICIO DE FLETE", 2, 10)), stock(m)).isEmpty());
	}

	/** La fila en blanco de la tabla (id -1) no es un producto. */
	@Test
	public void filaEnBlanco_seIgnora() {
		assertTrue(CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(-1, "", 1, 1)), stock(new HashMap<Integer, Double>())).isEmpty());
	}

	/** El pedido real: 3 con stock y 1 agotado -> solo ese sale en la lista. */
	@Test
	public void pedidoMixto_soloDevuelveElQueFalta() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(511, 0.0);
		m.put(1620, 502.0);
		m.put(1263, 2899.0);
		m.put(2145, 353.0);

		List<DetalleFactura> faltantes = CtlFacturarFrame.detectarFaltantes(Arrays.asList(
				linea(511, "CAJA ALKA SELTZER AZUL", 1, 1),
				linea(1620, "JABON DOÑA BLANCA", 1, 6),
				linea(1263, "EGO PANA ATRACCION 200ML", 1, 3),
				linea(2145, "PAQ EGO RISTRA", 1, 1)), stock(m));

		assertEquals(1, faltantes.size());
		assertEquals("CAJA ALKA SELTZER AZUL", faltantes.get(0).getArticulo().getArticulo());
	}

	@Test
	public void listaNula_noRevienta() {
		assertTrue(CtlFacturarFrame.detectarFaltantes(null,
				stock(new HashMap<Integer, Double>())).isEmpty());
	}

	/**
	 * US-145 — el caso de Ronal: pedido 103614, articulo 1137, 40 pedidas con
	 * existencia 43 y NINGUNA otra reserva. La fuente de disponibilidad DEBE
	 * excluir la reserva del propio pedido (disponible=43): asi no hay
	 * faltante. El bug era que tipoView aun valia 1 al cargar, la orden no se
	 * excluia y el pedido se descontaba a si mismo (disponible=43-40=3).
	 */
	@Test
	public void pedidoPropio_unicaReserva_noSeBloqueaASiMismo() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(1137, 43.0);   // disponible CON la propia orden excluida

		assertTrue(CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(1137, "ART 1137", 1, 40)), stock(m)).isEmpty());
	}

	/**
	 * La contracara: si la fuente NO excluye la propia orden (el bug), el
	 * mismo pedido sale como faltante. Documenta la aritmetica exacta del
	 * fallo: se bloquea siempre que pedida > existencia/2.
	 */
	@Test
	public void pedidoPropio_siLaFuenteNoExcluye_seAutobloquea() {
		Map<Integer, Double> m = new HashMap<>();
		m.put(1137, 3.0);    // 43 fisicas - 40 de su propia reserva

		assertEquals(1, CtlFacturarFrame.detectarFaltantes(
				Arrays.asList(linea(1137, "ART 1137", 1, 40)), stock(m)).size());
	}

	/** Si NINGUNO tiene existencia, salen todos: el controlador usa eso para
	 *  no ofrecer "quitar" y dejar un pedido vacio (caso de la factura 36113). */
	@Test
	public void ningunoDisponible_devuelveTodos() {
		Map<Integer, Double> m = new HashMap<>();
		List<DetalleFactura> detalles = new ArrayList<>();
		detalles.add(linea(511, "A", 1, 1));
		detalles.add(linea(512, "B", 1, 2));

		assertEquals(2, CtlFacturarFrame.detectarFaltantes(detalles, stock(m)).size());
	}
}
