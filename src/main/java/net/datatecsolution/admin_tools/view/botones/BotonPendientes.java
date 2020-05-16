package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonPendientes extends BotonesApp {

	public BotonPendientes() {
		// TODO Auto-generated constructor stub
		super("Crear cotizacion");
		
		//this.setIcon(new ImageIcon(BotonPendientes.class.getResource("/view/recursos/cotizaciones.png")));
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/drawable/cotizaciones.png"));
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
	}

}
