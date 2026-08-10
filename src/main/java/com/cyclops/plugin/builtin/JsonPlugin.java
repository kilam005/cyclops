package com.cyclops.plugin.builtin;

import com.cyclops.engine.JsonQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class JsonPlugin implements FileTypePlugin {
    private final JsonQueryEngine jsonQueryEngine = new JsonQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.json";
    }

    @Override
    public String getDisplayName() {
        return "JSON Developer Suite";
    }

    @Override
    public FileType getFileType() {
        return FileType.JSON;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        if ("json".equalsIgnoreCase(header.getExtension()) || "geojson".equalsIgnoreCase(header.getExtension()) || "jsonl".equalsIgnoreCase(header.getExtension())) {
            return true;
        }
        String p = header.getPreviewString().trim();
        return p.startsWith("{") || p.startsWith("[");
    }

    @Override
    public QueryEngine getQueryEngine() {
        return jsonQueryEngine;
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
        return SyntaxConstants.SYNTAX_STYLE_JSON;
    }
}
