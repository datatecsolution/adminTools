package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.MovimientoCartera;
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
 * US-127 — controlador de la transferencia de cartera de clientes.
 *
 * La venta y la cobranza son movimientos independientes (ver
 * {@link MovimientoCartera}); las reglas de que se mueve viven en
 * {@link TransferenciaCarteraService} y aca solo se orquesta la pantalla, se
 * pide la confirmacion y se llama al DAO.
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
		case "CAMBIO_CRITERIO":
			// Cambio en un origen o en un checkbox: cambia que clientes son
			// candidatos, hay que volver a consultar.
			cargarCartera(true);
			break;

		case "CAMBIO_DESTINO":
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
	 * @param avisarSiVacia avisar cuando la busqueda no trae clientes. Se
	 *                      avisa al cambiar el criterio (el usuario espera ver
	 *                      algo), pero no al recargar despues de transferir:
	 *                      ahi la lista vacia es justamente el resultado
	 *                      esperado y el aviso pisaria al de exito.
	 */
	private void cargarCartera(boolean avisarSiVacia) {
		MovimientoCartera venta = view.getMovimientoVenta();
		MovimientoCartera cobro = view.getMovimientoCobro();

		int origenVenta = venta.origenBuscable();
		int origenCobro = cobro.origenBuscable();

		if (origenVenta <= 0 && origenCobro <= 0) {
			view.getModeloClientes().cargar(null, 0, 0);
			actualizarResumen();
			return;
		}

		List<ClienteCartera> cartera = clienteDao.carteraParaTransferencia(origenVenta, origenCobro);
		view.getModeloClientes().cargar(cartera, origenVenta, origenCobro);
		actualizarResumen();

		if (cartera.isEmpty() && avisarSiVacia) {
			JOptionPane.showMessageDialog(view,
					"Los empleados de origen seleccionados no tienen clientes asignados.",
					"Sin cartera", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void actualizarResumen() {
		view.setResumen(servicio.resumenSeleccion(
				view.getModeloClientes().getClientes(),
				view.getMovimientoVenta(),
				view.getMovimientoCobro()));
	}

	private void transferir() {
		MovimientoCartera venta = view.getMovimientoVenta();
		MovimientoCartera cobro = view.getMovimientoCobro();

		List<ClienteCartera> marcados = servicio.seleccionados(view.getModeloClientes().getClientes());

		String error = servicio.validar(venta, cobro, marcados);
		if (error != null) {
			JOptionPane.showMessageDialog(view, error, "No se puede transferir", JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<ClienteCartera> cambianVenta = servicio.afectadosVenta(marcados, venta);
		List<ClienteCartera> cambianCobro = servicio.afectadosCobro(marcados, cobro);
		List<ClienteCartera> todos = servicio.afectados(marcados, venta, cobro);

		if (!confirmar(venta, cobro, cambianVenta, cambianCobro, todos)) {
			return;
		}

		boolean resultado = clienteDao.transferirCartera(
				servicio.codigos(cambianVenta), venta,
				servicio.codigos(cambianCobro), cobro);

		if (resultado) {
			JOptionPane.showMessageDialog(view,
					"Transferencia realizada sobre " + todos.size() + " clientes:\n"
							+ "  Venta: " + cambianVenta.size() + "\n"
							+ "  Cobro: " + cambianCobro.size(),
					"Transferencia realizada", JOptionPane.INFORMATION_MESSAGE);
			// Se recarga para que la tabla refleje lo que quedo en la base:
			// si solo se movio uno de los dos roles, los clientes siguen
			// apareciendo por el rol que no se toco.
			cargarCartera(false);
		} else {
			JOptionPane.showMessageDialog(view,
					"No se pudo completar la transferencia. No se modifico ningun cliente.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Dialogo de confirmacion con el detalle de cada movimiento y password de
	 * administrador, igual que la transferencia de saldo entre cuentas: es una
	 * operacion masiva sin deshacer.
	 */
	private boolean confirmar(MovimientoCartera venta, MovimientoCartera cobro,
			List<ClienteCartera> cambianVenta, List<ClienteCartera> cambianCobro,
			List<ClienteCartera> todos) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new JLabel("Se van a aplicar los siguientes movimientos:"));
		panel.add(Box.createRigidArea(new Dimension(0, 10)));

		if (venta.esUtilizable()) {
			panel.add(new JLabel("VENTA:  " + view.nombreOrigenVenta() + "  ->  " + view.nombreDestinoVenta()));
			panel.add(new JLabel("    " + cambianVenta.size() + " clientes"
					+ "   (saldo Lps " + servicio.saldoTotal(cambianVenta) + ")"));
		} else {
			panel.add(new JLabel("VENTA:  sin cambios"));
		}

		if (cobro.esUtilizable()) {
			panel.add(new JLabel("COBRO:  " + view.nombreOrigenCobro() + "  ->  " + view.nombreDestinoCobro()));
			panel.add(new JLabel("    " + cambianCobro.size() + " clientes"
					+ "   (saldo Lps " + servicio.saldoTotal(cambianCobro) + ")"));
		} else {
			panel.add(new JLabel("COBRO:  sin cambios"));
		}

		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(new JLabel("Clientes afectados en total: " + todos.size()));
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
}
