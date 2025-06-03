# PowerShell script to download JavaFX SDK and run QuickCalc application

$javafxVersion = "17.0.2"
$javafxURL = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-${javafxVersion}_windows-x64_bin-sdk.zip"
$javafxZip = "javafx-sdk.zip"
$javafxDir = "javafx-sdk-$javafxVersion"

# Check if JavaFX SDK is already downloaded
if (-not (Test-Path $javafxDir)) {
    Write-Host "Downloading JavaFX SDK $javafxVersion..."
    Invoke-WebRequest -Uri $javafxURL -OutFile $javafxZip
    
    Write-Host "Extracting JavaFX SDK..."
    Expand-Archive -Path $javafxZip -DestinationPath .
    
    Write-Host "Cleanup..."
    Remove-Item $javafxZip
}

# Set JavaFX module path
$javafxPath = Join-Path -Path (Get-Location) -ChildPath "$javafxDir\lib"

# Create bin directory if it doesn't exist
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin"
}

# Compile the application
Write-Host "Compiling QuickCalc..."
$compileCommand = "javac --module-path `"$javafxPath`" --add-modules javafx.controls,javafx.fxml -d bin -sourcepath src/main/java src/main/java/com/quickcalc/Main.java"
Invoke-Expression $compileCommand

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
    # Copy resources
    Write-Host "Copying resources..."
    if (-not (Test-Path "bin\fxml")) {
        New-Item -ItemType Directory -Path "bin\fxml"
    }
    Copy-Item -Path "src\main\resources\fxml\*" -Destination "bin\fxml" -Recurse -Force
    
    # Run the application
    Write-Host "Running QuickCalc..."
    $runCommand = "java --module-path `"$javafxPath`" --add-modules javafx.controls,javafx.fxml -cp bin com.quickcalc.Main"
    Invoke-Expression $runCommand
} else {
    Write-Host "Compilation failed. Please check the error messages above."
}
