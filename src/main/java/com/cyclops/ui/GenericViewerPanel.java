package com.cyclops.ui;

import com.cyclops.model.FileHeader;
import com.cyclops.service.FileDetector;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Universal Generic Viewer for viewing un-mentioned file formats in Text or Raw Hex view.
 */
public class GenericViewerPanel extends JPanel {
    private final EditorPanel editorPanel;
    private final JTextArea hexTextArea;
    private final JTabbedPane tabbedPane;
    private final JLabel fileStatsLabel;
    private final File targetFile;

    public GenericViewerPanel(File file) {
        this.targetFile = file;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        editorPanel = new EditorPanel();
        hexTextArea = new JTextArea();
        hexTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        hexTextArea.setEditable(false);

        tabbedPane.addTab("📄 Text View", editorPanel);
        tabbedPane.addTab("🔢 Hex Dump View", new JScrollPane(hexTextArea));

        fileStatsLabel = new JLabel("File Stats: Loading...");
        fileStatsLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        add(tabbedPane, BorderLayout.CENTER);
        add(fileStatsLabel, BorderLayout.SOUTH);

        loadFileContent();
    }

    private void loadFileContent() {
        if (targetFile == null || !targetFile.exists()) {
            editorPanel.setText("// File does not exist or has been removed.");
            fileStatsLabel.setText("File status: Missing.");
            return;
        }

        FileHeader header = FileDetector.inspect(targetFile);

        try {
            // Check if file contains null bytes (indicator of binary vs text)
            byte[] bytes = header.getMagicBytes();
            boolean isBinary = false;
            for (byte b : bytes) {
                if (b == 0) {
                    isBinary = true;
                    break;
                }
            }

            if (!isBinary && targetFile.length() < 15_000_000) { // Text file under 15MB
                String text = Files.readString(targetFile.toPath(), StandardCharsets.UTF_8);
                editorPanel.setText(text);
            } else {
                editorPanel.setText("// Binary file or large file content preview (" + targetFile.length() + " bytes).\n// Switch to 'Hex Dump View' tab to inspect raw bytes.");
                tabbedPane.setSelectedComponent(hexTextArea.getParent().getParent());
            }

            // Generate Hex Dump preview
            hexTextArea.setText(generateHexDump(bytes));

            fileStatsLabel.setText(String.format("File: %s | Size: %,d bytes | Mode: Generic %s Viewer",
                    targetFile.getName(), targetFile.length(), isBinary ? "Binary" : "Text"));

        } catch (Exception e) {
            editorPanel.setText("// Error reading file: " + e.getMessage());
            fileStatsLabel.setText("Error reading file: " + e.getMessage());
        }
    }

    private String generateHexDump(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "No preview bytes available.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s  %-47s  %s\n", "OFFSET", "BYTES (HEX)", "ASCII"));
        sb.append("--------------------------------------------------------------------------------\n");

        for (int i = 0; i < bytes.length; i += 16) {
            sb.append(String.format("%08X  ", i));

            // Hex values
            for (int j = 0; j < 16; j++) {
                if (i + j < bytes.length) {
                    sb.append(String.format("%02X ", bytes[i + j]));
                } else {
                    sb.append("   ");
                }
            }

            sb.append(" ");

            // ASCII printable values
            for (int j = 0; j < 16; j++) {
                if (i + j < bytes.length) {
                    byte b = bytes[i + j];
                    if (b >= 32 && b <= 126) {
                        sb.append((char) b);
                    } else {
                        sb.append('.');
                    }
                }
            }

            sb.append("\n");
            if (i >= 4096) {
                sb.append("\n... [Truncated preview at 4KB] ...");
                break;
            }
        }

        return sb.toString();
    }
}
