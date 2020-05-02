package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepSarCompras;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCajas;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroRepSarCompras extends  JDialog {
	private JButton btnBuscar;
	private JMonthChooser monthChooser;
	private JYearChooser yearChooser;
	private JComboBox<Caja> cbxCajas;
	private CbxTmCajas modeloListaCajas;

	public ViewFiltroRepSarCompras(Window view) {
		
		super(view,"Filtro reporte SAR compras", ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(71, 181, 168, 49);
		getContentPane().add(btnBuscar);
		
		monthChooser = new JMonthChooser();
		monthChooser.setBounds(16, 72, 275, 30);
		getContentPane().add(monthChooser);
		
		yearChooser = new JYearChooser();
		yearChooser.getSpinner().setLocation(0, 11);
		yearChooser.setBounds(16, 128, 275, 30);
		getContentPane().add(yearChooser);
		
		modeloListaCajas=new CbxTmCajas();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaCajas.agregar(new Caja());
		
		
		cbxCajas = new JComboBox<Caja>(modeloListaCajas);
		cbxCajas.setBounds(16, 19, 275, 30);
		getContentPane().add(cbxCajas);
		
		JLabel lblCaja = new JLabel("Tienda");
		lblCaja.setBounds(16, 4, 61, 16);
		getContentPane().add(lblCaja);
		
		JLabel lblMes = new JLabel("Mes");
		lblMes.setBounds(16, 58, 61, 16);
		getContentPane().add(lblMes);
		
		JLabel lblAo = new JLabel("Año");
		lblAo.setBounds(16, 112, 61, 16);
		getContentPane().add(lblAo);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(305, 270);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	public JYearChooser getAnio(){
		return yearChooser;
	}
	public JMonthChooser getMes(){
		return monthChooser;
	}
	
	public void conectarCtl(CtlFiltroRepSarCompras c){
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
