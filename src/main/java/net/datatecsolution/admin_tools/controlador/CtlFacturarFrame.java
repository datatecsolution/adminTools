package net.datatecsolution.admin_tools.controlador;


import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtlFacturarFrame  implements ActionListener, MouseListener, TableModelListener, WindowListener, KeyListener  {
	
	private ViewFacturarFrame view;
	private Factura myFactura=null;
	private FacturaDao facturaDao=null;//=new FacturaDao();
	private ClienteDao clienteDao=null;
	private Articulo myArticulo=null;
	private ArticuloDao myArticuloDao=null;
	private PrecioArticuloDao preciosDao=null;
	private InsumoDao insumoDao=null;
	private CodBarraDao codBarraDao=null;
	
	
	private Cliente myCliente=null;
	private int filaPulsada=0;
	private boolean resultado=false;
	private UsuarioDao myUsuarioDao;
	
	private int tipoView=1;
	private int netBuscar=0;
	
	private DetalleFacturaOrdenDao detallesOrdenDao=null;
	List<ViewFacturarFrame> ventanas;
	private FacturaOrdenVentaDao facturaOrdenesDao;
	private Caja cajaDefecto;
	private boolean isThereConexion=false;

	private boolean unirCanItem=true;
	
	
	public CtlFacturarFrame(ViewFacturarFrame v ,List<ViewFacturarFrame> ven){
	
		

		ventanas=ven;
		view=v;
		view.conectarContralador(this);
		//se inicializan atributos de la factura
		myFactura=new Factura();
		myArticuloDao=new ArticuloDao();
		clienteDao=new ClienteDao();
		facturaDao=new FacturaDao();
		facturaOrdenesDao=new FacturaOrdenVentaDao();
		preciosDao=new PrecioArticuloDao();
		codBarraDao=new CodBarraDao();
		detallesOrdenDao=new DetalleFacturaOrdenDao();
		insumoDao=new InsumoDao();
		
		myUsuarioDao=new UsuarioDao();
		
		
		this.setEmptyView();
		
		cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
		this.tipoView=1;
		
		this.setCierre();
		cajaDefecto=new Caja(ConexionStatic.getUsuarioLogin().getCajaActiva());
	
		
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

							//se verifica que la opcion de unir las cantidades en las articulos repetidos
							boolean unirCantidad=false;
							if(unirCanItem==true)
								unirCantidad=this.buscarArticuloEnFactura(myArticulo);
							else
								unirCantidad=false;

							//se busca en los item ya agregados si ya existe en el metodo solo se agrega la nueva cantidad sino se agrega como nuevo
						if( unirCantidad ==false  )

							//facturar sin tomar en cuenta el inventario
							if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario())
							{


								//activar para redondiar el precio de venta final
								if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){

									myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
								}

								//JOptionPane.showMessageDialog(view,myArticulo.getTipoArticulo()+" tipo de articulo","Error en existencia",JOptionPane.ERROR_MESSAGE);

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


							}else{
									//se comprueba que exista el producto en el inventario
									double existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

									//si es necesario comprobar los inventarios de articulos

									//fdsf
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
												//dfs
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
						}else{
							JOptionPane.showMessageDialog(view, "No se encontro el articulo");
							view.getTxtBuscar().setText("");
							view.getTxtBuscar().requestFocusInWindow();
							myArticulo=null;
						}
						
					//}
				}/*else//si el articulo esta nulo se agrega el ultimo articulo creado
				{
					//conseguir los precios del producto
					myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo.getId()));
					this.view.getModeloTabla().setArticulo(myArticulo);
					//this.view.getModelo().getDetalle(row).setCantidad(1);
					
					//calcularTotal(this.view.getModeloTabla().getDetalle(row));
					calcularTotales();
					this.view.getModeloTabla().agregarDetalle();
					view.getTxtBuscar().setText("");
					selectRowInset();
					
				}*/
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
			this.cobrar();
			break;
		case "GUARDAR":
			this.guardar();
			break;
			
		case "CIERRECAJA":
			this.cierreCaja();
			break;
		case "COTIZACION":
			//this.showPendientes();
			//JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			guardarCotizacion();
			break;
		case "GET_COTIZACIONES":
			buscarCotizaciones();
			break;

		case "IMPRIMIRPENDIENTE":

			int idFacturaTemporal=view.getBtnsGuardador().getFacturaSeleted().getIdFactura();

			//se imprime un tike de salida para prueba
			try {
				AbstractJasperReports.createReportOrdenCarta(ConexionStatic.getPoolConexion().getConnection(),idFacturaTemporal);

				//AbstractJasperReports.imprimierFactura();
				AbstractJasperReports.showViewer(view);


			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}




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
		
		//se configura la caja donde se guardo la factura
		Caja caja=ConexionStatic.getUsuarioLogin().setCajaActica(fact.getCodigoCaja());
		//JOptionPane.showMessageDialog(view, caja.toString());
		
		ViewModuloFacturar frame = (ViewModuloFacturar)view.getTopLevelAncestor();
		frame.btnCaja.setText(caja.getDescripcion());
		
		
		myFactura.setDetalles(detallesOrdenDao.detallesFacturaPendiente(numeroFactura));
		
		cargarFacturaView();
		this.calcularTotales();
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

					identificador=(int)this.view.getModeloTabla().getValueAt(row, 0);
					myArticulo=this.view.getModeloTabla().getDetalle(row).getArticulo();


					//se comprueba que exista el producto en el inventario
					double existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());

					double cantidad=1;

					double buscarEnRequisicionCantidad=view.getModeloTabla().buscarCantidadPorArticulo(myArticulo);

					if(buscarEnRequisicionCantidad>0){
						cantidad=cantidad+buscarEnRequisicionCantidad;
					}

					if(existencia>0.0 && cantidad<=existencia) {

						calcularTotales();
						view.getTxtBuscar().requestFocusInWindow();
					}else{
						JOptionPane.showMessageDialog(view, myArticulo.getArticulo()+" no tiene existencia en "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
						view.getModeloTabla().eliminarDetalle(row);
						calcularTotales();
					}
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
	
	for(int x=0; x<view.getModeloTabla().getDetalles().size();x++){
		
		DetalleFactura detalle=view.getModeloTabla().getDetalle(x);
		
		
		if(detalle.getArticulo().getId()!=-1)
			if(detalle.getCantidad().doubleValue()!=0 && detalle.getArticulo().getPrecioVenta()!=0){
				
				
				//dfs
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
		
		
		//Recoger qu� fila se ha pulsadao en la tabla
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
						cobrar();
						break;
						
					case KeyEvent.VK_F3:
							buscarCliente();
						break;
						
					case KeyEvent.VK_F4:
						
						ViewEntradaCaja vEntradaCaja=new ViewEntradaCaja(null);
						CtlEntradaCaja cEntradaCaja=new CtlEntradaCaja(vEntradaCaja);
						
						vEntradaCaja.dispose();
						vEntradaCaja=null;
						cEntradaCaja=null;
							
						break;
						
					case KeyEvent.VK_F5:
						view.getBtnsGuardador().deleteAll();
						cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
						break;
						
					case KeyEvent.VK_F6:
						cierreCaja();
						break;
						
					case KeyEvent.VK_F7:
						
						double maxDescuento=55;
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
							JPasswordField pfs = new JPasswordField();
							int action2 = JOptionPane.showConfirmDialog(view, pfs,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
							if(action2 < 0){
								
							}else{
								String pwd=new String(pfs.getPassword());
								
								//comprabacion del permiso administrativo
								if(myUsuarioDao.comprobarAdmin(pwd)){
									
									

									//si el descuento es un porcentaje
									if(ConexionStatic.getUsuarioLogin().getConfig().isDescPorcentaje()){
										
										
										if(filaPulsada>=0){
											
											etiqueta.setText("Escriba el porcentaje(%) de descuento 1-55%");
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
										    		JOptionPane.showMessageDialog(view, "No puede otrogar un descuento mayo del 55%", "Error", JOptionPane.ERROR_MESSAGE);
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
									
									etiqueta.setText("Escriba el porcentaje(%) de descuento 1-55%");
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
								    		JOptionPane.showMessageDialog(view, "No puede otrogar un descuento mayo del 55%", "Error", JOptionPane.ERROR_MESSAGE);
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
						
							JPasswordField pf = new JPasswordField();
							int action = JOptionPane.showConfirmDialog(view, pf,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
							if(action < 0){
								
							}else{
								String pwd=new String(pf.getPassword());
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
								String entrada=JOptionPane.showInputDialog("Escriba el precio");
						
								if(AbstractJasperReports.isNumberReal(entrada)){
											this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecioVenta(new Double(entrada));
											this.calcularTotales();
									}
							}
						}
						
						/*
						if(filaPulsada>=0){
							String entrada=JOptionPane.showInputDialog("Escriba el precio");
							
							if(modelo.AbstractJasperReports.isNumberReal(entrada)){
								this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecioVenta(new Double(entrada));
								this.calcularTotales();
							}
						}*/
						
						
						
						break;
					case KeyEvent.VK_F9:
						if(filaPulsada>=0){
							
							String entrada=JOptionPane.showInputDialog(view,"Escriba el cantida");
							
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
						
						
							ViewCobro viewCobro=new ViewCobro(null);
							CtlCobro ctlCobro=new CtlCobro(viewCobro);
							
							viewCobro.dispose();
							viewCobro=null;
							ctlCobro=null;
						
						
						break;
						
					case KeyEvent.VK_F11:
						ViewPagoProveedor vPagoProveedores=new ViewPagoProveedor(null);
						vPagoProveedores.getCbFormaPago().setEnabled(false);
						CtlPagoProveedor cPagoProveedores=new CtlPagoProveedor(vPagoProveedores);
						
						vPagoProveedores.dispose();
						cPagoProveedores=null;
						break;
						
					case KeyEvent.VK_F12:
						

							ViewSalidaCaja viewSalida = new ViewSalidaCaja(null);
							CtlSalidaCaja ctlSalida = new CtlSalidaCaja(viewSalida);

							
							viewSalida.dispose();
							viewSalida = null;
							ctlSalida = null;
						
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
								JPasswordField pf2 = new JPasswordField();
								int action2 = JOptionPane.showConfirmDialog(view, pf2,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
								if(action2 < 0){
									
								}else{
									String pwd=new String(pf2.getPassword());
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
						
								JPasswordField pf3 = new JPasswordField();
								int action3 = JOptionPane.showConfirmDialog(view, pf3,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
								//String pwd=JOptionPane.showInputDialog(view, "Escriba el password admin", "Seguridad", JOptionPane.INFORMATION_MESSAGE);
								if(action3 < 0){
									
								}else{
									String pwd=new String(pf3.getPassword());
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
	
	public void cargarFacturasPendientes(List<Factura> facturas){
		
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				view.addBotonPendiente(facturas.get(c),this);
			}
			view.getPanelGuardados().revalidate();
		}else{
			view.eliminarBotones();
		}
		
		
	}
	private void buscarCotizaciones() {
		// TODO Auto-generated method stub
		ViewListaCotizacion vistaFacturars=new ViewListaCotizacion(null);
		CtlCotizacionLista ctlFacturas=new CtlCotizacionLista(vistaFacturars );
		
		vistaFacturars.pack();
		
		boolean resul=ctlFacturas.buscarCotizaciones(null);
		
		if(resul){
			
			
			this.myFactura=ctlFacturas.getMyFactura();
			cargarFacturaView();

			this.view.getModeloTabla().agregarDetalle();
			
			/*myArticulo=ctlArticulo.getArticulo();
			//myArticulo=myArticulo1;
			preciosDao=new PrecioArticuloDao(conexion);
			
			myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo.getId()));
			myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
			
			this.view.getModeloTabla().setArticulo(myArticulo);
			//this.view.getModelo().getDetalle(row).setCantidad(1);
			
			//calcularTotal(this.view.getModeloTabla().getDetalle(row));
			calcularTotales();
			this.view.getModeloTabla().agregarDetalle();
			
			selectRowInset();*/
		}
		
		//myArticulo=null;
		vistaFacturars.dispose();
		ctlFacturas=null;
		vistaFacturars.dispose();
		ctlFacturas=null;
	}
	
	
	private void cierreCaja() {
		// TODO Auto-generated method stub
		
		
		
		
		CierreCajaDao cierreDao=new CierreCajaDao();



		CierreCaja oldCierre = new CierreCaja();

		// se consiguie el ultimo cierre del usuario
		oldCierre = cierreDao.getCierreUltimoUser();


			// se verifica que hay facturas para crear un cierre
		if(facturaDao.verificarCierre(ConexionStatic.getUsuarioLogin().getCajas()) && oldCierre.getEstado() == true){
			
			ViewCuentaEfectivo viewContar=new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar=new CtlContarEfectivo(viewContar);
			
			
			if(ctlContar.getEstado())//verifica que se ordeno realizar el cierre desde la view de contar dinero

				if(cierreDao.actualizarCierre(ctlContar.getTotal()))//se realiza el cierre y se verifica que todos salio bien
				{
					
					
					
					
					//si esta activado el reporte de ventas por categorias
					if(ConexionStatic.getUsuarioLogin().getConfig().isImprReportCategCierre())
					{
					
									//se consigue el cierre guardado
									CierreCaja elCierre=cierreDao.buscarPorId(cierreDao.idUltimoRequistro);
									
									//se crea el informe de ventas por categorias
									List<VentasCategoria> ventas= new ArrayList<VentasCategoria>();
									
									//se extren todas las categorias
									CategoriaDao categoriaDao=new CategoriaDao();
									List<Categoria> categorias=categoriaDao.todos();
									
									//se recorren las categorias y se agregan a reporte de ventas
									for(int yy=0;yy<categorias.size();yy++){
										VentasCategoria una =new VentasCategoria();
										una.setCodigoCategoria(categorias.get(yy).getId());
										una.setCategoria(categorias.get(yy).getDescripcion());
										ventas.add(una);
									}
									
									//se consigue los registros de las facturas de los cierre
									CierreFacturacionDao cierreFacturacioDao=new CierreFacturacionDao();
									elCierre.setCierreFacturas(cierreFacturacioDao.buscarIdCierre(elCierre.getId()));
									
									//si existe los registros de los cierres se recororen en busca los totales en las facturas
									if(elCierre.getCierreFacturas()!=null){
										
										//se recorren los registros para sacar los totales de la facturas
										for(int xx=0;xx<elCierre.getCierreFacturas().size();xx++){
											
											this.facturaDao.getVentasCategorias(elCierre.getCierreFacturas().get(xx).getNoFacturaInicio(),
																				elCierre.getCierreFacturas().get(xx).getNoFacturaFinal(), 
																				elCierre.getUsuario(), 
																				elCierre.getCierreFacturas().get(xx).getCaja(), ventas);
											
											
									
										}
										
										try {
										
											AbstractJasperReports.createReportVentasCategoria(ConexionStatic.getPoolConexion().getConnection(),elCierre,ventas);
											
											AbstractJasperReports.imprimierFactura();
											//AbstractJasperReports.showViewer(view);
										} catch (SQLException eee) {
											// TODO Auto-generated catch block
											eee.printStackTrace();
										}
									}
					}
			
					
					try {
						
						AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 4, cierreDao.idUltimoRequistro);
						
						AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
						
						
						viewContar.dispose();
						viewContar=null;
						ctlContar=null;
						salir();
						
						
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
				}else{
					JOptionPane.showMessageDialog(view, "No se guardo el cierre de corte. Vuelva a hacer el corte.");
				}
		}//fin de la verificacion de las facturas 
		else{
			JOptionPane.showMessageDialog(view, "No hay facturas para crear un cierre de caja o no tiene un cierre activo.");
			salir();
		}

		//ConexionStatic.setNivelFac(true);
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
		filaPulsada = this.view.getTableDetalle().getSelectedRow();
		
		
		if(e.getComponent()==this.view.getTxtNombrecliente()){
			view.getTxtIdcliente().setText("-1");
			this.myCliente=null;
			
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
		
		//para aplicar un precio en especifico
		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_UP){
			
			
			JPasswordField pfs = new JPasswordField();
			int action2 = JOptionPane.showConfirmDialog(view, pfs,"Escriba el password de admin",JOptionPane.OK_CANCEL_OPTION);
			if(action2 < 0){
				
			}else{
				String pwd=new String(pfs.getPassword());
				
				//comprabacion del permiso administrativo
				if(myUsuarioDao.comprobarAdmin(pwd)){
					
					ViewSelectPrecio viewSelectPrecio=new ViewSelectPrecio(null);
					CtlSelectPrecio ctlSelectPrecio=new CtlSelectPrecio(viewSelectPrecio);
					
					boolean resultado=ctlSelectPrecio.agregar();
					if(resultado){
						//JOptionPane.showMessageDialog(view,ctlSelectPrecio.isAplicarTodo()+" | " +ctlSelectPrecio.getPrecioSelect());
						
						//si se aplicara el precio a toda la factura
						if(ctlSelectPrecio.isAplicarTodo()){
							
							//se recorren los item de la factura aplicando el descuento
			    			for(int xx=0;xx<view.getModeloTabla().getDetalles().size();xx++){
			    				DetalleFactura detalle=this.view.getModeloTabla().getDetalle(xx);
			    				//se los precio sin el costo de la base de datos
								detalle.getArticulo().setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(detalle.getArticulo().getId()));
			    				//se verifica que el item no sea nullo
			    				if(detalle.getArticulo().getId()!=-1){

									//se verifica que seleccion el precio costo
									if(ctlSelectPrecio.getPrecioSelect().getCodigoPrecio()==4){

										//se busca el precio de costo del articulo
										PrecioArticulo unPrecio =this.preciosDao.getPrecioArticulo(detalle.getArticulo().getId(),4);
										//si el articulo tiene precio de costo se agrega a la el
										if(unPrecio!=null){
											detalle.getArticulo().getPreciosVenta().add(unPrecio);
											detalle.getArticulo().setPrecio(unPrecio);
										}else{
											detalle.getArticulo().lastPrecio();
											detalle.getArticulo().netPrecio();
										}

									}else{
										detalle.getArticulo().setPrecio(ctlSelectPrecio.getPrecioSelect());
									}
								}

			    					
			    			}
							
						}else{
							//fdsf
							if(filaPulsada>=0){
								//se los precio sin el costo de la base de datos
								view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getId()));

								//se verifica que seleccion el precio costo
								if(ctlSelectPrecio.getPrecioSelect().getCodigoPrecio()==4){

									//se busca el precio de costo del articulo
									PrecioArticulo unPrecio =this.preciosDao.getPrecioArticulo(view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getId(),4);
									//si el articulo tiene precio de costo se agrega a la el
									if(unPrecio!=null){
										//se agrega el precio de costo al item
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getPreciosVenta().add(unPrecio);
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecio(unPrecio);
										this.selectRowInset(filaPulsada);
									}else{
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().lastPrecio();
										this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().netPrecio();
										this.selectRowInset(filaPulsada);
									}
								}else{
									this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().setPrecio(ctlSelectPrecio.getPrecioSelect());

								}

								
							}
						}
						
						this.calcularTotales();
						
						////
					}
				}
			}
		}
		//para dejar la view para una nueva factura
		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N){
			setEmptyView();
		}
		
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
			
			
			//se verfica en la configuracion si se puede facturar sin inventario
			if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario())
			{
				Caja caja=ConexionStatic.getUsuarioLogin().nextCaja();
				//JOptionPane.showMessageDialog(view, caja.toString());
				
				ViewModuloFacturar frame = (ViewModuloFacturar)view.getTopLevelAncestor();
				frame.btnCaja.setText(caja.getDescripcion());
				
			}else{
					//verificamos que se agregaron articulos a la factura
					if(view.getModeloTabla().getRowCount()<=1){
						Caja caja=ConexionStatic.getUsuarioLogin().nextCaja();
						//JOptionPane.showMessageDialog(view, caja.toString());
						
						ViewModuloFacturar frame = (ViewModuloFacturar)view.getTopLevelAncestor();
						frame.btnCaja.setText(caja.getDescripcion());
					}
					else{
						JOptionPane.showMessageDialog(view, "No puede cambiar de caja. Debe eliminar los articulos agregado","ERORR",JOptionPane.ERROR_MESSAGE);
					}
			}
	
			

        }
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
			CajaDao cajasDao=new CajaDao();
			ConexionStatic.getUsuarioLogin().setCajas(cajasDao.getCajasUsuario(ConexionStatic.getUsuarioLogin()));
			ViewModuloFacturar frame = (ViewModuloFacturar)view.getTopLevelAncestor();
			frame.btnCaja.setText(ConexionStatic.getUsuarioLogin().getCajaActiva().getDescripcion());
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
				if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario()){
					
					this.view.getModeloTabla().masCantidad(filaPulsada);
					view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(0));
					//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
					this.calcularTotales();
					
				}else{
					
					//se extrae el articulo de tabla
					myArticulo= view.getModeloTabla().getDetalle(filaPulsada).getArticulo();
					
					//se filtra se es un bien
					if(myArticulo.getTipoArticulo()==1){
						//se extre la exista del producto en el inventario
						double existencia=myArticuloDao.getExistencia(myArticulo.getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());
						
						//se pasa a bigdecimal
						BigDecimal cantidadSaldoKardex=new BigDecimal(existencia);
						
						//se registra la cantida que tiene item
						BigDecimal cantidadSaldoItem=new BigDecimal(this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad().doubleValue());
						
						//se le suma uno que es la intencio del usuario
						cantidadSaldoItem=cantidadSaldoItem.add(new BigDecimal(1));
						
						//se calcula de diferencia 
						BigDecimal diferencia=cantidadSaldoKardex.subtract(cantidadSaldoItem);
						
						//si la direncia es mayor o igual a 0 se procede a establecer
						if(diferencia.doubleValue()>=0.00 || myArticulo.getTipoArticulo()==2){
							this.view.getModeloTabla().masCantidad(filaPulsada);
							//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
							this.calcularTotales();
						}else{
							JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);  
							//view.getModeloTabla().eliminarDetalle(filaPulsada);
						}
					}else{//si es un servicio se buscan los insumos
						
						//si es un servicio se procede a buscar sus insumos y si es posible facturar 
						
						//la variable para la existecia en el kardex
						double existencia=0;
						
						//se registra la cantida que tiene item
						BigDecimal cantidadSaldoItem=new BigDecimal(this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad().doubleValue());
						
						//se registra la cantida en la entrada del usuario
						BigDecimal newCantSaldoItem=cantidadSaldoItem.add(new BigDecimal(1));
						
						//comprobar la existencia de los insumos
						List<Insumo> insumos=this.insumoDao.buscarPorId(myArticulo.getId());
						
						//comprobamos de el articulo tenga insumos para hacer la busca de sus existencias
						if(insumos!=null && insumos.size()>0){
							boolean exist=false;
							//recoremos los insumos comprobando si tiene existencia
							for(int xx=0;xx<insumos.size();xx++){
								
								//se comprueba que exista el insumo en el inventario
								existencia=myArticuloDao.getExistencia(insumos.get(xx).getArticulo().getId(), ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getId());
								
								BigDecimal cantRequerida=newCantSaldoItem.multiply(insumos.get(xx).getCantidad());
								
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
								this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
								this.calcularTotales();
							}else{
								JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+newCantSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion());  
								view.getModeloTabla().eliminarDetalle(filaPulsada);
								this.calcularTotales();
							}
							
						}else{//sino tiene insumos el servicio se agrega directamente a la factura
							
							//se establece la cantidad al item
							this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
							this.calcularTotales();
						}
			
						
					}
				
				}
									
									
									
			}
		}
		if(caracter=='-'){
			if(filaPulsada>=0){
				//JOptionPane.showMessageDialog(view,e.getKeyChar()+" FIla:"+filaPulsada);
				this.view.getModeloTabla().restarCantidad(filaPulsada);
				//JOptionPane.showMessaconfigEnPantalla.setImprReportCategCierre(view.getTglbtnCategoriaEnCierre().isSelected());geDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
				this.calcularTotales();
			}
		}
		
		
	}
	public void actualizarVentanas() {
		// TODO Auto-generated method stub
		
		for(int x=0;x<ventanas.size();x++){
			if(ConexionStatic.getNivelFact()==true){
				ventanas.get(x).getTxtBuscar().setBackground(new Color(250, 0, 0));
			}	
			else{
				ventanas.get(x).getTxtBuscar().setBackground(new Color(60, 179, 113));
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
		
		
		if(view.getModeloTabla().getRowCount()>1){
			
		
					setFactura();
					myFactura.setCodigoCaja(ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
					
					boolean resultado=facturaOrdenesDao.registrar(myFactura);
					
					if(resultado){
						myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
						resultado=true;
						
						this.tipoView=1;
						//this.view.setVisible(false);
						//view.addBotonPendiente(myFactura,this);
						
						setEmptyView();
						
						view.getBtnsGuardador().deleteAll();
						
						cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
					}else{
						JOptionPane.showMessageDialog(view, "Error al guardar la factura temporal", "Error al guardar", JOptionPane.ERROR_MESSAGE);
					}
		
		}else{
			JOptionPane.showMessageDialog(view, "Para guardar debe agregar articulos.","ERROR",JOptionPane.ERROR_MESSAGE);
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
		
		isThereConexion =ConexionStatic.isDbConnected();
		//verificamos si existe la conexion a la base de datos
		if(isThereConexion){
			
			//verificamos que se agregaron articulos a la factura
			if(view.getModeloTabla().getRowCount()>1){
				
				boolean resulVendedor=false;
				
				//activas para cuando se necesite un vendedor
				if(ConexionStatic.getUsuarioLogin().getConfig().isVentanaVendedor()){
					ViewCargarVenderor viewVendedor=new ViewCargarVenderor(null);
					CtlCargarVendedor ctlVendedor=new CtlCargarVendedor(viewVendedor);
					
					 resulVendedor=ctlVendedor.cargarVendedor();
					 myFactura.setVendedor(ctlVendedor.getVendetor());//activas para cuando se necesite un vendedor
				}else{
				
					 resulVendedor=true;
					 
					 Empleado uno=new Empleado();
					 uno.setCodigo(1);
					myFactura.setVendedor(uno);
					 
				}
				
			
				//captura las observaciones d ela factura
				if(ConexionStatic.getUsuarioLogin().getConfig().isVentanaObservaciones()){
						String observaciones="";
						JTextArea ta = new JTextArea(20, 20);
						
						switch (JOptionPane.showConfirmDialog(view, new JScrollPane(ta),"Observaciones de la factura",JOptionPane.ERROR_MESSAGE)) {
						    case JOptionPane.OK_OPTION:
						        observaciones=ta.getText();
						        break;
						    default:
						    	observaciones="NA";
						    	break;
						}
						myFactura.setObservacion(observaciones);
				}
				
				if(resulVendedor)//verifica si ingreso el codigo del bombero
				{
					
					
				
					
					//
					
					
				
				
					//si la factura es al contado
					if(view.getRdbtnContado().isSelected()){
				
						//se muestra la vista para cobrar y introducir el cambio 
						ViewCambioPago viewPago=new ViewCambioPago(null);
						CtlCambioPago ctlPago=new CtlCambioPago(viewPago,myFactura.getTotal());
						//se muestra y ventana del cobro y se devuelve un resultado del cobro
						boolean resulPago=ctlPago.pagar();
						
						//se procede a verificar si se cobro
						if(resulPago)
						{
							//si la forma de pago fue en efectivo
							if(ctlPago.getFormaPago()==1){
								myFactura.setPago(ctlPago.getTotalPago());
								myFactura.setCambio(ctlPago.getCambio());
								myFactura.setCobroEfectivo(ctlPago.getCobroEfectivo());
								myFactura.setCobroTarjeta(ctlPago.getCobroTarjeta());
								myFactura.setTipoPago(1);
							}
							//si la forma de pago fue con tarjeta de credito o debito
							if(ctlPago.getFormaPago()==2){
								myFactura.setPago(myFactura.getTotal());
								myFactura.setCambio(new BigDecimal(00));
								myFactura.setTipoPago(2);
								myFactura.setObservacion(ctlPago.getRefencia());
							}
							setFactura();
							
							
							// se comprueba que sino tiene un cierre de caja
							// activo lo realice
							boolean resl = setCierre();
							
							
							//se procesa el resultado del cierre de caja
							if (resl) {
								
								this.guardarFactura();
							}else {
								JOptionPane.showMessageDialog(view,
										"No se puede cobrar la factura. Debe abrir la caja primero!!!", "Error caja",
										JOptionPane.ERROR_MESSAGE);
							}

						}//fin de la ventana en cobro
					
					}//fin de la factura al credito
					else
					if(view.getRdbtnCredito().isSelected()){//si la factura es al contado se procede a guardar e imprimir 
						
						
						
						//JOptionPane.showMessageDialog(view, myCliente.toString());
						//fsdf
						
						if(myCliente!=null &&  myCliente.getTipoCliente()==2){
							
							
							setFactura();
							myFactura.setTipoPago(3);
							//comprueba que el cliente este registrado
									//para verificar el credito del cliente
									///	BigDecimal saldo=this.myCliente.getSaldoCuenta();
									///	BigDecimal limite=this.myCliente.getLimiteCredito();
									///	BigDecimal nuevoSaldo=saldo.add(this.myFactura.getTotal());
						
										//no se necesita el cambio y pago porque es al credito
										myFactura.setCambio(new BigDecimal(0));
										myFactura.setPago(new BigDecimal(0));
										
										myFactura.setTipoFactura(2);
										
										// se comprueba que sino tiene un cierre de caja
										// activo lo realice
										boolean resl = setCierre();
										//se procesa el resultado del cierre de caja
										if (resl) {
											this.guardarFactura();
										}else {
											JOptionPane.showMessageDialog(view,
													"No se puede cobrar la factura. Debe abrir la caja primero!!!", "Error caja",
													JOptionPane.ERROR_MESSAGE);
										}
										
										
						}//fin de la verificacion del cliente 
						else{
							JOptionPane.showMessageDialog(view,
									"No se puede facturar. Debe crear el cliente primero!!!", "Error facturar",
									JOptionPane.ERROR_MESSAGE);
						}
						
						
					}//fin de la factura al credito
			}//sin del if donde se pide el codigo del vendedor
					
						
			}//fin del if donde se verifica que hay articulos que facturar
			else{
				JOptionPane.showMessageDialog(view, "Para guardar debe agregar articulos.","ERORR",JOptionPane.ERROR_MESSAGE);
			}
		
			//para cargar las ordenes de compra
			view.getBtnsGuardador().deleteAll();
			cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
			
		}//fin de la verificacion de la conexion de la base de datos
		
	}
	private void buscarArticulo(){
	
		//se llama el metodo que mostrar la ventana para buscar el articulo
		ViewListaArticulo viewListaArticulo=new ViewListaArticulo(null);
		CtlArticuloBuscar ctlArticulo=new CtlArticuloBuscar(viewListaArticulo);
		
		viewListaArticulo.pack();
		ctlArticulo.view.getTxtBuscar().setText("");
		ctlArticulo.view.getTxtBuscar().selectAll();
		view.getTxtBuscar().requestFocusInWindow();
		viewListaArticulo.conectarControladorBuscar(ctlArticulo);
		
		boolean resul=ctlArticulo.buscarArticulo(null);
		//dfsdf
		if(resul){
			myArticulo=ctlArticulo.getArticulo();
			double existencia=0;
			
			//se establece el codigo de barra del articulo
			myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
			
			//conseguir los precios del producto
			myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(myArticulo.getId()));
			
			//JOptionPane.showMessageDialog(view,ConexionStatic.getUsuarioLogin().getConfig(),"Error en existencia",JOptionPane.ERROR_MESSAGE);



			//se verifica que la opcion de unir las cantidades en las articulos repetidos
			boolean unirCantidad=false;
			if(unirCanItem==true)
				unirCantidad=this.buscarArticuloEnFactura(myArticulo);
			else
				unirCantidad=false;

			//se busca en los item ya agregados si ya existe en el metodo solo se agrega la nueva cantidad sino se agrega como nuevo
	if( unirCantidad ==false  )


			//se verfica en la configuracion si se puede facturar sin inventario
			if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario())
			{
				
				//activar para redondiar el precio de venta final
				if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
				
					myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
				}
				
				//JOptionPane.showMessageDialog(view,myArticulo.getTipoArticulo()+" tipo de articulo","Error en existencia",JOptionPane.ERROR_MESSAGE);  
				
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
					}
				}
				
				
				
			
			}else{//si es necesario comprobar los inventarios de articulos
				
				//fdsf
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

							JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidad+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion());


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
									//insumos.get(xx).getCantidad().multiply(new BigDecimal(e))
									
									//se valida la existencia solo para los bienes
									if(insumos.get(xx).getArticulo().getTipoArticulo()==1){
										//se comprueba la existencia del articulo en la farmacia
										if(existencia>0.0 && existencia>=insumos.get(xx).getCantidad().doubleValue()){
											//se establece que esiste 
											exist=true;
											
											
											
										}else if(insumos.get(xx).getArticulo().getTipoArticulo()==2){//fin se la comprobacion de la existencia
											exist=false;
											JOptionPane.showMessageDialog(view,"El insumo "+ insumos.get(xx).getArticulo().getArticulo()+" que pertence al servicio "+myArticulo.getArticulo()+" ,\nno tiene existencia en "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
											break;
										}
									}else{//si es un servicio se pasa por alto la validacion
										exist=true;
									}
								}//sin del for que recore los insumos
								
								//se comprueba que todos los insumos tienen exitencia para agregar el servicio a la factura
								if(exist){
									//se agrega articulo a la tabla
									this.view.getModeloTabla().setArticulo(myArticulo);
									
									//activar para redondiar el precio de venta final
									if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
									
										myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
									}
									
									calcularTotales();
									this.view.getModeloTabla().agregarDetalle();
									
									selectRowInset();	
								}
								
							}else{//sino tiene insumos el servicio se agrega directamente a la factura
									//se agrega articulo a la tabla
									this.view.getModeloTabla().setArticulo(myArticulo);
									
									//activar para redondiar el precio de venta final
									if(ConexionStatic.getUsuarioLogin().getConfig().isPrecioRedondear()){
									
										myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
									}
									
									calcularTotales();
									this.view.getModeloTabla().agregarDetalle();
									
									selectRowInset();
							}
							
							
						}else{
							JOptionPane.showMessageDialog(view,"Solo la caja "+cajaDefecto.getDescripcion() +" puede facturar servicios.","Error en articulo",JOptionPane.ERROR_MESSAGE); 
						}
				}//sin del else para comprobar las existencia de los servicios 
				
				
				
			}//sin de else para facturar con inventario
		}//fin de la busqueda de articulo
		
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
		//myFactura.getCliente()=null;
		
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
		ViewListaClientes viewListaCliente=new ViewListaClientes (null);
		
		CtlClienteBuscar ctlBuscarCliente=new CtlClienteBuscar(viewListaCliente);
		viewListaCliente.pack();
		ctlBuscarCliente.view.getTxtBuscar().setText("");
		ctlBuscarCliente.view.getTxtBuscar().selectAll();
		view.getTxtBuscar().requestFocusInWindow();
		
		boolean resul=ctlBuscarCliente.buscarCliente(null);
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
	
		//se captura el id de la factura por si la factura es temporal
		int idFacturaTemporal=myFactura.getIdFactura();
		
		//se registra la factura	
		boolean resul=facturaDao.registrar(myFactura);
		
		
			
		//se porcesa el resultado de ristrar la factura
		if(resul){
			myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
			
			
			if(ConexionStatic.getNivelFact()){//nivel de facturo 2
				try {
					/*this.view.setVisible(false);
					this.view.dispose();*/
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Saint_Paul.jasper",myFactura.getIdFactura() );
					AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(),6, myFactura.getIdFactura());
					//AbstractJasperReports.showViewer(view);
					AbstractJasperReports.imprimierFactura();
					//JOptionPane.showMessageDialog(view, "Aqui"+myFactura.toString());
					
					//muestra en la pantalla el cambio y lo mantiene permanente
					ViewCambio cambio=new ViewCambio(null);
					cambio.getTxtCambio().setText(myFactura.getCambio().toString());
					cambio.getTxtEfectivo().setText(myFactura.getPago().toString());
					cambio.setVisible(true);
					
					//myFactura=null;
					setEmptyView();
					
					//si la view es de actualizacion al cobrar se cierra la view
					if(this.tipoView==2){//dfsfda
						//myFactura=null;
						this.tipoView=1;
						this.view.getBtnGuardar().setEnabled(true);
						this.view.getBtnActualizar().setEnabled(false);
						
						Factura eliminarTem=new Factura();
						eliminarTem.setIdFactura(idFacturaTemporal);
						
						this.facturaOrdenesDao.eliminar(eliminarTem);
						
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
			}//fin del nivel de factura
			
			setEmptyView();
			
			
		}else{
			JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos", JOptionPane.ERROR_MESSAGE);
			this.view.setVisible(false);
			this.view.dispose();
		}//fin el if donde se guarda la factura
		
	}

