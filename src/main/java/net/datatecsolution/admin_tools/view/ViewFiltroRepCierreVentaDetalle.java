package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFiltroRepCierreVentasDetalle;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCajas;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroRepCierreVentaDetalle extends  JDialog {
	private JButton btnBuscar;
	private JComboBox<Caja> cbxCajas;
	private CbxTmCajas modeloListaCajas;

	public ViewFiltroRepCierreVentaDetalle(Window view) {
		
		super(view,"Filtro reporte CAJA", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(69, 61, 168, 49);
		getContentPane().add(btnBuscar);
		
		
		modeloListaCajas=new CbxTmCajas();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaCajas.agregar(new Caja());
		
		
		cbxCajas = new JComboBox<Caja>(modeloListaCajas);
		cbxCajas.setBounds(16, 19, 275, 30);
		getContentPane().add(cbxCajas);
		
		JLabel lblCaja = new JLabel("Caja");
		lblCaja.setBounds(16, 4, 61, 16);
		getContentPane().add(lblCaja);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(305, 161);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	
	public void conectarCtl(CtlFiltroRepCierreVentasDetalle c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
	}
	/**
	 * @return the cbxCajas
	 */
	public JComboBox<Caja> getCbxCajas() {
		return cbxCajas;
	}
	/**
	 * @return the modeloListaCajas
	 */
	public CbxTmCajas getModeloListaCajas() {
		return modeloListaCajas;
	}
}
