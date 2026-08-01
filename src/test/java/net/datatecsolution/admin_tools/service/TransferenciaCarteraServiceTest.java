package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import net.datatecsolution.admin_tools.modelo.MovimientoCartera;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * US-127 — reglas de la transferencia de cartera entre empleados.
 *
 * Lo que justifica esta clase: `cliente` tiene DOS asignaciones de empleado
 * (id_vendedor e id_cobrador, separadas en V9) y V9 las separo PORQUE PUEDEN
 * SER PERSONAS DISTINTAS. La transferencia trata cada rol como un movimiento
 * independiente, con su propio origen y su propio destino: mover la venta de
 * Ana no debe arrastrar la cobranza de Beto.
 */
public class TransferenciaCarteraServiceTest {

	// Ana vende, Beto cobra; Carlos y Dora reciben. Eva es ajena a todo.
	private static final int ANA = 10;
	private static final int BETO = 20;
	private static final int CARLOS = 30;
	private static final int DORA = 40;
	private static final int EVA = 50;

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

	private MovimientoCartera mov(int origen, int destino) {
		return new MovimientoCartera(true, origen, destino);
	}

	/* ============ control de acceso: SOLO administrador ============ */

	@Test
	public void soloElAdministradorPuedeTransferir() {
		assertTrue("4 = Administrador", servicio.puedeTransferir(4));

		assertFalse("1 = Supervisor: queda afuera aunque otras pantallas de clientes lo dejen pasar",
				servicio.puedeTransferir(1));
		assertFalse("2 = Cajero", servicio.puedeTransferir(2));
		assertFalse("3 = Vendedor", servicio.puedeTransferir(3));
	}

	@Test
	public void permisoDesconocidoONuloNoPasa() {
		assertFalse(servicio.puedeTransferir(null));
		assertFalse(servicio.puedeTransferir(0));
		assertFalse(servicio.puedeTransferir(5));
		assertFalse(servicio.puedeTransferir(-1));
	}

	@Test
	public void puedeTransferirComparaPORVALORNoPorReferencia() {
		// Integer fuera del cache de -128..127: un == entre objetos daria false
		// y dejaria afuera al admin. Cubre la regresion si alguien cambia la
		// firma o el desempaquetado.
		assertTrue(servicio.puedeTransferir(Integer.valueOf(TransferenciaCarteraService.TIPO_ADMIN)));
		assertEquals(4, TransferenciaCarteraService.TIPO_ADMIN);
	}

	/* ============ el caso que motivo el rediseño: dos personas distintas ============ */

	@Test
	public void ventaYCobroSeMuevenAPersonasDISTINTASEnLaMismaCorrida() {
		// Ana vende este cliente, Beto lo cobra.
		ClienteCartera c = cliente(1, ANA, BETO, "500");

		MovimientoCartera venta = mov(ANA, CARLOS);   // la venta de Ana pasa a Carlos
		MovimientoCartera cobro = mov(BETO, DORA);    // la cobranza de Beto pasa a Dora

		assertEquals("el cliente cambia por los dos roles, pero se cuenta una vez",
				1, servicio.afectados(Arrays.asList(c), venta, cobro).size());
		assertEquals(1, servicio.afectadosVenta(Arrays.asList(c), venta).size());
		assertEquals(1, servicio.afectadosCobro(Arrays.asList(c), cobro).size());
	}

	@Test
	public void moverLaVentaDeAnaNoArrastraLaCobranzaDeBeto() {
		ClienteCartera c = cliente(1, ANA, BETO, "0");

		MovimientoCartera venta = mov(ANA, CARLOS);
		MovimientoCartera cobro = MovimientoCartera.inactivo();

		assertEquals(1, servicio.afectadosVenta(Arrays.asList(c), venta).size());
		assertTrue("la cobranza de Beto no se toca",
				servicio.afectadosCobro(Arrays.asList(c), cobro).isEmpty());
	}

	@Test
	public void moverLaCobranzaDeBetoNoArrastraLaVentaDeAna() {
		ClienteCartera c = cliente(1, ANA, BETO, "0");

		MovimientoCartera venta = MovimientoCartera.inactivo();
		MovimientoCartera cobro = mov(BETO, DORA);

		assertTrue(servicio.afectadosVenta(Arrays.asList(c), venta).isEmpty());
		assertEquals(1, servicio.afectadosCobro(Arrays.asList(c), cobro).size());
	}

