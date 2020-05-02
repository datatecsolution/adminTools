package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.ConfigUserFactDao;
import net.datatecsolution.admin_tools.view.ViewListaConfigsUsuarios;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class CtlConfigUsuariosLista implements ActionListener, MouseListener {
	
	private ViewListaConfigsUsuarios view=null;
	private ConfigUserFacturacion myConfigUsuario=null;
	private ConfigUserFactDao myDao=null;
	private CajaDao cajaDao=null;
	//private ConfigUserFactDao configUserDao;
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	
	
	
	
	public CtlConfigUsuariosLista(ViewListaConfigsUsuarios v){
		
		view=v;
		myDao=new ConfigUserFactDao();
		cajaDao=new CajaDao();
		//configUserDao=new ConfigUserFactDao();
		
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		view.conectarCtl(this);
		
		view.setVisible(true);
		
		
		
	}

	private void cargarTabla(List<ConfigUserFacturacion> configs) {
		// TODO Auto-generated method stub
		view.getModelo().limpiar();
		if(configs!=null)
			for(int x=0;x<configs.size();x++)
				view.getModelo().agregar(configs.get(x));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	String user=(String)view.getModelo().getValueAt(filaPulsada, 0);
        	myConfigUsuario=view.getModelo().getUsuario(filaPulsada);  
        	
        	
        	
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		
        		
        		
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
			
			
			break;
		case "ELIMINAR":
			if(myDao.eliminar(myConfigUsuario)){//llamamos al metodo para agregar 
				JOptionPane.showMessageDialog(this.view, "Se elimino exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
				this.view.getModelo().eliminar(filaPulsada);
				this.view.getBtnEliminar().setEnabled(false);
				
			}
			else{
				JOptionPane.showMessageDialog(null, "No se Registro");
			}
			break;
		case "BUSCAR":
			
			view.getModelo().setPaginacion();
			
			if(view.getRdbtnTodos().isSelected()){
				
				view.getTxtBuscar().setText("");
                cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}else
				if(view.getRdbtnNombre().isSelected()){
					
				//	cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}else
					if(view.getRdbtnUser().isSelected()){
					
					//	cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			
			if(view.getRdbtnNombre().isSelected()){
				
			//	cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnUser().isSelected()){
				
			//	cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnUser().isSelected()){
			//	cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnNombre().isSelected()){
				view.getModelo().setPaginacion();
				//cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	}

}
