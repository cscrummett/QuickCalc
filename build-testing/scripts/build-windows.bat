@echo off
REM QuickCalc Windows Build Script
REM Builds both Python backend and JavaFX frontend for Windows distribution

echo ======================================
echo QuickCalc Windows Build Script
echo ======================================

REM Configuration
set PROJECT_ROOT=%~dp0..\..
set BUILD_DIR=%PROJECT_ROOT%\build-testing
set PYTHON_ENV=%BUILD_DIR%\python-env
set BACKEND_SRC=%BUILD_DIR%\python-build
set JAVAFX_BUILD=%BUILD_DIR%\javafx-build
set DIST_DIR=%BUILD_DIR%\distribution

REM Create distribution directory
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

echo.
echo Building Python Backend...
echo ======================================

REM Check if virtual environment exists
if not exist "%PYTHON_ENV%" (
    echo ERROR: Python virtual environment not found
    echo Please create virtual environment first
    pause
    exit /b 1
)

REM Activate virtual environment and build backend
call "%PYTHON_ENV%\Scripts\activate.bat"

REM Install PyInstaller if not available
pip install pyinstaller

REM Build backend executable
cd /d "%PROJECT_ROOT%"
python -m PyInstaller ^
    --onefile ^
    --distpath "%BACKEND_SRC%\dist" ^
    --workpath "%BACKEND_SRC%\build" ^
    --name quickcalc_backend ^
    "%BACKEND_SRC%\quickcalc_backend.py"

if %errorlevel% neq 0 (
    echo ERROR: Backend build failed
    pause
    exit /b 1
)

REM Copy backend to distribution
copy "%BACKEND_SRC%\dist\quickcalc_backend.exe" "%DIST_DIR%\"
echo ✓ Backend built: quickcalc_backend.exe

echo.
echo Building JavaFX Frontend...
echo ======================================

REM Check if JavaFX build directory exists
if not exist "%JAVAFX_BUILD%" (
    echo ERROR: JavaFX build directory not found
    echo Please copy JavaFX assets first
    pause
    exit /b 1
)

cd /d "%JAVAFX_BUILD%"

REM Clean previous builds
if exist "custom-jre" rmdir /s /q "custom-jre"
if exist "app-image" rmdir /s /q "app-image"

REM Create custom JRE
echo Creating custom JRE...
jlink --module-path javafx-sdk\lib ^
      --add-modules javafx.controls,javafx.fxml ^
      --output custom-jre

if %errorlevel% neq 0 (
    echo ERROR: jlink failed
    pause
    exit /b 1
)

REM Create JAR file first
echo Creating JAR file...
cd bin
jar --create --file ../quickcalc.jar --main-class com.quickcalc.Main .
cd ..

REM Create Windows installer with jpackage
echo Creating Windows installer...
jpackage --input . ^
         --main-jar quickcalc.jar ^
         --main-class com.quickcalc.Main ^
         --runtime-image custom-jre ^
         --name QuickCalc ^
         --dest app-image ^
         --type msi

if %errorlevel% neq 0 (
    echo ERROR: jpackage failed
    pause
    exit /b 1
)

REM Copy installer to distribution
copy "app-image\QuickCalc-1.0.msi" "%DIST_DIR%\"
echo ✓ Frontend built: QuickCalc-1.0.msi

echo.
echo Creating Distribution Package...
echo ======================================

REM Create README for Windows
(
echo QuickCalc v1.0.0 - Structural Beam Analysis
echo ================================================
echo.
echo This package contains QuickCalc for Windows.
echo.
echo Contents:
echo - quickcalc_backend.exe: Python FEA calculation engine
echo - QuickCalc-1.0.msi: JavaFX frontend installer
echo.
echo Installation:
echo 1. Run QuickCalc-1.0.msi to install the application
echo 2. The backend executable will be used automatically
echo.
echo Manual Testing:
echo - Test backend: quickcalc_backend.exe test
echo.
echo For more information, visit: https://github.com/your-username/QuickCalc
) > "%DIST_DIR%\README.txt"

REM Create ZIP package
cd /d "%DIST_DIR%"
powershell Compress-Archive -Path "quickcalc_backend.exe","QuickCalc-1.0.msi","README.txt" -DestinationPath "QuickCalc-v1.0.0-Windows.zip" -Force

echo.
echo ======================================
echo Windows build completed!
echo ======================================
echo Package: %DIST_DIR%\QuickCalc-v1.0.0-Windows.zip
echo.
echo Contents:
for %%f in ("%DIST_DIR%\quickcalc_backend.exe") do echo   - Backend: %%~zf bytes
for %%f in ("%DIST_DIR%\QuickCalc-1.0.msi") do echo   - Frontend: %%~zf bytes
for %%f in ("%DIST_DIR%\QuickCalc-v1.0.0-Windows.zip") do echo   - Total ZIP: %%~zf bytes
echo.
echo Ready for Windows distribution!
echo ======================================
pause