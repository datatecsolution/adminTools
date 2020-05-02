package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.view.ViewCambioPago;

import javax.swing.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class CtlCambioPago implements ActionListener,ItemListener, WindowListener ,KeyListener{
	private ViewCambioPago view=null;
	private boolean estadoPago=false;
	private BigDecimal efectivo;
	private BigDecimal cobroTarjeta;
	private BigDecimal cobroEfectivo;
	private BigDecimal cambio;
	private int formaPago=1;
	private String refencia;
	
	private BigDecimal total;
	private BigDecimal totalCobro;
	
	
	public CtlCambioPago(ViewCambioPago v,BigDecimal t){
		total=t;
		view=v;
		view.conectarCtl(this);
		view.getTxtEfectivo().requestFocusInWindow();
		
		totalCobro=new BigDecimal(0);
		//view.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		
		String comando=e.getActionCommand();
		switch (comando){
		case "CAMBIO":
			if(view.getTxtEfectivo().getText().length()==0){
				view.getTxtEfectivo().setText("0");
			}
			//se validad que sea un numero real o entero el que se ingreso
			if(AbstractJasperReports.isNumber(view.getTxtEfectivo().getText()) || AbstractJasperReports.isNumberReal(view.getTxtEfectivo().getText())){
				
				
				//totalCobro=BigDecimal.ZERO;
				efectivo=BigDecimal.ZERO;
				efectivo=new BigDecimal(view.getTxtEfectivo().getText());
				
				
				
				//totalCobro=totalCobro.add(efectivo);
				
				
				
				
				//se para el foco a el pago con tarjeta
				view.getTxtReferencia().requestFocusInWindow();
				
			}else{//si no es un numero que se ingreso se emite un error
				estadoPago=false;
				JOptionPane.showMessageDialog(view, "Escriba un cantidad valida","Error",JOptionPane.ERROR_MESSAGE);
				view.getTxtEfectivo().selectAll();
				view.getTxtEfectivo().requestFocusInWindow();
			}
			
			
			
		break;
		case "CERRAR":
			salir();
			break;
		case "COBRAR":
			cobrar();
			break;
		case "IMPRIMIR":
			cobrar();
			break;
		case "TARJETA":
			
			totalCobro=BigDecimal.ZERO;
			if(view.getTxtReferencia().getText().length()==0){
				view.getTxtReferencia().setText("0");
			}
			//se validad que sea un numero real o entero el que se ingreso
			if(AbstractJasperReports.isNumber(view.getTxtReferencia().getText()) || AbstractJasperReports.isNumberReal(view.getTxtReferencia().getText())){
				
					
					
					cobroTarjeta=new BigDecimal(view.getTxtReferencia().getText());
					
					//si el pago se hace solo con efectivo se calacula la cantidad de efectivo que se pago
					if(cobroTarjeta.doubleValue()==0){
						
						if(efectivo.doubleValue()>=total.doubleValue()){
							cobroEfectivo=new BigDecimal(total.doubleValue());
						}else{
							cobroEfectivo=new BigDecimal(efectivo.doubleValue());
						}
						//cobroEfectivo=efectivo.subtract(total);
						totalCobro=totalCobro.add(cobroEfectivo);
						
					}else{
						
						cobroEfectivo=total.subtract(cobroTarjeta);
						totalCobro=totalCobro.add(efectivo);
						totalCobro=totalCobro.add(cobroTarjeta);
						
					}
					
					
					
					//JOptionPane.showMessageDialog(view, "Hola "+total.doubleValue());
					//se valida que el total del pago sea el total de la factura
					if(totalCobro.doubleValue()>=total.doubleValue()){
						estadoPago=true;
					}else{
						JOptionPane.showMessageDialog(view, "Cantidad de efectivo incorrecta. No cubre el total de la factura."+
														" \nTotal efectivo:"+efectivo.doubleValue()+
														" \nTotal Tarjeta:"+cobroTarjeta.doubleValue()+
														" \nTotal cobro:"+totalCobro.doubleValue()+
														" \nTotal Factura:"+total.doubleValue(),"Error",JOptionPane.ERROR_MESSAGE);
						view.getTxtEfectivo().selectAll();
						view.getTxtEfectivo().requestFocusInWindow();
						estadoPago=false;
					}
			}else{//si no es un numero que se ingreso se emite un error
				estadoPago=false;
				JOptionPane.showMessageDialog(view, "Escriba un cantidad valida.","Error",JOptionPane.ERROR_MESSAGE);
				view.getTxtReferencia().selectAll();
				view.getTxtReferencia().requestFocusInWindow();
			}
			
			
			//parseBigDecimal( view.getTxtEfectivo().getText());
			if(this.estadoPago){ 
				
				BigDecimal totalPago=efectivo.add(cobroTarjeta);
				
				cambio=totalPago.subtract(total);
				
				view.getTxtCambio().setText(""+cambio.setScale(2, BigDecimal.ROUND_HALF_EVEN));
				/*
				int resul=JOptionPane.showConfirmDialog(view, "Imprimir Factura?","Confirmar factura", JOptionPane.YES_NO_OPTION);
				//sin confirmo la anulacion
				if(resul==0){
					this.cobrar();
					
				}*/
				view.getTxtCambio().requestFocusInWindow();
				//view.getBtnCobrar().requestFocusInWindow();
			}
				
			break;
		}
	}
	
	public int getFormaPago(){
		return formaPago;
	}
	public String getRefencia(){
		return refencia;
	}

	public boolean pagar() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return estadoPago;
	}
	public BigDecimal getEfectivo(){
		return efectivo;
	}
	public BigDecimal getCambio(){
		return cambio;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		/*if(view.getTglbtnEfectivo().isSelected()){
			//JOptionPane.showMessageDialog(view, "Efectivo");
			view.getPanelTarjeta().setVisible(true);
			view.getPanelEfectivo().setVisible(false);
			view.getTxtEfectivo().requestFocusInWindow();
		}
		
		if(view.getTglbtnTarjetaCredito().isSelected()){
			view.getPanelEfectivo().setVisible(true);
			view.getPanelTarjeta().setVisible(false);
			view.getTxtReferencia().requestFocusInWindow();
			
			//JOptionPane.showMessageDialog(view, "Tarjeta Credito");
		}*/
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
		salir();
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
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
			salir();
		}
		if(e.getKeyCode()==KeyEvent.VK_F2){
			cobrar();
		}
		
		
		
	}

	private void salir() {
		// TODO Auto-generated method stub
		estadoPago=false;
		this.view.setVisible(false);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_F3){
			cobrar();
		}
	}

	private void cobrar() {
		// TODO Auto-generated method stub

		
	//	if(view.getTglbtnEfectivo().isSelected()){
			if(estadoPago){
				this.formaPago=1;
				this.view.setVisible(false);
			}else{
				view.getTxtEfectivo().requestFocusInWindow();
				view.getToolkit().beep();
			}
			
	//	}
	/*	if(view.getTglbtnTarjetaCredito().isSelected()){
			if(view.getTxtReferencia().getText().trim().length()==0){
				
				view.getTxtReferencia().requestFocusInWindow();
				JOptionPane.showMessageDialog(view, "Escriba la referencia de pago","Error",JOptionPane.ERROR_MESSAGE);
			}else{
				this.formaPago=2;
				this.refencia=view.getTxtReferencia().getText();
				this.estadoPago=true;
				view.setVisible(false);
			}
				
			
		}*/
		
	
	}

	/**
	 * @return the cobroTarjeta
	 */
	public BigDecimal getCobroTarjeta() {
		return cobroTarjeta;
	}

	/**
	 * @return the cobroEfectivo
	 */
	public BigDecimal getCobroEfectivo() {
		return cobroEfectivo;
	}

	public BigDecimal getTotalPago() {
		// TODO Auto-generated method stub
		return efectivo.add(cobroTarjeta);
	}

}
