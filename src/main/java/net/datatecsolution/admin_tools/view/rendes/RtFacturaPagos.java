package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RtFacturaPagos implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		 JTextField editor = new JTextField();
		 JCheckBox check=new JCheckBox();
		 editor.setText("");
		    if (value != null)
		      editor.setText(value.toString());
		    
		    if (row % 2 == 0) {
		    	editor.setBackground(new Color(176, 224, 230));
		    	check.setBackground(new Color(176, 224, 230));
	        } else {
	        	editor.setBackground(Color.white);
	        	check.setBackground(Color.white);
	        }
		    
		    //editor.setBackground((row % 2 == 0) ? Color.white : Color.cyan);
		    
		    
		   if(column==0){
				   editor.setHorizontalAlignment(SwingConstants.CENTER);
			  }
		   if(column==1){
			   editor.setHorizontalAlignment(SwingConstants.LEFT);
		   }
		   if(column==2){
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   }
		   if(column==3)
			   editor.setHorizontalAlignment(SwingConstants.CENTER);
		   /*if(column==4)
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   
		 */
		    if (isSelected) {
		    	editor.setBackground(new Color(254, 172, 172));
	        }
		    
		    if(column==4){
		    	return check;
		    }
		    else{
		    	return editor;
		    }
	}
	
	

}
