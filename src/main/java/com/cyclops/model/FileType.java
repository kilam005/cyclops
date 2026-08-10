package com.cyclops.model;

/**
 * Supported file types in Cyclops.
 */
public enum FileType {
    XML("XML", "xml", "application/xml"),
    JSON("JSON", "json", "application/json"),
    CSV("CSV / Delimited", "csv", "text/csv"),
    PARQUET("Apache Parquet", "parquet", "application/vnd.apache.parquet"),
    AVRO("Apache Avro", "avro", "application/vnd.apache.avro"),
    ORC("Apache ORC", "orc", "application/vnd.apache.orc"),
    GENERIC_TEXT("Generic Text", "txt", "text/plain"),
    GENERIC_BINARY("Generic Binary / Hex", "bin", "application/octet-stream");

    private final String displayName;
    private final String defaultExtension;
    private final String mimeType;

    FileType(String displayName, String defaultExtension, String mimeType) {
        this.displayName = displayName;
        this.defaultExtension = defaultExtension;
        this.mimeType = mimeType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public String getMimeType() {
        return mimeType;
    }
}
