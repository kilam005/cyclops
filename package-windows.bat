@echo off
REM ==============================================================================
REM Cyclops IDE - Windows Executable (.exe) & Installer Packaging Script
REM ==============================================================================

echo 👁 Packaging Cyclops IDE for Windows...

REM 1. Build clean Fat JAR with Gradle
call gradlew.bat clean jar

set JAR_PATH=build\libs\Cyclops-1.0.0.jar
set APP_NAME=Cyclops
set OUTPUT_DIR=dist\windows

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo 📦 Packaging Windows Executable using jpackage...

where jpackage >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    jpackage --type app-image --name %APP_NAME% --input build\libs --main-jar Cyclops-1.0.0.jar --main-class com.cyclops.Main --dest %OUTPUT_DIR% --app-version 1.0.0
    echo ✅ Native Windows Application created at: %OUTPUT_DIR%\%APP_NAME%\%APP_NAME%.exe
) else (
    echo ⚠️ jpackage not found in PATH. Creating double-clickable batch launcher...
)

copy "%JAR_PATH%" "%OUTPUT_DIR%\" >nul

REM Create double-clickable batch launcher Cyclops.bat
(
echo @echo off
echo start javaw -jar "%%~dp0Cyclops-1.0.0.jar"
) > "%OUTPUT_DIR%\run-cyclops.bat"

echo ------------------------------------------------------------------------------
echo 🎉 Windows Packaging Complete!
echo 📍 Executables & Launchers generated in: %OUTPUT_DIR%\
echo ▶ To run on Windows, double-click: %OUTPUT_DIR%\run-cyclops.bat or %OUTPUT_DIR%\Cyclops\Cyclops.exe
echo ==============================================================================
