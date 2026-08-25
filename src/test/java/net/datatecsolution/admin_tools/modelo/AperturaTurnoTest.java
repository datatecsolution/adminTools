package net.datatecsolution.admin_tools.modelo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * US-149: reglas del rango de facturas del turno (incidente venecia 7175:
 * una fila previa confundida con "no existe" registro factura_inicial=1 y el
 * cierre sumo toda la historia de la caja).
 */
public class AperturaTurnoTest {

	private CierreFacturacion previa(int noFacturaFinal) {
		CierreFacturacion c = new CierreFacturacion();
		c.setNoFacturaFinal(noFacturaFinal);
		return c;
	}

	private Factura ultima(int numeroFactura) {
		Factura f = new Factura();
		f.setIdFactura(numeroFactura);
		return f;
	}

	// --- calcularFacturaInicial ---

	@Test
	public void conTurnoAnteriorCerradoContinuaSuFinal() {
		assertEquals(447573, AperturaTurno.calcularFacturaInicial(previa(447572), null));
	}

	@Test
	public void sinTurnoAnteriorContinuaLaUltimaFacturaDelUsuario() {
		// el caso del incidente: la fila previa "no aparece" pero hay historia
		assertEquals(447616, AperturaTurno.calcularFacturaInicial(null, ultima(447615)));
	}

	@Test
	public void turnoAnteriorSinCompletarUsaLaUltimaFactura() {
		// fila con factura_final=0 (turno que nunca completo el rango)
		assertEquals(447616, AperturaTurno.calcularFacturaInicial(previa(0), ultima(447615)));
	}

	@Test
	public void turnoAnteriorConFinalNegativoUsaLaUltimaFactura() {
		// -1 es el default de la columna en BD
		assertEquals(101, AperturaTurno.calcularFacturaInicial(previa(-1), ultima(100)));
	}

	@Test
	public void primeraVezRealEmpiezaEnUno() {
		assertEquals(1, AperturaTurno.calcularFacturaInicial(null, null));
	}

	@Test
	public void primeraVezConFilaVaciaYSinFacturasEmpiezaEnUno() {
		assertEquals(1, AperturaTurno.calcularFacturaInicial(previa(0), null));
	}

	// --- tieneFinalUsable ---

	@Test
	public void finalUsableSoloConNumeroPositivo() {
		assertTrue(AperturaTurno.tieneFinalUsable(previa(1)));
		assertFalse(AperturaTurno.tieneFinalUsable(previa(0)));
		assertFalse(AperturaTurno.tieneFinalUsable(previa(-1)));
		assertFalse(AperturaTurno.tieneFinalUsable(null));
	}

	// --- esRangoEnvenenado (guardia del cierre) ---

	@Test
	public void rangoEnvenenadoInicialUnoConHistoriaPrevia() {
		// la firma exacta del cierre 7175: inicial=1 con turno anterior cerrado
		assertTrue(AperturaTurno.esRangoEnvenenado(1, 447572));
	}

	@Test
	public void rangoEnvenenadoInicialCeroConHistoriaPrevia() {
		assertTrue(AperturaTurno.esRangoEnvenenado(0, 263019));
	}

	@Test
	public void primeraVezRealNoEsEnvenenado() {
		// inicial=1 sin ningun turno cerrado antes: caso legitimo
		assertFalse(AperturaTurno.esRangoEnvenenado(1, null));
	}

	@Test
	public void rangoNormalNoEsEnvenenado() {
		assertFalse(AperturaTurno.esRangoEnvenenado(447573, 447572));
	}

	@Test
	public void historiaPreviaSinCompletarNoBloquea() {
		// el turno anterior existe pero nunca lleno su final (final=0)
		assertFalse(AperturaTurno.esRangoEnvenenado(1, 0));
	}
}
