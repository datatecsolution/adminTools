package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;

public class BotonReporte extends BotonesApp {
	public BotonReporte(){
		setIcon(new ImageIcon(BotonKardex.class.getResource("/reporte.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Reporte");
	}
}
