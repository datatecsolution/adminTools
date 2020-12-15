package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCliente;
import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ViewCrearCliente extends JDialog{

	private JTextField txtIdVendedor;
	private JTextField txtNombre;
	private JTextField txtDireccion;
	private JTextField txtTelefono;
	private JTextField txtMovil;
	private JTextField txtRtn;
	
	private BotonCancelar btnCancelar;
	private BotonActualizar btnActualizar;
	private BotonGuardar btnGuardar;
	private JTextField txtVendedor;

	private JPopupMenu mcBusqueda;


	private List<JMenuItem> mntmItemBusqueda= new ArrayList<JMenuItem>();;
	
	public ViewCrearCliente() {
		
	
	
		
		setTitle("Crear Cliente");
		
		this.setSize(350,464);
		getContentPane().setLayout(null);
		
		JPanel JplPrincipal = new PanelPadre();
		JplPrincipal.setBounds(0, 0, 349, 428);
		getContentPane().add(JplPrincipal);
		JplPrincipal.setLayout(null);
		
		JLabel lblNombre = new JLabel("Nombre:");
		lblNombre.setBounds(19, 4, 60, 14);
		JplPrincipal.add(lblNombre);
		
		txtNombre = new JTextField();
		txtNombre.setBounds(19, 22, 311, 32);
		JplPrincipal.add(txtNombre);
		txtNombre.setColumns(10);
		
		JLabel lblDireccion = new JLabel("Direccion:");
		lblDireccion.setBounds(19, 58, 64, 14);
		JplPrincipal.add(lblDireccion);
		
		txtDireccion = new JTextField();
		txtDireccion.setBounds(19, 76, 311, 32);
		JplPrincipal.add(txtDireccion);
		txtDireccion.setColumns(10);
		
		JLabel lblTelefono = new JLabel("Telefono:");
		lblTelefono.setBounds(19, 112, 60, 14);
		JplPrincipal.add(lblTelefono);
		
		txtTelefono = new JTextField();
		txtTelefono.setBounds(19, 130, 311, 32);
		JplPrincipal.add(txtTelefono);
		txtTelefono.setColumns(10);
		
		JLabel lblMovil = new JLabel("Movil:");
		lblMovil.setBounds(19, 166, 64, 14);
		JplPrincipal.add(lblMovil);
		
		txtMovil = new JTextField();
		txtMovil.setBounds(19, 184, 311, 32);
		JplPrincipal.add(txtMovil);
		txtMovil.setColumns(10);
		
		JLabel lblRtn = new JLabel("RTN:");
		lblRtn.setBounds(19, 220, 60, 14);
		JplPrincipal.add(lblRtn);
		
		txtRtn = new JTextField();
		txtRtn.setBounds(19, 238, 311, 32);
		JplPrincipal.add(txtRtn);
		txtRtn.setColumns(10);
		
		// botones de accion
		btnCancelar = new BotonCancelar();
		btnCancelar.setLocation(180, 345);
		JplPrincipal.add(btnCancelar);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setLocation(19, 345);
		JplPrincipal.add(btnGuardar);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setLocation(19, 345);
		JplPrincipal.add(btnActualizar);
		
		JLabel lblVendedor = new JLabel("F1 Cobrador");
		lblVendedor.setBounds(19, 275, 119, 14);
		JplPrincipal.add(lblVendedor);

		txtIdVendedor = new JTextField();
		txtIdVendedor.setBounds(19, 290, 30, 32);
		txtIdVendedor.setToolTipText("Codigo vendedor");
		JplPrincipal.add(txtIdVendedor);

		
		txtVendedor = new JTextField();
		txtVendedor.setColumns(10);
		txtVendedor.setBounds(60, 290, 270, 32);
		JplPrincipal.add(txtVendedor);
		btnActualizar.setVisible(false);

		mcBusqueda = new JPopupMenu(); // crea el men� contextual
		
		
	}
	public JTextField getTxtNombre(){
		return txtNombre;
	}
	public  JTextField getTxtDireccion(){
		return  txtDireccion;
	}
	public JTextField getTxtTelefono(){
		return txtTelefono;
	}
	public JTextField getTxtMovil(){
		return txtMovil;
	}
	public JTextField getTxtRtn(){
		return txtRtn;
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public BotonGuardar getBtnGuardar(){
		return btnGuardar;
	}
	public void conectarControlador(CtlCliente c){

		txtIdVendedor.addActionListener(c);
		txtIdVendedor.setActionCommand("BUSCAR_VENDEDOR");
		
		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("CANCELAR");
		
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");
		
		btnGuardar.addKeyListener(c);
		btnCancelar.addKeyListener(c);
		txtNombre.addKeyListener(c);
		txtDireccion.addKeyListener(c);
		txtTelefono.addKeyListener(c);
		txtMovil.addKeyListener(c);
		txtMovil.addKeyListener(c);
		txtVendedor.addKeyListener(c);

	}
	public void configActualizar() {
		// TODO Auto-generated method stub
		this.btnActualizar.setVisible(true);
		this.btnGuardar.setVisible(false);
		
	}
	/**
	 * @return the txtVendedor
	 */
	public JTextField getTxtVendedor() {
		return txtVendedor;
	}

	public JPopupMenu getMcBusqueda() {
		return mcBusqueda;
	}

	public void setMcBusqueda(JPopupMenu mcBusqueda) {
		this.mcBusqueda = mcBusqueda;
	}
	public List<JMenuItem> getMntmItemBusqueda() {
		return mntmItemBusqueda;
	}

	public void setMntmItemBusqueda(List<JMenuItem> mntmItemBusqueda) {
		this.mntmItemBusqueda = mntmItemBusqueda;
	}

	public void setMcBusqueda() {
		for (JMenuItem itm:mntmItemBusqueda
			 ) {

			mcBusqueda.add(itm);

		}

	}
	public JTextField getTxtIdVendedor() {
		return txtIdVendedor;
	}

	public void setTxtIdVendedor(JTextField txtIdVendedor) {
		this.txtIdVendedor = txtIdVendedor;
	}
}
