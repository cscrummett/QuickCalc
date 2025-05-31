import React, { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import { FaRuler, FaWeight, FaArrowsAlt, FaPlus, FaTrash, FaRegSave } from 'react-icons/fa';

const Canvas = ({ onElementSelect }) => {
  const svgRef = useRef(null);
  const [activeTool, setActiveTool] = useState(null);
  const [gridVisible, setGridVisible] = useState(true);
  const [scale, setScale] = useState(1);
  
  // Sample beam data
  const beamData = {
    length: 20, // feet
    supports: [
      { position: 0, type: 'pin' },
      { position: 20, type: 'roller' }
    ],
    loads: [
      { position: 10, magnitude: -5, type: 'point' }, // kips
      { position: 5, magnitude: -2, length: 10, type: 'distributed' } // kips/ft
    ]
  };
  
  useEffect(() => {
    if (!svgRef.current) return;
    
    const svg = d3.select(svgRef.current);
    const width = svgRef.current.clientWidth;
    const height = svgRef.current.clientHeight;
    
    // Clear previous content
    svg.selectAll('*').remove();
    
    // Create main group for transformations
    const g = svg.append('g')
      .attr('transform', `scale(${scale})`);
    
    // Draw grid if visible
    if (gridVisible) {
      const gridSize = 20;
      const grid = g.append('g')
        .attr('class', 'grid')
        .style('opacity', 0.1);
      
      // Vertical lines
      for (let x = 0; x <= width; x += gridSize) {
        grid.append('line')
          .attr('x1', x)
          .attr('y1', 0)
          .attr('x2', x)
          .attr('y2', height)
          .style('stroke', '#000');
      }
      
      // Horizontal lines
      for (let y = 0; y <= height; y += gridSize) {
        grid.append('line')
          .attr('x1', 0)
          .attr('y1', y)
          .attr('x2', width)
          .attr('y2', y)
          .style('stroke', '#000');
      }
    }
    
    // Calculate scale factors
    const margin = 50;
    const beamLength = beamData.length;
    const xScale = (width - 2 * margin) / beamLength;
    
    // Draw beam
    const beamY = height / 2;
    const beamGroup = g.append('g')
      .attr('class', 'beam')
      .attr('transform', `translate(${margin}, 0)`);
    
    // Beam line
    beamGroup.append('line')
      .attr('x1', 0)
      .attr('y1', beamY)
      .attr('x2', beamLength * xScale)
      .attr('y2', beamY)
      .style('stroke', '#1f77b4')
      .style('stroke-width', 3)
      .on('click', () => onElementSelect({ type: 'beam', data: beamData }));
    
    // Draw supports
    if (activeBeam.supports && activeBeam.supports.length > 0) {
      activeBeam.supports.forEach(support => {
        const supportX = beamToSvgCoords(support.position, beamLength, width);
        const supportGroup = beamGroup.append('g')
          .attr('transform', `translate(${supportX}, ${beamY})`)
          .attr('class', `support ${selectedElement?.id === support.id ? 'selected' : ''}`)
          .on('click', () => {
            const supportElement = { ...support, type: 'support' };
            setSelectedElement(supportElement);
            onElementSelect(supportElement);
          });
        
        if (support.type === 'pin') {
          // Draw pin support (triangle)
          supportGroup.append('path')
            .attr('d', 'M-10,0 L10,0 L0,20 Z')
            .attr('fill', selectedElement?.id === support.id ? '#3182ce' : '#64748b');
        } else if (support.type === 'roller') {
          // Draw roller support (circle)
          supportGroup.append('circle')
            .attr('cx', 0)
            .attr('cy', 10)
            .attr('r', 10)
            .attr('fill', selectedElement?.id === support.id ? '#3182ce' : '#64748b');
        } else if (support.type === 'fixed') {
          // Draw fixed support (rectangle with hatching)
          supportGroup.append('rect')
            .attr('x', -10)
            .attr('y', 0)
            .attr('width', 20)
            .attr('height', 20)
            .attr('fill', selectedElement?.id === support.id ? '#3182ce' : '#64748b');
          
          // Add hatching lines
          for (let i = -10; i <= 10; i += 5) {
            supportGroup.append('line')
              .attr('x1', i)
              .attr('y1', 0)
              .attr('x2', i)
              .attr('y2', 20)
              .attr('stroke', 'white')
              .attr('stroke-width', 1);
          }
        }
        
        // Draw position label
        supportGroup.append('text')
          .attr('x', 0)
          .attr('y', 35)
          .attr('text-anchor', 'middle')
          .attr('fill', '#64748b')
          .attr('font-size', '10px')
          .text(`${support.position} m`);
      });
    }
    
    // Draw loads
    if (activeBeam.loads && activeBeam.loads.length > 0) {
      activeBeam.loads.forEach(load => {
        const loadX = beamToSvgCoords(load.position, beamLength, width);
        
        if (load.type === 'point') {
          // Draw point load
          const loadGroup = beamGroup.append('g')
            .attr('transform', `translate(${loadX}, ${beamY})`)
            .attr('class', `load ${selectedElement?.id === load.id ? 'selected' : ''}`)
            .on('click', () => {
              const loadElement = { ...load, type: 'load' };
              setSelectedElement(loadElement);
              onElementSelect(loadElement);
            });
          
          // Arrow pointing down for negative load (compression)
          const arrowDirection = load.magnitude < 0 ? -1 : 1;
          const arrowLength = 30 * Math.min(Math.abs(load.magnitude) / 5, 3);
          
          loadGroup.append('path')
            .attr('d', `M0,0 L-10,${arrowDirection * arrowLength} L10,${arrowDirection * arrowLength} Z`)
            .attr('fill', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e');
          
          loadGroup.append('line')
            .attr('x1', 0)
            .attr('y1', arrowDirection * arrowLength)
            .attr('x2', 0)
            .attr('y2', arrowDirection * arrowLength * 1.5)
            .attr('stroke', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e')
            .attr('stroke-width', 2);
          
          loadGroup.append('text')
            .attr('x', 0)
            .attr('y', arrowDirection * arrowLength * 1.5 - arrowDirection * 10)
            .attr('text-anchor', 'middle')
            .attr('fill', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e')
            .attr('font-size', '12px')
            .text(`${Math.abs(load.magnitude)} kN`);
          
        } else if (load.type === 'distributed') {
          // Draw distributed load
          const loadLength = (load.length / activeBeam.length) * beamLength;
          const loadGroup = beamGroup.append('g')
            .attr('class', `load ${selectedElement?.id === load.id ? 'selected' : ''}`)
            .on('click', () => {
              const loadElement = { ...load, type: 'load' };
              setSelectedElement(loadElement);
              onElementSelect(loadElement);
            });
          
          // Draw multiple arrows for distributed load
          const numArrows = Math.max(Math.floor(loadLength / 40), 2);
          const arrowSpacing = loadLength / numArrows;
          const arrowDirection = load.magnitude < 0 ? -1 : 1;
          const arrowLength = 20 * Math.min(Math.abs(load.magnitude) / 2, 3);
          
          // Draw the distributed load line
          loadGroup.append('line')
            .attr('x1', loadX)
            .attr('y1', beamY + arrowDirection * arrowLength)
            .attr('x2', loadX + loadLength)
            .attr('y2', beamY + arrowDirection * arrowLength)
            .attr('stroke', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e')
            .attr('stroke-width', 2);
          
          // Draw the arrows
          for (let i = 0; i <= numArrows; i++) {
            const x = loadX + i * arrowSpacing;
            
            loadGroup.append('line')
              .attr('x1', x)
              .attr('y1', beamY)
              .attr('x2', x)
              .attr('y2', beamY + arrowDirection * arrowLength)
              .attr('stroke', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e')
              .attr('stroke-width', 2);
            
            if (i < numArrows) {
              loadGroup.append('path')
                .attr('d', `M${x},${beamY} L${x-5},${beamY+arrowDirection*10} L${x+5},${beamY+arrowDirection*10} Z`)
                .attr('fill', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e');
            }
          }
          
          // Draw the load magnitude label
          loadGroup.append('text')
            .attr('x', loadX + loadLength / 2)
            .attr('y', beamY + arrowDirection * (arrowLength + 15))
            .attr('text-anchor', 'middle')
            .attr('fill', selectedElement?.id === load.id ? '#3182ce' : '#e53e3e')
            .attr('font-size', '12px')
            .text(`${Math.abs(load.magnitude)} kN/m`);
        }
      });
    }
    
  }, [svgRef, showGrid, activeBeam, selectedElement, onElementSelect, setSelectedElement]);
  
  const handleToolClick = (tool) => {
    setActiveTool(tool);
  };
  
  const handleZoomIn = () => {
    setZoom(prev => Math.min(prev + 0.1, 2));
  };
  
  const handleZoomOut = () => {
    setZoom(prev => Math.max(prev - 0.1, 0.5));
  };
  
  const handleMouseDown = (e) => {
    if (activeTool === 'pan') {
      setIsDragging(true);
      setDragStart({ x: e.clientX, y: e.clientY });
    }
  };
  
  const handleMouseMove = (e) => {
    if (isDragging && activeTool === 'pan') {
      const dx = e.clientX - dragStart.x;
      const dy = e.clientY - dragStart.y;
      
      setViewBox(prev => ({
        ...prev,
        x: prev.x - dx / zoom,
        y: prev.y - dy / zoom
      }));
      
      setDragStart({ x: e.clientX, y: e.clientY });
    }
  };
  
  const handleMouseUp = () => {
    setIsDragging(false);
  };
  
  return (
    <div className="h-full flex flex-col">
      {/* Toolbar */}
      <div className="bg-white shadow-sm p-2 flex items-center space-x-2">
        <button 
          className={`toolbar-btn ${activeTool === 'select' ? 'active' : ''}`}
          onClick={() => handleToolClick('select')}
          title="Select Tool"
        >
          <FaMousePointer />
        </button>
        <button 
          className={`toolbar-btn ${activeTool === 'beam' ? 'active' : ''}`}
          onClick={() => handleToolClick('beam')}
          title="Beam Tool"
        >
          <FaRuler />
        </button>
        <button 
          className={`toolbar-btn ${activeTool === 'load' ? 'active' : ''}`}
          onClick={() => handleToolClick('load')}
          title="Load Tool"
        >
          <FaWeight />
        </button>
        <button 
          className={`toolbar-btn ${activeTool === 'pan' ? 'active' : ''}`}
          onClick={() => handleToolClick('pan')}
          title="Pan Tool"
        >
          <FaHandPaper />
        </button>
        <button 
          className={`toolbar-btn ${activeTool === 'erase' ? 'active' : ''}`}
          onClick={() => handleToolClick('erase')}
          title="Erase Tool"
        >
          <FaEraser />
        </button>
        
        <div className="h-6 border-l border-gray-300 mx-1"></div>
        
        <button 
          className="toolbar-btn"
          onClick={handleZoomIn}
          title="Zoom In"
        >
          <FaPlus />
        </button>
        <button 
          className="toolbar-btn"
          onClick={handleZoomOut}
          title="Zoom Out"
        >
          <FaMinus />
        </button>
        <button 
          className="toolbar-btn"
          onClick={() => setZoom(1)}
          title="Reset Zoom"
        >
          <FaExpand />
        </button>
        
        <div className="h-6 border-l border-gray-300 mx-1"></div>
        
        <button 
          className={`toolbar-btn ${showGrid ? 'active' : ''}`}
          onClick={() => setShowGrid(!showGrid)}
          title="Toggle Grid"
        >
          <FaBorderAll />
        </button>
      </div>
      
      {/* Canvas */}
      <div className="flex-1 overflow-hidden bg-white">
        <svg 
          ref={svgRef} 
          width="100%" 
          height="100%" 
          style={{ 
            transform: `scale(${zoom})`,
            cursor: activeTool === 'pan' ? 'grab' : 
                    activeTool === 'select' ? 'pointer' : 
                    activeTool === 'beam' ? 'crosshair' : 
                    activeTool === 'load' ? 'crosshair' : 
                    activeTool === 'erase' ? 'no-drop' : 'default'
          }}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
        ></svg>
      </div>
    </div>
  );
};

export default Canvas;
