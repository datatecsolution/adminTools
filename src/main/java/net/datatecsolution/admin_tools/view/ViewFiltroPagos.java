package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepPagos;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroPagos extends JDialog {
	

	
	private JDateChooser dCBuscar1;
	private JButton btnBuscar;
	private JTextField txtCliente;
	private JDateChooser dCBuscar2;

	public ViewFiltroPagos(Window view) {
		
		super(view,"Reporte pagos clientes", ModalityType.DOCUMENT_MODAL);
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
		
		txtCliente = new JTextField();
		txtCliente.setEditable(false);
		txtCliente.setBounds(33, 96, 456, 26);
		getContentPane().add(txtCliente);
		txtCliente.setColumns(10);
		
		JLabel lblEmpleado = new JLabel("F1 - Cliente");
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
	public void conectarCtl(CtlFiltroRepPagos c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
		
		txtCliente.addKeyListener(c);
		btnBuscar.addKeyListener(c);
		
		dCBuscar1.addKeyListener(c);
		dCBuscar2.addKeyListener(c);
		
		
	}

	/**
	 * @return the txtEmpleado
	 */
	public JTextField getTxtCliente() {
		return txtCliente;
	}
}
