package com.cyclops.engine;

import com.cyclops.model.QueryResult;
import com.cyclops.plugin.QueryEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSONPath Engine using Jayway JsonPath.
 */
public class JsonQueryEngine implements QueryEngine {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getEngineName() {
        return "Jayway JSONPath Engine";
    }

    @Override
    public String getDefaultQueryPlaceholder() {
        return "$.store.book[*]\n" +
               "OR filter: $..[?(@.price > 10)]\n" +
               "OR wildcards: $.users[*].name";
    }

    @Override
    public QueryResult executeQuery(File file, String queryContent, String fileTextContent) {
        long startTime = System.currentTimeMillis();
        if (queryContent == null || queryContent.trim().isEmpty()) {
            return new QueryResult("", "JSONPath expression cannot be empty.", 0, true);
        }

        try {
            String jsonText = fileTextContent;
            if (jsonText == null && file != null && file.exists()) {
                jsonText = Files.readString(file.toPath());
            }

            if (jsonText == null || jsonText.trim().isEmpty()) {
                return new QueryResult(queryContent, "JSON document is empty.", System.currentTimeMillis() - startTime, true);
            }

            Configuration config = Configuration.defaultConfiguration()
                    .addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);

            Object jsonObject = Configuration.defaultConfiguration().jsonProvider().parse(jsonText);
            Object result = JsonPath.using(config).parse(jsonObject).read(queryContent.trim());

            List<List<Object>> rows = new ArrayList<>();
            List<String> columns = List.of("Index", "JSON Result Node");

            if (result instanceof List) {
                List<?> list = (List<?>) result;
                int idx = 1;
                for (Object item : list) {
                    List<Object> row = new ArrayList<>();
                    row.add(idx++);
                    if (item instanceof Map || item instanceof List) {
                        row.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                    } else {
                        row.add(String.valueOf(item));
                    }
                    rows.add(row);
                }
            } else {
                List<Object> row = new ArrayList<>();
                row.add(1);
                row.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                rows.add(row);
            }

            long duration = System.currentTimeMillis() - startTime;
            return new QueryResult(queryContent, columns, rows, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new QueryResult(queryContent, "JSONPath Error: " + e.getMessage(), duration, true);
        }
    }
}
