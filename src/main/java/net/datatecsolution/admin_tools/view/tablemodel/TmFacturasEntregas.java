package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Factura;

import java.util.ArrayList;
import java.util.List;

public class TmFacturasEntregas  extends TablaModelo {
	final private String []columnNames= {
			"# fact","Fecha","Tipo","Cliente", "Total"
		};
	private List<Factura> facturas=new ArrayList<Factura>();
	
	
	
	public Factura getFactura(int row){
		return facturas.get(row);
	}
	public void agregarFactura(Factura f){
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
			return facturas.get(rowIndex).getIdFactura();
		case 1:
			return facturas.get(rowIndex).getFecha();
		case 2:
				if(facturas.get(rowIndex).getTipoFactura()==1){
					return "Contado";
				}else{
					return "Credito";
				}
					
			//return facturas.get(rowIndex).getTipoFactura();
		case 3:
			return facturas.get(rowIndex).getCliente().getNombre();
		case 4:
			return facturas.get(rowIndex).getTotal();
	
		
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
	/**
	 * @return the facturas
	 */
	public List<Factura> getFacturas() {
		return facturas;
	}


}
