package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCaja;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmDepartamento;

import javax.swing.*;
import java.awt.*;

public class ViewCrearCaja extends JDialog {
	
	private JTextArea txtAreaDescripcion;
	
	private BotonCancelar btnCancelar;
	private BotonActualizar btnActualizar;
	private BotonGuardar btnGuardar;
	private JLabel lblBodega;

	private JComboBox cbxDepart;
	private CbxTmDepartamento modeloCbx;
	
	public ViewCrearCaja(Window view){
		super(view,"Agregar caja", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		this.setFont(new Font("Verdana", Font.PLAIN, 12));
		
		modeloCbx=new CbxTmDepartamento();//comentar para ver en forma de dise�o
		
		
		
		txtAreaDescripcion = new JTextArea();
		txtAreaDescripcion.setBounds(22, 30, 260, 130);
		getContentPane().add(txtAreaDescripcion);
		
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setSize(128, 78);
		btnCancelar.setLocation(166, 253);
		getContentPane().add(btnCancelar);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setSize(136, 78);
		btnActualizar.setLocation(15, 253);
		getContentPane().add(btnActualizar);
		btnActualizar.setVisible(false);
		
		btnGuardar = new BotonGuardar();	
		btnGuardar.setLocation(15, 254);
		getContentPane().add(btnGuardar);
		
		JLabel lblDescripcion = new JLabel("Descripcion");
		lblDescripcion.setBounds(22, 6, 91, 16);
		getContentPane().add(lblDescripcion);
		
		lblBodega = new JLabel("Bodega");
		lblBodega.setBounds(22, 174, 61, 16);
		getContentPane().add(lblBodega);
		
		 cbxDepart = new JComboBox();
		 cbxDepart.setBounds(22, 198, 260, 42);
		getContentPane().add(cbxDepart);
		cbxDepart.setModel(modeloCbx);
		
		this.setSize(309, 360);
		
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	public JComboBox getCbxDepart(){
		return cbxDepart;
	}
	public CbxTmDepartamento getModeloCbx(){
		return modeloCbx;
	}

	/**
	 * @return the txtAreaDescripcion
	 */
	public JTextArea getTxtAreaDescripcion() {
		return txtAreaDescripcion;
	}
	
	public void conectarControlador(CtlCaja c){
		
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addKeyListener(c);
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
		btnActualizar.addKeyListener(c);
		
		this.btnCancelar.addActionListener(c);
		this.btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addKeyListener(c);
		
	}

	/**
	 * @return the btnCancelar
	 */
	public BotonCancelar getBtnCancelar() {
		return btnCancelar;
	}

	/**
	 * @return the btnActualizar
	 */
	public BotonActualizar getBtnActualizar() {
		return btnActualizar;
	}

	/**
	 * @return the btnGuardar
	 */
	public BotonGuardar getBtnGuardar() {
		return btnGuardar;
	}
}
