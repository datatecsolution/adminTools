#!/bin/bash
# FASE 4 · Bloque A — Endurecimiento de servicios y actualizaciones.
#
# Reversible y sin reboot. Es el bloque que se aplica primero porque nada de
# lo que hace aqui puede dejarte fuera del terminal.
#
# Que hace y por que:
#   1. Congela las actualizaciones automaticas. Una caja registradora no se
#      actualiza sola a mitad de turno: un apt en segundo plano puede reiniciar
#      servicios o dejar el disco a medias. Se enmascaran los timers, no se
#      instala unattended-upgrades. Las actualizaciones se haran a mano.
#   2. Enmascara avahi-daemon. Descubrimiento mDNS en la LAN; aqui no se usa
#      (impresora y bascula son USB, la app va por HTTPS a Cloudflare).
#   3. Enmascara udisks2. Es lo que automonta pendrives: con esto el cajero no
#      puede montar un USB aunque lo enchufe. mask, no disable, porque arranca
#      solo por activacion D-Bus.
#   4. Retira pos-web.service. Servia la app de PRUEBA en 127.0.0.1:8080 y ya
#      no pinta nada: la app real vive en admintools.supermercadosurbina.com.
#   5. Journal a RAM y acotado. Con overlayroot (bloque C) /var/log se pierde
#      en cada arranque de todos modos; mejor decirlo explicitamente y ponerle
#      techo para que no se coma la memoria.
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-endurecer.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0" >&2
  exit 1
fi

S=/home/adminpos/pos-terminal

echo "== 1/6 · Congelando las actualizaciones automaticas"
for u in apt-daily.timer apt-daily-upgrade.timer apt-daily.service apt-daily-upgrade.service; do
  systemctl mask --now "$u" >/dev/null 2>&1 || true
  printf '   %-28s %s\n' "$u" "$(systemctl is-enabled "$u" 2>&1)"
done
echo "   (para actualizar a mano en el futuro:  sudo apt update && sudo apt upgrade)"

echo "== 2/6 · Enmascarando avahi-daemon (mDNS, sin uso aqui)"
systemctl disable --now avahi-daemon.service avahi-daemon.socket >/dev/null 2>&1 || true
systemctl mask avahi-daemon.service avahi-daemon.socket >/dev/null 2>&1 || true
printf '   %-28s %s\n' avahi-daemon.service "$(systemctl is-enabled avahi-daemon.service 2>&1)"

echo "== 3/6 · Enmascarando udisks2 (automontaje de pendrives)"
systemctl disable --now udisks2.service >/dev/null 2>&1 || true
systemctl mask udisks2.service >/dev/null 2>&1 || true
printf '   %-28s %s\n' udisks2.service "$(systemctl is-enabled udisks2.service 2>&1)"

echo "== 4/6 · Retirando pos-web.service (servidor de prueba, ya no se usa)"
if systemctl list-unit-files pos-web.service >/dev/null 2>&1 && \
   [ -f /etc/systemd/system/pos-web.service ]; then
  systemctl disable --now pos-web.service >/dev/null 2>&1 || true
  # se guarda una copia por si algun dia hace falta volver a servir algo local
  install -o root -g root -m 644 /etc/systemd/system/pos-web.service \
          "$S/etc/pos-web.service.retirado"
  rm -f /etc/systemd/system/pos-web.service
  echo "   retirada (copia en $S/etc/pos-web.service.retirado)"
else
  echo "   ya no estaba"
fi
# el kiosco la referenciaba en Wants=/After=; se reinstala la unidad ya limpia
install -o root -g root -m 644 "$S/etc/pos-kiosk.service" /etc/systemd/system/
systemctl daemon-reload
if grep -q pos-web /etc/systemd/system/pos-kiosk.service; then
  echo "   !! pos-kiosk.service todavia menciona pos-web — revisar $S/etc/pos-kiosk.service" >&2
else
  echo "   pos-kiosk.service ya no depende de pos-web"
fi

echo "== 5/6 · Journal en RAM y acotado a 64M"
install -d -o root -g root -m 755 /etc/systemd/journald.conf.d
cat > /etc/systemd/journald.conf.d/pos.conf <<'CONF'
# Fase 4 — el disco se congela con overlayroot, asi que el journal no
# sobrevive al reinicio de ninguna manera. Se dice explicitamente y se le
# pone techo para que no se coma la RAM en turnos largos.
# Para leer el log del arranque en curso:  journalctl -b -u pos-kiosk.service
[Journal]
Storage=volatile
RuntimeMaxUse=64M
ForwardToSyslog=no
CONF
systemctl restart systemd-journald
echo "   Storage=volatile · RuntimeMaxUse=64M"

echo "== 6/6 · Estado tras el bloque A"
echo -n "   kiosco: "; systemctl is-active pos-kiosk.service
echo    "   timers vivos:"
systemctl list-timers --no-pager 2>/dev/null | sed -n '2,20p' | sed 's/^/     /'
cat <<'TXT'

   Bloque A listo. No hace falta reiniciar.
   Siguiente:  sudo bash ~/pos-terminal/bin/fase4-ssh.sh
TXT