public void guardarRemoto(){
	
	
	//se captura el id de la factura por si la factura es temporal
	int idFacturaTemporal=myFactura.getIdFactura();
	
	//se registra la factura	
	//boolean resul=facturaDaoRemote.registrarFactura(myFactura);
	boolean resul=facturaDao.registrar(myFactura);
	
	
		
	//se porcesa el resultado de ristrar la factura
	if(resul){
		//myFactura.setIdFactura(facturaDaoRemote.getIdFacturaGuardada());
		//myFactura.setCodigo(this.facturaDaoRemote.getIdFacturaGuardada());
		myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
		myFactura.setCodigoAlter(this.facturaDao.getIdFacturaGuardada());
		
				try {
					
					AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 1, myFactura.getIdFactura());
					AbstractJasperReports.imprimierFactura();
					
					//muestra en la pantalla el cambio y lo mantiene permanente
					ViewCambio cambio=new ViewCambio(null);
					cambio.getTxtCambio().setText(myFactura.getCambio().toString());
					cambio.getTxtEfectivo().setText(myFactura.getPago().toString());
					cambio.setVisible(true);
					
					
					//si la view es de actualizacion al cobrar se cierra la view
					if(this.tipoView==2){
						//myFactura=null;
						this.tipoView=1;
						this.view.getBtnGuardar().setEnabled(true);
						this.view.getBtnActualizar().setEnabled(false);
						
						Factura eliminarTem=new Factura();
						eliminarTem.setIdFactura(idFacturaTemporal);
						
						this.facturaOrdenesDao.eliminar(eliminarTem);
						
						setEmptyView();
						
						view.getBtnsGuardador().deleteAll();
						
						cargarFacturasPendientes(facturaOrdenesDao.facturasEnProceso());
					}
					//myFactura.
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		
	}else{
		JOptionPane.showMessageDialog(view, "No se guardo la factura intente otra vez", "Error Base de Datos", JOptionPane.ERROR_MESSAGE);
		//this.view.setVisible(false);
		//this.view.dispose();
	}//fin el if donde se guarda la factura
	}


