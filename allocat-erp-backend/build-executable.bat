@echo off
echo Building Allocat ERP Backend Executable...
echo.

REM Check if Maven is available
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH, or use an IDE like IntelliJ IDEA or Eclipse
    echo.
    echo Alternative: Use the PowerShell script which may work with Maven wrapper
    pause
    exit /b 1
)

REM Clean previous builds
echo Cleaning previous builds...
call mvn clean
if %ERRORLEVEL% neq 0 (
    echo Error: Maven clean failed
    pause
    exit /b 1
)

REM Compile and package the application
echo Compiling and packaging the application...
call mvn package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo Error: Maven package failed
    pause
    exit /b 1
)

REM Check if the executable JAR was created
if exist "allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar" (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Executable JAR created at:
    echo %CD%\allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar
    echo.
    echo To run the application:
    echo java -jar allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar
    echo.
    echo Or simply double-click the JAR file to run it as a desktop application.
    echo.
) else (
    echo Error: Executable JAR was not created
    pause
    exit /b 1
)

pause
