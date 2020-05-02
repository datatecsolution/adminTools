package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.DatosFacturacion;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DatosFacturacionDao;
import net.datatecsolution.admin_tools.view.ViewCrearDatosFacturacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CtlDatosFacturacion implements ActionListener {
	
	private ViewCrearDatosFacturacion view;
	
	private DatosFacturacion myDatosF=new DatosFacturacion();
	private CajaDao cajaDao;
	private DatosFacturacionDao myDatosFdao;
	private boolean resultaOperacion=false;
	
	public CtlDatosFacturacion(ViewCrearDatosFacturacion v){
		view=v;
		view.conectarCtl(this);
		cajaDao=new CajaDao();
		myDatosFdao=new DatosFacturacionDao();
		cargarComboBox();
		
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCajas().setLista(this.cajaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbCaja().removeAllItems();
	
		this.view.getCbCaja().setSelectedIndex(0);
	}
	
	private boolean validar(){
		boolean resul=false;
		Caja c=(Caja)view.getCbCaja().getSelectedItem();
		if(c==null){
			JOptionPane.showMessageDialog(view, "Debe seleccionar una caja, sino hay debe crearla primero.","Error de validacion",JOptionPane.ERROR_MESSAGE);
			view.getCbCaja().requestFocusInWindow();
			
		}else if((!AbstractJasperReports.isNumber(view.getTxtFacturaInicial().getText())) || view.getTxtFacturaInicial().getText().trim().length()==0){
				JOptionPane.showMessageDialog(view, "Debe ingresar el numero de factura inicial correctamente.","Error de validacion",JOptionPane.ERROR_MESSAGE);
				view.getTxtFacturaInicial().selectAll();
				view.getTxtFacturaInicial().requestFocusInWindow();
				view.getTxtFacturaInicial().setSelectionColor(Color.RED);
				//view.getTxtFacturaInicial()
			}else if((!AbstractJasperReports.isNumber(view.getTxtFacturaFinal().getText())) || view.getTxtFacturaFinal().getText().trim().length()==0){
					JOptionPane.showMessageDialog(view, "Debe ingresar el numero de factura final correctamente.","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getTxtFacturaFinal().requestFocusInWindow();
					
				}else if(view.getTxtCodigoFacturacion().getText().trim().length()==0){
					JOptionPane.showMessageDialog(view, "Debe ingresar la codificacion de la facturas.","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getTxtCodigoFacturacion().requestFocusInWindow();
					
				}
				else if((!AbstractJasperReports.isNumber(view.getTxtCantidadOtorgada().getText())) || view.getTxtCantidadOtorgada().getText().trim().length()==0){
					JOptionPane.showMessageDialog(view, "Debe ingresar la cantidad de facturas otorganas correctamente.","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getTxtCantidadOtorgada().requestFocusInWindow();
					
				}else if(view.getFechaLimite().isValid()==true){
					JOptionPane.showMessageDialog(view, "Ingrese la fecha limite de emision.","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getFechaLimite().requestFocusInWindow();
					
				}else if(view.getTxtCai().getText().trim().length()==0){
					JOptionPane.showMessageDialog(view, "Debe ingresar la CAI de la facturas.","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getTxtCai().requestFocusInWindow();
					
				}else if(Integer.parseInt(view.getTxtFacturaFinal().getText())<Integer.parseInt(view.getTxtFacturaInicial().getText())){
					JOptionPane.showMessageDialog(view, "El numero de factura final debe ser mayor al numero de factura inicial","Error de validacion",JOptionPane.ERROR_MESSAGE);
					view.getTxtFacturaFinal().selectAll();
					view.getTxtFacturaFinal().requestFocusInWindow();
					view.getTxtFacturaFinal().setSelectionColor(Color.RED);
					
				}else
					resul=true;
		
		return resul;
	}
	private void setModeloFromView(){
		myDatosF.setFacturaInicial(Integer.parseInt(view.getTxtFacturaInicial().getText()));
		myDatosF.setFacturaFinal(Integer.parseInt(view.getTxtFacturaFinal().getText()));
		myDatosF.setCantOtorgada(Integer.parseInt(view.getTxtCantidadOtorgada().getText()));
		myDatosF.setCodigoFacturas(view.getTxtCodigoFacturacion().getText());
		myDatosF.setCAI(view.getTxtCai().getText());
		
		//se establece la caja seleccionada
		Caja c=(Caja)view.getCbCaja().getSelectedItem();
		myDatosF.setCaja(c);
		
		
		//se crear el formato para la fecha
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//se recoge la fecha de compra de la view
		String date = sdf.format(this.view.getFechaLimite().getDate());
		
		myDatosF.setFechaLimite(date);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
			case "GUARDAR":
					if(validar()){//se valida los datos de la view
						
						setModeloFromView();//se cargan los datos de la view
						
						//se verifica que no se estableca un numero de factura que ya esta en la base de datos
						if(myDatosFdao.verificarFacturacionFactInicial(myDatosF)){
						
							if(myDatosFdao.registrar(myDatosF)){//se guardan los datos en la base de datos
								
								JOptionPane.showMessageDialog(view, "Los datos de la facturacion se guardo correctamente.","Exito",JOptionPane.INFORMATION_MESSAGE);
								this.resultaOperacion=true;
								view.setVisible(false);
							}
						}else{
							JOptionPane.showMessageDialog(view, "El numero de factura inicial ya existe.","Error validacion",JOptionPane.ERROR_MESSAGE);
							view.getTxtFacturaInicial().selectAll();
							view.getTxtFacturaInicial().requestFocusInWindow();
						}
					}
				break;
				
			case "CANCELAR":
				view.setVisible(false);
				break;
			case "ACTUALIZAR":
				if(validar()){//se valida los datos de la view
					
					setModeloFromView();//se cargan los datos de la view
					
					//se verifica que no se estableca un numero de factura que ya esta en la base de datos
					if(myDatosFdao.verificarFacturacionFactInicial(myDatosF)){
						
						if(myDatosFdao.actualizar(myDatosF)){
							JOptionPane.showMessageDialog(view, "Los datos de la facturacion se actualizaron correctamente.","Exito",JOptionPane.INFORMATION_MESSAGE);
							this.resultaOperacion=true;
							view.setVisible(false);
						}
						
					}else{
						JOptionPane.showMessageDialog(view, "El numero de factura inicial ya existe.","Error en la base de datos",JOptionPane.ERROR_MESSAGE);
						view.getTxtFacturaInicial().selectAll();
						view.getTxtFacturaInicial().requestFocusInWindow();
					}
				}
					
				break;
		}
		
	}

	public boolean agregarDatos() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return resultaOperacion;
	}

	/**
	 * @return the myDatosF
	 */
	public DatosFacturacion getDatosF() {
		return myDatosF;
	}

	public boolean actualizarDatos(DatosFacturacion myDato) {
		// TODO Auto-generated method stub
		//se asigna el modelo envia al modelo actual
		this.myDatosF=myDato;
		//se esconde el boton guardar y se muestra el boton actualizar
		view.getBtnGuardar().setVisible(false);
		view.getBtnActualizar().setVisible(true);
		cargarDatosView();
		view.setVisible(true);
		return this.resultaOperacion;
	}

	private void cargarDatosView() {
		// TODO Auto-generated method stub
		
		//se consegue la caja en donde se agrego los nuevos datos de facturacion
		Caja c=myDatosF.getCaja();
		
		//se busca el item en el comboBox para seleccionar
		int cajaGuarda=view.getModeloListaCajas().buscarCaja(c);
		
		//se selecciona la caja en el comboBox que se guardo para que cambie los datos de facturacion
		view.getCbCaja().setSelectedIndex(cajaGuarda);
		
		view.getTxtCai().setText(myDatosF.getCAI());
		
		view.getTxtFacturaInicial().setText(myDatosF.getFacturaInicial()+"");
		
		view.getTxtFacturaFinal().setText(myDatosF.getFacturaFinal()+"");
		
		view.getTxtCantidadOtorgada().setText(myDatosF.getCantOtorgada()+"");
		
		view.getTxtCodigoFacturacion().setText(myDatosF.getCodigoFacturas());
		
		//se crear el formato para la fecha
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date date = null;
		try {
			date = sdf.parse(myDatosF.getFechaLimite());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view.getFechaLimite().setDate(date);
		
	}

}
