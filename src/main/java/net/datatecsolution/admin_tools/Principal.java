package net.datatecsolution.admin_tools;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.datatecsolution.admin_tools.controllerFx.ctlTablaCategoria;
import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;
import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.ConfigUserFactDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;


import javafx.scene.control.Button;

import java.net.URL;


public class Principal extends Application {
    //private FacturaDao facturaDao=null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/viewFx/vCategoriaTabla.fxml"));

        Parent p = fxmlLoader.load();

        ctlTablaCategoria c = fxmlLoader.getController();

        Scene scene = new Scene( p );

        Stage stage = new Stage();
        stage.setTitle( "SimpleTableApp - TableView");
        stage.setWidth( 800 );
        stage.setHeight( 600 );
        stage.setScene(scene);
        stage.setOnShown( (windowEvent) -> c.refresh() );
        stage.show();
    }



        public static void main(String[] args) {
        // TODO Auto-generated method stub

        //se cargan todos los reportes
        AbstractJasperReports.loadFileReport();

        //se establece la conecion a la base de datos
        ConexionStatic.conectarBD();


        //se aplican los interes de las facturas vendidas
        FacturaDao  facturaDao=new FacturaDao();

        facturaDao.aplicarInteresVenc();

        //KeyedObjectPoolFactory keyedObjectPoolFactory=new KeyedObjectPoolFactory();


        /*

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

         */

            launch(args);




    }

}
