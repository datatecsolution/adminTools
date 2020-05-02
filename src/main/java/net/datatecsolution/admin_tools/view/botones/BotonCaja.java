package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;

public class BotonCaja extends BotonesApp  {
	
	
	public BotonCaja(){
		
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/cash_drawer.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Cajas");
		//setSize(136, 77);
		
	}

}
