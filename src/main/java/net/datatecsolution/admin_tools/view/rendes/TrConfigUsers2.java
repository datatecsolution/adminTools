package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TrConfigUsers2 extends JCheckBox implements TableCellRenderer {
	
	 Font normal = new Font( "Arial",Font.PLAIN,12 );
	 Font negrilla = new Font( "Helvetica",Font.BOLD,12);
	 Font cursiva = new Font( "Times new roman",Font.ITALIC,12 );
	 private JComponent component = new JCheckBox();
	 
	 public TrConfigUsers2() {
	       
	        setOpaque(true);
	       // super();
	    //setHorizontalAlignment(JLabel.CENTER);
	    //setIcon(new ColorableMetalCheckBoxIcon());
	    //setBorderPainted(true);
	    }
	 
	 private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	    //private JComponent component = new JCheckBox();  

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// TODO Auto-generated method stub
		//Color de fondo de la celda
		 
	      JLabel etiqueta = new JLabel();
	       
	        etiqueta.setOpaque(true);
	        if (row % 2 == 0) {
	            etiqueta.setBackground(new Color(255, 255, 200));
	        } else {
	            etiqueta.setBackground(Color.white);
	        }
	   
	        if (column == 0) {
	            String nombre = (String) value;
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
	           
	            //etiqueta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/on.png")));
	           
	           
	        }
	       
	        if (column ==2) {
	            String nombre = (String) value;
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
	           
	            //    etiqueta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/off.png")));
	           
	           
	        }
	       
	       
	       
	         if (column == 1) {
	            String nombre = (String) value;
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
	            if (nombre.startsWith("V")) { //Hombre
	             //   etiqueta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/hombre.png")));
	            } else if (nombre.startsWith("&")) { //Mujer
	             //   etiqueta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/mujer.png")));
	            }
	            etiqueta.setText(value.toString().substring(1, nombre.length()));
	        }
	         
	         
	         
	           
	        else {
	            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	            etiqueta.setText(value.toString());
	        }
	        //color de fondo de la celada cuando es seleccionada
	        if (isSelected) {
	            etiqueta.setBackground(new Color(0,153,204));
	        }
	       
	        //else
	        //{
	          // etiqueta.setBackground(new Color(255, 255, 200));
	        //}
	   
	   
	         
	         if (hasFocus) {
	                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
	            } else {
	               setBorder(noFocusBorder);
	            }
	       
	       
	     
	        return etiqueta;
	}

}
