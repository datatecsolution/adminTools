package net.datatecsolution.admin_tools.controlador;

import java.io.*;
import java.util.Properties;


public abstract class App {
	
	private static String filename = ".config/config.properties";
	
	
	//private static OutputStream output = null;
	
	
	private static String serverName;
	private static String dataBase;
	private static String userDb;
	private static String pwUserDb;
	
	
	public static void saveFileConfigDefaul(){
		OutputStream output = null;
		try {
			output = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Properties prop = new Properties();
		// save properties to project root folder
		try {
			
			prop.setProperty("serverName", "localhost");
			prop.setProperty("database", "admin_tools");
			prop.setProperty("dbuser", "mkyong");
			prop.setProperty("dbpassword", "password");
			prop.store(output, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void loadFileConfig(){
		/*
		///se configura el objeto para leer el archivo de configuraciones
		input = App.class.getResourceAsStream(filename);
		if(input==null){//se verifica que no existan errores
            System.out.println("Sorry, unable to find " + filename);
	    return;
		}
		
		
		//se configura el objeto para escribir el archivo de configuracions
		try {
			output = new FileOutputStream("src/config/config.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}


	public static String getServerName() {
		
		InputStream input = App.class.getResourceAsStream(".config/config.properties");
		
		if(input==null){//se verifica que no existan errores
            System.out.println("Sorry, unable to find " + filename);
	    return null;
		}
		
		Properties prop = new Properties();
		
		try {
			prop.load(input);
			
			serverName=prop.getProperty("serverName");
			
			System.out.println(serverName);
	  
			return serverName;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}



	public static void setServerName(String serverName) {
		
		OutputStream output = null;
		try {
			output = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Properties prop = new Properties();
		
		try {
			prop.setProperty("serverName", serverName);
			prop.store(output, null);
			App.serverName = serverName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}



	public static String getUserDb() {
		
		InputStream input = App.class.getResourceAsStream(filename);
		
		if(input==null){//se verifica que no existan errores
            System.out.println("Sorry, unable to find " + filename);
	    return null;
		}
		
		Properties prop = new Properties();
		
		try {
			prop.load(input);
			
			
			userDb=prop.getProperty("dbuser");
			
	        System.out.println(userDb);
	       
			return userDb;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
	}



	public static void setUserDb(String userDb) {
		
		OutputStream output = null;
		try {
			output = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Properties prop = new Properties();
		
		try {
			prop.setProperty("dbuser", userDb);
			prop.store(output, null);
			App.userDb = userDb;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}



	public static String getPwUserDb() {
		
		
		InputStream input = App.class.getResourceAsStream(filename);
		
		if(input==null){//se verifica que no existan errores
            System.out.println("Sorry, unable to find " + filename);
	    return null;
		}
		
		Properties prop = new Properties();
		
		try {
			prop.load(input);
			
		
			pwUserDb=prop.getProperty("dbpassword");
			
	        System.out.println(pwUserDb);
	
	        return pwUserDb;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
	}



	public static void setPwUserDb(String pwUserDb) {
		
		OutputStream output = null;
		try {
			output = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Properties prop = new Properties();
		
		try {
			prop.setProperty("dbpassword", pwUserDb);
			prop.store(output, null);
			App.pwUserDb = pwUserDb;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static String getDataBase() {
		
		InputStream input = App.class.getResourceAsStream(filename);
		
		if(input==null){//se verifica que no existan errores
            System.out.println("Sorry, unable to find " + filename);
	    return null;
		}
		
		Properties prop = new Properties();
		
		try {
			prop.load(input);
			
			
			dataBase=prop.getProperty("database");
			
	        System.out.println(pwUserDb);
	       
	        return dataBase;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
		
		
	}

	public static void setDataBase(String dataBase) {
		
		OutputStream output = null;
		try {
			output = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Properties prop = new Properties();
		
		try {
			prop.setProperty("database", dataBase);
			prop.store(output, null);
			App.dataBase = dataBase;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
