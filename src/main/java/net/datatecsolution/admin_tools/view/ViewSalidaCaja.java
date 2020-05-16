package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlSalidaCaja;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;

import javax.swing.*;
import java.awt.*;

public class ViewSalidaCaja extends JDialog {
	private final JTextField txtCantidad;
	private final BotonGuardar btnGuardar;
	private final BotonCancelar btnCancelar;
	private final JTextArea txtrConcepto;
	private final JTextField txtEmpleado;
	private final CbxTmCuentasBancos modeloCuentasBancos;
	private final JComboBox<Banco> cbFormaPago;

	public ViewSalidaCaja(Window view) {
		// TODO Auto-generated constructor stub
		this.setTitle("Salida de efectivo");
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
		btnGuardar.setLocation(54, 358);
		//btnGuardar.setBounds(16, 219, 117, 29);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		//btnCancelar.setBounds(241, 219, 117, 29);
		btnCancelar.setLocation(244, 358);
		getContentPane().add(btnCancelar);
		
		txtrConcepto = new JTextArea();
		//txtrConcepto.setText("NA");
		txtrConcepto.setBounds(16, 97, 396, 91);
		getContentPane().add(txtrConcepto);
		
		JLabel lblEntregadoA = new JLabel("F1 - Entregado a");
		lblEntregadoA.setBounds(16, 200, 109, 16);
		getContentPane().add(lblEntregadoA);
		
		txtEmpleado = new JTextField();
		txtEmpleado.setEditable(false);
		txtEmpleado.setBounds(16, 228, 396, 40);
		getContentPane().add(txtEmpleado);
		txtEmpleado.setColumns(10);
		
		JLabel lblCuentaDeBanco = new JLabel("A cuenta de banco");
		lblCuentaDeBanco.setBounds(16, 280, 174, 16);
		getContentPane().add(lblCuentaDeBanco);
		
		modeloCuentasBancos=new CbxTmCuentasBancos();
		
		cbFormaPago = new JComboBox<Banco>();
		cbFormaPago.setBounds(16, 294, 396, 42);
		cbFormaPago.setModel(modeloCuentasBancos);//para poder mostrar el formulario en modo dise�o comente esta linea
		getContentPane().add(cbFormaPago);
		
		this.setSize(435, 473);
		
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
	public JTextField getTxtEmpleado(){
		return txtEmpleado;
	}
	
	public void conectarControlador(CtlSalidaCaja c){
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);
		
		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
		
		
		btnGuardar.addKeyListener(c);
		btnCancelar.addKeyListener(c);
		txtEmpleado.addKeyListener(c);
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
