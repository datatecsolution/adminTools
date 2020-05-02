package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlUsuario;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonAgregar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.ListaModeloCajas;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCrearUsuario extends JDialog {
	private JTextField txtUsuario;
	private JTextField txtNombre;
	private JTextField txtApellido;
	private JPasswordField pwdPwd;
	private JPasswordField pwdRePwd;
	private BotonGuardar btnGuardar;
	private JRadioButton rdbtnCajero;
	private JRadioButton rdbtnAdministrador;
	private BotonCancelar btnCancelar;
	private ButtonGroup grupoOpciones;
	private BotonActualizar btnActualizar;
	private JList lCajas;
	
	private ListaModeloCajas modeloListaCajas;
	private JButton btnAgregar;
	
	
	private JMenuItem mntmEliminar;
	

	
	private JPopupMenu menuContextual; // permite al usuario seleccionar el color
	private JMenuItem mntmDefault;

	
	
	public ViewCrearUsuario(Window view) {
		
		
		this.setTitle("Crear Usuario");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		this.setResizable(false);
		
		menuContextual = new JPopupMenu(); // crea el men� contextual
		
		//opcion del menu flotante
		mntmEliminar = new JMenuItem("Eliminar");
		menuContextual.add(mntmEliminar);
		
		//opcion del menu flotante
		mntmDefault = new JMenuItem("Default");
		menuContextual.add(mntmDefault);
		
		grupoOpciones = new ButtonGroup();
		this.setSize(534, 700);
		getContentPane().setLayout(null);
		
		getContentPane().setBackground(PanelPadre.color1);
		
		JLabel lblUsuario = new JLabel("Usuario");
		lblUsuario.setBounds(20, 9, 60, 15);
		getContentPane().add(lblUsuario);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(20, 234, 101, 15);
		getContentPane().add(lblPassword);
		
		JLabel lblRepetirPassword = new JLabel("Repetir Password");
		lblRepetirPassword.setBounds(20, 309, 172, 15);
		getContentPane().add(lblRepetirPassword);
		
		JLabel lblNombres = new JLabel("Nombre");
		lblNombres.setBounds(20, 84, 88, 15);
		getContentPane().add(lblNombres);
		
		txtNombre = new JTextField();
		txtNombre.setBounds(20, 108, 482, 42);
		getContentPane().add(txtNombre);
		txtNombre.setColumns(10);
		
		JLabel lblApellido = new JLabel("Apellido");
		lblApellido.setBounds(20, 159, 88, 15);
		getContentPane().add(lblApellido);
		
		txtApellido = new JTextField();
		txtApellido.setBounds(20, 183, 482, 42);
		getContentPane().add(txtApellido);
		txtApellido.setColumns(10);
		
		pwdPwd = new JPasswordField();
		pwdPwd.setBounds(20, 258, 482, 42);
		getContentPane().add(pwdPwd);
		
		pwdRePwd = new JPasswordField();
		pwdRePwd.setBounds(20, 333, 482, 42);
		getContentPane().add(pwdRePwd);
		
		rdbtnAdministrador = new JRadioButton("Administrador");
		rdbtnAdministrador.setBounds(222, 395, 149, 18);
		grupoOpciones.add(rdbtnAdministrador);
		getContentPane().add(rdbtnAdministrador);
		
		rdbtnCajero = new JRadioButton("Cajero");
		rdbtnCajero.setSelected(true);
		this.grupoOpciones.add(rdbtnCajero);
		rdbtnCajero.setBounds(20, 395, 111, 18);
		getContentPane().add(rdbtnCajero);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setLocation(87, 595);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(310, 595);
		getContentPane().add(btnCancelar);
		
		txtUsuario = new JTextField();
		txtUsuario.setBounds(20, 33, 482, 42);
		getContentPane().add(txtUsuario);
		txtUsuario.setColumns(10);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setLocation(87, 595);
		getContentPane().add(btnActualizar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new TitledBorder(null, "Cajas asignadas", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		scrollPane.setBounds(20, 425, 482, 158);
		scrollPane.setBackground(PanelPadre.color1);
		getContentPane().add(scrollPane);
		
		modeloListaCajas=new ListaModeloCajas();
		
		
		lCajas = new JList();
		lCajas.setModel(modeloListaCajas);
		lCajas.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(lCajas);
		
		
		
		btnAgregar = new BotonAgregar();
		scrollPane.setRowHeaderView(btnAgregar);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public BotonGuardar getBtnGuardar(){
		return btnGuardar;
	}
	public JRadioButton getRdbtnAdministrador(){
		return rdbtnAdministrador;
	}
	public JRadioButton getRdbtnCajero(){
		return rdbtnCajero;
	}
	public JTextField getTxtUser(){
		return txtUsuario;
	}
	public JTextField getTxtNombre(){
		return txtNombre;
	}
	public JTextField getTxtApellido(){
		return txtApellido;
	}
	public JPasswordField getPwd(){
		return pwdPwd;
	}
	public JPasswordField getRePwd(){
		return pwdRePwd;
	}
	
	public void conectarCtl(CtlUsuario c){
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("CANCELAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("AGREGARCAJA");
		
		lCajas.addMouseListener(c);
		
		mntmEliminar.addActionListener(c);
		mntmEliminar.setActionCommand("ELIMINARCODIGO");
		
		mntmDefault.addActionListener(c);
		mntmDefault.setActionCommand("SETDEFAULT");
		
		
		
	}

	/**
	 * @return the modeloListaCajas
	 */
	public ListaModeloCajas getModeloListaCajas() {
		return modeloListaCajas;
	}

	/**
	 * @return the lCajas
	 */
	public JList getlCajas() {
		return lCajas;
	}

	/**
	 * @return the btnAgregar
	 */
	public JButton getBtnAgregar() {
		return btnAgregar;
	}

	/**
	 * @return the menuContextual
	 */
	public JPopupMenu getMenuContextual() {
		return menuContextual;
	}
}
