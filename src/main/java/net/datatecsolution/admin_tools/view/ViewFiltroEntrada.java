package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepEntradas;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroEntrada extends JDialog {



	private JDateChooser dCBuscar1;
	private JButton btnBuscar;
	//private JTextField txtCta;
	private JDateChooser dCBuscar2;
	private CbxTmCuentasBancos modeloCuentasBancos;
	private JComboBox<Banco> cbFormaPago;

	public ViewFiltroEntrada(Window view) {
		
		super(view,"Reporte entrada efectivo cuenta", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		
		getContentPane().setBackground(PanelPadre.color1);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(512, 211);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		dCBuscar1 = new JDateChooser();
		dCBuscar1.setDateFormatString("dd-MM-yyyy");
		dCBuscar1.setSize(new Dimension(100, 20));
		dCBuscar1.setPreferredSize(new Dimension(160, 27));
		dCBuscar1.setBounds(33, 34, 194, 27);
		getContentPane().add(dCBuscar1);
		
		JLabel lblSelecioneRangoFecha = new JLabel("Selecione rango fecha");
		lblSelecioneRangoFecha.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblSelecioneRangoFecha.setBounds(6, 6, 194, 16);
		getContentPane().add(lblSelecioneRangoFecha);
		
		JLabel lblDe = new JLabel("De:");
		lblDe.setBounds(6, 34, 26, 16);
		getContentPane().add(lblDe);
		
		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setBounds(253, 34, 48, 16);
		getContentPane().add(lblHasta);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(177, 134, 168, 49);
		getContentPane().add(btnBuscar);

		modeloCuentasBancos=new CbxTmCuentasBancos();

		cbFormaPago = new JComboBox<Banco>();
		cbFormaPago.setBounds(33, 96, 456, 26);
		cbFormaPago.setModel(modeloCuentasBancos);//para poder mostrar el formulario en modo dise�o comente esta linea
		getContentPane().add(cbFormaPago);

		/*
		
		txtCta = new JTextField();
		txtCta.setEditable(false);
		txtCta.setBounds(33, 96, 456, 26);
		getContentPane().add(txtCta);
		txtCta.setColumns(10);

		 */
		
		JLabel lblEmpleado = new JLabel("Seleccione Cuenta");
		lblEmpleado.setBounds(6, 78, 103, 16);
		getContentPane().add(lblEmpleado);
		
		dCBuscar2 = new JDateChooser();
		dCBuscar2.setSize(new Dimension(100, 20));
		dCBuscar2.setPreferredSize(new Dimension(160, 27));
		dCBuscar2.setDateFormatString("dd-MM-yyyy");
		dCBuscar2.setBounds(314, 34, 175, 27);
		getContentPane().add(dCBuscar2);
	}
	
	public JDateChooser getBuscar1(){
		return this.dCBuscar1;
	}
	public JDateChooser getBuscar2(){
		return this.dCBuscar2;
	}
	public void conectarCtl(CtlFiltroRepEntradas c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
		
		//txtCta.addKeyListener(c);
		cbFormaPago.addKeyListener(c);
		btnBuscar.addKeyListener(c);
		
		dCBuscar1.addKeyListener(c);
		dCBuscar2.addKeyListener(c);
		
		
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
	public JComboBox<Banco> getCbCuentasBancos() {
		return cbFormaPago;
	}
}
