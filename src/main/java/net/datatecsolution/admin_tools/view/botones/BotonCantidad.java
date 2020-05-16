package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCantidad extends BotonesApp {
	
	public BotonCantidad(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/cantidad.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Cantidad (F9)");
		//setSize(136, 77);
	}
	
}
