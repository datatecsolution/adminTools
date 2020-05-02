package net.datatecsolution.admin_tools.controlador;


import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.modelo.dao.DepartamentoDao;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CtlArticuloBuscar implements ActionListener,MouseListener, WindowListener,KeyListener {
	
	public ViewListaArticulo view;
	//public ViewCrearArticulo viewArticulo;
	
	
	private Articulo myArticulo;
	private ArticuloDao myArticuloDao;
	
	//fila selecciona enla lista
	private int filaPulsada;
	
	private boolean resultado=false;
	private DepartamentoDao deptDao=null;
	
	public CtlArticuloBuscar(ViewListaArticulo view){
		
		
		this.view=view;
		myArticulo=new Articulo();
		myArticuloDao=new ArticuloDao();
		//cargarTabla(myArticuloDao.todoArticulos());
		deptDao=new DepartamentoDao();
		cargarComboBox();
		
		//se establece el filtro para el estada de los registros
		myArticuloDao.setEstado(1);
		
		view.getPanelEstadoRegistro().setVisible(false);
		
		view.getRdbtnArticulo().setSelected(true);
		
	}
	
	private void cargarComboBox(){
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloCbxDepartamento().setLista(this.deptDao.todos());
		
		
		//se remueve la lista por defecto
		this.view.getCbxDepart().removeAllItems();
	
		this.view.getCbxDepart().setSelectedIndex(0);
	}
	
	public void cargarTabla(List<Articulo> articulos){
		//JOptionPane.showMessageDialog(view, articulos);
		
		if(articulos!=null){
			this.view.getModelo().limpiarArticulos();
			for(int c=0;c<articulos.size();c++){
				this.view.getModelo().agregarArticulo(articulos.get(c));
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		//myArticuloDao.desconectarBD();
		//JOptionPane.showMessageDialog(view, "Se esta Cerrando");
		this.myArticulo=null;
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
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        //JOptionPane.showMessageDialog(view, filaPulsada);
		if (e.getClickCount() == 2){
			
			myArticulo=this.view.getModelo().getArticulo(filaPulsada);
			
			resultado=true;
			//myArticuloDao.desconectarBD();
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
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//se recoge el comando programado en el boton que se hizo click
				String comando=e.getActionCommand();
				

				//Se establece el departamento seleccionado
				myArticuloDao.setMyBodega(view.getModeloCbxDepartamento().getDepartamento(view.getCbxDepart().getSelectedIndex()));
				
				switch(comando){
				case "ESCRIBIR":
					view.setTamanioVentana(1);
					break;
				
				case "BUSCAR":
					//se restablece la paginacion
					view.getModelo().setPaginacion();
					//si se seleciono el boton ID
					if(this.view.getRdbtnId().isSelected()){  
						
						myArticulo=myArticuloDao.buscarArticulo(Integer.parseInt(this.view.getTxtBuscar().getText()));
						if(myArticulo!=null){
							resultado=true;
							this.view.getModelo().limpiarArticulos();
							this.view.getModelo().agregarArticulo(myArticulo);
							view.setVisible(false);
						}else{
							JOptionPane.showMessageDialog(view, "No se encuentro el articulo");
							this.view.getTxtBuscar().setText("");
						}
					} 
					
					if(this.view.getRdbtnArticulo().isSelected()){ //si esta selecionado la busqueda por nombre	
						
						if(this.filaPulsada>=0 && myArticulo!=null){
							resultado=true;
						}
				        view.setVisible(false);
						}
					if(this.view.getRdbtnMarca().isSelected()){  
						
						cargarTabla(myArticuloDao.buscarArticuloMarca(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
							if(myArticulo!=null){
								view.setVisible(false);
							}else{
								this.view.getTxtBuscar().setText("");
							}
						}
					
					if(this.view.getRdbtnTodos().isSelected()){  
					
						cargarTabla(myArticuloDao.todoArticulos());
						this.view.getTxtBuscar().setText("");
						}
					
					//se establece el numero de pagina en la view
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
					break;
					
					case "NEXT":
						view.getModelo().netPag();
						if(this.view.getRdbtnTodos().isSelected()){  
							
							
							cargarTabla(myArticuloDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
							
							
						}
						if(view.getRdbtnArticulo().isSelected()){
							cargarTabla(myArticuloDao.buscarArticulo(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
						}
						if(this.view.getRdbtnMarca().isSelected()){  
							
							cargarTabla(myArticuloDao.buscarArticuloMarca(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
							}
						view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
						break;
					case "LAST":
						view.getModelo().lastPag();
						
						if(this.view.getRdbtnTodos().isSelected()){  

							cargarTabla(myArticuloDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
							
							
						}
						if(view.getRdbtnArticulo().isSelected()){
							cargarTabla(myArticuloDao.buscarArticulo(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
						}
						
						if(this.view.getRdbtnMarca().isSelected()){  
							
							//cargarTabla(myArticuloDao.buscarArticuloMarca(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteInferior()));
							}
						view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
						break;
				
				
					
						
					}
	}
	public Articulo getArticulo(){
		return myArticulo;
	}
	//public void buscarArticulo()
	public boolean buscarArticulo(Window v){
		
		//this.myArticuloDao.cargarInstrucciones();
		//cargarTabla(myArticuloDao.todoArticulos());
		myArticulo=null;
		this.view.getBtnEliminar().setEnabled(false);
		this.view.getBtnAgregar().setEnabled(false);
		this.view.setLocationRelativeTo(v);
		this.view.setModal(true);
		view.getTxtBuscar().requestFocusInWindow();
		
		//view.setFocusable(true);
		this.view.setVisible(true);
		//
		return resultado;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()!=0){
			
			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnArticulo().isSelected()){
				
				this.filaPulsada=this.view.getTabla().getSelectedRow();
				
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					filaPulsada++;
					this.view.getTabla().setRowSelectionInterval(0	,filaPulsada);
					
					myArticulo=view.getModelo().getArticulo(filaPulsada);
					
					
				}else
					if(e.getKeyCode()==KeyEvent.VK_UP){
						
						filaPulsada--;
						this.view.getTabla().setRowSelectionInterval(0	, filaPulsada);
						myArticulo=view.getModelo().getArticulo(filaPulsada);
					}
				
				
				
				//this.view.getTablaArticulos().setRowSelectionInterval(0	, 0);
				
				//myArticulo=view.getModelo().getArticulo(0);
			}
		
		
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
		//filaPulsada = this.view.getTablaArticulos().getSelectedRow();
		//JOptionPane.showConfirmDialog(view, filaPulsada);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			resultado=false;
			this.myArticulo=null;
	         view.setVisible(false);
	      }
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			
			if(filaPulsada>0){
	            
	            //se consigue el proveedore de la fila seleccionada
	            myArticulo=this.view.getModelo().getArticulo(filaPulsada);// .getCliente(filaPulsada);
	            this.resultado=true;
	            
				//myArticulo=view.getModelo().getArticulo(filaPulsada-1);4
				view.setVisible(false);
			}
		}
		
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()>=3&&e.getKeyCode()!=KeyEvent.VK_UP&&e.getKeyCode()!=KeyEvent.VK_DOWN){
			
			//Se establece el departamento seleccionado
			myArticuloDao.setMyBodega(view.getModeloCbxDepartamento().getDepartamento(view.getCbxDepart().getSelectedIndex()));
			view.getModelo().setPaginacion();
			
			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnArticulo().isSelected()){
				
				
				
				cargarTabla(myArticuloDao.buscarArticulo(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				this.view.getTabla().setRowSelectionInterval(0	, 0);
				filaPulsada=1;
				
				myArticulo=view.getModelo().getArticulo(0);
			}
			
			//si esta activado las busqueda por Marca
			if(this.view.getRdbtnMarca().isSelected()){  
				cargarTabla(myArticuloDao.buscarArticuloMarca(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				this.view.getTabla().setRowSelectionInterval(0	, 0);
				filaPulsada=1;
				
				
				myArticulo=view.getModelo().getArticulo(0);
			}
			
			//si esta activado la busqueda por id
			if(this.view.getRdbtnId().isSelected()){ 

				
				myArticulo=myArticuloDao.buscarArticuloBarraCod(this.view.getTxtBuscar().getText());
				if(myArticulo!=null){												
					this.view.getModelo().limpiarArticulos();
					this.view.getModelo().agregarArticulo(myArticulo);
					this.view.getTabla().setRowSelectionInterval(0	, 0);
					filaPulsada=1;
					
					myArticulo=view.getModelo().getArticulo(0);
				}
			} 
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		}
	}

}
