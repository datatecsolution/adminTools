package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewFacturaDevolucion;
import net.datatecsolution.admin_tools.view.ViewFacturas;

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
import java.util.List;

public class CtlFacturas implements ActionListener, MouseListener, ChangeListener {
	private ViewFacturas view;
	
	private FacturaDao myFacturaDao=null;
	private Factura myFactura;
	private UsuarioDao myUsuarioDao=null;
	private DetalleFacturaDao detallesDao=null;
	private DevolucionesDao devolucionDao=null;
	private CajaDao cajaDao;
	private ReciboPagoDao myReciboDao=null;
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	
	public CtlFacturas(ViewFacturas v) {
		
		view =v;
		view.conectarControlador(this);
		cajaDao=new CajaDao();
		myFacturaDao=new FacturaDao();
		 myReciboDao=new ReciboPagoDao();
		cargarComboBox();
		cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
		myFactura=new Factura();
		myUsuarioDao=new UsuarioDao();
		detallesDao=new DetalleFacturaDao();
		devolucionDao=new DevolucionesDao();
		
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCajas().setLista(this.cajaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbxCajas().removeAllItems();
	
		this.view.getCbxCajas().setSelectedIndex(0);
	}
	
	public void cargarTabla(List<Factura> facturas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiarFacturas();
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				this.view.getModelo().agregarFactura(facturas.get(c));
				
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        //JOptionPane.showMessageDialog(view, "click en la tabla"+filaPulsada);
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	
            this.myFactura=this.view.getModelo().getFactura(filaPulsada);
           
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		Caja caja=(Caja)view.getCbxCajas().getSelectedItem();
        		caja.setActiva(true);
        		ConexionStatic.getUsuarioLogin().getCajas().add(caja);
        		//ConexionStatic.getUsuarioLogin().getca
        		//deer
        		try {
    				
        			//si la factura es al contado se imprime con un formato especifico
					if(myFactura.getTipoFactura()==1){
						//si la configuracion de la impresion de la factura es tiket o carta
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFactura().equals("tiket")){
						
							AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(),6, myFactura.getIdFactura());
							AbstractJasperReports.showViewer(view);
							//AbstractJasperReports.imprimierFactura();
							//AbstractJasperReports.imprimierFactura();
						}
						
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFactura().equals("carta")){
								AbstractJasperReports.createReportFacturaCarta(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"COPIA");
								AbstractJasperReports.showViewer(view);
								//AbstractJasperReports.imprimierFactura();
								
						}
					}//fin de la impresion de la factura carta al contado
					
