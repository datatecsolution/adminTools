#!/bin/bash
# FASE 4 · Bloque D — Encerrar al cajero en la app.
#
# Dos puertas que hoy siguen abiertas con el teclado puesto:
#   1. cage se lanza con -s, que permite Ctrl+Alt+F2 y saltar a una consola de
#      login. No podria entrar sin contrasena, pero sale de la app y ve el
#      sistema. Se quita el -s.
#   2. Ctrl+Alt+Supr reinicia la maquina en mitad de una venta. Se enmascara
#      ctrl-alt-del.target.
#
# Se ejecuta DESPUES de SSH y ANTES de overlayroot: a partir de aqui la unica
# via de entrada comoda es SSH, y todavia el disco esta escribible por si hay
# que deshacer.
#
# DESHACER (si te quedas sin app y sin SSH): en el menu de GRUB, tecla 'e',
# anadir  systemd.unit=rescue.target  a la linea del kernel, Ctrl+X.
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-cerrar-consola.sh
#       sudo bash .../fase4-cerrar-consola.sh --abrir   (vuelve a poner el -s)
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0" >&2
  exit 1
fi

S=/home/adminpos/pos-terminal
SRC=$S/bin/pos-kiosk-start
DST=/opt/pos/bin/pos-kiosk-start
MODO="${1:---cerrar}"

if [ "$MODO" = "--abrir" ]; then
  echo "== Reabriendo la via de rescate"
  sed -i 's/^exec cage -d -- /exec cage -d -s -- /' "$SRC"
  systemctl unmask ctrl-alt-del.target >/dev/null 2>&1 || true
else
  echo "== 1/3 · Quitando el -s de cage (bloquea el cambio de terminal virtual)"
  sed -i 's/^exec cage -d -s -- /exec cage -d -- /' "$SRC"
  sed -i 's|^# -s = permite.*|# SIN -s (Fase 4): el cajero no puede cambiar de terminal virtual.\n# Para reabrirlo:  sudo bash ~/pos-terminal/bin/fase4-cerrar-consola.sh --abrir|' "$SRC"
  echo "== 2/3 · Enmascarando Ctrl+Alt+Supr"
  systemctl mask ctrl-alt-del.target >/dev/null 2>&1 || true
fi

grep -n '^exec cage' "$SRC" | sed 's/^/   /'

echo "== 3/3 · Desplegando y reiniciando el kiosco"
install -o root -g root -m 755 "$SRC" "$DST"
systemctl restart pos-kiosk.service
sleep 3
echo -n "   kiosco: "; systemctl is-active pos-kiosk.service
echo    "   procesos: cage=$(pgrep -c cage || echo 0)  chromium=$(pgrep -c chromium || echo 0)"

cat <<'TXT'

   Comprueba EN LA PANTALLA de la caja que la app volvio a cargar y que
   Ctrl+Alt+F2 ya no hace nada. Si la app no vuelve, deshaz con:

       sudo bash ~/pos-terminal/bin/fase4-cerrar-consola.sh --abrir

   Siguiente (el ultimo):  sudo bash ~/pos-terminal/bin/fase4-overlayroot.sh
TXT
