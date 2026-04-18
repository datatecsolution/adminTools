package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepVentasArtVendedor;
import net.datatecsolution.admin_tools.view.ViewListaArticulo;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CtlFiltroRepVentasArtVendedor implements ActionListener, KeyListener {
	private final ViewFiltroRepVentasArtVendedor view;
	private Articulo articuloReporte = null;
	private Empleado vendedorReporte = null;
	private final DetalleFacturaDao detalleFacturaDao;
	private final CajaDao cajasDao;
	private final List<DetalleFactura> detalles = new ArrayList<DetalleFactura>();
	private List<Caja> listCajas = null;
	private String date1;
	private String date2;
	private String date1Display;
	private String date2Display;

	public CtlFiltroRepVentasArtVendedor(ViewFiltroRepVentasArtVendedor v) {
		view = v;
		view.conectarCtl(this);

		cajasDao = new CajaDao();
		detalleFacturaDao = new DetalleFacturaDao();
		listCajas = cajasDao.todosList();

		Date horaLocal = new Date();
		view.getBuscar1().setDate(horaLocal);
		view.getBuscar2().setDate(horaLocal);

		view.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando = e.getActionCommand();

		switch (comando) {
			case "GENERAR":
				if (articuloReporte == null) {
					JOptionPane.showMessageDialog(view, "Debe seleccionar un articulo. Utilice F1 para buscar.", "Error validacion.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (vendedorReporte == null) {
					JOptionPane.showMessageDialog(view, "Debe seleccionar un vendedor. Utilice F2 para buscar.", "Error validacion.", JOptionPane.ERROR_MESSAGE);
					return;
				}

				detalles.clear();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd-MM-yyyy");
				date1 = sdf.format(this.view.getBuscar1().getDate());
				date2 = sdf.format(this.view.getBuscar2().getDate());
				date1Display = sdfDisplay.format(this.view.getBuscar1().getDate());
				date2Display = sdfDisplay.format(this.view.getBuscar2().getDate());

				for (int x = 0; x < this.listCajas.size(); x++) {
					detalleFacturaDao.getDetallesFactura(listCajas.get(x), detalles, articuloReporte, vendedorReporte, date1, date2);
				}
				try {
					AbstractJasperReports.createReportVentasArticuloVendedor(
							ConexionStatic.getPoolConexion().getConnection(),
							detalles, articuloReporte, vendedorReporte, date1Display, date2Display);

					AbstractJasperReports.showViewer(view);
				} catch (SQLException eee) {
					eee.printStackTrace();
				}
				break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_F1:
				buscarArticulo();
				break;
			case KeyEvent.VK_F2:
				buscarEmpleado();
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	private void buscarArticulo() {
		ViewListaArticulo viewListaArticulo = new ViewListaArticulo(view);
		CtlArticuloBuscar ctlArticulo = new CtlArticuloBuscar(viewListaArticulo);

		viewListaArticulo.pack();
		ctlArticulo.view.getTxtBuscar().setText("");
		ctlArticulo.view.getTxtBuscar().selectAll();
		viewListaArticulo.conectarControladorBuscar(ctlArticulo);

		boolean resul = ctlArticulo.buscarArticulo(view);

		if (resul) {
			articuloReporte = ctlArticulo.getArticulo();
			view.getTxtArticulo().setText(articuloReporte.getArticulo());
		}
	}

	private void buscarEmpleado() {
		ViewListaEmpleados viewBuscarEmpleado = new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado = new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado = ctBuscarEmpleado.buscar();

		if (resultado) {
			vendedorReporte = ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtVendedor().setText(vendedorReporte.toString());
		} else {
			vendedorReporte = null;
		}
	}
}
