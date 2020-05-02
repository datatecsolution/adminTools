package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCargarVendedor;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewCargarVenderor extends JDialog {
	
	private JTextField txtEfectivo;
	private JTextField txtCambio;
	//private final ToggleGroup grupo;
	private ButtonGroup grupoOpciones;
	
	private JToggleButton tglbtnEfectivo;
	private JPanel panel;
	private BotonCobrar btnCobrar;
	private BotonCancelar btnCerrar;
	private JPanel panel_2;
	private JLabel lblApellido;
	private JTextField txtApellido;

	public ViewCargarVenderor(Window v) {
		super(v,"Seleccione bombero", ModalityType.DOCUMENT_MODAL);
		setTitle("Seleccionar");
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 25);
		 grupoOpciones = new ButtonGroup(); // crea ButtonGroup//para el grupo de la forma de pago
		 getContentPane().setBackground(PanelPadre.color1);
		 
		
		getContentPane().setLayout(null);
		
		panel = new JPanel();
		panel.setBackground(PanelPadre.color1);
		panel.setBounds(221, 11, 357, 165);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblPagaCon = new JLabel("Codigo:");
		lblPagaCon.setFont(new Font("Georgia", Font.BOLD, 13));
		lblPagaCon.setBounds(0, 11, 106, 14);
		panel.add(lblPagaCon);
		
		txtEfectivo = new JTextField();
		txtEfectivo.setBounds(116, 3, 223, 41);
		txtEfectivo.setFont(myFont);
		panel.add(txtEfectivo);
		txtEfectivo.setColumns(10);
		
		JLabel lblCambio = new JLabel("Nombre:");
		lblCambio.setFont(new Font("Georgia", Font.BOLD, 13));
		lblCambio.setBounds(0, 63, 106, 14);
		panel.add(lblCambio);
		
		txtCambio = new JTextField();
		txtCambio.setEditable(false);
		txtCambio.setFont(myFont);
		txtCambio.setBounds(116, 55, 223, 37);
		panel.add(txtCambio);
		txtCambio.setColumns(10);
		
		lblApellido = new JLabel("Apellido:");
		lblApellido.setFont(new Font("Georgia", Font.BOLD, 13));
		lblApellido.setBounds(0, 112, 90, 14);
		panel.add(lblApellido);
		
		txtApellido = new JTextField();
		txtApellido.setEditable(false);
		txtApellido.setBounds(116, 103, 223, 37);
		txtApellido.setFont(myFont);
		panel.add(txtApellido);
		txtApellido.setColumns(10);
		
		//imagen para el boton efectivo
		ImageIcon imgEfectivo=new ImageIcon(BotonCancelar.class.getResource("/USUARIOS.png"));
		
		Image image = imgEfectivo.getImage();
		    // reduce by 50%
		image = image.getScaledInstance(image.getWidth(null)/3, image.getHeight(null)/3, Image.SCALE_SMOOTH);
		imgEfectivo.setImage(image);
		
				
		btnCobrar = new BotonCobrar();
		btnCobrar.setSize(128, 77);
		btnCobrar.setText("F2 Cobrar");
		//btnCobrar.setBounds(424, 209, 144, 38);
		btnCobrar.setLocation(424, 209);
		getContentPane().add(btnCobrar);
		
		btnCerrar = new BotonCancelar();
		//btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCerrar.setText("Esc Cerrar");
		//btnCerrar.setBounds(252, 209, 144, 38);
		btnCerrar.setLocation(252, 209);
		getContentPane().add(btnCerrar);
		
		panel_2 = new JPanel();
		panel_2.setBackground(new Color(60, 179, 113));
		panel_2.setBounds(0, 0, 219, 301);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		tglbtnEfectivo = new JToggleButton("Vendedor");
		tglbtnEfectivo.setBounds(10, 27, 199, 111);
		panel_2.add(tglbtnEfectivo);
		
		tglbtnEfectivo.setHorizontalAlignment(SwingConstants.LEFT);
		tglbtnEfectivo.setIcon(imgEfectivo);
		grupoOpciones.add(tglbtnEfectivo);
		tglbtnEfectivo.setSelected(true);
		
		
		this.setSize(588, 330);
		this.setPreferredSize(new Dimension(588, 330));
		//setUndecorated(true);
		
		this.setResizable(false);
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.pack();
	}
	
	
	public JPanel getPanelTarjeta(){
		return panel;
	}
	public JTextField getTxtNombre(){
		return txtCambio;
	}
	public JTextField getTxtIdVendedor(){
		return txtEfectivo;
	}
	
	public JToggleButton getTglbtnEfectivo(){
		return tglbtnEfectivo;
	}
	
	public JTextField getTxtApellido(){
		return txtApellido;
	}
	
	public JButton getBtnCobrar(){
		return btnCobrar;
	}
	public void conectarCtl(CtlCargarVendedor c) {
		// TODO Auto-generated method stub
		
		//tglbtnEfectivo.addActionListener(c);
		//tglbtnEfectivo.addItemListener(c);
		txtEfectivo.addActionListener(c);
		txtEfectivo.addKeyListener(c);
		
		txtEfectivo.setActionCommand("BUSCAR");
			
		
		tglbtnEfectivo.addKeyListener(c);
				
		btnCerrar.addActionListener(c);
		btnCerrar.setActionCommand("CERRAR");
		this.btnCerrar.addKeyListener(c);
		
		btnCobrar.addActionListener(c);
		btnCobrar.setActionCommand("COBRAR");
		this.btnCobrar.addKeyListener(c);
		this.addWindowListener(c);
		
		txtCambio.addActionListener(c);
		txtCambio.addKeyListener(c);
		txtCambio.setActionCommand("IMPRIMIR");
		
	}

}
