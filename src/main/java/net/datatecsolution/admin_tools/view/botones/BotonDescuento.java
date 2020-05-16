package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonDescuento extends BotonesApp {
	
	public BotonDescuento(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/descuento.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Descuento (F7)");
		//setSize(136, 77);
	}

}
