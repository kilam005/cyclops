package com.cyclops.plugin;

import com.cyclops.model.FileHeader;
import com.cyclops.plugin.builtin.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry managing all file format plugins in Cyclops.
 * Supports built-in plugins (XML, JSON, CSV, Parquet, Avro, ORC)
 * as well as dynamically registered user plugins.
 */
public class PluginRegistry {
    private static final PluginRegistry INSTANCE = new PluginRegistry();

    private final List<FileTypePlugin> plugins = new CopyOnWriteArrayList<>();
    private final FileTypePlugin fallbackPlugin = new GenericFallbackPlugin();

    private PluginRegistry() {
        // Register built-in plugins in priority order
        registerPlugin(new ParquetPlugin());
        registerPlugin(new AvroPlugin());
        registerPlugin(new OrcPlugin());
        registerPlugin(new XmlPlugin());
        registerPlugin(new JsonPlugin());
        registerPlugin(new CsvPlugin());
    }

    public static PluginRegistry getInstance() {
        return INSTANCE;
    }

    public void registerPlugin(FileTypePlugin plugin) {
        if (plugin != null && !plugins.contains(plugin)) {
            plugins.add(plugin);
        }
    }

    public void unregisterPlugin(FileTypePlugin plugin) {
        plugins.remove(plugin);
    }

    public List<FileTypePlugin> getRegisteredPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public FileTypePlugin findPluginForFile(FileHeader header) {
        if (header == null) {
            return fallbackPlugin;
        }

        for (FileTypePlugin plugin : plugins) {
            try {
                if (plugin.canHandle(header)) {
                    return plugin;
                }
            } catch (Exception e) {
                // Log and bypass broken plugins gracefully
            }
        }

        return fallbackPlugin;
    }

    public FileTypePlugin getFallbackPlugin() {
        return fallbackPlugin;
    }
}
