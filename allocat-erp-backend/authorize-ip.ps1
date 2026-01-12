# Cloud SQL IP Authorization Helper
# Run this to add your IP address to the Cloud SQL authorized networks
# Usage: .\authorize-ip.ps1 [optional-ip-address]

param(
    [string]$IpAddress = ""
)

$PROJECT_ID = "allocat-481117"
$INSTANCE_NAME = "allocat-db"

# Get current public IP if not provided
if (-not $IpAddress) {
    Write-Host "Detecting your public IP address..." -ForegroundColor Cyan
    $IpAddress = (Invoke-RestMethod -Uri "https://api.ipify.org")
}

Write-Host "Your IP address: $IpAddress" -ForegroundColor Green
Write-Host "Adding to Cloud SQL authorized networks..." -ForegroundColor Cyan

# Get current timestamp for unique name
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$networkName = "dev-$env:USERNAME-$timestamp"

# Add the IP to authorized networks
gcloud sql instances patch $INSTANCE_NAME `
    --project=$PROJECT_ID `
    --authorized-networks="$IpAddress/32" `
    --quiet

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nSuccess! Your IP ($IpAddress) is now authorized." -ForegroundColor Green
    Write-Host "You can now connect to Cloud SQL at: 34.66.13.124:5432" -ForegroundColor Cyan
} else {
    Write-Host "`nFailed to add IP. Make sure you have Cloud SQL Admin permissions." -ForegroundColor Red
}
