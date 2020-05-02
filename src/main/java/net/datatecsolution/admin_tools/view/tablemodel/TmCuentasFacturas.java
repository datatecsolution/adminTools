package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.CuentaFactura;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TmCuentasFacturas  extends TablaModelo {
	final private String []columnNames= {
			"No Factura","Fecha Venc","Fecha Ultimo Pago","Cliente", "Telefono", "Saldo Factura"
		};
	private List<CuentaFactura> cuentas=new ArrayList<CuentaFactura>();
	
	
	
	public CuentaFactura getCuenta(int row){
		return cuentas.get(row);
	}
	public void agregarCuenta(CuentaFactura c){
		cuentas.add(c);
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		//String date1 = sdf.format(this.view.getDcFecha1().getDate());
		
		switch (columnIndex) {
		case 0:
			return cuentas.get(rowIndex).getNoFactura();
		case 1:
			return sdf.format(cuentas.get(rowIndex).getFechaVenc());
		case 2:
			if(cuentas.get(rowIndex).getUltimoPago()!=null) return sdf.format(cuentas.get(rowIndex).getUltimoPago().getFecha()); else return "No tiene pago";
		case 3:
			return cuentas.get(rowIndex).getCliente().getNombre();
		case 4:
			return cuentas.get(rowIndex).getCliente().getTelefono();
		case 5:
			return cuentas.get(rowIndex).getSaldo();
		
		default:
				return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	public void limpiarCuentas() {
		// TODO Auto-generated method stub
		cuentas.clear();
		fireTableDataChanged();
	}
	public void eliminarCuenta(int row) {
		// TODO Auto-generated method stub
		cuentas.remove(row);
		fireTableDataChanged();
	}


}
