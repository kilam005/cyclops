package com.cyclops.ui;

import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.builtin.GenericFallbackPlugin;
import com.cyclops.service.BookmarkManager;
import com.cyclops.service.FileDetector;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Side-by-side workspace tab panel with File Loading Progress Status & Float/Dock capability.
 */
public class DocumentTabPanel extends JPanel {
    private final File file;
    private final FileTypePlugin plugin;
    private final JLabel formatBadgeLabel;

    private EditorPanel editorPanel;
    private TableGridPanel tableGridPanel;
    private XmlGroupedTableView xmlGroupedTableView;
    private TreeInspectorPanel treeInspectorPanel;
    private QueryConsolePanel queryConsolePanel;
    private GenericViewerPanel genericViewerPanel;

    private JSplitPane mainSplitPane;
    private JTabbedPane inspectorTabPane;
    private JPanel loadingPanel;
    private JLabel loadingStatusLabel;
    private JProgressBar loadingProgressBar;

    public interface StatusUpdateListener {
        void onStatusUpdate(String status);
    }
    private StatusUpdateListener statusUpdateListener;

    public DocumentTabPanel(File file) {
        this.file = file;
        this.plugin = FileDetector.detectPlugin(file);

        setLayout(new BorderLayout());

        // Top Toolbar displaying File Path, Bookmark Button, Format Badge, and Layout View Switcher
        JToolBar headerBar = new JToolBar();
        headerBar.setFloatable(false);

        JLabel fileIconLabel = new JLabel("📄 ");
        JLabel filePathLabel = new JLabel(file.getName() + "  (" + file.getAbsolutePath() + ")");
        filePathLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton bookmarkBtn = new JButton("⭐ Bookmark File");
        bookmarkBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bookmarkBtn.setToolTipText("Add this file location to your bookmarks");
        bookmarkBtn.addActionListener(e -> {
            int line = editorPanel != null ? editorPanel.getTextArea().getCaretLineNumber() + 1 : 1;
            BookmarkManager.getInstance().addBookmark(file, line, file.getName());
            JOptionPane.showMessageDialog(this, "Bookmarked " + file.getName() + " (Line " + line + ")", "Bookmark Added", JOptionPane.INFORMATION_MESSAGE);
        });

        formatBadgeLabel = new JLabel(" [" + plugin.getFileType().getDisplayName() + "] ");
        formatBadgeLabel.setOpaque(true);
        formatBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formatBadgeLabel.setBackground(new Color(0x38, 0xbd, 0xf8));
        formatBadgeLabel.setForeground(Color.BLACK);
        formatBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        // Layout View Toggle Buttons
        JRadioButton sideBySideBtn = new JRadioButton("↔ Side-by-Side", true);
        JRadioButton editorOnlyBtn = new JRadioButton("◀ Editor Only");
        JRadioButton inspectorOnlyBtn = new JRadioButton("▶ Inspector Only");
        ButtonGroup layoutGroup = new ButtonGroup();
        layoutGroup.add(sideBySideBtn);
        layoutGroup.add(editorOnlyBtn);
        layoutGroup.add(inspectorOnlyBtn);

        sideBySideBtn.addActionListener(e -> setSideBySideMode());
        editorOnlyBtn.addActionListener(e -> setEditorOnlyMode());
        inspectorOnlyBtn.addActionListener(e -> setInspectorOnlyMode());

        headerBar.add(fileIconLabel);
        headerBar.add(filePathLabel);
        headerBar.add(Box.createHorizontalGlue());
        headerBar.add(bookmarkBtn);
        headerBar.addSeparator();
        headerBar.add(new JLabel("View Mode: "));
        headerBar.add(sideBySideBtn);
        headerBar.add(editorOnlyBtn);
        headerBar.add(inspectorOnlyBtn);
        headerBar.addSeparator();
        headerBar.add(new JLabel("Format: "));
        headerBar.add(formatBadgeLabel);

        add(headerBar, BorderLayout.NORTH);

        // Loading Overlay Panel
        createLoadingPanel();

        if (plugin instanceof GenericFallbackPlugin) {
            genericViewerPanel = new GenericViewerPanel(file);
            add(genericViewerPanel, BorderLayout.CENTER);
        } else {
            // Left Side: Syntax Code Editor
            editorPanel = new EditorPanel();
            editorPanel.setSyntaxStyle(plugin.getSyntaxStyleKey());

            // Right Side: Tabbed Inspector Components wrapped in DockablePanelWrapper
            inspectorTabPane = new JTabbedPane();

            // Table Grid / XML Grouped Table View
            if (plugin.getFileType() == FileType.XML) {
                xmlGroupedTableView = new XmlGroupedTableView();
                DockablePanelWrapper gridDockWrapper = new DockablePanelWrapper("📊 XML Grouped Table View", xmlGroupedTableView, inspectorTabPane);
                inspectorTabPane.addTab("📊 Grouped Table View", gridDockWrapper);
            } else if (plugin.hasTableSupport()) {
                tableGridPanel = new TableGridPanel();
                DockablePanelWrapper gridDockWrapper = new DockablePanelWrapper("📊 Data Table Grid", tableGridPanel, inspectorTabPane);
                inspectorTabPane.addTab("📊 Data Table Grid", gridDockWrapper);
            }

            // Tree Inspector View
            if (plugin.hasTreeSupport()) {
                treeInspectorPanel = new TreeInspectorPanel();
                DockablePanelWrapper treeDockWrapper = new DockablePanelWrapper("🌳 Rich Tree View", treeInspectorPanel, inspectorTabPane);
                inspectorTabPane.addTab("🌳 Rich Tree View", treeDockWrapper);
            }

            // Query Console View
            if (plugin.getQueryEngine() != null) {
                queryConsolePanel = new QueryConsolePanel(plugin.getQueryEngine(), file, () -> editorPanel != null ? editorPanel.getText() : "");
                DockablePanelWrapper queryDockWrapper = new DockablePanelWrapper("⚡ Query Console", queryConsolePanel, inspectorTabPane);
                inspectorTabPane.addTab("⚡ Query Console", queryDockWrapper);
            }

            // Create Side-by-Side Horizontal Split Pane
            mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, inspectorTabPane);
            mainSplitPane.setDividerLocation(520);
            mainSplitPane.setResizeWeight(0.45);

            add(loadingPanel, BorderLayout.CENTER);
            loadFileAsync();
        }
    }

    private void createLoadingPanel() {
        loadingPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel iconLabel = new JLabel("⏳");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        loadingPanel.add(iconLabel, gbc);

        gbc.gridy++;
        loadingStatusLabel = new JLabel("Loading " + file.getName() + " (" + formatFileSize(file.length()) + ")...");
        loadingStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadingStatusLabel.setForeground(new Color(0x38, 0xbd, 0xf8));
        loadingPanel.add(loadingStatusLabel, gbc);

        gbc.gridy++;
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setIndeterminate(true);
        loadingProgressBar.setPreferredSize(new Dimension(360, 18));
        loadingPanel.add(loadingProgressBar, gbc);
    }

    public void setStatusUpdateListener(StatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    private void setSideBySideMode() {
        if (mainSplitPane != null) {
            mainSplitPane.setLeftComponent(editorPanel);
            mainSplitPane.setRightComponent(inspectorTabPane);
            mainSplitPane.setDividerLocation(520);
        }
    }

    private void setEditorOnlyMode() {
        if (mainSplitPane != null) {
            mainSplitPane.setLeftComponent(editorPanel);
            mainSplitPane.setRightComponent(null);
        }
    }

    private void setInspectorOnlyMode() {
        if (mainSplitPane != null) {
            mainSplitPane.setLeftComponent(null);
            mainSplitPane.setRightComponent(inspectorTabPane);
        }
    }

    private void loadFileAsync() {
        long startTime = System.currentTimeMillis();
        new Thread(() -> {
            try {
                updateStatus("⏳ Sniffing magic bytes & parsing " + file.getName() + "...");

                if (file.length() < 15_000_000) { // Under 15MB text read directly into editor
                    String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                    SwingUtilities.invokeLater(() -> {
                        if (editorPanel != null) {
                            editorPanel.setText(text);
                        }

                        if (treeInspectorPanel != null) {
                            if (plugin.getFileType() == FileType.XML) {
                                treeInspectorPanel.loadXmlTree(text);
                            } else if (plugin.getFileType() == FileType.JSON) {
                                treeInspectorPanel.loadJsonTree(text);
                            }
                        }

                        if (xmlGroupedTableView != null) {
                            xmlGroupedTableView.loadXml(text);
                        }

                        if (tableGridPanel != null && plugin.getFileType() == FileType.CSV) {
                            loadCsvIntoGrid(text);
                        }

                        // Remove loading panel and swap to workspace
                        remove(loadingPanel);
                        add(mainSplitPane, BorderLayout.CENTER);
                        revalidate();
                        repaint();

                        long duration = System.currentTimeMillis() - startTime;
                        String statusMsg = String.format("✅ Loaded %s (%s) in %d ms | Format: %s",
                                file.getName(), formatFileSize(file.length()), duration, plugin.getDisplayName());
                        updateStatus(statusMsg);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        if (editorPanel != null) {
                            editorPanel.setText("// Large file (" + formatFileSize(file.length()) + "). Use Query Console or Table view.");
                        }
                        remove(loadingPanel);
                        add(mainSplitPane, BorderLayout.CENTER);
                        revalidate();
                        repaint();

                        updateStatus("✅ Opened large file: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    if (editorPanel != null) {
                        editorPanel.setText("// Error reading file: " + e.getMessage());
                    }
                    remove(loadingPanel);
                    add(mainSplitPane, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                    updateStatus("❌ Error loading " + file.getName() + ": " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateStatus(String status) {
        if (statusUpdateListener != null) {
            statusUpdateListener.onStatusUpdate(status);
        }
    }

    private void loadCsvIntoGrid(String csvText) {
        if (csvText == null || csvText.trim().isEmpty()) return;
        try {
            String[] lines = csvText.split("\r?\n");
            if (lines.length == 0) return;

            String delimiter = ",";
            if (lines[0].contains("\t")) delimiter = "\t";
            else if (lines[0].contains(";")) delimiter = ";";
            else if (lines[0].contains("|")) delimiter = "\\|";

            String[] headers = lines[0].split(delimiter);
            java.util.List<String> cols = new ArrayList<>(Arrays.asList(headers));
            java.util.List<java.util.List<Object>> rows = new ArrayList<>();

            for (int i = 1; i < Math.min(lines.length, 5000); i++) {
                String[] parts = lines[i].split(delimiter);
                java.util.List<Object> row = new ArrayList<>(Arrays.asList(parts));
                rows.add(row);
            }

            tableGridPanel.setData(cols, rows);
        } catch (Exception e) {
            // Ignore CSV preview parse error
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public File getFile() { return file; }
    public FileTypePlugin getPlugin() { return plugin; }
    public EditorPanel getEditorPanel() { return editorPanel; }
}
