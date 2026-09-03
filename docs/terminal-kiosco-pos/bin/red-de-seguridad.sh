#!/bin/bash
set -uo pipefail
echo "=== logind: autovt ==="
grep -E '^\s*#?\s*(NAutoVTs|ReserveVT)' /etc/systemd/logind.conf 2>/dev/null || echo "  (valores por defecto: NAutoVTs=6 ReserveVT=6)"
echo
echo "=== habilitando gettys permanentes en tty2 y tty3 (rescate garantizado) ==="
systemctl enable --now getty@tty2.service getty@tty3.service
echo
echo "=== deteniendo el kiosco de prueba (se relevanta solo en el reboot) ==="
systemctl stop pos-kiosk.service
echo
echo "=== ESTADO ==="
for u in getty@tty2 getty@tty3 pos-web pos-kiosk; do
  printf '%-16s enabled=%-10s active=%s\n' "$u" "$(systemctl is-enabled $u.service 2>&1)" "$(systemctl is-active $u.service 2>&1)"
done
echo "default target: $(systemctl get-default)"
