package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlEmpleado;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewCrearEmpleado extends JDialog {
	private JTextField txtNombre;
	private JTextField txtApellido;
	private JTextField txtTelefono;
	private JTextField txtCorreo;
	private BotonGuardar btnGuardar;
	private JButton btnCancelar;
	private BotonActualizar btnActualizar;
	
	public ViewCrearEmpleado(Window view) {
		
		this.setTitle("Crear Usuario");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		this.setResizable(false);
		getContentPane().setLayout(null);
		
		JLabel lblNombre = new JLabel("Nombre");
		lblNombre.setBounds(22, 6, 61, 16);
		getContentPane().add(lblNombre);
		
		getContentPane().setBackground(PanelPadre.color1);
		
		txtNombre = new JTextField();
		txtNombre.setBounds(22, 28, 384, 42);
		getContentPane().add(txtNombre);
		txtNombre.setColumns(10);
		
		JLabel lblApellido = new JLabel("Apellido");
		lblApellido.setBounds(22, 76, 61, 16);
		getContentPane().add(lblApellido);
		
		txtApellido = new JTextField();
		txtApellido.setBounds(22, 98, 384, 42);
		getContentPane().add(txtApellido);
		txtApellido.setColumns(10);
		
		JLabel lblTelefono = new JLabel("Telefono");
		lblTelefono.setBounds(22, 146, 61, 16);
		getContentPane().add(lblTelefono);
		
		txtTelefono = new JTextField();
		txtTelefono.setBounds(22, 168, 384, 42);
		getContentPane().add(txtTelefono);
		txtTelefono.setColumns(10);
		
		JLabel lblCorreo = new JLabel("Correo");
		lblCorreo.setBounds(22, 216, 61, 16);
		getContentPane().add(lblCorreo);
		
		txtCorreo = new JTextField();
		txtCorreo.setBounds(22, 238, 384, 42);
		getContentPane().add(txtCorreo);
		txtCorreo.setColumns(10);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setLocation(52, 302);
		getContentPane().add(btnGuardar);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(240, 302);
		getContentPane().add(btnCancelar);
		
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setLocation(52, 302);
		btnActualizar.setVisible(false);
		getContentPane().add(btnActualizar);
		
		
		
		
		this.setSize(429, 412);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public BotonGuardar getBtnGuardar(){
		return btnGuardar;
	}
	public JTextField getTxtNombre(){
		return txtNombre;
	}
	public JTextField getTxtApellido(){
		return txtApellido;
	}
	public JTextField getTxtTelefono(){
		return this.txtTelefono;
	}
	public JTextField getTxtCorreo(){
		return this.txtCorreo;
	}
	
	public void conectarCtl(CtlEmpleado c){
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("CANCELAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
	}
}
