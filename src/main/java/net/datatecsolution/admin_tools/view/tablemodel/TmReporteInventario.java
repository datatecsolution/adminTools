package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.FilaReporteInventario;

import java.util.ArrayList;
import java.util.List;

public class TmReporteInventario extends TablaModelo {

	private static final long serialVersionUID = 1L;
	private final String[] columnNames = {
		"No", "Articulo", "Cód. Articulo", "Total Ingreso", "Total Salidas", "Saldo", "Efectivo Saldo"
	};
	private final List<FilaReporteInventario> filas = new ArrayList<>();

	public void agregar(FilaReporteInventario fila) {
		filas.add(fila);
		fireTableDataChanged();
	}

	public void quitar(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= filas.size()) return;
		filas.remove(rowIndex);
		fireTableDataChanged();
	}

	public void limpiar() {
		filas.clear();
		fireTableDataChanged();
	}

	public FilaReporteInventario get(int rowIndex) {
		return filas.get(rowIndex);
	}

	public List<FilaReporteInventario> getFilas() {
		return filas;
	}

	public void reemplazarFila(int rowIndex, FilaReporteInventario fila) {
		filas.set(rowIndex, fila);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	public boolean contiene(int codigoArticulo) {
		for (FilaReporteInventario f : filas) {
			if (f.getCodigoArticulo() == codigoArticulo) return true;
		}
		return false;
	}

	public double getTotalEfectivo() {
		double total = 0;
		for (FilaReporteInventario f : filas) {
			total += f.getEfectivoSaldo();
		}
		return total;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getRowCount() {
		return filas.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		FilaReporteInventario f = filas.get(rowIndex);
		switch (columnIndex) {
			case 0: return rowIndex + 1;
			case 1: return f.getArticulo();
			case 2: return f.getCodArticulo();
			case 3: return f.getTotalIngreso();
			case 4: return f.getTotalSalida();
			case 5: return f.getSaldo();
			case 6: return f.getEfectivoSaldo();
			default: return "";
		}
	}
}
