#!/usr/bin/env bash
# fase3-ignorar-mm.sh - Que ModemManager deje en paz al CH340 de la bascula.
#
#   sudo bash fase3-ignorar-mm.sh
#
# ModemManager sondea los puertos serie nuevos: los abre y les manda comandos AT
# para ver si son un modem. Mientras lo hace se come los bytes que manda la
# bascula. En Windows esto no pasa porque no existe ese servicio: por eso "solo
# la conecte y funciono".
#
# Este equipo NO tiene modem WWAN (ver Fase 1), asi que la marca ID_MM_DEVICE_IGNORE
# sobre el 1a86:7523 no le quita nada a nadie.

set -euo pipefail
REGLA=/etc/udev/rules.d/99-pos-bascula.rules

[[ $EUID -eq 0 ]] || { echo "Corre con sudo."; exit 1; }

cat > "$REGLA" <<'RULE'
# Bascula POS sobre adaptador CH340 (QinHeng 1a86:7523).
# 1) ModemManager no debe sondearlo: no es un modem y su sondeo se traga la trama.
# 2) Alias estable /dev/bascula, para no depender del orden de enumeracion.
SUBSYSTEM=="tty", ATTRS{idVendor}=="1a86", ATTRS{idProduct}=="7523", \
  ENV{ID_MM_DEVICE_IGNORE}="1", ENV{ID_MM_PORT_IGNORE}="1", \
  SYMLINK+="bascula", GROUP="dialout", MODE="0660"
RULE

udevadm control --reload-rules
udevadm trigger --subsystem-match=tty --action=add

echo "OK   regla escrita en $REGLA"
echo "     Desconecta y vuelve a conectar el USB de la bascula, y comprueba:"
echo "       ls -l /dev/bascula"
echo "       udevadm info -q property -n /dev/ttyUSB0 | grep ID_MM"
echo
echo "     Este equipo no tiene modem WWAN. Si no vuelve a hacer falta,"
echo "     en la Fase 4 se puede quitar del todo:"
echo "       sudo systemctl disable --now ModemManager"
