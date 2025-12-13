# Cloud SQL Setup Script for Allocat ERP (PowerShell)
# Usage: .\cloudsql-setup.ps1 -ProjectId "your-project-id" -DbPassword "your-password"

param(
    [Parameter(Mandatory=$false)]
    [string]$ProjectId = $env:GOOGLE_CLOUD_PROJECT,
    
    [Parameter(Mandatory=$true)]
    [string]$DbPassword
)

if ([string]::IsNullOrEmpty($ProjectId)) {
    Write-Host "Error: Project ID is required" -ForegroundColor Red
    Write-Host "Usage: .\cloudsql-setup.ps1 -ProjectId 'your-project-id' -DbPassword 'your-password'" -ForegroundColor Yellow
    exit 1
}

if ([string]::IsNullOrEmpty($DbPassword)) {
    Write-Host "Error: Database password is required" -ForegroundColor Red
    Write-Host "Usage: .\cloudsql-setup.ps1 -ProjectId 'your-project-id' -DbPassword 'your-password'" -ForegroundColor Yellow
    exit 1
}

$Region = "northamerica-northeast1"
$InstanceName = "allocat-db"
$DatabaseName = "allocat_db"
$DbUser = "allocat_user"

Write-Host "🗄️  Setting up Cloud SQL for Allocat ERP..." -ForegroundColor Green
Write-Host "Project ID: $ProjectId" -ForegroundColor Cyan
Write-Host "Region: $Region" -ForegroundColor Cyan
Write-Host "Instance: $InstanceName" -ForegroundColor Cyan
Write-Host ""

# Set the project
gcloud config set project $ProjectId

# Enable SQL Admin API
Write-Host "📦 Enabling SQL Admin API..." -ForegroundColor Yellow
gcloud services enable sqladmin.googleapis.com

# Check if instance already exists
Write-Host "🔍 Checking if instance exists..." -ForegroundColor Yellow
$instanceExists = gcloud sql instances describe $InstanceName --project=$ProjectId 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "⚠️  Instance $InstanceName already exists. Skipping creation." -ForegroundColor Yellow
} else {
    # Create Cloud SQL instance
    Write-Host "🔨 Creating Cloud SQL instance..." -ForegroundColor Yellow
    gcloud sql instances create $InstanceName `
        --database-version=POSTGRES_15 `
        --tier=db-f1-micro `
        --region=$Region `
        --root-password=$DbPassword `
        --storage-type=SSD `
        --storage-size=20GB `
        --backup-start-time=03:00 `
        --enable-bin-log `
        --project=$ProjectId
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Instance created successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to create instance" -ForegroundColor Red
        exit 1
    }
}

# Create database
Write-Host "📊 Creating database..." -ForegroundColor Yellow
$dbExists = gcloud sql databases describe $DatabaseName --instance=$InstanceName --project=$ProjectId 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "⚠️  Database $DatabaseName already exists. Skipping creation." -ForegroundColor Yellow
} else {
    gcloud sql databases create $DatabaseName `
        --instance=$InstanceName `
        --project=$ProjectId
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Database created successfully" -ForegroundColor Green
    }
}

# Create database user
Write-Host "👤 Creating database user..." -ForegroundColor Yellow
$userExists = gcloud sql users describe $DbUser --instance=$InstanceName --project=$ProjectId 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "⚠️  User $DbUser already exists. Updating password..." -ForegroundColor Yellow
    gcloud sql users set-password $DbUser `
        --instance=$InstanceName `
        --password=$DbPassword `
        --project=$ProjectId
} else {
    gcloud sql users create $DbUser `
        --instance=$InstanceName `
        --password=$DbPassword `
        --project=$ProjectId
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ User created/updated successfully" -ForegroundColor Green
}

# Get connection name
Write-Host "🔍 Getting connection name..." -ForegroundColor Yellow
$ConnectionName = gcloud sql instances describe $InstanceName `
    --format="value(connectionName)" `
    --project=$ProjectId

Write-Host ""
Write-Host "✅ Cloud SQL setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Connection details:" -ForegroundColor Cyan
Write-Host "  Instance: $InstanceName" -ForegroundColor White
Write-Host "  Database: $DatabaseName" -ForegroundColor White
Write-Host "  User: $DbUser" -ForegroundColor White
Write-Host "  Connection Name: $ConnectionName" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Store the password in Secret Manager:" -ForegroundColor White
Write-Host "   echo -n '$DbPassword' | gcloud secrets create db-password --data-file=-" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Store the connection name in Secret Manager:" -ForegroundColor White
Write-Host "   echo -n '$ConnectionName' | gcloud secrets create cloudsql-connection-name --data-file=-" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Run .\secrets-setup.ps1 to set up all secrets" -ForegroundColor White
