package com.cyclops.ui;

import com.cyclops.service.BookmarkManager;
import com.cyclops.service.SampleDataLoader;
import com.cyclops.service.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary Desktop Window for Cyclops with Native Drag & Drop Overlay,
 * File Loading Status Feedback, Active Tab Stopwatch Clock, and Dockable Workspace.
 */
public class CyclopsMainFrame extends JFrame {
    private final JTabbedPane workspaceTabPane;
    private final JLabel statusLabel;
    private final JLabel activeTimeClockLabel;
    private final JButton themeToggleBtn;
    private final JButton bookmarksBtn;
    private final DragDropOverlayPanel dragDropOverlay;

    // Track active time spent per tab
    private final Map<Component, Long> tabActiveTimeMap = new HashMap<>();
    private Component currentActiveTab = null;
    private long lastTabSwitchTimestamp = System.currentTimeMillis();
    private Timer activeClockTimer;

    public CyclopsMainFrame() {
        setTitle("Cyclops - One eye for all your data");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 850);
        setLocationRelativeTo(null);

        // Header Toolbar
        JToolBar mainToolBar = new JToolBar();
        mainToolBar.setFloatable(false);

        JLabel logoLabel = new JLabel(" 👁 Cyclops ");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel taglineLabel = new JLabel("  \"One eye for all your data\"  ");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        taglineLabel.setForeground(Color.GRAY);

        JButton openFileBtn = new JButton("📂 Open File");
        openFileBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        openFileBtn.addActionListener(e -> chooseAndOpenFile());

        // Bookmarks Dropdown
        bookmarksBtn = new JButton("⭐ Bookmarks ▾");
        bookmarksBtn.addActionListener(e -> showBookmarksMenu());

        // Sample Files Dropdown
        JButton samplesBtn = new JButton("🧪 Load Samples ▾");
        JPopupMenu sampleMenu = createSampleMenu();
        samplesBtn.addActionListener(e -> sampleMenu.show(samplesBtn, 0, samplesBtn.getHeight()));

        themeToggleBtn = new JButton(ThemeManager.getInstance().isDarkTheme() ? "☀️ Light Mode" : "🌙 Dark Mode");
        themeToggleBtn.setToolTipText("Toggle Light / Dark UI Theme");
        themeToggleBtn.addActionListener(e -> {
            ThemeManager.getInstance().toggleTheme();
            themeToggleBtn.setText(ThemeManager.getInstance().isDarkTheme() ? "☀️ Light Mode" : "🌙 Dark Mode");
        });

        mainToolBar.add(logoLabel);
        mainToolBar.add(taglineLabel);
        mainToolBar.addSeparator();
        mainToolBar.add(openFileBtn);
        mainToolBar.add(bookmarksBtn);
        mainToolBar.add(samplesBtn);
        mainToolBar.add(Box.createHorizontalGlue());
        mainToolBar.add(themeToggleBtn);

        add(mainToolBar, BorderLayout.NORTH);

        // Layered Pane for Native Drag & Drop Overlay
        JLayeredPane layeredPane = getLayeredPane();

        // Workspace Tab Pane
        workspaceTabPane = new JTabbedPane();
        workspaceTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        workspaceTabPane.addTab("🏠 Welcome", createWelcomePanel());

        // Drag & Drop Glass Overlay
        dragDropOverlay = new DragDropOverlayPanel();
        dragDropOverlay.setBounds(0, 0, getWidth(), getHeight());

        add(workspaceTabPane, BorderLayout.CENTER);

