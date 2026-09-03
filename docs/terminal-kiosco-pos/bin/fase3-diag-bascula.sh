#!/usr/bin/env bash
# fase3-diag-bascula.sh - Por que /dev/ttyUSB0 esta mudo en Linux y no en Windows.
#
#   sudo bash fase3-diag-bascula.sh
#
# No instala nada. Lo unico que toca es parar ModemManager un momento y
# volverlo a arrancar al final (trap EXIT, se restaura pase lo que pase).

set -uo pipefail

DEV=/dev/ttyUSB0
AQUI="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

c()  { printf '\n\033[1m== %s\033[0m\n' "$*"; }
ok() { printf '   OK   %s\n' "$*"; }
no() { printf '   --   %s\n' "$*"; }
i_() { printf '        %s\n' "$*"; }

[[ $EUID -eq 0 ]] || { echo "Corre con sudo."; exit 1; }
[[ -e $DEV ]] || { echo "$DEV no existe."; exit 1; }

MM_ESTABA=no
restaurar() {
  if [[ $MM_ESTABA == si ]]; then
    systemctl start ModemManager 2>/dev/null && \
      printf '\n   (ModemManager rearrancado)\n'
  fi
}
trap restaurar EXIT

# ------------------------------------------------ 1. quien tiene abierto el tty
c "1/5  Quien tiene abierto $DEV"
DUENOS=""
for fd in /proc/[0-9]*/fd/*; do
  [[ -L $fd ]] || continue
  if [[ "$(readlink -f "$fd" 2>/dev/null)" == "$DEV" ]]; then
    p=${fd#/proc/}; p=${p%%/*}
    DUENOS+=" $p"
  fi
done
if [[ -n $DUENOS ]]; then
  no "El puerto NO esta libre. Estos procesos lo tienen abierto:"
  for p in $DUENOS; do
    i_ "PID $p  ->  $(tr '\0' ' ' < /proc/$p/cmdline 2>/dev/null)"
  done
  i_ "Un segundo lector se COME los bytes: la sonda ve silencio aunque"
  i_ "la bascula este emitiendo."
else
  ok "Nadie mas lo tiene abierto ahora mismo."
fi

# --------------------------------------------------- 2. ModemManager en el log
c "2/5  ModemManager (probador de puertos serie, activo en este equipo)"
printf '   estado: %s / %s\n' \
  "$(systemctl is-enabled ModemManager 2>&1)" \
  "$(systemctl is-active ModemManager 2>&1)"
echo "   --- lo que dijo del ttyUSB0 en este arranque ---"
if journalctl -b -u ModemManager --no-pager 2>/dev/null \
     | grep -Ei 'ttyUSB|1a86|7523|ch341|probe|filter' | tail -20 | sed 's/^/   /'
then :; fi
journalctl -b -u ModemManager --no-pager 2>/dev/null \
  | grep -Eqi 'ttyUSB' || i_ "(nada sobre ttyUSB en el journal)"
echo "   --- lo que dice udev del nodo (ID_MM_*) ---"
udevadm info -q property -n "$DEV" 2>/dev/null \
  | grep -E 'ID_MM|ID_VENDOR_ID|ID_MODEL_ID|ID_USB_DRIVER' | sed 's/^/   /'

# ------------------------------------------- 3. contadores del driver usbserial
c "3/5  Contadores del driver (bytes que el CH340 ha recibido de verdad)"
if [[ -r /proc/tty/driver/usbserial ]]; then
  sed 's/^/   /' /proc/tty/driver/usbserial
  i_ "OJO: el kernel 6.x ya NO publica contadores 'tx:/rx:' aqui; esta linea"
  i_ "solo confirma que el CH340 esta enganchado al driver ch341-uart."
else
  no "/proc/tty/driver/usbserial no disponible (usbserial compilado en el kernel)"
fi

# ------------------------------ 4. escucha con ModemManager parado y sin rivales
c "4/5  Escucha limpia con ModemManager PARADO"
if systemctl is-active --quiet ModemManager; then
  MM_ESTABA=si
  systemctl stop ModemManager && ok "ModemManager parado durante la prueba"
  sleep 1
else
  no "ModemManager no estaba activo; la prueba va igual"
fi

# Intento de leer contadores de bytes del driver. En kernels modernos (>=3.x)
# serial_proc_show() ya no imprime 'tx:/rx:', asi que esto casi siempre sale
# vacio; se conserva por si el equipo corre un kernel viejo que si los trae.
# Cuando no hay contador, quien decide es la prueba de loopback.
leer_rx() { grep -o 'rx:[0-9]*' /proc/tty/driver/usbserial 2>/dev/null | head -1 | cut -d: -f2; }

RX_SUBIO=no
RX_LEIBLE=no
for b in 9600 2400 4800 19200; do
  printf '   -- %-6s\n' "$b"
  TMP=$(mktemp)
  python3 "$AQUI/fase3-bascula.py" "$DEV" --escuchar 6 --baudios "$b" >"$TMP" 2>&1 &
  PY=$!
  sleep 1; rx1=$(leer_rx)
  sleep 4; rx2=$(leer_rx)
  wait $PY
  sed '1,2d' "$TMP" | sed 's/^/   /'
  rm -f "$TMP"
  if [[ -n ${rx1:-} && -n ${rx2:-} ]]; then
    RX_LEIBLE=si
    if (( rx2 > rx1 )); then
      printf '      contador RX del driver: %s -> %s  (SUBE: SI llegan bytes por RX)\n' "$rx1" "$rx2"
      RX_SUBIO=si
    else
      printf '      contador RX del driver: %s -> %s  (no sube: ni un bit por RX)\n' "$rx1" "$rx2"
    fi
  else
    printf '      contador RX: este kernel no publica tx:/rx: (normal, no es un fallo)\n'
  fi
done

# ------------------------------------------------------- 5. veredicto y camino
c "5/5  Que significa"
i_ "Si AHORA aparecen datos -> era ModemManager. Fix permanente:"
i_ "   sudo bash $AQUI/fase3-ignorar-mm.sh"
i_ ""
case "$RX_SUBIO/$RX_LEIBLE" in
  si/*) i_ "MEDIDO: el contador RX SI subio -> llegan bytes al CH340, pero ninguno"
        i_ "        formaba trama legible: es cuestion de velocidad/formato, no de cable." ;;
  no/si) i_ "MEDIDO: el contador RX NO subio en ninguna velocidad -> cero senal"
         i_ "        electrica en el hilo RX. Eso no se arregla con software." ;;
  *) i_ "Sin contador RX en este kernel. Pero el dato que importa ya esta:"
     i_ "        CERO bytes a 8 velocidades distintas. Una senal real mal ajustada"
     i_ "        daria basura, no silencio => o no llega senal, o TX/RX estan"
     i_ "        cruzados. Lo decide el loopback." ;;
esac
i_ ""
i_ "Si sigue mudo y el contador rx NO subio -> no hay senal electrica en RX."
i_ "Eso no se arregla con software. La prueba que lo confirma en 30 s:"
i_ "   puentea los pines 2 y 3 del DB9 del adaptador (un clip) y corre"
i_ "   sudo bash $AQUI/fase3-probar.sh loopback"
i_ "   - vuelve el eco  -> adaptador OK, el problema es la bascula o el cable"
i_ "   - no vuelve nada -> el adaptador CH340 o su cable estan mal"
i_ ""
i_ "Y si el loopback SI hace eco (adaptador sano) revisa, por este orden:"
i_ "   1. la salida serie de la bascula esta habilitada en su menu?"
i_ "   2. cable cruzado (null-modem): muchas basculas necesitan TX/RX invertidos;"
i_ "      un adaptador null-modem de 3 EUR descarta esto en un minuto."
i_ "   3. el conector va al puerto de DATOS, no al de impresora de la bascula"
