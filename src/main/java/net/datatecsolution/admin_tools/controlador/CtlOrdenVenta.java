package net.datatecsolution.admin_tools.controlador;


import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;


public class CtlOrdenVenta  implements ActionListener, MouseListener, TableModelListener, WindowListener, KeyListener  {
	
	private ViewOrdeneVenta view;
	private Factura myFactura=null;
	private FacturaDao facturaDao=null;//=new FacturaDao();

	private ClienteDao clienteDao=null;
	private Articulo myArticulo=null;
	private ArticuloDao myArticuloDao=null;
	private PrecioArticuloDao preciosDao=null;
	private CodBarraDao codBarraDao=null;
	private Cliente myCliente=null;
	private UsuarioDao myUsuarioDao;
	private InsumoDao insumoDao=null;
	private Caja cajaDefecto;

	private int filaPulsada=0;
	private boolean resultado=false;
	
	private int tipoView=1;
	private int netBuscar=0;
	
	private FacturaOrdenVentaDao facturaOrdenesDao;
	private DetalleFacturaOrdenDao detallesOrdenDao=null;
	private boolean unirCanItem=true;
	
	public CtlOrdenVenta(ViewOrdeneVenta v){
	
		
		view=v;
		view.conectarContralador(this);
		
		//se inicializan atributos de la factura
		myFactura=new Factura();
		myArticuloDao=new ArticuloDao();
		clienteDao=new ClienteDao();
		facturaDao=new FacturaDao();
		preciosDao=new PrecioArticuloDao();
		codBarraDao=new CodBarraDao();
		this.setEmptyView();
		
		facturaOrdenesDao=new FacturaOrdenVentaDao();
		detallesOrdenDao=new DetalleFacturaOrdenDao();
		myUsuarioDao=new UsuarioDao();
		insumoDao=new InsumoDao();
		cajaDefecto=new Caja(ConexionStatic.getUsuarioLogin().getCajaActiva());
		
		cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
		this.tipoView=1;

		view.getBtnCobrar().setVisible(false);
		view.getBtnCierreCaja().setVisible(false);

	
		
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		//se filtra el comando para verificar que la accion se genero desde los botones de las facturas pendientes.
		if(AbstractJasperReports.isNumber(comando)){
			int numeroFactura=Integer.parseInt(comando);
			//si es un factura guardada
			if(numeroFactura>0){
				cargarFacturaPendiente(numeroFactura);
			}
			//si es una nueva factura
			if(numeroFactura==0){
				this.tipoView=1;
				this.view.getBtnGuardar().setEnabled(true);
				this.view.getBtnActualizar().setEnabled(false);
				
				//view.getBtnsGuardador().setFactura(myFactura);
				
				setEmptyView();
				
				view.getBtnsGuardador().deleteAll();
				
				cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
			}
		}
		//JOptionPane.showMessageDialog(view, "paso de celdas"+comando);
		switch(comando){
		
		case "BUSCARARTICULO2":
			//se comprueba que se ingreso un codigo de barra o que el articulo este nulo para poder buscar
			if(view.getTxtBuscar().getText().trim().length()!=0 || myArticulo==null){


				String busca=this.view.getTxtBuscar().getText();

				this.myArticulo=this.myArticuloDao.buscarArticuloBarraCod(busca);

				if(myArticulo!=null){

					//activar para redondiar el precio de venta final
					if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
						myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
					}

					//conseguir los precios del producto
					myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(myArticulo.getId()));

					//facturar sin tomar en cuenta el inventario
					if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario()){
						//activar para redondiar el precio de venta final
						if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
							myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
						}

						//si es un articulo tipo bien se puede agregar en cualquier caja
						if(myArticulo.getTipoArticulo()==1){
							//se agrega articulo a la tabla
							this.view.getModeloTabla().setArticulo(myArticulo);
							calcularTotales();
							this.view.getModeloTabla().agregarDetalle();
							selectRowInset();
						}else{

							//se filtra para que la caja por defecto sera la unica que puede facturar servicios
							if(ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo()==this.cajaDefecto.getCodigo()){
								//se agrega articulo a la tabla
								this.view.getModeloTabla().setArticulo(myArticulo);
								calcularTotales();
								this.view.getModeloTabla().agregarDetalle();
								selectRowInset();
							}else{
								JOptionPane.showMessageDialog(view,"Solo la caja "+cajaDefecto.getDescripcion() +" puede facturar servicios.","Error en articulo",JOptionPane.ERROR_MESSAGE);
								view.getTxtBuscar().setText("");
							}
						}


					} else{  //si esta activado la facturacion con inventario

						//se comprueba que exista el producto en el inventario
						double existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

						//es necesario comprobar los inventarios de articulos
						//si es un bien se combrueba la existencia de ese articulo
						if(myArticulo.getTipoArticulo()==1){

							//se comprueba que exista el producto en el inventario
							existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

							double cantidad=1;

							double buscarEnRequisicionCantidad=view.getModeloTabla().buscarCantidadPorArticulo(myArticulo);

							if(buscarEnRequisicionCantidad>0){
								cantidad=cantidad+buscarEnRequisicionCantidad;
							}

							if(existencia>0.0 && cantidad<=existencia){

								//activar para redondiar el precio de venta final
								if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
									myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
								}

								//se agrega articulo a la tabla
								this.view.getModeloTabla().setArticulo(myArticulo);
								calcularTotales();
								this.view.getModeloTabla().agregarDetalle();
								selectRowInset();

							}else{//fin se la comprobacion de la existencia
								JOptionPane.showMessageDialog(view, myArticulo.getArticulo()+" no tiene existencia en "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
								view.getTxtBuscar().setText("");
							}
						}else{//si es un servicio que se necesita comprobar la existencia se busca sus insumos

							//se filtra para que la caja por defecto sera la unica que puede facturar servicios
							if(ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo()==this.cajaDefecto.getCodigo()){

								//comprobar la existencia de los insumos
								List<Insumo> insumos=this.insumoDao.buscarPorId(myArticulo.getId());

								//comprobamos de el articulo tenga insumos para hacer la busca de sus existencias
								if(insumos!=null && insumos.size()>0){
									boolean exist=false;
									//recoremos los insumos comprobando si tiene existencia
									for(int xx=0;xx<insumos.size();xx++){

										//se comprueba que exista el insumo en el inventario
										existencia=myArticuloDao.getExistencia(insumos.get(xx).getArticulo().getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

										if(existencia>0.0 && existencia>=insumos.get(xx).getCantidad().doubleValue()){
											//se establece que esiste
											exist=true;
											//activar para redondiar el precio de venta final
											if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
												myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
											}
										}else{//fin se la comprobacion de la existencia
											exist=false;
											JOptionPane.showMessageDialog(view,"El insumo "+ insumos.get(xx).getArticulo().getArticulo()+" que pertence al servicio "+myArticulo.getArticulo()+" ,\nno tiene existencia en "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
											view.getTxtBuscar().setText("");
											break;
										}
									}//sin del for que recore los insumos

									//se comprueba que todos los insumos tienen exitencia para agregar el servicio a la factura
									if(exist){
										//se agrega articulo a la tabla
										this.view.getModeloTabla().setArticulo(myArticulo);

										calcularTotales();
										this.view.getModeloTabla().agregarDetalle();

										selectRowInset();
									}

								}else{//sino tiene insumos el servicio se agrega directamente a la factura
									//se agrega articulo a la tabla
									this.view.getModeloTabla().setArticulo(myArticulo);
									calcularTotales();
									this.view.getModeloTabla().agregarDetalle();
									selectRowInset();
								}
							}else{
								JOptionPane.showMessageDialog(view,"Solo la caja "+cajaDefecto.getDescripcion() +" puede facturar servicios.","Error en articulo",JOptionPane.ERROR_MESSAGE);
								view.getTxtBuscar().setText("");
							}
						}//sin del else para comprobar las existencia de los servicios

					}
					view.getTxtBuscar().setText("");

				} else{//sino se encuentra ningun articulo se avisa al usuario
					JOptionPane.showMessageDialog(view, "No se encontro el articulo");
					view.getTxtBuscar().setText("");
					view.getTxtBuscar().requestFocusInWindow();
					myArticulo=null;
				}

