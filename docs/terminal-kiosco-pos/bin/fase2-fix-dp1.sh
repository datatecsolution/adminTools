#!/bin/bash
# Fase 2 — Apaga el conector DRM fantasma DP-1 y guarda el journal del kiosco.
#
# Por qué: este CX20 tiene un solo panel (eDP-1, BOE NV156FHM-N22, 1920x1080),
# pero el board reporta ademas DP-1 como "connected" con el EDID de un panel de
# 17.3" que no esta montado. cage 0.2.0 no sabe elegir salida: arma un layout de
# 3200x1080 con las dos, la pagina queda corrida a la derecha y el tactil mapea
# con un factor 3200/1920 = 1.67, o sea que los toques caen fuera de los botones.
#
# Uso:  sudo /home/adminpos/pos-terminal/bin/fase2-fix-dp1.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo $0" >&2
  exit 1
fi

GRUB=/etc/default/grub
PARAM="video=DP-1:d"
LOG=/home/adminpos/kiosk-boot.log

echo "== 1/4 · Guardando el journal del kiosco en $LOG"
journalctl -u pos-kiosk.service -b --no-pager -o short-precise > "$LOG" 2>&1 || true
journalctl -u pos-web.service   -b --no-pager -o short-precise >> "$LOG" 2>&1 || true
chown adminpos:adminpos "$LOG"
echo "   $(wc -l < "$LOG") lineas guardadas"

echo "== 2/4 · Verificando que DP-1 exista y que la consola NO este ahi"
if [ ! -d /sys/class/drm/card0-DP-1 ]; then
  echo "   !! No existe card0-DP-1. Abortando sin tocar nada." >&2
  exit 1
fi
echo "   eDP-1: $(cat /sys/class/drm/card0-eDP-1/status)  ·  DP-1: $(cat /sys/class/drm/card0-DP-1/status)"
echo "   framebuffer de consola: $(cat /sys/class/graphics/fb0/virtual_size)  (debe ser 1920,1080 = eDP-1)"

echo "== 3/4 · Aplicando '$PARAM' en $GRUB"
if grep -q "$PARAM" "$GRUB"; then
  echo "   ya estaba puesto, no toco nada"
else
  cp -n "$GRUB" "$GRUB.bak"
  echo "   backup: $GRUB.bak"
  sed -i -E "s/^(GRUB_CMDLINE_LINUX_DEFAULT=\")(.*)(\")$/\1\2 $PARAM\3/" "$GRUB"
  sed -i -E "s/^(GRUB_CMDLINE_LINUX_DEFAULT=\") +/\1/" "$GRUB"   # por si estaba vacio
fi
grep '^GRUB_CMDLINE' "$GRUB" | sed 's/^/   /'

if ! grep -q "^GRUB_CMDLINE_LINUX_DEFAULT=.*$PARAM" "$GRUB"; then
  echo "   !! No quedo aplicado. Revisa $GRUB a mano. Backup en $GRUB.bak" >&2
  exit 1
fi

echo "== 4/4 · Regenerando grub.cfg"
update-grub

echo
echo "LISTO. Ahora:  sudo reboot"
echo
echo "Si el arranque queda en negro: en el menu de GRUB apreta 'e', borra"
echo "  '$PARAM' de la linea que empieza con 'linux', y Ctrl+X para ese arranque."
echo "  Para revertir del todo:  sudo cp $GRUB.bak $GRUB && sudo update-grub"
