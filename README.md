# QuickCalc

A desktop application for structural engineers to quickly analyze and design beams using JavaFX.

## Development Setup

This project uses JavaFX and requires manual compilation and running via a batch script or direct `javac`/`java` commands.

### Prerequisites

1.  **Java Development Kit (JDK) 17:**
    *   Ensure JDK 17 is installed. The `run_app.bat` script is currently configured for `C:\Program Files\Java\jdk-17`. If your JDK is installed elsewhere, you may need to update this path in `run_app.bat` or ensure `java` and `javac` from JDK 17 are in your system's PATH.
2.  **JavaFX SDK 17.0.2:**
    *   Download the JavaFX SDK version 17.0.2 for your operating system (e.g., from [GluonHQ](https://gluonhq.com/products/javafx/)).
    *   Extract the SDK into a directory named `javafx-sdk-17.0.2` directly within the project's root folder (i.e., at the same level as `run_app.bat` and the `src` directory).

### Project Structure

*   `src/main/java/`: Contains the main Java source code for the application.
    *   `com.quickcalc.Main`: The main entry point of the application.
    *   `com.quickcalc.controllers/`: JavaFX controllers.
    *   `com.quickcalc.models/`: Data models.
    *   `com.quickcalc.views.components/`: Custom UI components.
    *   `com.quickcalc.utils/`: Utility classes.
    *   `com.quickcalc.constants/`: Application constants.
    *   `module-info.java`: Java module definition.
*   `src/main/resources/fxml/`: Contains FXML files for the UI layout.
*   `src/test/java/`: Contains test source code (note: `run_app.bat` currently includes these in compilation).
*   `bin/`: Directory where compiled `.class` files and copied resources are placed. This directory is created by `run_app.bat`.
*   `javafx-sdk-17.0.2/`: Directory where the JavaFX SDK should be placed.
*   `run_app.bat`: Batch script to compile and run the application.

### Running the Application

The primary way to run the application is using the `run_app.bat` script:

1.  **Ensure Prerequisites:** Verify that JDK 17 is installed and configured, and the JavaFX SDK 17.0.2 is placed correctly in `.\javafx-sdk-17.0.2`.
2.  **Open a Command Prompt or PowerShell** in the root directory of the project.
3.  **Execute the script:**
    ```bash
    .\run_app.bat
    ```
    This script will:
    *   Set up necessary paths.
    *   Create `bin` directories if they don't exist.
    *   Copy FXML resources to `bin/fxml`.
    *   Compile all Java source files (from `src/main/java` and `src/test/java`) into the `bin` directory.
    *   Run the main class `com.quickcalc.Main`.

### Manual Compilation and Running (Alternative)

If you prefer or need to compile and run manually, follow the steps outlined in `run_app.bat`:

1.  **Set Environment Variables (example for PowerShell):**
    ```powershell
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-17" # Adjust if your path is different
    $env:Path = "$env:JAVA_HOME\bin;" + $env:Path
    $env:JAVAFX_HOME = ".\javafx-sdk-17.0.2"
    $env:JAVAFX_MODULES = "$env:JAVAFX_HOME\lib"
    ```
2.  **Create Directories:**
    ```powershell
    if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" }
    if (-not (Test-Path "bin\fxml")) { New-Item -ItemType Directory -Path "bin\fxml" }
    ```
3.  **Copy Resources:**
    ```powershell
    if (Test-Path "src\main\resources\fxml") {
        Copy-Item -Path "src\main\resources\fxml\*" -Destination "bin\fxml\" -Recurse -Force
    }
    ```
4.  **Compile:**
    ```powershell
    javac --module-path $env:JAVAFX_MODULES --add-modules javafx.controls,javafx.fxml -d bin src\main\java\module-info.java src\main\java\com\quickcalc\*.java src\main\java\com\quickcalc\controllers\*.java src\main\java\com\quickcalc\models\*.java src\main\java\com\quickcalc\views\components\*.java src\main\java\com\quickcalc\utils\*.java src\main\java\com\quickcalc\constants\*.java src\test\java\com\quickcalc\*.java
    ```
5.  **Run:**
    ```powershell
    java --module-path $env:JAVAFX_MODULES --add-modules javafx.controls,javafx.fxml -cp bin com.quickcalc.Main
    ```

## Python Backend

QuickCalc uses a Python backend for finite element analysis (FEA) calculations. The backend is located in the `backend/` directory and includes:

### Python Components
- `fea.py` - Core finite element analysis engine using NumPy/SciPy
- `load_combinations.py` - Load combination logic (ASCE 7, custom factors)
- `data.py` - Beam data handling and validation
- `app.py` - Main analysis interface

### Python Setup
1. Install Python 3.10+ 
2. Install dependencies: `pip install -r requirements.txt`
3. The JavaFX application communicates with Python via subprocess calls

### Integration
- JavaFX sends beam data as JSON to Python subprocess
- Python performs FEA calculations and returns results as JSON
- Results include reactions, shear/moment diagrams, and deflections

### Testing Python Backend
Run tests independently: `python -m pytest tests/` from project root

## License

This project is licensed under the MIT License - see the `LICENSE` file for details. (Assuming a `LICENSE` file exists and is MIT).