package net.datatecsolution.admin_tools.view.rendes;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class RtPrecios implements TableCellRenderer {

	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		
		JTextField editor = new JTextField();
		 editor.setText("");
		    if (value != null)
		      editor.setText(value.toString());
		    
		    if (row % 2 == 0) {
		    	editor.setBackground(new Color(176, 224, 230));
	        } else {
	        	editor.setBackground(Color.white);
	        }
		    
		    //editor.setBackground((row % 2 == 0) ? Color.white : Color.cyan);
		    
		    
		   if(column==0){
				   editor.setHorizontalAlignment(SwingConstants.CENTER);
			  }
		   if(column==1){
			   editor.setHorizontalAlignment(SwingConstants.LEFT);
		   }
		   if (isSelected) {
		    	editor.setBackground(new Color(254, 172, 172));
	        }
		 return editor;
	}

}
