package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlBanco;
import net.datatecsolution.admin_tools.controlador.CtlFactCredito;
import net.datatecsolution.admin_tools.view.botones.BotonCliente;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewFactCredito extends JDialog {

    private JButton btnClientes;
    private JButton btnCreditos;
    private JButton btnFacturar;

    public ViewFactCredito(Window view) {

        this.setTitle("Modulo de ventas y creditos");
        this.setLocationRelativeTo(view);
        this.setModal(true);

        this.setResizable(false);
        getContentPane().setBackground(PanelPadre.color1);
        getContentPane().setLayout(null);

        btnFacturar = new JButton("FACTURAR");
        btnFacturar.setBounds(45, 52, 176, 62);
        getContentPane().add(btnFacturar);

        btnCreditos = new JButton("CREDITOS");
        btnCreditos.setBounds(281, 52, 176, 62);
        getContentPane().add(btnCreditos);

        btnClientes = new JButton("PAGOS");
        btnClientes.setBounds(45, 149, 176, 62);
        getContentPane().add(btnClientes);


        this.setSize(600, 400);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }

    public void conectarCtl(CtlFactCredito c){

        btnFacturar.addActionListener(c);
        btnFacturar.setActionCommand("FACTURAR");

        btnCreditos.addActionListener(c);
        btnCreditos.setActionCommand("CREDITOS");

        btnClientes.addActionListener(c);
        btnClientes.setActionCommand("CLIENTES");

    }
}
