package com.cyclops;

import com.cyclops.service.ThemeManager;
import com.cyclops.ui.CyclopsMainFrame;
import com.cyclops.ui.SplashScreen;

import javax.swing.SwingUtilities;

/**
 * Application Entry Point for Cyclops IDE.
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Cyclops");
        System.setProperty("flatlaf.useWindowDecorations", "true");

        // Initialize UI theme
        ThemeManager.getInstance().initializeDefaultTheme();

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showSplashAndLoad(() -> {
                CyclopsMainFrame frame = new CyclopsMainFrame();
                frame.setVisible(true);
            });
        });
    }
}
