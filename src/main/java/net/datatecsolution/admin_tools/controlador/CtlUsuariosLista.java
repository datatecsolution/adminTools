package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewCrearUsuario;
import net.datatecsolution.admin_tools.view.ViewListaUsuarios;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class CtlUsuariosLista implements ActionListener, MouseListener {
	
	private ViewListaUsuarios view=null;
	private Usuario myUsuario=null;
	private UsuarioDao myDao=null;
	private CajaDao cajaDao=null;
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	
	
	
	
	public CtlUsuariosLista(ViewListaUsuarios v){
		
		view=v;
		myDao=new UsuarioDao();
		cajaDao=new CajaDao();
		
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		view.conectarCtl(this);
		
		view.setVisible(true);
		
		
		
	}

	private void cargarTabla(List<Usuario> usuarios) {
		// TODO Auto-generated method stub
		view.getModelo().limpiar();
		if(usuarios!=null)
			for(int x=0;x<usuarios.size();x++)
				view.getModelo().agregar(usuarios.get(x));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	String user=(String)view.getModelo().getValueAt(filaPulsada, 0);
        	myUsuario=view.getModelo().getUsuario(filaPulsada);  
        	
        	
        	
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		
        		ViewCrearUsuario viewCrear=new ViewCrearUsuario(view);
        		CtlUsuario ctlUsuario=new CtlUsuario(viewCrear);
        		
        		myUsuario.setCajas(cajaDao.getCajasUsuario(myUsuario));
        		
        		boolean resul=ctlUsuario.actualizarUsuario(myUsuario);
        		
        		if(resul){
        			
        			this.view.getModelo().fireTableDataChanged();//se refrescan los cambios
        		}
        		
        		viewCrear.dispose();
        		//view=null;
        		ctlUsuario=null;
        		
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
			ViewCrearUsuario viewCrearUsuario=new ViewCrearUsuario(view);
			CtlUsuario ctlCrearUsuario=new CtlUsuario(viewCrearUsuario);
			//viewCrearUsuario.setVisible(true);
			
			boolean resul=ctlCrearUsuario.agregarUsuario();
			if(resul){
				view.getModelo().setPaginacion();
				
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				//se establece el numero de pagina en la view
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			}
			
			break;
		case "ELIMINAR":
			if(myDao.eliminar(myUsuario)){//llamamos al metodo para agregar 
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
					
					cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}else
					if(view.getRdbtnUser().isSelected()){
					
						cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
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
				
				cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnUser().isSelected()){
				
				cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(view.getRdbtnTodos().isSelected()){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnUser().isSelected()){
				cargarTabla(myDao.porUser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			if(view.getRdbtnNombre().isSelected()){
				view.getModelo().setPaginacion();
				cargarTabla(myDao.porNombre(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}
		
	}

}