					if(myFactura.getTipoFactura()==2){
						//si la configuracion de la impresion de la factura es tiket o carta
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFacturaCredito().equals("tiket")){
						
							AbstractJasperReports.createReportFacturaTiketCredito(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
							AbstractJasperReports.showViewer(view);
							//AbstractJasperReports.imprimierFactura();
							//AbstractJasperReports.imprimierFactura();
						}
						
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFacturaCredito().equals("carta")){
								AbstractJasperReports.createReportFacturaCartaCredito(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"COPIA");
								AbstractJasperReports.showViewer(view);
								//AbstractJasperReports.imprimierFactura();
								
						}
					}
        			
        			
        			
    				//AbstractJasperReports.imprimierFactura();
    				this.view.getBtnImprimir().setEnabled(false);
    				myFactura=null;
    				ConexionStatic.getUsuarioLogin().getCajas().clear();
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
		
		//se seleciona la caja donde se esta trajando y se asigna por defecto al usuario del login
		//esto para realizar consultas en los diferentes base de datos
		Caja caja=(Caja)view.getCbxCajas().getSelectedItem();
		caja.setActiva(true);
		ConexionStatic.getUsuarioLogin().getCajas().add(caja);
		
		
		String comando=e.getActionCommand();
		
		switch (comando){
		
		case "CAMBIOCOMBOBOX":
			view.getModelo().setPaginacion();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
				
		
				}
			
			if(view.getRdbtnCliente().isSelected()){
				
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(myFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag(),view.getCbxCajas().getSelectedItem()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		break;
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;

		case "BUSCAR":
			view.getModelo().setPaginacion();
			//si la busqueda es por id
			if(this.view.getRdbtnId().isSelected()){
				//JOptionPane.showMessageDialog(view, "No se encuentro la factura");
				myFactura=myFacturaDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()),view.getCbxCajas().getSelectedItem());
				if(myFactura!=null){												
					this.view.getModelo().limpiarFacturas();
					this.view.getModelo().agregarFactura(myFactura); 
				}else{
					JOptionPane.showMessageDialog(view, "No se encuentro la factura");
				}
				
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
		
				}
			
			
			//si la busqueda son tadas
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
				}
			if(view.getRdbtnCliente().isSelected()){
				
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(myFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),
																	view.getModelo().getLimiteSuperior(),
																	view.getModelo().getCanItemPag(),
																	view.getCbxCajas().getSelectedItem()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "ANULARFACTURA":
			
			//se verifica que se haya selecciona una fila
			if(verificarSelecion()){
				this.myFactura=this.view.getModelo().getFactura(this.view.getTabla().getSelectedRow());
				int resul=JOptionPane.showConfirmDialog(view, "Desea anular la factura no "+myFactura.getIdFactura()+"?");

				//sin confirmo la anulacion
				if(resul==0){

					JPasswordField pf = new JPasswordField();
					int action = JOptionPane.showConfirmDialog(view, pf,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);

					if(action == 0){

						String pwd=new String(pf.getPassword());

						//comprabacion del permiso administrador
						if(this.myUsuarioDao.comprobarAdmin(pwd)){

							//se extraen los detalles de la factura de la base de datos
							myFactura.setDetalles(detallesDao.getDetallesFactura(myFactura.getIdFactura()));

							//se anula la factura en la bd
							if(myFacturaDao.anularFactura(myFactura)){

								//si tiene detalles la factura se realizan operaciones con los detalles
								if(!myFactura.getDetalles().isEmpty() && myFactura.getDetalles()!=null)
								{
									//si la factura que se anulo es al credito se hace un pago a farvor del cliente para rebajar el saldo y lo mismo con la factura
									if(myFactura.getTipoFactura()==2){
										createCreditoToCliente(caja);
									}

									//bandera para verificar si se realizo una devolucion
									boolean resultado=false;

									//se registrar los detalles de la factura anulada como una devolucion
									//se recorre los detalles de la factura
									for(int x=0;x<myFactura.getDetalles().size();x++){
										//se verifica la existencia de una devolucion previa
										List<DetalleFactura> unDetalle=devolucionDao.getDevolucionArticulo( myFactura.getIdFactura(), myFactura.getDetalles().get(x).getArticulo().getId());

										if(unDetalle==null){//sino hay una devolucion previa se devuelve toda la cantidad
											//se registra la devolucion
											this.devolucionDao.agregarDetalle(myFactura.getDetalles().get(x), myFactura.getIdFactura());
											resultado=true;

										}else{//si existe una devolucion previa se resta la devolucion previa y se registra solo el restante

											//conseguir la cantidad que se devolvio en los diferentes item
											BigDecimal can=new BigDecimal(0.0);
											for(int c=0;c<unDetalle.size();c++)
											{
												can=can.add(unDetalle.get(c).getCantidad());
											}

											//conseguir la cantidad total en la factura
											BigDecimal canFactura=new BigDecimal(0.0);
											for(int cc=0;cc<myFactura.getDetalles().size();cc++){
												if(myFactura.getDetalles().get(cc).getArticulo().getId()==myFactura.getDetalles().get(x).getArticulo().getId()){
													canFactura=canFactura.add(myFactura.getDetalles().get(cc).getCantidad());
												}
											}

											//se verifica que al resta la devolucion existente sea mayor que cero
											if(canFactura.subtract(can).floatValue()>0){
												//se cambia la cantidad en el modelo
												myFactura.getDetalles().get(x).setCantidad(new BigDecimal(canFactura.subtract(can).floatValue()));


												//get cantidad de la row para calcular el total
												BigDecimal cantidad=myFactura.getDetalles().get(x).getCantidad();
												//get precio venta de la row para calcular el total
												BigDecimal precioVenta= new BigDecimal(myFactura.getDetalles().get(x).getArticulo().getPrecioVenta());

												//se calcula el total del item
												BigDecimal totalItem=cantidad.multiply(precioVenta);

												//se establece el nuevo total
												myFactura.getDetalles().get(x).setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

												//se manda a guardar la devolucion
												devolucionDao.agregarDetalle(myFactura.getDetalles().get(x), myFactura.getIdFactura());
												resultado=true;

											}
										}

									}

									//se verifica que se realizo una devolucion para poder imprimir el reporte
									if(resultado){
										//se imprime el reporte
										try {
											AbstractJasperReports.createReportDevolucionVenta(ConexionStatic.getPoolConexion().getConnection(),myFactura.getIdFactura());
											AbstractJasperReports.showViewer(view);
										} catch (SQLException ee) {
												ee.printStackTrace();
											}
										}

								}//fin de la comprovacion de la existencia de detalles de la factura

								//se cargan la facturas en la view
								cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));

							}//fin de la verificacion de la  anulacion del encabezado de la facrura


						}else{
							JOptionPane.showMessageDialog(view, "Usuario Invalido","Error de validacion!!",JOptionPane.ERROR_MESSAGE);
						}
					}

				}
					
			}
			break;
			
		case "IMPRIMIR":
			if(verificarSelecion()){
				
				
				try {
					
					
	    			AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 3, myFactura.getIdFactura());
	    			AbstractJasperReports.showViewer(this.view);
				
					myFactura=null;
				} catch (SQLException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
				
			}
			break;
			
		case "DEVOLUCION":
			if(verificarSelecion()){

				//Recoger qu� fila se ha pulsadao en la tabla
				filaPulsada = this.view.getTabla().getSelectedRow();
				myFactura=this.view.getModelo().getFactura(filaPulsada);
				myFactura.setDetalles(detallesDao.getDetallesFactura(myFactura.getIdFactura()));

				ViewFacturaDevolucion viewDevolucion=new ViewFacturaDevolucion(view);
				CtlDevoluciones ctlDevolucion=new CtlDevoluciones(viewDevolucion);
				ctlDevolucion.actualizarFactura(myFactura);
				viewDevolucion.dispose();
				viewDevolucion=null;
				ctlDevolucion=null;
				
				
			}
			
			break;
			
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
		
				}
			if(view.getRdbtnCliente().isSelected()){
				
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(myFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag(),view.getCbxCajas().getSelectedItem()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myFacturaDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
			}
			//si la busqueda es por fecha
			if(this.view.getRdbtnFecha().isSelected()){ 
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				
				
				//JOptionPane.showMessageDialog(view, date1+" al  "+date2);
				cargarTabla(myFacturaDao.buscarPorFecha(date1,
														date2,
														view.getModelo().getLimiteSuperior(),
														view.getModelo().getCanItemPag(),
														view.getCbxCajas().getSelectedItem()));
		
				}
			
			if(view.getRdbtnCliente().isSelected()){
				
				if(view.getTxtBuscar().getText().length()!=0)
					cargarTabla(myFacturaDao.buscarPorNombreCliente(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag(),view.getCbxCajas().getSelectedItem()));
				else{
					JOptionPane.showMessageDialog(view, "Debe escribir algo en la busqueda","Error en busqueda",JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().requestFocusInWindow();
				}
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		}//fin del witch
		
		
		//se limpia la caja para esperar una nueva seleccion
		ConexionStatic.getUsuarioLogin().getCajas().clear();

	}

	private void createCreditoToCliente(Caja caja) {


		/**** se establece la cuenta de la factura a cero por anulacion *****/

		//se busca la cuenta de la factura
		CuentaFactura cuentaFactura=new CuentaFactura();
		CuentaFacturaDao cuentaFacturaDao=new CuentaFacturaDao();
		CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();

		//se busca la factura en la base de datos
		cuentaFactura=cuentaFacturaDao.buscarPorId(myFactura.getCliente().getId(),caja.getCodigo(),myFactura.getIdFactura());

		if(cuentaFactura!=null){
			CuentaXCobrarFactura cuenta=new CuentaXCobrarFactura();

			cuenta.setCodigoCuenta(cuentaFactura.getCodigoCuenta());
			cuenta.setDebito(new BigDecimal(cuentaFactura.getSaldo().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
			cuenta.setSaldo(new BigDecimal(0));
			cuenta.setDescripcion("Anulacion de factura ");

			cuentaXCobrarFacturaDao.reguistrarDebito(cuenta);
		}


		/**** para la cuenta general del cliente se estable un credito
		 *  para rebajar la factura anulada ****/

		//se crea un recibo con los datos del cliente y de la factura
		ReciboPago myRecibo=new ReciboPago();
		myRecibo.setCliente(myFactura.getCliente());
		String concepto="Anulacion de factura # "+myFactura.getIdFactura();
		myRecibo.setTotal(myFactura.getTotal());
		myRecibo.setConcepto(concepto);
		myRecibo.setTotalLetras(NumberToLetterConverter.convertNumberToLetter(myRecibo.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));


		//se manda aguardar el recibo con los pagos realizados
		boolean resulta=this.myReciboDao.registrar(myRecibo);

		if(resulta){

			myRecibo.setNoRecibo(myReciboDao.idUltimoRecibo);
			try {
				AbstractJasperReports.createReportReciboCobroCaja(ConexionStatic.getPoolConexion().getConnection(), myRecibo.getNoRecibo());
				AbstractJasperReports.imprimierFactura();
				AbstractJasperReports.showViewer(view);
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}

		}else{//
			JOptionPane.showMessageDialog(view, "El recibo no se guardo correctamente.");
		}//fin del if que verefica la acccion de guardar el recibo

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

}
