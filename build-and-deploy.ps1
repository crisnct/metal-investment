# Build and Deploy Script for Metal Investment UI
# This script builds the React app and overwrites existing files instead of creating new ones

Write-Host "Building React application..." -ForegroundColor Green
npm run build

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Deploying to static resources..." -ForegroundColor Green
    
    # Remove old static files to prevent accumulation
    Write-Host "Cleaning old static files..." -ForegroundColor Yellow
    if (Test-Path "src\main\resources\static\static") {
        Remove-Item -Path "src\main\resources\static\static" -Recurse -Force
    }
    
    # Copy new build files, overwriting existing ones
    Write-Host "Copying build files to static resources..." -ForegroundColor Yellow
    xcopy /E /Y build\* src\main\resources\static\
    
    Write-Host "Deployment completed successfully!" -ForegroundColor Green
    Write-Host "Static files have been updated in src\main\resources\static\" -ForegroundColor Cyan
} else {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    exit 1
}
