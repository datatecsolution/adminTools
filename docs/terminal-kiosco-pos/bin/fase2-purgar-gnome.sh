#!/bin/bash
# Fase 2 (cierre) — Purga el escritorio GNOME que quedo instalado pero muerto.
#
# Por que: se eligio la opcion (a) (kiosco minimo con cage). El default target es
# multi-user.target y el symlink display-manager.service esta borrado, asi que GDM y
# GNOME ya no arrancan: son ~51 paquetes ocupando /, que solo tiene 19G. Se dejaron
# instalados a proposito como escritorio de rescate hasta validar el kiosco.
# Validado el 2026-09-01 12:54 -> ya se pueden sacar.
#
# La red de seguridad real pasa a ser: gettys en tty2/tty3 + cage con '-s'.
#
# Uso:
#   sudo /home/adminpos/pos-terminal/bin/fase2-purgar-gnome.sh            # simulacro
#   sudo /home/adminpos/pos-terminal/bin/fase2-purgar-gnome.sh --aplicar  # de verdad
set -euo pipefail

APLICAR=0
[ "${1:-}" = "--aplicar" ] && APLICAR=1

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo $0 ${1:-}" >&2
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
LOG=/home/adminpos/purga-gnome.log

# --- 1/5 · Guardas: no purgar si el kiosco no esta realmente en pie ------------
echo "== 1/5 · Verificando que el kiosco este validado y el rescate en pie"
fallo=0
chk() { # chk <descripcion> <condicion-ok>
  if eval "$2" >/dev/null 2>&1; then echo "   OK   $1"; else echo "   FALLA $1"; fallo=1; fi
}
chk "default target = multi-user.target"      '[ "$(systemctl get-default)" = multi-user.target ]'
chk "pos-kiosk.service enabled"               'systemctl is-enabled -q pos-kiosk.service'
chk "pos-kiosk.service active"                'systemctl is-active  -q pos-kiosk.service'
chk "pos-web.service active"                  'systemctl is-active  -q pos-web.service'
chk "chromium corriendo"                      '[ "$(pgrep -c chromium)" -gt 0 ]'
chk "getty@tty2 activo (rescate)"             'systemctl is-active -q getty@tty2.service'
chk "getty@tty3 activo (rescate)"             'systemctl is-active -q getty@tty3.service'
chk "cage con -s (Ctrl+Alt+Fn habilitado)"    'grep -q -- " -s " /opt/pos/bin/pos-kiosk-start'
chk "sin display-manager.service"             '[ ! -e /etc/systemd/system/display-manager.service ]'
if [ "$fallo" -ne 0 ]; then
  echo
  echo "!! Alguna guarda fallo. NO se purga nada: si algo del kiosco esta mal," >&2
  echo "   GNOME sigue siendo la unica via de vuelta a un escritorio." >&2
  exit 1
fi

# --- 2/5 · Proteger lo que el kiosco necesita ---------------------------------
# apt autoremove borra todo lo 'auto' que quede huerfano. Marcar manual lo
# imprescindible evita que se lo lleve por delante junto con GNOME.
echo
echo "== 2/5 · Marcando como manual lo que el kiosco necesita"
# OJO: sudo, network-manager y wpasupplicant estan marcados 'auto' y cuelgan de
# task-desktop. Sin este paso, la purga se los lleva -> equipo sin Wi-Fi y sin sudo.
# Verificado en simulacro 2026-09-01.
IMPRESCINDIBLE=(
  cage chromium libinput-tools
  network-manager wpasupplicant
  python3 sudo grub-efi-amd64 linux-image-amd64
  firmware-iwlwifi
  fonts-dejavu-core fonts-liberation fonts-noto-color-emoji
)
PRESENTES=()
for p in "${IMPRESCINDIBLE[@]}"; do
  dpkg -s "$p" >/dev/null 2>&1 && PRESENTES+=("$p")
done
apt-mark manual "${PRESENTES[@]}" | sed 's/^/   /'

