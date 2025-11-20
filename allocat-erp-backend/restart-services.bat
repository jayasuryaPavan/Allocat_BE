@echo off
echo ========================================
echo Allocat ERP - Service Restart Script
echo ========================================
echo.

echo Stopping any running Java processes on ports 8080 and 8081...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do taskkill /F /PID %%a 2>NUL
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do taskkill /F /PID %%a 2>NUL
timeout /t 2 >NUL

echo.
echo Building projects...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo Starting Backend API on port 8081...
start "Backend API (8081)" cmd /k "cd allocat-api && mvn spring-boot:run"

echo Waiting 10 seconds for backend to initialize...
timeout /t 10 >NUL

echo.
echo Starting Gateway on port 8080...
start "Gateway (8080)" cmd /k "cd allocat-gateway && mvn spring-boot:run"

echo.
echo ========================================
echo Services are starting!
echo ========================================
echo.
echo Backend API:  http://localhost:8081
echo Gateway:      http://localhost:8080
echo.
echo Check the new terminal windows for startup logs.
echo Press any key to exit this window...
pause >NUL



