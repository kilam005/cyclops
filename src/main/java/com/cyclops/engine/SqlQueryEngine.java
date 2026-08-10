package com.cyclops.engine;

import com.cyclops.model.QueryResult;
import com.cyclops.plugin.QueryEngine;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL Query Engine powered by embedded DuckDB JDBC.
 * Allows executing ANSI-SQL queries over CSV, TSV, Parquet, Avro, ORC files seamlessly.
 */
public class SqlQueryEngine implements QueryEngine {

    static {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("DuckDB Driver warning: " + e.getMessage());
        }
    }

    @Override
    public String getEngineName() {
        return "DuckDB SQL Engine";
    }

    @Override
    public String getDefaultQueryPlaceholder() {
        return "SELECT * FROM data LIMIT 50\n" +
               "OR filter: SELECT category, COUNT(*), AVG(price) FROM data GROUP BY category\n" +
               "OR search: SELECT * FROM data WHERE price > 100 ORDER BY price DESC";
    }

    @Override
    public QueryResult executeQuery(File file, String queryContent, String fileTextContent) {
        long startTime = System.currentTimeMillis();
        if (queryContent == null || queryContent.trim().isEmpty()) {
            return new QueryResult("", "SQL Query cannot be empty.", 0, true);
        }

        if (file == null || !file.exists()) {
            return new QueryResult(queryContent, "Target file does not exist on disk.", System.currentTimeMillis() - startTime, true);
        }

        String filePath = file.getAbsolutePath().replace("\\", "/");

        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:")) {
            try (Statement stmt = conn.createStatement()) {
                // Determine file reader wrapper based on extension
                String ext = getFileExtension(file.getName());
                String tableRef;
                if ("parquet".equalsIgnoreCase(ext)) {
                    tableRef = "read_parquet('" + filePath + "')";
                } else if ("orc".equalsIgnoreCase(ext)) {
                    tableRef = "read_orc('" + filePath + "')";
                } else if ("json".equalsIgnoreCase(ext)) {
                    tableRef = "read_json_auto('" + filePath + "')";
                } else { // csv, tsv, txt, etc.
                    tableRef = "read_csv_auto('" + filePath + "')";
                }

                // Create a temporary view 'data' so users can write simple queries like `SELECT * FROM data`
                stmt.execute("CREATE VIEW data AS SELECT * FROM " + tableRef);

                String sql = queryContent.trim();
                // Replace 'file' or 'data' if needed
                boolean isSelect = sql.toLowerCase().startsWith("select") || sql.toLowerCase().startsWith("with");
                if (!isSelect) {
                    sql = "SELECT * FROM data WHERE " + sql;
                }

                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();
                    List<String> columnNames = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        columnNames.add(md.getColumnLabel(i));
                    }

                    List<List<Object>> rows = new ArrayList<>();
                    int rowLimit = 5000; // Cap UI row results for extreme performance
                    while (rs.next() && rows.size() < rowLimit) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            row.add(val != null ? val.toString() : "NULL");
                        }
                        rows.add(row);
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    return new QueryResult(queryContent, columnNames, rows, duration);
                }
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new QueryResult(queryContent, "SQL Error: " + e.getMessage(), duration, true);
        }
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx > 0 && idx < fileName.length() - 1) {
            return fileName.substring(idx + 1).toLowerCase();
        }
        return "";
    }
}
