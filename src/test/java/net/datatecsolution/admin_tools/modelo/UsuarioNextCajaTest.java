package net.datatecsolution.admin_tools.modelo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class UsuarioNextCajaTest {

	private static Caja caja(int codigo, String nombreBd, boolean activa) {
		Caja c = new Caja();
		c.setCodigo(codigo);
		c.setNombreBd(nombreBd);
		c.setDescripcion("caja-" + codigo);
		c.setActiva(activa);
		return c;
	}

	private static Usuario usuarioCon(Caja... cajas) {
		Usuario u = new Usuario();
		List<Caja> lista = new ArrayList<>();
		for (Caja c : cajas) lista.add(c);
		u.setCajas(lista);
		return u;
	}

	// ---------------------------------------------------------------------
	// Usuario con una sola caja
	// ---------------------------------------------------------------------

	@Test
	public void nextCajaConUnaSolaCajaDevuelveLaMisma() {
		Caja unica = caja(10, "db_caja_10", true);
		Usuario u = usuarioCon(unica);

		Caja resultado = u.nextCaja();

		assertSame(unica, resultado);
		assertTrue("la única caja debe quedar activa", unica.isActiva());
		assertEquals("db_caja_10", resultado.getNombreBd());
	}

	@Test
	public void nextCajaRepetidaConUnaSolaCajaSiempreDevuelveLaMisma() {
		Caja unica = caja(10, "db_caja_10", true);
		Usuario u = usuarioCon(unica);

		for (int i = 0; i < 50; i++) {
			Caja resultado = u.nextCaja();
			assertSame("iteración " + i, unica, resultado);
			assertTrue("iteración " + i + ": la única caja debe seguir activa", unica.isActiva());
		}
	}

	@Test
	public void getCajaActivaConUnaSolaCajaDevuelveEsaCaja() {
		Caja unica = caja(10, "db_caja_10", true);
		Usuario u = usuarioCon(unica);

		Caja activa = u.getCajaActiva();

		assertNotNull(activa);
		assertSame(unica, activa);
	}

	// ---------------------------------------------------------------------
	// Simulación de flujo: 100 facturas id=1 con una sola caja en modo auto
	// ---------------------------------------------------------------------

	@Test
	public void cienFacturasConsumidorFinalConUnaSolaCajaTodasVanALaMismaCaja() {
		Caja unica = caja(10, "db_caja_unica", true);
		Usuario u = usuarioCon(unica);

		int bandera = 0;
		int facturasEnCajaUnica = 0;

		for (int i = 0; i < 100; i++) {
			int clienteId = 1;
			boolean rota = RotacionCajas.debeRotarPreGuardar(true, false, clienteId, bandera);
			if (rota) {
				u.nextCaja();
			}
			assertSame("la caja destino debe seguir siendo la única", unica, u.getCajaActiva());
			assertEquals("db_caja_unica", u.getCajaActiva().getNombreBd());
			facturasEnCajaUnica++;

			bandera = RotacionCajas.banderaPostGuardar(true, clienteId, bandera);
		}

		assertEquals(100, facturasEnCajaUnica);
	}

	// ---------------------------------------------------------------------
	// Sanity: nextCaja con 2 cajas sí rota (no se rompió el caso común)
	// ---------------------------------------------------------------------

	@Test
	public void nextCajaConDosCajasAlternaEntreEllas() {
		Caja c1 = caja(10, "db_c1", true);
		Caja c2 = caja(20, "db_c2", false);
		Usuario u = usuarioCon(c1, c2);

		assertSame(c1, u.getCajaActiva());

		Caja siguiente = u.nextCaja();
		assertSame(c2, siguiente);
		assertSame(c2, u.getCajaActiva());

		Caja vuelta = u.nextCaja();
		assertSame(c1, vuelta);
		assertSame(c1, u.getCajaActiva());
	}
}
