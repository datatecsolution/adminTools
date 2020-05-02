package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Proveedor;
import net.datatecsolution.admin_tools.modelo.dao.ProveedorDao;
import net.datatecsolution.admin_tools.view.ViewCrearProveedor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

public class CtlProveedor implements ActionListener, WindowListener {
	
	ViewCrearProveedor viewProveedor;//se crea la vista para mostrar los datos
	private Proveedor myProveedor; // se crea un proveedor-modelo para realizar las operacions basicas
	
	boolean verificadorAcualizacion=false;
	private ProveedorDao myProveedorDao;
	boolean resultaOperacion=false;
	
	public CtlProveedor(ViewCrearProveedor view){
		this.viewProveedor=view;
		
		myProveedorDao=  new ProveedorDao();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//se inicialza el proveedor-modelo con los datos correspondientes de la vistaCrearProveedor
		myProveedor=new Proveedor();
		myProveedor.setId(viewProveedor.getId());
		myProveedor.setNombre(viewProveedor.getTxtNombre().getText());
		myProveedor.setDireccion(viewProveedor.getTxtDireccion().getText());
		myProveedor.setTelefono(viewProveedor.getTxtTelefono().getText());
		myProveedor.setCelular(viewProveedor.getTxtCelular().getText());
		
		//ProveedorDao proveedorDao= new ProveedorDao();//objeto proveedor que administra la interfaz con la BD
		
		
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		switch(comando){
		case "GUARDAR":
			
			if(myProveedorDao.registrar(myProveedor)){//se ejecuta la accion de guardar
				JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente","Informaci�n",JOptionPane.INFORMATION_MESSAGE);
				myProveedor.setId(myProveedorDao.getIdProveedorRegistrado());//se completa el proveedor guardado con el ID asignado por la BD
				resultaOperacion=true;
				viewProveedor.setVisible(false);
				this.viewProveedor.dispose(); 
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
				resultaOperacion=false;
				viewProveedor.setVisible(false);
				this.viewProveedor.dispose(); 
			}
			
			break;
		case "ACTUALIZAR":
			
			if(myProveedorDao.actualizar(myProveedor)){//ejecuta el metodo actualize en el Dao y espera que devuelva true o false
				JOptionPane.showMessageDialog(null, "Se ha registrado Exitosamente","Informaci�n",JOptionPane.INFORMATION_MESSAGE);
				viewProveedor.setVisible(false);
				verificadorAcualizacion=true;
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
				verificadorAcualizacion=false;
			}
			break;
		
		case "CANCELAR":
			resultaOperacion=false;
			viewProveedor.setVisible(false);
			this.viewProveedor.dispose(); 
			break;
			
			
		}
		
	}
	
public Proveedor getProveedor(){
	return myProveedor;
}
public boolean actualizar(){
		
		this.viewProveedor.setVisible(true);
		return true;
	}

public boolean agregarProveedor(){
	this.viewProveedor.setVisible(true);
	return resultaOperacion;
}

@Override
public void windowOpened(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowClosing(WindowEvent e) {
	// TODO Auto-generated method stub
	
	resultaOperacion=false;
	viewProveedor.setVisible(false);
	this.viewProveedor.dispose(); 
	
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

}
