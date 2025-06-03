# Product Requirements Document (PRD): QuickCalc

## 1. Introduction

### 1.1 Product Overview
**QuickCalc** is a cross-platform desktop application designed for professional structural engineers to quickly analyze and design single and multi-span beams. It addresses the slow setup times of complex finite element software (e.g., SAP2000, RISA) by offering a streamlined workflow, accurate finite element analysis (FEA), and compliance with American design codes (AISC, ASCE, NDS) or user-defined criteria. The interface is modern, trustworthy, and visually engaging, avoiding the dull aesthetics of traditional engineering tools, with a toggleable light grey/tan grid background and vibrant, gradient-filled diagrams.

### 1.2 Purpose
This PRD outlines the functional and non-functional requirements, visual design, technical architecture, and development phases for QuickCalc, ensuring a clear roadmap for creating a fast, reliable, and visually appealing tool for structural engineers.

### 1.3 Target Audience
- **Primary Users**: Professional structural engineers seeking rapid beam analysis and design.
- **Use Case**: Quick calculations of forces (reactions, shear, moments) and beam sizing for project planning, preliminary design, or verification.

### 1.4 Success Metrics
- **Usability**: Setup and analysis completed in <1 minute for a typical beam.
- **Performance**: FEA completes in <1 second for a 3-span beam with 5 loads.
- **Accuracy**: Results within 1% of industry standards (e.g., Enercalc, manual calculations).
- **Adoption**: Positive feedback from engineers on GitHub/Eng-Tips forums.

## 2. Product Scope

### 2.1 In-Scope
- Finite element analysis for single and multi-span beams (up to 5 spans).
- Compliance with AISC 360, ASCE 7, NDS, or custom design criteria.
- Load combinations: None (1.0 factor, default), ASCE 7, or custom.
- Project-based file saving (multiple beams per project).
- Modern, trustworthy UI with vibrant 2D diagrams, light grey/tan grid background (toggleable).
- PDF/CSV report export with embedded diagrams.
- Open-source (MIT license, GitHub-hosted).

### 2.2 Out-of-Scope
- Integration with other tools (e.g., AutoCAD, Revit).
- Advanced analysis (e.g., dynamic loading, seismic).
- 3D visualizations or animations beyond subtle micro-animations.
- Multi-language support (English only).
- Cloud storage or server-based features.

## 3. Functional Requirements

### 3.1 Beam Configuration
- **Description**: Users can define single or multi-span beams (up to 5 spans).
- **Inputs**:
  - Span lengths (per span, in feet or meters).
  - Cross-sections: AISC steel (e.g., W, C), NDS timber, or custom (moment of inertia, area).
  - Materials: Steel (A36), timber, or custom (Young’s modulus, yield strength).
  - Supports: Pinned, fixed, roller (drag-and-drop).
- **UI**: Dropdowns for sections/materials, text inputs for lengths, real-time beam preview with minimalist vector icons.
- **Validation**: Prevent invalid inputs (e.g., negative lengths).

### 3.2 Load Input
- **Description**: Users can apply multiple load types and define load combinations.
- **Load Types**: Point loads, uniformly distributed loads (UDLs), triangular loads, moments.
- **Inputs**: Magnitude, position (span or global coordinates), direction.
- **Load Combinations**:
  - **Default**: No combinations (1.0 factor for all loads).
  - **Options**: ASCE 7 (e.g., 1.2D + 1.6L) or custom (user-defined factors, e.g., 1.5D + 0.8W).
  - Toggle in settings to enable/disable combinations.
- **UI**: Click-to-place loads (glowing dots for point loads, gradient bars for UDLs), quick templates (e.g., midspan point load).
- **Validation**: Check for realistic load values.

### 3.3 Analysis Engine (Finite Element Analysis)
- **Description**: Compute forces and deflections for single/multi-span beams using FEA.
- **Outputs**:
  - Support reactions (forces, moments).
  - Shear force diagrams.
  - Bending moment diagrams.
  - Deflections and slopes.
