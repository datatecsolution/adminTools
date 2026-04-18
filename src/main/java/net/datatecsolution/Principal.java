package net.datatecsolution;

import jssc.SerialPort;
import jssc.SerialPortException;
import net.datatecsolution.admin_tools.controlador.CtlFactCredito;
import net.datatecsolution.admin_tools.controlador.CtlLogin;
import net.datatecsolution.admin_tools.controlador.CtlMenuPrincipal;
import net.datatecsolution.admin_tools.controlador.CtlOrdenVenta;
import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.ConfigUserFactDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.view.*;


import net.datatecsolution.admin_tools.modelo.AppLogger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.sf.jasperreports.engine.util.JRStyledTextParser;



public class Principal {
    //private FacturaDao facturaDao=null;

    public static void main(String[] args) {
        AppLogger.init();

        Locale.setDefault(new Locale("es", "HN"));

        configurarConexion();

        //se cargan todos los reportes
        AbstractJasperReports.loadFileReport();





        //se aplican los interes de las facturas vendidas
        FacturaDao  facturaDao=new FacturaDao();

        facturaDao.aplicarInteresVenc();

        //KeyedObjectPoolFactory keyedObjectPoolFactory=new KeyedObjectPoolFactory();

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(10,10,10,10));
                    UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }




        ViewLogin viewLogin =new ViewLogin();
        CtlLogin ctlLogin=new CtlLogin(viewLogin);

        boolean login=ctlLogin.login();

        if(login){
            Usuario user=ConexionStatic.getUsuarioLogin();

            Integer permiso=ConexionStatic.getUsuarioLogin().getTipoPermiso();

            //se recogen las configuraciones para el usuario
            ConfigUserFactDao configDao=new ConfigUserFactDao();

            ConfigUserFacturacion conf=configDao.buscarPorUser(user.getUser());

            if(conf == null)
                user.setConfig(new ConfigUserFacturacion()) ;
            else
                user.setConfig(conf);
		/*
		if(user.getCajas()!=null)
		{

			for(int x=0;x<user.getCajas().size();x++){
				JOptionPane.showMessageDialog(viewLogin,x+" de "+ user.getCajas().size());
				JOptionPane.showMessageDialog(viewLogin, user.getCajas().get(x).toString());
			}
		}*/
            if(permiso==3){

                ViewOrdeneVenta vistaOrdenes=new ViewOrdeneVenta(null);
                CtlOrdenVenta ctlOrdenes=new CtlOrdenVenta(vistaOrdenes );

                //vistaOrdenes.pack();
                vistaOrdenes.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); //.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
                vistaOrdenes.setVisible(true);
                System.exit(0);
                //boolean resul=ctlFacturas.buscarCotizaciones(null);
            }
            if(permiso==1 || permiso==4){
                //este manejador de subprocesos el cual permite ejecutar el metodo run de l ctl cada cierta cantida de tiempo
                ScheduledExecutorService scheduler= Executors.newSingleThreadScheduledExecutor();

                ConexionStatic.getUsuarioLogin().setCajasEmpty();
                ViewMenuPrincipal principal=new ViewMenuPrincipal();
                CtlMenuPrincipal ctl=new CtlMenuPrincipal(principal);
                principal.conectarControlador(ctl);
                principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                //segunto inicial
                int initialDelay = 1;
                //cada cuantos segundos se ejecuta
                int periodicDelay =1;
                //se progrma la ejecucion pasandole el ctl el
                scheduler.scheduleAtFixedRate(ctl, initialDelay, periodicDelay,
                        TimeUnit.SECONDS     );
				/*
				ViewMenuPrincipal principal=new ViewMenuPrincipal();
				CtlMenuPrincipal ctl=new CtlMenuPrincipal(principal,conexion);
				principal.conectarControlador(ctl);*/
            }
            if(permiso==2){
				/*
				ViewFacturar vistaFacturar=new ViewFacturar(null);
				CtlFacturar ctlFacturar=new CtlFacturar(vistaFacturar,conexion );
				vistaFacturar.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); //.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
				vistaFacturar.setVisible(true);
				System.exit(0);*/

                if(user.getCajas()!=null){

                    /*
                    ViewFactCredito viewFactCredito=new ViewFactCredito(null);
                    CtlFactCredito ctlFactCredito =new CtlFactCredito(viewFactCredito);

                    viewFactCredito.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); //.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

                     */



                    ViewModuloFacturar marcoEscritorio = new ViewModuloFacturar();
                    //CtlModuloFacturar ctlMarcoEscritorio=new CtlModuloFacturar(marcoEscritorio,conexion);
                    marcoEscritorio.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                    //marcoEscritorio.setSize( 1000, 800 ); // establece el tamaño del marco
                    marcoEscritorio.setVisible( true ); // muestra el marco

                }else{
                    JOptionPane.showMessageDialog(viewLogin, "No tiene cajas asignadas para poder facturar.","Error",JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }



        }



    }

    private static void configurarConexion() {
        boolean necesitaConfig = !ConexionStatic.existeArchivoConfig();

        if (!necesitaConfig) {
            ConexionStatic.conectarBD();
            necesitaConfig = !ConexionStatic.isDbConnected();
        }

        while (necesitaConfig) {
            net.datatecsolution.admin_tools.view.BdConfig viewBdConfig =
                new net.datatecsolution.admin_tools.view.BdConfig(null);
            net.datatecsolution.admin_tools.controlador.CtlBdConfig ctlBdConfig =
                new net.datatecsolution.admin_tools.controlador.CtlBdConfig(viewBdConfig);
            viewBdConfig.dispose();

            boolean conectado = ConexionStatic.isConfiguracionCargada()
                && ConexionStatic.isDbConnected();

            if (!conectado) {
                int opcion = JOptionPane.showConfirmDialog(null,
                    "No se pudo conectar a la base de datos.\n¿Desea reintentar la configuración?",
                    "Error de conexión", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (opcion != JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            } else {
                necesitaConfig = false;
            }
        }
    }
}