	@Test
	public void cadaRolFiltraPorSuPROPIOOrigen() {
		ClienteCartera soloAnaVende = cliente(1, ANA, EVA, "0");   // Eva cobra, no Beto
		ClienteCartera soloBetoCobra = cliente(2, EVA, BETO, "0"); // Eva vende, no Ana
		ClienteCartera ambos = cliente(3, ANA, BETO, "0");
		ClienteCartera ajeno = cliente(4, EVA, EVA, "0");

		List<ClienteCartera> cartera = Arrays.asList(soloAnaVende, soloBetoCobra, ambos, ajeno);
		MovimientoCartera venta = mov(ANA, CARLOS);
		MovimientoCartera cobro = mov(BETO, DORA);

		assertEquals("solo los que vende Ana", 2, servicio.afectadosVenta(cartera, venta).size());
		assertEquals("solo los que cobra Beto", 2, servicio.afectadosCobro(cartera, cobro).size());
		assertEquals("la union, sin duplicar el que entra por ambos",
				3, servicio.afectados(cartera, venta, cobro).size());
	}

	@Test
	public void unRolPuedeIrAlEmpleadoQueEsOrigenDelOtro() {
		// Valido: la venta de Ana pasa a Carlos y la cobranza de Beto pasa a Ana.
		ClienteCartera c = cliente(1, ANA, BETO, "0");

		MovimientoCartera venta = mov(ANA, CARLOS);
		MovimientoCartera cobro = mov(BETO, ANA);

		assertNull(servicio.validar(venta, cobro, Arrays.asList(c)));
		assertEquals(1, servicio.afectados(Arrays.asList(c), venta, cobro).size());
	}

	@Test
	public void elMismoEmpleadoEnLosDosRolesSigueFuncionando() {
		// Caso simple: Ana se va y todo lo suyo pasa a Carlos.
		ClienteCartera c = cliente(1, ANA, ANA, "0");

		MovimientoCartera venta = mov(ANA, CARLOS);
		MovimientoCartera cobro = mov(ANA, CARLOS);

		assertEquals(1, servicio.afectadosVenta(Arrays.asList(c), venta).size());
		assertEquals(1, servicio.afectadosCobro(Arrays.asList(c), cobro).size());
		assertEquals(1, servicio.afectados(Arrays.asList(c), venta, cobro).size());
	}

	@Test
	public void clienteDeUnTerceroNuncaSeMueve() {
		ClienteCartera ajeno = cliente(1, EVA, EVA, "0");

		assertTrue(servicio.afectados(Arrays.asList(ajeno), mov(ANA, CARLOS), mov(BETO, DORA)).isEmpty());
	}

	@Test
	public void afectadosToleraListaNula() {
		assertTrue(servicio.afectados(null, mov(ANA, CARLOS), mov(BETO, DORA)).isEmpty());
		assertTrue(servicio.afectadosVenta(null, mov(ANA, CARLOS)).isEmpty());
		assertTrue(servicio.afectadosCobro(null, mov(BETO, DORA)).isEmpty());
	}

	/* ============ transferir UN SOLO rol (casilla del otro destildada) ============ */

	@Test
	public void soloVentaDejaAlCobradorDONDEESTA() {
		// El escenario mas comun: se reasigna la fuerza de ventas y la
		// cobranza no se toca. Incluye el caso en que el MISMO empleado tiene
		// los dos roles: la venta se va, la cobranza se queda.
		ClienteCartera cobraOtro = cliente(1, ANA, BETO, "0");
		ClienteCartera cobraLaMisma = cliente(2, ANA, ANA, "0");
		ClienteCartera contadoSinCobrador = cliente(3, ANA, 0, "0");

		List<ClienteCartera> cartera = Arrays.asList(cobraOtro, cobraLaMisma, contadoSinCobrador);
		MovimientoCartera venta = mov(ANA, CARLOS);
		MovimientoCartera cobro = MovimientoCartera.inactivo();

		assertNull("debe dejar transferir con un solo rol", servicio.validar(venta, cobro, cartera));
		assertEquals("los tres cambian de vendedor", 3, servicio.afectadosVenta(cartera, venta).size());
		assertTrue("NINGUN cobrador se toca, ni siquiera el de Ana",
				servicio.afectadosCobro(cartera, cobro).isEmpty());
		assertEquals(3, servicio.afectados(cartera, venta, cobro).size());
	}

	@Test
	public void soloCobroDejaAlVendedorDONDEESTA() {
		ClienteCartera vendeOtro = cliente(1, ANA, BETO, "0");
		ClienteCartera vendeElMismo = cliente(2, BETO, BETO, "0");

		List<ClienteCartera> cartera = Arrays.asList(vendeOtro, vendeElMismo);
		MovimientoCartera venta = MovimientoCartera.inactivo();
		MovimientoCartera cobro = mov(BETO, DORA);

		assertNull(servicio.validar(venta, cobro, cartera));
		assertEquals(2, servicio.afectadosCobro(cartera, cobro).size());
		assertTrue("ningun vendedor se toca", servicio.afectadosVenta(cartera, venta).isEmpty());
	}

