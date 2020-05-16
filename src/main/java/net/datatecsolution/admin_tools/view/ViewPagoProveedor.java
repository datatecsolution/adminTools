package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlPagoProveedor;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.botones.BotonBuscarProveedor;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturasPago;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewPagoProveedor extends JDialog {
	private TmFacturasPago modelo;
	private JPanel panelAcciones;
	private JPanel panelDatosFactura;
	private JLabel lblCodigoCliente;
	private JTextField txtIdProveedor;
	private JTextField txtNombreProveedor;
	
	private ButtonGroup grupoOpciones;
	private BotonCancelar btnCerrar;
	private BotonBuscarProveedor btnProveedor;
	private BotonCobrar btnPagar;
	private JLabel lblNombreCliente;
	private JTextField txtSaldoProveedor;
	
	
	private JTextField txtTotal;
	private JLabel lblTotal;
	private PanelPadre panelDatosPago;
	private JTextField txtObservacion;
	private JLabel lblFormaDePago;
	private CbxTmCuentasBancos modeloCuentasBancos;
	private JComboBox<Banco> cbFormaPago;

	public ViewPagoProveedor(Window view) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewPagoProveedor.class.getResource("/drawable/logo_2.png")));
		
		
		this.setTitle("Pago a proveedor");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		//setIconImage(Toolkit.getDefaultToolkit().getImage(ViewFacturar.class.getResource("/view/recursos/logo-admin-tool1.png")));
		getContentPane().setBackground(PanelPadre.color1);
		
		getContentPane().setLayout(null);
		
		panelAcciones=new PanelPadre();
		panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAcciones.setBounds(6, 11, 143, 491);
		panelAcciones.setLayout(null);
		//panelAcciones.setVisible(false);
		
		
		//this.setTitle("Facturar");
		getContentPane().add(panelAcciones);
		//btnBuscar.getInputMap().put(KeyStroke.getKeyStroke("F1"), sumar());
		
		btnProveedor = new BotonBuscarProveedor();
		btnProveedor.setSize(115, 70);
		btnProveedor.setText("F3 Proveedores");
		btnProveedor.setHorizontalAlignment(SwingConstants.LEFT);
		btnProveedor.setLocation(10, 30);
		//btnCliente.setBounds(10, 19, 158, 80);
		panelAcciones.add(btnProveedor);
		
		btnPagar = new BotonCobrar();
		btnPagar.setText("F2 pagar");
		btnPagar.setHorizontalAlignment(SwingConstants.LEFT);
		btnPagar.setBounds(10, 130, 115, 70);
		
		panelAcciones.add(btnPagar);
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setBounds(10, 230, 115, 70);
		panelAcciones.add(btnCerrar);
		
		
		
		
		panelDatosFactura=new PanelPadre();
		//panelDatosFactura.setBackground(Color.WHITE);
		panelDatosFactura.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelDatosFactura.setBounds(161, 11, 482, 234);
		panelDatosFactura.setLayout(null);
		
		getContentPane().add(panelDatosFactura);
		
		
		panelDatosPago=new PanelPadre();
		//panelDatosFactura.setBackground(Color.WHITE);
		panelDatosPago.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos de pago", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelDatosPago.setBounds(161, 257, 482, 245);
		panelDatosPago.setLayout(null);
		getContentPane().add(panelDatosPago);
		
		
		
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 45);
				
		
		txtTotal = new JTextField();
		txtTotal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtObservacion.requestFocusInWindow();
				txtObservacion.selectAll();
			}
		});
		txtTotal.setBounds(20, 105, 438, 58);
		panelDatosPago.add(txtTotal);
		//txtTotal.setForeground(Color.RED);
		txtTotal.setHorizontalAlignment(SwingConstants.LEFT);
		txtTotal.setFont(myFont);
		txtTotal.setText("00");
		txtTotal.setColumns(10);
		
		lblTotal = new JLabel("Total");
		lblTotal.setBounds(20, 90, 46, 14);
		panelDatosPago.add(lblTotal);
		
		JLabel lblReferncia = new JLabel("Obsevacion");
		lblReferncia.setBounds(20, 175, 287, 16);
		panelDatosPago.add(lblReferncia);
		
		txtObservacion = new JTextField();
		txtObservacion.setBounds(20, 193, 438, 42);
		panelDatosPago.add(txtObservacion);
		txtObservacion.setColumns(10);
		
		lblFormaDePago = new JLabel("Forma de pago");
		lblFormaDePago.setBounds(20, 20, 170, 16);
		panelDatosPago.add(lblFormaDePago);
		
		modeloCuentasBancos=new CbxTmCuentasBancos();
		
		cbFormaPago = new JComboBox<Banco>();
		cbFormaPago.setModel(modeloCuentasBancos);//para poder mostrar el formulario en modo dise�o comente esta linea
		cbFormaPago.setBounds(20, 36, 438, 42);
		panelDatosPago.add(cbFormaPago);
		
		
		
		lblCodigoCliente = new JLabel("Id Proveedor");
		lblCodigoCliente.setBounds(20, 24, 117, 14);
		panelDatosFactura.add(lblCodigoCliente);
		
		
		
		txtIdProveedor = new JTextField();
		txtIdProveedor.setBounds(20, 38, 438, 42);
		panelDatosFactura.add(txtIdProveedor);
		txtIdProveedor.setColumns(10);
		
		txtNombreProveedor = new JTextField();
		txtNombreProveedor.setToolTipText("Nombre Cliente");
		txtNombreProveedor.setBounds(20, 105, 438, 42);
		panelDatosFactura.add(txtNombreProveedor);
		txtNombreProveedor.setColumns(10);
		
		
		lblNombreCliente = new JLabel("Nombre Proveedor");
		lblNombreCliente.setBounds(20, 73, 145, 42);
		panelDatosFactura.add(lblNombreCliente);
		
		JLabel lblSaldoCliente = new JLabel("Saldo Proveedor");
		lblSaldoCliente.setBounds(20, 142, 197, 42);
		panelDatosFactura.add(lblSaldoCliente);
		
		txtSaldoProveedor = new JTextField();
		txtSaldoProveedor.setBounds(20, 171, 438, 42);
		panelDatosFactura.add(txtSaldoProveedor);
		txtSaldoProveedor.setColumns(10);
		modelo=new TmFacturasPago();
		
		setSize(650, 550);
		
		//this.setPreferredSize(new Dimension(660, 330));
		this.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setPreferredSize(new Dimension(650, 550));
		this.pack();
	}
	
	public JButton getBtnBuscarProveedor(){
		return btnProveedor;
	}
	public JButton getBtnPagar(){
		return btnPagar;
	}
	public JButton getBtnCerrar(){
		return btnCerrar;
	}
	public JTextField getTxtTotal(){
		return txtTotal;
	}
	public JTextField getTxtNombreProveedor(){
		return txtNombreProveedor;
	}
	public JTextField getTxtIdProveedor(){
		return txtIdProveedor;
	}
	
	
	public JTextField getTxtSaldo(){
		return this.txtSaldoProveedor;
	}
	public void conectarContralador(CtlPagoProveedor c){

		
		txtIdProveedor.addActionListener(c);
		txtIdProveedor.setActionCommand("BUSCARPROVEEDOR");
		
		
		txtIdProveedor.addKeyListener(c);
		txtNombreProveedor.addKeyListener(c);
		this.txtObservacion.addKeyListener(c);
		
		this.btnCerrar.addKeyListener(c);
		this.btnCerrar.addActionListener(c);
		this.btnCerrar.setActionCommand("CERRAR");
		
		this.btnProveedor.addKeyListener(c);
		this.btnProveedor.addActionListener(c);
		this.btnProveedor.setActionCommand("BUSCARPROVEEDORES");
		
		this.btnPagar.addKeyListener(c);
		this.btnPagar.addActionListener(c);
		this.btnPagar.setActionCommand("PAGAR");
		
		txtObservacion.addActionListener(c);
		txtObservacion.setActionCommand("PAGAR");
		
		txtTotal.addKeyListener(c);
		
		
	}

	/**
	 * @return the txtrObservacion
	 */
	public JTextField getTxtrObservacion() {
		return txtObservacion;
	}

	/**
	 * @return the modeloCuentasBancos
	 */
	public CbxTmCuentasBancos getModeloCuentasBancos() {
		return modeloCuentasBancos;
	}

	/**
	 * @return the cbFormaPago
	 */
	public JComboBox<Banco> getCbFormaPago() {
		return cbFormaPago;
	}
}
