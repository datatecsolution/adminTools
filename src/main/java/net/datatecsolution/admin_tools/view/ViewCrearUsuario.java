package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlUsuario;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonAgregar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.ListaModeloCajas;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ViewCrearUsuario extends JDialog {
	public static final String CARD_ESCRITORIO = "ESCRITORIO";
	public static final String CARD_MOVIL = "MOVIL";

	private final JRadioButton rdbtnSupervisor;
	private final JRadioButton rdbtnVendedor;
	private final JTextField txtUsuario;
	private final JTextField txtNombre;
	private final JPasswordField pwdPwd;
	private final JPasswordField pwdRePwd;
	private final BotonGuardar btnGuardar;
	private final JRadioButton rdbtnCajero;
	private final JRadioButton rdbtnAdministrador;
	private final BotonCancelar btnCancelar;
	private final ButtonGroup grupoOpciones;
	private final BotonActualizar btnActualizar;
	private final JList lCajas;

	private final ListaModeloCajas modeloListaCajas;
	private final JButton btnAgregar;

	private final JMenuItem mntmEliminar;

	private final JPopupMenu menuContextual;
	private final JMenuItem mntmDefault;

	private final JRadioButton rdbtnTipoEscritorio;
	private final JRadioButton rdbtnTipoMovil;
	private final ButtonGroup grupoTipo;

	private final CardLayout cardLayout;
	private final JPanel panelCards;

	private final JPanel panelEmpleadosEscritorio;
	private final Map<Integer, JCheckBox> empleadosCheckboxes = new HashMap<Integer, JCheckBox>();

	private final JComboBox<Empleado> cbxEmpleadoMovil;
	private final DefaultComboBoxModel<Empleado> modeloCbxEmpleadoMovil;
	private final JComboBox<Caja> cbxCajaMovil;
	private final DefaultComboBoxModel<Caja> modeloCbxCajaMovil;
	private final JPanel panelPreciosMovil;
	private final Map<Integer, JCheckBox> preciosCheckboxes = new HashMap<Integer, JCheckBox>();

	public ViewCrearUsuario(Window view) {
		this.setTitle("Crear Usuario");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		this.setResizable(false);

		menuContextual = new JPopupMenu();
		mntmEliminar = new JMenuItem("Eliminar");
		menuContextual.add(mntmEliminar);
		mntmDefault = new JMenuItem("Default");
		menuContextual.add(mntmDefault);

		grupoOpciones = new ButtonGroup();
		this.setSize(560, 760);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);

		JLabel lblUsuario = new JLabel("Usuario");
		lblUsuario.setBounds(20, 17, 60, 15);
		getContentPane().add(lblUsuario);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(20, 157, 101, 15);
		getContentPane().add(lblPassword);

		JLabel lblRepetirPassword = new JLabel("Repetir Password");
		lblRepetirPassword.setBounds(20, 227, 172, 15);
		getContentPane().add(lblRepetirPassword);

		JLabel lblNombres = new JLabel("Nombre");
		lblNombres.setBounds(20, 87, 88, 15);
		getContentPane().add(lblNombres);

		txtNombre = new JTextField();
		txtNombre.setBounds(20, 103, 482, 42);
		getContentPane().add(txtNombre);
		txtNombre.setColumns(10);

		pwdPwd = new JPasswordField();
		pwdPwd.setBounds(20, 173, 482, 42);
		getContentPane().add(pwdPwd);

		pwdRePwd = new JPasswordField();
		pwdRePwd.setBounds(20, 243, 482, 42);
		getContentPane().add(pwdRePwd);

		grupoTipo = new ButtonGroup();
		rdbtnTipoEscritorio = new JRadioButton("Escritorio");
		rdbtnTipoEscritorio.setBounds(20, 295, 120, 22);
		rdbtnTipoEscritorio.setSelected(true);
		grupoTipo.add(rdbtnTipoEscritorio);
		getContentPane().add(rdbtnTipoEscritorio);

		rdbtnTipoMovil = new JRadioButton("Movil");
		rdbtnTipoMovil.setBounds(150, 295, 120, 22);
		grupoTipo.add(rdbtnTipoMovil);
		getContentPane().add(rdbtnTipoMovil);

		rdbtnAdministrador = new JRadioButton("Administrador");
		rdbtnAdministrador.setBounds(353, 325, 149, 18);
		grupoOpciones.add(rdbtnAdministrador);
		getContentPane().add(rdbtnAdministrador);

		rdbtnSupervisor = new JRadioButton("Supervisor");
		rdbtnSupervisor.setBounds(240, 325, 111, 18);
		grupoOpciones.add(rdbtnSupervisor);
		getContentPane().add(rdbtnSupervisor);

		rdbtnVendedor = new JRadioButton("Vendedor");
		rdbtnVendedor.setBounds(117, 325, 111, 18);
		grupoOpciones.add(rdbtnVendedor);
		getContentPane().add(rdbtnVendedor);

		rdbtnCajero = new JRadioButton("Cajero");
		rdbtnCajero.setSelected(true);
		grupoOpciones.add(rdbtnCajero);
		rdbtnCajero.setBounds(20, 325, 111, 18);
		getContentPane().add(rdbtnCajero);

		btnGuardar = new BotonGuardar();
		btnGuardar.setLocation(87, 640);
		getContentPane().add(btnGuardar);

		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(310, 640);
		getContentPane().add(btnCancelar);

		txtUsuario = new JTextField();
		txtUsuario.setBounds(20, 33, 482, 42);
		getContentPane().add(txtUsuario);
		txtUsuario.setColumns(10);

		btnActualizar = new BotonActualizar();
		btnActualizar.setLocation(87, 640);
		getContentPane().add(btnActualizar);

		cardLayout = new CardLayout();
		panelCards = new JPanel(cardLayout);
		panelCards.setBounds(20, 355, 500, 270);
		panelCards.setBackground(PanelPadre.color1);
		getContentPane().add(panelCards);

		// CARD ESCRITORIO: cajas asignadas + vendedores asignados
		JPanel cardEscritorio = new JPanel(null);
		cardEscritorio.setBackground(PanelPadre.color1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new TitledBorder(null, "Cajas asignadas", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		scrollPane.setBounds(0, 0, 500, 130);
		scrollPane.setBackground(PanelPadre.color1);
		cardEscritorio.add(scrollPane);

		modeloListaCajas = new ListaModeloCajas();
		lCajas = new JList();
		lCajas.setModel(modeloListaCajas);
		lCajas.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(lCajas);

		btnAgregar = new BotonAgregar();
		scrollPane.setRowHeaderView(btnAgregar);

		panelEmpleadosEscritorio = new JPanel();
		panelEmpleadosEscritorio.setLayout(new BoxLayout(panelEmpleadosEscritorio, BoxLayout.Y_AXIS));
		panelEmpleadosEscritorio.setBackground(PanelPadre.color1);
		JScrollPane scrollEmpleados = new JScrollPane(panelEmpleadosEscritorio);
		scrollEmpleados.setViewportBorder(new TitledBorder(null, "Vendedores asignados", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		scrollEmpleados.setBounds(0, 135, 500, 130);
		cardEscritorio.add(scrollEmpleados);

		panelCards.add(cardEscritorio, CARD_ESCRITORIO);

		// CARD MOVIL: combo empleado + CAJA + lista precios
		JPanel cardMovil = new JPanel(null);
		cardMovil.setBackground(PanelPadre.color1);

		JLabel lblEmpleadoMovil = new JLabel("Vendedor (movil)");
		lblEmpleadoMovil.setBounds(0, 5, 200, 15);
		cardMovil.add(lblEmpleadoMovil);

		modeloCbxEmpleadoMovil = new DefaultComboBoxModel<Empleado>();
		cbxEmpleadoMovil = new JComboBox<Empleado>(modeloCbxEmpleadoMovil);
		cbxEmpleadoMovil.setBounds(0, 22, 500, 28);
		cardMovil.add(cbxEmpleadoMovil);

		// US-128: la caja FALTABA en este card. El selector de cajas vivia solo
		// en cardEscritorio, asi que todo usuario creado como Movil —o sea,
		// todo vendedor— nacia sin caja y la API le rechazaba cada pedido con
		// 409. Va un combo simple y no la lista del otro card porque US-110
		// define al vendedor como mono-caja.
		JLabel lblCajaMovil = new JLabel("Caja asignada (obligatoria)");
		lblCajaMovil.setBounds(0, 58, 300, 15);
		cardMovil.add(lblCajaMovil);

		modeloCbxCajaMovil = new DefaultComboBoxModel<Caja>();
		cbxCajaMovil = new JComboBox<Caja>(modeloCbxCajaMovil);
		cbxCajaMovil.setBounds(0, 75, 500, 28);
		cardMovil.add(cbxCajaMovil);

		panelPreciosMovil = new JPanel();
		panelPreciosMovil.setLayout(new BoxLayout(panelPreciosMovil, BoxLayout.Y_AXIS));
		panelPreciosMovil.setBackground(PanelPadre.color1);
		JScrollPane scrollPrecios = new JScrollPane(panelPreciosMovil);
		scrollPrecios.setViewportBorder(new TitledBorder(null, "Precios visibles", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		scrollPrecios.setBounds(0, 113, 500, 150);
		cardMovil.add(scrollPrecios);

		panelCards.add(cardMovil, CARD_MOVIL);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	}

	public void cargarEmpleadosEscritorio(java.util.List<Empleado> todos) {
		panelEmpleadosEscritorio.removeAll();
		empleadosCheckboxes.clear();
		if (todos != null) {
			for (Empleado e : todos) {
				JCheckBox cb = new JCheckBox(e.toString());
				cb.setBackground(PanelPadre.color1);
				panelEmpleadosEscritorio.add(cb);
				empleadosCheckboxes.put(e.getCodigo(), cb);
			}
		}
		panelEmpleadosEscritorio.revalidate();
		panelEmpleadosEscritorio.repaint();
	}

	public void cargarComboEmpleadoMovil(java.util.List<Empleado> todos) {
		modeloCbxEmpleadoMovil.removeAllElements();
		Empleado vacio = new Empleado();
		vacio.setCodigo(0);
		vacio.setNombre("(ninguno)");
		vacio.setApellido("");
		modeloCbxEmpleadoMovil.addElement(vacio);
		if (todos != null) {
			for (Empleado e : todos) {
				modeloCbxEmpleadoMovil.addElement(e);
			}
		}
	}

	/**
	 * US-128 — llena el combo de caja del card Movil.
	 *
	 * El placeholder lleva un {@link Departamento} vacio a proposito:
	 * {@code Caja.toString()} desreferencia el departamento sin comprobarlo,
	 * asi que una Caja sin el revienta con NPE al pintar el combo.
	 */
	public void cargarCajasMovil(java.util.List<Caja> todas) {
		modeloCbxCajaMovil.removeAllElements();
		modeloCbxCajaMovil.addElement(cajaVacia());
		if (todas != null) {
			for (Caja c : todas) {
				modeloCbxCajaMovil.addElement(c);
			}
		}
	}

	private Caja cajaVacia() {
		Caja vacia = new Caja();
		vacia.setCodigo(0);
		vacia.setDescripcion("(seleccione)");
		Departamento sinDepto = new Departamento();
		sinDepto.setDescripcion("");
		vacia.setDetartamento(sinDepto);
		return vacia;
	}

	/** Caja elegida en el card Movil, o {@code null} si esta el placeholder. */
	public Caja getCajaMovilSeleccionada() {
		Caja seleccionada = (Caja) cbxCajaMovil.getSelectedItem();
		return (seleccionada == null || seleccionada.getCodigo() == 0) ? null : seleccionada;
	}

	/** Deja seleccionada la caja del usuario al abrirlo para editar. */
	public void seleccionarCajaMovil(int codigoCaja) {
		for (int i = 0; i < modeloCbxCajaMovil.getSize(); i++) {
			if (modeloCbxCajaMovil.getElementAt(i).getCodigo() == codigoCaja) {
				cbxCajaMovil.setSelectedIndex(i);
				return;
			}
		}
		cbxCajaMovil.setSelectedIndex(0);
	}

	public void cargarPreciosMovil(java.util.List<PrecioArticulo> todos) {
		panelPreciosMovil.removeAll();
		preciosCheckboxes.clear();
		if (todos != null) {
			for (PrecioArticulo p : todos) {
				JCheckBox cb = new JCheckBox(p.getDescripcion());
				cb.setBackground(PanelPadre.color1);
				panelPreciosMovil.add(cb);
				preciosCheckboxes.put(p.getCodigoPrecio(), cb);
			}
		}
		panelPreciosMovil.revalidate();
		panelPreciosMovil.repaint();
	}

	public void marcarEmpleadosAsignados(java.util.List<Empleado> asignados) {
		for (JCheckBox cb : empleadosCheckboxes.values()) cb.setSelected(false);
		if (asignados == null) return;
		for (Empleado e : asignados) {
			JCheckBox cb = empleadosCheckboxes.get(e.getCodigo());
			if (cb != null) cb.setSelected(true);
		}
	}

	public void marcarPreciosAsignados(java.util.List<PrecioArticulo> asignados) {
		for (JCheckBox cb : preciosCheckboxes.values()) cb.setSelected(false);
		if (asignados == null) return;
		for (PrecioArticulo p : asignados) {
			JCheckBox cb = preciosCheckboxes.get(p.getCodigoPrecio());
			if (cb != null) cb.setSelected(true);
		}
	}

	public void seleccionarEmpleadoMovil(int codigoEmpleado) {
		for (int i = 0; i < modeloCbxEmpleadoMovil.getSize(); i++) {
			if (modeloCbxEmpleadoMovil.getElementAt(i).getCodigo() == codigoEmpleado) {
				cbxEmpleadoMovil.setSelectedIndex(i);
				return;
			}
		}
		cbxEmpleadoMovil.setSelectedIndex(0);
	}

	public java.util.List<Integer> getEmpleadosMarcados() {
		java.util.List<Integer> ids = new java.util.ArrayList<Integer>();
		for (Map.Entry<Integer, JCheckBox> entry : empleadosCheckboxes.entrySet()) {
			if (entry.getValue().isSelected()) ids.add(entry.getKey());
		}
		return ids;
	}

	public java.util.List<Integer> getPreciosMarcados() {
		java.util.List<Integer> ids = new java.util.ArrayList<Integer>();
		for (Map.Entry<Integer, JCheckBox> entry : preciosCheckboxes.entrySet()) {
			if (entry.getValue().isSelected()) ids.add(entry.getKey());
		}
		return ids;
	}

	public int getCodigoEmpleadoMovilSeleccionado() {
		Empleado sel = (Empleado) cbxEmpleadoMovil.getSelectedItem();
		return sel == null ? 0 : sel.getCodigo();
	}

	public void mostrarCard(String card) {
		cardLayout.show(panelCards, card);
		boolean esEscritorio = CARD_ESCRITORIO.equals(card);
		rdbtnAdministrador.setVisible(esEscritorio);
		rdbtnSupervisor.setVisible(esEscritorio);
		rdbtnVendedor.setVisible(esEscritorio);
		rdbtnCajero.setVisible(esEscritorio);
	}

	public BotonActualizar getBtnActualizar() { return btnActualizar; }
	public BotonGuardar getBtnGuardar() { return btnGuardar; }
	public JRadioButton getRdbtnAdministrador() { return rdbtnAdministrador; }
	public JRadioButton getRdbtnCajero() { return rdbtnCajero; }
	public JTextField getTxtUser() { return txtUsuario; }
	public JTextField getTxtNombre() { return txtNombre; }
	public JPasswordField getPwd() { return pwdPwd; }
	public JPasswordField getRePwd() { return pwdRePwd; }
	public JRadioButton getRdbtnTipoEscritorio() { return rdbtnTipoEscritorio; }
	public JRadioButton getRdbtnTipoMovil() { return rdbtnTipoMovil; }

	public void conectarCtl(CtlUsuario c) {
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

		rdbtnTipoEscritorio.addActionListener(c);
		rdbtnTipoEscritorio.setActionCommand("TIPO_ESCRITORIO");

		rdbtnTipoMovil.addActionListener(c);
		rdbtnTipoMovil.setActionCommand("TIPO_MOVIL");
	}

	public ListaModeloCajas getModeloListaCajas() { return modeloListaCajas; }
	public JList getlCajas() { return lCajas; }
	public JButton getBtnAgregar() { return btnAgregar; }
	public JPopupMenu getMenuContextual() { return menuContextual; }
	public JRadioButton getRdbtnSupervisor() { return rdbtnSupervisor; }
	public JRadioButton getRdbtnVendedor() { return rdbtnVendedor; }
}
