Write-Host "Starting Allocat ERP Backend in development mode..." -ForegroundColor Green
Write-Host "Using H2 in-memory database for development" -ForegroundColor Yellow
Write-Host ""
Write-Host "You can access:" -ForegroundColor Cyan
Write-Host "- Health Check: http://localhost:8080/api/health" -ForegroundColor White
Write-Host "- Test Endpoint: http://localhost:8080/api/test" -ForegroundColor White
Write-Host "- Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "- H2 Console: http://localhost:8080/h2-console (if enabled)" -ForegroundColor White
Write-Host ""

Set-Location allocat-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring.profiles.active=dev
