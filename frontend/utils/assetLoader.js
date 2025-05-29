const path = require('path');
const fs = require('fs');

class AssetLoader {
    constructor() {
        // In production, assets are in resources/assets
        // In development, they're in the assets directory
        this.assetPath = process.env.NODE_ENV === 'production'
            ? path.join(process.resourcesPath, 'assets')
            : path.join(__dirname, '..', '..', 'assets');
    }

    /**
     * Load an SVG file and return its contents
     * @param {string} name - Name of the SVG file (e.g., 'pinned-support')
     * @returns {string} SVG contents
     */
    loadSVG(name) {
        const filePath = path.join(this.assetPath, `${name}.svg`);
        try {
            return fs.readFileSync(filePath, 'utf8');
        } catch (error) {
            console.error(`Failed to load SVG: ${name}`, error);
            return null;
        }
    }

    /**
     * Get the full path to an asset
     * @param {string} name - Name of the asset file
     * @returns {string} Full path to the asset
     */
    getAssetPath(name) {
        return path.join(this.assetPath, name);
    }
}

module.exports = new AssetLoader(); 