package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlMenuPrincipalFrame;

import javax.swing.*;
import java.awt.*;

public class ViewMenuPrincipalFrame extends JFrame{
	
	private final JDesktopPane elEscritorio;
	private final JLabel usuario = new JLabel("Usuario:");
	private final JMenuItem mntmProveedores;
	private final JMenuItem mntmArticulos;
	private final JMenuItem mntmCategorias;
	private final JMenuItem mntmFacturar;
	private final JMenuItem mntmClientes;
	private final JMenuItem mntmBuscarFacturas;
	private final JLabel lblUserName;
	private final JMenu mnArchivo;
	private final JMenuItem mntmUsuarios;
	private final JMenuItem mntmSalir;
	private final JMenuItem mnRequisiciones;
	
	
	private final JMenuItem mntmListaPagos;
	private final JMenu mnReportes;
	private final JMenuItem mntmDeclaracionDei;

	private final JMenuItem mntmInventario;

	private final JMenuItem mntmCierresDeCaja;

	private final JMenuItem mntmEmpleados;
	private final JMenuItem mntmComisiones;
	private final JMenu mnCompras;
	private final JMenuItem mntmGestionCompas;
	private final JMenuItem mntmSalidasCaja;
	private final JMenuItem mntmPagoAproveedores;
	private final JMenuItem mntmCotizaciones;
	private final JMenuItem mntmCuentasDeBancos;
	private final JButton btnAlertaExistencia;
	
	public ViewMenuPrincipalFrame() {
		setTitle("Admin Tools");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewMenuPrincipalFrame.class.getResource("/drawable/logo-admin-tool1.png")));
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);
		
		mntmUsuarios = new JMenuItem("Usuarios");
		mnArchivo.add(mntmUsuarios);
		
		mntmEmpleados = new JMenuItem("Empleados");
		mnArchivo.add(mntmEmpleados);
		
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
		
		JMenu mnFacturacion = new JMenu("Facturacion");
		menuBar.add(mnFacturacion);
		
		mntmFacturar = new JMenuItem("Facturar");
		mnFacturacion.add(mntmFacturar);
		

		mntmClientes = new JMenuItem("Clientes");
		mnFacturacion.add(mntmClientes);
		
		mntmBuscarFacturas = new JMenuItem("Buscar facturas");
		mnFacturacion.add(mntmBuscarFacturas);
		
		mntmSalidasCaja = new JMenuItem("Salidas caja");
		mnFacturacion.add(mntmSalidasCaja);
		
		mntmCotizaciones = new JMenuItem("Cotizaciones");
		mnFacturacion.add(mntmCotizaciones);
		
		mnCompras = new JMenu("Compras");
		menuBar.add(mnCompras);
		
		mntmGestionCompas = new JMenuItem("Gestion compras");
		mnCompras.add(mntmGestionCompas);
		
		JMenu mnCuentasPorCobrar = new JMenu("Cuentas por cobrar");
		menuBar.add(mnCuentasPorCobrar);
		
		mntmListaPagos = new JMenuItem("Ver pagos");
		mnCuentasPorCobrar.add(mntmListaPagos);
		
		
		mnReportes = new JMenu("Reportes");
		menuBar.add(mnReportes);
		
		mntmDeclaracionDei = new JMenuItem("Declaracion DEI");
		mnReportes.add(mntmDeclaracionDei);
		
		mntmInventario = new JMenuItem("Inventario");
		mnReportes.add(mntmInventario);
		
		mntmCierresDeCaja = new JMenuItem("Cierres de caja");
		mnReportes.add(mntmCierresDeCaja);
		
		mntmComisiones = new JMenuItem("Comisiones");
		mnReportes.add(mntmComisiones);
		
		JMenu mnCuentasPorPagar = new JMenu("Cuentas por pagar");
		menuBar.add(mnCuentasPorPagar);
		
		mntmPagoAproveedores = new JMenuItem("Pagos de aproveedores");
		mnCuentasPorPagar.add(mntmPagoAproveedores);
		
		mntmCuentasDeBancos = new JMenuItem("Cuentas de bancos");
		mnCuentasPorPagar.add(mntmCuentasDeBancos);
		
		
		JMenuItem mntmAcercaDe = new JMenuItem("Acerca de..");
		menuBar.add(mntmAcercaDe);
		setSize(1024,700);
		
		
		elEscritorio = new JDesktopPane(); // crea el panel de escritorio
		getContentPane().add( elEscritorio ); // agrega el panel de escritorio al marco
		
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
		
		/*JPanel panel_1 = new panelFondo();
		getContentPane().add(panel_1, BorderLayout.CENTER);*/
		
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		/*addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});*/
	}
	
	

	public void conectarControlador(CtlMenuPrincipalFrame c){
		
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
		mntmDeclaracionDei.setActionCommand("R_DEI");
		
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
		
		mntmPagoAproveedores.addActionListener(c);
		mntmPagoAproveedores.setActionCommand("PAGOPROVEEDORES");
		
		
		mntmCotizaciones.addActionListener(c);
		mntmCotizaciones.setActionCommand("COTIZACIONES");
		
		btnAlertaExistencia.addActionListener(c);
		btnAlertaExistencia.setActionCommand("ALERTAEXISTENCIAS");
		
	}
	public JLabel getLblUserName(){
		return lblUserName;
	}
	
	/*private class panelFondo extends JPanel{
		@Override
		   public void paintComponent(Graphics g){
		      Dimension tamanio = getSize();
		      ImageIcon imagenFondo = new ImageIcon(getClass().
		      getResource("/view/recursos/fondo-sistema.jpg"));
		      g.drawImage(imagenFondo.getImage(), 0, 0,
		      tamanio.width, tamanio.height, null);
		      setOpaque(false);
		      super.paintComponent(g);
		   }
	}*/
	/**
	 * @return the btnAlertaExistencia
	 */
	public JButton getBtnAlertaExistencia() {
		return btnAlertaExistencia;
	}



	/**
	 * @return the elEscritorio
	 */
	public JDesktopPane getElEscritorio() {
		return elEscritorio;
	}

}
