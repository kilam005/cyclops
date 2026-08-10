package com.cyclops.plugin.builtin;

import com.cyclops.engine.XmlQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class XmlPlugin implements FileTypePlugin {
    private final XmlQueryEngine xmlQueryEngine = new XmlQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.xml";
    }

    @Override
    public String getDisplayName() {
        return "XML Developer Suite";
    }

    @Override
    public FileType getFileType() {
        return FileType.XML;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        if ("xml".equalsIgnoreCase(header.getExtension()) || "xsd".equalsIgnoreCase(header.getExtension()) || "wsdl".equalsIgnoreCase(header.getExtension())) {
            return true;
        }
        return header.startsWithText("<?xml") || (header.startsWithText("<") && header.getPreviewString().contains(">"));
    }

    @Override
    public QueryEngine getQueryEngine() {
        return xmlQueryEngine;
    }

    @Override
    public boolean hasTableSupport() {
        return true;
    }

    @Override
    public boolean hasTreeSupport() {
        return true;
    }

    @Override
    public String getSyntaxStyleKey() {
        return SyntaxConstants.SYNTAX_STYLE_XML;
    }
}
