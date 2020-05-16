package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlLogin;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonesApp;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewLogin extends JDialog{
	private JTextField txtUser, txtPass;
	private JLabel lblUser, lblPass;
	private JButton btnAceptar, btnCancelar;
	String usuario, elPassword;
	private JPanel panel_1;
	private JLabel lblNewLabel;
	private JButton btnConfig;

	public ViewLogin()
		{
		super(null,"LOGIN", ModalityType.DOCUMENT_MODAL);

		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewLogin.class.getResource("/drawable/logo-admin-tool1.png")));

		Container contenedor = getContentPane();
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);

		setUndecorated(true);
		JPanel panel = new JPanel();
		panel.setBackground(new Color(60, 179, 113));
		panel.setBounds(0, 0, 400, 37);
		getContentPane().add(panel);
		panel.setLayout(null);
		JLabel lblLogin = new JLabel("Login AdminTools");
		lblLogin.setForeground(Color.WHITE);
		lblLogin.setFont(new Font("Rod", Font.BOLD, 18));
		lblLogin.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogin.setBounds(0, 0, 400, 37);
		panel.add(lblLogin);


		ImageIcon imgLogo=new ImageIcon(BotonCancelar.class.getResource("/drawable/logo-admin-tool1.png"));
		Image image = imgLogo.getImage();

		image = image.getScaledInstance(image.getWidth(null)/22, image.getHeight(null)/22, Image.SCALE_SMOOTH);
		imgLogo.setImage(image);

		panel_1 = new PanelPadre();
		panel_1.setBounds(0, 0, 400, 250);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);
		lblUser = new JLabel("Usuario: ");
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblUser.setForeground(Color.BLACK);
		lblUser.setBounds(20, 84, 64, 14);
		panel_1.add(lblUser);

		// crear etiqueta y cuadro de texxto del usuario
		txtUser = new JTextField(10);
		txtUser.setBounds(106, 73, 172, 37);
		panel_1.add(txtUser);
		txtUser.setToolTipText("Escriba su nombre de usuario");
		lblPass = new JLabel("Password: ");
		lblPass.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblPass.setForeground(Color.BLACK);
		lblPass.setBounds(10, 130, 100, 14);
		panel_1.add(lblPass);
		//crear etiqueta y cuadro de texxto del pw
		txtPass = new JPasswordField(10);
		txtPass.setBounds(106, 119, 172, 37);
		panel_1.add(txtPass);
		txtPass.setToolTipText("Escriba su contrase�a");

		//Crear y agregar los botones
		btnAceptar = new BotonesApp("Aceptar");
		btnAceptar.setLocation(48, 186);
		panel_1.add(btnAceptar);
		//establecer Boton aceptar por defecto
		getRootPane().setDefaultButton(btnAceptar);

		btnCancelar = new BotonesApp("Cancelar");
		btnCancelar.setLocation(224, 186);
		panel_1.add(btnCancelar);

		lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(imgLogo);
		lblNewLabel.setBounds(288, 57, 102, 92);
		panel_1.add(lblNewLabel);

		btnConfig = new JButton("...");
		btnConfig.setBounds(0, 227, 29, 23);
		btnConfig.setVisible(false);
		panel_1.add(btnConfig);






		setTitle("Autentificacion");
		setSize(400,250);           // Tamanio del Frame
		setResizable(false);       // que no se le pueda cambiar el tamanio
		//Centrar la ventana de autentificacion en la pantalla
		Dimension tamFrame=this.getSize();//para obtener las dimensiones del frame
		Dimension tamPantalla=Toolkit.getDefaultToolkit().getScreenSize();      //para obtener el tamanio de la pantalla
		setLocation((tamPantalla.width-tamFrame.width)/2, (tamPantalla.height-tamFrame.height)/2);  //para posicionar
		//setVisible(true);           // Hacer visible al frame

	}   // fin de constructor



	public JTextField getTxtUser(){
		return txtUser;
	}
	public JTextField getTxtPass(){
		return txtPass;
	}

	public void conectarControlador(CtlLogin c){

		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("SALIR");

		btnAceptar.addActionListener(c);
		btnAceptar.setActionCommand("LOGIN");

		btnConfig.addActionListener(c);
		btnConfig.setActionCommand("BD_CONFIG");
	}

	private class panelFondo extends JPanel{
		@Override
		   public void paintComponent(Graphics g){
		      Dimension tamanio = getSize();
		      ImageIcon imagenFondo = new ImageIcon(getClass().
		      getResource("/drawable/login.jpg"));
		      g.drawImage(imagenFondo.getImage(), 0, 0,
		      tamanio.width, tamanio.height, null);
		      setOpaque(false);
		      super.paintComponent(g);
		   }
	}
}
