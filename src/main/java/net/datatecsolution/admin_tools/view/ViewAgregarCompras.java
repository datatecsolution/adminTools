package net.datatecsolution.admin_tools.view;


import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlCompras;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.rendes.TrProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmDepartamento;
import net.datatecsolution.admin_tools.view.tablemodel.DmtFacturaProveedores;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Date;


public class ViewAgregarCompras extends JDialog {
	/**
	 * Vista para ingresar las facturasd el la compra
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtIdProveedor;
	private JTextField txtNombreproveedor;

	private JTable tablaArticulos;
	private DmtFacturaProveedores modelo;
	private JTextField txtNofactura;
	private ButtonGroup grupoOpciones;
	private JRadioButton rdbtnCredito;
	private JRadioButton rdbtnContado;


	private BotonCancelar btnCancelar;
	private BotonGuardar btnGuardar;
	private BotonActualizar btnActualizar;

	private JDateChooser dateCompra;
	private JDateChooser dateVencFactura;
	private JTextField txtTotalimpuesto15;
	private JTextField txtTotal;
	private JLabel lblFechaVencimiento;
	private JTextField txtSubtotal;

	private CbxTmDepartamento modeloCbx;
	private JComboBox cbxDepart;
	private JLabel lblContado;
	private JLabel lblCredito;
	private JButton btnBuscar;
	private JTextField txtTotalImpusto18;
	private JPanel panel;
	private JCheckBox chckbxIVAincluido;


	public ViewAgregarCompras(Window view) {
		super(view,"Registrar Compras", ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Evita que se cierre la ventana al hacer clic en el botón de cierre
		getContentPane().setLayout(null);
		modeloCbx=new CbxTmDepartamento();

		Color color1 =new Color(60, 179, 113);
		Color color2 =Color.decode("#33cccc");
		Color color3 =Color.decode("#d4f4ff");
		Color color4 =Color.decode("#f4fbfe");

		this.getContentPane().setBackground(color3);

		JPanel panelInfoCompra = new PanelPadre();
		panelInfoCompra.setBounds(10, 11, 1350, 80);
		panelInfoCompra.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		getContentPane().add(panelInfoCompra);
		panelInfoCompra.setLayout(null);

		JLabel lblFecha = new JLabel("Fecha Factura");
		lblFecha.setBounds(10, 18, 90, 14);
		panelInfoCompra.add(lblFecha);

		dateCompra = new JDateChooser("dd/MM/yyyy", "##/##/####", '_');
		dateCompra.setBounds(10, 36, 134, 31);

		panelInfoCompra.add(dateCompra);
		dateCompra.setDateFormatString("dd-MM-yyyy");
		//dateCompra.setDate(Date.);

		JLabel lblNoFactura = new JLabel("No Factura");
		lblNoFactura.setBounds(149, 18, 90, 14);
		panelInfoCompra.add(lblNoFactura);

		txtNofactura = new JTextField();
		txtNofactura.setBounds(149, 36, 192, 31);
		panelInfoCompra.add(txtNofactura);
		txtNofactura.setColumns(10);

		grupoOpciones = new ButtonGroup();
		rdbtnCredito = new JRadioButton("");
		rdbtnCredito.setBounds(437, 42, 40, 23);
		grupoOpciones.add(rdbtnCredito);
		panelInfoCompra.add(rdbtnCredito);

		rdbtnContado = new JRadioButton("");
		rdbtnContado.setSelected(true);
		rdbtnContado.setBounds(371, 42, 40, 23);
		grupoOpciones.add(rdbtnContado);
		panelInfoCompra.add(rdbtnContado);

		lblFechaVencimiento = new JLabel("Vencimiento");
		lblFechaVencimiento.setBounds(502, 20, 90, 14);
		panelInfoCompra.add(lblFechaVencimiento);

		dateVencFactura = new JDateChooser();
		dateVencFactura.setDateFormatString("dd-MM-yyyy");
		dateVencFactura.setBounds(502, 38, 129, 31);
		dateVencFactura.setEnabled(false);
		panelInfoCompra.add(dateVencFactura);

		JLabel lblDepartementoDeLa = new JLabel("Departemento de la compra");
		lblDepartementoDeLa.setBounds(641, 20, 200, 14);
		panelInfoCompra.add(lblDepartementoDeLa);

		lblContado = new JLabel("Contado");
		lblContado.setBounds(361, 19, 61, 16);
		panelInfoCompra.add(lblContado);

		lblCredito = new JLabel("Credito");
		lblCredito.setBounds(427, 19, 61, 16);
		panelInfoCompra.add(lblCredito);


		cbxDepart = new JComboBox();
		cbxDepart.setModel(modeloCbx);
		cbxDepart.setBounds(641, 38, 210, 31);
		panelInfoCompra.add(cbxDepart);

		JLabel lblNombreProveedor = new JLabel("Proveedor");
		lblNombreProveedor.setBounds(855, 20, 104, 14);
		panelInfoCompra.add(lblNombreProveedor);

		txtNombreproveedor = new JTextField();
		txtNombreproveedor.setBounds(899, 38, 292, 31);
		panelInfoCompra.add(txtNombreproveedor);
		txtNombreproveedor.setEditable(false);
		txtNombreproveedor.setColumns(10);

		txtIdProveedor = new JTextField();
		txtIdProveedor.setText("ID");
		txtIdProveedor.setBounds(855, 38, 45, 31);
		panelInfoCompra.add(txtIdProveedor);
		txtIdProveedor.setToolTipText("Id Proveedor");
		txtIdProveedor.setColumns(10);

		btnBuscar = new JButton("...");
		btnBuscar.setBounds(1190, 37, 32, 29);
		panelInfoCompra.add(btnBuscar);

		chckbxIVAincluido = new JCheckBox("");
		chckbxIVAincluido.setToolTipText("Incluir impuesto en factura?");
		chckbxIVAincluido.setSelected(true);
		chckbxIVAincluido.setBounds(1269, 36, 32, 23);
		panelInfoCompra.add(chckbxIVAincluido);

		JLabel lblIVAincluido = new JLabel("Imp/Venta");
		lblIVAincluido.setBounds(1250, 18, 71, 16);
		panelInfoCompra.add(lblIVAincluido);

		//botones
		btnGuardar = new BotonGuardar();
		btnGuardar.setSize(128, 64);
		btnGuardar.setLocation(20, 626);
		//tnCancelar.setLocation(42, 175);
		getContentPane().add(btnGuardar);
		btnActualizar=new BotonActualizar();
		btnActualizar.setSize(128, 64);
		btnActualizar.setLocation(20, 626);
		getContentPane().add(btnActualizar);
		btnActualizar.setVisible(false);

		btnCancelar = new BotonCancelar();
		btnCancelar.setSize(128, 64);

		//btnCancelar.setBounds(212, 175, 135, 39);
		btnCancelar.setLocation(174, 626);
		getContentPane().add(btnCancelar);


		//tabla de registro de los proveedores
		tablaArticulos=new JTable();
		tablaArticulos.setDefaultRenderer(Object.class, new TrProveedor());


		modelo = new DmtFacturaProveedores();//se crea el modelo de los datos de la tabla


		tablaArticulos.setModel(modelo);
		tablaArticulos.getColumnModel().getColumn(0).setPreferredWidth(50);     //Tama�o de las columnas de las tablas
		tablaArticulos.getColumnModel().getColumn(1).setPreferredWidth(250);	//
		tablaArticulos.getColumnModel().getColumn(2).setPreferredWidth(50);	//
		tablaArticulos.getColumnModel().getColumn(3).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(4).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(5).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(6).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(7).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(8).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(9).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(10).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(11).setPreferredWidth(100);	//
		tablaArticulos.setRowHeight(25);
		tablaArticulos.getColumnModel().getColumn(11).setCellEditor(new DateCellEditor());
		//tablaArticulos.getColumnModel().getColumn(6).setPreferredWidth(100);	//
		//Estitlo para la tabla

		JScrollPane scrollPane = new JScrollPane(tablaArticulos);
		scrollPane.setBounds(10, 103, 1350, 511);
		scrollPane.setBackground(color3);
		scrollPane.getViewport().setBackground(color3);
		getContentPane().add(scrollPane);

		panel = new PanelPadre();
		panel.setBounds(499, 626, 861, 64);
		getContentPane().add(panel);
		panel.setLayout(null);

		txtTotalimpuesto15 = new JTextField();
		txtTotalimpuesto15.setBounds(230, 24, 184, 34);
		panel.add(txtTotalimpuesto15);
		txtTotalimpuesto15.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotalimpuesto15.setEditable(false);
		txtTotalimpuesto15.setColumns(10);

		txtTotal = new JTextField();
		txtTotal.setBounds(644, 24, 184, 34);
		panel.add(txtTotal);
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setEditable(false);
		txtTotal.setColumns(10);

		JLabel lblTotalImpuesto = new JLabel("Total Impuesto 15%");
		lblTotalImpuesto.setBounds(230, 5, 128, 14);
		panel.add(lblTotalImpuesto);

		JLabel lblTotalFactura = new JLabel("Total Factura");
		lblTotalFactura.setBounds(644, 5, 84, 14);
		panel.add(lblTotalFactura);

		JLabel lblSubtotal = new JLabel("SubTotal");
		lblSubtotal.setBounds(23, 4, 84, 14);
		panel.add(lblSubtotal);

		txtSubtotal = new JTextField();
		txtSubtotal.setBounds(23, 22, 184, 36);
		panel.add(txtSubtotal);
		txtSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtSubtotal.setEditable(false);
		txtSubtotal.setColumns(10);

		txtTotalImpusto18 = new JTextField();
		txtTotalImpusto18.setBounds(437, 24, 184, 34);
		panel.add(txtTotalImpusto18);
		txtTotalImpusto18.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotalImpusto18.setEditable(false);
		txtTotalImpusto18.setColumns(10);

		JLabel lblTotalImpuesto_1 = new JLabel("Total Impuesto 18%");
		lblTotalImpuesto_1.setBounds(437, 5, 128, 14);
		panel.add(lblTotalImpuesto_1);



		///DetalleFacturaProveedor uno= new DetalleFacturaProveedor();

		//modelo.agregarDetalle(uno);
		this.setSize(1366, 768);

		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);






	}
	public JComboBox getCbxDepart(){
		return cbxDepart;
	}
	public CbxTmDepartamento getModeloCbx(){
		return modeloCbx;
	}
	public JTextField getTxtNofactura(){
		return txtNofactura;
	}
	public JTextField getTxtSubtotal(){
		return txtSubtotal;
	}

	public JTextField getTxtTotal(){
		return txtTotal;
	}
	public JTextField getTxtTotalimpuesto(){
		return  txtTotalimpuesto15;
	}
	public JDateChooser getDateCompra(){
		return dateCompra;
	}
	public JDateChooser getDateVencFactura(){
		return dateVencFactura;
	}
	public JTextField getTxtNombreproveedor(){
		return txtNombreproveedor;
	}
	public JTextField getTxtIdProveedor(){
		return txtIdProveedor;
	}
	public DmtFacturaProveedores getModelo(){
		return modelo;
	}
	public JTable getTablaArticulos(){
		return tablaArticulos;
	}
	public JCheckBox getChckbxIVAincluido() {
		return chckbxIVAincluido;
	}
	public void setChckbxIVAincluido(JCheckBox chckbxIVAincluido) {
		this.chckbxIVAincluido = chckbxIVAincluido;
	}
	public void conectarControlador(CtlCompras c){


		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCARPROVEEDOR2");

		this.txtIdProveedor.addActionListener(c);
		this.txtIdProveedor.setActionCommand("BUSCARPROVEEDOR");
		txtIdProveedor.addKeyListener(c);


		this.btnGuardar.addActionListener(c);
		this.btnGuardar.setActionCommand("GUARDARCOMPRA");
		btnGuardar.addKeyListener(c);


		this.rdbtnContado.addActionListener(c);
		this.rdbtnContado.setActionCommand("CONTADO");
		rdbtnContado.addKeyListener(c);


		chckbxIVAincluido.addActionListener(c);
		chckbxIVAincluido.setActionCommand("CAL_IMPUESTO");
		chckbxIVAincluido.addKeyListener(c);


		this.rdbtnCredito.addActionListener(c);
		this.rdbtnCredito.setActionCommand("CREDITO");
		rdbtnCredito.addKeyListener(c);

		this.btnCancelar.addActionListener(c);
		this.btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addKeyListener(c);

		this.addWindowListener(c);

		tablaArticulos.addKeyListener(c);
		txtNofactura.addKeyListener(c);
		dateCompra.getDateEditor().getUiComponent().addKeyListener(c);

		dateVencFactura.addKeyListener(c);
		dateVencFactura.getDateEditor().getUiComponent().addKeyListener(c);
		txtTotalimpuesto15.addKeyListener(c);
		txtTotal.addKeyListener(c);
		txtSubtotal.addKeyListener(c);


		tablaArticulos.addMouseListener(c);
		modelo.addTableModelListener(c);
		//tablaArticulos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaArticulos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tablaArticulos.setColumnSelectionAllowed(true);
		tablaArticulos.setRowSelectionAllowed(true);
	}
	/**
	 * @return the txtTotalImpusto18
	 */
	public JTextField getTxtTotalImpusto18() {
		return txtTotalImpusto18;
	}

	class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
		private JDateChooser dateChooser;

		public DateCellEditor() {
			dateChooser = new JDateChooser("dd/MM/yyyy", "##/##/####", '_');
		}

		@Override
		public Object getCellEditorValue() {
			return dateChooser.getDate();
		}

		@Override
		public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value instanceof Date) {
				dateChooser.setDate((Date) value);
			}
			return dateChooser;
		}
	}
}
