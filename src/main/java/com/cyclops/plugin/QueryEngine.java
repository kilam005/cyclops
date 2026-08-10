package com.cyclops.plugin;

import com.cyclops.model.QueryResult;
import java.io.File;

/**
 * Interface implemented by format query engines (XPath, XQuery, JSONPath, SQL).
 */
public interface QueryEngine {
    /**
     * Engine label, e.g. "XPath / XQuery 3.1", "JSONPath", "SQL Engine (DuckDB)".
     */
    String getEngineName();

    /**
     * Default query prompt/placeholder for user guidance.
     */
    String getDefaultQueryPlaceholder();

    /**
     * Executes the query on the target file.
     */
    QueryResult executeQuery(File file, String queryContent, String fileTextContent);
}
