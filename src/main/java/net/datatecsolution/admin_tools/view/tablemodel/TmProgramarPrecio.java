package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Articulo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TmProgramarPrecio extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1222;
	private String []columnNames={"Id","Nombre","Marca","Precio Venta"};
	private List<Articulo> articulos = new ArrayList<Articulo>();
	
	
	public void agregarArticulo(Articulo articulo) {
		articulos.add(articulo);
        fireTableDataChanged();
    }
	
	public Articulo getArticulo(int index){
		//proveedores.
		return articulos.get(index);
		
	}
	
	public void cambiarArticulo(int index,Articulo articulo){
		articulos.set(index, articulo);
	}
 
    public void eliminarArticulos(int rowIndex) {
    	articulos.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiarArticulos() {
    	articulos.clear();
        fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return articulos.size();
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
            return articulos.get(rowIndex).getId();
        case 1:
            return articulos.get(rowIndex).getArticulo();
        case 2:
        	return articulos.get(rowIndex).getCategoria().getDescripcion();//articulos.get(rowIndex).getMarca();
       
        case 3:
        	if(articulos.get(rowIndex).getPrecioVenta()>0)
        		return articulos.get(rowIndex).getPrecioVenta();
        	else
        		return null;
        default:
            return null;
		}
	}
	
	public List<Articulo> getArticulos(){
		return articulos;
	}
	 @Override
	    public void setValueAt(Object value, int rowIndex, int columnIndex) {
	        Articulo articulo = articulos.get(rowIndex);
	        String v=(String) value;
	        switch (columnIndex) {
	            case 0:
	            	articulo.setId((Integer) value);
	            case 1:
	            	articulo.setArticulo((String) value);
	            case 2:
	            	//articulo.setMarca((String) value);
	            	articulo.getCategoria().setDescripcion((String) value);
	            case 3:
	            	articulo.getImpuestoObj().setPorcentaje((String) value);
	            case 4:
	            	
	            		articulo.setPrecioVenta(Double.parseDouble(v));
	            	
	            		
	            	//articulo.setImpuesto((Double) value);
	   ///�	�	�	1�	�	�	�	1	q1	1	Q	11 codigo de mi  ni�o jajajaj
	        }
	        fireTableCellUpdated(rowIndex, columnIndex);
	    }
	

	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul=false;
		
		
		if(columnIndex==3)
			resul=true;
		/*if(columnIndex==6)
			resul=true;*/
	
		
		
		return resul;
  
    }

}