- **Checks**: Yielding, shear, buckling per AISC/NDS.
- **Performance**: <1 second for 3-span beam with 5 loads.
- **Accuracy**: Within 1% of Enercalc or manual calculations.

### 3.4 Design Module
- **Description**: Recommend optimal beam sizes based on analysis and design criteria.
- **Features**:
  - Sections: AISC steel, NDS timber, or custom.
  - Compliance: AISC 360, ASCE 7, NDS, or custom (e.g., L/480 deflection).
  - Option to ignore codes.
  - Optimization: Minimize weight or cost while meeting criteria.
- **UI**: Card-based recommendations (e.g., “W10x12: Pass, L/400”).

### 3.5 Visualization
- **Description**: Display 2D diagrams for beam geometry, loads, and results.
- **Components**:
  - Beam sketch: Minimalist vector icons for supports (e.g., triangle for pinned) and loads (glowing dots, gradient bars).
  - Shear/moment/deflection diagrams: Gradient-filled curves (teal-to-red for intensity).
  - Background: Light grey/tan grid (blueprint-style, default on, toggleable).
- **Interactivity**: Hover for values, click for critical points (e.g., max moment).
- **Micro-Animations**: Slide-in for diagrams, fade-in for inputs.
- **Style**: Modern, inspired by Figma/Tableau, with clean lines and vibrant gradients.

### 3.6 Reporting
- **Description**: Generate professional reports with inputs, results, and diagrams.
- **Outputs**:
  - Inputs: Beam geometry, loads, materials, load combinations.
  - Results: Reactions, max shear/moment/deflection.
  - Design: Recommended sections, compliance status.
  - Diagrams: Gradient-filled, embedded in reports.
- **Formats**: PDF (primary), CSV (numerical data).
- **UI**: Export button with report preview.

### 3.7 Data Management
- **Description**: Save and manage projects with multiple beams.
- **Features**:
  - **Project-Based Saving**: Each project (JSON file) contains multiple beams, accessible via sidebar.
  - Libraries: AISC steel, NDS timber, user-defined sections/materials (JSON).
- **UI**: Sidebar for project/beam navigation, dropdowns for library access.

### 3.8 Settings
- **Description**: Customize app behavior.
- **Options**:
  - Load Combinations: None (default), ASCE 7, or custom.
  - Background Grid: Light grey/tan grid on/off (default on).
  - Units: Imperial (default) or SI.
- **UI**: Settings menu in toolbar.

### 3.9 Additional Features
- **Input Validation**: Real-time alerts (e.g., “Invalid length”) with red highlights.
- **Undo/Redo**: For input changes, with smooth feedback.
- **Help System**: Tooltips, searchable menu with AISC/ASCE/NDS references.
- **Quick Templates**: Predefined setups (e.g., simply supported beam with UDL).

## 4. Non-Functional Requirements

- **Performance**: FEA completes in <1 second for typical cases (3 spans, 5 loads).
- **Usability**: Setup and analysis in <1 minute, with intuitive drag-and-drop.
- **Reliability**: FEA results within 1% of industry standards (Enercalc, manual).
- **Visual Appeal**: Modern, trustworthy, vibrant diagrams, inspired by Figma/Tableau.
- **Open-Source**: MIT license, hosted on GitHub for community contributions.
- **Platform**: Cross-platform (Windows, macOS, Linux).
- **Language**: English only.

## 5. Visual Design

### 5.1 Design Principles
- **Trustworthy**: Clean typography, precise alignments, consistent colors.
- **Modern**: Flat design, subtle gradients, soft shadows, micro-animations.
- **Visually Engaging**: Vibrant diagrams, intuitive layouts, avoiding “boring” engineering software aesthetics.
- **Clarity**: Legible, uncluttered interface for quick tasks.

### 5.2 Visual Elements
- **Typography**: Sans-serif (Inter or Roboto) for clarity.
- **Color Scheme**:
  - Neutral: Dark gray (`#333`), off-white (`#F5F5F5`).
  - Accents: Teal (`#26A69A`), deep blue (`#1976D3`), muted red (`#EF5350`).
  - Gradients: Teal-to-red for diagrams (heatmap-style).
