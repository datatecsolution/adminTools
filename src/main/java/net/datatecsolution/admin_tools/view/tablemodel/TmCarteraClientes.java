package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.ClienteCartera;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * US-127 — modelo de la tabla de clientes de la pantalla de transferencia de
 * cartera.
 *
 * Extiende {@link AbstractTableModel} y no {@code TablaModelo} porque esta
 * pantalla NO pagina: carga la cartera completa del vendedor de una sola vez
 * para que "marcar todos" signifique realmente todos y no solo la pagina
 * visible.
 *
 * La primera columna es una casilla editable; el resto es de solo lectura.
 *
 * @author jdmayorga
 */
public class TmCarteraClientes extends AbstractTableModel {

	public static final int COL_SELECCION = 0;
	public static final int COL_CODIGO = 1;
	public static final int COL_CLIENTE = 2;
	public static final int COL_RTN = 3;
	public static final int COL_TIPO = 4;
	public static final int COL_ROL = 5;
	public static final int COL_SALDO = 6;

	private final String[] columnNames = { "", "Codigo", "Cliente", "RTN", "Tipo", "Rol actual", "Saldo" };

	private final List<ClienteCartera> clientes = new ArrayList<ClienteCartera>();

	/** Empleado consultado; define que dice la columna "Rol actual". */
	private int codigoEmpleado;

	public void cargar(List<ClienteCartera> nuevos, int codigoEmpleado) {
		this.codigoEmpleado = codigoEmpleado;
		clientes.clear();
		if (nuevos != null) {
			clientes.addAll(nuevos);
		}
		fireTableDataChanged();
	}

	public void limpiar() {
		clientes.clear();
		fireTableDataChanged();
	}

	public List<ClienteCartera> getClientes() {
		return clientes;
	}

	public ClienteCartera getCliente(int index) {
		return clientes.get(index);
	}

	/** Marca o desmarca todas las filas de un tiron. */
	public void marcarTodos(boolean marcar) {
		for (ClienteCartera c : clientes) {
			c.setSeleccionado(marcar);
		}
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getRowCount() {
		return clientes.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ClienteCartera c = clientes.get(rowIndex);
		switch (columnIndex) {
		case COL_SELECCION:
			return c.isSeleccionado();
		case COL_CODIGO:
			return c.getCodigo();
		case COL_CLIENTE:
			return c.getNombre();
		case COL_RTN:
			return c.getRtn();
		case COL_TIPO:
			return c.getTipoCliente() == 2 ? "Credito" : "Contado";
		case COL_ROL:
			return c.rolRespectoA(codigoEmpleado);
		case COL_SALDO:
			return c.getSaldo();
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object valor, int rowIndex, int columnIndex) {
		if (columnIndex == COL_SELECCION && valor instanceof Boolean) {
			clientes.get(rowIndex).setSeleccionado((Boolean) valor);
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COL_SELECCION:
			return Boolean.class;
		case COL_CODIGO:
			return Integer.class;
		default:
			return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == COL_SELECCION;
	}
}
