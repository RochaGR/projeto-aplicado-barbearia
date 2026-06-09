# =============================================
# deploy.ps1 - Build + Deploy completo
# Uso: .\deploy.ps1
# Requer: PowerShell, npm, Java, Maven wrapper
# =============================================

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$SSH_KEY = "C:\Users\gustavo_g_rocha\Desktop\Oracle Projeto Aplicado\ssh-key-2026-05-11.key"
$VM_HOST = "opc@168.138.147.219"
$VM_DIR = "/home/opc"
$NGINX_HTML = "/usr/share/nginx/html"

# --- 1. BUILD ---
Write-Host "=== [1/4] Build Angular ===" -ForegroundColor Cyan
Set-Location "$rootDir/barbearia-front"
npm run build
if ($LASTEXITCODE -ne 0) { throw "Angular build failed" }

Write-Host "`n=== [2/4] Build Spring Boot JAR ===" -ForegroundColor Cyan
Set-Location "$rootDir/barbearia"
.\mvnw.cmd package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }

# --- 2. ENVIAR PARA VM ---
Write-Host "`n=== [3/4] Enviando arquivos para VM ===" -ForegroundColor Cyan

# JAR
& "C:\Windows\System32\OpenSSH\scp.exe" -i "$SSH_KEY" `
    "$rootDir/barbearia/target/agendamento-0.0.1-SNAPSHOT.jar" `
    "${VM_HOST}:${VM_DIR}/backend.jar"

# Frontend
& "C:\Windows\System32\OpenSSH\scp.exe" -i "$SSH_KEY" `
    -r "$rootDir/barbearia-front/dist/barbearia-front/browser/*" `
    "${VM_HOST}:${VM_DIR}/html/"

# .env (credenciais - necessario apenas na primeira vez)
# & "C:\Windows\System32\OpenSSH\scp.exe" -i "$SSH_KEY" "$rootDir/.env" "${VM_HOST}:${VM_DIR}/.env"

# Config Nginx (necessario apenas se mudar)
# & "C:\Windows\System32\OpenSSH\scp.exe" -i "$SSH_KEY" "$rootDir/deploy/nginx/barbearia.conf" "${VM_HOST}:${VM_DIR}/barbearia.conf"

# --- 3. APLICAR NA VM ---
Write-Host "`n=== [4/4] Aplicando na VM ===" -ForegroundColor Cyan

& "C:\Windows\System32\OpenSSH\ssh.exe" -i "$SSH_KEY" ${VM_HOST} @"
# 1. Mover frontend para o Nginx
sudo rm -rf ${NGINX_HTML}/*
sudo cp -r ${VM_DIR}/html/* ${NGINX_HTML}/
sudo rm -rf ${VM_DIR}/html

# 2. Atualizar Nginx (se tiver enviado config nova)
# sudo cp ${VM_DIR}/barbearia.conf /etc/nginx/conf.d/barbearia.conf
# sudo rm ${VM_DIR}/barbearia.conf
# sudo nginx -t && sudo systemctl reload nginx

# 3. Restart backend
sudo systemctl restart barbearia-backend.service

echo "DEPLOY_CONCLUIDO"
"@

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Deploy concluido com sucesso!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
