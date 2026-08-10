package com.cyclops.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Interactive JTable grid for viewing tabular files (CSV, Parquet, Avro, ORC, SQL query results).
 */
public class TableGridPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private final JTextField filterField;
    private final JLabel statsLabel;

    public TableGridPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // View-only grid
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(true);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Top Search & Filter Bar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(new JLabel("🔍 Filter: "));
        filterField = new JTextField(20);
        filterField.setToolTipText("Type to filter table rows instantly...");
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        toolBar.add(filterField);

        JButton copyBtn = new JButton("📋 Copy Selected");
        copyBtn.addActionListener(e -> copySelectedCells());
        toolBar.addSeparator();
        toolBar.add(copyBtn);

        statsLabel = new JLabel("Columns: 0  |  Rows: 0");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(statsLabel);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setData(List<String> columnNames, List<List<Object>> rows) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        if (columnNames != null) {
            for (String col : columnNames) {
                tableModel.addColumn(col);
            }
        }

        if (rows != null) {
            for (List<Object> row : rows) {
                tableModel.addRow(row.toArray());
            }
        }

        // Adjust column widths based on content length
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxLen = tableModel.getColumnName(col).length();
            for (int row = 0; row < Math.min(table.getRowCount(), 100); row++) {
                Object val = table.getValueAt(row, col);
                if (val != null) {
                    maxLen = Math.max(maxLen, val.toString().length());
                }
            }
            int preferredWidth = Math.min(Math.max(maxLen * 9, 80), 350);
            table.getColumnModel().getColumn(col).setPreferredWidth(preferredWidth);
        }

        updateStats();
    }

    private void applyFilter() {
        String text = filterField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
        updateStats();
    }

    private void updateStats() {
        int visibleRows = table.getRowCount();
        int totalRows = tableModel.getRowCount();
        int cols = tableModel.getColumnCount();
        if (visibleRows != totalRows) {
            statsLabel.setText(String.format("Cols: %d | Rows: %,d (Filtered from %,d)", cols, visibleRows, totalRows));
        } else {
            statsLabel.setText(String.format("Cols: %d | Rows: %,d", cols, totalRows));
        }
    }

    private void copySelectedCells() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedCols = table.getSelectedColumns();
        if (selectedRows.length == 0 || selectedCols.length == 0) return;

        StringBuilder sb = new StringBuilder();
        for (int r : selectedRows) {
            for (int c : selectedCols) {
                Object val = table.getValueAt(r, c);
                sb.append(val != null ? val.toString() : "").append("\t");
            }
            sb.append("\n");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        JOptionPane.showMessageDialog(this, "Copied selected cells to clipboard.", "Clipboard", JOptionPane.INFORMATION_MESSAGE);
    }
}
