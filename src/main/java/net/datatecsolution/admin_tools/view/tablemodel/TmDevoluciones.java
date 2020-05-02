package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TmDevoluciones extends AbstractTableModel {

	final private String []columnNames= {
			 "Articulo", "Precio Unidad", "Cantidad","SubTotal","Impuesto","Descuento", "Total","Devolucion?"
		};
	private List<DetalleFactura> detallesFactura=new ArrayList<DetalleFactura>();
	
	public void agregarDetalle(){
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
		for(int x=0;x<detallesFactura.size();x++){
			
			//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo() );
			if(detallesFactura.get(x).getArticulo().getId()<0){
				//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo()+ "..Eliminando");
				detallesFactura.remove(x);
				
			}
		}
		DetalleFactura uno =new DetalleFactura();
		detallesFactura.add(uno);
		fireTableDataChanged();
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
	}
	public void setEmptyDetalles(){
		
			detallesFactura.clear();
		
		fireTableDataChanged();
	}
	public void setDetalles(List<DetalleFactura> d){
		detallesFactura.clear();
		if(d!=null){
			for(int x=0;x<d.size();x++){
				detallesFactura.add(d.get(x));
			}
		}else
			agregarDetalle();
		//detallesFactura=d;
		fireTableDataChanged();
	}
	public List<DetalleFactura> getDetalles(){
		return detallesFactura;
	}
	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }
	public DetalleFactura getDetalle(int row){
		return detallesFactura.get(row);
	}
	public void setArticulo(Articulo a, int row){
		detallesFactura.get(row).setListArticulos(a);
	}
	
	public void setArticulo(Articulo a){
		
		for(int x=0;x<detallesFactura.size();x++){
			if(detallesFactura.get(x).getArticulo().getId()==-1){
				detallesFactura.get(x).setListArticulos(a);
				break;
			}
		}
		
	}
	public boolean hayDevoluciones() {
		boolean resul=false;
		
		for(int x=0;x<this.detallesFactura.size();x++){
			if(detallesFactura.get(x).getAccion()==true){
				resul=true;
			}
		}
		
		return resul;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return detallesFactura.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}
	
	public void eliminarDetalle(int index){
		detallesFactura.remove(index);
		fireTableDataChanged();
		
	}
	public void masCantidad(int index){
		BigDecimal temp=detallesFactura.get(index).getCantidad().add(new BigDecimal(1));
		detallesFactura.get(index).setCantidad(temp);
		fireTableCellUpdated(index, 3);
		fireTableCellUpdated(index, 4);
		fireTableCellUpdated(index, 5);
		fireTableCellUpdated(index, 6);
		fireTableCellUpdated(index, 7);
		//fireTableDataChanged();
		//fireTableDataChanged();
	}
	public void restarCantidad(int index){
		BigDecimal temp=detallesFactura.get(index).getCantidad().subtract(new BigDecimal(1));
		detallesFactura.get(index).setCantidad(temp);
		fireTableCellUpdated(index, 3);
		fireTableCellUpdated(index, 4);
		fireTableCellUpdated(index, 5);
		fireTableCellUpdated(index, 6);
		fireTableCellUpdated(index, 7);
		//fireTableDataChanged();
		//fireTableDataChanged();
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		double salida=00;
		if(detallesFactura.get(rowIndex).getArticulo().getId()==-1){
			return null;
		}
		else{
		switch (columnIndex) {
		
		case 0:
			return detallesFactura.get(rowIndex).getArticulo().getArticulo();
		case 1:
		 return detallesFactura.get(rowIndex).getArticulo().getPrecioVenta();
		case 2:
			if(detallesFactura.get(rowIndex).getCantidad().doubleValue()!=0){
				return detallesFactura.get(rowIndex).getCantidad().setScale(2, BigDecimal.ROUND_HALF_EVEN);
			}else
				return null;
		
		case 3:
			//salida=detallesFactura.get(rowIndex).getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			return  detallesFactura.get(rowIndex).getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		case 4:
			//salida=detallesFactura.get(rowIndex).getImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			return  detallesFactura.get(rowIndex).getImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		case 5:
			if(detallesFactura.get(rowIndex).getDescuentoItem().doubleValue()!=0)
				return detallesFactura.get(rowIndex).getDescuentoItem().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			else
				return null;
		case 6:
			//salida=detallesFactura.get(rowIndex).getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			return  detallesFactura.get(rowIndex).getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		case 7:
			return detallesFactura.get(rowIndex).getAccion();
		 default:
	            return null;
		}
		}
	}
	
	@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		DetalleFactura detalle = detallesFactura.get(rowIndex);
		boolean v=(boolean) value;
		switch(columnIndex){
		
		case 7:
			
			detallesFactura.get(rowIndex).setAccion(v);// Double.parseDouble(v));
			fireTableCellUpdated(rowIndex, columnIndex);
				//fireTableDataChanged();
			break;
	}
	}
	
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
		switch (columnIndex) {
        case 0:
            return String.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        case 3:
            return String.class;
        case 4:
            return String.class;
        case 5:
            return String.class;
        case 6:
            return String.class;
        default:
            return Boolean.class;
		}
    }
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul=false;
		
		
		if(columnIndex==7)
			resul=true;
		
		
		return resul;
	}
	
	public boolean hayAcciones() {
		boolean resul=false;
		
		for(int x=0;x<this.detallesFactura.size();x++){
			if(detallesFactura.get(x).getAccion()==true){
				resul=true;
			}
		}
		
		return resul;
	}

}
