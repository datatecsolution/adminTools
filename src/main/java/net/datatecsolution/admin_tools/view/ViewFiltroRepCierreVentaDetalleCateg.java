package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFiltroRepCierreVentasDetalleCateg;
import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCategoria;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroRepCierreVentaDetalleCateg extends  JDialog {
	private JButton btnBuscar;
	private JComboBox<Categoria> cbxCajas;
	private CbxTmCategoria modeloListaCajas;

	public ViewFiltroRepCierreVentaDetalleCateg(Window view) {
		
		super(view,"Filtro reporte caja categoria", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(69, 61, 168, 49);
		getContentPane().add(btnBuscar);
		
		
		modeloListaCajas=new CbxTmCategoria();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaCajas.agregar(new Categoria());
		
		
		cbxCajas = new JComboBox<Categoria>(modeloListaCajas);
		cbxCajas.setBounds(16, 19, 275, 30);
		getContentPane().add(cbxCajas);
		
		JLabel lblCaja = new JLabel("Categoria");
		lblCaja.setBounds(16, 4, 61, 16);
		getContentPane().add(lblCaja);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(305, 161);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	
	public void conectarCtl(CtlFiltroRepCierreVentasDetalleCateg c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
	}
	/**
	 * @return the cbxCajas
	 */
	public JComboBox<Categoria> getCbxCategorias() {
		return cbxCajas;
	}
	/**
	 * @return the modeloListaCajas
	 */
	public CbxTmCategoria getModeloListaCategorias() {
		return modeloListaCajas;
	}
}
