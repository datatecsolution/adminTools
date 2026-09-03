#!/bin/bash
# FASE 4 · Verificador. Se puede correr tantas veces como se quiera; no toca nada.
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-verificar.sh
set -uo pipefail

ok(){ printf '  \033[32m✔\033[0m %-42s %s\n' "$1" "${2:-}"; }
no(){ printf '  \033[31mX\033[0m %-42s %s\n' "$1" "${2:-}"; }
nn(){ printf '  \033[33m·\033[0m %-42s %s\n' "$1" "${2:-}"; }

if [ "$(id -u)" -ne 0 ]; then
  echo "  aviso: sin root no se puede mirar dentro de /home/caja1 (perfil de Chromium)."
  echo "         Para el informe completo:  sudo bash $0"
fi

echo; echo "FASE 4 — verificacion   ($(date '+%F %T') · arranque $(uptime -s))"

echo; echo "A · Servicios y actualizaciones"
for u in apt-daily.timer apt-daily-upgrade.timer avahi-daemon.service udisks2.service; do
  e=$(systemctl is-enabled "$u" 2>&1)
  [ "$e" = masked ] && ok "$u" "masked" || no "$u" "$e"
done
[ -e /etc/systemd/system/pos-web.service ] && no "pos-web.service" "sigue instalada" || ok "pos-web.service" "retirada"
s=$(systemctl show -p Storage --value systemd-journald 2>/dev/null)
grep -q "Storage=volatile" /etc/systemd/journald.conf.d/pos.conf 2>/dev/null \
  && ok "journal en RAM" "$(journalctl --disk-usage 2>/dev/null | tail -1)" || no "journal en RAM"

echo; echo "B · SSH"
if systemctl is-active ssh.socket >/dev/null 2>&1 || systemctl is-active ssh.service >/dev/null 2>&1; then
  ok "sshd activo" "$(ss -lnt 2>/dev/null | awk '$4 ~ /:22$/{printf "%s ", $4}')"
  grep -h '^\(AllowUsers\|PermitRootLogin\|PasswordAuthentication\)' /etc/ssh/sshd_config.d/99-pos.conf 2>/dev/null \
    | sed 's/^/      /'
else
  no "sshd activo"
fi

echo; echo "C · Disco congelado"
if grep -qE '^overlay(root)? / ' /proc/mounts || [ -d /media/root-ro ]; then
  ok "raiz en overlay" "$(findmnt -no FSTYPE,OPTIONS / | cut -c1-60)"
  m=$(findmnt -no OPTIONS /media/root-ro 2>/dev/null | grep -o '^r[ow]')
  [ "$m" = ro ] && ok "disco real /media/root-ro" "solo lectura" || nn "disco real /media/root-ro" "${m:-no montado}"
else
  no "raiz en overlay" "el disco sigue escribible (falta reiniciar?)"
fi
h=$(findmnt -no FSTYPE,OPTIONS /home 2>/dev/null)
case "$h" in *rw*) ok "/home escribible" "$(echo "$h" | cut -c1-40)";; *) no "/home escribible" "$h";; esac
P=/home/caja1/.config/pos-chromium
[ -d "$P" ] && ok "perfil de Chromium presente" "$(du -sh "$P" 2>/dev/null | cut -f1)" || no "perfil de Chromium" "no existe"

echo; echo "D · Kiosco encerrado"
grep -q 'exec cage -d -s' /opt/pos/bin/pos-kiosk-start 2>/dev/null \
  && no "cambio de terminal (Ctrl+Alt+Fn)" "ABIERTO (cage tiene -s)" \
  || ok "cambio de terminal (Ctrl+Alt+Fn)" "bloqueado"
[ "$(systemctl is-enabled ctrl-alt-del.target 2>&1)" = masked ] \
  && ok "Ctrl+Alt+Supr" "masked" || no "Ctrl+Alt+Supr" "activo"
echo -n "  "; printf '%-44s' "  pos-kiosk.service"; systemctl is-active pos-kiosk.service
pid=$(systemctl show -p MainPID --value pos-kiosk.service 2>/dev/null)
if [ "${pid:-0}" -gt 0 ] 2>/dev/null; then
  g=$(grep ^Groups /proc/$pid/status | tr '\t' ' ')
  case "$g" in *" 7 "*) case "$g" in *" 20 "*) ok "kiosco con grupos lp y dialout";; *) no "kiosco sin dialout" "$g";; esac;; *) no "kiosco sin lp" "$g";; esac
fi

echo; echo "E · Hardware"
[ -e /dev/ttyUSB0 ] && ok "bascula /dev/ttyUSB0" "$(stat -c '%U:%G %a' /dev/ttyUSB0)" || no "bascula /dev/ttyUSB0" "ausente"
lsusb 2>/dev/null | grep -q 04b8:0e39 && ok "impresora Epson en el bus USB" || no "impresora Epson" "no enumerada"
[ -e /dev/usb/lp0 ] && nn "/dev/usb/lp0" "existe (usblp la sujeta; Chromium la suelta al reclamarla)" \
                    || nn "/dev/usb/lp0" "no existe (usblp desenganchado)"
echo
