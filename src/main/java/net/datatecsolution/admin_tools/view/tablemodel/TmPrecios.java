package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.table.AbstractTableModel;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TmPrecios extends AbstractTableModel {
	
	private final String []columnNames={"Tipo Precio","Precio"};
	private final List<PrecioArticulo> precios = new ArrayList<PrecioArticulo>();
	
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
		String v=value==null ? "" : value.toString().trim();
        switch (columnIndex) {
            case 0:
            	precio.setDecripcion(v);// .setId((Integer) value);
            	break;
            case 1:
            	// vacio = sin precio; se aceptan separadores de miles ("1,250.50")
            	if(v.isEmpty()){
            		precio.setPrecio(BigDecimal.ZERO);
            	}else{
            		try{
            			precio.setPrecio(new BigDecimal(v.replace(",", "")));
            		}catch(NumberFormatException e){
            			JOptionPane.showMessageDialog(null, "\""+v+"\" no es un precio válido para "+precio.getDescripcion()+".",
            					"Precio no válido", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            	}
            	break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
	 @Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul= columnIndex == 1;
		/*if(columnIndex==0)
			resul= true;*/
		
		/*if(columnIndex==6)
			resul=true;*/
	
		
		
		return resul;
	}

}
