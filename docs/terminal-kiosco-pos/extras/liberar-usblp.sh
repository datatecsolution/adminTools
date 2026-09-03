#!/bin/bash
# Suelta usblp de la Epson TM-T20IV para que WebUSB reclame la interfaz.
# Persiste el fix en el DISCO REAL (overlayroot activo => /etc en vivo es RAM).
set -euo pipefail
RULE=/etc/udev/rules.d/99-pos-liberar-usblp.rules

# 1) desenganche en vivo (vale para esta sesión)
FOUND=0
for d in /sys/bus/usb/drivers/usblp/*:*; do
  [ -e "$d" ] || continue
  v=$(cat "$d/../idVendor" 2>/dev/null || true)
  p=$(cat "$d/../idProduct" 2>/dev/null || true)
  if [ "$v" = "04b8" ] && [ "$p" = "0e39" ]; then
    IF=$(basename "$d")
    echo -n "$IF" > /sys/bus/usb/drivers/usblp/unbind
    echo "[+] usblp desenganchado de $IF (en vivo)"
    FOUND=1
  fi
done
[ "$FOUND" = 0 ] && echo "[=] usblp ya no estaba enganchado a la Epson"

# 2) regla corregida en el sistema en vivo (por si reconectan el cable hoy)
sed -i 's/^ACTION=="add",/ACTION=="bind",/' "$RULE"
udevadm control --reload
echo "[+] Regla en vivo corregida (ACTION==bind)"

# 3) lo mismo en el DISCO REAL, para que sobreviva al reboot
overlayroot-chroot sh -c '
  RULE=/etc/udev/rules.d/99-pos-liberar-usblp.rules
  cp "$RULE" "$RULE.bak-antes-bind"
  sed -i "s/^ACTION==\"add\",/ACTION==\"bind\",/" "$RULE"
  grep -q "ACTION==\"bind\"" "$RULE" && echo "[+] Regla corregida en el disco real (persiste)"
'

echo "--- Verificación:"
ls -l /sys/bus/usb/devices/3-8.1:1.0/driver 2>/dev/null || echo "interfaz SIN driver (libre para WebUSB) ✔"
ls /dev/usb/lp0 2>/dev/null || echo "/dev/usb/lp0 ya no existe (esperado)"
