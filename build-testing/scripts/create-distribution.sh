#!/bin/bash
# QuickCalc Distribution Package Creator
# Creates a complete distribution package with both frontend and backend

set -e  # Exit on any error

echo "======================================"
echo "QuickCalc Distribution Creator"
echo "======================================"

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DIR="$PROJECT_ROOT/build-testing"
DIST_DIR="$BUILD_DIR/distribution"
VERSION="1.0.0"
DIST_NAME="QuickCalc-v${VERSION}-Linux"

# Check if distribution components exist
echo "Checking distribution components..."

BACKEND_EXEC="$DIST_DIR/quickcalc_backend"
FRONTEND_DEB="$DIST_DIR/quickcalc_1.0-1_amd64.deb"
FRONTEND_APP="$DIST_DIR/QuickCalc"

HAS_BACKEND=false
HAS_FRONTEND=false

if [ -f "$BACKEND_EXEC" ]; then
    echo "✓ Backend executable found"
    HAS_BACKEND=true
else
    echo "! Backend executable not found at $BACKEND_EXEC"
fi

if [ -f "$FRONTEND_DEB" ]; then
    echo "✓ Frontend DEB package found"
    HAS_FRONTEND=true
    FRONTEND_FILE="$FRONTEND_DEB"
elif [ -d "$FRONTEND_APP" ]; then
    echo "✓ Frontend application directory found"
    HAS_FRONTEND=true
    FRONTEND_FILE="$FRONTEND_APP"
else
    echo "! Frontend package not found"
fi

if [ "$HAS_BACKEND" = false ] && [ "$HAS_FRONTEND" = false ]; then
    echo "Error: No distribution components found"
    echo "Please run build-backend.sh and build-frontend.sh first"
    exit 1
fi

# Create distribution package
echo "Creating distribution package..."
PACKAGE_DIR="$DIST_DIR/$DIST_NAME"
mkdir -p "$PACKAGE_DIR"

# Copy components
if [ "$HAS_BACKEND" = true ]; then
    echo "Copying backend executable..."
    cp "$BACKEND_EXEC" "$PACKAGE_DIR/"
    chmod +x "$PACKAGE_DIR/quickcalc_backend"
fi

if [ "$HAS_FRONTEND" = true ]; then
    echo "Copying frontend..."
    if [ -f "$FRONTEND_DEB" ]; then
        cp "$FRONTEND_DEB" "$PACKAGE_DIR/"
    elif [ -d "$FRONTEND_APP" ]; then
        cp -r "$FRONTEND_APP" "$PACKAGE_DIR/"
    fi
fi

# Create README for the package
cat > "$PACKAGE_DIR/README.txt" << EOF
QuickCalc v${VERSION} - Structural Beam Analysis
================================================

This package contains the QuickCalc desktop application for structural engineers.

Contents:
EOF

if [ "$HAS_BACKEND" = true ]; then
    cat >> "$PACKAGE_DIR/README.txt" << EOF
- quickcalc_backend: Python FEA calculation engine
EOF
fi

if [ "$HAS_FRONTEND" = true ]; then
    if [ -f "$FRONTEND_DEB" ]; then
        cat >> "$PACKAGE_DIR/README.txt" << EOF
- quickcalc_1.0-1_amd64.deb: JavaFX frontend installer for Ubuntu/Debian
EOF
    elif [ -d "$FRONTEND_APP" ]; then
        cat >> "$PACKAGE_DIR/README.txt" << EOF
- QuickCalc/: JavaFX frontend application directory
EOF
    fi
fi

cat >> "$PACKAGE_DIR/README.txt" << EOF

Installation:
1. For DEB package: sudo dpkg -i quickcalc_1.0-1_amd64.deb
2. For portable app: Run QuickCalc/bin/QuickCalc

Testing:
- Test backend: ./quickcalc_backend test
- Frontend will automatically find and use the backend

For more information, visit: https://github.com/your-username/QuickCalc
EOF

# Create TAR.GZ archive (more universal than ZIP on Linux)
echo "Creating TAR.GZ archive..."
cd "$DIST_DIR"
tar -czf "${DIST_NAME}.tar.gz" "$DIST_NAME/"

# Show results
echo "======================================"
echo "Distribution package created!"
echo "======================================"
echo "Package: $DIST_DIR/${DIST_NAME}.tar.gz"
echo "Contents:"
if [ "$HAS_BACKEND" = true ]; then
    BACKEND_SIZE=$(du -h "$PACKAGE_DIR/quickcalc_backend" | cut -f1)
    echo "  - Backend: $BACKEND_SIZE"
fi
if [ "$HAS_FRONTEND" = true ]; then
    if [ -f "$PACKAGE_DIR/quickcalc_1.0-1_amd64.deb" ]; then
        FRONTEND_SIZE=$(du -h "$PACKAGE_DIR/quickcalc_1.0-1_amd64.deb" | cut -f1)
        echo "  - Frontend (DEB): $FRONTEND_SIZE"
    elif [ -d "$PACKAGE_DIR/QuickCalc" ]; then
        FRONTEND_SIZE=$(du -sh "$PACKAGE_DIR/QuickCalc" | cut -f1)
        echo "  - Frontend (App): $FRONTEND_SIZE"
    fi
fi

TOTAL_SIZE=$(du -sh "$DIST_DIR/${DIST_NAME}.tar.gz" | cut -f1)
echo "  - Total Archive: $TOTAL_SIZE"

echo ""
echo "Ready for distribution to non-coders!"
echo "======================================"