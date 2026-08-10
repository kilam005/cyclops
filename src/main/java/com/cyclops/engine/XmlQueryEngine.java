package com.cyclops.engine;

import com.cyclops.model.QueryResult;
import com.cyclops.plugin.QueryEngine;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Saxon-HE Query Engine supporting both XPath 3.1 and XQuery 3.1.
 */
public class XmlQueryEngine implements QueryEngine {
    private final Processor processor;

    public XmlQueryEngine() {
        this.processor = new Processor(false);
    }

    @Override
    public String getEngineName() {
        return "Saxon-HE XPath 3.1 / XQuery 3.1 Engine";
    }

    @Override
    public String getDefaultQueryPlaceholder() {
        return "//order[price > 100]\n" +
               "OR XQuery:\n" +
               "for $x in //item\n" +
               "where $x/price > 50\n" +
               "return $x/name/text()";
    }

    @Override
    public QueryResult executeQuery(File file, String queryContent, String fileTextContent) {
        long startTime = System.currentTimeMillis();
        if (queryContent == null || queryContent.trim().isEmpty()) {
            return new QueryResult("", "Query string cannot be empty.", 0, true);
        }

        try {
            String xmlData = fileTextContent;
            if (xmlData == null && file != null && file.exists()) {
                xmlData = Files.readString(file.toPath());
            }

            if (xmlData == null || xmlData.trim().isEmpty()) {
                return new QueryResult(queryContent, "XML content is empty.", System.currentTimeMillis() - startTime, true);
            }

            DocumentBuilder builder = processor.newDocumentBuilder();
            XdmNode doc = builder.build(new StreamSource(new StringReader(xmlData)));

            String trimmedQuery = queryContent.trim();
            boolean isXQuery = trimmedQuery.toLowerCase().startsWith("for ") ||
                               trimmedQuery.toLowerCase().startsWith("let ") ||
                               trimmedQuery.toLowerCase().startsWith("declare ") ||
                               trimmedQuery.contains("return ");

            if (isXQuery) {
                return runXQuery(doc, trimmedQuery, startTime);
            } else {
                return runXPath(doc, trimmedQuery, startTime);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new QueryResult(queryContent, "XML Query Error: " + e.getMessage(), duration, true);
        }
    }

    private QueryResult runXPath(XdmNode doc, String xpathExpr, long startTime) throws SaxonApiException {
        XPathCompiler compiler = processor.newXPathCompiler();
        XPathSelector selector = compiler.compile(xpathExpr).load();
        selector.setContextItem(doc);

        XdmValue evalResult = selector.evaluate();
        List<List<Object>> rows = new ArrayList<>();
        List<String> columns = List.of("Item", "Type", "Value");

        int index = 1;
        for (XdmItem item : evalResult) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(item.isNode() ? ((XdmNode) item).getNodeKind().toString() : "AtomicValue");
            row.add(item.getStringValue());
            rows.add(row);
        }

        long duration = System.currentTimeMillis() - startTime;
        if (rows.isEmpty()) {
            return new QueryResult(xpathExpr, "No nodes matched XPath expression: " + xpathExpr, duration);
        }

        return new QueryResult(xpathExpr, columns, rows, duration);
    }

    private QueryResult runXQuery(XdmNode doc, String xqueryExpr, long startTime) throws SaxonApiException {
        XQueryCompiler compiler = processor.newXQueryCompiler();
        XQueryExecutable executable = compiler.compile(xqueryExpr);
        XQueryEvaluator evaluator = executable.load();
        evaluator.setContextItem(doc);

        XdmValue evalResult = evaluator.evaluate();
        List<List<Object>> rows = new ArrayList<>();
        List<String> columns = List.of("Result Index", "Content");

        int index = 1;
        for (XdmItem item : evalResult) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(item.getStringValue());
            rows.add(row);
        }

        long duration = System.currentTimeMillis() - startTime;
        return new QueryResult(xqueryExpr, columns, rows, duration);
    }
}
