"""
Test to check if current BeamAnalyzer can handle multi-span beams.
This will reveal the limitations and what needs to be fixed.
"""
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from backend.fea import BeamAnalyzer, PointLoad, DistributedLoad, SupportType

def test_two_span_beam():
    """Test a two-span continuous beam."""
    print("=" * 60)
    print("MULTI-SPAN BEAM TEST")
    print("=" * 60)
    
    # Create a 40 ft beam with supports at 0, 20, and 40 ft (two 20-ft spans)
    beam = BeamAnalyzer(length=40.0, min_elements=30, E=29000.0, I=100.0)
    
    # Add supports: pinned at ends and center
    beam.add_support(0.0, SupportType.PINNED)   # Left end
    beam.add_support(20.0, SupportType.PINNED)  # Center support (creates two spans)
    beam.add_support(40.0, SupportType.PINNED)  # Right end
    
    print("Support Configuration:")
    for support in beam.supports:
        print(f"  {support.position:.1f} ft: {support.support_type.value}")
    
    # Apply loads to test behavior
    point_loads = [
        PointLoad(position=10.0, magnitude=10.0),  # Load on first span
        PointLoad(position=30.0, magnitude=8.0)    # Load on second span
    ]
    
    try:
        results = beam.solve(point_loads=point_loads)
        print("\nSolution completed successfully!")
        print(beam.get_summary())
        
        # Check if intermediate support has a reaction
        print(f"\nIntermediate Support Reactions:")
        for pos, reaction in results['support_reactions'].items():
            print(f"  At {pos:.1f} ft: {reaction['force']:.3f} kips")
        
        # Check equilibrium
        total_reactions = sum(r['force'] for r in results['support_reactions'].values())
        total_applied = sum(load.magnitude for load in point_loads)
        print(f"\nEquilibrium Check:")
        print(f"Total reactions: {total_reactions:.3f} kips")
        print(f"Total applied:   {total_applied:.3f} kips")
        print(f"Error: {abs(total_reactions - total_applied):.6f} kips")
        
        # Check deflection at supports (should be zero)
        print(f"\nDeflection at supports:")
        for support in beam.supports:
            deflection = beam.get_deflection_at(support.position)
            print(f"  At {support.position:.1f} ft: {deflection:.8f} in (should be ~0)")
        
        return True, results
        
    except Exception as e:
        print(f"\nERROR: {e}")
        return False, None

def test_three_span_beam():
    """Test a three-span continuous beam."""
    print("\n" + "=" * 60)
    print("THREE-SPAN BEAM TEST")
    print("=" * 60)
    
    # Create a 60 ft beam with supports at 0, 20, 40, and 60 ft (three 20-ft spans)
    beam = BeamAnalyzer(length=60.0, min_elements=40, E=29000.0, I=100.0)
    
    # Add supports
    beam.add_support(0.0, SupportType.PINNED)   # Left end
    beam.add_support(20.0, SupportType.PINNED)  # First intermediate
    beam.add_support(40.0, SupportType.PINNED)  # Second intermediate  
    beam.add_support(60.0, SupportType.PINNED)  # Right end
    
    print("Support Configuration:")
    for support in beam.supports:
        print(f"  {support.position:.1f} ft: {support.support_type.value}")
    
    # Apply uniform load over entire beam
    distributed_loads = [DistributedLoad(
        start_pos=0.0, 
        end_pos=60.0, 
        start_magnitude=2.0,  # 2 kips/ft
        end_magnitude=2.0
    )]
    
    try:
        results = beam.solve(distributed_loads=distributed_loads)
        print("\nSolution completed successfully!")
        print(beam.get_summary())
        
        # For a uniform load on equal spans, interior reactions should be higher
        print(f"\nSupport Reactions:")
        for pos, reaction in sorted(results['support_reactions'].items()):
            print(f"  At {pos:.1f} ft: {reaction['force']:.3f} kips")
        
        # Theoretical: for uniform load on continuous beam
        # End reactions ≈ 0.375wL, Interior reactions ≈ 1.25wL (per span)
        w = 2.0  # kips/ft
        L_span = 20.0  # ft per span
        theoretical_end = 0.375 * w * L_span  # ≈ 15 kips
        theoretical_interior = 1.25 * w * L_span  # ≈ 50 kips
        
        print(f"\nTheoretical reactions (approximate):")
        print(f"  End supports: ~{theoretical_end:.1f} kips each")
        print(f"  Interior supports: ~{theoretical_interior:.1f} kips each")
        
        return True, results
        
    except Exception as e:
        print(f"\nERROR: {e}")
        return False, None

