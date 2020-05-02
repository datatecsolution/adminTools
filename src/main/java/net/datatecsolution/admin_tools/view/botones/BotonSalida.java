package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;

public class BotonSalida extends BotonesApp {
	
	public BotonSalida(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/salida_dinero.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Salida efectivo (F12)");
		//setSize(136, 77);
	}

}
