@echo off
echo Compiling and running LoadRenderingTest...
javac --module-path "C:\Program Files\Java\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -d target/classes src/test/java/com/quickcalc/LoadRenderingTest.java
java --module-path "C:\Program Files\Java\javafx-sdk-17.0.2\lib;target/classes" --add-modules javafx.controls,javafx.fxml com.quickcalc.LoadRenderingTest
