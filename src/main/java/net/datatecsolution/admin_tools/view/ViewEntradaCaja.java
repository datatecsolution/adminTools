package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlEntradaCaja;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;

import javax.swing.*;
import java.awt.*;

public class ViewEntradaCaja extends JDialog {
	private JTextField txtCantidad;
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCancelar;
	private JTextArea txtrConcepto;
	private CbxTmCuentasBancos modeloCuentasBancos;
	private JComboBox<Banco> cbFormaPago;

	public ViewEntradaCaja(Window view) {
		// TODO Auto-generated constructor stub
		this.setTitle("Entrada de efectivo");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		getContentPane().setBackground(PanelPadre.color1);
		getContentPane().setLayout(null);
		
		JLabel lblCantida = new JLabel("Cantidad");
		lblCantida.setBounds(16, 6, 109, 16);
		getContentPane().add(lblCantida);
		
		txtCantidad = new JTextField();
		txtCantidad.setBounds(16, 25, 402, 40);
		getContentPane().add(txtCantidad);
		txtCantidad.setColumns(10);
		
		JLabel lblConcepto = new JLabel("Concepto");
		lblConcepto.setBounds(16, 68, 61, 16);
		getContentPane().add(lblConcepto);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setLocation(56, 274);
		//btnGuardar.setBounds(16, 219, 117, 29);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		//btnCancelar.setBounds(241, 219, 117, 29);
		btnCancelar.setLocation(246, 274);
		getContentPane().add(btnCancelar);
		
		txtrConcepto = new JTextArea();
		txtrConcepto.setBounds(16, 97, 396, 91);
		getContentPane().add(txtrConcepto);
		
		JLabel lblEntregadoA = new JLabel("Para Cuenta de banco");
		lblEntregadoA.setBounds(16, 200, 146, 16);
		getContentPane().add(lblEntregadoA);
		
		modeloCuentasBancos=new CbxTmCuentasBancos();
		
		cbFormaPago = new JComboBox<Banco>();
		cbFormaPago.setBounds(16, 220, 396, 42);
		cbFormaPago.setModel(modeloCuentasBancos);//para poder mostrar el formulario en modo dise�o comente esta linea
		getContentPane().add(cbFormaPago);
		
		this.setSize(435, 397);
		
		setResizable(false);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	public JTextField getTxtCantidad(){
		return txtCantidad;
	}
	public JTextArea getTxtConcepto(){
		return txtrConcepto;
	}
	
	
	public void conectarControlador(CtlEntradaCaja c){
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);
		
		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
		
		
		btnGuardar.addKeyListener(c);
		btnCancelar.addKeyListener(c);
		cbFormaPago.addKeyListener(c);
		txtrConcepto.addKeyListener(c);
		txtCantidad.addKeyListener(c);
		
		
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
