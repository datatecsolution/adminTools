package net.datatecsolution.admin_tools.view.rendes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TablaRenderizadorProveedor implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		JLabel etiqueta = new JLabel();
        etiqueta.setOpaque(true);
        if (row % 2 == 0) {
            etiqueta.setBackground(new Color(176, 224, 230));
        } else {
            etiqueta.setBackground(Color.white);
        }
        
        
        
       if (column == 1) {
            String nombre = (String) value;
            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            /*if (nombre.startsWith("#")) { //Hombre
                etiqueta.setIcon(new ImageIcon("recursos/user.png")); // NOI18N
            } else if (nombre.startsWith("&")) { //Mujer
                etiqueta.setIcon(new ImageIcon("recursos/user2.png")); // NOI18N
            }*/
            etiqueta.setText(value.toString());
        } else {
            etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            etiqueta.setText(value.toString());
        }
        if (isSelected) {
            etiqueta.setBackground(new Color(254, 172, 172));
        }
        return etiqueta;
		
		
	}

}
