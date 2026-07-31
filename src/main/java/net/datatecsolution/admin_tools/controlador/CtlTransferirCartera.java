package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.service.TransferenciaCarteraService;
import net.datatecsolution.admin_tools.view.ViewTransferirCartera;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * US-127 — controlador de la transferencia de cartera de clientes entre
 * vendedores.
 *
 * Las reglas de que se mueve viven en {@link TransferenciaCarteraService};
 * aca solo se orquesta la pantalla, se pide la confirmacion y se llama al DAO.
 *
 * @author jdmayorga
 */
public class CtlTransferirCartera implements ActionListener, TableModelListener {

	private final ViewTransferirCartera view;
	private final ClienteDao clienteDao;
	private final EmpleadoDao empleadoDao;
	private final UsuarioDao usuarioDao;
	private final TransferenciaCarteraService servicio;

	public CtlTransferirCartera(ViewTransferirCartera v) {
		view = v;

		clienteDao = new ClienteDao();
		empleadoDao = new EmpleadoDao();
		usuarioDao = new UsuarioDao();
		servicio = new TransferenciaCarteraService();

		view.conectarCtl(this);
		cargarEmpleados();
		actualizarResumen();

		view.setVisible(true);
	}

	private void cargarEmpleados() {
		Vector<Empleado> empleados = empleadoDao.todoEmpleadosVendedores();
		List<Empleado> lista = empleados == null
				? new ArrayList<Empleado>()
				: new ArrayList<Empleado>(empleados);
		view.cargarEmpleados(lista);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando = e.getActionCommand();

		switch (comando) {
		case "CAMBIO_ORIGEN":
			cargarCartera(true);
			break;

		case "CAMBIO_DESTINO":
		case "CAMBIO_ROLES":
			actualizarResumen();
			break;

		case "MARCAR_TODOS":
			view.getModeloClientes().marcarTodos(true);
			actualizarResumen();
			break;

		case "DESMARCAR_TODOS":
			view.getModeloClientes().marcarTodos(false);
			actualizarResumen();
			break;

		case "TRANSFERIR":
			transferir();
			break;

		case "CANCELAR":
			view.dispose();
			break;

		default:
			break;
		}
	}

	/** Cada vez que el usuario tilda o destilda un cliente, se recalcula el pie. */
	@Override
	public void tableChanged(TableModelEvent e) {
		actualizarResumen();
	}

	/**
	 * @param avisarSiVacia avisar cuando el vendedor no tiene clientes. Se
	 *                      avisa al elegir el origen (el usuario espera ver
	 *                      algo), pero no al recargar despues de transferir:
	 *                      ahi la cartera vacia es justamente el resultado
	 *                      esperado y el aviso pisaria al de exito.
	 */
	private void cargarCartera(boolean avisarSiVacia) {
		int origen = codigoOrigen();
		if (origen <= 0) {
			view.getModeloClientes().limpiar();
			actualizarResumen();
			return;
		}

		List<ClienteCartera> cartera = clienteDao.carteraDeEmpleado(origen);
		view.getModeloClientes().cargar(cartera, origen);
		actualizarResumen();

		if (cartera.isEmpty() && avisarSiVacia) {
			JOptionPane.showMessageDialog(view,
					"El vendedor seleccionado no tiene clientes asignados.",
					"Sin cartera", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void actualizarResumen() {
		view.setResumen(servicio.resumenSeleccion(
				view.getModeloClientes().getClientes(),
				codigoOrigen(),
				view.isMoverVenta(),
				view.isMoverCobro()));
	}

	private void transferir() {
		int origen = codigoOrigen();
		int destino = codigoDestino();
		boolean moverVenta = view.isMoverVenta();
		boolean moverCobro = view.isMoverCobro();

		List<ClienteCartera> marcados = servicio.seleccionados(view.getModeloClientes().getClientes());

		String error = servicio.validar(origen, destino, moverVenta, moverCobro, marcados);
		if (error != null) {
			JOptionPane.showMessageDialog(view, error, "No se puede transferir", JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<ClienteCartera> afectados = servicio.afectados(marcados, origen, moverVenta, moverCobro);

		if (!confirmar(afectados, moverVenta, moverCobro)) {
			return;
		}

		boolean resultado = clienteDao.transferirCartera(
				servicio.codigos(afectados), origen, destino, moverVenta, moverCobro);

		if (resultado) {
			JOptionPane.showMessageDialog(view,
					"Se transfirieron " + afectados.size() + " clientes a "
							+ view.getDestinoSeleccionado() + ".",
					"Transferencia realizada", JOptionPane.INFORMATION_MESSAGE);
			// Se recarga para que la tabla refleje lo que quedo en la base:
			// si solo se movio uno de los dos roles, el vendedor de origen
			// sigue teniendo esos clientes con el otro rol.
			cargarCartera(false);
		} else {
			JOptionPane.showMessageDialog(view,
					"No se pudo completar la transferencia. No se modifico ningun cliente.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Dialogo de confirmacion con resumen y password de administrador, igual
	 * que la transferencia de saldo entre cuentas: es una operacion masiva sin
	 * deshacer.
	 */
	private boolean confirmar(List<ClienteCartera> afectados, boolean moverVenta, boolean moverCobro) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new JLabel("Se va a transferir " + servicio.descripcionRoles(moverVenta, moverCobro) + ":"));
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(new JLabel("-> Origen:  " + view.getOrigenSeleccionado()));
		panel.add(new JLabel("-> Destino: " + view.getDestinoSeleccionado()));
		panel.add(new JLabel("-> Clientes que cambian: " + afectados.size()));
		panel.add(new JLabel("-> Saldo involucrado: Lps " + servicio.saldoTotal(afectados)));
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(new JLabel("Esta operacion no se puede deshacer."));
		panel.add(new JLabel("Escriba el password de admin para confirmar"));

		JPasswordField pwdAdmin = new JPasswordField();
		panel.add(pwdAdmin);

		int accion = JOptionPane.showConfirmDialog(view, panel,
				"Confirmacion de transferencia de cartera", JOptionPane.OK_CANCEL_OPTION);

		if (accion != JOptionPane.OK_OPTION) {
			return false;
		}

		String pwd = new String(pwdAdmin.getPassword());
		if (!usuarioDao.comprobarAdmin(pwd)) {
			JOptionPane.showMessageDialog(view, "Password de administrador incorrecto.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private int codigoOrigen() {
		Empleado origen = view.getOrigenSeleccionado();
		return origen == null ? 0 : origen.getCodigo();
	}

	private int codigoDestino() {
		Empleado destino = view.getDestinoSeleccionado();
		return destino == null ? 0 : destino.getCodigo();
	}
}