				//}
			}
				netBuscar=0;
			break;
		case "BUSCARCLIENTE":

			myCliente=null;
			myCliente=clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtIdcliente().getText()));

			if(myCliente!=null){
				this.view.getTxtNombrecliente().setText(myCliente.getNombre());
				this.view.getTxtRtn().setText(myCliente.getRtn());
			}else{

				JOptionPane.showMessageDialog(view, "Cliente no encontrado");
				this.view.getTxtIdcliente().setText("1");
				this.view.getTxtNombrecliente().setText("Cliente Normal");
			}
			//96995768
			break;
		case "ACTUALIZAR":
			this.actualizar();
			break;
		case "BUSCARARTICULO":
			//se verfica si esta activo la busqueda de articulo por descripcion
			if(ConexionStatic.getUsuarioLogin().getConfig().isActivarBusquedaFacturacion())
			{
				this.buscarArticulo();
			}
		break;
		
		case "CERRAR":
			this.salir();
		break;
		case "BUSCARCLIENTES":
			this.buscarCliente();
			break;
		case "COBRAR":
			//this.cobrar();
			break;
		case "GUARDAR":
			this.guardar();
			break;
			
		case "CIERRECAJA":
			//this.cierreCaja();
			break;
		case "COTIZACION":
			//this.showPendientes();
			//JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			guardarCotizacion();
			break;
		case "GET_COTIZACIONES":
			showPendientes();
			break;
		case "ELIMINARPENDIENTE":
			int idFacturaTemporal2=view.getBtnsGuardador().getFacturaSeleted().getIdFactura();

			Factura eliminarTem=new Factura();
			eliminarTem.setIdFactura(idFacturaTemporal2);


			JPasswordField pf = new JPasswordField();
			int action =JOptionPane.showConfirmDialog(view,"Desea eliminar la orden de "+view.getBtnsGuardador().getFacturaSeleted().getCliente().getNombre()+" ?","Confirmacion",JOptionPane.OK_CANCEL_OPTION);
			//JOptionPane.showMessageDialog(view,"La accion es: "+action);
			if(action == 0){



				this.facturaOrdenesDao.eliminar(eliminarTem);

				this.tipoView=1;
				this.view.getBtnGuardar().setEnabled(true);
				this.view.getBtnActualizar().setEnabled(false);

				//view.getBtnsGuardador().setFactura(myFactura);

				//setEmptyView();
				setEmptyView();

				view.getBtnsGuardador().deleteAll();

				cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());

			}

			break;
		
		}
		
	}



	private void guardarCotizacion() {
		// TODO Auto-generated method stub
		//s
		setFactura();
		if(validar()){
				CotizacionDao cotizacioDao=new CotizacionDao();
				//sf
				boolean resultado=cotizacioDao.registrar(myFactura);
				
				if(resultado){
					
					try {
						/*this.view.setVisible(false);
						this.view.dispose();*/
						//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Saint_Paul.jasper",myFactura.getIdFactura() );
						AbstractJasperReports.crearReporteCotizacion(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
						AbstractJasperReports.showViewer(view);
						//AbstractJasperReports.imprimierFactura();
						
						
						//myFactura=null;
						setEmptyView();
						
						//si la view es de actualizacion al cobrar se cierra la view
						if(this.tipoView==2){//dfsfda
							//myFactura=null;
							this.tipoView=1;
							this.view.getBtnGuardar().setEnabled(true);
							this.view.getBtnActualizar().setEnabled(false);
							
						//	this.facturaDao.EliminarTemp(idFacturaTemporal);
							setEmptyView();
							
							view.getBtnsGuardador().deleteAll();
							
							cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
							//view.setVisible(false);
							
							
						}
						//myFactura.
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//JOptionPane.showMessageDialog(view, "Se guardao la cotizacion con exito", "Cotizacion", JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(view, "Error al guardar la cotizacion", "Error al guardar", JOptionPane.ERROR_MESSAGE);
				}
		}
		
	}

	private void cargarFacturaPendiente(int numeroFactura) {
		// TODO Auto-generated method stub
		Factura fact=view.getBtnsGuardador().buscarFactura(numeroFactura);
		this.myFactura=fact;
		
		myFactura.setDetalles(detallesOrdenDao.detallesFacturaPendiente(numeroFactura));
		
		cargarFacturaView();
		this.view.getBtnGuardar().setEnabled(false);
		this.view.getBtnActualizar().setEnabled(true);
		this.view.getModeloTabla().agregarDetalle();
		tipoView=2;
	}

	private void setFactura(){
		
		//sino se ingreso un cliente en particular que coge el cliente por defecto
		if(myCliente==null){
			myCliente=new Cliente();
			myCliente.setId(Integer.parseInt(this.view.getTxtIdcliente().getText()));
			myCliente.setNombre(this.view.getTxtNombrecliente().getText());
			myCliente.setRtn(view.getTxtRtn().getText());
			
		}
		
		if(this.view.getRdbtnContado().isSelected()){
			myFactura.setTipoFactura(1);
			myFactura.setEstadoPago(1);
		}
		
		if(this.view.getRdbtnCredito().isSelected()){
			myFactura.setTipoFactura(2);
			myFactura.setEstadoPago(0);
		}
		
		myFactura.setCliente(myCliente);
		myFactura.setDetalles(this.view.getModeloTabla().getDetalles());
		myFactura.setFecha(facturaDao.getFechaSistema());
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		checkForTriggerEvent( e ); // comprueba el desencadenador
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		checkForTriggerEvent( e ); // comprueba el desencadenador
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void check(MouseEvent e)
	{ 
		if (e.isPopupTrigger()) { //if the event shows the menu 
			//this.view.getListCodigos().setSelectedIndex(this.view.getListCodigos().locationToIndex(e.getPoint())); //select the item 
			//menuContextual.show(listCodigos, e.getX(), e.getY()); //and show the menu 
		} 
		
		
	}
	// determina si el evento debe desencadenar el men� contextual
	private void checkForTriggerEvent( MouseEvent evento )
	{
		if ( evento.isPopupTrigger() ){
			
			//se recoge el boton que produjo en evento
			JToggleButton even= (JToggleButton) evento.getComponent();
			even.setSelected(true);//se selecciona
			
			// se consigue el codigo de factura del boton seleccionado
			int idFacturaTemporal=view.getBtnsGuardador().getFacturaSeleted().getIdFactura();
			//se carga la factura en la view.
			this.cargarFacturaPendiente(idFacturaTemporal);
			this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY() );
			
			
		}
	} // fin del metodo checkForTriggerEvent

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
		int colum=e.getColumn();
		int row=e.getFirstRow();
		//JOptionPane.showMessageDialog(view, myArticulo);
		//JOptionPane.showMessageDialog(view, "paso de celdas");
		switch(e.getType()){
		
			
		
			case TableModelEvent.UPDATE:
				
				//Se recoge el id de la fila marcada
		        int identificador=0; 
				
				//se ingreso un id o codigo de barra en la tabla
				if(colum==0){
					
					identificador=(int)this.view.getModeloTabla().getValueAt(row, 0);
			        myArticulo=this.view.getModeloTabla().getDetalle(row).getArticulo();
			        myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
					
			        //se ingreso un codigo de barra y si el articulo en la bd 
			        if(myArticulo.getId()==-2){
						String cod=this.view.getModeloTabla().getDetalle(row).getArticulo().getCodBarra().get(0).getCodigoBarra();
						this.myArticulo=this.myArticuloDao.buscarArticuloBarraCod(cod);
						
					}else{//sino se ingreso un codigo de barra se busca por id de articulo
						this.myArticulo=this.myArticuloDao.buscarArticulo(identificador);
						myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
					}
					
					//si se encuentra  el articulo por codigo de barra o por id se calcula los totales y se agrega 
					if(myArticulo!=null){
						
						//se estable en articulo en la tabla
						this.view.getModeloTabla().setArticulo(myArticulo, row);
						//se calcula los totales
						calcularTotales();
						
						
						boolean toggle = false;
					    boolean extend = false;
					    this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
					    this.view.getTableDetalle().changeSelection(row, colum, toggle, extend);
					    this.view.getTableDetalle().addColumnSelectionInterval(3, 3);
					    
					    
					    
						//se agrega otra fila en la tabla
						//this.view.getModeloTabla().agregarDetalle();
						
					}else{//si no se encuentra
						
						JOptionPane.showMessageDialog(view, "No se encuentra el articulo");
						//sino se encuentra se estable un id de -1 para que sea eliminado el articulo en la tabla
						this.view.getModeloTabla().getDetalle(row).getArticulo().setId(-1);
						
						//se agrega la nueva fila de la tabla
						this.view.getModeloTabla().agregarDetalle();
						
						// se vuelve a calcular los totales
						calcularTotales();
					}
					
					
					
					
					
				}
				//se cambia el precio en la tabla
				if(colum==1){
					calcularTotales();
					view.getTxtBuscar().requestFocusInWindow();
				}
				
				//se cambia la cantidad en la tabla
				if(colum==2){
					
					calcularTotales();
					view.getTxtBuscar().requestFocusInWindow();
				}
				
				//se agrego un descuento a la tabla
				if(colum==5){
					calcularTotales();
					view.getTxtBuscar().requestFocusInWindow();
					//JOptionPane.showMessageDialog(view, "Modifico el Descuento "+this.view.getModeloTabla().getDetalle(row).getDescuentoItem().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				}
				
				//view.getTxtBuscar().requestFocusInWindow();
			break;
			
		}
		
	}
	
	public boolean esValido(Character caracter)
    {
        char c = caracter.charValue();
        return Character.isLetter(c) //si es letra
                || c == ' ' //o un espacio
                || c == 8 //o backspace
                || (Character.isDigit(c));
    }
	
	
	public void calcularTotales(){
		
		//se establecen los totales en cero
		this.myFactura.resetTotales();
		
		for(int x=0; x<this.view.getModeloTabla().getDetalles().size();x++){
			
			DetalleFactura detalle=this.view.getModeloTabla().getDetalle(x);
			
			
			if(detalle.getArticulo().getId()!=-1)
				if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){
					
					
					
					//se obtien la cantidad y el precio de compra por unidad
					BigDecimal cantidad=detalle.getCantidad();
					BigDecimal precioVenta= new BigDecimal(detalle.getArticulo().getPrecioVenta());
					
					//se calcula el total del item
					BigDecimal totalItem=cantidad.multiply(precioVenta);
					
					BigDecimal des =detalle.getDescuentoItem();
					
					//double desc=detalle.getDescuento()/100;
					
					//BigDecimal des=totalItem.multiply(new BigDecimal(desc));
					
					//detalle.setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));
					
					totalItem=totalItem.subtract(des.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					//int desc=detalle.getDescuento();
				
					
					
					//se obtiene el impuesto del articulo 
					BigDecimal porcentaImpuesto =new BigDecimal(detalle.getArticulo().getImpuestoObj().getPorcentaje());
					BigDecimal porImpuesto=new BigDecimal(0);
					porImpuesto=porcentaImpuesto.divide(new BigDecimal(100));
					porImpuesto=porImpuesto.add(new BigDecimal(1));
							//new BigDecimal(((Double.parseDouble(detalle.getArticulo().getImpuestoObj().getPorcentaje())  )/100)+1);
					
					
					
					//se calcula el total sin  el impuesto;
					BigDecimal totalsiniva= new BigDecimal("0.0");
					totalsiniva=totalItem.divide(porImpuesto,2,BigDecimal.ROUND_HALF_EVEN);//.divide(porImpuesto);// (totalItem)/(porcentaImpuesto);
				
					
					//se calcula el total de impuesto del item
					BigDecimal impuestoItem=totalItem.subtract(totalsiniva);//-totalsiniva;
					
					
					
					//se estable el total y impuesto en el modelo
					myFactura.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					if(porcentaImpuesto.intValue()==0){
						myFactura.setSubTotalExcento(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					}else
						if(porcentaImpuesto.intValue()==15){
							myFactura.setTotalImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
							myFactura.setSubTotal15(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						}else
							if(porcentaImpuesto.intValue()==18){
								myFactura.setTotalImpuesto18(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								myFactura.setSubTotal18(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
							}
					
					//se calcuala el total del impuesto de los articulo que son servicios de turismo
					if(detalle.getArticulo().getTipoArticulo()==3){
						BigDecimal totalOtrosImp= new BigDecimal("0.0");
						
						totalOtrosImp=totalsiniva.multiply(new BigDecimal(0.04));
						
						myFactura.setTotalOtrosImpuesto(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						myFactura.setTotal(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
					}
					
					myFactura.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					//myFactura.getDetalles().add(detalle);
					myFactura.setTotalDescuento(detalle.getDescuentoItem().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					detalle.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					detalle.setImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					//myFactura.getDetalles()
					
					//se establece en la y el impuesto en el item de la vista
					//detalle.setImpuesto(impuesto2.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					detalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					//se establece el total e impuesto en el vista
					this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					this.view.getTxtImpuesto().setText(""+myFactura.getTotalImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					this.view.getTxtImpuesto18().setText(""+myFactura.getTotalImpuesto18().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					this.view.getTxtDescuento().setText(""+myFactura.getTotalDescuento().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					view.getModeloTabla().fireTableDataChanged();
					this.selectRowInset();
					
					view.getTxtBuscar().requestFocusInWindow();
				
					
					//this.view.getModelo().fireTableDataChanged();
				}//fin del if
			
		}//fin del for
		}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		

		
		//Recoger que fila se ha pulsadao en la tabla
		filaPulsada = this.view.getTableDetalle().getSelectedRow();
		
			switch(e.getKeyCode()){
					
					case KeyEvent.VK_F1:
						//se verfica si esta activo la busqueda de articulo por descripcion
						if(ConexionStatic.getUsuarioLogin().getConfig().isActivarBusquedaFacturacion())
						{
							buscarArticulo();
						}
						break;
						
					case KeyEvent.VK_F2:
						//cobrar();
						break;
						
					case KeyEvent.VK_F3:
							buscarCliente();
						break;
						
					case KeyEvent.VK_F4:
						this.guardar();
						break;
						
					case KeyEvent.VK_F5:
						guardarCotizacion();
						break;
						
					case KeyEvent.VK_F6:
						//cierreCaja();
						break;
						
					case KeyEvent.VK_F7:

						double maxDescuento=5;
						//configuracion del panel descuento
						JPanel panelDescuento=new JPanel();
						panelDescuento.setLayout(new BoxLayout(panelDescuento, BoxLayout.Y_AXIS));

						JLabel etiqueta =new JLabel("Escriba el descuento");
						panelDescuento.add(etiqueta);

						JTextField descuento=new JTextField(15);
						panelDescuento.add(descuento);
						//dfs
						JCheckBox   rememberChk = new JCheckBox("Agregar descuento a todos los items?");
						panelDescuento.add(rememberChk);

						descuento.requestFocusInWindow();
						//sdfsdf



						//si es necesario el password para el descuento
						if(ConexionStatic.getUsuarioLogin().getConfig().isPwdDescuento()){
							String pwd=getPassword();
							if(pwd!=null)
							{
								//comprabacion del permiso administrativo
								if(myUsuarioDao.comprobarAdmin(pwd)){


									//si el descuento es un porcentaje
									if(ConexionStatic.getUsuarioLogin().getConfig().isDescPorcentaje()){


										if(filaPulsada>=0){

											etiqueta.setText("Escriba el porcentaje(%) de descuento 1-5%");

											//se muestra la pantalla para solitar el descuento
											JOptionPane pane = new JOptionPane(panelDescuento, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
												@Override
												public void selectInitialValue() {
													descuento.requestFocusInWindow();
												}
											};
											pane.createDialog(view, "Descuento").setVisible(true);

											String seleccionadoDescuento=descuento.getText();

											boolean aplicarTodo = rememberChk.isSelected();


											if(AbstractJasperReports.isNumberReal(seleccionadoDescuento)){
												double bdDescuento=Double.parseDouble(seleccionadoDescuento);

												if(bdDescuento<=maxDescuento){

													if(aplicarTodo==false){
														BigDecimal cantidad=this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad();
														BigDecimal precioVenta= new BigDecimal(view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getPrecioVenta());
														//se calcula el total del item
														BigDecimal totalItem=cantidad.multiply(precioVenta);


														double desc=bdDescuento/100;

														BigDecimal des=totalItem.multiply(new BigDecimal(desc));

														this.view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));
													}else{

														//se recorren los item de la factura aplicando el descuento
														for(int xx=0;xx<view.getModeloTabla().getDetalles().size();xx++){
															DetalleFactura detalle=this.view.getModeloTabla().getDetalle(xx);

															//dfsdf
															if(detalle.getArticulo().getId()!=-1)
																if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){


																	BigDecimal cantidad=detalle.getCantidad();
																	BigDecimal precioVenta= new BigDecimal(detalle.getArticulo().getPrecioVenta());
																	//se calcula el total del item
																	BigDecimal totalItem=cantidad.multiply(precioVenta);


																	double desc=bdDescuento/100;

																	BigDecimal des=totalItem.multiply(new BigDecimal(desc));

																	detalle.setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));


																}
														}
													}//fin del descuento por item o todo
													//this.view.getModeloTabla().getDetalle(filaPulsada).setDescuento(bdDescuento);//.getArticulo().setPrecioVenta(new Double(entrada));
													this.calcularTotales();
												}else{
													JOptionPane.showMessageDialog(view, "No puede otorgar un descuento mayo del 5%", "Error", JOptionPane.ERROR_MESSAGE);
												}
											}else{
												JOptionPane.showMessageDialog(view, "El descuento debe ser un numero", "Error", JOptionPane.ERROR_MESSAGE);
											}
										}

									}else{//sino es un porcentaje

										if(filaPulsada>=0){
											//String entrada=JOptionPane.showInputDialog("Escriba el descuento");

											etiqueta.setText("Escriba el descuento");
											JOptionPane.showMessageDialog ( view,  panelDescuento,  "Descuento",JOptionPane.INFORMATION_MESSAGE);
											//String seleccionadoDescuento=JOptionPane.showInputDialog(view,"Escriba el porcentaje(%) de descuento 1-55%",JOptionPane.QUESTION_MESSAGE);
											String entrada=descuento.getText();
											boolean aplicarTodo = rememberChk.isSelected();

											if(AbstractJasperReports.isNumberReal(entrada)){

												if(aplicarTodo==false){

													this.view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(entrada));//.getArticulo().setPrecioVenta(new Double(entrada));

												}else{

													//se recorren los item de la factura aplicando el descuento
													for(int xx=0;xx<view.getModeloTabla().getDetalles().size();xx++){
														DetalleFactura detalle=this.view.getModeloTabla().getDetalle(xx);

														//dfsdf
														if(detalle.getArticulo().getId()!=-1)
															if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){

																detalle.setDescuentoItem(new BigDecimal(entrada));
															}
													}
												}

												this.calcularTotales();
											}
										}
									}//fin del descuento no porjentaje



								}//fin comprobacion del usuario admin
							}//fin de la captura del pwd del usuario

						}else{





							//si el descuento es un porcentaje
							if(ConexionStatic.getUsuarioLogin().getConfig().isDescPorcentaje()){


								if(filaPulsada>=0){

									etiqueta.setText("Escriba el porcentaje(%) de descuento 1-5%");
									JOptionPane.showMessageDialog ( view,  panelDescuento,  "Descuento",JOptionPane.INFORMATION_MESSAGE);
									//String seleccionadoDescuento=JOptionPane.showInputDialog(view,"Escriba el porcentaje(%) de descuento 1-55%",JOptionPane.QUESTION_MESSAGE);
									String seleccionadoDescuento=descuento.getText();

									boolean aplicarTodo = rememberChk.isSelected();


									if(AbstractJasperReports.isNumberReal(seleccionadoDescuento)){
										double bdDescuento=Double.parseDouble(seleccionadoDescuento);

										if(bdDescuento<=maxDescuento){

											if(aplicarTodo==false){
												BigDecimal cantidad=this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad();
												BigDecimal precioVenta= new BigDecimal(view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getPrecioVenta());
												//se calcula el total del item
												BigDecimal totalItem=cantidad.multiply(precioVenta);


												double desc=bdDescuento/100;

												BigDecimal des=totalItem.multiply(new BigDecimal(desc));

												this.view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));
											}else{

												//se recorren los item de la factura aplicando el descuento
												for(int xx=0;xx<view.getModeloTabla().getDetalles().size();xx++){
													DetalleFactura detalle=this.view.getModeloTabla().getDetalle(xx);

													//dfsdf
													if(detalle.getArticulo().getId()!=-1)
														if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){


															BigDecimal cantidad=detalle.getCantidad();
															BigDecimal precioVenta= new BigDecimal(detalle.getArticulo().getPrecioVenta());
															//se calcula el total del item
															BigDecimal totalItem=cantidad.multiply(precioVenta);


															double desc=bdDescuento/100;

															BigDecimal des=totalItem.multiply(new BigDecimal(desc));

															detalle.setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));


														}
												}
											}//fin del descuento por item o todo
											//this.view.getModeloTabla().getDetalle(filaPulsada).setDescuento(bdDescuento);//.getArticulo().setPrecioVenta(new Double(entrada));
											this.calcularTotales();
										}else{
											JOptionPane.showMessageDialog(view, "No puede otorgar un descuento mayo del 5%", "Error", JOptionPane.ERROR_MESSAGE);
										}
									}else{
										JOptionPane.showMessageDialog(view, "El descuento debe ser un numero", "Error", JOptionPane.ERROR_MESSAGE);
									}
								}

							}else{//sino es un porcentaje

								if(filaPulsada>=0){
									//String entrada=JOptionPane.showInputDialog("Escriba el descuento");

									etiqueta.setText("Escriba el descuento");
									JOptionPane.showMessageDialog ( view,  panelDescuento,  "Descuento",JOptionPane.INFORMATION_MESSAGE);
									//String seleccionadoDescuento=JOptionPane.showInputDialog(view,"Escriba el porcentaje(%) de descuento 1-55%",JOptionPane.QUESTION_MESSAGE);
									String entrada=descuento.getText();
									boolean aplicarTodo = rememberChk.isSelected();

									if(AbstractJasperReports.isNumberReal(entrada)){

										if(aplicarTodo==false){

											this.view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(entrada));//.getArticulo().setPrecioVenta(new Double(entrada));

										}else{

											//se recorren los item de la factura aplicando el descuento
											for(int xx=0;xx<view.getModeloTabla().getDetalles().size();xx++){
												DetalleFactura detalle=this.view.getModeloTabla().getDetalle(xx);

												//dfsdf
												if(detalle.getArticulo().getId()!=-1)
													if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){

														detalle.setDescuentoItem(new BigDecimal(entrada));
													}
											}
										}

										this.calcularTotales();
									}
								}
							}//fin del descuento no porjentaje




						}
						break;
						
					case KeyEvent.VK_F8:

						if(ConexionStatic.getUsuarioLogin().getConfig().isPwdPrecio()){

							String pwd=getPassword();
							if(pwd!=null)
							{
								//String pwd=new String(pf.getPassword());
								//comprabacion del permiso administrativo
								if(myUsuarioDao.comprobarAdmin(pwd)){

									if(filaPulsada>=0){
										String entrada=JOptionPane.showInputDialog("Escriba el precio");

										if(AbstractJasperReports.isNumberReal(entrada)){
											this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecioVenta(new Double(entrada));
											this.calcularTotales();
										}
									}
								}
							}
						}else{
							if(filaPulsada>=0){
								String entrada=JOptionPane.showInputDialog(view,"Escriba el precio");

								if(AbstractJasperReports.isNumberReal(entrada)){
									this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecioVenta(new Double(entrada));
									this.calcularTotales();
								}
							}
						}
						
						break;
					case KeyEvent.VK_F9:
						if(filaPulsada>=0){


							String entrada=JOptionPane.showInputDialog(view,"Escriba la candidad");

							//se verfica en la configuracion si se puede facturar sin inventario
							if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario())
							{
								//se registra la cantida en la entrada del usuario
								BigDecimal cantidadSaldoItem=new BigDecimal(entrada);

								this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
								this.calcularTotales();

							}else{//se verfica en la configuracion si se puede facturar con inventario

								if(AbstractJasperReports.isNumberReal(entrada)){
									//si es un bien se procede de esta manera
									if(myArticulo.getTipoArticulo()==1){
										//se extre la exista del producto en el inventario
										double existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());


										//se registra la cantida en la entrada del usuario
										BigDecimal cantidadSaldoItem=new BigDecimal(entrada);

										//se recoge la nueva cantidad a colocar en el item
										double cantidad=cantidadSaldoItem.doubleValue();

										//se establece la nueva cantidad
										this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);


										//se busca el articulo en la factura las cantidades del mismo articulo
										double buscarEnRequisicionCantidad=view.getModeloTabla().buscarCantidadPorArticulo(myArticulo);


										if(existencia>0.0 && cantidad<=existencia) {
											this.calcularTotales();
										}else{
											JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion());
											view.getModeloTabla().eliminarDetalle(filaPulsada);
										}
									}else{//si es un servicio se procede a buscar sus insumos y si es posible facturar

										//la variable para la existecia en el kardex
										double existencia=0;

										//se registra la cantida en la entrada del usuario
										BigDecimal cantidadSaldoItem=new BigDecimal(entrada);

										//comprobar la existencia de los insumos
										List<Insumo> insumos=this.insumoDao.buscarPorId(myArticulo.getId());

										//comprobamos de el articulo tenga insumos para hacer la busca de sus existencias
										if(insumos!=null && insumos.size()>0){
											boolean exist=false;
											//recoremos los insumos comprobando si tiene existencia
											for(int xx=0;xx<insumos.size();xx++){

												//se comprueba que exista el insumo en el inventario
												existencia=myArticuloDao.getExistencia(insumos.get(xx).getArticulo().getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

												BigDecimal cantRequerida=cantidadSaldoItem.multiply(insumos.get(xx).getCantidad());

												if(existencia>0.0 && existencia>=cantRequerida.doubleValue()){
													//se establece que esiste
													exist=true;

												}else{//fin se la comprobacion de la existencia
													exist=false;
													JOptionPane.showMessageDialog(view,"El insumo "+ insumos.get(xx).getArticulo().getArticulo()+" que pertence al servicio "+myArticulo.getArticulo()+" ,\nno tiene existencia en "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
													break;
												}
											}//sin del for que recore los insumos

											//se comprueba que todos los insumos tienen exitencia para agregar el servicio a la factura
											if(exist){

												//activar para redondiar el precio de venta final
												if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){

													myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
												}
												//se establece la cantidad al item
												this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
												this.calcularTotales();
											}else{
												JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion());
												view.getModeloTabla().eliminarDetalle(filaPulsada);
												this.calcularTotales();
											}

										}else{//sino tiene insumos el servicio se agrega directamente a la factura

											//se establece la cantidad al item
											this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
											this.calcularTotales();
										}
									}
								}//fin de la comprobacion que la estrada es un numero
							}
						}
						
						break;
						
					case KeyEvent.VK_F10:
						
						
						break;
						
					case KeyEvent.VK_F11:

						break;
						
					case KeyEvent.VK_F12:

						
						break;
						
					case  KeyEvent.VK_ESCAPE:
						salir();
					break;
					
					case KeyEvent.VK_DELETE:
						if(filaPulsada>=0){
							 this.view.getModeloTabla().eliminarDetalle(filaPulsada);
							 this.calcularTotales();
						 }
						break;
						
					case KeyEvent.VK_DOWN:
						this.netBuscar++;
					//	this.buscarMasOmenos(netBuscar);
						break;
					case KeyEvent.VK_UP:
						if(netBuscar>=1){
							this.netBuscar--;
					//		this.buscarMasOmenos(netBuscar);
						}
						break;
					case KeyEvent.VK_LEFT:
						if(ConexionStatic.getUsuarioLogin().getConfig().isPwdEntrePrecio()){
							String pwd=getPassword();
							if(pwd!=null)
							{
								//comprabacion del permiso administrativo
								if(myUsuarioDao.comprobarAdmin(pwd)){

									if(filaPulsada>=0){
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().netPrecio();
										this.calcularTotales();
										selectRowInset(filaPulsada);
									}
								}
							}
						}else{
							if(filaPulsada>=0){
								this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().netPrecio();
								this.calcularTotales();
								selectRowInset(filaPulsada);
							}
						}
						break;
					case KeyEvent.VK_RIGHT:
						if(ConexionStatic.getUsuarioLogin().getConfig().isPwdEntrePrecio()){

							String pwd=getPassword();
							if(pwd!=null)
							{
								//comprabacion del permiso administrativo
								if(myUsuarioDao.comprobarAdmin(pwd)){

									if(filaPulsada>=0){
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().lastPrecio();
										this.calcularTotales();
										selectRowInset(filaPulsada);
									}
								}
							}
						}else{


							if(filaPulsada>=0){
								this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().lastPrecio();
								this.calcularTotales();
								selectRowInset(filaPulsada);
							}
						}
						break;
					}
					
								 
							
								
	}

	public static String getPassword() {
		JPanel panel = new JPanel();
		final JPasswordField passwordField = new JPasswordField(10);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				passwordField.requestFocusInWindow();
			}
		};
		pane.createDialog(null, "Autorizacion").setVisible(true);
		return passwordField.getPassword().length == 0 ? null : new String(passwordField.getPassword());
	}
	private void selectRowInset(int row){
		int col = 1;
		boolean toggle = false;
		boolean extend = false;
		this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
		this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
		this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

	}
	
	public void cargarFacturasPendientes(List<Factura> facturas){
		
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				this.view.addBotonPendiente(facturas.get(c),this);
				//view.conectarBtnContralador();
				
			}
		}else{
			view.eliminarBotones();
		}
		
	}
	private void showPendientes() {
		// TODO Auto-generated method stub
		ViewListaCotizacion vistaFacturars=new ViewListaCotizacion(this.view);
		CtlCotizacionLista ctlFacturas=new CtlCotizacionLista(vistaFacturars );
		
		vistaFacturars.pack();
		
		boolean resul=ctlFacturas.buscarCotizaciones(view);
		
		if(resul){
			
			
			this.myFactura=ctlFacturas.getMyFactura();
			cargarFacturaView();
			
		}
		
		//myArticulo=null;
		vistaFacturars.dispose();
		ctlFacturas=null;
		vistaFacturars.dispose();
		ctlFacturas=null;
	}
	
	
	private void cierreCaja() {
		// TODO Auto-generated method stub
		
		
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
		
		if(e.getComponent()==this.view.getTxtNombrecliente()){
			view.getTxtIdcliente().setText("-1");
			this.myCliente=null;
			
		}
		//para dejar la view para una nueva factura
		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N){
			setEmptyView();
		}
		
		//para actulizar factura
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A){
						if(view.getBtnActualizar().isEnabled())
							actualizar();
					}
				//para guardar factura
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_G){
						if(view.getBtnGuardar().isEnabled())
							guardar();
						
					}
				//para dejar la view para una nueva factura
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N){
					setEmptyView();
				}
				
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
					

					

		        }
		//Recoger que fila se ha pulsadao en la tabla
		filaPulsada = this.view.getTableDetalle().getSelectedRow();
		char caracter = e.getKeyChar();
		
		
		//para quitar los simnos mas o numero que ingrese en la busqueda
		if(e.getComponent()==this.view.getTxtBuscar()){
			Character caracter1 = new Character(e.getKeyChar());
	        if (!esValido(caracter1))
	        {
	           String texto = "";
	           for (int i = 0; i < view.getTxtBuscar().getText().length(); i++)
	                if (esValido(new Character(view.getTxtBuscar().getText().charAt(i))))
	                    texto += view.getTxtBuscar().getText().charAt(i);
	           			view.getTxtBuscar().setText(texto);
	                //view.getToolkit().beep();
	        }
		}
		/*
	//que no se la fecha de arriba y abajo
	if(e.getKeyCode()!=KeyEvent.VK_DOWN && e.getKeyCode()!= KeyEvent.VK_UP && e.getKeyCode()!= KeyEvent.VK_ENTER)
		
		//se comprueba que hay algo que buscar
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()!=0){
			
			myArticuloDao=new ArticuloDao(conexion);
			//JOptionPane.showMessageDialog(view, "2");
			//JOptionPane.showMessageDialog(view, view.getTxtBuscar().getText());
			this.myArticulo=this.myArticuloDao.buscarArticuloNombre(view.getTxtBuscar().getText());
			
			//JOptionPane.showMessageDialog(view, myArticulo);
			if(myArticulo!=null){
				view.getTxtArticulo().setText(myArticulo.getArticulo());
				view.getTxtPrecio().setText("L. "+myArticulo.getPrecioVenta());
				netBuscar=0;
				netBuscar++;
				
			}
			else{
				myArticulo=null;
				view.getTxtArticulo().setText("");
				view.getTxtPrecio().setText("");
			}
		}
		else{
			myArticulo=null;
			view.getTxtArticulo().setText("");
			view.getTxtPrecio().setText("");
		}
		*/
		
		if(caracter=='+'){
			if(filaPulsada>=0){
				//JOptionPane.showMessageDialog(view,e.getKeyChar()+" FIla:"+filaPulsada);
				this.view.getModeloTabla().masCantidad(filaPulsada);
				//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
				this.calcularTotales();
			}
		}
		if(caracter=='-'){
			if(filaPulsada>=0){
				//JOptionPane.showMessageDialog(view,e.getKeyChar()+" FIla:"+filaPulsada);
				this.view.getModeloTabla().restarCantidad(filaPulsada);
				//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
				this.calcularTotales();
			}
		}
		
		
	}
	/*	
	private void buscarMasOmenos(int p){
		//se comprueba que hay algo que buscar
				if(view.getTxtBuscar().getText().trim().length()!=0){
					
					myArticuloDao=new ArticuloDao(conexion);
					//JOptionPane.showMessageDialog(view, "2");
					//JOptionPane.showMessageDialog(view, view.getTxtBuscar().getText());
					this.myArticulo=this.myArticuloDao.buscarArticuloNombre(view.getTxtBuscar().getText(),p);
					
					//JOptionPane.showMessageDialog(view, myArticulo);
					if(myArticulo!=null){
						view.getTxtArticulo().setText(myArticulo.getArticulo());
						view.getTxtPrecio().setText("L. "+myArticulo.getPrecioVenta());
						
					}
					else{
						myArticulo=null;
						view.getTxtArticulo().setText("");
						view.getTxtPrecio().setText("");
					}
				}
				else{
					myArticulo=null;
					view.getTxtArticulo().setText("");
					view.getTxtPrecio().setText("");
				}
	}*/
	
	private void salir(){
		this.view.setVisible(false);
		
		
	}
	private void guardar(){
		
		
		
		setFactura();


		boolean resulVendedor=false;

		ViewCargarVenderor viewVendedor=new ViewCargarVenderor(view);
		CtlCargarVendedor ctlVendedor=new CtlCargarVendedor(viewVendedor);

		resulVendedor=ctlVendedor.cargarVendedor();

		if(resulVendedor) {

			myFactura.setVendedor(ctlVendedor.getVendedor());//activas para cuando se necesite un vendedor
			boolean resultado = facturaOrdenesDao.registrar(myFactura);

			if (resultado) {
				myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
				resultado = true;

				this.tipoView = 1;
				//this.view.setVisible(false);
				//view.addBotonPendiente(myFactura,this);

				setEmptyView();


				view.getBtnsGuardador().deleteAll();

				cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
			} else {
				JOptionPane.showMessageDialog(view, "Error al guardar la factura temporal", "Error al guardar", JOptionPane.ERROR_MESSAGE);
			}
		}else {
			JOptionPane.showMessageDialog(view, "Debe agregar un vendendor a la factura", "Error al guardar", JOptionPane.ERROR_MESSAGE);
		}

	
		
	}
	private void actualizar() {
		// TODO Auto-generated method stub
		setFactura();
		facturaOrdenesDao.actualizar(myFactura);
		//this.view.setVisible(false);
		//giu
		this.tipoView=1;
		this.view.getBtnGuardar().setEnabled(true);
		this.view.getBtnActualizar().setEnabled(false);
		
		//view.getBtnsGuardador().setFactura(myFactura);
		
		setEmptyView();
		
		view.getBtnsGuardador().deleteAll();
		
		cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
		
		//view.addBotonPendiente(myFactura,this);
		
	}
	private boolean validar(){
		boolean resultado=false;
		if(!(view.getModeloTabla().getRowCount()>1)){
			JOptionPane.showMessageDialog(view, " Debe agregar articulos primero.","Error Validacion",JOptionPane.ERROR_MESSAGE);
			resultado=false;
		}
		else if(this.myCliente==null){
			
			//JOptionPane.showMessageDialog(view, "Debe agregar el cliente primero");
			JOptionPane.showMessageDialog(view, "Debe agregar el cliente primero", "Error Validacion", JOptionPane.ERROR_MESSAGE);
			resultado=false;
			
		}else{
			resultado=true;
		}
		return resultado;
	}
	private void cobrar(){
		
		
	}
	private void buscarArticulo(){
	
		//se llama el metodo que mostrar la ventana para buscar el articulo
		ViewListaArticulo viewListaArticulo=new ViewListaArticulo(view);
		CtlArticuloBuscar ctlArticulo=new CtlArticuloBuscar(viewListaArticulo);
		
		viewListaArticulo.pack();
		ctlArticulo.view.getTxtBuscar().setText("");
		ctlArticulo.view.getTxtBuscar().selectAll();
		view.getTxtBuscar().requestFocusInWindow();
		viewListaArticulo.conectarControladorBuscar(ctlArticulo);
		
		boolean resul=ctlArticulo.buscarArticulo(view);
		
		if(resul){
			
			myArticulo=ctlArticulo.getArticulo();
			//myArticulo=myArticulo1;
			preciosDao=new PrecioArticuloDao();
			
			myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo.getId()));
			myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
			
			this.view.getModeloTabla().setArticulo(myArticulo);
			//this.view.getModelo().getDetalle(row).setCantidad(1);
			
			//calcularTotal(this.view.getModeloTabla().getDetalle(row));
			calcularTotales();
			this.view.getModeloTabla().agregarDetalle();
			
			selectRowInset();
		}
		
		//myArticulo=null;
		viewListaArticulo.dispose();
		ctlArticulo=null;
		
	}
	private void setEmptyView(){
		//se estable la tabla de detalles vacia
		view.getModeloTabla().setEmptyDetalles();
		
		myFactura.setCodigoAlter(0);
		//se agrega una fila vacia a la tabla detalle
		view.getModeloTabla().agregarDetalle();
		
		
		//conseguir la fecha la facturaa
		view.getTxtFechafactura().setText(facturaDao.getFechaSistema());
		
		//se estable un cliente generico para la factura
		this.view.getTxtIdcliente().setText("1");
		this.view.getTxtNombrecliente().setText("Consumidor final");
		view.getTxtRtn().setText("");
		
		
		this.myCliente=null;
		this.myArticulo=null;
		
		//this.view.getTxtArticulo().setText("");
		this.view.getTxtBuscar().setText("");
		this.view.getTxtDescuento().setText("");
		this.view.getTxtImpuesto().setText("0.00");
		this.view.getTxtImpuesto18().setText("0.00");
		//this.view.getTxtPrecio().setText("0.00");
		this.view.getTxtSubtotal().setText("0.00");
		this.view.getTxtTotal().setText("0.00");
		this.myFactura.setObservacion("");
		this.view.getRdbtnContado().setSelected(true);
		
		//se estable el focus de la view en la caja de texto buscar
		this.view.getTxtBuscar().requestFocusInWindow();
		
	}
	private void buscarCliente(){
		//se crea la vista para buscar los cliente
		ViewListaClientes viewListaCliente=new ViewListaClientes (this.view);
		
		CtlClienteBuscar ctlBuscarCliente=new CtlClienteBuscar(viewListaCliente);
		
		boolean resul=ctlBuscarCliente.buscarCliente(view);
		//se comprueba si le regreso un articulo valido
		if(resul){
			
			myCliente=ctlBuscarCliente.getCliente();
			this.view.getTxtIdcliente().setText(""+myCliente.getId());
            this.view.getTxtNombrecliente().setText(myCliente.getNombre());
			this.view.getTxtRtn().setText(myCliente.getRtn());
		
		}else{
			//JOptionPane.showMessageDialog(view, "No se encontro el cliente");
			this.view.getTxtIdcliente().setText("1");
            this.view.getTxtNombrecliente().setText("Consumidor final");
		
		}
		viewListaCliente.dispose();
		ctlBuscarCliente=null;
	}
	
	
