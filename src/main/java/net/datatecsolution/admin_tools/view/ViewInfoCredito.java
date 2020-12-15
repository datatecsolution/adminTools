package net.datatecsolution.admin_tools.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewInfoCredito extends JDialog  {

	private JTextField txtNoCuenta;
	private JTextField txtCliente;
	private JPanel panel;
	private JTextField txtCobrador;
	private JTextField txtSaldo;


	public ViewInfoCredito(Window v) {


		super(v,"Informacion del credito!!!", ModalityType.DOCUMENT_MODAL);
		//setUndecorated(true);
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 18);
		Color color1 =Color.decode("#d4f4ff");
		this.getContentPane().setBackground(color1);




		getContentPane().setLayout(null);

		panel = new JPanel();
		panel.setBounds(19, 21, 395, 320);
		panel.setBackground(color1);
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblPagaCon = new JLabel("No cuenta:");
		lblPagaCon.setFont(new Font("Georgia", Font.BOLD, 13));
		lblPagaCon.setForeground(Color.BLACK);
		lblPagaCon.setBounds(6, 6, 77, 14);
		panel.add(lblPagaCon);

		txtNoCuenta = new JTextField();
		txtNoCuenta.setEditable(false);
		txtNoCuenta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				setVisible(false);
			}
		});
		txtNoCuenta.setBounds(6, 26, 383, 54);
		txtNoCuenta.setFont(myFont);
		panel.add(txtNoCuenta);
		txtNoCuenta.setColumns(10);

		JLabel lblCambio = new JLabel("Cliente:");
		lblCambio.setFont(new Font("Georgia", Font.BOLD, 13));
		lblCambio.setForeground(Color.BLACK);
		lblCambio.setBounds(6, 92, 77, 14);
		panel.add(lblCambio);

		txtCliente = new JTextField();
		txtCliente.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		txtCliente.setEditable(false);
		txtCliente.setFont(myFont);
		txtCliente.setBounds(6, 106, 383, 49);
		panel.add(txtCliente);
		txtCliente.setColumns(10);

		JLabel lblCobrador = new JLabel("Cobrador:");
		lblCobrador.setForeground(Color.BLACK);
		lblCobrador.setFont(new Font("Georgia", Font.BOLD, 13));
		lblCobrador.setBounds(6, 167, 77, 14);
		panel.add(lblCobrador);

		txtCobrador = new JTextField();
		txtCobrador.setFont(myFont);
		txtCobrador.setEditable(false);
		txtCobrador.setColumns(10);
		txtCobrador.setBounds(6, 181, 383, 49);
		panel.add(txtCobrador);

		JLabel lblSaldo = new JLabel("Saldo:");
		lblSaldo.setForeground(Color.BLACK);
		lblSaldo.setFont(new Font("Georgia", Font.BOLD, 13));
		lblSaldo.setBounds(6, 242, 77, 14);
		panel.add(lblSaldo);

		txtSaldo = new JTextField();
		txtSaldo.setFont(myFont);
		txtSaldo.setEditable(false);
		txtSaldo.setColumns(10);
		txtSaldo.setBounds(6, 256, 383, 49);
		panel.add(txtSaldo);

		this.setSize(420, 400);
		this.setPreferredSize(new Dimension(420, 400));
		this.setResizable(false);
		///setUndecorated(true);

		//this.setResizable(false);
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.pack();
	}
	public JTextField getTxtCliente(){
		return txtCliente;
	}
	public JTextField getTxtNoCuenta(){
		return txtNoCuenta;
	}
	public JTextField getTxtCobrador() {
		return txtCobrador;
	}
	public void setTxtCobrador(JTextField txtCobrador) {
		this.txtCobrador = txtCobrador;
	}
	public JTextField getTxtSaldo() {
		return txtSaldo;
	}
	public void setTxtSaldo(JTextField txtSaldo) {
		this.txtSaldo = txtSaldo;
	}

}
