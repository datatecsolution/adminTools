package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.PrecioArticuloDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.service.ConfiguracionUsuarioService;
import net.datatecsolution.admin_tools.view.ViewCrearUsuario;
import net.datatecsolution.admin_tools.view.ViewListaCajas;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CtlUsuario extends MouseAdapter implements ActionListener {
	private ViewCrearUsuario view = null;
	private Usuario myUsuario = null;
	private UsuarioDao myDao = null;
	private EmpleadoDao empleadoDao = null;
	private PrecioArticuloDao precioDao = null;
	private CajaDao cajaDao = null;
	private final ConfiguracionUsuarioService configService = new ConfiguracionUsuarioService();
	private List<Empleado> todosEmpleados = null;
	private List<PrecioArticulo> todosPrecios = null;
	private boolean resultaOperacion = false;

	public CtlUsuario(ViewCrearUsuario v) {
		view = v;

		myUsuario = new Usuario();
		myDao = new UsuarioDao();
		empleadoDao = new EmpleadoDao();
		precioDao = new PrecioArticuloDao();
		cajaDao = new CajaDao();

		view.conectarCtl(this);
		cargarListasReferencia();
		view.mostrarCard(ViewCrearUsuario.CARD_ESCRITORIO);
	}

	private void cargarListasReferencia() {
		java.util.Vector<Empleado> empleados = empleadoDao.todoEmpleadosVendedores();
		todosEmpleados = empleados == null ? new ArrayList<Empleado>() : new ArrayList<Empleado>(empleados);
		todosPrecios = precioDao.getTipoPrecios();
		if (todosPrecios == null) todosPrecios = new ArrayList<PrecioArticulo>();

		// US-128: el card Movil ahora tiene su propio selector de caja.
		List<Caja> todasCajas = cajaDao.todosList();
		if (todasCajas == null) todasCajas = new ArrayList<Caja>();

		view.cargarEmpleadosEscritorio(todosEmpleados);
		view.cargarComboEmpleadoMovil(todosEmpleados);
		view.cargarCajasMovil(todasCajas);
		view.cargarPreciosMovil(todosPrecios);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando = e.getActionCommand();

		switch (comando) {
		case "AGREGARCAJA":
			ViewListaCajas vListaCajas = new ViewListaCajas(view);
			CtlCajasBuscar cListaCajaBuscar = new CtlCajasBuscar(vListaCajas);

			boolean resul = cListaCajaBuscar.buscarCaja();

			if (resul) {
				boolean verificar = view.getModeloListaCajas().verificarDuplicado(cListaCajaBuscar.getMyCaja());
				if (verificar) {
					JOptionPane.showMessageDialog(view, "La caja ya esta asignada al usuario", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					if (view.getModeloListaCajas().getSize() == 0)
						cListaCajaBuscar.getMyCaja().setActiva(true);
					view.getModeloListaCajas().addCaja(cListaCajaBuscar.getMyCaja());
				}
			}

			break;
		case "GUARDAR":
			if (validar()) {
				setUser();
				guardarUsuario();
			}
			break;
		case "CANCELAR":
			view.setVisible(false);
			break;
		case "ACTUALIZAR":
			if (validar()) {
				setUser();
				actualizarUsuario();
			}
			break;
		case "ELIMINARCODIGO":
			int idxCodigo = this.view.getlCajas().getSelectedIndex();
			Caja cajaSelect = this.view.getModeloListaCajas().getCaja(idxCodigo);
			int conf = JOptionPane.showConfirmDialog(view, "Desea eliminar [" + cajaSelect + "] del usuario?");
			if (conf == 0) this.view.getModeloListaCajas().eliminarCaja(idxCodigo);
			break;
		case "SETDEFAULT":
			int idxCodigo2 = this.view.getlCajas().getSelectedIndex();
			Caja cajaSelect2 = this.view.getModeloListaCajas().getCaja(idxCodigo2);
			int conf2 = JOptionPane.showConfirmDialog(view, "Desea establecer default la caja [" + cajaSelect2 + "] del usuario?");
			if (conf2 == 0) this.view.getModeloListaCajas().setCajaDefault(idxCodigo2);
			break;
		case "TIPO_ESCRITORIO":
			view.mostrarCard(ViewCrearUsuario.CARD_ESCRITORIO);
			break;
		case "TIPO_MOVIL":
			view.mostrarCard(ViewCrearUsuario.CARD_MOVIL);
			break;
		}
	}

	private void actualizarUsuario() {
		if (myDao.actualizar(myUsuario)) {
			JOptionPane.showMessageDialog(view, "El Usuario se actualizo correctamente.");
			this.resultaOperacion = true;
			view.setVisible(false);
		} else {
			JOptionPane.showMessageDialog(view, "Ocurrio un problema para actualizar el usuario");
		}
	}

	private void guardarUsuario() {
		if (myDao.registrar(myUsuario)) {
			JOptionPane.showMessageDialog(view, "El Usuario se guardo correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
			this.resultaOperacion = true;
			view.setVisible(false);
		} else {
			JOptionPane.showMessageDialog(view, "Ocurrio un problema para guardar el usuario", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setUser() {
		myUsuario.setUser(view.getTxtUser().getText());
		myUsuario.setNombre(view.getTxtNombre().getText());
		myUsuario.setPwd(view.getPwd().getText());
		myUsuario.setCajas(view.getModeloListaCajas().getCajas());

		if (view.getRdbtnTipoMovil().isSelected()) {
			myUsuario.setTipoPermiso(3);
			myUsuario.setPermiso("Vendedor");
			myUsuario.setCodigoEmpleado(view.getCodigoEmpleadoMovilSeleccionado());

			// US-128: la caja del vendedor sale del combo de ESTE card, no de
			// la lista del card Escritorio (que en modo Movil queda vacia y
			// por eso el usuario nacia sin caja). Va marcada como activa
			// porque asignarCajas persiste por_defecto desde isActiva().
			List<Caja> cajaDelVendedor = new ArrayList<Caja>();
			Caja elegida = view.getCajaMovilSeleccionada();
			if (elegida != null) {
				elegida.setActiva(true);
				cajaDelVendedor.add(elegida);
			}
			myUsuario.setCajas(cajaDelVendedor);

			List<PrecioArticulo> seleccionados = new ArrayList<PrecioArticulo>();
			for (Integer codigo : view.getPreciosMarcados()) {
				PrecioArticulo p = new PrecioArticulo();
				p.setCodigoPrecio(codigo);
				seleccionados.add(p);
			}
			myUsuario.setPreciosAsignados(seleccionados);
			myUsuario.setVendedoresAsignados(new ArrayList<Empleado>());
		} else {
			if (view.getRdbtnAdministrador().isSelected()) {
				myUsuario.setTipoPermiso(4);
				myUsuario.setPermiso("Administrador");
			} else if (view.getRdbtnCajero().isSelected()) {
				myUsuario.setTipoPermiso(2);
				myUsuario.setPermiso("Cajero");
			} else if (view.getRdbtnVendedor().isSelected()) {
				myUsuario.setTipoPermiso(3);
				myUsuario.setPermiso("Vendedor");
			} else if (view.getRdbtnSupervisor().isSelected()) {
				myUsuario.setTipoPermiso(1);
				myUsuario.setPermiso("Supervisor");
			}
			myUsuario.setCodigoEmpleado(0);

			List<Empleado> seleccionados = new ArrayList<Empleado>();
			for (Integer codigo : view.getEmpleadosMarcados()) {
				Empleado e = new Empleado();
				e.setCodigo(codigo);
				seleccionados.add(e);
			}
			myUsuario.setVendedoresAsignados(seleccionados);
			myUsuario.setPreciosAsignados(new ArrayList<PrecioArticulo>());
		}
	}

	private boolean validar() {
		String jpf1Text = Arrays.toString(view.getPwd().getPassword());
		String jpf2Text = Arrays.toString(view.getRePwd().getPassword());
		String errorVendedor = errorConfiguracionVendedor();
		boolean resul = false;
		if (view.getTxtUser().getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getTxtUser().requestFocusInWindow();
		} else if (view.getTxtNombre().getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getTxtNombre().requestFocusInWindow();
		} else if (view.getPwd().getPassword().length == 0) {
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getPwd().requestFocusInWindow();
		} else if (view.getRePwd().getPassword().length == 0) {
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getRePwd().requestFocusInWindow();
		} else if (!jpf1Text.equals(jpf2Text)) {
			JOptionPane.showMessageDialog(view, "Password no son iguales!!!");
			view.getPwd().requestFocusInWindow();
		} else if (view.getRdbtnTipoMovil().isSelected() && view.getCodigoEmpleadoMovilSeleccionado() == 0) {
			JOptionPane.showMessageDialog(view, "Debe seleccionar un vendedor para el usuario movil");
		} else if (errorVendedor != null) {
			// US-128: un vendedor sin caja (o sin empleado) se guardaba "con
			// exito" y quedaba inutilizable — la API le rechaza cada pedido
			// con 409. Ahora no se guarda hasta que este completo.
			JOptionPane.showMessageDialog(view, errorVendedor,
					"Configuracion incompleta", JOptionPane.WARNING_MESSAGE);
		} else {
			resul = true;
		}
		return resul;
	}

	/**
	 * US-128 — comprueba, ANTES de guardar, que un vendedor quede utilizable.
	 * Lee la pantalla en el mismo orden en que lo hara {@link #setUser()}.
	 */
	private String errorConfiguracionVendedor() {
		int tipoPermiso;
		int codigoEmpleado;
		List<Caja> cajas;

		if (view.getRdbtnTipoMovil().isSelected()) {
			tipoPermiso = ConfiguracionUsuarioService.TIPO_VENDEDOR;
			codigoEmpleado = view.getCodigoEmpleadoMovilSeleccionado();
			cajas = new ArrayList<Caja>();
			if (view.getCajaMovilSeleccionada() != null) {
				cajas.add(view.getCajaMovilSeleccionada());
			}
		} else if (view.getRdbtnVendedor().isSelected()) {
			// Vendedor creado en modo Escritorio: ese camino fuerza
			// codigo_empleado = 0, asi que nunca puede quedar completo.
			tipoPermiso = ConfiguracionUsuarioService.TIPO_VENDEDOR;
			codigoEmpleado = 0;
			cajas = view.getModeloListaCajas().getCajas();
		} else {
			return null;
		}

		return configService.validarVendedor(tipoPermiso, codigoEmpleado, cajas);
	}

	public boolean agregarUsuario() {
		view.setVisible(true);
		return resultaOperacion;
	}

	public Usuario getUsuario() {
		return myUsuario;
	}

	public void loadUsuario() {
		view.getTxtUser().setText(myUsuario.getUser());
		view.getTxtNombre().setText(myUsuario.getNombre());

		boolean esMovil = myUsuario.getPreciosAsignados() != null && !myUsuario.getPreciosAsignados().isEmpty();

		if (esMovil) {
			view.getRdbtnTipoMovil().setSelected(true);
			view.mostrarCard(ViewCrearUsuario.CARD_MOVIL);
			view.seleccionarEmpleadoMovil(myUsuario.getCodigoEmpleado());
			view.marcarPreciosAsignados(myUsuario.getPreciosAsignados());
			// US-128: reflejar la caja que ya tiene (o dejar el placeholder si
			// quedo sin ninguna, como WACAESTRA, para que se vea que falta).
			if (myUsuario.getCajas() != null && !myUsuario.getCajas().isEmpty()) {
				view.seleccionarCajaMovil(myUsuario.getCajas().get(0).getCodigo());
			} else {
				view.seleccionarCajaMovil(0);
			}
		} else {
			view.getRdbtnTipoEscritorio().setSelected(true);
			view.mostrarCard(ViewCrearUsuario.CARD_ESCRITORIO);

			if (myUsuario.getTipoPermiso() == 4) view.getRdbtnAdministrador().setSelected(true);
			else if (myUsuario.getTipoPermiso() == 2) view.getRdbtnCajero().setSelected(true);
			else if (myUsuario.getTipoPermiso() == 1) view.getRdbtnSupervisor().setSelected(true);
			else if (myUsuario.getTipoPermiso() == 3) view.getRdbtnVendedor().setSelected(true);

			view.marcarEmpleadosAsignados(myUsuario.getVendedoresAsignados());
		}

		if (myUsuario.getCajas() != null) {
			view.getModeloListaCajas().setCajas(myUsuario.getCajas());
		}
	}

	public boolean actualizarUsuario(Usuario user) {
		myUsuario = user;
		myUsuario.setUserOld(user.getUser());

		myDao.cargarAsignaciones(myUsuario);

		view.getBtnActualizar().setVisible(true);
		view.getBtnGuardar().setVisible(false);
		view.getTxtUser().setEnabled(false);
		loadUsuario();
		view.setVisible(true);
		return resultaOperacion;
	}

	public void mousePressed(MouseEvent evento) {
		check(evento);
		checkForTriggerEvent(evento);
	}

	public void mouseReleased(MouseEvent evento) {
		check(evento);
		checkForTriggerEvent(evento);
	}

	private void checkForTriggerEvent(MouseEvent evento) {
		if (evento.isPopupTrigger())
			this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY());
	}

	public void check(MouseEvent e) {
		if (e.isPopupTrigger()) {
			this.view.getlCajas().setSelectedIndex(this.view.getlCajas().locationToIndex(e.getPoint()));
		}
	}

}
