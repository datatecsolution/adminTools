package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonCierreCaja extends BotonesApp {

	public BotonCierreCaja() {
		// TODO Auto-generated constructor stub
		super("Cierre de caja");
		
		//this.setIcon(new ImageIcon(BotonCierreCaja.class.getResource("/view/recursos/cierre.png")));
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/drawable/cierre.png"));
		setIcon(resizeIcon(icon, 25, 25));
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
	}

}
