package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFacturarFrame;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.view.botones.*;
import net.datatecsolution.admin_tools.view.dto.FacturaCabeceraData;
import net.datatecsolution.admin_tools.view.dto.FacturaClienteData;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFactura;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmEmpleado;
import net.datatecsolution.admin_tools.view.tablemodel.ListaBotonesFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.TablaModeloFactura;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewFacturarFrame extends JInternalFrame {

	protected BorderLayout miEsquema;
	private final JTable tableDetalle;
	private final TablaModeloFactura modeloTabla;
	private CtlFacturarFrame ctl=null;
	
	private final JPanel panelAcciones;
	private final JPanel panelBuscar;
	private final JPanel panelDatosFactura;
	protected JPanel panelNorte;
	
	protected JPanel panel_1;
	private final JTextField txtFechafactura;
	private final JLabel lblCodigoCliente;
	private final JTextField txtIdcliente;
	private final JTextField txtNombrecliente;
	
	private final ButtonGroup grupoOpciones;
	private final JRadioButton rdbtnCredito;
	private final JRadioButton rdbtnContado;
	
	private final JTextField txtSubtotal;
	private final JLabel lblSubtotal;
	private final JTextField txtImpuesto;
	private final JLabel lblImpuesto;
	private final JTextField txtTotal;
	private final JLabel lblTotal;
	private final JLabel lblNombreCliente;
	private final JLabel lblContado;
	private final JLabel lblCredito;
	
	private final BotonGuardar btnGuardar;
	
	private final ListaBotonesFacturas btnGuardados=new ListaBotonesFacturas();
	private final BotonCancelar btnCerrar;
	private final BotonBuscar1 btnBuscar;
	private final BotonBuscarClientes btnCliente;
	private final BotonCobrar btnCobrar;
	private final JButton btnCierreCaja;
	private final JButton btnGetCotizacion;
	
	private final JTextField txtDescuento;
	
	private final BotonActualizar btnActualizar;
	
	
	private final JTextField txtBuscar;
	private final JTextField txtImpuesto18;
	private final JButton btnGuardarCotizacion;
	
	private final JTextField txtRtn;
	
	private final CbxTmEmpleado modeloEmpleado;
	
	protected JPanel panel;
	protected JLabel lblBuscar;
	private final JPanel panel_2;
	private final JLabel lblLogo;
	private final JPanel panelGuardados;
	
	
	private final JMenuItem mntmEliminar;
	private final JMenuItem mntmImprimir;
	private final JPopupMenu menuContextual;
	private final Color colorNormal;
	private static final Color COLOR_EDITANDO = new Color(214, 245, 224);

	public JPanel getPanelGuardados() {
		return panelGuardados;
	}

	public void setEstadoFactura(boolean editando, int numeroFactura) {
		TitledBorder border;
		if (editando) {
			border = new TitledBorder(new LineBorder(new Color(130, 135, 144)),
				"Editando Orden #" + numeroFactura, TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0));
			panelDatosFactura.setBackground(COLOR_EDITANDO);
		} else {
			border = new TitledBorder(new LineBorder(new Color(130, 135, 144)),
				"Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0));
			panelDatosFactura.setBackground(colorNormal);
		}
		panelDatosFactura.setBorder(BorderFactory.createCompoundBorder(
			border, BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(130, 135, 144))
		));
		panelDatosFactura.repaint();
	}

	public ViewFacturarFrame(String string, boolean b, boolean c, boolean d, boolean e) {
		super( string,  b,  c,  d,  e );
		((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null);
		setBorder(null);
		
		menuContextual = new JPopupMenu();
		mntmEliminar = new JMenuItem("Eliminar");
		mntmImprimir = new JMenuItem("Imprimir");
		menuContextual.add(mntmEliminar);
		menuContextual.add(mntmImprimir);
		
		
		
		grupoOpciones = new ButtonGroup();
		modeloEmpleado=new CbxTmEmpleado();
		modeloTabla=new TablaModeloFactura();
		RenderizadorTablaFactura renderizador = new RenderizadorTablaFactura();
		miEsquema=new BorderLayout();
		Color color1 =new Color(60, 179, 113);
		Color color3 =Color.decode("#d4f4ff");
		colorNormal = color3;
		Color color4 =Color.decode("#f4fbfe");
		
		
		
		panelNorte=new JPanel();
	
		this.getContentPane().setBackground(color3);
		
		getContentPane().setLayout(miEsquema);
		panelAcciones=new JPanel();
		panelAcciones.setPreferredSize(new Dimension(140,128));
		panelAcciones.setBackground(color3);
		panelNorte=new JPanel();
		panelNorte.setBackground(color3);
		panelNorte.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		getContentPane().add(panelNorte, BorderLayout.NORTH);
		panelNorte.setLayout(new BorderLayout(0, 0));
				
		
		
		
		
		
		
		
		
		tableDetalle = new JTable();
		tableDetalle.setModel(modeloTabla);
		tableDetalle.setDefaultRenderer(String.class, renderizador);
		tableDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableDetalle.getColumnModel().getColumn(0).setPreferredWidth(400);
		tableDetalle.getColumnModel().getColumn(1).setPreferredWidth(120);
		tableDetalle.getColumnModel().getColumn(2).setPreferredWidth(100);
		tableDetalle.getColumnModel().getColumn(3).setPreferredWidth(100);
		tableDetalle.getColumnModel().getColumn(4).setPreferredWidth(100);
		tableDetalle.getColumnModel().getColumn(5).setPreferredWidth(100);
		tableDetalle.getColumnModel().getColumn(6).setPreferredWidth(180);
		
		tableDetalle.setRowHeight(30);
		tableDetalle.setToolTipText("Use +/- para cantidades, Delete para eliminar, F7 descuento, F8 precio");
		JScrollPane scrollPane = new JScrollPane(tableDetalle);
		
		scrollPane.setBackground(color3);
		scrollPane.getViewport().setBackground(color3);
		
		panel_2 = new JPanel();
		panel_2.setBackground(color3);
		panelNorte.add(panel_2, BorderLayout.WEST);
		
		lblLogo = new JLabel("");
		ImageIcon logoOriginal = new ImageIcon(ViewFacturarFrame.class.getResource("/drawable/logo_facturar.png"));
		int logoAlto = 55;
		int logoAncho = logoOriginal.getIconWidth() * logoAlto / logoOriginal.getIconHeight();
		lblLogo.setIcon(new ImageIcon(logoOriginal.getImage().getScaledInstance(logoAncho, logoAlto, Image.SCALE_SMOOTH)));
		panel_2.add(lblLogo);
		
		panel_1 = new JPanel();
		panel_1.setBackground(color3);
		panel_1.setLayout(new BorderLayout(0, 0));
		panelNorte.add(panel_1, BorderLayout.CENTER);
		
		
		
		
		panelDatosFactura=new JPanel();
		panel_1.add(panelDatosFactura, BorderLayout.NORTH);
		panelDatosFactura.setBackground(color3);

		panelDatosFactura.setBorder(BorderFactory.createCompoundBorder(
			new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)),
			BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(130, 135, 144))
		));
		panelDatosFactura.setLayout(new GridLayout(0, 5, 10, 0));

		lblCodigoCliente = new JLabel("Id Cliente");
		lblCodigoCliente.setFont(new Font("Georgia", Font.BOLD, 13));
		panelDatosFactura.add(lblCodigoCliente);
		
		lblNombreCliente = new JLabel("Nombre Cliente");
		lblNombreCliente.setFont(new Font("Georgia", Font.BOLD, 13));
		panelDatosFactura.add(lblNombreCliente);
		
		
		JLabel lblRtn = new JLabel("R:T:N");
		lblRtn.setFont(new Font("Georgia", Font.BOLD, 13));
		panelDatosFactura.add(lblRtn);
		
		lblContado = new JLabel("Contado");
		lblContado.setFont(new Font("Georgia", Font.BOLD, 13));
		lblContado.setHorizontalAlignment(SwingConstants.CENTER);
		panelDatosFactura.add(lblContado);
		
		lblCredito = new JLabel("Credito");
		lblCredito.setFont(new Font("Georgia", Font.BOLD, 13));
		lblCredito.setHorizontalAlignment(SwingConstants.CENTER);
		panelDatosFactura.add(lblCredito);
		
		txtFechafactura = new JTextField();
		txtFechafactura.setEditable(false);
		txtFechafactura.setVisible(false);

		txtIdcliente = new JTextField();
		txtIdcliente.setBackground(color4);
		txtIdcliente.setToolTipText("Ingrese el codigo del cliente y presione Enter");
		panelDatosFactura.add(txtIdcliente);
		txtIdcliente.setColumns(5);

		txtNombrecliente = new JTextField();
		txtNombrecliente.setBackground(color4);
		txtNombrecliente.setToolTipText("Nombre del cliente");
		panelDatosFactura.add(txtNombrecliente);
		txtNombrecliente.setColumns(20);

		txtRtn = new JTextField();
		txtRtn.setBackground(color4);
		txtRtn.setToolTipText("Registro Tributario Nacional del cliente");
		panelDatosFactura.add(txtRtn);
		txtRtn.setColumns(10);
		
		rdbtnContado = new JRadioButton("");
		rdbtnContado.setHorizontalAlignment(SwingConstants.CENTER);
		rdbtnContado.setSelected(true);
		grupoOpciones.add(rdbtnContado);
		panelDatosFactura.add(rdbtnContado);
		rdbtnCredito = new JRadioButton("");
		rdbtnCredito.setHorizontalAlignment(SwingConstants.CENTER);
		grupoOpciones.add(rdbtnCredito);
		panelDatosFactura.add(rdbtnCredito);
		
		
		
		
		
		
		
		
		panelBuscar= new JPanel();
		panelBuscar.setBackground(color3);
		panelBuscar.setLayout(new BorderLayout(5, 0));

		JLabel lblBuscar = new JLabel(" Buscar:");
		lblBuscar.setFont(new Font("Georgia", Font.BOLD, 13));
		panelBuscar.add(lblBuscar, BorderLayout.WEST);

		txtBuscar = new JTextField();
		txtBuscar.setForeground(Color.WHITE);
		txtBuscar.setBackground(new Color(30, 120, 70));
		txtBuscar.setToolTipText("Buscar articulo por nombre o codigo de barras");
		txtBuscar.setPreferredSize(new Dimension(0, 30));
		panelBuscar.add(txtBuscar, BorderLayout.CENTER);

		JPanel panelCentral = new JPanel(new BorderLayout());
		panelCentral.add(panelBuscar, BorderLayout.NORTH);
		panelCentral.add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(panelCentral, BorderLayout.CENTER);
		
		getContentPane().add(panelAcciones, BorderLayout.WEST);
		panelAcciones.setLayout(new GridLayout(9, 1, 0, 0));
		
		btnBuscar = new BotonBuscar1();
		btnBuscar.setBackground(color1);
		btnBuscar.setText("F1 Buscar");
		btnBuscar.setToolTipText("Buscar articulo por descripcion (F1)");
		panelAcciones.add(btnBuscar);

		btnCobrar = new BotonCobrar();
		btnCobrar.setBackground(color1);
		btnCobrar.setText("F2 Cobrar");
		btnCobrar.setToolTipText("Cobrar y cerrar la factura (F2)");
		panelAcciones.add(btnCobrar);

		btnCliente = new BotonBuscarClientes();
		btnCliente.setBackground(color1);
		btnCliente.setText("Clientes");
		btnCliente.setToolTipText("Buscar y seleccionar cliente");
		panelAcciones.add(btnCliente);

		btnGuardar = new BotonGuardar();
		btnGuardar.setBackground(color1);
		btnGuardar.setText("Ctrl+G Guardar");
		btnGuardar.setToolTipText("Guardar factura como pendiente (Ctrl+G)");
		panelAcciones.add(btnGuardar);

		btnGuardarCotizacion = new BotonCrearCotizaciones();
		btnGuardarCotizacion.setBackground(color1);
		btnGuardarCotizacion.setText("Crear cotizacion");
		btnGuardarCotizacion.setToolTipText("Crear una cotizacion de la factura actual");
		panelAcciones.add(btnGuardarCotizacion);

		btnGetCotizacion = new BotonPendientes();
		btnGetCotizacion.setBackground(color1);
		btnGetCotizacion.setText("Cotizaciones");
		btnGetCotizacion.setToolTipText("Ver lista de cotizaciones guardadas");
		panelAcciones.add(btnGetCotizacion);

		btnCierreCaja = new BotonCierreCaja();
		btnCierreCaja.setBackground(color1);
		btnCierreCaja.setText("F6 Cierre");
		btnCierreCaja.setHorizontalTextPosition(SwingConstants.CENTER);
		btnCierreCaja.setToolTipText("Realizar cierre de caja (F6)");
		panelAcciones.add(btnCierreCaja);

		btnActualizar=new BotonActualizar();
		btnActualizar.setEnabled(false);
		btnActualizar.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnActualizar.setBackground(color1);
		btnActualizar.setText("Ctrl+A Actualizar");
		btnActualizar.setToolTipText("Actualizar factura pendiente (Ctrl+A)");
		panelAcciones.add(btnActualizar);

		btnCerrar = new BotonCancelar();
		btnCerrar.setBackground(color1);
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setToolTipText("Cerrar ventana de facturacion (Esc)");
		panelAcciones.add(btnCerrar);





		panelGuardados = new JPanel();
		panelGuardados.setBackground(color3);
		panelGuardados.setLayout(new GridLayout(0, 1, 0, 0));
		panelGuardados.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Pendientes", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));

		JScrollPane scrollPane2 = new JScrollPane(panelGuardados, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane2.setPreferredSize(new Dimension(160, 100));
		scrollPane2.setBackground(color3);
		getContentPane().add(scrollPane2, BorderLayout.EAST);


		
		panel = new JPanel();
		panel.setBackground(color3);
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 5, 5, 2));
		
		lblSubtotal = new JLabel("SubTotal");
		lblSubtotal.setFont(new Font("Georgia", Font.BOLD, 13));
		lblSubtotal.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblSubtotal);
		
		JLabel lblDescuento = new JLabel("Descuento");
		lblDescuento.setFont(new Font("Georgia", Font.BOLD, 13));
		lblDescuento.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDescuento);
		
		lblImpuesto = new JLabel("Impuesto 15");
		lblImpuesto.setFont(new Font("Georgia", Font.BOLD, 13));
		lblImpuesto.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblImpuesto);
		
		JLabel lblImpuesto_1 = new JLabel("Impuesto 18");
		lblImpuesto_1.setFont(new Font("Georgia", Font.BOLD, 13));
		lblImpuesto_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblImpuesto_1);
		
		lblTotal = new JLabel("Total");
		lblTotal.setFont(new Font("Georgia", Font.BOLD, 13));
		lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblTotal);
		
		
		
		
		txtSubtotal = new JTextField();
		txtSubtotal.setBackground(color4);
		panel.add(txtSubtotal);
		txtSubtotal.setFont(new Font("Dialog", Font.PLAIN, 35));
		txtSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtSubtotal.setText("00");
		txtSubtotal.setEditable(false);
		
		
		txtDescuento = new JTextField();
		txtDescuento.setBackground(color4);
		panel.add(txtDescuento);
		txtDescuento.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDescuento.setEditable(false);
		txtDescuento.setText("00");
		txtDescuento.setFont(new Font("Dialog", Font.PLAIN, 35));
		
		
		
		txtImpuesto = new JTextField();
		txtImpuesto.setBackground(color4);
		panel.add(txtImpuesto);
		txtImpuesto.setHorizontalAlignment(SwingConstants.RIGHT);
		txtImpuesto.setFont(new Font("Dialog", Font.PLAIN, 35));
		txtImpuesto.setText("00");
		txtImpuesto.setEditable(false);
		
		
		
		txtImpuesto18 = new JTextField();
		txtImpuesto18.setBackground(color4);
		panel.add(txtImpuesto18);
		txtImpuesto18.setText("00");
		txtImpuesto18.setHorizontalAlignment(SwingConstants.RIGHT);
		txtImpuesto18.setFont(new Font("Dialog", Font.PLAIN, 35));
		txtImpuesto18.setEditable(false);
		
		
		
		
		
		
		txtTotal = new JTextField();
		txtTotal.setBackground(color4);
		panel.add(txtTotal);
		txtTotal.setForeground(Color.RED);
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setFont(new Font("Dialog", Font.PLAIN, 35));
		txtTotal.setText("00");
		txtTotal.setEditable(false);
		txtTotal.setColumns(8);
		
		
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setMaximizable(true);
		
		//this.pack();
		this.rdbtnCredito.setSelected(false);
		
	}
	
	
	public void addBotonPendiente(Factura newFactura,CtlFacturarFrame c){
		
		
		btnGuardados.addBoton(newFactura);
		actualizarView(c);
		
		
		
	}
	private void actualizarView(CtlFacturarFrame c) {
		eliminarBotones();
		
		for(int x=0;x<btnGuardados.getSize();x++){
			
			JToggleButton boton=btnGuardados.getElementAt(x);
			Factura fac=btnGuardados.getFacturaBoton(x);
			conectarBtnContralador(c, fac.getIdFactura()+"", boton);
			panelGuardados.add(boton);
			
		}
		panelGuardados.updateUI();
	}
	public void eliminarBotones(){
		panelGuardados.removeAll();
		panelGuardados.revalidate();
		panelGuardados.repaint();
	}
	public ListaBotonesFacturas getBtnsGuardador(){
		return btnGuardados;
	}
	public CbxTmEmpleado getModeloEmpleados(){
		return this.modeloEmpleado;
	}
	
	public JRadioButton getRdbtnContado(){
		return rdbtnContado;
	}
	public  JRadioButton getRdbtnCredito(){
		return  rdbtnCredito;
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public JTextField getTxtRtn(){
		return txtRtn;
	}
	public BotonGuardar getBtnGuardar(){
		return btnGuardar;
	}
	public JButton getBtnBuscar(){
		return btnBuscar;
	}
	public JButton getBtnBuscarCliente(){
		return btnCliente;
	}
	public JButton getBtnCobrar(){
		return btnCobrar;
	}
	public JButton getBtnCerrar(){
		return btnCerrar;
	}
	public JButton getBtnGuardarCotizacion(){
		return this.btnGuardarCotizacion;
	}
	public JPanel getPanelAcciones(){
		return panelAcciones;
	}
	public JTextField getTxtDescuento(){
		return txtDescuento;		
	}
	public JTextField getTxtSubtotal(){
		return txtSubtotal;
	}
	public JTextField getTxtImpuesto(){
		return txtImpuesto;
	}
	public JTextField getTxtImpuesto18(){
		return txtImpuesto18;
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
	public void actualizarTotales(net.datatecsolution.admin_tools.modelo.Factura factura) {
		txtTotal.setText("" + factura.getTotal().setScale(2, java.math.BigDecimal.ROUND_HALF_EVEN));
		txtImpuesto.setText("" + factura.getTotalImpuesto().setScale(2, java.math.BigDecimal.ROUND_HALF_EVEN));
		txtImpuesto18.setText("" + factura.getTotalImpuesto18().setScale(2, java.math.BigDecimal.ROUND_HALF_EVEN));
		txtSubtotal.setText("" + factura.getSubTotal().setScale(2, java.math.BigDecimal.ROUND_HALF_EVEN));
		txtDescuento.setText("" + factura.getTotalDescuento().setScale(2, java.math.BigDecimal.ROUND_HALF_EVEN));
	}

	public void resetTotales() {
		txtDescuento.setText("");
		txtImpuesto.setText("0.00");
		txtImpuesto18.setText("0.00");
		txtSubtotal.setText("0.00");
		txtTotal.setText("0.00");
	}

	public TablaModeloFactura getModeloTabla(){
		return modeloTabla;
	}
	public JTable getTableDetalle(){
		return tableDetalle;
	}
	public JTextField getTxtBuscar(){
		return txtBuscar;
	}
	public String getTextoBusqueda() {
		return txtBuscar.getText();
	}
	public void setTextoBusqueda(String texto) {
		txtBuscar.setText(texto);
	}
	public void limpiarBusqueda() {
		txtBuscar.setText("");
	}
	public void enfocarBusqueda() {
		txtBuscar.requestFocusInWindow();
	}
	public void limpiarYEnfocarBusqueda() {
		txtBuscar.setText("");
		txtBuscar.requestFocusInWindow();
	}
	public void marcarBusquedaNivelFact(boolean nivelFact) {
		txtBuscar.setBackground(nivelFact ? new Color(250, 0, 0) : new Color(60, 179, 113));
	}
	public JPopupMenu getMenuContextual(){
		return menuContextual;
		
	}
	public JTextField getTxtFechafactura(){
		return txtFechafactura;
	}

	public FacturaCabeceraData getCabeceraData() {
		int tipo = rdbtnCredito.isSelected()
				? FacturaCabeceraData.TIPO_CREDITO
				: FacturaCabeceraData.TIPO_CONTADO;
		return new FacturaCabeceraData(tipo, txtFechafactura.getText());
	}

	public void setCabeceraData(FacturaCabeceraData data) {
		if (data.esCredito()) {
			rdbtnCredito.setSelected(true);
		} else {
			rdbtnContado.setSelected(true);
		}
		txtFechafactura.setText(data.getFecha());
	}

	public FacturaClienteData getClienteData() {
		int id = 0;
		try {
			id = Integer.parseInt(txtIdcliente.getText().trim());
		} catch (NumberFormatException e) {
			id = 0;
		}
		return new FacturaClienteData(id, txtNombrecliente.getText(), txtRtn.getText());
	}

	public void setClienteData(FacturaClienteData data) {
		txtIdcliente.setText("" + data.getId());
		txtNombrecliente.setText(data.getNombre());
		txtRtn.setText(data.getRtn() == null ? "" : data.getRtn());
	}

	public void conectarBtnContralador(CtlFacturarFrame c,String cmd,JToggleButton btn){
		btn.addActionListener(c);
		
		btn.setActionCommand(cmd);
		
		btn.addKeyListener(c);
		btn.addMouseListener(c);
	}
	public void conectarContralador(CtlFacturarFrame c){
		ctl=c;
		
		txtIdcliente.addActionListener(c);
		txtIdcliente.setActionCommand("BUSCARCLIENTE");
		
		tableDetalle.addKeyListener(c);
		tableDetalle.addMouseListener(c);
		modeloTabla.addTableModelListener(c);
		tableDetalle.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableDetalle.setColumnSelectionAllowed(true);
		tableDetalle.setRowSelectionAllowed(true);
		tableDetalle.setCellSelectionEnabled(true);
		
		txtIdcliente.addKeyListener(c);
		txtNombrecliente.addKeyListener(c);
		
		btnCierreCaja.addKeyListener(c);
		btnCierreCaja.addActionListener(c);
		btnCierreCaja.setActionCommand("CIERRECAJA");
		
		
		btnGuardarCotizacion.addKeyListener(c);
		btnGuardarCotizacion.addActionListener(c);
		btnGuardarCotizacion.setActionCommand("COTIZACION");
		
		btnGetCotizacion.addKeyListener(c);
		btnGetCotizacion.addActionListener(c);
		btnGetCotizacion.setActionCommand("GET_COTIZACIONES");
		
		this.btnBuscar.addKeyListener(c);
		this.btnBuscar.addActionListener(c);
		this.btnBuscar.setActionCommand("BUSCARARTICULO");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCARARTICULO2");
		
		this.btnCerrar.addKeyListener(c);
		this.btnCerrar.addActionListener(c);
		this.btnCerrar.setActionCommand("CERRAR");
		
		this.btnCliente.addKeyListener(c);
		this.btnCliente.addActionListener(c);
		this.btnCliente.setActionCommand("BUSCARCLIENTES");
		
		this.btnCobrar.addKeyListener(c);
		this.btnCobrar.addActionListener(c);
		this.btnCobrar.setActionCommand("COBRAR");
		
		this.btnGuardar.addKeyListener(c);
		this.btnGuardar.addActionListener(c);
		this.btnGuardar.setActionCommand("GUARDAR");
		
		btnActualizar.addKeyListener(c);
		this.btnActualizar.addActionListener(c);
		this.btnActualizar.setActionCommand("ACTUALIZAR");
		
		mntmEliminar.addActionListener(c);
		mntmEliminar.setActionCommand("ELIMINARPENDIENTE");

		mntmImprimir.addActionListener(c);
		mntmImprimir.setActionCommand("IMPRIMIRPENDIENTE");
		
		this.rdbtnContado.addKeyListener(c);
		this.rdbtnCredito.addKeyListener(c);
		this.txtDescuento.addKeyListener(c);
		this.txtImpuesto.addKeyListener(c);
		this.txtSubtotal.addKeyListener(c);
		txtRtn.addKeyListener(c);
		this.txtTotal.addKeyListener(c);
		txtBuscar.addKeyListener(c);
		//txtBuscar.
		this.addInternalFrameListener(c);
	}


	public CtlFacturarFrame getCtl() {
		return ctl;
	}


}