        // Status Bar with Active Time Spent Clock & File Load Progress Status
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x33, 0x33, 0x33)));

        statusLabel = new JLabel(" Ready | Drag & Drop files anywhere into Cyclops to open.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        activeTimeClockLabel = new JLabel("⏱ Active Tab Time: 00m 00s  ");
        activeTimeClockLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        activeTimeClockLabel.setForeground(new Color(0x38, 0xbd, 0xf8));
        activeTimeClockLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        statusBarPanel.add(statusLabel, BorderLayout.CENTER);
        statusBarPanel.add(activeTimeClockLabel, BorderLayout.EAST);
        add(statusBarPanel, BorderLayout.SOUTH);

        // Resize overlay on window resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                dragDropOverlay.setBounds(0, 0, getWidth(), getHeight());
            }
        });

        // Tab Change Listener for Active Time Stopwatch
        workspaceTabPane.addChangeListener(e -> onTabChanged());

        // Initialize Active Tab Stopwatch Timer
        startActiveClockTimer();

        // Native Drag & Drop Handler with Visual Indicator Overlay
        setupNativeDragAndDrop();
    }

    private void startActiveClockTimer() {
        lastTabSwitchTimestamp = System.currentTimeMillis();
        activeClockTimer = new Timer(1000, e -> {
            Component selected = workspaceTabPane.getSelectedComponent();
            if (selected != null) {
                long now = System.currentTimeMillis();
                long prevAccumulated = tabActiveTimeMap.getOrDefault(selected, 0L);
                long currentSessionTime = now - lastTabSwitchTimestamp;
                long totalTimeMs = prevAccumulated + currentSessionTime;

                long seconds = (totalTimeMs / 1000) % 60;
                long minutes = (totalTimeMs / (1000 * 60)) % 60;
                long hours = totalTimeMs / (1000 * 60 * 60);

                if (hours > 0) {
                    activeTimeClockLabel.setText(String.format("⏱ Active Tab Time: %02dh %02dm %02ds  ", hours, minutes, seconds));
                } else {
                    activeTimeClockLabel.setText(String.format("⏱ Active Tab Time: %02dm %02ds  ", minutes, seconds));
                }
            }
        });
        activeClockTimer.start();
    }

    private void onTabChanged() {
        long now = System.currentTimeMillis();
        if (currentActiveTab != null) {
            long prevAccumulated = tabActiveTimeMap.getOrDefault(currentActiveTab, 0L);
            long sessionTime = now - lastTabSwitchTimestamp;
            tabActiveTimeMap.put(currentActiveTab, prevAccumulated + sessionTime);
        }
        currentActiveTab = workspaceTabPane.getSelectedComponent();
        lastTabSwitchTimestamp = now;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("👁 Cyclops");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel tag = new JLabel("\"One eye for all your data\"");
        tag.setFont(new Font("SansSerif", Font.ITALIC, 18));
        panel.add(tag, gbc);

        gbc.gridy++;
        JLabel desc = new JLabel("<html><center>Lightweight Data IDE with automated format detection,<br>Side-by-Side views, Rich XML Data Grids, Saxon-HE XPath 3.1, and Dockable Floating Windows.</center></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(desc, gbc);

        gbc.gridy++;
        JPanel btnBox = new JPanel(new FlowLayout());
        JButton openBtn = new JButton("📂 Open File");
        openBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        openBtn.addActionListener(e -> chooseAndOpenFile());

        JButton sampleXmlBtn = new JButton("🧪 Sample XML");
        sampleXmlBtn.addActionListener(e -> openFile(SampleDataLoader.createSampleXml()));

        JButton sampleJsonBtn = new JButton("🧪 Sample JSON");
        sampleJsonBtn.addActionListener(e -> openFile(SampleDataLoader.createSampleJson()));

        JButton sampleCsvBtn = new JButton("🧪 Sample CSV");
        sampleCsvBtn.addActionListener(e -> openFile(SampleDataLoader.createSampleCsv()));

        JButton sampleParquetBtn = new JButton("🧪 Sample Parquet");
        sampleParquetBtn.addActionListener(e -> openFile(SampleDataLoader.createSampleParquet()));

        btnBox.add(openBtn);
        btnBox.add(sampleXmlBtn);
        btnBox.add(sampleJsonBtn);
        btnBox.add(sampleCsvBtn);
        btnBox.add(sampleParquetBtn);

        panel.add(btnBox, gbc);
        return panel;
    }

    private void showBookmarksMenu() {
        JPopupMenu menu = new JPopupMenu();
        List<BookmarkManager.BookmarkItem> list = BookmarkManager.getInstance().getBookmarks();

        if (list.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("No bookmarks added yet. Click '⭐ Bookmark File' in any tab.");
            emptyItem.setEnabled(false);
            menu.add(emptyItem);
        } else {
            for (BookmarkManager.BookmarkItem item : list) {
                JMenuItem menuItem = new JMenuItem(item.toString());
                menuItem.addActionListener(e -> {
                    openFile(item.getFile());
                    Component comp = workspaceTabPane.getSelectedComponent();
                    if (comp instanceof DocumentTabPanel) {
                        DocumentTabPanel dtp = (DocumentTabPanel) comp;
                        if (dtp.getEditorPanel() != null) {
                            int line = Math.max(0, item.getLineNumber() - 1);
                            try {
                                int off = dtp.getEditorPanel().getTextArea().getLineStartOffset(line);
                                dtp.getEditorPanel().getTextArea().setCaretPosition(off);
                            } catch (Exception ex) {
                                // Ignore
                            }
                        }
                    }
                });
                menu.add(menuItem);
            }
        }

        menu.show(bookmarksBtn, 0, bookmarksBtn.getHeight());
    }

    private JPopupMenu createSampleMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem xmlItem = new JMenuItem("📄 XML (Purchase Orders)");
        xmlItem.addActionListener(e -> openFile(SampleDataLoader.createSampleXml()));

        JMenuItem jsonItem = new JMenuItem("📄 JSON (User Analytics)");
        jsonItem.addActionListener(e -> openFile(SampleDataLoader.createSampleJson()));

        JMenuItem csvItem = new JMenuItem("📄 CSV (Regional Sales)");
        csvItem.addActionListener(e -> openFile(SampleDataLoader.createSampleCsv()));

        JMenuItem parquetItem = new JMenuItem("📦 Parquet (Sales Data)");
        parquetItem.addActionListener(e -> openFile(SampleDataLoader.createSampleParquet()));

        JMenuItem logItem = new JMenuItem("📝 Generic Log (Application Server)");
        logItem.addActionListener(e -> openFile(SampleDataLoader.createSampleGenericLog()));

        menu.add(xmlItem);
        menu.add(jsonItem);
        menu.add(csvItem);
        menu.add(parquetItem);
        menu.add(logItem);
        return menu;
    }

    public void openFile(File file) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "File does not exist: " + (file != null ? file.getAbsolutePath() : "null"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if file is already open
        for (int i = 0; i < workspaceTabPane.getTabCount(); i++) {
            Component comp = workspaceTabPane.getComponentAt(i);
            if (comp instanceof DocumentTabPanel) {
                DocumentTabPanel dtp = (DocumentTabPanel) comp;
                if (dtp.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    workspaceTabPane.setSelectedIndex(i);
                    statusLabel.setText(" Switched to open tab: " + file.getAbsolutePath());
                    return;
                }
            }
        }

        // Open in new tab with loading status feedback
        statusLabel.setText(" ⏳ Loading " + file.getName() + "...");
        DocumentTabPanel tabPanel = new DocumentTabPanel(file);
        tabPanel.setStatusUpdateListener(statusText -> statusLabel.setText(" " + statusText));

        workspaceTabPane.addTab(file.getName(), tabPanel);
        int newIdx = workspaceTabPane.getTabCount() - 1;
        workspaceTabPane.setSelectedIndex(newIdx);

        // Add close button to tab header
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabHeader.setOpaque(false);
        JLabel titleLabel = new JLabel(file.getName());
        JButton closeBtn = new JButton("✕");
        closeBtn.setMargin(new Insets(0, 4, 0, 4));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusable(false);
        closeBtn.addActionListener(e -> {
            tabActiveTimeMap.remove(tabPanel);
            workspaceTabPane.remove(tabPanel);
        });

        tabHeader.add(titleLabel);
        tabHeader.add(closeBtn);
        workspaceTabPane.setTabComponentAt(newIdx, tabHeader);
    }

    private void chooseAndOpenFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Data File in Cyclops");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    private void setupNativeDragAndDrop() {
        setGlassPane(dragDropOverlay);

        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                getGlassPane().setVisible(true);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                getGlassPane().setVisible(true);
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {
                getGlassPane().setVisible(false);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                getGlassPane().setVisible(false);
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        openFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
