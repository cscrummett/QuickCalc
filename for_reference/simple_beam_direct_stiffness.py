import numpy as np

'''Key Steps for a Pinned Beam Analysis:
Beam Element Stiffness Matrix: 
    Develop the local stiffness matrix for each beam element.
Global Stiffness Matrix: 
    Assemble the global stiffness matrix for the entire beam.
Apply Loads: 
    Define external loads applied to the beam (e.g., point loads, distributed loads).
Apply Boundary Conditions: 
    Apply boundary conditions for pinned supports, which restrict vertical displacement but allow rotation.
    For wood design, we will stick with only pinned connections.
Solve for Displacements: 
    Solve the system of equations for vertical displacements and rotations at each node.
Post-Processing: 
    Use the displacements to calculate internal forces (reactions, bending moments, and shear forces).
'''

def subdivide_beam(lengths, num_subdivisions):
    #want to subdivide to give us close to a FEA where we create a mesh which is accurate.
    #consider that it should subdivide intelligently, ie. no need to always add the same number of sub_nodes between main_nodes
    new_lengths = []
    connectivity = []
    node_index = 0
    
    for n_length in lengths: #iterates through each main_length to create sub_length
        subdiv_length = n_length / num_subdivisions
        for i in range(num_subdivisions):
            new_lengths.append(subdiv_length)
            connectivity.append((node_index, node_index + 1)) 
            node_index += 1
                       
    return new_lengths, connectivity


def subdivide_forces(F, lengths, num_subdivisions):
    new_F = []
    counter = 0
    for n_length in range(len(lengths)):
        for i in range(num_subdivisions):
            #This should place forces at main_nodes, and leave sub_nodes at 0
            if i == 0:
                new_F.append(F[counter][0])
                new_F.append(F[counter+1][0])
                counter+=2
            else:
                new_F.append(0)
                new_F.append(0)
    #add final node forces to end (num_nodes = 1+members)
    new_F.append(F[-2][0])
    new_F.append(F[-1][0])
    #turn list into array, and make vertical
    new_F = np.array(new_F)
    new_F = new_F.reshape(-1,1)   
    
    return new_F


# (Ke matrix)
def beam_element_stiffness(E, I, L): #All inputs in inches
    # Stiffness matrix for a beam element in local coordinates
    stiffness_matrix = (E * I / L**3) * np.array([
        [12, 6*L, -12, 6*L],
        [6*L, 4*L**2, -6*L, 2*L**2],
        [-12, -6*L, 12, -6*L],
        [6*L, 2*L**2, -6*L, 4*L**2]
    ])
    return stiffness_matrix


# (K matrix)
def assemble_global_stiffness(num_dof, element_stiffness, connectivity):
    # Initialize the global stiffness matrix based on the total number of degrees of freedom
    K_global = np.zeros((num_dof, num_dof))
    
    # Loop over all elements and assemble them into the global stiffness matrix
    for i, elem in enumerate(connectivity): 
        elem_stifness = element_stiffness[i] # retrieves the local stiffness matrix for the current element
        node_i, node_j = elem # elem contains the two node indices and these are unpacked into node_i and node_j
                
        # Map element stiffness matrix into global stiffness matrix
        # this creates a list of global DOF for the current element. Even ones are vert, odd ones are rotation
        global_dof = np.array([2*node_i, 2*node_i+1, 2*node_j, 2*node_j+1])
        
        # Assemble element stiffness into the global matrix
        for m_row in range(4): # only 4 DOF per element
            for n_col in range(4): # loops us over a 4x4 stiffness matrix
                # adds the contribution of this elements stiffness matrix to the global stiffness matrix
                K_global[global_dof[m_row], global_dof[n_col]] += elem_stifness[m_row, n_col] 
                
    return K_global


def apply_point_loads(num_nodes, loads):
    """Generates the load vector"""
    F = np.zeros((2 * num_nodes,1)) # 2 DOFs per node, also make sure it's a vertical matrix!
    #loads is a dictionary where the keys are the node indices and the values are the load values applied at those nodes!
    for node, load_value in loads.items():
        F[2*node,0] = load_value # Vertical load applied to the even (vert.) displacement DOF
    
    return F


