package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepComisiones;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepVentasUsuarios;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroComisiones extends JDialog {
	
	private JDateChooser dCBuscar1;
	private JDateChooser dCBuscar2;
	private JButton btnBuscar;
	private JSpinner spPorcentaje;
	private SpinnerNumberModel modelJs;

	public ViewFiltroComisiones(Window view) {
		
		super(view,"Reporte Comisiones", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		getContentPane().setBackground(PanelPadre.color1);
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(289, 286);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		dCBuscar1 = new JDateChooser();
		dCBuscar1.setDateFormatString("dd-MM-yyyy");
		dCBuscar1.setSize(new Dimension(100, 20));
		dCBuscar1.setPreferredSize(new Dimension(160, 27));
		dCBuscar1.setBounds(26, 27, 244, 27);
		getContentPane().add(dCBuscar1);
		
		dCBuscar2 = new JDateChooser();
		dCBuscar2.setDateFormatString("dd-MM-yyyy");
		dCBuscar2.setPreferredSize(new Dimension(160, 27));
		dCBuscar2.setBounds(26, 86, 244, 27);
		getContentPane().add(dCBuscar2);
		
		JLabel lblDe = new JLabel("Desde:");
		lblDe.setBounds(26, 8, 92, 16);
		getContentPane().add(lblDe);
		
		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setBounds(26, 67, 48, 16);
		getContentPane().add(lblHasta);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(55, 193, 168, 49);
		getContentPane().add(btnBuscar);
		
		JLabel lblPorcentajeSv = new JLabel("Porcentaje S/V");
		lblPorcentajeSv.setBounds(26, 126, 144, 16);
		getContentPane().add(lblPorcentajeSv);
		
		spPorcentaje = new JSpinner();
		modelJs=new SpinnerNumberModel(1,1,20,1);
		spPorcentaje.setModel(modelJs);
		
		spPorcentaje.setBounds(26, 145, 244, 26);
		getContentPane().add(spPorcentaje);
	}
	
	public JDateChooser getBuscar1(){
		return this.dCBuscar1;
	}
	public JDateChooser getBuscar2(){
		return this.dCBuscar2;
	}
	public void conectarCtl(CtlFiltroRepComisiones c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
	}
	public void conectarCtl(CtlFiltroRepVentasUsuarios c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
	}

	/**
	 * @return the spPorcentaje
	 */
	public JSpinner getSpPorcentaje() {
		return spPorcentaje;
	}

	/**
	 * @return the modelJs
	 */
	public SpinnerNumberModel getModelJs() {
		return modelJs;
	}
}
