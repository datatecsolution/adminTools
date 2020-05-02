

package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;







/**
 * Clase que permite conectar con la base de datos
 * @author jdmayorga
 *
 */
public class Conexion{


        //10.10.10.8:3306  http://192.168.1.112/
        private  BasicDataSource basicDataSource;

        private DataSource dataSource;
        static String bd = "admin_tools";
        static String login = "user_pos";
        static String password = "admin123.";
        static String server = "192.168.1.10";



	   /*static String bd = "admin_tools";
	   static String login = "root";
	   static String password = "jdmm123.";
	   static String server = "localhost";*/

	   /*static String bd = "miswendy_admin_tools";
	   static String login = "miswendy_pos";
	   static String password = "bTp.rxN-*~Z4";
	   static String server = "108.167.189.55";8*/

        static String url = "jdbc:mysql://"+server+":3306/"+bd;
        static String driver="com.mysql.jdbc.Driver";
        private Usuario usuarioLogin=null;

        Connection conn = null;

        private  DataSource poolConexiones=null;
        //private static DataSource poolConexionRemote=null;

        private boolean nivelFac=false;


        public void setNivelFac(boolean n){
            nivelFac=n;
        }


        public Conexion(String sql){
            poolConexiones=setDataSourceRemote("mysql");
        }



        /** Constructor de DbConnection */
        public Conexion() {

            poolConexiones=setDataSource("mysql");
            //poolConexionRemote=setDataSourceRemote("mysql");

	   /*BasicDataSource basicDataSource = new BasicDataSource();

	   basicDataSource.setDriverClassName(driver);
	   basicDataSource.setUsername(login);
	   basicDataSource.setPassword(password);
	   basicDataSource.setUrl(url);
	   basicDataSource.setMaxActive(50);


	  // basicDataSource.setValidationQuery("select 1");

	    dataSource = basicDataSource;

	   try {

		   conn=basicDataSource.getConnection();// dataSource.getConnection();

           if(conn!=null){

               //JOptionPane.showMessageDialog(null, "Conectado");
               System.out.println("Conecci�n a base de datos "+bd+" OK");

           }

       } catch (SQLException e) {

           System.out.println(e);

       }/*finally{


               try {

            	   conn.close();

               } catch (SQLException ex) {

                   System.out.println(ex);

               }


       }




     /* try{
         //obtenemos el driver de para mysql
         Class.forName(driver);
         //obtenemos la conexi�n
         conn = DriverManager.getConnection(url,login,password);

         if (conn!=null){
            System.out.println("Conecci�n a base de datos "+bd+" OK");
         }
      }
      catch(SQLException e){
         System.out.println(e);
      }catch(ClassNotFoundException e){
         System.out.println(e);
      }catch(Exception e){
         System.out.println(e);
      }*/
            // conn.p
        }

        private static DataSource setDataSourceRemote(String dbType) {
            //Properties props = new Properties();
            //FileInputStream fis = null;
            BasicDataSource ds = new BasicDataSource();

       /*try {
          fis = new FileInputStream( "db.config");
    	   //Conexion.class.getResource("/View/imagen/actualizar.png");
    	   //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	   //fis = ClassLoader.class.getResourceAsStream("/path/to/your/xml");
           props.load(fis);
       }catch(IOException e){
           e.printStackTrace();
           return null;
       }*/
            if("mysql".equals(dbType)){


    	  /*
    	   ds.setDriverClassName(driver);
           ds.setUrl("jdbc:mysql://108.167.189.55:3306/");
           ds.setDefaultCatalog("miswendy_admin_tools");
           ds.setUsername("miswendy_pos");
           ds.setPassword("bTp.rxN-*~Z4");

           ds.setMaxActive(5);
           ds.setMaxIdle(5);
           ds.setMinIdle(3);
           ds.setInitialSize(4);*/


                ds.setDriverClassName(driver);
                ds.setUrl("jdbc:mysql://192.168.1.25:3306/");
                ds.setDefaultCatalog("admin_tools_resp");
                ds.setUsername("user_pos");
                ds.setPassword("Admin123.");
               // ds.setMaxActive(3);
                //ds.setMaxIdle(5);
                //ds.setMinIdle(3);
                ds.setInitialSize(3);
                //ds.default.maxIdleTime = 3600 ;
                // ds.setMaxIdle(maxIdle);

                ds.setValidationQuery("select 1;");
                //maxWait = 180000

               // ds.setMaxWait(-1);
                // minEvictableIdleTimeMillis = 1000 * 60 * 15

                ds.setMinEvictableIdleTimeMillis( 1000 * 60 * 15);

                // timeBetweenEvictionRunsMillis = 1000 * 60 * 15
                ds.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
                // numTestsPerEvictionRun = 3
                ds.setNumTestsPerEvictionRun(1);
                // testOnBorrow = true
                ds.setTestOnBorrow(true);
                // testWhileIdle = true
                ds.setTestWhileIdle(true);
                // testOnReturn = false

                ds.setTestOnReturn(true);




                //ds.setMaxWait(maxWait);



           /*ds.setUrl("jdbc:mysql://108.167.189.55:3306/");
           ds.setDefaultCatalog("miswendy_admin_tools");
           ds.setUsername("miswendy_pos");
           ds.setPassword("bTp.rxN-*~Z4");

          // ds.setUsername("jdmayorg_datatec");
           //ds.setPassword("uXn;eBtZ[#P{");

          // ds.setUrl("jdbc:mysql://108.167.189.55:3306/miswendy_admin_tools");
          // ds.setUsername("miswendy_pos");
          // ds.setPassword("bTp.rxN-*~Z4");
          // ds.setMinIdle(20);
           ds.setMaxActive(5);
           ds.setMaxIdle(8);
           ds.setMinIdle(3);
           ds.setInitialSize(3);


           ds.setInitialSize(4);
           ds.setValidationQuery("select 1;");
           //maxWait = 180000

        		   ds.setMaxWait(-1);
	       // minEvictableIdleTimeMillis = 1000 * 60 * 15

	        	ds.setMinEvictableIdleTimeMillis( 1000 * 60 * 15);

	       // timeBetweenEvictionRunsMillis = 1000 * 60 * 15
	        		ds.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
	       // numTestsPerEvictionRun = 3
	        		ds.setNumTestsPerEvictionRun(1);
	       // testOnBorrow = true
	        		ds.setTestOnBorrow(true);
	       // testWhileIdle = true
	        		ds.setTestWhileIdle(true);
	       // testOnReturn = false

	        		ds.setTestOnReturn(true);
	       // validationQuery = "select 1"*/


            }else{
                return null;
            }

            return ds;
        }

