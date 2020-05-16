package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonImprimirSmall extends BotonesApp  {
	private ImageIcon imgGuardar;
	
	
	
	public BotonImprimirSmall(){
		//super("F3 Cobrar");
		
		
		
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/imprimir3.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Imprimir");
		
		
	}
	

}
