package net.datatecsolution.admin_tools.view.rendes;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class RenderizadorTablaFacturaCompra implements TableCellRenderer {

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
		
		 if(column==5){
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
			   editor.setText(" L. "+value.toString()+"  ");
		 }
		
		 
		 
		 if (isSelected) {
			 editor.setBackground(new Color(254, 172, 172));
	        }
		
		
		return editor;
	}

}
