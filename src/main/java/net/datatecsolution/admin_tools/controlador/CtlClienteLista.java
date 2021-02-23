package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewListaClientes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.List;

public class CtlClienteLista implements ActionListener, MouseListener {
	private ViewListaClientes view;
	private ClienteDao clienteDao=null;
	private Cliente myCliente=null;
	private EmpleadoDao empleadoDao=null;
	
	//fila selecciona enla lista
		private int filaPulsada;
	
	public CtlClienteLista(ViewListaClientes v){
		view=v;
		view.conectarControlador(this);
		empleadoDao=new EmpleadoDao ();
		
		cargarComboBox();
		
		clienteDao=new ClienteDao();
		cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaEmpleados().setLista(this.empleadoDao.todoEmpleadosVendedores());
		
		
		//se remueve la lista por defecto
		this.view.getCbxEmpleados().removeAllItems();
	
		//
		int vendedor=view.getModeloListaEmpleados().buscarEmpleado(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda());
		this.view.getCbxEmpleados().setSelectedIndex(vendedor);
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
				ConexionStatic.getUsuarioLogin().getConfig().setVendedorEnBusqueda(miEmpleado);
			}
			
			
			
			
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
	
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "NUEVO":
			ViewCrearCliente viewNewCliente=new ViewCrearCliente();
			CtlCliente ctlCliente=new CtlCliente(viewNewCliente);
			
			boolean resuldoGuarda=ctlCliente.agregarCliente();
			
			if(resuldoGuarda){
				view.getModelo().setPaginacion();
				cargarTabla(clienteDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				
			}
			view=null;
			ctlCliente=null;
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
		case "CUENTASCLIENTES":
			try {
    			AbstractJasperReports.createReportSaltosClientes(ConexionStatic.getPoolConexion().getConnection());
    			AbstractJasperReports.showViewer(this.view);
    			
    		}catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
			
			break;
		case "CUENTACLIENTE":
			
			
            
          //se consigue el proveedore de la fila seleccionada
            myCliente=this.view.getModelo().getCliente(filaPulsada);
            
            
            try {
				AbstractJasperReports.createReportCuentaCliente(ConexionStatic.getPoolConexion().getConnection(),myCliente.getId());
				AbstractJasperReports.showViewer(this.view);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
			break;
		}
	}
	public void cargarTabla(List<Cliente> clientes){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModelo().limpiarClientes();
		if(clientes!=null){
			for(int c=0;c<clientes.size();c++){
				this.view.getModelo().agregarCliente(clientes.get(c));
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//Se recoge el id de la fila marcada
            int identificador= (int)this.view.getModelo().getValueAt(filaPulsada, 0);
            
          //se consigue el proveedore de la fila seleccionada
            myCliente=this.view.getModelo().getCliente(filaPulsada);
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		myCliente=this.view.getModelo().getCliente(filaPulsada);
        		//myArticulo=this.view.getModelo().getArticulo(filaPulsada);//se onsigue el Marca de la fila seleccionada
	           
	        	//crea la ventana para ingresar un nuevo proveedor
				ViewCrearCliente viewCliente= new ViewCrearCliente();
				
				//se crea el controlador de la ventana y se le pasa la view
				CtlCliente ctlActulizarCliente=new CtlCliente(viewCliente);
				
				
						
				
				//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
				boolean resultado=ctlActulizarCliente.actualizarCliente(myCliente);
				
				//se proceso el resultado de modificar la marca
				if(resultado){
					this.view.getModelo().cambiarCliente(filaPulsada, ctlActulizarCliente.getClienteGuardado());//se cambia en la vista
					this.view.getModelo().fireTableDataChanged();//se refrescan los cambios
					this.view.getTabla().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
				}	
			
				ctlActulizarCliente=null;
				viewCliente=null;
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		//this.view.getBtnEliminar().setEnabled(true);
        		/*idProveedor=identificador;
        		filaTabla=filaPulsada;*/
        		
        	}
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

}
