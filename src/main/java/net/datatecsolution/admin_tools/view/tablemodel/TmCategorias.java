package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Categoria;

import java.util.ArrayList;
import java.util.List;

public class TmCategorias extends TablaModelo {
	
	private String []columnNames={"Id","Descripcion","Observacion"};
	private List<Categoria> marcas = new ArrayList<Categoria>();
	
	public void agregarMarca(Categoria marca) {
		marcas.add(marca);
        fireTableDataChanged();
    }
	
	public Categoria getMarca(int index){
		
		return marcas.get(index);
		
	}
	
	public void cambiarMarca(int index,Categoria marca){
		marcas.set(index, marca);
	}
 
    public void eliminarMarca(int rowIndex) {
    	marcas.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiarMarcas() {
    	marcas.clear();
        fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return marcas.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		switch (columnIndex) {
        case 0:
            return marcas.get(rowIndex).getId();
        case 1:
            return marcas.get(rowIndex).getDescripcion();
        case 2:
            return marcas.get(rowIndex).getObservacion();
        
        default:
            return null;
		}
	}
	
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
        
        
        /*switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return String.class;
        case 2:
        	return String.class;
        
        default:
            return null;
            }*/
    }
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
	
	 @Override
	    public void setValueAt(Object value, int rowIndex, int columnIndex) {
	        Categoria marca = marcas.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	marca.setId((Integer) value);
	            case 1:
	            	marca.setDescripcion((String) value);
	            case 2:
	            	marca.setObservacion((String) value);
	    
	        }
	        fireTableCellUpdated(rowIndex, columnIndex);
	    }

}
