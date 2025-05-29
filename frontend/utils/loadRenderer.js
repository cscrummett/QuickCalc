const d3 = require('d3');

class LoadRenderer {
    constructor(svgContainer) {
        this.svg = svgContainer;
        this.MAX_HEIGHT = 60; // Maximum height in pixels
        this.TOP_OFFSET = 10; // Fixed distance from beam to loads
        this.ARROW_SIZE = 8; // Size of arrowhead
        this.ARROW_OVERLAP = 3; // How far the line extends into the arrow
        this.SCALE_FACTOR = 10; // Pixels per unit of magnitude
        this.loads = new Map(); // Store all loads
        this.gradientCounter = 0; // For unique gradient IDs

        // Ensure defs exists or create it
        let defs = this.svg.select('defs');
        if (!defs.node()) {
            defs = this.svg.append('defs');
        }
        this.defs = defs;

        // Draw beam line
        this.svg.append('line')
            .attr('class', 'beam-line')
            .attr('x1', 50)
            .attr('y1', 150)  // Fixed beam position
            .attr('x2', 750)
            .attr('y2', 150)  // Fixed beam position
            .attr('stroke', '#666')
            .attr('stroke-width', 2);
    }

    /**
     * Create a gradient for a specific color
     * @private
     */
    createColorGradient(color) {
        const gradientId = `load-gradient-${this.gradientCounter++}`;
        
        // Remove any existing gradient with this ID
        this.defs.select(`#${gradientId}`).remove();
        
        // Create new gradient with explicit attributes
        const gradient = this.defs.append('linearGradient')
            .attr('id', gradientId)
            .attr('x1', '0%')
            .attr('y1', '0%')
            .attr('x2', '0%')
            .attr('y2', '100%')
            .attr('gradientUnits', 'userSpaceOnUse');
            
        // Parse color and create stops with explicit opacity
        const rgb = d3.color(color);
        gradient.append('stop')
            .attr('offset', '0%')
            .attr('stop-color', rgb.toString())
            .attr('stop-opacity', '0.3');
            
        gradient.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', rgb.toString())
            .attr('stop-opacity', '0.8');

        // Return the complete URL reference
        return `url(#${gradientId})`;
    }

    /**
     * Get the current maximum magnitude for a specific load type
     * @private
     */
    getMaxMagnitude(type) {
        const loadsOfType = Array.from(this.loads.values())
            .filter(load => load.type === type);
        
        if (loadsOfType.length === 0) return 1;
        return Math.max(...loadsOfType.map(load => Math.abs(load.magnitude)));
    }

    /**
     * Calculate height based on magnitude and current maximum for the load type
     * @private
     */
    calculateHeight(magnitude) {
        return Math.min(Math.abs(magnitude) * this.SCALE_FACTOR, this.MAX_HEIGHT);
    }

    /**
     * Create an arrowhead path string
     * @private
     */
    createArrowPath(x, y, size) {
        // Arrow points downward at the bottom of the line
        // Width reduced to ~2/3, centered (±0.67 instead of ±1)
        const halfWidth = size * 0.67;
        return `M ${x} ${y} l ${-halfWidth} ${-size} l ${halfWidth * 2} 0 Z`;
    }

    /**
     * Format a number to one decimal place
     * @private
     */
    formatMagnitude(value) {
        return value.toFixed(1);
    }

    /**
     * Update all loads to maintain relative scaling within their type
     * @private
     */
    updateAllLoads() {
        this.loads.forEach((loadData, loadGroup) => {
            const height = this.calculateHeight(loadData.magnitude);
            const d3Group = d3.select(loadGroup);
            
            if (loadData.type === 'udl') {
                const y = 150 - height - this.TOP_OFFSET;  // Adjusted for beam position
                const rect = d3Group.select('rect');
                const width = parseFloat(rect.attr('width'));
                const startX = parseFloat(rect.attr('x'));
                const endX = startX + width;

                // Create new gradient for the update
                const gradientFill = this.createColorGradient(loadData.color || 'black');

                // Update rectangle with new gradient
                rect.transition()
                    .duration(300)
                    .attr('y', y)
                    .attr('height', height)
                    .attr('fill', gradientFill);

                // Update arrows
                const arrows = d3Group.selectAll('path');
                arrows.nodes().forEach((arrow, i) => {
                    const x = i === 0 ? startX : endX;
                    d3.select(arrow)
                        .transition()
                        .duration(300)
                        .attr('d', this.createArrowPath(x, 150 - this.TOP_OFFSET, this.ARROW_SIZE));
                });

                // Update text
                d3Group.select('text')
                    .transition()
                    .duration(300)
                    .attr('y', y - 5)
                    .text(`${this.formatMagnitude(loadData.magnitude)} k/ft`);
            } else { // point load
                const y2 = 150 - height - this.TOP_OFFSET;  // Adjusted for beam position
                const y1 = 150 - this.TOP_OFFSET - this.ARROW_OVERLAP;
                
                d3Group.select('line')
                    .transition()
                    .duration(300)
                    .attr('y1', y1)
                    .attr('y2', y2);

                d3Group.select('path')
                    .transition()
                    .duration(300)
                    .attr('d', this.createArrowPath(parseFloat(d3Group.select('line').attr('x1')), 150 - this.TOP_OFFSET, this.ARROW_SIZE));

                d3Group.select('text')
                    .transition()
                    .duration(300)
                    .attr('y', y2 - 5)
                    .text(`${this.formatMagnitude(loadData.magnitude)} k`);
            }
        });
    }

