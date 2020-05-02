package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.FacturaCompra;

import java.util.ArrayList;
import java.util.List;

public class TablaModeloFacturasCompra extends TablaModelo {
	
	final private String []columnNames= {
			"No Ingreso","Fecha","No Factura","Proveedor", "Telefono", "Total","Estado"
		};
	
	
	private List<FacturaCompra> facturas=new ArrayList<FacturaCompra>();
	
	public FacturaCompra getFactura(int row){
		return facturas.get(row);
	}
	public void agregarFactura(FacturaCompra f){
		facturas.add(f);
		fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return facturas.size();
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
			return facturas.get(rowIndex).getNoCompra();
		case 1:
			return facturas.get(rowIndex).getFechaCompra();
		case 2:
			return facturas.get(rowIndex).getIdFactura();
		case 3:
			return facturas.get(rowIndex).getProveedor().getNombre();
		case 4:
			return facturas.get(rowIndex).getProveedor().getTelefono();
		case 5:
			return facturas.get(rowIndex).getTotal();
		case 6:
			return facturas.get(rowIndex).getEstado();
		
		default:
				return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	public void limpiarFacturas() {
		// TODO Auto-generated method stub
		facturas.clear();
		fireTableDataChanged();
	}
	public void eliminarFactura(int row) {
		// TODO Auto-generated method stub
		facturas.remove(row);
		fireTableDataChanged();
	}

}
