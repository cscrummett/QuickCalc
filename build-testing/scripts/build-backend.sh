#!/bin/bash
# QuickCalc Backend Build Script
# Builds the Python backend into a standalone executable

set -e  # Exit on any error

echo "======================================"
echo "QuickCalc Backend Build Script"
echo "======================================"

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DIR="$PROJECT_ROOT/build-testing"
PYTHON_ENV="$BUILD_DIR/python-env"
BACKEND_SRC="$BUILD_DIR/python-build"
DIST_DIR="$BUILD_DIR/distribution"

# Check if virtual environment exists
if [ ! -d "$PYTHON_ENV" ]; then
    echo "Error: Python virtual environment not found at $PYTHON_ENV"
    echo "Please run setup first or create virtual environment"
    exit 1
fi

# Activate virtual environment
echo "Activating Python virtual environment..."
source "$PYTHON_ENV/bin/activate"

# Check if PyInstaller is available
if ! command -v pyinstaller &> /dev/null; then
    echo "PyInstaller not found in virtual environment. Installing..."
    pip install pyinstaller
fi

# Create distribution directory
mkdir -p "$DIST_DIR"

# Build the backend executable
echo "Building QuickCalc backend executable..."
cd "$PROJECT_ROOT"

# Clean previous builds
rm -rf "$BACKEND_SRC/build/quickcalc_backend"
rm -rf "$BACKEND_SRC/dist/quickcalc_backend"

# Build with PyInstaller
$PYTHON_ENV/bin/python -m PyInstaller \
    --onefile \
    --distpath "$BACKEND_SRC/dist" \
    --workpath "$BACKEND_SRC/build" \
    --name quickcalc_backend \
    "$BACKEND_SRC/quickcalc_backend.py"

# Copy to distribution directory
echo "Copying backend executable to distribution..."
cp "$BACKEND_SRC/dist/quickcalc_backend" "$DIST_DIR/"

# Test the executable
echo "Testing backend executable..."
if "$DIST_DIR/quickcalc_backend" test > /dev/null; then
    echo "✓ Backend executable test passed"
else
    echo "✗ Backend executable test failed"
    exit 1
fi

# Show file size
BACKEND_SIZE=$(du -h "$DIST_DIR/quickcalc_backend" | cut -f1)
echo "✓ Backend executable built successfully ($BACKEND_SIZE)"
echo "Location: $DIST_DIR/quickcalc_backend"

echo "======================================"
echo "Backend build completed!"
echo "======================================"