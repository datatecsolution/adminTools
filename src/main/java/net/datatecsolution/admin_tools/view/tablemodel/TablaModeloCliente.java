package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Cliente;

import java.util.ArrayList;
import java.util.List;

public class TablaModeloCliente extends TablaModelo {
	private String []columnNames={"Id","Nombre","Direccion","Telefono","RTN","Saldo"};
	private List<Cliente> clientes = new ArrayList<Cliente>();
	
	@Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return clientes.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}
	
	 @Override
	 public void setValueAt(Object value, int rowIndex, int columnIndex) {
		 
	 }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
        case 0:
            return clientes.get(rowIndex).getId();
        case 1:
            return clientes.get(rowIndex).getNombre();
        case 2:
        	return  clientes.get(rowIndex).getDereccion();
        case 3:
        	return clientes.get(rowIndex).getTelefono();//articulos.get(rowIndex).getMarca();
        case 4:
            
            return clientes.get(rowIndex).getRtn();
        case 5:
            
            return clientes.get(rowIndex).getSaldoCuenta().doubleValue();
       
        default:
            return null;
		}
	}
	
	public Cliente getCliente(int index){
		//proveedores.
		return clientes.get(index);
		
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

	public void limpiarClientes() {
		// TODO Auto-generated method stub
		clientes.clear();
		this.fireTableDataChanged();
	}

	public void agregarCliente(Cliente cliente) {
		// TODO Auto-generated method stub
		clientes.add(cliente);
		this.fireTableDataChanged();
		
	}

	public void cambiarCliente(int filaPulsada, Cliente cliente) {
		// TODO Auto-generated method stub
		clientes.set(filaPulsada, cliente);
		this.fireTableDataChanged();
	}


}
