@echo off
echo Starting Allocat ERP Backend in development mode...
echo Using H2 in-memory database for development
echo.
echo You can access:
echo - Health Check: http://localhost:8080/api/health
echo - Test Endpoint: http://localhost:8080/api/test
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo - H2 Console: http://localhost:8080/h2-console (if enabled)
echo.

cd allocat-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring.profiles.active=dev
