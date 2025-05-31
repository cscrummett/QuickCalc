"""
Test load combinations for beam analysis.
"""
import sys
import os
import numpy as np
import matplotlib.pyplot as plt
from pathlib import Path

# Add project root to path for imports
project_root = str(Path(__file__).parent.parent)
if project_root not in sys.path:
    sys.path.append(project_root)

from backend.fea import BeamAnalyzer, PointLoad, DistributedLoad, SupportType
from backend.load_combinations import LoadCombinationManager, LoadType, LoadCase

def test_simply_supported_beam_load_combinations():
    """Test load combinations on a simply supported beam."""
    print("\n=== Testing Load Combinations on Simply Supported Beam ===")
    
    # Create a 20 ft beam
    beam = BeamAnalyzer(length=20.0, min_elements=20)
    
    # Create load combination manager
    lcm = LoadCombinationManager()
    
    # Add load cases
    # Dead load: uniform 1 kip/ft
    dead_load_case = LoadCase(
        load_type=LoadType.DEAD,
        distributed_loads=[
            DistributedLoad(
                start_pos=0.0,
                end_pos=20.0,
                start_magnitude=-1.0,
                end_magnitude=-1.0
            )
        ]
    )
    lcm.add_load_case(LoadType.DEAD, dead_load_case)
    
    # Live load: point load of 10 kips at center
    live_load_case = LoadCase(
        load_type=LoadType.LIVE,
        point_loads=[
            PointLoad(
                position=10.0,
                magnitude=-10.0
            )
        ]
    )
    lcm.add_load_case(LoadType.LIVE, live_load_case)
    
    # Add a custom load combination
    lcm.add_custom_combination(
        name="Custom: 1.1D + 1.5L",
        factors={
            LoadType.DEAD: 1.1,
            LoadType.LIVE: 1.5
        }
    )
    
    # Solve for different load combinations
    combinations = [
        "Service",  # 1.0D + 1.0L
        "ASCE 7: 1.2D + 1.6L + 0.5(Lr/S/R)",  # 1.2D + 1.6L
        "Custom: 1.1D + 1.5L"
    ]
    
    results = {}
    max_deflections = {}
    
    for combo in combinations:
        print(f"\nSolving for load combination: {combo}")
        result = beam.solve(load_combination=combo, load_combination_manager=lcm)
        results[combo] = result
        max_deflections[combo] = result['max_deflection']['value']
        
        # Print summary
        print(beam.get_summary())
        
        # Verify equilibrium
        reactions = result['support_reactions']
        total_reaction = sum(r['force'] for r in reactions.values())
        
        # Calculate applied loads based on the combination
        if combo == "Service":
            expected_load = 20.0 + 10.0  # 1.0 * (1.0 kip/ft * 20 ft) + 1.0 * 10 kips
        elif combo == "ASCE 7: 1.2D + 1.6L + 0.5(Lr/S/R)":
            expected_load = 1.2 * 20.0 + 1.6 * 10.0  # 1.2 * (1.0 kip/ft * 20 ft) + 1.6 * 10 kips
        elif combo == "Custom: 1.1D + 1.5L":
            expected_load = 1.1 * 20.0 + 1.5 * 10.0  # 1.1 * (1.0 kip/ft * 20 ft) + 1.5 * 10 kips
        
        print(f"Total applied load: {expected_load:.2f} kips")
        print(f"Total reaction: {total_reaction:.2f} kips")
        print(f"Equilibrium check: {abs(total_reaction - expected_load) < 1e-6}")
    
    # Compare max deflections
    print("\n=== Maximum Deflection Comparison ===")
    for combo, deflection in max_deflections.items():
        print(f"{combo}: {deflection:.4f} in")
    
    # Plot deflections for comparison
    plt.figure(figsize=(10, 6))
    
    for combo, result in results.items():
        node_positions = result['node_positions']
        displacements = result['displacements'][::2]  # Extract vertical displacements
        plt.plot(node_positions, displacements, label=combo)
    
    plt.xlabel('Position (ft)')
    plt.ylabel('Deflection (in)')
    plt.title('Beam Deflection for Different Load Combinations')
    plt.grid(True)
    plt.legend()
    
    # Save the plot
    plt.savefig('load_combination_comparison.png')
    print("Plot saved as 'load_combination_comparison.png'")
    
    return results

def test_load_combination_json():
    """Test saving and loading load combinations to/from JSON."""
    print("\n=== Testing Load Combination JSON Serialization ===")
    
    # Create load combination manager
    lcm = LoadCombinationManager()
    
    # Add load cases
    dead_load_case = LoadCase(
        load_type=LoadType.DEAD,
        distributed_loads=[
            DistributedLoad(
                start_pos=0.0,
                end_pos=20.0,
                start_magnitude=-1.0,
                end_magnitude=-1.0
            )
        ]
    )
    lcm.add_load_case(LoadType.DEAD, dead_load_case)
    
    live_load_case = LoadCase(
        load_type=LoadType.LIVE,
        point_loads=[
            PointLoad(
                position=10.0,
                magnitude=-10.0
            )
        ]
    )
    lcm.add_load_case(LoadType.LIVE, live_load_case)
    
    # Add a custom load combination
    lcm.add_custom_combination(
        name="Custom: 1.1D + 1.5L",
        factors={
            LoadType.DEAD: 1.1,
            LoadType.LIVE: 1.5
        }
    )
    
    # Save to JSON
    json_file = "load_combinations.json"
    lcm.save_to_file(json_file)
    print(f"Saved load combinations to {json_file}")
    
    # Load from JSON
    loaded_lcm = LoadCombinationManager.load_from_file(json_file, PointLoad, DistributedLoad)
    
    # Verify loaded data
    print("\nVerifying loaded data:")
    
    # Check load cases
    for load_type, load_case in lcm.load_cases.items():
        loaded_case = loaded_lcm.load_cases.get(load_type)
        if loaded_case:
            print(f"Load case {load_type.value} loaded successfully")
            
            # Check point loads
            if len(load_case.point_loads) == len(loaded_case.point_loads):
                print(f"  Point loads match: {len(load_case.point_loads)}")
            else:
                print(f"  Point loads mismatch: {len(load_case.point_loads)} vs {len(loaded_case.point_loads)}")
            
            # Check distributed loads
            if len(load_case.distributed_loads) == len(loaded_case.distributed_loads):
                print(f"  Distributed loads match: {len(load_case.distributed_loads)}")
            else:
                print(f"  Distributed loads mismatch: {len(load_case.distributed_loads)} vs {len(loaded_case.distributed_loads)}")
        else:
            print(f"Load case {load_type.value} not found in loaded data")
    
    # Check combinations
    for combo in lcm.combinations:
        loaded_combo = next((c for c in loaded_lcm.combinations if c.name == combo.name), None)
        if loaded_combo:
            print(f"Combination '{combo.name}' loaded successfully")
            
            # Check factors
            factors_match = True
            for load_type, factor in combo.factors.items():
                loaded_factor = loaded_combo.factors.get(load_type)
                if loaded_factor != factor:
                    factors_match = False
                    print(f"  Factor mismatch for {load_type.value}: {factor} vs {loaded_factor}")
            
            if factors_match:
                print(f"  All factors match")
        else:
            print(f"Combination '{combo.name}' not found in loaded data")
    
    # Clean up
    if os.path.exists(json_file):
        os.remove(json_file)
        print(f"Removed {json_file}")
    
    return loaded_lcm

if __name__ == "__main__":
    # Run tests
    test_simply_supported_beam_load_combinations()
    test_load_combination_json()
