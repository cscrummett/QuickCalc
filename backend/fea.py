"""
Enhanced Finite Element Analysis module for beam analysis with distributed loads.
"""
import numpy as np
from dataclasses import dataclass
from typing import List, Tuple, Optional, Set
from enum import Enum

class SupportType(Enum):
    """Types of support conditions."""
    FREE = "free"
    PINNED = "pinned"
    FIXED = "fixed"
    ROLLER = "roller"

@dataclass
class Node:
    """Represents a node in the finite element mesh."""
    x: float  # Position along beam (in)
    support_type: SupportType = SupportType.FREE

@dataclass
class Element:
    """Represents a beam element."""
    node1_idx: int  # Index of first node
    node2_idx: int  # Index of second node
    length: float  # Element length (in)
    E: float  # Young's modulus (ksi)
    I: float  # Moment of inertia (in^4)

@dataclass
class PointLoad:
    """Represents a point load on the beam."""
    position: float  # Position along beam (ft)
    magnitude: float  # Force magnitude (kips, positive downward)

@dataclass
class DistributedLoad:
    """Represents a distributed load on the beam."""
    start_pos: float  # Start position (ft)
    end_pos: float    # End position (ft)
    start_magnitude: float  # Load at start (kips/ft, positive downward)
    end_magnitude: float    # Load at end (kips/ft, positive downward)
    
    def __post_init__(self):
        if self.start_pos >= self.end_pos:
            raise ValueError("Start position must be less than end position")
    
    @property
    def is_uniform(self) -> bool:
        """Check if load is uniform."""
        return abs(self.start_magnitude - self.end_magnitude) < 1e-6
    
    @property
    def is_triangular(self) -> bool:
        """Check if load is triangular."""
        return (abs(self.start_magnitude) < 1e-6) or (abs(self.end_magnitude) < 1e-6)
    
    def magnitude_at(self, x: float) -> float:
        """Get load magnitude at position x (in feet)."""
        if x < self.start_pos or x > self.end_pos:
            return 0.0
        
        # Linear interpolation
        ratio = (x - self.start_pos) / (self.end_pos - self.start_pos)
        return self.start_magnitude + ratio * (self.end_magnitude - self.start_magnitude)

@dataclass
class Support:
    """Represents a support condition."""
    position: float  # Position along beam (ft)
    support_type: SupportType

