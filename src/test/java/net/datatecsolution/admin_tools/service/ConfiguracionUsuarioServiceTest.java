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
		String error = servicio.validar(VENDEDOR, 35, new ArrayList<Caja>());

		assertNotNull("es el bug que dejo a un vendedor dos dias sin poder trabajar", error);
		assertTrue("el mensaje debe hablar de la caja: " + error, error.toLowerCase().contains("caja"));
	}

	@Test
	public void vendedorConCajaNulaTampoco() {
		assertNotNull(servicio.validar(VENDEDOR, 35, null));
	}

	/* ===== el caso MYMEDDY: vendedor sin empleado ===== */

	@Test
	public void vendedorSinEmpleadoNoSePuedeGuardar() {
		String error = servicio.validar(VENDEDOR, 0, Arrays.asList(caja(2)));

		assertNotNull("sin empleado ningun cajero puede ver ni facturar sus pedidos", error);
		assertTrue("el mensaje debe hablar del empleado: " + error,
				error.toLowerCase().contains("empleado"));
	}

	@Test
	public void elEmpleadoSeValidaANTESQueLaCaja() {
		// Ambos faltan: el mensaje debe guiar primero al empleado, que es lo
		// que decide en que modo se crea el usuario.
		String error = servicio.validar(VENDEDOR, 0, new ArrayList<Caja>());

		assertTrue("debe reportar el empleado primero: " + error,
				error.toLowerCase().contains("empleado"));
	}

	/* ===== US-110: el vendedor es mono-caja ===== */

	@Test
	public void vendedorConDosCajasNoSePuedeGuardar() {
		String error = servicio.validar(VENDEDOR, 35, Arrays.asList(caja(2), caja(3)));

		assertNotNull("US-110: un vendedor tiene UNA caja", error);
		assertTrue("el mensaje debe decir cuantas tiene: " + error, error.contains("2"));
	}

	@Test
	public void vendedorCompletoPasa() {
		assertNull(servicio.validar(VENDEDOR, 35, Arrays.asList(caja(2))));
	}

	/* ===== supervisor y admin NO facturan: no deben tener caja ===== */

	@Test
	public void supervisorYAdminSeGuardanSinCaja() {
		assertNull(servicio.validar(SUPERVISOR, 0, new ArrayList<Caja>()));
		assertNull(servicio.validar(ADMIN, 0, new ArrayList<Caja>()));
	}

	@Test
	public void supervisorCONCajaNoSePuedeGuardar() {
		String error = servicio.validar(SUPERVISOR, 0, Arrays.asList(caja(2)));

		assertNotNull("el rol no esta disenado para facturar", error);
		assertTrue("el mensaje debe nombrar el rol: " + error, error.contains("Supervisor"));
	}

	@Test
	public void administradorCONCajaNoSePuedeGuardar() {
		String error = servicio.validar(ADMIN, 0, Arrays.asList(caja(1), caja(4)));

		assertNotNull(error);
		assertTrue("el mensaje debe nombrar el rol: " + error, error.contains("Administrador"));
		assertTrue("y decir cuantas tiene: " + error, error.contains("2"));
	}

	@Test
	public void elMensajeDeRolSinCajaExplicaQueHacer() {
		String error = servicio.validar(ADMIN, 0, Arrays.asList(caja(1)));

		assertTrue("debe decir por que: " + error, error.toLowerCase().contains("facturar"));
		assertTrue("y que hacer: " + error, error.toLowerCase().contains("quite"));
	}

	/* ===== el cajero si factura: conserva sus cajas ===== */

	@Test
	public void cajeroSinCajaNoSeBloquea() {
		// No se le exige aca para no bloquear la edicion de cuentas existentes.
		assertNull(servicio.validar(CAJERO, 0, new ArrayList<Caja>()));
	}

	@Test
	public void cajeroConVariasCajasSigueSiendoValido() {
		// Un cajero SI puede tener varias cajas (rotacion, US-102).
		assertNull(servicio.validar(CAJERO, 0, Arrays.asList(caja(1), caja(2), caja(3))));
	}

	/* ===== admiteCaja / requiereCaja ===== */

	@Test
	public void soloCajeroYVendedorAdmitenCaja() {
		assertTrue(servicio.admiteCaja(CAJERO));
		assertTrue(servicio.admiteCaja(VENDEDOR));

		assertFalse("el supervisor no factura", servicio.admiteCaja(SUPERVISOR));
		assertFalse("el administrador no factura", servicio.admiteCaja(ADMIN));
	}

	@Test
	public void soloElVendedorRequiereCaja() {
		assertTrue(servicio.requiereCaja(VENDEDOR));

		assertFalse(servicio.requiereCaja(SUPERVISOR));
		assertFalse(servicio.requiereCaja(CAJERO));
		assertFalse(servicio.requiereCaja(ADMIN));
	}

	@Test
	public void lasConstantesSonLasDelEsquema() {
		assertTrue(1 == ConfiguracionUsuarioService.TIPO_SUPERVISOR);
		assertTrue(2 == ConfiguracionUsuarioService.TIPO_CAJERO);
		assertTrue(3 == ConfiguracionUsuarioService.TIPO_VENDEDOR);
		assertTrue(4 == ConfiguracionUsuarioService.TIPO_ADMIN);
	}

	@Test
	public void nombreRolParaLosMensajes() {
		assertTrue("Supervisor".equals(servicio.nombreRol(SUPERVISOR)));
		assertTrue("Cajero".equals(servicio.nombreRol(CAJERO)));
		assertTrue("Vendedor".equals(servicio.nombreRol(VENDEDOR)));
		assertTrue("Administrador".equals(servicio.nombreRol(ADMIN)));
	}

	/* ===== los mensajes tienen que servirle al operador ===== */

	@Test
	public void losMensajesExplicanLaConsecuencia() {
		String sinCaja = servicio.validar(VENDEDOR, 35, new ArrayList<Caja>());
		String sinEmpleado = servicio.validar(VENDEDOR, 0, Arrays.asList(caja(2)));

		assertTrue("debe decir por que importa: " + sinCaja,
				sinCaja.toLowerCase().contains("rechaza"));
		assertTrue("debe decir donde arreglarlo: " + sinEmpleado,
				sinEmpleado.toLowerCase().contains("movil"));
	}
}
