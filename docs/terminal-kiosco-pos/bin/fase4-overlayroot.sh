#!/bin/bash
# FASE 4 · Bloque C — Congelar el disco con overlayroot.
#
# A partir del proximo arranque, la particion raiz (sda2) se monta de solo
# lectura y todo lo que se escriba encima va a un overlay en RAM que se tira al
# reiniciar. El sistema vuelve virgen en cada encendido: ni un apt a medias, ni
# un /etc tocado, ni un disco lleno de logs, ni nada que deje el cajero.
#
# /home (sda4) SE QUEDA ESCRIBIBLE — eso es lo que hace 'recurse=0'.
# Es deliberado: ahi vive /home/caja1/.config/pos-chromium, y dentro el
# localStorage con la clave admintools-pos.scale, o sea el protocolo de la
# bascula ("Torrey (a demanda)", 9600 8N1) y el emparejamiento de la impresora.
# Si /home se congelara tambien, la caja volveria al protocolo "simulator" —
# que INVENTA pesos con Math.random() — en cada arranque.
#
# 'swap=1' deja que el overlay tire de la swap de 7.7G si un turno largo llena
# la RAM, en vez de morir por falta de memoria.
#
# COMO TOCAR EL SISTEMA DESPUES (importante, leelo):
#   Con el disco congelado, un 'sudo apt install' o un 'sudo nano /etc/...'
#   parecen funcionar pero se evaporan al reiniciar. Para un cambio de verdad:
#       sudo overlayroot-chroot            # te mete en el disco real, rw
#       ...haces el cambio, luego  exit
#       sudo reboot
#   Y para descongelar del todo:
#       sudo bash ~/pos-terminal/bin/fase4-overlayroot.sh --desactivar && sudo reboot
#   Si el arranque falla: en GRUB, tecla 'e', anadir  overlayroot=disabled
#   a la linea del kernel, Ctrl+X. Eso arranca con el disco normal.
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-overlayroot.sh
#       sudo bash .../fase4-overlayroot.sh --desactivar
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0" >&2
  exit 1
fi

CONF=/etc/overlayroot.conf
ACTIVO=no
grep -qE '^overlay(root)? / ' /proc/mounts && ACTIVO=si
[ -d /media/root-ro ] && ACTIVO=si

# ---------------------------------------------------------------- desactivar
if [ "${1:-}" = "--desactivar" ]; then
  echo "== Descongelando el disco"
  if [ "$ACTIVO" = si ]; then
    echo "   overlayroot esta activo: escribiendo en el disco real via overlayroot-chroot"
    overlayroot-chroot /bin/sh -c \
      "sed -i 's/^overlayroot=.*/overlayroot=\"\"/' $CONF" 
    overlayroot-chroot /bin/sh -c "grep '^overlayroot=' $CONF" | sed 's/^/   /'
  else
    sed -i 's/^overlayroot=.*/overlayroot=""/' "$CONF"
    grep '^overlayroot=' "$CONF" | sed 's/^/   /'
  fi
  echo "   Hecho. Reinicia para arrancar con el disco escribible:  sudo reboot"
  exit 0
fi

# ------------------------------------------------------------------ preflight
echo "== 1/5 · Comprobaciones previas (si alguna falla, se aborta sin tocar nada)"

if [ "$ACTIVO" = si ]; then
  echo "   !! overlayroot YA esta activo en este arranque." >&2
  echo "      Para cambiar la configuracion:  sudo overlayroot-chroot" >&2
  exit 1
fi

# /home tiene que ser una particion aparte y estar en fstab, o recurse=0 no
# sirve de nada: si /home colgara de la raiz, se congelaria con ella.
if ! findmnt -no SOURCE /home >/dev/null 2>&1 || [ "$(findmnt -no TARGET /home 2>/dev/null)" != /home ]; then
  echo "   !! /home NO es un punto de montaje propio. Con overlayroot se congelaria" >&2
  echo "      junto con la raiz y la caja perderia la config de la bascula." >&2
  exit 1
