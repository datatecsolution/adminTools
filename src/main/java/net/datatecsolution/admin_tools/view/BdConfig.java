package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlBdConfig;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;

import javax.swing.*;
import java.awt.*;

public class BdConfig extends JDialog {
	private JTextField txtUrl;
	private JPasswordField tpfClave;
	private JTextField txtUser;
	private JTextField txtPwd;
	private JTextField txtDataBase;
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCancelar;
	
	
	public BdConfig(Window v) {
		super(v,"Configuracion de la base de datos", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(new BorderLayout());
		this.setSize(450, 400);
		this.setPreferredSize(new Dimension(450,400));
		JPanel jpDatos=new JPanel(new GridLayout(0, 2,3,3));


		JLabel lblClave = new JLabel("clave");
		//jpDatos.add(lblClave);

		tpfClave = new JPasswordField();
		//jpDatos.add(tpfClave);
		tpfClave.setColumns(10);

		JLabel lblServidor = new JLabel("Host name/ip adress");
		jpDatos.add(lblServidor);
		
		txtUrl = new JTextField();
		jpDatos.add(txtUrl);
		txtUrl.setColumns(10);
		
		JLabel lblUserName = new JLabel("User name");
		jpDatos.setBounds(23, 78, 106, 15);
		jpDatos.add(lblUserName);
		
		txtUser = new JTextField();
		jpDatos.add(txtUser);
		txtUser.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		jpDatos.add(lblPassword);
		
		txtPwd = new JPasswordField();
		jpDatos.add(txtPwd);
		txtPwd.setColumns(10);
		
		JLabel lblDataBase = new JLabel("Data base");
		jpDatos.add(lblDataBase);
		
		txtDataBase = new JTextField();
		jpDatos.add(txtDataBase);
		txtDataBase.setColumns(10);
		
		btnGuardar = new BotonGuardar();
		jpDatos.add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		jpDatos.add(btnCancelar);
		getContentPane().add(jpDatos);
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

		tpfClave.setActionCommand("CLAVE");
		tpfClave.addActionListener(c);

		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);
		
		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
	}
}
