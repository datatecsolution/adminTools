package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.dao.ConfigUserFactDao;
import net.datatecsolution.admin_tools.view.ViewConfigUser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;



public class CtlConfigUser implements ActionListener, ItemListener {
	
	private ViewConfigUser view;
	private ConfigUserFactDao myDao=null;
	private List<ConfigUserFacturacion> configs=new ArrayList<ConfigUserFacturacion>();
	private ConfigUserFacturacion configEnPantalla=null;
	private int selector=-1;
	
	public CtlConfigUser(ViewConfigUser v){
		view=v;
		view.conectarCtl(this);
		myDao=new ConfigUserFactDao();
		configs=myDao.todos();
		setUserNext();
		view.setVisible(true);
	}

	private void setUserNext() {
		// TODO Auto-generated method stub
		if(configs!=null){


			if( selector+1<configs.size()){
				selector++;
				configEnPantalla=configs.get(selector);
				setView();
				if(selector+1==configs.size()){
					view.getBtnSig().setEnabled(false);
					view.getBtnAtras().setEnabled(true);

				}
			}

		}
	}

	private void setUserBefore() {
		// TODO Auto-generated method stub
		if(configs!=null){


			if( selector-1>=0){
				selector--;
				configEnPantalla=configs.get(selector);
				setView();
				if(selector==0){
					view.getBtnSig().setEnabled(true);
					view.getBtnAtras().setEnabled(false);
				}
			}

		}
	}

	private void setView() {
		// TODO Auto-generated method stub
		view.getTxtUsuario().setText(configEnPantalla.getUser().getUser());






		view.getTglbtnImprimirSalida().setSelected(configEnPantalla.isImprReportSalida());
		view.getTglbtnObservarSalida().setSelected(configEnPantalla.isShowReportSalida());
		view.getTglbtnImprimirEntrada().setSelected(configEnPantalla.isImprReportEntrada());
		view.getTglbtnObservarEntrada().setSelected(configEnPantalla.isShowReportEntrada());
		view.getTglbtnDescPorcentaje().setSelected(configEnPantalla.isDescPorcentaje());
		view.getTglbtnPwdDescuento().setSelected(configEnPantalla.isPwdDescuento());
		view.getTglbtnPwdPrecio().setSelected(configEnPantalla.isPwdPrecio());
		view.getTglbtnVentVendedor().setSelected(configEnPantalla.isVentanaVendedor());
		view.getTglbtnVentObrs().setSelected(configEnPantalla.isVentanaObservaciones());
		view.getTglbtnVenBusq().setSelected(configEnPantalla.isActivarBusquedaFacturacion());
		view.getTglbtnRedPrecioVenta().setSelected(configEnPantalla.isPrecioRedondear());
		view.getTglbtnDescPorcentaje().setSelected(configEnPantalla.isDescPorcentaje());
		view.getTglbtnFactSinInven().setSelected(configEnPantalla.isFacturarSinInventario());
		view.getTglbtnAddClienteCredito().setSelected(configEnPantalla.isAgregarClienteCredito());
		view.getTglbtnCategoriaEnCierre().setSelected(configEnPantalla.isImprReportCategCierre());

		for(int x=0;x<view.getCbFormatoCredito().getItemCount();x++){
			String formato=view.getCbFormatoCredito().getModel().getElementAt(x);
			if(configEnPantalla.getFormatoFacturaCredito().equals(formato)){
				view.getCbFormatoCredito().setSelectedIndex(x);
			}

		}

		for(int x=0;x<view.getCbFacturaContado().getItemCount();x++){
			String formato=view.getCbFacturaContado().getModel().getElementAt(x);
			if(configEnPantalla.getFormatoFactura().equals(formato)){
				view.getCbFacturaContado().setSelectedIndex(x);
			}

		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){


		case "ACTUALIZAR":
					setConfig();
					this.myDao.actualizar(configEnPantalla);
				break;
		
		case "NEXT":
			//selector++;
			setUserNext();

			break;
		case "BEFORE":

			setUserBefore();
			break;
		}
	}
	public void setConfig(){



		configEnPantalla.setImprReportSalida(view.getTglbtnImprimirSalida().isSelected());
		configEnPantalla.setShowReportSalida(view.getTglbtnObservarSalida().isSelected());
		configEnPantalla.setImprReportEntrada(view.getTglbtnImprimirEntrada().isSelected());
		configEnPantalla.setShowReportEntrada(view.getTglbtnObservarEntrada().isSelected());
		configEnPantalla.setDescPorcentaje(view.getTglbtnDescPorcentaje().isSelected());
		configEnPantalla.setPwdDescuento(view.getTglbtnPwdDescuento().isSelected());
		configEnPantalla.setPwdPrecio(view.getTglbtnPwdPrecio().isSelected());
		configEnPantalla.setVentanaVendedor(view.getTglbtnVentVendedor().isSelected());
		configEnPantalla.setVentanaObservaciones(view.getTglbtnVentObrs().isSelected());
		configEnPantalla.setActivarBusquedaFacturacion(view.getTglbtnVenBusq().isSelected());
		configEnPantalla.setPrecioRedondear(view.getTglbtnRedPrecioVenta().isSelected());
		configEnPantalla.setDescPorcentaje(view.getTglbtnDescPorcentaje().isSelected());
		configEnPantalla.setFacturarSinInventario(view.getTglbtnFactSinInven().isSelected());
		configEnPantalla.setAgregarClienteCredito(view.getTglbtnAddClienteCredito().isSelected());
		configEnPantalla.setImprReportCategCierre(view.getTglbtnCategoriaEnCierre().isSelected());

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange()==ItemEvent.SELECTED){

			JComboBox comboBoxChanged = (JComboBox) e.getSource();




			if(comboBoxChanged==view.getCbFormatoCredito()){
				String credito = view.getCbFormatoCredito().getSelectedItem().toString();
				setConfig();
				myDao.actualizarFormatoCredito(credito,configEnPantalla.getUsuario());

			}
			if(comboBoxChanged==view.getCbFacturaContado()){
				String contado = view.getCbFacturaContado().getSelectedItem().toString();
				setConfig();
				myDao.actualizarFormatoContado(contado,configEnPantalla.getUsuario());

			}



		}

	}
}
