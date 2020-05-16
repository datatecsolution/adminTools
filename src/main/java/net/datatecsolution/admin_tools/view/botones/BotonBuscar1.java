package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;
import java.awt.*;

public class BotonBuscar1  extends BotonesApp {
	
private ImageIcon imgGuardar;
	
	
public BotonBuscar1(){
		super("F1 Buscar");
		
	
		//this.setIcon(new ImageIcon(BotonBuscar1.class.getResource("/view/recursos/buscar.png")));

		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/drawable/buscar.png"));
		
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setPreferredSize(new Dimension(128,200));
		
			
		
	}
	

}
