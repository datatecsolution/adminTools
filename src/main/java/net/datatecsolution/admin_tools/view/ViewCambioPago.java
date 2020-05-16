package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCambioPago;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;

import javax.swing.*;
import java.awt.*;

public class ViewCambioPago extends JDialog {
	private JTextField txtEfectivo;
	private JTextField txtCambio;
	//private final ToggleGroup grupo;
	//private ButtonGroup grupoOpciones;
	
	//private JToggleButton tglbtnEfectivo;
	//private JToggleButton tglbtnTarjetaCredito;
	
	private JTextField txtReferenciatarjeta;
	private BotonCobrar btnCobrar;
	private BotonCancelar btnCerrar;
	private JLabel lblTarjetaCreditodebito;
	
	public ViewCambioPago(Window v) {
		
		super(v,"Forma de pago", ModalityType.DOCUMENT_MODAL);
		//setUndecorated(true);
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 45);
		 //Color color1 =Color.decode("#0009999");
		 Color color1 =Color.decode("#d4f4ff");
		 this.getContentPane().setBackground(color1);
		this.setSize(588, 316);
		this.setPreferredSize(new Dimension(588, 316));
		this.setResizable(false);
		
		JLabel lblEfectivo = new JLabel("      Efectivo");
		lblEfectivo.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		getContentPane().add(lblEfectivo);
		
		txtEfectivo = new JTextField();
		txtEfectivo.setBounds(6, 26, 341, 54);
		txtEfectivo.setFont(myFont);
		txtEfectivo.setColumns(10);
		getContentPane().add(txtEfectivo);
		
		
		
		
		lblTarjetaCreditodebito = new JLabel("      Tarjeta Credito/Debito");
		lblTarjetaCreditodebito.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		getContentPane().add(lblTarjetaCreditodebito);
		
		txtReferenciatarjeta = new JTextField();
		txtReferenciatarjeta.setBounds(6, 26, 341, 54);
		txtReferenciatarjeta.setColumns(10);
		txtReferenciatarjeta.setFont(myFont);
		getContentPane().add(txtReferenciatarjeta);
		
		
		
		JLabel lblCambio = new JLabel("        Cambio:");
		lblCambio.setFont(new Font("Georgia", Font.BOLD, 16));
		lblCambio.setForeground(Color.BLACK);
		lblCambio.setBounds(6, 92, 77, 14);
		getContentPane().add(lblCambio);
		
		txtCambio = new JTextField();
		txtCambio.setEditable(false);
		txtCambio.setFont(myFont);
		txtCambio.setBounds(6, 106, 341, 49);
		getContentPane().add(txtCambio);
		txtCambio.setColumns(10);
		
		//imagen para el boton efectivo
		ImageIcon imgEfectivo=new ImageIcon(BotonCancelar.class.getResource("/drawable/icono_pago_efectivo.png"));
		
		Image image = imgEfectivo.getImage();
		    // reduce by 50%
		image = image.getScaledInstance(image.getWidth(null)/2, image.getHeight(null)/2, Image.SCALE_SMOOTH);
		imgEfectivo.setImage(image);
		
		//imagen para el boton tarjeta de credito
		ImageIcon imgTarjeta=new ImageIcon(BotonCancelar.class.getResource("/drawable/credit-card-icon.png"));
		
		image = imgTarjeta.getImage();
		    // reduce by 50%
		image = image.getScaledInstance(image.getWidth(null)/6, image.getHeight(null)/6, Image.SCALE_SMOOTH);
		imgTarjeta.setImage(image);
		
		getContentPane().setLayout(new GridLayout(0, 2, 0, 0));
		
		
		
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setFont(new Font("Georgia", Font.BOLD, 13));
		btnCerrar.setForeground(Color.BLACK);
		btnCerrar.setBackground(color1);
		btnCerrar.setText("Esc Cerrar");
		getContentPane().add(btnCerrar);
		
		btnCobrar = new BotonCobrar();
		btnCobrar.setFont(new Font("Georgia", Font.BOLD, 13));
		btnCobrar.setForeground(Color.BLACK);
		btnCobrar.setBackground(color1);
		btnCobrar.setText("F2 Cobrar");
		getContentPane().add(btnCobrar);
		
		
		
		this.setResizable(false);
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.pack();
	}
	public JTextField getTxtCambio(){
		return txtCambio;
	}
	public JTextField getTxtEfectivo(){
		return txtEfectivo;
	}
	public JTextField getTxtReferencia(){
		return txtReferenciatarjeta;
	}
	
	public JButton getBtnCobrar(){
		return btnCobrar;
	}
	public void conectarCtl(CtlCambioPago c) {
		// TODO Auto-generated method stub
		
		//tglbtnEfectivo.addActionListener(c);
		
		txtEfectivo.addActionListener(c);
		txtEfectivo.addKeyListener(c);
		txtEfectivo.setActionCommand("CAMBIO");
		
		txtReferenciatarjeta.addActionListener(c);
		txtReferenciatarjeta.setActionCommand("TARJETA");
		txtReferenciatarjeta.addKeyListener(c);
	
		
		btnCerrar.addActionListener(c);
		btnCerrar.setActionCommand("CERRAR");
		this.btnCerrar.addKeyListener(c);
		
		btnCobrar.addActionListener(c);
		btnCobrar.setActionCommand("COBRAR");
		this.btnCobrar.addKeyListener(c);
		this.addWindowListener(c);
		
		txtCambio.addActionListener(c);
		txtCambio.setActionCommand("IMPRIMIR");
		
		txtCambio.addKeyListener(c);
		
	}
	/*public static void main(String arg[]){
		ViewCambioPago viewPago=new ViewCambioPago(null);
		CtlCambioPago ctlPago=new CtlCambioPago(viewPago, new BigDecimal(1000));
		boolean resulPago=ctlPago.pagar();
	}*/
}
