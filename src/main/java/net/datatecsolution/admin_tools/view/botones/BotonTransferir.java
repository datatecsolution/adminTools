package net.datatecsolution.admin_tools.view.botones;

import javax.swing.*;
import java.awt.*;

public class BotonTransferir extends BotonesApp {
	private ImageIcon imgGuardar;



	public BotonTransferir(){
		setIcon(new ImageIcon(BotonAgregar.class.getResource("/drawable/transferir2.png"))); // NOI18N
		//this.setSize(200, 100);.
		setToolTipText("Transferir saldo de cuenta");
		//setSize(136, 77);
	}
	

}
