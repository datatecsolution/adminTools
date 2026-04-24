package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.view.*;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtlFacturarFrame
		implements ActionListener, MouseListener, TableModelListener, KeyListener, InternalFrameListener {
	private ViewFacturarFrame view;
	private Factura myFactura = null;
	private FacturaDao facturaDao = null;
	private ClienteDao clienteDao = null;
	private Articulo myArticulo = null;
	private ArticuloDao myArticuloDao = null;
	private PrecioArticuloDao preciosDao = null;
	private InsumoDao insumoDao = null;
	private CodBarraDao codBarraDao = null;

	private Cliente myCliente = null;
	private int filaPulsada = 0;
	private boolean resultado = false;
	private UsuarioDao myUsuarioDao;

	private int tipoView = 1;
	private int netBuscar = 0;

	private DetalleFacturaOrdenDao detallesOrdenDao = null;
	List<ViewFacturarFrame> ventanas;
	private FacturaOrdenVentaDao facturaOrdenesDao;
	private Caja cajaDefecto;
	private boolean isThereConexion = false;

	private Integer bandera = 0;
	private boolean rotacionManual = false;
	private boolean unirCanItem=true;

	private final Usuario usuario;
	private final ConfigUserFacturacion config;
	private Caja cajaActiva;

	public CtlFacturarFrame(ViewFacturarFrame v, List<ViewFacturarFrame> ven) {

		ventanas = ven;
		view = v;
		view.conectarContralador(this);
		myFactura = new Factura();
		myArticuloDao = new ArticuloDao();
		clienteDao = new ClienteDao();
		facturaDao = new FacturaDao();
		facturaOrdenesDao = new FacturaOrdenVentaDao();
		preciosDao = new PrecioArticuloDao();
		codBarraDao = new CodBarraDao();
		detallesOrdenDao = new DetalleFacturaOrdenDao();
		insumoDao = new InsumoDao();

		myUsuarioDao = new UsuarioDao();

		usuario = ConexionStatic.getUsuarioLogin();
		config = usuario.getConfig();
		cajaActiva = usuario.getCajaActiva();

		this.setEmptyView();

		cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
		this.tipoView = 1;

		this.setCierre();
		cajaDefecto = new Caja(cajaActiva);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando = e.getActionCommand();

		if (AbstractJasperReports.isNumber(comando)) {
			int numeroFactura = Integer.parseInt(comando);
			if (numeroFactura > 0) {
				cargarFacturaPendiente(numeroFactura);
			}
			if (numeroFactura == 0) {
				this.tipoView = 1;
				this.view.getBtnGuardar().setEnabled(true);
				this.view.getBtnActualizar().setEnabled(false);

				setEmptyView();

				view.getBtnsGuardador().deleteAll();

				cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
			}
		}
		switch (comando) {

			case "BUSCARARTICULO2":
				agregarArticuloPorCodigo();
				break;
			case "BUSCARCLIENTE":
				buscarClientePorId();
				break;
			case "ACTUALIZAR":

				this.actualizar();

				break;
			case "BUSCARARTICULO":
				if (config.isActivarBusquedaFacturacion()) {
					this.buscarArticulo();
				}
				break;

			case "CERRAR":
				this.salir();
				break;
			case "BUSCARCLIENTES":
				this.buscarCliente();
				break;
			case "COBRAR":
				this.cobrar();
				break;
			case "GUARDAR":
				this.guardar();
				break;

			case "CIERRECAJA":
				this.cierreCaja();
				break;
			case "COTIZACION":
				guardarCotizacion();
				break;
			case "GET_COTIZACIONES":
				buscarCotizaciones();
				break;

			case "IMPRIMIRPENDIENTE":
				imprimirPendiente();
				break;
			case "ELIMINARPENDIENTE":
				eliminarPendiente();
				break;

		}

	}

	private void guardarCotizacion() {
		setFactura();
		if (validar()) {
			CotizacionDao cotizacioDao = new CotizacionDao();
			boolean resultado = cotizacioDao.registrar(myFactura);

			if (resultado) {

				try {
					AbstractJasperReports.crearReporteCotizacion(ConexionStatic.getPoolConexion().getConnection(),
							myFactura.getIdFactura());
					AbstractJasperReports.showViewer(view);

					setEmptyView();

					if (this.tipoView == 2) {
						this.tipoView = 1;
						this.view.getBtnGuardar().setEnabled(true);
						this.view.getBtnActualizar().setEnabled(false);

						setEmptyView();

						view.getBtnsGuardador().deleteAll();

						cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(view, "Error al guardar la cotizacion", "Error al guardar",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private void agregarArticuloPorCodigo() {
		if (view.getTxtBuscar().getText().trim().length() == 0 && myArticulo != null) {
			netBuscar = 0;
			return;
		}

		String busca = this.view.getTxtBuscar().getText();
		this.myArticulo = this.myArticuloDao.buscarArticuloBarraCod(busca);

		if (myArticulo == null) {
			JOptionPane.showMessageDialog(view, "No se encontro el articulo");
			view.getTxtBuscar().setText("");
			view.getTxtBuscar().requestFocusInWindow();
			myArticulo = null;
			netBuscar = 0;
			return;
		}

		if (config.isPrecioRedondear()) {
			myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
		}

		myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(myArticulo.getId()));

		boolean unirCantidad = false;
		if (config.isUnirCanItem())
			unirCantidad = this.buscarArticuloEnFactura(myArticulo);

		if (unirCantidad) {
			view.getTxtBuscar().setText("");
			netBuscar = 0;
			return;
		}

		if (config.isFacturarSinInventario()) {
			agregarArticuloSinInventario();
		} else {
			agregarArticuloConInventario();
		}
		view.getTxtBuscar().setText("");
		netBuscar = 0;
	}

	private void agregarArticuloSinInventario() {
		if (config.isPrecioRedondear()) {
			myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
		}

		if (myArticulo.getTipoArticulo() == 1) {
			agregarArticuloATabla(myArticulo);
		} else {
			if (cajaActiva.getCodigo() == this.cajaDefecto.getCodigo()) {
				this.view.getModeloTabla().setArticulo(myArticulo);
				calcularTotales();
				this.view.getModeloTabla().agregarDetalle();
				selectRowInset();
			} else {
				JOptionPane.showMessageDialog(view,
						"Solo la caja " + cajaDefecto.getDescripcion() + " puede facturar servicios.",
						"Error en articulo", JOptionPane.ERROR_MESSAGE);
				view.getTxtBuscar().setText("");
			}
		}
	}

	private void agregarArticuloConInventario() {
		double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
				cajaActiva.getDetartamento().getId());

		if (myArticulo.getTipoArticulo() == 1) {
			existencia = myArticuloDao.getExistencia(myArticulo.getId(),
					cajaActiva.getDetartamento().getId());

			double cantidad = 1;
			double buscarEnRequisicionCantidad = view.getModeloTabla().buscarCantidadPorArticulo(myArticulo);
			if (buscarEnRequisicionCantidad > 0) {
				cantidad = cantidad + buscarEnRequisicionCantidad;
			}

			if (existencia > 0.0 && cantidad <= existencia) {
				if (config.isPrecioRedondear()) {
					myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
				}
				this.view.getModeloTabla().setArticulo(myArticulo);
				calcularTotales();
				this.view.getModeloTabla().agregarDetalle();
				selectRowInset();
			} else {
				JOptionPane.showMessageDialog(view,
						myArticulo.getArticulo() + " no tiene existencia en "
								+ cajaActiva.getDetartamento().getDescripcion(),
						"Error en existencia", JOptionPane.ERROR_MESSAGE);
				view.getTxtBuscar().setText("");
			}
		} else {
			if (cajaActiva.getCodigo() != this.cajaDefecto.getCodigo()) {
				JOptionPane.showMessageDialog(view,
						"Solo la caja " + cajaDefecto.getDescripcion() + " puede facturar servicios.",
						"Error en articulo", JOptionPane.ERROR_MESSAGE);
				view.getTxtBuscar().setText("");
				return;
			}

			List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

			if (insumos == null || insumos.size() == 0) {
				this.view.getModeloTabla().setArticulo(myArticulo);
				calcularTotales();
				this.view.getModeloTabla().agregarDetalle();
				selectRowInset();
				return;
			}

			boolean exist = false;
			for (int xx = 0; xx < insumos.size(); xx++) {
				double existenciaInsumo = myArticuloDao.getExistencia(
						insumos.get(xx).getArticulo().getId(),
						cajaActiva.getDetartamento().getId());

				if (existenciaInsumo > 0.0 && existenciaInsumo >= insumos.get(xx).getCantidad().doubleValue()) {
					exist = true;
					if (config.isPrecioRedondear()) {
						myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
					}
				} else {
					exist = false;
					JOptionPane.showMessageDialog(view,
							"El insumo " + insumos.get(xx).getArticulo().getArticulo()
									+ " que pertence al servicio " + myArticulo.getArticulo()
									+ " ,\nno tiene existencia en "
									+ cajaActiva
											.getDetartamento().getDescripcion(),
							"Error en existencia", JOptionPane.ERROR_MESSAGE);
					view.getTxtBuscar().setText("");
					break;
				}
			}

			if (exist) {
				this.view.getModeloTabla().setArticulo(myArticulo);
				calcularTotales();
				this.view.getModeloTabla().agregarDetalle();
				selectRowInset();
			}
		}
	}

	private void buscarClientePorId() {
		myCliente = null;
		myCliente = clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtIdcliente().getText()));

		if (myCliente != null) {
			this.view.getTxtNombrecliente().setText(myCliente.getNombre());
			this.view.getTxtRtn().setText(myCliente.getRtn());
		} else {
			JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			this.view.getTxtIdcliente().setText("1");
			this.view.getTxtNombrecliente().setText("Cliente Normal");
		}
	}

	private void imprimirPendiente() {
		int idFacturaTemporal = view.getBtnsGuardador().getFacturaSeleted().getIdFactura();
		try {
			AbstractJasperReports.createReportOrdenCarta(ConexionStatic.getPoolConexion().getConnection(),
					idFacturaTemporal);
			AbstractJasperReports.showViewer(view);
		} catch (SQLException ee) {
			ee.printStackTrace();
		}
	}

	private void eliminarPendiente() {
		int idFacturaTemporal = view.getBtnsGuardador().getFacturaSeleted().getIdFactura();
		Factura eliminarTem = new Factura();
		eliminarTem.setIdFactura(idFacturaTemporal);

		if (solicitarPasswordAdmin()) {
			this.facturaOrdenesDao.eliminar(eliminarTem);
			this.tipoView = 1;
			this.view.getBtnGuardar().setEnabled(true);
			this.view.getBtnActualizar().setEnabled(false);
			setEmptyView();
			view.getBtnsGuardador().deleteAll();
			cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
		}
	}

	public void aplicarDescuento() {
		double maxDescuento = 35;
		JPanel panelDescuento = new JPanel();
		panelDescuento.setLayout(new BoxLayout(panelDescuento, BoxLayout.Y_AXIS));

		JLabel etiqueta = new JLabel("Escriba el descuento");
		panelDescuento.add(etiqueta);

		JTextField descuento = new JTextField(15);
		descuento.addAncestorListener(new javax.swing.event.AncestorListener() {
			public void ancestorAdded(javax.swing.event.AncestorEvent e) {
				SwingUtilities.invokeLater(() -> descuento.requestFocusInWindow());
			}
			public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
			public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
		});
		panelDescuento.add(descuento);

		JCheckBox rememberChk = new JCheckBox("Agregar descuento a todos los items?");
		panelDescuento.add(rememberChk);

		if (config.isPwdDescuento()) {
			if (!solicitarPasswordAdmin()) {
				return;
			}
		}

		if (config.isDescPorcentaje()) {
			aplicarDescuentoPorcentaje(panelDescuento, etiqueta, descuento, rememberChk, maxDescuento);
		} else {
			aplicarDescuentoFijo(panelDescuento, etiqueta, descuento, rememberChk);
		}
	}

	private void aplicarDescuentoPorcentaje(JPanel panelDescuento, JLabel etiqueta, JTextField descuento,
			JCheckBox rememberChk, double maxDescuento) {
		if (filaPulsada < 0) return;

		etiqueta.setText("Escriba el porcentaje(%) de descuento 1-35%");
		JOptionPane.showMessageDialog(view, panelDescuento, "Descuento", JOptionPane.INFORMATION_MESSAGE);
		String seleccionadoDescuento = descuento.getText();
		boolean aplicarTodo = rememberChk.isSelected();

		if (!AbstractJasperReports.isNumberReal(seleccionadoDescuento)) {
			JOptionPane.showMessageDialog(view, "El descuento debe ser un numero", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		double bdDescuento = Double.parseDouble(seleccionadoDescuento);

		if (bdDescuento > maxDescuento) {
			JOptionPane.showMessageDialog(view, "No puede otorgar un descuento mayo del 35%", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (!aplicarTodo) {
			BigDecimal cantidad = this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad();
			BigDecimal precioVenta = new BigDecimal(
					view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getPrecioVenta());
			BigDecimal totalItem = cantidad.multiply(precioVenta);
			double desc = bdDescuento / 100;
			BigDecimal des = totalItem.multiply(new BigDecimal(desc));
			this.view.getModeloTabla().getDetalle(filaPulsada)
					.setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));
		} else {
			for (int xx = 0; xx < view.getModeloTabla().getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getModeloTabla().getDetalle(xx);
				if (detalle.getArticulo().getId() != -1)
					if (detalle.getCantidad().doubleValue() != 0 && detalle.getArticulo().getPrecioVenta() != 0) {
						BigDecimal cantidad = detalle.getCantidad();
						BigDecimal precioVenta = new BigDecimal(detalle.getArticulo().getPrecioVenta());
						BigDecimal totalItem = cantidad.multiply(precioVenta);
						double desc = bdDescuento / 100;
						BigDecimal des = totalItem.multiply(new BigDecimal(desc));
						detalle.setDescuentoItem(des.setScale(0, BigDecimal.ROUND_HALF_EVEN));
					}
			}
		}
		this.calcularTotales();
	}

	private void aplicarDescuentoFijo(JPanel panelDescuento, JLabel etiqueta, JTextField descuento,
			JCheckBox rememberChk) {
		if (filaPulsada < 0) return;

		etiqueta.setText("Escriba el descuento");
		JOptionPane.showMessageDialog(view, panelDescuento, "Descuento", JOptionPane.INFORMATION_MESSAGE);
		String entrada = descuento.getText();
		boolean aplicarTodo = rememberChk.isSelected();

		if (!AbstractJasperReports.isNumberReal(entrada)) return;

		if (!aplicarTodo) {
			this.view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(entrada));
		} else {
			for (int xx = 0; xx < view.getModeloTabla().getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getModeloTabla().getDetalle(xx);
				if (detalle.getArticulo().getId() != -1)
					if (detalle.getCantidad().doubleValue() != 0 && detalle.getArticulo().getPrecioVenta() != 0) {
						detalle.setDescuentoItem(new BigDecimal(entrada));
					}
			}
		}
		this.calcularTotales();
	}

	public void modificarPrecio() {
		if (config.isPwdPrecio()) {
			if (!solicitarPasswordAdmin()) {
				return;
			}
		}
		if (filaPulsada >= 0) {
			String entrada = JOptionPane.showInputDialog("Escriba el precio");
			if (AbstractJasperReports.isNumberReal(entrada)) {
				this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo()
						.setPrecioVenta(new Double(entrada));
				this.calcularTotales();
			}
		}
	}

	public void modificarCantidad() {
		if (filaPulsada < 0) return;

		String entrada = JOptionPane.showInputDialog(view, "Escriba el cantida");
		if (entrada == null || entrada.trim().isEmpty()) return;

		if (config.isFacturarSinInventario()) {
			BigDecimal cantidadSaldoItem = new BigDecimal(entrada);
			this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
			this.calcularTotales();
		} else {
			modificarCantidadConInventario(entrada);
		}
	}

	private void modificarCantidadConInventario(String entrada) {
		if (!AbstractJasperReports.isNumberReal(entrada)) return;

		if (myArticulo.getTipoArticulo() == 1) {
			double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
					cajaActiva.getDetartamento().getId());

			BigDecimal cantidadSaldoItem = new BigDecimal(entrada);
			double cantidad = cantidadSaldoItem.doubleValue();

			this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);

			if (existencia > 0.0 && cantidad <= existencia) {
				this.calcularTotales();
			} else {
				JOptionPane.showMessageDialog(view,
						"No se puede requerir la cantidad de "
								+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
								+ " del articulo en la bodega "
								+ cajaActiva.getDetartamento().getDescripcion());
				view.getModeloTabla().eliminarDetalle(filaPulsada);
			}
		} else {
			BigDecimal cantidadSaldoItem = new BigDecimal(entrada);
			List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

			if (insumos == null || insumos.size() == 0) {
				this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
				this.calcularTotales();
				return;
			}

			boolean exist = false;
			for (int xx = 0; xx < insumos.size(); xx++) {
				double existencia = myArticuloDao.getExistencia(insumos.get(xx).getArticulo().getId(),
						cajaActiva.getDetartamento().getId());

				BigDecimal cantRequerida = cantidadSaldoItem.multiply(insumos.get(xx).getCantidad());

				if (existencia > 0.0 && existencia >= cantRequerida.doubleValue()) {
					exist = true;
				} else {
					exist = false;
					JOptionPane.showMessageDialog(view,
							"El insumo " + insumos.get(xx).getArticulo().getArticulo()
									+ " que pertence al servicio " + myArticulo.getArticulo()
									+ " ,\nno tiene existencia en "
									+ cajaActiva
											.getDetartamento().getDescripcion(),
							"Error en existencia", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}

			if (exist) {
				if (config.isPrecioRedondear()) {
					myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
				}
				this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
				this.calcularTotales();
			} else {
				JOptionPane.showMessageDialog(view,
						"No se puede requerir la cantidad de "
								+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
								+ " del articulo en la bodega "
								+ cajaActiva
										.getDetartamento().getDescripcion());
				view.getModeloTabla().eliminarDetalle(filaPulsada);
				this.calcularTotales();
			}
		}
	}

	public void abrirCobros() {
		ViewCobro viewCobro = new ViewCobro(null);
		CtlCobro ctlCobro = new CtlCobro(viewCobro);
		viewCobro.dispose();
	}

	public void abrirPagoProveedor() {
		ViewPagoProveedor vPagoProveedores = new ViewPagoProveedor(null);
		vPagoProveedores.getCbFormaPago().setEnabled(false);
		CtlPagoProveedor cPagoProveedores = new CtlPagoProveedor(vPagoProveedores);
		vPagoProveedores.dispose();
	}

	public void abrirSalidaCaja() {
		ViewSalidaCaja viewSalida = new ViewSalidaCaja(null);
		CtlSalidaCaja ctlSalida = new CtlSalidaCaja(viewSalida);
		viewSalida.dispose();
	}

	private void seleccionarPrecioEspecifico() {
		if (!solicitarPasswordAdmin()) {
			return;
		}

		ViewSelectPrecio viewSelectPrecio = new ViewSelectPrecio(null);
		CtlSelectPrecio ctlSelectPrecio = new CtlSelectPrecio(viewSelectPrecio);

		boolean resultado = ctlSelectPrecio.agregar();
		if (!resultado) return;

		if (ctlSelectPrecio.isAplicarTodo()) {
			for (int xx = 0; xx < view.getModeloTabla().getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getModeloTabla().getDetalle(xx);
				detalle.getArticulo().setPreciosVenta(
						this.preciosDao.getPreciosArticuloSinCosto(detalle.getArticulo().getId()));
				if (detalle.getArticulo().getId() != -1) {
					if (ctlSelectPrecio.getPrecioSelect().getCodigoPrecio() == 4) {
						PrecioArticulo unPrecio = this.preciosDao
								.getPrecioArticulo(detalle.getArticulo().getId(), 4);
						if (unPrecio != null) {
							detalle.getArticulo().getPreciosVenta().add(unPrecio);
							detalle.getArticulo().setPrecio(unPrecio);
						} else {
							detalle.getArticulo().lastPrecio();
							detalle.getArticulo().netPrecio();
						}
					} else {
						detalle.getArticulo().setPrecio(ctlSelectPrecio.getPrecioSelect());
					}
				}
			}
		} else {
			if (filaPulsada >= 0) {
				view.getModeloTabla().getDetalle(filaPulsada).getArticulo()
						.setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(
								view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getId()));

				if (ctlSelectPrecio.getPrecioSelect().getCodigoPrecio() == 4) {
					PrecioArticulo unPrecio = this.preciosDao.getPrecioArticulo(
							view.getModeloTabla().getDetalle(filaPulsada).getArticulo().getId(), 4);
					if (unPrecio != null) {
						this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo()
								.getPreciosVenta().add(unPrecio);
						this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo()
								.setPrecio(unPrecio);
						this.selectRowInset(filaPulsada);
					} else {
						this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().lastPrecio();
						this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().netPrecio();
						this.selectRowInset(filaPulsada);
					}
				} else {
					this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo()
							.setPrecio(ctlSelectPrecio.getPrecioSelect());
				}
			}
		}
		this.calcularTotales();
	}

	private void incrementarCantidad() {
		if (filaPulsada < 0) return;

		if (config.isFacturarSinInventario()) {
			this.view.getModeloTabla().masCantidad(filaPulsada);
			view.getModeloTabla().getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(0));
			this.calcularTotales();
			return;
		}

		myArticulo = view.getModeloTabla().getDetalle(filaPulsada).getArticulo();

		if (myArticulo.getTipoArticulo() == 1) {
			incrementarCantidadBien();
		} else {
			incrementarCantidadServicio();
		}
	}

	private void incrementarCantidadBien() {
		double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
				cajaActiva.getDetartamento().getId());

		BigDecimal cantidadSaldoKardex = new BigDecimal(existencia);
		BigDecimal cantidadSaldoItem = new BigDecimal(
				this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad().doubleValue());
		cantidadSaldoItem = cantidadSaldoItem.add(new BigDecimal(1));

		BigDecimal diferencia = cantidadSaldoKardex.subtract(cantidadSaldoItem);

		if (diferencia.doubleValue() >= 0.00 || myArticulo.getTipoArticulo() == 2) {
			this.view.getModeloTabla().masCantidad(filaPulsada);
			this.calcularTotales();
		} else {
			JOptionPane.showMessageDialog(view, "No se puede requerir la cantidad de "
					+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
					+ " del articulo en la bodega " + cajaActiva.getDetartamento().getDescripcion(),
					"Error en existencia", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void incrementarCantidadServicio() {
		BigDecimal cantidadSaldoItem = new BigDecimal(
				this.view.getModeloTabla().getDetalle(filaPulsada).getCantidad().doubleValue());
		BigDecimal newCantSaldoItem = cantidadSaldoItem.add(new BigDecimal(1));

		List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

		if (insumos == null || insumos.size() == 0) {
			this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
			this.calcularTotales();
			return;
		}

		boolean exist = false;
		for (int xx = 0; xx < insumos.size(); xx++) {
			double existencia = myArticuloDao.getExistencia(insumos.get(xx).getArticulo().getId(),
					cajaActiva.getDetartamento().getId());

			BigDecimal cantRequerida = newCantSaldoItem.multiply(insumos.get(xx).getCantidad());

			if (existencia > 0.0 && existencia >= cantRequerida.doubleValue()) {
				exist = true;
			} else {
				exist = false;
				JOptionPane.showMessageDialog(view,
						"El insumo " + insumos.get(xx).getArticulo().getArticulo()
								+ " que pertence al servicio " + myArticulo.getArticulo()
								+ " ,\nno tiene existencia en "
								+ cajaActiva.getDetartamento().getDescripcion(),
						"Error en existencia", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}

		if (exist) {
			if (config.isPrecioRedondear()) {
				myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
			}
			this.view.getModeloTabla().getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
			this.calcularTotales();
		} else {
			JOptionPane.showMessageDialog(view,
					"No se puede requerir la cantidad de "
							+ newCantSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
							+ " del articulo en la bodega "
							+ cajaActiva.getDetartamento().getDescripcion());
			view.getModeloTabla().eliminarDetalle(filaPulsada);
			this.calcularTotales();
		}
	}

	private void cargarFacturaPendiente(int numeroFactura) {

		Factura fact = view.getBtnsGuardador().buscarFactura(numeroFactura);
		this.myFactura = fact;

		myFactura.setDetalles(detallesOrdenDao.detallesFacturaPendiente(numeroFactura));

		cargarFacturaView();
		this.calcularTotales();
		this.view.getBtnGuardar().setEnabled(false);
		this.view.getBtnActualizar().setEnabled(true);
		this.view.getModeloTabla().agregarDetalle();
		this.view.setEstadoFactura(true, numeroFactura);
		tipoView = 2;
	}

	private boolean setFactura() {
		if (myCliente == null) {
			myCliente = new Cliente();
			myCliente.setId(Integer.parseInt(this.view.getTxtIdcliente().getText()));
			myCliente.setNombre(this.view.getTxtNombrecliente().getText());
			myCliente.setRtn(view.getTxtRtn().getText());

		}

		if (this.view.getRdbtnContado().isSelected()) {
			myFactura.setTipoFactura(1);
			myFactura.setEstadoPago(1);
		}

		if (this.view.getRdbtnCredito().isSelected()) {
			myFactura.setTipoFactura(2);
			myFactura.setEstadoPago(0);
		}

		myFactura.setCliente(myCliente);
		myFactura.setDetalles(this.view.getModeloTabla().getDetalles());
		myFactura.setFecha(facturaDao.getFechaSistema());
		myFactura.setCodigoCaja(cajaActiva.getCodigo());

		boolean verificarAccion = false;

		if (config.isVentanaVendedor()) {
			if (myFactura.getVendedor().getCodigo() < 1) {
				ViewCargarVenderor viewVendedor = new ViewCargarVenderor(SwingUtilities.getWindowAncestor(view));
				CtlCargarVendedor ctlVendedor = new CtlCargarVendedor(viewVendedor);

				verificarAccion = ctlVendedor.cargarVendedor();

				if (!verificarAccion) {
					return verificarAccion;
				}
				myFactura.setVendedor(ctlVendedor.getVendedor());
			} else {
				verificarAccion = true;
			}
		} else {
			if (myFactura.getVendedor().getCodigo() < 1) {
				Empleado uno = new Empleado();
				uno.setCodigo(1);
				myFactura.setVendedor(uno);
			}
			verificarAccion = true;

		}

		if (config.isVentanaObservaciones()) {
			String observaciones = "";
			JTextArea ta = new JTextArea(20, 20);
			switch (JOptionPane.showConfirmDialog(view, new JScrollPane(ta), "Observaciones de la factura",
					JOptionPane.ERROR_MESSAGE)) {
				case JOptionPane.OK_OPTION:
					observaciones = ta.getText();
					break;
				default:
					observaciones = "NA";
					break;
			}
			myFactura.setObservacion(observaciones);
		}

		if (myFactura.getTipoFactura() == 1) {
			ViewCambioPago viewPago = new ViewCambioPago(SwingUtilities.getWindowAncestor(view));
			CtlCambioPago ctlPago = new CtlCambioPago(viewPago, myFactura.getTotal());
			boolean resulPago = ctlPago.pagar();
			if (resulPago) {
				if (ctlPago.getFormaPago() == 1) {
					myFactura.setPago(ctlPago.getTotalPago());
					myFactura.setCambio(ctlPago.getCambio());
					myFactura.setCobroEfectivo(ctlPago.getCobroEfectivo());
					myFactura.setCobroTarjeta(ctlPago.getCobroTarjeta());
					myFactura.setTipoPago(1);
				}
				if (ctlPago.getFormaPago() == 2) {
					myFactura.setPago(myFactura.getTotal());
					myFactura.setCambio(new BigDecimal(00));
					myFactura.setTipoPago(2);
					myFactura.setObservacion(ctlPago.getRefencia());
				}
				verificarAccion = true;
			} else {
				verificarAccion = false;
			}
		}

		return verificarAccion;

	}

	private void setFacturaBasica() {
		if (myCliente == null) {
			myCliente = new Cliente();
			myCliente.setId(Integer.parseInt(this.view.getTxtIdcliente().getText()));
			myCliente.setNombre(this.view.getTxtNombrecliente().getText());
			myCliente.setRtn(view.getTxtRtn().getText());
		}

		if (this.view.getRdbtnContado().isSelected()) {
			myFactura.setTipoFactura(1);
			myFactura.setEstadoPago(1);
		}

		if (this.view.getRdbtnCredito().isSelected()) {
			myFactura.setTipoFactura(2);
			myFactura.setEstadoPago(0);
		}

		myFactura.setCliente(myCliente);
		myFactura.setDetalles(this.view.getModeloTabla().getDetalles());
		myFactura.setFecha(facturaDao.getFechaSistema());
		myFactura.setCodigoCaja(cajaActiva.getCodigo());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	

	}

	@Override
	public void mousePressed(MouseEvent e) {
	
		checkForTriggerEvent(e); // comprueba el desencadenador

	}

	@Override
	public void mouseReleased(MouseEvent e) {
	
		checkForTriggerEvent(e); // comprueba el desencadenador
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	

	}

	@Override
	public void mouseExited(MouseEvent e) {
	

	}

	public void check(MouseEvent e) {
		if (e.isPopupTrigger()) {
		}
	}

	private void checkForTriggerEvent(MouseEvent evento) {
		if (evento.isPopupTrigger()) {

			JToggleButton even = (JToggleButton) evento.getComponent();
			even.setSelected(true);

			Factura facturaSeleccionada = view.getBtnsGuardador().getFacturaSeleted();
			if (facturaSeleccionada == null || facturaSeleccionada.getIdFactura() <= 0) return;

			int idFacturaTemporal = facturaSeleccionada.getIdFactura();
			this.cargarFacturaPendiente(idFacturaTemporal);
			this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY());

		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
	

		int colum = e.getColumn();
		int row = e.getFirstRow();
		switch (e.getType()) {

			case TableModelEvent.UPDATE:

				int identificador = 0;

				if (colum == 0) {

					identificador = (int) this.view.getModeloTabla().getValueAt(row, 0);
					myArticulo = this.view.getModeloTabla().getDetalle(row).getArticulo();
					myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));

					if (myArticulo.getId() == -2) {
						String cod = this.view.getModeloTabla().getDetalle(row).getArticulo().getCodBarra().get(0)
								.getCodigoBarra();
						this.myArticulo = this.myArticuloDao.buscarArticuloBarraCod(cod);

					} else {
						this.myArticulo = this.myArticuloDao.buscarArticulo(identificador);
						myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
					}

					if (myArticulo != null) {
						this.view.getModeloTabla().setArticulo(myArticulo, row);
						calcularTotales();

						boolean toggle = false;
						boolean extend = false;
						this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
						this.view.getTableDetalle().changeSelection(row, colum, toggle, extend);
						this.view.getTableDetalle().addColumnSelectionInterval(3, 3);

					} else {
						JOptionPane.showMessageDialog(view, "No se encuentra el articulo");
						this.view.getModeloTabla().getDetalle(row).getArticulo().setId(-1);
						this.view.getModeloTabla().agregarDetalle();
						calcularTotales();
					}

				}
				if (colum == 1) {
					calcularTotales();
					view.getTxtBuscar().requestFocusInWindow();
				}

				if (colum == 2) {

					identificador = (int) this.view.getModeloTabla().getValueAt(row, 0);
					myArticulo = this.view.getModeloTabla().getDetalle(row).getArticulo();

					double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
							cajaActiva.getDetartamento().getId());

					double cantidad = 1;

					double buscarEnRequisicionCantidad = view.getModeloTabla().buscarCantidadPorArticulo(myArticulo);

					if (buscarEnRequisicionCantidad > 0) {
						cantidad = cantidad + buscarEnRequisicionCantidad;
					}

					if (existencia > 0.0 && cantidad <= existencia) {

						calcularTotales();
						view.getTxtBuscar().requestFocusInWindow();
					} else {
						JOptionPane.showMessageDialog(view,
								myArticulo.getArticulo() + " no tiene existencia en " + usuario
										.getCajaActiva().getDetartamento().getDescripcion(),
								"Error en existencia", JOptionPane.ERROR_MESSAGE);
						view.getModeloTabla().eliminarDetalle(row);
						calcularTotales();
					}
				}

				if (colum == 5) {
					calcularTotales();
					view.getTxtBuscar().requestFocusInWindow();
				}

				break;

		}

	}

	public boolean esValido(Character caracter) {
		char c = caracter.charValue();
		return Character.isLetter(c) // si es letra
				|| c == ' ' // o un espacio
				|| c == 8 // o backspace
				|| (Character.isDigit(c));
	}

	private void agregarArticuloATabla(Articulo articulo) {
		this.view.getModeloTabla().setArticulo(articulo);
		calcularTotales();
		this.view.getModeloTabla().agregarDetalle();
		selectRowInset();
	}

	public void calcularTotales() {

		this.myFactura.resetTotales();

		for (int x = 0; x < view.getModeloTabla().getDetalles().size(); x++) {

			DetalleFactura detalle = view.getModeloTabla().getDetalle(x);

			if (detalle.getArticulo().getId() != -1)
				if (detalle.getCantidad().doubleValue() != 0 && detalle.getArticulo().getPrecioVenta() != 0) {

					calcularTotalesDetalle(detalle);

				}

		}

		view.actualizarTotales(myFactura);
		view.getModeloTabla().fireTableDataChanged();
		this.selectRowInset();
		view.getTxtBuscar().requestFocusInWindow();
	}

	private void calcularTotalesDetalle(DetalleFactura detalle) {
		BigDecimal cantidad = detalle.getCantidad();
		BigDecimal precioVenta = new BigDecimal(detalle.getArticulo().getPrecioVenta());

		BigDecimal totalItem = cantidad.multiply(precioVenta);

		BigDecimal des = detalle.getDescuentoItem();
		totalItem = totalItem.subtract(des.setScale(2, BigDecimal.ROUND_HALF_EVEN));

		BigDecimal porcentaImpuesto = new BigDecimal(
				detalle.getArticulo().getImpuestoObj().getPorcentaje());
		BigDecimal porImpuesto = porcentaImpuesto.divide(new BigDecimal(100));
		porImpuesto = porImpuesto.add(new BigDecimal(1));

		BigDecimal totalsiniva = totalItem.divide(porImpuesto, 2, BigDecimal.ROUND_HALF_EVEN);

		BigDecimal impuestoItem = totalItem.subtract(totalsiniva);

		myFactura.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

		if (porcentaImpuesto.intValue() == 0) {
			myFactura.setSubTotalExcento(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		} else if (porcentaImpuesto.intValue() == 15) {
			myFactura.setTotalImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			myFactura.setSubTotal15(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		} else if (porcentaImpuesto.intValue() == 18) {
			myFactura.setTotalImpuesto18(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			myFactura.setSubTotal18(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		if (detalle.getArticulo().getTipoArticulo() == 3) {
			BigDecimal totalOtrosImp = totalsiniva.multiply(new BigDecimal(0.04));
			myFactura.setTotalOtrosImpuesto(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			myFactura.setTotal(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		myFactura.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		myFactura.setTotalDescuento(detalle.getDescuentoItem().setScale(2, BigDecimal.ROUND_HALF_EVEN));

		detalle.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		detalle.setImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		detalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
	}

	@Override
	public void keyTyped(KeyEvent e) {
	

	}

	@Override
	public void keyPressed(KeyEvent e) {
	

		filaPulsada = this.view.getTableDetalle().getSelectedRow();

		switch (e.getKeyCode()) {

			case KeyEvent.VK_F1:
				if (config.isActivarBusquedaFacturacion()) {
					buscarArticulo();
				}
				break;

			case KeyEvent.VK_F2:
				cobrar();
				break;

			case KeyEvent.VK_F3:
				buscarOrden();
				break;

			case KeyEvent.VK_F4:

				ViewEntradaCaja vEntradaCaja = new ViewEntradaCaja(null);
				CtlEntradaCaja cEntradaCaja = new CtlEntradaCaja(vEntradaCaja);

				vEntradaCaja.dispose();
				vEntradaCaja = null;
				cEntradaCaja = null;

				break;

			case KeyEvent.VK_F5:
				view.getBtnsGuardador().deleteAll();
				cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
				break;

			case KeyEvent.VK_F6:
				cierreCaja();
				break;

			case KeyEvent.VK_F7:
				aplicarDescuento();
				break;

			case KeyEvent.VK_F8:
				modificarPrecio();
				break;
			case KeyEvent.VK_F9:
				modificarCantidad();
				break;

			case KeyEvent.VK_F10:
				abrirCobros();
				break;

			case KeyEvent.VK_F11:
				abrirPagoProveedor();
				break;

			case KeyEvent.VK_F12:
				abrirSalidaCaja();
				break;

			case KeyEvent.VK_ESCAPE:
				salir();
				break;

			case KeyEvent.VK_DELETE:
				if (filaPulsada >= 0) {

					if (config.isDeleteItemFact()) {
						if (!solicitarPasswordAdmin()) {
							break;
						}
					}
					this.view.getModeloTabla().eliminarDetalle(filaPulsada);
					this.calcularTotales();

				}
				break;

			case KeyEvent.VK_DOWN:
				this.netBuscar++;
				break;
			case KeyEvent.VK_UP:
				if (netBuscar >= 1) {
					this.netBuscar--;
				}
				break;
			case KeyEvent.VK_LEFT:

				if (config.isPwdEntrePrecio()) {
					if (!solicitarPasswordAdmin()) {
						break;
					}
				}
				if (filaPulsada >= 0) {
					this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().netPrecio();
					this.calcularTotales();
					selectRowInset(filaPulsada);
				}

				break;
			case KeyEvent.VK_RIGHT:

				if (config.isPwdEntrePrecio()) {
					if (!solicitarPasswordAdmin()) {
						break;
					}
				}
				if (filaPulsada >= 0) {
					this.view.getModeloTabla().getDetalle(filaPulsada).getArticulo().lastPrecio();
					this.calcularTotales();
					selectRowInset(filaPulsada);
				}

				break;
		}

	}

	public void cargarFacturasPendientes(List<Factura> facturas) {

		if (facturas != null) {
			for (int c = 0; c < facturas.size(); c++) {
				view.addBotonPendiente(facturas.get(c), this);
			}
			view.getPanelGuardados().revalidate();
		} else {
			view.eliminarBotones();
		}

	}

	private void buscarCotizaciones() {
	
		ViewListaCotizacion vistaFacturars = new ViewListaCotizacion(SwingUtilities.getWindowAncestor(view));
		CtlCotizacionLista ctlFacturas = new CtlCotizacionLista(vistaFacturars);

		vistaFacturars.pack();

		boolean resul = ctlFacturas.buscarCotizaciones(null);

		if (resul) {

			this.myFactura = ctlFacturas.getMyFactura();
			cargarFacturaView();

			this.view.getModeloTabla().agregarDetalle();

		}

		vistaFacturars.dispose();
		ctlFacturas = null;
		vistaFacturars.dispose();
		ctlFacturas = null;
	}

	private void cierreCaja() {
	

		CierreCajaDao cierreDao = new CierreCajaDao();
		CierreCaja oldCierre = cierreDao.getCierreUltimoUser();

		if (facturaDao.verificarCierre(usuario.getCajas()) && oldCierre.getEstado() == true) {

			ViewCuentaEfectivo viewContar = new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar = new CtlContarEfectivo(viewContar);

			if (ctlContar.getEstado())
				if (cierreDao.actualizarCierre(ctlContar.getTotal()))
				{
					if (config.isImprReportCategCierre()) {
						CierreCaja elCierre = cierreDao.buscarPorId(cierreDao.idUltimoRequistro);
						List<VentasCategoria> ventas = new ArrayList<VentasCategoria>();
						CategoriaDao categoriaDao = new CategoriaDao();
						List<Categoria> categorias = categoriaDao.todos();

						for (int yy = 0; yy < categorias.size(); yy++) {
							VentasCategoria una = new VentasCategoria();
							una.setCodigoCategoria(categorias.get(yy).getId());
							una.setCategoria(categorias.get(yy).getDescripcion());
							ventas.add(una);
						}

						CierreFacturacionDao cierreFacturacioDao = new CierreFacturacionDao();
						elCierre.setCierreFacturas(cierreFacturacioDao.buscarIdCierre(elCierre.getId()));

						if (elCierre.getCierreFacturas() != null) {
							for (int xx = 0; xx < elCierre.getCierreFacturas().size(); xx++) {

								this.facturaDao.getVentasCategorias(
										elCierre.getCierreFacturas().get(xx).getNoFacturaInicio(),
										elCierre.getCierreFacturas().get(xx).getNoFacturaFinal(),
										elCierre.getUsuario(),
										elCierre.getCierreFacturas().get(xx).getCaja(), ventas);

							}

							try {

								AbstractJasperReports.createReportVentasCategoria(
										ConexionStatic.getPoolConexion().getConnection(), elCierre, ventas);

								AbstractJasperReports.imprimierFactura();
							} catch (SQLException eee) {
								eee.printStackTrace();
							}
						}
					}

					try {

						AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 4,
								cierreDao.idUltimoRequistro);

						AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);

						viewContar.dispose();
						viewContar = null;
						ctlContar = null;
						salir();

					} catch (SQLException ee) {
						ee.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(view, "No se guardo el cierre de corte. Vuelva a hacer el corte.");
				}
		} // fin de la verificacion de las facturas
		else {
			JOptionPane.showMessageDialog(view,
					"No hay facturas para crear un cierre de caja o no tiene un cierre activo.");
			salir();
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
	

		filaPulsada = this.view.getTableDetalle().getSelectedRow();

		if (e.getComponent() == this.view.getTxtNombrecliente()) {
			view.getTxtIdcliente().setText("-1");
			this.myCliente = null;

		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A) {
			if (view.getBtnActualizar().isEnabled())
				actualizar();
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_G) {
			if (view.getBtnGuardar().isEnabled())
				guardar();

		}

		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_UP) {
			seleccionarPrecioEspecifico();
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N) {
			setEmptyView();
		}

		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {

			if (config.isFacturarSinInventario()) {
				Caja caja = usuario.nextCaja();
				this.cajaActiva = caja;
				this.rotacionManual = true;

				ViewModuloFacturar frame = (ViewModuloFacturar) view.getTopLevelAncestor();
				frame.btnCaja.setText(caja.getDescripcion());

			} else {
				if (view.getModeloTabla().getRowCount() <= 1) {
					Caja caja = usuario.nextCaja();
					this.cajaActiva = caja;
					this.rotacionManual = true;

					ViewModuloFacturar frame = (ViewModuloFacturar) view.getTopLevelAncestor();
					frame.btnCaja.setText(caja.getDescripcion());
				} else {
					JOptionPane.showMessageDialog(view,
							"No puede cambiar de caja. Debe eliminar los articulos agregado", "ERORR",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
			CajaDao cajasDao = new CajaDao();
			usuario.setCajas(cajasDao.getCajasUsuario(usuario));
			ViewModuloFacturar frame = (ViewModuloFacturar) view.getTopLevelAncestor();
			frame.btnCaja.setText(cajaActiva.getDescripcion());
		}
		filaPulsada = this.view.getTableDetalle().getSelectedRow();
		char caracter = e.getKeyChar();

		if (e.getComponent() == this.view.getTxtBuscar()) {
			Character caracter1 = new Character(e.getKeyChar());
			if (!esValido(caracter1)) {
				String texto = "";
				for (int i = 0; i < view.getTxtBuscar().getText().length(); i++)
					if (esValido(new Character(view.getTxtBuscar().getText().charAt(i))))
						texto += view.getTxtBuscar().getText().charAt(i);
				view.getTxtBuscar().setText(texto);
			}
		}
		if (caracter == '+') {
			incrementarCantidad();
		}
		if (caracter == '-') {
			if (filaPulsada >= 0) {
				this.view.getModeloTabla().restarCantidad(filaPulsada);
				this.calcularTotales();
			}
		}

	}

	public void actualizarVentanas() {
	

		for (int x = 0; x < ventanas.size(); x++) {
			if (ConexionStatic.getNivelFact() == true) {
				ventanas.get(x).getTxtBuscar().setBackground(new Color(250, 0, 0));
			} else {
				ventanas.get(x).getTxtBuscar().setBackground(new Color(60, 179, 113));
			}
		}

	}

	private void salir() {
		this.view.setVisible(false);

	}

	private JPasswordField crearPasswordConFoco() {
		JPasswordField pf = new JPasswordField();
		pf.addAncestorListener(new javax.swing.event.AncestorListener() {
			public void ancestorAdded(javax.swing.event.AncestorEvent e) {
				SwingUtilities.invokeLater(() -> pf.requestFocusInWindow());
			}
			public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
			public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
		});
		return pf;
	}

	private boolean solicitarPasswordAdmin() {
		JPasswordField pf = crearPasswordConFoco();
		int action = JOptionPane.showConfirmDialog(view, pf, "Escriba el password de admin",
				JOptionPane.OK_CANCEL_OPTION);
		if (action != JOptionPane.OK_OPTION) {
			return false;
		}
		String pwd = new String(pf.getPassword());
		if (myUsuarioDao.comprobarAdmin(pwd)) {
			return true;
		}
		JOptionPane.showMessageDialog(view, "Password incorrecto", "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private void guardar() {

		if (view.getModeloTabla().getRowCount() > 1) {
			setFacturaBasica();

			boolean resulVendedor = false;

			if (config.isVentanaVendedor()) {
				if (myFactura.getVendedor().getCodigo() < 1) {
					ViewCargarVenderor viewVendedor = new ViewCargarVenderor(SwingUtilities.getWindowAncestor(view));
					CtlCargarVendedor ctlVendedor = new CtlCargarVendedor(viewVendedor);

					resulVendedor = ctlVendedor.cargarVendedor();
					myFactura.setVendedor(ctlVendedor.getVendedor());
				} else {
					resulVendedor = true;
				}
			} else {
				if (myFactura.getVendedor().getCodigo() < 1) {
					Empleado uno = new Empleado();
					uno.setCodigo(1);
					myFactura.setVendedor(uno);
				}
				resulVendedor = true;
			}

			if (resulVendedor) {
				boolean resultado = facturaOrdenesDao.registrar(myFactura);

				if (resultado) {
					myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
					resultado = true;

					this.tipoView = 1;

					setEmptyView();

					view.getBtnsGuardador().deleteAll();

					cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
				} else {
					JOptionPane.showMessageDialog(view, "Error al guardar la factura temporal", "Error al guardar",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		} else {
			JOptionPane.showMessageDialog(view, "Para guardar debe agregar articulos.", "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void actualizar() {
		setFacturaBasica();
		facturaOrdenesDao.actualizar(myFactura);
		this.tipoView = 1;
		this.view.getBtnGuardar().setEnabled(true);
		this.view.getBtnActualizar().setEnabled(false);

		setEmptyView();

		view.getBtnsGuardador().deleteAll();

		cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());

	}

	private boolean validar() {
		boolean resultado = false;
		if (!(view.getModeloTabla().getRowCount() > 1)) {
			JOptionPane.showMessageDialog(view, " Debe agregar articulos primero.", "Error Validacion",
					JOptionPane.ERROR_MESSAGE);
			resultado = false;
		} else if (this.myCliente == null) {

			JOptionPane.showMessageDialog(view, "Debe agregar el cliente primero", "Error Validacion",
					JOptionPane.ERROR_MESSAGE);
			resultado = false;

		} else {
			resultado = true;
		}
		return resultado;
	}

	private void cobrar() {

		recargarCajasPreservandoActiva();
		isThereConexion = ConexionStatic.isDbConnected();

		if (isThereConexion) {

			if (view.getModeloTabla().getRowCount() > 1) {

				if (view.getRdbtnContado().isSelected()) {

					if (!setFactura())
						return;

					if (setCierre()) {
						this.guardarFactura();
					} else {
						JOptionPane.showMessageDialog(view,
								"No se puede cobrar la factura. Debe abrir la caja primero!!!", "Error caja",
								JOptionPane.ERROR_MESSAGE);
					}

				} else
				if (view.getRdbtnCredito().isSelected()) {// si la factura es al contado se procede a guardar e imprimir

						if (myCliente != null && myCliente.getTipoCliente() == 2) {

						myFactura.setTipoPago(3);

						myFactura.setCambio(new BigDecimal(0));
						myFactura.setPago(new BigDecimal(0));

						myFactura.setTipoFactura(2);

						if (!setFactura())
							return;

						boolean resl = setCierre();

						if (resl) {

							this.guardarFactura();
						} else {
							JOptionPane.showMessageDialog(view,
									"No se puede cobrar la factura. Debe abrir la caja primero!!!", "Error caja",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(view, "El Cliente no tiene cuenta de credito o no ha sido creado",
								"Error facturar", JOptionPane.ERROR_MESSAGE);
					}
				}

			} else {
				JOptionPane.showMessageDialog(view, "Para guardar debe agregar articulos.", "ERORR",
						JOptionPane.ERROR_MESSAGE);
			}

			view.getBtnsGuardador().deleteAll();
			cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());

		}
	}

	private void buscarArticulo() {
		ViewListaArticulo viewListaArticulo = new ViewListaArticulo(SwingUtilities.getWindowAncestor(view));
		CtlArticuloBuscar ctlArticulo = new CtlArticuloBuscar(viewListaArticulo);

		viewListaArticulo.pack();
		ctlArticulo.view.getTxtBuscar().setText("");
		ctlArticulo.view.getTxtBuscar().selectAll();
		viewListaArticulo.conectarControladorBuscar(ctlArticulo);
		ctlArticulo.view.getTxtBuscar().requestFocusInWindow();

		boolean resul = ctlArticulo.buscarArticulo(null);

		if (resul) {
			myArticulo = ctlArticulo.getArticulo();

			myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
			myArticulo.setPreciosVenta(this.preciosDao.getPreciosArticuloSinCosto(myArticulo.getId()));

			boolean unirCantidad = false;
			if (config.isUnirCanItem())
				unirCantidad = this.buscarArticuloEnFactura(myArticulo);

			if (!unirCantidad) {
				if (config.isFacturarSinInventario()) {
					agregarArticuloSinInventario();
				} else {
					agregarArticuloConInventario();
				}
			}
		}

		viewListaArticulo.dispose();
		ctlArticulo = null;
	}

	private void setEmptyView() {
		view.getModeloTabla().setEmptyDetalles();
		myFactura.setCodigoAlter(0);
		view.getModeloTabla().agregarDetalle();

		this.myFactura.resetTotales();
		this.myFactura.setVendedor(new Empleado());

		String fechaSistema = facturaDao.getFechaSistema();
		view.getTxtFechafactura().setText(fechaSistema);
		ViewModuloFacturar framePadre = (ViewModuloFacturar) view.getTopLevelAncestor();
		if (framePadre != null) {
			framePadre.btnFecha.setText("Fecha: " + fechaSistema);
			framePadre.btnFecha.revalidate();
		}

		this.view.getTxtIdcliente().setText("1");
		this.view.getTxtNombrecliente().setText("Consumidor final");
		view.getTxtRtn().setText("");

		this.myCliente = null;
		this.myArticulo = null;

		this.view.getTxtBuscar().setText("");
		this.view.getTxtDescuento().setText("");
		this.view.getTxtImpuesto().setText("0.00");
		this.view.getTxtImpuesto18().setText("0.00");
		this.view.getTxtSubtotal().setText("0.00");
		this.view.getTxtTotal().setText("0.00");
		this.myFactura.setObservacion("");
		this.view.getRdbtnContado().setSelected(true);
		this.view.setEstadoFactura(false, 0);

		this.view.getTxtBuscar().requestFocusInWindow();
	}

	private void buscarOrden() {
		ViewListaOrdenes viewListaOrdenes = new ViewListaOrdenes(SwingUtilities.getWindowAncestor(view));
		CtlOrdenesBuscar ctlBuscarOrden = new CtlOrdenesBuscar(viewListaOrdenes);

		viewListaOrdenes.pack();
		ctlBuscarOrden.view.getTxtBuscar().setText("");
		ctlBuscarOrden.view.getTxtBuscar().selectAll();
		ctlBuscarOrden.view.getTxtBuscar().requestFocusInWindow();

		boolean resul = ctlBuscarOrden.buscarCliente(null);
		if (resul) {
			this.myFactura = ctlBuscarOrden.getOrden();

			myFactura.setDetalles(detallesOrdenDao.detallesFacturaPendiente(myFactura.getIdFactura()));

			cargarFacturaView();
			this.calcularTotales();
			this.view.getBtnGuardar().setEnabled(false);
			this.view.getBtnActualizar().setEnabled(true);
			this.view.getModeloTabla().agregarDetalle();
			this.view.setEstadoFactura(true, myFactura.getIdFactura());
			tipoView = 2;

		}
		viewListaOrdenes.dispose();
		ctlBuscarOrden = null;
	}

	private void buscarCliente() {
		ViewListaClientes viewListaCliente = new ViewListaClientes(SwingUtilities.getWindowAncestor(view));

		CtlClienteBuscar ctlBuscarCliente = new CtlClienteBuscar(viewListaCliente);
		viewListaCliente.pack();
		ctlBuscarCliente.view.getTxtBuscar().setText("");
		ctlBuscarCliente.view.getTxtBuscar().selectAll();
		ctlBuscarCliente.view.getTxtBuscar().requestFocusInWindow();

		boolean resul = ctlBuscarCliente.buscarCliente(null);
		if (resul) {

			myCliente = ctlBuscarCliente.getCliente();
			this.view.getTxtIdcliente().setText("" + myCliente.getId());
			this.view.getTxtNombrecliente().setText(myCliente.getNombre());
			this.view.getTxtRtn().setText(myCliente.getRtn());

		} else {
			this.view.getTxtIdcliente().setText("1");
			this.view.getTxtNombrecliente().setText("Consumidor final");
		}
		viewListaCliente.dispose();
		ctlBuscarCliente = null;
	}

	public void guardarLocal() {
		int idFacturaTemporal = myFactura.getIdFactura();
		boolean resul = facturaDao.registrar(myFactura);

		if (resul) {
			myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());

			if (ConexionStatic.getNivelFact()) {
				try {
					AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 6,
							myFactura.getIdFactura());
					AbstractJasperReports.imprimierFactura();

						ViewCambio cambio = new ViewCambio(null);
					cambio.getTxtCambio().setText(myFactura.getCambio().toString());
					cambio.getTxtEfectivo().setText(myFactura.getPago().toString());
					cambio.setVisible(true);

					setEmptyView();

					if (this.tipoView == 2) {
						this.tipoView = 1;
						this.view.getBtnGuardar().setEnabled(true);
						this.view.getBtnActualizar().setEnabled(false);

						Factura eliminarTem = new Factura();
						eliminarTem.setIdFactura(idFacturaTemporal);

						this.facturaOrdenesDao.eliminar(eliminarTem);

						setEmptyView();

						view.getBtnsGuardador().deleteAll();

						cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			setEmptyView();

		} else {
			JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos",
					JOptionPane.ERROR_MESSAGE);
			this.view.setVisible(false);
			this.view.dispose();
		}

	}

	private void selectRowInset() {

		int row = this.view.getTableDetalle().getRowCount() - 2;
		int col = 1;
		boolean toggle = false;
		boolean extend = false;
		this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
		this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
		this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

	}

	public void cargarFacturaView() {

		this.view.getTxtIdcliente().setText("" + myFactura.getCliente().getId());
		this.view.getTxtNombrecliente().setText(myFactura.getCliente().getNombre());
		view.getTxtRtn().setText(myFactura.getCliente().getRtn());

		view.actualizarTotales(myFactura);

		this.view.getModeloTabla().setDetalles(myFactura.getDetalles());
	}

	public Factura actualizarFactura(Factura f) {

		this.myFactura = f;
		cargarFacturaView();
		this.view.getBtnGuardar().setVisible(false);
		this.view.getBtnActualizar().setVisible(true);
		this.view.getModeloTabla().agregarDetalle();
		this.view.setEstadoFactura(true, f.getIdFactura());
		tipoView = 2;
		this.view.setVisible(true);

		return myFactura;

	}

	public boolean getAccion() {
		view.setVisible(true);
		return resultado;
	}

	public void viewFactura(Factura f) {
	
		this.myFactura = f;
		cargarFacturaView();
		this.view.getPanelAcciones().setVisible(false);
		this.view.setVisible(true);
	}

	public Factura getFactura() {
	
		return this.myFactura;
	}

	private boolean setCierre() {
		/* seccion de cierre de caja */
		/* seccion de cierre de caja */

		boolean resul = false;

		CierreCajaDao cierreDao = new CierreCajaDao();
		CierreFacturacionDao cierreFacturasDao = new CierreFacturacionDao();

		CierreCaja oldCierre = cierreDao.getCierreUltimoUser();

		if (oldCierre.getEstado() == false) {
			ViewCuentaEfectivo viewContar = new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar = new CtlContarEfectivo(viewContar);

			if (ctlContar.getEstado()) {
				CierreCaja newCierre = new CierreCaja();
				newCierre.setEfectivoInicial(ctlContar.getTotal());
				newCierre.setUsuario(usuario.getUser());

				for (int xx = 0; xx < usuario.getCajas().size(); xx++) {
					CierreFacturacion unaC = cierreFacturasDao.buscarPorCajaUsuario(
							usuario.getCajas().get(xx),
							usuario.getUser());

					if (unaC != null) {
						CierreFacturacion una = new CierreFacturacion();
						una.setCaja(unaC.getCaja());
						una.setNoFacturaInicio(unaC.getNoFacturaFinal() + 1);
						una.setUsuario(usuario.getUser());
						newCierre.getCierreFacturas().add(una);
					} else {
						CierreFacturacion una = new CierreFacturacion();
						una.setCaja(usuario.getCajas().get(xx));
						una.setNoFacturaInicio(1);
						una.setUsuario(usuario.getUser());
						newCierre.getCierreFacturas().add(una);
					}
				}

				newCierre.setNoSalidaInicial(oldCierre.getNoSalidaFinal() + 1);
				newCierre.setNoEntradaInicial(oldCierre.getNoEntradaFinal() + 1);
				newCierre.setNoCobroInicial(oldCierre.getNoCobroFinal() + 1);
				newCierre.setNoPagoInicial(oldCierre.getNoPagoFinal() + 1);
				cierreDao.registrarCierre(newCierre);
				resul = true;

				viewContar.dispose();
				viewContar = null;
				ctlContar = null;
			} else {
				resul = false;
			}

		} else {
			resul = true;
		}

		return resul;

	}

	public void guardarFactura() {
		if (config.isRotacionAutomaticaCajas()
				&& !rotacionManual
				&& myCliente != null && myCliente.getId() == 1 && bandera < 1) {
			usuario.nextCaja();
			this.cajaActiva = usuario.getCajaActiva();
		}

		Integer idFacturaTemporal = myFactura.getIdFactura();
		boolean resul = facturaDao.registrar(myFactura);

		if (resul) {
			myFactura.setIdFactura(facturaDao.getIdFacturaGuardada());
			Integer copiasFacturas = config.getCopiasFacturas();

			try {
				if (myFactura.getTipoFactura() == 1) {
					if (config.getFormatoFactura().equals("tiket")) {
						for (int xx = 0; xx < copiasFacturas; xx++) {
							AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 1,
									myFactura.getIdFactura());
							AbstractJasperReports.imprimierFactura();
						}
					}

					if (config.getFormatoFactura().equals("carta")) {
						for (int xx = 0; xx < copiasFacturas; xx++) {
							if (xx == 0) {
								AbstractJasperReports.createReportFacturaCarta(
										ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),
										"ORIGINAL");
								AbstractJasperReports.imprimierFactura();
							} else {
								AbstractJasperReports.createReportFacturaCarta(
										ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),
										"COPIA");
								AbstractJasperReports.imprimierFactura();
							}
						}
					}
				} // fin de la impresion de la factura carta al contado

				if (myFactura.getTipoFactura() == 2) {
					if (config.getFormatoFacturaCredito().equals("tiket")) {
						for (int xx = 0; xx < copiasFacturas; xx++) {
							AbstractJasperReports.createReportFacturaTiketCredito(
									ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());
							AbstractJasperReports.imprimierFactura();
						}
					}

					if (config.getFormatoFacturaCredito().equals("carta")) {
						for (int xx = 0; xx < copiasFacturas; xx++) {
							if (xx == 0) {
								AbstractJasperReports.createReportFacturaCartaCredito(
										ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),
										"ORIGINAL");
								AbstractJasperReports.imprimierFactura();
							} else {
								AbstractJasperReports.createReportFacturaCartaCredito(
										ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura(),
										"COPIA");
								AbstractJasperReports.imprimierFactura();
							}
						}
					}
				}

				if (config.isImprReportOrden()) {
					int resul2 = JOptionPane.showConfirmDialog(view, "Desea imprimir la orden?");
					if (resul2 == 0) {
						AbstractJasperReports.createReportOrden(ConexionStatic.getPoolConexion().getConnection(),
								myFactura.getIdFactura());
						AbstractJasperReports.imprimierFactura();

					}
				}

				if (config.isRotacionAutomaticaCajas()) {
					CajaDao cajasDao = new CajaDao();
					usuario.setCajas(cajasDao.getCajasUsuario(usuario));
					this.cajaActiva = usuario.getCajaActiva();
					ViewModuloFacturar frameCaja = (ViewModuloFacturar) view.getTopLevelAncestor();
					if (cajaActiva != null) {
						frameCaja.btnCaja.setText(cajaActiva.getDescripcion());
					}

					if (myCliente != null && myCliente.getId() == 1) {
						if (bandera > 1) {
							bandera = 0;
						} else {
							bandera++;
						}
					}
				} else {
					recargarCajasPreservandoActiva();
				}
				rotacionManual = false;

				String cambioEfectivo = myFactura.getCambio().toString();
				String pago = myFactura.getPago().toString();

				setEmptyView();

				ViewCambio cambio = new ViewCambio(SwingUtilities.getWindowAncestor(view));
				cambio.getTxtCambio().setText(cambioEfectivo);
				cambio.getTxtEfectivo().setText(pago);
				cambio.setVisible(true);

				if (this.tipoView == 2) {
					this.tipoView = 1;
					this.view.getBtnGuardar().setEnabled(true);
					this.view.getBtnActualizar().setEnabled(false);

					Factura eliminarTem = new Factura();
					eliminarTem.setIdFactura(idFacturaTemporal);

					this.facturaOrdenesDao.cambiarEstado(eliminarTem, 3);

					setEmptyView();

					view.getBtnsGuardador().deleteAll();

					cargarFacturasPendientes(facturaOrdenesDao.ordenesPorEmpleadosUsuarios());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			setEmptyView();

		} else {
			JOptionPane.showMessageDialog(view, "No se guardo la factura", "Error Base de Datos",
					JOptionPane.ERROR_MESSAGE);
			this.view.setVisible(false);
			this.view.dispose();
		}

	}

	public boolean buscarArticuloEnFactura(Articulo art) {
		boolean existe = false;
		for (int x = 0; x < view.getModeloTabla().getDetalles().size(); x++) {
			Articulo artLocal = view.getModeloTabla().getDetalles().get(x).getArticulo();

			if (art.getId() == artLocal.getId()) {
				existe = true;

				int row = x;
				int col = 1;
				boolean toggle = false;
				boolean extend = false;
				this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
				this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
				this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

				String entrada = (String) JOptionPane.showInputDialog(view,
						"El articula ya esta en la factura. Escriba el cantida a agregar:",
						"Agregar cantidad\n", JOptionPane.OK_CANCEL_OPTION, null,
						null, "1");

				if (config.isFacturarSinInventario()) {
					BigDecimal cantidadSaldoItem = new BigDecimal(entrada);

					BigDecimal newCantidadSaldoItem = new BigDecimal(
							view.getModeloTabla().getDetalle(x).getCantidad().add(cantidadSaldoItem).doubleValue());

					this.view.getModeloTabla().getDetalle(x).setCantidad(newCantidadSaldoItem);
					this.calcularTotales();

				} else {
					if (AbstractJasperReports.isNumberReal(entrada)) {
						if (myArticulo.getTipoArticulo() == 1) {
							double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
									cajaActiva.getDetartamento().getId());

							BigDecimal cantidadSaldoItem = new BigDecimal(entrada);

							double cantidad = view.getModeloTabla().getDetalle(x).getCantidad().add(cantidadSaldoItem)
									.doubleValue();

							BigDecimal newCantidadSaldoItem = new BigDecimal(view.getModeloTabla().getDetalle(x)
									.getCantidad().add(cantidadSaldoItem).doubleValue());

							this.view.getModeloTabla().getDetalle(x).setCantidad(newCantidadSaldoItem);

							if (existencia > 0.0 && cantidad <= existencia) {
								this.calcularTotales();
							} else {
								JOptionPane.showMessageDialog(view,
										"No se puede requerir la cantidad de "
												+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN)
														.doubleValue()
												+ " del articulo en la bodega " + usuario
														.getCajaActiva().getDetartamento().getDescripcion());
								view.getModeloTabla().eliminarDetalle(x);
							}
						}
					}
				}

			}

		}
		return existe;
	}

	private void selectRowInset(int row) {
		int col = 1;
		boolean toggle = false;
		boolean extend = false;
		this.view.getTableDetalle().changeSelection(row, 0, toggle, extend);
		this.view.getTableDetalle().changeSelection(row, col, toggle, extend);
		this.view.getTableDetalle().addColumnSelectionInterval(0, 6);

	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		this.guardar();

	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

	private void recargarCajasPreservandoActiva() {
		Caja actual = usuario.getCajaActiva();
		int codigoActual = actual != null ? actual.getCodigo() : -1;

		CajaDao cajasDao = new CajaDao();
		usuario.setCajas(cajasDao.getCajasUsuario(usuario));

		if (codigoActual > 0 && usuario.getCajas() != null) {
			boolean encontrada = false;
			for (Caja c : usuario.getCajas()) {
				if (c.getCodigo() == codigoActual) {
					c.setActiva(true);
					encontrada = true;
				} else {
					c.setActiva(false);
				}
			}
			if (encontrada) {
				this.cajaActiva = usuario.getCajaActiva();
			}
		}
	}

}
