#!/bin/bash
# LIBERAR USBLP — suelta la interfaz 0 de la Epson TM-T20IV del modulo usblp
# para que WebUSB pueda reclamarla desde el kiosco.
#
# Usar SOLO si al emparejar la impresora la app dice que no puede reclamar la
# interfaz ("Unable to claim interface"). Efecto colateral: desaparece
# /dev/usb/lp0 y con el deja de funcionar fase3-impresora.py.
#
#   sudo bash ~/usblp.sh            instala la regla
#   sudo bash ~/usblp.sh --quitar   la desinstala y devuelve /dev/usb/lp0
set -euo pipefail

S=/home/adminpos/pos-terminal
REGLA=/etc/udev/rules.d/99-pos-liberar-usblp.rules
if [ "$(id -u)" -eq 0 ]; then SUDO=""; else SUDO="sudo"; fi

if [ "${1:-}" = "--quitar" ]; then
    $SUDO rm -f "$REGLA"
    $SUDO udevadm control --reload
    echo "Regla quitada. Reinicia (sudo reboot) para recuperar /dev/usb/lp0."
    exit 0
fi

$SUDO install -o root -g root -m 644 "$S/etc/99-pos-liberar-usblp.rules" "$REGLA"
$SUDO udevadm control --reload
$SUDO udevadm trigger --subsystem-match=usb --attr-match=idVendor=04b8

echo -n "usblp enganchado ahora: "
ls /sys/bus/usb/drivers/usblp/ 2>/dev/null | grep -c ':' || echo 0
[ -e /dev/usb/lp0 ] && echo "/dev/usb/lp0 sigue ahi (desaparecera tras reiniciar)"

cat <<'TXT'

Regla instalada. Ahora:

    sudo reboot

y vuelve a emparejar la impresora en Configuracion -> Impresora.
Si no era esto, deshazlo con:  sudo bash ~/usblp.sh --quitar
TXT
