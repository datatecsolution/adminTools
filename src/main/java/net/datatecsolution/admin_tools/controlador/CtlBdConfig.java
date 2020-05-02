package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.view.BdConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlBdConfig implements ActionListener {
	private BdConfig view=null;
	/*private Properties props;
	private Properties propsout;
	private InputStream file=null;
	
	private OutputStream output = null;
	private URL url=null;
	private File archivoOut=null;*/
	
	public CtlBdConfig(BdConfig v){
		
		view=v;
		
		
		//App.loadFileConfig();
		//App.saveFileConfigDefaul();
		
		cargarDatos();
		view.conectarControlador(this);
		
		view.setVisible(true);
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch (comando ){
		case "CANCELAR":
			view.setVisible(false);
			break;
		case "GUARDAR":
			 guardar();
			break;
		
		}
		
	}
	
	private void guardar() {
		// TODO Auto-generated method stub
		
		
		//App.setDataBase(dataBase);
		App.setServerName(view.getTxtUrl().getText());
		
		App.setUserDb(view.getTxtUser().getText());
		
		App.setPwUserDb(view.getTxtPwd().getText());
		
		App.setDataBase(view.getTxtDataBase().getText());
		
		

		
		
		
		
		view.setVisible(false);
		
		System.exit(0);
		
		
	}
	
	
	
	public void cargarDatos() {
		
		//JOptionPane.showMessageDialog(view,"" + App.getPwUserDb());
		view.getTxtUrl().setText(App.getServerName());
	     view.getTxtUser().setText(App.getUserDb());
	     view.getTxtPwd().setText(App.getPwUserDb());
	    view.getTxtDataBase().setText(App.getDataBase());
		
		
		
	 
	    
	}

}
