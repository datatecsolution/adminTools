package net.datatecsolution.admin_tools.view;


import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlCobroFactura;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonCobrar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

public class ViewCobroFactura extends JDialog {

    private JPanel panelAcciones;
    private JPanel panelDatosFactura;
    private JLabel lblCodigoCliente;
    private JTextField txtIdcliente;
    private JTextField txtNombrecliente;


    private BotonCancelar btnCerrar;
    private BotonCobrar btnCobrar;
    private JLabel lblNombreCliente;
    private JTextField txtLimitecredito;
    private JTextField txtSaldocliente;


    private JTextField txtTotal;
    private JLabel lblTotal;
    private JTextField txtNumeroFact;
    private JTextField txtFechaEmision;
    private JTextField txtFechaUltimoPago;
    private JTextField txtSaldoFactura;



    //private JTextField txtFecha;
    protected JDateChooser jdcFecha;
    private JLabel lblReferencia;
    private JTextField txtReferencia;
    // private JDateChooser dcFecha1;



    public ViewCobroFactura(Window view) {


        this.setTitle("Cobro");
        this.setLocationRelativeTo(view);
        this.setModal(true);

        //setIconImage(Toolkit.getDefaultToolkit().getImage(ViewFacturar.class.getResource("/view/recursos/logo-admin-tool1.png")));
        getContentPane().setBackground(PanelPadre.color1);

        getContentPane().setLayout(null);

        panelAcciones=new PanelPadre();
        panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelAcciones.setBounds(20, 11, 117, 511);
        panelAcciones.setLayout(null);

        getContentPane().add(panelAcciones);



        btnCobrar = new BotonCobrar();
        btnCobrar.setText("F2 Cobrar");
        btnCobrar.setHorizontalAlignment(SwingConstants.LEFT);
        btnCobrar.setBounds(10, 19, 92, 70);

        panelAcciones.add(btnCobrar);

        btnCerrar = new BotonCancelar();
        btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
        btnCerrar.setText("Esc Cerrar");
        btnCerrar.setBounds(10, 102, 92, 70);
        panelAcciones.add(btnCerrar);




        panelDatosFactura=new PanelPadre();
        //panelDatosFactura.setBackground(Color.WHITE);
        panelDatosFactura.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelDatosFactura.setBounds(161, 11, 604, 124);
        panelDatosFactura.setLayout(null);

        getContentPane().add(panelDatosFactura);

        lblCodigoCliente = new JLabel("Codigo");
        lblCodigoCliente.setBounds(20, 23, 237, 14);
        panelDatosFactura.add(lblCodigoCliente);


        txtIdcliente = new JTextField();
        txtIdcliente.setBounds(20, 35, 237, 29);
        panelDatosFactura.add(txtIdcliente);
        txtIdcliente.setColumns(10);

        txtNombrecliente = new JTextField();
        txtNombrecliente.setEditable(false);
        txtNombrecliente.setToolTipText("Nombre Cliente");
        txtNombrecliente.setBounds(269, 35, 313, 29);
        panelDatosFactura.add(txtNombrecliente);
        txtNombrecliente.setColumns(10);


        lblNombreCliente = new JLabel("Nombre");
        lblNombreCliente.setBounds(269, 23, 313, 14);
        panelDatosFactura.add(lblNombreCliente);

        JLabel lblSaldoCliente = new JLabel("Saldo General");
        lblSaldoCliente.setBounds(269, 70, 313, 14);
        panelDatosFactura.add(lblSaldoCliente);

        JLabel lblLimiteDeCredito = new JLabel("Limite de credito");
        lblLimiteDeCredito.setBounds(20, 70, 237, 14);
        panelDatosFactura.add(lblLimiteDeCredito);

        txtLimitecredito = new JTextField();
        txtLimitecredito.setEditable(false);
        txtLimitecredito.setBounds(20, 84, 237, 29);
        panelDatosFactura.add(txtLimitecredito);
        txtLimitecredito.setColumns(10);

        txtSaldocliente = new JTextField();
        txtSaldocliente.setEditable(false);
        txtSaldocliente.setBounds(269, 84, 313, 29);
        panelDatosFactura.add(txtSaldocliente);
        txtSaldocliente.setColumns(10);






        txtTotal = new JTextField();
        //txtTotal.setForeground(Color.RED);
        txtTotal.setHorizontalAlignment(SwingConstants.LEFT);
        txtTotal.setFont(new Font("OCR A Extended", Font.PLAIN, 20));
        txtTotal.setText("00");
        txtTotal.setBounds(161, 303, 604, 58);
        getContentPane().add(txtTotal);
        txtTotal.setColumns(10);

        lblTotal = new JLabel("Pago");
        lblTotal.setBounds(165, 283, 46, 14);
        getContentPane().add(lblTotal);

        PanelPadre panelPadre = new PanelPadre();
        panelPadre.setLayout(null);
        panelPadre.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos de la factura", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelPadre.setBounds(161, 147, 604, 124);
        getContentPane().add(panelPadre);

        JLabel lblNumeroFactura = new JLabel("Numero");
        lblNumeroFactura.setBounds(20, 23, 237, 14);
        panelPadre.add(lblNumeroFactura);

        txtNumeroFact = new JTextField();
        txtNumeroFact.setColumns(10);
        txtNumeroFact.setBounds(20, 35, 237, 29);
        panelPadre.add(txtNumeroFact);

        txtFechaEmision = new JTextField();
        txtFechaEmision.setToolTipText("Nombre Cliente");
        txtFechaEmision.setEditable(false);
        txtFechaEmision.setColumns(10);
        txtFechaEmision.setBounds(269, 35, 313, 29);
        panelPadre.add(txtFechaEmision);

        JLabel lblFechaEmision = new JLabel("Fecha emision");
        lblFechaEmision.setBounds(269, 23, 313, 14);
        panelPadre.add(lblFechaEmision);

        JLabel lblSaldo = new JLabel("Saldo");
        lblSaldo.setBounds(269, 70, 313, 14);
        panelPadre.add(lblSaldo);

        JLabel lblUltimoPago = new JLabel("Ultimo pago");
        lblUltimoPago.setBounds(20, 70, 237, 14);
        panelPadre.add(lblUltimoPago);

        txtFechaUltimoPago = new JTextField();
        txtFechaUltimoPago.setEditable(false);
        txtFechaUltimoPago.setColumns(10);
        txtFechaUltimoPago.setBounds(20, 84, 237, 29);
        panelPadre.add(txtFechaUltimoPago);

        txtSaldoFactura = new JTextField();
        txtSaldoFactura.setEditable(false);
        txtSaldoFactura.setColumns(10);
        txtSaldoFactura.setBounds(269, 84, 313, 29);
        panelPadre.add(txtSaldoFactura);

        JLabel lblFecha = new JLabel("Fecha");
        lblFecha.setBounds(165, 373, 46, 14);
        getContentPane().add(lblFecha);

        jdcFecha = new JDateChooser();
        jdcFecha.setFont(new Font("Dialog", Font.PLAIN, 17));
        jdcFecha.setBounds(161, 389, 604, 43);
        jdcFecha.setDate(new Date());
        jdcFecha.setDateFormatString("dd-MM-yyyy");
        getContentPane().add(jdcFecha);

        lblReferencia = new JLabel("Referencia");
        lblReferencia.setBounds(161, 444, 88, 14);
        getContentPane().add(lblReferencia);

        txtReferencia = new JTextField();
        txtReferencia.setHorizontalAlignment(SwingConstants.LEFT);
        txtReferencia.setFont(new Font("Dialog", Font.PLAIN, 17));
        txtReferencia.setColumns(10);
        txtReferencia.setBounds(161, 464, 604, 58);
        getContentPane().add(txtReferencia);

        setSize(800, 560);

        //this.setPreferredSize(new Dimension(660, 330));
        this.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        this.setPreferredSize(new Dimension(800, 560));
        this.pack();
    }

