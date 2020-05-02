package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Proveedor;
import net.datatecsolution.admin_tools.modelo.dao.ProveedorDao;
import net.datatecsolution.admin_tools.view.ViewCrearProveedor;
import net.datatecsolution.admin_tools.view.ViewListaProveedor;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class CtlProveedorLista  implements ActionListener, MouseListener, WindowListener,ItemListener {
	public ViewCrearProveedor viewProveedor;
	private ViewListaProveedor view;
	
	private Proveedor myProveedor;
	private ProveedorDao myProveedorDao;
	
	private int idProveedor;
	private int filaTabla;
	private String tipoBusqueda;
	
	
	
	public CtlProveedorLista(ViewListaProveedor v){
		
		//myProveedorDao.conexion=this.conexion;
		this.view=v;
		myProveedor=new Proveedor();
		myProveedorDao=new ProveedorDao();
		view.conectarControlador(this);
		
		cargarTabla(myProveedorDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		//se recoge el comando programado en el boton que se hizo click
		String comando=e.getActionCommand();
		
		
		
		
		
		
		//ProveedorDao myProveedorDao=new ProveedorDao();
		
		switch(comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "INSERTAR":
			viewProveedor= new ViewCrearProveedor(this.view);//crea la ventana para ingresar un nuevo proveedor
			CtlProveedor ctl=new CtlProveedor(viewProveedor);//se crea el controlador de la ventana y se le pasa la view
			viewProveedor.conectarCtl(ctl);//se llama la metodo conectarCtl encargado de hacer set al manejador de eventos
			
			boolean resul=ctl.agregarProveedor();//se llama el metodo agregar proveedor que devuelve un resultado
			
			if(resul){//se procesa el resultado de agregar proveeros
				view.getModelo().setPaginacion();
				cargarTabla(myProveedorDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			}
			
			break;
			
		case "ELIMINAR":
			
			
			
			if(myProveedorDao.eliminar(idProveedor)){//llamamos al metodo para agregar 
				JOptionPane.showMessageDialog(null, "Se elimino exitosamente","Informaci�n",JOptionPane.INFORMATION_MESSAGE);
				this.view.modelo.eliminarProveedor(filaTabla);
				this.view.getBtnEliminar().setEnabled(false);
				
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
			}
			
			
			
			
			break;
			
		case "BUSCAR":
			view.getModelo().setPaginacion();
			
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){  
				myProveedor=myProveedorDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myProveedor!=null){
					this.view.modelo.limpiarProveedores();
					this.view.modelo.agregarProveedor(myProveedor);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
				}
			} 
			
			if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myProveedorDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDireccion().isSelected()){  
				cargarTabla(myProveedorDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myProveedorDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myProveedorDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myProveedorDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDireccion().isSelected()){  
				cargarTabla(myProveedorDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){
				cargarTabla(myProveedorDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnNombre().isSelected()){ //si esta selecionado la busqueda por nombre	
				
				cargarTabla(myProveedorDao.buscarPorNombre(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
		        
				}
			if(this.view.getRdbtnDireccion().isSelected()){  
				cargarTabla(myProveedorDao.buscarPorDireccion(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "CUENTASCLIENTES":
			try {
    			AbstractJasperReports.createReportSaltosProveedores(ConexionStatic.getPoolConexion().getConnection());
    			AbstractJasperReports.showViewer(this.view);
    			
    		}catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
			
			break;
		case "CUENTACLIENTE":
			
			
            
          //se consigue el proveedore de la fila seleccionada
            myProveedor=this.view.getModelo().getProveedor(view.getTabla().getSelectedRow());
            
            
            try {
				AbstractJasperReports.createReportCuentaProveedor(ConexionStatic.getPoolConexion().getConnection(),myProveedor.getId());
				AbstractJasperReports.showViewer(this.view);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		}
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
		//Recoger qu� fila se ha pulsadao en la tabla
        int filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//Se recoge el id de la fila marcada
            int identificador= (int)this.view.modelo.getValueAt(filaPulsada, 0);
            
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
	        	//myProveedor=myProveedorDao.buscarPro(identificador);
	        	myProveedor=this.view.modelo.getProveedor(filaPulsada);//se consigue el proveedore de la fila seleccionada
	           
	            viewProveedor= new ViewCrearProveedor(myProveedor,this.view);//se crea la vista para agregar proveedor con un proveedor ya hecho
				CtlProveedor ctl=new CtlProveedor(viewProveedor);
				viewProveedor.conectarCtl(ctl);
				
				
				viewProveedor.setVisible(true);//se muestra la ventana de actualizar
				
				if(ctl.verificadorAcualizacion){//se verifica si se actualizo en la BD
					this.view.modelo.cambiarProveedor(filaPulsada, ctl.getProveedor());//se cambia en la vista
					this.view.modelo.fireTableDataChanged();//se refrescan los cambios
					this.view.getTabla().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
					
				}
				
				
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		this.view.getBtnEliminar().setEnabled(true);
        		idProveedor=identificador;
        		filaTabla=filaPulsada;
        		
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
	
	public void cargarTabla(List<Proveedor> proveedores){
		
		this.view.modelo.limpiarProveedores();
		if(proveedores!=null){
			
			for(int c=0;c<proveedores.size();c++)
				this.view.modelo.agregarProveedor(proveedores.get(c));
		}
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		//this.conexion.desconectar();
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
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		this.view.getTxtBuscar().setText("");
	}


}
