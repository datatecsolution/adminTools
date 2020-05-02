package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCategoria;
import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewCrearCategoria extends JDialog {
	private JTextField txtMarca;
	private JLabel lblMarca;
	
	private JTextArea txtAreaObservacion;
	private JLabel lblObservacion;
	
	private BotonCancelar btnCancelar;
	private BotonActualizar btnActualizar;
	private BotonGuardar btnGuardar;
	private Categoria myCategoria;
	
	
	public ViewCrearCategoria(Categoria m,ViewListaCategorias view){
		this(view);
		myCategoria=m;
		cargarDatos();
		btnGuardar.setVisible(false);
		btnActualizar.setVisible(true);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public ViewCrearCategoria(ViewListaCategorias view) {
		super(view,"Agregar categoria", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		this.setFont(new Font("Verdana", Font.PLAIN, 12));
	
		
		lblMarca = new JLabel("Categoria");
		lblMarca.setBounds(22, 12, 90, 14);
		getContentPane().add(lblMarca);
		
		txtMarca = new JTextField();
		txtMarca.setBounds(22, 38, 260, 32);
		getContentPane().add(txtMarca);
		txtMarca.setColumns(10);
		
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
	public Categoria getMarca(){
		myCategoria.setDescripcion(txtMarca.getText());
		myCategoria.setObservacion(txtAreaObservacion.getText());
		return myCategoria;
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public JTextField getTxtMarca(){
		return txtMarca;
	}
	public JTextArea getTxtObservacion(){
		return txtAreaObservacion;
	}
	
	public void conectarControlador(CtlCategoria c){
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
	}
	
	private void cargarDatos(){
		
		txtAreaObservacion.setText(myCategoria.getObservacion());
		txtMarca.setText(myCategoria.getDescripcion());
		
	}
}
