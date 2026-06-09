#!/bin/bash
# =============================================
# setup-prod.sh - Configuracao do servidor Oracle
# =============================================
# Executar UMA VEZ na maquina Oracle para configurar
# o servico systemd e o Nginx.
# Use: chmod +x setup-prod.sh && sudo ./setup-prod.sh

set -e

echo "=== Configurando ambiente de producao Barbearia ==="

# 1. Nginx config
echo "[1/3] Instalando config do Nginx..."
cp deploy/nginx/barbearia.conf /etc/nginx/conf.d/barbearia.conf
nginx -t
systemctl reload nginx

# 2. Systemd service
echo "[2/3] Instalando servico systemd..."
cat > /etc/systemd/system/barbearia-backend.service << 'SERVICEEOF'
[Unit]
Description=Barbearia Backend Spring Boot
After=network.target postgresql.service

[Service]
Type=simple
User=opc
WorkingDirectory=/home/opc
ExecStart=/usr/bin/java -jar /home/opc/backend.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
SERVICEEOF

systemctl daemon-reload
systemctl enable barbearia-backend.service

# 3. Criar diretorio do frontend
echo "[3/3] Preparando diretorio do frontend..."
mkdir -p /usr/share/nginx/html

echo ""
echo "=== Configuracao concluida! ==="
echo "Proximos passos:"
echo "1. Copie o JAR para /home/opc/backend.jar"
echo "2. Copie os arquivos do frontend para /usr/share/nginx/html/"
echo "3. Crie /home/opc/.env com as credenciais"
echo "4. sudo systemctl start barbearia-backend.service"
echo ""
