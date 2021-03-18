package net.datatecsolution.admin_tools.view.tablemodel;


import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.DetalleFacturaProveedor;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class DmtFacturaProveedores extends AbstractTableModel {
	
	final private String []columnNames= {
			"Id Articulo", "Nombre", "Cantidad", "Precio Unidad","SubTotal","Impuesto", "Total","P/venta","P/venta 2","P/venta 3","P/costo","IVA incluido?"
		};
	private List<DetalleFacturaProveedor> detallesFactura=new ArrayList<DetalleFacturaProveedor>();
	private double totalCompra=0;
	
	public DmtFacturaProveedores(){
		//datosVacios();
	
	}
	
	public void agregarDetalle(){
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
		for(int x=0;x<detallesFactura.size();x++){
			
			//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo() );
			if(detallesFactura.get(x).getArticulo().getId()<0){
				//JOptionPane.showMessageDialog(null,detallesFactura.get(x).getArticulo()+ "..Eliminando");
				detallesFactura.remove(x);
				
			}
		}
		DetalleFacturaProveedor uno =new DetalleFacturaProveedor();
		detallesFactura.add(uno);
		fireTableDataChanged();
		//JOptionPane.showMessageDialog(null,detallesFactura.size() );
	}
	public void setTotalCompra(double t){
		totalCompra+=t;
	}
	public double getTotalCompra(){
		return totalCompra;
	}
	public void agregarDetalle(DetalleFacturaProveedor detalle) {
		detallesFactura.add(detalle);
        fireTableDataChanged();
    }
	
	public void setArticulo(Articulo a, int row){
		detallesFactura.get(row).setListArticulos(a);
	}
	

	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
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

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
		if(detallesFactura.get(rowIndex).getArticulo().getId()==-1){
			return null;
		}
		else{
				switch (columnIndex) {
		        case 0:
		        	
		        		return detallesFactura.get(rowIndex).getArticulo().getId();
		        	
		        case 1:
		        		return detallesFactura.get(rowIndex).getArticulo().getArticulo();
		        case 2:
		        	
		        		if(detallesFactura.get(rowIndex).getCantidad().doubleValue()!=0)
		        			return detallesFactura.get(rowIndex).getCantidad();
		        		else
		        			return null;
		        	
		        case 3:
		        	
		        		if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			return detallesFactura.get(rowIndex).getPrecioCompra();
		        			
		        			
		        		}
		        		else
		        			return null;
		        case 4:
		        	if(detallesFactura.get(rowIndex).getCantidad().doubleValue()!=0)
		        		return detallesFactura.get(rowIndex).getSubTotal();
		        	else
	        			return null;
		        
		        case 5:
		        	
		        		if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			return detallesFactura.get(rowIndex).getImpuesto();
		        		}
		        		else
		        			return null;
		        	
		        case 6:
		        	
		        		if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			return detallesFactura.get(rowIndex).getTotal();
		        		}
		        		else
		        			return null;
		        case 7:
		        		if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
		        			//descomentar para calcular un porcentaje de utilidad en los precios
		        			//BigDecimal precioVenta=new BigDecimal(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue());
		        	
		        	//		BigDecimal newPrecioVenta=precioVenta.multiply(new BigDecimal(1.4));
		        			
		        			//detallesFactura.get(rowIndex).getArticulo().setPrecioVenta(newPrecioVenta.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue());
		        			//return detallesFactura.get(rowIndex).getArticulo().getPrecioVenta();
		        			return detallesFactura.get(rowIndex).getArticulo().getPrecioVenta();
		        			
		        		}else
		        			return null;

				case 8:
					PrecioArticulo precioVenta2=null;

					for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
						if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==2){
							precioVenta2=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
						}
					}
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioVenta2!=null){


						return precioVenta2.getPrecio();

					}else
						return null;
				case 9:
					PrecioArticulo precioVenta3=null;

					for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
						if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==3){
							precioVenta3=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
						}
					}
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioVenta3!=null){
						return precioVenta3.getPrecio();
					}else
						return null;
		        case 10:
		        	PrecioArticulo precioCosto=null;
		        	
		        	for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
		        		if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==4){
		        			precioCosto=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
		        		}
		        	}
		        	if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioCosto!=null){
	        			
	        			
	        			return precioCosto.getPrecio();
	        			
	        		}else
	        			return null;
		        	
		        case 11:
		        	return detallesFactura.get(rowIndex).isIvaIncludo();
		        	
		        default:
		            return null;
				}
			}
	}
	
	
	public void setDetalles(List<DetalleFacturaProveedor> d){
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
	
	public DetalleFacturaProveedor getDetalle(int row){
		return detallesFactura.get(row);
	}
	
	@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		DetalleFacturaProveedor detalle = detallesFactura.get(rowIndex);
		//JOptionPane.showMessageDialog(null, value);
		
		String v = null;
		Boolean vv = true;
		if(columnIndex==11)
			vv=(Boolean) value;
		else
			v=(String) value;
			
			
		//JOptionPane.showMessageDialog(null, "Columan"+columnIndex+" fila"+rowIndex);
		switch(columnIndex){
			case 0:
				/*
				try{
					int id=Integer.parseInt(v);
					detallesFactura.get(rowIndex).getArticulo().setId(id);
					
					//this.fireTableDataChanged();
					//fireTableCellUpdated(row,col);
					//fireTableDataChanged();
				}catch(NumberFormatException e){
					CodBarra cod=new CodBarra();
					cod.setCodigoBarra(v);
					detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(cod);
					detallesFactura.get(rowIndex).getArticulo().setId(-2);
					//detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(e)
				}*/
				
				
				CodBarra cod=new CodBarra();
				cod.setCodigoBarra(v);
				detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(cod);
				detallesFactura.get(rowIndex).getArticulo().setId(-2);
				this.fireTableCellUpdated(rowIndex, columnIndex);
					break;
			case 2:
				
				detallesFactura.get(rowIndex).setCantidad(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
					//fireTableDataChanged();
				break;
			case 3:
				detallesFactura.get(rowIndex).setPrecioCompra(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
				
				break;
				
			case 4:
				detallesFactura.get(rowIndex).setSubTotal(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
				
				break;
			case 7:
					//BigDecimal 
					detallesFactura.get(rowIndex).getArticulo().setPrecioVenta(new Double(v));
					fireTableCellUpdated(rowIndex, columnIndex);
				break;

			case 8:
				PrecioArticulo precioVenta2=null;

				for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==2){
						precioVenta2=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
					}
				}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){

					precioVenta2.setPrecio(new BigDecimal(v));
					fireTableCellUpdated(rowIndex, columnIndex);

				}else{
					PrecioArticulo precioVent2=new PrecioArticulo();
					precioVent2.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
					precioVent2.setCodigoPrecio(2);
					precioVent2.setPrecio(new BigDecimal(v));
					detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioVent2);
					//JOptionPane.showMessageDialog(null,"El articulo no tiene precio de costo.","Error en articulo",JOptionPane.ERROR_MESSAGE);
				}

				break;
			case 9:
				PrecioArticulo precioVenta3=null;

				for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==3){
						precioVenta3=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
					}
				}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){

					precioVenta3.setPrecio(new BigDecimal(v));
					fireTableCellUpdated(rowIndex, columnIndex);

				}else{
					PrecioArticulo precioVent3=new PrecioArticulo();
					precioVent3.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
					precioVent3.setCodigoPrecio(3);
					precioVent3.setPrecio(new BigDecimal(v));
					detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioVent3);
					//JOptionPane.showMessageDialog(null,"El articulo no tiene precio de costo.","Error en articulo",JOptionPane.ERROR_MESSAGE);
				}

				break;
			case 10:
				PrecioArticulo precioCosto=null;
	        	
	        	for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
	        		if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==4){
	        			precioCosto=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
	        		}
	        	}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){
        			
					precioCosto.setPrecio(new BigDecimal(v));
                    fireTableCellUpdated(rowIndex, columnIndex);
        			
        		}else{
        			PrecioArticulo precioCosto2=new PrecioArticulo();
        			precioCosto2.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
        			precioCosto2.setCodigoPrecio(4);
        			precioCosto2.setPrecio(new BigDecimal(v));
        			detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioCosto2);
        			//JOptionPane.showMessageDialog(null,"El articulo no tiene precio de costo.","Error en articulo",JOptionPane.ERROR_MESSAGE); 
        		}
	        	
				break;
			case 11:
				detallesFactura.get(rowIndex).setIvaIncludo(vv);
				fireTableCellUpdated(rowIndex, columnIndex);
				break;
		}
        

       // fireTableCellUpdated(rowIndex, columnIndex);
    }


