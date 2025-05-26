// Initialize D3.js beam visualization
const beamCanvas = d3.select('#beam-canvas')
    .append('svg')
    .attr('width', '100%')
    .attr('height', '100%');

// Add grid background (can be toggled)
const gridSize = 20;
const grid = beamCanvas.append('g')
    .attr('class', 'grid')
    .style('opacity', 0.1);

// Create grid lines
function createGrid() {
    const width = beamCanvas.node().getBoundingClientRect().width;
    const height = beamCanvas.node().getBoundingClientRect().height;

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

// Initial grid creation
createGrid();

// Handle window resize
window.addEventListener('resize', () => {
    grid.selectAll('*').remove();
    createGrid();
}); 