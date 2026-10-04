#!/bin/bash
# Puerta de la etapa C: la VM arranca como caja1-lafe (kiosco, overlayroot) y
# conectada a ALPHANET-LAB por wifi, con internet y el POS falso SOLO por la wifi.
# Se corre desde la Mac: bash verificar-c.sh
cd "$(dirname "$0")"
FALLAS=0
ok()  { echo "  ✔ $*"; }
mal() { echo "  ✘ $*"; FALLAS=$((FALLAS + 1)); }
chk() { local desc=$1; shift; if bash vm.sh ssh "$@" >/dev/null 2>&1; then ok "$desc"; else mal "$desc"; fi; }

echo "== Etapa C: caja1-lab =="
chk "raíz congelada (overlayroot)"        'findmnt -no FSTYPE / | grep -qx overlay'
chk "/home escribible en disco aparte"     'findmnt -no SOURCE /home | grep -q vdb && sudo touch /home/.prueba-escritura && sudo rm /home/.prueba-escritura'
chk "routers del laboratorio arriba"      'systemctl is-active -q lab-router.service && sudo ip netns exec router ip -br link | grep -q ap-alphanet'
chk "POS falso arriba"                     'systemctl is-active -q lab-pos-falso.service'
chk "NetworkManager maneja wlan0"          'nmcli -t -f DEVICE,STATE device | grep -q "^wlan0:connected"'
chk "conectada a ALPHANET-LAB"             'nmcli -t -f NAME,DEVICE connection show --active | grep -q "^ALPHANET-LAB:wlan0"'
chk "la salida por defecto es la wifi"     'ip route show default | grep -q "dev wlan0"'
chk "mgmt0 sin puerta de enlace"           '! ip route show default | grep -q mgmt0'
chk "internet por la wifi (TCP 1.1.1.1:443)" 'timeout 5 bash -c "</dev/tcp/1.1.1.1/443"'
chk "POS falso responde /healthz"          'wget -qO- -T 5 http://10.99.0.1:8080/healthz | grep -qx ok'
chk "«hub» de la VPN responde"             'ping -c1 -W2 10.10.0.1'
chk "kiosco corriendo (cage + Chromium)"   'pgrep -x cage && pgrep -f "chromium.*--kiosk"'
# La página que muestra el kiosco (por la depuración de Chromium, solo laboratorio)
TITULO=$(curl -s -m 5 http://127.0.0.1:9223/json/list | python3 -c "import json,sys;print(next((t['title'] for t in json.load(sys.stdin) if t['type']=='page'),''))" 2>/dev/null)
[ "$TITULO" = "POS (laboratorio)" ] && ok "el kiosco muestra el POS ($TITULO)" || mal "el kiosco muestra «$TITULO», no el POS"
# Y lo que de verdad SE VE en la pantalla (en el laboratorio la pantalla llegó a quedar
# congelada en la página de espera aunque Chromium ya mostrara el POS).
bash vm.sh captura c-kiosco >/dev/null
VERDE=$(python3 vista.py ~/pos-lab-wifi/capturas/c-kiosco.png 6,78,59)
[ "${VERDE%%.*}" -ge 60 ] && ok "la pantalla muestra el POS (${VERDE}% en el verde del POS)" || mal "la pantalla NO muestra el POS (${VERDE}% verde)"
echo
bash vm.sh captura c-kiosco >/dev/null && echo "  captura: ~/pos-lab-wifi/capturas/c-kiosco.png"
echo
[ $FALLAS -eq 0 ] && echo "PUERTA C: OK" || { echo "PUERTA C: $FALLAS falla(s)"; exit 1; }
