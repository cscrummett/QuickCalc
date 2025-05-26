# QuickCalc Development To-Do List

This to-do list provides a step-by-step guide for building **QuickCalc**, a cross-platform desktop application for structural engineers to analyze and design single and multi-span beams. The tasks are organized into the four development phases from the Product Requirements Document (PRD), focusing on a streamlined workflow, finite element analysis (FEA), AISC/ASCE/NDS compliance, project-based file saving, and a modern, trustworthy UI with a light grey/tan grid background. The list is designed for solo development using Cursor AI, with a Python backend and JavaScript/Electron frontend.

## Phase 1: Setup and Prototype

1. **Set Up Development Environment**

   - Install Python 3.10+ and required libraries: NumPy, SciPy, Pandas, ReportLab.
   - Install Node.js and Electron for frontend development.
   - Set up Cursor AI as the IDE with Python and JavaScript support.
   - Create a Git repository on GitHub with an MIT license.

2. **Scaffold Project Structure**

   - Create directories: `backend` (Python), `frontend` (Electron/JS), `assets` (SVGs).
   - Initialize Python project with `pyproject.toml` or `requirements.txt`.
   - Initialize Electron project with `package.json` (include D3.js, Tailwind CSS, Anime.js).
   - Use Cursor AI to generate boilerplate: “Scaffold Python backend with Flask API and Electron frontend with Tailwind.”

3. **Develop FEA Prototype (Single-Span Beam)**

   - Write Python code for FEA of a simply supported beam with a point load (1.0 factor).
   - Implement stiffness matrix assembly, displacement solver, and force calculations (reactions, shear, moment).
   - Use NumPy for matrix operations and SciPy for sparse solvers.
   - Test against manual calculations (e.g., 20-ft beam, 10-kip midspan load: max moment = 50 kip-ft).
   - Prompt Cursor AI: “Generate Python FEA for single-span beam with point load, using NumPy/SciPy.”

4. **Create Minimalist SVG Assets**

   - Use Inkscape to create SVGs for supports (pinned: triangle, fixed: rectangle, roller: circle) and loads (point: dot, UDL: bar).
   - Alternatively, source open-source SVGs from Heroicons or similar.
   - Store in `assets` directory.

5. **Implement Basic JSON Saving**

   - Create a JSON schema for a single beam (spans, section, material, loads, supports).
   - Write Python functions to save/load beam data to/from JSON.
   - Prompt Cursor AI: “Generate Python code to save beam data as JSON.”

6. **Test Prototype**

   - Run FEA for multiple test cases (e.g., different lengths, loads).
   - Validate results against Enercalc or manual calculations.
   - Save a test beam to JSON and verify loading.

## Phase 2: Core Development

 7. **Extend FEA to Multi-Span Beams**

    - Modify FEA code to handle multi-span beams (up to 5 spans, 10–20 elements/span).
    - Update stiffness matrix assembly for continuous beams with varying supports (pinned, fixed, roller).
    - Compute reactions, shear, moment, and deflection across all spans.
    - Prompt Cursor AI: “Extend Python FEA for multi-span beams with pinned/fixed supports.”

 8. **Implement Load Combinations**

    - Add logic for load combinations: none (1.0 factor, default), ASCE 7 (e.g., 1.2D + 1.6L), or custom (user-defined factors).
    - Store load combination settings in JSON (per beam).
    - Update FEA to apply combination factors to loads.
    - Prompt Cursor AI: “Add ASCE 7 and custom load combinations to Python FEA.”

 9. **Build Core UI**

    - Create Electron frontend with Tailwind CSS:
      - **Sidebar**: Project and beam list (e.g., “Project: Warehouse” → “Beam 1: 20 ft”).
      - **Input Panel**: Forms for spans, sections (AISC/NDS dropdowns), materials, supports (drag-and-drop).
      - **Load Input**: Click-to-place loads (point: glowing dot, UDL: gradient bar).
    - Use D3.js for basic beam sketch (no force diagrams yet).
    - Prompt Cursor AI: “Build Tailwind CSS UI with sidebar and drag-and-drop inputs for Electron.”

10. **Integrate AISC/NDS Libraries**

    - Create JSON files for AISC steel (e.g., W, C sections) and NDS timber sections (e.g., sawn lumber).
    - Write Python functions to load libraries and populate dropdowns.
    - Allow custom sections (user-defined moment of inertia, area).
    - Prompt Cursor AI: “Generate JSON parser for AISC steel sections in Python.”