- **Background**: Light grey/tan grid (toggleable, default on).
- **Diagrams**:
  - Beam/supports: Minimalist vector icons (e.g., triangle for pinned).
  - Loads: Glowing dots (point loads), gradient bars (UDLs).
  - Force diagrams: Smooth curves with gradient fills, hover effects.
- **Micro-Animations**: Slide-in for diagrams, fade-in for inputs.
- **Inspiration**: Figma’s clean layouts, Tableau’s vibrant charts, blueprint aesthetics.

### 5.3 Trustworthiness Cues
- Precise diagram scaling (e.g., proportional deflections).
- Consistent color coding (e.g., shear: teal, moment: blue).
- Transparent assumptions in reports (e.g., “FEA: 20 elements/span”).
- Professional report formatting with clean tables and diagrams.

## 6. Technical Architecture

### 6.1 Technology Stack
- **Backend**: Python
  - **Libraries**:
    - **NumPy/SciPy**: FEA (stiffness matrices, sparse solvers).
    - **Pandas**: AISC/NDS libraries.
    - **ReportLab**: PDF reports.
- **Frontend**: JavaScript with Electron
  - **Framework**: Electron for cross-platform desktop.
  - **Libraries**:
    - **D3.js**: Gradient-filled 2D diagrams.
    - **Tailwind CSS**: Modern, clean UI styling.
    - **Anime.js**: Micro-animations (e.g., slide-in).
- **Storage**: JSON for projects (multi-beam) and libraries.
- **Build**: PyInstaller (Python), Electron Builder (JS).
- **Testing**: Pytest (backend), Jest (frontend).

### 6.2 System Components
- **Frontend (Electron/JS)**:
  - Input panel: Tailwind-styled forms for beams, supports, loads.
  - Project sidebar: List projects and beams for quick access.
  - Visualization area: D3.js diagrams with grid background.
  - Results panel: Card-based results/recommendations.
  - Settings: Toggles for grid, load combinations, units.
- **Backend (Python)**:
  - FEA engine: Solve multi-span beams (displacements, forces).
  - Design module: Check AISC/ASCE/NDS or custom criteria.
  - Data handler: Manage JSON projects (multiple beams).
- **Integration**: Python APIs (Flask/Pyodide) for frontend-backend communication.
- **Visualization Engine**: D3.js for SVG diagrams, Tailwind for grid, Anime.js for animations.

### 6.3 Data Flow
1. User selects/creates project in sidebar, adds beam via forms.
2. JS validates inputs, sends JSON to Python backend.
3. Backend runs FEA (with selected load combination), returns results.
4. Frontend renders gradient-filled diagrams and results.
5. Project saved as JSON, containing all beams.
6. User exports report or switches beams within project.

## 7. User Interface Mockup

### 7.1 Main Window Layout
- **Left Sidebar (15%)**:
  - Project list (e.g., “Bridge A,” “Building B”).
  - Beam list per project (e.g., “Beam 1: 10 ft”).
  - Buttons: New Project, New Beam, Save.
- **Central Panel (50%)**:
  - Input forms: Spans, sections (AISC/NDS dropdowns), materials, supports (drag-and-drop).
  - Loads: Click-to-place glowing dots (point loads), gradient bars (UDLs).
- **Right Panel (35%)**:
  - Visualization: Beam sketch, shear/moment/deflection diagrams (gradient-filled, teal-to-red).
  - Background: Light grey/tan grid (toggleable).
  - Results: Cards with reactions, max values, design recommendations.
- **Toolbar**: Analyze, Export (PDF/CSV), Settings (grid, load combos, units).

