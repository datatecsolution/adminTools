package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;
import java.awt.*;



public class BotonAceptar extends BotonesApp {
	private ImageIcon imgGuardar;
	
	
	public BotonAceptar(){
		super("Aceptar");
		
		/*imgGuardar=new ImageIcon(BotonCancelar.class.getResource("/view/recursos/guardar_2.png"));
		
		 Image image = imgGuardar.getImage();
		    // reduce by 50%
		 image = image.getScaledInstance(image.getWidth(null)/2, image.getHeight(null)/2, Image.SCALE_SMOOTH);
		 imgGuardar.setImage(image);
	
		this.setIcon(imgGuardar);*/
		this.setSize(136, 77);
		this.setPreferredSize(new Dimension(136, 77));
		setToolTipText("Acepar (ctl+a)");
		
		//this.setIcon(new ImageIcon(BotonGuardar.class.getResource("/view/recursos/guardar.png")));
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/aceptar.png"));
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
			
		
	}
	
	
	
  

}
