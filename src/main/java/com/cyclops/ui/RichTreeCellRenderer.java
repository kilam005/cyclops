package com.cyclops.ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Rich, colorful tree cell renderer for XML, JSON, and Schema trees with color-coded syntax highlighting and icons.
 */
public class RichTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();

            if (obj != null) {
                String str = obj.toString();
                setText(formatColoredHtml(str, leaf));
            }
        }

        setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
        return this;
    }

    private String formatColoredHtml(String raw, boolean isLeaf) {
        if (raw.startsWith("XML Document:")) {
            return "<html><b style='color:#38bdf8;'>📁 " + escape(raw) + "</b></html>";
        }
        if (raw.startsWith("JSON Root")) {
            return "<html><b style='color:#c084fc;'>📁 " + escape(raw) + "</b></html>";
        }

        // XML Element Node parsing format: "elementName [attr1=val1, attr2=val2]"
        if (raw.contains(" [") && raw.endsWith("]")) {
            int bracketIdx = raw.indexOf(" [");
            String tagName = raw.substring(0, bracketIdx);
            String attrs = raw.substring(bracketIdx + 2, raw.length() - 1);

            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b style='color:#38bdf8;'>&lt;").append(escape(tagName)).append("&gt;</b> ");

            String[] attrPairs = attrs.split(", ");
            for (String pair : attrPairs) {
                int eqIdx = pair.indexOf('=');
                if (eqIdx > 0) {
                    String k = pair.substring(0, eqIdx);
                    String v = pair.substring(eqIdx + 1);
                    sb.append("<span style='color:#fbbf24;'>").append(escape(k)).append("</span>=")
                      .append("<span style='color:#a3e635;'>\"").append(escape(v)).append("\"</span> ");
                } else {
                    sb.append("<span style='color:#fbbf24;'>").append(escape(pair)).append("</span> ");
                }
            }
            sb.append("</html>");
            return sb.toString();
        }

        // Simple XML Tag
        if (!raw.contains(":") && !raw.startsWith("value:")) {
            return "<html><b style='color:#818cf8;'>&lt;" + escape(raw) + "&gt;</b></html>";
        }

        // Value node: "value: textContent"
        if (raw.startsWith("value: ")) {
            String val = raw.substring(7);
            return "<html><span style='color:#94a3b8;'>text:</span> <b style='color:#4ade80;'>\"" + escape(val) + "\"</b></html>";
        }

        // Key-Value Pair: "key: value"
        if (raw.contains(": ")) {
            int colonIdx = raw.indexOf(": ");
            String key = raw.substring(0, colonIdx);
            String val = raw.substring(colonIdx + 2);

            return "<html><b style='color:#c084fc;'>" + escape(key) + ":</b> <span style='color:#f472b6;'>" + escape(val) + "</span></html>";
        }

        return "<html><span style='color:#e2e8f0;'>" + escape(raw) + "</span></html>";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
