#!/usr/bin/env bash
# Guia las 4 medidas del serie en el orden que descarta mas cosas por paso.
# Aprovecha que el cable ya esta desconectado para sacar la referencia gratis.
#   sudo bash fase3-loopback-guiado.sh
set -uo pipefail

AQUI="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEV="${1:-/dev/ttyUSB0}"
PY="$AQUI/fase3-bascula.py"

c()  { printf '\n\033[1m== %s\033[0m\n' "$*"; }
ok() { printf '   \033[32mOK\033[0m   %s\n' "$*"; }
i_() { printf '        %s\n' "$*"; }

pausa() {                      # no sigue hasta que el hardware este como toca
  printf '\n   >> %s\n   >> Pulsa ENTER cuando este listo (o Ctrl-C para salir): ' "$1"
  read -r _ </dev/tty
}

[[ $EUID -eq 0 ]] || { echo "Corre con sudo."; exit 1; }
[[ -e $DEV ]] || { echo "$DEV no existe. Esta enchufado el adaptador USB?"; exit 1; }

# ModemManager solo sondea al enchufar, pero si el adaptador se reconecta a
# media prueba puede robar el puerto y dar un falso 'no vuelve nada'.
MM=no
if systemctl is-active --quiet ModemManager; then
  systemctl stop ModemManager && MM=si && ok "ModemManager parado durante las pruebas"
fi
restaurar() { [[ $MM == si ]] && systemctl start ModemManager && echo "   (ModemManager rearrancado)"; }
trap restaurar EXIT

c "1/4  Lineas de control CON la bascula conectada"
i_ "No toques nada: deja el cable como esta ahora."
pausa "Bascula ENCENDIDA y su cable enchufado al adaptador"
python3 "$PY" "$DEV" --lineas

c "2/4  Lineas de control SIN la bascula (referencia)"
pausa "Desenchufa el cable de la bascula DEL ADAPTADOR (deja el USB puesto)"
python3 "$PY" "$DEV" --lineas
i_ "Si 1 y 2 salen identicos, la bascula no mueve ninguna linea de control."

c "3/4  Loopback en el DB9 del ADAPTADOR (prueba el adaptador solo)"
i_ "Dobla la grapa en U, unos 3 mm entre patas."
i_ "Pin 3 = el del MEDIO de la fila de 5. Pin 2 = el de al lado."
i_ "Si no hay eco, mueve la grapa un pin hacia el otro lado y repite:"
i_ "puentear 3-4 por error no rompe nada, solo no hace eco."
pausa "Grapa puenteando los pines 2 y 3 del DB9 del adaptador"
python3 "$PY" "$DEV" --loopback

c "4/4  Loopback en el EXTREMO LEJANO del cable (prueba adaptador + cable)"
i_ "Ahora el eco tiene que ir y volver por todo el cable serie."
pausa "Cable enchufado al adaptador, y la grapa en los pines 2-3 del OTRO extremo (el que va a la bascula)"
python3 "$PY" "$DEV" --loopback

c "Como se lee esto"
i_ "3 hace eco y 4 tambien  -> adaptador y cable SANOS. Culpa de la bascula:"
i_ "                           salida serie apagada en su menu, o TX/RX cruzados"
i_ "                           (prueba un adaptador null-modem)."
i_ "3 hace eco y 4 NO       -> el CABLE serie esta roto o mal cableado."
i_ "3 NO hace eco           -> el adaptador CH340 o su conector estan mal."
i_ "Ademas: si en el paso 1 habia alguna linea ALTA y en el 2 no, la bascula"
i_ "        si esta viva electricamente aunque su TX calle."
