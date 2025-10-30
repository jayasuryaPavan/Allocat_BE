@echo off
REM InvenGadu Backend - Quick Setup Script for Windows
REM This script helps set up the Python environment and dependencies

echo ========================================
echo InvenGadu Backend - Setup Script
echo ========================================
echo.

REM Check Python installation
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python is not installed or not in PATH
    echo Please install Python 3.8+ from https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [OK] Python found
python --version

REM Check if virtual environment exists
if exist "venv\" (
    echo.
    echo Virtual environment already exists.
    echo Skipping virtual environment creation...
) else (
    echo.
    echo Creating virtual environment...
    python -m venv venv
    if errorlevel 1 (
        echo [ERROR] Failed to create virtual environment
        pause
        exit /b 1
    )
    echo [OK] Virtual environment created
)

REM Activate virtual environment
echo.
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Upgrade pip
echo.
echo Upgrading pip...
python -m pip install --upgrade pip

REM Install dependencies
echo.
echo Installing dependencies from requirements.txt...
pip install -r requirements.txt
if errorlevel 1 (
    echo [ERROR] Failed to install dependencies
    echo Please check your internet connection and try again
    pause
    exit /b 1
)

echo.
echo [OK] Dependencies installed successfully

REM Check if .env exists
if exist ".env" (
    echo.
    echo [INFO] .env file already exists
) else (
    echo.
    echo Creating .env file from env.example...
    copy env.example .env >nul
    echo [OK] .env file created
    echo [INFO] Please edit .env file with your settings
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Install Ollama from https://ollama.ai/download
echo 2. Run: ollama pull llama3
echo 3. Start Ollama: ollama serve
echo 4. Ensure Spring Boot backend is running on http://localhost:8080/api
echo 5. Start InvenGadu: python main.py
echo.
echo For detailed instructions, see README.md
echo.
pause