fi
printf '   %-34s %s\n' "/home es particion aparte:" "$(findmnt -no SOURCE,FSTYPE /home)"
grep -qE '^[^#].*[[:space:]]/home[[:space:]]' /etc/fstab \
  && echo "   /home esta en /etc/fstab                 OK" \
  || { echo "   !! /home no aparece en /etc/fstab — abortando" >&2; exit 1; }

# el perfil de Chromium (config de bascula e impresora) tiene que vivir en /home
PERFIL=/home/caja1/.config/pos-chromium
if [ -d "$PERFIL" ]; then
  echo "   perfil de Chromium en /home             OK  ($(du -sh "$PERFIL" 2>/dev/null | cut -f1))"
else
  echo "   !! No existe $PERFIL." >&2
  echo "      Eso significa que el kiosco aun no ha guardado la configuracion de la" >&2
  echo "      bascula/impresora. Empareja y configura ANTES de congelar el disco." >&2
  exit 1
fi

# SSH tiene que estar en pie: es la via de entrada una vez congelado
if systemctl is-active ssh.socket >/dev/null 2>&1 || systemctl is-active ssh.service >/dev/null 2>&1; then
  echo "   SSH en marcha                            OK"
else
  echo "   !! SSH no esta activo. Corre antes el bloque B (fase4-ssh.sh)." >&2
  echo "      Sin SSH y con el disco congelado, administrar esto es ir al local." >&2
  exit 1
fi

echo "   espacio en /: $(df -h / | awk 'NR==2{print $4" libres de "$2}')"
echo "   RAM/swap:     $(free -h | awk 'NR==2{print $2" RAM"} NR==3{print "                 "$2" swap"}')"

# ------------------------------------------------------------------ instalar
echo "== 2/5 · Instalando overlayroot"
if dpkg -s overlayroot >/dev/null 2>&1; then
  echo "   ya estaba instalado"
else
  DEBIAN_FRONTEND=noninteractive apt-get update -qq
  DEBIAN_FRONTEND=noninteractive apt-get install -y -qq overlayroot
fi

echo "== 3/5 · Escribiendo $CONF"
cp -n "$CONF" "$CONF.bak" 2>/dev/null || true
cat > "$CONF" <<'CONF'
# Fase 4 — terminal POS caja1-samuel.
#
# tmpfs     : el overlay vive en RAM y se descarta en cada reinicio.
# swap=1    : si la RAM se llena en un turno largo, tira de la swap (7.7G)
#             en vez de quedarse sin memoria.
# recurse=0 : congela SOLO la raiz. /home (sda4) sigue escribiendose, que es
#             donde estan el perfil de Chromium de caja1 y con el la config de
#             la bascula (admintools-pos.scale) y el emparejamiento WebUSB.
#             Si esto pasara a 1, la caja volveria a "simulator" —pesos
#             inventados— en cada arranque.
overlayroot="tmpfs:swap=1,recurse=0"

# Sin menu interactivo en el arranque.
overlayroot_cfgdisk="disabled"
CONF
grep '^overlayroot' "$CONF" | sed 's/^/   /'

echo "== 4/5 · Regenerando el initramfs"
update-initramfs -u >/dev/null 2>&1
echo "   hecho ($(ls -la /boot/initrd.img-$(uname -r) | awk '{print $5" bytes"}'))"

echo "== 5/5 · Listo — falta reiniciar para que entre en vigor"
cat <<'TXT'

   ---------------------------------------------------------------
   ANTES DE REINICIAR, deja hecho todo lo que quieras que quede en
   el disco: es la ultima escritura "normal" que vas a hacer.

       sudo reboot

   Tras el arranque, comprueba con:

       sudo bash ~/pos-terminal/bin/fase4-verificar.sh

   Se espera ver  /  como overlay (ro por debajo) y  /home  ext4 rw.
   ---------------------------------------------------------------
TXT
