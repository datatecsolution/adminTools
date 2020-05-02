package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.CuentaBanco;

import java.util.ArrayList;
import java.util.List;

public class TmCuentasBanco extends TablaModelo {
	private String []columnNames={"Fecha","Descripcion","Referencia","Debito","Credito","Saldo"};
	private List<CuentaBanco> cuentas = new ArrayList<CuentaBanco>();
	
	public void agregar(CuentaBanco c) {
		cuentas.add(c);
        fireTableDataChanged();
    }
	
	public CuentaBanco getCuenta(int index){
		
		return cuentas.get(index);
		
	}
	
	
	public void eliminar(int rowIndex) {
    	cuentas.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	cuentas.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cuentas.size();
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
				return cuentas.get(rowIndex).getFecha();
		case 1: 
			return cuentas.get(rowIndex).getDescripcion();
		case 2: 
			return cuentas.get(rowIndex).getReferencia();
		case 3:
			return cuentas.get(rowIndex).getDebito().toString();
		case 4:
			return cuentas.get(rowIndex).getCredito().toString();
		case 5:
			return cuentas.get(rowIndex).getSaldo().toString();
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
