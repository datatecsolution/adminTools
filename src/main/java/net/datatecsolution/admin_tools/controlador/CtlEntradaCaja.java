package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.EntradaCaja;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.EntradaCajaDao;
import net.datatecsolution.admin_tools.view.ViewEntradaCaja;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;



public class CtlEntradaCaja implements ActionListener, KeyListener {
	
	private ViewEntradaCaja view;
	private EntradaCaja myEntrada;
	private EntradaCajaDao myDao;
	private boolean resul=false;
	private BancosDao myFormaPago;
	//private static final Pattern numberPattern=Pattern.compile("-?\\d+");

	public CtlEntradaCaja(ViewEntradaCaja v ) {
		// TODO Auto-generated constructor stub
	
		view=v;
		
		myEntrada=new EntradaCaja();
		
		
		myFormaPago=new BancosDao();
		
		cargarComboBox(myFormaPago.getCuentasExectoEfectivo());

		
		myDao=new EntradaCajaDao();
		
		view.conectarControlador(this);
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
	
	public EntradaCaja getSalidaCaja(){
		return this.myEntrada;
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
				setEntrada();
				
			
				if(myDao.registrar(myEntrada)){
					resul=true;
					
					try {
						//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
						AbstractJasperReports.createReportEntradaCaja(ConexionStatic.getPoolConexion().getConnection(), myDao.getIdRegistrado());
						
						//AbstractJasperReports.Imprimir2();
						//JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.");
						
						view.setVisible(false);
						//se verfica si esta activo imprimir el reporte
						if(ConexionStatic.getUsuarioLogin().getConfig().isImprReportEntrada())
						{
							AbstractJasperReports.imprimierFactura();
						}
						//se verfica si esta activo mostrar el reporte 
						if(ConexionStatic.getUsuarioLogin().getConfig().isShowReportEntrada())
						{
							AbstractJasperReports.showViewer(view);
						}
						
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

	private void setEntrada() {
		// TODO Auto-generated method stub
		myEntrada.setCantidad(new BigDecimal(view.getTxtCantidad().getText()));
		myEntrada.setConcepto(view.getTxtConcepto().getText());
		
		Banco miCuenta=(Banco)view.getCbFormaPago().getSelectedItem();
		
		myEntrada.setCodigoCuenta(miCuenta.getId());
		myEntrada.setBanco(miCuenta);
		//JOptionPane.showMessageDialog(view,miCuenta.toString());
	}

	private boolean validar() {
		// TODO Auto-generated method stub
		boolean resul=false;
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

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			myEntrada=null;
			
	        view.setVisible(false);
	        view=null;
	      }
		
	}
	
	
}
