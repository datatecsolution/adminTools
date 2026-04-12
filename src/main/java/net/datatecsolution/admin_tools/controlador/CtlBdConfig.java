package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.config.Cifrado;
import net.datatecsolution.admin_tools.view.BdConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

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
		case "CLAVE":
			//motrara los datos de conexion guardados
			break;
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
		/*
		
		//App.setDataBase(dataBase);
		App.setServerName(view.getTxtUrl().getText());
		
		App.setUserDb(view.getTxtUser().getText());
		
		App.setPwUserDb(view.getTxtPwd().getText());
		
		App.setDataBase(view.getTxtDataBase().getText());
		
		

		
		
		
		*/
		// Creamos el objeto Cifrado con la clave proporcionada
		Cifrado cifrado = new Cifrado();
		// Creamos un objeto Properties para guardar los datos de conexión
		Properties datos = new Properties();
		datos.setProperty("host", view.getTxtUrl().getText());
		datos.setProperty("usuario",view.getTxtUser().getText());
		datos.setProperty("pwd", view.getTxtPwd().getText());
		datos.setProperty("dataBase", view.getTxtDataBase().getText());

		// Guardamos los datos cifrados en un archivo
		try {
			cifrado.guardarDatosCifrados(datos);
		} catch (Exception e) {
			e.printStackTrace();
		}

		view.setVisible(false);
		
		//System.exit(0);
		
		
	}
	
	
	
	public void cargarDatos() {
		
		//JOptionPane.showMessageDialog(view,"" + App.getPwUserDb());
		view.getTxtUrl().setText(App.getServerName());
	     view.getTxtUser().setText(App.getUserDb());
	     view.getTxtPwd().setText(App.getPwUserDb());
	    view.getTxtDataBase().setText(App.getDataBase());
		
		
		
	 
	    
	}

}
