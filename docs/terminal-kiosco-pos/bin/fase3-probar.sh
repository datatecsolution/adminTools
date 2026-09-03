#!/usr/bin/env bash
# fase3-probar.sh - Deteccion y prueba del hardware POS (Fase 3)
#
#   sudo bash fase3-probar.sh inventario   # solo mira, no toca nada
#   sudo bash fase3-probar.sh bascula      # escucha /dev/ttyUSB0 a varias velocidades
#   sudo bash fase3-probar.sh lineas       # CTS/DSR/DCD: hay algo vivo al otro lado?
#   sudo bash fase3-probar.sh loopback     # puentea pines 2-3 del DB9: prueba el cable
#   sudo bash fase3-probar.sh imprimir     # ticket de prueba en la Epson
#   sudo bash fase3-probar.sh gaveta       # pulso de apertura de gaveta (RJ11 de la impresora)
#   sudo bash fase3-probar.sh todo         # inventario + bascula + imprimir
#
# No modifica el sistema: no instala, no crea reglas udev, no toca grupos.

set -uo pipefail

IMPRESORA=/dev/usb/lp0
BASCULA=/dev/ttyUSB0

c()  { printf '\n\033[1m== %s\033[0m\n' "$*"; }
ok() { printf '   OK   %s\n' "$*"; }
no() { printf '   --   %s\n' "$*"; }

[[ $EUID -eq 0 ]] || { echo "Corre con sudo."; exit 1; }

# ------------------------------------------------------------------ inventario
inventario() {
  c "Inventario del hardware POS"

  echo "   --- USB ---"
  lsusb | sed 's/^/   /'

  echo
  echo "   --- nodos ---"
  for d in "$IMPRESORA" "$BASCULA"; do
    if [[ -e $d ]]; then
      ok "$d  ->  $(stat -c '%U:%G %a' "$d")"
    else
      no "$d  NO EXISTE"
    fi
  done

  echo
  echo "   --- ruta fisica (para la regla udev) ---"
  # Ojo: los nodos usbmisc (la impresora) NO reciben propiedades ID_* de udev,
  # asi que `info -q property` sale vacio para /dev/usb/lp0. Los datos que
  # sirven para la regla estan en los ATTRS del dispositivo USB padre, y esos
  # solo los da `info -a`. Por eso se leen las dos cosas.
  for d in "$IMPRESORA" "$BASCULA"; do
    [[ -e $d ]] || continue
    echo "   $d:"
    udevadm info -q property -n "$d" 2>/dev/null \
      | grep -E '^(ID_VENDOR_ID|ID_MODEL_ID|ID_SERIAL_SHORT|ID_PATH)=' \
      | sed 's/^/      /'
    udevadm info -a -n "$d" 2>/dev/null \
      | awk '/ATTRS\{idVendor\}/ && !v { v=$0; print }
             /ATTRS\{idProduct\}/ && !p { p=$0; print }
             /ATTRS\{serial\}/ && !s && $0 !~ /0000:00/ { s=$0; print }' \
      | sed 's/^ *//; s/^/      /'
  done

  echo
  echo "   --- quien puede abrirlos ---"
  for u in caja1 adminpos; do
    echo "   $(id "$u" 2>/dev/null || echo "$u: no existe")"
  done
  echo "   (caja1 necesita 'lp' para la impresora y 'dialout' para la bascula)"
}

# -------------------------------------------------------------------- bascula
# La mayoria de las basculas de mostrador emiten peso en continuo (streaming).
# Otras solo contestan si se les manda un byte de peticion. La sonda prueba las
# dos cosas. Va en Python porque con `cat` no se puede esperar de verdad sobre
# un tty: con VMIN=0/VTIME=0 una lectura sin datos devuelve 0 bytes y cat lo
# toma por fin de fichero, asi que sale al instante sin escuchar nada.
bascula() {
  c "Bascula en $BASCULA"
  python3 "$(dirname "${BASH_SOURCE[0]}")/fase3-bascula.py" "$BASCULA" "$@"
}

