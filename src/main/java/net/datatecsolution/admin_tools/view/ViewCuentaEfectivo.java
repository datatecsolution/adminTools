package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlContarEfectivo;
import net.datatecsolution.admin_tools.view.botones.BotonBuscar1;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class ViewCuentaEfectivo extends JDialog {
	private JTextField txtUno;
	private JTextField txtDos;
	private JTextField txtCinco;
	private JTextField txtDiez;
	private JTextField txtVeinte;
	private JTextField txtCincuenta;
	private JTextField txtCien;
	private JTextField txtQuinientos;
	
	private JPanel panelAcciones;
	
	private ButtonGroup grupoOpciones;
	private BotonCancelar btnCerrar;
	private BotonGuardar btnGuardar;
	private BotonBuscar1 btnMostrar;
	private JTextField txtTotal;

	public ViewCuentaEfectivo(Window v) {
		// TODO Auto-generated constructor stub
		
		super(v,"Contar Efectivo", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		
		panelAcciones=new PanelPadre();
		panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAcciones.setBounds(10, 11, 118, 248);
		panelAcciones.setLayout(null);
		//panelAcciones.setVisible(false);
		
		
		//this.setTitle("Facturar");
		getContentPane().add(panelAcciones);
		//btnBuscar.getInputMap().put(KeyStroke.getKeyStroke("F1"), sumar());
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setText("F1 Guardar");
		btnGuardar.setSize(101, 70);
		//btnCierre.setText("Guardar");
		btnGuardar.setHorizontalAlignment(SwingConstants.LEFT);
		btnGuardar.setLocation(8, 14);
		//btnCliente.setBounds(10, 19, 158, 80);
		panelAcciones.add(btnGuardar);
		
		btnMostrar = new BotonBuscar1();
		btnMostrar.setText("F2 Mostrar");
		btnMostrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnMostrar.setBounds(8, 85, 101, 70);
		
		panelAcciones.add(btnMostrar);
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setBounds(8, 164, 101, 70);
		panelAcciones.add(btnCerrar);
		
		txtUno = new JTextField();
		txtUno.setText("0");
		txtUno.setHorizontalAlignment(SwingConstants.LEFT);
		txtUno.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				
				try {
					if(txtUno.getText().trim().length()!=0)
						calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}

			
		});
		txtUno.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtDos.selectAll();
				txtDos.requestFocusInWindow();
			}
		});
		txtUno.setBounds(173, 72, 171, 30);
		getContentPane().add(txtUno);
		txtUno.setColumns(10);
		

		
		JLabel lblLempiras = new JLabel("1");
		lblLempiras.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLempiras.setBounds(140, 72, 25, 30);
		getContentPane().add(lblLempiras);
		
		txtDos = new JTextField();
		txtDos.setText("0");
		txtDos.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtDos.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtDos.setHorizontalAlignment(SwingConstants.LEFT);
		txtDos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtCinco.selectAll();
				txtCinco.requestFocusInWindow();
			}
		});
		txtDos.setBounds(175, 126, 171, 30);
		getContentPane().add(txtDos);
		txtDos.setColumns(10);
		
		txtCinco = new JTextField();
		txtCinco.setText("0");
		txtCinco.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtCinco.getText().trim().length()!=0)
						calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtCinco.setHorizontalAlignment(SwingConstants.LEFT);
		txtCinco.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtDiez.selectAll();
				txtDiez.requestFocusInWindow();
			}
		});
		txtCinco.setBounds(173, 180, 171, 30);
		getContentPane().add(txtCinco);
		txtCinco.setColumns(10);
		
		txtDiez = new JTextField();
		txtDiez.setText("0");
		txtDiez.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtDiez.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtDiez.setHorizontalAlignment(SwingConstants.LEFT);
		txtDiez.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtVeinte.selectAll();
				txtVeinte.requestFocusInWindow();
			}
		});
		txtDiez.setBounds(173, 234, 171, 30);
		getContentPane().add(txtDiez);
		txtDiez.setColumns(10);
		
		txtVeinte = new JTextField();
		txtVeinte.setText("0");
		txtVeinte.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtVeinte.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtVeinte.setHorizontalAlignment(SwingConstants.LEFT);
		txtVeinte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtCincuenta.selectAll();
				txtCincuenta.requestFocusInWindow();
			}
		});
		txtVeinte.setBounds(385, 72, 171, 30);
		getContentPane().add(txtVeinte);
		txtVeinte.setColumns(10);
		
		txtCincuenta = new JTextField();
		txtCincuenta.setText("0");
		txtCincuenta.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtCincuenta.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtCincuenta.setHorizontalAlignment(SwingConstants.LEFT);
		txtCincuenta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtCien.selectAll();
				txtCien.requestFocusInWindow();
			}
		});
		txtCincuenta.setBounds(383, 126, 171, 30);
		getContentPane().add(txtCincuenta);
		txtCincuenta.setColumns(10);
		
		txtCien = new JTextField();
		txtCien.setText("0");
		txtCien.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtCien.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtCien.setHorizontalAlignment(SwingConstants.LEFT);
		txtCien.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtQuinientos.selectAll();
				txtQuinientos.requestFocusInWindow();
			}
		});
		txtCien.setBounds(385, 180, 171, 30);
		getContentPane().add(txtCien);
		txtCien.setColumns(10);
		
		txtQuinientos = new JTextField();
		txtQuinientos.setText("0");
		txtQuinientos.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if(txtQuinientos.getText().trim().length()!=0)
					calcularTotal();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		txtQuinientos.setHorizontalAlignment(SwingConstants.LEFT);
		txtQuinientos.setBounds(385, 234, 171, 30);
		getContentPane().add(txtQuinientos);
		txtQuinientos.setColumns(10);
		
		JLabel lblDos = new JLabel("2");
		lblDos.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDos.setBounds(141, 126, 25, 30);
		getContentPane().add(lblDos);
		
		JLabel lblCinco = new JLabel("5");
		lblCinco.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCinco.setBounds(140, 180, 25, 30);
		getContentPane().add(lblCinco);
		
		JLabel lblDiez = new JLabel("10");
		lblDiez.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDiez.setBounds(140, 234, 25, 30);
		getContentPane().add(lblDiez);
		
		JLabel lblVeinte = new JLabel("20");
		lblVeinte.setBounds(352, 72, 35, 30);
		getContentPane().add(lblVeinte);
		
		JLabel lblCincuenta = new JLabel("50");
		lblCincuenta.setBounds(355, 126, 32, 30);
		getContentPane().add(lblCincuenta);
		
		JLabel lblCien = new JLabel("100");
		lblCien.setBounds(352, 180, 35, 30);
		getContentPane().add(lblCien);
		
		JLabel lblQuinientos = new JLabel("500");
		lblQuinientos.setBounds(352, 234, 35, 30);
		getContentPane().add(lblQuinientos);
		
		txtTotal = new JTextField();
		txtTotal.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setBounds(173, 18, 383, 42);
		getContentPane().add(txtTotal);
		txtTotal.setColumns(10);
		this.setResizable(false);
		setSize(582,333);
		txtUno.requestFocusInWindow();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		txtUno.requestFocusInWindow();
		
	}
	public JTextField getTxtUno(){
		return txtUno;
	}
	public JTextField getTxtDos(){
		return txtDos;
	}
	public JTextField getTxtCinco(){
		return txtCinco;
	}
	public JTextField getTxtDiez(){
		return txtDiez;
	}
	public JTextField getTxtVeinte(){
		return txtVeinte;
	}
	public JTextField getTxtCincuenta(){
		return txtCincuenta;
	}
	public JTextField getTxtCien(){
		return txtCien;
	}
	public JTextField getTxtQuiniento(){
		return txtQuinientos;
	}
	
	private void calcularTotal() throws Exception  {
		
		
			BigDecimal uno=new BigDecimal(this.txtUno.getText());
			BigDecimal dos=new BigDecimal(this.txtDos.getText());
			dos=dos.multiply(new BigDecimal(2));
			BigDecimal cinco=new BigDecimal(this.txtCinco.getText());
			cinco=cinco.multiply(new BigDecimal(5));
			BigDecimal diez=new BigDecimal(this.txtDiez.getText());
			diez=diez.multiply(new BigDecimal(10));
			BigDecimal veinte=new BigDecimal(this.txtVeinte.getText());
			veinte=veinte.multiply(new BigDecimal(20));
			BigDecimal cincuenta=new BigDecimal(this.txtCincuenta.getText());
			cincuenta=cincuenta.multiply(new BigDecimal(50));
			BigDecimal cien=new BigDecimal(this.txtCien.getText());
			cien=cien.multiply(new BigDecimal(100));
			BigDecimal quiniento=new BigDecimal(this.txtQuinientos.getText());
			quiniento=quiniento.multiply(new BigDecimal(500));
			
			BigDecimal total=uno.add(dos).add(cinco).add(diez).add(veinte).add(cincuenta).add(cien).add(quiniento);
			
			txtTotal.setText(""+total.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		
		
	}
	
	public void conectarControlador(CtlContarEfectivo c){
		
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addKeyListener(c);
		
		btnMostrar.addActionListener(c);
		btnMostrar.setActionCommand("MOSTRAR");
		btnMostrar.addKeyListener(c);
		
		btnCerrar.addActionListener(c);
		btnCerrar.setActionCommand("SALIR");
		btnCerrar.addKeyListener(c);
		
		txtUno.addKeyListener(c);
		txtDos.addKeyListener(c);
		txtCinco.addKeyListener(c);
		txtDiez.addKeyListener(c);
		txtVeinte.addKeyListener(c);
		txtCincuenta.addKeyListener(c);
		txtCien.addKeyListener(c);
		txtQuinientos.addKeyListener(c);
		
	}
}
