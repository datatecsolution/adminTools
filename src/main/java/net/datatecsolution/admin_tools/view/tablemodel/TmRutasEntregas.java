package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.RutaEntrega;

import java.util.ArrayList;
import java.util.List;

public class TmRutasEntregas extends TablaModelo {
	private String []columnNames={"codigo","Vendedor","Fecha","Estado"};
	private List<RutaEntrega> rutas = new ArrayList<RutaEntrega>();
	
	public void agregar(RutaEntrega c) {
		rutas.add(c);
        fireTableDataChanged();
    }
	
	public RutaEntrega getRuta(int index){
		
		return rutas.get(index);
		
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
			return rutas.get(rowIndex).getIdRuta();
		case 1:
			return rutas.get(rowIndex).getVendedor().getNombre();
		case 2:
			return rutas.get(rowIndex).getFecha();
		case 3:
			return rutas.get(rowIndex).getEstado();
		default:
            return null;
		}
	}
	public void add(RutaEntrega una){
		 rutas.add(una);
		 fireTableDataChanged();
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

	public void cambiarRuta(int index, RutaEntrega myRuta) {
		// TODO Auto-generated method stub
		this.rutas.set(index, myRuta);
		 fireTableDataChanged();
	}

}
