package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonDevolucion extends BotonesApp {
	
	public BotonDevolucion(){
		setIcon(new ImageIcon(BotonKardex.class.getResource("/drawable/devolucion.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Devolucion de producto");
	}

}
