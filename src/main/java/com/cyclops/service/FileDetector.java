package com.cyclops.service;

import com.cyclops.model.FileHeader;
import com.cyclops.plugin.FileTypePlugin;
import com.cyclops.plugin.PluginRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Automated File Detector that inspects file header magic bytes and structure
 * to resolve the appropriate FileTypePlugin.
 */
public class FileDetector {

    public static FileHeader inspect(File file) {
        if (file == null || !file.exists()) {
            return new FileHeader(file, new byte[0], 0);
        }

        byte[] buffer = new byte[8192]; // Read up to 8KB preview
        int bytesRead = 0;
        try (InputStream is = new FileInputStream(file)) {
            bytesRead = is.read(buffer);
        } catch (Exception e) {
            // Log warning
        }

        byte[] actualBytes;
        if (bytesRead > 0) {
            actualBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, actualBytes, 0, bytesRead);
        } else {
            actualBytes = new byte[0];
        }

        return new FileHeader(file, actualBytes, file.length());
    }

    public static FileTypePlugin detectPlugin(File file) {
        FileHeader header = inspect(file);
        return PluginRegistry.getInstance().findPluginForFile(header);
    }
}
