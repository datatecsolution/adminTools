package net.datatecsolution;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class CustomDateTableExample extends JFrame {
    private JTable table;
    private CustomTableModel tableModel;

    public CustomDateTableExample() {
        setTitle("Tabla de Fechas Personalizada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        tableModel = new CustomTableModel();
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setCellRenderer(new DateRenderer());
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

    class CustomTableModel extends AbstractTableModel {
        private String[] columnNames = {"Nombre", "Fecha"};
        private Object[][] data = {};

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data[rowIndex][columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow(Object[] rowData) {
            Object[][] newData = new Object[data.length + 1][2];
            for (int i = 0; i < data.length; i++) {
                newData[i] = data[i];
            }
            newData[data.length] = rowData;
            data = newData;
            fireTableDataChanged();
        }
    }

    class DateRenderer extends JDateChooser implements TableCellRenderer {
        public DateRenderer() {
            setDateFormatString("dd/MM/yyyy");
            setFont(table.getFont());
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Date) {
                setDate((Date) value);
            }
            return this;
        }
    }

    class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JDateChooser dateChooser;

        public DateCellEditor() {
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
            CustomDateTableExample example = new CustomDateTableExample();
            example.setVisible(true);
        });
    }
}
