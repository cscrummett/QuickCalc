"""
Test cases for the FEA module.
"""
import pytest
import numpy as np
from backend.fea import BeamAnalyzer, PointLoad

def test_simply_supported_beam_midspan_load():
    """Test a simply supported beam with a point load at midspan."""
    # Create a 20-ft beam with 10 elements
    beam = BeamAnalyzer(
        length=20.0,  # ft
        num_elements=10,
        E=29000.0,  # ksi
        I=100.0  # in^4
    )
    
    # Apply 10-kip point load at midspan
    loads = [PointLoad(position=10.0, magnitude=10.0)]
    
    # Solve
    U, V, M = beam.solve(loads)
    
    # Expected results from manual calculations
    # Max moment = PL/4 = 10 * 20 / 4 = 50 kip-ft
    # Max deflection = PL^3/(48EI) = 10 * (240)^3 / (48 * 29000 * 100) = 0.993 in = 0.0828 ft
    
    # Test reactions (should be P/2 = 5 kips each, upward)
    assert np.abs((V[0] - 5.0) / 5.0) < 1e-10  # Relative difference
    assert np.abs((V[-1] - 5.0) / 5.0) < 1e-10  # Relative difference
    
    # Test maximum moment magnitude at midspan
    max_moment = np.max(np.abs(M))  # Get maximum absolute moment
    assert np.abs((max_moment - 50.0) / 50.0) < 1e-10  # Relative difference
    
    # Test deflection magnitude
    deflections = U[::2]  # Even indices are displacements
    max_deflection = np.max(np.abs(deflections))
    expected_deflection = 10 * (240**3) / (48 * 29000 * 100) / 12  # Convert in to ft
    assert np.abs((max_deflection - expected_deflection) / expected_deflection) < 0.01  # 1% tolerance

def test_simply_supported_beam_quarter_point_load():
    """Test a simply supported beam with a point load at quarter point."""
    # Create a 20-ft beam with 10 elements
    beam = BeamAnalyzer(
        length=20.0,
        num_elements=10,
        E=29000.0,
        I=100.0
    )
    
    # Apply 10-kip point load at L/4
    loads = [PointLoad(position=5.0, magnitude=10.0)]
    
    # Solve
    U, V, M = beam.solve(loads)
    
    # Expected results from manual calculations
    # Left reaction = 3P/4 = 7.5 kips
    # Right reaction = P/4 = 2.5 kips
    # Max moment = 3PL/16 = 37.5 kip-ft
    
    # Test reactions
    assert np.isclose(V[0], 7.5)  # Left reaction
    assert np.isclose(V[-1], 2.5)  # Right reaction
    
    # Test moment at load point
    # Find the node at the load position
    load_node_idx = None
    for i, node in enumerate(beam.nodes):
        if np.isclose(node.x / 12.0, 5.0):  # Convert from inches to feet
            load_node_idx = i
            break
    assert load_node_idx is not None, "No node found at load position"
    
    # Test moment at load point
    assert np.isclose(M[load_node_idx], 37.5)  # Moment at load point
    
    # Test maximum moment magnitude
    max_moment = np.max(np.abs(M))  # Get maximum absolute moment
    assert np.isclose(max_moment, 37.5)  # Maximum moment

def test_simply_supported_beam_quarter_point_load_exact_results():
    """Test a simply supported beam with a point load at quarter point."""
    # Create a 20-ft beam with 20 elements for better accuracy
    beam = BeamAnalyzer(
        length=20.0,
        num_elements=20,
        E=29000.0,
        I=100.0
    )
    
    # Apply 10-kip point load at L/4
    loads = [PointLoad(position=5.0, magnitude=10.0)]
    
    # Solve
    U, V, M = beam.solve(loads)
    
    # Expected results from manual calculations
    # Left reaction = 3P/4 = 7.5 kips
    # Right reaction = P/4 = 2.5 kips
    # Max moment = 3PL/16 = 37.5 kip-ft
    
    # Test reactions with 1% tolerance due to numerical approximations
    assert np.abs((V[0] - 7.5) / 7.5) < 0.01  # Relative difference
    assert np.abs((V[-1] - 2.5) / 2.5) < 0.01  # Relative difference
    
    # Test maximum moment magnitude with 1% tolerance
    max_moment = np.max(np.abs(M))  # Get maximum absolute moment
    assert np.abs((max_moment - 37.5) / 37.5) < 0.01  # Relative difference

def test_simply_supported_beam_distributed_load():
    """Test a simply supported beam with a uniform distributed load."""
    # Create a 20-ft beam with 10 elements
    beam = BeamAnalyzer(
        length=20.0,  # ft
        num_elements=10,
        E=29000.0,  # ksi
        I=100.0  # in^4
    )
    
    # Apply 1 kip/ft uniform distributed load over entire length
    # Convert to equivalent point loads at element midpoints
    w = 1.0  # kip/ft
    loads = []
    for i in range(beam.num_elements):
        pos = (i + 0.5) * beam.length / beam.num_elements  # Midpoint of each element
        mag = w * beam.length / beam.num_elements  # Load magnitude for each element
        loads.append(PointLoad(position=pos, magnitude=mag))
    
    # Solve
    U, V, M = beam.solve(loads)
    
    # Expected results from manual calculations
    # Reactions = wL/2 = 1 * 20 / 2 = 10 kips each
    # Max moment = wL^2/8 = 1 * (20)^2 / 8 = 50 kip-ft
    # Max deflection = 5wL^4/(384EI) = 5 * 1 * (240)^4 / (384 * 29000 * 100) = 1.241 in = 0.1034 ft
    
    # Test reactions
    assert np.abs((V[0] - 10.0) / 10.0) < 1e-3  # Relative difference
    assert np.abs((V[-1] - 10.0) / 10.0) < 1e-3  # Relative difference
    
    # Test maximum moment (should occur at midspan)
    max_moment_idx = np.argmax(np.abs(M))
    assert np.abs((M[max_moment_idx] - 50.0) / 50.0) < 1e-3  # Relative difference
    
    # Test maximum deflection (should occur at midspan)
    max_deflection_idx = np.argmax(np.abs(U))
    assert np.abs((U[max_deflection_idx] - 0.1034) / 0.1034) < 1e-3  # Relative difference

if __name__ == '__main__':
    pytest.main([__file__])