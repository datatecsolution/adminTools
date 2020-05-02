package net.datatecsolution.admin_tools.view.tablemodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

public class TmPrecios extends AbstractTableModel {
	
	private String []columnNames={"Tipo Precio","Precio"};
	private List<PrecioArticulo> precios = new ArrayList<PrecioArticulo>();
	
	public void agregarPrecio(PrecioArticulo precio) {
		precios.add(precio);
        fireTableDataChanged();
    }
	public List<PrecioArticulo> getPrecios(){
		return precios;
	}
	public void eliminar(int rowIndex) {
    	precios.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	precios.clear();
        fireTableDataChanged();
    }

	public TmPrecios() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return precios.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
        case 0:
            return precios.get(rowIndex).getDescripcion();
        case 1:
        	
        	if(precios.get(rowIndex).getPrecio().doubleValue()>0){
        		return precios.get(rowIndex).getPrecio().setScale(2, BigDecimal.ROUND_HALF_EVEN);
			}else
				return null;
            
               
        default:
            return null;
		}
	}
	 @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	 
	 @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		PrecioArticulo precio = precios.get(rowIndex);
		String v=(String) value;
        switch (columnIndex) {
            case 0:
            	precio.setDecripcion(v);// .setId((Integer) value);
            case 1:
            	precio.setPrecio(new BigDecimal(v)); //.get(rowIndex).setCantidad(new BigDecimal(v));// Double.parseDouble(v));
    			fireTableCellUpdated(rowIndex, columnIndex);
           
    
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
	 @Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul=false;
		/*if(columnIndex==0)
			resul= true;*/
		
		if(columnIndex==1)
			resul=true;
		/*if(columnIndex==6)
			resul=true;*/
	
		
		
		return resul;
	}

}
