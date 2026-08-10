package com.cyclops.ui;

import com.cyclops.model.QueryResult;
import com.cyclops.plugin.QueryEngine;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Contextual Query Console for running format queries (XPath 3.1, XQuery 3.1, JSONPath, SQL).
 */
public class QueryConsolePanel extends JPanel {
    private final QueryEngine queryEngine;
    private final File targetFile;
    private final Supplier<String> fileTextSupplier;

    private final JTextArea queryTextArea;
    private final TableGridPanel resultGridPanel;
    private final JTextArea resultRawTextArea;
    private final JLabel statusLabel;
    private final JLabel engineTitleLabel;

    public interface Supplier<T> {
        T get();
    }

    public QueryConsolePanel(QueryEngine queryEngine, File targetFile, Supplier<String> fileTextSupplier) {
        this.queryEngine = queryEngine;
        this.targetFile = targetFile;
        this.fileTextSupplier = fileTextSupplier;

        setLayout(new BorderLayout());

        // Top Query Input Section
        JPanel queryInputPanel = new JPanel(new BorderLayout());
        queryInputPanel.setBorder(BorderFactory.createTitledBorder("Interactive Query Console"));

        JToolBar headerToolBar = new JToolBar();
        headerToolBar.setFloatable(false);

        engineTitleLabel = new JLabel("Engine: " + (queryEngine != null ? queryEngine.getEngineName() : "None"));
        engineTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerToolBar.add(engineTitleLabel);
        headerToolBar.add(Box.createHorizontalGlue());

        JButton executeBtn = new JButton("⚡ Execute Query");
        executeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        executeBtn.addActionListener(e -> runQuery());
        headerToolBar.add(executeBtn);

        queryInputPanel.add(headerToolBar, BorderLayout.NORTH);

        queryTextArea = new JTextArea(4, 50);
        queryTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        if (queryEngine != null) {
            queryTextArea.setText(queryEngine.getDefaultQueryPlaceholder());
        }

        queryInputPanel.add(new JScrollPane(queryTextArea), BorderLayout.CENTER);

        // Bottom Result Section
        JTabbedPane resultTabPane = new JTabbedPane();

        resultGridPanel = new TableGridPanel();
        resultRawTextArea = new JTextArea();
        resultRawTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        resultRawTextArea.setEditable(false);

        resultTabPane.addTab("📊 Grid View", resultGridPanel);
        resultTabPane.addTab("📄 Raw Output", new JScrollPane(resultRawTextArea));

        // Status Bar
        statusLabel = new JLabel("Ready. Press 'Execute Query' to evaluate.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryInputPanel, resultTabPane);
        splitPane.setDividerLocation(140);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void runQuery() {
        if (queryEngine == null) {
            statusLabel.setText("No query engine available for this file format.");
            return;
        }

        String query = queryTextArea.getText();
        statusLabel.setText("Executing query...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(() -> {
            try {
                String currentText = fileTextSupplier != null ? fileTextSupplier.get() : null;
                QueryResult result = queryEngine.executeQuery(targetFile, query, currentText);

                if (result.isSuccess()) {
                    statusLabel.setText(String.format("✅ Success | Time: %d ms | Rows: %,d",
                            result.getExecutionTimeMs(), result.getRowCount()));

                    if (result.getRawOutput() != null) {
                        resultRawTextArea.setText(result.getRawOutput());
                        resultGridPanel.setData(result.getColumnNames(), result.getRows());
                    } else {
                        resultGridPanel.setData(result.getColumnNames(), result.getRows());
                        StringBuilder sb = new StringBuilder();
                        sb.append("Columns: ").append(result.getColumnNames()).append("\n\n");
                        for (java.util.List<Object> row : result.getRows()) {
                            sb.append(row).append("\n");
                        }
                        resultRawTextArea.setText(sb.toString());
                    }
                } else {
                    statusLabel.setText("❌ Error: " + result.getErrorMessage());
                    resultRawTextArea.setText("Query Execution Error:\n" + result.getErrorMessage());
                }
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }
}
