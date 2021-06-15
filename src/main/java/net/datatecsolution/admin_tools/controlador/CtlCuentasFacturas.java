package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewCobroFactura;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewCuentasFacturas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CtlCuentasFacturas implements ActionListener, MouseListener, ChangeListener, KeyListener {
	public ViewCuentasFacturas view;
	
	private CuentaFacturaDao cuentaFacturaDao;
	private CuentaPorCobrarDao myCuentaCobrarDao=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;

	private ClienteDao clienteDao=new ClienteDao();
	
	//fila selecciona enla lista
	private int filaPulsada=-1;
	private EmpleadoDao empleadoDao=null;
	private RutaCobroDao rutaCobroDao=null;
	private UsuarioDao usuarioDao=null;
	private ReciboPagoDao myReciboDao=null;
	
	public CtlCuentasFacturas(ViewCuentasFacturas v) {
		
		view =v;
		view.conectarControlador(this);
		cuentaFacturaDao=new CuentaFacturaDao();

		cuentaFacturaDao.setTodoReg(true);

		empleadoDao=new EmpleadoDao();
		rutaCobroDao=new RutaCobroDao();
		usuarioDao=new UsuarioDao();
		myReciboDao=new ReciboPagoDao();
		myCuentaCobrarDao=new CuentaPorCobrarDao();
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();

		cargarComboBox();
		cargarCbxRutas();
		
		
		//cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));

		view.pack();
		view.getTxtBuscar().setText("");
		view.getTxtBuscar().selectAll();
		//view.getRdbtnCliente().setSelected(true);
		//view.getCbxEmpleados().setSelectedIndex(0);
		view.getTxtBuscar().requestFocusInWindow();
		view.setVisible(true);
	}

	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);

		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaEmpleados().setLista(this.empleadoDao.todoEmpleadosVendedores());
		//se remueve la lista por defecto
		this.view.getCbxEmpleados().removeAllItems();
		//
		int vendedor=view.getModeloListaEmpleados().buscarEmpleado(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda());
		this.view.getCbxEmpleados().setSelectedIndex(vendedor);
		//this.view.getCbxEmpleados().setSelectedIndex(0);
	}

	private void cargarCbxRutas(){

		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaRutas().setLista(this.rutaCobroDao.todoRutas());
		//se remueve la lista por defecto
		this.view.getCbxRutas().removeAllItems();
		//
		int idRuta=view.getModeloListaRutas().buscarRuta(ConexionStatic.getUsuarioLogin().getConfig().getRutaCobroEnBusqueda());
		//this.view.getCbxRutas().setSelectedIndex(idRuta);
		this.view.getCbxRutas().setSelectedIndex(0);
	}
	
	
	
	public void cargarTabla(List<CuentaFactura> cuentas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiarCuentas();
		
		if(cuentas!=null){
			for(int c=0;c<cuentas.size();c++){
				this.view.getModelo().agregarCuenta(cuentas.get(c));
				
			}

		}
		setTotalReg();
	}
	void setTotalReg(){

		int count =cuentaFacturaDao.getRowCount();
		String text="";
		if(count<10)
			text=text+"    "+count;
		else if(count<100)
			text=text+"  "+count;
		else
			text=count+"";
		view.getJltotalReg().setText(text);

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
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
	public void mousePressed(MouseEvent evento) {
		// TODO Auto-generated method stub
		check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador

	}

	@Override
	public void mouseReleased(MouseEvent evento) {
		// TODO Auto-generated method stub
		int r = view.getTabla().rowAtPoint(evento.getPoint());
		if (r >= 0 && r < view.getTabla().getRowCount()) {
			view.getTabla().setRowSelectionInterval(r, r);
		} else {
			view.getTabla().clearSelection();
		}

		int rowindex = view.getTabla().getSelectedRow();
		if (rowindex < 0)
			return;

		check(evento);
		checkForTriggerEvent( evento ); // comprueba el desencadenador

	}

	// determina si el evento debe desencadenar el menu contextual
	private void checkForTriggerEvent( MouseEvent evento )
	{
		if ( evento.isPopupTrigger() )
			this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY() );
	} // fin del metodo checkForTriggerEvent

	public void check(MouseEvent e)
	{
		if (e.isPopupTrigger()) { //if the event shows the menu
			//this.view.getListCodigos().setSelectedIndex(this.view.getListCodigos().locationToIndex(e.getPoint())); //select the item
			//view.getTabla().setColumnSelectionInterval(index0, index1);
			//JOptionPane.showMessageDialog(view, "Donde se dio clip "+e.getPoint());
		}


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

			case "EDITAR_CLIENTE":

				filaPulsada = this.view.getTabla().getSelectedRow();


				//si seleccion una fila
				if(filaPulsada>=0){
					CuentaFactura cuentaSelected=view.getModelo().getCuenta(filaPulsada);

					//crea la ventana para ingresar un nuevo proveedor
					ViewCrearCliente viewCliente= new ViewCrearCliente();

					//se crea el controlador de la ventana y se le pasa la view
					CtlCliente ctlActulizarCliente=new CtlCliente(viewCliente);


					 Cliente myCliente=clienteDao.buscarPorId(view.getModelo().getCuenta(filaPulsada).getCliente().getId());




					//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
					boolean resultado=ctlActulizarCliente.actualizarCliente(myCliente);

					//se proceso el resultado de modificar la marca
					if(resultado){

						this.view.getRdbtnId().setSelected(true);
						view.getTxtBuscar().setText(cuentaSelected.getCodigoCuenta()+"");
						ActionEvent actionEvent=new ActionEvent(view,ActionEvent.ACTION_PERFORMED,"BUSCAR");
						this.actionPerformed(actionEvent);

						view.getTxtBuscar().selectAll();
						view.getTxtBuscar().requestFocusInWindow();

					}

					ctlActulizarCliente=null;
					viewCliente=null;


				}

				break;

			case "CAMBIOCOMBOBOXRUTA":

				RutaCobro miRuta=(RutaCobro) view.getCbxRutas().getSelectedItem();

				if(miRuta!=null){
					ConexionStatic.getUsuarioLogin().getConfig().setRutaCobroEnBusqueda(miRuta);
				}

				/*
				ActionEvent actionEvent1=new ActionEvent(view,ActionEvent.ACTION_PERFORMED,"BUSCAR");

				this.actionPerformed(actionEvent1);
				*/
				break;

			case "CAMBIOCOMBOBOX":
				//JOptionPane.showMessageDialog(view, "Cambio el vendedor");

				Empleado miEmpleado=(Empleado)view.getCbxEmpleados().getSelectedItem();

				if(miEmpleado!=null){
					ConexionStatic.getUsuarioLogin().getConfig().setVendedorEnBusqueda(miEmpleado);
				}
				/*
				ActionEvent actionEvent=new ActionEvent(view,ActionEvent.ACTION_PERFORMED,"BUSCAR");
				this.actionPerformed(actionEvent);

				 */

				break;

			case "REG_ACTIVO":
				cuentaFacturaDao.setTodoReg(true);
				break;

			case "REG_INACTIVO":
				//cuentaFacturaDao.setTodoReg(true);
				break;

			case "REG_TODOS":
				cuentaFacturaDao.setTodoReg(false);
				break;

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

						this.view.getRdbtnId().setSelected(true);
						view.getTxtBuscar().setText(cuentaSelected.getCodigoCuenta()+"");
						ActionEvent actionEvent=new ActionEvent(view,ActionEvent.ACTION_PERFORMED,"BUSCAR");
						this.actionPerformed(actionEvent);

						view.getTxtBuscar().selectAll();
						view.getTxtBuscar().requestFocusInWindow();

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

				}else if(this.view.getRdbtnFecha().isSelected()){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date1 = sdf.format(this.view.getDcFecha1().getDate());
					String date2 = sdf.format(this.view.getDcFecha2().getDate());
					cargarTabla(cuentaFacturaDao.buscarConSaldoXfecha(date1,date2));

				}else if(this.view.getRdbtnRTN().isSelected()){
					cargarTabla(cuentaFacturaDao.buscarConSaldoXrtnCliente(view.getTxtBuscar().getText()));
				}else if(this.view.getRdbtnTodos().isSelected()){
					cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}else if(view.getRdbtnCliente().isSelected()&&view.getTxtBuscar().getText().trim().length()>=3){
					cargarTabla(cuentaFacturaDao.buscarConSaldoXnombreCliente(view.getTxtBuscar().getText()));
				}else{
					this.view.getModelo().limpiarCuentas();
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
		
			
		case "TRANSFERIR":

			if(verificarSelecion()) {
				CuentaFactura cuentaFacturaOrigen = view.getModelo().getCuenta(filaPulsada);

				if (cuentaFacturaOrigen.getSaldo().doubleValue() > 0) {

						String numCuenta = JOptionPane.showInputDialog("Transferir saldo a cuenta #:");
						if (AbstractJasperReports.isNumber(numCuenta)) {
							CuentaFactura cuentaFacturaDestino = cuentaFacturaDao.buscarPorIdCuenta(Integer.parseInt(numCuenta));
							if (cuentaFacturaDestino != null) {


								//se crea el mensaje para mostrar en la pantalla
								JLabel jLabel1 = new JLabel("Se realizara la siguiente trasfericia de saldo:");
								JLabel jLabel2 = new JLabel("-> Cuenta de origen: " + cuentaFacturaOrigen.getCodigoCuenta() + " " + cuentaFacturaOrigen.getCliente().getNombre() + " con saldo Lps " + cuentaFacturaOrigen.getSaldo());
								JLabel jLabel3 = new JLabel("-> Cuenta de destino: " + cuentaFacturaDestino.getCodigoCuenta() + " " + cuentaFacturaDestino.getCliente().getNombre() + " con saldo Lps " + cuentaFacturaDestino.getSaldo());
								JLabel jLabel4 = new JLabel("Escriba el password de admin para confirmar");
								JPasswordField pf2 = new JPasswordField();
								JPanel jPane = new JPanel();
								jPane.setLayout(new BoxLayout(jPane, BoxLayout.PAGE_AXIS));
								jPane.add(jLabel1);
								jPane.add(Box.createRigidArea(new Dimension(0, 10)));
								jPane.add(jLabel2);
								jPane.add(jLabel3);
								jPane.add(Box.createRigidArea(new Dimension(0, 10)));
								jPane.add(jLabel4);
								jPane.add(pf2);
								//fin del mensaje

								int action2 = JOptionPane.showConfirmDialog(view, jPane, "Confirmacion de tranferencia!!!", JOptionPane.OK_CANCEL_OPTION);

								if (action2 < 0) {

								} else {
									String pwd = new String(pf2.getPassword());

									//comprabacion del permiso administrativo
									if (usuarioDao.comprobarAdmin(pwd)) {

										//ser crea el recibo para la cancelacion del saldo de la cuenta
										ReciboPago myRecibo = new ReciboPago();

										myRecibo.setCliente(cuentaFacturaOrigen.getCliente());
										String concepto = "Transferencia de saldo a cuenta detalle # " + cuentaFacturaDestino.getCodigoCuenta();
										myRecibo.setConcepto(concepto);
										myRecibo.setTotal(cuentaFacturaOrigen.getSaldo());
										DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
										LocalDateTime now = LocalDateTime.now();
										myRecibo.setFecha(sdf.format(now));
										//se establece la cantidad en letras
										myRecibo.setTotalLetras(NumberToLetterConverter.convertNumberToLetter(myRecibo.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));

										//se manda aguardar el recibo con los pagos realizados
										boolean resulta = this.myReciboDao.registrar(myRecibo, cuentaFacturaOrigen);


										if (resulta) {
											//se incrementa el saldo de la cuenta de destino general y de detalle

											//creacion reg cuenta general
											CuentaPorCobrar newRegCuentaGeneral = new CuentaPorCobrar();
											newRegCuentaGeneral.setCliente(cuentaFacturaDestino.getCliente());
											newRegCuentaGeneral.setDescripcion("Transferencia de saldo a cuenta factura # " + cuentaFacturaDestino.getCodigoCuenta());
											newRegCuentaGeneral.setCredito(myRecibo.getTotal());

											myCuentaCobrarDao.reguistrarCredito(newRegCuentaGeneral);

											//creacion reg cuenta detalle factura o cuenta donde se transfirio el saldo
											CuentaFactura unaCuentaFactura = new CuentaFactura();
											unaCuentaFactura.setCodigoCuenta(cuentaFacturaDestino.getCodigoCuenta());
											unaCuentaFactura.setCaja(ConexionStatic.getUsuarioLogin().getCajaActiva());
											unaCuentaFactura.setCliente(cuentaFacturaDestino.getCliente());
											unaCuentaFactura.setDetalleCredito("Transferencia de saldo de cuenta factura # " + cuentaFacturaOrigen.getCodigoCuenta());
											unaCuentaFactura.setSaldo(myRecibo.getTotal());

											cuentaXCobrarFacturaDao.reguistrarCreditoSinFactura(unaCuentaFactura);


											myRecibo.setNoRecibo(myReciboDao.idUltimoRecibo);

											try {

												AbstractJasperReports.createReportReciboCobroCajaFactura(ConexionStatic.getPoolConexion().getConnection(), myRecibo.getNoRecibo());
												//AbstractJasperReports.imprimierFactura();
												AbstractJasperReports.showViewer(view);

												this.actionPerformed(new ActionEvent(this,1, "BUSCAR"));

												//myFactura.
											} catch (SQLException e3) {
												// TODO Auto-generated catch block
												e3.printStackTrace();
											}

										} else {//
											JOptionPane.showMessageDialog(view, "El recibo no se guardo correctamente.");
										}//fin del if que verefica la acccion de guardar el recibo
									}

								}

							} else {
								JOptionPane.showMessageDialog(view, "No se encontro la cuenta!!!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}else{
					JOptionPane.showMessageDialog(view, "La cuenta de origen no tiene saldo.", "Error", JOptionPane.ERROR_MESSAGE);
				}

			}


			break;
			
			
			
		case "NEXT":
			view.getModelo().netPag();
			if(this.view.getRdbtnId().isSelected()){

				cargarTabla(cuentaFacturaDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText())));

			}else if(this.view.getRdbtnFecha().isSelected()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				cargarTabla(cuentaFacturaDao.buscarConSaldoXfecha(date1,date2));
			}else if(this.view.getRdbtnRTN().isSelected()){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXrtnCliente(view.getTxtBuscar().getText()));
			}else if(this.view.getRdbtnTodos().isSelected()&&ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()!=0){

				cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}else if(view.getRdbtnCliente().isSelected()&&view.getTxtBuscar().getText().trim().length()>=3){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXnombreCliente(view.getTxtBuscar().getText()));
			}else{
				this.view.getModelo().limpiarCuentas();
			}
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			if(this.view.getRdbtnId().isSelected()){
				cargarTabla(cuentaFacturaDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText())));

			}else if(this.view.getRdbtnFecha().isSelected()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getDcFecha1().getDate());
				String date2 = sdf.format(this.view.getDcFecha2().getDate());
				cargarTabla(cuentaFacturaDao.buscarConSaldoXfecha(date1,date2));
			}else if(this.view.getRdbtnRTN().isSelected()){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXrtnCliente(view.getTxtBuscar().getText()));
			}else if(this.view.getRdbtnTodos().isSelected()&&ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getCodigo()!=0){

				cargarTabla(cuentaFacturaDao.buscarConSaldo(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}else if(view.getRdbtnCliente().isSelected()&&view.getTxtBuscar().getText().trim().length()>=3){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXnombreCliente(view.getTxtBuscar().getText()));
			}else{
				this.view.getModelo().limpiarCuentas();
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

			//si esta activado la busqueda por articulo
			if(this.view.getRdbtnRTN().isSelected()){
				cargarTabla(cuentaFacturaDao.buscarConSaldoXrtnCliente(view.getTxtBuscar().getText()));
			}

			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
		}


	}

}
