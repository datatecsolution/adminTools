package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlTransferirCartera;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.MovimientoCartera;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonTransferir;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.TmCarteraClientes;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * US-127 — pantalla para transferir la cartera de clientes entre empleados.
 *
 * La venta ({@code cliente.id_vendedor}) y la cobranza ({@code id_cobrador})
 * son movimientos INDEPENDIENTES: cada uno con su propio origen y su propio
 * destino. La migracion V9 separo los dos roles porque pueden ser personas
 * distintas, y esta pantalla respeta esa separacion — se puede pasar la venta
 * de Ana a Carlos y la cobranza de Beto a Dora en la misma corrida.
 *
 * La tabla muestra la UNION de los dos criterios; la columna "Rol actual"
 * indica por cual entro cada cliente.
 *
 * @author jdmayorga
 */
public class ViewTransferirCartera extends JDialog {

	private final JCheckBox chkVenta;
	private final JComboBox<Empleado> cbxOrigenVenta;
	private final JComboBox<Empleado> cbxDestinoVenta;
	private final DefaultComboBoxModel<Empleado> modeloOrigenVenta;
	private final DefaultComboBoxModel<Empleado> modeloDestinoVenta;

	private final JCheckBox chkCobro;
	private final JComboBox<Empleado> cbxOrigenCobro;
	private final JComboBox<Empleado> cbxDestinoCobro;
	private final DefaultComboBoxModel<Empleado> modeloOrigenCobro;
	private final DefaultComboBoxModel<Empleado> modeloDestinoCobro;

	private final JTable tablaClientes;
	private final TmCarteraClientes modeloClientes;

	private final JButton btnMarcarTodos;
	private final JButton btnDesmarcarTodos;
	private final JLabel lblResumen;

	private final BotonTransferir btnTransferir;
	private final BotonCancelar btnCancelar;

	public ViewTransferirCartera(Window view) {
		this.setTitle("Transferir cartera de clientes");
		this.setModal(true);
		this.setSize(900, 680);
		this.setLocationRelativeTo(view);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);

