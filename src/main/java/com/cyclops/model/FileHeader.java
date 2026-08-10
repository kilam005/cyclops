package com.cyclops.model;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * FileHeader encapsulates file inspection metadata such as magic bytes,
 * extension, character preview, and size.
 */
public class FileHeader {
    private final File file;
    private final String extension;
    private final byte[] magicBytes;
    private final String previewString;
    private final long length;

    public FileHeader(File file, byte[] magicBytes, long length) {
        this.file = file;
        this.magicBytes = magicBytes != null ? magicBytes : new byte[0];
        this.length = length;
        this.extension = extractExtension(file != null ? file.getName() : "");
        this.previewString = new String(this.magicBytes, StandardCharsets.UTF_8);
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx > 0 && idx < fileName.length() - 1) {
            return fileName.substring(idx + 1).toLowerCase();
        }
        return "";
    }

    public File getFile() {
        return file;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getMagicBytes() {
        return magicBytes;
    }

    public String getPreviewString() {
        return previewString;
    }

    public long getLength() {
        return length;
    }

    public boolean hasMagicBytes(byte[] target) {
        if (target == null || magicBytes.length < target.length) {
            return false;
        }
        for (int i = 0; i < target.length; i++) {
            if (magicBytes[i] != target[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWithText(String prefix) {
        if (previewString == null || prefix == null) {
            return false;
        }
        return previewString.trim().toLowerCase().startsWith(prefix.toLowerCase());
    }
}
