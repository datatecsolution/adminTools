package net.datatecsolution.admin_tools.controlador;

import org.junit.Test;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.junit.Assert.assertEquals;

/** Alternativas Ctrl+numero de las teclas F (para macOS). */
public class AtajosTecladoTest {

	private static final Component FUENTE = new Canvas();

	private static int tecla(int modificadores, int codigo) {
		KeyEvent e = new KeyEvent(FUENTE, KeyEvent.KEY_PRESSED, 0L, modificadores, codigo, KeyEvent.CHAR_UNDEFINED);
		return AtajosTeclado.teclaEquivalente(e);
	}

	@Test
	public void ctrlNumeroEsLaTeclaF() {
		assertEquals(KeyEvent.VK_F1, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_1));
		assertEquals(KeyEvent.VK_F7, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_7));
		assertEquals(KeyEvent.VK_F9, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_9));
		assertEquals(KeyEvent.VK_F10, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_0));
		assertEquals(KeyEvent.VK_F11, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_MINUS));
		assertEquals(KeyEvent.VK_F11, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SUBTRACT));
		assertEquals(KeyEvent.VK_F12, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_EQUALS));
		assertEquals(KeyEvent.VK_F12, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_PLUS));
		assertEquals(KeyEvent.VK_F12, tecla(InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_ADD));
	}

	@Test
	public void loDemasQuedaIgual() {
		// las teclas F originales
		assertEquals(KeyEvent.VK_F2, tecla(0, KeyEvent.VK_F2));
		// numeros sin Ctrl (escribir cantidades o codigos)
		assertEquals(KeyEvent.VK_3, tecla(0, KeyEvent.VK_3));
		// Ctrl+Shift+0 en teclado latinoamericano es "=", no F10
		assertEquals(KeyEvent.VK_0, tecla(InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_0));
		// combinaciones con Alt o Cmd no se tocan
		assertEquals(KeyEvent.VK_1, tecla(InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK, KeyEvent.VK_1));
		assertEquals(KeyEvent.VK_1, tecla(InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK, KeyEvent.VK_1));
		// atajos Ctrl+letra existentes
		assertEquals(KeyEvent.VK_D, tecla(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_D));
	}
}
