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

if __name__ == '__main__':
    pytest.main([__file__]) 