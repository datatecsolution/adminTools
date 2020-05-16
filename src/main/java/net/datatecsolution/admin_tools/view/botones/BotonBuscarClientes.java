package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;

public class BotonBuscarClientes extends BotonesApp  {
	
	private ImageIcon imgGuardar;
	
	
	public BotonBuscarClientes(){
			super("F2 Clientes");
			
			/*imgGuardar=new ImageIcon(BotonCancelar.class.getResource("/view/recursos/clientes_2.png"));
			
			 Image image = imgGuardar.getImage();
			    // reduce by 50%
			 image = image.getScaledInstance(image.getWidth(null)/4, image.getHeight(null)/4, Image.SCALE_SMOOTH);
			 imgGuardar.setImage(image);
		
			this.setIcon(imgGuardar);*/
			

			ImageIcon icon =  new ImageIcon(BotonCierreCaja.class.getResource("/drawable/clientes.png"));
			
			setIcon(resizeIcon(icon, 25, 25));
			
			//this.setIcon(new ImageIcon(BotonBuscarClientes.class.getResource("/view/recursos/clientes.png")));
			this.setVerticalTextPosition(SwingConstants.BOTTOM);
			this.setHorizontalTextPosition(SwingConstants.CENTER);
				
			
		}

}
