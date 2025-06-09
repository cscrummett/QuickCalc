# Running QuickCalc Application

This document explains how to run the QuickCalc JavaFX application.

## Running the Application

Use the batch file from Windows Command Prompt or PowerShell:
```cmd
.\run_app.bat
```

## Prerequisites

The script requires:
1. **Java 17 or later** - Make sure `java` and `javac` are in your PATH
2. **JavaFX SDK 17.0.2** - Must be extracted to `./javafx-sdk-17.0.2/` in the project root

## What the Script Does

The script performs these operations:

1. **Validate Environment**
   - Check Java installation
   - Verify JavaFX SDK location

2. **Create Build Directory**
   - Create `bin/` folder for compiled classes
   - Create `bin/fxml/` for FXML resources

3. **Copy Resources**
   - Copy FXML files from `src/main/resources/fxml/` to `bin/fxml/`

4. **Compile All Java Files**
   - Compile all packages: main, controllers, models, services, views, utils, constants
   - Include test files for development
   - Use JavaFX module path and required modules

5. **Run Application**
   - Execute `com.quickcalc.Main` with proper JavaFX configuration

## Package Structure Compiled

The script compiles all Java files in these packages:
- `com.quickcalc.*` - Main classes
- `com.quickcalc.controllers.*` - FXML controllers
- `com.quickcalc.models.*` - Data models
- `com.quickcalc.services.*` - Business logic services (Phase 1 refactoring)
- `com.quickcalc.views.components.*` - UI components
- `com.quickcalc.utils.*` - Utility classes
- `com.quickcalc.constants.*` - Application constants
- Test classes for development

## Troubleshooting

### Common Issues

**"Java is not installed or not in PATH"**
- Install Java 17 or later
- Add Java to your system PATH

**"JavaFX SDK not found"**
- Download JavaFX SDK 17.0.2
- Extract to `javafx-sdk-17.0.2/` in project root

**"Compilation failed"**
- Check for syntax errors in Java files
- Ensure all required imports are available
- Verify JavaFX SDK version compatibility

**"Error running the application"**
- Make sure JavaFX runtime modules are accessible
- Check that FXML files are properly copied to `bin/fxml/`

### Manual Compilation (Advanced)

If the script fails, you can compile manually from Windows Command Prompt:

```cmd
REM Create directories
if not exist bin mkdir bin
if not exist bin\fxml mkdir bin\fxml

REM Copy resources
xcopy /y /s src\main\resources\fxml\*.* bin\fxml\

REM Compile
javac --module-path .\javafx-sdk-17.0.2\lib ^
      --add-modules javafx.controls,javafx.fxml ^
      -d bin ^
      src\main\java\module-info.java ^
      src\main\java\com\quickcalc\*.java ^
      src\main\java\com\quickcalc\controllers\*.java ^
      src\main\java\com\quickcalc\models\*.java ^
      src\main\java\com\quickcalc\services\*.java ^
      src\main\java\com\quickcalc\views\components\*.java ^
      src\main\java\com\quickcalc\utils\*.java ^
      src\main\java\com\quickcalc\constants\*.java

REM Run
java --module-path .\javafx-sdk-17.0.2\lib ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp bin ^
     com.quickcalc.Main
```

## Development Notes

- **Phase 1 Refactoring**: The script includes the new `services` package created during Phase 1 refactoring
- **Testing**: The script supports running tests alongside the main application
- **Build Output**: Compiled classes go to `bin/` directory (ignored by git)

## IDE Alternative

Most IDEs (IntelliJ IDEA, Eclipse, VS Code) can run JavaFX applications directly if configured with:
- JavaFX SDK path
- Module path configuration
- VM options: `--module-path .\javafx-sdk-17.0.2\lib --add-modules javafx.controls,javafx.fxml`