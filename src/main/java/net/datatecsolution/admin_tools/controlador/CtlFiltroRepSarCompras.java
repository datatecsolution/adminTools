package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepSarCompras;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class CtlFiltroRepSarCompras implements ActionListener {
	
	private ViewFiltroRepSarCompras view;
	private CajaDao cajaDao;

	public CtlFiltroRepSarCompras(ViewFiltroRepSarCompras v) {
		// TODO Auto-generated constructor stub
		
		view =v;
		view.conectarCtl(this);
		
		cajaDao=new CajaDao();
		//cargarComboBox();
		
		
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCajas().setLista(this.cajaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbxCajas().removeAllItems();
	
		this.view.getCbxCajas().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GENERAR":
			
			Caja myCaja=(Caja)view.getCbxCajas().getSelectedItem();

			int mes=view.getMes().getMonth()+1;
			int anio=view.getAnio().getYear();
			
			
			
			try {
				
				AbstractJasperReports.createReportSarCompras(ConexionStatic.getPoolConexion().getConnection(),mes, anio, ConexionStatic.getUsuarioLogin().getUser());
				
				AbstractJasperReports.showViewer(view);
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
		
			break;
		}
	}

}
