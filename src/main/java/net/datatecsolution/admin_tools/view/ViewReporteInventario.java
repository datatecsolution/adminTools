package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlReporteInventario;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.FilaReporteInventario;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.TmReporteInventario;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ViewReporteInventario extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final NumberFormat MONEY = NumberFormat.getNumberInstance(new Locale("es", "HN"));

	private final JComboBox<Departamento> cbxBodega;
	private final JTextField txtBuscar;
	private final JButton btnBuscar;
	private final JButton btnAgregar;
	private final JButton btnQuitar;
	private final JButton btnLimpiar;
	private final JButton btnExportar;
	private final JCheckBox chkSoloComprasVentas;
	private final JTable tabla;
	private final TmReporteInventario modelo;
	private final JLabel lblTotalEfectivo;

	static {
		MONEY.setMinimumFractionDigits(2);
		MONEY.setMaximumFractionDigits(2);
	}

	public ViewReporteInventario(Window owner) {
		super(owner, "Reporte de inventario por selección", ModalityType.APPLICATION_MODAL);

		modelo = new TmReporteInventario();

		// --- Header ---
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(PanelPadre.color1);

		JPanel headerFila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		headerFila1.setBackground(PanelPadre.color1);
		headerFila1.add(new JLabel("Bodega:"));
		cbxBodega = new JComboBox<>();
		cbxBodega.setPreferredSize(new Dimension(220, 26));
		headerFila1.add(cbxBodega);

		headerFila1.add(Box.createHorizontalStrut(15));
		headerFila1.add(new JLabel("Artículo (código / barra):"));
		txtBuscar = new JTextField(18);
		headerFila1.add(txtBuscar);

		btnBuscar = new JButton("Buscar...");
		headerFila1.add(btnBuscar);

		btnAgregar = new JButton("Agregar");
		headerFila1.add(btnAgregar);

		JPanel headerFila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		headerFila2.setBackground(PanelPadre.color1);
		chkSoloComprasVentas = new JCheckBox("Solo compras y ventas");
		chkSoloComprasVentas.setBackground(PanelPadre.color1);
		chkSoloComprasVentas.setToolTipText("Excluye traslados, devoluciones, ajustes e inventario inicial");
		headerFila2.add(chkSoloComprasVentas);

		header.add(headerFila1);
		header.add(headerFila2);

		// --- Tabla ---
		tabla = new JTable(modelo);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.setRowHeight(22);
		tabla.setAutoCreateRowSorter(false);
		tabla.setDefaultRenderer(Object.class, new RendererInventario());

		tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
		tabla.getColumnModel().getColumn(1).setPreferredWidth(260);
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
		tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
		tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
		tabla.getColumnModel().getColumn(5).setPreferredWidth(100);
		tabla.getColumnModel().getColumn(6).setPreferredWidth(120);

		JScrollPane scroll = new JScrollPane(tabla);

		// --- Footer ---
		JPanel footer = new JPanel(new BorderLayout(8, 8));
		footer.setBackground(PanelPadre.color1);
		JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		footerLeft.setBackground(PanelPadre.color1);
		btnQuitar = new JButton("Quitar fila");
		btnLimpiar = new JButton("Limpiar");
		footerLeft.add(btnQuitar);
		footerLeft.add(btnLimpiar);

		JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
		footerRight.setBackground(PanelPadre.color1);
		lblTotalEfectivo = new JLabel("Total Efectivo: L. 0.00");
		lblTotalEfectivo.setFont(lblTotalEfectivo.getFont().deriveFont(Font.BOLD, 14f));
		btnExportar = new JButton("Exportar PDF");
		footerRight.add(lblTotalEfectivo);
		footerRight.add(Box.createHorizontalStrut(20));
		footerRight.add(btnExportar);

		footer.add(footerLeft, BorderLayout.WEST);
		footer.add(footerRight, BorderLayout.EAST);

		setLayout(new BorderLayout());
		getContentPane().setBackground(PanelPadre.color1);
		add(header, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);

		setSize(960, 520);
		setLocationRelativeTo(owner);
	}

	public void conectarControlador(CtlReporteInventario c) {
		cbxBodega.addActionListener(c);
		cbxBodega.setActionCommand("BODEGA_CAMBIA");

		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("AGREGAR_POR_TEXTO");

		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR_ARTICULO");

		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("AGREGAR_POR_TEXTO");

		btnQuitar.addActionListener(c);
		btnQuitar.setActionCommand("QUITAR");

		btnLimpiar.addActionListener(c);
		btnLimpiar.setActionCommand("LIMPIAR");

		btnExportar.addActionListener(c);
		btnExportar.setActionCommand("EXPORTAR");

		chkSoloComprasVentas.addActionListener(c);
		chkSoloComprasVentas.setActionCommand("FILTRO_CAMBIA");
	}

	public void cargarBodegas(java.util.List<Departamento> bodegas) {
		cbxBodega.removeAllItems();
		if (bodegas == null) return;
		for (Departamento d : bodegas) {
			cbxBodega.addItem(d);
		}
	}

	public Departamento getBodegaSeleccionada() {
		return (Departamento) cbxBodega.getSelectedItem();
	}

	public boolean isSoloComprasVentas() {
		return chkSoloComprasVentas.isSelected();
	}

	public TmReporteInventario getModelo() {
		return modelo;
	}

	public JTable getTabla() {
		return tabla;
	}

	public JTextField getTxtBuscar() {
		return txtBuscar;
	}

	public void actualizarTotal() {
		lblTotalEfectivo.setText("Total Efectivo: L. " + MONEY.format(modelo.getTotalEfectivo()));
	}

	private static class RendererInventario extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private static final Color BG_VACIO = new Color(245, 245, 245);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TmReporteInventario m = (TmReporteInventario) table.getModel();
			FilaReporteInventario f = m.get(row);
			boolean vacio = f.getSaldo() == 0 && f.getTotalIngreso() == 0 && f.getTotalSalida() == 0;

			if (column >= 3 && value instanceof Number) {
				setText(MONEY.format(((Number) value).doubleValue()));
				setHorizontalAlignment(SwingConstants.RIGHT);
			} else if (column == 0) {
				setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				setHorizontalAlignment(SwingConstants.LEFT);
			}

			if (!isSelected) {
				c.setBackground(vacio ? BG_VACIO : Color.WHITE);
				Font base = table.getFont();
				c.setFont(vacio ? base.deriveFont(Font.ITALIC) : base.deriveFont(Font.PLAIN));
			}
			return c;
		}
	}
}
