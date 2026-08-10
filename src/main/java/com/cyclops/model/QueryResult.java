package com.cyclops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates execution results from query engines (XPath/XQuery, JSONPath, SQL).
 */
public class QueryResult {
    private final boolean success;
    private final String query;
    private final List<String> columnNames;
    private final List<List<Object>> rows;
    private final String rawOutput;
    private final String errorMessage;
    private final long executionTimeMs;

    public QueryResult(String query, List<String> columnNames, List<List<Object>> rows, long executionTimeMs) {
        this.success = true;
        this.query = query;
        this.columnNames = columnNames != null ? columnNames : new ArrayList<>();
        this.rows = rows != null ? rows : new ArrayList<>();
        this.rawOutput = null;
        this.errorMessage = null;
        this.executionTimeMs = executionTimeMs;
    }

    public QueryResult(String query, String rawOutput, long executionTimeMs) {
        this.success = true;
        this.query = query;
        this.columnNames = List.of("Result");
        this.rows = new ArrayList<>();
        this.rawOutput = rawOutput;
        this.errorMessage = null;
        this.executionTimeMs = executionTimeMs;
    }

    public QueryResult(String query, String errorMessage, long executionTimeMs, boolean isError) {
        this.success = false;
        this.query = query;
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.rawOutput = null;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public int getRowCount() {
        return rows != null ? rows.size() : 0;
    }
}
