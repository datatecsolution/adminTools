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
import java.awt.*;


public class ViewAgregarCompras extends JDialog {
	/**
	 * Vista para ingresar las facturasd el la compra
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtIdProveedor;
	private JTextField txtNombreproveedor;
	protected JPanel panelProveedor;
	private JTextField txtTelefonoProveedor;
	
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
	
	
	public ViewAgregarCompras(Window view) {
		super(view,"Registrar Compras", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		modeloCbx=new CbxTmDepartamento();//comentar para ver en forma de dise�o
		
		panelProveedor=new PanelPadre();
		panelProveedor.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Proveedor", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelProveedor.setBounds(10, 90, 1007, 78);
		panelProveedor.setLayout(null);
		
		Color color1 =new Color(60, 179, 113);
		Color color2 =Color.decode("#33cccc");
		Color color3 =Color.decode("#d4f4ff");
		Color color4 =Color.decode("#f4fbfe");
		
		this.getContentPane().setBackground(color3);
		
		//JPanel panelProveedor = new JPanel();
		
		//
		
		JLabel lblIdProveedor = new JLabel("Id");
		lblIdProveedor.setBounds(15, 21, 72, 14);
		panelProveedor.add(lblIdProveedor);
		
		txtIdProveedor = new JTextField();
		txtIdProveedor.setBounds(15, 36, 104, 31);
		txtIdProveedor.setToolTipText("Id Proveedor");
		panelProveedor.add(txtIdProveedor);
		txtIdProveedor.setColumns(10);
		
		JLabel lblNombreProveedor = new JLabel("Nombre");
		lblNombreProveedor.setBounds(181, 21, 104, 14);
		panelProveedor.add(lblNombreProveedor);
		
		txtNombreproveedor = new JTextField();
		txtNombreproveedor.setEditable(false);
		txtNombreproveedor.setBounds(181, 36, 276, 31);
		panelProveedor.add(txtNombreproveedor);
		txtNombreproveedor.setColumns(10);
		
		getContentPane().add(panelProveedor);
		
		JLabel lblTelefono = new JLabel("Telefono");
		lblTelefono.setBounds(472, 21, 58, 14);
		panelProveedor.add(lblTelefono);
		
		txtTelefonoProveedor = new JTextField();
		txtTelefonoProveedor.setEditable(false);
		txtTelefonoProveedor.setBounds(472, 36, 276, 31);
		panelProveedor.add(txtTelefonoProveedor);
		txtTelefonoProveedor.setColumns(10);
		
		btnBuscar = new JButton("...");
		btnBuscar.setBounds(116, 38, 32, 29);
		panelProveedor.add(btnBuscar);
		
		JPanel panelInfoCompra = new PanelPadre();
		panelInfoCompra.setBounds(10, 11, 1007, 80);
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
		txtNofactura.setBounds(149, 36, 102, 31);
		panelInfoCompra.add(txtNofactura);
		txtNofactura.setColumns(10);
		
		grupoOpciones = new ButtonGroup();
		rdbtnCredito = new JRadioButton("");
		rdbtnCredito.setBounds(340, 40, 40, 23);
		grupoOpciones.add(rdbtnCredito);
		panelInfoCompra.add(rdbtnCredito);
		
		rdbtnContado = new JRadioButton("");
		rdbtnContado.setSelected(true);
		rdbtnContado.setBounds(274, 40, 40, 23);
		grupoOpciones.add(rdbtnContado);
		panelInfoCompra.add(rdbtnContado);
		
		lblFechaVencimiento = new JLabel("Vencimiento");
		lblFechaVencimiento.setBounds(405, 18, 90, 14);
		panelInfoCompra.add(lblFechaVencimiento);
		
		dateVencFactura = new JDateChooser();
		dateVencFactura.setDateFormatString("dd-MM-yyyy");
		dateVencFactura.setBounds(405, 36, 129, 31);
		dateVencFactura.setEnabled(false);
		panelInfoCompra.add(dateVencFactura);
		
		JLabel lblDepartementoDeLa = new JLabel("Departemento de la compra");
		lblDepartementoDeLa.setBounds(544, 18, 200, 14);
		panelInfoCompra.add(lblDepartementoDeLa);
		
		lblContado = new JLabel("Contado");
		lblContado.setBounds(274, 17, 61, 16);
		panelInfoCompra.add(lblContado);
		
		lblCredito = new JLabel("Credito");
		lblCredito.setBounds(340, 17, 61, 16);
		panelInfoCompra.add(lblCredito);
		
	
		cbxDepart = new JComboBox();
		cbxDepart.setModel(modeloCbx);
		cbxDepart.setBounds(544, 36, 210, 31);
		panelInfoCompra.add(cbxDepart);
		
		//botones
		btnGuardar = new BotonGuardar();
		btnGuardar.setSize(128, 72);
		btnGuardar.setLocation(42, 559);
		//tnCancelar.setLocation(42, 175);
		getContentPane().add(btnGuardar);
		btnActualizar=new BotonActualizar();
		btnActualizar.setSize(128, 72);
		btnActualizar.setLocation(42, 559);
		getContentPane().add(btnActualizar);
		btnActualizar.setVisible(false);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setSize(128, 72);
		
		//btnCancelar.setBounds(212, 175, 135, 39);
		btnCancelar.setLocation(270, 559);
		getContentPane().add(btnCancelar);
		
		
		//tabla de registro de los proveedores
		tablaArticulos=new JTable();
		
		TrProveedor renderizador = new TrProveedor();
		tablaArticulos.setDefaultRenderer(String.class, renderizador);
		
		modelo = new DmtFacturaProveedores();//se crea el modelo de los datos de la tabla
		
		
		tablaArticulos.setModel(modelo);
		tablaArticulos.getColumnModel().getColumn(0).setPreferredWidth(100);     //Tama�o de las columnas de las tablas
		tablaArticulos.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tablaArticulos.getColumnModel().getColumn(2).setPreferredWidth(70);	//
		tablaArticulos.getColumnModel().getColumn(3).setPreferredWidth(90);	//
		tablaArticulos.getColumnModel().getColumn(4).setPreferredWidth(90);	//
		tablaArticulos.getColumnModel().getColumn(5).setPreferredWidth(90);	//
		tablaArticulos.getColumnModel().getColumn(5).setPreferredWidth(90);	//
		tablaArticulos.setRowHeight(25);
		//tablaArticulos.getColumnModel().getColumn(6).setPreferredWidth(100);	//
		//Estitlo para la tabla		
		
		JScrollPane scrollPane = new JScrollPane(tablaArticulos);
		scrollPane.setBounds(10, 171, 1007, 301);
		scrollPane.setBackground(color3);
		scrollPane.getViewport().setBackground(color3);
		getContentPane().add(scrollPane);
		
		panel = new PanelPadre();
		panel.setBounds(664, 492, 330, 160);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		txtTotalimpuesto15 = new JTextField();
		txtTotalimpuesto15.setBounds(146, 44, 184, 34);
		panel.add(txtTotalimpuesto15);
		txtTotalimpuesto15.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotalimpuesto15.setEditable(false);
		txtTotalimpuesto15.setColumns(10);
		
		txtTotal = new JTextField();
		txtTotal.setBounds(146, 120, 184, 34);
		panel.add(txtTotal);
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setEditable(false);
		txtTotal.setColumns(10);
		
		JLabel lblTotalImpuesto = new JLabel("Total Impuesto 15%");
		lblTotalImpuesto.setBounds(20, 54, 128, 14);
		panel.add(lblTotalImpuesto);
		
		JLabel lblTotalFactura = new JLabel("Total Factura");
		lblTotalFactura.setBounds(20, 122, 84, 14);
		panel.add(lblTotalFactura);
		
		JLabel lblSubtotal = new JLabel("SubTotal");
		lblSubtotal.setBounds(20, 20, 84, 14);
		panel.add(lblSubtotal);
		
		txtSubtotal = new JTextField();
		txtSubtotal.setBounds(146, 4, 184, 36);
		panel.add(txtSubtotal);
		txtSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtSubtotal.setEditable(false);
		txtSubtotal.setColumns(10);
		
		txtTotalImpusto18 = new JTextField();
		txtTotalImpusto18.setBounds(146, 82, 184, 34);
		panel.add(txtTotalImpusto18);
		txtTotalImpusto18.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotalImpusto18.setEditable(false);
		txtTotalImpusto18.setColumns(10);
		
		JLabel lblTotalImpuesto_1 = new JLabel("Total Impuesto 18%");
		lblTotalImpuesto_1.setBounds(20, 88, 128, 14);
		panel.add(lblTotalImpuesto_1);
		
		
		
		///DetalleFacturaProveedor uno= new DetalleFacturaProveedor();
		
		//modelo.agregarDetalle(uno);
		this.setSize(1050, 700);
		
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
	public JTextField gettxtTelefonoProveedor(){
		return txtTelefonoProveedor;
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
		
		
		this.rdbtnCredito.addActionListener(c);
		this.rdbtnCredito.setActionCommand("CREDITO");
		rdbtnCredito.addKeyListener(c);
		
		this.btnCancelar.addActionListener(c);
		this.btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addKeyListener(c);
		
		this.addWindowListener(c);
		
		txtTelefonoProveedor.addKeyListener(c);
		
		tablaArticulos.addKeyListener(c);
		txtNofactura.addKeyListener(c);
		dateCompra.getDateEditor().getUiComponent().addKeyListener(c);
		
		dateVencFactura.addKeyListener(c);
		dateVencFactura.getDateEditor().getUiComponent().addKeyListener(c);
		txtTotalimpuesto15.addKeyListener(c);
		txtTotal.addKeyListener(c);
		txtSubtotal.addKeyListener(c);
	
		/*rdbtnTodos.addItemListener(c);
		
		
		rdbtnId.addActionListener(c);
		rdbtnId.addItemListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnObservacion.addActionListener(c);
		rdbtnObservacion.addItemListener(c);
		rdbtnObservacion.setActionCommand("OBSERVACION");
		
		rdbtnMarca.addActionListener(c);
		rdbtnMarca.addItemListener(c);
		rdbtnMarca.setActionCommand("MARCA");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 
		 btnLimpiar.addActionListener(c);
		 btnLimpiar.setActionCommand("LIMPIAR");*/
		 
		 tablaArticulos.addMouseListener(c);
		// tablaArticulos.getModel().addTableModelListener(c);
		/* tablaArticulos.getModel().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {
					// TODO Auto-generated method stub
					JOptionPane.showMessageDialog(null, "Se modifico el dato en la celda "+e.getColumn()+", "+e.getFirstRow());
				}
	        });*/
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
}
