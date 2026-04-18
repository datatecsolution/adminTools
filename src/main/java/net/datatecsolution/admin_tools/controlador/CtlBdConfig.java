package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.config.Cifrado;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.view.BdConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class CtlBdConfig implements ActionListener {
	private BdConfig view=null;

	public CtlBdConfig(BdConfig v){
		view=v;
		cargarDatos();
		view.conectarControlador(this);
		view.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando=e.getActionCommand();

		switch (comando){
		case "CANCELAR":
			view.setVisible(false);
			break;
		case "GUARDAR":
			 guardar();
			break;
		}
	}

	private void guardar() {
		Properties props = new Properties();
		props.setProperty("db.server", view.getTxtUrl().getText().trim());
		props.setProperty("db.login", view.getTxtUser().getText().trim());
		props.setProperty("db.password", view.getTxtPwd().getText().trim());
		props.setProperty("db.name", view.getTxtDataBase().getText().trim());
		props.setProperty("db.timezone", "GMT-6");

		Cifrado cifrado = new Cifrado();
		try {
			cifrado.guardar(props);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(view,
				"No se pudo guardar la configuración:\n" + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ConexionStatic.cargarConfiguracion();
		ConexionStatic.conectarBD();

		JOptionPane.showMessageDialog(view,
			"Configuración guardada. La conexión ha sido actualizada.",
			"Configuración", JOptionPane.INFORMATION_MESSAGE);
		view.setVisible(false);
	}

	public void cargarDatos() {
		Properties props = null;
		Cifrado cifrado = new Cifrado();

		try {
			props = cifrado.cargar();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (props == null) {
			props = new Properties();
		}

		view.getTxtUrl().setText(props.getProperty("db.server", "127.0.0.1"));
		view.getTxtUser().setText(props.getProperty("db.login", "admin"));
		view.getTxtPwd().setText(props.getProperty("db.password", ""));
		view.getTxtDataBase().setText(props.getProperty("db.name", "admin_tools"));
	}
}
