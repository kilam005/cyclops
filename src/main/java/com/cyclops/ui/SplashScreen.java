package com.cyclops.ui;

import com.cyclops.service.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Splash Screen displayed during startup for at least 5 seconds showing the Cyclops title and tagline.
 */
public class SplashScreen extends JWindow {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public SplashScreen() {
        setSize(560, 320);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x38, 0xbd, 0xf8), 2),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        mainPanel.setBackground(new Color(0x0d, 0x11, 0x17)); // Dark glass IDE theme

        // Center Content Box
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel logoLabel = new JLabel("👁 Cyclops");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 38));
        logoLabel.setForeground(new Color(0x38, 0xbd, 0xf8));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("\"One eye for all your data\"");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        taglineLabel.setForeground(new Color(0x94, 0xa3, 0xb8));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version 1.0.0 Enterprise Data IDE");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(0x64, 0x74, 0x8b));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(logoLabel);
        centerBox.add(Box.createVerticalStrut(8));
        centerBox.add(taglineLabel);
        centerBox.add(Box.createVerticalStrut(12));
        centerBox.add(versionLabel);
        centerBox.add(Box.createVerticalStrut(24));

        // Progress Bar & Loading Status
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0x38, 0xbd, 0xf8));
        progressBar.setBackground(new Color(0x16, 0x1b, 0x22));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(480, 22));

        statusLabel = new JLabel("Initializing Cyclops Data Engines...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerBox.add(progressBar);
        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(statusLabel);

        mainPanel.add(centerBox, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void showSplashAndLoad(Runnable onComplete) {
        setVisible(true);

        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            String[] steps = new String[]{
                "Loading FlatLaf dark theme tokens...",
                "Initializing Saxon-HE XPath 3.1 & XQuery 3.1 engine...",
                "Bootstrapping Jayway JSONPath engine...",
                "Setting up DuckDB SQL engine...",
                "Scanning magic byte file detector plugins...",
                "Configuring side-by-side dockable workspace...",
                "Loading sample datasets...",
                "Cyclops IDE Ready!"
            };

            for (int i = 0; i < steps.length; i++) {
                final int progress = (int) (((i + 1) / (float) steps.length) * 100);
                final String text = steps[i];
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progress);
                    statusLabel.setText(text);
                });

                try {
                    Thread.sleep(600); // Enforce smooth loading progress
                } catch (InterruptedException e) {
                    // Ignore
                }
            }

            // Enforce at least 5 seconds total splash time
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < 5000) {
                try {
                    Thread.sleep(5000 - elapsed);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }

            SwingUtilities.invokeLater(() -> {
                dispose();
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        }).start();
    }
}
