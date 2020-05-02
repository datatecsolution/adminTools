package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.FacturaCompra;
import net.datatecsolution.admin_tools.modelo.dao.DevolucionesCompraDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaCompraDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewAgregarCompras;
import net.datatecsolution.admin_tools.view.ViewListaFacturasCompra;

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

public class CtlFacturasCompra implements ActionListener, MouseListener, ChangeListener {
	private ViewListaFacturasCompra view;
	private FacturaCompraDao facturaCompraDao=null;
	private UsuarioDao myUsuarioDao=null;
	private FacturaCompra myFacturaCompra=null;
	private DevolucionesCompraDao devolucionDao=null;
	
	//fila selecciona enla lista
		private int filaPulsada;
	
	
	
	public CtlFacturasCompra(ViewListaFacturasCompra v){
		
		view =v;
		view.conectarControlador(this);
		
		facturaCompraDao=new FacturaCompraDao();
		myUsuarioDao=new UsuarioDao();
		devolucionDao=new DevolucionesCompraDao();
		
		myFacturaCompra=new FacturaCompra();
		
		cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.setVisible(true);
		
	}
	
	public void cargarTabla(List<FacturaCompra> facturas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiarFacturas();
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				this.view.getModelo().agregarFactura(facturas.get(c));
				
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
        	
        	//Se recoge el id de la fila marcada
            int idFactura= (int)this.view.getModelo().getValueAt(filaPulsada, 0);
            
            this.view.getBtnEliminar().setEnabled(true);
            //this.view.getBtnImprimir().setEnabled(true);
            this.myFacturaCompra=this.view.getModelo().getFactura(filaPulsada);
            //se consigue el proveedore de la fila seleccionada
           // myArticulo=this.view.getModelo().getArticulo(filaPulsada);
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		try {
    				//this.view.setVisible(false);
    				//this.view.dispose();
    				//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Compra_Saint_Paul.jasper",idFactura);
    				AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 2, idFactura);
    				AbstractJasperReports.showViewer(this.view);
    				//AbstractJasperReports.imprimierFactura();
    				this.view.getBtnImprimir().setEnabled(false);
    				myFacturaCompra=null;
    			} catch (SQLException ee) {
    				// TODO Auto-generated catch block
    				ee.printStackTrace();
    			}
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		this.view.getBtnEliminar().setEnabled(true);
        		
        		
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
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			//si la busqueda es por id
			if(this.view.getRdbtnId().isSelected()){
				myFacturaCompra=facturaCompraDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myFacturaCompra!=null){												
					this.view.getModelo().limpiarFacturas();
					this.view.getModelo().agregarFactura(myFacturaCompra); 
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro la factura");
				}
				
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(facturaCompraDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				}
			
			
			//si la busqueda son tadas
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			
			if(view.getRdbtnProveedor().isSelected()){
				
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(facturaCompraDao.buscarPorProveedor(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "ANULARFACTURA":

			//Recoger qu� fila se ha pulsadao en la tabla
	        filaPulsada = this.view.getTabla().getSelectedRow();
	        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        		this.myFacturaCompra=this.view.getModelo().getFactura(filaPulsada);
	        		
	        		if(myFacturaCompra.getEstado().equals("ACT")){
							int resul=JOptionPane.showConfirmDialog(view, "Desea anular la factura no "+myFacturaCompra.getIdFactura()+"?");
							//sin confirmo la anulacion
							if(resul==0){
								JPasswordField pf = new JPasswordField();
								int action = JOptionPane.showConfirmDialog(view, pf,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
							
								if(action < 0){
									
									
								}else{
										String pwd=new String(pf.getPassword());
										//comprabacion del permiso administrativo
										if(this.myUsuarioDao.comprobarAdmin(pwd)){
											//se anula la factura en la bd
											if(facturaCompraDao.anularFactura(myFacturaCompra)){
												
												cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
												
												//se registrar los detalles de la factura anulada como una devolucion
												if(myFacturaCompra.getDetalles()!=null)
													//se recorre los detalles de la factura
													for(int x=0;x<myFacturaCompra.getDetalles().size();x++){
														
														boolean resu=this.devolucionDao.registrar(myFacturaCompra.getDetalles().get(x), myFacturaCompra.getNoCompra());
													}
											}
											this.view.getBtnEliminar().setEnabled(false);
										}else{
									
											JOptionPane.showMessageDialog(view,"Usuario Invalido","Error de validacion",JOptionPane.ERROR_MESSAGE);
										}
								}
								
								
							}
	        		}else{
	        			JOptionPane.showMessageDialog(view,"La fatura ya esta anulada","Error de validacion",JOptionPane.ERROR_MESSAGE);
	        		}
	        }else{
    			JOptionPane.showMessageDialog(view,"Debe seleccionar una factura primero.","Error de validacion",JOptionPane.ERROR_MESSAGE);
    		}
			
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnProveedor().isSelected()){
				cargarTabla(facturaCompraDao.buscarPorProveedor(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(facturaCompraDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(view.getRdbtnProveedor().isSelected()){
				cargarTabla(facturaCompraDao.buscarPorProveedor(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				cargarTabla(facturaCompraDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				
				}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		case "INSERTAR":
			
			ViewAgregarCompras viewAgregarCompras= new ViewAgregarCompras(this.view);
			CtlCompras ctlAgregarCompra=new CtlCompras(viewAgregarCompras);
			
			
			viewAgregarCompras.dispose();
			viewAgregarCompras=null;
			ctlAgregarCompra=null;
			
			view.getModelo().setPaginacion();
			
			cargarTabla(facturaCompraDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			
			
			break;

		}

	}

	

}
