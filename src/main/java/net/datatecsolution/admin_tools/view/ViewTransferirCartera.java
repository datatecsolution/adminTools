package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlTransferirCartera;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonTransferir;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.TmCarteraClientes;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * US-127 — pantalla para transferir la cartera de clientes de un vendedor a
 * otro.
 *
 * El usuario elige origen y destino, decide si mueve la asignacion de venta
 * ({@code cliente.id_vendedor}), la cartera de cobro ({@code id_cobrador}) o
 * ambas, y destilda de la tabla los clientes que quiera dejar donde estan.
 *
 * @author jdmayorga
 */
public class ViewTransferirCartera extends JDialog {

	private final JComboBox<Empleado> cbxOrigen;
	private final JComboBox<Empleado> cbxDestino;
	private final DefaultComboBoxModel<Empleado> modeloOrigen;
	private final DefaultComboBoxModel<Empleado> modeloDestino;

	private final JCheckBox chkVenta;
	private final JCheckBox chkCobro;

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
		this.setSize(900, 660);
		this.setLocationRelativeTo(view);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);

		JLabel lblOrigen = new JLabel("Vendedor de origen");
		lblOrigen.setBounds(20, 15, 200, 15);
		getContentPane().add(lblOrigen);

		modeloOrigen = new DefaultComboBoxModel<Empleado>();
		cbxOrigen = new JComboBox<Empleado>(modeloOrigen);
		cbxOrigen.setBounds(20, 33, 400, 30);
		getContentPane().add(cbxOrigen);

		JLabel lblDestino = new JLabel("Vendedor de destino");
		lblDestino.setBounds(450, 15, 200, 15);
		getContentPane().add(lblDestino);

		modeloDestino = new DefaultComboBoxModel<Empleado>();
		cbxDestino = new JComboBox<Empleado>(modeloDestino);
		cbxDestino.setBounds(450, 33, 400, 30);
		getContentPane().add(cbxDestino);

		JPanel panelQue = new JPanel();
		panelQue.setLayout(null);
		panelQue.setBackground(PanelPadre.color1);
		panelQue.setBorder(new TitledBorder(null, "Que transferir", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panelQue.setBounds(20, 75, 830, 60);
		getContentPane().add(panelQue);

		chkVenta = new JCheckBox("Asignacion de venta (vendedor)");
		chkVenta.setSelected(true);
		chkVenta.setBackground(PanelPadre.color1);
		chkVenta.setBounds(15, 22, 280, 25);
		panelQue.add(chkVenta);

		chkCobro = new JCheckBox("Cartera de cobro (cobrador)");
		chkCobro.setSelected(true);
		chkCobro.setBackground(PanelPadre.color1);
		chkCobro.setBounds(310, 22, 280, 25);
		panelQue.add(chkCobro);

		modeloClientes = new TmCarteraClientes();
		tablaClientes = new JTable(modeloClientes);
		tablaClientes.setRowHeight(24);
		tablaClientes.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		ajustarAnchoColumnas();

		JScrollPane scrollClientes = new JScrollPane(tablaClientes);
		scrollClientes.setViewportBorder(new TitledBorder(null, "Clientes de la cartera", TitledBorder.LEFT,
				TitledBorder.TOP, null, null));
		scrollClientes.setBounds(20, 145, 830, 360);
		getContentPane().add(scrollClientes);

		btnMarcarTodos = new JButton("Marcar todos");
		btnMarcarTodos.setBounds(20, 515, 140, 28);
		getContentPane().add(btnMarcarTodos);

		btnDesmarcarTodos = new JButton("Desmarcar todos");
		btnDesmarcarTodos.setBounds(170, 515, 150, 28);
		getContentPane().add(btnDesmarcarTodos);

		lblResumen = new JLabel(" ");
		lblResumen.setBounds(335, 515, 515, 28);
		getContentPane().add(lblResumen);

		btnTransferir = new BotonTransferir();
		// El icono es compartido con la transferencia de saldo entre cuentas,
		// que trae su propio tooltip; aca significa otra cosa.
		btnTransferir.setToolTipText("Transferir la cartera seleccionada al vendedor de destino");
		btnTransferir.setLocation(280, 555);
		getContentPane().add(btnTransferir);

		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(490, 555);
		getContentPane().add(btnCancelar);
	}

	private void ajustarAnchoColumnas() {
		int[] anchos = { 30, 70, 320, 110, 80, 110, 100 };
		for (int i = 0; i < anchos.length && i < tablaClientes.getColumnCount(); i++) {
			tablaClientes.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
		}
		tablaClientes.getColumnModel().getColumn(TmCarteraClientes.COL_SELECCION).setMaxWidth(30);
	}

	public void conectarCtl(CtlTransferirCartera c) {
		cbxOrigen.addActionListener(c);
		cbxOrigen.setActionCommand("CAMBIO_ORIGEN");

		cbxDestino.addActionListener(c);
		cbxDestino.setActionCommand("CAMBIO_DESTINO");

		chkVenta.addActionListener(c);
		chkVenta.setActionCommand("CAMBIO_ROLES");

		chkCobro.addActionListener(c);
		chkCobro.setActionCommand("CAMBIO_ROLES");

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
	 * Llena los dos combos con la misma lista de empleados, precedida por un
	 * elemento vacio para que ninguno arranque preseleccionado (una
	 * transferencia mal disparada por descuido no tiene deshacer).
	 */
	public void cargarEmpleados(List<Empleado> empleados) {
		modeloOrigen.removeAllElements();
		modeloDestino.removeAllElements();

		modeloOrigen.addElement(vacio());
		modeloDestino.addElement(vacio());

		if (empleados != null) {
			for (Empleado e : empleados) {
				modeloOrigen.addElement(e);
				modeloDestino.addElement(e);
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

	public Empleado getOrigenSeleccionado() {
		return (Empleado) cbxOrigen.getSelectedItem();
	}

	public Empleado getDestinoSeleccionado() {
		return (Empleado) cbxDestino.getSelectedItem();
	}

	public boolean isMoverVenta() {
		return chkVenta.isSelected();
	}

	public boolean isMoverCobro() {
		return chkCobro.isSelected();
	}

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
