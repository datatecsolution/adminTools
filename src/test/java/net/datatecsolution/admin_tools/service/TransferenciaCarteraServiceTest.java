package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * US-127 — reglas de la transferencia de cartera entre vendedores.
 *
 * El caso que justifica esta clase: `cliente` tiene DOS asignaciones de
 * empleado (id_vendedor e id_cobrador, separadas en V9) y la pantalla lista
 * los clientes donde el origen figure en CUALQUIERA de las dos. Si el usuario
 * destilda uno de los roles, los clientes que solo figuran en el rol
 * destildado NO deben moverse.
 */
public class TransferenciaCarteraServiceTest {

	private static final int ORIGEN = 10;
	private static final int DESTINO = 20;
	private static final int TERCERO = 30;

	private TransferenciaCarteraService servicio;

	@Before
	public void setUp() {
		servicio = new TransferenciaCarteraService();
	}

	private ClienteCartera cliente(int codigo, int idVendedor, int idCobrador, String saldo) {
		ClienteCartera c = new ClienteCartera();
		c.setCodigo(codigo);
		c.setNombre("Cliente " + codigo);
		c.setIdVendedor(idVendedor);
		c.setIdCobrador(idCobrador);
		c.setSaldo(new BigDecimal(saldo));
		return c;
	}

	/* ---------------------- afectados() ---------------------- */

	@Test
	public void moverSoloCobroNoTocaAlQueSoloEsDeVenta() {
		// El origen vende pero NO cobra: el cobrador es un tercero.
		ClienteCartera soloVenta = cliente(1, ORIGEN, TERCERO, "0");

		List<ClienteCartera> resultado = servicio.afectados(
				Arrays.asList(soloVenta), ORIGEN, false, true);

		assertTrue("un cliente que el origen no cobra no debe moverse al transferir solo la cobranza",
				resultado.isEmpty());
	}

	@Test
	public void moverSoloVentaNoTocaAlQueSoloEsDeCobro() {
		ClienteCartera soloCobro = cliente(2, TERCERO, ORIGEN, "0");

		List<ClienteCartera> resultado = servicio.afectados(
				Arrays.asList(soloCobro), ORIGEN, true, false);

		assertTrue(resultado.isEmpty());
	}

	@Test
	public void moverAmbosRolesTomaAlQueFiguraEnCualquiera() {
		ClienteCartera soloVenta = cliente(1, ORIGEN, TERCERO, "0");
		ClienteCartera soloCobro = cliente(2, TERCERO, ORIGEN, "0");
		ClienteCartera ambos = cliente(3, ORIGEN, ORIGEN, "0");

		List<ClienteCartera> resultado = servicio.afectados(
				Arrays.asList(soloVenta, soloCobro, ambos), ORIGEN, true, true);

		assertEquals(3, resultado.size());
	}

	@Test
	public void clienteDeUnTerceroNuncaSeMueve() {
		ClienteCartera ajeno = cliente(4, TERCERO, TERCERO, "0");

		assertTrue(servicio.afectados(Arrays.asList(ajeno), ORIGEN, true, true).isEmpty());
	}

	@Test
	public void afectadosToleraListaNula() {
		assertTrue(servicio.afectados(null, ORIGEN, true, true).isEmpty());
	}

	@Test
	public void atajosDeRolCoincidenConAfectados() {
		List<ClienteCartera> cartera = Arrays.asList(
				cliente(1, ORIGEN, TERCERO, "0"),
				cliente(2, TERCERO, ORIGEN, "0"));

		assertEquals(1, servicio.afectadosVenta(cartera, ORIGEN).size());
		assertEquals(1, servicio.afectadosCobro(cartera, ORIGEN).size());
	}

	/* ---------------------- validar() ---------------------- */

