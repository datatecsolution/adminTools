package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;
import net.datatecsolution.admin_tools.service.CierreCajaService;
import net.datatecsolution.admin_tools.service.FacturacionService;
import net.datatecsolution.admin_tools.view.*;
import net.datatecsolution.admin_tools.view.dto.FacturaCabeceraData;
import net.datatecsolution.admin_tools.view.dto.FacturaClienteData;

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
	private ClienteDao clienteDao = null;
	private Articulo myArticulo = null;
	private ArticuloDao myArticuloDao = null;
	private InsumoDao insumoDao = null;
	private CodBarraDao codBarraDao = null;

	private Cliente myCliente = null;
	private int filaPulsada = 0;
	private boolean resultado = false;
	private UsuarioDao myUsuarioDao;

	private int tipoView = 1;
	private int netBuscar = 0;

	List<ViewFacturarFrame> ventanas;
	private final FacturacionService facturacionService;
	private final CierreCajaService cierreCajaService;
	private Caja cajaDefecto;
	private boolean isThereConexion = false;

	private Integer bandera = 0;
	private boolean rotacionManual = false;

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
		codBarraDao = new CodBarraDao();
		insumoDao = new InsumoDao();
		facturacionService = new FacturacionService();
		cierreCajaService = new CierreCajaService();

		myUsuarioDao = new UsuarioDao();

		usuario = ConexionStatic.getUsuarioLogin();
		config = usuario.getConfig();
		cajaActiva = usuario.getCajaActiva();

		this.setEmptyView();

		cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
				this.view.setEstadoBotonesNuevo();

				setEmptyView();

				view.limpiarOrdenesGuardadas();

				cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
		setFacturaBasica();
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
						this.view.setEstadoBotonesNuevo();

						setEmptyView();

						view.limpiarOrdenesGuardadas();

						cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
		if (view.getTextoBusqueda().trim().length() == 0 && myArticulo != null) {
			netBuscar = 0;
			return;
		}

		String busca = view.getTextoBusqueda();
		this.myArticulo = this.myArticuloDao.buscarArticuloBarraCod(busca);

		if (myArticulo == null) {
			JOptionPane.showMessageDialog(view, "No se encontro el articulo");
			view.limpiarYEnfocarBusqueda();
			myArticulo = null;
			netBuscar = 0;
			return;
		}

		if (config.isPrecioRedondear()) {
			myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
		}

		myArticulo.setPreciosVenta(this.facturacionService.obtenerPreciosSinCosto(myArticulo.getId()));

		boolean unirCantidad = false;
		if (config.isUnirCanItem())
			unirCantidad = this.buscarArticuloEnFactura(myArticulo);

		if (unirCantidad) {
			view.limpiarBusqueda();
			netBuscar = 0;
			return;
		}

		if (config.isFacturarSinInventario()) {
			agregarArticuloSinInventario();
		} else {
			agregarArticuloConInventario();
		}
		view.limpiarBusqueda();
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
				this.view.setArticuloDetalle(myArticulo);
				calcularTotales();
				this.view.agregarDetalle();
				selectRowInset();
			} else {
				JOptionPane.showMessageDialog(view,
						"Solo la caja " + cajaDefecto.getDescripcion() + " puede facturar servicios.",
						"Error en articulo", JOptionPane.ERROR_MESSAGE);
				view.limpiarBusqueda();
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
			double buscarEnRequisicionCantidad = view.buscarCantidadPorArticulo(myArticulo);
			if (buscarEnRequisicionCantidad > 0) {
				cantidad = cantidad + buscarEnRequisicionCantidad;
			}

			if (existencia > 0.0 && cantidad <= existencia) {
				if (config.isPrecioRedondear()) {
					myArticulo.setPrecioVenta((int) Math.round(myArticulo.getPrecioVenta()));
				}
				this.view.setArticuloDetalle(myArticulo);
				calcularTotales();
				this.view.agregarDetalle();
				selectRowInset();
			} else {
				JOptionPane.showMessageDialog(view,
						myArticulo.getArticulo() + " no tiene existencia en "
								+ cajaActiva.getDetartamento().getDescripcion(),
						"Error en existencia", JOptionPane.ERROR_MESSAGE);
				view.limpiarBusqueda();
			}
		} else {
			if (cajaActiva.getCodigo() != this.cajaDefecto.getCodigo()) {
				JOptionPane.showMessageDialog(view,
						"Solo la caja " + cajaDefecto.getDescripcion() + " puede facturar servicios.",
						"Error en articulo", JOptionPane.ERROR_MESSAGE);
				view.limpiarBusqueda();
				return;
			}

			List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

			if (insumos == null || insumos.size() == 0) {
				this.view.setArticuloDetalle(myArticulo);
				calcularTotales();
				this.view.agregarDetalle();
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
					view.limpiarBusqueda();
					break;
				}
			}

			if (exist) {
				this.view.setArticuloDetalle(myArticulo);
				calcularTotales();
				this.view.agregarDetalle();
				selectRowInset();
			}
		}
	}

	private void buscarClientePorId() {
		myCliente = null;
		myCliente = clienteDao.buscarPorId(view.getClienteData().getId());

		if (myCliente != null) {
			view.setClienteData(new FacturaClienteData(myCliente.getId(), myCliente.getNombre(), myCliente.getRtn()));
		} else {
			JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			view.setClienteData(new FacturaClienteData(1, "Cliente Normal", ""));
		}
	}

	private void imprimirPendiente() {
		int idFacturaTemporal = view.getOrdenSeleccionadaPanel().getIdFactura();
		try {
			AbstractJasperReports.createReportOrdenCarta(ConexionStatic.getPoolConexion().getConnection(),
					idFacturaTemporal);
			AbstractJasperReports.showViewer(view);
		} catch (SQLException ee) {
			ee.printStackTrace();
		}
	}

	private void eliminarPendiente() {
		int idFacturaTemporal = view.getOrdenSeleccionadaPanel().getIdFactura();

		int confirma = JOptionPane.showConfirmDialog(
				view,
				"¿Está seguro que desea eliminar la orden #" + idFacturaTemporal + "?",
				"Confirmar eliminación",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirma == JOptionPane.YES_OPTION) {
			Factura eliminarTem = new Factura();
			eliminarTem.setIdFactura(idFacturaTemporal);
			this.facturacionService.cambiarEstadoOrden(eliminarTem, 5);
			this.tipoView = 1;
			this.view.setEstadoBotonesNuevo();
			setEmptyView();
			view.limpiarOrdenesGuardadas();
			cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
			DetalleFactura detalle = view.getDetalle(filaPulsada);
			detalle.setDescuentoItem(facturacionService.calcularDescuentoPorcentaje(detalle, bdDescuento));
		} else {
			for (int xx = 0; xx < view.getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getDetalle(xx);
				if (detalle.getArticulo().getId() != -1
						&& detalle.getCantidad().doubleValue() != 0
						&& detalle.getArticulo().getPrecioVenta() != 0) {
					detalle.setDescuentoItem(facturacionService.calcularDescuentoPorcentaje(detalle, bdDescuento));
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
			this.view.getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(entrada));
		} else {
			for (int xx = 0; xx < view.getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getDetalle(xx);
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
				this.view.getDetalle(filaPulsada).getArticulo()
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
			this.view.getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
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

			this.view.getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);

			if (existencia > 0.0 && cantidad <= existencia) {
				this.calcularTotales();
			} else {
				JOptionPane.showMessageDialog(view,
						"No se puede requerir la cantidad de "
								+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
								+ " del articulo en la bodega "
								+ cajaActiva.getDetartamento().getDescripcion());
				view.eliminarDetalle(filaPulsada);
			}
		} else {
			BigDecimal cantidadSaldoItem = new BigDecimal(entrada);
			List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

			if (insumos == null || insumos.size() == 0) {
				this.view.getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
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
				this.view.getDetalle(filaPulsada).setCantidad(cantidadSaldoItem);
				this.calcularTotales();
			} else {
				JOptionPane.showMessageDialog(view,
						"No se puede requerir la cantidad de "
								+ cantidadSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
								+ " del articulo en la bodega "
								+ cajaActiva
										.getDetartamento().getDescripcion());
				view.eliminarDetalle(filaPulsada);
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
			for (int xx = 0; xx < view.getDetalles().size(); xx++) {
				DetalleFactura detalle = this.view.getDetalle(xx);
				detalle.getArticulo().setPreciosVenta(
						this.facturacionService.obtenerPreciosSinCosto(detalle.getArticulo().getId()));
				if (detalle.getArticulo().getId() != -1) {
					if (ctlSelectPrecio.getPrecioSelect().getCodigoPrecio() == 4) {
						PrecioArticulo unPrecio = this.facturacionService
								.obtenerPrecioArticulo(detalle.getArticulo().getId(), 4);
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
				view.getDetalle(filaPulsada).getArticulo()
						.setPreciosVenta(this.facturacionService.obtenerPreciosSinCosto(
								view.getDetalle(filaPulsada).getArticulo().getId()));

				if (ctlSelectPrecio.getPrecioSelect().getCodigoPrecio() == 4) {
					PrecioArticulo unPrecio = this.facturacionService.obtenerPrecioArticulo(
							view.getDetalle(filaPulsada).getArticulo().getId(), 4);
					if (unPrecio != null) {
						this.view.getDetalle(filaPulsada).getArticulo()
								.getPreciosVenta().add(unPrecio);
						this.view.getDetalle(filaPulsada).getArticulo()
								.setPrecio(unPrecio);
						this.selectRowInset(filaPulsada);
					} else {
						this.view.getDetalle(filaPulsada).getArticulo().lastPrecio();
						this.view.getDetalle(filaPulsada).getArticulo().netPrecio();
						this.selectRowInset(filaPulsada);
					}
				} else {
					this.view.getDetalle(filaPulsada).getArticulo()
							.setPrecio(ctlSelectPrecio.getPrecioSelect());
				}
			}
		}
		this.calcularTotales();
	}

	private void incrementarCantidad() {
		if (filaPulsada < 0) return;

		if (config.isFacturarSinInventario()) {
			this.view.masCantidad(filaPulsada);
			view.getDetalle(filaPulsada).setDescuentoItem(new BigDecimal(0));
			this.calcularTotales();
			return;
		}

		myArticulo = view.getDetalle(filaPulsada).getArticulo();

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
				this.view.getDetalle(filaPulsada).getCantidad().doubleValue());
		cantidadSaldoItem = cantidadSaldoItem.add(new BigDecimal(1));

		BigDecimal diferencia = cantidadSaldoKardex.subtract(cantidadSaldoItem);

		if (diferencia.doubleValue() >= 0.00 || myArticulo.getTipoArticulo() == 2) {
			this.view.masCantidad(filaPulsada);
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
				this.view.getDetalle(filaPulsada).getCantidad().doubleValue());
		BigDecimal newCantSaldoItem = cantidadSaldoItem.add(new BigDecimal(1));

		List<Insumo> insumos = this.insumoDao.buscarPorId(myArticulo.getId());

		if (insumos == null || insumos.size() == 0) {
			this.view.getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
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
			this.view.getDetalle(filaPulsada).setCantidad(newCantSaldoItem);
			this.calcularTotales();
		} else {
			JOptionPane.showMessageDialog(view,
					"No se puede requerir la cantidad de "
							+ newCantSaldoItem.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()
							+ " del articulo en la bodega "
							+ cajaActiva.getDetartamento().getDescripcion());
			view.eliminarDetalle(filaPulsada);
			this.calcularTotales();
		}
	}

	private void cargarFacturaPendiente(int numeroFactura) {

		Factura fact = view.buscarOrdenEnPanel(numeroFactura);
		if (fact == null) {
			return;
		}

		this.myFactura = fact;

		myFactura.setDetalles(facturacionService.detallesOrdenPendiente(numeroFactura));

		cargarFacturaView();
		this.calcularTotales();
		this.view.setEstadoBotonesEditandoOrden();
		this.view.agregarDetalle();
		this.view.setEstadoFactura(true, numeroFactura);
		tipoView = 2;
	}

	private boolean setFactura() {
		if (myCliente == null) {
			FacturaClienteData clienteData = view.getClienteData();
			myCliente = new Cliente();
			myCliente.setId(clienteData.getId());
			myCliente.setNombre(clienteData.getNombre());
			myCliente.setRtn(clienteData.getRtn());
		}

		FacturaCabeceraData cabecera = view.getCabeceraData();
		myFactura.setTipoFactura(cabecera.getTipoFactura());
		myFactura.setEstadoPago(cabecera.getEstadoPago());

		myFactura.setCliente(myCliente);
		myFactura.setDetalles(this.view.getDetalles());
		myFactura.setFecha(facturacionService.getFechaSistema());
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
			FacturaClienteData clienteData = view.getClienteData();
			myCliente = new Cliente();
			myCliente.setId(clienteData.getId());
			myCliente.setNombre(clienteData.getNombre());
			myCliente.setRtn(clienteData.getRtn());
		}

		FacturaCabeceraData cabecera = view.getCabeceraData();
		myFactura.setTipoFactura(cabecera.getTipoFactura());
		myFactura.setEstadoPago(cabecera.getEstadoPago());

		myFactura.setCliente(myCliente);
		myFactura.setDetalles(this.view.getDetalles());
		myFactura.setFecha(facturacionService.getFechaSistema());
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

			Factura facturaSeleccionada = view.getOrdenSeleccionadaPanel();
			if (facturaSeleccionada == null || facturaSeleccionada.getIdFactura() <= 0) return;

			int idFacturaTemporal = facturaSeleccionada.getIdFactura();
			this.cargarFacturaPendiente(idFacturaTemporal);
			this.view.mostrarMenuContextual(evento.getComponent(), evento.getX(), evento.getY());

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

					identificador = (int) this.view.getValorTabla(row, 0);
					myArticulo = this.view.getDetalle(row).getArticulo();
					myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));

					if (myArticulo.getId() == -2) {
						String cod = this.view.getDetalle(row).getArticulo().getCodBarra().get(0)
								.getCodigoBarra();
						this.myArticulo = this.myArticuloDao.buscarArticuloBarraCod(cod);

					} else {
						this.myArticulo = this.myArticuloDao.buscarArticulo(identificador);
						myArticulo.setCodigoBarra(codBarraDao.getCodArticulo(myArticulo.getId()));
					}

					if (myArticulo != null) {
						this.view.setArticuloDetalle(myArticulo, row);
						calcularTotales();

						this.view.enfocarCeldaTabla(row, colum, 3, 3);

					} else {
						JOptionPane.showMessageDialog(view, "No se encuentra el articulo");
						this.view.getDetalle(row).getArticulo().setId(-1);
						this.view.agregarDetalle();
						calcularTotales();
					}

				}
				if (colum == 1) {
					calcularTotales();
					view.enfocarBusqueda();
				}

				if (colum == 2) {

					identificador = (int) this.view.getValorTabla(row, 0);
					myArticulo = this.view.getDetalle(row).getArticulo();

					double existencia = myArticuloDao.getExistencia(myArticulo.getId(),
							cajaActiva.getDetartamento().getId());

					double cantidad = 1;

					double buscarEnRequisicionCantidad = view.buscarCantidadPorArticulo(myArticulo);

					if (buscarEnRequisicionCantidad > 0) {
						cantidad = cantidad + buscarEnRequisicionCantidad;
					}

					if (existencia > 0.0 && cantidad <= existencia) {

						calcularTotales();
						view.enfocarBusqueda();
					} else {
						JOptionPane.showMessageDialog(view,
								myArticulo.getArticulo() + " no tiene existencia en " + usuario
										.getCajaActiva().getDetartamento().getDescripcion(),
								"Error en existencia", JOptionPane.ERROR_MESSAGE);
						view.eliminarDetalle(row);
						calcularTotales();
					}
				}

				if (colum == 5) {
					calcularTotales();
					view.enfocarBusqueda();
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
		this.view.setArticuloDetalle(articulo);
		calcularTotales();
		this.view.agregarDetalle();
		selectRowInset();
	}

	public void calcularTotales() {
		int filaActual = view.getFilaSeleccionada();
		facturacionService.calcularTotales(myFactura, view.getDetalles());

		view.actualizarTotales(myFactura);
		view.refrescarTablaDetalle();
		if (filaActual >= 0) {
			view.seleccionarFila(filaActual);
		} else {
			this.selectRowInset();
		}
		view.enfocarBusqueda();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	

	}

	@Override
	public void keyPressed(KeyEvent e) {
	

		filaPulsada = this.view.getFilaSeleccionada();

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
				view.limpiarOrdenesGuardadas();
				cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
					this.view.eliminarDetalle(filaPulsada);
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
					this.view.getDetalle(filaPulsada).getArticulo().netPrecio();
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
					this.view.getDetalle(filaPulsada).getArticulo().lastPrecio();
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
			view.refrescarPanelGuardados();
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

			this.view.agregarDetalle();

		}

		vistaFacturars.dispose();
		ctlFacturas = null;
		vistaFacturars.dispose();
		ctlFacturas = null;
	}

	private void cierreCaja() {

		CierreCaja oldCierre = cierreCajaService.getUltimoCierreUsuario();

		if (cierreCajaService.verificarCierrePendiente(usuario.getCajas()) && oldCierre.getEstado() == true) {

			ViewCuentaEfectivo viewContar = new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar = new CtlContarEfectivo(viewContar);

			if (ctlContar.getEstado())
				if (cierreCajaService.actualizarCierre(ctlContar.getTotal()))
				{
					if (config.isImprReportCategCierre()) {
						CierreCaja elCierre = cierreCajaService.buscarPorId(cierreCajaService.getIdUltimoRegistro());
						List<VentasCategoria> ventas = new ArrayList<VentasCategoria>();
						CategoriaDao categoriaDao = new CategoriaDao();
						List<Categoria> categorias = categoriaDao.todos();

						for (int yy = 0; yy < categorias.size(); yy++) {
							VentasCategoria una = new VentasCategoria();
							una.setCodigoCategoria(categorias.get(yy).getId());
							una.setCategoria(categorias.get(yy).getDescripcion());
							ventas.add(una);
						}

						cierreCajaService.cargarCierreFacturas(elCierre);

						if (elCierre.getCierreFacturas() != null) {
							for (int xx = 0; xx < elCierre.getCierreFacturas().size(); xx++) {

								cierreCajaService.getVentasCategorias(
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
								cierreCajaService.getIdUltimoRegistro());

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
	

		filaPulsada = this.view.getFilaSeleccionada();

		if (this.view.esCampoNombreCliente(e.getComponent())) {
			view.resetIdCliente();
			this.myCliente = null;

		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A) {
			if (view.puedeActualizar())
				actualizar();
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_G) {
			if (view.puedeGuardar())
				guardar();

		}

		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_UP) {
			seleccionarPrecioEspecifico();
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N) {
			setEmptyView();
		}

		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P && !config.isRotacionAutomaticaCajas()) {

			if (config.isFacturarSinInventario()) {
				Caja caja = usuario.nextCaja();
				this.cajaActiva = caja;
				this.rotacionManual = true;

				ViewModuloFacturar frame = (ViewModuloFacturar) view.getTopLevelAncestor();
				frame.btnCaja.setText(caja.getDescripcion());

			} else {
				if (view.getCantidadFilasDetalle() <= 1) {
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
		filaPulsada = this.view.getFilaSeleccionada();
		char caracter = e.getKeyChar();

		if (e.getComponent() == this.view.getTxtBuscar()) {
			Character caracter1 = new Character(e.getKeyChar());
			if (!esValido(caracter1)) {
				String entrada = view.getTextoBusqueda();
				String texto = "";
				for (int i = 0; i < entrada.length(); i++)
					if (esValido(new Character(entrada.charAt(i))))
						texto += entrada.charAt(i);
				view.setTextoBusqueda(texto);
			}
		}
		if (caracter == '+') {
			incrementarCantidad();
		}
		if (caracter == '-') {
			if (filaPulsada >= 0) {
				this.view.restarCantidad(filaPulsada);
				this.calcularTotales();
			}
		}

	}

	public void actualizarVentanas() {
	

		boolean nivelFact = ConexionStatic.getNivelFact();
		for (int x = 0; x < ventanas.size(); x++) {
			ventanas.get(x).marcarBusquedaNivelFact(nivelFact);
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

		if (view.getCantidadFilasDetalle() > 1) {
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
				boolean resultado = facturacionService.guardarFacturaTemporal(myFactura);

				if (resultado) {
					myFactura.setIdFactura(facturacionService.getIdOrdenGuardada());
					resultado = true;

					this.tipoView = 1;

					setEmptyView();

					view.limpiarOrdenesGuardadas();

					cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
		facturacionService.actualizarFacturaTemporal(myFactura);
		this.tipoView = 1;
		this.view.setEstadoBotonesNuevo();

		setEmptyView();

		view.limpiarOrdenesGuardadas();

		cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());

	}

	private boolean validar() {
		boolean resultado = false;
		if (!(view.getCantidadFilasDetalle() > 1)) {
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

			if (view.getCantidadFilasDetalle() > 1) {

				FacturaCabeceraData cabecera = view.getCabeceraData();

				if (cabecera.esContado()) {

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
				if (cabecera.esCredito()) {// si la factura es al contado se procede a guardar e imprimir

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

			view.limpiarOrdenesGuardadas();
			cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());

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
			myArticulo.setPreciosVenta(this.facturacionService.obtenerPreciosSinCosto(myArticulo.getId()));

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
		view.vaciarDetalles();
		myFactura.setCodigoAlter(0);
		view.agregarDetalle();

		this.myFactura.resetTotales();
		this.myFactura.setVendedor(new Empleado());

		String fechaSistema = facturacionService.getFechaSistema();
		view.setCabeceraData(new FacturaCabeceraData(FacturaCabeceraData.TIPO_CONTADO, fechaSistema));
		ViewModuloFacturar framePadre = (ViewModuloFacturar) view.getTopLevelAncestor();
		if (framePadre != null) {
			framePadre.btnFecha.setText("Fecha: " + fechaSistema);
			framePadre.btnFecha.revalidate();
		}

		view.setClienteData(new FacturaClienteData(1, "Consumidor final", ""));

		this.myCliente = null;
		this.myArticulo = null;

		view.resetTotales();
		this.myFactura.setObservacion("");
		this.view.setEstadoFactura(false, 0);
		this.view.seleccionarBotonNuevaFactura();

		view.limpiarYEnfocarBusqueda();
	}

	private void buscarOrden() {
		ViewListaOrdenes viewListaOrdenes = new ViewListaOrdenes(SwingUtilities.getWindowAncestor(view));
		CtlOrdenesBuscar ctlBuscarOrden = new CtlOrdenesBuscar(viewListaOrdenes);

		viewListaOrdenes.pack();

		boolean resul = ctlBuscarOrden.buscarCliente(null);
		boolean ordenCargada = false;
		if (resul) {
			this.myFactura = ctlBuscarOrden.getOrden();

			myFactura.setDetalles(facturacionService.detallesOrdenPendiente(myFactura.getIdFactura()));

			cargarFacturaView();
			this.calcularTotales();
			this.view.setEstadoBotonesEditandoOrden();
			this.view.agregarDetalle();
			this.view.setEstadoFactura(true, myFactura.getIdFactura());
			tipoView = 2;
			ordenCargada = true;
		}
		sincronizarPanelPendientes(ordenCargada);
		viewListaOrdenes.dispose();
		ctlBuscarOrden = null;
	}

	private void sincronizarPanelPendientes() {
		sincronizarPanelPendientes(false);
	}

	private void sincronizarPanelPendientes(boolean ordenRecienCargada) {
		int idActual = (tipoView == 2 && myFactura != null) ? myFactura.getIdFactura() : 0;

		view.limpiarOrdenesGuardadas();
		cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());

		if (idActual > 0 && !view.seleccionarOrdenEnPanel(idActual) && !ordenRecienCargada) {
			setEmptyView();
			this.tipoView = 1;
			this.view.setEstadoBotonesNuevo();
		}
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
			view.setClienteData(new FacturaClienteData(myCliente.getId(), myCliente.getNombre(), myCliente.getRtn()));
		} else {
			view.setClienteData(new FacturaClienteData(1, "Consumidor final", ""));
		}
		viewListaCliente.dispose();
		ctlBuscarCliente = null;
	}

	public void guardarLocal() {
		int idFacturaTemporal = myFactura.getIdFactura();
		boolean resul = facturacionService.registrarFactura(myFactura);

		if (resul) {
			myFactura.setIdFactura(facturacionService.getIdFacturaGuardada());

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
						this.view.setEstadoBotonesNuevo();

						Factura eliminarTem = new Factura();
						eliminarTem.setIdFactura(idFacturaTemporal);

						this.facturacionService.eliminarOrden(eliminarTem);

						setEmptyView();

						view.limpiarOrdenesGuardadas();

						cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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

		int row = this.view.getCantidadFilasDetalle() - 2;
		this.view.enfocarCeldaTabla(row, 1, 0, 6);

	}

	public void cargarFacturaView() {

		Cliente clienteFactura = myFactura.getCliente();
		this.myCliente = clienteFactura;
		view.setClienteData(new FacturaClienteData(clienteFactura.getId(), clienteFactura.getNombre(), clienteFactura.getRtn()));

		view.setCabeceraData(new FacturaCabeceraData(myFactura.getTipoFactura(), view.getCabeceraData().getFecha()));

		view.actualizarTotales(myFactura);

		this.view.setDetalles(myFactura.getDetalles());
	}

	public Factura actualizarFactura(Factura f) {

		this.myFactura = f;
		cargarFacturaView();
		this.view.setModoActualizarFactura();
		this.view.agregarDetalle();
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
		this.view.ocultarPanelAcciones();
		this.view.setVisible(true);
	}

	public Factura getFactura() {
	
		return this.myFactura;
	}

	private boolean setCierre() {
		/* seccion de cierre de caja */
		/* seccion de cierre de caja */

		boolean resul = false;

		CierreCaja oldCierre = cierreCajaService.getUltimoCierreUsuario();

		if (oldCierre.getEstado() == false) {
			ViewCuentaEfectivo viewContar = new ViewCuentaEfectivo(null);
			CtlContarEfectivo ctlContar = new CtlContarEfectivo(viewContar);

			if (ctlContar.getEstado()) {
				CierreCaja newCierre = new CierreCaja();
				newCierre.setEfectivoInicial(ctlContar.getTotal());
				newCierre.setUsuario(usuario.getUser());

				for (int xx = 0; xx < usuario.getCajas().size(); xx++) {
					CierreFacturacion unaC = cierreCajaService.buscarFacturacionPorCajaUsuario(
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
				cierreCajaService.registrarCierre(newCierre);
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
		int clienteId = myCliente != null ? myCliente.getId() : -1;
		if (RotacionCajas.debeRotarPreGuardar(
				config.isRotacionAutomaticaCajas(), rotacionManual, clienteId, bandera)) {
			usuario.nextCaja();
			this.cajaActiva = usuario.getCajaActiva();
		}

		Integer idFacturaTemporal = myFactura.getIdFactura();
		boolean resul = facturacionService.registrarFactura(myFactura);

		if (resul) {
			myFactura.setIdFactura(facturacionService.getIdFacturaGuardada());
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

					int clienteIdPost = myCliente != null ? myCliente.getId() : -1;
					bandera = RotacionCajas.banderaPostGuardar(
							config.isRotacionAutomaticaCajas(), clienteIdPost, bandera);
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
					this.view.setEstadoBotonesNuevo();

					Factura eliminarTem = new Factura();
					eliminarTem.setIdFactura(idFacturaTemporal);

					this.facturacionService.cambiarEstadoOrden(eliminarTem, 3);

					setEmptyView();

					view.limpiarOrdenesGuardadas();

					cargarFacturasPendientes(facturacionService.obtenerOrdenesPendientes());
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
		for (int x = 0; x < view.getDetalles().size(); x++) {
			Articulo artLocal = view.getDetalles().get(x).getArticulo();

			if (art.getId() != artLocal.getId()) {
				continue;
			}

			BigDecimal nuevaCantidad = view.getDetalle(x).getCantidad().add(BigDecimal.ONE);

			if (!config.isFacturarSinInventario() && art.getTipoArticulo() == 1) {
				double existencia = myArticuloDao.getExistencia(art.getId(),
						cajaActiva.getDetartamento().getId());
				if (existencia <= 0.0 || nuevaCantidad.doubleValue() > existencia) {
					JOptionPane.showMessageDialog(view,
							"No hay existencia suficiente del articulo en la bodega "
									+ usuario.getCajaActiva().getDetartamento().getDescripcion());
					this.view.enfocarCeldaTabla(x, 1, 0, 6);
					return true;
				}
			}

			this.view.getDetalle(x).setCantidad(nuevaCantidad);
			this.calcularTotales();
			this.view.enfocarCeldaTabla(x, 1, 0, 6);
			return true;
		}
		return false;
	}

	private void selectRowInset(int row) {
		this.view.enfocarCeldaTabla(row, 1, 0, 6);
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
