package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.SalidaCaja;
import net.datatecsolution.admin_tools.modelo.dao.SalidaCajaDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewFiltroSalidas;
import net.datatecsolution.admin_tools.view.ViewListaSalidas;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.List;

public class CtlSalidasListas implements ActionListener, MouseListener {
	private ViewListaSalidas view;
	
	
	private SalidaCajaDao myDao;
	private SalidaCaja mySalida;
	private UsuarioDao myUsuarioDao=null;
	
	private int filaPulsada;
	
	
	public CtlSalidasListas(ViewListaSalidas v){
		view=v;
		view.concetarControlador(this);
		
		mySalida=new SalidaCaja();
		
		myDao=new SalidaCajaDao();
		myUsuarioDao=new UsuarioDao();
		
		
		
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		view.setVisible(true);
		
	}
	
	private void cargarTabla(List<SalidaCaja> salidas) {
		// TODO Auto-generated method stub
		view.getModelo().limpiar();
		if(salidas!=null){
		for(int x=0;x<salidas.size();x++)
			view.getModelo().agregar(salidas.get(x));
		}
	}
	
	public boolean verificarSelecion(){
		//fsdf
		boolean resul=false;
		
		if(view.getTabla().getSelectedRow()!=-1){
			this.filaPulsada=view.getTabla().getSelectedRow();
			this.mySalida=view.getModelo().getSalida(filaPulsada);
			resul=true;
		}else{
			JOptionPane.showMessageDialog(view,"No seleccion una fila. Debe Selecionar una fila primero","Error",JOptionPane.ERROR_MESSAGE);
		}
		return resul;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		//se recoge el comando programado en el boton que se hizo click
				String comando=e.getActionCommand();
				

				
				
				switch(comando){
				
				case "ELIMINAR":
					
					//se verifica que se haya selecciona una fila
					if(verificarSelecion()){
									//JOptionPane.showMessageDialog(null,"Hola "+mySalida.getEstado()+ ", row selected no:"+view.getTabla().getSelectedRow());
									int resul=JOptionPane.showConfirmDialog(view, "Desea "+((mySalida.getEstado().equals("ACT")?"NULA":"ACT"))  +" la salida no "+this.mySalida.getCodigoSalida()+"?");
									//sin confirmo la anulacion
									if(resul==0){
										JPasswordField pf = new JPasswordField();
										int action = JOptionPane.showConfirmDialog(view, pf,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
										//String pwd=JOptionPane.showInputDialog(view, "Escriba la contrase�a admin", "Seguridad", JOptionPane.INFORMATION_MESSAGE);
										if(action < 0){
											
											
										}else{
											String pwd=new String(pf.getPassword());
											//comprabacion del permiso administrativo
											if(this.myUsuarioDao.comprobarAdmin(pwd)){
												
												
												
												
												
												mySalida.setEstado((mySalida.getEstado().equals("ACT"))?"NULA":"ACT");
												
												//se anula la factura en la bd
												if(this.myDao.actualizarEstado(mySalida)){
													this.actionPerformed(new ActionEvent(this, resul, "BUSCAR"));
												}//fin de la verificacion de la  anulacion del encabezado de la facrura
												else
													JOptionPane.showMessageDialog(view, "No se puedo realizar la operacion","Error de validacion!!",JOptionPane.ERROR_MESSAGE);
												
											}else{
												JOptionPane.showMessageDialog(view, "Usuario Invalido","Error de validacion!!",JOptionPane.ERROR_MESSAGE);
											}
										}
										
									}
							
					}
					
					break;
				case "BUSCAR":
					//si se seleciono el boton ID
					if(this.view.getRdbtnId().isSelected()){
						
						
						this.mySalida=myDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText()));
						if(mySalida!=null){
							view.getModelo().limpiar();
							view.getModelo().agregar(mySalida);
						}
						//myArticulo=myArticuloDao.buscarArticulo(Integer.parseInt(this.view.getTxtBuscar().getText()));
				/*		myArticulo=myArticuloDao.buscarArticuloBarraCod(this.view.getTxtBuscar().getText());
						if(myArticulo!=null){												
							this.view.getModelo().limpiarArticulos();
							this.view.getModelo().agregarArticulo(myArticulo);
						}else{
							JOptionPane.showMessageDialog(view, "No se encuentro el articulo");
						}*/
					} 
					
					if(this.view.getRdbtnEmpleado().isSelected()){
						cargarTabla(myDao.buscarPorNombreEmpleado(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
					
				/*	if(this.view.getRdbtnArticulo().isSelected()){ //si esta selecionado la busqueda por nombre	
						
						cargarTabla(myDao.todos(view.getModelo().getLimiteInferior(),view.getModelo().getLimiteSuperior()));
				        
						}
					if(this.view.getRdbtnMarca().isSelected()){  
						cargarTabla(myArticuloDao.buscarArticuloMarca(this.view.getTxtBuscar().getText()));
						}*/
					
					if(this.view.getRdbtnTodos().isSelected()){  
						cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				//		this.view.getTxtBuscar().setText("");
						}
					break;
				
				
				
					
				
					
				case "NEXT":
					view.getModelo().netPag();
					if(this.view.getRdbtnTodos().isSelected()){
						cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					}
					if(this.view.getRdbtnEmpleado().isSelected()){
						cargarTabla(myDao.buscarPorNombreEmpleado(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
					
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
					break;
				case "LAST":
					view.getModelo().lastPag();
					if(this.view.getRdbtnTodos().isSelected()){
						cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					}
					if(this.view.getRdbtnEmpleado().isSelected()){
						cargarTabla(myDao.buscarPorNombreEmpleado(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
					}
					
					view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
					break;
				case "ESCRIBIR":
					view.setTamanioVentana(1);
					break;
				case "TODOS":
					
					view.getModelo().limpiar();
					view.getTxtBuscar().setText("");
					view.getTxtBuscar().setRequestFocusEnabled(true);
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				break;
				case "REPORTE":
						ViewFiltroSalidas vFiltroSalidas=new ViewFiltroSalidas(null);
						CtlFiltroRepSalidas cFiltroSalidas=new CtlFiltroRepSalidas(vFiltroSalidas);
						
					break;
					
		
							
						
			}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
      //si fue doble click mostrar modificar
    	if (e.getClickCount() == 2) {
    		
    		
    		this.mySalida=view.getModelo().getSalida(filaPulsada);
    		try {
				
    			//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
				AbstractJasperReports.createReportSalidaCaja(ConexionStatic.getPoolConexion().getConnection(), mySalida.getCodigoSalida());
				
				//AbstractJasperReports.Imprimir2();
				//JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.");
				
				//view.setVisible(false);
				//AbstractJasperReports.imprimierFactura();
				AbstractJasperReports.showViewer(view);
				//AbstractJasperReports.imprimierFactura();
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
    	
    		/*ViewFacturar viewFacturar=new ViewFacturar(this.view);
    		CtlFacturar ctlFacturar=new CtlFacturar(viewFacturar,conexion);
    		
    		//si se cobro la factura se debe eleminiar el temp por eso se guarda el id
    		int idFactura=myFactura.getIdFactura();
    		
    		//se llama al controlador de la factura para que la muestre 
    		ctlFacturar.viewFactura(myFactura);//actualizarFactura(myFactura);
    		
    		//si la factura se cobro se regresara null sino modificamos la factura en la lista
    		if(myFactura==null){
    			this.view.getModelo().eliminarFactura(filaPulsada);
    			myFacturaDao.EliminarTemp(idFactura);
    		}else{
    			this.view.getModelo().cambiarArticulo(filaPulsada, myFactura);
    			this.view.getTablaFacturas().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
    		}
    		viewFacturar.dispose();
    		ctlFacturar=null;*/
    		
        	
		
			
			
			
        }//fin del if del doble click
		
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