class BeamAnalyzer:
    """Enhanced beam analyzer with distributed load support and adaptive meshing."""
    
    def __init__(self, length: float, min_elements: int = 10, E: float = 29000.0, I: float = 100.0):
        """
        Initialize beam analyzer.
        
        Args:
            length: Beam length (ft)
            min_elements: Minimum number of elements (actual number may be higher for adaptive mesh)
            E: Young's modulus (ksi)
            I: Moment of inertia (in^4)
        """
        self.length = length * 12.0  # Convert ft to in
        self.min_elements = min_elements
        self.E = E
        self.I = I
        self.point_loads = []
        self.distributed_loads = []
        self.supports = []
        self.nodes = []
        self.elements = []
        self.results = None
        
        # Set default supports (simply supported)
        self.supports = [
            Support(position=0.0, support_type=SupportType.PINNED),
            Support(position=length, support_type=SupportType.PINNED)
        ]
    
    def add_support(self, position: float, support_type: SupportType):
        """Add or modify support at specified position."""
        # Remove existing support at this position
        self.supports = [s for s in self.supports if abs(s.position - position) > 1e-6]
        # Add new support
        self.supports.append(Support(position=position, support_type=support_type))
    
    def _generate_node_positions(self) -> List[float]:
        """Generate adaptive node positions based on loads and supports."""
        # Start with required positions
        required_positions: Set[float] = set()
        
        # Add beam ends
        required_positions.add(0.0)
        required_positions.add(self.length)
        
        # Add point load positions (convert to inches)
        for load in self.point_loads:
            required_positions.add(load.position * 12.0)
        
        # Add support positions (convert to inches)
        for support in self.supports:
            required_positions.add(support.position * 12.0)
        
        # Add distributed load boundaries (convert to inches)
        for dist_load in self.distributed_loads:
            required_positions.add(dist_load.start_pos * 12.0)
            required_positions.add(dist_load.end_pos * 12.0)
        
        # Convert to sorted list
        required_positions = sorted(required_positions)
        
        # Generate additional nodes to meet minimum element count
        # Calculate current number of elements with required positions only
        current_elements = len(required_positions) - 1
        
        if current_elements < self.min_elements:
            # Need to add more nodes
            additional_nodes_needed = self.min_elements - current_elements
            
            # Find the largest gaps and subdivide them
            gaps = []
            for i in range(len(required_positions) - 1):
                gap_size = required_positions[i+1] - required_positions[i]
                gaps.append((gap_size, i))
            
            # Sort gaps by size (largest first)
            gaps.sort(reverse=True)
            
            # Add nodes in largest gaps
            nodes_added = 0
            gap_index = 0
            
            while nodes_added < additional_nodes_needed and gap_index < len(gaps):
                gap_size, pos_index = gaps[gap_index]
                
                if gap_size > 1e-6:  # Only subdivide meaningful gaps
                    # Add node at midpoint
                    start_pos = required_positions[pos_index]
                    end_pos = required_positions[pos_index + 1]
                    mid_pos = (start_pos + end_pos) / 2.0
                    
                    # Insert at correct position
                    required_positions.insert(pos_index + 1, mid_pos)
                    nodes_added += 1
                    
                    # Update gap indices for remaining gaps
                    for j in range(gap_index + 1, len(gaps)):
                        if gaps[j][1] > pos_index:
                            gaps[j] = (gaps[j][0], gaps[j][1] + 1)
                
                gap_index += 1
        
        return sorted(required_positions)
    
    def _create_adaptive_mesh(self):
        """Create adaptive mesh with nodes at critical locations."""
        node_positions = self._generate_node_positions()
        
        # Create nodes
        self.nodes = []
        for x in node_positions:
            node = Node(x=x)
            
            # Set support conditions
            x_ft = x / 12.0  # Convert to feet for comparison
            for support in self.supports:
                if abs(support.position - x_ft) < 1e-6:
                    node.support_type = support.support_type
                    break
            
            self.nodes.append(node)
        
        # Create elements
        self.elements = []
        for i in range(len(self.nodes) - 1):
            element = Element(
                node1_idx=i,
                node2_idx=i + 1,
                length=self.nodes[i + 1].x - self.nodes[i].x,
                E=self.E,
                I=self.I
            )
            self.elements.append(element)
    
    def element_stiffness_matrix(self, element: Element) -> np.ndarray:
        """Calculate element stiffness matrix for Euler-Bernoulli beam."""
        L = element.length
        EI = element.E * element.I
        
        # 4x4 stiffness matrix for beam element
        # DOFs: [v1, θ1, v2, θ2] (displacement, rotation at each node)
        k = np.array([
            [12,     6*L,    -12,     6*L],
            [6*L,  4*L**2,   -6*L,  2*L**2],
            [-12,    -6*L,     12,    -6*L],
            [6*L,  2*L**2,   -6*L,  4*L**2]
        ]) * EI / (L**3)
        
        return k
    
    def _convert_distributed_to_nodal(self, dist_load: DistributedLoad, element: Element) -> np.ndarray:
        """Convert distributed load to equivalent nodal loads for an element."""
        # Convert positions to inches
        start_pos = dist_load.start_pos * 12.0
        end_pos = dist_load.end_pos * 12.0
        element_start = self.nodes[element.node1_idx].x
        element_end = self.nodes[element.node2_idx].x
        
        # Check if load overlaps with element
        if end_pos <= element_start or start_pos >= element_end:
            return np.zeros(4)  # No load on this element
        
        # Find overlap region
        overlap_start = max(start_pos, element_start)
        overlap_end = min(end_pos, element_end)
        
        if overlap_start >= overlap_end:
            return np.zeros(4)
        
        # Get load magnitudes at overlap boundaries
        w1 = dist_load.magnitude_at(overlap_start / 12.0)  # Convert back to ft
        w2 = dist_load.magnitude_at(overlap_end / 12.0)
        
        # Length of loaded portion
        loaded_length = overlap_end - overlap_start
        
        # Calculate equivalent nodal loads using standard formulas
        if abs(w1 - w2) < 1e-6:  # Uniform load
            total_load = w1 * loaded_length / 12.0  # Convert to kips
            f1 = f2 = total_load / 2.0
            # Moments for uniform load
            m1 = -total_load * loaded_length / (12.0 * 12.0)  # wL²/12
            m2 = total_load * loaded_length / (12.0 * 12.0)   # wL²/12
        else:  # Trapezoidal load
            loaded_length_ft = loaded_length / 12.0
            # Standard trapezoidal load formulas
            f1 = loaded_length_ft * (2*w1 + w2) / 6.0
            f2 = loaded_length_ft * (w1 + 2*w2) / 6.0
            # Simplified moments (could be more precise)
            total_load = (w1 + w2) * loaded_length_ft / 2.0
            m1 = -total_load * loaded_length / (12.0 * 12.0)
            m2 = total_load * loaded_length / (12.0 * 12.0)
        
        return np.array([f1, m1, f2, m2])
    
    def _apply_boundary_conditions(self, K: np.ndarray, F: np.ndarray) -> Tuple[np.ndarray, np.ndarray, List[int]]:
        """Apply boundary conditions using elimination method."""
        constrained_dofs = []
        
        for i, node in enumerate(self.nodes):
            if node.support_type == SupportType.PINNED:
                constrained_dofs.append(2*i)  # Constrain displacement only
            elif node.support_type == SupportType.FIXED:
                constrained_dofs.extend([2*i, 2*i+1])  # Constrain displacement and rotation
            elif node.support_type == SupportType.ROLLER:
                constrained_dofs.append(2*i)  # Constrain displacement only
        
        # Create reduced system by eliminating constrained DOFs
        n_dof = K.shape[0]
        free_dofs = [i for i in range(n_dof) if i not in constrained_dofs]
        
        K_reduced = K[np.ix_(free_dofs, free_dofs)]
        F_reduced = F[free_dofs]
        
        return K_reduced, F_reduced, free_dofs
    
    def solve(self, point_loads: List[PointLoad] = None, distributed_loads: List[DistributedLoad] = None) -> dict:
        """
        Solve the beam problem with point and distributed loads.
        
        Returns:
            Dictionary containing:
            - displacements: nodal displacements (in) and rotations (rad)
            - reactions: support reactions (kips)
            - node_positions: x-coordinates of nodes (ft)
            - max_deflection: maximum deflection and its location
            - nodes: list of Node objects
            - elements: list of Element objects
        """
        self.point_loads = point_loads or []
        self.distributed_loads = distributed_loads or []
        
        # Create adaptive mesh
        self._create_adaptive_mesh()
        
        n_nodes = len(self.nodes)
        n_dof = 2 * n_nodes
        
        # Initialize global matrices
        K = np.zeros((n_dof, n_dof))
        F = np.zeros(n_dof)
        
        # Assemble global stiffness matrix
        for element in self.elements:
            k = self.element_stiffness_matrix(element)
            
            # Global DOF indices
            dofs = [
                2 * element.node1_idx,      # v1
                2 * element.node1_idx + 1,  # θ1
                2 * element.node2_idx,      # v2
                2 * element.node2_idx + 1   # θ2
            ]
            
            # Add to global stiffness matrix
            for i in range(4):
                for j in range(4):
                    K[dofs[i], dofs[j]] += k[i, j]
        
        # Apply point loads - since we have nodes at exact load positions, this is precise
        for load in self.point_loads:
            x_load = load.position * 12.0  # Convert to inches
            # Find exact node at this position
            for i, node in enumerate(self.nodes):
                if abs(node.x - x_load) < 1e-6:  # Node at exact load position
                    F[2*i] += load.magnitude  # Apply to displacement DOF
                    break
            else:
                raise ValueError(f"No node found at point load position {load.position} ft. This shouldn't happen with adaptive mesh.")
        
        # Apply distributed loads
        for dist_load in self.distributed_loads:
            for element in self.elements:
                nodal_forces = self._convert_distributed_to_nodal(dist_load, element)
                if np.any(np.abs(nodal_forces) > 1e-6):
                    dofs = [
                        2 * element.node1_idx,
                        2 * element.node1_idx + 1,
                        2 * element.node2_idx,
                        2 * element.node2_idx + 1
                    ]
                    for i, dof in enumerate(dofs):
                        F[dof] += nodal_forces[i]
        
        # Apply boundary conditions
        K_reduced, F_reduced, free_dofs = self._apply_boundary_conditions(K, F)
        
        # Solve reduced system
        U_reduced = np.linalg.solve(K_reduced, F_reduced)
        
        # Reconstruct full displacement vector
        U = np.zeros(n_dof)
        for i, dof in enumerate(free_dofs):
            U[dof] = U_reduced[i]
        
        # Calculate reactions
        reactions = K @ U - F
        
        # Find support reactions only
        support_reactions = {}
        for i, node in enumerate(self.nodes):
            if node.support_type != SupportType.FREE:
                reaction_force = reactions[2*i]  # Vertical reaction
                reaction_moment = reactions[2*i+1] if node.support_type == SupportType.FIXED else 0.0
                support_reactions[node.x/12.0] = {
                    'force': reaction_force,
                    'moment': reaction_moment,
                    'type': node.support_type.value
                }
        
        # Find maximum deflection
        displacements = U[::2]  # Extract displacement DOFs
        max_disp_idx = np.argmax(np.abs(displacements))
        max_deflection = {
            'value': displacements[max_disp_idx],
            'location_ft': self.nodes[max_disp_idx].x / 12.0,
            'location_in': self.nodes[max_disp_idx].x
        }
        
        # Node positions for reference
        node_positions = [node.x / 12.0 for node in self.nodes]
        
        self.results = {
            'displacements': U,
            'support_reactions': support_reactions,
            'node_positions': node_positions,
            'max_deflection': max_deflection,
            'nodes': self.nodes,
            'elements': self.elements,
            'num_elements': len(self.elements)
        }
        
        return self.results
    
    def get_deflection_at(self, position: float) -> float:
        """Get deflection at specified position (in feet) using interpolation if needed."""
        if self.results is None:
            raise ValueError("No results available. Run solve() first.")
        
        position_in = position * 12.0  # Convert to inches
        
        # Check if we have a node at exact position
        for i, node in enumerate(self.nodes):
            if abs(node.x - position_in) < 1e-6:
                return self.results['displacements'][2*i]
        
        # Interpolate between nodes
        for i in range(len(self.nodes) - 1):
            x1 = self.nodes[i].x
            x2 = self.nodes[i+1].x
            if x1 <= position_in <= x2:
                # Linear interpolation
                ratio = (position_in - x1) / (x2 - x1)
                disp1 = self.results['displacements'][2*i]
                disp2 = self.results['displacements'][2*(i+1)]
                return disp1 + ratio * (disp2 - disp1)
        
        raise ValueError(f"Position {position} ft is outside beam length")
    
    def get_summary(self) -> str:
        """Get a summary of analysis results."""
        if self.results is None:
            return "No results available. Run solve() first."
        
        max_disp = self.results['max_deflection']
        
        summary = f"""
Beam Analysis Summary
====================
Beam Length: {self.length/12.0:.1f} ft
Number of Elements: {self.results['num_elements']}
Number of Nodes: {len(self.nodes)}

Maximum Deflection: {max_disp['value']:.4f} in at {max_disp['location_ft']:.2f} ft

Support Reactions:"""
        
        for pos, reaction in self.results['support_reactions'].items():
            summary += f"\n  At {pos:.1f} ft ({reaction['type']}): {reaction['force']:.2f} kips"
            if abs(reaction['moment']) > 1e-6:
                summary += f", {reaction['moment']:.2f} kip-ft"
        
        summary += f"\n\nLoading:"
        summary += f"\n  Point Loads: {len(self.point_loads)}"
        summary += f"\n  Distributed Loads: {len(self.distributed_loads)}"
        
        return summary

