package net.datatecsolution.admin_tools.modelo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RotacionCajasTest {

	private static final int ID_CONSUMIDOR_FINAL = 1;
	private static final int ID_CLIENTE_NORMAL_A = 45;
	private static final int ID_CLIENTE_NORMAL_B = 78;

	// ---------------------------------------------------------------------
	// debeRotarPreGuardar
	// ---------------------------------------------------------------------

	@Test
	public void consumidorFinalConBanderaCeroEnAutomaticoRota() {
		assertTrue(RotacionCajas.debeRotarPreGuardar(true, false, ID_CONSUMIDOR_FINAL, 0));
	}

	@Test
	public void consumidorFinalConBanderaUnoNoRota() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(true, false, ID_CONSUMIDOR_FINAL, 1));
	}

	@Test
	public void consumidorFinalConBanderaDosNoRota() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(true, false, ID_CONSUMIDOR_FINAL, 2));
	}

	@Test
	public void clienteNormalNuncaRotaAunqueBanderaSeaCero() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(true, false, ID_CLIENTE_NORMAL_A, 0));
	}

	@Test
	public void modoManualNuncaRotaAunqueSeaConsumidorFinal() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(false, false, ID_CONSUMIDOR_FINAL, 0));
	}

	@Test
	public void rotacionManualCtlPCancelaLaAutomatica() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(true, true, ID_CONSUMIDOR_FINAL, 0));
	}

	@Test
	public void clienteSinIdConocidoNoRota() {
		assertFalse(RotacionCajas.debeRotarPreGuardar(true, false, -1, 0));
	}

	// ---------------------------------------------------------------------
	// banderaPostGuardar
	// ---------------------------------------------------------------------

	@Test
	public void banderaAvanzaDeCeroAUnoConConsumidorFinal() {
		assertEquals(1, RotacionCajas.banderaPostGuardar(true, ID_CONSUMIDOR_FINAL, 0));
	}

	@Test
	public void banderaAvanzaDeUnoADosConConsumidorFinal() {
		assertEquals(2, RotacionCajas.banderaPostGuardar(true, ID_CONSUMIDOR_FINAL, 1));
	}

	@Test
	public void banderaReseteaDeDosACeroConConsumidorFinal() {
		assertEquals(0, RotacionCajas.banderaPostGuardar(true, ID_CONSUMIDOR_FINAL, 2));
	}

	@Test
	public void clienteNormalNoMueveBanderaAunqueEsteEnReset() {
		assertEquals(2, RotacionCajas.banderaPostGuardar(true, ID_CLIENTE_NORMAL_A, 2));
	}

	@Test
	public void clienteNormalNoIncrementaBandera() {
		assertEquals(0, RotacionCajas.banderaPostGuardar(true, ID_CLIENTE_NORMAL_A, 0));
		assertEquals(1, RotacionCajas.banderaPostGuardar(true, ID_CLIENTE_NORMAL_B, 1));
	}

	@Test
	public void modoManualNoMueveBandera() {
		assertEquals(0, RotacionCajas.banderaPostGuardar(false, ID_CONSUMIDOR_FINAL, 0));
		assertEquals(2, RotacionCajas.banderaPostGuardar(false, ID_CONSUMIDOR_FINAL, 2));
	}

	// ---------------------------------------------------------------------
	// Simulaciones de flujo completo
	// ---------------------------------------------------------------------

	private static class Simulacion {
		int caja1 = 0;
		int caja2 = 0;
		int bandera = 0;

		void factura(int clienteId, boolean rotacionAuto, boolean rotacionManual) {
			boolean rota = RotacionCajas.debeRotarPreGuardar(rotacionAuto, rotacionManual, clienteId, bandera);
			if (rota) caja2++;
			else caja1++;
			bandera = RotacionCajas.banderaPostGuardar(rotacionAuto, clienteId, bandera);
		}
	}

	@Test
	public void tresFacturasConsumidorFinalSiguenPatronCaja2Caja1Caja1() {
		Simulacion s = new Simulacion();
		s.factura(ID_CONSUMIDOR_FINAL, true, false);
		s.factura(ID_CONSUMIDOR_FINAL, true, false);
		s.factura(ID_CONSUMIDOR_FINAL, true, false);
		assertEquals(1, s.caja2);
		assertEquals(2, s.caja1);
		assertEquals("ciclo cierra a 0", 0, s.bandera);
	}

	@Test
	public void cienFacturasConsumidorFinalDistribuyenTreintayCuatroACaja2() {
		Simulacion s = new Simulacion();
		for (int i = 0; i < 100; i++) {
			s.factura(ID_CONSUMIDOR_FINAL, true, false);
		}
		assertEquals(100, s.caja1 + s.caja2);
		assertEquals(34, s.caja2);
		assertEquals(66, s.caja1);
	}

	@Test
	public void noventayNueveFacturasConsumidorFinalDistribuyenExactoUnTercio() {
		Simulacion s = new Simulacion();
		for (int i = 0; i < 99; i++) {
			s.factura(ID_CONSUMIDOR_FINAL, true, false);
		}
		assertEquals(33, s.caja2);
		assertEquals(66, s.caja1);
	}

	@Test
	public void clientesNormalesIntermediosNoDesfasanElCiclo() {
		Simulacion s = new Simulacion();
		// Tabla original discutida en la conversación
		s.factura(ID_CONSUMIDOR_FINAL, true, false);   // 1 caja2, bandera 1
		s.factura(ID_CONSUMIDOR_FINAL, true, false);   // 2 caja1, bandera 2
		s.factura(ID_CLIENTE_NORMAL_A, true, false);   // 3 caja1, bandera 2
		s.factura(ID_CONSUMIDOR_FINAL, true, false);   // 4 caja1, bandera 0 (reset)
		s.factura(ID_CLIENTE_NORMAL_B, true, false);   // 5 caja1, bandera 0
		s.factura(ID_CONSUMIDOR_FINAL, true, false);   // 6 caja2, bandera 1
		assertEquals(2, s.caja2);
		assertEquals(4, s.caja1);
		assertEquals(1, s.bandera);
	}

	@Test
	public void bateriaDeClientesNormalesNoMueveElContador() {
		Simulacion s = new Simulacion();
		for (int i = 0; i < 50; i++) {
			s.factura(ID_CLIENTE_NORMAL_A, true, false);
		}
		assertEquals(50, s.caja1);
		assertEquals(0, s.caja2);
		assertEquals(0, s.bandera);
	}

	@Test
	public void modoManualNuncaRotaNiMueveBandera() {
		Simulacion s = new Simulacion();
		for (int i = 0; i < 20; i++) {
			s.factura(ID_CONSUMIDOR_FINAL, false, false);
		}
		assertEquals(20, s.caja1);
		assertEquals(0, s.caja2);
		assertEquals(0, s.bandera);
	}

	@Test
	public void ctlpManualDentroFlujoAutomaticoNoAdelantaElCiclo() {
		Simulacion s = new Simulacion();
		// Primera factura con Ctrl+P: manual cancela la automática pero sí avanza bandera
		s.factura(ID_CONSUMIDOR_FINAL, true, true);    // caja1 (manual = no rotar en el helper)
		assertEquals(1, s.bandera);
		assertEquals(1, s.caja1);
		assertEquals(0, s.caja2);

		// Siguiente factura ya sin manual: bandera=1 → no rota (caja1)
		s.factura(ID_CONSUMIDOR_FINAL, true, false);
		assertEquals(2, s.bandera);
		assertEquals(2, s.caja1);

		// Tercera factura: bandera=2 → no rota + reset
		s.factura(ID_CONSUMIDOR_FINAL, true, false);
		assertEquals(0, s.bandera);
		assertEquals(3, s.caja1);
		assertEquals(0, s.caja2);
	}

	@Test
	public void banderaSiempreDentroDeRango() {
		Simulacion s = new Simulacion();
		for (int i = 0; i < 1000; i++) {
			int id = (i % 7 == 0) ? ID_CLIENTE_NORMAL_A : ID_CONSUMIDOR_FINAL;
			boolean manual = (i % 23 == 0);
			s.factura(id, true, manual);
			assertTrue("bandera fuera de rango en iteracion " + i + ": " + s.bandera,
					s.bandera >= 0 && s.bandera <= 2);
		}
	}
}
