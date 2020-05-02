package net.datatecsolution.admin_tools.view.botones;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class BotonCobrar extends BotonesApp {
private ImageIcon imgGuardar;
	
	
	public BotonCobrar(){
			super("F3 Cobrar");
			
			/*imgGuardar=new ImageIcon(BotonCancelar.class.getResource("/view/recursos/Facturacion.png"));
			
			 Image image = imgGuardar.getImage();
			    // reduce by 50%
			 image = image.getScaledInstance(image.getWidth(null)/7, image.getHeight(null)/7, Image.SCALE_SMOOTH);
			 imgGuardar.setImage(image);
		
			this.setIcon(imgGuardar);*/
			ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/cobrar.png"));
			
			setIcon(resizeIcon(icon, 25, 25));
			
			//this.setIcon(new ImageIcon(BotonCobrar.class.getResource("/view/recursos/cobrar.png")));
			this.setVerticalTextPosition(SwingConstants.BOTTOM);
			this.setHorizontalTextPosition(SwingConstants.CENTER);
				
			
		}

}
