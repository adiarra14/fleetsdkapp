#
# CMA-CGM UAT Test - Simple Version
# Quick test for SiniGroup integration with CMA-CGM technical teams
#
# Project: Fleet Management System with Maxvision SDK Integration
# Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
# Project Chief: Antoine Diarra
#
# Copyright (c) 2025 Ynnov
#

Write-Host "=== CMA-CGM UAT Test for SiniGroup ===" -ForegroundColor Green
Write-Host "Testing connection to CMA-CGM technical teams" -ForegroundColor Gray
Write-Host "Date: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# Check if we're in the right directory
if (-not (Test-Path "CmaCgmTestClientClean.class")) {
    Write-Host "Compiling Java test client..." -ForegroundColor Yellow
    javac CmaCgmTestClientClean.java
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to compile Java test client" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Running CMA-CGM integration test..." -ForegroundColor Yellow
Write-Host ""

# Run the Java test client
java CmaCgmTestClientClean

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== TEST COMPLETED SUCCESSFULLY ===" -ForegroundColor Green
    Write-Host "Connection to CMA-CGM technical teams verified!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Confirm with Sophie RODIER that data was received" -ForegroundColor White
    Write-Host "2. Integrate with real balise data from Maxvision SDK" -ForegroundColor White
    Write-Host "3. Set up automated data transmission" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "=== TEST FAILED ===" -ForegroundColor Red
    Write-Host "Check the error messages above for details" -ForegroundColor Red
}
