package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrProveedor extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {

		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (!isSelected) {
			if (row % 2 == 0) {
				cell.setBackground(new Color(176, 224, 230));
			} else {
				cell.setBackground(Color.WHITE);
			}
		}else {
			cell.setBackground(new Color(254, 172, 172));
		}


		if ( column == 1 ) {
			setHorizontalAlignment(SwingConstants.LEFT);
		} else if (column == 0 || column == 2  || column==11) {
			setHorizontalAlignment(SwingConstants.CENTER);
		}else{
			setHorizontalAlignment(SwingConstants.RIGHT);
		}


		return cell;
		// TODO Auto-generated method stub
		/*JLabel etiqueta = new JLabel();
		//JCheckBox cbIvaIncluido=new JCheckBox();
		JDateChooser dateVencimiento = new JDateChooser("dd/MM/yyyy", "##/##/####", '_');
		
        etiqueta.setOpaque(true);
        //cbIvaIncluido.setOpaque(true);
        if (row % 2 == 0) {
            etiqueta.setBackground(new Color(176, 224, 230));
            //cbIvaIncluido.setBackground(new Color(176, 224, 230));
        } else {
            etiqueta.setBackground(Color.white);
            //cbIvaIncluido.setBackground(Color.white);
        }
        
        if(column==11){
        	 if(value !=null)
        	 {
				 String date = (String) value;
				 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				 try{
					 dateVencimiento.setDate(sdf.parse(date) );
				 }catch (Exception ee){

				 }


	        	//Boolean uno=(Boolean)value;
	        	//cbIvaIncluido.setSelected(uno);
	        	//cbIvaIncluido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

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
        	return dateVencimiento;*/
		
		
	}

}
