package com.cyclops;

import com.cyclops.engine.XmlQueryEngine;
import com.cyclops.model.QueryResult;
import com.cyclops.service.SampleDataLoader;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class XmlQueryEngineTest {

    @Test
    public void testXPathEvaluation() {
        File xmlFile = SampleDataLoader.createSampleXml();
        XmlQueryEngine engine = new XmlQueryEngine();

        QueryResult result = engine.executeQuery(xmlFile, "//item[price > 200]", null);
        assertTrue(result.isSuccess());
        assertTrue(result.getRowCount() >= 2);
    }

    @Test
    public void testXQueryEvaluation() {
        File xmlFile = SampleDataLoader.createSampleXml();
        XmlQueryEngine engine = new XmlQueryEngine();

        String xquery = "for $x in //customer return $x/name/text()";
        QueryResult result = engine.executeQuery(xmlFile, xquery, null);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRowCount());
    }
}
