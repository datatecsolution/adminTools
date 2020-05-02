package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlConfigUser;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewConfigUser extends JDialog {
	private JComboBox<String> cbFacturaContado;
	private JComboBox<String> cbFormatoCredito;
	private JToggleButton tglbtnVentVendedor;
	private JToggleButton tglbtnPwdDescuento;
	private JToggleButton tglbtnPwdPrecio;
	private JToggleButton tglbtnVentObrs;
	private JToggleButton tglbtnVenBusq;
	private JToggleButton tglbtnRedPrecioVenta;
	private JToggleButton tglbtnDescPorcentaje;
	private JToggleButton tglbtnFactSinInven;
	private JToggleButton tglbtnAddClienteCredito;
	private JToggleButton tglbtnCategoriaEnCierre;
	private JToggleButton tglbtnImprimirSalida;
	private JToggleButton tglbtnObservarSalida;
	private JToggleButton tglbtnImprimirEntrada;
	private JToggleButton tglbtnObservarEntrada;
	private JLabel lblUsuairo;
	private JTextField txtUsuario;
	private JButton btnAtras;
	private JButton btnSig;

	public ViewConfigUser(Window view) {
		this.setTitle("Crear Cuentas de Bancos");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 64, 424, 230);
		getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		panel.setBackground(PanelPadre.color2);
		tabbedPane.addTab("Formato de Factura", null, panel, null);
		panel.setLayout(null);
		
		JLabel lblFormatoFacturaContado = new JLabel("Formato factura contado");
		lblFormatoFacturaContado.setBounds(6, 6, 188, 16);
		panel.add(lblFormatoFacturaContado);
		
		 cbFacturaContado = new JComboBox();
		cbFacturaContado.setModel(new DefaultComboBoxModel(new String[] {"tiket", "carta"}));
		cbFacturaContado.setBounds(6, 22, 391, 27);
		panel.add(cbFacturaContado);
		
		JLabel lblFormatoFacturaCredito = new JLabel("Formato factura credito");
		lblFormatoFacturaCredito.setBounds(6, 50, 181, 16);
		panel.add(lblFormatoFacturaCredito);
		
		cbFormatoCredito = new JComboBox();
		cbFormatoCredito.setModel(new DefaultComboBoxModel(new String[] {"tiket", "carta"}));
		cbFormatoCredito.setBounds(6, 68, 391, 27);
		panel.add(cbFormatoCredito);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(PanelPadre.color2);
		tabbedPane.addTab("Opciones de facturacion", null, panel_1, null);
		panel_1.setLayout(null);
		
		tglbtnVentVendedor = new JToggleButton("Ventana vendedor");
		tglbtnVentVendedor.setBounds(6, 6, 180, 29);
		panel_1.add(tglbtnVentVendedor);
		
		tglbtnPwdDescuento = new JToggleButton("Password descuento");
		tglbtnPwdDescuento.setBounds(6, 41, 180, 29);
		panel_1.add(tglbtnPwdDescuento);
		
		tglbtnPwdPrecio = new JToggleButton("Password Precio");
		tglbtnPwdPrecio.setBounds(6, 76, 180, 29);
		panel_1.add(tglbtnPwdPrecio);
		
		tglbtnVentObrs = new JToggleButton("Ventana observaciones");
		tglbtnVentObrs.setBounds(6, 111, 180, 29);
		panel_1.add(tglbtnVentObrs);
		
		tglbtnVenBusq = new JToggleButton("Ventana busqueda");
		tglbtnVenBusq.setBounds(6, 146, 180, 29);
		panel_1.add(tglbtnVenBusq);
		
		tglbtnRedPrecioVenta = new JToggleButton("Redondiar precio venta");
		tglbtnRedPrecioVenta.setBounds(210, 6, 187, 29);
		panel_1.add(tglbtnRedPrecioVenta);
		
		tglbtnDescPorcentaje = new JToggleButton("Descuento en porcentaje");
		tglbtnDescPorcentaje.setBounds(210, 41, 187, 29);
		panel_1.add(tglbtnDescPorcentaje);
		
		tglbtnFactSinInven = new JToggleButton("Facturar sin inventario");
		tglbtnFactSinInven.setBounds(210, 76, 187, 29);
		panel_1.add(tglbtnFactSinInven);
		
		tglbtnAddClienteCredito = new JToggleButton("Agregar cliente a credito");
		tglbtnAddClienteCredito.setBounds(210, 111, 187, 29);
		panel_1.add(tglbtnAddClienteCredito);
		
		JPanel panel_2 = new JPanel();
		panel_2.setForeground(Color.LIGHT_GRAY);
		panel_2.setBackground(PanelPadre.color2);
		tabbedPane.addTab("Reportes", null, panel_2, null);
		panel_2.setLayout(null);
		
		tglbtnCategoriaEnCierre = new JToggleButton("Categoria en cierre de caja");
		tglbtnCategoriaEnCierre.setBounds(6, 6, 203, 29);
		panel_2.add(tglbtnCategoriaEnCierre);
		
		tglbtnImprimirSalida = new JToggleButton("Imprimir salida");
		tglbtnImprimirSalida.setBounds(6, 41, 203, 29);
		panel_2.add(tglbtnImprimirSalida);
		
		tglbtnObservarSalida = new JToggleButton("Observar salida");
		tglbtnObservarSalida.setBounds(6, 76, 203, 29);
		panel_2.add(tglbtnObservarSalida);
		
		tglbtnImprimirEntrada = new JToggleButton("Imprimir entrada");
		tglbtnImprimirEntrada.setBounds(6, 111, 203, 29);
		panel_2.add(tglbtnImprimirEntrada);
		
		tglbtnObservarEntrada = new JToggleButton("Observar entrada");
		tglbtnObservarEntrada.setBounds(6, 146, 203, 29);
		panel_2.add(tglbtnObservarEntrada);
		
		lblUsuairo = new JLabel("Usuario");
		lblUsuairo.setBounds(17, 6, 61, 16);
		getContentPane().add(lblUsuairo);
		
		txtUsuario = new JTextField();
		txtUsuario.setEditable(false);
		txtUsuario.setBounds(17, 26, 413, 26);
		getContentPane().add(txtUsuario);
		txtUsuario.setColumns(10);
		
		btnAtras = new JButton("Atras");
		btnAtras.setBounds(16, 306, 77, 29);
		getContentPane().add(btnAtras);
		
		btnSig = new JButton("Sig");
		btnSig.setBounds(102, 306, 76, 29);
		getContentPane().add(btnSig);

		
		setSize(443,373);
		
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	public void conectarCtl(CtlConfigUser c){
		
		btnAtras.addActionListener(c);
		btnAtras.setActionCommand("NEXT");
		
		btnSig.addActionListener(c);
		btnSig.setActionCommand("BEFORE");
		
		cbFacturaContado.addActionListener(c);
		cbFacturaContado.setActionCommand("ELIMINAR_INSUMO");
		
		cbFormatoCredito.addActionListener(c);
		cbFormatoCredito.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnVentVendedor.addActionListener(c);
		tglbtnVentVendedor.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnPwdDescuento.addActionListener(c);
		tglbtnPwdDescuento.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnPwdPrecio.addActionListener(c);
		tglbtnPwdPrecio.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnVentObrs.addActionListener(c);
		tglbtnVentObrs.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnVenBusq.addActionListener(c);
		tglbtnVenBusq.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnRedPrecioVenta.addActionListener(c);
		tglbtnRedPrecioVenta.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnDescPorcentaje.addActionListener(c);
		tglbtnDescPorcentaje.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnFactSinInven.addActionListener(c);
		tglbtnFactSinInven.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnAddClienteCredito.addActionListener(c);
		tglbtnAddClienteCredito.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnCategoriaEnCierre.addActionListener(c);
		tglbtnCategoriaEnCierre.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnImprimirSalida.addActionListener(c);
		tglbtnImprimirSalida.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnObservarSalida.addActionListener(c);
		tglbtnObservarSalida.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnImprimirEntrada.addActionListener(c);
		tglbtnImprimirEntrada.setActionCommand("ELIMINAR_INSUMO");
		
		tglbtnObservarEntrada.addActionListener(c);
		tglbtnObservarEntrada.setActionCommand("ELIMINAR_INSUMO");
	}

	/**
	 * @return the cbFacturaContado
	 */
	public JComboBox<String> getCbFacturaContado() {
		return cbFacturaContado;
	}

	/**
	 * @param cbFacturaContado the cbFacturaContado to set
	 */
	public void setCbFacturaContado(JComboBox<String> cbFacturaContado) {
		this.cbFacturaContado = cbFacturaContado;
	}

	/**
	 * @return the cbFormatoCredito
	 */
	public JComboBox<String> getCbFormatoCredito() {
		return cbFormatoCredito;
	}

	/**
	 * @param cbFormatoCredito the cbFormatoCredito to set
	 */
	public void setCbFormatoCredito(JComboBox<String> cbFormatoCredito) {
		this.cbFormatoCredito = cbFormatoCredito;
	}

	/**
	 * @return the tglbtnVentVendedor
	 */
	public JToggleButton getTglbtnVentVendedor() {
		return tglbtnVentVendedor;
	}

	/**
	 * @param tglbtnVentVendedor the tglbtnVentVendedor to set
	 */
	public void setTglbtnVentVendedor(JToggleButton tglbtnVentVendedor) {
		this.tglbtnVentVendedor = tglbtnVentVendedor;
	}

	/**
	 * @return the tglbtnPwdDescuento
	 */
	public JToggleButton getTglbtnPwdDescuento() {
		return tglbtnPwdDescuento;
	}

	/**
	 * @param tglbtnPwdDescuento the tglbtnPwdDescuento to set
	 */
	public void setTglbtnPwdDescuento(JToggleButton tglbtnPwdDescuento) {
		this.tglbtnPwdDescuento = tglbtnPwdDescuento;
	}

	/**
	 * @return the tglbtnPwdPrecio
	 */
	public JToggleButton getTglbtnPwdPrecio() {
		return tglbtnPwdPrecio;
	}

	/**
	 * @param tglbtnPwdPrecio the tglbtnPwdPrecio to set
	 */
	public void setTglbtnPwdPrecio(JToggleButton tglbtnPwdPrecio) {
		this.tglbtnPwdPrecio = tglbtnPwdPrecio;
	}

	/**
	 * @return the tglbtnVentObrs
	 */
	public JToggleButton getTglbtnVentObrs() {
		return tglbtnVentObrs;
	}

	/**
	 * @param tglbtnVentObrs the tglbtnVentObrs to set
	 */
	public void setTglbtnVentObrs(JToggleButton tglbtnVentObrs) {
		this.tglbtnVentObrs = tglbtnVentObrs;
	}

	/**
	 * @return the tglbtnVenBusq
	 */
	public JToggleButton getTglbtnVenBusq() {
		return tglbtnVenBusq;
	}

	/**
	 * @param tglbtnVenBusq the tglbtnVenBusq to set
	 */
	public void setTglbtnVenBusq(JToggleButton tglbtnVenBusq) {
		this.tglbtnVenBusq = tglbtnVenBusq;
	}

	/**
	 * @return the tglbtnRedPrecioVenta
	 */
	public JToggleButton getTglbtnRedPrecioVenta() {
		return tglbtnRedPrecioVenta;
	}

	/**
	 * @param tglbtnRedPrecioVenta the tglbtnRedPrecioVenta to set
	 */
	public void setTglbtnRedPrecioVenta(JToggleButton tglbtnRedPrecioVenta) {
		this.tglbtnRedPrecioVenta = tglbtnRedPrecioVenta;
	}

	/**
	 * @return the tglbtnDescPorcentaje
	 */
	public JToggleButton getTglbtnDescPorcentaje() {
		return tglbtnDescPorcentaje;
	}

	/**
	 * @param tglbtnDescPorcentaje the tglbtnDescPorcentaje to set
	 */
	public void setTglbtnDescPorcentaje(JToggleButton tglbtnDescPorcentaje) {
		this.tglbtnDescPorcentaje = tglbtnDescPorcentaje;
	}

	/**
	 * @return the tglbtnFactSinInven
	 */
	public JToggleButton getTglbtnFactSinInven() {
		return tglbtnFactSinInven;
	}

	/**
	 * @param tglbtnFactSinInven the tglbtnFactSinInven to set
	 */
	public void setTglbtnFactSinInven(JToggleButton tglbtnFactSinInven) {
		this.tglbtnFactSinInven = tglbtnFactSinInven;
	}

	/**
	 * @return the tglbtnAddClienteCredito
	 */
	public JToggleButton getTglbtnAddClienteCredito() {
		return tglbtnAddClienteCredito;
	}

	/**
	 * @param tglbtnAddClienteCredito the tglbtnAddClienteCredito to set
	 */
	public void setTglbtnAddClienteCredito(JToggleButton tglbtnAddClienteCredito) {
		this.tglbtnAddClienteCredito = tglbtnAddClienteCredito;
	}

	/**
	 * @return the tglbtnCategoriaEnCierre
	 */
	public JToggleButton getTglbtnCategoriaEnCierre() {
		return tglbtnCategoriaEnCierre;
	}

	/**
	 * @param tglbtnCategoriaEnCierre the tglbtnCategoriaEnCierre to set
	 */
	public void setTglbtnCategoriaEnCierre(JToggleButton tglbtnCategoriaEnCierre) {
		this.tglbtnCategoriaEnCierre = tglbtnCategoriaEnCierre;
	}

	/**
	 * @return the tglbtnImprimirSalida
	 */
	public JToggleButton getTglbtnImprimirSalida() {
		return tglbtnImprimirSalida;
	}

	/**
	 * @param tglbtnImprimirSalida the tglbtnImprimirSalida to set
	 */
	public void setTglbtnImprimirSalida(JToggleButton tglbtnImprimirSalida) {
		this.tglbtnImprimirSalida = tglbtnImprimirSalida;
	}

	/**
	 * @return the tglbtnObservarSalida
	 */
	public JToggleButton getTglbtnObservarSalida() {
		return tglbtnObservarSalida;
	}

	/**
	 * @param tglbtnObservarSalida the tglbtnObservarSalida to set
	 */
	public void setTglbtnObservarSalida(JToggleButton tglbtnObservarSalida) {
		this.tglbtnObservarSalida = tglbtnObservarSalida;
	}

	/**
	 * @return the tglbtnImprimirEntrada
	 */
	public JToggleButton getTglbtnImprimirEntrada() {
		return tglbtnImprimirEntrada;
	}

	/**
	 * @param tglbtnImprimirEntrada the tglbtnImprimirEntrada to set
	 */
	public void setTglbtnImprimirEntrada(JToggleButton tglbtnImprimirEntrada) {
		this.tglbtnImprimirEntrada = tglbtnImprimirEntrada;
	}

	/**
	 * @return the tglbtnObservarEntrada
	 */
	public JToggleButton getTglbtnObservarEntrada() {
		return tglbtnObservarEntrada;
	}

	/**
	 * @param tglbtnObservarEntrada the tglbtnObservarEntrada to set
	 */
	public void setTglbtnObservarEntrada(JToggleButton tglbtnObservarEntrada) {
		this.tglbtnObservarEntrada = tglbtnObservarEntrada;
	}

	/**
	 * @return the lblUsuairo
	 */
	public JLabel getLblUsuairo() {
		return lblUsuairo;
	}

	/**
	 * @param lblUsuairo the lblUsuairo to set
	 */
	public void setLblUsuairo(JLabel lblUsuairo) {
		this.lblUsuairo = lblUsuairo;
	}

	/**
	 * @return the txtUsuario
	 */
	public JTextField getTxtUsuario() {
		return txtUsuario;
	}

	/**
	 * @param txtUsuario the txtUsuario to set
	 */
	public void setTxtUsuario(JTextField txtUsuario) {
		this.txtUsuario = txtUsuario;
	}
}
