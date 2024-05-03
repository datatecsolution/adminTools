package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepVenc;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepVentasArticulo;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepVentasCategoria;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroRepVenc extends JDialog {
	private JButton btnBuscar;
	private JTextField txtArticulo;
	private JSpinner spDias;


	public ViewFiltroRepVenc(Window view) {
		
		super(view,"Reporte venta articulo", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		
		getContentPane().setBackground(PanelPadre.color1);
		
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(441, 264);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		txtArticulo = new JTextField();
		txtArticulo.setEditable(false);
		txtArticulo.setBounds(18, 24, 404, 26);
		getContentPane().add(txtArticulo);
		txtArticulo.setColumns(10);
		
		JLabel lblEmpleado = new JLabel("Categoria");
		lblEmpleado.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblEmpleado.setBounds(18, 6, 103, 16);
		getContentPane().add(lblEmpleado);
		
		JLabel lblSelecioneRangoFecha = new JLabel("Dias por vencer");
		lblSelecioneRangoFecha.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblSelecioneRangoFecha.setBounds(18, 62, 194, 16);
		getContentPane().add(lblSelecioneRangoFecha);
		
		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(116, 146, 168, 49);
		getContentPane().add(btnBuscar);
		
		spDias = new JSpinner();
		 // Crear un SpinnerModel con un valor inicial de 30
        SpinnerModel model = new SpinnerNumberModel(30, // valor inicial
                                                     1,   // valor mínimo
                                                     Integer.MAX_VALUE, // valor máximo
                                                     1);  // incremento
        spDias.setModel(model);
		spDias.setBounds(18, 90, 73, 26);
		getContentPane().add(spDias);
		
		
	}
	
	
	public JSpinner getSpDias() {
		return spDias;
	}
	/**
	 * @return the txtEmpleado
	 */
	public JTextField getTxtArticulo() {
		return txtArticulo;
	}
	
	public void conectarCtl(CtlFiltroRepVenc c){
		btnBuscar.addActionListener( c);
		btnBuscar.setActionCommand("GENERAR");
		
		txtArticulo.addKeyListener(c);
		btnBuscar.addKeyListener(c);
		
		spDias.addKeyListener(c);
		
		
	}

	
}
