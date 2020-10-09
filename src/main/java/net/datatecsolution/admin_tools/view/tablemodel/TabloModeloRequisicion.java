package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.DetalleFacturaProveedor;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TabloModeloRequisicion extends AbstractTableModel  {
	
	final private String []columnNames= {
			"Id Articulo", "Nombre", "Cantidad", "Precio Unidad", "Total"
		};
	
	private List<DetalleFacturaProveedor> detallesRequi=new ArrayList<DetalleFacturaProveedor>();
	
	
	public void agregarDetalle(){
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
		for(int x=0;x<detallesRequi.size();x++){
			
			//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo() );
			if(detallesRequi.get(x).getArticulo().getId()<0){
				//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo()+ "..Eliminando");
				detallesRequi.remove(x);
				
			}
		}
		DetalleFacturaProveedor uno =new DetalleFacturaProveedor();
		detallesRequi.add(uno);
		fireTableDataChanged();
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
	}
	
	public void setEmptyDetalles(){
		
		detallesRequi.clear();
	
		fireTableDataChanged();
	}
	public void eliminarDetalle(int index){
		detallesRequi.remove(index);
		fireTableDataChanged();
		
	}
	public void agregarDetalle(DetalleFacturaProveedor detalle) {
		detallesRequi.add(detalle);
        fireTableDataChanged();
    }
	
	public void setArticulo(Articulo a, int row){
		detallesRequi.get(row).setListArticulos(a);
	}
	
	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return detallesRequi.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		if(detallesRequi.get(rowIndex).getArticulo().getId()==-1){
			return null;
		}
		else{
				switch (columnIndex) {
		        case 0:
		        	
		        		return detallesRequi.get(rowIndex).getArticulo().getId();
		        	
		        case 1:
		        		return detallesRequi.get(rowIndex).getArticulo().getArticulo();
		        case 2:
		        	
		        		if(detallesRequi.get(rowIndex).getCantidad().doubleValue()!=0)
		        			return detallesRequi.get(rowIndex).getCantidad();
		        		else
		        			return null;
		        	
		        case 3:
		        	
		        		if(detallesRequi.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			//detallesFactura.
		        			return detallesRequi.get(rowIndex).getPrecioCompra().setScale(2, BigDecimal.ROUND_HALF_EVEN);
		        			
		        			
		        		}
		        		else
		        			return null;
		        
		       		        	
		        case 4:
		        	
		        		if(detallesRequi.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			return detallesRequi.get(rowIndex).getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN);
		        		}
		        		else
		        			return null;
		        	
		        default:
		            return null;
				}
			}
	}
	
	public void setDetalles(List<DetalleFacturaProveedor> d){
		detallesRequi.clear();
		if(d!=null){
			for(int x=0;x<d.size();x++){
				detallesRequi.add(d.get(x));
			}
		}else
			agregarDetalle();
		//detallesFactura=d;
		fireTableDataChanged();
	}
	public DetalleFacturaProveedor getDetalle(int row){
		return detallesRequi.get(row);
	}
	
	@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		DetalleFacturaProveedor detalle = detallesRequi.get(rowIndex);
		//JOptionPane.showMessageDialog(null, value);
		String v=(String) value;
		//JOptionPane.showMessageDialog(null, "Columan"+columnIndex+" fila"+rowIndex);
		switch(columnIndex){
			case 0:
				try{
					int id=Integer.parseInt(v);
					detallesRequi.get(rowIndex).getArticulo().setId(id);
					
					//this.fireTableDataChanged();
					//fireTableCellUpdated(row,col);
					//fireTableDataChanged();
				}catch(NumberFormatException e){
					CodBarra cod=new CodBarra();
					cod.setCodigoBarra(v);
					detallesRequi.get(rowIndex).getArticulo().getCodBarra().add(cod);
					detallesRequi.get(rowIndex).getArticulo().setId(-2);
					//detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(e)
				}
				this.fireTableCellUpdated(rowIndex, columnIndex);
					break;
			case 2:
				
				detallesRequi.get(rowIndex).setCantidad(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
					//fireTableDataChanged();
				break;
			case 3:
				detallesRequi.get(rowIndex).setPrecioCompra(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
				
				break;
		}
       

       // fireTableCellUpdated(rowIndex, columnIndex);
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
		if(columnIndex==1)
			resul=false;
		if(columnIndex==2)
			resul=true;
		if(columnIndex==3)
			resul=false;
		if(columnIndex==4)
			resul=false;
		
		
		
		return resul;
	}
	public DetalleFacturaProveedor getDetalles(int index) {
		// TODO Auto-generated method stub
		return detallesRequi.get(index);
	}


	public List<DetalleFacturaProveedor> getDetalles() {
		// TODO Auto-generated method stub
		return detallesRequi;
	}

	public void setArticulo(Articulo a){
		
		for(int x=0;x<detallesRequi.size();x++){
			if(detallesRequi.get(x).getArticulo().getId()==-1){
				detallesRequi.get(x).setListArticulos(a);
				break;
			}
		}
		
	}

	public void masCantidad(int index) {
		// TODO Auto-generated method stub
		BigDecimal temp=detallesRequi.get(index).getCantidad().add(new BigDecimal(1));
		detallesRequi.get(index).setCantidad(temp);
		fireTableCellUpdated(index, 2);
		fireTableCellUpdated(index, 3);
		fireTableCellUpdated(index, 4);
		//fireTableCellUpdated(index, 6);
		//fireTableCellUpdated(index, 7);
		
	}

	public void restarCantidad(int index) {
		// TODO Auto-generated method stub
		BigDecimal temp=detallesRequi.get(index).getCantidad().subtract(new BigDecimal(1));
		detallesRequi.get(index).setCantidad(temp);
		fireTableCellUpdated(index, 2);
		fireTableCellUpdated(index, 3);
		fireTableCellUpdated(index, 4);
	}

	public void setPricioCompra(BigDecimal precioCompra, int index) {
		// TODO Auto-generated method stub
		detallesRequi.get(index).setPrecioCompra(precioCompra);
		fireTableCellUpdated(index, 3);
	}


	public DetalleFacturaProveedor buscarDetallePorArticulo(Articulo articulo) {
		boolean existe=false;
		DetalleFacturaProveedor encontrado=null;

		this.detallesRequi.forEach(
				System.out::println
		);

		for (DetalleFacturaProveedor ar:detallesRequi
			 ) {
			if(articulo.getId()==ar.getArticulo().getId()){
				encontrado=ar;
				existe=true;

			}
			
		}
		
		
		
		if(existe)
			return encontrado;
		else 
			return null;
			
	}

	public double buscarCantidadPorArticulo(Articulo articulo) {

		double encontrado=0;

		/*
		this.detallesRequi.forEach(
				System.out::println
		);

		 */

		for (DetalleFacturaProveedor ar:detallesRequi
		) {
			if(articulo.getId()==ar.getArticulo().getId()){

				encontrado=encontrado+ar.getCantidad().doubleValue();

			}

		}

		return encontrado;


	}
}
