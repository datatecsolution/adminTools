package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RtCuentasFacturas implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel editor= new JLabel();
		editor.setOpaque(true);
		if (value != null)
		      editor.setText("  "+value.toString()+"  ");
		
		
		if (row % 2 == 0) {
			editor.setBackground(new Color(176, 224, 230));
        } else {
        	editor.setBackground(Color.white);
        }

		if(column==7)
			editor.setHorizontalAlignment(SwingConstants.RIGHT);
		
		 
		 
		 if (isSelected) {
			 editor.setBackground(new Color(254, 172, 172));
	        }
		
		
		return editor;
	}

}