/*
public void guardarRemotoCredito(){
		
	//dfs
	//se registra la factura	
	boolean resul=facturaDaoRemote.registrarFactura(myFactura);
		
	//se porcesa el resultado de ristrar la factura
	if(resul){
		myFactura.setIdFactura(facturaDaoRemote.getIdFacturaGuardada());
		myFactura.setCodigo(this.facturaDaoRemote.getIdFacturaGuardada());
		
				try {
					
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Saint_Paul.jasper",myFactura.getIdFactura() );
					AbstractJasperReports.createReport(conexionRemote.getPoolConexion().getConnection(), 7, myFactura.getIdFactura());
					//AbstractJasperReports.imprimierFactura();
					//AbstractJasperReports.imprimierFactura();
					AbstractJasperReports.showViewer(view);
					//myFactura=null;
					//setEmptyView();
					
					//si la view es de actualizacion al cobrar se cierra la view
					if(this.tipoView==2){
						myFactura=null;
						view.setVisible(false);
					}
					//myFactura.
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		
	}else{
		JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos", JOptionPane.ERROR_MESSAGE);
		this.view.setVisible(false);
		this.view.dispose();
	}//fin el if donde se guarda la factura
	}

	public void guardarLocalCredito(){
		
		
		//se registra la factura	
		boolean resul=facturaDao.registrarFactura(myFactura);
			
		//se porcesa el resultado de ristrar la factura
		if(resul){
			myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
			
			
			if(conexion.getNivelFact()){//nivel de facturo 2
				
				try {
					
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Saint_Paul.jasper",myFactura.getIdFactura() );
					AbstractJasperReports.createReport(conexion.getPoolConexion().getConnection(), 8, myFactura.getIdFactura());
					AbstractJasperReports.showViewer(view);
					//AbstractJasperReports.imprimierFactura();
					//myFactura=null;
					setEmptyView();
					
					//si la view es de actualizacion al cobrar se cierra la view
					if(this.tipoView==2){
						myFactura=null;
						view.setVisible(false);
					}
					//myFactura.
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}//fin del nivel de factura
			
			setEmptyView();
			
			
		}else{
			JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos", JOptionPane.ERROR_MESSAGE);
			this.view.setVisible(false);
			this.view.dispose();
		}//fin el if donde se guarda la factura
		
	}

	*/
	
	
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
		/* seccion de cierre de caja */
		/* seccion de cierre de caja */

		boolean resul = false;

		CierreCajaDao cierreDao = new CierreCajaDao();
		CierreFacturacionDao cierreFacturasDao=new CierreFacturacionDao();

		CierreCaja oldCierre = new CierreCaja();

		// se consiguie el ultimo cierre del usuario
		oldCierre = cierreDao.getCierreUltimoUser();

		if (oldCierre.getEstado() == false) {// si el ultimo cierre esta hecho
											// se registras el nuevo aumentando los valores en uno del anterior cierre
				

			
			// cantidad de efectivo inicial de la caja");
			ViewCuentaEfectivo viewContar = new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar = new CtlContarEfectivo(viewContar);

			//comprobar si se guardo el efectivo inicial
			if (ctlContar.getEstado()) {
				
				CierreCaja newCierre = new CierreCaja();//se crea el nuevo cierre

				newCierre.setEfectivoInicial(ctlContar.getTotal());//se establece el efectivo inicial
				newCierre.setUsuario(ConexionStatic.getUsuarioLogin().getUser());//se establece el usuario
				
				//se recoren las cajas donde el usuario puede facturar para ver los registros de los ultimos cierre
				for(int xx=0;xx<ConexionStatic.getUsuarioLogin().getCajas().size();xx++){
					
					//se consigue los datos de cierre por cada caja
					CierreFacturacion unaC=cierreFacturasDao.buscarPorCajaUsuario(ConexionStatic.getUsuarioLogin().getCajas().get(xx),ConexionStatic.getUsuarioLogin().getUser());
					
					//si ya existe el registro se crea uno nuevo incrementando en uno el del anterior
					if(unaC!=null){
						//JOptionPane.showMessageDialog(view, unaC.toString());
						//se crear los nuevos registros para el cierre nuevo
						CierreFacturacion una=new CierreFacturacion();
						//se estraablece la caja
						una.setCaja(unaC.getCaja());
						
						una.setNoFacturaInicio(unaC.getNoFacturaFinal()+1);// se
																		// estable
																		// la
																		// factura
																		// incial
																		// sumandole
																		// uno
																		// a
																		// la
																		// ultima
																		// factura
																		// realizada
																		// por
																		// el
																		// usuario
						una.setUsuario(ConexionStatic.getUsuarioLogin().getUser());//se establece el usuario
						
						//se agrega el registro al cierre nuevo
						newCierre.getCierreFacturas().add(una);
						
					}else{//sino existe se crea uno comenzando con el numero de factura 1 para esa caja y usuario
						
						//se crear los nuevos registros para el cierre nuevo
						CierreFacturacion una=new CierreFacturacion();
						
						//se estraablece la caja
						una.setCaja(ConexionStatic.getUsuarioLogin().getCajas().get(xx));
						
						una.setNoFacturaInicio(1);// se
												// estable
												// la
												// factura
												// incial
												// en 1
												
						una.setUsuario(ConexionStatic.getUsuarioLogin().getUser());//se establece el usuario
						
						//se agrega el registro al cierre nuevo
						newCierre.getCierreFacturas().add(una);
					}
				}

				
				newCierre.setNoSalidaInicial(oldCierre.getNoSalidaFinal() + 1);// se
																				// estable
																				// la
																				// salida
																				// incial
																				// sumandole
																				// uno
																				// a
																				// la
																				// ultima
																				// salida
																				// realizada
																				// por
																				// el
																				// usuario
				newCierre.setNoEntradaInicial(oldCierre.getNoEntradaFinal() + 1);// se
																				// estable
																				// la
																				// entrada
																				// incial
																				// sumandole
																				// uno
																				// a
																				// la
																				// ultima
																				// salida
																				// realizada
																				// por
																				// el
																				// usuario
				newCierre.setNoCobroInicial(oldCierre.getNoCobroFinal() + 1);// se
																			// estable
																			// la
																			// cobros
																			// incial
																			// sumandole
																			// uno
																			// a
																			// la
																			// ultima
																			// salida
																			// realizada
																			// por
																			// el
																			// usuario
				//JOptionPane.showMessageDialog(view, oldCierre.getNoPagoFinal());
				newCierre.setNoPagoInicial(oldCierre.getNoPagoFinal() + 1);// se
																			// estable
																			// la
																			// pagos
																			// incial
																			// sumandole
																			// uno
																			// a
																			// al
																			// ultimo
																			// pago
																			// realizada
																			// por
																			// el
																			// usuario
				cierreDao.registrarCierre(newCierre);
				resul = true;

				viewContar.dispose();
				viewContar = null;
				ctlContar = null;
			} else {
				resul = false;
			}

		} // fin de nuevo cierre
		else {
			resul = true;
		}

		return resul;

	}


	public void guardarFactura(){
	
		
		//se captura el id de la factura por si la factura es temporal
		int idFacturaTemporal=myFactura.getIdFactura();



		//se registra la factura	
		boolean resul=facturaDao.registrar(myFactura);
		
		
			
		//se porcesa el resultado de ristrar la factura
		if(resul){
			myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());

			//erwqrq
			
				try {
					
					//si la factura es al contado se imprime con un formato especifico
					if(myFactura.getTipoFactura()==1){
						//si la configuracion de la impresion de la factura es tiket o carta
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFactura().equals("tiket")){
						
							AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(),6, myFactura.getIdFactura());
							//AbstractJasperReports.showViewer(view);
							AbstractJasperReports.imprimierFactura();
							//AbstractJasperReports.imprimierFactura();
						}
						
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFactura().equals("carta")){
								AbstractJasperReports.createReportFacturaCarta(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"ORIGINAL");
								//AbstractJasperReports.showViewer(view);
								AbstractJasperReports.imprimierFactura();
								
								AbstractJasperReports.createReportFacturaCarta(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"COPIA");
								AbstractJasperReports.imprimierFactura();
								
						}
					}//fin de la impresion de la factura carta al contado
					
					if(myFactura.getTipoFactura()==2){
						//si la configuracion de la impresion de la factura es tiket o carta
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFacturaCredito().equals("tiket")){
						
							AbstractJasperReports.createReportFacturaTiketCredito(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
							//AbstractJasperReports.showViewer(view);
							//AbstractJasperReports.imprimierFactura();
							AbstractJasperReports.imprimierFactura();
						}
						
						if(ConexionStatic.getUsuarioLogin().getConfig().getFormatoFacturaCredito().equals("carta")){
								AbstractJasperReports.createReportFacturaCartaCredito(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"ORIGINAL");
								//AbstractJasperReports.showViewer(view);
								AbstractJasperReports.imprimierFactura();
								
								AbstractJasperReports.createReportFacturaCartaCredito(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),"COPIA");
								AbstractJasperReports.imprimierFactura();
								
						}
					}
					
					
					/*
					int resul2=JOptionPane.showConfirmDialog(view, "Desea imprimir la orden?");
					if(resul2==0){
						AbstractJasperReports.createReportOrden(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
						//AbstractJasperReports.showViewer(view);
						AbstractJasperReports.imprimierFactura();
						
					}

					 */
					String cambioEfectivo=myFactura.getCambio().toString();
					String pago=myFactura.getPago().toString();


					setEmptyView();



					//muestra en la pantalla el cambio y lo mantiene permanente
					ViewCambio cambio=new ViewCambio(null);
					cambio.getTxtCambio().setText(cambioEfectivo);
					cambio.getTxtEfectivo().setText(pago);
					cambio.setVisible(true);
					
					//myFactura=null;

					
					//si la view es de actualizacion al cobrar se cierra la view
					if(this.tipoView==2){//dfsfda
						//myFactura=null;
						this.tipoView=1;
						this.view.getBtnGuardar().setEnabled(true);
						this.view.getBtnActualizar().setEnabled(false);
						
						Factura eliminarTem=new Factura();
						eliminarTem.setIdFactura(idFacturaTemporal);
						
						this.facturaOrdenesDao.eliminar(eliminarTem);
						
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

			
			setEmptyView();
			
			
		}else{
			JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos", JOptionPane.ERROR_MESSAGE);
			this.view.setVisible(false);
			this.view.dispose();
		}//fin el if donde se guarda la factura
		
	}
	public boolean buscarArticuloEnFactura(Articulo art){
		boolean existe=false;
		for(int x=0;x<view.getModeloTabla().getDetalles().size();x++){
			Articulo artLocal=view.getModeloTabla().getDetalles().get(x).getArticulo();

			if(art.getId()==artLocal.getId()){
				existe=true;

				//String entrada=(String) JOptionPane.showInputDialog(view,"El articula ya esta en la factura. Escriba el cantida a agrgar:",JOptionPane.OK_CANCEL_OPTION, null, null, "9090");
				//se selecciona el item encontrado
				int row = x;
				int col = 1;
				boolean toggle = false;
				boolean extend = false;
				this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
				this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
				this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

				//se pide el ingreso de la cantidad a agregar
				String entrada = (String) JOptionPane.showInputDialog(view,
						"El articula ya esta en la factura. Escriba el cantida a agregar:",
						"Agregar cantidad\n", JOptionPane.OK_CANCEL_OPTION, null,
						null, "1");

				//se verfica en la configuracion si se puede facturar sin inventario
				if(ConexionStatic.getUsuarioLogin().getConfig().isFacturarSinInventario())
				{
					//se registra la cantida en la entrada del usuario
					BigDecimal cantidadSaldoItem=new BigDecimal(entrada);


					BigDecimal newCantidadSaldoItem= new BigDecimal(view.getModeloTabla().getDetalle(x).getCantidad().add(cantidadSaldoItem).doubleValue());

					this.view.getModeloTabla().getDetalle(x).setCantidad(newCantidadSaldoItem);
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
							double cantidad=view.getModeloTabla().getDetalle(x).getCantidad().add(cantidadSaldoItem).doubleValue();

							BigDecimal newCantidadSaldoItem= new BigDecimal(view.getModeloTabla().getDetalle(x).getCantidad().add(cantidadSaldoItem).doubleValue());


							//se establece la nueva cantidad
							this.view.getModeloTabla().getDetalle(x).setCantidad(newCantidadSaldoItem);



							if(existencia>0.0 && cantidad<=existencia) {
								this.calcularTotales();
							}else{
								JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDetartamento().getDescripcion());
								view.getModeloTabla().eliminarDetalle(x);
							}
						}
					}//fin de la comprobacion que la estrada es un numero
				}


			}


		}
		return existe;
	}

	private void selectRowInset(int row){
		int col = 1;
		boolean toggle = false;
		boolean extend = false;
		this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
		this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
		this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

	}

}
