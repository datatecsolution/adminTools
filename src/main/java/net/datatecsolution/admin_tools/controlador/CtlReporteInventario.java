package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.FilaReporteInventario;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.modelo.dao.DepartamentoDao;
import net.datatecsolution.admin_tools.modelo.dao.ReporteInventarioDao;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;
import net.datatecsolution.admin_tools.view.ViewReporteInventario;
import net.datatecsolution.admin_tools.view.tablemodel.TmReporteInventario;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class CtlReporteInventario implements ActionListener {

	private final ViewReporteInventario view;
	private final ReporteInventarioDao reporteDao;
	private final ArticuloDao articuloDao;
	private final DepartamentoDao bodegaDao;

	public CtlReporteInventario(ViewReporteInventario view) {
		this.view = view;
		this.reporteDao = new ReporteInventarioDao();
		this.articuloDao = new ArticuloDao();
		this.articuloDao.setEstado(1);
		this.bodegaDao = new DepartamentoDao();

		cargarBodegas();
		view.actualizarTotal();
		view.conectarControlador(this);
	}

	private void cargarBodegas() {
		Vector<Departamento> bodegas = bodegaDao.todos();
		view.cargarBodegas(bodegas);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null) return;
		switch (cmd) {
			case "BODEGA_CAMBIA":
			case "FILTRO_CAMBIA":
				recalcularFilas();
				break;
			case "AGREGAR_POR_TEXTO":
				agregarPorTexto();
				break;
			case "BUSCAR_ARTICULO":
				abrirBuscadorArticulos();
				break;
			case "QUITAR":
				quitarFilaSeleccionada();
				break;
			case "LIMPIAR":
				limpiar();
				break;
			case "EXPORTAR":
				exportar();
				break;
		}
	}

	private void agregarPorTexto() {
		Departamento bodega = view.getBodegaSeleccionada();
		if (bodega == null) {
			JOptionPane.showMessageDialog(view, "Seleccione una bodega.", "Reporte de inventario", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String texto = view.getTxtBuscar().getText().trim();
		if (texto.isEmpty()) {
			view.getTxtBuscar().requestFocusInWindow();
			return;
		}

		articuloDao.setMyBodega(bodega);
		Articulo art = null;
		if (AbstractJasperReports.isNumber(texto)) {
			art = articuloDao.buscarArticulo(Integer.parseInt(texto));
		}
		if (art == null) {
			art = articuloDao.buscarArticuloBarraCod(texto);
		}
		if (art == null || art.getId() <= 0) {
			JOptionPane.showMessageDialog(view, "Artículo no encontrado.", "Reporte de inventario", JOptionPane.INFORMATION_MESSAGE);
			view.getTxtBuscar().selectAll();
			view.getTxtBuscar().requestFocusInWindow();
			return;
		}
		agregarArticulo(art, bodega);
		view.getTxtBuscar().setText("");
		view.getTxtBuscar().requestFocusInWindow();
	}

	private void abrirBuscadorArticulos() {
		Departamento bodega = view.getBodegaSeleccionada();
		if (bodega == null) {
			JOptionPane.showMessageDialog(view, "Seleccione una bodega.", "Reporte de inventario", JOptionPane.WARNING_MESSAGE);
			return;
		}
		ViewListaArticulo viewLista = new ViewListaArticulo(view);
		CtlArticuloBuscar ctl = new CtlArticuloBuscar(viewLista);
		viewLista.pack();
		viewLista.conectarControladorBuscar(ctl);
		ctl.view.getTxtBuscar().requestFocusInWindow();

		boolean res = ctl.buscarArticulo(null);
		if (res) {
			Articulo art = ctl.getArticulo();
			if (art != null && art.getId() > 0) {
				agregarArticulo(art, bodega);
			}
		}
		viewLista.dispose();
	}

	private void agregarArticulo(Articulo art, Departamento bodega) {
		TmReporteInventario modelo = view.getModelo();
		if (modelo.contiene(art.getId())) {
			JOptionPane.showMessageDialog(view, "El artículo ya está en la lista.", "Reporte de inventario", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		FilaReporteInventario fila = reporteDao.consultarPorArticulo(art.getId(), bodega.getId(), view.isSoloComprasVentas());
		if (fila == null) {
			JOptionPane.showMessageDialog(view, "No se pudo consultar el artículo.", "Reporte de inventario", JOptionPane.WARNING_MESSAGE);
			return;
		}
		modelo.agregar(fila);
		view.actualizarTotal();
	}

	private void recalcularFilas() {
		Departamento bodega = view.getBodegaSeleccionada();
		if (bodega == null) return;
		TmReporteInventario modelo = view.getModelo();
		List<FilaReporteInventario> filas = modelo.getFilas();
		for (int i = 0; i < filas.size(); i++) {
			FilaReporteInventario actual = filas.get(i);
			FilaReporteInventario nueva = reporteDao.consultarPorArticulo(actual.getCodigoArticulo(), bodega.getId(), view.isSoloComprasVentas());
			if (nueva != null) {
				modelo.reemplazarFila(i, nueva);
			}
		}
		view.actualizarTotal();
	}

	private void quitarFilaSeleccionada() {
		int row = view.getTabla().getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(view, "Seleccione una fila para quitar.", "Reporte de inventario", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		view.getModelo().quitar(row);
		view.actualizarTotal();
	}

	private void limpiar() {
		if (view.getModelo().getRowCount() == 0) return;
		int op = JOptionPane.showConfirmDialog(view, "¿Quitar todos los artículos?", "Reporte de inventario", JOptionPane.YES_NO_OPTION);
		if (op == JOptionPane.YES_OPTION) {
			view.getModelo().limpiar();
			view.actualizarTotal();
		}
	}

	private void exportar() {
		Departamento bodega = view.getBodegaSeleccionada();
		if (bodega == null) {
			JOptionPane.showMessageDialog(view, "Seleccione una bodega.", "Reporte de inventario", JOptionPane.WARNING_MESSAGE);
			return;
		}
		List<FilaReporteInventario> filas = view.getModelo().getFilas();
		if (filas.isEmpty()) {
			JOptionPane.showMessageDialog(view, "Agregue al menos un artículo.", "Reporte de inventario", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		try {
			AbstractJasperReports.createReporteInventarioSeleccion(
				ConexionStatic.getPoolConexion().getConnection(),
				filas,
				bodega.getDescripcion(),
				view.getModelo().getTotalEfectivo(),
				ConexionStatic.getUsuarioLogin().getUser());
			AbstractJasperReports.showViewer(view);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view, "Error al generar el reporte: " + ex.getMessage(), "Reporte de inventario", JOptionPane.ERROR_MESSAGE);
		}
	}
}
