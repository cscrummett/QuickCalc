import React, { useState, useEffect } from 'react';
import { 
  FaChartLine, 
  FaChartBar, 
  FaTable,
  FaExchangeAlt
} from 'react-icons/fa';
import useAppStore from '../../store/appStore';

const Analysis = () => {
  const [activeView, setActiveView] = useState('diagram');
  const [analysisResults, setAnalysisResults] = useState(null);
  
  const activeBeam = useAppStore(state => state.activeBeam);
  const activeLoadCombination = useAppStore(state => state.activeLoadCombination);
  
  // Simulate fetching analysis results when beam or load combination changes
  useEffect(() => {
    if (!activeBeam) {
      setAnalysisResults(null);
      return;
    }
    
    // In a real implementation, this would call the backend API
    // to perform the beam analysis with the selected load combination
    const fetchAnalysisResults = async () => {
      try {
        // Simulate API call delay
        await new Promise(resolve => setTimeout(resolve, 300));
        
        // Mock analysis results
        const results = {
          maxShear: 10.5,
          maxMoment: 25.3,
          maxDeflection: 0.015,
          reactions: [
            { position: 0, value: 15.2 },
            { position: activeBeam.length, value: 9.8 }
          ],
          shearDiagram: generateMockDiagram(activeBeam.length, 10.5),
          momentDiagram: generateMockDiagram(activeBeam.length, 25.3),
          deflectionDiagram: generateMockDiagram(activeBeam.length, 0.015),
          loadCombination: activeLoadCombination ? activeLoadCombination.name : 'Unfactored'
        };
        
        setAnalysisResults(results);
      } catch (error) {
        console.error('Error fetching analysis results:', error);
        setAnalysisResults(null);
      }
    };
    
    fetchAnalysisResults();
  }, [activeBeam, activeLoadCombination]);
  
  // Helper function to generate mock diagram data
  const generateMockDiagram = (length, maxValue) => {
    const points = 20;
    const data = [];
    
    for (let i = 0; i <= points; i++) {
      const x = (i / points) * length;
      // Generate a parabolic curve for the diagram
      const y = maxValue * Math.sin((Math.PI * x) / length);
      data.push({ x, y });
    }
    
    return data;
  };
  
  return (
    <div className="p-4">
      <h2 className="text-lg font-medium mb-4">Analysis Results</h2>
      
      {!activeBeam && (
        <div className="text-center text-gray-500 py-8">
          Select or create a beam to view analysis results
        </div>
      )}
      
      {activeBeam && !analysisResults && (
        <div className="text-center text-gray-500 py-8">
          Loading analysis results...
        </div>
      )}
      
      {activeBeam && analysisResults && (
        <>
          {/* View Selector */}
          <div className="flex border-b border-gray-200 mb-4">
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeView === 'diagram' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => setActiveView('diagram')}
            >
              <FaChartLine className="mr-2" />
              Diagrams
            </button>
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeView === 'results' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => setActiveView('results')}
            >
              <FaTable className="mr-2" />
              Results
            </button>
            <button
              className={`flex items-center px-4 py-2 text-sm font-medium ${activeView === 'combinations' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              onClick={() => setActiveView('combinations')}
            >
              <FaExchangeAlt className="mr-2" />
              Combinations
            </button>
          </div>
          
          {/* Diagrams View */}
          {activeView === 'diagram' && (
            <div className="space-y-4">
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Shear Diagram</h3>
                <div className="h-32 bg-white rounded border border-gray-300">
                  {/* This would be a D3.js visualization in the real implementation */}
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    Shear Diagram Visualization
                  </div>
                </div>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Moment Diagram</h3>
                <div className="h-32 bg-white rounded border border-gray-300">
                  {/* This would be a D3.js visualization in the real implementation */}
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    Moment Diagram Visualization
                  </div>
                </div>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Deflection Diagram</h3>
                <div className="h-32 bg-white rounded border border-gray-300">
                  {/* This would be a D3.js visualization in the real implementation */}
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    Deflection Diagram Visualization
                  </div>
                </div>
              </div>
            </div>
          )}
          
          {/* Results View */}
          {activeView === 'results' && (
            <div className="space-y-4">
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Load Combination</h3>
                <p className="text-blue-600 font-medium">{analysisResults.loadCombination}</p>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Maximum Values</h3>
                <table className="w-full">
                  <tbody>
                    <tr>
                      <td className="py-1">Max Shear:</td>
                      <td className="py-1 text-right font-medium">{analysisResults.maxShear.toFixed(2)} kN</td>
                    </tr>
                    <tr>
                      <td className="py-1">Max Moment:</td>
                      <td className="py-1 text-right font-medium">{analysisResults.maxMoment.toFixed(2)} kN·m</td>
                    </tr>
                    <tr>
                      <td className="py-1">Max Deflection:</td>
                      <td className="py-1 text-right font-medium">{analysisResults.maxDeflection.toFixed(4)} m</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Reactions</h3>
                <table className="w-full">
                  <thead>
                    <tr>
                      <th className="text-left py-1">Position</th>
                      <th className="text-right py-1">Value</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analysisResults.reactions.map((reaction, index) => (
                      <tr key={index}>
                        <td className="py-1">{reaction.position.toFixed(2)} m</td>
                        <td className="py-1 text-right font-medium">{reaction.value.toFixed(2)} kN</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
          
          {/* Load Combinations View */}
          {activeView === 'combinations' && (
            <div className="space-y-4">
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Current Load Combination</h3>
                <p className="text-blue-600 font-medium">{analysisResults.loadCombination}</p>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">ASCE 7 Load Combinations</h3>
                <ul className="space-y-1">
                  <li className="text-sm">1.4D</li>
                  <li className="text-sm">1.2D + 1.6L + 0.5(Lr or S or R)</li>
                  <li className="text-sm">1.2D + 1.6(Lr or S or R) + (0.5L or 0.5W)</li>
                  <li className="text-sm">1.2D + 1.0W + 0.5L + 0.5(Lr or S or R)</li>
                  <li className="text-sm">1.2D + 1.0E + 0.5L + 0.2S</li>
                  <li className="text-sm">0.9D + 1.0W</li>
                  <li className="text-sm">0.9D + 1.0E</li>
                </ul>
              </div>
              
              <div className="bg-gray-100 p-3 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Custom Load Combinations</h3>
                <p className="text-sm text-gray-500 italic">No custom combinations defined</p>
                <button className="mt-2 px-3 py-1 bg-blue-500 text-white text-sm rounded">
                  Create Custom Combination
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Analysis;
