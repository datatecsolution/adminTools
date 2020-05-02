package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCobro;
import net.datatecsolution.admin_tools.view.botones.BotonBuscarClientes;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturasCredito;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturasPago;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCobro extends JDialog {
	private TmFacturasPago modelo;
	private JPanel panelAcciones;
	private JPanel panelDatosFactura;
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
	private JTable tFacturas;
	private TmFacturasCredito modeloFacturas;

	public ViewCobro(Window view) {
		
		
		this.setTitle("Cobro");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		//setIconImage(Toolkit.getDefaultToolkit().getImage(ViewFacturar.class.getResource("/view/recursos/logo-admin-tool1.png")));
		getContentPane().setBackground(PanelPadre.color1);
		
		getContentPane().setLayout(null);
		
		panelAcciones=new PanelPadre();
		panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAcciones.setBounds(20, 11, 117, 461);
		panelAcciones.setLayout(null);
		//panelAcciones.setVisible(false);
		
		
		//this.setTitle("Facturar");
		getContentPane().add(panelAcciones);
		//btnBuscar.getInputMap().put(KeyStroke.getKeyStroke("F1"), sumar());
		
		btnCliente = new BotonBuscarClientes();
		btnCliente.setSize(92, 70);
		btnCliente.setText("F3 Clientes");
		btnCliente.setHorizontalAlignment(SwingConstants.LEFT);
		btnCliente.setLocation(10, 19);
		//btnCliente.setBounds(10, 19, 158, 80);
		panelAcciones.add(btnCliente);
		
		btnCobrar = new BotonCobrar();
		btnCobrar.setText("F2 Cobrar");
		btnCobrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCobrar.setBounds(10, 102, 92, 70);
		
		panelAcciones.add(btnCobrar);
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setBounds(10, 185, 92, 70);
		panelAcciones.add(btnCerrar);
		
		
		
		
		panelDatosFactura=new PanelPadre();
		//panelDatosFactura.setBackground(Color.WHITE);
		panelDatosFactura.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelDatosFactura.setBounds(161, 11, 604, 165);
		panelDatosFactura.setLayout(null);
		
		getContentPane().add(panelDatosFactura);
		
		lblCodigoCliente = new JLabel("Id Cliente");
		lblCodigoCliente.setBounds(20, 23, 237, 14);
		panelDatosFactura.add(lblCodigoCliente);
		
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 45);
		
		txtIdcliente = new JTextField();
		txtIdcliente.setBounds(20, 35, 237, 29);
		panelDatosFactura.add(txtIdcliente);
		txtIdcliente.setColumns(10);
		
		txtNombrecliente = new JTextField();
		txtNombrecliente.setEditable(false);
		txtNombrecliente.setToolTipText("Nombre Cliente");
		txtNombrecliente.setBounds(269, 35, 313, 29);
		panelDatosFactura.add(txtNombrecliente);
		txtNombrecliente.setColumns(10);
		
		
		lblNombreCliente = new JLabel("Nombre Cliente");
		lblNombreCliente.setBounds(269, 23, 313, 14);
		panelDatosFactura.add(lblNombreCliente);
		
		JLabel lblSaldoCliente = new JLabel("Saldo Cliente");
		lblSaldoCliente.setBounds(269, 87, 313, 14);
		panelDatosFactura.add(lblSaldoCliente);
		
		JLabel lblLimiteDeCredito = new JLabel("Limite de credito");
		lblLimiteDeCredito.setBounds(20, 87, 237, 14);
		panelDatosFactura.add(lblLimiteDeCredito);
		
		txtLimitecredito = new JTextField();
		txtLimitecredito.setEditable(false);
		txtLimitecredito.setBounds(20, 100, 237, 29);
		panelDatosFactura.add(txtLimitecredito);
		txtLimitecredito.setColumns(10);
		
		txtSaldocliente = new JTextField();
		txtSaldocliente.setEditable(false);
		txtSaldocliente.setBounds(269, 100, 313, 29);
		panelDatosFactura.add(txtSaldocliente);
		txtSaldocliente.setColumns(10);
		modelo=new TmFacturasPago();
		
		
		
		
				
		
		txtTotal = new JTextField();
		//txtTotal.setForeground(Color.RED);
		txtTotal.setHorizontalAlignment(SwingConstants.LEFT);
		txtTotal.setFont(myFont);
		txtTotal.setText("00");
		txtTotal.setBounds(161, 229, 604, 58);
		getContentPane().add(txtTotal);
		txtTotal.setColumns(10);
		
		lblTotal = new JLabel("Pago");
		lblTotal.setBounds(161, 203, 46, 14);
		getContentPane().add(lblTotal);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(161, 316, 604, 156);
		getContentPane().add(scrollPane);
		
		modeloFacturas= new TmFacturasCredito();
		
		tFacturas = new JTable();
		tFacturas.setModel(modeloFacturas);
		scrollPane.setViewportView(tFacturas);
		
		tFacturas.getColumnModel().getColumn(0).setPreferredWidth(100);     //Tama�o de las columnas de las tablas
		tFacturas.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tFacturas.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tFacturas.getColumnModel().getColumn(3).setPreferredWidth(100);	//
		tFacturas.getColumnModel().getColumn(4).setPreferredWidth(100);	//
		//tFacturas.setAutoCreateRowSorter(true);
		
		JLabel lblFacturasAPagar = new JLabel("Facturas a pagar");
		lblFacturasAPagar.setBounds(161, 299, 103, 16);
		getContentPane().add(lblFacturasAPagar);
		
		setSize(720, 500);
		
		//this.setPreferredSize(new Dimension(660, 330));
		this.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setPreferredSize(new Dimension(800, 600));
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
	public JTextField getTxtTotal(){
		return txtTotal;
	}
	public JTextField getTxtNombrecliente(){
		return txtNombrecliente;
	}
	public JTextField getTxtIdcliente(){
		return txtIdcliente;
	}
	
	public JTextField getTxtLimiteCredito(){
		return this.txtLimitecredito;
	}
	public JTextField getTxtSaldo(){
		return this.txtSaldocliente;
	}
	public void conectarContralador(CtlCobro c){

		
		txtIdcliente.addActionListener(c);
		txtIdcliente.setActionCommand("BUSCARCLIENTE");
		
		
		txtIdcliente.addKeyListener(c);
		txtNombrecliente.addKeyListener(c);
		
		this.btnCerrar.addKeyListener(c);
		this.btnCerrar.addActionListener(c);
		this.btnCerrar.setActionCommand("CERRAR");
		
		this.btnCliente.addKeyListener(c);
		this.btnCliente.addActionListener(c);
		this.btnCliente.setActionCommand("BUSCARCLIENTES");
		
		this.btnCobrar.addKeyListener(c);
		this.btnCobrar.addActionListener(c);
		this.btnCobrar.setActionCommand("COBRAR");
		
		txtTotal.addKeyListener(c);
		
		
		
	}

	/**
	 * @return the modeloFacturas
	 */
	public TmFacturasCredito getModeloFacturas() {
		return modeloFacturas;
	}

}
