package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepBanco;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCuentasBancos;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroSaldoBanco extends JDialog {
	

	
	private JDateChooser dCBuscar1;
	private JButton btnBuscar;
	private JDateChooser dCBuscar2;
	
	private JComboBox<Banco> cbxBancos;
	private CbxTmCuentasBancos modeloCuentasBancos;

	public ViewFiltroSaldoBanco(Window view) {
		
		super(view,"Repordes de saldos de bancos", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		
		getContentPane().setBackground(PanelPadre.color1);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(512, 237);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		dCBuscar1 = new JDateChooser();
		dCBuscar1.setDateFormatString("dd-MM-yyyy");
		dCBuscar1.setSize(new Dimension(100, 20));
		dCBuscar1.setPreferredSize(new Dimension(160, 27));
		dCBuscar1.setBounds(40, 34, 194, 27);
		getContentPane().add(dCBuscar1);
		
		JLabel lblSelecioneRangoFecha = new JLabel("Selecione rango fecha");
		lblSelecioneRangoFecha.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblSelecioneRangoFecha.setBounds(15, 10, 194, 16);
		getContentPane().add(lblSelecioneRangoFecha);
		
		JLabel lblDe = new JLabel("De:");
		lblDe.setBounds(15, 39, 26, 16);
		getContentPane().add(lblDe);
		
		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setBounds(253, 39, 48, 16);
		getContentPane().add(lblHasta);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(177, 145, 168, 49);
		getContentPane().add(btnBuscar);
		
		JLabel lblEmpleado = new JLabel("Cuenta de banco");
		lblEmpleado.setBounds(15, 80, 145, 16);
		getContentPane().add(lblEmpleado);
		
		modeloCuentasBancos=new CbxTmCuentasBancos();
		
		cbxBancos = new JComboBox<Banco>();
		cbxBancos.setModel(modeloCuentasBancos);
		cbxBancos.setBounds(15, 100, 474, 33);
		getContentPane().add(cbxBancos);
		
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
	public void conectarCtl(CtlFiltroRepBanco c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
		
		
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
	 * @return the cbxBancos
	 */
	public JComboBox<Banco> getCbxBancos() {
		return cbxBancos;
	}


}