public void guardarLocal(){
	
		
	}

public void guardarRemoto(){

	}



	
	
	private void selectRowInset(){
		
		int row = this.view.getTableDetalle().getRowCount () - 2;
	    int col = 1;
	    boolean toggle = false;
	    boolean extend = false;
	    this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
	    this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
	    this.view.getTableDetalle().addColumnSelectionInterval(0, 6);
		
		/*<<<<<<<<<<<<<<<selecionar la ultima fila creada>>>>>>>>>>>>>>>*/
		/*int row =  this.view.geTableDetalle().getRowCount () - 2;
		JOptionPane.showMessageDialog(view, row);
		/Rectangle rect = this.view.geTableDetalle().getCellRect(row, 0, true);
		this.view.geTableDetalle().scrollRectToVisible(rect);
		this.view.geTableDetalle().clearSelection();*/
		//this.view.geTableDetalle().setRowSelectionInterval(row, row);
		//view.geTableDetalle().setRowSelectionInterval(row, row);
		//view.geTableDetalle().clearSelection();
		//view.geTableDetalle().addRowSelectionInterval(row,row);
		//TablaModeloFactura modelo = (TablaModeloFactura)this.view.geTableDetalle().getModel();
		//modelo.fireTableDataChanged();
		//this.view.getModeloTabla().fireTableDataChanged();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		//facturaDao.desconectarBD();
		//this.clienteDao.desconectarBD();
		//this.myArticuloDao.desconectarBD();
		//this.myFactura.setIdFactura(-1);
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
	
	public void cargarFacturaView(){
		
		this.view.getTxtIdcliente().setText(""+myFactura.getCliente().getId());
        this.view.getTxtNombrecliente().setText(myFactura.getCliente().getNombre());
		view.getTxtRtn().setText(myFactura.getCliente().getRtn());
		
		//se establece el total e impuesto en el vista
		this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtImpuesto().setText(""+myFactura.getTotalImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		this.view.getModeloTabla().setDetalles(myFactura.getDetalles());
	}


	public Factura actualizarFactura(Factura f) {
		// TODO Auto-generated method stub
		this.myFactura=f;
		cargarFacturaView();
		this.view.getBtnGuardar().setVisible(false);
		this.view.getBtnActualizar().setVisible(true);
		this.view.getModeloTabla().agregarDetalle();
		tipoView=2;
		this.view.setVisible(true);
		
		//para controlar que es una actualizacion la que se hace
		
		
		return myFactura;
		
	}


	public boolean getAccion() {
		view.setVisible(true);
		return resultado;
	}


	public void viewFactura(Factura f) {
		// TODO Auto-generated method stub
		this.myFactura=f;
		cargarFacturaView();
		this.view.getPanelAcciones().setVisible(false);
		this.view.setVisible(true);
	}


	public Factura getFactura() {
		// TODO Auto-generated method stub
		return this.myFactura;
	}
	
	private boolean setCierre() {
		return true;

	}

}
