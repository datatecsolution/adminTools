#!/bin/bash
# FASE 2 — Kiosco. Idempotente. NO purga GNOME (eso va despues, ya probado el kiosco).
# Corre como root (via pkexec) o como adminpos (usa sudo).
set -euo pipefail

S=/home/adminpos/pos-terminal
if [ "$(id -u)" -eq 0 ]; then SUDO=""; else SUDO="sudo"; fi

echo "### 1/7 · Instalando cage + chromium + libinput-tools"
$SUDO apt-get update -qq
$SUDO env DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
     cage chromium libinput-tools

echo "### 2/7 · Desplegando /opt/pos"
$SUDO install -d -o root -g root -m 755 /opt/pos /opt/pos/bin /opt/pos/www
$SUDO install -o root -g root -m 755 "$S/bin/pos-kiosk-start" /opt/pos/bin/pos-kiosk-start
$SUDO install -o root -g root -m 644 "$S/www/index.html"     /opt/pos/www/index.html

echo "### 3/7 · Instalando unidades systemd"
$SUDO install -o root -g root -m 644 "$S/etc/pos-web.service"   /etc/systemd/system/
$SUDO install -o root -g root -m 644 "$S/etc/pos-kiosk.service" /etc/systemd/system/
$SUDO systemctl daemon-reload

echo "### 4/7 · tty1 para el kiosco (tty2-tty6 quedan como rescate)"
$SUDO systemctl disable --now getty@tty1.service 2>/dev/null || true

echo "### 5/7 · Arranque en consola, sin GDM"
$SUDO systemctl set-default multi-user.target
$SUDO systemctl disable gdm.service 2>/dev/null || true

echo "### 6/7 · Habilitando servicios POS"
$SUDO systemctl enable pos-web.service pos-kiosk.service

echo "### 7/7 · Levantando el servidor de prueba"
$SUDO systemctl start pos-web.service
sleep 1
python3 - <<'PY' || true
import urllib.request
try:
    r = urllib.request.urlopen("http://localhost:8080/", timeout=5)
    print(f"pagina de prueba: HTTP {r.status} · {len(r.read())} bytes")
except Exception as e:
    print("pagina de prueba: FALLO ->", e)
PY

echo
echo "=== ESTADO FINAL ==="
for u in pos-web.service pos-kiosk.service gdm.service getty@tty1.service; do
  printf '%-22s %s\n' "$u" "$(systemctl is-enabled "$u" 2>&1)"
done
echo "default target: $(systemctl get-default)"
echo "cage:     $(cage --version 2>&1 | head -1)"
echo "chromium: $(chromium --version 2>&1 | head -1)"
echo
echo ">>> El kiosco NO se arranca ahora (tomaria tty1 y competiria con tu sesion)."
echo ">>> Para probarlo:  sudo reboot"
