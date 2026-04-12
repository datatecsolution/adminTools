package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.view.ViewCuentasFacturas;
import net.datatecsolution.admin_tools.view.ViewFactCredito;
import net.datatecsolution.admin_tools.view.ViewModuloFacturar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlFactCredito implements ActionListener {
    private ViewFactCredito view;

    public CtlFactCredito(ViewFactCredito v)
    {
        view=v;
        view.conectarCtl(this);
        view.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {

        String comando=e.getActionCommand();

        switch(comando){
            case "FACTURAR":
                ViewModuloFacturar marcoEscritorio = new ViewModuloFacturar();
                //CtlModuloFacturar ctlMarcoEscritorio=new CtlModuloFacturar(marcoEscritorio,conexion);
                marcoEscritorio.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                //marcoEscritorio.setSize( 1000, 800 ); // establece el tamaño del marco
                marcoEscritorio.setVisible( true ); // muestra el marco
                break;
            case "CREDITOS":
                break;
            case "CLIENTES":
                ViewCuentasFacturas viewCuentasFacturas=new ViewCuentasFacturas(view);
                CtlCuentasFacturas ctlCuentasFacturas=new CtlCuentasFacturas(viewCuentasFacturas);


                viewCuentasFacturas.dispose();
                ctlCuentasFacturas=null;
                break;
        }

    }
}
