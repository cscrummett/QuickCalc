#!/usr/bin/env python3
"""
Command-line FEA backend for QuickCalc that JavaFX can call directly.
This version processes JSON input and returns JSON output without Flask.
"""

import sys
import json
import os

def simple_beam_analysis(beam_data):
    """
    Simplified beam analysis that will eventually call your FEA code.
    For now, returns mock results to test the build pipeline.
    """
    
    # Mock analysis results
    results = {
        "success": True,
        "beam_length": beam_data.get("length", 10.0),
        "max_moment": 125.5,  # kip-ft
        "max_shear": 50.2,    # kips
        "max_deflection": 0.75,  # inches
        "reactions": [
            {"location": 0.0, "force": 25.1},
            {"location": beam_data.get("length", 10.0), "force": 25.1}
        ],
        "message": "FEA analysis completed successfully (mock results)"
    }
    
    return results

def main():
    """Main function for command-line interface."""
    
    if len(sys.argv) != 2:
        print(json.dumps({
            "success": False,
            "error": "Usage: fea_backend.py <input_json_file>"
        }))
        sys.exit(1)
    
    input_file = sys.argv[1]
    
    try:
        # Read beam data from JSON file
        with open(input_file, 'r') as f:
            beam_data = json.load(f)
        
        # Perform analysis
        results = simple_beam_analysis(beam_data)
        
        # Output results as JSON
        print(json.dumps(results, indent=2))
        
    except FileNotFoundError:
        print(json.dumps({
            "success": False,
            "error": f"Input file not found: {input_file}"
        }))
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(json.dumps({
            "success": False,
            "error": f"Invalid JSON in input file: {e}"
        }))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": f"Analysis failed: {str(e)}"
        }))
        sys.exit(1)

if __name__ == "__main__":
    main()