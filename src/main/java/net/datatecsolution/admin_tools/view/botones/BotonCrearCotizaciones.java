package net.datatecsolution.admin_tools.view.botones;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class BotonCrearCotizaciones extends BotonesApp {

	public BotonCrearCotizaciones() {
		// TODO Auto-generated constructor stub
		
		super("Cotizaciones");
		add = true;
		
		setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/cotizacion.png"));
		
		setIcon(resizeIcon(icon, 25, 25));
	}

	public BotonCrearCotizaciones(String titulo) {
		super(titulo);
		// TODO Auto-generated constructor stub
	}

}
