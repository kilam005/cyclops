# Cyclops - "One eye for all your data" 👁

> **A Lightweight, High-Performance Cross-Platform Data IDE & Text Editor with Automated Format Detection, Native Side-by-Side Views, Infinite-Depth XML Tree-Grid, and Embedded Query Engines.**

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21%2B%20%2F%2025-orange.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.x-blue.svg)
![Platform](https://img.shields.io/badge/Platform-macOS%20%7C%20Windows-brightgreen.svg)

---

## 🌟 Overview

**Cyclops** is a modern data IDE designed for data engineers, backend developers, and software architects working with heterogeneous data formats. Whether inspecting binary columnar data, evaluating complex XPath/XQuery expressions, or querying CSV logs via SQL, Cyclops provides a single unified workspace with **automated format detection** and **context-sensitive developer tooling**.

---

## ✨ Key Features

- **Automated File Format Detection**:
  - Sniffs magic byte headers (`PAR1` for Parquet, `Obj\x01` for Avro, `ORC` for ORC, XML prologs, JSON braces, CSV delimiter density).
  - Automatically configures the workspace viewers, syntax highlighting, and query engines upon opening or dragging & dropping files.
- **Native Side-by-Side Workspace**:
  - Left pane: Syntax-highlighted Code Editor (`RSyntaxTextArea`) with line numbers, code folding, find, and formatting tools.
  - Right pane: Tabbed Visual Inspectors & Query Consoles.
  - View switcher buttons: `↔ Side-by-Side`, `◀ Editor Only`, `▶ Inspector Only`.
- **Infinite-Depth XML Tree-Grid Viewer**:
  - Renders XML hierarchies recursively to any nesting depth with collapsible toggle nodes (`▼` / `▶`).
  - **1-Row Data Tables**: Non-repeatable single complex elements render as 1-row data tables.
  - **Multi-Row Data Tables**: Repeatable element lists (e.g. `<entry>` 4 rows, `<PSQLSelColumn>` 40 rows) render as indexed multi-row data tables (`# 1, 2, 3...`).
  - **Property Tables**: Node attributes (`@id`, `@date`) and scalar fields display as styled property rows.
  - **Live Filter**: Instant regex filtering across tags, attributes, property values, and data table rows with auto-expanded matching nodes.
- **Internal Query Engines**:
  - **XML**: Saxon-HE supporting full **XPath 3.1** and **XQuery 3.1** standards.
  - **JSON**: Jayway **JSONPath** query runner and JSON tree inspector.
  - **CSV / Parquet / Avro / ORC**: Embedded **DuckDB SQL Engine** (`SELECT * FROM data WHERE ...`) executing ANSI-SQL queries over tabular data files.
- **Freely Movable Floating Windows**:
  - Click `↗ Float Window` on any view (Data Grid, Tree Inspector, Query Console) to pop it out into an independent desktop window that can be moved across multi-monitors or docked back (`↙ Dock Back`).
- **5-Second Startup Splash Screen**:
  - Dark glass startup window displaying the Cyclops logo, tagline, animated progress bar, and resource initialization milestones.
- **Bookmarking & Active Stopwatch Clock**:
  - `⭐ Bookmark File` button tags line numbers and files with 1-click navigation dropdown menu (`⭐ Bookmarks ▾`).
  - Active Tab Stopwatch (`⏱ Active Tab Time: 02m 45s`) tracks time spent per tab.
- **Native Glassmorphic Drag & Drop**:
  - Visual overlay when dragging files over Cyclops, opening dropped files in new tabs or switching if already open.
- **Universal Generic Viewer Fallback**:
  - Unknown or raw binary/text files open in a line-numbered text editor or 16-byte raw hex dump inspector with character encoding stats.

---

## 🚀 Quick Start

### Prerequisites
- **Java 21+** (Tested on OpenJDK 25)
- **Gradle 9.x** (Gradle wrapper included)

### Running Locally

Clone the repository and run:

```bash
# Via Gradle Wrapper
./gradlew run
```

Or build and run the standalone runnable Jar:

```bash
./gradlew clean jar
java -jar build/libs/Cyclops-1.0.0.jar
```

---

## 📦 Building Native Installers / Executables

### macOS (`.app` Bundle & Launcher)
Run the macOS packaging script:

```bash
./package-app.sh
```

**Output Artifacts (`dist/macOS/`):**
- Native macOS App Bundle: `dist/macOS/Cyclops.app`
- Standalone Launcher Script: `dist/macOS/run-cyclops.command`
- Fat Runnable Jar: `dist/macOS/Cyclops-1.0.0.jar`

### Windows (`.exe` Executable & Launcher)
Run the Windows packaging batch script:

```cmd
package-windows.bat
```

**Output Artifacts (`dist/windows/`):**
- Native Windows Executable: `dist/windows/Cyclops/Cyclops.exe`
- Standalone Batch Launcher: `dist/windows/run-cyclops.bat`
- Fat Runnable Jar: `dist/windows/Cyclops-1.0.0.jar`

---

## 📂 Project Architecture

```
Cyclops
├── build.gradle                 # Gradle Groovy DSL script & dependencies
├── settings.gradle              # Gradle settings
├── package-app.sh               # macOS packaging script
├── package-windows.bat          # Windows packaging script
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── cyclops
    │               ├── Main.java                        # Entry point & splash launch
    │               ├── engine
    │               │   ├── JsonQueryEngine.java         # Jayway JSONPath engine
    │               │   ├── SqlQueryEngine.java          # DuckDB JDBC SQL engine
    │               │   └── XmlQueryEngine.java          # Saxon-HE XPath/XQuery 3.1 engine
    │               ├── model
    │               │   ├── FileHeader.java              # Magic bytes header inspector
    │               │   ├── FileType.java                # Supported format enum
    │               │   └── QueryResult.java             # Query execution result model
    │               ├── plugin
    │               │   ├── FileTypePlugin.java          # Extension interface for plugins
    │               │   ├── PluginRegistry.java          # Plugin registry service
    │               │   └── builtin/                     # Built-in plugins (XML, JSON, CSV, Parquet, Avro, ORC, Generic)
    │               ├── service
    │               │   ├── BookmarkManager.java         # Bookmarking service
    │               │   ├── FileDetector.java            # Automated magic byte sniffer
    │               │   ├── SampleDataLoader.java        # Pre-loaded sample datasets
    │               │   └── ThemeManager.java            # FlatLaf Light/Dark theme manager
    │               └── ui
    │                   ├── CyclopsMainFrame.java        # Primary Desktop Window
    │                   ├── DockablePanelWrapper.java    # Floating / Docking window wrapper
    │                   ├── DocumentTabPanel.java        # Workspace document tab
    │                   ├── DragDropOverlayPanel.java    # Native Drag & Drop overlay
    │                   ├── EditorPanel.java             # Code editor wrapper
    │                   ├── GenericViewerPanel.java      # Generic text/hex fallback
    │                   ├── QueryConsolePanel.java       # Format query console
    │                   ├── RichTreeCellRenderer.java    # HTML syntax color tree renderer
    │                   ├── SplashScreen.java            # 5-second startup splash screen
    │                   ├── TableGridPanel.java          # Tabular dataset JTable grid
    │                   └── XmlGroupedTableView.java     # Infinite-depth XML Tree-Grid
    └── test
        └── java
            └── com
                └── cyclops/                             # JUnit 5 test suites
```

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
