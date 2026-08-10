package com.cyclops;

import com.cyclops.engine.JsonQueryEngine;
import com.cyclops.model.QueryResult;
import com.cyclops.service.SampleDataLoader;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class JsonQueryEngineTest {

    @Test
    public void testJsonPathEvaluation() {
        File jsonFile = SampleDataLoader.createSampleJson();
        JsonQueryEngine engine = new JsonQueryEngine();

        QueryResult result = engine.executeQuery(jsonFile, "$.users[*].username", null);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getRowCount());
    }
}
