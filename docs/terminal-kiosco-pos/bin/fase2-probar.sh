#!/bin/bash
# Arranca el kiosco en tty1 y recoge diagnostico. Reversible: systemctl stop pos-kiosk
set -uo pipefail

echo "### VT activo antes: $(cat /sys/class/tty/tty0/active 2>/dev/null)"
systemctl start pos-kiosk.service
echo "### esperando 10s a que levante cage+chromium..."
sleep 10

echo
echo "=== STATUS ==="
systemctl status pos-kiosk.service --no-pager -n 0 2>&1 | head -12

echo
echo "=== SESIONES LOGIND ==="
loginctl list-sessions --no-legend

echo
echo "=== PROCESOS ==="
pgrep -a cage || echo "  cage: NO CORRIENDO"
pgrep -a chromium | head -3 || echo "  chromium: NO CORRIENDO"

echo
echo "=== VT activo ahora: $(cat /sys/class/tty/tty0/active 2>/dev/null) ==="

echo
echo "=== JOURNAL pos-kiosk (ultimas 40) ==="
journalctl -u pos-kiosk.service -n 40 --no-pager -o cat 2>&1
