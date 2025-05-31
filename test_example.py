from backend.fea import BeamAnalyzer, PointLoad, DistributedLoad, SupportType

# Test 1: Simply supported beam (pinned at both ends) - DEFAULT CONFIGURATION
print("=" * 60)
print("TEST 1: Simply Supported Beam (Default)")
print("=" * 60)
beam1 = BeamAnalyzer(length=20.0, min_elements=15, E=29000.0, I=100.0)
beam1.add_support(0.0, SupportType.PINNED)
beam1.add_support(20.0, SupportType.PINNED)
point_loads = [PointLoad(position=10.0, magnitude=10.0)]  # 10 kips at midspan
results1 = beam1.solve(point_loads=point_loads)
print("\nSimply Supported Beam Results:")
print(beam1.get_summary())

# Analytical comparison for simply supported beam with center point load
P = 10.0  # kips
L = 20.0 * 12.0  # convert to inches
E = 29000.0  # ksi
I = 100.0  # in⁴
theoretical_deflection = -(P * L**3) / (48 * E * I)
print(f"\nAnalytical max deflection: {theoretical_deflection:.4f} in")
print(f"FEA max deflection:        {results1['max_deflection']['value']:.4f} in")
print(f"Error: {abs(theoretical_deflection - results1['max_deflection']['value']):.6f} in")

# Test 2: Fixed beam (fixed at both ends)
print("\n" + "=" * 60)
print("TEST 2: Fixed Beam (Both Ends Fixed)")
print("=" * 60)
beam2 = BeamAnalyzer(length=20.0, min_elements=15, E=29000.0, I=100.0)
beam2.add_support(0.0, SupportType.FIXED)
beam2.add_support(20.0, SupportType.FIXED)
results2 = beam2.solve(point_loads=point_loads)
print("\nFixed Beam Results:")
print(beam2.get_summary())

# Analytical comparison for fixed beam with center point load
theoretical_deflection_fixed = -(P * L**3) / (192 * E * I)  # PL³/(192EI) for fixed-fixed
print(f"\nAnalytical max deflection: {theoretical_deflection_fixed:.4f} in")
print(f"FEA max deflection:        {results2['max_deflection']['value']:.4f} in")
print(f"Error: {abs(theoretical_deflection_fixed - results2['max_deflection']['value']):.6f} in")

# Test 3: Cantilever beam (fixed at one end, free at other)
print("\n" + "=" * 60)
print("TEST 3: Cantilever Beam")
print("=" * 60)
beam3 = BeamAnalyzer(length=20.0, min_elements=15, E=29000.0, I=100.0)
beam3.add_support(0.0, SupportType.FIXED)   # Left end fixed
# Remove the default right support to make it free
beam3.supports = [s for s in beam3.supports if s.position != 20.0]
# Apply load at free end instead of center for better test
cantilever_loads = [PointLoad(position=20.0, magnitude=10.0)]  # 10 kips at free end
results3 = beam3.solve(point_loads=cantilever_loads)
print("Cantilever Beam Results:")
print(beam3.get_summary())

# Analytical comparison for cantilever with end load
theoretical_deflection_cantilever = -(P * L**3) / (3 * E * I)  # PL³/(3EI) for cantilever
print(f"\nAnalytical max deflection: {theoretical_deflection_cantilever:.4f} in")
print(f"FEA max deflection:        {results3['max_deflection']['value']:.4f} in")
print(f"Error: {abs(theoretical_deflection_cantilever - results3['max_deflection']['value']):.6f} in")

# Verify boundary conditions for cantilever
deflection_at_fixed = beam3.get_deflection_at(0.0)
print(f"\nBoundary condition check:")
print(f"Deflection at fixed end: {deflection_at_fixed:.8f} in (should be ~0)")

# Test 4: Propped cantilever (fixed at one end, pinned at other)
print("\n" + "=" * 60)
print("TEST 4: Propped Cantilever")
print("=" * 60)
beam4 = BeamAnalyzer(length=20.0, min_elements=15, E=29000.0, I=100.0)
beam4.add_support(0.0, SupportType.FIXED)   # Left end fixed
beam4.add_support(20.0, SupportType.PINNED) # Right end pinned
results4 = beam4.solve(point_loads=point_loads)
print("Propped Cantilever Results:")
print(beam4.get_summary())

