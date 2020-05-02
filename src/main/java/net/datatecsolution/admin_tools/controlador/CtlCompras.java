package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewAgregarCompras;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;
import net.datatecsolution.admin_tools.view.ViewListaProveedor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CtlCompras implements ActionListener,MouseListener,TableModelListener, WindowListener,KeyListener {
	public ViewAgregarCompras view;
	private ArticuloDao myArticuloDao;
	private Articulo myArticulo;
	private Proveedor myProveedor;
	private ProveedorDao myProveedorDao;
	private FacturaCompra myFactura;
	private FacturaCompraDao myFacturaDao;
	private DepartamentoDao deptDao=null;
	private PrecioArticuloDao preciosDao=null;
	
	 
	public CtlCompras(ViewAgregarCompras v){
		//se asigna las variales recibida a las variables locales
		
		this.view=v;
		this.view.conectarControlador(this);
		preciosDao=new PrecioArticuloDao();
		myFactura=new FacturaCompra();
		//se crea las clases para consultar a la BD
		this.myArticuloDao=new ArticuloDao();
		this.myProveedorDao=new ProveedorDao();
		this.myFacturaDao=new FacturaCompraDao();
		
		
		deptDao=new DepartamentoDao();
		// view.getTablaArticulos().getModel().addTableModelListener(this);
		///this.view.getTablaArticulos().getSelectionModel().set
		
		//se llena la tabla de articulos llamando a este metodo 
		view.getModelo().agregarDetalle();
		//this.view.getTxtFechaIngreso().setText(myFacturaDao.getFechaSistema());
		//this.view.setVisible(true);
		cargarComboBox();
		
		Date horaLocal=new Date();
		view.getDateCompra().setDate(horaLocal);
		
		view.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		switch(comando){
		
			case "BUSCARPROVEEDOR2":
				
				ViewListaProveedor vListaProveedores= new ViewListaProveedor(view);
				CtlProveedoresBuscar ctlProveedoresBuscar= new CtlProveedoresBuscar(vListaProveedores);
				
				boolean resultado=ctlProveedoresBuscar.buscarProveedores(view);
				if(resultado){
					myProveedor=ctlProveedoresBuscar.getProveedor();
					view.getTxtIdProveedor().setText(myProveedor.getId()+"");
					this.view.getTxtNombreproveedor().setText(myProveedor.getNombre());
					this.view.gettxtTelefonoProveedor().setText(myProveedor.getTelefono());
					this.myFactura.setProveedor(myProveedor);
				}
				
				vListaProveedores.dispose();
				ctlProveedoresBuscar=null;
				break;
			case "BUSCARPROVEEDOR":
				
				//String stIdProveedor=this.view.getTxtIdProveedor().getText();
				//int inIdProveedor=Integer.parseInt(stIdProveedor);
				myProveedor=myProveedorDao.buscarPorId(Integer.parseInt(this.view.getTxtIdProveedor().getText()));
				if(myProveedor!=null){
					this.view.getTxtNombreproveedor().setText(myProveedor.getNombre());
					this.view.gettxtTelefonoProveedor().setText(myProveedor.getTelefono());
					//JOptionPane.showMessageDialog(view, "Intento Buscar Proveedor");
					this.myFactura.setProveedor(myProveedor);
				}
				else{
					JOptionPane.showMessageDialog(view, "No se encuentro el proveedor");
					this.view.getTxtNombreproveedor().setText("");
					this.view.gettxtTelefonoProveedor().setText("");
				}
				break;
			case "GUARDARCOMPRA":
				//JOptionPane.showMessageDialog(view, "Filas de la tabla: "+view.getModelo().getDetalles().size());
				//se valida la compra
				if(this.view.getDateCompra().isValid()==true){
					JOptionPane.showMessageDialog(view,"Ingrese la fecha de la compra","Error",JOptionPane.ERROR_MESSAGE);
					view.getDateCompra().requestFocusInWindow();
					break;
				}else if(this.myProveedor==null){
					JOptionPane.showMessageDialog(view,"Debe seleccionar un proveedor","Error",JOptionPane.ERROR_MESSAGE);
					view.getTxtIdProveedor().requestFocusInWindow();
					break;
				}else if(view.getModelo().getDetalles().size()==1){
					JOptionPane.showMessageDialog(view,"Debe ingresar articulos en la compra","Error",JOptionPane.ERROR_MESSAGE);
					break;
				}
				
				
				//se crear el formato para la fecha
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//se recoge la fecha de compra de la view
				String date = sdf.format(this.view.getDateCompra().getDate());
				//se estable la feca de compra en el modelo
				myFactura.setFechaCompra(date);
					
				
				
				//se obtine el numero de factura de compra para incoorporarla al modelo
				myFactura.setIdFactura(this.view.getTxtNofactura().getText());
				
				//si la factura es al credito se completa la fecha de vencimiento de la factura 
				if(myFactura.getTipoFactura()==2){
					
					//verifica que se seleciono una fecha sino se rellena una por defecto
					if(view.getDateVencFactura().getDate()!=null){
						
						String date2 = sdf.format(this.view.getDateVencFactura().getDate());
						myFactura.setFechaVencimento(date2);
					}
				}
				
				
				
				
				//se consiguie los detalles de la factura de la view y se psa al modelo
				myFactura.setDetalles(view.getModelo().getDetalles());
				
				//Se establece el departamento seleccionado
				Departamento depart= (Departamento) this.view.getCbxDepart().getSelectedItem();
				myFactura.setDepartamento(depart);
				
				//se manda a guardar la factura y se procesa el resultadoa
				boolean result=this.myFacturaDao.registrar(myFactura);
				if(result){
					//JOptionPane.showMessageDialog(view,"Se guarda la factura");
				
					/*int confirmacion=JOptionPane.showConfirmDialog(view, "Desea agregar la entradas a la compra "+myFactura.getNoCompra()+" ?");
					
					// si se confirma la eliminacion se procede a eliminar
					if(confirmacion==0){
						
						
						this.facturaDaoRemote.registrarFactura(myFactura);
						
					}*/
					try {
						
						AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 2, myFactura.getNoCompra());
						AbstractJasperReports.showViewer(view);
						//AbstractJasperReports.imprimierFactura();
						//AbstractJasperReports.imprimierFactura();
						//myFactura=null;
						//setEmptyView();
						
						
						//myFactura.
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					this.view.setVisible(false);
					
				}else{
					JOptionPane.showMessageDialog(view,"No se guarda la factura");
					this.view.setVisible(false);
				}
				
	
				
				
				break;
			case "CREDITO":
				this.view.getDateVencFactura().setEnabled(true);
				myFactura.setTipoFactura(2);
				break;
			case "CONTADO":
				this.view.getDateVencFactura().setEnabled(false);
				myFactura.setTipoFactura(1);
				//this.view.getDateCompra().cleanup();
				break;
			case "CANCELAR":
				//this.conexion.desconectar();
				this.view.setVisible(false);
			break;
		}
	}
	
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloCbx().setLista(this.deptDao.todos());
		
		
		//se remueve la lista por defecto
		this.view.getCbxDepart().removeAllItems();
	
		this.view.getCbxDepart().setSelectedIndex(0);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
		int colum=e.getColumn();
		int row=e.getFirstRow();
		
		//JOptionPane.showMessageDialog(view, "Se modifico el dato en la celda "+e.getColumn());
		/*
		if (colum == 8) {
            TableModel model = (TableModel) e.getSource();
            String columnName = model.getColumnName(colum);
            Boolean checked = (Boolean) model.getValueAt(row, colum);
            if (checked) {
                System.out.println(columnName + ": " + true);
                JOptionPane.showMessageDialog(view, "Se modifico el dato en la celda "+e.getColumn() + ": " + true);
            } else {
                System.out.println(columnName + ": " + false);
                JOptionPane.showMessageDialog(view, "Se modifico el dato en la celda "+e.getColumn() + ": " + false);
            }
        }*/
		
		
		switch(e.getType()){
			
		
		
			case TableModelEvent.UPDATE:
				
				
				//JOptionPane.showMessageDialog(view, "Se modifico el dato en la celda "+e.getColumn()+", "+e.getFirstRow());
				if(colum==0){
					
					//Se recoge el id de la fila marcada
			        int identificador= (int)this.view.getModelo().getValueAt(row, 0);
			        
			        myArticulo=this.view.getModelo().getDetalle(row).getArticulo();
					//myArticulo=this.myArticuloDao.buscarArticulo(identificador);
					
					//se ingreso un codigo de barra y si el articulo en la bd 
			        if(myArticulo.getId()==-2){
						String cod=this.view.getModelo().getDetalle(row).getArticulo().getCodBarra().get(0).getCodigoBarra();
						this.myArticulo=this.myArticuloDao.buscarArticuloBarraCod(cod);
						
					}else{//sino se ingreso un codigo de barra se busca por id de articulo
						this.myArticulo=this.myArticuloDao.buscarArticulo(identificador);
					}
					
			        
					if(myArticulo!=null){
						
						//conseguir los precios del producto
						myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo.getId()));
						
						this.view.getModelo().setArticulo(myArticulo, row);

						//this.view.getModelo().getDetalle(row).setCantidad(1);
						boolean toggle = false;
						boolean extend = false;
						this.view.getTablaArticulos().requestFocus();
							
						this.view.getTablaArticulos().changeSelection(row,colum+2, toggle, extend);
							
						//calcularTotales();
						
						//se agrega otra fila en la tabla
						//this.view.getModelo().agregarDetalle();
					}else{
						JOptionPane.showMessageDialog(view, "No se encuentra el articulo");
						
						//sino se encuentra se estable un id de -1 para que sea eliminado el articulo en la tabla
						this.view.getModelo().getDetalle(row).getArticulo().setId(-1);
						
						//se agrega la nueva fila de la tabla
						//this.view.getModelo().agregarDetalle();
						
						// se vuelve a calcular los totales
						//calcularTotales();
					}
					
					
				}
				if(colum==2){
					calcularTotales();
					boolean toggle = false;
					boolean extend = false;
					this.view.getTablaArticulos().requestFocus();
						
					this.view.getTablaArticulos().changeSelection(row,colum+1, toggle, extend);
					
					//se agrega la nueva fila de la tabla
					//this.view.getModelo().agregarDetalle();
					//calcularTotales();
				}
				if(colum==3){
					calcularTotales();
					boolean toggle = false;
					boolean extend = false;
					this.view.getTablaArticulos().requestFocus();
					//se agrega la nueva fila de la tabla
					//this.view.getModelo().agregarDetalle();
						
					this.view.getTablaArticulos().changeSelection(row,colum+3, toggle, extend);	
					//calcularTotales();
				}
				if(colum==4){
					BigDecimal newPrecioU;
					//view.getModelo().getDetalle(row);
					//siempre calcula el precio unidad siempre y cuando la cantidad y el subtotal sea mayor que cero
					if( view.getModelo().getDetalle(row).getCantidad().doubleValue()>0 && view.getModelo().getDetalle(row).getSubTotal().doubleValue()>0){
						 newPrecioU=view.getModelo().getDetalle(row).getSubTotal().divide(view.getModelo().getDetalle(row).getCantidad(),4,BigDecimal.ROUND_HALF_EVEN);
						 view.getModelo().getDetalle(row).setPrecioCompra(newPrecioU);
						 calcularTotales();
					}
				}
				if(colum==6){
					
					//JOptionPane.showMessageDialog(view,"No se guarda la factura");
					
					calcularTotales();
					boolean toggle = false;
					boolean extend = false;
					this.view.getTablaArticulos().requestFocus();
					//se agrega la nueva fila de la tabla
					//this.view.getModelo().agregarDetalle();
						
					this.view.getTablaArticulos().changeSelection(row+1,0, toggle, extend);	
				}
				
				
				if(colum==9){
					calcularTotales();
				}
				
				
				//se agrega la nueva fila de la tabla
				//this.view.getModelo().agregarDetalle();
			break;
		}
		
		
		
	}
	public void agregarNuevoDetalle(){
		for(int x=0;x<50;x++){
			DetalleFacturaProveedor uno =new DetalleFacturaProveedor();
			this.view.getModelo().agregarDetalle(uno);
		}
	}
	
	
	public void calcularTotales(){
		
		//se establecen los totales en cero
		this.myFactura.resetTotales();
		
		//se recoren los detalles de la factura
		for(int x=0; x<this.view.getModelo().getDetalles().size();x++){
			
			//se obtiene cada detalle por separado de la factura
			DetalleFacturaProveedor detalle=this.view.getModelo().getDetalle(x);
			
			
			if(detalle.getArticulo().getId()!=-1)//si el detalle es valido
				//if(detalle.getCantidad()!=0 && detalle.getPrecioCompra()!=0)
				if(detalle.getCantidad().doubleValue()!=0 && detalle.getPrecioCompra().doubleValue()!=0){
					
					//se obtien la cantidad y el precio de compra por unidad
					BigDecimal cantidad=detalle.getCantidad();
					BigDecimal precioCompra=detalle.getPrecioCompra();
					
					
					
					//si el item tiene el impuesto incluido 
					if(detalle.isIvaIncludo()){
								//se calcula el total del item
								BigDecimal totalItem=cantidad.multiply(precioCompra);
								
								
								/*int desc=detalle.getDescuento();
							
								if(desc==1)
								{
									BigDecimal des=totalItem.multiply(new BigDecimal(0.05));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);
									
								}else if(desc==2){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.10));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==3){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.15));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==4){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.20));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==5){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.25));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==6){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.30));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==7){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.35));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==8){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.40));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==9){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.45));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}else if(desc==10){
									BigDecimal des=totalItem.multiply(new BigDecimal(0.50));
									detalle.setDescuentoItem(des);
									totalItem=totalItem.subtract(des);	
								}
								
								*/
								
								//se obtiene el impuesto del articulo 
								//((Double.parseDouble(detalle.getArticulo().getImpuestoObj().getPorcentaje())  )/100)+1;
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
								myFactura.setTotal(totalItem);
								
								if(porcentaImpuesto.intValue()==0){
									myFactura.addSubTotalExcento(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								}else
									if(porcentaImpuesto.intValue()==15){
										myFactura.addTotalImpuesto15(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
										myFactura.addSubTotal15(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
									}else
										if(porcentaImpuesto.intValue()==18){
											myFactura.addTotalImpuesto18(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
											myFactura.addSubTotal18(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
										}
								
								//se agrega el total del impuesto 15%
								//myFactura.addTotalImpuesto15(impuestoItem);
								//se agrega el total de la venta sin el iva
								myFactura.addSubTotal(totalsiniva);
								
								//myFactura.setTotalDescuento(detalle.getDescuentoItem());
								
								//detalle.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								detalle.setImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								
								detalle.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								
								detalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								
								//se establece el total e impuesto en el vista
								this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								this.view.getTxtTotalimpuesto().setText(""+myFactura.getTotalImpuesto15().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								this.view.getTxtTotalImpusto18().setText(""+myFactura.getTotalImpuesto18().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								//this.view.getTxtImpuesto().setText(""+myFactura.getTotalImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								//this.view.getTxtDescuento().setText(""+myFactura.getTotalDescuento().setScale(2, BigDecimal.ROUND_HALF_EVEN));
								
								
								//se agrega la nueva fila de la tabla
								this.view.getModelo().agregarDetalle();
								
							
								
								//this.view.getModelo().fireTableDataChanged();
					
					
					}else{//para calcular los totales del item cuando no esta incluido el iva
						
						
						BigDecimal subTotalItem=cantidad.multiply(precioCompra);
						
						//se obtiene el impuesto del articulo 
						//((Double.parseDouble(detalle.getArticulo().getImpuestoObj().getPorcentaje())  )/100)+1;
						BigDecimal porcentaImpuesto =new BigDecimal(detalle.getArticulo().getImpuestoObj().getPorcentaje());
						BigDecimal porImpuesto=new BigDecimal(0);
						porImpuesto=porcentaImpuesto.divide(new BigDecimal(100));
						
						
						//se calcula el total de impuesto del item
						BigDecimal impuestoItem=subTotalItem.multiply(porImpuesto);//totalItem.subtract(totalsiniva);
						
						
						
						BigDecimal totalItem=subTotalItem.add(impuestoItem);
						
						//se estable el total y impuesto en el modelo
						myFactura.setTotal(totalItem);
						
						if(porcentaImpuesto.intValue()==0){
							myFactura.addSubTotalExcento(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						}else
							if(porcentaImpuesto.intValue()==15){
								myFactura.addTotalImpuesto15(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								myFactura.addSubTotal15(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
							}else
								if(porcentaImpuesto.intValue()==18){
									myFactura.addTotalImpuesto18(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
									myFactura.addSubTotal18(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
								}
						
						myFactura.addSubTotal(subTotalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						detalle.setImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						detalle.setSubTotal(subTotalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						detalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						//se establece el total e impuesto en el vista
						this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						this.view.getTxtTotalimpuesto().setText(""+myFactura.getTotalImpuesto15().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						this.view.getTxtTotalImpusto18().setText(""+myFactura.getTotalImpuesto18().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						//this.view.getTxtImpuesto().setText(""+myFactura.getTotalImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						//this.view.getTxtDescuento().setText(""+myFactura.getTotalDescuento().setScale(2, BigDecimal.ROUND_HALF_EVEN));
						
						
						//se agrega la nueva fila de la tabla
						this.view.getModelo().agregarDetalle();
						
					}
				}//fin del if
			
		}//fin del for
		}
	
	/*public void calcularTotal(DetalleFacturaProveedor detalle){
		
		if(detalle.getCantidad()!=0 && detalle.getPrecioCompra()!=0){
			//se obtien la cantidad y el precio de compra por unidad
			double cantidad=detalle.getCantidad();
			double precioCompra=detalle.getPrecioCompra();
			
			//se obtiene el impuesto del articulo 
			double porcentaImpuesto =((Double.parseDouble(detalle.getArticulo().getImpuestoObj().getPorcentaje())  )/100)+1;
			
			//se calcula el total del item
			double totalItem=cantidad*precioCompra;
			//se calcula el total sin  el impuesto;
			double totalsiniva=(totalItem)/(porcentaImpuesto);
			
			//se redondea el total
			BigDecimal total=new BigDecimal(totalItem);
			BigDecimal total2= total.setScale(2,BigDecimal.ROUND_HALF_EVEN);
			totalItem=total2.doubleValue();
			
			//se calcula el total de impuesto del item
			double impuestoItem=totalItem-totalsiniva;
			
			//se redondea el total del impuesto del item
			BigDecimal impuesto=new BigDecimal(impuestoItem);
			BigDecimal impuesto2=impuesto.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			
			//se redondea el total del impuesto del item
			BigDecimal subt=new BigDecimal(totalsiniva);
			BigDecimal subtTotal=subt.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			totalsiniva=subtTotal.doubleValue();
			
			//se estable el total y impuesto en el modelo
			myFactura.setTotal(totalItem);
			myFactura.setTotalImpuesto(impuesto2.doubleValue());
			myFactura.setSubTotal(totalsiniva);
			
			//se establece el total e impuesto en el vista
			this.view.getTxtTotal().setText(""+myFactura.getTotal());
			this.view.getTxtTotalimpuesto().setText(""+myFactura.getTotalImpuesto());
			this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal());
			
			//se establece en la y el impuesto en el item de la vista
			detalle.setImpuesto(impuesto2.doubleValue());
			detalle.setTotal(totalItem);
			myFactura.getDetalles().add(detalle);
			
		
			
			//this.view.getModelo().fireTableDataChanged();
		}
	}*/

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
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
	public void cargarFacturaview(){
		
		//this.view.getTxtIdcliente().setText(""+myFactura.getCliente().getId());;
		//this.view.getTxtNombrecliente().setText(myFactura.getCliente().getNombre());
		
		//se establece el total e impuesto en el vista
		this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtTotalimpuesto().setText(""+myFactura.getTotalImpuesto15().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		this.view.getModelo().setDetalles(myFactura.getDetalles());
	}
	
	public void viewFactura(FacturaCompra f) {
		// TODO Auto-generated method stub
		this.myFactura=f;
		cargarFacturaview();
		//this.view.getPanelAcciones().setVisible(false);
		this.view.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		int filaPulsada = this.view.getTablaArticulos().getSelectedRow();
		if(e.getKeyCode()==KeyEvent.VK_F1){
			buscarArticulo();
		}else
			if(e.getKeyCode()==KeyEvent.VK_F2){
				//buscarCliente();
			}else
				if(e.getKeyCode()==KeyEvent.VK_F3){
					//cobrar();
				}else
					if(e.getKeyCode()==KeyEvent.VK_F4){
						//guardar();
					}else
						if(e.getKeyCode()==KeyEvent.VK_F5){
							//salir();
						}else
							if(e.getKeyCode()==KeyEvent.VK_DELETE){
								 
								 if(filaPulsada>=0){
									 this.view.getModelo().eliminarDetalle(filaPulsada);
									 this.calcularTotales();
								 }
							}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void buscarArticulo(){
		
		//se llama el metodo que mostrar la ventana para buscar el articulo
		ViewListaArticulo viewListaArticulo=new ViewListaArticulo(view);
		CtlArticuloBuscar ctlArticulo=new CtlArticuloBuscar(viewListaArticulo);
		
		viewListaArticulo.pack();
		ctlArticulo.view.getTxtBuscar().setText("");
		ctlArticulo.view.getTxtBuscar().selectAll();
		//ctlArticulo.view.getTxtBuscar().requestFocus(true);
		//ctlArticulo.view.getTxtBuscar().selectAll();
		//view.getTxtBuscar().requestFocusInWindow();
		viewListaArticulo.conectarControladorBuscar(ctlArticulo);
		
		boolean resul=ctlArticulo.buscarArticulo(view);
		//JOptionPane.showMessageDialog(view, myArticulo1);
		//se comprueba si le regreso un articulo valido
		if(resul){
			
			
			
			Articulo myArticulo1=ctlArticulo.getArticulo();
			this.view.getModelo().setArticulo(myArticulo1);
			//this.view.getModelo().getDetalle(row).setCantidad(1);
			
			//conseguir los precios del producto
			myArticulo1.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo1.getId()));
			
			//calcularTotal(this.view.getModeloTabla().getDetalle(row));
			calcularTotales();
			this.view.getModelo().agregarDetalle();
			
			selectRowInset();
		}
		
		myArticulo=null;
		viewListaArticulo.dispose();
		ctlArticulo=null;
		
	}
	
	private void selectRowInset(){
		/*<<<<<<<<<<<<<<<selecionar la ultima fila creada>>>>>>>>>>>>>>>*/
		int row =  this.view.getTablaArticulos().getRowCount () - 2;
		Rectangle rect = this.view.getTablaArticulos().getCellRect(row, 0, true);
		this.view.getTablaArticulos().scrollRectToVisible(rect);
		this.view.getTablaArticulos().clearSelection();
		this.view.getTablaArticulos().setRowSelectionInterval(row, row);
		view.getModelo().fireTableDataChanged();
		//DmtFacturaProveedores modelo = (DmtFacturaProveedores)this.view.getTablaArticulos().getModel();
		//modelo.fireTableDataChanged();
	}

	
}