	@Test
	public void conUnRolApagadoElOtroNoNecesitaSusCombos() {
		// La casilla destildada puede dejar basura en sus combos: no bloquea.
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		assertNull(servicio.validar(mov(ANA, CARLOS), new MovimientoCartera(false, 999, 999), cartera));
		assertNull(servicio.validar(new MovimientoCartera(false, 999, 999), mov(BETO, DORA), cartera));
	}

	@Test
	public void elRolApagadoNoAportaClientesALaLista() {
		// Un cliente que SOLO entra por el rol apagado no debe contarse.
		ClienteCartera soloLoCobraBeto = cliente(1, EVA, BETO, "0");

		assertTrue(servicio.afectados(Arrays.asList(soloLoCobraBeto),
				mov(ANA, CARLOS), MovimientoCartera.inactivo()).isEmpty());
	}

	/* ============ mensajes de validacion ============ */

	@Test
	public void losMensajesNombranElRolCorrectamente() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		String faltaDestinoVenta = servicio.validar(
				new MovimientoCartera(true, ANA, 0), MovimientoCartera.inactivo(), cartera);
		String faltaDestinoCobro = servicio.validar(
				MovimientoCartera.inactivo(), new MovimientoCartera(true, BETO, 0), cartera);

		assertTrue("debe decir de que rol habla: " + faltaDestinoVenta,
				faltaDestinoVenta.contains(TransferenciaCarteraService.ROL_VENTA));
		assertTrue("debe decir de que rol habla: " + faltaDestinoCobro,
				faltaDestinoCobro.contains(TransferenciaCarteraService.ROL_COBRO));

