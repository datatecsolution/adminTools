package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlBdConfig;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;

import javax.swing.*;
import java.awt.*;

public class BdConfig extends JDialog {
	private JTextField txtUrl;
	private JTextField txtUser;
	private JTextField txtPwd;
	private JTextField txtDataBase;
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCancelar;
	
	
	public BdConfig(Window v) {
		super(v,"Configuracion de la base de datos", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		this.setSize(450, 385);
		this.setPreferredSize(new Dimension(450,385));
		
		JLabel lblServidor = new JLabel("Host name/ip adress");
		lblServidor.setBounds(23, 12, 151, 15);
		getContentPane().add(lblServidor);
		
		txtUrl = new JTextField();
		txtUrl.setBounds(23, 39, 405, 27);
		getContentPane().add(txtUrl);
		txtUrl.setColumns(10);
		
		JLabel lblUserName = new JLabel("User name");
		lblUserName.setBounds(23, 78, 106, 15);
		getContentPane().add(lblUserName);
		
		txtUser = new JTextField();
		txtUser.setBounds(23, 105, 405, 27);
		getContentPane().add(txtUser);
		txtUser.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(23, 144, 106, 15);
		getContentPane().add(lblPassword);
		
		txtPwd = new JTextField();
		txtPwd.setBounds(23, 171, 405, 27);
		getContentPane().add(txtPwd);
		txtPwd.setColumns(10);
		
		JLabel lblDataBase = new JLabel("Data base");
		lblDataBase.setBounds(23, 210, 151, 15);
		getContentPane().add(lblDataBase);
		
		txtDataBase = new JTextField();
		txtDataBase.setBounds(23, 237, 405, 27);
		getContentPane().add(txtDataBase);
		txtDataBase.setColumns(10);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setBounds(29, 274, 145, 77);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setBounds(244, 274, 146, 77);
		getContentPane().add(btnCancelar);
		
		//Centrar la ventana de autentificacion en la pantalla
		Dimension tamFrame=this.getSize();//para obtener las dimensiones del frame
		Dimension tamPantalla=Toolkit.getDefaultToolkit().getScreenSize();      //para obtener el tamanio de la pantalla
		setLocation((tamPantalla.width-tamFrame.width)/2, (tamPantalla.height-tamFrame.height)/2);  //para posicionar
		//setVisible(true);           // Hacer visible al frame 
	}
	public JTextField getTxtDataBase(){
		return txtDataBase;
	}
	
	public JTextField getTxtPwd(){
		return txtPwd;
	}
	
	public JTextField getTxtUser(){
		return txtUser;
	}
	
	public JTextField getTxtUrl(){
		return txtUrl;
	}
	
	public void conectarControlador(CtlBdConfig c){
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);
		
		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
	}
}
