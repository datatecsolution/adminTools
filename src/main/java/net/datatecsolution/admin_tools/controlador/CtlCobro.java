package net.datatecsolution.admin_tools.controlador;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.ViewCobro;
import net.datatecsolution.admin_tools.view.ViewListaClientes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CtlCobro implements ActionListener, KeyListener {
	
	private ViewCobro view=null;
	private Cliente myCliente=null;
	private ClienteDao clienteDao=null;
	private FacturaDao myFacturaDao=null;
	
	private ReciboPago myRecibo=null;
	private ReciboPagoDao myReciboDao=null;
	private CuentaFacturaDao cuentaFacturaDao=null;
	private CuentaXCobrarFacturaDao cuentaXCobrarFacturaDao=null;
	private List<CuentaXCobrarFactura> cuentasFacturas=new ArrayList<CuentaXCobrarFactura>();
	
	private boolean resul=false;

	public CtlCobro(ViewCobro v) {
		view=v;
		
		view.conectarContralador(this);
		clienteDao=new ClienteDao();
		
		cuentaFacturaDao=new CuentaFacturaDao();
		
		cuentaXCobrarFacturaDao=new CuentaXCobrarFacturaDao();
		
		myFacturaDao=new FacturaDao();
		myReciboDao=new ReciboPagoDao();
		myRecibo=new ReciboPago();
		view.getTxtTotal().setEnabled(false);
		view.setVisible(true);
	}
	
	public boolean getResultado(){
		return resul;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		//JOptionPane.showMessageDialog(view, "paso de celdas");
		switch(comando){
		case "BUSCARCLIENTE":
			myCliente=null;
			myCliente=clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtIdcliente().getText()));
			
			if(myCliente!=null){
				this.view.getTxtNombrecliente().setText(myCliente.getNombre());
				view.getTxtLimiteCredito().setText("L. "+myCliente.getLimiteCredito());
				view.getTxtSaldo().setText("L. "+myCliente.getSaldoCuenta());
				//cargarFacturasClientes(myFacturaDao.sinPagarCliente(myCliente));
				view.getTxtTotal().setEnabled(true);
				//se buscan las facturas con saldo pendiente para cargar en view
				buscarSaldosFacturas();
			}else{
				this.view.getTxtIdcliente().setText("");
				this.view.getTxtNombrecliente().setText("");
				JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			}
			
			break;
			
		case "BUSCARCLIENTES":
			this.buscarCliente();
			break;
			
			
		case "COBRAR":
			 cobrar();
			break;
			
		case "CERRAR":
				view.setVisible(false);
			break;
		
		}
		
		
		
		
	}
	public ReciboPago getRecibo(){
		return this.myRecibo;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getComponent()==this.view.getTxtTotal()){
			char caracter = e.getKeyChar();

		      // Verificar si la tecla pulsada no es un digito
		      if(((caracter < '0') ||
		         (caracter > '9')) &&
		         (caracter != '\b' /*corresponde a BACK_SPACE*/)
		         && (caracter !='.'))
		      {
		         e.consume();  // ignorar el evento de teclado
		      }
		      if (caracter == '.' && view.getTxtTotal().getText().contains(".")) {
		    	  e.consume();
		    	  }
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
switch(e.getKeyCode()){
		
		case KeyEvent.VK_F1:
			
			break;
			
		case KeyEvent.VK_F2:
			cobrar();
			break;
			
		case KeyEvent.VK_F3:
				buscarCliente();
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

		if(e.getComponent()==this.view.getTxtTotal()){
			
			if(AbstractJasperReports.isNumber(view.getTxtTotal().getText())||AbstractJasperReports.isNumberReal(view.getTxtTotal().getText())){
					if(this.myCliente!=null && view.getTxtTotal().getText().trim().length()!=0 && new BigDecimal(view.getTxtTotal().getText()).doubleValue()!=0 ){
						view.getModeloFacturas().setPago(new BigDecimal(view.getTxtTotal().getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
                    }
					if(view.getTxtTotal().getText().trim().length()==0){
						view.getModeloFacturas().resetPago();
					}
			}else {
				if(view.getTxtTotal().getText().trim().length()==0){
					view.getModeloFacturas().resetPago();
				}
			}
			
			
		}
		if(e.getComponent()==this.view.getTxtIdcliente()&& e.getKeyCode() != KeyEvent.VK_ENTER){
			//if(this.myCliente==null){
				myCliente=null;
				view.getTxtTotal().setEnabled(false);
				//this.view.getTxtIdcliente().setText("");;
				this.view.getTxtNombrecliente().setText("");
				view.getTxtLimiteCredito().setText("");
				view.getTxtSaldo().setText( "");
			//}
		}
		
	}
	
	private void buscarCliente(){
		//se crea la vista para buscar los cliente
		ViewListaClientes viewListaCliente=new ViewListaClientes (this.view);
		
		CtlClienteBuscar ctlBuscarCliente=new CtlClienteBuscar(viewListaCliente);
		
		view.getTxtTotal().setEnabled(false);
		this.view.getTxtIdcliente().setText("");
        this.view.getTxtNombrecliente().setText("");
		view.getTxtLimiteCredito().setText("");
		view.getTxtSaldo().setText( "");
		
		boolean resul=ctlBuscarCliente.buscarCliente(view);
		
		//se comprueba si le regreso un articulo valido
		if(resul){
			myCliente=ctlBuscarCliente.getCliente();
			this.view.getTxtIdcliente().setText(""+myCliente.getId());
            this.view.getTxtNombrecliente().setText(myCliente.getNombre());
			view.getTxtLimiteCredito().setText("L. "+myCliente.getLimiteCredito());
			view.getTxtSaldo().setText("L. "+myCliente.getSaldoCuenta());
			view.getTxtTotal().setEnabled(true);
			//se buscan las facturas con saldo pendiente para cargar en view
			buscarSaldosFacturas();
		
		}else{
			JOptionPane.showMessageDialog(view, "No se encontro el cliente");
			myCliente=null;
			
			//this.view.getTxtIdcliente().setText("1");;
			//this.view.getTxtNombrecliente().setText("Cliente Normal");
		}
		viewListaCliente.dispose();
		ctlBuscarCliente=null;
	}
	
	private void buscarSaldosFacturas() {
		// TODO Auto-generated method stub
		view.getModeloFacturas().limpiarCuentas();
		
		List<CuentaFactura> facturas=cuentaFacturaDao.buscarPorIdCliente(this.myCliente.getId());
		if(facturas!=null){
			
			for(int x=0;x<facturas.size();x++){
				
				view.getModeloFacturas().agregarCuenta(facturas.get(x));
			}
		}
		
	}

	public boolean validar(){
		boolean comprobado=false;
		
		//se comprueba que el total sea un numero entero o real
		boolean comprobado1=AbstractJasperReports.isNumber(view.getTxtTotal().getText());
		comprobado1=AbstractJasperReports.isNumber(view.getTxtTotal().getText()) || AbstractJasperReports.isNumberReal(view.getTxtTotal().getText());
	
		
		if(comprobado1==false ){
				JOptionPane.showMessageDialog(view, "El total No es un numero.","Error de validacion.",JOptionPane.ERROR_MESSAGE);
				view.getTxtTotal().setText("");
				view.getTxtTotal().requestFocusInWindow();
				view.getModeloFacturas().resetPago();
					
		}else
			{ 
				
				if(view.getTxtNombrecliente().getText().trim().length()==0){
					
					JOptionPane.showMessageDialog(view, "El no existe el cliente");
					
				}else
					if(view.getTxtLimiteCredito().getText().trim().length()==0){
						JOptionPane.showMessageDialog(view, "El cliente no tiene credito");
						//view.getTxtCantidad().requestFocusInWindow();
					}else
						if(view.getTxtTotal().getText().trim().length()==0){
							JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
							view.getTxtTotal().requestFocusInWindow();
						}else
							if(view.getTxtSaldo().getText().trim().length()==0){
								JOptionPane.showMessageDialog(view, "El cliente no tiene saldo pendiente");	
							}
							else
								if(Double.parseDouble(view.getTxtTotal().getText())==0){
									JOptionPane.showMessageDialog(view, "Coloque la cantidad a cobrar");
									view.getTxtTotal().requestFocusInWindow();
								}
								else{
								
								
									comprobado=true;
								}
		}
			
		return comprobado;
	}
	
	private void cobrar() {
		
			
			
			if(this.validar()){
				buscarSaldosFacturas();
				view.getModeloFacturas().setPago(new BigDecimal(view.getTxtTotal().getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN));

				setRecibo();




				//se manda aguardar el recibo con los pagos realizados
				boolean resulta=this.myReciboDao.registrar(myRecibo);

//				buscarSaldosFacturas();
//				view.getModeloFacturas().setPago(new BigDecimal(view.getTxtTotal().getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN));

				if(resulta){
					
					this.resul=true;
					myRecibo.setNoRecibo(myReciboDao.idUltimoRecibo);
					
					registraPagoAfactura();
					
					//JOptionPane.showMessageDialog(view, "El recibo se guardo correctamente.");
					this.view.setVisible(false);
					
					//resul=true;
					try {
					
						//AbstractJasperReports.createReport(conexion.getPoolConexion().getConnection(), 5, myRecibo.getNoRecibo());
						AbstractJasperReports.createReportReciboCobroCaja(ConexionStatic.getPoolConexion().getConnection(), myRecibo.getNoRecibo());
						//AbstractJasperReports.showViewer(view);
						AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
						
						//myFactura.
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}else{//
					JOptionPane.showMessageDialog(view, "El recibo no se guardo correctamente.");
				}//fin del if que verefica la acccion de guardar el recibo
			}
		
		
	}
	private void registraPagoAfactura() {
		// TODO Auto-generated method stub
		
				//se procesan las facturas y sus saldos para poderlos actualizar
				List<CuentaFactura> facturas=view.getModeloFacturas().getCuentasFacturas();
				
				for(int y=0;y<facturas.size();y++){
					
					
					if(facturas.get(y).getPago().doubleValue()>0){
						CuentaXCobrarFactura cuenta=new CuentaXCobrarFactura();
						
						cuenta.setCodigoCuenta(facturas.get(y).getCodigoCuenta());
						cuenta.setDebito(new BigDecimal(facturas.get(y).getPago().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
						cuenta.setSaldo(new BigDecimal(facturas.get(y).getNewSaldo().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
						cuenta.setDescripcion("Pago con recibo # "+this.myRecibo.getNoRecibo());
						cuentaXCobrarFacturaDao.reguistrarDebitoYaProcesado(cuenta);
						//cuentasFacturas.add(cuenta);
					}
					
					
				}
	}

	private void setRecibo() {
		// TODO Auto-generated method stub
		myRecibo.setCliente(myCliente);
		
		String concepto="Pago a facturas #";
		
		//
		List<CuentaFactura> facturas=view.getModeloFacturas().getCuentasFacturas();
		
		for(int y=0;y<facturas.size();y++){
			
			
			if(facturas.get(y).getPago().doubleValue()>0){
				concepto+=" "+facturas.get(y).getNoFactura();
				if(y==(facturas.size()-1)){
					concepto+=".";
				}else{
					concepto+=",";
				}
			}
			
			
		}
		
		myRecibo.setConcepto(concepto);
		myRecibo.setTotal(new BigDecimal(view.getTxtTotal().getText()));
	
		//se establece la cantidad en letras
		myRecibo.setTotalLetras(NumberToLetterConverter.convertNumberToLetter(myRecibo.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
	}

}