        public boolean getNivelFact(){
            return this.nivelFac;
        }
        public Usuario getUsuarioLogin(){
            return usuarioLogin;
        }
        public void setUsuarioLogin(Usuario u){
            usuarioLogin=u;

        }

        /**Permite retornar la conexi�n*/
        public Connection getConnection(){

            return conn;
        }

  /* public void desconectar(){
      //conn = null;
      try {
		conn.close();

	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }*/

        public DataSource getPoolConexion(){
            return poolConexiones;
        }

        /* public static DataSource getPoolConexionRemote(){
             return poolConexionRemote;
         }*/
        public void setUsuario(Usuario u){
            this.usuarioLogin=u;
        }
        public boolean getConnectionStatus () {
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


        public static DataSource setDataSource(String dbType){
            Properties props = new Properties();
            // File file = new File("/config/db.config");
            InputStream file=null;
            FileInputStream fis = null;
            //InputStream fis=null;
            BasicDataSource ds = new BasicDataSource();

            //fis=new FileInputStream(file);
            file=AbstractJasperReports.class.getResourceAsStream("/config/db.config");
            try {
                props.load(file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

       /*try {
          fis = new FileInputStream( "db.config");
    	   //Conexion.class.getResource("/View/imagen/actualizar.png");
    	   //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	   //fis = ClassLoader.class.getResourceAsStream("/path/to/your/xml");
           props.load(fis);
       }catch(IOException e){
           e.printStackTrace();
           return null;
       }*/
            if("mysql".equals(dbType)){

                ds.setDriverClassName(props.getProperty("MYSQL_DB_DRIVER_CLASS"));
                ds.setUrl("jdbc:mysql://"+props.getProperty("MYSQL_DB_URL")+":3306/");
                ds.setDefaultCatalog(props.getProperty("MYSQL_DB"));
                ds.setUsername(props.getProperty("MYSQL_DB_USERNAME"));
                ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

           /*ds.setDriverClassName(driver);
           ds.setUrl(url);
           ds.setUsername(login);
           ds.setPassword(password);*/
                // ds.setMinIdle(20);
               // ds.setMaxActive(3);
                ds.setMaxIdle(3);
                ds.setMinIdle(3);
                ds.setInitialSize(3);


                ds.setInitialSize(3);
                ds.setValidationQuery("select 1;");
                //maxWait = 180000

                //ds.setMaxWait(-1);
                // minEvictableIdleTimeMillis = 1000 * 60 * 15

                ds.setMinEvictableIdleTimeMillis( 1000 * 60 * 15);

                // timeBetweenEvictionRunsMillis = 1000 * 60 * 15
                ds.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
                // numTestsPerEvictionRun = 3
                ds.setNumTestsPerEvictionRun(1);
                // testOnBorrow = true
                ds.setTestOnBorrow(true);
                // testWhileIdle = true
                ds.setTestWhileIdle(true);
                // testOnReturn = false

                ds.setTestOnReturn(true);
                // validationQuery = "select 1"
          /*
           ds.setValidationQuery("select 1;");
           //maxWait = 180000

        		   ds.setMaxWait(-1);
	       // minEvictableIdleTimeMillis = 1000 * 60 * 15

	        		ds.setMinEvictableIdleTimeMillis( 1000 * 60 * 15);
	       // timeBetweenEvictionRunsMillis = 1000 * 60 * 15
	        		ds.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
	       // numTestsPerEvictionRun = 3
	        		ds.setNumTestsPerEvictionRun(3);
	       // testOnBorrow = true
	        		ds.setTestOnBorrow(true);
	       // testWhileIdle = true
	        		ds.setTestWhileIdle(true);
	       // testOnReturn = false

	        		ds.setTestOnReturn(true);
	       // validationQuery = "select 1"*/





           /*ds.setDriverClassName(props.getProperty("MYSQL_DB_DRIVER_CLASS"));
           ds.setUrl(props.getProperty("MYSQL_DB_URL"));
           ds.setUsername(props.getProperty("MYSQL_DB_USERNAME"));
           ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
          // ds.setMinIdle(20);
           ds.setMaxActive(10);
           ds.setMaxIdle(5);
           ds.setMinIdle(3);
           ds.setInitialSize(2);*/
            }


       /*else if("oracle".equals(dbType)){
           ds.setDriverClassName(props.getProperty("ORACLE_DB_DRIVER_CLASS"));
           ds.setUrl(props.getProperty("ORACLE_DB_URL"));
           ds.setUsername(props.getProperty("ORACLE_DB_USERNAME"));
           ds.setPassword(props.getProperty("ORACLE_DB_PASSWORD"));
       }*/else{
                return null;
            }

            return ds;
        }

}
