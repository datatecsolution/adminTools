package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlMenuPrincipal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewMenuPrincipal extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JLabel usuario = new JLabel("Usuario:");
	private JMenuItem mntmProveedores;
	private JMenuItem mntmArticulos;
	private JMenuItem mntmCategorias;
	private JMenuItem mntmFacturar;
	private JMenuItem mntmClientes;
	private JMenuItem mntmBuscarFacturas;
	private JLabel lblUserName;
	private JMenu mnArchivo;
	private JMenuItem mntmUsuarios;
	private JMenuItem mntmSalir;
	private JMenuItem mnRequisiciones;
	
	
	private JMenuItem mntmListaPagos;
	private JMenu mnReportes;
	private JMenuItem mntmDeclaracionDei;

	private JMenuItem mntmInventario;

	private JMenuItem mntmCierresDeCaja;

	private JMenuItem mntmEmpleados;
	private JMenuItem mntmComisiones;
	private JMenu mnCompras;
	private JMenuItem mntmGestionCompas;
	private JMenuItem mntmSalidasCaja;
	private JMenuItem mntmPagoAproveedores;
	private JMenuItem mntmCotizaciones;
	private JMenuItem mntmCuentasDeBancos;
	private JButton btnAlertaExistencia;
	private JMenuItem mntmCajas;
	private JMenuItem mntmDatosFacturacion;
	private JMenuItem mntmDeclaracionSarCompras;
	private JMenuItem mntmVentasUsuarios;
	private JMenuItem mntmEntradasCaja;
	private JMenu mnBancos;
	private JMenuItem mntmDepositoretiros;
	private JMenuItem mntmConfiguracionUsuarios;
	private JMenuItem mntmAplicarInteresA;
	private JMenuItem mntmFacturasVencidas;
	private JMenuItem mntmRutasDeEntregas;
	private JMenuItem mntmRutasDeCobro;
	private JMenuItem mntmReporteCuentasFacturas;
	
	public ViewMenuPrincipal() {
		setTitle("Admin Tools");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewMenuPrincipal.class.getResource("/drawable/logo-admin-tool1.png")));
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);
		
		mntmUsuarios = new JMenuItem("Usuarios");
		mnArchivo.add(mntmUsuarios);
		
		mntmConfiguracionUsuarios = new JMenuItem("Configuracion Usuarios");
		mnArchivo.add(mntmConfiguracionUsuarios);
		
		mntmEmpleados = new JMenuItem("Empleados");
		mnArchivo.add(mntmEmpleados);
		
		mntmAplicarInteresA = new JMenuItem("Interes a facturas venc");
		mnArchivo.add(mntmAplicarInteresA);
		
		mntmSalir = new JMenuItem("Salir");
		mnArchivo.add(mntmSalir);
		
		JMenu mnInventario = new JMenu("Inventario");
		menuBar.add(mnInventario);
		
		mntmProveedores = new JMenuItem("Proveedores");
		mnInventario.add(mntmProveedores);
		
		mntmArticulos = new JMenuItem("Articulos");
		mnInventario.add(mntmArticulos);
		
		mntmCategorias = new JMenuItem("Categorias");
		mnInventario.add(mntmCategorias);
		
		mnRequisiciones = new JMenuItem("Requisiciones");
		mnInventario.add(mnRequisiciones);
		
		JMenu mnFacturacion = new JMenu("Ventas");
		menuBar.add(mnFacturacion);
		
		mntmFacturar = new JMenuItem("Facturar");
		mnFacturacion.add(mntmFacturar);
		
		mntmDatosFacturacion = new JMenuItem("Datos Facturacion");
		mnFacturacion.add(mntmDatosFacturacion);
		
		mntmCajas = new JMenuItem("Cajas");
		mnFacturacion.add(mntmCajas);
		

		mntmClientes = new JMenuItem("Clientes");
		mnFacturacion.add(mntmClientes);
		
		mntmBuscarFacturas = new JMenuItem("Buscar facturas");
		mnFacturacion.add(mntmBuscarFacturas);
		
		mntmEntradasCaja = new JMenuItem("Entradas caja");
		mnFacturacion.add(mntmEntradasCaja);
		
		mntmSalidasCaja = new JMenuItem("Salidas caja");
		mnFacturacion.add(mntmSalidasCaja);
		
		mntmCotizaciones = new JMenuItem("Cotizaciones");
		mnFacturacion.add(mntmCotizaciones);
		
		mntmRutasDeEntregas = new JMenuItem("Rutas de entregas");
		mnFacturacion.add(mntmRutasDeEntregas);

		mntmRutasDeCobro= new JMenuItem("Rutas de cobros");
		mnFacturacion.add(mntmRutasDeCobro);
		
		mnCompras = new JMenu("Compras");
		menuBar.add(mnCompras);
		
		mntmGestionCompas = new JMenuItem("Gestion compras");
		mnCompras.add(mntmGestionCompas);
		
		JMenu mnCuentasPorCobrar = new JMenu("Cuentas por cobrar");
		menuBar.add(mnCuentasPorCobrar);
		
		mntmListaPagos = new JMenuItem("Pagos");
		mnCuentasPorCobrar.add(mntmListaPagos);
		
		mntmFacturasVencidas = new JMenuItem("CXC por facturas");
		mnCuentasPorCobrar.add(mntmFacturasVencidas);

		mntmReporteCuentasFacturas= new JMenuItem("Reportes CXC por facturas");
		mnCuentasPorCobrar.add(mntmReporteCuentasFacturas);
		
		
		mnReportes = new JMenu("Reportes");
		menuBar.add(mnReportes);
		
		mntmDeclaracionDei = new JMenuItem("Declaracion SAR ventas");
		mnReportes.add(mntmDeclaracionDei);
		
		mntmDeclaracionSarCompras = new JMenuItem("Declaracion SAR compras");
		mnReportes.add(mntmDeclaracionSarCompras);
		
		mntmInventario = new JMenuItem("Inventario");
		mnReportes.add(mntmInventario);
		
		mntmCierresDeCaja = new JMenuItem("Cierres de caja");
		mnReportes.add(mntmCierresDeCaja);
		
		mntmComisiones = new JMenuItem("Comisiones");
		mnReportes.add(mntmComisiones);
		
		mntmVentasUsuarios = new JMenuItem("Ventas usuarios");
		mnReportes.add(mntmVentasUsuarios);
		
		JMenu mnCuentasPorPagar = new JMenu("Cuentas por pagar");
		menuBar.add(mnCuentasPorPagar);
		
		mntmPagoAproveedores = new JMenuItem("Pagos de aproveedores");
		mnCuentasPorPagar.add(mntmPagoAproveedores);
		
		mnBancos = new JMenu("Bancos");
		menuBar.add(mnBancos);
		
		mntmCuentasDeBancos = new JMenuItem("Cuentas de bancos");
		mnBancos.add(mntmCuentasDeBancos);
		
		mntmDepositoretiros = new JMenuItem("Transaccione");
		mnBancos.add(mntmDepositoretiros);
		
		
		JMenuItem mntmAcercaDe = new JMenuItem("Acerca de..");
		menuBar.add(mntmAcercaDe);
		setSize(1024,700);
		
		JPanel panel = new JPanel();
		//panel.setBackground(new Color(0, 191, 255));
		//panel.setBackground(new Color(119, 136, 153));
		panel.setSize(700, 100);
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(usuario);
		
		lblUserName = new JLabel("Unico");
		panel.add(lblUserName);
		
		btnAlertaExistencia = new JButton("New button");
		panel.add(btnAlertaExistencia);
		
		JPanel panel_1 = new panelFondo();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	
	public void conectarControlador(CtlMenuPrincipal c){
		
		mntmRutasDeEntregas.addActionListener(c);
		mntmRutasDeEntregas.setActionCommand("RUTAS_ENTREGAS");

		mntmRutasDeCobro.addActionListener(c);
		mntmRutasDeCobro.setActionCommand("RUTAS_COBROS");
		
		mntmFacturasVencidas.addActionListener(c);
		mntmFacturasVencidas.setActionCommand("FACT_VENCIDAS");
		
		mntmAplicarInteresA.addActionListener(c);
		mntmAplicarInteresA.setActionCommand("APLICAR_INTERES_FACT");
		
		mntmConfiguracionUsuarios.addActionListener(c);
		mntmConfiguracionUsuarios.setActionCommand("CONFIG_USUARIOS");
		
		mntmVentasUsuarios.addActionListener(c);
		mntmVentasUsuarios.setActionCommand("VENTASUSUARIOS");
		
		mntmDepositoretiros.addActionListener(c);
		mntmDepositoretiros.setActionCommand("DEPOSITOS_Y_RETIROS");
		
		mntmDatosFacturacion.addActionListener(c);
		mntmDatosFacturacion.setActionCommand("DATOSFACTURACION");
		
		mntmProveedores.addActionListener(c);
		mntmProveedores.setActionCommand("PROVEEDORES");
		
		mntmArticulos.addActionListener(c);
		mntmArticulos.setActionCommand("ARTICULOS");
		
		mntmCategorias.addActionListener(c);
		mntmCategorias.setActionCommand("CATEGORIAS");
		
		
		mntmGestionCompas.addActionListener(c);
		mntmGestionCompas.setActionCommand("LISTAFACTURASCOMPRA");
		
		mntmCuentasDeBancos.addActionListener(c);
		mntmCuentasDeBancos.setActionCommand("CUENTASBANCOS");
		
		
		
		mntmFacturar.addActionListener(c);
		mntmFacturar.setActionCommand("FACTURAR");
		
		mntmClientes.addActionListener(c);
		mntmClientes.setActionCommand("CLIENTES");
		
		mntmBuscarFacturas.addActionListener(c);
		mntmBuscarFacturas.setActionCommand("BUSCARFACTURAS");
		
		mnRequisiciones.addActionListener(c);
		mnRequisiciones.setActionCommand("REQUISICIONES");
		
		mntmUsuarios.addActionListener(c);
		mntmUsuarios.setActionCommand("USUARIOS");
		
		
		
		mntmListaPagos.addActionListener(c);
		mntmListaPagos.setActionCommand("LISTAPAGOS");
		
		
		mntmDeclaracionDei.addActionListener(c);
		mntmDeclaracionDei.setActionCommand("R_DEI_VENTAS");
		
		mntmDeclaracionSarCompras.addActionListener(c);
		mntmDeclaracionSarCompras.setActionCommand("R_DEI_COMPRAS");
		
		mntmInventario.addActionListener(c);
		mntmInventario.setActionCommand("INVENTARIO");
		
		
		mntmCierresDeCaja.addActionListener(c);
		mntmCierresDeCaja.setActionCommand("CIERRES_CAJA");
		
		mntmEmpleados.addActionListener(c);
		mntmEmpleados.setActionCommand("EMPLEADOS");
		
		mntmComisiones.addActionListener(c);
		mntmComisiones.setActionCommand("COMISIONES");
		
		mntmSalidasCaja.addActionListener(c);
		mntmSalidasCaja.setActionCommand("SALIDASCAJAS");
		
		mntmEntradasCaja.addActionListener(c);
		mntmEntradasCaja.setActionCommand("ENTRADASCAJAS");
		
		mntmPagoAproveedores.addActionListener(c);
		mntmPagoAproveedores.setActionCommand("PAGOPROVEEDORES");
		
		
		mntmCotizaciones.addActionListener(c);
		mntmCotizaciones.setActionCommand("COTIZACIONES");
		
		btnAlertaExistencia.addActionListener(c);
		btnAlertaExistencia.setActionCommand("ALERTAEXISTENCIAS");

		mntmReporteCuentasFacturas.addActionListener(c);
		mntmReporteCuentasFacturas.setActionCommand("REPORTE_CXC_FACTURA");
		
		mntmCajas.addActionListener(c);
		mntmCajas.setActionCommand("CAJAS");
		
	}
	public JLabel getLblUserName(){
		return lblUserName;
	}
	
	private class panelFondo extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		   public void paintComponent(Graphics g){
		      Dimension tamanio = getSize();
		      ImageIcon imagenFondo = new ImageIcon(getClass().
		      getResource("/drawable/fondo-sistema.jpg"));
		      g.drawImage(imagenFondo.getImage(), 0, 0,
		      tamanio.width, tamanio.height, null);
		      setOpaque(false);
		      super.paintComponent(g);
		   }
	}
	/**
	 * @return the btnAlertaExistencia
	 */
	public JButton getBtnAlertaExistencia() {
		return btnAlertaExistencia;
	}

}
