package com.cyclops.plugin.builtin;

import com.cyclops.engine.SqlQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class OrcPlugin implements FileTypePlugin {
    private static final byte[] ORC_MAGIC = new byte[]{0x4F, 0x52, 0x43}; // "ORC"
    private final SqlQueryEngine sqlQueryEngine = new SqlQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.orc";
    }

    @Override
    public String getDisplayName() {
        return "Apache ORC Inspector";
    }

    @Override
    public FileType getFileType() {
        return FileType.ORC;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        if ("orc".equalsIgnoreCase(header.getExtension())) {
            return true;
        }
        return header.hasMagicBytes(ORC_MAGIC);
    }

    @Override
    public QueryEngine getQueryEngine() {
        return sqlQueryEngine;
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
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }
}