	@Test
	public void validaCasoCorrecto() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, ORIGEN, "0"));

		assertNull(servicio.validar(ORIGEN, DESTINO, true, true, cartera));
	}

	@Test
	public void rechazaOrigenSinSeleccionar() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, ORIGEN, "0"));

		assertNotNull(servicio.validar(0, DESTINO, true, true, cartera));
	}

	@Test
	public void rechazaDestinoSinSeleccionar() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, ORIGEN, "0"));

		assertNotNull(servicio.validar(ORIGEN, 0, true, true, cartera));
	}

	@Test
	public void rechazaOrigenIgualADestino() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, ORIGEN, "0"));

		assertNotNull(servicio.validar(ORIGEN, ORIGEN, true, true, cartera));
	}

	@Test
	public void rechazaSinNingunRolMarcado() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, ORIGEN, "0"));

		assertNotNull(servicio.validar(ORIGEN, DESTINO, false, false, cartera));
	}

	@Test
	public void rechazaSeleccionVacia() {
		assertNotNull(servicio.validar(ORIGEN, DESTINO, true, true, new ArrayList<ClienteCartera>()));
		assertNotNull(servicio.validar(ORIGEN, DESTINO, true, true, null));
	}

	@Test
	public void rechazaCuandoNingunSeleccionadoCambia() {
		// Solo es vendedor de ese cliente, pero se pidio mover unicamente la cobranza.
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ORIGEN, TERCERO, "0"));

		assertNotNull("no debe dejar disparar un UPDATE que no cambia nada",
				servicio.validar(ORIGEN, DESTINO, false, true, cartera));
	}

	/* ---------------------- helpers de pantalla ---------------------- */

	@Test
	public void sumaSaldosDeLosAfectados() {
		List<ClienteCartera> cartera = Arrays.asList(
				cliente(1, ORIGEN, ORIGEN, "150.50"),
				cliente(2, ORIGEN, ORIGEN, "49.50"));

		assertEquals(new BigDecimal("200.00"), servicio.saldoTotal(cartera));
	}

	@Test
	public void saldoTotalToleraNulos() {
		assertEquals(BigDecimal.ZERO, servicio.saldoTotal(null));

		ClienteCartera sinSaldo = cliente(1, ORIGEN, ORIGEN, "0");
		sinSaldo.setSaldo(null);
		assertEquals(BigDecimal.ZERO, servicio.saldoTotal(Arrays.asList(sinSaldo)));
	}

	@Test
	public void seleccionadosFiltraPorLaCasilla() {
		ClienteCartera marcado = cliente(1, ORIGEN, ORIGEN, "0");
		ClienteCartera desmarcado = cliente(2, ORIGEN, ORIGEN, "0");
		desmarcado.setSeleccionado(false);

		List<ClienteCartera> resultado = servicio.seleccionados(Arrays.asList(marcado, desmarcado));

		assertEquals(1, resultado.size());
		assertEquals(1, resultado.get(0).getCodigo());
	}

	@Test
	public void clientesNacenSeleccionados() {
		assertTrue("la pantalla ofrece transferir toda la cartera por defecto",
				cliente(1, ORIGEN, ORIGEN, "0").isSeleccionado());
	}

	@Test
	public void codigosExtraeLosIdParaElDao() {
		List<Integer> codigos = servicio.codigos(Arrays.asList(
				cliente(7, ORIGEN, ORIGEN, "0"),
				cliente(9, ORIGEN, ORIGEN, "0")));

		assertEquals(Arrays.asList(7, 9), codigos);
	}

	@Test
	public void resumenDistingueSeleccionadosDeLosQueCambian() {
		// 3 en la cartera, 2 marcados, pero solo 1 cambia porque se mueve solo la cobranza.
		ClienteCartera cobraElOrigen = cliente(1, TERCERO, ORIGEN, "0");
		ClienteCartera soloVende = cliente(2, ORIGEN, TERCERO, "0");
		ClienteCartera desmarcado = cliente(3, ORIGEN, ORIGEN, "0");
		desmarcado.setSeleccionado(false);

		String resumen = servicio.resumenSeleccion(
				Arrays.asList(cobraElOrigen, soloVende, desmarcado), ORIGEN, false, true);

		assertEquals("2 de 3 clientes seleccionados - 1 cambian", resumen);
	}

	@Test
	public void describeLosRolesQueSeMueven() {
		assertEquals("la asignacion de venta y la cartera de cobro", servicio.descripcionRoles(true, true));
		assertEquals("la asignacion de venta", servicio.descripcionRoles(true, false));
		assertEquals("la cartera de cobro", servicio.descripcionRoles(false, true));
	}

	/* ---------------------- columna "Rol actual" ---------------------- */

	@Test
	public void rolRespectoAlEmpleadoConsultado() {
		assertEquals("Venta y cobro", cliente(1, ORIGEN, ORIGEN, "0").rolRespectoA(ORIGEN));
		assertEquals("Venta", cliente(2, ORIGEN, TERCERO, "0").rolRespectoA(ORIGEN));
		assertEquals("Cobro", cliente(3, TERCERO, ORIGEN, "0").rolRespectoA(ORIGEN));
		assertEquals("", cliente(4, TERCERO, TERCERO, "0").rolRespectoA(ORIGEN));
	}
}
