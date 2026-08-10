package com.cyclops.ui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * High-performance, flexible XML Data Grid featuring:
 * 1. XML Entity Group Selector Dropdown
 * 2. Automatic dot-notation flattening of single nested structures (e.g. customer.name)
 * 3. Multi-Tab Sub-Grid Details Inspector for array child structures
 */
public class XmlNestedGridPanel extends JPanel {
    private final JComboBox<EntityOption> entitySelectorCombo;
    private final JTable mainTable;
    private final DefaultTableModel mainTableModel;
    private final JTabbedPane detailTabPane;
    private final JLabel masterTitleLabel;
    private final JLabel detailTitleLabel;

    private final Map<String, List<Element>> allTagGroups = new LinkedHashMap<>();
    private final Map<Integer, Map<String, List<Element>>> rowNestedArraysMap = new HashMap<>();

    public static class EntityOption {
        private final String tagName;
        private final int count;

        public EntityOption(String tagName, int count) {
            this.tagName = tagName;
            this.count = count;
        }

        @Override
        public String toString() {
            return "<" + tagName + "> (" + count + " records)";
        }
    }

    public XmlNestedGridPanel() {
        setLayout(new BorderLayout());

        // Header Control Bar with Entity Group Selector
        JToolBar headerToolBar = new JToolBar();
        headerToolBar.setFloatable(false);

        masterTitleLabel = new JLabel(" Master XML Grid: ");
        masterTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        entitySelectorCombo = new JComboBox<>();
        entitySelectorCombo.setToolTipText("Switch between XML entity groups found in document");
        entitySelectorCombo.addActionListener(e -> {
            EntityOption selected = (EntityOption) entitySelectorCombo.getSelectedItem();
            if (selected != null) {
                renderEntityGroup(selected.tagName);
            }
        });

        headerToolBar.add(masterTitleLabel);
        headerToolBar.add(entitySelectorCombo);
        headerToolBar.add(Box.createHorizontalGlue());

        mainTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        mainTable = new JTable(mainTableModel);
        mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mainTable.setRowHeight(26);
        mainTable.getTableHeader().setReorderingAllowed(true);

        JPanel masterPanel = new JPanel(new BorderLayout());
        masterPanel.add(headerToolBar, BorderLayout.NORTH);
        masterPanel.add(new JScrollPane(mainTable), BorderLayout.CENTER);

        // Detail Sub-Grid Multi-Tab Inspector
        detailTabPane = new JTabbedPane();
        detailTitleLabel = new JLabel(" 📦 Sub-Grid Details (Select a row above to inspect child arrays)");
        detailTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel detailContainerPanel = new JPanel(new BorderLayout());
        detailContainerPanel.add(detailTitleLabel, BorderLayout.NORTH);
        detailContainerPanel.add(detailTabPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, masterPanel, detailContainerPanel);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.55);

        add(splitPane, BorderLayout.CENTER);

