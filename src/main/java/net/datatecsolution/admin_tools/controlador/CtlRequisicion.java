package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;
import net.datatecsolution.admin_tools.view.ViewRequisicion;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;

public class CtlRequisicion implements ActionListener, MouseListener, TableModelListener, KeyListener, ItemListener   {
	private ViewRequisicion view=null;
	private Requisicion myRequisicion=null;
	private Articulo myArticulo=null;
	private ArticuloDao myArticuloDao=null;
	private int filaPulsada=0;
	private KardexDao myKardexDao;
	private DepartamentoDao deptDao=null;
	private RequisicionDao myRequiDao=null;
	private PrecioArticuloDao preciosDao=null;
	
	public CtlRequisicion(ViewRequisicion v){
		view=v;

		myRequisicion=new Requisicion();
		preciosDao=new PrecioArticuloDao();
		
		
		myArticuloDao=new ArticuloDao();
		view.conectarContralador(this);
		myKardexDao=new KardexDao();
		deptDao=new DepartamentoDao();
		
		myRequiDao=new RequisicionDao();
		
		view.getTxtFecha().setText(myRequiDao.getFechaSistema());
		
		cargarComboBox();
		view.getModelo().agregarDetalle();


		
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		//Recoger qu� fila se ha pulsadao en la tabla
				filaPulsada = this.view.getTablaArticulos().getSelectedRow();
				
					switch(e.getKeyCode()){
							
							case KeyEvent.VK_F1:
									buscarArticulo();
								break;
							case KeyEvent.VK_DELETE:
								if(filaPulsada>=0){
									 this.view.getModelo().eliminarDetalle(filaPulsada);
									 this.calcularTotales();
								 }
								break;
					}		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

		//Recoger qu� fila se ha pulsadao en la tabla
		filaPulsada = this.view.getTablaArticulos().getSelectedRow();
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
		
		if(caracter=='+'){
			if(filaPulsada>=0){
				//JOptionPane.showMessageDialog(view,e.getKeyChar()+" FIla:"+filaPulsada);
				this.view.getModelo().masCantidad(filaPulsada);
				//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
				this.calcularTotales();
			}
		}
		if(caracter=='-'){
			if(filaPulsada>=0){
				//JOptionPane.showMessageDialog(view,e.getKeyChar()+" FIla:"+filaPulsada);
				this.view.getModelo().restarCantidad(filaPulsada);
				//JOptionPane.showMessageDialog(view,view.getModeloTabla().getDetalle(filaPulsada).getCantidad());
				this.calcularTotales();
			}
		}
		
	}

