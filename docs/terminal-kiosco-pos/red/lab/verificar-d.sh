#!/bin/bash
# Puerta de la etapa D: la fase 5 instalada en la caja virtual y, tras reiniciar,
# todo en su lugar sin romper lo que ya funcionaba. Desde la Mac: bash verificar-d.sh
cd "$(dirname "$0")"
FALLAS=0
ok()  { echo "  ✔ $*"; }
mal() { echo "  ✘ $*"; FALLAS=$((FALLAS + 1)); }
chk() { local desc=$1; shift; if bash vm.sh ssh "$@" >/dev/null 2>&1; then ok "$desc"; else mal "$desc"; fi; }
API='curl -s -m 8 -H "Host: 127.0.0.1:8090" http://127.0.0.1:8090/api/estado'

echo "== Etapa D: fase 5 en caja1-lab =="
chk "raíz sigue congelada"                       'findmnt -no FSTYPE / | grep -qx overlay'
chk "pos-red.service activo como posred"         'systemctl is-active -q pos-red && [ "$(ps -o user= -C python3 | sort -u | grep -c posred)" = 1 ]'
chk "pos-red escucha SOLO en 127.0.0.1:8090"     'ss -ltn | grep -q "127.0.0.1:8090" && ! ss -ltn | grep -q "0.0.0.0:8090"'
chk "NetworkManager guarda en /home/pos-red"     'sudo NetworkManager --print-config 2>/dev/null | grep -q "path=/home/pos-red/system-connections"'
chk "NetworkManager puede escribir en /home/pos-red" 'systemctl show NetworkManager -p ReadWritePaths | grep -q /home/pos-red/system-connections'
chk "redes de base en /usr/lib (ALPHANET + SOPORTE)" 'ls /usr/lib/NetworkManager/system-connections/ | grep -q ALPHANET-LAB && ls /usr/lib/NetworkManager/system-connections/ | grep -q LAFE-SOPORTE'
chk "ninguna red en /etc/NetworkManager"         '! sudo ls /etc/NetworkManager/system-connections/*.nmconnection'
chk "conectada con la red de base de /usr/lib"   'nmcli -t -f NAME,FILENAME,DEVICE connection show --active | grep -q "^ALPHANET-LAB:/usr/lib/.*:wlan0"'
chk "/home/pos-red con dueños correctos"         '[ "$(sudo stat -c "%U:%G %a" /home/pos-red/system-connections)" = "root:root 700" ] && [ "$(sudo stat -c "%U %a" /home/pos-red/cambios.log)" = "posred 640" ]'
chk "polkit: posred puede escanear"              'sudo -u posred nmcli device wifi rescan'
chk "polkit: otro usuario NO puede"              '! sudo -u caja1 nmcli connection modify ALPHANET-LAB connection.autoconnect-priority 31'
chk "rescate armado (timer)"                     'systemctl is-active -q pos-red-rescate.timer'
chk "API: conectado, internet, POS y soporte"    "$API | python3 -c \"import json,sys;e=json.load(sys.stdin);sys.exit(not (e['conectado_a']=='ALPHANET-LAB' and e['internet'] and e['pos'] and e['soporte'] and e['pin']))\""
chk "API: Host ajeno rechazado"                  '[ "$(curl -s -o /dev/null -w "%{http_code}" -H "Host: malo.example" http://127.0.0.1:8090/api/estado)" = 403 ]'
chk "página de wifi servida"                     'curl -s -H "Host: 127.0.0.1:8090" http://127.0.0.1:8090/ | grep -q "Red de esta caja"'
chk "esperando.html con la integración"          'grep -q "revisarRed" /opt/pos/www/esperando.html'
bash vm.sh captura d-kiosco >/dev/null
VERDE=$(python3 vista.py ~/pos-lab-wifi/capturas/d-kiosco.png 6,78,59)
[ "${VERDE%%.*}" -ge 60 ] && ok "el kiosco sigue mostrando el POS (${VERDE}% verde)" || mal "el kiosco NO muestra el POS (${VERDE}% verde)"
echo
[ $FALLAS -eq 0 ] && echo "PUERTA D: OK" || { echo "PUERTA D: $FALLAS falla(s)"; exit 1; }
