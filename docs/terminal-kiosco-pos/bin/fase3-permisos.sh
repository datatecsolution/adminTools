#!/bin/bash
# Fase 3 — Da acceso a caja1 a la impresora y a la bascula, y fija sus nombres.
#
# Por que: la prueba de la Fase 3 corrio con sudo y funciono, pero el backend
# POS corre como caja1, y caja1 no esta ni en 'lp' ni en 'dialout':
#
#     /dev/usb/lp0   root:lp       660      lp:x:7:        <- grupo vacio
#     /dev/ttyUSB0   root:dialout  660      dialout:x:20:  <- grupo vacio
#
# Sin esto el backend va a dar EACCES al abrir cualquiera de los dos.
# ('lpadmin', que caja1 si tiene, es de CUPS — y CUPS se purgo en la Fase 2.)
#
# Ademas fija dos nombres estables. Hoy /dev/ttyUSB0 depende del orden de
# enchufado, y el by-path cambia de puerto a puerto: al mover la bascula de USB
# paso de ...-usb-0:6:1.0 a ...-usb-0:7.2:1.0. El CH340 no trae numero de serie,
# asi que la regla va por idVendor/idProduct (basta mientras haya un solo CH340).
#
# Uso:  sudo /home/adminpos/pos-terminal/bin/fase3-permisos.sh
#       sudo /home/adminpos/pos-terminal/bin/fase3-permisos.sh --verificar
set -euo pipefail

REGLAS=/etc/udev/rules.d/60-pos.rules
CAJERO=caja1

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo $0" >&2
  exit 1
fi

verificar() {
  echo "== Verificacion"
  echo "   grupos de $CAJERO: $(id -nG "$CAJERO" 2>/dev/null || echo 'no existe')"
  for g in lp dialout; do
    if id -nG "$CAJERO" 2>/dev/null | tr ' ' '\n' | grep -qx "$g"; then
      echo "   OK   $CAJERO esta en '$g'"
    else
      echo "   --   $CAJERO NO esta en '$g'"
    fi
  done
  for d in /dev/usb/lp0 /dev/impresora-pos /dev/ttyUSB0 /dev/bascula; do
    if [ -e "$d" ]; then
      echo "   OK   $d  ->  $(stat -Lc '%U:%G %a' "$d")"
    else
      echo "   --   $d  no existe"
    fi
  done
  echo "   ModemManager: $(systemctl is-active ModemManager 2>/dev/null || true)"
  if [ -e /dev/ttyUSB0 ]; then
    echo "   ID_MM_DEVICE_IGNORE en ttyUSB0: $(udevadm info -q property -n /dev/ttyUSB0 2>/dev/null | grep -c '^ID_MM_DEVICE_IGNORE=1') (1 = MM lo deja en paz)"
  fi
}

if [ "${1:-}" = "--verificar" ]; then
  verificar
  exit 0
fi

echo "== 1/4 · Metiendo a $CAJERO en 'lp' y 'dialout'"
if ! id "$CAJERO" >/dev/null 2>&1; then
  echo "   !! el usuario $CAJERO no existe. Abortando." >&2
  exit 1
fi
for g in lp dialout; do
  if id -nG "$CAJERO" | tr ' ' '\n' | grep -qx "$g"; then
    echo "   ya estaba en '$g'"
  else
    usermod -aG "$g" "$CAJERO"
    echo "   agregado a '$g'"
  fi
done

echo "== 2/4 · Escribiendo $REGLAS"
[ -e "$REGLAS" ] && cp -n "$REGLAS" "$REGLAS.bak" && echo "   backup: $REGLAS.bak"
cat > "$REGLAS" <<'REGLAS_EOF'
# Hardware POS — Fase 3. Generado por fase3-permisos.sh, no editar a mano.

# Impresora termica Epson TM-T20IV-SP (kernel usblp, sin CUPS).
# El nodo vive en usbmisc y el kernel ya lo deja root:lp 660; se repite aqui
# para que no dependa de las reglas por defecto de la distro.
SUBSYSTEM=="usbmisc", KERNEL=="lp[0-9]*", ATTRS{idVendor}=="04b8", ATTRS{idProduct}=="0e39", GROUP="lp", MODE="0660", SYMLINK+="impresora-pos"

# Bascula por adaptador USB-serie CH340 (1a86:7523).
# ID_MM_DEVICE_IGNORE evita que ModemManager abra el puerto y le mande comandos
# AT al enchufarlo: udev lo marca hoy como ID_MM_CANDIDATE=1.
SUBSYSTEM=="tty", ATTRS{idVendor}=="1a86", ATTRS{idProduct}=="7523", GROUP="dialout", MODE="0660", SYMLINK+="bascula", ENV{ID_MM_DEVICE_IGNORE}="1"
REGLAS_EOF
sed 's/^/   /' "$REGLAS"

echo "== 3/4 · Recargando udev"
udevadm control --reload
udevadm trigger --subsystem-match=usbmisc --subsystem-match=tty
udevadm settle
echo "   recargado"

echo "== 4/4 · Verificando"
verificar

echo
echo "   OJO: los grupos de $CAJERO solo aplican en una sesion NUEVA."
echo "   El kiosco ya esta corriendo con los grupos viejos. Para que tome los"
echo "   nuevos hace falta reiniciar su sesion:"
echo
echo "       sudo systemctl restart pos-kiosk.service"
echo
echo "   (o un reboot, que ademas es la prueba de verdad). Recorda lo anotado"
echo "   en la Fase 2: cage y Chromium quedan en session-1.scope y no en el"
echo "   cgroup del servicio, asi que un restart puede dejar Chromium huerfano."
