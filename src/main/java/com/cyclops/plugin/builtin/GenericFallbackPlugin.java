package com.cyclops.plugin.builtin;

import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class GenericFallbackPlugin implements FileTypePlugin {

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.generic";
    }

    @Override
    public String getDisplayName() {
        return "Generic Text / Hex Viewer";
    }

    @Override
    public FileType getFileType() {
        return FileType.GENERIC_TEXT;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        // Fallback plugin accepts any file as a universal safety net
        return true;
    }

    @Override
    public QueryEngine getQueryEngine() {
        return null; // Generic files do not have specialized query engines by default
    }

    @Override
    public boolean hasTableSupport() {
        return false;
    }

    @Override
    public boolean hasTreeSupport() {
        return false;
    }

    @Override
    public String getSyntaxStyleKey() {
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }
}
