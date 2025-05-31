import React, { useState, useEffect } from 'react';
import { 
  FaFolder, 
  FaFolderOpen, 
  FaChevronDown, 
  FaChevronRight, 
  FaSearch,
  FaRuler,
  FaPlus,
  FaRegFileAlt,
  FaWeight,
  FaLayerGroup,
  FaCalculator
} from 'react-icons/fa';
import useAppStore from '../../store/appStore';

const TreeNode = ({ node, level = 0, collapsed, onSelect }) => {
  const [isOpen, setIsOpen] = useState(node.defaultOpen || false);
  const hasChildren = node.children && node.children.length > 0;
  
  const toggleOpen = (e) => {
    e.stopPropagation();
    setIsOpen(!isOpen);
  };
  
  return (
    <div>
      <div 
        className={`sidebar-item ${node.active ? 'active' : ''}`}
        style={{ paddingLeft: `${level * 12 + 12}px` }}
        onClick={() => onSelect(node)}
      >
        {hasChildren ? (
          <span onClick={toggleOpen} className="mr-1 text-gray-500">
            {isOpen ? <FaChevronDown size={12} /> : <FaChevronRight size={12} />}
          </span>
        ) : (
          <span className="mr-1 w-3"></span>
        )}
        
        <span className="sidebar-item-icon">
          {node.type === 'folder' ? (
            isOpen ? <FaFolderOpen /> : <FaFolder />
          ) : node.type === 'beam' ? (
            <FaRuler />
          ) : node.type === 'load' ? (
            <FaWeight />
          ) : node.type === 'loadCombination' ? (
            <FaLayerGroup />
          ) : (
            <FaRegFileAlt />
          )}
        </span>
        
        {!collapsed && <span className="truncate">{node.name}</span>}
      </div>
      
      {hasChildren && isOpen && !collapsed && (
        <div className="ml-2">
          {node.children.map((child, index) => (
            <TreeNode 
              key={child.id || index} 
              node={child} 
              level={level + 1}
              collapsed={collapsed}
              onSelect={onSelect}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const Sidebar = ({ collapsed }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredProjects, setFilteredProjects] = useState([]);
  
  // Get state and actions from the store
  const projects = useAppStore(state => state.projects);
  const activeProject = useAppStore(state => state.activeProject);
  const activeBeam = useAppStore(state => state.activeBeam);
  const loadCombinations = useAppStore(state => state.loadCombinations);
  
  const setActiveProject = useAppStore(state => state.setActiveProject);
  const setActiveBeam = useAppStore(state => state.setActiveBeam);
  const setActiveLoadCombination = useAppStore(state => state.setActiveLoadCombination);
  const setSelectedElement = useAppStore(state => state.setSelectedElement);
  
  // Sample project data with load combinations integration
  const projectData = [
    {
      id: 'project-1',
      name: 'Structural Analysis',
      type: 'folder',
      defaultOpen: true,
      children: [
        {
          id: 'beam-analysis',
          name: 'Beam Analysis',
          type: 'folder',
          defaultOpen: true,
          children: [
            { id: 'beam-1', name: 'Simple Beam', type: 'beam', active: true },
            { id: 'beam-2', name: 'Cantilever Beam', type: 'beam' },
            { id: 'beam-3', name: 'Multi-span Beam', type: 'beam' }
          ]
        },
        {
          id: 'load-combinations',
          name: 'Load Combinations',
          type: 'folder',
          children: [
            { id: 'asce-7', name: 'ASCE 7 Combinations', type: 'loadCombination' },
            { id: 'custom-combo', name: 'Custom Combinations', type: 'loadCombination' }
          ]
        }
      ]
    },
    {
      id: 'project-2',
      name: 'Material Properties',
      type: 'folder',
      children: [
        { id: 'steel', name: 'Steel Properties', type: 'file' },
        { id: 'concrete', name: 'Concrete Properties', type: 'file' }
      ]
    }
  ];
  
  // Set the initial projects if none exist
  useEffect(() => {
    if (projects.length === 0) {
      useAppStore.getState().setProjects(projectData);
    }
  }, [projects.length]);
  
  // Filter projects based on search term
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredProjects(projects.length > 0 ? projects : projectData);
      return;
    }
    
    const searchLower = searchTerm.toLowerCase();
    
    // Helper function to search recursively through the tree
    const searchNodes = (nodes) => {
      const results = [];
      
      for (const node of nodes) {
        const nodeMatches = node.name.toLowerCase().includes(searchLower);
        let childMatches = [];
        
        if (node.children && node.children.length > 0) {
          childMatches = searchNodes(node.children);
        }
        
        if (nodeMatches || childMatches.length > 0) {
          // Create a copy of the node
          const nodeCopy = { ...node };
          
          // If there are matching children, include only those
          if (childMatches.length > 0) {
            nodeCopy.children = childMatches;
            nodeCopy.defaultOpen = true; // Auto-expand when filtered
          }
          
          results.push(nodeCopy);
        }
      }
      
      return results;
    };
    
    const filtered = searchNodes(projects.length > 0 ? projects : projectData);
    setFilteredProjects(filtered);
  }, [searchTerm, projects, projectData]);
  
  const handleNodeSelect = (node) => {
    // Handle different node types
    switch (node.type) {
      case 'beam':
        // Set the active beam
        setActiveBeam({
          id: node.id,
          name: node.name,
          // Sample beam data - in a real app, this would come from the backend
          length: 20,
          supports: [
            { id: 'support-1', position: 0, type: 'pin' },
            { id: 'support-2', position: 20, type: 'roller' }
          ],
          loads: [
            { id: 'load-1', position: 10, magnitude: -5, type: 'point' },
            { id: 'load-2', position: 5, magnitude: -2, length: 10, type: 'distributed' }
          ]
        });
        break;
        
      case 'loadCombination':
        // Set the active load combination
        setActiveLoadCombination({
          id: node.id,
          name: node.name,
          // Sample load combination data
          combinations: node.id === 'asce-7' ? [
            { name: '1.4D', factors: { dead: 1.4 } },
            { name: '1.2D + 1.6L', factors: { dead: 1.2, live: 1.6 } },
            { name: '1.2D + 1.0W + 0.5L', factors: { dead: 1.2, wind: 1.0, live: 0.5 } }
          ] : [
            { name: 'Custom 1', factors: { dead: 1.0, live: 1.0 } },
            { name: 'Custom 2', factors: { dead: 1.1, live: 1.1 } }
          ]
        });
        break;
        
      case 'folder':
        // Just toggle the folder open/closed
        break;
        
      default:
        // Handle other node types
        console.log(`Selected: ${node.name} (${node.type})`);
    }
  };
  
  return (
    <div className="h-full flex flex-col">
      {/* Search bar */}
      {!collapsed && (
        <div className="p-3 border-b border-gray-200">
          <div className="relative">
            <input
              type="text"
              placeholder="Search..."
              className="w-full pl-8 pr-3 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <FaSearch className="absolute left-3 top-2.5 text-gray-400" />
          </div>
        </div>
      )}
      
      {/* Project tree */}
      <div className="flex-1 overflow-y-auto py-2">
        {filteredProjects.map((project) => (
          <TreeNode 
            key={project.id} 
            node={project} 
            collapsed={collapsed} 
            onSelect={handleNodeSelect}
          />
        ))}
      </div>
      
      {/* Action buttons */}
      {!collapsed && (
        <div className="p-3 border-t border-gray-200 space-y-2">
          <button className="btn btn-primary w-full flex items-center justify-center">
            <FaPlus className="mr-2" />
            New Beam
          </button>
          <button className="btn btn-secondary w-full flex items-center justify-center">
            <FaCalculator className="mr-2" />
            Run Analysis
          </button>
        </div>
      )}
    </div>
  );
};

export default Sidebar;
