package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Insumo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



public class TmInsumo extends TablaModelo {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1222;
	private String []columnNames={"Id","Descripcion","Cantidad","Precio U","Total"};
	private List<Insumo> insumos = new ArrayList<Insumo>();
	
	
	
	public void agregarInsumo(Insumo articulo) {
		insumos.add(articulo);
        fireTableDataChanged();
    }
	
	public Insumo getInsumo(int index){
		//proveedores.
		return insumos.get(index);
		
	}
	public List<Insumo> getInsumos(){
		return insumos;
	}
	
	public void cambiarInsumo(int index,Insumo a){
		insumos.set(index, a);
	}
 
    public void eliminarInsumo(int rowIndex) {
    	insumos.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiarInsumo() {
    	insumos.clear();
        fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return insumos.size();
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
        	
        	if(insumos.get(rowIndex).getArticulo().getCodBarra()==null || insumos.get(rowIndex).getArticulo().getCodBarra().isEmpty())
        		return insumos.get(rowIndex).getArticulo().getId();
        	else
        		return insumos.get(rowIndex).getArticulo().getCodBarra().get(0);
        case 1:
            return insumos.get(rowIndex).getArticulo().getArticulo();
        case 2:
        	return insumos.get(rowIndex).getCantidad();
        case 3:
        	return insumos.get(rowIndex).getArticulo().getPrecioVenta();
        case 4:
        	return insumos.get(rowIndex).getTotal();
        default:
            return null;
		}
	}
	
	
	 @Override
	    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		 Insumo insumo = insumos.get(rowIndex);
		 String v=(String) value;
	        switch (columnIndex) {
	            case 0:
	            	insumo.getArticulo().setId((Integer) value);
	            case 1:
	            	insumo.getArticulo().setArticulo((String) value);
	            case 2:
	            	//articulo.setMarca((String) value);
	            	insumo.setCantidad(new BigDecimal(v));
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
		/*if(columnIndex==0)
			resul= true;*/
		if(columnIndex==2)
			resul=true;
		
		
		return resul;
    }

	/**
	 * @param insumos the insumos to set
	 */
	public void setInsumos(List<Insumo> insumos) {
		this.insumos = insumos;
		fireTableDataChanged();
	}

}
