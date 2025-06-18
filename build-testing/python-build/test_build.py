#!/usr/bin/env python3
"""
Simple test script to verify PyInstaller can bundle Python backend with dependencies.
This script tests that NumPy and SciPy load correctly in a bundled executable.
"""

import sys
import os

def test_dependencies():
    """Test that all required Python dependencies load correctly."""
    print("=" * 50)
    print("QuickCalc Backend Build Test")
    print("=" * 50)
    
    # Test Python version
    print(f"Python version: {sys.version}")
    print(f"Executable path: {sys.executable}")
    
    # Test NumPy
    try:
        import numpy as np
        print(f"✓ NumPy {np.__version__} loaded successfully")
        
        # Test basic NumPy functionality
        test_array = np.array([1, 2, 3, 4, 5])
        result = np.sum(test_array)
        print(f"  - NumPy sum test: {test_array} → {result}")
        
    except ImportError as e:
        print(f"✗ NumPy failed to load: {e}")
        return False
    
    # Test SciPy
    try:
        import scipy
        from scipy import linalg
        print(f"✓ SciPy {scipy.__version__} loaded successfully")
        
        # Test basic SciPy functionality
        test_matrix = np.array([[1, 2], [3, 4]])
        det = linalg.det(test_matrix)
        print(f"  - SciPy linalg test: determinant = {det}")
        
    except ImportError as e:
        print(f"✗ SciPy failed to load: {e}")
        return False
    
    # Test other dependencies that QuickCalc will need
    dependencies = ['pandas', 'reportlab']
    for dep in dependencies:
        try:
            __import__(dep)
            print(f"✓ {dep} loaded successfully")
        except ImportError:
            print(f"! {dep} not available (will be needed later)")
    
    print("=" * 50)
    print("Backend dependencies test completed successfully!")
    print("Ready to proceed with PyInstaller build test.")
    print("=" * 50)
    
    return True

if __name__ == "__main__":
    success = test_dependencies()
    
    # Keep console open if run directly (useful for testing executable)
    if len(sys.argv) > 1 and sys.argv[1] == "--pause":
        input("Press Enter to exit...")
    
    sys.exit(0 if success else 1)