
package net.datatecsolution.admin_tools.modelo;

import net.datatecsolution.admin_tools.config.Cifrado;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase que permite conectar con la base de datos
 *
 * @author jdmayorga
 *
 */
public abstract class ConexionStatic implements Runnable {

	private static String bd;
	private static String login;
	private static String password;
	private static String server;
	private static String url;
	private static String urlTemplate;
	private static String driver = "com.mysql.cj.jdbc.Driver";

	private static Usuario usuarioLogin = null;

	private static DataSource poolConexiones = null;

	private static boolean nivelFac = false;
	private static boolean configuracionCargada = false;

	static {
		cargarConfiguracion();
	}

	public static boolean isConfiguracionCargada() {
		return configuracionCargada;
	}

	public static boolean existeArchivoConfig() {
		return Cifrado.existeArchivo();
	}

	public static void cargarConfiguracion() {
		configuracionCargada = false;

		Cifrado cifrado = new Cifrado();
		Properties props;
		try {
			props = cifrado.cargar();
			if (props == null) {
				aplicarDefaults();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			aplicarDefaults();
			return;
		}

		login = props.getProperty("db.login", "admin");
		password = props.getProperty("db.password", "");
		server = props.getProperty("db.server", "127.0.0.1");
		String port = props.getProperty("db.port", "3306");
		bd = props.getProperty("db.name", "admin_tools");
		String timezone = props.getProperty("db.timezone", "GMT-6");

		url = "jdbc:mysql://" + server + ":" + port + "/" + bd + "?serverTimezone=" + timezone;
		urlTemplate = "jdbc:mysql://" + server + ":" + port + "/%s?serverTimezone=" + timezone;
		configuracionCargada = true;
	}

	private static void aplicarDefaults() {
		login = "admin";
		password = "";
		server = "127.0.0.1";
		bd = "admin_tools";
		url = "jdbc:mysql://" + server + ":3306/" + bd + "?serverTimezone=GMT-6";
		urlTemplate = "jdbc:mysql://" + server + ":3306/%s?serverTimezone=GMT-6";
	}

	public static void setNivelFac(boolean n) {
		nivelFac = n;
	}

	public static void conectarBD() {
		poolConexiones = setDataSource("mysql");
		aplicarMigraciones();
	}

	private static void aplicarMigraciones() {
		try {
			buildSchemaMigrator().migrateAll();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"No se pudieron aplicar las migraciones de la base de datos:\n" + e.getMessage(),
					"Error al migrar el esquema", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static SchemaMigrator buildSchemaMigrator() {
		return new SchemaMigrator(urlTemplate, login, password, driver);
	}

	public static boolean getNivelFact() {
		return ConexionStatic.nivelFac;
	}

	public static Usuario getUsuarioLogin() {
		return ConexionStatic.usuarioLogin;
	}

	public static void setUsuarioLogin(Usuario u) {
		ConexionStatic.usuarioLogin = u;

	}

	public static DataSource getPoolConexion() {
		return ConexionStatic.poolConexiones;
	}

	static String getServer() { return server; }
	static String getLogin() { return login; }
	static String getPassword() { return password; }
	static String getDriver() { return driver; }
	static String getUrlTemplate() { return urlTemplate; }

	public void setUsuario(Usuario u) {
		ConexionStatic.usuarioLogin = u;
	}

	public boolean getConnectionStatus() {
		boolean conStatus = false;
		try {
			URL u = new URL("https://www.google.com/");
			HttpsURLConnection huc = (HttpsURLConnection) u.openConnection();
			huc.connect();
			conStatus = true;
		} catch (Exception e) {
			conStatus = false;
		}
		return conStatus;
	}

	public static boolean isDbConnected() {
		final String CHECK_SQL_QUERY = "SELECT 1";
		Connection con = null;
		PreparedStatement statement = null;

		boolean isConnected = false;

		try {
			con = getPoolConexion().getConnection();
			statement = con.prepareStatement(CHECK_SQL_QUERY);
			statement.executeQuery();
			isConnected = true;
		} catch (SQLException | NullPointerException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "No se pudo estrablecer una conexion con la base datos.",
					"Error de conexion.", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (statement != null)
					statement.close();
				if (con != null)
					con.close();
			} catch (SQLException excepcionSql) {
				excepcionSql.printStackTrace();
			}
		}
		return isConnected;
	}

	private static DataSource setDataSource(String dbType) {

		BasicDataSource ds = new BasicDataSource();

		if ("mysql".equals(dbType)) {

			ds.setDriverClassName(driver);
			ds.setUrl(url);
			ds.setUsername(login);
			ds.setPassword(password);
			ds.setMaxIdle(3);
			ds.setMinIdle(3);
			ds.setInitialSize(3);
			ds.setValidationQuery("select 1;");
			ds.setMinEvictableIdleTimeMillis(1000 * 60 * 15);
			ds.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
			ds.setNumTestsPerEvictionRun(1);
			ds.setTestOnBorrow(true);
			ds.setTestWhileIdle(true);
			ds.setTestOnReturn(true);

		} else {
			return null;
		}

		return ds;
	}
}
