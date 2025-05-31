import { create } from 'zustand';

// Create a store for our application state
const useAppStore = create((set) => ({
  // Project state
  projects: [],
  activeProject: null,
  
  // Beam analysis state
  beams: [],
  activeBeam: null,
  selectedElement: null,
  
  // UI state
  sidebarCollapsed: false,
  showPropertiesPanel: true,
  activeTab: 'geometry',
  
  // Load combinations from the backend
  loadCombinations: [],
  activeLoadCombination: null,
  
  // Actions
  setProjects: (projects) => set({ projects }),
  setActiveProject: (activeProject) => set({ activeProject }),
  
  setBeams: (beams) => set({ beams }),
  setActiveBeam: (activeBeam) => set({ activeBeam }),
  
  setSelectedElement: (selectedElement) => set({ selectedElement }),
  
  toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
  togglePropertiesPanel: () => set((state) => ({ showPropertiesPanel: !state.showPropertiesPanel })),
  setActiveTab: (activeTab) => set({ activeTab }),
  
  setLoadCombinations: (loadCombinations) => set({ loadCombinations }),
  setActiveLoadCombination: (activeLoadCombination) => set({ activeLoadCombination }),
  
  // Add a new beam to the current project
  addBeam: (beam) => set((state) => ({ 
    beams: [...state.beams, beam],
    activeBeam: beam
  })),
  
  // Update a beam in the current project
  updateBeam: (updatedBeam) => set((state) => ({
    beams: state.beams.map(beam => 
      beam.id === updatedBeam.id ? updatedBeam : beam
    )
  })),
  
  // Remove a beam from the current project
  removeBeam: (beamId) => set((state) => ({
    beams: state.beams.filter(beam => beam.id !== beamId),
    activeBeam: state.activeBeam?.id === beamId ? null : state.activeBeam
  })),
  
  // Add a load to the active beam
  addLoad: (load) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      loads: [...state.activeBeam.loads, load]
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam
    };
  }),
  
  // Update a load in the active beam
  updateLoad: (updatedLoad) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      loads: state.activeBeam.loads.map(load => 
        load.id === updatedLoad.id ? updatedLoad : load
      )
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam
    };
  }),
  
  // Remove a load from the active beam
  removeLoad: (loadId) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      loads: state.activeBeam.loads.filter(load => load.id !== loadId)
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam,
      selectedElement: state.selectedElement?.id === loadId ? null : state.selectedElement
    };
  }),
  
  // Add a support to the active beam
  addSupport: (support) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      supports: [...state.activeBeam.supports, support]
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam
    };
  }),
  
  // Update a support in the active beam
  updateSupport: (updatedSupport) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      supports: state.activeBeam.supports.map(support => 
        support.id === updatedSupport.id ? updatedSupport : support
      )
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam
    };
  }),
  
  // Remove a support from the active beam
  removeSupport: (supportId) => set((state) => {
    if (!state.activeBeam) return state;
    
    const updatedBeam = {
      ...state.activeBeam,
      supports: state.activeBeam.supports.filter(support => support.id !== supportId)
    };
    
    return {
      beams: state.beams.map(beam => 
        beam.id === updatedBeam.id ? updatedBeam : beam
      ),
      activeBeam: updatedBeam,
      selectedElement: state.selectedElement?.id === supportId ? null : state.selectedElement
    };
  }),
}));

export default useAppStore;
