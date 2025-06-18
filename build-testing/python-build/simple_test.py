#!/usr/bin/env python3
"""
Minimal test script to verify PyInstaller works without complex dependencies.
"""

import sys
import os

def main():
    print("=" * 40)
    print("QuickCalc Simple Build Test")
    print("=" * 40)
    
    print(f"Python version: {sys.version}")
    print(f"Platform: {sys.platform}")
    
    # Test NumPy only (skip SciPy for now)
    try:
        import numpy as np
        print(f"✓ NumPy {np.__version__} loaded successfully")
        
        # Simple calculation
        result = np.sum([1, 2, 3, 4, 5])
        print(f"  - NumPy test: sum([1,2,3,4,5]) = {result}")
        
    except ImportError as e:
        print(f"✗ NumPy failed: {e}")
        return False
    
    print("=" * 40)
    print("✓ Simple test completed successfully!")
    print("Ready for PyInstaller build test.")
    print("=" * 40)
    
    return True

if __name__ == "__main__":
    success = main()
    
    # Keep console open if run directly
    if len(sys.argv) > 1 and sys.argv[1] == "--pause":
        input("Press Enter to exit...")
    
    sys.exit(0 if success else 1)