# --- 3/5 · Que se va -----------------------------------------------------------
echo
echo "== 3/5 · Calculando la purga"
# Solo los metapaquetes de nivel alto + gdm3. El resto cae solo con autoremove,
# que es mas seguro que un 'apt purge gnome*' a ciegas.
CABEZAS=(
  task-gnome-desktop task-desktop
  gnome-core gnome gnome-shell gnome-session gnome-shell-extensions
  gdm3 gnome-control-center gnome-tweaks
  firefox-esr
)
OBJETIVO=()
for p in "${CABEZAS[@]}"; do
  dpkg -s "$p" >/dev/null 2>&1 && OBJETIVO+=("$p")
done

if [ "${#OBJETIVO[@]}" -eq 0 ]; then
  echo "   No queda ningun metapaquete de GNOME. Nada que hacer."
  exit 0
fi
echo "   Cabezas a purgar: ${OBJETIVO[*]}"
echo
echo "   --- simulacro (apt -s) ---"
apt-get -s purge --autoremove --no-install-recommends "${OBJETIVO[@]}" 2>&1 | tail -n 40 | sed 's/^/   /'
N=$(apt-get -s purge --autoremove --no-install-recommends "${OBJETIVO[@]}" 2>/dev/null | grep -c '^Purg\|^Remv' || true)
echo
echo "   => se irian ~$N paquetes.  Libre en / ahora: $(df -h / | awk 'NR==2{print $4}')"

# --- guarda dura: que ningun imprescindible haya quedado en la lista de borrado
echo
echo "   --- verificando que no se vaya nada critico ---"
apt-get -s purge --autoremove --no-install-recommends "${OBJETIVO[@]}" 2>/dev/null \
  | awk '/^(Purg|Remv)/{print $2}' | sed 's/:amd64$//' | sort -u > /tmp/pos-a-quitar.txt
COLADOS=$(grep -Fxf <(printf '%s\n' "${PRESENTES[@]}") /tmp/pos-a-quitar.txt || true)
if [ -n "$COLADOS" ]; then
  echo "   !! ABORTO: estos paquetes imprescindibles seguirian yendose:" >&2
  printf '      %s\n' $COLADOS >&2
  echo "   Revisar a mano antes de continuar. No se toco nada." >&2
  exit 1
fi
echo "   ninguno de los ${#PRESENTES[@]} imprescindibles esta en la lista de borrado"

if [ "$APLICAR" -ne 1 ]; then
  echo
  echo "SIMULACRO. No se toco nada."
  echo "Para aplicar de verdad:  sudo $0 --aplicar"
  exit 0
fi

# --- 4/5 · Purgar --------------------------------------------------------------
echo
echo "== 4/5 · Purgando (log en $LOG)"
{
  echo "### purga $(date -Is)"
  dpkg -l | awk '/^ii/{print $2}' | LC_ALL=C sort > /tmp/pkgs-antes.txt
  apt-get -y purge --autoremove --no-install-recommends "${OBJETIVO[@]}"
  apt-get -y autoremove --purge --no-install-recommends
  apt-get -y clean
} >>"$LOG" 2>&1
dpkg -l | awk '/^ii/{print $2}' | LC_ALL=C sort > /tmp/pkgs-despues.txt
LC_ALL=C comm -23 /tmp/pkgs-antes.txt /tmp/pkgs-despues.txt > /home/adminpos/paquetes-purgados.txt || true
chown adminpos:adminpos "$LOG" /home/adminpos/paquetes-purgados.txt
echo "   $(wc -l < /home/adminpos/paquetes-purgados.txt) paquetes purgados"
echo "   lista en /home/adminpos/paquetes-purgados.txt"

# --- 5/5 · Comprobar que el kiosco sigue vivo ----------------------------------
echo
echo "== 5/5 · El kiosco despues de la purga"
for u in pos-web.service pos-kiosk.service getty@tty2.service getty@tty3.service; do
  printf '   %-24s %s\n' "$u" "$(systemctl is-active "$u" 2>&1)"
done
echo "   chromium: $(pgrep -c chromium) procesos"
echo "   cage:     $(pgrep -c cage) procesos"
echo "   libre en /: $(df -h / | awk 'NR==2{print $4}')"
echo
echo "LISTO. Reinicia para confirmar que arranca limpio:  sudo reboot"
echo "Si tras el reboot algo falla, Ctrl+Alt+F2 -> adminpos y revisa $LOG"
