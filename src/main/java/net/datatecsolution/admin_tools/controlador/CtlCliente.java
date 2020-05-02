package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;

import javax.swing.*;
import java.awt.event.*;

public class CtlCliente implements ActionListener,WindowListener , KeyListener{
	
	private ViewCrearCliente view;
	private Cliente myCliente=null;
	private ClienteDao myClienteDao=null;
	private boolean resultaOperacion=false;
	private Empleado myVendedor=null;
	private EmpleadoDao myEmpleadoDao;
	
	public CtlCliente(ViewCrearCliente v){
		view=v;
		view.conectarControlador(this);
		
		
		myEmpleadoDao=new EmpleadoDao();
		
		//se busca el empleado por defecto es con el id 0
		myVendedor=myEmpleadoDao.buscarPorId(1);
		if(myVendedor!=null){
			view.getTxtVendedor().setText(myVendedor.toString());
		}
		myCliente=new Cliente();
		myClienteDao=new ClienteDao();
		view.setLocationRelativeTo(view);
		view.setModal(true);
		//view.setVisible(true);
		
	}
	private void getCliente(){
		myCliente.setNombre(view.getTxtNombre().getText());
		myCliente.setDireccion(view.getTxtDireccion().getText());
		myCliente.setTelefono(view.getTxtTelefono().getText());
		myCliente.setCelular(view.getTxtMovil().getText());
		myCliente.setRtn(view.getTxtRtn().getText());
		myCliente.setVendedor(myVendedor);
	}
	
	public boolean agregarCliente(){
		this.view.setVisible(true);
		return resultaOperacion;
	}
	
	/*<<<<<<<<<<<<<<<<<<<<<<<<< metodo que devuelve el cliente guardado >>>>>>>>>>>>>>>>>>>>>>>>>         */
	public Cliente getClienteGuardado(){
		return myCliente;
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GUARDAR":
			getCliente();
			//se ejecuta la accion de guardar
			if(myClienteDao.registrar(myCliente)){
				JOptionPane.showMessageDialog(this.view, "Se ha registrado Exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
				myCliente.setId(myClienteDao.getIdClienteRegistrado());//se completa el proveedor guardado con el ID asignado por la BD
				resultaOperacion=true;
				this.view.setVisible(false);
			}else{
				JOptionPane.showMessageDialog(this.view, "No se Registro");
				resultaOperacion=false;
				this.view.setVisible(false);
			}
			break;
		case "CANCELAR":
			this.view.setVisible(false);
			break;
		case "ACTUALIZAR":
			getCliente();
				if(myClienteDao.actualizar(myCliente)){//se manda actualizar el cliente en la bd
					JOptionPane.showMessageDialog(view, "Se Actualizo el articulo");
					resultaOperacion=true;
					this.view.dispose();
				}
			break;
		}
	}
	
	
	public boolean actualizarCliente(Cliente cliente){
		//se carga la configuracionde la view articulo para la actulizacion
		this.view.configActualizar();
		
		
		//se establece el nombre de articulo en la view
		this.view.getTxtNombre().setText(cliente.getNombre());
		
		//se establece la marca en la view
		this.view.getTxtDireccion().setText(cliente.getDereccion());
		
		this.view.getTxtTelefono().setText(cliente.getTelefono());
		this.view.getTxtMovil().setText(cliente.getCelular());
		this.view.getTxtRtn().setText(cliente.getRtn());
		
		view.getTxtVendedor().setText(cliente.getVendedor().toString());
		
		
		
		//se establece el articulo de la clase this
		myCliente=cliente;
		
		
				
		// se hace visible la ventana modal
		this.view.setVisible(true);
		
		
		
		return this.resultaOperacion;
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		this.view.setVisible(false);
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		switch(e.getKeyCode()){
		
		case KeyEvent.VK_F1:
			buscarEmpleado();
			break;
	}
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void buscarEmpleado(){
		ViewListaEmpleados viewBuscarEmpleado=new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado=new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado=ctBuscarEmpleado.buscar();
		
		if(resultado){
			myVendedor=ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtVendedor().setText(myVendedor.toString());
		}
		
	}

}
