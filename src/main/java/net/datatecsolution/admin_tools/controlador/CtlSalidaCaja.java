package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.SalidaCajaDao;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;
import net.datatecsolution.admin_tools.view.ViewSalidaCaja;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;

public class CtlSalidaCaja implements ActionListener, KeyListener {
	
	private ViewSalidaCaja view;
	private SalidaCaja mySalida;
	private SalidaCajaDao myDao;
	private boolean resul=false;
	private Empleado myEmpleado=null;
	private EmpleadoDao myEmpleadoDao;

	
	private BancosDao myFormaPago;

	public CtlSalidaCaja(ViewSalidaCaja v ) {
		// TODO Auto-generated constructor stub
	
		view=v;
		
		mySalida=new SalidaCaja();
		
		myEmpleadoDao=new EmpleadoDao();
		
		//se busca el empleado por defecto es con el id 0
		myEmpleado=myEmpleadoDao.buscarPorId(1);
		if(myEmpleado!=null){
			view.getTxtEmpleado().setText(myEmpleado.toString());
		}

		
		myDao=new SalidaCajaDao();
		
		view.conectarControlador(this);
		
		myFormaPago=new BancosDao();
		cargarComboBox(myFormaPago.getCuentasExectoEfectivo());
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
	
	public SalidaCaja getSalidaCaja(){
		return this.mySalida;
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
				setSalida();
				if(myDao.registrar(mySalida)){
					resul=true;
					
					try {
						//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Cierre_Caja_Saint_Paul.jasper",1 );
						AbstractJasperReports.createReportSalidaCaja(ConexionStatic.getPoolConexion().getConnection(), myDao.getIdRegistrado());
						
						//AbstractJasperReports.Imprimir2();
						//JOptionPane.showMessageDialog(view, "Se realizo el corte correctamente.");
						
						view.setVisible(false);
						
						//se verfica si esta activo imprimir el reporte
						if(ConexionStatic.getUsuarioLogin().getConfig().isImprReportSalida())
						{
							AbstractJasperReports.imprimierFactura();
						}
						//se verfica si esta activo mostrar el reporte 
						if(ConexionStatic.getUsuarioLogin().getConfig().isShowReportSalida())
						{
							AbstractJasperReports.showViewer(view);
						}
						
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

	private void setSalida() {
		// TODO Auto-generated method stub
		mySalida.setCantidad(new BigDecimal(view.getTxtCantidad().getText()));
		mySalida.setConcepto(view.getTxtConcepto().getText());
		mySalida.setEmpleado(myEmpleado);
		
		Banco miCuenta=(Banco)view.getCbFormaPago().getSelectedItem();
		mySalida.setBanco(miCuenta);
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
			mySalida=null;
			
	        view.setVisible(false);
	        view=null;
	      }
		
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

	
}
