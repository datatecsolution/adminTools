package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.RutaEntrega;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.modelo.dao.RutasEntregasDao;
import net.datatecsolution.admin_tools.view.ViewCrearRuta;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CtlRutaEntrega implements ActionListener, KeyListener, MouseListener, TableModelListener {
	
	private ViewCrearRuta view;
	private RutaEntrega myRuta;
	private RutasEntregasDao myDao;
	private boolean resul=false;
	private Empleado myEmpleado=null;
	private EmpleadoDao myEmpleadoDao;
	private CajaDao cajaDao;
	private Factura myFactura;
	private FacturaDao myFacturaDao=null;
	private int filaPulsada;


	public CtlRutaEntrega(ViewCrearRuta v ) {
		// TODO Auto-generated constructor stub
	
		view=v;
		
		myRuta=new RutaEntrega();
		
		myEmpleadoDao=new EmpleadoDao();
		myFacturaDao=new FacturaDao();
		cajaDao=new CajaDao();
		myFactura=new Factura();
		
		//se busca el empleado por defecto es con el id 0
		myEmpleado=myEmpleadoDao.buscarPorId(1);
		if(myEmpleado!=null){
			view.getTxtEmpleado().setText(myEmpleado.toString());
		}
		cargarComboBox();

		myDao=new RutasEntregasDao();
		
		view.conectarControlador(this);
		
		Date horaLocal=new Date();
		view.getDateFecha().setDate(horaLocal);
		//view.getDateFecha()
		
		
		
		
	}
	
	/*
	private void cargarComboBox(Vector<Banco> bancos){
		
		if(bancos!=null){
			//se obtiene la lista de los formas de pago y se le pasa al modelo de la lista
			this.view.getModeloCuentasBancos().setLista(bancos);
			
			
			//se remueve la lista por defecto
			this.view.getCbFormaPago().removeAllItems();
		
			this.view.getCbFormaPago().setSelectedIndex(0);
		}
	
	}*/
	
	public RutaEntrega getRutaEntrega(){
		return this.myRuta;
	}
	public boolean getResultado(){
		
		return resul;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "ACTUALIZAR":
			
				if(validar()){
								
					setRuta();
					
					if(myDao.actualizar(myRuta)){
						resul=true;
						
						try {
							SimpleDateFormat formato = new SimpleDateFormat("yyy-MM-dd");
							Date fechaDate;
							fechaDate = formato.parse(myRuta.getFecha());
							SimpleDateFormat formato2 = new SimpleDateFormat("dd-MM-yyy");
							String fecha=formato2.format(fechaDate);
							myRuta.setFecha(fecha);
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						view.setVisible(false);
						JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.","Transaccion completada!!",JOptionPane.INFORMATION_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(view, "Ocurrio un problema para registrar la salida");
					}
					
				}else{
					JOptionPane.showMessageDialog(view,"Debe agregar por lo menos una factura a la ruta.", "Error en la ruta",JOptionPane.ERROR_MESSAGE);
				}
			
			break;
		
		case "ELIMINAR":

			//Recoger que fila se ha pulsadao en la tabla
	        filaPulsada = this.view.getTablasFacturas().getSelectedRow();
			//Se recoge el id de la fila marcada
            int idFactura= (int)this.view.getModeloFacturas().getValueAt(filaPulsada, 0);
    
			view.getModeloFacturas().eliminarFactura(filaPulsada);
			
			break;
		case "BUSCAR":
			
			
			myFactura=myFacturaDao.buscarPorIdAndVendedor(Integer.parseInt(this.view.getTxtNofact().getText()),view.getCbxCajas().getSelectedItem(),myEmpleado.getCodigo());
			if(myFactura!=null){												
				
				
				Caja caja=(Caja)view.getCbxCajas().getSelectedItem();
				//se verifica que no este agregada la factura en otra ruta
				boolean verificar=myDao.verificarExistenciaEnRuta(myFactura,caja);
				myFactura.setCodigoCaja(caja.getCodigo());

				boolean verificarLocal=verificarFactYaAgregada(myFactura);
				
				if(verificar==false && verificarLocal==false){
					view.getModeloFacturasEntregas().agregarFactura(myFactura);
					view.getTxtNofact().setText("");
					selectRowInset();
				}else{
					JOptionPane.showMessageDialog(view, "La factura ya esta agregada a una ruta.","Error al agregar",JOptionPane.ERROR_MESSAGE);
				}
			}else{
				JOptionPane.showMessageDialog(view, "No se encuentro la factura","Error al agregar",JOptionPane.ERROR_MESSAGE);
				view.getTxtNofact().selectAll();
			}
			
			break;
		case "GUARDAR":
			if(validar()){
				
				setRuta();
				
				if(myDao.registrar(myRuta)){
					resul=true;
					view.setVisible(false);
					JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.","Transaccion completada!!",JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(view, "Ocurrio un problema para registrar la salida");
				}
				
			}else{
				JOptionPane.showMessageDialog(view,"Debe agregar por lo menos una factura a la ruta.", "Error en la ruta",JOptionPane.ERROR_MESSAGE);
			}
			
			break;
			
		case "CANCELAR":
			view.setVisible(false);
			break;
		}
		
	}
	
	private void selectRowInset(){
		
		int row = this.view.getTablasFacturas().getRowCount () - 2;
	    int col = 1;
	    boolean toggle = false;
	    boolean extend = false;
	    this.view.getTablasFacturas().changeSelection(row, 0, toggle, extend);
	    this.view.getTablasFacturas().changeSelection(row, col, toggle, extend);
	    this.view.getTablasFacturas().addColumnSelectionInterval(0, 6);
		
	}

	private boolean verificarFactYaAgregada(Factura factVerificar){
		boolean existe=false;
		for(int x=0;x<view.getModeloFacturasEntregas().getFacturas().size();x++){

			Factura myFact=view.getModeloFacturasEntregas().getFacturas().get(x);
			int valor1=myFact.getIdFactura();
			int valor2=factVerificar.getIdFactura();
			if(valor1==valor2)
				if(myFact.getCodigoCaja()==factVerificar.getCodigoCaja())
					existe=true;

		}
		return existe;

	}
	
	public boolean actualizarRuta(RutaEntrega r){
		
		this.myRuta=r;
		this.myEmpleado=myRuta.getVendedor();
		
		view.getTxtEmpleado().setText(myEmpleado.toString());
		//envia una  ruta nueva para que le agregen los codigos de las facturas;
		RutaEntrega facturasRuta=new RutaEntrega();
		
		facturasRuta.setIdRuta(r.getIdRuta());
		myDao.getFacturas(facturasRuta);
		
		if(facturasRuta.getFacturas().size()>0){
			for(int x=0;x<facturasRuta.getFacturas().size();x++){
				
				Caja caja=this.cajaDao.buscarPorId(facturasRuta.getFacturas().get(x).getCodigoCaja());
				
				//JOptionPane.showMessageDialog(view, caja.getNombreBd());
				Factura una =myFacturaDao.buscarPorId(facturasRuta.getFacturas().get(x).getIdFactura(),caja);
				una.setCodigoCaja(facturasRuta.getFacturas().get(x).getCodigoCaja());
				view.getModeloFacturas().agregarFactura(una);
				
			}
		}
		
		
		try {
			SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyy");
			Date fechaDate = formato.parse(r.getFecha());
			view.getDateFecha().setDate(fechaDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//view.getDateFecha().setDate(arg0);
		
		view.getBtnGuardar().setVisible(false);
		view.getBtnActualizar().setVisible(true);
		
		this.view.setVisible(true);
		
		
		
		
		return this.resul;
	}

	private void setRuta() {
		// TODO Auto-generated method stub
		
		myRuta.setVendedor(myEmpleado);
		
		myRuta.setEstado("Ruta");
		
		//se crear el formato para la fecha
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//se recoge la fecha de compra de la view
		String date = sdf.format(this.view.getDateFecha().getDate());
		//se estable la feca de compra en el modelo
		myRuta.setFecha(date);
		myRuta.setFacturas(view.getModeloFacturas().getFacturas());
		
		//JOptionPane.showMessageDialog(view, myRuta.getFacturas().size());
	}
	private boolean validar() {
		// TODO Auto-generated method stub
		boolean resul=false;
		/*
		if(view.getTxtCantidad().getText().trim().length()==0 ){
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos","Erro validacion",JOptionPane.ERROR_MESSAGE);
			view.getTxtCantidad().requestFocusInWindow();
		}else
			if(view.getTxtConcepto().getText().trim().length()==0){
				JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos","Erro validacion",JOptionPane.ERROR_MESSAGE);
				view.getTxtConcepto().requestFocusInWindow();
			}else if(!AbstractJasperReports.isNumber(view.getTxtCantidad().getText())){
				
						JOptionPane.showMessageDialog(view, "La cantidad debe ser un numero","Erro validacion",JOptionPane.ERROR_MESSAGE);
						view.getTxtCantidad().selectAll();
						view.getTxtCantidad().requestFocusInWindow();
					}
					else{
							resul=true;
						}*/
		if(view.getModeloFacturas().getFacturas().size()>0){
			resul=true;
		}
		return resul;
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
				buscarEmpleado();
				break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			
			myEmpleado=null;
			myRuta=null;
			
	        view.setVisible(false);
	        view=null;
	      }
		
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
	
	public void buscarEmpleado(){
		ViewListaEmpleados viewBuscarEmpleado=new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado=new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado=ctBuscarEmpleado.buscar();
		
		if(resultado){
			myEmpleado=ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtEmpleado().setText(myEmpleado.toString());
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		

		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTablasFacturas().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//Se recoge el id de la fila marcada
            int idFactura= (int)this.view.getModeloFacturasEntregas().getValueAt(filaPulsada, 0);
            
            this.view.getBotonEliminar().setEnabled(true);
            //this.view.getBtnImprimir().setEnabled(true);
           this.myFactura=this.view.getModeloFacturasEntregas().getFactura(filaPulsada);
            //se consigue el proveedore de la fila seleccionada
           // myArticulo=this.view.getModelo().getArticulo(filaPulsada);
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		/*
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
				*/
				
	        }//fin del if del doble click
        	else{//si solo seleccion la fila se guarda el id de proveedor para accion de eliminar
        		
        		//this.view.getBtnEliminar().setEnabled(true);
        		
        		
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
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean agregarRuta() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return this.resul;
	}

	/**
	 * @return the myRuta
	 */
	public RutaEntrega getMyRuta() {
		return myRuta;
	}

	/**
	 * @param myRuta the myRuta to set
	 */
	public void setMyRuta(RutaEntrega myRuta) {
		this.myRuta = myRuta;
	}

	
}
