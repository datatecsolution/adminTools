package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Requisicion;

import java.util.ArrayList;
import java.util.List;

public class TmRequisicionesEncabezados extends TablaModelo {
	
	private String []columnNames={"codigo","Fecha","De","Para","Total","Estado"};
	
	private List<Requisicion> requisiciones=new ArrayList<Requisicion>();

	public TmRequisicionesEncabezados() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return requisiciones.size();
	}
	public Requisicion getRequisicion(int row){
		return requisiciones.get(row);
	}
	public void addRequisicion(Requisicion requi){
		 requisiciones.add(requi);
		 fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		switch (columnIndex){
		case 0:
			return requisiciones.get(rowIndex).getNoRequisicion();
		case 1:
			return requisiciones.get(rowIndex).getFechaCompra();
		case 2:
			return requisiciones.get(rowIndex).getDepartamentoOrigen().getDescripcion();
		case 3:
			return requisiciones.get(rowIndex).getDepartamentoDestino().getDescripcion();
		case 4:
			return requisiciones.get(rowIndex).getTotal();
		case 5:
			return requisiciones.get(rowIndex).getEstado();
		default:
			return null;
		}
		
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }

	public void limpiar() {
		requisiciones.clear();
		fireTableDataChanged();
		
	}

}
