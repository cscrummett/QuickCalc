# QuickCalc SVG Assets

This directory contains the SVG assets used in the QuickCalc application.

## Support Types
- `pinned-support.svg` - Triangle symbol for pinned supports
- `fixed-support.svg` - Rectangle symbol for fixed supports
- `roller-support.svg` - Circle symbol for roller supports

## Load Types
- `point-load.svg` - Dot symbol for point loads
- `udl.svg` - Bar symbol for uniformly distributed loads

## Usage Guidelines
1. All SVGs should be minimal and professional in design
2. Use a consistent stroke width (recommended: 2px)
3. Default colors should be black (#000000) to allow for dynamic coloring in the application
4. Maintain a consistent viewBox size for each category:
   - Supports: 24x24
   - Loads: 24x24

## Implementation Notes
These SVGs are used in the D3.js diagrams and UI elements. They can be dynamically colored and scaled as needed through the application's CSS and JavaScript. 