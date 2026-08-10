package com.cyclops.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Native-style Glassmorphic Drag & Drop Overlay for Cyclops Main Frame.
 * Appears automatically when dragging files over the application window.
 */
public class DragDropOverlayPanel extends JPanel {

    public DragDropOverlayPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setVisible(false);

        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Semi-transparent dark glass background
                g2.setColor(new Color(0x0d, 0x11, 0x17, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Dashed cyan border
                g2.setColor(new Color(0x38, 0xbd, 0xf8));
                Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                g2.setStroke(dashed);
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));

        JLabel iconLabel = new JLabel("📥");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 54));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Drop Files Here to Open in Cyclops");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0x38, 0xbd, 0xf8));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("Supports XML, JSON, CSV, Parquet, Avro, ORC, & Generic Text/Logs");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(0x94, 0xa3, 0xb8));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(iconLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(descLabel);

        add(box);
    }
}
