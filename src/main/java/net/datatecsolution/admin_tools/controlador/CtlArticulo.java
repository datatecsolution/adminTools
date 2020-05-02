package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.modelo.dao.ImpuestoDao;
import net.datatecsolution.admin_tools.modelo.dao.InsumoDao;
import net.datatecsolution.admin_tools.modelo.dao.PrecioArticuloDao;
import net.datatecsolution.admin_tools.view.ViewCrearArticulo;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;
import net.datatecsolution.admin_tools.view.ViewListaCategorias;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;

public class CtlArticulo extends MouseAdapter implements ActionListener,KeyListener,  WindowListener, TableModelListener {
	
	
	public ViewCrearArticulo view;
	private ArticuloDao myArticuloDao;
	private Articulo myArticulo=new Articulo();
	private boolean resultaOperacion=false;
	private ImpuestoDao myImpuestoDao;
	private InsumoDao insumoDao;
	private PrecioArticuloDao precioDao=null;

	
	public CtlArticulo(ViewCrearArticulo view, ArticuloDao a){
		

		
		this.view=view;
		this.myArticuloDao=a;
		precioDao= new PrecioArticuloDao();
		insumoDao=new InsumoDao();
		
		cargarTabla(precioDao.getTipoPrecios());
		cargarComboBox();
		view.getBtnAgregarInsumo().setEnabled(false);
		view.gettInsumos().setEnabled(false);
	}
	
	
	
	public void cargarTabla(List<PrecioArticulo> precios){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModeloPrecio().limpiar();
		if(precios!=null)
			for(int c=0;c<precios.size();c++){
				this.view.getModeloPrecio().agregarPrecio(precios.get(c));
			}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch (comando){
		
		case "ELIMINAR_INSUMO":
			int filaPulsada = this.view.gettInsumos().getSelectedRow();
		        
	        //si seleccion una fila
	        if(filaPulsada>=0){
	        	view.getModeloInsumos().eliminarInsumo(filaPulsada);
	        	calcularTotalInsumo();
	        	
	        }
			break;
		
		case "AGREGAR_INSUMO":
			//se llama el metodo que mostrar la ventana para buscar el articulo
			ViewListaArticulo viewListaArticulo=new ViewListaArticulo(null);
			CtlArticuloBuscar ctlArticulo=new CtlArticuloBuscar(viewListaArticulo);
			
			viewListaArticulo.pack();
			ctlArticulo.view.getTxtBuscar().setText("");
			ctlArticulo.view.getTxtBuscar().selectAll();
			viewListaArticulo.conectarControladorBuscar(ctlArticulo);
			
			boolean resuls=ctlArticulo.buscarArticulo(null);
			
			if(resuls){
				Articulo articuloBuscado=ctlArticulo.getArticulo();
				Insumo isum=new Insumo();
				isum.setArticulo(articuloBuscado);
				
				view.getModeloInsumos().agregarInsumo(isum);
				calcularTotalInsumo();
				
			}
			break;
		
		case "CAMBIOCOMBOBOX":
			
			int x=this.view.getCbxTipo().getSelectedIndex();
			if(x==0){//seleciono un bien
				view.getBtnAgregarInsumo().setEnabled(false);
				view.getTpInsumos().setEnabledAt(1,false);
				view.gettInsumos().setEnabled(false);
				view.getModeloInsumos().limpiarInsumo();
			}else{//selecciono un servicio
				view.getBtnAgregarInsumo().setEnabled(true);
				view.getTpInsumos().setEnabledAt(1,true);
				view.gettInsumos().setEnabled(true);
			}
				
			break;
		
		case "BUSCAR":
				ViewListaCategorias viewListaM=new ViewListaCategorias(view);
				CtlCategoriaBuscar ctl=new CtlCategoriaBuscar(viewListaM);
				viewListaM.conectarControladorBusqueda(ctl);
				//se crea una marca y se llena con la busqueda que selecciona el usuario
				Categoria myMarca=ctl.buscarMarca();
				
			//se compara si el usuario selecciono un marca
				if(myMarca.getDescripcion()!=null && myMarca.getId()!=0){
					//se pasa la marca buscada y selecciona al nuevo articulo
					myArticulo.setMarcaObj(myMarca);
					
					//se muestra el nombre de la marca en la caja de texto
					view.getTxtMarca().setText(myMarca.getDescripcion());
				
				}else{
					JOptionPane.showMessageDialog(this.view,"No seleciono una Categoria");
				}
			break;
		case "GUARDAR":
			//filtrar los codigos de barra del articulo
			if(this.view.getModeloCodBarra().getSize()==0){
				int resul=JOptionPane.showConfirmDialog(view, "Desea guardar un articulos sin condigos de barra?");
				if(resul==0){
					guardarArticulo();
					this.view.dispose();
				}
			}else{
				guardarArticulo();
				this.view.dispose();
			}
			
			
			break;
		case "NUEVOCODIGO":
			CodBarra newCodigo=new CodBarra();
			newCodigo.setCodigoBarra(this.view.getTxtCodigo().getText());
			newCodigo.setCodArticulo(0);
			this.view.getModeloCodBarra().addCodBarra(newCodigo);
			this.view.getTxtCodigo().setText("");
			break;
		case "ELIMINARCODIGO":
				//se obtiene el index del codigo selecionado
				int indexCodigoSelecionado=this.view.getListCodigos().getSelectedIndex();
				
				//con el index del codigo seleccionado se obtiene el objeto codigo selecionado			
				CodBarra cod= this.view.getModeloCodBarra().getCodBarra(indexCodigoSelecionado);
				
				//se pregunta si en verdad se quiere borrar el codigo de bgarra
				int confirmacion=JOptionPane.showConfirmDialog(view, "Desea eliminar el codigo de barra "+cod+" ?");
				
				// si se confirma la eliminacion se procede a eliminar
				if(confirmacion==0){
					
					//se eliminar el codigo en la view list y se coloca el eliminado en una lista especial para eliminar de la bd
					this.view.getModeloCodBarra().eliminarCodBarra(indexCodigoSelecionado);
					
					
				}
				
				
			break;
		case "ACTUALIZAR":
			cargarDatosArticuloView();
				//se actulizar el articulo en la base de datos
				if(myArticuloDao.actualizarArticulo(myArticulo,this.view.getModeloCodBarra().getCodsElimnar())){
					
					//se registraran los insumos de los servicios que los tengan agregardos
					if(myArticulo.getTipoArticulo()==2 && view.getModeloInsumos().getInsumos()!=null && view.getModeloInsumos().getInsumos().size()>0){
						
						//eliminar los insumos anteriores y agregar hoy
						insumoDao.eliminarTodosArticulo(myArticulo.getId());
						
						//se recorre la tabla de insumos para guardarlos en la base de datos
						for(int y=0;y<view.getModeloInsumos().getInsumos().size();y++){
							Insumo uno=view.getModeloInsumos().getInsumos().get(y);
							uno.setServicio(myArticulo);
							
							if(uno.getCantidad().floatValue()>0){
								uno.setCodigoServicio(myArticulo.getId());
								insumoDao.registrar(uno);
							}
							
						}
					}
					
					JOptionPane.showMessageDialog(view, "Se Actualizo el articulo");
					resultaOperacion=true;
					this.view.dispose();
				}
			break;
		case "CANCELAR":
			//cargarDatosArticuloView();
			//JOptionPane.showMessageDialog(view, this.view.getCbxTipo().getSelectedIndex());
			this.view.setVisible(false);
			
			break;
		}
		
	}
	
