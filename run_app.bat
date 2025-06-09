@echo off
echo Compiling and running QuickCalc JavaFX application...

REM ===== CONFIGURATION =====
REM Set Java and JavaFX paths
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=.\javafx-sdk-17.0.2
set JAVAFX_MODULES=%JAVAFX_HOME%\lib

REM ===== VALIDATE JAVA VERSION =====
java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH
    exit /b 1
)

REM ===== CREATE DIRECTORIES =====
if not exist bin mkdir bin
if not exist bin\fxml mkdir bin\fxml

REM ===== COPY RESOURCES =====
echo Copying FXML resources...
if exist src\main\resources\fxml (
    xcopy /y /s src\main\resources\fxml\*.* bin\fxml\ >nul
)

REM ===== COMPILE =====
echo Compiling Java files...
javac --module-path %JAVAFX_MODULES% --add-modules javafx.controls,javafx.fxml -d bin src\main\java\module-info.java src\main\java\com\quickcalc\*.java src\main\java\com\quickcalc\controllers\*.java src\main\java\com\quickcalc\models\*.java src\main\java\com\quickcalc\services\*.java src\main\java\com\quickcalc\views\components\*.java src\main\java\com\quickcalc\utils\*.java src\main\java\com\quickcalc\constants\*.java src\test\java\com\quickcalc\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed
    exit /b 1
)

REM ===== RUN =====
set MAIN_CLASS=com.quickcalc.Main
if not "%1"=="" set MAIN_CLASS=%1

echo Running %MAIN_CLASS%...
java --module-path %JAVAFX_MODULES% --add-modules javafx.controls,javafx.fxml -cp bin %MAIN_CLASS%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error running the application
    echo Make sure:
    echo 1. Java 17 is installed
    echo 2. JavaFX SDK 17.0.2 is extracted to .\javafx-sdk-17.0.2
    echo 3. The main class exists: %MAIN_CLASS%
    exit /b 1
)

echo Done.
