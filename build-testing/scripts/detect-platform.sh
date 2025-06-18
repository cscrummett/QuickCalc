#!/bin/bash
# Platform Detection Script for QuickCalc
# Detects the current platform and suggests appropriate build approach

echo "======================================"
echo "QuickCalc Platform Detection"
echo "======================================"

# Detect operating system
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    PLATFORM="Linux"
    BACKEND_EXT=""
    FRONTEND_TYPE="deb"
    ARCHIVE_TYPE="tar.gz"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    PLATFORM="macOS"
    BACKEND_EXT=""
    FRONTEND_TYPE="dmg"
    ARCHIVE_TYPE="tar.gz"
elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    PLATFORM="Windows"
    BACKEND_EXT=".exe"
    FRONTEND_TYPE="msi"
    ARCHIVE_TYPE="zip"
else
    PLATFORM="Unknown"
fi

echo "Detected Platform: $PLATFORM"
echo "Backend Extension: $BACKEND_EXT"
echo "Frontend Package: $FRONTEND_TYPE"
echo "Archive Format: $ARCHIVE_TYPE"

echo ""
echo "Build Recommendations:"
echo "======================================"

if [[ "$PLATFORM" == "Windows" ]]; then
    echo "✓ Perfect! You're on Windows."
    echo "✓ Run: build-testing/scripts/build-windows.bat"
    echo "✓ This will create Windows .exe and .msi files"
    
elif [[ "$PLATFORM" == "Linux" ]]; then
    echo "⚠ You're on Linux, but need Windows distribution."
    echo ""
    echo "Options:"
    echo "1. Use WSL/Linux for development, then:"
    echo "   - Copy project to Windows machine"
    echo "   - Run build-windows.bat on Windows"
    echo ""
    echo "2. Set up Windows VM with:"
    echo "   - Java 17"
    echo "   - Python 3.8+"
    echo "   - Your project files"
    echo ""
    echo "3. Cross-compile (advanced):"
    echo "   - Use Wine to run Windows tools"
    echo "   - May have compatibility issues"
    
elif [[ "$PLATFORM" == "macOS" ]]; then
    echo "⚠ You're on macOS, but need Windows distribution."
    echo ""
    echo "Options:"
    echo "1. Use Boot Camp or Parallels with Windows"
    echo "2. Copy project to Windows machine"
    echo "3. Use GitHub Actions for Windows builds"
    
else
    echo "❌ Unknown platform detected"
    echo "Please run this on Windows for Windows distribution"
fi

echo ""
echo "Target Users Need:"
echo "- quickcalc_backend.exe (26MB)"
echo "- QuickCalc-1.0.msi (Windows installer)"
echo "- No Java/Python installation required"
echo "======================================"