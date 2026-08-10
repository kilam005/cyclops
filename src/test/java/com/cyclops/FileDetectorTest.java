package com.cyclops;

import com.cyclops.model.FileType;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.service.FileDetector;
import com.cyclops.service.SampleDataLoader;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileDetectorTest {

    @Test
    public void testXmlDetection() {
        File xmlFile = SampleDataLoader.createSampleXml();
        FileTypePlugin plugin = FileDetector.detectPlugin(xmlFile);
        assertNotNull(plugin);
        assertEquals(FileType.XML, plugin.getFileType());
    }

    @Test
    public void testJsonDetection() {
        File jsonFile = SampleDataLoader.createSampleJson();
        FileTypePlugin plugin = FileDetector.detectPlugin(jsonFile);
        assertNotNull(plugin);
        assertEquals(FileType.JSON, plugin.getFileType());
    }

    @Test
    public void testCsvDetection() {
        File csvFile = SampleDataLoader.createSampleCsv();
        FileTypePlugin plugin = FileDetector.detectPlugin(csvFile);
        assertNotNull(plugin);
        assertEquals(FileType.CSV, plugin.getFileType());
    }

    @Test
    public void testParquetDetection() {
        File parquetFile = SampleDataLoader.createSampleParquet();
        FileTypePlugin plugin = FileDetector.detectPlugin(parquetFile);
        assertNotNull(plugin);
        assertEquals(FileType.PARQUET, plugin.getFileType());
    }

    @Test
    public void testGenericLogDetection() {
        File logFile = SampleDataLoader.createSampleGenericLog();
        FileTypePlugin plugin = FileDetector.detectPlugin(logFile);
        assertNotNull(plugin);
        assertEquals(FileType.GENERIC_TEXT, plugin.getFileType());
    }
}