# ------------------------------------------------------------------- imprimir
imprimir() {
  c "Ticket de prueba en $IMPRESORA (Epson TM-T20IV-SP)"

  if [[ ! -e $IMPRESORA ]]; then
    no "$IMPRESORA no existe. Esta encendida la impresora?"
    return 1
  fi

  {
    printf '\x1b\x40'              # ESC @  - init
    printf '\x1b\x74\x10'          # ESC t 16 - codepage WPC1252 (para acentos)
    printf '\x1b\x61\x01'          # ESC a 1 - centrado
    printf '\x1b\x21\x30'          # ESC ! 48 - doble alto y ancho
    printf 'POS caja1\n'
    printf '\x1b\x21\x00'          # ESC ! 0 - normal
    printf 'prueba de impresion\n'
    printf '\x1b\x61\x00'          # ESC a 0 - izquierda
    printf -- '--------------------------------\n'
    printf 'Fecha : %s\n' "$(date '+%Y-%m-%d %H:%M:%S')"
    printf 'Equipo: %s\n' "$(hostname)"
    printf 'Nodo  : %s\n' "$IMPRESORA"
    printf -- '--------------------------------\n'
    printf 'ASCII : ABCDEFGHIJ 0123456789\n'
    printf 'Acento: %b\n' '\xe1\xe9\xed\xf3\xfa \xf1 \xdc \xa1! \xbf?'
    printf 'Ancho : 0123456789012345678901234567890123456789012\n'
    printf -- '--------------------------------\n'
    printf '\x1b\x61\x01'
    printf 'si lees esto, la impresora\n'
    printf 'funciona sin CUPS\n'
    printf '\n\n\n\n'
    printf '\x1d\x56\x42\x00'      # GS V 66 0 - corte parcial con avance
  } > "$IMPRESORA"

  local rc=$?
  if [[ $rc -eq 0 ]]; then
    ok "Bytes enviados sin error. Mira el papel:"
    echo "      - Salio el ticket y corto?"
    echo "      - La linea 'Acento' se ve como  aeiou n U !  ?"
    echo "      - La linea 'Ancho' entra completa o se corta?  (dice cuantas"
    echo "        columnas tiene el papel: 42 = 80mm fuente A, 32 = 58mm)"
  else
    no "Fallo al escribir en $IMPRESORA (codigo $rc)"
  fi
  return $rc
}

# --------------------------------------------------------------------- gaveta
gaveta() {
  c "Pulso de apertura de gaveta (por el RJ11 de la impresora)"
  if [[ ! -e $IMPRESORA ]]; then
    no "$IMPRESORA no existe."
    return 1
  fi
  printf '\x1b\x70\x00\x19\xfa' > "$IMPRESORA"   # ESC p 0 25 250 - pin 2
  ok "Enviado ESC p 0 (pin 2). Se abrio la gaveta?"
  sleep 1
  printf '\x1b\x70\x01\x19\xfa' > "$IMPRESORA"   # ESC p 1 25 250 - pin 5
  ok "Enviado ESC p 1 (pin 5). Y ahora?"
  echo "      Si no abrio con ninguno: la gaveta no esta conectada al RJ11,"
  echo "      o el cable no es el correcto (no todos los RJ11 son iguales)."
}

modo="${1:-todo}"; shift || true
case "$modo" in
  inventario) inventario ;;
  bascula)    bascula "$@" ;;
  loopback)   c "Loopback del adaptador serie"; bascula --loopback ;;
  lineas)     c "Lineas de control del puerto serie (sin puentes)"; bascula --lineas ;;
  imprimir)   imprimir ;;
  gaveta)     gaveta ;;
  todo)       inventario; bascula; imprimir ;;
  *) echo "uso: sudo bash $0 {inventario|bascula|lineas|loopback|imprimir|gaveta|todo}"; exit 1 ;;
esac

echo
