#!/usr/bin/env python3
"""
QuickCalc Command-Line Backend
Processes beam analysis requests from JavaFX frontend using actual FEA code.
"""

import sys
import json
import os
from pathlib import Path

try:
    # Import the actual FEA modules (now in same directory)
    from fea import BeamAnalyzer, SupportType, PointLoad, DistributedLoad, Support
    from data import BeamData
    from load_combinations import LoadCombination
    print("✓ FEA modules imported successfully", file=sys.stderr)
    FEA_AVAILABLE = True
except ImportError as e:
    print(f"Warning: Could not import FEA modules: {e}", file=sys.stderr)
    print("Using mock analysis instead.", file=sys.stderr)
    FEA_AVAILABLE = False

def mock_beam_analysis(beam_data):
    """Mock analysis for testing when FEA modules aren't available."""
    return {
        "success": True,
        "beam_length": beam_data.get("length", 10.0),
        "max_moment": 125.5,
        "max_shear": 50.2,
        "max_deflection": 0.75,
        "reactions": [
            {"location": 0.0, "force": 25.1},
            {"location": beam_data.get("length", 10.0), "force": 25.1}
        ],
        "analysis_type": "mock",
        "message": "Mock analysis completed - FEA modules not available"
    }

def real_beam_analysis(beam_data):
    """Perform actual FEA analysis using the imported modules."""
    try:
        # Extract beam parameters
        length = beam_data.get("length", 10.0)  # feet
        E = beam_data.get("E", 29000.0)  # ksi
        I = beam_data.get("I", 100.0)  # in^4
        
        # Create beam analyzer
        analyzer = BeamAnalyzer(length=length, E=E, I=I)
        
        # Add supports if provided
        supports = beam_data.get("supports", [])
        for support_data in supports:
            position = support_data.get("position", 0.0)
            support_type_str = support_data.get("type", "pinned")
            
            # Convert string to SupportType enum
            if support_type_str.lower() == "fixed":
                support_type = SupportType.FIXED
            elif support_type_str.lower() == "roller":
                support_type = SupportType.ROLLER
            else:
                support_type = SupportType.PINNED
            
            analyzer.add_support(position, support_type)
        
        # Create point loads list
        point_loads_list = []
        point_loads = beam_data.get("point_loads", [])
        for load_data in point_loads:
            position = load_data.get("position", 0.0)
            magnitude = load_data.get("magnitude", 0.0)
            load = PointLoad(position=position, magnitude=magnitude)
            point_loads_list.append(load)
        
        # Create distributed loads list
        distributed_loads_list = []
        distributed_loads = beam_data.get("distributed_loads", [])
        for load_data in distributed_loads:
            start_pos = load_data.get("start_pos", 0.0)
            end_pos = load_data.get("end_pos", length)
            start_mag = load_data.get("start_magnitude", 0.0)
            end_mag = load_data.get("end_magnitude", 0.0)
            load = DistributedLoad(
                start_pos=start_pos, 
                end_pos=end_pos,
                start_magnitude=start_mag,
                end_magnitude=end_mag
            )
            distributed_loads_list.append(load)
        
        # Perform analysis with loads
        results = analyzer.solve(
            point_loads=point_loads_list,
            distributed_loads=distributed_loads_list
        )
        
        # Extract results and convert to JSON-serializable format
        max_deflection = results.get("max_deflection", {"value": 0.0, "location_ft": 0.0})
        support_reactions = results.get("support_reactions", {})
        
        # Convert support reactions to list format
        reactions_list = []
        for position, reaction_data in support_reactions.items():
            reactions_list.append({
                "location": float(position),
                "force": float(reaction_data.get("force", 0.0)),
                "moment": float(reaction_data.get("moment", 0.0)),
                "type": reaction_data.get("type", "unknown")
            })
        
        return {
            "success": True,
            "beam_length": length,
            "max_deflection": {
                "value": float(max_deflection.get("value", 0.0)),
                "location": float(max_deflection.get("location_ft", 0.0))
            },
            "reactions": reactions_list,
            "num_elements": results.get("num_elements", 0),
            "node_positions": [float(pos) for pos in results.get("node_positions", [])],
            "displacements": [float(d) for d in results.get("displacements", [])],
            "analysis_type": "real_fea",
            "message": "FEA analysis completed successfully"
        }
        
    except Exception as e:
        return {
            "success": False,
            "error": f"FEA analysis failed: {str(e)}",
            "analysis_type": "real_fea_error"
        }

def main():
    """Main command-line interface."""
    
    if len(sys.argv) < 2:
        print(json.dumps({
            "success": False,
            "error": "Usage: quickcalc_backend.py <command> [args...]"
        }))
        sys.exit(1)
    
    command = sys.argv[1]
    
    if command == "analyze":
        if len(sys.argv) != 3:
            print(json.dumps({
                "success": False,
                "error": "Usage: quickcalc_backend.py analyze <input_json_file>"
            }))
            sys.exit(1)
        
        input_file = sys.argv[2]
        
        try:
            with open(input_file, 'r') as f:
                beam_data = json.load(f)
            
            # Use real FEA if available, otherwise mock
            if FEA_AVAILABLE:
                results = real_beam_analysis(beam_data)
            else:
                results = mock_beam_analysis(beam_data)
            
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
                "error": f"Invalid JSON: {e}"
            }))
            sys.exit(1)
    
    elif command == "version":
        print(json.dumps({
            "success": True,
            "version": "1.0.0",
            "backend": "QuickCalc FEA Engine"
        }))
    
    elif command == "test":
        print(json.dumps({
            "success": True,
            "message": "Backend is working correctly",
            "python_version": sys.version
        }))
    
    else:
        print(json.dumps({
            "success": False,
            "error": f"Unknown command: {command}"
        }))
        sys.exit(1)

if __name__ == "__main__":
    main()