def test_mixed_supports():
    """Test beam with mixed support types."""
    print("\n" + "=" * 60)
    print("MIXED SUPPORT TYPES TEST")
    print("=" * 60)
    
    # Create beam with different support types
    beam = BeamAnalyzer(length=30.0, min_elements=25, E=29000.0, I=100.0)
    
    # Mixed supports: fixed, pinned, roller
    beam.add_support(0.0, SupportType.FIXED)    # Fixed at left
    beam.add_support(15.0, SupportType.PINNED)  # Pinned at center
    beam.add_support(30.0, SupportType.ROLLER)  # Roller at right
    
    print("Support Configuration:")
    for support in beam.supports:
        print(f"  {support.position:.1f} ft: {support.support_type.value}")
    
    point_loads = [PointLoad(position=7.5, magnitude=12.0)]
    
    try:
        results = beam.solve(point_loads=point_loads)
        print("\nSolution completed successfully!")
        print(beam.get_summary())
        return True, results
        
    except Exception as e:
        print(f"\nERROR: {e}")
        return False, None

def run_multispan_tests():
    """Run all multi-span tests."""
    print("MULTI-SPAN BEAM CAPABILITY TESTS")
    print("=" * 60)
    
    # Test 1: Two-span beam
    success1, results1 = test_two_span_beam()
    
    # Test 2: Three-span beam  
    success2, results2 = test_three_span_beam()
    
    # Test 3: Mixed supports
    success3, results3 = test_mixed_supports()
    
    print("\n" + "=" * 60)
    print("MULTI-SPAN TEST SUMMARY")
    print("=" * 60)
    
    tests = [
        ("Two-span beam", success1),
        ("Three-span beam", success2), 
        ("Mixed supports", success3)
    ]
    
    for test_name, success in tests:
        status = "PASSED" if success else "FAILED"
        print(f"{test_name:20s}: {status}")
    
    if all(success for _, success in tests):
        print("\n✅ Current code CAN handle multi-span beams!")
    else:
        print("\n❌ Current code has limitations with multi-span beams.")
        print("\nCommon issues for multi-span beams:")
        print("- Intermediate supports not properly handled")
        print("- Incorrect reaction calculations")
        print("- Boundary condition assembly problems")
        print("- Missing continuity conditions")

# Example of what a multi-span beam should look like:
def show_multispan_requirements():
    """Show what proper multi-span handling requires."""
    print("\n" + "=" * 60)
    print("MULTI-SPAN BEAM REQUIREMENTS")
    print("=" * 60)
    
    requirements = [
        "1. Proper intermediate support handling",
        "2. Continuity of deflection at internal supports",
        "3. Continuity of slope at internal supports (for pinned)",
        "4. Correct reaction force calculations",
        "5. Moment redistribution between spans",
        "6. Proper assembly of global stiffness matrix",
        "7. Support for different support types at intermediate points"
    ]
    
    for req in requirements:
        print(f"  {req}")
    
    print(f"\nTheoretical validation should show:")
    print(f"  - Interior support reactions > exterior reactions for uniform loads")
    print(f"  - Proper moment redistribution")
    print(f"  - Zero deflection at all supports")
    print(f"  - Force equilibrium")

if __name__ == "__main__":
    run_multispan_tests()
    show_multispan_requirements()