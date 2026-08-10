package com.cyclops.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * Structural Tree Inspector panel for XML DOM, JSON objects, and Schema trees.
 */
public class TreeInspectorPanel extends JPanel {
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TreeInspectorPanel() {
        setLayout(new BorderLayout());

        rootNode = new DefaultMutableTreeNode("Document Root");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new RichTreeCellRenderer());
        tree.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadXmlTree(String xmlContent) {
        rootNode.removeAllChildren();
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            treeModel.reload();
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            Element rootElement = doc.getDocumentElement();
            if (rootElement != null) {
                DefaultMutableTreeNode xmlRoot = buildXmlNode(rootElement);
                rootNode.setUserObject("XML Document: " + rootElement.getNodeName());
                rootNode.add(xmlRoot);
            }
        } catch (Exception e) {
            rootNode.setUserObject("XML Parsing Error: " + e.getMessage());
        }

        treeModel.reload();
        expandAllNodes(tree, 0, tree.getRowCount());
    }

    public void loadJsonTree(String jsonContent) {
        rootNode.removeAllChildren();
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            treeModel.reload();
            return;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            rootNode.setUserObject("JSON Root (" + jsonNode.getNodeType() + ")");
            buildJsonNode(jsonNode, rootNode);
        } catch (Exception e) {
            rootNode.setUserObject("JSON Parsing Error: " + e.getMessage());
        }

        treeModel.reload();
        expandAllNodes(tree, 0, tree.getRowCount());
    }

    private DefaultMutableTreeNode buildXmlNode(Node node) {
        String label = node.getNodeName();
        if (node.hasAttributes()) {
            StringBuilder attrs = new StringBuilder(" [");
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attr = node.getAttributes().item(i);
                attrs.append(attr.getNodeName()).append("=").append(attr.getNodeValue());
                if (i < node.getAttributes().getLength() - 1) attrs.append(", ");
            }
            attrs.append("]");
            label += attrs.toString();
        }

        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(label);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                treeNode.add(buildXmlNode(child));
            } else if (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().trim().isEmpty()) {
                treeNode.add(new DefaultMutableTreeNode("value: " + child.getNodeValue().trim()));
            }
        }
        return treeNode;
    }

    private void buildJsonNode(JsonNode node, DefaultMutableTreeNode parent) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode child = entry.getValue();
                if (child.isValueNode()) {
                    parent.add(new DefaultMutableTreeNode(key + ": " + child.asText()));
                } else {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(key + " (" + child.getNodeType() + ")");
                    parent.add(childNode);
                    buildJsonNode(child, childNode);
                }
            }
        } else if (node.isArray()) {
            int idx = 0;
            for (JsonNode child : node) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode("[" + idx++ + "] (" + child.getNodeType() + ")");
                parent.add(childNode);
                buildJsonNode(child, childNode);
            }
        } else {
            parent.add(new DefaultMutableTreeNode("Value: " + node.asText()));
        }
    }

    private void expandAllNodes(JTree tree, int startingRow, int rowCount) {
        for (int i = startingRow; i < rowCount; i++) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
}
