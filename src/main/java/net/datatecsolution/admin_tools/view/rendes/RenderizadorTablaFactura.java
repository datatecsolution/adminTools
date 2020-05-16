package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RenderizadorTablaFactura implements TableCellRenderer{

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		
		Font myFont=new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		 JTextField editor = new JTextField();
		 editor.setFont(myFont);
		 editor.setText("");
		    if (value != null)
		      editor.setText(value.toString());
		    
		    if (row % 2 == 0) {
		    	editor.setBackground(new Color(176, 224, 230));
	        } else {
	        	editor.setBackground(Color.white);
	        }
		    
		    //editor.setBackground((row % 2 == 0) ? Color.white : Color.cyan);
		    
		    
		  /* if(column==0){
				   editor.setHorizontalAlignment(SwingConstants.CENTER);
			  }*/
		   if(column==0){
			   editor.setHorizontalAlignment(SwingConstants.LEFT);
		   }
		   if(column==1){
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   }
		   if(column==2)
			   editor.setHorizontalAlignment(SwingConstants.CENTER);
		   if(column==3)
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   if(column==4)
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   if(column==5)
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		   if(column==6)
			   editor.setHorizontalAlignment(SwingConstants.RIGHT);
		 
		    if (isSelected) {
		    	editor.setBackground(new Color(254, 172, 172));
	        }
		 return editor;
	}
	
	

}
