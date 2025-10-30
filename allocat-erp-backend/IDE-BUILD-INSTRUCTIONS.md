# IDE Build Instructions for Allocat ERP Backend

If Maven is not available in your PATH, you can build the executable using an IDE.

## IntelliJ IDEA

1. **Open the project:**
   - File → Open → Select the `allocat-erp-backend` folder
   - Wait for Maven to download dependencies

2. **Build the executable:**
   - Open the Maven tool window (View → Tool Windows → Maven)
   - Navigate to: `allocat-erp-backend` → `Lifecycle`
   - Double-click `clean` to clean previous builds
   - Double-click `package` to build the executable JAR

3. **Find the executable:**
   - The executable JAR will be created at: `allocat-api/target/allocat-api-0.0.1-SNAPSHOT.jar`

## Eclipse

1. **Import the project:**
   - File → Import → Maven → Existing Maven Projects
   - Select the `allocat-erp-backend` folder
   - Click Finish

2. **Build the executable:**
   - Right-click on the project root
   - Run As → Maven build...
   - Goals: `clean package`
   - Click Run

3. **Find the executable:**
   - The executable JAR will be created at: `allocat-api/target/allocat-api-0.0.1-SNAPSHOT.jar`

## Visual Studio Code

1. **Install extensions:**
   - Java Extension Pack
   - Maven for Java

2. **Open the project:**
   - File → Open Folder → Select `allocat-erp-backend`

3. **Build the executable:**
   - Open Command Palette (Ctrl+Shift+P)
   - Type "Java: Run Maven Goals"
   - Select "clean package"

4. **Find the executable:**
   - The executable JAR will be created at: `allocat-api/target/allocat-api-0.0.1-SNAPSHOT.jar`

## Running the Application

After building, you can run the application in several ways:

### Method 1: Double-click the JAR
- Navigate to `allocat-api/target/`
- Double-click `allocat-api-0.0.1-SNAPSHOT.jar`

### Method 2: Command Line
```bash
java -jar allocat-api/target/allocat-api-0.0.1-SNAPSHOT.jar
```

### Method 3: IDE Run Configuration
- In IntelliJ: Run → Edit Configurations → Add New → Application
- Main class: `com.allocat.api.AllocatApplication`
- Module: `allocat-api`

## Troubleshooting

### "Java not found"
- Ensure Java 17+ is installed
- In IntelliJ: File → Project Structure → Project → Project SDK
- In Eclipse: Window → Preferences → Java → Installed JREs

### "Maven dependencies not found"
- Refresh Maven project
- In IntelliJ: Maven tool window → Reload All Maven Projects
- In Eclipse: Right-click project → Maven → Reload Projects

### "Port already in use"
- Change server port in `allocat-core/src/main/resources/application.yml`
- Or stop other services using port 8080

## Configuration

The application uses these configuration files:
- `allocat-core/src/main/resources/application.yml` - Base configuration
- `allocat-core/src/main/resources/application-dev.yml` - Development settings
- `allocat-core/src/main/resources/application-prod.yml` - Production settings

## Next Steps

Once you have the executable JAR:
1. Test it by running: `java -jar allocat-api-0.0.1-SNAPSHOT.jar`
2. Access the application at: http://localhost:8080
3. View API documentation at: http://localhost:8080/swagger-ui.html
4. For production deployment, configure the appropriate profile and database settings
