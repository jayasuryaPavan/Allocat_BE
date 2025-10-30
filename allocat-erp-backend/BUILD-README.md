# Allocat ERP Backend - Build Instructions

This document explains how to build the Allocat ERP Backend as an executable desktop application.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Windows 10/11 (for .exe creation)

## Build Options

### Option 1: Executable JAR (Recommended)

The simplest way to create an executable desktop application:

```bash
# Run the batch script
build-executable.bat

# Or run the PowerShell script
.\build-executable.ps1

# Or manually with Maven
mvn clean package -DskipTests
```

This creates an executable JAR file at: `allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar`

**To run the application:**
- Double-click the JAR file, or
- Run: `java -jar allocat-api\target\allocat-api-0.0.1-SNAPSHOT.jar`

### Option 2: Windows Executable (.exe)

For a native Windows executable:

```bash
# Run the exe build script
build-exe.bat
```

This creates a Windows executable (.exe) in the `dist` folder using Java's `jpackage` tool.

**Requirements for .exe creation:**
- Java 14+ (jpackage support)
- Windows SDK or Visual Studio Build Tools (for native compilation)

## Build Scripts

### `build-executable.bat`
- Creates an executable JAR file
- Works with any Java version 17+
- Cross-platform compatible

### `build-executable.ps1`
- PowerShell version of the build script
- Includes interactive features
- Option to run the application after build

### `build-exe.bat`
- Creates a native Windows executable
- Requires Java 14+ and Windows SDK
- Falls back to JAR if jpackage fails

## Running the Application

### As a Desktop Application
1. **JAR Method**: Double-click the JAR file or associate .jar files with Java
2. **EXE Method**: Run the generated .exe file from the dist folder

### As a Service
The executable JAR can be run as a Windows service using tools like:
- NSSM (Non-Sucking Service Manager)
- Apache Commons Daemon
- Windows Service Wrapper

## Configuration

The application uses Spring Boot profiles:
- `application.yml` - Base configuration
- `application-dev.yml` - Development settings
- `application-prod.yml` - Production settings

## Troubleshooting

### Common Issues

1. **"Java not found"**
   - Ensure Java 17+ is installed and in PATH
   - Verify with: `java -version`

2. **"Maven not found"**
   - Install Maven and add to PATH
   - Verify with: `mvn -version`

3. **"jpackage failed"**
   - Install Windows SDK or Visual Studio Build Tools
   - Use the JAR method instead

4. **"Port already in use"**
   - Change the server port in `application.yml`
   - Or stop other services using the same port

### Logs and Debugging

- Application logs are written to console by default
- For production, configure logging in `application-prod.yml`
- Use `--debug` flag for detailed startup logs

## Distribution

### JAR Distribution
- Include the JAR file and any required configuration files
- Ensure Java 17+ is installed on target machines
- Provide startup scripts for easy execution

### EXE Distribution
- The generated .exe is self-contained
- No additional Java installation required on target machines
- Include any required configuration files

## Security Considerations

- The executable JAR includes all dependencies
- Ensure sensitive configuration is externalized
- Use environment variables for production secrets
- Consider code signing for the .exe file

## Performance

- The JAR method has faster startup time
- The EXE method has better integration with Windows
- Both methods have similar runtime performance
- Consider JVM tuning for production deployments
