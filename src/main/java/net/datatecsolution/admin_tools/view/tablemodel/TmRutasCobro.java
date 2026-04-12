package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.RutaCobro;

import java.util.ArrayList;
import java.util.List;

public class TmRutasCobro extends TablaModelo {
	
	private String []columnNames={"Codigo","Descripcion","Observacion"};
	private List<RutaCobro> rutas = new ArrayList<RutaCobro>();
	
	public void agregar(RutaCobro rut) {
		rutas.add(rut);
        fireTableDataChanged();
    }
	
	public RutaCobro getRuta(int index){
		
		return rutas.get(index);
		
	}
	
	public void cambiarRuta(int index,RutaCobro r){
		rutas.set(index, r);
	}
 
    public void eliminar(int rowIndex) {
    	rutas.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	rutas.clear();
        fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return rutas.size();
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
            return rutas.get(rowIndex).getCodigo();
        case 1:
            return rutas.get(rowIndex).getDescripcion();
        case 2:
            return rutas.get(rowIndex).getObser();
        
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
	        RutaCobro r = rutas.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	r.setCodigo((Integer) value);
	            case 1:
	            	r.setDescripcion((String) value);
	            case 2:
	            	r.setObser((String) value);
	    
	        }
	        fireTableCellUpdated(rowIndex, columnIndex);
	    }

}
