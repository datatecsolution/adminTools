package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCuenta  extends BotonesApp {
	
	public BotonCuenta(){
		setIcon(new ImageIcon(BotonKardex.class.getResource("/drawable/contable.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Reporte cuenta");
	}

}
