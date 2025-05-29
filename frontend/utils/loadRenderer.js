const d3 = require('d3');

class LoadRenderer {
    constructor(svgContainer) {
        this.svg = svgContainer;
        this.MAX_HEIGHT = 60; // Maximum height in pixels
        this.TOP_OFFSET = 20; // Fixed distance from beam to loads
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
     * Update all loads to maintain relative scaling within their type
     * @private
     */
    updateAllLoads() {
        this.loads.forEach((loadData, loadGroup) => {
            const height = this.calculateHeight(loadData.magnitude, loadData.type);
            const d3Group = d3.select(loadGroup);
            
            if (loadData.type === 'udl') {
                d3Group.select('rect')
                    .transition()
                    .duration(300)
                    .attr('y', -height - this.TOP_OFFSET)
                    .attr('height', height);

                d3Group.select('text')
                    .transition()
                    .duration(300)
                    .attr('y', -height - this.TOP_OFFSET - 5);
            } else { // point load
                d3Group.select('line')
                    .transition()
                    .duration(300)
                    .attr('y1', -this.TOP_OFFSET)
                    .attr('y2', -height - this.TOP_OFFSET);

                d3Group.select('text')
                    .transition()
                    .duration(300)
                    .attr('y', -height - this.TOP_OFFSET - 5);
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
            .attr('y', -height - this.TOP_OFFSET)
            .attr('width', endX - startX)
            .attr('height', height)
            .attr('fill', 'none')
            .attr('stroke', color)
            .attr('stroke-width', 2);

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', startX + (endX - startX) / 2)
            .attr('y', -height - this.TOP_OFFSET - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .text(`${magnitude} k/ft`);

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
            .attr('y1', -this.TOP_OFFSET)
            .attr('x2', x)
            .attr('y2', -height - this.TOP_OFFSET)
            .attr('stroke', color)
            .attr('stroke-width', 2);

        // Add magnitude label
        loadGroup.append('text')
            .attr('x', x)
            .attr('y', -height - this.TOP_OFFSET - 5)
            .attr('text-anchor', 'middle')
            .attr('font-size', '12px')
            .text(`${magnitude} k`);

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
                ? `${newMagnitude} k/ft` 
                : `${newMagnitude} k`);
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