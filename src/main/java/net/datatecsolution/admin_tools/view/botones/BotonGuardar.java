package net.datatecsolution.admin_tools.view.botones;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;



public class BotonGuardar extends BotonesApp {
	private ImageIcon imgGuardar;
	
	
	public BotonGuardar(){
		super("Guardar");
		
		/*imgGuardar=new ImageIcon(BotonCancelar.class.getResource("/view/recursos/guardar_2.png"));
		
		 Image image = imgGuardar.getImage();
		    // reduce by 50%
		 image = image.getScaledInstance(image.getWidth(null)/2, image.getHeight(null)/2, Image.SCALE_SMOOTH);
		 imgGuardar.setImage(image);
	
		this.setIcon(imgGuardar);*/
		this.setSize(136, 77);
		this.setPreferredSize(new Dimension(136, 77));
		setToolTipText("Guarda factura (ctl+g)");
		
		//this.setIcon(new ImageIcon(BotonGuardar.class.getResource("/view/recursos/guardar.png")));
		
		ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/guardar.png"));
		setIcon(resizeIcon(icon, 25, 25));
		
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
			
		
	}
	
	
	
  

}
