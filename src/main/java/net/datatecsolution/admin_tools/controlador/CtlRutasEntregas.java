package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewCrearRuta;
import net.datatecsolution.admin_tools.view.ViewListaRutasEntregas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CtlRutasEntregas implements ActionListener, MouseListener, ChangeListener {
	
	private ViewListaRutasEntregas view=null;
	private RutasEntregasDao myRutasDao=null;
	private RutaEntrega myRuta=null;
	//fila selecciona enla lista
	private int filaPulsada;
	private CajaDao cajaDao=new CajaDao();
	private DetalleFacturaDao detalleFacturaDao=new DetalleFacturaDao();
	private EmpleadoDao empleadoDao=new EmpleadoDao();
	
	private UsuarioDao myUsuarioDao=null;

	public CtlRutasEntregas(ViewListaRutasEntregas v) {
		view=v;
		myRutasDao=new RutasEntregasDao();
		myRuta=new RutaEntrega();
		myUsuarioDao=new UsuarioDao();
		
		cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		view.conectarCtl(this);
		view.setVisible(true);
	}
	
	public void cargarTabla(List<RutaEntrega> rutas){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModelo().limpiar();
		
		if(rutas!=null){
			
			for(int c=0;c<rutas.size();c++){
				this.view.getModelo().add(rutas.get(c));
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
		
		//Recoger quefila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		RutaEntrega unRuta =view.getModelo().getRuta(filaPulsada);
        		
        		ViewCrearRuta viewRutaEntrega=new ViewCrearRuta(view);
        		CtlRutaEntrega ctlRutaEntrega=new CtlRutaEntrega(viewRutaEntrega);
        		
        		
        		//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
				boolean resultado=ctlRutaEntrega.actualizarRuta(unRuta);
				
				//se proceso el resultado de modificar la marca
				if(resultado){
					this.view.getModelo().cambiarRuta(filaPulsada, ctlRutaEntrega.getMyRuta());//se cambia en la vista
					this.view.getTabla().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
				}	
        		
        		
    
        		
	        	
			
				
				
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		
        		
        		
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
			
			ViewCrearRuta viewCrearRuta =new ViewCrearRuta(view);
			CtlRutaEntrega ctlRutaEntrega=new CtlRutaEntrega(viewCrearRuta);
			boolean resuldoGuarda=ctlRutaEntrega.agregarRuta();
			
			if(resuldoGuarda){
				view.getModelo().setPaginacion();
				
				cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				
			}
			
			//ctlRutaEntrega
			/*
			ViewRequisicion viewRequi=new ViewRequisicion(view);
			CtlRequisicion ctlRequi=new CtlRequisicion(viewRequi);
			
			
			boolean resuldoGuarda=ctlRequi.agregarRequisicion();
			
			if(resuldoGuarda){
				view.getModelo().setPaginacion();
				
				cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				
			}*/
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
				myRuta=myRutasDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				if(myRuta!=null){												
					this.view.getModelo().limpiar();
					this.view.getModelo().add(myRuta);
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro la factura","Error",JOptionPane.ERROR_MESSAGE);
				}
				
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				//cargarTabla(myRutasDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				//this.view.getTxtBuscar1().setText("");
				//this.view.getTxtBuscar2().setText("");
				}
			
			
			//si la busqueda son tadas
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		
			
		case "IMPRIMIR":
			
			
			filaPulsada = this.view.getTabla().getSelectedRow();
		
			//si seleccion una fila
	        if(filaPulsada>=0){
	        	//se crea el informe de ventas por categorias
				List<DetalleFactura> reportArticuloRuta= new ArrayList<DetalleFactura>();

				reportArticuloRuta.clear();
				
				//se recoge la ruta selecionada 
				RutaEntrega unRuta =view.getModelo().getRuta(filaPulsada);
				
				unRuta.setVendedor(empleadoDao.buscarPorId(unRuta.getIdVendedor()));

				//se reinician las facturas
				unRuta.getFacturas().clear();

				//se recoge las facturas de la ruta
				myRutasDao.getFacturas(unRuta);
				
				//recoremos las facturas de la ruta
				for(int y=0;y<unRuta.getFacturas().size();y++){
					//se extrae la caja de la factura
					Caja unaCaja=this.cajaDao.buscarPorId(unRuta.getFacturas().get(y).getCodigoCaja());
					
					//se extrae los detalles de las facturas
					List<DetalleFactura> unDetalle=detalleFacturaDao.getDetallesFactura(unRuta.getFacturas().get(y).getIdFactura(), unaCaja);
					
					
					
					//int filaDetalleFactura;
					
					//recorremos las detalles de la factura
					for(int xx=0; xx<unDetalle.size();xx++){
						
						int filaDetalleEncontra=-1;
						
						//JOptionPane.showMessageDialog(view, unDetalle.get(xx).getIdFactura()+" cantidad "+ unDetalle.get(xx).getCantidad()+" "+unDetalle.get(xx).getCodigoArt()+" | Articulo: "+unDetalle.get(xx).getArt());
						
						Articulo unArt=unDetalle.get(xx).getArticulo();
						
						//se recorre los detalles del informe en busca de considencias
						for(int yy=0;yy<reportArticuloRuta.size();yy++){
							
							if(unArt.getId()==reportArticuloRuta.get(yy).getArticulo().getId()){
								filaDetalleEncontra=yy;
								break;
							}
						}
						
						//se filtra si ya esta
						if(filaDetalleEncontra==-1){//sino esta el articulo en el reporte se agrega normal
							reportArticuloRuta.add(unDetalle.get(xx));
						}else{//si existe el articulo se suma la cantidad y el total a ya existente
							BigDecimal newCantida=reportArticuloRuta.get(filaDetalleEncontra).getCantidad().add(unDetalle.get(xx).getCantidad());
							BigDecimal newTotal=reportArticuloRuta.get(filaDetalleEncontra).getTotal().add(unDetalle.get(xx).getTotal());
							reportArticuloRuta.get(filaDetalleEncontra).setCantidad(newCantida);
							reportArticuloRuta.get(filaDetalleEncontra).setTotal(newTotal);
							
						}
						
					}
					
					
					
					
				}
				
				
				try {
					
					AbstractJasperReports.createReportRuta(ConexionStatic.getPoolConexion().getConnection(),unRuta,reportArticuloRuta);
					
					//AbstractJasperReports.imprimierFactura();
					AbstractJasperReports.showViewer(view);
				} catch (SQLException eee) {
					// TODO Auto-generated catch block
					eee.printStackTrace();
				}
	        	
	        }
			
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				//cargarTabla(myRutasDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myRutasDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			if(this.view.getRdbtnFecha().isSelected()){  
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
			//	cargarTabla(myRutasDao.buscarPorFechas(date1,date2,view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}

	}

}
