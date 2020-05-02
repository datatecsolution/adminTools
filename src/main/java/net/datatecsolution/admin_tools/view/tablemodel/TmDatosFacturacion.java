package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.DatosFacturacion;

import java.util.ArrayList;
import java.util.List;

public class TmDatosFacturacion extends TablaModelo {
	private String []columnNames={"Codigo","CAI","Fact Inicial","Fact Final","Codigo Facturacion","Cant Otorgada","Fecha Limite","Observacion"};
	private List<DatosFacturacion> datosFs = new ArrayList<DatosFacturacion>();
	
	public void agregar(DatosFacturacion c) {
		datosFs.add(c);
        fireTableDataChanged();
    }
	
	public DatosFacturacion getDatoF(int index){
		
		return datosFs.get(index);
		
	}
	

	public void eliminar(int rowIndex) {
    	datosFs.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	datosFs.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return datosFs.size();
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
			return datosFs.get(rowIndex).getCodigo();
		case 1:
			return datosFs.get(rowIndex).getCAI();
		case 2:
			return datosFs.get(rowIndex).getFacturaInicial();
		case 3:
			return datosFs.get(rowIndex).getFacturaFinal();
		case 4:
			return datosFs.get(rowIndex).getCodigoFacturas();
		case 5:
			return datosFs.get(rowIndex).getCantOtorgada();
		case 6:
			return datosFs.get(rowIndex).getFechaLimite();
		case 7:
			if(rowIndex==0){
				return "ACTIVO";
			}else
				return " ";
		default:
            return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
        
        
        /*switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return String.class;
        case 2:
        	return String.class;
        
        default:
            return null;
            }*/
    }
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
