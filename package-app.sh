#!/bin/bash

# ==============================================================================
# Cyclops IDE - macOS Application & Installer Packaging Script
# ==============================================================================

set -e

echo "👁 Packaging Cyclops IDE for macOS..."

# 1. Ensure clean Gradle build and Fat JAR exist
./gradlew clean jar

JAR_PATH="build/libs/Cyclops-1.0.0.jar"
APP_NAME="Cyclops"
OUTPUT_DIR="dist/macOS"

mkdir -p "$OUTPUT_DIR"
rm -rf "$OUTPUT_DIR/$APP_NAME.app"

echo "📦 Creating macOS Native App Bundle using jpackage..."

if command -v jpackage &> /dev/null; then
    jpackage \
      --type app-image \
      --name "$APP_NAME" \
      --input build/libs \
      --main-jar Cyclops-1.0.0.jar \
      --main-class com.cyclops.Main \
      --dest "$OUTPUT_DIR" \
      --java-options "-Dapple.laf.useScreenMenuBar=true -Dapple.awt.application.name=Cyclops" \
      --app-version 1.0.0

    echo "✅ Native macOS App Bundle created at: $OUTPUT_DIR/$APP_NAME.app"
else
    echo "⚠️ jpackage not found in PATH. Creating standalone macOS Launcher..."
fi

# Create standalone double-clickable macOS Launcher script
LAUNCHER_SCRIPT="$OUTPUT_DIR/run-cyclops.command"
cat << 'EOF' > "$LAUNCHER_SCRIPT"
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -jar "$DIR/Cyclops-1.0.0.jar"
EOF

chmod +x "$LAUNCHER_SCRIPT"
cp "$JAR_PATH" "$OUTPUT_DIR/"

echo "------------------------------------------------------------------------------"
echo "🎉 macOS Packaging Complete!"
echo "📍 Standalone Executable Jar & Launcher: $OUTPUT_DIR/"
echo "▶ To run on macOS, double-click: $OUTPUT_DIR/run-cyclops.command"
echo "=============================================================================="