### 7.2 Example Workflow
1. User creates “Project: Warehouse,” adds “Beam 1: 20 ft.”
2. Selects W8x10 (A36 steel), drags pinned supports, places 10-kip point load.
3. Sets load combination to “None” (1.0 factor) in settings.
4. Clicks “Analyze”; diagrams slide in with gradients, grid background.
5. Results show “Max Moment: 50 kip-ft”; design suggests W10x12 for L/480.
6. Adds “Beam 2: 3-Span” to project, saves, exports PDF.

## 8. Development Plan (Solo with Cursor AI)

### 8.1 Phases and Order of Operations
1. **Phase 1: Setup and Prototype**
   - Set up Python (NumPy, SciPy, ReportLab) and Electron (D3.js, Tailwind, Anime.js).
   - Use Cursor AI to scaffold project (backend APIs, frontend layout).
   - Develop FEA prototype for single-span beam with point load (1.0 factor).
   - Create minimalist SVG icons for supports/loads (Inkscape or open-source).
   - Implement basic project-based JSON saving (single beam).
   - Test FEA against manual calculations.
2. **Phase 2: Core Development**
   - Extend FEA to multi-span beams (10–20 elements/span).
   - Build UI: Sidebar for projects/beams, input forms, basic diagrams (D3.js).
   - Add load combination logic (none, ASCE 7, custom).
   - Integrate AISC/NDS JSON libraries.
   - Enable multi-beam storage per project.
   - Use Cursor AI to debug FEA and UI interactions.
3. **Phase 3: Visuals and Design**
   - Add gradient-filled diagrams and light grey/tan grid (toggleable).
   - Implement micro-animations (Anime.js, e.g., slide-in).
   - Develop design module for AISC/ASCE/NDS and custom criteria.
   - Add settings for grid, load combinations, units.
   - Test usability with multi-span problems.
4. **Phase 4: Reporting and Polish**
   - Implement PDF/CSV export (ReportLab).
   - Polish UI for <1-minute setup (e.g., shortcuts, defaults).
   - Optimize FEA (<1 second for 3 spans).
   - Finalize project-based saving (multiple beams).
   - Package with PyInstaller/Electron Builder.
   - Publish to GitHub with MIT license.

### 8.2 Tools and Resources
- **IDE**: Cursor AI for coding, debugging, autocompletion.
- **Version Control**: Git/GitHub.
- **Assets**: Inkscape for SVGs or open-source libraries (e.g., Heroicons).
- **Testing**: Pytest (FEA), Jest (UI), manual testing for visuals.
- **Documentation**: Markdown user manual, docstrings for code.

### 8.3 Solo Development Tips
- **Cursor AI**: Prompt for FEA (e.g., “Python beam FEA with load combinations”), UI (e.g., “Tailwind sidebar for project navigation”), JSON handling.
- **Modular Code**: Separate FEA, UI, visualization, data management.
- **Visuals**: Start with simple SVGs, add gradients/animations later.
- **Testing**: Validate FEA with Enercalc or manual solutions.
- **Community**: Share beta on GitHub/Eng-Tips for feedback.

## 9. Risks and Mitigation
- **Risk**: FEA accuracy errors.
  - **Mitigation**: Validate against Enercalc and manual calculations.
- **Risk**: UI complexity slows setup.
  - **Mitigation**: Prioritize drag-and-drop, templates, and defaults.
- **Risk**: Visuals distract from functionality.
  - **Mitigation**: Keep diagrams clean, animations subtle, grid toggleable.
- **Risk**: Solo development scope creep.
  - **Mitigation**: Use Cursor AI for rapid prototyping, focus on core features first.

## 10. Next Steps
- **Start Coding**: Use existing FEA prototype (single-span beam) and extend for multi-span.
- **UI Mockup**: Develop HTML/CSS mockup with Tailwind for sidebar, forms, and diagrams.
- **Assets**: Create/source minimalist SVGs for supports/loads.
- **Cursor AI**: Use prompts for FEA, UI, and JSON handling (e.g., “Add ASCE 7 load combinations to Python FEA”).
- **Community**: Plan GitHub repository setup with CONTRIBUTING.md for open-source contributions.