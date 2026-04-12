package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlRutaCobro;
import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewCrearRutaCobro extends JDialog {
	private JTextField txtDescripcion;
	private JLabel lblDescripcioon;

	private JTextArea txtAreaObservacion;
	private JLabel lblObservacion;

	private BotonCancelar btnCancelar;
	private BotonActualizar btnActualizar;

	public BotonGuardar getBtnGuardar() {
		return btnGuardar;
	}

	private BotonGuardar btnGuardar;


	/*
	public ViewCrearRutaCobro(RutaCobro m, ViewListaRutasCobro view){
		this(view);
		myRuta =m;
		cargarDatos();
		btnGuardar.setVisible(false);
		btnActualizar.setVisible(true);
	}

	 */

	/**
	 * @wbp.parser.constructor
	 */
	public ViewCrearRutaCobro(ViewListaRutasCobro view) {
		super(view,"Agregar ruta de cobro", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		this.setFont(new Font("Verdana", Font.PLAIN, 12));
	
		
		lblDescripcioon = new JLabel("Descripcion");
		lblDescripcioon.setBounds(22, 12, 90, 14);
		getContentPane().add(lblDescripcioon);
		
		txtDescripcion = new JTextField();
		txtDescripcion.setBounds(22, 38, 260, 32);
		getContentPane().add(txtDescripcion);
		txtDescripcion.setColumns(10);
		
		lblObservacion = new JLabel("Observacion");
		lblObservacion.setBounds(22, 82, 90, 14);
		getContentPane().add(lblObservacion);
		
		txtAreaObservacion = new JTextArea();
		txtAreaObservacion.setBounds(22, 108, 260, 130);
		getContentPane().add(txtAreaObservacion);
		
		// botones de accion
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setSize(128, 78);
		btnCancelar.setLocation(154, 277);
		getContentPane().add(btnCancelar);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setSize(136, 78);
		btnActualizar.setLocation(22, 277);
		getContentPane().add(btnActualizar);
		btnActualizar.setVisible(false);
		
		btnGuardar = new BotonGuardar();	
		btnGuardar.setLocation(22, 277);
		getContentPane().add(btnGuardar);
		
		this.setSize(309, 395);
		
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public JTextField getTxtDescripcion(){
		return txtDescripcion;
	}
	public JTextArea getTxtObservacion(){
		return txtAreaObservacion;
	}
	
	public void conectarControlador(CtlRutaCobro c){
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
	}

}
