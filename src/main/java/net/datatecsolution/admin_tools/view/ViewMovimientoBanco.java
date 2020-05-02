package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlMovimientoBanco;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;

import javax.swing.*;
import java.awt.*;

public class ViewMovimientoBanco extends JDialog  {
	private JTextField txtMonto;
	private CbxTmCuentasBancos modeloCuentasBancos;
	private JComboBox<Banco> cbFormaPago;
	private JComboBox<String> cbTipoMovimiento;
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCancelar;
	private JTextArea txtrDescripcion;
	
	
public ViewMovimientoBanco(Window view) {
		
		super(view,"Movimiento de bancos", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		getContentPane().setBackground(PanelPadre.color1);
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(376, 488);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		JLabel lblCuentaDeBanco = new JLabel("Cuenta de banco");
		lblCuentaDeBanco.setBounds(20, 6, 151, 16);
		getContentPane().add(lblCuentaDeBanco);
		
		modeloCuentasBancos=new CbxTmCuentasBancos();
		
		cbFormaPago = new JComboBox<Banco>();
		cbFormaPago.setBounds(18, 26, 338, 38);
		cbFormaPago.setModel(modeloCuentasBancos);
		getContentPane().add(cbFormaPago);
		
		JLabel lblTipoTransaccion = new JLabel("Tipo transaccion");
		lblTipoTransaccion.setBounds(20, 70, 121, 16);
		getContentPane().add(lblTipoTransaccion);
		
		cbTipoMovimiento = new JComboBox<String>();
		cbTipoMovimiento.setModel(new DefaultComboBoxModel(new String[] {"Deposito", "Retiro"}));
		cbTipoMovimiento.setBounds(20, 90, 338, 38);
		getContentPane().add(cbTipoMovimiento);
		
		JLabel lblDescripcion = new JLabel("Descripcion");
		lblDescripcion.setBounds(20, 126, 129, 16);
		getContentPane().add(lblDescripcion);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 146, 317, 140);
		getContentPane().add(scrollPane);
		
		txtrDescripcion = new JTextArea();
		txtrDescripcion.setLocation(20, 0);
		scrollPane.setViewportView(txtrDescripcion);
		
		JLabel lblMonto = new JLabel("Monto");
		lblMonto.setBounds(18, 293, 61, 16);
		getContentPane().add(lblMonto);
		
		txtMonto = new JTextField();
		txtMonto.setBounds(20, 307, 317, 38);
		getContentPane().add(txtMonto);
		txtMonto.setColumns(10);
		
		
		
	
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setBounds(30, 357, 130, 65);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setBounds(192, 357, 130, 65);
		getContentPane().add(btnCancelar);
}

public void conectarControlador(CtlMovimientoBanco c){
	
	btnGuardar.setActionCommand("GUARDAR");
	btnGuardar.addActionListener(c);
	
	btnCancelar.setActionCommand("CANCELAR");
	btnCancelar.addActionListener(c);
	
	
	btnGuardar.addKeyListener(c);
	btnCancelar.addKeyListener(c);
	txtMonto.addKeyListener(c);
	txtrDescripcion.addKeyListener(c);
	txtMonto.addKeyListener(c);
	
	
}


/**
 * @return the txtMonto
 */
public JTextField getTxtMonto() {
	return txtMonto;
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


/**
 * @return the cbTipoMovimiento
 */
public JComboBox getCbTipoMovimiento() {
	return cbTipoMovimiento;
}


/**
 * @return the botonGuardar
 */
public BotonGuardar getBotonGuardar() {
	return btnGuardar;
}


/**
 * @return the botonCancelar
 */
public BotonCancelar getBotonCancelar() {
	return btnCancelar;
}

/**
 * @return the txtrDescripcion
 */
public JTextArea getTxtrDescripcion() {
	return txtrDescripcion;
}
}
