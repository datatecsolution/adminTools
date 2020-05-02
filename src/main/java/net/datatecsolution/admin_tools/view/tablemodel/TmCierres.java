package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.CierreCaja;

import java.util.ArrayList;
import java.util.List;

public class TmCierres extends TablaModelo {
	private String []columnNames={"Fecha","Cajero","Turno","T Efectivo","T Tarjeta","T Credito","ISV15","ISV18","Total"};
	private List<CierreCaja> cierres = new ArrayList<CierreCaja>();
	
	public void agregar(CierreCaja cierre) {
		cierres.add(cierre);
        fireTableDataChanged();
    }
	
	public CierreCaja getCierreCaja(int index){
		
		return cierres.get(index);
		
	}
	
	public void cambiarCierreCaja(int index,CierreCaja cierre){
		cierres.set(index, cierre);
		fireTableDataChanged();
	}
	
	public void eliminar(int rowIndex) {
    	cierres.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	cierres.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cierres.size();
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
				return cierres.get(rowIndex).getFecha();
		case 1: 
			return cierres.get(rowIndex).getUsuario();
		case 2:
			return cierres.get(rowIndex).getTurno();
		case 3: 
			return cierres.get(rowIndex).getEfectivo().toString();
		case 4:
			return cierres.get(rowIndex).getTarjeta().toString();
		case 5:
			return cierres.get(rowIndex).getCredito().toString();
		case 6:
			return cierres.get(rowIndex).getIsv15().toString();
		case 7:
			return cierres.get(rowIndex).getIsv18().toString();
		case 8:
			return cierres.get(rowIndex).getTotal().toString();
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

}
