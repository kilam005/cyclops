package com.cyclops.plugin.builtin;

import com.cyclops.engine.SqlQueryEngine;
import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.QueryEngine;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class CsvPlugin implements FileTypePlugin {
    private final SqlQueryEngine sqlQueryEngine = new SqlQueryEngine();

    @Override
    public String getPluginId() {
        return "com.cyclops.plugin.csv";
    }

    @Override
    public String getDisplayName() {
        return "CSV / Delimited Data Suite";
    }

    @Override
    public FileType getFileType() {
        return FileType.CSV;
    }

    @Override
    public boolean canHandle(FileHeader header) {
        if (header == null) return false;
        String ext = header.getExtension();
        if ("csv".equalsIgnoreCase(ext) || "tsv".equalsIgnoreCase(ext) || "psv".equalsIgnoreCase(ext) || "tab".equalsIgnoreCase(ext)) {
            return true;
        }
        // Non-CSV text extensions should not be claimed by CSV plugin
        if ("log".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext) || "md".equalsIgnoreCase(ext) || "properties".equalsIgnoreCase(ext) || "conf".equalsIgnoreCase(ext)) {
            return false;
        }
        // Delimiter heuristic check on preview string
        String p = header.getPreviewString();
        if (p.contains(",") || p.contains("\t") || p.contains("|") || p.contains(";")) {
            String[] lines = p.split("\r?\n");
            if (lines.length >= 2) {
                long c1 = countChar(lines[0], ',');
                long c2 = countChar(lines[1], ',');
                if (c1 >= 2 && c1 == c2) return true;

                long t1 = countChar(lines[0], '\t');
                long t2 = countChar(lines[1], '\t');
                if (t1 >= 2 && t1 == t2) return true;
            }
        }
        return false;
    }

    private long countChar(String str, char target) {
        return str.chars().filter(ch -> ch == target).count();
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
        return false;
    }

    @Override
    public String getSyntaxStyleKey() {
        return SyntaxConstants.SYNTAX_STYLE_CSV;
    }
}