11. **Implement Project-Based Saving**

    - Update JSON schema to store multiple beams per project.
    - Modify save/load functions to handle project files (e.g., `warehouse.json` with multiple beams).
    - Update sidebar to display project beams dynamically.
    - Prompt Cursor AI: “Modify Python JSON handler for multi-beam projects.”

12. **Test Core Functionality**

    - Test FEA for multi-span beams (e.g., 3 spans with UDL and point load).
    - Verify load combinations (none, ASCE 7, custom).
    - Test UI inputs and project saving/loading.
    - Debug with Cursor AI: “Debug multi-span FEA and JSON saving.”

## Phase 3: Visuals and Design

13. **Implement Gradient-Filled Diagrams**

    - Use D3.js to render shear, moment, and deflection diagrams with teal-to-red gradients.
    - Add light grey/tan grid background (blueprint-style).
    - Enable hover effects to show values (e.g., max moment).
    - Prompt Cursor AI: “Create D3.js diagrams with gradient fills for shear/moment.”

14. **Add Micro-Animations**

    - Use Anime.js for subtle transitions:
      - Slide-in for diagrams when analysis completes.
      - Fade-in for inputs when adding loads/supports.
    - Keep animations minimal to maintain professionalism.
    - Prompt Cursor AI: “Add Anime.js slide-in animations for D3.js diagrams.”

15. **Develop Design Module**

    - Implement checks for AISC 360, ASCE 7, NDS, or custom criteria (e.g., L/480 deflection).
    - Recommend optimal sections (minimize weight, ensure compliance).
    - Display results in Tailwind-styled cards (e.g., “W10x12: Pass, L/400”).
    - Prompt Cursor AI: “Write Python design module for AISC/NDS beam sizing.”

16. **Add Settings Menu**

    - Create settings in toolbar:
      - Load combinations: None (default), ASCE 7, custom.
      - Grid background: On (default), off.
      - Units: Imperial (default), SI.
    - Store settings in JSON (per project).
    - Prompt Cursor AI: “Build Tailwind settings menu for Electron.”

17. **Test Visuals and Design**

    - Test diagrams for accuracy and visual clarity (e.g., gradient scaling).
    - Verify settings (grid toggle, load combinations).
    - Test design module against AISC/NDS examples.
    - Debug with Cursor AI: “Debug D3.js diagrams and design module.”

## Phase 4: Reporting and Polish

18. **Implement Report Generation**

    - Use ReportLab to generate PDF reports:
      - Inputs: Beam geometry, loads, materials, combinations.
      - Results: Reactions, max shear/moment/deflection.
      - Design: Recommended sections, compliance.
      - Embed D3.js diagrams (export as SVG).
    - Add CSV export for numerical data.
    - Prompt Cursor AI: “Generate PDF reports with ReportLab and embedded SVG diagrams.”

19. **Polish UI**

    - Add keyboard shortcuts (e.g., Enter to analyze).
    - Implement undo/redo for inputs with smooth feedback.
    - Add tooltips and searchable help menu with AISC/ASCE/NDS references.
    - Optimize for &lt;1-minute setup (e.g., default values, templates).
    - Prompt Cursor AI: “Add keyboard shortcuts and tooltips to Tailwind UI.”

20. **Optimize FEA Performance**

    - Use SciPy’s sparse solvers for faster matrix operations.
    - Ensure &lt;1 second for 3-span beam with 5 loads.
    - Test performance with complex cases (e.g., 5 spans, multiple loads).
    - Prompt Cursor AI: “Optimize Python FEA with sparse solvers.”

21. **Finalize Project-Based Saving**

    - Ensure robust JSON saving/loading for projects with multiple beams.
    - Test edge cases (e.g., empty projects, large beam counts).
    - Prompt Cursor AI: “Debug JSON project saving for multiple beams.”

22. **Package and Release**

    - Package with PyInstaller (Python) and Electron Builder (JS) for Windows, macOS, Linux.
    - Create GitHub release with binaries and documentation.
    - Write `README.md` and `CONTRIBUTING.md` for open-source community.
    - Share beta on GitHub/Eng-Tips for feedback.
    - Prompt Cursor AI: “Generate GitHub README for QuickCalc.”

## Additional Notes

- **Cursor AI Usage**: Use prompts like “Generate Python FEA for multi-span beams,” “Build Tailwind sidebar,” or “Optimize D3.js diagrams” to accelerate development.
- **Testing**: Validate FEA against Enercalc or manual calculations at each phase.
- **Assets**: Prioritize minimalist SVGs; consider hiring a designer for polished icons if budget allows.
- **Community**: Engage Eng-Tips forum for beta testing and feedback.