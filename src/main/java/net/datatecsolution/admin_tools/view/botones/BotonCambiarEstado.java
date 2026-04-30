package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCambiarEstado extends BotonesApp {

	public BotonCambiarEstado(){
		setIcon(new ImageIcon(BotonCambiarEstado.class.getResource("/drawable/clear.png")));
		setToolTipText("Cambiar estado de la orden");
	}

}
