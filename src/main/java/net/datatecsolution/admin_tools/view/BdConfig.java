package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlBdConfig;
import net.datatecsolution.admin_tools.view.botones.BotonesApp;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class BdConfig extends JDialog {
	private final JTextField txtUrl;
	private final JTextField txtPort;
	private final JTextField txtUser;
	private final JTextField txtPwd;
	private final JTextField txtDataBase;
	private final JButton btnGuardar;
	private final JButton btnCancelar;

	public BdConfig(Window v) {
		super(v, "Configuración de base de datos", ModalityType.DOCUMENT_MODAL);

		setIconImage(Toolkit.getDefaultToolkit().getImage(
			BdConfig.class.getResource("/drawable/logo-admin-tool1.png")));

		setUndecorated(true);
		getContentPane().setLayout(new BorderLayout());

		JPanel panelTitulo = new JPanel();
		panelTitulo.setBackground(new Color(60, 179, 113));
		panelTitulo.setPreferredSize(new Dimension(0, 37));
		panelTitulo.setLayout(new BorderLayout());
		JLabel lblTitulo = new JLabel("Configuración de Base de Datos");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(new Font("Rod", Font.BOLD, 16));
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		panelTitulo.add(lblTitulo, BorderLayout.CENTER);
		getContentPane().add(panelTitulo, BorderLayout.NORTH);

		JPanel panelContenido = new PanelPadre();
		panelContenido.setLayout(null);
		getContentPane().add(panelContenido, BorderLayout.CENTER);

		Font lblFont = new Font("Tahoma", Font.BOLD, 13);
		int lblX = 20;
		int txtX = 160;
		int txtW = 240;
		int rowH = 37;
		int gap = 8;
		int y = 20;

		JLabel lblServidor = new JLabel("Servidor:");
		lblServidor.setFont(lblFont);
		lblServidor.setBounds(lblX, y + 10, 130, 14);
		panelContenido.add(lblServidor);

		txtUrl = new JTextField();
		txtUrl.setBounds(txtX, y, txtW, rowH);
		panelContenido.add(txtUrl);

		y += rowH + gap;

		JLabel lblPuerto = new JLabel("Puerto:");
		lblPuerto.setFont(lblFont);
		lblPuerto.setBounds(lblX, y + 10, 130, 14);
		panelContenido.add(lblPuerto);

		txtPort = new JTextField();
		txtPort.setBounds(txtX, y, txtW, rowH);
		panelContenido.add(txtPort);

		y += rowH + gap;

		JLabel lblUserName = new JLabel("Usuario:");
		lblUserName.setFont(lblFont);
		lblUserName.setBounds(lblX, y + 10, 130, 14);
		panelContenido.add(lblUserName);

		txtUser = new JTextField();
		txtUser.setBounds(txtX, y, txtW, rowH);
		panelContenido.add(txtUser);

		y += rowH + gap;

		JLabel lblPassword = new JLabel("Contraseña:");
		lblPassword.setFont(lblFont);
		lblPassword.setBounds(lblX, y + 10, 130, 14);
		panelContenido.add(lblPassword);

		txtPwd = new JPasswordField();
		txtPwd.setBounds(txtX, y, txtW, rowH);
		panelContenido.add(txtPwd);

		y += rowH + gap;

		JLabel lblDataBase = new JLabel("Base de datos:");
		lblDataBase.setFont(lblFont);
		lblDataBase.setBounds(lblX, y + 10, 130, 14);
		panelContenido.add(lblDataBase);

		txtDataBase = new JTextField();
		txtDataBase.setBounds(txtX, y, txtW, rowH);
		panelContenido.add(txtDataBase);

		y += rowH + gap + 10;

		btnGuardar = new BotonesApp("Guardar");
		btnGuardar.setLocation(80, y);
		panelContenido.add(btnGuardar);

		btnCancelar = new BotonesApp("Cancelar");
		btnCancelar.setLocation(260, y);
		panelContenido.add(btnCancelar);

		y += rowH + 20;

		int totalHeight = 37 + y;
		setSize(430, totalHeight);
		setResizable(false);

		Dimension tamPantalla = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((tamPantalla.width - 430) / 2, (tamPantalla.height - totalHeight) / 2);
	}

	public JTextField getTxtUrl() {
		return txtUrl;
	}

	public JTextField getTxtPort() {
		return txtPort;
	}

	public JTextField getTxtUser() {
		return txtUser;
	}

	public JTextField getTxtPwd() {
		return txtPwd;
	}

	public JTextField getTxtDataBase() {
		return txtDataBase;
	}

	public void conectarControlador(CtlBdConfig c) {
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);

		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
	}
}