def apply_pinned_boundary_conditions(K, F, pinned_nodes, large_value=1e12):
    """Applies pinned boundary conditions by setting large stiffness at constrained DOFs."""
    for node in pinned_nodes:
        K[2*node, 2*node] = large_value # Restrict vertical displacement DOF
        F[2*node] = 0 # No load at the pinned support in the vertical direction
    
    return K, F


def solve_system_displacement(K, F):
    disp_matrix = np.linalg.solve(K,F)
    
    return disp_matrix


def compute_internal_forces(global_disp_matrix, member_connectivity, member_lengths, E, I):
    shear_forces = [0]
    bending_moments = []
    node_locations = [0] #use for plotting
    # get DOF indices for each element
       
    all_mbr_stiffness = []
    all_mbr_disp = []
        
    for i, indices in enumerate(member_connectivity):
        node_locations.append(node_locations[-1] + member_lengths[i]) #should start at zero, then add on..
        
        node_1, node_2 = indices  
        matrix_indices = [2*node_1, 2*node_1+1, 2*node_2, 2*node_2+1]
        mbr_disp = global_disp_matrix[matrix_indices] # 4x1

        mbr_stiffness = beam_element_stiffness(E, I, member_lengths[i]) # 4x4 (original members stiffness matrix)
        
        mbr_forces = mbr_stiffness @ mbr_disp
                
        shear_force_n1 = mbr_forces[0]
        shear_force_n2 = mbr_forces[2]
        
        shear_forces[-1] += shear_force_n1 # Add onto the previous force (sum of forces must = 0)
        shear_forces.append(shear_force_n2)
        
        mbr_moment_n1 = mbr_forces[1]
        mbr_moment_n2 = mbr_forces[3]
        
        bending_moments.append(mbr_moment_n1)
        bending_moments.append(mbr_moment_n2)      
           
    return np.array(node_locations), np.array(all_mbr_disp), np.array(shear_forces), np.array(bending_moments)

        

def filtered_bending_moments(bending_moments, node_locations): 
    #keep the first two, then get rid of every other
    filtered_bending_moments = np.array([bending_moments[i][0] for i in range(len(bending_moments)) if i == 0 or i % 2 != 0])
    
    return node_locations, filtered_bending_moments



# shear_forces = [[  5.],[-10.],[  5.]]
# point_load_nodes = {0:0, 1:-10, 2:0} #Downwards is negative 
# pin_locations = [0,2] 

def filtered_shear_forces(shear_forces, point_load_nodes, pin_locations, num_subdivs, node_locations):
    filtered_shear_forces = []
    
    point_load_list = [] #if the load isn't zero, I will store the node number
    for node, force in point_load_nodes.items():
        if force != 0:
            point_load_list.append(num_subdivs*node)
    
    # now says [2] i think
    
    pin_loc_list = []
    
    for pin in pin_locations:
        if pin != 0: # ignore the first entry so we don't double count in the for loop coming up
            pin_loc_list.append(num_subdivs*pin)
   
    # add shear forces to each node
    counter = 0
    for ii, force in enumerate(shear_forces):
        for node in point_load_list:
            for pin in pin_loc_list:       
                if ii == 0:
                    filtered_shear_forces.append(0) # at start of graph, start at zero
                    node_locations = np.insert(node_locations,0,0) # add x_loc to match
                if ii == node or ii == pin: # if there is a point load or a pin to a beam here:
                    #then steal previous forces
                    counter+=1                 
                    filtered_shear_forces.append(filtered_shear_forces[-1]) #steal previous force
                    filtered_shear_forces.append((force + filtered_shear_forces[-1])[0]) #do normal force calc
                    node_locations = np.insert(node_locations,ii+counter,node_locations[ii+counter]) # this work? 
                else:
                    filtered_shear_forces.append((force + filtered_shear_forces[-1])[0])

    
    
    #should output [1]
    #need to set that to an adjusted number for subdivisions
    # we want 3, based on subdiv of 2
    
        
    
    #at node 1, append previous node's value
    #then append a new value of force[-1] + node1_force
    
    # at point loads, add an extra node for before..
    # add a duplicate of the previous node for now before doing math for next node, may want to do linear interpolation in the future
    
                
                
                
# should say [0,5,5,5,-5,-5,-5,0]
                
    filtered_shear_forces = np.array(filtered_shear_forces)  
    return node_locations, filtered_shear_forces

        
    