	private boolean esValido(Character caracter)
    {
        char c = caracter.charValue();
        return Character.isLetter(c) //si es letra
                || c == ' ' //o un espacio
                || c == 8 //o backspace
                || (Character.isDigit(c));
    }
	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
		int colum=e.getColumn();
		int row=e.getFirstRow();
		
		
		switch(e.getType()){
		
			case TableModelEvent.UPDATE:
				
				//Se establece el departamento seleccionado
				Departamento depart= (Departamento) this.view.getCbxDepatOrigen().getSelectedItem();
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
					
			        //si el articulo se encontro se procesa
					if(myArticulo!=null){
						
						
						
						//se comprueba que exista el producto en el inventario
						boolean resul=myKardexDao.comprobarKardex(myArticulo.getId(), depart.getId());
						if(resul){
							//se consigue el articulo en la bd
							this.view.getModelo().setArticulo(myArticulo, row);
							
							//se consiguie el precio de costo 
							this.view.getModelo().setPricioCompra(myKardexDao.buscarKardexPrecio(myArticulo.getId(), depart.getId()), row);
							//this.view.getModelo().getDetalle(row).setCantidad(1);
							boolean toggle = false;
							boolean extend = false;
							this.view.getTablaArticulos().requestFocus();
								
							this.view.getTablaArticulos().changeSelection(row,colum+2, toggle, extend);
								
							//
							
							//se agrega otra fila en la tabla
							this.view.getModelo().agregarDetalle();
							
							calcularTotales();
						}else{
							JOptionPane.showMessageDialog(view, "El articulo no se encuentra en la "+depart.getDescripcion());  
						}
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

					//Se recoge el id de la fila marcada
					int identificador= (int)this.view.getModelo().getValueAt(row, 0);

					myArticulo=this.view.getModelo().getDetalle(row).getArticulo();

					//JOptionPane.showMessageDialog(view,"Cantidad item"+this.view.getModelo().getDetalle(row).getCantidad().doubleValue());

					BigDecimal cantidadSaldoKardex=myKardexDao.buscarExistencia(myArticulo.getId(), depart.getId());



					double buscarEnRequisicionCantidad=view.getModelo().buscarCantidadPorArticulo(myArticulo);


					BigDecimal diferencia=cantidadSaldoKardex.subtract(new BigDecimal(buscarEnRequisicionCantidad));
					//JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "+cantidadSaldoKardex.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()+" del articulo en la bodega "+depart.getDescripcion());

					if(diferencia.doubleValue()>=0.00){
						calcularTotales();
					}else{
						JOptionPane.showMessageDialog(view, "El cantidad de "+ buscarEnRequisicionCantidad+" requerida del articulo no se encuentra en la "+depart.getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
						view.getModelo().eliminarDetalle(row);
						calcularTotales();
					}
				}
				
			break;
		}
		
		
		
	}
	
public void calcularTotales(){
		
		//se establecen los totales en cero
		this.myRequisicion.resetTotales();
		
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
					
					//se calcula el total del item
					BigDecimal totalItem=cantidad.multiply(precioCompra);
					
					
					//se estable el total y impuesto en el modelo
					myRequisicion.setTotal(totalItem);
					
					detalle.setSubTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					//se establece el total e impuesto en el vista
					this.view.getTxtTotal().setText(""+myRequisicion.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
					
					//se agrega la nueva fila de la tabla
					this.view.getModelo().agregarDetalle();
					
				
					
					//this.view.getModelo().fireTableDataChanged();
				}//fin del if
			
		}//fin del for
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
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch (comando){
		case "BUSCARARTICULO2":
			//se comprueba que se ingreso un codigo de barra o que el articulo este nulo para poder buscar
			if(view.getTxtBuscar().getText().trim().length()!=0 || myArticulo==null){
				
					
					String busca=this.view.getTxtBuscar().getText();
						
						this.myArticulo=this.myArticuloDao.buscarArticuloBarraCod(busca);
						
						if(myArticulo!=null){
							
							
								//Se establece el departamento seleccionado
								Departamento depart= (Departamento) this.view.getCbxDepatOrigen().getSelectedItem();
								
								//se comprueba que exista el producto en el inventario
								double existencia=myArticuloDao.getExistencia(myArticulo.getId(), depart.getId());
								//boolean resul=myKardex.comprobarKardex(myArticulo.getId(), depart.getId());

								//se estable la pasar de una bodega a otra por defecto es uno
								double cantidad=1;

								double buscarEnRequisicionCantidad=view.getModelo().buscarCantidadPorArticulo(myArticulo);
								if(buscarEnRequisicionCantidad>0){
									cantidad=cantidad+buscarEnRequisicionCantidad;
								}
								
								if(existencia>0.0 && cantidad<=existencia){
									//se agrega un item a la lista con el articulo seleccionado o buscado
									this.view.getModelo().setArticulo(myArticulo);
									
									//se agrega un item vacio  a la lista
									this.view.getModelo().agregarDetalle();

									view.getTxtBuscar().setText("");
									
									
									int row =  this.view.getTablaArticulos().getRowCount () - 2;
							
									this.view.getModelo().setPricioCompra(myKardexDao.buscarKardexPrecio(myArticulo.getId(), depart.getId()), row);
									calcularTotales();
									selectRowInset();
								}else{
									JOptionPane.showMessageDialog(view, "El cantidad de "+ cantidad+" requerida del articulo no se encuentra en la "+depart.getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
									//se agrega una fila en blanco
									this.view.getModelo().agregarDetalle();
								}
							
						}else{
							JOptionPane.showMessageDialog(view, "No se encontro el articulo");
							view.getTxtBuscar().setText("");
							view.getTxtBuscar().requestFocusInWindow();
						}
						
					//}
				}else//si el articulo esta nulo se agrega el ultimo articulo creado
				{
					//conseguir los precios del producto
					myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticulo(myArticulo.getId()));
					this.view.getModelo().setArticulo(myArticulo);
					//this.view.getModelo().getDetalle(row).setCantidad(1);
					
					//calcularTotal(this.view.getModeloTabla().getDetalle(row));
					calcularTotales();
					this.view.getModelo().agregarDetalle();
					view.getTxtBuscar().setText("");
					selectRowInset();
					
				}
			break;
			
		case "BUSCARARTICULO":
			this.buscarArticulo();
		break;
		
		case "CERRAR":
			this.salir();
			
			break;
		case "GUARDAR":
			this.guardar();
			break;
		}
		
	}
	private void guardar(){
		
		if(view.getModelo().getRowCount()>1){

			if(view.getCbxDepartDestino().getSelectedIndex()!=0) {

				setRequisicion();
				boolean resul = myRequiDao.registrar(myRequisicion);
				if (resul) {
					try {
						//this.view.setVisible(false);
						//this.view.dispose();
						AbstractJasperReports.createReportRequisicion(ConexionStatic.getPoolConexion().getConnection(), "requisiciones_carta.jasper", myRequisicion.getNoRequisicion());
						//AbstractJasperReports.showViewer();
						AbstractJasperReports.showViewer(view);

					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					view.setVisible(false);
				} else {
					JOptionPane.showMessageDialog(view, "No se guardo correctamente.");
				}
			}else {
				JOptionPane.showMessageDialog(view, "Debe seleccionar una bodega de destino.","Error de validacion",JOptionPane.ERROR_MESSAGE);
			}

		}else
		{
			JOptionPane.showMessageDialog(view, "Se debe tener articulos para crear la requision. Agrege Articulos primero.");
		}

	}
	private void setRequisicion() {
		
		Departamento depart= (Departamento) this.view.getCbxDepatOrigen().getSelectedItem();
		Departamento departDestino= (Departamento) this.view.getCbxModeloDestino().getSelectedItem();
		
		this.myRequisicion.setDepartamentoOrigen(depart);
		this.myRequisicion.setDepartamentoDestino(departDestino);
		
		String total=view.getTxtTotal().getText();
		this.myRequisicion.setTotal(new BigDecimal(total));
		this.myRequisicion.setDetalles(view.getModelo().getDetalles());

		
	}

	private void salir(){
		//facturaDao.desconectarBD();
		//this.clienteDao.desconectarBD();
		//this.myArticuloDao.desconectarBD();
		//this.myFactura.setIdFactura(-1);
		this.view.setVisible(false);
		
		
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
		
		boolean result=ctlArticulo.buscarArticulo(view);
		

		//se comprueba si le regreso un articulo valido
		if(result){
			Articulo myArticulo1=ctlArticulo.getArticulo();
			//Se establece el departamento seleccionado
			Departamento depart= (Departamento) this.view.getCbxDepatOrigen().getSelectedItem();
			
			//se comprueba que exista el producto en el inventario
			double existencia=myArticuloDao.getExistencia(myArticulo1.getId(), depart.getId());
			//boolean resul=myKardex.comprobarKardex(myArticulo.getId(), depart.getId());

			//se estable la pasar de una bodega a otra por defecto es uno
			double cantidad=1;

			double buscarEnRequisicionCantidad=view.getModelo().buscarCantidadPorArticulo(myArticulo1);
			if(buscarEnRequisicionCantidad>0){
				cantidad=cantidad+buscarEnRequisicionCantidad;
			}
			
			if(existencia>0.0 && cantidad<=existencia){
				this.view.getModelo().setArticulo(myArticulo1);
				
				//se agrega una fila en blanco
				this.view.getModelo().agregarDetalle();
				view.getTxtBuscar().setText("");
				
				
				int row =  this.view.getTablaArticulos().getRowCount () - 2;
				//this.view.getModelo().getDetalle(row).setCantidad(1);
				//JOptionPane.showMessageDialog(view, row);
				this.view.getModelo().setPricioCompra(myKardexDao.buscarKardexPrecio(myArticulo1.getId(), depart.getId()), row);
				
				calcularTotales();
				selectRowInset();
			}else{
				JOptionPane.showMessageDialog(view, "El cantidad de "+ cantidad+" requerida del articulo no se encuentra en la "+depart.getDescripcion(),"Error en existencia",JOptionPane.ERROR_MESSAGE);
				//se agrega una fila en blanco
				this.view.getModelo().agregarDetalle();
			}
			
			selectRowInset();
		}
		
		myArticulo=null;
		viewListaArticulo.dispose();
		ctlArticulo=null;
		
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getCbxModeloOrigen().setLista(this.deptDao.todos());
		
		
		
		//se remueve la lista por defecto
		
		this.view.getCbxDepatOrigen().removeAllItems();
		
		
		this.view.getCbxDepatOrigen().setSelectedIndex(0);
		
	
		
		
	}
private void selectRowInset(){
		
		int row = this.view.getTablaArticulos().getRowCount () - 2;
	    int col = 1;
	    boolean toggle = false;
	    boolean extend = false;
	    this.view.getTablaArticulos().changeSelection(row, 0, toggle, extend);
	    this.view.getTablaArticulos().changeSelection(row, col, toggle, extend);
	    this.view.getTablaArticulos().addColumnSelectionInterval(0, 6);
		
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getStateChange() == ItemEvent.SELECTED) {
	          //Object item = e.getItem();
	          // do something with object
			//se remueve la lista por defecto
			//this.view.getCbxDepatOrigen().removeAllItems();
			
			//Se establece el departamento seleccionado
			view.getModelo().setEmptyDetalles();
			
			Departamento depart= (Departamento) this.view.getCbxDepatOrigen().getSelectedItem();

			//se extra de la base de datos
			Vector<Departamento> departBD=this.deptDao.todosExecto(depart.getId());
			//se crea el vector para mostrar
			Vector<Departamento> departamentos=new Vector<Departamento>();
			//se agregar un selecion por defecto
			Departamento unDept=new Departamento();
			unDept.setId(0);
			unDept.setDescripcion("Seleccione una opcion");
			departamentos.add(unDept);

			for (Departamento dep:departBD
			) {
				departamentos.add(dep);
			}

			//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
			this.view.getCbxModeloDestino().setLista(departamentos);
			//se remueve la lista por defecto
			this.view.getCbxDepartDestino().removeAllItems();
			this.view.getCbxDepartDestino().setSelectedIndex(0);
			view.getModelo().agregarDetalle();
			
			
	       }
	}

	public boolean agregarRequisicion() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return false;
	}

	public Requisicion getRequisicionGuardado() {
		// TODO Auto-generated method stub
		return this.myRequisicion;
	}

}