    public JButton getBtnCobrar(){
        return btnCobrar;
    }
    public JButton getBtnCerrar(){
        return btnCerrar;
    }
    public JTextField getTxtTotal(){
        return txtTotal;
    }
    public JTextField getTxtNombrecliente(){
        return txtNombrecliente;
    }
    public JTextField getTxtIdcliente(){
        return txtIdcliente;
    }

    public JTextField getTxtNumeroFact() {
        return txtNumeroFact;
    }

    public JTextField getTxtFechaEmision() {
        return txtFechaEmision;
    }

    public JTextField getTxtFechaUltimoPago() {
        return txtFechaUltimoPago;
    }

    public JTextField getTxtSaldoFactura() {
        return txtSaldoFactura;
    }

    public JTextField getTxtLimiteCredito(){
        return this.txtLimitecredito;
    }
    public JTextField getTxtSaldo(){
        return this.txtSaldocliente;
    }
    public JTextField getTxtReferencia() {
        return txtReferencia;
    }

    public void setTxtReferencia(JTextField txtReferencia) {
        this.txtReferencia = txtReferencia;
    }

    public JDateChooser getJdcFecha() {
        return jdcFecha;
    }

    public void setJdcFecha(JDateChooser jdcFecha) {
        this.jdcFecha = jdcFecha;
    }
    public void conectarContralador(CtlCobroFactura c){



        txtIdcliente.addKeyListener(c);
        txtNombrecliente.addKeyListener(c);
        txtNumeroFact.addKeyListener(c);
        txtFechaEmision.addKeyListener(c);
        txtFechaUltimoPago.addKeyListener(c);
        txtSaldoFactura.addKeyListener(c);

        this.btnCerrar.addKeyListener(c);
        this.btnCerrar.addActionListener(c);
        this.btnCerrar.setActionCommand("CERRAR");

        this.btnCobrar.addKeyListener(c);
        this.btnCobrar.addActionListener(c);
        this.btnCobrar.setActionCommand("COBRAR");

        txtTotal.addKeyListener(c);
        txtReferencia.addKeyListener(c);
        this.jdcFecha.addKeyListener(c);
        //jdcFecha.getDateEditor().get

        this.txtTotal.addActionListener(c);
        this.txtTotal.setActionCommand("TOTAL");

        this.txtReferencia.addActionListener(c);
        this.txtReferencia.setActionCommand("REFERENCIA");



    }
}
