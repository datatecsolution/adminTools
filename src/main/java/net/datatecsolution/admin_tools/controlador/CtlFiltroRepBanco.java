package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.CuentaBanco;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.CuentaBancosDao;
import net.datatecsolution.admin_tools.view.ViewFiltroSaldoBanco;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

public class CtlFiltroRepBanco implements ActionListener, KeyListener  {
	private ViewFiltroSaldoBanco view;
	
	private CuentaBanco myCuentaBanco;
	private CuentaBancosDao myCuentaBancoDao;

	private BancosDao myBancoDao;
	
	
	
	public CtlFiltroRepBanco(ViewFiltroSaldoBanco v){
		
		view =v;
		view.conectarCtl(this);
		
		myCuentaBanco=new CuentaBanco();
		myCuentaBancoDao=new CuentaBancosDao();
		myBancoDao=new BancosDao();
		
		cargarComboBox(myBancoDao.getCuentas());
		
	
		
		
		
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
			this.view.getCbxBancos().removeAllItems();
		
			this.view.getCbxBancos().setSelectedIndex(0);
		}
	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":
					Banco miBanco=(Banco)view.getCbxBancos().getSelectedItem();
					
					try {
						
						AbstractJasperReports.createReportSaldoBancos(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(), view.getBuscar2().getDate(),miBanco.getId());
						
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
		
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
