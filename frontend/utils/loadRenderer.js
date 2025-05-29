const d3 = require('d3');

class LoadRenderer {
    constructor(svgContainer) {
        this.svg = svgContainer;
        this.MAX_HEIGHT = 60; // Maximum height in pixels
        this.TOP_OFFSET = 10; // Fixed distance from beam to loads (reduced from 20)
        this.ARROW_SIZE = 8; // Size of arrowhead
        this.ARROW_OVERLAP = 3; // How far the line extends into the arrow
        this.loads = new Map(); // Store all loads to track max magnitude
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
    calculateHeight(magnitude, type) {
        const maxMagnitude = this.getMaxMagnitude(type);
        return (Math.abs(magnitude) / maxMagnitude) * this.MAX_HEIGHT;
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
            const height = this.calculateHeight(loadData.magnitude, loadData.type);
            const d3Group = d3.select(loadGroup);
            
            if (loadData.type === 'udl') {
                const y = -height - this.TOP_OFFSET;
                const rect = d3Group.select('rect');
                const width = parseFloat(rect.attr('width'));
                const startX = parseFloat(rect.attr('x'));
                const endX = startX + width;

                rect.transition()
                    .duration(300)
                    .attr('y', y)
                    .attr('height', height);

                // Update arrows
                const arrows = d3Group.selectAll('path');
                arrows.nodes().forEach((arrow, i) => {
                    const x = i === 0 ? startX : endX;
                    d3.select(arrow)
                        .transition()
                        .duration(300)
                        .attr('d', d => this.createArrowPath(x, -this.TOP_OFFSET, this.ARROW_SIZE));
                });

                d3Group.select('text')
                    .transition()
                    .duration(300)
                    .attr('y', y - 5)
                    .text(`${this.formatMagnitude(loadData.magnitude)} k/ft`);
            } else { // point load
                const y2 = -height - this.TOP_OFFSET;
                const y1 = -this.TOP_OFFSET - this.ARROW_OVERLAP;
                
                d3Group.select('line')
                    .transition()
                    .duration(300)
                    .attr('y1', y1)
                    .attr('y2', y2);

                d3Group.select('path')
                    .transition()
                    .duration(300)
                    .attr('d', d => this.createArrowPath(d.x, -this.TOP_OFFSET, this.ARROW_SIZE))
                    .attr('fill', d => d.color);

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

        // Calculate height relative to current maximum for UDLs
        const height = this.calculateHeight(magnitude, 'udl');
        const y = -height - this.TOP_OFFSET;
        
        // Create load group
        const loadGroup = this.svg.append('g')
            .attr('class', 'udl-load');

        // Store load data
        this.loads.set(loadGroup.node(), {
            type: 'udl',
            magnitude: magnitude
        });

        // Main rectangle for UDL
        loadGroup.append('rect')
            .attr('x', startX)
            .attr('y', y)
            .attr('width', endX - startX)
            .attr('height', height)
            .attr('fill', 'none')
            .attr('stroke', color)
            .attr('stroke-width', 2);

        // Left arrow
        loadGroup.append('path')
            .datum({ x: startX, color: color })
            .attr('d', d => this.createArrowPath(d.x, -this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Right arrow
        loadGroup.append('path')
            .datum({ x: endX, color: color })
            .attr('d', d => this.createArrowPath(d.x, -this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', startX + (endX - startX) / 2)
            .attr('y', y - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .text(`${this.formatMagnitude(magnitude)} k/ft`);

        // Update all loads to maintain relative scaling
        this.updateAllLoads();

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

        const height = this.calculateHeight(magnitude, 'point');
        const y2 = -height - this.TOP_OFFSET;
        const y1 = -this.TOP_OFFSET - this.ARROW_OVERLAP; // Extend line into arrow

        const loadGroup = this.svg.append('g')
            .attr('class', 'point-load');

        // Store load data
        this.loads.set(loadGroup.node(), {
            type: 'point',
            magnitude: magnitude
        });

        // Vertical line
        loadGroup.append('line')
            .attr('x1', x)
            .attr('y1', y1)
            .attr('x2', x)
            .attr('y2', y2)
            .attr('stroke', color)
            .attr('stroke-width', 2);

        // Arrowhead at bottom of line (near beam)
        loadGroup.append('path')
            .datum({ x: x, color: color })
            .attr('d', d => this.createArrowPath(d.x, -this.TOP_OFFSET, this.ARROW_SIZE))
            .attr('fill', color)
            .attr('stroke', 'none');

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', x)
            .attr('y', y2 - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .text(`${this.formatMagnitude(magnitude)} k`);

        // Update all loads to maintain relative scaling
        this.updateAllLoads();

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