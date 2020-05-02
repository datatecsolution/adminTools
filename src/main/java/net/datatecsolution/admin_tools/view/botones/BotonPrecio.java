package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;

public class BotonPrecio extends BotonesApp {
	
	public BotonPrecio(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/precio.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Precio (F8)");
		//setSize(136, 77);
		
	}

}