		JPanel panelQue = new JPanel();
		panelQue.setLayout(null);
		panelQue.setBackground(PanelPadre.color1);
		panelQue.setBorder(new TitledBorder(null, "Que transferir (los dos roles son independientes)",
				TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panelQue.setBounds(20, 15, 850, 120);
		getContentPane().add(panelQue);

		// --- fila VENTA ---
		// Las dos casillas arrancan APAGADAS a proposito: el usuario tiene que
		// elegir explicitamente que mueve. Con una marcada por defecto, quien
		// solo queria transferir el otro rol se lleva puesto uno que no
		// pensaba tocar, y esto no tiene deshacer.
		chkVenta = new JCheckBox("VENTA");
		chkVenta.setSelected(false);
		chkVenta.setBackground(PanelPadre.color1);
		chkVenta.setBounds(15, 28, 90, 25);
		panelQue.add(chkVenta);

		JLabel lblOrigenVenta = new JLabel("origen");
		lblOrigenVenta.setBounds(110, 32, 50, 18);
		panelQue.add(lblOrigenVenta);

		modeloOrigenVenta = new DefaultComboBoxModel<Empleado>();
		cbxOrigenVenta = new JComboBox<Empleado>(modeloOrigenVenta);
		cbxOrigenVenta.setBounds(160, 28, 280, 26);
		panelQue.add(cbxOrigenVenta);

		JLabel lblFlechaVenta = new JLabel("->  destino");
		lblFlechaVenta.setBounds(450, 32, 75, 18);
		panelQue.add(lblFlechaVenta);

		modeloDestinoVenta = new DefaultComboBoxModel<Empleado>();
		cbxDestinoVenta = new JComboBox<Empleado>(modeloDestinoVenta);
		cbxDestinoVenta.setBounds(530, 28, 300, 26);
		panelQue.add(cbxDestinoVenta);

		// --- fila COBRO ---
		chkCobro = new JCheckBox("COBRO");
		chkCobro.setSelected(false);
		chkCobro.setBackground(PanelPadre.color1);
		chkCobro.setBounds(15, 70, 90, 25);
		panelQue.add(chkCobro);

		JLabel lblOrigenCobro = new JLabel("origen");
		lblOrigenCobro.setBounds(110, 74, 50, 18);
		panelQue.add(lblOrigenCobro);

		modeloOrigenCobro = new DefaultComboBoxModel<Empleado>();
		cbxOrigenCobro = new JComboBox<Empleado>(modeloOrigenCobro);
		cbxOrigenCobro.setBounds(160, 70, 280, 26);
		panelQue.add(cbxOrigenCobro);

		JLabel lblFlechaCobro = new JLabel("->  destino");
		lblFlechaCobro.setBounds(450, 74, 75, 18);
		panelQue.add(lblFlechaCobro);

		modeloDestinoCobro = new DefaultComboBoxModel<Empleado>();
		cbxDestinoCobro = new JComboBox<Empleado>(modeloDestinoCobro);
		cbxDestinoCobro.setBounds(530, 70, 300, 26);
		panelQue.add(cbxDestinoCobro);

		// --- tabla ---
		modeloClientes = new TmCarteraClientes();
		tablaClientes = new JTable(modeloClientes);
		tablaClientes.setRowHeight(24);
		tablaClientes.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		ajustarAnchoColumnas();

		JScrollPane scrollClientes = new JScrollPane(tablaClientes);
		scrollClientes.setViewportBorder(new TitledBorder(null, "Clientes de la cartera", TitledBorder.LEFT,
				TitledBorder.TOP, null, null));
		scrollClientes.setBounds(20, 145, 850, 370);
		getContentPane().add(scrollClientes);

		btnMarcarTodos = new JButton("Marcar todos");
		btnMarcarTodos.setBounds(20, 525, 140, 28);
		getContentPane().add(btnMarcarTodos);

		btnDesmarcarTodos = new JButton("Desmarcar todos");
		btnDesmarcarTodos.setBounds(170, 525, 150, 28);
		getContentPane().add(btnDesmarcarTodos);

		lblResumen = new JLabel(" ");
		lblResumen.setBounds(335, 525, 535, 28);
		getContentPane().add(lblResumen);

		btnTransferir = new BotonTransferir();
		// El icono es compartido con la transferencia de saldo entre cuentas,
		// que trae su propio tooltip; aca significa otra cosa.
		btnTransferir.setToolTipText("Aplicar los movimientos marcados a los clientes seleccionados");
		btnTransferir.setLocation(280, 570);
		getContentPane().add(btnTransferir);

		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(490, 570);
		getContentPane().add(btnCancelar);

		// Los combos de cada fila quedan inertes mientras su casilla este
		// apagada: deja ver de un vistazo que rol esta en juego y evita que
		// alguien elija empleados creyendo que va a mover algo.
		ActionListener sincronizador = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sincronizarHabilitacion();
			}
		};
		chkVenta.addActionListener(sincronizador);
		chkCobro.addActionListener(sincronizador);
		sincronizarHabilitacion();
	}

	private void sincronizarHabilitacion() {
		cbxOrigenVenta.setEnabled(chkVenta.isSelected());
		cbxDestinoVenta.setEnabled(chkVenta.isSelected());
		cbxOrigenCobro.setEnabled(chkCobro.isSelected());
		cbxDestinoCobro.setEnabled(chkCobro.isSelected());
	}

	private void ajustarAnchoColumnas() {
		int[] anchos = { 30, 70, 330, 110, 80, 110, 110 };
		for (int i = 0; i < anchos.length && i < tablaClientes.getColumnCount(); i++) {
			tablaClientes.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
		}
		tablaClientes.getColumnModel().getColumn(TmCarteraClientes.COL_SELECCION).setMaxWidth(30);
	}

	public void conectarCtl(CtlTransferirCartera c) {
		// Cualquier cambio en los origenes o en los checkboxes redefine que
		// clientes son candidatos, asi que hay que recargar la tabla.
		cbxOrigenVenta.addActionListener(c);
		cbxOrigenVenta.setActionCommand("CAMBIO_CRITERIO");

		cbxOrigenCobro.addActionListener(c);
		cbxOrigenCobro.setActionCommand("CAMBIO_CRITERIO");

		chkVenta.addActionListener(c);
		chkVenta.setActionCommand("CAMBIO_CRITERIO");

		chkCobro.addActionListener(c);
		chkCobro.setActionCommand("CAMBIO_CRITERIO");

		// Los destinos no cambian la lista, solo el resumen.
		cbxDestinoVenta.addActionListener(c);
		cbxDestinoVenta.setActionCommand("CAMBIO_DESTINO");

		cbxDestinoCobro.addActionListener(c);
		cbxDestinoCobro.setActionCommand("CAMBIO_DESTINO");

		btnMarcarTodos.addActionListener(c);
		btnMarcarTodos.setActionCommand("MARCAR_TODOS");

		btnDesmarcarTodos.addActionListener(c);
		btnDesmarcarTodos.setActionCommand("DESMARCAR_TODOS");

		btnTransferir.addActionListener(c);
		btnTransferir.setActionCommand("TRANSFERIR");

		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("CANCELAR");

		modeloClientes.addTableModelListener(c);
	}

	/**
	 * Llena los cuatro combos con la misma lista de empleados, precedida por
	 * un elemento vacio para que ninguno arranque preseleccionado (una
	 * transferencia mal disparada por descuido no tiene deshacer).
	 */
	public void cargarEmpleados(List<Empleado> empleados) {
		llenar(modeloOrigenVenta, empleados);
		llenar(modeloDestinoVenta, empleados);
		llenar(modeloOrigenCobro, empleados);
		llenar(modeloDestinoCobro, empleados);
	}

	private void llenar(DefaultComboBoxModel<Empleado> modelo, List<Empleado> empleados) {
		modelo.removeAllElements();
		modelo.addElement(vacio());
		if (empleados != null) {
			for (Empleado e : empleados) {
				modelo.addElement(e);
			}
		}
	}

	private Empleado vacio() {
		Empleado e = new Empleado();
		e.setCodigo(0);
		e.setNombre("(seleccione)");
		e.setApellido("");
		return e;
	}

	/** Movimiento de la asignacion de venta tal como esta configurado en pantalla. */
	public MovimientoCartera getMovimientoVenta() {
		return new MovimientoCartera(chkVenta.isSelected(),
				codigoDe(cbxOrigenVenta), codigoDe(cbxDestinoVenta));
	}

	/** Movimiento de la cartera de cobro tal como esta configurado en pantalla. */
	public MovimientoCartera getMovimientoCobro() {
		return new MovimientoCartera(chkCobro.isSelected(),
				codigoDe(cbxOrigenCobro), codigoDe(cbxDestinoCobro));
	}

	private int codigoDe(JComboBox<Empleado> combo) {
		Empleado e = (Empleado) combo.getSelectedItem();
		return e == null ? 0 : e.getCodigo();
	}

	public String nombreOrigenVenta() { return String.valueOf(cbxOrigenVenta.getSelectedItem()); }
	public String nombreDestinoVenta() { return String.valueOf(cbxDestinoVenta.getSelectedItem()); }
	public String nombreOrigenCobro() { return String.valueOf(cbxOrigenCobro.getSelectedItem()); }
	public String nombreDestinoCobro() { return String.valueOf(cbxDestinoCobro.getSelectedItem()); }

	public TmCarteraClientes getModeloClientes() {
		return modeloClientes;
	}

	public JTable getTablaClientes() {
		return tablaClientes;
	}

	public void setResumen(String texto) {
		lblResumen.setText(texto);
	}
}
