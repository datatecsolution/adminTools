#!/bin/bash
set -uo pipefail
( sleep 40; chvt 2 2>/dev/null ) & GUARD=$!
trap 'chvt 2 2>/dev/null' EXIT

CURSOR=$(journalctl -u pos-kiosk.service -n0 --show-cursor 2>/dev/null | tail -1 | sed 's/^-- cursor: //')
echo "t=0  chvt 1"
chvt 1
for i in $(seq 1 20); do
  sleep 1
  printf 't=%-3s VT=%-5s cage=%-6s chromium=%-3s activo=%s\n' \
    "$i" "$(cat /sys/class/tty/tty0/active)" \
    "$(pgrep -x cage | head -1 || echo NO)" \
    "$(pgrep -c chromium 2>/dev/null || echo 0)" \
    "$(loginctl show-session 6 -p Active --value 2>/dev/null || echo '?')"
done
echo
echo "### volviendo a tty2"; chvt 2; kill $GUARD 2>/dev/null; sleep 1
echo "VT final: $(cat /sys/class/tty/tty0/active)"
echo
echo "=== JOURNAL del servicio (todo) ==="
journalctl -u pos-kiosk.service --no-pager -o short-precise -n 60
echo
echo "=== NRestarts ==="; systemctl show pos-kiosk.service -p NRestarts --value
