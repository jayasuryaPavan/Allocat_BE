@echo off
REM Start Spring Boot and InvenGadu (Python) together
setlocal ENABLEDELAYEDEXPANSION

pushd %~dp0

echo ==============================================
echo Starting Spring Boot and InvenGadu (Python) ...
echo ==============================================

echo.
echo Launching Spring Boot (dev) in a new window...
start "SpringBoot" powershell -NoProfile -ExecutionPolicy Bypass -File "allocat-erp-backend\run-dev.ps1"

REM Prepare Python service
set PY_DIR=inven_gadu_backend
if not exist "%PY_DIR%" (
  echo [ERROR] Cannot find Python backend folder: %PY_DIR%
  goto :eof
)

pushd "%PY_DIR%"

if not exist "venv" (
  echo Creating virtual environment...
  python -m venv venv
  if errorlevel 1 (
    echo [ERROR] Failed to create virtual environment
    popd
    goto :eof
  )
)

echo Installing dependencies (if needed)...
call venv\Scripts\activate.bat
pip install -r requirements.txt >nul 2>nul

if not exist ".env" if exist "env.example" (
  copy /Y env.example .env >nul
  echo Created .env from env.example
)

echo Starting InvenGadu FastAPI server in a new window...
start "InvenGadu" powershell -NoProfile -ExecutionPolicy Bypass -Command "cd '%CD%'; .\venv\Scripts\Activate.ps1; python main.py"

popd

echo.
echo All services launched:
echo - Spring Boot: http://localhost:8080
echo - InvenGadu:  http://localhost:8000

echo.
popd
endlocal
