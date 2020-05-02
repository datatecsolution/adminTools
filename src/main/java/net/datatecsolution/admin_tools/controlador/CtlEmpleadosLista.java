package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.view.ViewCrearEmpleado;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class CtlEmpleadosLista implements ActionListener, MouseListener {
	private ViewListaEmpleados view;
	private EmpleadoDao myDao;
	private int filaPulsada;
	private Empleado myEmpleado;
	
	public CtlEmpleadosLista(ViewListaEmpleados v){
		view=v;
		
		
		myDao=new EmpleadoDao();
		
		view.conectarCtl(this);
		
		myEmpleado=new Empleado();
		
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		view.setVisible(true);
	}
	
	private void cargarTabla(List<Empleado> empleados) {
		// TODO Auto-generated method stub
		view.getModelo().limpiar();
		if(empleados!=null){
		for(int x=0;x<empleados.size();x++)
			view.getModelo().agregar(empleados.get(x));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//String user=(String)view.getModelo().getValueAt(filaPulsada, 0);
        	myEmpleado=view.getModelo().getEmpleado(filaPulsada);  
        	
        	
        	
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		
        		ViewCrearEmpleado viewCrearEmpleado=new ViewCrearEmpleado(view);
				CtlEmpleado ctlCrearEmpleado=new CtlEmpleado(viewCrearEmpleado);
				
        		
        		boolean resul=ctlCrearEmpleado.actualizarEmpleado(myEmpleado);
        		
        		if(resul){
        			
        			this.view.getModelo().fireTableDataChanged();//se refrescan los cambios
        		}
        		
        		viewCrearEmpleado.dispose();
				viewCrearEmpleado=null;
				ctlCrearEmpleado=null;
        		
        	}
        	
        }
        
        /*if(e.getClickCount()==1){
        	this.view.getBtnEliminar().setEnabled(true);
        }*/
		
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
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		switch(comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "INSERTAR":
			ViewCrearEmpleado viewCrearEmpleado=new ViewCrearEmpleado(view);
			CtlEmpleado ctlCrearEmpleado=new CtlEmpleado(viewCrearEmpleado);
			//viewCrearUsuario.setVisible(true);
			
			boolean resul=ctlCrearEmpleado.agregarEmpleado();
			if(resul){
				view.getModelo().setPaginacion();
				
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				//se establece el numero de pagina en la view
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			}
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			if(view.getRdbtnTodos().isSelected()){
				
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				view.getTxtBuscar().setText("");
			}else
				if(view.getRdbtnNombre().isSelected()){
					cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}else
					if(view.getRdbtnApellido().isSelected()){
						cargarTabla(myDao.porApellido(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}else
						if(view.getRdbtnId().isSelected()){
							myEmpleado=myDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText()));
							if(myEmpleado!=null){
								view.getModelo().limpiar();
								view.getModelo().agregar(myEmpleado);
							}else{
								JOptionPane.showMessageDialog(view, "No se encontro el empleado.");
							}
						}
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			
			break;
		/*case "ELIMINAR":
			if(myDao.eliminarUsuario(myUsuario.getUser())){//llamamos al metodo para agregar 
				JOptionPane.showMessageDialog(this.view, "Se elimino exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
				this.view.getModelo().eliminar(filaPulsada);
				this.view.getBtnEliminar().setEnabled(false);
				
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
			}
			break;*/
			
		case "NEXT":
			view.getModelo().netPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnNombre().isSelected()){
				cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnApellido().isSelected()){
				cargarTabla(myDao.porApellido(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnNombre().isSelected()){
				cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnApellido().isSelected()){
				cargarTabla(myDao.porApellido(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	}

}