@Override
public Class getColumnClass(int columnIndex) {
	//        return getValueAt(0, columnIndex).getClass();
	if(columnIndex==11)
		return Boolean.class;
	else
		return String.class;
}

@Override
public boolean isCellEditable(int rowIndex, int columnIndex) {
	boolean resul=false;
	if(columnIndex==0)
		resul= true;
	if(columnIndex==1)
		resul=false;
	if(columnIndex==2)
		resul=true;
	if(columnIndex==3)
		resul=true;
	if(columnIndex==4)
		resul=true;
	if(columnIndex==5)
		resul=false;
	if(columnIndex==6)
		resul=false;
	if(columnIndex==6)
		resul=true;
	
	if(columnIndex==7)
		resul=true;
	if(columnIndex==8)
		resul=true;
	if(columnIndex==9)
		resul=true;
	if(columnIndex==10)
		resul=true;
	if(columnIndex==11)
		resul=true;
	
	
	return resul;
}
public DetalleFacturaProveedor getDetalles(int index) {
	// TODO Auto-generated method stub
	return detallesFactura.get(index);
}


public List<DetalleFacturaProveedor> getDetalles() {
	// TODO Auto-generated method stub
	return detallesFactura;
}

public void setArticulo(Articulo a){
	
	for(int x=0;x<detallesFactura.size();x++){
		if(detallesFactura.get(x).getArticulo().getId()==-1){
			detallesFactura.get(x).setListArticulos(a);
			break;
		}
	}
	
}

public void eliminarDetalle(int index){
		detallesFactura.remove(index);
		fireTableDataChanged();
		
}

}
