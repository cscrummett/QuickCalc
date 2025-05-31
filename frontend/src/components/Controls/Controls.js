import React, { useState } from 'react';
import { 
  FaPlus, 
  FaTrash, 
  FaCalculator,
  FaSave,
  FaFileExport,
  FaFileImport
} from 'react-icons/fa';
import useAppStore from '../../store/appStore';

const Controls = () => {
  const [loadType, setLoadType] = useState('point');
  const [loadPosition, setLoadPosition] = useState(0);
  const [loadMagnitude, setLoadMagnitude] = useState(0);
  const [loadLength, setLoadLength] = useState(0);
  
  const activeBeam = useAppStore(state => state.activeBeam);
  const addLoad = useAppStore(state => state.addLoad);
  const addSupport = useAppStore(state => state.addSupport);
  const loadCombinations = useAppStore(state => state.loadCombinations);
  const activeLoadCombination = useAppStore(state => state.activeLoadCombination);
  const setActiveLoadCombination = useAppStore(state => state.setActiveLoadCombination);
  
  const handleAddLoad = () => {
    if (!activeBeam) return;
    
    const newLoad = {
      id: `load-${Date.now()}`,
      type: loadType,
      position: parseFloat(loadPosition),
      magnitude: parseFloat(loadMagnitude),
      length: loadType === 'distributed' ? parseFloat(loadLength) : 0
    };
    
    addLoad(newLoad);
  };
  
  const handleAddSupport = (type) => {
    if (!activeBeam) return;
    
    const newSupport = {
      id: `support-${Date.now()}`,
      type: type,
      position: type === 'fixed' ? 0 : (type === 'pin' ? 0 : activeBeam.length)
    };
    
    addSupport(newSupport);
  };
  
  const handleLoadCombinationChange = (e) => {
    const combinationId = e.target.value;
    const combination = loadCombinations.find(combo => combo.id === combinationId);
    setActiveLoadCombination(combination);
  };
  
  return (
    <div className="p-4">
      <h2 className="text-lg font-medium mb-4">Controls</h2>
      
      {/* Load Controls */}
      <div className="mb-6">
        <h3 className="text-md font-medium mb-2">Add Load</h3>
        <div className="grid grid-cols-2 gap-3">
          <div className="form-group">
            <label className="form-label">Type</label>
            <select 
              className="form-select"
              value={loadType}
              onChange={(e) => setLoadType(e.target.value)}
            >
              <option value="point">Point Load</option>
              <option value="distributed">Distributed Load</option>
              <option value="moment">Moment</option>
            </select>
          </div>
          
          <div className="form-group">
            <label className="form-label">Position (m)</label>
            <input 
              type="number" 
              className="form-input"
              value={loadPosition}
              onChange={(e) => setLoadPosition(e.target.value)}
              min={0}
              max={activeBeam ? activeBeam.length : 100}
              step={0.1}
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">Magnitude (kN)</label>
            <input 
              type="number" 
              className="form-input"
              value={loadMagnitude}
              onChange={(e) => setLoadMagnitude(e.target.value)}
              step={0.1}
            />
          </div>
          
          {loadType === 'distributed' && (
            <div className="form-group">
              <label className="form-label">Length (m)</label>
              <input 
                type="number" 
                className="form-input"
                value={loadLength}
                onChange={(e) => setLoadLength(e.target.value)}
                min={0.1}
                max={activeBeam ? activeBeam.length : 100}
                step={0.1}
              />
            </div>
          )}
        </div>
        
        <button 
          className="px-3 py-2 bg-blue-500 text-white rounded flex items-center mt-2"
          onClick={handleAddLoad}
        >
          <FaPlus className="mr-1" /> Add Load
        </button>
      </div>
      
      {/* Support Controls */}
      <div className="mb-6">
        <h3 className="text-md font-medium mb-2">Add Support</h3>
        <div className="flex space-x-2">
          <button 
            className="px-3 py-2 bg-gray-700 text-white rounded flex items-center"
            onClick={() => handleAddSupport('pin')}
          >
            Add Pin
          </button>
          <button 
            className="px-3 py-2 bg-gray-700 text-white rounded flex items-center"
            onClick={() => handleAddSupport('roller')}
          >
            Add Roller
          </button>
          <button 
            className="px-3 py-2 bg-gray-700 text-white rounded flex items-center"
            onClick={() => handleAddSupport('fixed')}
          >
            Add Fixed
          </button>
        </div>
      </div>
      
      {/* Load Combination Controls */}
      <div className="mb-6">
        <h3 className="text-md font-medium mb-2">Load Combinations</h3>
        <div className="form-group">
          <label className="form-label">Select Combination</label>
          <select 
            className="form-select"
            value={activeLoadCombination?.id || ''}
            onChange={handleLoadCombinationChange}
          >
            <option value="">None (Unfactored)</option>
            {loadCombinations.map(combo => (
              <option key={combo.id} value={combo.id}>
                {combo.name}
              </option>
            ))}
          </select>
        </div>
      </div>
      
      {/* Analysis Controls */}
      <div>
        <button 
          className="px-3 py-2 bg-green-600 text-white rounded flex items-center w-full justify-center"
        >
          <FaCalculator className="mr-1" /> Run Analysis
        </button>
        
        <div className="flex space-x-2 mt-2">
          <button className="px-3 py-2 bg-blue-500 text-white rounded flex items-center flex-1 justify-center">
            <FaSave className="mr-1" /> Save
          </button>
          <button className="px-3 py-2 bg-purple-500 text-white rounded flex items-center flex-1 justify-center">
            <FaFileExport className="mr-1" /> Export
          </button>
          <button className="px-3 py-2 bg-orange-500 text-white rounded flex items-center flex-1 justify-center">
            <FaFileImport className="mr-1" /> Import
          </button>
        </div>
      </div>
    </div>
  );
};

export default Controls;
