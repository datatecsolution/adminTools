package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Caja;

import java.util.ArrayList;
import java.util.List;

public class TmCajas extends TablaModelo {
	private String []columnNames={"codigo","descricion","Ubicacion"};
	private List<Caja> cajas = new ArrayList<Caja>();
	
	public void agregar(Caja c) {
		cajas.add(c);
        fireTableDataChanged();
    }
	
	public Caja getCaja(int index){
		
		return cajas.get(index);
		
	}
	
	public void cambiarUsuario(int index,Caja c){
		cajas.set(index, c);
		fireTableDataChanged();
	}
	
	public void eliminar(int rowIndex) {
    	cajas.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	cajas.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cajas.size();
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
			return cajas.get(rowIndex).getCodigo();
		case 1:
			return cajas.get(rowIndex).getDescripcion();
		case 2:
			return cajas.get(rowIndex).getDetartamento().getDescripcion();
		default:
            return null;
		}
	}
	public void add(Caja una){
		 cajas.add(una);
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

}
