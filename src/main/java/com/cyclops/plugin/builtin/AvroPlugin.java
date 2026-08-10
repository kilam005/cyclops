package com.cyclops.plugin.builtin;

import com.cyclops.engine.SqlQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class AvroPlugin implements FileTypePlugin {
    private static final byte[] AVRO_MAGIC = new byte[]{0x4F, 0x62, 0x6A, 0x01}; // "Obj\1"
    private final SqlQueryEngine sqlQueryEngine = new SqlQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.avro";
    }

    @Override
    public String getDisplayName() {
        return "Apache Avro Inspector";
    }

    @Override
    public FileType getFileType() {
        return FileType.AVRO;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        if ("avro".equalsIgnoreCase(header.getExtension()) || "avsc".equalsIgnoreCase(header.getExtension())) {
            return true;
        }
        return header.hasMagicBytes(AVRO_MAGIC);
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
