package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class TmUsuarios extends TablaModelo {
	private String []columnNames={"Usuario","Nombre","Tipo Usuario"};
	private List<Usuario> usuarios = new ArrayList<Usuario>();
	
	public void agregar(Usuario user) {
		usuarios.add(user);
        fireTableDataChanged();
    }
	
	public Usuario getUsuario(int index){
		
		return usuarios.get(index);
		
	}
	
	public void cambiarUsuario(int index,Usuario user){
		usuarios.set(index, user);
		fireTableDataChanged();
	}
	
	public void eliminar(int rowIndex) {
    	usuarios.remove(rowIndex);
        fireTableDataChanged();
    }
     
    public void limpiar() {
    	usuarios.clear();
        fireTableDataChanged();
    }
	
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return usuarios.size();
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
			return usuarios.get(rowIndex).getUser();
		case 1:
			return usuarios.get(rowIndex).getNombre();
		case 2:
			return usuarios.get(rowIndex).getPermiso();
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
