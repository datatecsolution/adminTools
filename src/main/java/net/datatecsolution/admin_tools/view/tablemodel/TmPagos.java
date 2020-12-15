package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.ReciboPago;

import java.util.ArrayList;
import java.util.List;


public class TmPagos extends TablaModelo {
	final private String []columnNames= {
			"Fecha","No Recibo", "Cliente", "Total","ref"
		};
	private List<ReciboPago> pagos=new ArrayList<ReciboPago>();
	
	public TmPagos(){
		
	}

	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return pagos.size();
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
			return pagos.get(rowIndex).getFecha();
		case 1:
			return pagos.get(rowIndex).getNoRecibo();
		case 2:
			return pagos.get(rowIndex).getCliente().getNombre();
		case 3:
			return pagos.get(rowIndex).getTotal();
		case 4:
			return pagos.get(rowIndex).getRef();
		default:
				return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	
	public void limpiar(){
		pagos.clear();
		fireTableDataChanged();
	}
	public void elimiar(int row){
		pagos.remove(row);
		fireTableDataChanged();
	}
	
	public ReciboPago getRecibo(int row){
		return pagos.get(row);
		
	}
	
	public void agregarPago(ReciboPago r){
		pagos.add(r);
		fireTableDataChanged();
	}

}
