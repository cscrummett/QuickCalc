import React from 'react';
import { 
  FaRuler, 
  FaWeight, 
  FaLayerGroup,
  FaTimes,
  FaCalculator,
  FaTrash
} from 'react-icons/fa';
import useAppStore from '../../store/appStore';

const PropertiesPanel = () => {
  // Get state and actions from the store
  const activeTab = useAppStore(state => state.activeTab);
  const setActiveTab = useAppStore(state => state.setActiveTab);
  const selectedElement = useAppStore(state => state.selectedElement);
  const setSelectedElement = useAppStore(state => state.setSelectedElement);
  
  const activeBeam = useAppStore(state => state.activeBeam);
  const updateBeam = useAppStore(state => state.updateBeam);
  const updateLoad = useAppStore(state => state.updateLoad);
  const removeLoad = useAppStore(state => state.removeLoad);
  const updateSupport = useAppStore(state => state.updateSupport);
  const removeSupport = useAppStore(state => state.removeSupport);
  
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };
  
  const handleClearSelection = () => {
    setSelectedElement(null);
  };
  
  const handleDeleteElement = () => {
    if (!selectedElement) return;
    
    if (selectedElement.type === 'load') {
      removeLoad(selectedElement.id);
    } else if (selectedElement.type === 'support') {
      removeSupport(selectedElement.id);
    }
    
    setSelectedElement(null);
  };
  
  const handleInputChange = (field, value) => {
    if (!selectedElement) return;
    
    if (selectedElement.type === 'beam') {
      const updatedBeam = { ...activeBeam, [field]: value };
      updateBeam(updatedBeam);
    } else if (selectedElement.type === 'load') {
      const updatedLoad = { ...selectedElement, [field]: value };
      updateLoad(updatedLoad);
    } else if (selectedElement.type === 'support') {
      const updatedSupport = { ...selectedElement, [field]: value };
      updateSupport(updatedSupport);
    }
  };
  
  const handleSelectChange = (field, event) => {
    handleInputChange(field, event.target.value);
  };
  
  const handleNumberChange = (field, event) => {
    const value = parseFloat(event.target.value);
    if (!isNaN(value)) {
      handleInputChange(field, value);
    }
  };
  
  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 flex justify-between items-center">
        <h2 className="text-lg font-medium">
          {selectedElement ? 
            `${selectedElement.type.charAt(0).toUpperCase() + selectedElement.type.slice(1)} Properties` : 
            'Properties'}
        </h2>
        {selectedElement && (
          <div className="flex space-x-2">
            <button 
              className="text-red-500 hover:text-red-700"
              onClick={handleDeleteElement}
              title="Delete Element"
            >
              <FaTrash />
            </button>
            <button 
              className="text-gray-500 hover:text-gray-700"
              onClick={handleClearSelection}
              title="Clear Selection"
            >
              <FaTimes />
            </button>
          </div>
        )}
      </div>
      
      {selectedElement ? (
        <>
          {/* Tabs */}
          <div className="flex border-b border-gray-200">
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeTab === 'geometry' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => handleTabChange('geometry')}
            >
              <FaRuler className="mr-2" />
              Geometry
            </button>
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeTab === 'materials' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => handleTabChange('materials')}
            >
              <FaLayerGroup className="mr-2" />
              Materials
            </button>
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeTab === 'loads' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => handleTabChange('loads')}
            >
              <FaWeight className="mr-2" />
              Loads
            </button>
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeTab === 'analysis' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => handleTabChange('analysis')}
            >
              <FaCalculator className="mr-2" />
              Analysis
            </button>
          </div>
          
          {/* Content */}
          <div className="flex-1 overflow-y-auto p-4">
            {activeTab === 'geometry' && (
              <div>
                {selectedElement.type === 'beam' && (
                  <div className="space-y-4">
                    <div className="form-group">
                      <label className="form-label">Name</label>
                      <input 
                        type="text" 
                        className="form-input" 
                        value={selectedElement.name || ''} 
                        onChange={(e) => handleInputChange('name', e.target.value)} 
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Length</label>
                      <input 
                        type="number" 
                        className="form-input" 
                        value={selectedElement.length || ''} 
                        onChange={(e) => handleNumberChange('length', e)} 
                      />
                      <span className="text-sm text-gray-500 ml-2">m</span>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Section</label>
                      <select 
                        className="form-select"
                        value={selectedElement.section || 'W12x26'}
                        onChange={(e) => handleSelectChange('section', e)}
                      >
                        <option value="W12x26">W12x26</option>
                        <option value="W16x31">W16x31</option>
                        <option value="W18x35">W18x35</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Moment of Inertia</label>
                      <input 
                        type="number" 
                        className="form-input" 
                        value={selectedElement.momentOfInertia || 1250} 
                        onChange={(e) => handleNumberChange('momentOfInertia', e)} 
                      />
                      <span className="text-sm text-gray-500 ml-2">in⁴</span>
                    </div>
                  </div>
                )}
                
                {selectedElement.type === 'support' && (
                  <div className="space-y-4">
                    <div className="form-group">
                      <label className="form-label">Type</label>
                      <select 
                        className="form-select"
                        value={selectedElement.type || 'pin'}
                        onChange={(e) => handleSelectChange('type', e)}
                      >
                        <option value="pin">Pin</option>
                        <option value="roller">Roller</option>
                        <option value="fixed">Fixed</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Position</label>
                      <input 
                        type="number" 
                        className="form-input" 
                        value={selectedElement.position || 0} 
                        onChange={(e) => handleNumberChange('position', e)} 
                      />
                      <span className="text-sm text-gray-500 ml-2">m</span>
                    </div>
                  </div>
                )}
                
                {selectedElement.type === 'load' && (
                  <div className="space-y-4">
                    <div className="form-group">
                      <label className="form-label">Type</label>
                      <select 
                        className="form-select"
                        value={selectedElement.type || 'point'}
                        onChange={(e) => handleSelectChange('loadType', e)}
                      >
                        <option value="point">Point Load</option>
                        <option value="distributed">Distributed Load</option>
                        <option value="moment">Moment</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Position</label>
                      <input 
                        type="number" 
                        className="form-input" 
                        value={selectedElement.position || 0} 
                        onChange={(e) => handleNumberChange('position', e)} 
                      />
                      <span className="text-sm text-gray-500 ml-2">m</span>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Magnitude</label>
                      <input 
                        type="number" 
                        className="form-input" 
                        value={selectedElement.magnitude || 0} 
                        onChange={(e) => handleNumberChange('magnitude', e)} 
                      />
                      <span className="text-sm text-gray-500 ml-2">kN</span>
                    </div>
                    {selectedElement.loadType === 'distributed' && (
                      <div className="form-group">
                        <label className="form-label">Length</label>
                        <input 
                          type="number" 
                          className="form-input" 
                          value={selectedElement.length || 0} 
                          onChange={(e) => handleNumberChange('length', e)} 
                        />
                        <span className="text-sm text-gray-500 ml-2">m</span>
                      </div>
                    )}
                    
                    <div className="form-group">
                      <label className="form-label">Direction</label>
                      <select
                        className="form-select"
                        value={selectedElement.magnitude < 0 ? 'down' : 'up'}
                        onChange={(e) => handleInputChange('magnitude', e.target.value === 'down' ? -Math.abs(selectedElement.magnitude) : Math.abs(selectedElement.magnitude))}
                      >
                        <option value="down">Downward</option>
                        <option value="up">Upward</option>
                      </select>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
          
          {/* Footer actions */}
          <div className="p-4 border-t border-gray-200 bg-gray-50">
            <div className="flex justify-end space-x-2">
              <button className="btn btn-secondary">
                Reset
              </button>
              <button className="btn btn-primary">
                Apply
              </button>
            </div>
          </div>
        </>
      ) : (
        <div className="flex-1 flex items-center justify-center text-gray-500">
          <div className="text-center p-6">
            <p className="mb-4">No element selected</p>
            <p className="text-sm">Select an element on the canvas to view and edit its properties</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default PropertiesPanel;
