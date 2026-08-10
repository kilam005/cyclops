package com.cyclops.service;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages dynamic Light & Dark theme switching across the Cyclops IDE.
 */
public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private boolean darkTheme = true;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void setDarkTheme(boolean dark) {
        if (this.darkTheme == dark) return;
        this.darkTheme = dark;
        applyTheme();
    }

    public void toggleTheme() {
        setDarkTheme(!darkTheme);
    }

    public void applyTheme() {
        try {
            // Configure FlatLaf Scrollbar properties (Auto-hide on hover, translucent, padded)
            UIManager.put("ScrollBar.autoHide", true);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.trackShowable", false);
            UIManager.put("ScrollBar.showButtons", false);

            if (darkTheme) {
                FlatMacDarkLaf.setup();
                UIManager.put("ScrollBar.thumb", new Color(255, 255, 255, 60));
                UIManager.put("ScrollBar.hoverThumbColor", new Color(255, 255, 255, 140));
            } else {
                FlatMacLightLaf.setup();
                UIManager.put("ScrollBar.thumb", new Color(0, 0, 0, 60));
                UIManager.put("ScrollBar.hoverThumbColor", new Color(0, 0, 0, 140));
            }
            FlatLaf.updateUI();

            for (ThemeChangeListener listener : listeners) {
                try {
                    listener.onThemeChanged(darkTheme);
                } catch (Exception e) {
                    // Ignore listener errors
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeDefaultTheme() {
        applyTheme();
    }
}
