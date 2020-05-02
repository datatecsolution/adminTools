package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Requisicion;
import net.datatecsolution.admin_tools.modelo.dao.RequisicionDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewListaRequisiciones;
import net.datatecsolution.admin_tools.view.ViewRequisicion;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CtlRequisicionesLista implements ActionListener, MouseListener, ChangeListener {
	
	private ViewListaRequisiciones view=null;
	private RequisicionDao myRequiDao=null;
	private Requisicion myRequi=null;
	//fila selecciona enla lista
	private int filaPulsada;
	
	private UsuarioDao myUsuarioDao=null;

	public CtlRequisicionesLista(ViewListaRequisiciones v) {
		view=v;
		myRequiDao=new RequisicionDao();
		myRequi=new Requisicion();
		myUsuarioDao=new UsuarioDao();
		
		cargarTabla(myRequiDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.conectarCtl(this);
		view.setVisible(true);
	}
	
	public void cargarTabla(List<Requisicion> requisiciones){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModelo().limpiar();
		
		if(requisiciones!=null){
			
			for(int c=0;c<requisiciones.size();c++){
				this.view.getModelo().addRequisicion(requisiciones.get(c));
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		
        		try {
    				//this.view.setVisible(false);
    				//this.view.dispose();
    				AbstractJasperReports.createReportRequisicion( ConexionStatic.getPoolConexion().getConnection(), "requisiciones.jasper",view.getModelo().getRequisicion(view.getTabla().getSelectedRow()).getNoRequisicion() );
    				//AbstractJasperReports.showViewer();
    				AbstractJasperReports.showViewer(view);

    			} catch (SQLException ee) {
    				// TODO Auto-generated catch block
    				ee.printStackTrace();
    			}
        		
        		
    
        		
	        	
			
				
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		//this.view.getBtnEliminar().setEnabled(true);
        		
        		
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch (comando){
		
		case "INSERTAR":
			ViewRequisicion viewRequi=new ViewRequisicion(view);
			CtlRequisicion ctlRequi=new CtlRequisicion(viewRequi);
			
			
			boolean resuldoGuarda=ctlRequi.agregarRequisicion();
			
			if(resuldoGuarda){
				view.getModelo().setPaginacion();
				
				cargarTabla(myRequiDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				
			}
			break;
		case "FECHA":
			
			break;
			
		case "TODAS":
			
			
			break;
		case "ID":
			
			
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			
			//si la busqueda es por id
			if(this.view.getRdbtnId().isSelected()){
				myRequi=myRequiDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myRequi!=null){												
					this.view.getModelo().limpiar();
					this.view.getModelo().addRequisicion(myRequi);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro la factura","Error",JOptionPane.ERROR_MESSAGE);
				}
				
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				cargarTabla(myRequiDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				//this.view.getTxtBuscar1().setText("");
				//this.view.getTxtBuscar2().setText("");
				}
			
			
			//si la busqueda son tadas
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRequiDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "ANULARFACTURA":
			/*
			//se verifica si la factura ya esta agregada al kardex
			if (myRequi.getAgregadoAkardex()==0){
				
					int resul=JOptionPane.showConfirmDialog(view, "Desea anular la requisicion no. "+myRequi.getNoRequisicion()+"?");
					//sin confirmo la anulacion
					if(resul==0){
						JPasswordField pf = new JPasswordField();
						int action = JOptionPane.showConfirmDialog(view, pf,"Escriba la password admin",JOptionPane.OK_CANCEL_OPTION);
						//String pwd=JOptionPane.showInputDialog(view, "Escriba la contrase�a admin", "Seguridad", JOptionPane.INFORMATION_MESSAGE);
						if(action < 0){
							
							
						}else{
							String pwd=new String(pf.getPassword());
							//comprabacion del permiso administrativo
							if(this.myUsuarioDao.comprobarAdmin(pwd)){
								//se anula la factura en la bd
								if(myRequiDao.anularRequi(myRequi))
									myRequi.setEstado("NULA");
								//JOptionPane.showMessageDialog(view, "Usuario Valido");
							}else{
								JOptionPane.showMessageDialog(view, "Usuario Invalido");
							}
						}
						
					}
			}else{
				JOptionPane.showMessageDialog(view, "No se puede anular la compra porque ya esta en el Kardex!!!");
				this.view.getBtnEliminar().setEnabled(false);
			}
			*/
			break;
			
		case "IMPRIMIR":
			
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRequiDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				cargarTabla(myRequiDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRequiDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				cargarTabla(myRequiDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}

	}

}
