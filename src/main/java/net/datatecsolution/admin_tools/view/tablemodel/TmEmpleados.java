package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Empleado;

import java.util.ArrayList;
import java.util.List;

public class TmEmpleados extends TablaModelo {
	private String []columnNames={"Codigo","Nombre","Apellido","Telefono","Correo"};
	private List<Empleado> empleados = new ArrayList<Empleado>();
	
	public void agregar(Empleado emple) {
		empleados.add(emple);
        fireTableDataChanged();
    }
	
	public Empleado getEmpleado(int index){
		
		return empleados.get(index);
		
	}
	
	public void cambiarEmpleado(int index,Empleado emple){
		empleados.set(index, emple);
		fireTableDataChanged();
	}
	
	public void eliminar(int rowIndex) {
    	empleados.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	empleados.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return empleados.size();
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
				return empleados.get(rowIndex).getCodigo();
		case 1: 
			return empleados.get(rowIndex).getNombre();
		case 2:
			return empleados.get(rowIndex).getApellido();
		case 3:
			return empleados.get(rowIndex).getTelefono();
		case 4: 
			return empleados.get(rowIndex).getCorreo();
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
