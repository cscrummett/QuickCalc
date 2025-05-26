"""
Finite Element Analysis module for beam analysis.
"""
import numpy as np
from dataclasses import dataclass
from typing import List, Tuple

@dataclass
class Node:
    """Represents a node in the finite element mesh."""
    x: float  # Position along beam (in)
    fixed: bool = False  # Whether the node is fixed (support)
    pinned: bool = False  # Whether the node is pinned (support)

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

class BeamAnalyzer:
    """Analyzes a single-span beam using FEA."""
    
    def __init__(self, length: float, num_elements: int, E: float = 29000.0, I: float = 100.0):
        """
        Initialize beam analyzer.
        
        Args:
            length: Beam length (ft)
            num_elements: Number of elements to use
            E: Young's modulus (ksi)
            I: Moment of inertia (in^4)
        """
        self.length = length * 12.0  # Convert ft to in
        self.num_elements = num_elements
        self.E = E  # Keep in ksi (kip/in^2)
        self.I = I  # Keep in^4
        self.element_length = self.length / num_elements
        self.loads = []
        self.nodes = []
        self.elements = []
    
    def _create_mesh(self):
        """Create uniform mesh with equal element lengths."""
        # Create nodes at uniform spacing
        self.nodes = []
        for i in range(self.num_elements + 1):
            x = i * self.element_length
            node = Node(x=x)
            # Add supports
            if i == 0:  # Left end
                node.pinned = True
            elif i == self.num_elements:  # Right end
                node.pinned = True
            self.nodes.append(node)
        
        # Create elements
        self.elements = []
        for i in range(self.num_elements):
            element = Element(
                node1_idx=i,
                node2_idx=i + 1,
                length=self.element_length,
                E=self.E,
                I=self.I
            )
            self.elements.append(element)
    
    def element_stiffness_matrix(self, element: Element) -> np.ndarray:
        """Calculate element stiffness matrix."""
        L = element.length
        EI = element.E * element.I
        
        # Basic beam element stiffness matrix
        k = np.array([
            [12,     6*L,    -12,     6*L],
            [6*L,  4*L**2,   -6*L,  2*L**2],
            [-12,    -6*L,     12,    -6*L],
            [6*L,  2*L**2,   -6*L,  4*L**2]
        ]) * EI / (L**3)
        
        return k
    
    def _distribute_point_load(self, load: PointLoad) -> np.ndarray:
        """Distribute a point load to the nearest node."""
        F = np.zeros((2 * len(self.nodes), 1))  # Initialize force vector as column vector
        
        # Convert load position to inches
        x_load = load.position * 12.0
        
        # Find nearest node
        distances = [abs(node.x - x_load) for node in self.nodes]
        nearest_node = distances.index(min(distances))
        
        # Apply load to vertical DOF of nearest node
        F[2*nearest_node, 0] = load.magnitude
        
        return F
    
    def solve(self, loads: List[PointLoad]) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
        """
        Solve the beam problem.
        
        Returns:
            Tuple of:
            - Nodal displacements (ft) and rotations (rad)
            - Shear forces at nodes (kips, positive upward)
            - Bending moments at nodes (kip-ft, positive causes tension in bottom fiber)
        """
        self.loads = loads
        self._create_mesh()
        
        # Get number of nodes and DOFs
        n_nodes = len(self.nodes)
        n_dof = 2 * n_nodes
        
        # Initialize global matrices
        K = np.zeros((n_dof, n_dof))
        F = np.zeros((n_dof, 1))  # Make F a column vector
        
        # Assemble global stiffness matrix
        for element in self.elements:
            k = self.element_stiffness_matrix(element)
            
            # Get global DOF indices
            i1 = 2 * element.node1_idx  # First node displacement
            i2 = 2 * element.node1_idx + 1  # First node rotation
            i3 = 2 * element.node2_idx  # Second node displacement
            i4 = 2 * element.node2_idx + 1  # Second node rotation
            dofs = [i1, i2, i3, i4]
            
            # Add to global stiffness matrix
            for i in range(4):
                for j in range(4):
                    K[dofs[i], dofs[j]] += k[i, j]
        
        # Apply loads
        for load in self.loads:
            F += self._distribute_point_load(load)
        
        # Apply boundary conditions using penalty method
        large_value = 1e12
        for i, node in enumerate(self.nodes):
            if node.pinned:
                K[2*i, 2*i] = large_value  # Fix displacement
                F[2*i, 0] = 0  # Zero out force at constrained DOF
        
        # Solve system
        U = np.linalg.solve(K, F)
        
        # Calculate reactions and internal forces
        V = np.zeros(n_nodes)  # Shear forces
        M = np.zeros(n_nodes)  # Bending moments
        
        # Calculate internal forces at nodes
        for i, element in enumerate(self.elements):
            k = self.element_stiffness_matrix(element)
            dofs = [
                2*element.node1_idx,
                2*element.node1_idx + 1,
                2*element.node2_idx,
                2*element.node2_idx + 1
            ]
            u_element = U[dofs]
            f_element = k @ u_element
            
            # Add element end forces to nodes with correct sign convention
            # Positive shear is upward, positive moment causes tension in bottom fiber
            V[element.node1_idx] -= f_element[0, 0]  # Left node shear (flip sign)
            V[element.node2_idx] -= f_element[2, 0]  # Right node shear (flip sign)
            
            # For moments, we need to consider the contribution from both shear and moment
            # Left node moment
            M[element.node1_idx] = max(abs(M[element.node1_idx]), abs(f_element[1, 0]))
            # Right node moment
            M[element.node2_idx] = max(abs(M[element.node2_idx]), abs(f_element[3, 0]))
        
        # Convert displacements to feet
        U[::2] /= 12.0
        
        # Convert moments to kip-ft
        M /= 12.0
        
        return U, V, M 