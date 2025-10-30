# Allocat ERP Backend

A Spring Boot multi-module ERP backend application for retail management.

## Project Structure

- `allocat-common`: Shared DTOs, entities, and utilities
- `allocat-security`: Security configuration and JWT handling
- `allocat-core`: Business logic, entities, repositories, and services
- `allocat-api`: REST controllers and main application entry point

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production)
- Redis (optional, for caching)

## Quick Start (Development Mode)

### Option 1: Using the provided scripts

```bash
# Windows (PowerShell)
.\run-dev.ps1

# Windows (Command Prompt)
run-dev.bat
```

### Option 2: Manual Maven commands

```bash
# Build the project
mvn clean compile

# Run in development mode (uses H2 in-memory database)
cd allocat-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Configuration

### Development Profile
The application uses H2 in-memory database for development, which requires no external setup.

### Production Profile
For production, configure the following environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/allocat_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-secret-key
```

## Available Endpoints

- **Health Check**: `GET /api/health`
- **Test Endpoint**: `GET /api/test`
- **User Management**: `GET /api/users`, `POST /api/users`
- **H2 Console**: `http://localhost:8080/h2-console` (development only)

## API Documentation

The application includes Swagger/OpenAPI documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

You can explore and test all API endpoints through the Swagger UI interface.

## Database Migrations

The application uses Flyway for database migrations. Migration files are located in:
`allocat-core/src/main/resources/db/migration/`

## Troubleshooting

### Common Issues

1. **Maven not found**: Ensure Maven is installed and added to your PATH
2. **Database connection issues**: 
   - For development: Ensure no external dependencies are required (H2 is embedded)
   - For production: Ensure PostgreSQL is running and accessible
3. **Port already in use**: Change the port in `application.yml` or stop the conflicting service

### Logs

The application logs are configured to show:
- DEBUG level for `com.allocat` packages
- SQL queries (when `show-sql: true`)
- Web requests and responses

## Next Steps

1. Create authentication endpoints
2. Implement product management
3. Add inventory tracking
4. Set up POS functionality
5. Configure proper security and JWT handling
