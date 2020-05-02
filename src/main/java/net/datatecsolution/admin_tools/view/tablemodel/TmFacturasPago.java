package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Factura;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class TmFacturasPago extends AbstractTableModel {
	final private String []columnNames= {
			"No Factura","Fecha", "Total","Estado","Pagar Factura?"
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
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		Factura detalle = facturas.get(rowIndex);
		boolean v=(boolean) value;
		switch(columnIndex){
		
		case 4:
			
			detalle.setDeseaPagar(v);// Double.parseDouble(v));
			fireTableCellUpdated(rowIndex, columnIndex);
				//fireTableDataChanged();
			break;
		
	}
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
			return facturas.get(rowIndex).getTotal();
		case 3:
			return facturas.get(rowIndex).getEstado();
		case 4:
			return facturas.get(rowIndex).getDeseaPagar();
		
		
		default:
				return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		switch (columnIndex) {
        case 0:
            return String.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        case 3:
            return String.class;
        default:
            return Boolean.class;

    }
	//return String.class;
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
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul=false;
		/*if(columnIndex==0)
			resul= true;*/
		
		if(columnIndex==4)
			resul=true;
		/*if(columnIndex==6)
			resul=true;*/
	
		
		
		return resul;
	}
	public void cambiarEstado(boolean estado) {
		// TODO Auto-generated method stub
		for(int x=0;x<this.facturas.size();x++){
			facturas.get(x).setDeseaPagar(estado);
		}
		fireTableDataChanged();
	}
	public List<Factura> getDetalles() {
		// TODO Auto-generated method stub
		return facturas;
	}
	public Factura getDetalle(int x) {
		// TODO Auto-generated method stub
		return facturas.get(x);
	}
	public boolean hayPagos() {
		boolean resul=false;
		
		for(int x=0;x<this.facturas.size();x++){
			if(facturas.get(x).getDeseaPagar()==true){
				resul=true;
			}
		}
		
		return resul;
	}

}
