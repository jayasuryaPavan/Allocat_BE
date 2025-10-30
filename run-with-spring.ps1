Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Starting Spring Boot and InvenGadu (Python) ..." -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

# Start Spring Boot (dev) in a new window
Write-Host "Launching Spring Boot (dev profile) in new window..." -ForegroundColor Green
$springScript = Join-Path $repoRoot "allocat-erp-backend/run-dev.ps1"
if (-not (Test-Path $springScript)) {
    Write-Host "[ERROR] Cannot find: $springScript" -ForegroundColor Red
    exit 1
}

Start-Process powershell -ArgumentList "-NoProfile","-ExecutionPolicy","Bypass","-File","`"$springScript`"" -WindowStyle Normal

# Start InvenGadu backend
Write-Host "Preparing Python virtual environment for InvenGadu..." -ForegroundColor Green
$pyDir = Join-Path $repoRoot "inven_gadu_backend"
if (-not (Test-Path $pyDir)) {
    Write-Host "[ERROR] Cannot find Python backend folder: $pyDir" -ForegroundColor Red
    exit 1
}

Set-Location $pyDir

if (-not (Test-Path "venv")) {
    Write-Host "Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
    if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Failed to create venv" -ForegroundColor Red; exit 1 }
}

Write-Host "Activating virtual environment..." -ForegroundColor Yellow
& "venv/Scripts/Activate.ps1"

Write-Host "Installing Python dependencies (if needed)..." -ForegroundColor Yellow
pip install -r requirements.txt | Out-Null

if (-not (Test-Path ".env") -and (Test-Path "env.example")) {
    Copy-Item "env.example" ".env"
    Write-Host "Created .env from env.example" -ForegroundColor Yellow
}

Write-Host "Starting InvenGadu FastAPI server..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoProfile","-ExecutionPolicy","Bypass","-Command","cd `"$pyDir`"; & `"$pyDir/venv/Scripts/Activate.ps1`"; python main.py" -WindowStyle Normal

Write-Host "All services launched:" -ForegroundColor Cyan
Write-Host "- Spring Boot: http://localhost:8080" -ForegroundColor White
Write-Host "- InvenGadu:  http://localhost:8000" -ForegroundColor White