		// Concatenar "la " + "cobro" daba "de la cobro".
		assertFalse(faltaDestinoCobro.contains("la cobro"));
		assertFalse(faltaDestinoVenta.contains("la venta."));
	}

	/* ============ MovimientoCartera ============ */

	@Test
	public void movimientoInactivoNoEsUtilizableNiBuscable() {
		MovimientoCartera m = MovimientoCartera.inactivo();

		assertFalse(m.isActivo());
		assertFalse(m.esUtilizable());
		assertEquals("0 nunca coincide con un codigo de empleado", 0, m.origenBuscable());
	}

	@Test
	public void movimientoActivoSinDestinoNoEsUtilizable() {
		assertFalse(new MovimientoCartera(true, ANA, 0).esUtilizable());
		assertFalse(new MovimientoCartera(true, 0, CARLOS).esUtilizable());
		assertFalse("origen igual a destino no mueve nada",
				new MovimientoCartera(true, ANA, ANA).esUtilizable());
		assertTrue(new MovimientoCartera(true, ANA, CARLOS).esUtilizable());
	}

	@Test
	public void origenBuscableSeApagaConElCheckbox() {
		assertEquals(ANA, new MovimientoCartera(true, ANA, CARLOS).origenBuscable());
		assertEquals(0, new MovimientoCartera(false, ANA, CARLOS).origenBuscable());
	}

	/* ============ validar() ============ */

	@Test
	public void validaCasoCorrecto() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		assertNull(servicio.validar(mov(ANA, CARLOS), mov(BETO, DORA), cartera));
	}

	@Test
	public void rechazaSinNingunRolMarcado() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		assertNotNull(servicio.validar(MovimientoCartera.inactivo(), MovimientoCartera.inactivo(), cartera));
	}

	@Test
	public void cadaRolSeValidaPORSEPARADO() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		// La venta esta mal (sin destino) aunque el cobro este perfecto.
		assertNotNull(servicio.validar(new MovimientoCartera(true, ANA, 0), mov(BETO, DORA), cartera));
		// Y al reves.
		assertNotNull(servicio.validar(mov(ANA, CARLOS), new MovimientoCartera(true, BETO, 0), cartera));
	}

	@Test
	public void rechazaOrigenIgualADestinoEnCualquierRol() {
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		assertNotNull(servicio.validar(mov(ANA, ANA), mov(BETO, DORA), cartera));
		assertNotNull(servicio.validar(mov(ANA, CARLOS), mov(BETO, BETO), cartera));
	}

	@Test
	public void unRolApagadoNoSeValida() {
		// El cobro esta apagado con datos basura: no debe bloquear.
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, ANA, BETO, "0"));

		assertNull(servicio.validar(mov(ANA, CARLOS), new MovimientoCartera(false, 0, 0), cartera));
	}

	@Test
	public void rechazaSeleccionVacia() {
		assertNotNull(servicio.validar(mov(ANA, CARLOS), mov(BETO, DORA), new ArrayList<ClienteCartera>()));
		assertNotNull(servicio.validar(mov(ANA, CARLOS), mov(BETO, DORA), null));
	}

	@Test
	public void rechazaCuandoNingunSeleccionadoCambia() {
		// Los origenes no tienen nada que ver con este cliente.
		List<ClienteCartera> cartera = Arrays.asList(cliente(1, EVA, EVA, "0"));

		assertNotNull("no debe dejar disparar un UPDATE que no cambia nada",
				servicio.validar(mov(ANA, CARLOS), mov(BETO, DORA), cartera));
	}

	/* ============ helpers de pantalla ============ */

	@Test
	public void sumaSaldosDeLosAfectados() {
		List<ClienteCartera> cartera = Arrays.asList(
				cliente(1, ANA, ANA, "150.50"),
				cliente(2, ANA, ANA, "49.50"));

		assertEquals(new BigDecimal("200.00"), servicio.saldoTotal(cartera));
	}

	@Test
	public void saldoTotalToleraNulos() {
		assertEquals(BigDecimal.ZERO, servicio.saldoTotal(null));

		ClienteCartera sinSaldo = cliente(1, ANA, ANA, "0");
		sinSaldo.setSaldo(null);
		assertEquals(BigDecimal.ZERO, servicio.saldoTotal(Arrays.asList(sinSaldo)));
	}

	@Test
	public void seleccionadosFiltraPorLaCasilla() {
		ClienteCartera marcado = cliente(1, ANA, ANA, "0");
		ClienteCartera desmarcado = cliente(2, ANA, ANA, "0");
		desmarcado.setSeleccionado(false);

		List<ClienteCartera> resultado = servicio.seleccionados(Arrays.asList(marcado, desmarcado));

		assertEquals(1, resultado.size());
		assertEquals(1, resultado.get(0).getCodigo());
	}

	@Test
	public void clientesNacenSeleccionados() {
		assertTrue("la pantalla ofrece transferir toda la cartera por defecto",
				cliente(1, ANA, ANA, "0").isSeleccionado());
	}

	@Test
	public void codigosExtraeLosIdParaElDao() {
		List<Integer> codigos = servicio.codigos(Arrays.asList(
				cliente(7, ANA, ANA, "0"),
				cliente(9, ANA, ANA, "0")));

		assertEquals(Arrays.asList(7, 9), codigos);
	}

	@Test
	public void resumenDistingueSeleccionadosDeLosQueCambian() {
		// 3 en la lista, 2 marcados, pero solo 1 cambia (el otro es de Eva).
		ClienteCartera deAna = cliente(1, ANA, EVA, "0");
		ClienteCartera deEva = cliente(2, EVA, EVA, "0");
		ClienteCartera desmarcado = cliente(3, ANA, BETO, "0");
		desmarcado.setSeleccionado(false);

		String resumen = servicio.resumenSeleccion(
				Arrays.asList(deAna, deEva, desmarcado), mov(ANA, CARLOS), mov(BETO, DORA));

		assertEquals("2 de 3 clientes seleccionados - 1 cambian", resumen);
	}

	/* ============ columna "Rol actual" ============ */

	@Test
	public void rolIndicaPorQueCriterioEntroElCliente() {
		assertEquals("Venta y cobro", cliente(1, ANA, BETO, "0").rolEn(ANA, BETO));
		assertEquals("Venta", cliente(2, ANA, EVA, "0").rolEn(ANA, BETO));
		assertEquals("Cobro", cliente(3, EVA, BETO, "0").rolEn(ANA, BETO));
		assertEquals("", cliente(4, EVA, EVA, "0").rolEn(ANA, BETO));
	}

	@Test
	public void rolIgnoraElRolApagado() {
		ClienteCartera c = cliente(1, ANA, BETO, "0");

		assertEquals("con el cobro apagado solo debe reportar la venta", "Venta", c.rolEn(ANA, 0));
		assertEquals("con la venta apagada solo debe reportar el cobro", "Cobro", c.rolEn(0, BETO));
		assertEquals("", c.rolEn(0, 0));
	}

	@Test
	public void rolNoConfundeElCeroDeIdCobradorConUnEmpleado() {
		// id_cobrador es NOT NULL DEFAULT 0 (V9): los clientes de contado lo
		// tienen en 0. Un rol apagado (0) NO debe hacerlos coincidir.
		ClienteCartera contado = cliente(1, ANA, 0, "0");

		assertEquals("Venta", contado.rolEn(ANA, 0));
		assertEquals("", contado.rolEn(0, 0));
	}
}
