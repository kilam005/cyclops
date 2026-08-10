package com.cyclops.plugin.builtin;

import com.cyclops.engine.SqlQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class ParquetPlugin implements FileTypePlugin {
    private static final byte[] PARQUET_MAGIC = new byte[]{0x50, 0x41, 0x52, 0x31}; // "PAR1"
    private final SqlQueryEngine sqlQueryEngine = new SqlQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.parquet";
    }

    @Override
    public String getDisplayName() {
        return "Apache Parquet Inspector";
    }

    @Override
    public FileType getFileType() {
        return FileType.PARQUET;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        if ("parquet".equalsIgnoreCase(header.getExtension())) {
            return true;
        }
        return header.hasMagicBytes(PARQUET_MAGIC);
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
