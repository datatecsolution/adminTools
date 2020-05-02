package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewListaClientes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CtlClienteBuscar implements ActionListener ,MouseListener, WindowListener, KeyListener{
	

	public ViewListaClientes view;
	
	private ClienteDao clienteDao=null;
	private Cliente myCliente=null;
	//fila selecciona enla lista
	private int filaPulsada;
	private boolean resultado=false;
	private EmpleadoDao empleadoDao=null;
	
	public CtlClienteBuscar(ViewListaClientes v){
		view=v;
		view.conectarControladorBuscar(this);
		view.getBtnAgregar().setEnabled(true);
		clienteDao=new ClienteDao();
		empleadoDao=new EmpleadoDao ();
		cargarComboBox();
		view.getRdbtnNombre().setSelected(true);
		//cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		//view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaEmpleados().setLista(this.empleadoDao.todoEmpleadosVendedores());
		
		
		//se remueve la lista por defecto
		this.view.getCbxEmpleados().removeAllItems();
	
		//
		int vendedor=view.getModeloListaEmpleados().buscarImpuesto(ConexionStatic.getUsuarioLogin().getConfig().getVendedorBusqueda());
		this.view.getCbxEmpleados().setSelectedIndex(vendedor);
	}
	
	
	public void cargarTabla(List<Cliente> clientes){
	
		this.view.getModelo().limpiarClientes();
		if(clientes!=null){
			for(int c=0;c<clientes.size();c++){
				this.view.getModelo().agregarCliente(clientes.get(c));
			}
		}
	}
	
public boolean buscarCliente(Window v){
		
		//this.myArticuloDao.cargarInstrucciones();
		//cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		//this.view.getBtnEliminar().setEnabled(false);
		if(ConexionStatic.getUsuarioLogin().getConfig().isAgregarClienteCredito()){
			this.view.getBtnAgregar().setEnabled(true);
		}
		this.view.getBtnEliminar().setEnabled(false);
		this.view.setLocationRelativeTo(v);
		this.view.setModal(true);
		view.getTxtBuscar().requestFocusInWindow();
		
		this.view.setVisible(true);
		
		return resultado;
	}
	public Cliente getCliente(){
		return this.myCliente;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		switch (comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
			
		case "CAMBIOCOMBOBOX":
				//JOptionPane.showMessageDialog(view, "Cambio el vendedor");
			
				Empleado miEmpleado=(Empleado)view.getCbxEmpleados().getSelectedItem();
				
				if(miEmpleado!=null){
					ConexionStatic.getUsuarioLogin().getConfig().setVendedorBusqueda(miEmpleado);
				}
				
				//JOptionPane.showMessageDialog(view, "Cambio el vendedor "+miEmpleado.toString());
				
			break;
			
		case "BUSCAR":
			
			//si se seleciono el boton ID
			view.getModelo().setPaginacion();
			if(this.view.getRdbtnId().isSelected()){  
				myCliente=clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myCliente!=null){												
					this.view.getModelo().limpiarClientes();
					this.view.getModelo().agregarCliente(myCliente);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro el registro","Error",JOptionPane.ERROR_MESSAGE);
				}
			} 
		
			if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(clienteDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnRtn().isSelected()){  
				cargarTabla(clienteDao.buscarPorRtn(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		
			break;
			
			
			
		case "NUEVO":
			if(ConexionStatic.getUsuarioLogin().getConfig().isAgregarClienteCredito()){
				ViewCrearCliente viewNewCliente=new ViewCrearCliente();
				CtlCliente ctlCliente=new CtlCliente(viewNewCliente);
				
				boolean resuldoGuarda=ctlCliente.agregarCliente();
				if(resuldoGuarda){
					view.getModelo().setPaginacion();
					cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				}
				viewNewCliente=null;
				ctlCliente=null;
			}
			break;
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnRtn().isSelected()){  
				cargarTabla(clienteDao.buscarPorRtn(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
            if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre
				
				cargarTabla(clienteDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnRtn().isSelected()){  
				cargarTabla(clienteDao.buscarPorRtn(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(clienteDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
		if (e.getClickCount() == 2){
			myCliente=this.view.getModelo().getCliente(filaPulsada);
			resultado=true;
			//clienteDao.desconectarBD();
			this.view.setVisible(false);
			//JOptionPane.showMessageDialog(null,myMarca);
			this.view.dispose();
			
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
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
		
		//if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()!=0){
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()>=3&&e.getKeyCode()!=KeyEvent.VK_ENTER){
			filaPulsada = this.view.getTabla().getSelectedRow();
			
			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnNombre().isSelected()){
				
				//this.filaPulsada=this.view.getTabla().getSelectedRow();
				
				
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					filaPulsada++;
					this.view.getTabla().setRowSelectionInterval(0	,filaPulsada);
					
					this.myCliente=this.view.getModelo().getCliente(filaPulsada);
					
					
				}else
					if(e.getKeyCode()==KeyEvent.VK_UP){
						
						filaPulsada--;
						this.view.getTabla().setRowSelectionInterval(0	, filaPulsada);
						this.myCliente=this.view.getModelo().getCliente(filaPulsada);
					}
				//JOptionPane.showMessageDialog(view, "Entro aqui pressed "+filaPulsada);
				
				
			
			}
		
		
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
		//si apreta el boton escape se sale de la busqueda
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			resultado=false;
			this.myCliente=null;
	         view.setVisible(false);
	      }
		
		//si apreta el boton enter y tiene seleccionado un cliente 
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			//filaPulsada = this.view.getTabla().getSelectedRow();
			//JOptionPane.showMessageDialog(view, "Entro aqui released "+filaPulsada);
			if(filaPulsada>=0){
			
	            //se consigue el proveedore de la fila seleccionada
	            this.myCliente=this.view.getModelo().getCliente(filaPulsada);// .getCliente(filaPulsada);
	            this.resultado=true;
	            
				//myArticulo=view.getModelo().getArticulo(filaPulsada-1);4
				view.setVisible(false);
			}
		}
		
		
		//si escribe en la busque un cliente
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()>=3&&e.getKeyCode()!=KeyEvent.VK_UP&&e.getKeyCode()!=KeyEvent.VK_DOWN&&e.getKeyCode()!=KeyEvent.VK_ENTER){
			
			//Se establece el departamento seleccionado
			//myArticuloDao.setMyBodega(view.getModeloCbxDepartamento().getDepartamento(view.getCbxDepart().getSelectedIndex()));
			view.getModelo().setPaginacion();
			
			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnNombre().isSelected()){
				
				
				
				cargarTabla(clienteDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				this.view.getTabla().setRowSelectionInterval(0	, 0);
				filaPulsada=0;
				
				this.myCliente=view.getModelo().getCliente(0);
			}
			
			//si esta activado las busqueda por Marca
			if(this.view.getRdbtnRtn().isSelected()){  
				cargarTabla(clienteDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				this.view.getTabla().setRowSelectionInterval(0	, 0);
				filaPulsada=0;
				
				
				this.myCliente=view.getModelo().getCliente(0);
			}
			
			//si esta activado la busqueda por id
			if(this.view.getRdbtnId().isSelected()){ 
				
				myCliente=clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myCliente!=null){												
					this.view.getModelo().limpiarClientes();
					this.view.getModelo().agregarCliente(myCliente);
					this.view.getTabla().setRowSelectionInterval(0	, 0);
					filaPulsada=0;
					
					this.myCliente=view.getModelo().getCliente(0);
				}
			} 
			
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		}
		
	}

}