        // Master Row Selection Listener to populate Sub-Grid Tabs
        mainTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = mainTable.getSelectedRow();
                if (selectedRow >= 0) {
                    showNestedDetailsForSelectedRow(selectedRow);
                }
            }
        });
    }

    public void loadXml(String xmlContent) {
        allTagGroups.clear();
        entitySelectorCombo.removeAllItems();
        mainTableModel.setRowCount(0);
        mainTableModel.setColumnCount(0);
        rowNestedArraysMap.clear();
        detailTabPane.removeAll();

        if (xmlContent == null || xmlContent.trim().isEmpty()) return;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            Element root = doc.getDocumentElement();
            if (root != null) {
                scanTagGroups(root);
            }

            if (!allTagGroups.isEmpty()) {
                for (Map.Entry<String, List<Element>> entry : allTagGroups.entrySet()) {
                    entitySelectorCombo.addItem(new EntityOption(entry.getKey(), entry.getValue().size()));
                }

                // Render primary tag group
                String primaryTag = allTagGroups.keySet().iterator().next();
                renderEntityGroup(primaryTag);
            } else {
                masterTitleLabel.setText(" Master XML Grid: No elements found.");
            }
        } catch (Exception e) {
            masterTitleLabel.setText(" XML Grid Error: " + e.getMessage());
        }
    }

    private void renderEntityGroup(String tagName) {
        mainTableModel.setRowCount(0);
        mainTableModel.setColumnCount(0);
        rowNestedArraysMap.clear();
        detailTabPane.removeAll();

        List<Element> records = allTagGroups.get(tagName);
        if (records == null || records.isEmpty()) return;

        masterTitleLabel.setText(" Master XML Grid for <" + tagName + ">: ");

        // Discover all columns (attributes, direct fields, flattened single child structs)
        LinkedHashSet<String> columns = new LinkedHashSet<>();
        for (Element record : records) {
            extractColumnsRecursive(record, "", columns);
        }

        List<String> colList = new ArrayList<>(columns);
        for (String col : colList) {
            mainTableModel.addColumn(col);
        }

        // Build Rows
        int rowIndex = 0;
        for (Element record : records) {
            Vector<Object> rowData = new Vector<>();
            Map<String, List<Element>> nestedArrays = new HashMap<>();

            for (String col : colList) {
                Object val = extractValueRecursive(record, col, nestedArrays);
                rowData.add(val != null ? val : "");
            }

            mainTableModel.addRow(rowData);
            if (!nestedArrays.isEmpty()) {
                rowNestedArraysMap.put(rowIndex, nestedArrays);
            }
            rowIndex++;
        }

        // Adjust Column Widths
        for (int c = 0; c < mainTable.getColumnCount(); c++) {
            int maxLen = mainTable.getColumnName(c).length();
            for (int r = 0; r < Math.min(mainTable.getRowCount(), 50); r++) {
                Object obj = mainTable.getValueAt(r, c);
                if (obj != null) maxLen = Math.max(maxLen, obj.toString().length());
            }
            int width = Math.min(Math.max(maxLen * 10, 110), 320);
            mainTable.getColumnModel().getColumn(c).setPreferredWidth(width);
        }
    }

    private void extractColumnsRecursive(Element el, String prefix, Set<String> columns) {
        // 1. Attributes
        for (int i = 0; i < el.getAttributes().getLength(); i++) {
            String attrName = el.getAttributes().item(i).getNodeName();
            columns.add(prefix.isEmpty() ? "@" + attrName : prefix + ".@" + attrName);
        }

        // 2. Child Elements
        Map<String, List<Element>> childMap = getChildElementsGrouped(el);
        for (Map.Entry<String, List<Element>> entry : childMap.entrySet()) {
            String childTag = entry.getKey();
            List<Element> children = entry.getValue();

            String fullKey = prefix.isEmpty() ? childTag : prefix + "." + childTag;

            if (children.size() == 1) {
                Element child = children.get(0);
                if (isSimpleTextElement(child)) {
                    columns.add(fullKey);
                } else {
                    // Recurse into single child struct for dot-notation column flattening
                    extractColumnsRecursive(child, fullKey, columns);
                }
            } else {
                // Multi-item child array
                columns.add(fullKey);
            }
        }

        // 3. Text content if leaf with attributes
        if (childMap.isEmpty() && !el.getTextContent().trim().isEmpty() && el.getAttributes().getLength() > 0) {
            columns.add(prefix.isEmpty() ? "#text" : prefix + ".#text");
        }
    }

    private Object extractValueRecursive(Element record, String targetCol, Map<String, List<Element>> nestedArrays) {
        String[] parts = targetCol.split("\\.");
        Element current = record;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("@")) {
                String attr = part.substring(1);
                return current.hasAttribute(attr) ? current.getAttribute(attr) : "";
            } else if (part.equals("#text")) {
                return current.getTextContent().trim();
            } else {
                List<Element> children = getDirectChildElements(current, part);
                if (children.isEmpty()) {
                    return "";
                }

                if (children.size() == 1) {
                    current = children.get(0);
                    if (i == parts.length - 1) {
                        if (isSimpleTextElement(current)) {
                            return current.getTextContent().trim();
                        } else {
                            nestedArrays.put(part, children);
                            return "📦 1 Nested Struct";
                        }
                    }
                } else {
                    // Array of child elements
                    nestedArrays.put(part, children);
                    return "📦 " + children.size() + " Items (Sub-Grid)";
                }
            }
        }

        return current != null ? current.getTextContent().trim() : "";
    }

    private void showNestedDetailsForSelectedRow(int rowIndex) {
        detailTabPane.removeAll();
        Map<String, List<Element>> nestedArrays = rowNestedArraysMap.get(rowIndex);

        if (nestedArrays == null || nestedArrays.isEmpty()) {
            detailTitleLabel.setText(" 📦 Sub-Grid Details: Row " + (rowIndex + 1) + " has no nested child arrays.");
            return;
        }

        detailTitleLabel.setText(" 📦 Sub-Grid Details for Row " + (rowIndex + 1) + " (" + nestedArrays.size() + " child structure tabs)");

        for (Map.Entry<String, List<Element>> entry : nestedArrays.entrySet()) {
            String subTag = entry.getKey();
            List<Element> subElements = entry.getValue();

            TableGridPanel subGrid = new TableGridPanel();

            // Extract sub-columns
            LinkedHashSet<String> subCols = new LinkedHashSet<>();
            for (Element subEl : subElements) {
                for (int i = 0; i < subEl.getAttributes().getLength(); i++) {
                    subCols.add("@" + subEl.getAttributes().item(i).getNodeName());
                }
                for (Element child : getDirectChildren(subEl)) {
                    subCols.add(child.getNodeName());
                }
            }

            List<String> colList = new ArrayList<>(subCols);
            List<List<Object>> rows = new ArrayList<>();

            for (Element subEl : subElements) {
                List<Object> row = new ArrayList<>();
                for (String col : colList) {
                    if (col.startsWith("@")) {
                        String attr = col.substring(1);
                        row.add(subEl.hasAttribute(attr) ? subEl.getAttribute(attr) : "");
                    } else {
                        List<Element> matches = getDirectChildElements(subEl, col);
                        if (!matches.isEmpty()) {
                            row.add(matches.get(0).getTextContent().trim());
                        } else {
                            row.add("");
                        }
                    }
                }
                rows.add(row);
            }

            subGrid.setData(colList, rows);
            detailTabPane.addTab("📦 <" + subTag + "> (" + subElements.size() + " items)", subGrid);
        }
    }

    private void scanTagGroups(Element parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                allTagGroups.computeIfAbsent(el.getNodeName(), k -> new ArrayList<>()).add(el);
                scanTagGroups(el); // Recurse
            }
        }
    }

    private Map<String, List<Element>> getChildElementsGrouped(Element parent) {
        Map<String, List<Element>> map = new LinkedHashMap<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                map.computeIfAbsent(el.getNodeName(), k -> new ArrayList<>()).add(el);
            }
        }
        return map;
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
}
