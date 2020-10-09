package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.view.ViewFiltroEntrada;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

public class CtlFiltroRepEntradas implements ActionListener, KeyListener  {
	private ViewFiltroEntrada view;
	private Banco banco =null;
	private BancosDao bancosDao;


	public CtlFiltroRepEntradas(ViewFiltroEntrada v){
		
		view =v;
		view.conectarCtl(this);
		bancosDao =new BancosDao();

		cargarComboBox(bancosDao.getCuentas());
		
		Date horaLocal=new Date();
		view.getBuscar1().setDate(horaLocal);
		view.getBuscar2().setDate(horaLocal);
		
		view.setVisible(true);
		
	}

	private void cargarComboBox(Vector<Banco> bancos){

		if(bancos!=null){
			//se obtiene la lista de los formas de pago y se le pasa al modelo de la lista
			this.view.getModeloCuentasBancos().setLista(bancos);


			//se remueve la lista por defecto
			this.view.getCbCuentasBancos().removeAllItems();

			this.view.getCbCuentasBancos().setSelectedIndex(0);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":

				Banco miCuenta=(Banco)view.getCbCuentasBancos().getSelectedItem();

					
					try {
						
						AbstractJasperReports.createReportEntradasBanco(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(), view.getBuscar2().getDate(), miCuenta.getId());
						
						AbstractJasperReports.showViewer(view);
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}


					
			break;
		}
		
	
		
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
			//buscarBanco();
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	


}
