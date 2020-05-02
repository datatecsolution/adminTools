package net.datatecsolution.admin_tools.view.rendes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class TRconfigUsers implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		JLabel etiqueta = new JLabel();
		JCheckBox checkBox=new JCheckBox();
        etiqueta.setOpaque(true);
        checkBox.setOpaque(true);
       
        
        Color alternate = UIManager.getColor("Table.alternateRowColor");
        Color normal = new Color(table.getBackground().getRGB());
        checkBox.setEnabled(table.isCellEditable(row, column));
        checkBox.setBorderPainted(true);
        checkBox.setBackground(alternate != null && row % 2 == 0 ? alternate : normal);
        checkBox.setForeground(table.getForeground());
        /*
        if (row % 2 == 0) {
            etiqueta.setBackground(new Color(176, 224, 230));
            checkBox.setBackground(new Color(176, 224, 230));
            
        }else {
            etiqueta.getBackground();
            checkBox.getBackground();
            
        }*/
        
        
        
       if (column == 0 || column==1) {
            String nombre = (String) value;
            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            etiqueta.setText(value.toString());
        } else {
        	Boolean vv = true;
        	vv=(Boolean) value;
            //etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        	
            //etiqueta.setText(value.toString());
            checkBox.setEnabled(vv);
        }/*
        if (isSelected) {
            etiqueta.setBackground(new Color(254, 172, 172));
            checkBox.setBackground(new Color(254, 172, 172));
        }*/
       
       
        
        
        
        if (column == 0 || column == 1){
        	return etiqueta;
        }else{
        		return checkBox;
        }
		
		
	}

}
