# InvenGadu Backend - Quick Setup Script for PowerShell
# This script helps set up the Python environment and dependencies

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "InvenGadu Backend - Setup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Python installation
try {
    $pythonVersion = python --version 2>&1
    Write-Host "[OK] Python found: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Python is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Python 3.8+ from https://www.python.org/downloads/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if virtual environment exists
if (Test-Path "venv") {
    Write-Host ""
    Write-Host "Virtual environment already exists." -ForegroundColor Yellow
    Write-Host "Skipping virtual environment creation..." -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "Creating virtual environment..." -ForegroundColor Cyan
    python -m venv venv
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to create virtual environment" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host "[OK] Virtual environment created" -ForegroundColor Green
}

# Activate virtual environment
Write-Host ""
Write-Host "Activating virtual environment..." -ForegroundColor Cyan
& "venv\Scripts\Activate.ps1"

# Upgrade pip
Write-Host ""
Write-Host "Upgrading pip..." -ForegroundColor Cyan
python -m pip install --upgrade pip --quiet

# Install dependencies
Write-Host ""
Write-Host "Installing dependencies from requirements.txt..." -ForegroundColor Cyan
pip install -r requirements.txt
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Failed to install dependencies" -ForegroundColor Red
    Write-Host "Please check your internet connection and try again" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[OK] Dependencies installed successfully" -ForegroundColor Green

# Check if .env exists
if (Test-Path ".env") {
    Write-Host ""
    Write-Host "[INFO] .env file already exists" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "Creating .env file from env.example..." -ForegroundColor Cyan
    Copy-Item env.example .env -ErrorAction SilentlyContinue
    Write-Host "[OK] .env file created" -ForegroundColor Green
    Write-Host "[INFO] Please edit .env file with your settings" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Install Ollama from https://ollama.ai/download" -ForegroundColor White
Write-Host "2. Run: ollama pull llama3" -ForegroundColor White
Write-Host "3. Start Ollama: ollama serve" -ForegroundColor White
Write-Host "4. Ensure Spring Boot backend is running on http://localhost:8080/api" -ForegroundColor White
Write-Host "5. Start InvenGadu: python main.py" -ForegroundColor White
Write-Host ""
Write-Host "For detailed instructions, see README.md" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"

