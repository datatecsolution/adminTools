package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Articulo;

import java.util.ArrayList;
import java.util.List;



public class TmArticulo extends TablaModelo {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1222;
	private String []columnNames={"Id","Nombre","Categoria","Impuesto","Precio Venta","Existencia","Estado"};
	private List<Articulo> articulos = new ArrayList<Articulo>();
	
	
	
	public void agregarArticulo(Articulo articulo) {
		articulos.add(articulo);
        fireTableDataChanged();
    }
	
	public Articulo getArticulo(int index){
		//proveedores.
		return articulos.get(index);
		
	}
	public List<Articulo> getArticulos(){
		return articulos;
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
		return 6;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
        case 0:
        	
        	if(articulos.get(rowIndex).getCodBarra()==null || articulos.get(rowIndex).getCodBarra().isEmpty())
        		return articulos.get(rowIndex).getId();
        	else
        		return articulos.get(rowIndex).getCodBarra().get(0);
        case 1:
            return articulos.get(rowIndex).getArticulo();
        case 2:
        	return articulos.get(rowIndex).getMarcaObj().getDescripcion();//articulos.get(rowIndex).getMarca();
        case 3:
            
            return articulos.get(rowIndex).getImpuestoObj().getPorcentaje();
        case 4:
        	 return articulos.get(rowIndex).getPrecioVenta();
        case 5:
       	 return  articulos.get(rowIndex).getExistencia();//articulos.get(rowIndex).getPrecioVenta();
        case 6:
        	return (articulos.get(rowIndex).isEstado()==true) ? "Alta":"Baja";
        default:
            return null;
		}
	}
	
	
	 @Override
	    public void setValueAt(Object value, int rowIndex, int columnIndex) {
	        Articulo articulo = articulos.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	articulo.setId((Integer) value);
	            case 1:
	            	articulo.setArticulo((String) value);
	            case 2:
	            	//articulo.setMarca((String) value);
	            	articulo.getMarcaObj().setDescripcion((String) value);
	            case 3:
	            	articulo.getImpuestoObj().setPorcentaje((String) value);
	            case 4:
	            	articulo.setPrecioVenta((Double)value);
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
        return false;
    }

}
