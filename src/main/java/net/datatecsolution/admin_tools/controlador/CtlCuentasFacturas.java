package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.CuentaFactura;
import net.datatecsolution.admin_tools.modelo.dao.CuentaFacturaDao;
import net.datatecsolution.admin_tools.view.ViewCobroFactura;
import net.datatecsolution.admin_tools.view.ViewCuentasFacturas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CtlCuentasFacturas implements ActionListener, MouseListener, ChangeListener, KeyListener {
	public ViewCuentasFacturas view;
	
	private CuentaFacturaDao cuentaFacturaDao;
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	
	public CtlCuentasFacturas(ViewCuentasFacturas v) {
		
		view =v;
		view.conectarControlador(this);
		cuentaFacturaDao=new CuentaFacturaDao();
		
		
		//cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));

		view.pack();
		view.getTxtBuscar().setText("");
		view.getTxtBuscar().selectAll();
		view.getRdbtnCliente().setSelected(true);
		view.getTxtBuscar().requestFocusInWindow();
		view.setVisible(true);
	}
	
	
	
	public void cargarTabla(List<CuentaFactura> cuentas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiarCuentas();
		
		if(cuentas!=null){
			for(int c=0;c<cuentas.size();c++){
				this.view.getModelo().agregarCuenta(cuentas.get(c));
				
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        //JOptionPane.showMessageDialog(view, "click en la tabla"+filaPulsada);
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	
           //this.myFactura=this.view.getModelo().getFactura(filaPulsada);
           CuentaFactura cuentaSelected=view.getModelo().getCuenta(filaPulsada);
           
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		try {
        			
        			AbstractJasperReports.createReportCuentaFactura(ConexionStatic.getPoolConexion().getConnection(),cuentaSelected.getCodigoCuenta());
					AbstractJasperReports.showViewer(view);
        		
	        	} catch (SQLException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
        		
        
        		
        		
				
	        }//fin del if del doble click

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

			case "PAGOS":

				filaPulsada = this.view.getTabla().getSelectedRow();
				//JOptionPane.showMessageDialog(view, "click en la tabla"+filaPulsada);

				//si seleccion una fila
				if(filaPulsada>=0) {

					CuentaFactura cuentaSelected=view.getModelo().getCuenta(filaPulsada);

					ViewCobroFactura viewCobroFactura = new ViewCobroFactura(view);
					CtlCobroFactura ctlCobroFactura = new CtlCobroFactura(viewCobroFactura);

					ctlCobroFactura.setDatos(cuentaSelected);

					if(ctlCobroFactura.getResultado()){
						viewCobroFactura.dispose();
						ctlCobroFactura = null;

						view.setVisible(false);

					}


				}else{
					JOptionPane.showMessageDialog(view,"Debe seleccionar una factura!!!","Error",JOptionPane.ERROR_MESSAGE);
				}
				break;


		
		
			case "ESCRIBIR":
				view.setTamanioVentana(1);
				break;

			case "BUSCAR":
				 filaPulsada = this.view.getTabla().getSelectedRow();
				view.getModelo().setPaginacion();
				//si la busqueda es por id
				if(this.view.getRdbtnId().isSelected()){

					cargarTabla(cuentaFacturaDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText())));

				}
				//si la busqueda es por fecha
				if(this.view.getRdbtnFecha().isSelected()){


					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date1 = sdf.format(this.view.getDcFecha1().getDate());
					String date2 = sdf.format(this.view.getDcFecha2().getDate());

					cargarTabla(cuentaFacturaDao.buscarConSaldoXfecha(date1,date2));

					}


				//si la busqueda son tadas
				if(this.view.getRdbtnTodos().isSelected()){

					cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				if(view.getRdbtnCliente().isSelected()){
					cargarTabla(cuentaFacturaDao.buscarConSaldoXnombreCliente(view.getTxtBuscar().getText()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
		
			
		case "IMPRIMIR":
			if(verificarSelecion()){
				/*
				
				try {
					
					
	    			AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 3, myFactura.getIdFactura());
	    			AbstractJasperReports.showViewer(this.view);
				
					myFactura=null;
				} catch (SQLException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
				*/
				
			}
			break;
			
			
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(cuentaFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				/* cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
				*/
				}
			if(view.getRdbtnCliente().isSelected()){
				/*
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(cuentaFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag(),view.getCbxCajas().getSelectedItem()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}*/
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(cuentaFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				/* cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
					*/
				}
			
			if(view.getRdbtnCliente().isSelected()){
				/*
				if(view.getTxtBuscar().getText().length()!=0)
					//cargarTabla(cuentaFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}*/
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}//fin del witch
		
		
	

	}
	
	public boolean verificarSelecion(){
		//fsdf
		boolean resul=false;
		
		if(view.getTabla().getSelectedRow()>=0){
			this.filaPulsada=view.getTabla().getSelectedRow();
			resul=true;
		}else{
			JOptionPane.showMessageDialog(view,"No seleccion una fila. Debe Selecionar una fila primero","Error",JOptionPane.ERROR_MESSAGE);
		}
		return resul;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub facturasPorId
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch(e.getKeyCode()){

			case KeyEvent.VK_F1:

				break;

			case KeyEvent.VK_F2:
				break;

			case KeyEvent.VK_F3:

				break;

			case KeyEvent.VK_F4:

				break;

			case KeyEvent.VK_F5:

				break;

			case KeyEvent.VK_F6:

				break;

			case KeyEvent.VK_F7:

				break;

			case KeyEvent.VK_F8:

				break;
			case KeyEvent.VK_F9:

				break;

			case KeyEvent.VK_F10:


				break;

			case KeyEvent.VK_F11:

				break;

			case KeyEvent.VK_F12:

				break;

			case  KeyEvent.VK_ESCAPE:
				view.setVisible(false);
				break;

			case KeyEvent.VK_DELETE:

				break;

			case KeyEvent.VK_DOWN:

			case KeyEvent.VK_UP:

				break;
			case KeyEvent.VK_LEFT:

				break;
			case KeyEvent.VK_RIGHT:

				break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub


		//si escribe en la busque un cliente
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()>=3&&e.getKeyCode()!=KeyEvent.VK_UP&&e.getKeyCode()!=KeyEvent.VK_DOWN&&e.getKeyCode()!=KeyEvent.VK_ENTER){

			//Se establece el departamento seleccionado
			//myArticuloDao.setMyBodega(view.getModeloCbxDepartamento().getDepartamento(view.getCbxDepart().getSelectedIndex()));
			view.getModelo().setPaginacion();

			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnCliente().isSelected()){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXnombreCliente(view.getTxtBuscar().getText()));
			}

			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		}

	}

}
