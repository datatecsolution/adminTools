package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.ProveedorDao;
import net.datatecsolution.admin_tools.modelo.dao.ReciboPagoProveedoresDao;
import net.datatecsolution.admin_tools.view.ViewListaProveedor;
import net.datatecsolution.admin_tools.view.ViewPagoProveedor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;

public class CtlPagoProveedor implements ActionListener, KeyListener {
	
	ViewPagoProveedor view;
	
	private Proveedor myProveedor=null;
	private ProveedorDao myDao=null;
	private BancosDao myFormaPago;
	
	
	private ReciboPagoProveedor myRecibo=null;
	private ReciboPagoProveedoresDao myReciboDao=null;
	
	private boolean resul=false;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public CtlPagoProveedor(ViewPagoProveedor v){
		view=v;
	
		myDao=new ProveedorDao();
		myReciboDao=new ReciboPagoProveedoresDao();
		myFormaPago=new BancosDao();
		myRecibo=new ReciboPagoProveedor();
		view.conectarContralador(this);
		
		cargarComboBox();
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		
	
		//se obtiene la lista de los formas de pago y se le pasa al modelo de la lista
		this.view.getModeloCuentasBancos().setLista(myFormaPago.getCuentas());
		
		
		//se remueve la lista por defecto
		this.view.getCbFormaPago().removeAllItems();
	
		this.view.getCbFormaPago().setSelectedIndex(0);
	
	}

	public boolean getResultado(){
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
			
			break;
			
		case KeyEvent.VK_F2:
			pagar();
			break;
			
		case KeyEvent.VK_F3:
			bucarProveedores();
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
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		//JOptionPane.showMessageDialog(view, "paso de celdas");
		switch(comando){
			case "BUSCARPROVEEDOR":
				
				//se verifica que el id que se escribio es un numero
				if(AbstractJasperReports.isNumber(view.getTxtIdProveedor().getText())){
					
					myProveedor=null;
					myProveedor=myDao.buscarPorId(Integer.parseInt(view.getTxtIdProveedor().getText()));
					
					//se verifica que se encontro el proveedor
					if(myProveedor!=null){
						
						cargarView();
						
					}else
					{
						view.getTxtIdProveedor().requestFocusInWindow();
						view.getTxtIdProveedor().selectAll();
						view.getTxtIdProveedor().setText("");
						JOptionPane.showMessageDialog(view, "No se encontro el proveedor","Error",JOptionPane.ERROR_MESSAGE);
						
					}
					
				}else{
					JOptionPane.showMessageDialog(view, "Debe escribir un numero","Error",JOptionPane.ERROR_MESSAGE);
					view.getTxtIdProveedor().requestFocusInWindow();
					view.getTxtIdProveedor().selectAll();
				}
				break;
			case "BUSCARPROVEEDORES":
				bucarProveedores();
					
				break;
			case "PAGAR":
				 pagar();
				break;
			case "CERRAR":
				view.setVisible(false);
			break;
		
		}
	
		
	}

	private void bucarProveedores() {
		// TODO Auto-generated method stub
		ViewListaProveedor vListaProveedores= new ViewListaProveedor(view);
		CtlProveedoresBuscar ctlProveedoresBuscar= new CtlProveedoresBuscar(vListaProveedores);
		vListaProveedores.getTxtBuscar().requestFocusInWindow();
		boolean resultado=ctlProveedoresBuscar.buscarProveedores(view);
		if(resultado){
			myProveedor=ctlProveedoresBuscar.getProveedor();
			cargarView();
		}
		
		vListaProveedores.dispose();
		ctlProveedoresBuscar=null;
	}

	private void pagar() {
		// TODO Auto-generated method stub
		setRecibo();
		
		if(this.validar()){
			
			//se manda aguardar el recibo con los pagos realizados
			boolean resulta=this.myReciboDao.registrar(myRecibo);
			
			if(resulta){
				
				this.resul=true;
				myRecibo.setNoRecibo(myReciboDao.idUltimoRecibo);
				
				//JOptionPane.showMessageDialog(view, "El recibo se guardo correctamente.");
				this.view.setVisible(false);
				
				//resul=true;
				try {
				
					//AbstractJasperReports.createReport(conexion.getPoolConexion().getConnection(), 5, myRecibo.getNoRecibo());
					AbstractJasperReports.createReportReciboPagoCaja(ConexionStatic.getPoolConexion().getConnection(), myRecibo.getNoRecibo());
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

	private void setRecibo() {
		// TODO Auto-generated method stub
		Banco cuenta=(Banco) view.getCbFormaPago().getSelectedItem();
		myRecibo.setFormaPago(cuenta);
		myRecibo.setProveedor(myProveedor);
		myRecibo.setConcepto(view.getTxtrObservacion().getText());
		myRecibo.setTotal(new BigDecimal(view.getTxtTotal().getText()));
		
		myRecibo.setTotalLetras(NumberToLetterConverter.convertNumberToLetter(myRecibo.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
		
		
	}
	
	public boolean validar(){
		boolean comprobado=false;
		//Double saldo=0.0;
		//Double pago=0.0;
		if(view.getTxtNombreProveedor().getText().trim().length()==0){
			JOptionPane.showMessageDialog(view, "El no existe el proveedor");
		}
		else
			if(view.getTxtTotal().getText().trim().length()==0){
					JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
					view.getTxtTotal().requestFocusInWindow();
				}else
					if(view.getTxtSaldo().getText().trim().length()==0){
						JOptionPane.showMessageDialog(view, "El proveedor no tiene saldo pendiente");	
					}
					else
						if(Double.parseDouble(view.getTxtTotal().getText())==0){
							JOptionPane.showMessageDialog(view, "Coloque la cantidad a pagar");
							view.getTxtTotal().requestFocusInWindow();
						}
						else{
						
							/*saldo=Double.parseDouble(view.getTxtSaldo().getText());
							pago=Double.parseDouble(view.getTxtTotal().getText());
							
							if()*/
						
							comprobado=true;
						}
			
		return comprobado;
	}

	private void cargarView() {
		// TODO Auto-generated method stub
		view.getTxtIdProveedor().setText(myProveedor.getId()+"");
		view.getTxtNombreProveedor().setText(myProveedor.getNombre());
		view.getTxtSaldo().setText(""+myProveedor.getSaldo());
		view.getTxtTotal().requestFocusInWindow();
		view.getTxtTotal().selectAll();
		
	}

}
