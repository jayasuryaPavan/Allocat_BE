@echo off
echo Building Allocat ERP Backend as Windows Executable (.exe)...
echo.

REM Check if Java 17+ is available
java -version 2>&1 | findstr /i "version" | findstr /i "17\|18\|19\|20\|21" >nul
if %ERRORLEVEL% neq 0 (
    echo Warning: Java 17+ is recommended for jpackage. Current version:
    java -version
    echo.
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
if not exist "allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar" (
    echo Error: Executable JAR was not created
    pause
    exit /b 1
)

REM Create Windows executable using jpackage
echo Creating Windows executable...
jpackage --input allocat-api\target --main-jar allocat-api-0.0.1-SNAPSHOT.jar --main-class com.allocat.api.AllocatApplication --name "AllocatERP" --type exe --dest dist --win-console --win-shortcut --win-menu

if %ERRORLEVEL% neq 0 (
    echo Warning: jpackage failed. This might be due to:
    echo 1. Java version not supporting jpackage (requires Java 14+)
    echo 2. Missing Windows SDK or Visual Studio Build Tools
    echo.
    echo Falling back to executable JAR...
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL (JAR only)!
    echo ========================================
    echo.
    echo Executable JAR created at:
    echo %CD%\allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar
    echo.
    echo To run the application:
    echo java -jar allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Windows executable (.exe) created in the 'dist' folder
    echo You can now run AllocatERP.exe as a desktop application
    echo.
)

pause
