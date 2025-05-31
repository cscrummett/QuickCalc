import React from 'react';
import BeamCanvas from '../Canvas/BeamCanvas';
import Controls from '../Controls/Controls';
import Analysis from '../Analysis/Analysis';
import PropertiesPanel from '../PropertiesPanel/PropertiesPanel';
import useAppStore from '../../store/appStore';

const MainContent = () => {
  const showPropertiesPanel = useAppStore(state => state.showPropertiesPanel);
  
  return (
    <div className="p-6 h-full flex flex-col">
      {/* Beam Canvas */}
      <div className="w-full h-64 bg-white rounded-lg shadow-lg mb-6">
        <BeamCanvas />
      </div>
      
      {/* Controls and Analysis Results */}
      <div className="grid grid-cols-2 gap-6 flex-1">
        <div className="bg-white rounded-lg shadow-lg overflow-auto">
          <Controls />
        </div>
        <div className="bg-white rounded-lg shadow-lg overflow-auto">
          <Analysis />
        </div>
      </div>
      
      {/* Properties Panel - Floating */}
      {showPropertiesPanel && (
        <div className="absolute top-20 right-10 w-80 bg-white shadow-lg rounded-lg z-10 max-h-[70vh] overflow-auto">
          <PropertiesPanel />
        </div>
      )}
    </div>
  );
};

export default MainContent;
