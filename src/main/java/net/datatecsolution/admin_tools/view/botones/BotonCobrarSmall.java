package net.datatecsolution.admin_tools.view.botones;

import java.awt.Image;

import javax.swing.ImageIcon;

public class BotonCobrarSmall extends BotonesApp {
	private ImageIcon imgGuardar;
	
	
	
	public BotonCobrarSmall(){
		//super("F3 Cobrar");
		
		imgGuardar=new ImageIcon(BotonCancelar.class.getResource("/Facturacion.png"));
		
		 Image image = imgGuardar.getImage();
		    // reduce by 50%
		 image = image.getScaledInstance(image.getWidth(null)/15, image.getHeight(null)/15, Image.SCALE_SMOOTH);
		 imgGuardar.setImage(image);
	
		this.setIcon(imgGuardar);
	}
	

}
