package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TrProveedor implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		JLabel etiqueta = new JLabel();
		JCheckBox cbIvaIncluido=new JCheckBox();
		
        etiqueta.setOpaque(true);
        cbIvaIncluido.setOpaque(true);
        if (row % 2 == 0) {
            etiqueta.setBackground(new Color(176, 224, 230));
            cbIvaIncluido.setBackground(new Color(176, 224, 230));
        } else {
            etiqueta.setBackground(Color.white);
            cbIvaIncluido.setBackground(Color.white);
        }
        
        if(column==11){
        	 if(value !=null)
        	 {
	        	Boolean uno=(Boolean)value;
	        	cbIvaIncluido.setSelected(uno);
	        	cbIvaIncluido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        	 }
        	
        }else if (column == 1) {
        	
	            String nombre = (String) value;
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
	            
	            
	            if(value !=null) 
	            	etiqueta.setText(value.toString());
	            else
	            	etiqueta.setText("");
        	} else {
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	            if(value !=null) 
	            	etiqueta.setText(value.toString());
	            else
	            	etiqueta.setText("");
        	}
        
        
        if (isSelected) {
            etiqueta.setBackground(new Color(254, 172, 172));
        }
        
        if(column!=11)
        	return etiqueta;
        else
        	return cbIvaIncluido;
		
		
	}

}
