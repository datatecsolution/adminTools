#!/bin/bash
# Activa la sesion del kiosco 15s via logind y vuelve SIEMPRE a la de adminpos.
set -uo pipefail

find_session() {  # $1 = usuario ; devuelve la sesion con VT asignado
  local u=$1 s
  for s in $(loginctl list-sessions --no-legend | awk '{print $1}'); do
    if [ "$(loginctl show-session "$s" -p Name --value 2>/dev/null)" = "$u" ] \
    && [ -n "$(loginctl show-session "$s" -p VTNr --value 2>/dev/null)" ] \
    && [ "$(loginctl show-session "$s" -p VTNr --value 2>/dev/null)" != "0" ]; then
      echo "$s"; return
    fi
  done
}

KIOSK=$(find_session caja1)
ADMIN=$(find_session adminpos)
echo "sesion kiosco=${KIOSK:-<ninguna>} (tty$(loginctl show-session ${KIOSK:-x} -p VTNr --value 2>/dev/null))"
echo "sesion admin =${ADMIN:-<ninguna>} (tty$(loginctl show-session ${ADMIN:-x} -p VTNr --value 2>/dev/null))"
[ -z "${KIOSK:-}" ] && { echo "ERROR: no encuentro la sesion del kiosco"; exit 1; }

( sleep 45; [ -n "${ADMIN:-}" ] && loginctl activate "$ADMIN" 2>/dev/null; chvt 2 2>/dev/null ) &
GUARD=$!
trap '[ -n "${ADMIN:-}" ] && loginctl activate "$ADMIN" 2>/dev/null; chvt 2 2>/dev/null' EXIT

echo "### activando sesion $KIOSK (tty1) — mirá la pantalla"
loginctl activate "$KIOSK"
sleep 15

echo
echo "=== VT activo: $(cat /sys/class/tty/tty0/active) ==="
echo "=== sesion kiosco Active: $(loginctl show-session $KIOSK -p Active --value) ==="
echo
echo "=== PROCESOS ==="
pgrep -a chromium | head -4 || echo "  chromium: NO CORRIENDO"
echo
systemctl status pos-kiosk.service --no-pager -n 0 2>&1 | grep -E 'Active:|Tasks:|Memory:'
echo
echo "=== JOURNAL ==="
journalctl -u pos-kiosk.service -n 30 --no-pager -o cat

echo
echo "### volviendo a la sesion de adminpos..."
[ -n "${ADMIN:-}" ] && loginctl activate "$ADMIN"
kill $GUARD 2>/dev/null
sleep 2
echo "=== VT activo final: $(cat /sys/class/tty/tty0/active) ==="
