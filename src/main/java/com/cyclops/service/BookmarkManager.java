package com.cyclops.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages file & line bookmarks in Cyclops.
 */
public class BookmarkManager {
    private static final BookmarkManager INSTANCE = new BookmarkManager();

    public static class BookmarkItem {
        private final File file;
        private final int lineNumber;
        private final String label;
        private final long timestamp;

        public BookmarkItem(File file, int lineNumber, String label) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.label = label != null && !label.isEmpty() ? label : (file.getName() + " (Line " + lineNumber + ")");
            this.timestamp = System.currentTimeMillis();
        }

        public File getFile() { return file; }
        public int getLineNumber() { return lineNumber; }
        public String getLabel() { return label; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "⭐ " + label + " - " + file.getName() + ":" + lineNumber;
        }
    }

    public interface BookmarkChangeListener {
        void onBookmarksChanged();
    }

    private final List<BookmarkItem> bookmarks = new CopyOnWriteArrayList<>();
    private final List<BookmarkChangeListener> listeners = new ArrayList<>();

    private BookmarkManager() {}

    public static BookmarkManager getInstance() {
        return INSTANCE;
    }

    public void addBookmark(File file, int lineNumber, String snippet) {
        if (file == null) return;
        BookmarkItem item = new BookmarkItem(file, lineNumber, snippet);
        bookmarks.add(item);
        notifyListeners();
    }

    public void removeBookmark(BookmarkItem item) {
        bookmarks.remove(item);
        notifyListeners();
    }

    public List<BookmarkItem> getBookmarks() {
        return Collections.unmodifiableList(bookmarks);
    }

    public void addListener(BookmarkChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (BookmarkChangeListener listener : listeners) {
            try {
                listener.onBookmarksChanged();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
