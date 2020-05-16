package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCliente extends BotonesApp {
	
	public BotonCliente(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/cliente.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Pago cliente (F10)");
		//setSize(136, 77);
		
	}

}