	private void calcularTotalInsumo() {
		// TODO Auto-generated method stub
		BigDecimal totalInsumos=new BigDecimal(0);
		
		//se verifica que la lista de insumos no sea nula
		if(view.getModeloInsumos().getInsumos().size()>=0){
			for(int x=0;x<view.getModeloInsumos().getInsumos().size();x++){
				BigDecimal cantidad=view.getModeloInsumos().getInsumos().get(x).getCantidad();
				//BigDecimal precioVenta= new BigDecimal(detalle.getArticulo().getPrecioVenta());
				
				
				
				BigDecimal precio =new BigDecimal(view.getModeloInsumos().getInsumos().get(x).getArticulo().getPrecioVenta());
				//se calcula el total del item
				BigDecimal totalItem=cantidad.multiply(precio);
				view.getModeloInsumos().getInsumos().get(x).setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
				view.getModeloInsumos().fireTableDataChanged();
				totalInsumos=totalInsumos.add(totalItem);
			}
			view.getTxtInsumoTotal().setText("Lps "+totalInsumos.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
		}
	}



	/*<<<<<<<<<<<<<<<<<<<<<<<<< metodo que devuelve el articulo guardado >>>>>>>>>>>>>>>>>>>>>>>>>         */
	public Articulo getArticuloGuardado(){
		return myArticulo;
		
		
	}
	
	public void guardarArticulo(){
		
		cargarDatosArticuloView();
		
		boolean existeCodigo=false;
		
		//se verificara que no exista otro articulo con el mismo codigo de barra
		//se recorre la lista de codigo de barra en busqueda de duplicados
		for(int x=0; x<view.getModeloCodBarra().getSize();x++){
			
			//se obtiene el primer codigo de barra del la lista
			String codigo=view.getModeloCodBarra().getCodBarra(x).getCodigoBarra();
			
			//se busca en la base de datos por el codigo de barra
			Articulo articuloBusqueda=myArticuloDao.buscarArticuloBarraCod(codigo);
			
			//si nos retorna un articulo significa que existe el codigo de barra
			if(articuloBusqueda!=null){
				
				//se estable que si exite el codigo de barra
				existeCodigo=true;
				//JOptionPane.showMessageDialog(view, "El codigo de barra '"+codigo+"' ya esta asignado al articulo.\n"+articuloBusqueda.toString());
				JOptionPane.showMessageDialog(view, "El codigo de barra '"+codigo+"' ya esta asignado al articulo.\n"+articuloBusqueda.toString(), "Error al guardar articulo", JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
		//se verifica que en la busqueda no se encontro un codigo de barra igual
		if(existeCodigo==false){
		
			//se ejecuta la accion de guardar
			if(myArticuloDao.registrar(myArticulo)){
				
				
				//se registraran los insumos de los servicios que los tengan agregardos
				if(myArticulo.getTipoArticulo()==2 && view.getModeloInsumos().getInsumos()!=null && view.getModeloInsumos().getInsumos().size()>0){
					
					//se recorre la tabla de insumos para guardarlos en la base de datos
					for(int y=0;y<view.getModeloInsumos().getInsumos().size();y++){
						Insumo uno=view.getModeloInsumos().getInsumos().get(y);
						uno.setServicio(myArticulo);
						
						if(uno.getCantidad().floatValue()>0){
							uno.setCodigoServicio(myArticulo.getId());
							this.insumoDao.registrar(uno);
						}
						
					}
				}
				JOptionPane.showMessageDialog(this.view, "Se ha registrado Exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
				myArticulo.setId(myArticuloDao.getIdArticuloRegistrado());//se completa el proveedor guardado con el ID asignado por la BD
				resultaOperacion=true;
				this.view.setVisible(false);
				//this.myArticuloDaoRemote.registrarArticulo(getArticulo());
				
				
			}
			else{
				JOptionPane.showMessageDialog(this.view, "No se Registro","Error",JOptionPane.ERROR_MESSAGE);
				resultaOperacion=false;
				this.view.setVisible(false);
			}
		}//fin verificacion de codigos de barras
		else{
			JOptionPane.showMessageDialog(view, "No se guardo el articulo.\nPorque algunos codigos de barras ya estan utilizados por otros articulos., ","Error al guardar articulo", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void cargarDatosArticuloView(){
		//Se establece el nombre del articulo
		myArticulo.setArticulo(this.view.getTxtNombre().getText());
		
		//Se establece el impuesto seleccionado
		Impuesto imp= (Impuesto) this.view.getCbxImpuesto().getSelectedItem();
		myArticulo.setImpuestoObj(imp);
		
		//JOptionPane.showMessageDialog(view, this.view.getCbxTipo().getSelectedItem());
		//Se establece los codigos de barra
		myArticulo.setCodBarras(this.view.getModeloCodBarra().getCodsBarras());
		
		//se establece el precion de articulo
		//myArticulo.setPrecioVenta(Double.parseDouble(this.view.getTxtPrecio().getText()));
		
		//se Estable los precios de ventas del articulo
		myArticulo.setPreciosVenta(view.getModeloPrecio().getPrecios());
		
		int x=this.view.getCbxTipo().getSelectedIndex();
		if(x==0){
			myArticulo.setTipoArticulo(1);
		}
		if(x==1){
			myArticulo.setTipoArticulo(2);
		}
	}
	
	
	public boolean agregarArticulo(){
		view.setVisible(true);
		return resultaOperacion;
	}
	
	
	public boolean actualizarArticulo(Articulo a){
		//se carga la configuracionde la view articulo para la actulizacion
		this.view.configActualizar();
		
		
		//se establece el nombre de articulo en la view
		this.view.getTxtNombre().setText(a.getArticulo());
		
		//se establece la marca en la view
		this.view.getTxtMarca().setText(a.getMarcaObj().getDescripcion());
		
		//si el articulo tiene una lista de codigos de barra se incluyen en la view
		if(a.getCodBarra()!=null){
			this.view.getModeloCodBarra().setCodsBarra(a.getCodBarra());
		}
		
		
		//se consigue el index del impuesto del articulo
		int indexImpuesto=this.view.getListaCbxImpuesto().buscarImpuesto(a.getImpuestoObj());
		//se selecciona el impuesto apropiado para el articulo
		this.view.getCbxImpuesto().setSelectedIndex(indexImpuesto);
		
		
		//se establece el articulo de la clase this
		myArticulo=a;
		
		//se estable el precio del articulo en la view
		//this.view.getTxtPrecio().setText(""+a.getPrecioVenta());
		
		//se estable el tipo de articulo
		if(a.getTipoArticulo()==1){
			this.view.getCbxTipo().setSelectedIndex(0);
			view.getBtnAgregarInsumo().setEnabled(false);
			view.getBtnEliminarInsumo().setEnabled(false);
			view.gettInsumos().setEnabled(false);
		}
		if(a.getTipoArticulo()==2){
			this.view.getCbxTipo().setSelectedIndex(1);
			cargarTablaInsumo(insumoDao.buscarPorId(a.getId()));
			this.calcularTotalInsumo();
			view.getBtnAgregarInsumo().setEnabled(true);
			view.getBtnEliminarInsumo().setEnabled(true);
			view.gettInsumos().setEnabled(true);
			
		}
		//se cargar los precios de los articulos si los hay
		//this.cargarTabla(this.precioDao.getPreciosArticulo(myArticulo.getId()));
		List<PrecioArticulo> preciosArticulo=precioDao.getPreciosArticulo(myArticulo.getId());
		
		//se buscar
		List<PrecioArticulo> precios=precioDao.getTipoPrecios();
		
		//se estable el valor de los tipos de precios que se encontraron en la base de datos
		if(preciosArticulo!=null && precios!=null){
			
			//se recorren todos los precios
			for(int y=0;y<precios.size();y++){
				//se recorren los precios de los articulos en busca algun valor para el tipo de precio
				for(int xx=0;xx<preciosArticulo.size();xx++){
					if(precios.get(y).getCodigoPrecio()==preciosArticulo.get(xx).getCodigoPrecio()){
						precios.get(y).setPrecio(preciosArticulo.get(xx).getPrecio());
						
					}
				}
			}
		
			this.cargarTabla(precios);
		}
		
		//view.getModeloInsumos().setInsumos(this.insumoDao.buscarPorId(a.getId()));
				
		// se hace visible la ventana modal
		this.view.setVisible(true);
		
		
		
		return this.resultaOperacion;
	}
	
	private void cargarTablaInsumo(List<Insumo> insumos) {
		// TODO Auto-generated method stub
		
		if(insumos!=null){
			for(int x=0;x<insumos.size();x++){
				view.getModeloInsumos().agregarInsumo(insumos.get(x));
			}
		}
		
	}



	public Articulo getArticulo(){
		return myArticulo;
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		myImpuestoDao=new ImpuestoDao();
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getListaCbxImpuesto().setLista(myImpuestoDao.todoImpuesto());
		
		
		//se remueve la lista por defecto
		this.view.getCbxImpuesto().removeAllItems();
	
		this.view.getCbxImpuesto().setSelectedIndex(1);
	}
	
	 // maneja el evento de oprimir el boton del raton
	public void mousePressed( MouseEvent evento )
	{
		check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador
	} // fin del motodo mousePressed
	
	// maneja el evento de liberacion del boton del raton
	public void mouseReleased( MouseEvent evento )
	{
		check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador
	} // fin del metodo mouseReleased
	
	// determina si el evento debe desencadenar el menu contextual
	private void checkForTriggerEvent( MouseEvent evento )
	{
		if ( evento.isPopupTrigger() )
			this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY() );
	} // fin del motodo checkForTriggerEvent
	
	public void check(MouseEvent e)
	{ 
		if (e.isPopupTrigger()) { //if the event shows the menu 
			this.view.getListCodigos().setSelectedIndex(this.view.getListCodigos().locationToIndex(e.getPoint())); //select the item 
			//menuContextual.show(listCodigos, e.getX(), e.getY()); //and show the menu 
		} 
		
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

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



	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		int colum=e.getColumn();
		int row=e.getFirstRow();
		
		switch(e.getType()){
		
		case TableModelEvent.UPDATE:
			//Se recoge el id de la fila marcada
	        int identificador=0; 
	        
	        if(colum==2){
	        	this.calcularTotalInsumo();
	        }
	        
			break;
		}
	}
	
}
