# PowerShell script to download JavaFX SDK and run QuickCalc application

# Configuration
$javafxVersion = "17.0.2"
$javafxURL = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-${javafxVersion}_windows-x64_bin-sdk.zip"
$javafxZip = "javafx-sdk.zip"
$javafxDir = "javafx-sdk-$javafxVersion"

# Create bin directory if it doesn't exist
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}
if (-not (Test-Path "bin\fxml")) {
    New-Item -ItemType Directory -Path "bin\fxml" | Out-Null
}

# Download and extract JavaFX SDK if it doesn't exist
if (-not (Test-Path $javafxDir)) {
    Write-Host "Downloading JavaFX SDK $javafxVersion..."
    Invoke-WebRequest -Uri $javafxURL -OutFile $javafxZip
    
    Write-Host "Extracting JavaFX SDK..."
    Expand-Archive -Path $javafxZip -DestinationPath . -Force
    
    Write-Host "Cleanup..."
    Remove-Item $javafxZip -Force
}

# Set JavaFX module path
$javafxPath = Join-Path -Path (Get-Location) -ChildPath "$javafxDir\lib"
Write-Host "JavaFX SDK path: $javafxPath"

# Copy FXML resources
Write-Host "Copying FXML resources..."
Copy-Item -Path "src\main\resources\fxml\*" -Destination "bin\fxml" -Recurse -Force

# Compile the application
Write-Host "Compiling QuickCalc..."
$compileCommand = "javac --module-path `"$javafxPath`" --add-modules javafx.controls,javafx.fxml -d bin src\main\java\com\quickcalc\*.java src\main\java\com\quickcalc\controllers\*.java src\main\java\com\quickcalc\models\*.java src\main\java\com\quickcalc\views\components\*.java src\main\java\com\quickcalc\utils\*.java"
Write-Host "Running: $compileCommand"
Invoke-Expression $compileCommand

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
    
    # Run the application
    Write-Host "Running QuickCalc..."
    $runCommand = "java --module-path `"$javafxPath`" --add-modules javafx.controls,javafx.fxml -cp bin com.quickcalc.Main"
    Write-Host "Running: $runCommand"
    Invoke-Expression $runCommand
} else {
    Write-Host "Compilation failed. Please check the error messages above."
}

Write-Host "Done."
