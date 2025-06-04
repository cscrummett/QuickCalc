@echo off
echo Compiling and running QuickCalc JavaFX application...

REM ===== CONFIGURATION =====
REM Set Java and JavaFX paths
set JAVA_HOME=C:\Program Files\Java\jdk-18
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=.\javafx-sdk-17.0.2
set JAVAFX_MODULES=%JAVAFX_HOME%\lib

REM ===== CREATE DIRECTORIES =====
if not exist bin mkdir bin
if not exist bin\fxml mkdir bin\fxml

REM ===== COPY RESOURCES =====
echo Copying FXML resources...
xcopy /y /s src\main\resources\fxml\*.* bin\fxml\

REM ===== COMPILE =====
echo Compiling Java files...
javac --module-path %JAVAFX_MODULES% --add-modules javafx.controls,javafx.fxml -d bin src\main\java\module-info.java src\main\java\com\quickcalc\*.java src\main\java\com\quickcalc\controllers\*.java src\main\java\com\quickcalc\models\*.java src\main\java\com\quickcalc\views\components\*.java src\main\java\com\quickcalc\utils\*.java src\main\java\com\quickcalc\constants\*.java src\test\java\com\quickcalc\*.java

REM ===== RUN =====
echo Running QuickCalc...
java --module-path %JAVAFX_MODULES% --add-modules javafx.controls,javafx.fxml -cp bin %1

echo Done.
