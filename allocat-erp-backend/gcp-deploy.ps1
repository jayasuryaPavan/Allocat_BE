# GCP Deployment Script for Allocat ERP (PowerShell)
# Usage: .\gcp-deploy.ps1 -ProjectId "your-project-id"

param(
    [Parameter(Mandatory=$false)]
    [string]$ProjectId = $env:GOOGLE_CLOUD_PROJECT
)

if ([string]::IsNullOrEmpty($ProjectId)) {
    Write-Host "Error: Project ID is required" -ForegroundColor Red
    Write-Host "Usage: .\gcp-deploy.ps1 -ProjectId 'your-project-id'" -ForegroundColor Yellow
    Write-Host "Or set GOOGLE_CLOUD_PROJECT environment variable" -ForegroundColor Yellow
    exit 1
}

$Region = "northamerica-northeast1"
$ServiceName = "allocat-erp"
$ImageName = "gcr.io/$ProjectId/$ServiceName"

Write-Host "🚀 Starting deployment to GCP..." -ForegroundColor Green
Write-Host "Project ID: $ProjectId" -ForegroundColor Cyan
Write-Host "Region: $Region" -ForegroundColor Cyan
Write-Host "Service: $ServiceName" -ForegroundColor Cyan
Write-Host ""

# Set the project
Write-Host "📦 Setting GCP project..." -ForegroundColor Yellow
gcloud config set project $ProjectId

# Enable required APIs
Write-Host "📦 Enabling required GCP APIs..." -ForegroundColor Yellow
gcloud services enable `
    cloudbuild.googleapis.com `
    run.googleapis.com `
    sqladmin.googleapis.com `
    secretmanager.googleapis.com `
    containerregistry.googleapis.com

# Build and push Docker image
Write-Host "🔨 Building Docker image..." -ForegroundColor Yellow
$gitCommit = git rev-parse --short HEAD 2>$null
if ($LASTEXITCODE -ne 0) { $gitCommit = "latest" }

gcloud builds submit `
    --tag "$ImageName:latest" `
    --tag "$ImageName:$gitCommit" `
    --project $ProjectId

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

# Get Cloud SQL connection name
$CloudSqlInstance = "$ProjectId`:$Region`:allocat-db"

# Check if Cloud SQL instance exists
Write-Host "🔍 Checking Cloud SQL instance..." -ForegroundColor Yellow
$sqlExists = gcloud sql instances describe allocat-db --project=$ProjectId 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Warning: Cloud SQL instance 'allocat-db' not found!" -ForegroundColor Red
    Write-Host "Please run .\cloudsql-setup.ps1 first" -ForegroundColor Yellow
    exit 1
}

# Deploy to Cloud Run
Write-Host "🚢 Deploying to Cloud Run..." -ForegroundColor Yellow
gcloud run deploy $ServiceName `
    --image "$ImageName:latest" `
    --region $Region `
    --platform managed `
    --allow-unauthenticated `
    --port 8081 `
    --memory 2Gi `
    --cpu 2 `
    --min-instances 1 `
    --max-instances 10 `
    --set-env-vars "SPRING_PROFILES_ACTIVE=prod" `
    --add-cloudsql-instances $CloudSqlInstance `
    --set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest,SPRING_DATASOURCE_USERNAME=db-user:latest,JWT_SECRET=jwt-secret:latest,CLOUDSQL_INSTANCE=cloudsql-connection-name:latest" `
    --project $ProjectId

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Deployment failed!" -ForegroundColor Red
    exit 1
}

# Get the service URL
Write-Host "🔍 Getting service URL..." -ForegroundColor Yellow
$ServiceUrl = gcloud run services describe $ServiceName `
    --region $Region `
    --format 'value(status.url)' `
    --project $ProjectId

Write-Host ""
Write-Host "✅ Deployment complete!" -ForegroundColor Green
Write-Host "Service URL: $ServiceUrl" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Test the health endpoint: $ServiceUrl/actuator/health" -ForegroundColor White
Write-Host "2. Update your DNS to point to this URL (or set up a custom domain)" -ForegroundColor White
Write-Host "3. Update frontend environment with this backend URL" -ForegroundColor White
Write-Host "4. Verify database migrations ran successfully" -ForegroundColor White
Write-Host ""
Write-Host "To check logs:" -ForegroundColor Yellow
Write-Host "  gcloud run services logs read $ServiceName --region $Region" -ForegroundColor White
