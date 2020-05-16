package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFacturaPagos;
import net.datatecsolution.admin_tools.view.botones.BotonBuscarClientes;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFactura;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturasPago;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCxCPagos extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	
	private JTable tabla;
	private TmFacturasPago modelo;
	private JPanel panelAcciones;
	private JPanel panelDatosFactura;
	private JLabel lblFecha;
	private JTextField txtFechafactura;
	private JLabel lblCodigoCliente;
	private JTextField txtIdcliente;
	private JTextField txtNombrecliente;
	
	private ButtonGroup grupoOpciones;
	private BotonCancelar btnCerrar;
	private BotonBuscarClientes btnCliente;
	private BotonCobrar btnCobrar;
	private JLabel lblNombreCliente;
	private JTextField txtLimitecredito;
	private JTextField txtSaldocliente;
	
	
	private JTextField txtTotal;
	private JLabel lblTotal;
	
	private JCheckBox chckbxSeleccionarTodos;

	public ViewCxCPagos(Window view) {
		
		super(view,"Pago de facturas", ModalityType.DOCUMENT_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewFacturar.class.getResource("/drawable/logo-admin-tool1.png")));
		panelAcciones=new JPanel();
		panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAcciones.setBounds(20, 11, 178, 459);
		panelAcciones.setLayout(null);
		//panelAcciones.setVisible(false);
		
		
		//this.setTitle("Facturar");
		getContentPane().add(panelAcciones);
		//btnBuscar.getInputMap().put(KeyStroke.getKeyStroke("F1"), sumar());
		
		btnCliente = new BotonBuscarClientes();
		btnCliente.setText("F3 Clientes");
		btnCliente.setHorizontalAlignment(SwingConstants.LEFT);
		btnCliente.setBounds(10, 19, 158, 38);
		panelAcciones.add(btnCliente);
		
		btnCobrar = new BotonCobrar();
		btnCobrar.setText("F2 Cobrar");
		btnCobrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCobrar.setBounds(10, 76, 158, 38);
		
		panelAcciones.add(btnCobrar);
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setBounds(10, 133, 158, 38);
		panelAcciones.add(btnCerrar);
		
		
		
		
		panelDatosFactura=new JPanel();
		//panelDatosFactura.setBackground(Color.WHITE);
		panelDatosFactura.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelDatosFactura.setBounds(208, 11, 790, 84);
		panelDatosFactura.setLayout(null);
		
		getContentPane().add(panelDatosFactura);
		
		lblFecha = new JLabel("Fecha");
		lblFecha.setBounds(20, 23, 40, 14);
		panelDatosFactura.add(lblFecha);
		
		txtFechafactura = new JTextField();
		txtFechafactura.setEditable(false);
		txtFechafactura.setBounds(20, 44, 104, 29);
		panelDatosFactura.add(txtFechafactura);
		txtFechafactura.setColumns(10);
		
		lblCodigoCliente = new JLabel("Id Cliente");
		lblCodigoCliente.setBounds(156, 23, 61, 14);
		panelDatosFactura.add(lblCodigoCliente);
		
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 45);
		
		txtIdcliente = new JTextField();
		txtIdcliente.setBounds(156, 44, 67, 29);
		panelDatosFactura.add(txtIdcliente);
		txtIdcliente.setColumns(10);
		
		txtNombrecliente = new JTextField();
		txtNombrecliente.setToolTipText("Nombre Cliente");
		txtNombrecliente.setBounds(233, 44, 214, 29);
		panelDatosFactura.add(txtNombrecliente);
		txtNombrecliente.setColumns(10);
		
		grupoOpciones = new ButtonGroup();
		
		lblNombreCliente = new JLabel("Nombre Cliente");
		lblNombreCliente.setBounds(233, 23, 104, 14);
		panelDatosFactura.add(lblNombreCliente);
		
		JLabel lblSaldoCliente = new JLabel("Saldo Cliente");
		lblSaldoCliente.setBounds(574, 23, 86, 14);
		panelDatosFactura.add(lblSaldoCliente);
		
		JLabel lblLimiteDeCredito = new JLabel("Limite de credito");
		lblLimiteDeCredito.setBounds(461, 23, 103, 14);
		panelDatosFactura.add(lblLimiteDeCredito);
		
		txtLimitecredito = new JTextField();
		txtLimitecredito.setBounds(461, 44, 103, 29);
		panelDatosFactura.add(txtLimitecredito);
		txtLimitecredito.setColumns(10);
		
		txtSaldocliente = new JTextField();
		txtSaldocliente.setBounds(574, 44, 86, 29);
		panelDatosFactura.add(txtSaldocliente);
		txtSaldocliente.setColumns(10);
		
		
		
		
		tabla = new JTable();
		modelo=new TmFacturasPago();
		tabla.setModel(modelo);
		
		RenderizadorTablaFactura renderizador = new RenderizadorTablaFactura();
		tabla.setDefaultRenderer(String.class, renderizador);
		//tableDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tabla.getColumnModel().getColumn(0).setPreferredWidth(100);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(80);	//
		tabla.getColumnModel().getColumn(3).setPreferredWidth(80);	//
		//tabla.getColumnModel().getColumn(4).setPreferredWidth(80);	//
		
		//registerEnterKey( );
		
		JScrollPane scrollPane = new JScrollPane(tabla);
		scrollPane.setBounds(208, 145, 790, 325);
		getContentPane().add(scrollPane);
		
		
		getContentPane().setLayout(null);
				
		
		txtTotal = new JTextField();
		txtTotal.setForeground(Color.RED);
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setFont(myFont);
		txtTotal.setText("00");
		txtTotal.setEditable(false);
		txtTotal.setBounds(778, 506, 220, 44);
		getContentPane().add(txtTotal);
		txtTotal.setColumns(10);
		
		lblTotal = new JLabel("Total");
		lblTotal.setBounds(780, 490, 46, 14);
		getContentPane().add(lblTotal);
		
		chckbxSeleccionarTodos = new JCheckBox("Seleccionar todos");
		chckbxSeleccionarTodos.setBounds(867, 116, 131, 23);
		getContentPane().add(chckbxSeleccionarTodos);
		
		setSize(1024, 600);
		
		this.setPreferredSize(new Dimension(1024, 600));
		this.setResizable(false);
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.pack();
		
		
	}
	
	
	
	public JButton getBtnBuscarCliente(){
		return btnCliente;
	}
	public JButton getBtnCobrar(){
		return btnCobrar;
	}
	public JButton getBtnCerrar(){
		return btnCerrar;
	}
	
	public JPanel getPanelAcciones(){
		return panelAcciones;
	}
	
	public JTextField getTxtTotal(){
		return txtTotal;
	}
	public JTextField getTxtNombrecliente(){
		return txtNombrecliente;
	}
	public JTextField getTxtIdcliente(){
		return txtIdcliente;
	}
	public TmFacturasPago getModeloTabla(){
		return modelo;
	}
	public JTable geTabla(){
		return tabla;
	}
	
	public JTextField getTxtFechafactura(){
		return txtFechafactura;
	}
	public JCheckBox getChckbxTodos(){
		return chckbxSeleccionarTodos;
	}
	public JTextField getTxtLimiteCredito(){
		return this.txtLimitecredito;
	}
	public JTextField getTxtSaldo(){
		return this.txtSaldocliente;
	}
	public void conectarContralador(CtlFacturaPagos c){
		
		txtIdcliente.addActionListener(c);
		txtIdcliente.setActionCommand("BUSCARCLIENTE");
		
		tabla.addKeyListener(c);
		tabla.addMouseListener(c);
		modelo.addTableModelListener(c);
		//tableDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabla.setColumnSelectionAllowed(true);
		tabla.setRowSelectionAllowed(true);
		tabla.setCellSelectionEnabled(true);
		
		txtIdcliente.addKeyListener(c);
		txtNombrecliente.addKeyListener(c);
		txtFechafactura.addKeyListener(c);
		
		
		
		
		
		
		this.btnCerrar.addKeyListener(c);
		this.btnCerrar.addActionListener(c);
		this.btnCerrar.setActionCommand("CERRAR");
		
		this.btnCliente.addKeyListener(c);
		this.btnCliente.addActionListener(c);
		this.btnCliente.setActionCommand("BUSCARCLIENTES");
		
		this.btnCobrar.addKeyListener(c);
		this.btnCobrar.addActionListener(c);
		this.btnCobrar.setActionCommand("COBRAR");
		
		
		chckbxSeleccionarTodos.addActionListener(c);
		chckbxSeleccionarTodos.setActionCommand("SELECTALL");
		
		
		//KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		//manager.addKeyEventDispatcher( c);
		//this.addWindowListener(c);
		//this.addw
	}
}
