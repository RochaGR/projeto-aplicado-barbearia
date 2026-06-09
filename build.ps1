# =============================================
# build.ps1 - Build do Barbearia (Angular + Spring Boot)
# =============================================

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDist = "$rootDir/barbearia-front/dist/barbearia-front/browser"
$jarSource = "$rootDir/barbearia/target/agendamento-0.0.1-SNAPSHOT.jar"
$pkgDir = "$rootDir/deploy/package"

Write-Host "=== Build do Barbearia ===" -ForegroundColor Cyan

# 1. Build Angular
Write-Host "[1/3] Build Angular..." -ForegroundColor Yellow
Set-Location "$rootDir/barbearia-front"
npm install --silent
ng build --configuration production
if ($LASTEXITCODE -ne 0) { throw "Angular build failed" }
Write-Host "  -> $frontendDist" -ForegroundColor Green

# 2. Copiar Angular para o static/ do Spring (opcional - para servir via backend)
Write-Host "[2/3] Preparando deploy package..." -ForegroundColor Yellow
$staticDir = "$rootDir/barbearia/src/main/resources/static"
if (Test-Path $staticDir) { Remove-Item -Recurse -Force $staticDir }
New-Item -ItemType Directory -Path $staticDir -Force | Out-Null
Copy-Item -Recurse "$frontendDist/*" $staticDir

# 3. Build Spring Boot JAR
Write-Host "[3/3] Build Spring Boot..." -ForegroundColor Yellow
Set-Location "$rootDir/barbearia"
.\mvnw.cmd clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }
Write-Host "  -> $jarSource" -ForegroundColor Green

Write-Host ""
Write-Host "========================" -ForegroundColor Cyan
Write-Host "Build concluido!" -ForegroundColor Green
Write-Host "========================" -ForegroundColor Cyan
Write-Host ""
Write-Host "COMANDOS PARA DEPLOY:" -ForegroundColor Cyan
Write-Host '1. Enviar JAR e frontend:' -ForegroundColor Yellow
Write-Host "   scp "$jarSource" opc@168.138.147.219:/home/opc/backend.jar" -ForegroundColor White
Write-Host "   scp -r $frontendDist/* opc@168.138.147.219:/usr/share/nginx/html/" -ForegroundColor White
Write-Host "   scp $rootDir/.env opc@168.138.147.219:/home/opc/.env" -ForegroundColor White
Write-Host ""
Write-Host '2. Na Oracle, reiniciar:' -ForegroundColor Yellow
Write-Host "   ssh opc@168.138.147.219 'sudo systemctl restart barbearia-backend.service'" -ForegroundColor White
Write-Host ""
Write-Host '3. Se for a primeira vez, rodar o setup:' -ForegroundColor Yellow
Write-Host "   ssh opc@168.138.147.219 'sudo bash deploy/setup-prod.sh'" -ForegroundColor White
