"""
Test visualization of beam analysis results.
"""
import numpy as np
import matplotlib.pyplot as plt
from backend.fea import BeamAnalyzer, PointLoad

def plot_beam_results(beam: BeamAnalyzer, loads: list[PointLoad], title: str):
    """Plot beam, shear, and moment diagrams."""
    # Solve beam
    U, V, M = beam.solve(loads)
    
    # Create x-coordinates (in feet)
    x = np.array([node.x / 12.0 for node in beam.nodes])  # Convert to feet
    
    # Create figure with 3 subplots
    fig, (ax1, ax2, ax3) = plt.subplots(3, 1, figsize=(12, 14))
    fig.suptitle(title, fontsize=14, y=0.95)
    
    # Plot 1: Beam diagram
    ax1.set_title('Beam Diagram')
    # Plot beam line
    ax1.plot([0, beam.length/12], [0, 0], 'k-', linewidth=2)
    # Plot supports (triangles)
    ax1.plot([0], [0], 'k^', markersize=10, label='Support')
    ax1.plot([beam.length/12], [0], 'k^', markersize=10)
    # Plot loads (arrows)
    for load in loads:
        ax1.arrow(load.position, 1, 0, -0.8, 
                 head_width=0.5, head_length=0.2, fc='r', ec='r')
        ax1.text(load.position, 1.2, f'{load.magnitude} kips ↓', 
                horizontalalignment='center', color='red', fontweight='bold')
    ax1.grid(True, linestyle='--', alpha=0.7)
    ax1.set_ylabel('Position')
    ax1.set_xlim(-2, beam.length/12 + 2)
    ax1.set_ylim(-2, 2)
    
    # Plot 2: Shear diagram
    ax2.set_title('Shear Diagram')
    
    # Create proper shear diagram
    x_shear = []  # x coordinates for shear diagram
    v_shear = []  # shear values
    
    # Start with left reaction
    x_shear.append(0)
    v_shear.append(V[0])  # Left reaction
    
    # Add points just before and after each load
    sorted_loads = sorted(loads, key=lambda l: l.position)
    current_shear = V[0]  # Start with left reaction
    
    for load in sorted_loads:
        # Point just before load
        x_shear.append(load.position - 0.001)
        v_shear.append(current_shear)
        
        # Point just after load
        x_shear.append(load.position + 0.001)
        current_shear -= load.magnitude  # Reduce shear by load amount
        v_shear.append(current_shear)
    
    # End with right reaction (maintain the last shear value)
    x_shear.append(beam.length/12)
    v_shear.append(current_shear)
    
    # Convert to numpy arrays
    x_shear = np.array(x_shear)
    v_shear = np.array(v_shear)
    
    # Plot shear diagram
    ax2.plot(x_shear, v_shear, 'b-', linewidth=2)
    ax2.fill_between(x_shear, v_shear, 0, alpha=0.1, color='blue')
    
    # Add max/min shear annotations
    max_shear = np.max(v_shear)
    min_shear = np.min(v_shear)
    ax2.annotate(f'Max: {max_shear:.1f} kips', 
                xy=(x_shear[np.argmax(v_shear)], max_shear),
                xytext=(5, 10), textcoords='offset points',
                ha='left', va='bottom')
    ax2.annotate(f'Min: {min_shear:.1f} kips',
                xy=(x_shear[np.argmin(v_shear)], min_shear),
                xytext=(5, -10), textcoords='offset points',
                ha='left', va='top')
    ax2.grid(True, linestyle='--', alpha=0.7)
    ax2.set_ylabel('Shear (kips)')
    
    # Plot 3: Moment diagram
    ax3.set_title('Moment Diagram')
    ax3.plot(x, M, 'r-', linewidth=2)
    ax3.fill_between(x, M, 0, alpha=0.1, color='red')
    # Add max moment annotation
    max_moment = np.max(np.abs(M))
    max_moment_x = x[np.argmax(np.abs(M))]
    ax3.annotate(f'Max: {max_moment:.1f} kip-ft',
                xy=(max_moment_x, M[np.argmax(np.abs(M))]),
                xytext=(5, 10), textcoords='offset points',
                ha='left', va='bottom')
    ax3.grid(True, linestyle='--', alpha=0.7)
    ax3.set_ylabel('Moment (kip-ft)')
    ax3.set_xlabel('Position along beam (ft)')
    
    # Adjust layout and display
    plt.tight_layout()
    plt.show()

def test_plot_midspan_load():
    """Test plotting beam with midspan load."""
    # Create a 20-ft beam with 40 elements for smoother plots
    beam = BeamAnalyzer(
        length=20.0,  # ft
        num_elements=40,  # increased for smoother plots
        E=29000.0,  # ksi
        I=100.0  # in^4
    )
    
    # Apply 10-kip point load at midspan
    loads = [PointLoad(position=10.0, magnitude=10.0)]
    
    # Plot results
    plot_beam_results(beam, loads, 'Simply Supported Beam - 10k Midspan Load')

def test_plot_quarter_point_load():
    """Test plotting beam with quarter-point load."""
    # Create a 20-ft beam with 40 elements for smoother plots
    beam = BeamAnalyzer(
        length=20.0,
        num_elements=40,  # increased for smoother plots
        E=29000.0,
        I=100.0
    )
    
    # Apply 10-kip point load at L/4
    loads = [PointLoad(position=5.0, magnitude=10.0)]
    
    # Plot results
    plot_beam_results(beam, loads, 'Simply Supported Beam - 10k Quarter-Point Load')

def test_plot_two_point_loads():
    """Test plotting beam with two point loads."""
    # Create a 20-ft beam with 40 elements for smoother plots
    beam = BeamAnalyzer(
        length=20.0,
        num_elements=40,  # increased for smoother plots
        E=29000.0,
        I=100.0
    )
    
    # Apply 10-kip load at L/3 and 5-kip load at 2L/3
    loads = [
        PointLoad(position=20/3, magnitude=10.0),  # 6.67 ft from left
        PointLoad(position=40/3, magnitude=5.0)    # 13.33 ft from left
    ]
    
    # Plot results
    plot_beam_results(beam, loads, 'Simply Supported Beam - Two Point Loads (10k at L/3, 5k at 2L/3)')

if __name__ == '__main__':
    test_plot_midspan_load()
    test_plot_quarter_point_load()
    test_plot_two_point_loads() 