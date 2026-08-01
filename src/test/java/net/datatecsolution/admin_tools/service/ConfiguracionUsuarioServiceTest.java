package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.Caja;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * US-128 — reglas de completitud del alta de usuarios.
 *
 * Regresion de un incidente real (Sharon, 2026-08-01): WACAESTRA se creo como
 * Vendedor, la pantalla dijo "El Usuario se guardo correctamente" y quedo sin
 * caja; la API le rechazo cada pedido con 409 durante dos dias. En la misma
 * auditoria aparecio MYMEDDY, creado por el otro camino y sin empleado.
 */
public class ConfiguracionUsuarioServiceTest {

	private static final int SUPERVISOR = 1;
	private static final int CAJERO = 2;
	private static final int VENDEDOR = 3;
	private static final int ADMIN = 4;

	private ConfiguracionUsuarioService servicio;

	@Before
	public void setUp() {
		servicio = new ConfiguracionUsuarioService();
	}

	private Caja caja(int codigo) {
		Caja c = new Caja();
		c.setCodigo(codigo);
		c.setDescripcion("CAJA " + codigo);
		return c;
	}

	/* ===== el caso WACAESTRA: vendedor sin caja ===== */

	@Test
	public void vendedorSinCajaNoSePuedeGuardar() {
		String error = servicio.validarVendedor(VENDEDOR, 35, new ArrayList<Caja>());

		assertNotNull("es el bug que dejo a un vendedor dos dias sin poder trabajar", error);
		assertTrue("el mensaje debe hablar de la caja: " + error, error.toLowerCase().contains("caja"));
	}

	@Test
	public void vendedorConCajaNulaTampoco() {
		assertNotNull(servicio.validarVendedor(VENDEDOR, 35, null));
	}

	/* ===== el caso MYMEDDY: vendedor sin empleado ===== */

	@Test
	public void vendedorSinEmpleadoNoSePuedeGuardar() {
		String error = servicio.validarVendedor(VENDEDOR, 0, Arrays.asList(caja(2)));

		assertNotNull("sin empleado ningun cajero puede ver ni facturar sus pedidos", error);
		assertTrue("el mensaje debe hablar del empleado: " + error,
				error.toLowerCase().contains("empleado"));
	}

	@Test
	public void elEmpleadoSeValidaANTESQueLaCaja() {
		// Ambos faltan: el mensaje debe guiar primero al empleado, que es lo
		// que decide en que modo se crea el usuario.
		String error = servicio.validarVendedor(VENDEDOR, 0, new ArrayList<Caja>());

		assertTrue("debe reportar el empleado primero: " + error,
				error.toLowerCase().contains("empleado"));
	}

	/* ===== US-110: el vendedor es mono-caja ===== */

	@Test
	public void vendedorConDosCajasNoSePuedeGuardar() {
		String error = servicio.validarVendedor(VENDEDOR, 35, Arrays.asList(caja(2), caja(3)));

		assertNotNull("US-110: un vendedor tiene UNA caja", error);
		assertTrue("el mensaje debe decir cuantas tiene: " + error, error.contains("2"));
	}

	@Test
	public void vendedorCompletoPasa() {
		assertNull(servicio.validarVendedor(VENDEDOR, 35, Arrays.asList(caja(2))));
	}

	/* ===== los otros tipos no se ven afectados ===== */

	@Test
	public void supervisorYAdminSeGuardanSinCaja() {
		// En Sharon hay diez usuarios asi trabajando sin problema; exigirles
		// caja bloquearia editarlos.
		assertNull(servicio.validarVendedor(SUPERVISOR, 0, new ArrayList<Caja>()));
		assertNull(servicio.validarVendedor(ADMIN, 0, new ArrayList<Caja>()));
	}

	@Test
	public void cajeroNoEntraEnEstaRegla() {
		assertNull(servicio.validarVendedor(CAJERO, 0, new ArrayList<Caja>()));
	}

	@Test
	public void otrosTiposConVariasCajasSiguenSiendoValidos() {
		// Un cajero SI puede tener varias cajas (rotacion, US-102).
		assertNull(servicio.validarVendedor(CAJERO, 0, Arrays.asList(caja(1), caja(2), caja(3))));
	}

	/* ===== requiereCaja ===== */

	@Test
	public void soloElVendedorRequiereCaja() {
		assertTrue(servicio.requiereCaja(VENDEDOR));

		assertFalse(servicio.requiereCaja(SUPERVISOR));
		assertFalse(servicio.requiereCaja(CAJERO));
		assertFalse(servicio.requiereCaja(ADMIN));
	}

	@Test
	public void laConstanteDeVendedorEsLaDelEsquema() {
		assertTrue(3 == ConfiguracionUsuarioService.TIPO_VENDEDOR);
	}

	/* ===== los mensajes tienen que servirle al operador ===== */

	@Test
	public void losMensajesExplicanLaConsecuencia() {
		String sinCaja = servicio.validarVendedor(VENDEDOR, 35, new ArrayList<Caja>());
		String sinEmpleado = servicio.validarVendedor(VENDEDOR, 0, Arrays.asList(caja(2)));

		assertTrue("debe decir por que importa: " + sinCaja,
				sinCaja.toLowerCase().contains("rechaza"));
		assertTrue("debe decir donde arreglarlo: " + sinEmpleado,
				sinEmpleado.toLowerCase().contains("movil"));
	}
}
