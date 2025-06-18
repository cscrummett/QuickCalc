#!/bin/bash
# QuickCalc Frontend Build Script
# Builds the JavaFX frontend into a standalone application

set -e  # Exit on any error

echo "======================================"
echo "QuickCalc Frontend Build Script"
echo "======================================"

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DIR="$PROJECT_ROOT/build-testing"
JAVAFX_BUILD="$BUILD_DIR/javafx-build"
DIST_DIR="$BUILD_DIR/distribution"

# Check if JavaFX build directory exists
if [ ! -d "$JAVAFX_BUILD" ]; then
    echo "Error: JavaFX build directory not found at $JAVAFX_BUILD"
    echo "Please copy JavaFX assets first"
    exit 1
fi

# Create distribution directory
mkdir -p "$DIST_DIR"

# Navigate to JavaFX build directory
cd "$JAVAFX_BUILD"

# Check for required directories
if [ ! -d "bin" ] || [ ! -d "javafx-sdk" ]; then
    echo "Error: Missing required directories (bin or javafx-sdk)"
    echo "Please ensure JavaFX assets are copied correctly"
    exit 1
fi

# Clean previous builds
echo "Cleaning previous builds..."
rm -rf custom-jre app-image

# Create custom JRE with jlink
echo "Creating custom JRE with jlink..."
jlink --module-path javafx-sdk/lib \
      --add-modules javafx.controls,javafx.fxml \
      --output custom-jre

if [ ! -d "custom-jre" ]; then
    echo "Error: Failed to create custom JRE"
    exit 1
fi

echo "✓ Custom JRE created successfully"

# Create application package with jpackage
echo "Creating application package with jpackage..."
jpackage --input bin \
         --main-jar . \
         --main-class com.quickcalc.Main \
         --runtime-image custom-jre \
         --name QuickCalc \
         --dest ./app-image

# Check if package was created
if [ -f "app-image/quickcalc_1.0-1_amd64.deb" ]; then
    echo "✓ DEB package created successfully"
    
    # Copy to distribution directory
    echo "Copying package to distribution..."
    cp "app-image/quickcalc_1.0-1_amd64.deb" "$DIST_DIR/"
    
    # Show file size
    FRONTEND_SIZE=$(du -h "$DIST_DIR/quickcalc_1.0-1_amd64.deb" | cut -f1)
    echo "✓ Frontend package built successfully ($FRONTEND_SIZE)"
    echo "Location: $DIST_DIR/quickcalc_1.0-1_amd64.deb"
    
elif [ -d "app-image/QuickCalc" ]; then
    echo "✓ Application directory created successfully"
    
    # For other platforms, copy the entire app directory
    echo "Copying application directory to distribution..."
    cp -r "app-image/QuickCalc" "$DIST_DIR/"
    
    APP_SIZE=$(du -sh "$DIST_DIR/QuickCalc" | cut -f1)
    echo "✓ Frontend application built successfully ($APP_SIZE)"
    echo "Location: $DIST_DIR/QuickCalc/"
else
    echo "✗ No package or application directory found"
    echo "jpackage may have failed"
    exit 1
fi

echo "======================================"
echo "Frontend build completed!"
echo "======================================"