package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;
import java.awt.*;

public class BotonActualizar extends BotonesApp {

	public BotonActualizar() {
		super("Actualizar");
		this.setSize(136, 77);
		setToolTipText("Actualizar factura (ctl+a)");
		this.setPreferredSize(new Dimension(136, 77));
		// TODO Auto-generated constructor stub
		//this.setIcon(new ImageIcon(BotonActualizar.class.getResource("/view/recursos/actualizar.png")));
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/drawable/actualizar.png"));
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
	}

}
