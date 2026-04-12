package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCobrador extends BotonesApp  {
	private ImageIcon imgGuardar;



	public BotonCobrador(){
		//super("F3 Cobrar");
		
		
		
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/cobrador.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Cobrador");
		
		
	}
	

}
