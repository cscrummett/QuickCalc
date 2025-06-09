@echo off
echo Testing compilation of refactored code...

REM Set Java and JavaFX paths
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=.\javafx-sdk-17.0.2
set JAVAFX_MODULES=%JAVAFX_HOME%\lib

REM Create temp directory
if not exist temp_test mkdir temp_test

REM Test compilation
echo Compiling all Java files...
dir /s /b src\main\java\*.java > temp_java_files.txt
javac --module-path %JAVAFX_MODULES% --add-modules javafx.controls,javafx.fxml -d temp_test @temp_java_files.txt

if %ERRORLEVEL% NEQ 0 (
    echo Compilation FAILED
    del temp_java_files.txt
    rmdir /s /q temp_test
    exit /b 1
) else (
    echo Compilation SUCCESSFUL
    del temp_java_files.txt
    rmdir /s /q temp_test
)

echo Done.