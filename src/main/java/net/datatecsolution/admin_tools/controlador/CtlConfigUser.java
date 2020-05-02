package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.dao.ConfigUserFactDao;
import net.datatecsolution.admin_tools.view.ViewConfigUser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;



public class CtlConfigUser implements ActionListener {
	
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
		setUserNext(true);
		view.setVisible(true);
	}

	private void setUserNext(boolean bandera) {
		// TODO Auto-generated method stub
		if(configs!=null){
			if(bandera){
				selector=0;
			}else{
				selector++;
			}
			
			if(selector<=configs.size() && selector>=0){
				configEnPantalla=configs.get(selector);
				setView();
			}
		}
	}

	private void setView() {
		// TODO Auto-generated method stub
		view.getTxtUsuario().setText(configEnPantalla.getUser().getUser());
		view.getTglbtnImprimirSalida().setSelected(configEnPantalla.isImprReportSalida());
		view.getTglbtnImprimirSalida().setSelected(configEnPantalla.isShowReportSalida());
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
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "NEXT":
			selector++;
			this.setUserNext(false);
			break;
		case "BEFORE":
			break;
		}
	}

}
