package net.datatecsolution.admin_tools.controlador;

import java.awt.event.KeyEvent;

/**
 * Alternativas con Ctrl para las teclas F de Facturar y Ordenes, pensadas para
 * macOS (ahi las teclas F piden fn y algunas las toma el sistema, p. ej. F11 =
 * «Mostrar escritorio»). Las teclas F siguen funcionando igual:
 *
 *   Ctrl+1 .. Ctrl+9  = F1 .. F9
 *   Ctrl+0            = F10
 *   Ctrl+-            = F11
 *   Ctrl+= (o Ctrl++) = F12   (en teclado latinoamericano el = va con Shift)
 */
public final class AtajosTeclado {

	private AtajosTeclado() {
	}

	/**
	 * La tecla F equivalente si el evento es una alternativa Ctrl+numero; si no,
	 * el codigo de tecla tal cual.
	 */
	public static int teclaEquivalente(KeyEvent e) {
		int codigo = e.getKeyCode();
		if (!e.isControlDown() || e.isAltDown() || e.isMetaDown())
			return codigo;

		if (!e.isShiftDown()) {
			if (codigo >= KeyEvent.VK_1 && codigo <= KeyEvent.VK_9)
				return KeyEvent.VK_F1 + (codigo - KeyEvent.VK_1);
			if (codigo == KeyEvent.VK_0)
				return KeyEvent.VK_F10;
			if (codigo == KeyEvent.VK_MINUS)
				return KeyEvent.VK_F11;
			if (codigo == KeyEvent.VK_EQUALS)
				return KeyEvent.VK_F12;
		}
		// teclado numerico y la tecla + dedicada (con o sin Shift segun el teclado)
		if (codigo == KeyEvent.VK_SUBTRACT)
			return KeyEvent.VK_F11;
		if (codigo == KeyEvent.VK_PLUS || codigo == KeyEvent.VK_ADD)
			return KeyEvent.VK_F12;
		return codigo;
	}
}
