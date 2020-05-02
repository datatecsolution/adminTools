package net.datatecsolution.admin_tools.view.tablemodel;


import net.datatecsolution.admin_tools.modelo.ConfigUserFacturacion;

import java.util.ArrayList;
import java.util.List;


public class TmConfigUser extends TablaModelo {
	
	final private String []columnNames= {
			"Usuario", "Formato Fact", "Ventana Vendedor", "PWD Desc","PWD Precio","Desc %", "Ventana Obs","Redondiar Precio","Fact Sin/Inv"
		};
	private List<ConfigUserFacturacion> configs=new ArrayList<ConfigUserFacturacion>();
	
	
	public TmConfigUser(){
		//datosVacios();
	
	}
	
	public void agregar(ConfigUserFacturacion config) {
		configs.add(config);
        fireTableDataChanged();
    }
	
	public ConfigUserFacturacion getUsuario(int index){
		
		return configs.get(index);
		
	}
	
	
	public void eliminar(int rowIndex) {
		configs.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	configs.clear();
        fireTableDataChanged();
    }
	
	
	

	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return configs.size();
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
		        	
		        		return configs.get(rowIndex).getUsuario();
		        	
		        case 1:
		        		return configs.get(rowIndex).getFormatoFactura();
		        case 2:
		        	
		        		return configs.get(rowIndex).isVentanaVendedor();
		        	
		        case 3:
		        	
		        		return configs.get(rowIndex).isPwdDescuento();
		        case 4:
		        		return configs.get(rowIndex).isPwdPrecio();
		        
		        case 5:
		        	
		        		return configs.get(rowIndex).isDescPorcentaje();
		        	
		        case 6:
		        		return configs.get(rowIndex).isVentanaObservaciones();
		        case 7:
		        		return configs.get(rowIndex).isPrecioRedondear();
		        case 8:
		        	return configs.get(rowIndex).isFacturarSinInventario();
		        	
		        default:
		            return null;
				}
			}
	
	
	
	
	
	@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
		ConfigUserFacturacion detalle = configs.get(rowIndex);
		//JOptionPane.showMessageDialog(null, value);
		
		String v = null;
		Boolean vv = true;
		
		if(columnIndex==0 || columnIndex==1)
			v=(String) value;
		else
			vv=(Boolean) value;
			
			
		//JOptionPane.showMessageDialog(null, "Columan"+columnIndex+" fila"+rowIndex);
		switch(columnIndex){
		
			case 1:
				configs.get(rowIndex).setFormatoFactura(v);
                this.fireTableCellUpdated(rowIndex, columnIndex);
			break;
		
			case 2:
				configs.get(rowIndex).setVentanaVendedor(vv);
				this.fireTableCellUpdated(rowIndex, columnIndex);
			break;
			case 3:
				configs.get(rowIndex).setPwdDescuento(vv);
				this.fireTableCellUpdated(rowIndex, columnIndex);
				break;
			case 4:
				
				configs.get(rowIndex).setPwdPrecio(vv);
				fireTableCellUpdated(rowIndex, columnIndex);
					//fireTableDataChanged();
				break;
			case 5:
				configs.get(rowIndex).setDescPorcentaje(vv);
				fireTableCellUpdated(rowIndex, columnIndex);
				
				break;
				
			case 6:
				configs.get(rowIndex).setVentanaObservaciones(vv);
				fireTableCellUpdated(rowIndex, columnIndex);
				
				break;
			case 7:
					configs.get(rowIndex).setPrecioRedondear(vv);
					fireTableCellUpdated(rowIndex, columnIndex);
				break;
			case 8:
				configs.get(rowIndex).setFacturarSinInventario(vv);
				fireTableCellUpdated(rowIndex, columnIndex);
				break;
		}
        

       // fireTableCellUpdated(rowIndex, columnIndex);
    }


@Override
public Class getColumnClass(int columnIndex) {
	//        return getValueAt(0, columnIndex).getClass();
	if(columnIndex==0 || columnIndex==1)
		return String.class;
	else
		return Boolean.class;
		
}

@Override
public boolean isCellEditable(int rowIndex, int columnIndex) {
	boolean resul=false;
	if(columnIndex==0)
		resul= false;
	if(columnIndex==1)
		resul=true;
	if(columnIndex==2)
		resul=true;
	if(columnIndex==3)
		resul=true;
	if(columnIndex==4)
		resul=true;
	if(columnIndex==5)
		resul=true;
	if(columnIndex==6)
		resul=true;
	if(columnIndex==7)
		resul=true;
	if(columnIndex==8)
		resul=true;
	
	
	return resul;
}



}
