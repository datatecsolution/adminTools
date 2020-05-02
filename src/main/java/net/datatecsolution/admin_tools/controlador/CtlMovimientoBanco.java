package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.MovimientoBanco;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.MovimientoBancoDao;
import net.datatecsolution.admin_tools.view.ViewMovimientoBanco;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;

public class CtlMovimientoBanco implements ActionListener, KeyListener {
	
	private ViewMovimientoBanco view;
	private MovimientoBanco myMovimiento;
	private MovimientoBancoDao myDao;
	private boolean resul=false;
	

	
	private BancosDao myFormaPago;

	public CtlMovimientoBanco(ViewMovimientoBanco v ) {
		// TODO Auto-generated constructor stub
	
		view=v;
		
		myMovimiento=new MovimientoBanco();
		
		myDao=new MovimientoBancoDao();
		
		view.conectarControlador(this);
		
		myFormaPago=new BancosDao();
		cargarComboBox(myFormaPago.getCuentas());
		view.setVisible(true);
		
		
	}
	
	private void cargarComboBox(Vector<Banco> bancos){
		
		if(bancos!=null){
			//se obtiene la lista de los formas de pago y se le pasa al modelo de la lista
			this.view.getModeloCuentasBancos().setLista(bancos);
			
			
			//se remueve la lista por defecto
			this.view.getCbFormaPago().removeAllItems();
		
			this.view.getCbFormaPago().setSelectedIndex(0);
		}
	
	}
	
	public MovimientoBanco getMovimiento(){
		return this.myMovimiento;
	}
	public boolean getResultado(){
		
		return resul;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "GUARDAR":
			if(validar()){
				setMovimiento();
				if(myDao.registrar(myMovimiento)){
					resul=true;
					
					try {
						
						AbstractJasperReports.createReportMovimientoBanco(ConexionStatic.getPoolConexion().getConnection(), myDao.getIdRegistrado());
						
						//AbstractJasperReports.Imprimir2();
						//JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.");
						
						view.setVisible(false);
						AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
						
						//this.view.setModal(false);
						//AbstractJasperReports.imprimierFactura();
						
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
				}else{
					JOptionPane.showMessageDialog(view, "Ocurrio un problema para registrar la salida");
				}
				
			}
			
			break;
			
		case "CANCELAR":
			view.setVisible(false);
			break;
		}
		
	}

	private void setMovimiento() {
		// TODO Auto-generated method stub
		myMovimiento.setCantidad(new BigDecimal(view.getTxtMonto().getText()));
		myMovimiento.setDescripcion(view.getTxtrDescripcion().getText());
		
		Banco miCuenta=(Banco)view.getCbFormaPago().getSelectedItem();
		myMovimiento.setBanco(miCuenta);
		myMovimiento.setCodigoCuenta(miCuenta.getId());
		myMovimiento.setCodigoTipoMovimiento(view.getCbTipoMovimiento().getSelectedIndex()+1);
		
	}

	private boolean validar() {
		// TODO Auto-generated method stub
		
		boolean resul=false;
		
		if(view.getTxtrDescripcion().getText().trim().length()==0){
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos","Erro validacion",JOptionPane.ERROR_MESSAGE);
			view.getTxtrDescripcion().requestFocusInWindow();
		}
		else
			if(view.getTxtMonto().getText().trim().length()==0 ){
				JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos","Erro validacion",JOptionPane.ERROR_MESSAGE);
				view.getTxtMonto().requestFocusInWindow();
			}
			else if(!AbstractJasperReports.isNumber(view.getTxtMonto().getText()) || !AbstractJasperReports.isNumberReal(view.getTxtMonto().getText())){
				
						JOptionPane.showMessageDialog(view, "La cantidad debe ser un numero","Erro validacion",JOptionPane.ERROR_MESSAGE);
						view.getTxtMonto().selectAll();
						view.getTxtMonto().requestFocusInWindow();
					}
					else{
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
			
				break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			
			myMovimiento=null;
			
	        view.setVisible(false);
	        view=null;
	      }
		
	}
	

	
}
