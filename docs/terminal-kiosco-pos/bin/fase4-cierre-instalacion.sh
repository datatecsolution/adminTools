#!/bin/bash
# FASE 4 · Cierre — retirar los dos permisos temporales de la instalacion.
#
#   --red   quita net-fallback.service: la unidad de rescate que, si a los
#           90 s del arranque no hay red, tira NetworkManager y restaura
#           ifupdown. Sirvio durante la migracion a NM; en produccion es un
#           peligro (un corte de internet al encender la dejaria sin NM y sin
#           las 3 redes guardadas). Se guarda copia en ~/pos-terminal/etc.
#   --sudo  quita /etc/sudoers.d/90-instalacion-kiosco (sudo SIN contrasena
#           para adminpos). Desde ese momento todo sudo pide la contrasena de
#           adminpos: hacerlo EL ULTIMO, cuando ya no quede nada que aplicar.
#
# Si overlayroot ya esta activo, el cambio se hace en el disco real
# (overlayroot-chroot) Y en el sistema en vivo, para que valga ya y despues
# del reinicio.
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-cierre-instalacion.sh --red
#       sudo bash .../fase4-cierre-instalacion.sh --sudo
set -euo pipefail
# Usuario administrador de la caja (el que entra por SSH y corre estos scripts con sudo).
# Se toma de SUDO_USER; se puede forzar con ADMIN_USER=... (caja1/caja2-samuel: adminpos, caja1-lafe: farmacialafe).
ADMIN_USER="${ADMIN_USER:-${SUDO_USER:-adminpos}}"

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0 --red|--sudo" >&2
  exit 1
fi
S=/home/$ADMIN_USER/pos-terminal
ACTIVO=no
[ -d /media/root-ro ] && grep -q ' /media/root-ro ' /proc/mounts && ACTIVO=si

# corre un comando en vivo y, si hay overlayroot, tambien en el disco real
ambos() {
  if [ "$ACTIVO" = si ]; then
    overlayroot-chroot /bin/sh -c "$1" && echo "   (aplicado en el disco real)"
  fi
  /bin/sh -c "$1"
}

case "${1:-}" in
  --red)
    echo "== Retirando net-fallback.service"
    if [ -f /etc/systemd/system/net-fallback.service ]; then
      install -d -o "$ADMIN_USER" -g "$ADMIN_USER" "$S/etc"
      cp -n /etc/systemd/system/net-fallback.service "$S/etc/net-fallback.service.retirado"
      cp -n /usr/local/sbin/net-fallback.sh "$S/etc/net-fallback.sh.retirado" 2>/dev/null || true
      systemctl disable --now net-fallback.service >/dev/null 2>&1 || true
      ambos 'rm -f /etc/systemd/system/net-fallback.service /usr/local/sbin/net-fallback.sh; systemctl daemon-reload 2>/dev/null || true'
      echo "   retirada (copia en $S/etc/net-fallback.service.retirado)"
    else
      echo "   ya no estaba"
    fi
    printf '   %-28s %s\n' net-fallback.service "$(systemctl is-enabled net-fallback.service 2>&1)"
    printf '   %-28s %s\n' NetworkManager "$(systemctl is-enabled NetworkManager 2>&1) / $(systemctl is-active NetworkManager 2>&1)"
    ;;
  --sudo)
    echo "== Retirando el sudo sin contrasena de $ADMIN_USER"
    if [ -f /etc/sudoers.d/90-instalacion-kiosco ]; then
      ambos 'rm -f /etc/sudoers.d/90-instalacion-kiosco'
      echo "   retirado"
    else
      echo "   ya no estaba"
    fi
    visudo -c 2>&1 | sed 's/^/   /'
    echo "   desde ahora:  sudo  pide la contrasena de $ADMIN_USER"
    ;;
  *)
    echo "Uso: $0 --red | --sudo" >&2; exit 1;;
esac
