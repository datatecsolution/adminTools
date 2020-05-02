package net.datatecsolution.admin_tools.view.tablemodel;

import java.util.ArrayList;
import java.util.List;


import net.datatecsolution.admin_tools.modelo.SalidaCaja;


public class TmSalidas extends TablaModelo {
	
	
	private String []columnNames={"Id","Concepto","Entregado a","Fecha","Cantidad","Estado"};
	private List<SalidaCaja> salidas = new ArrayList<SalidaCaja>();
	
	
	public void agregar(SalidaCaja a) {
		salidas.add(a);
        fireTableDataChanged();
    }
	
	public SalidaCaja getSalida(int index){
		//proveedores.
		return salidas.get(index);
		
	}
	 public void eliminar(int rowIndex) {
	    	salidas.remove(rowIndex);
	        fireTableDataChanged();
	    }
	 
	 public void limpiar() {
	    	salidas.clear();
	        fireTableDataChanged();
	    }

	public TmSalidas() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return salidas.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		switch(columnIndex){
			case 0:
				return salidas.get(rowIndex).getCodigoSalida();
			case 1:
				return salidas.get(rowIndex).getConcepto();
			case 2: 
				return salidas.get(rowIndex).getEmpleado().toString();
			case 3:
				return salidas.get(rowIndex).getFecha();
			case 4:
				return salidas.get(rowIndex).getCantidad();
			case 5:
				return salidas.get(rowIndex).getEstado();
			default:
	            return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
