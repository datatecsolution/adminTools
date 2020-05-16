package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.EntradaCaja;

import java.util.ArrayList;
import java.util.List;


public class TmEntradas extends TablaModelo {
	
	
	private String []columnNames={"Id","Concepto","Fecha","Cantidad","Estado"};
	private List<EntradaCaja> entradas = new ArrayList<EntradaCaja>();
	
	
	public void agregar(EntradaCaja a) {
		entradas.add(a);
        fireTableDataChanged();
    }
	
	public EntradaCaja getEntrada(int index){
		//proveedores.
		return entradas.get(index);
		
	}
	 public void eliminar(int rowIndex) {
	    	entradas.remove(rowIndex);
	        fireTableDataChanged();
	    }
	 
	 public void limpiar() {
	    	entradas.clear();
	        fireTableDataChanged();
	    }

	public TmEntradas() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return entradas.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		switch(columnIndex){
			case 0:
				return entradas.get(rowIndex).getCodigoEntrada();
			case 1:
				return entradas.get(rowIndex).getConcepto();
			case 2:
				return entradas.get(rowIndex).getFecha();
			case 3:
				return entradas.get(rowIndex).getCantidad();
			case 4:
				return entradas.get(rowIndex).getEstado();
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