# Test 5: Distributed load test
print("\n" + "=" * 60)
print("TEST 5: Simply Supported Beam with Distributed Load")
print("=" * 60)
beam5 = BeamAnalyzer(length=20.0, min_elements=20, E=29000.0, I=100.0)
# Default pinned-pinned supports
distributed_loads = [DistributedLoad(
    start_pos=0.0, 
    end_pos=20.0, 
    start_magnitude=2.0,  # 2 kips/ft uniform load
    end_magnitude=2.0
)]
results5 = beam5.solve(distributed_loads=distributed_loads)
print("Distributed Load Results:")
print(beam5.get_summary())

# Analytical comparison for uniform distributed load
w = 2.0  # kips/ft
w_in = w / 12.0  # convert to kips/in
theoretical_deflection_uniform = -(5 * w_in * L**4) / (384 * E * I)  # 5wL⁴/(384EI)
print(f"\nAnalytical max deflection: {theoretical_deflection_uniform:.4f} in")
print(f"FEA max deflection:        {results5['max_deflection']['value']:.4f} in")
print(f"Error: {abs(theoretical_deflection_uniform - results5['max_deflection']['value']):.6f} in")

# Test 6: Triangular distributed load
print("\n" + "=" * 60)
print("TEST 6: Simply Supported Beam with Triangular Load")
print("=" * 60)
beam6 = BeamAnalyzer(length=20.0, min_elements=20, E=29000.0, I=100.0)
triangular_loads = [DistributedLoad(
    start_pos=0.0, 
    end_pos=20.0, 
    start_magnitude=0.0,  # Triangular load: 0 to 3 kips/ft
    end_magnitude=3.0
)]
results6 = beam6.solve(distributed_loads=triangular_loads)
print("Triangular Load Results:")
print(beam6.get_summary())

# Verify equilibrium for all tests
print("\n" + "=" * 60)
print("EQUILIBRIUM VERIFICATION")
print("=" * 60)

test_cases = [
    ("Simply Supported", results1, 10.0),
    ("Fixed Beam", results2, 10.0),
    ("Cantilever", results3, 10.0),
    ("Propped Cantilever", results4, 10.0),
    ("Distributed Load", results5, 2.0 * 20.0),  # w * L = total load
    ("Triangular Load", results6, 3.0 * 20.0 / 2.0)  # w_max * L / 2 = total load
]

for name, results, expected_total_load in test_cases:
    total_reactions = sum(reaction['force'] for reaction in results['support_reactions'].values())
    equilibrium_error = abs(total_reactions - expected_total_load)
    print(f"{name:20s}: Reactions = {total_reactions:6.3f} kips, "
          f"Applied = {expected_total_load:6.3f} kips, "
          f"Error = {equilibrium_error:.6f} kips")

print("\n" + "=" * 60)
print("SUPPORT CONSTRAINT VERIFICATION")
print("=" * 60)

# Check that constrained DOFs are indeed zero
test_beams = [
    ("Simply Supported", beam1, results1),
    ("Fixed Beam", beam2, results2),
    ("Cantilever", beam3, results3),
    ("Propped Cantilever", beam4, results4)
]

for name, beam, results in test_beams:
    print(f"\n{name}:")
    for i, node in enumerate(beam.nodes):
        if node.support_type != SupportType.FREE:
            displacement = results['displacements'][2*i]
            rotation = results['displacements'][2*i + 1]
            pos_ft = node.x / 12.0
            
            print(f"  Node at {pos_ft:5.1f} ft ({node.support_type.value:6s}): "
                  f"disp = {displacement:10.8f} in, rot = {rotation:10.8f} rad")
            
            # Check constraints
            if node.support_type in [SupportType.PINNED, SupportType.ROLLER, SupportType.FIXED]:
                if abs(displacement) > 1e-10:
                    print(f"    WARNING: Displacement constraint violated!")
            
            if node.support_type == SupportType.FIXED:
                if abs(rotation) > 1e-10:
                    print(f"    WARNING: Rotation constraint violated!")