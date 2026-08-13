package net.datatecsolution.admin_tools.modelo.dao;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * US-142 — el error que llega al cajero cuando una factura no se puede emitir.
 *
 * Contexto: el guard de sobreventa (V33) rechaza la linea con
 * SIGNAL SQLSTATE '45000' y el texto "Stock insuficiente; usuario bloqueado
 * para sobrevender (V33).". Antes ese texto se mostraba crudo y, peor, la
 * factura se guardaba igual sin esa linea. Ahora no se guarda nada y el
 * mensaje tiene que decirle al cajero QUE hacer.
 */
public class FacturaDaoMensajeTest {

	private final FacturaDao dao = new FacturaDao();

	@Test
	public void stockInsuficiente_explicaElCasoYQueNoSeGuardoNada() {
		SQLException e = new SQLException(
				"Stock insuficiente; usuario bloqueado para sobrevender (V33).", "45000", 1644);

		String msg = dao.mensajeParaElCajero(e);

		assertTrue("debe decir que la factura NO se emitio",
				msg.toLowerCase().contains("no se emitio"));
		assertTrue("debe hablar de existencia, no de 'sobrevender'",
				msg.toLowerCase().contains("existencia"));
		assertTrue("debe explicar que pudo agotarse desde que se levanto el pedido",
				msg.toLowerCase().contains("pedido"));
		assertTrue("debe conservar el detalle tecnico para soporte",
				msg.contains("V33"));
	}

	@Test
	public void otroErrorDeBd_avisaQueNoQuedoNadaGuardado() {
		SQLException e = new SQLException("Duplicate entry '1' for key 'PRIMARY'", "23000", 1062);

		String msg = dao.mensajeParaElCajero(e);

		assertTrue(msg.toLowerCase().contains("no se emitio"));
		assertTrue("el cajero tiene que saber que puede reintentar sin duplicar",
				msg.toLowerCase().contains("no se guardo nada"));
		assertTrue(msg.contains("Duplicate entry"));
	}

	/**
	 * Un SIGNAL 45000 que NO sea el de stock (otro guard futuro) no debe
	 * disfrazarse de problema de existencias.
	 */
	@Test
	public void signal45000DeOtraCosa_noHablaDeExistencias() {
		SQLException e = new SQLException("El usuario no tiene caja asignada", "45000", 1644);

		String msg = dao.mensajeParaElCajero(e);

		assertTrue(msg.toLowerCase().contains("no se emitio"));
		assertTrue("no debe atribuirlo a falta de existencia",
				!msg.toLowerCase().contains("existencia"));
	}

	/** Un getMessage() null no debe reventar el diálogo de error. */
	@Test
	public void mensajeNulo_noRevienta() {
		String msg = dao.mensajeParaElCajero(new SQLException((String) null, "08S01", 0));
		assertTrue(msg.toLowerCase().contains("no se emitio"));
	}
}
