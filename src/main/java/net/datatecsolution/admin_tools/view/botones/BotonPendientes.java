package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class BotonPendientes extends BotonesApp {

	public BotonPendientes() {
		// TODO Auto-generated constructor stub
		super("Crear cotizacion");
		
		//this.setIcon(new ImageIcon(BotonPendientes.class.getResource("/view/recursos/cotizaciones.png")));
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/cotizaciones.png"));
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
	}

}