    /**
     * Render a uniformly distributed load
     * @param {Object} params
     * @param {number} params.startX - Start position on beam
     * @param {number} params.endX - End position on beam
     * @param {number} params.magnitude - Load magnitude (determines height)
     * @param {string} params.color - Load color (default: black)
     */
    renderUDL(params) {
        const {
            startX,
            endX,
            magnitude,
            color = 'black'
        } = params;

        const height = this.calculateHeight(magnitude);
        const y = 150 - height - this.TOP_OFFSET;  // Adjusted for beam position
        
        const gradientFill = this.createColorGradient(color);
        
        const loadGroup = this.svg.append('g')
            .attr('class', 'udl-load');

        this.loads.set(loadGroup.node(), {
            type: 'udl',
            magnitude: magnitude,
            color: color
        });

        // Main rectangle for UDL with gradient fill
        loadGroup.append('rect')
            .attr('x', startX)
            .attr('y', y)
            .attr('width', endX - startX)
            .attr('height', height)
            .attr('fill', gradientFill)
            .attr('stroke', color)
            .attr('stroke-width', 1)
            .attr('stroke-opacity', 0.7);

        // Left arrow
        loadGroup.append('path')
            .attr('d', this.createArrowPath(startX, 150 - this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Right arrow
        loadGroup.append('path')
            .attr('d', this.createArrowPath(endX, 150 - this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', startX + (endX - startX) / 2)
            .attr('y', y - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .attr('fill', color)
            .text(`${this.formatMagnitude(magnitude)} k/ft`);

        return loadGroup;
    }

    /**
     * Render a point load
     * @param {Object} params
     * @param {number} params.x - Position on beam
     * @param {number} params.magnitude - Load magnitude
     * @param {string} params.color - Load color
     */
    renderPointLoad(params) {
        const {
            x,
            magnitude,
            color = 'black'
        } = params;

        const height = this.calculateHeight(magnitude);
        const y2 = 150 - height - this.TOP_OFFSET;  // Adjusted for beam position
        const y1 = 150 - this.TOP_OFFSET - this.ARROW_OVERLAP;

        const loadGroup = this.svg.append('g')
            .attr('class', 'point-load');

        this.loads.set(loadGroup.node(), {
            type: 'point',
            magnitude: magnitude,
            color: color
        });

        // Vertical line
        loadGroup.append('line')
            .attr('x1', x)
            .attr('y1', y1)
            .attr('x2', x)
            .attr('y2', y2)
            .attr('stroke', color)
            .attr('stroke-width', 2);

        // Arrowhead at bottom of line
        loadGroup.append('path')
            .attr('d', this.createArrowPath(x, 150 - this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', x)
            .attr('y', y2 - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .attr('fill', color)
            .text(`${this.formatMagnitude(magnitude)} k`);

        return loadGroup;
    }

    /**
     * Update an existing load's magnitude
     * @param {Object} loadGroup - D3 selection of the load group
     * @param {number} newMagnitude - New load magnitude
     */
    updateMagnitude(loadGroup, newMagnitude) {
        // Update stored magnitude
        const loadData = this.loads.get(loadGroup.node());
        loadData.magnitude = newMagnitude;
        
        // Update all loads to maintain relative scaling
        this.updateAllLoads();

        // Update label text
        loadGroup.select('text')
            .text(loadData.type === 'udl' 
                ? `${this.formatMagnitude(newMagnitude)} k/ft` 
                : `${this.formatMagnitude(newMagnitude)} k`);
    }

    /**
     * Remove a load
     * @param {Object} loadGroup - D3 selection of the load group to remove
     */
    removeLoad(loadGroup) {
        this.loads.delete(loadGroup.node());
        loadGroup.remove();
        this.updateAllLoads();
    }
}

module.exports = LoadRenderer; 