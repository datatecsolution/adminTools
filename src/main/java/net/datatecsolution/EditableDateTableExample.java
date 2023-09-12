package net.datatecsolution;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class EditableDateTableExample extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public EditableDateTableExample() {
        setTitle("Tabla de Fechas Editables");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        tableModel = new DefaultTableModel(new Object[]{"Nombre", "Fecha"}, 0);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setCellEditor(new DateCellEditor());

        JButton addButton = new JButton("Agregar");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow(new Object[]{"", new Date()});
            }
        });

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(addButton, BorderLayout.SOUTH);
    }

    class DateCellEditor extends DefaultCellEditor {
        private JDateChooser dateChooser;

        public DateCellEditor() {
            super(new JTextField());
            dateChooser = new JDateChooser();
        }

        @Override
        public Object getCellEditorValue() {
            return dateChooser.getDate();
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Date) {
                dateChooser.setDate((Date) value);
            }
            return dateChooser;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditableDateTableExample example = new EditableDateTableExample();
            example.setVisible(true);
        });
    }
}
