package com.cyclops.service;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility to generate and load bundled sample dataset files for XML, JSON, CSV, Parquet, Avro, ORC, and generic text.
 */
public class SampleDataLoader {

    public static File getSampleDirectory() {
        File sampleDir = new File(System.getProperty("user.home"), ".cyclops/samples");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        return sampleDir;
    }

    public static File createSampleXml() {
        File file = new File(getSampleDirectory(), "purchase_orders.xml");
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <store name="Cyclops Global Data Store" location="San Francisco">
                    <orders>
                        <order id="ORD-1001" status="completed">
                            <customer id="C-501">
                                <name>Alice Smith</name>
                                <email>alice@example.com</email>
                            </customer>
                            <items>
                                <item id="ITM-1" category="Electronics">
                                    <name>UltraHD Monitor 32-inch</name>
                                    <price>499.99</price>
                                    <quantity>2</quantity>
                                </item>
                                <item id="ITM-2" category="Peripherals">
                                    <name>Ergonomic Mechanical Keyboard</name>
                                    <price>129.50</price>
                                    <quantity>1</quantity>
                                </item>
                            </items>
                            <totalAmount>1129.48</totalAmount>
                        </order>
                        <order id="ORD-1002" status="pending">
                            <customer id="C-502">
                                <name>Bob Jones</name>
                                <email>bob@example.com</email>
                            </customer>
                            <items>
                                <item id="ITM-3" category="Developer Tools">
                                    <name>Cyclops IDE Enterprise License</name>
                                    <price>299.00</price>
                                    <quantity>5</quantity>
                                </item>
                            </items>
                            <totalAmount>1495.00</totalAmount>
                        </order>
                    </orders>
                </store>
                """;
        writeFile(file, xml);
        return file;
    }

    public static File createSampleJson() {
        File file = new File(getSampleDirectory(), "user_analytics.json");
        String json = """
                {
                  "system": "Cyclops Telemetry Engine",
                  "version": "1.0.0",
                  "timestamp": "2026-08-10T11:00:00Z",
                  "users": [
                    {
                      "id": 101,
                      "username": "malik_dev",
                      "role": "Lead Data Architect",
                      "active": true,
                      "metrics": {
                        "queriesExecuted": 1420,
                        "dataProcessedMb": 8500.5,
                        "favoriteFormat": "Parquet"
                      },
                      "tags": ["java", "sql", "xml", "parquet"]
                    },
                    {
                      "id": 102,
                      "username": "sarah_analytics",
                      "role": "BI Engineer",
                      "active": true,
                      "metrics": {
                        "queriesExecuted": 890,
                        "dataProcessedMb": 3200.0,
                        "favoriteFormat": "CSV"
                      },
                      "tags": ["sql", "duckdb", "tableau"]
                    },
                    {
                      "id": 103,
                      "username": "alex_backend",
                      "role": "Distributed Systems Dev",
                      "active": false,
                      "metrics": {
                        "queriesExecuted": 2300,
                        "dataProcessedMb": 15400.2,
                        "favoriteFormat": "Avro"
                      },
                      "tags": ["avro", "orc", "grpc"]
                    }
                  ]
                }
                """;
        writeFile(file, json);
        return file;
    }

    public static File createSampleCsv() {
        File file = new File(getSampleDirectory(), "regional_sales.csv");
        String csv = """
                TransactionID,Region,ProductCategory,Price,Quantity,Discount,SaleDate,SalesRep
                TX-10001,North America,Laptops,1299.99,3,0.05,2026-07-01,John Doe
                TX-10002,Europe,Servers,3499.50,2,0.10,2026-07-02,Anna Mueller
                TX-10003,Asia Pacific,Cloud Software,499.00,10,0.00,2026-07-03,Kenji Sato
                TX-10004,North America,Monitors,399.99,5,0.12,2026-07-04,Jane Smith
                TX-10005,Latin America,Networking,850.00,4,0.08,2026-07-05,Carlos Silva
                TX-10006,Europe,Laptops,1299.99,7,0.15,2026-07-06,Anna Mueller
                TX-10007,Asia Pacific,Monitors,399.99,12,0.10,2026-07-07,Kenji Sato
                """;
        writeFile(file, csv);
        return file;
    }

    public static File createSampleGenericLog() {
        File file = new File(getSampleDirectory(), "application_server.log");
        String log = """
                2026-08-10 11:00:01.123 [INFO] [com.cyclops.Main] Cyclops IDE initializing...
                2026-08-10 11:00:01.145 [INFO] [com.cyclops.plugin.PluginRegistry] Registered 7 built-in plugins (XML, JSON, CSV, Parquet, Avro, ORC, Generic Fallback).
                2026-08-10 11:00:01.200 [INFO] [com.cyclops.service.ThemeManager] Applied theme: FlatMacDarkLaf.
                2026-08-10 11:00:02.050 [DEBUG] [com.cyclops.service.FileDetector] Magic byte detection active on 8KB preview buffer.
                2026-08-10 11:00:03.410 [WARN] [com.cyclops.engine.SqlQueryEngine] DuckDB in-memory session established.
                2026-08-10 11:00:05.800 [INFO] [com.cyclops.ui.CyclopsMainFrame] Workspace window rendered successfully on High-DPI screen.
                """;
        writeFile(file, log);
        return file;
    }

    public static File createSampleParquet() {
        File file = new File(getSampleDirectory(), "sales_data.parquet");
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:")) {
            try (Statement stmt = conn.createStatement()) {
                File csvFile = createSampleCsv();
                String csvPath = csvFile.getAbsolutePath().replace("\\", "/");
                String parquetPath = file.getAbsolutePath().replace("\\", "/");
                stmt.execute("COPY (SELECT * FROM read_csv_auto('" + csvPath + "')) TO '" + parquetPath + "' (FORMAT PARQUET)");
            }
        } catch (Exception e) {
            // Fallback if duckdb copy fails
            writeFile(file, "PAR1_PARQUET_SAMPLE_MOCK_DATA");
        }
        return file;
    }

    private static void writeFile(File file, String content) {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
