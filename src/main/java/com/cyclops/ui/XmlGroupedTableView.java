package com.cyclops.ui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * Universal Infinite-Depth Hierarchical XML Tree-Grid Viewer with Live Node & Row Search Filtering:
 * - Top Toolbar Search Field dynamically filters property rows, data table rows, and tree nodes.
 * - Auto-expands matching sections to reveal matching rows instantly.
 */
public class XmlGroupedTableView extends JPanel {
    private final JPanel mainContainer;
    private final JScrollPane scrollPane;
    private final JLabel headerTitleLabel;
    private final JTextField searchField;
    private final List<XmlNodeController> nodeControllers = new ArrayList<>();

    public interface XmlNodeController {
        boolean filter(String query);
    }

    public XmlGroupedTableView() {
        setLayout(new BorderLayout());

        mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        headerTitleLabel = new JLabel(" XML Tree-Grid View ");
        headerTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        searchField = new JTextField(18);
        searchField.setToolTipText("Filter XML nodes, properties, or row values...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        JButton expandAllBtn = new JButton("▼ Expand All");
        expandAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        expandAllBtn.addActionListener(e -> toggleAllSections(mainContainer, true));

        JButton collapseAllBtn = new JButton("▶ Collapse All");
        collapseAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        collapseAllBtn.addActionListener(e -> toggleAllSections(mainContainer, false));

        toolBar.add(headerTitleLabel);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(new JLabel("🔍 Filter: "));
        toolBar.add(searchField);
        toolBar.addSeparator();
        toolBar.add(expandAllBtn);
        toolBar.add(collapseAllBtn);

        add(toolBar, BorderLayout.NORTH);

        scrollPane = new JScrollPane(mainContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadXml(String xmlContent) {
        mainContainer.removeAll();
        nodeControllers.clear();
        searchField.setText("");

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            mainContainer.revalidate();
            mainContainer.repaint();
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            Element root = doc.getDocumentElement();
            if (root != null) {
                headerTitleLabel.setText(" XML Grid View: <" + root.getNodeName() + ">");
                XmlNodePanel rootNodePanel = buildRecursiveGridNode(root, 0);
                mainContainer.add(rootNodePanel.getPanel());
                nodeControllers.add(rootNodePanel);
            }

        } catch (Exception e) {
            headerTitleLabel.setText(" XML Grid Parse Error: " + e.getMessage());
        }

        mainContainer.revalidate();
        mainContainer.repaint();
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();
        for (XmlNodeController controller : nodeControllers) {
            controller.filter(query);
        }
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    /**
     * XML Node Controller panel wrapping expandable tree-grid node & filtering behavior.
     */
    private class XmlNodePanel implements XmlNodeController {
        private final JPanel container;
        private final JButton toggleBtn;
        private final JPanel contentBox;
        private final String tagTitle;
        private final List<XmlNodeController> childControllers = new ArrayList<>();

        private TableRowSorter<DefaultTableModel> propTableSorter;
        private final List<TableRowSorter<DefaultTableModel>> dataTableSorters = new ArrayList<>();
        private final List<JPanel> tablePanels = new ArrayList<>();

        public XmlNodePanel(Element element, int depth) {
            container = new JPanel(new BorderLayout());
            int borderColorAlpha = Math.max(30, 160 - (depth * 25));
            container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x38, 0xbd, 0xf8, borderColorAlpha)));

            // Header section for node name & attributes
            JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            headerBar.setOpaque(true);
            headerBar.setBackground(getDepthHeaderColor(depth));

            toggleBtn = new JButton("▼");
            toggleBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            toggleBtn.setMargin(new Insets(0, 2, 0, 2));
            toggleBtn.setFocusable(false);

            tagTitle = buildHeaderTitle(element);
            JLabel tagLabel = new JLabel(tagTitle);
            tagLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            tagLabel.setForeground(getDepthTextColor(depth));

            headerBar.add(toggleBtn);
            headerBar.add(tagLabel);

            contentBox = new JPanel();
            contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
            int leftIndent = Math.min(depth * 12 + 12, 60);
            contentBox.setBorder(BorderFactory.createEmptyBorder(4, leftIndent, 6, 4));

            toggleBtn.addActionListener(e -> {
                boolean visible = !contentBox.isVisible();
                contentBox.setVisible(visible);
                toggleBtn.setText(visible ? "▼" : "▶");
                container.revalidate();
            });

            // 1. Gather Attributes & Direct Simple Text Fields for Property Table
            List<String[]> propertyRows = new ArrayList<>();
            for (int i = 0; i < element.getAttributes().getLength(); i++) {
                Node attr = element.getAttributes().item(i);
                propertyRows.add(new String[]{"@" + attr.getNodeName(), attr.getNodeValue()});
            }

            Map<String, List<Element>> childGroups = new LinkedHashMap<>();
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) n;
                    childGroups.computeIfAbsent(child.getNodeName(), k -> new ArrayList<>()).add(child);
                }
            }

            Map<String, List<Element>> complexChildGroups = new LinkedHashMap<>();
            for (Map.Entry<String, List<Element>> entry : childGroups.entrySet()) {
                String tagName = entry.getKey();
                List<Element> list = entry.getValue();

                if (list.size() == 1 && isSimpleTextElement(list.get(0))) {
                    propertyRows.add(new String[]{tagName, list.get(0).getTextContent().trim()});
                } else {
                    complexChildGroups.put(tagName, list);
                }
            }

            if (!propertyRows.isEmpty()) {
                JPanel propTablePanel = createPropertyTable(propertyRows);
                contentBox.add(propTablePanel);
                contentBox.add(Box.createVerticalStrut(4));
            }

            // 2. Process Complex Child Element Groups
            for (Map.Entry<String, List<Element>> entry : complexChildGroups.entrySet()) {
                String tagName = entry.getKey();
                List<Element> list = entry.getValue();

                if (list.size() == 1) {
                    Element singleChild = list.get(0);
                    if (hasOnlySimpleChildren(singleChild)) {
                        JPanel p = createSingleRowTablePanel(tagName, singleChild);
                        contentBox.add(p);
                        contentBox.add(Box.createVerticalStrut(4));
                    } else {
                        XmlNodePanel childNode = buildRecursiveGridNode(singleChild, depth + 1);
                        contentBox.add(childNode.getPanel());
                        contentBox.add(Box.createVerticalStrut(4));
                        childControllers.add(childNode);
                    }
                } else {
                    boolean itemsHaveDeepNesting = false;
                    for (Element item : list) {
                        if (!hasOnlySimpleChildren(item)) {
                            itemsHaveDeepNesting = true;
                            break;
                        }
                    }

                    if (itemsHaveDeepNesting) {
                        JPanel listContainer = new JPanel();
                        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
                        listContainer.setBorder(BorderFactory.createTitledBorder(" " + tagName + " (" + list.size() + " nested items) "));

                        for (Element item : list) {
                            XmlNodePanel childNode = buildRecursiveGridNode(item, depth + 1);
                            listContainer.add(childNode.getPanel());
                            listContainer.add(Box.createVerticalStrut(4));
                            childControllers.add(childNode);
                        }
                        contentBox.add(listContainer);
                        contentBox.add(Box.createVerticalStrut(6));
                    } else {
                        JPanel p = createMultiRowTablePanel(tagName, list);
                        contentBox.add(p);
                        contentBox.add(Box.createVerticalStrut(6));
                    }
                }
            }

            container.add(headerBar, BorderLayout.NORTH);
            container.add(contentBox, BorderLayout.CENTER);
        }

        public JPanel getPanel() {
            return container;
        }

        @Override
        public boolean filter(String query) {
            if (query == null || query.isEmpty()) {
                container.setVisible(true);
                if (propTableSorter != null) propTableSorter.setRowFilter(null);
                for (TableRowSorter<DefaultTableModel> sorter : dataTableSorters) {
                    sorter.setRowFilter(null);
                }
                for (JPanel p : tablePanels) p.setVisible(true);
                for (XmlNodeController child : childControllers) child.filter(query);
                return true;
            }

            boolean selfMatch = tagTitle.toLowerCase().contains(query);
            boolean childMatch = false;

            // Filter Property Table
            if (propTableSorter != null) {
                try {
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query));
                    propTableSorter.setRowFilter(rf);
                    if (propTableSorter.getViewRowCount() > 0) childMatch = true;
                } catch (Exception e) {}
            }

            // Filter Data Tables
            for (int i = 0; i < dataTableSorters.size(); i++) {
                TableRowSorter<DefaultTableModel> sorter = dataTableSorters.get(i);
                JPanel tablePanel = tablePanels.get(i);
                try {
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query));
                    sorter.setRowFilter(rf);
                    boolean hasRows = sorter.getViewRowCount() > 0;
                    tablePanel.setVisible(hasRows);
                    if (hasRows) childMatch = true;
                } catch (Exception e) {}
            }

            // Filter Child Nodes
            for (XmlNodeController child : childControllers) {
                boolean m = child.filter(query);
                if (m) childMatch = true;
            }

            boolean isVisible = selfMatch || childMatch;
            container.setVisible(isVisible);
            if (isVisible) {
                contentBox.setVisible(true);
                toggleBtn.setText("▼");
            }
            return isVisible;
        }

        private JPanel createPropertyTable(List<String[]> properties) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Property", "Value"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            for (String[] prop : properties) {
                model.addRow(prop);
            }

            JTable table = new JTable(model);
            propTableSorter = new TableRowSorter<>(model);
            table.setRowSorter(propTableSorter);
            table.setRowHeight(22);
            table.getColumnModel().getColumn(0).setPreferredWidth(160);
            table.getColumnModel().getColumn(1).setPreferredWidth(450);

            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                    if (c == 0) {
                        setFont(getFont().deriveFont(Font.BOLD));
                        setForeground(new Color(0xfb, 0xbf, 0x24));
                    } else {
                        setForeground(new Color(0xe2, 0xe8, 0xf0));
                    }
                    return comp;
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(table, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createSingleRowTablePanel(String tagName, Element singleElement) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(" " + tagName + " (1 row) "));

            LinkedHashSet<String> cols = new LinkedHashSet<>();
            for (int i = 0; i < singleElement.getAttributes().getLength(); i++) {
                cols.add("@" + singleElement.getAttributes().item(i).getNodeName());
            }
            for (Element child : getDirectChildren(singleElement)) {
                cols.add(child.getNodeName());
            }

            List<String> colList = new ArrayList<>(cols);
            List<String> fullHeader = new ArrayList<>();
            fullHeader.add("#");
            fullHeader.addAll(colList);

            DefaultTableModel model = new DefaultTableModel(fullHeader.toArray(), 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            Vector<Object> row = new Vector<>();
            row.add(1);
            for (String col : colList) {
                if (col.startsWith("@")) {
                    String attr = col.substring(1);
                    row.add(singleElement.hasAttribute(attr) ? singleElement.getAttribute(attr) : "");
                } else {
                    List<Element> matches = getDirectChildElements(singleElement, col);
                    row.add(!matches.isEmpty() ? matches.get(0).getTextContent().trim() : "");
                }
            }
            model.addRow(row);

            JTable table = createStyledGridTable(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            dataTableSorters.add(sorter);
            tablePanels.add(panel);

            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(600, 84));

            panel.add(sp, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createMultiRowTablePanel(String tagName, List<Element> list) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(" " + tagName + " (" + list.size() + " rows) "));

            LinkedHashSet<String> cols = new LinkedHashSet<>();
            for (Element el : list) {
                for (int i = 0; i < el.getAttributes().getLength(); i++) {
                    cols.add("@" + el.getAttributes().item(i).getNodeName());
                }
                for (Element child : getDirectChildren(el)) {
                    cols.add(child.getNodeName());
                }
            }

            List<String> colList = new ArrayList<>(cols);
            List<String> fullHeader = new ArrayList<>();
            fullHeader.add("#");
            fullHeader.addAll(colList);

            DefaultTableModel model = new DefaultTableModel(fullHeader.toArray(), 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            int rowIdx = 1;
            for (Element el : list) {
                Vector<Object> row = new Vector<>();
                row.add(rowIdx++);
                for (String col : colList) {
                    if (col.startsWith("@")) {
                        String attr = col.substring(1);
                        row.add(el.hasAttribute(attr) ? el.getAttribute(attr) : "");
                    } else {
                        List<Element> matches = getDirectChildElements(el, col);
                        row.add(!matches.isEmpty() ? matches.get(0).getTextContent().trim() : "");
                    }
                }
                model.addRow(row);
            }

            JTable table = createStyledGridTable(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            dataTableSorters.add(sorter);
            tablePanels.add(panel);

            JScrollPane sp = new JScrollPane(table);
            int preferredHeight = Math.min(Math.max(list.size() * 28 + 48, 110), 300);
            sp.setPreferredSize(new Dimension(600, preferredHeight));

            panel.add(sp, BorderLayout.CENTER);
            return panel;
        }

        private JTable createStyledGridTable(DefaultTableModel model) {
            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setRowHeight(28);
            table.getTableHeader().setReorderingAllowed(true);

            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                    setBorder(BorderFactory.createEmptyBorder(2, 8, 4, 8));
                    return comp;
                }
            });

            table.getColumnModel().getColumn(0).setPreferredWidth(45);
            for (int c = 1; c < table.getColumnCount(); c++) {
                table.getColumnModel().getColumn(c).setPreferredWidth(140);
            }

            return table;
        }
    }

    private XmlNodePanel buildRecursiveGridNode(Element element, int depth) {
        return new XmlNodePanel(element, depth);
    }

    private Color getDepthHeaderColor(int depth) {
        switch (depth % 4) {
            case 0: return new Color(0x16, 0x1b, 0x22);
            case 1: return new Color(0x1e, 0x26, 0x32);
            case 2: return new Color(0x25, 0x30, 0x40);
            default: return new Color(0x2d, 0x3b, 0x4e);
        }
    }

    private Color getDepthTextColor(int depth) {
        switch (depth % 4) {
            case 0: return new Color(0x38, 0xbd, 0xf8); // Cyan
            case 1: return new Color(0x81, 0x8c, 0xf8); // Indigo
            case 2: return new Color(0xc0, 0x84, 0xfc); // Purple
            default: return new Color(0x2d, 0xd4, 0xbf); // Teal
        }
    }

    private String buildHeaderTitle(Element el) {
        StringBuilder sb = new StringBuilder("<").append(el.getNodeName()).append(">");
        for (int i = 0; i < el.getAttributes().getLength(); i++) {
            Node attr = el.getAttributes().item(i);
            sb.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
        }
        return sb.toString();
    }


    private void toggleAllSections(Container parent, boolean visible) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                if (p.getComponentCount() >= 2 && p.getComponent(1) instanceof JPanel) {
                    p.getComponent(1).setVisible(visible);
                }
                toggleAllSections(p, visible);
            }
        }
        parent.revalidate();
        parent.repaint();
    }

    private List<Element> getDirectChildElements(Element parent, String tagName) {
        List<Element> list = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tagName)) {
                list.add((Element) n);
            }
        }
        return list;
    }

    private List<Element> getDirectChildren(Element parent) {
        List<Element> list = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element) n);
            }
        }
        return list;
    }

    private boolean isSimpleTextElement(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOnlySimpleChildren(Element el) {
        for (Element child : getDirectChildren(el)) {
            if (!isSimpleTextElement(child)) return false;
        }
        return true;
    }
}
