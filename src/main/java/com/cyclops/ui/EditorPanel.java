package com.cyclops.ui;

import com.cyclops.service.ThemeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Modern code editor panel powered by RSyntaxTextArea with line numbers, code folding, and formatting options.
 */
public class EditorPanel extends JPanel {
    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final JLabel infoLabel;
    private String currentSyntaxStyle = SyntaxConstants.SYNTAX_STYLE_NONE;

    public EditorPanel() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea();
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setLineWrap(false);
        textArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);

        // Toolbar for editor tools (Prettify, Minify, Word Wrap, Find)
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton prettifyBtn = new JButton("✨ Format / Prettify");
        prettifyBtn.setToolTipText("Format XML or JSON document cleanly");
        prettifyBtn.addActionListener(e -> formatContent());

        JButton searchBtn = new JButton("🔍 Find");
        searchBtn.addActionListener(e -> showFindDialog());

        JToggleButton wrapToggle = new JToggleButton("↩ Word Wrap");
        wrapToggle.addActionListener(e -> textArea.setLineWrap(wrapToggle.isSelected()));

        infoLabel = new JLabel("Lines: 0  |  Chars: 0");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        toolBar.add(prettifyBtn);
        toolBar.addSeparator();
        toolBar.add(searchBtn);
        toolBar.add(wrapToggle);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(infoLabel);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Listen for document updates to update metrics
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateMetrics(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateMetrics(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateMetrics(); }
        });

        // Apply initial theme
        applyTheme(ThemeManager.getInstance().isDarkTheme());

        // Register theme listener
        ThemeManager.getInstance().addThemeChangeListener(this::applyTheme);
    }

    public void setSyntaxStyle(String styleKey) {
        this.currentSyntaxStyle = styleKey != null ? styleKey : SyntaxConstants.SYNTAX_STYLE_NONE;
        textArea.setSyntaxEditingStyle(this.currentSyntaxStyle);
    }

    public void setText(String text) {
        textArea.setText(text != null ? text : "");
        textArea.setCaretPosition(0);
        updateMetrics();
    }

    public String getText() {
        return textArea.getText();
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    private void updateMetrics() {
        int lineCount = textArea.getLineCount();
        int charCount = textArea.getText().length();
        infoLabel.setText(String.format("Lines: %,d  |  Chars: %,d", lineCount, charCount));
    }

    private void formatContent() {
        String text = textArea.getText();
        if (text == null || text.trim().isEmpty()) return;

        try {
            if (SyntaxConstants.SYNTAX_STYLE_JSON.equals(currentSyntaxStyle) || text.trim().startsWith("{") || text.trim().startsWith("[")) {
                ObjectMapper mapper = new ObjectMapper();
                Object json = mapper.readValue(text, Object.class);
                String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                setText(prettyJson);
            } else if (SyntaxConstants.SYNTAX_STYLE_XML.equals(currentSyntaxStyle) || text.trim().startsWith("<")) {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                StreamResult result = new StreamResult(new StringWriter());
                transformer.transform(new StreamSource(new StringReader(text)), result);
                setText(result.getWriter().toString());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not format document: " + e.getMessage(), "Format Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showFindDialog() {
        String search = JOptionPane.showInputDialog(this, "Enter search string:", "Find Text", JOptionPane.QUESTION_MESSAGE);
        if (search != null && !search.isEmpty()) {
            int pos = textArea.getText().toLowerCase().indexOf(search.toLowerCase(), textArea.getCaretPosition());
            if (pos >= 0) {
                textArea.setCaretPosition(pos);
                textArea.select(pos, pos + search.length());
                textArea.requestFocusInWindow();
            } else {
                JOptionPane.showMessageDialog(this, "Search string not found.", "Find", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void applyTheme(boolean isDark) {
        try {
            String themePath = isDark ? "/org/fife/ui/rsyntaxtextarea/themes/dark.xml" : "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
            InputStream in = getClass().getResourceAsStream(themePath);
            if (in != null) {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            } else {
                if (isDark) {
                    textArea.setBackground(new Color(0x1e, 0x1e, 0x1e));
                    textArea.setForeground(new Color(0xd4, 0xd4, 0xd4));
                    textArea.setCaretColor(Color.WHITE);
                } else {
                    textArea.setBackground(Color.WHITE);
                    textArea.setForeground(Color.BLACK);
                    textArea.setCaretColor(Color.BLACK);
                }
            }
        } catch (Exception e) {
            // Ignore theme load exception
        }
    }
}
