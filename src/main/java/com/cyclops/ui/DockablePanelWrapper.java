package com.cyclops.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Wrapper allowing any panel (Data Table Grid, Rich Tree View, Query Console, Code Editor)
 * to be popped out into an independent floating window or docked back seamlessly.
 */
public class DockablePanelWrapper extends JPanel {
    private final String title;
    private final Component contentComponent;
    private final JTabbedPane parentTabPane;

    private JDialog floatingDialog;
    private JPanel placeholderPanel;
    private boolean isFloating = false;
    private final JButton floatBtn;

    public DockablePanelWrapper(String title, Component contentComponent, JTabbedPane parentTabPane) {
        this.title = title;
        this.contentComponent = contentComponent;
        this.parentTabPane = parentTabPane;

        setLayout(new BorderLayout());

        // Header Control Toolbar
        JToolBar headerToolBar = new JToolBar();
        headerToolBar.setFloatable(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        floatBtn = new JButton("↗ Float Window");
        floatBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        floatBtn.setToolTipText("Pop out this view into an independent floating desktop window");
        floatBtn.addActionListener(e -> toggleFloatState());

        headerToolBar.add(titleLabel);
        headerToolBar.add(Box.createHorizontalGlue());
        headerToolBar.add(floatBtn);

        add(headerToolBar, BorderLayout.NORTH);
        add(contentComponent, BorderLayout.CENTER);

        createPlaceholder();
    }

    private void createPlaceholder() {
        placeholderPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel label = new JLabel("<html><center><b>" + title + "</b><br>Currently floating as an independent window.<br>You can move it anywhere on your desktop.</center></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        placeholderPanel.add(label, gbc);

        gbc.gridy++;
        JButton dockBackBtn = new JButton("↙ Dock Back to Main Window");
        dockBackBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        dockBackBtn.addActionListener(e -> dockBack());
        placeholderPanel.add(dockBackBtn, gbc);
    }

    public void toggleFloatState() {
        if (isFloating) {
            dockBack();
        } else {
            floatWindow();
        }
    }

    public void floatWindow() {
        if (isFloating) return;

        Window topLevel = SwingUtilities.getWindowAncestor(this);
        floatingDialog = new JDialog(topLevel, title, Dialog.ModalityType.MODELESS);
        floatingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        floatingDialog.setSize(750, 550);
        floatingDialog.setLocationRelativeTo(topLevel);

        floatingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dockBack();
            }
        });

        // Move component into floating dialog
        remove(contentComponent);
        add(placeholderPanel, BorderLayout.CENTER);

        JPanel dialogContent = new JPanel(new BorderLayout());
        JToolBar dialogToolbar = new JToolBar();
        dialogToolbar.setFloatable(false);
        JButton dockBtn = new JButton("↙ Dock Back to Main Window");
        dockBtn.addActionListener(e -> dockBack());
        dialogToolbar.add(Box.createHorizontalGlue());
        dialogToolbar.add(dockBtn);

        dialogContent.add(dialogToolbar, BorderLayout.NORTH);
        dialogContent.add(contentComponent, BorderLayout.CENTER);

        floatingDialog.setContentPane(dialogContent);
        floatingDialog.setVisible(true);

        isFloating = true;
        floatBtn.setText("↙ Dock Back");
        revalidate();
        repaint();
    }

    public void dockBack() {
        if (!isFloating) return;

        if (floatingDialog != null) {
            floatingDialog.getContentPane().removeAll();
            floatingDialog.dispose();
            floatingDialog = null;
        }

        remove(placeholderPanel);
        add(contentComponent, BorderLayout.CENTER);

        isFloating = false;
        floatBtn.setText("↗ Float Window");
        revalidate();
        repaint();
    }
}
