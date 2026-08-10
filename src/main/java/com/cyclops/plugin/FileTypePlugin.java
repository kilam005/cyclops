package com.cyclops.plugin;

import com.cyclops.model.FileHeader;
import com.cyclops.model.FileType;
import java.io.File;

/**
 * Extension interface for format plugins in Cyclops.
 * Developers can implement this interface to support new file types in the future.
 */
public interface FileTypePlugin {

    /**
     * Unique identifier for the plugin (e.g. "com.cyclops.plugin.xml").
     */
    String getPluginId();

    /**
     * Human-readable plugin name (e.g. "XML Developer Suite").
     */
    String getDisplayName();

    /**
     * FileType enum associated with this plugin.
     */
    FileType getFileType();

    /**
     * Evaluates if this plugin can handle the given file based on header signature, magic bytes, or extension.
     * @param header FileHeader metadata
     * @return true if this plugin claims support for the file.
     */
    boolean canHandle(FileHeader header);

    /**
     * Returns the query engine for this format, or null if query engine is not supported.
     */
    QueryEngine getQueryEngine();

    /**
     * Indicates whether this format supports interactive table view.
     */
    boolean hasTableSupport();

    /**
     * Indicates whether this format supports structural tree inspection.
     */
    boolean hasTreeSupport();

    /**
     * Returns custom syntax highlighting key for RSyntaxTextArea (e.g. SyntaxConstants.SYNTAX_STYLE_XML).
     */
    String getSyntaxStyleKey();
}
