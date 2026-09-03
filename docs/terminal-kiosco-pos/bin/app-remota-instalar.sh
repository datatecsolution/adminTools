#!/bin/bash
# APP REMOTA — apunta el kiosco a https://admintools.supermercadosurbina.com/
# y le concede de antemano la impresora (WebUSB) y la bascula (WebSerial).
# Idempotente. Corre como root o como adminpos (usa sudo).
#
# NO instala la regla que suelta usblp: eso solo hace falta si WebUSB no puede
# reclamar la interfaz de la impresora, y romperia /dev/usb/lp0. Ver el paso 5.
set -euo pipefail

S=/home/adminpos/pos-terminal
if [ "$(id -u)" -eq 0 ]; then SUDO=""; else SUDO="sudo"; fi

echo "### 1/5 · Unidad del kiosco -> app remota + espera de red"
$SUDO install -o root -g root -m 644 "$S/etc/pos-kiosk.service" /etc/systemd/system/
$SUDO systemctl daemon-reload
grep -E '^(Environment=POS_URL|Wants=network-online)' /etc/systemd/system/pos-kiosk.service | sed 's/^/     /'

echo "### 2/5 · Politica de Chromium (permisos de hardware sin selector)"
$SUDO install -d -o root -g root -m 755 /etc/chromium/policies/managed
$SUDO install -o root -g root -m 644 "$S/etc/chromium-pos.json" \
      /etc/chromium/policies/managed/chromium-pos.json

echo "### 3/5 · Regla udev de acceso al USB crudo"
$SUDO install -o root -g root -m 644 "$S/etc/99-pos-webusb.rules" \
      /etc/udev/rules.d/99-pos-webusb.rules
$SUDO udevadm control --reload
$SUDO udevadm trigger --subsystem-match=usb --attr-match=idVendor=04b8
$SUDO udevadm trigger --subsystem-match=usb --attr-match=idVendor=1a86

echo "### 4/5 · Comprobaciones"
echo -n "     app alcanzable: "
curl -s -o /dev/null -w 'HTTP %{http_code} en %{time_total}s\n' -L --max-time 20 \
     https://admintools.supermercadosurbina.com/ || echo "FALLO (revisar red)"
echo -n "     caja1 en lp y dialout: "
id -nG caja1 | tr ' ' '\n' | grep -cx -e lp -e dialout | xargs -I{} echo "{}/2"
for n in /dev/ttyUSB0 /dev/usb/lp0; do [ -e "$n" ] && echo "     $n  $(stat -c '%U:%G %a' $n)"; done

echo "### 5/5 · Listo — falta reiniciar"
cat <<'TXT'

     sudo reboot

     Tras el arranque, en la pantalla del kiosco:
       Configuracion -> Bascula   : protocolo "Torrey (a demanda)", 9600 8N1
                                    (viene en "simulator", que INVENTA pesos)
       Configuracion -> Impresora : emparejar la Epson

     Si al emparejar la impresora WebUSB dice que no puede reclamar la
     interfaz, entonces —y solo entonces— hace falta soltar usblp:
       sudo bash ~/usblp.sh
     (eso hace desaparecer /dev/usb/lp0 y con el fase3-impresora.py;
      para deshacerlo:  sudo bash ~/usblp.sh --quitar)
TXT
