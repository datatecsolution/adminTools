#!/bin/bash
# FASE 5 — Wifi en autoservicio (pos-red). Ver red/README.md y plan-wifi-autoservicio.md.
#
#   sudo bash fase5-wifi-autoservicio.sh --ensayo                 # solo muestra qué haría
#   sudo bash fase5-wifi-autoservicio.sh --aplicar [opciones]     # instala (pide el PIN)
#   sudo bash fase5-wifi-autoservicio.sh --cerrar [--sin-soporte] # retira el rescate al cerrar la ventana
#   sudo bash fase5-wifi-autoservicio.sh --quitar                 # deja la caja como antes
#   sudo bash fase5-wifi-autoservicio.sh --estado
#
# Opciones de --aplicar:
#   --teclado auto|pantalla|fisico   teclado en pantalla (auto = solo si es táctil)
#   --hub IP                         hub de la VPN para el «soporte remoto» (10.10.0.1)
#   --soporte-ssid NOMBRE --soporte-clave CLAVE   red de emergencia (por defecto LAFE-SOPORTE)
#   --sin-soporte                    no crear la red de emergencia
#   --pin-stdin                      lee el PIN (dos veces) de la entrada estándar
#
# Con overlayroot activo escribe en el disco REAL vía overlayroot-chroot; los cambios
# valen después de REINICIAR. Lecciones de la fase 4: verificar cada paso antes del
# siguiente, respaldar antes de tocar, nada de cadenas largas con && bajo set -e.
set -u
AQUI=$(cd "$(dirname "$0")" && pwd)
RED=$(cd "$AQUI/../red" 2>/dev/null && pwd)
CASA=/home/pos-red
CONN_DIR=$CASA/system-connections
ETAPA=/run/pos-red-fase5          # /run se ve desde overlayroot-chroot
NM_BASE=/usr/lib/NetworkManager/system-connections
NM_ETC=/etc/NetworkManager/system-connections
ACCION="" TECLADO=auto HUB=10.10.0.1 SOPORTE_SSID=LAFE-SOPORTE SOPORTE_CLAVE="" CON_SOPORTE=1 PIN_STDIN=0

ok()  { echo "   OK    $*"; }
mal() { echo "   FALLA $*"; }
paso(){ echo; echo "== $* =="; }
morir() { mal "$*"; echo; echo "ABORTADO: no se cambió nada más."; exit 1; }

while [ $# -gt 0 ]; do
  case "$1" in
    --ensayo|--aplicar|--cerrar|--quitar|--estado) ACCION=${1#--} ;;
    --teclado) TECLADO=$2; shift ;;
    --hub) HUB=$2; shift ;;
    --soporte-ssid) SOPORTE_SSID=$2; shift ;;
    --soporte-clave) SOPORTE_CLAVE=$2; shift ;;
    --sin-soporte) CON_SOPORTE=0 ;;
    --pin-stdin) PIN_STDIN=1 ;;
    *) sed -n '2,20p' "$0"; exit 1 ;;
  esac
  shift
done
[ -n "$ACCION" ] || { sed -n '2,20p' "$0"; exit 1; }
[ "$(id -u)" = 0 ] || { echo "correr con sudo"; exit 1; }

# --- escribir en el disco real (con o sin overlayroot) --------------------------------
CONGELADA=0; [ "$(findmnt -no FSTYPE /)" = overlay ] && CONGELADA=1
en_raiz() {  # en_raiz 'comandos'  → en el disco real
  if [ $CONGELADA = 1 ]; then
    # Los avisos propios de overlayroot-chroot («Chrooting», «mount point is busy»…)
    # no son del comando: se filtran para no contaminar lo que se lee de acá.
    overlayroot-chroot sh -c "$1" 2>&1 | grep -vE '^INFO: Chrooting|mount point is busy|dmesg\(1\)|still mounted read/write'
    local rc=${PIPESTATUS[0]}
    mount -o remount,ro /media/root-ro 2>/dev/null   # «mount point is busy» es inofensivo
    return $rc
  fi
  sh -c "$1"
}

pos_url() { systemctl show pos-kiosk.service -p Environment 2>/dev/null | tr ' ' '\n' | sed -n 's/^POS_URL=//p' | head -1; }
wifi_dev() { nmcli -t -f DEVICE,TYPE device 2>/dev/null | awk -F: '$2=="wifi"{print $1; exit}'; }

estado() {
  paso "estado de la fase 5"
  echo "   raíz congelada: $([ $CONGELADA = 1 ] && echo sí || echo no)"
  echo "   placa wifi: $(wifi_dev || true)"
  for u in pos-red.service pos-red-rescate.timer; do echo "   $u: $(systemctl is-enabled $u 2>/dev/null) / $(systemctl is-active $u 2>/dev/null)"; done
  echo "   NetworkManager guarda en: $(grep -h '^path=' /etc/NetworkManager/conf.d/50-pos-red.conf 2>/dev/null || echo '/etc (sin fase 5)')"
  echo "   conexiones:"; nmcli -t -f NAME,FILENAME,AUTOCONNECT-PRIORITY connection show 2>/dev/null | sed 's/^/     /'
  [ -f $CASA/cambios.log ] && { echo "   últimos cambios:"; tail -3 $CASA/cambios.log | sed 's/^/     /'; }
  [ -f $CASA/rescate.log ] && { echo "   rescate:"; tail -3 $CASA/rescate.log | sed 's/^/     /'; }
}

preflight() {
  paso "1. comprobaciones (solo lectura)"
  [ -n "$RED" ] && [ -f "$RED/pos_red.py" ] && ok "plantilla de pos-red en $RED" || morir "no encuentro red/pos_red.py junto a este script"
  command -v nmcli >/dev/null && ok "NetworkManager" || morir "falta NetworkManager"
  systemctl is-enabled -q NetworkManager && ok "NetworkManager habilitado" || morir "NetworkManager no está habilitado"
  dpkg -s polkitd >/dev/null 2>&1 && ok "polkitd" || morir "falta polkitd: posred no podría manejar la wifi"
  python3 -c 'import sys; sys.exit(sys.version_info < (3, 9))' && ok "python3 ≥ 3.9" || morir "python3 ≥ 3.9"
  [ -n "$(wifi_dev)" ] && ok "placa wifi: $(wifi_dev)" || echo "   AVISO sin placa wifi: la página dirá «conecte el cable»"
  [ -n "$(pos_url)" ] && ok "POS_URL del kiosco: $(pos_url)" || morir "no encuentro POS_URL en pos-kiosk.service"
  findmnt -no TARGET /home | grep -qx /home && ok "/home es una partición aparte" || morir "/home no es una partición aparte: con overlayroot se perderían las redes"
  case "$TECLADO" in auto|pantalla|fisico) ok "teclado: $TECLADO" ;; *) morir "--teclado debe ser auto, pantalla o fisico" ;; esac
  if [ $CON_SOPORTE = 1 ]; then
    [ ${#SOPORTE_CLAVE} -ge 8 ] && ok "red de emergencia: $SOPORTE_SSID" || morir "--soporte-clave de 8+ caracteres (o --sin-soporte)"
  fi
  [ $CONGELADA = 1 ] && ok "raíz congelada: se escribe con overlayroot-chroot (vale tras reiniciar)" || ok "raíz sin congelar: se escribe directo"
  [ -f /etc/NetworkManager/conf.d/50-pos-red.conf ] && echo "   AVISO la fase 5 ya estaba aplicada: se reinstala"
  local base; base=$(ls $NM_ETC/*.nmconnection 2>/dev/null | wc -l)
  echo "   conexiones de base en $NM_ETC: $base (pasarán a $NM_BASE)"
}

conexion_soporte() {  # imprime el keyfile de la red de emergencia
  cat <<EOF
[connection]
id=$SOPORTE_SSID
type=wifi
autoconnect=true
autoconnect-priority=5

[wifi]
mode=infrastructure
ssid=$SOPORTE_SSID

[wifi-security]
key-mgmt=wpa-psk
psk=$SOPORTE_CLAVE

[ipv4]
method=auto
dns=1.1.1.1;

[ipv6]
method=ignore
EOF
}

aplicar() {
  preflight
  local ts; ts=$(date +%Y%m%d_%H%M%S)

  paso "2. respaldo en /home (antes de tocar nada)"
  install -d -m 700 /home/pos-red-respaldos
  local rsp=/home/pos-red-respaldos/antes-fase5_$ts.tar.gz
  tar -czf "$rsp" -C / etc/NetworkManager opt/pos/www/esperando.html etc/systemd/system/pos-kiosk.service \
      $(ls -d etc/systemd/system/pos-kiosk.service.d etc/polkit-1/rules.d 2>/dev/null) 2>/dev/null
  tar -tzf "$rsp" >/dev/null 2>&1 && ok "respaldo verificado: $rsp" || morir "el respaldo no se pudo leer"
  echo "$rsp" > /home/pos-red-respaldos/ULTIMO
  # El respaldo de «antes de la fase 5» NUNCA se pisa: si se reinstala (p. ej. para
  # actualizar pos-red), el último respaldo ya tiene la fase 5 adentro (laboratorio 2026-10-03).
  if [ ! -f /home/pos-red-respaldos/ORIGINAL ]; then
    local primero; primero=$(ls -1 /home/pos-red-respaldos/antes-fase5_*.tar.gz | sort | head -1)
    echo "$primero" > /home/pos-red-respaldos/ORIGINAL
  fi
  ok "respaldo de antes de la fase 5: $(cat /home/pos-red-respaldos/ORIGINAL)"

  paso "3. preparar archivos en $ETAPA"
  rm -rf "$ETAPA"; install -d "$ETAPA/raiz"
  local R=$ETAPA/raiz
  install -D -m 644 "$RED/pos_red.py"        "$R/opt/pos/red/pos_red.py"
  install -D -m 644 "$RED/nm.py"             "$R/opt/pos/red/nm.py"
  install -D -m 644 "$RED/www/index.html"    "$R/opt/pos/red/www/index.html"
  install -D -m 644 "$RED/sistema/pos-red.service"         "$R/etc/systemd/system/pos-red.service"
  install -D -m 644 "$RED/sistema/pos-red-rescate.service" "$R/etc/systemd/system/pos-red-rescate.service"
  install -D -m 644 "$RED/sistema/pos-red-rescate.timer"   "$R/etc/systemd/system/pos-red-rescate.timer"
  install -D -m 755 "$RED/sistema/pos-red-rescate"         "$R/usr/local/sbin/pos-red-rescate"
  install -D -m 644 "$RED/sistema/50-pos-red.rules"        "$R/etc/polkit-1/rules.d/50-pos-red.rules"
  install -D -m 644 "$RED/sistema/50-pos-red.conf"         "$R/etc/NetworkManager/conf.d/50-pos-red.conf"
  install -D -m 644 "$RED/sistema/NetworkManager-pos-red.conf" "$R/etc/systemd/system/NetworkManager.service.d/50-pos-red.conf"
  install -D -m 644 "$AQUI/../www/esperando.html"          "$R/opt/pos/www/esperando.html"
  if [ $CON_SOPORTE = 1 ]; then
    install -d "$ETAPA/base"; conexion_soporte > "$ETAPA/base/$SOPORTE_SSID.nmconnection"
  fi
  ok "$(find "$R" -type f | wc -l) archivos preparados"

  paso "4. disco real: usuario, archivos, redes de base y servicios"
  en_raiz "
    set -e
    id posred >/dev/null 2>&1 || useradd --system --no-create-home --home-dir /nonexistent --shell /usr/sbin/nologin posred
    cp -a $ETAPA/raiz/. /
    install -d -m 755 $NM_BASE
    for f in $NM_ETC/*.nmconnection; do [ -e \"\$f\" ] && mv \"\$f\" $NM_BASE/; done
    if [ -d $ETAPA/base ]; then cp $ETAPA/base/*.nmconnection $NM_BASE/; fi
    chmod 600 $NM_BASE/*.nmconnection
    systemctl enable pos-red.service pos-red-rescate.timer >/dev/null 2>&1
  " || morir "falló la escritura en el disco real (ver arriba)"
  en_raiz "
    test -f /opt/pos/red/pos_red.py && test -f /etc/NetworkManager/conf.d/50-pos-red.conf &&
    test -f /etc/polkit-1/rules.d/50-pos-red.rules && id posred >/dev/null &&
    test -f /etc/systemd/system/NetworkManager.service.d/50-pos-red.conf &&
    test -L /etc/systemd/system/multi-user.target.wants/pos-red.service &&
    test -L /etc/systemd/system/timers.target.wants/pos-red-rescate.timer &&
    ! ls $NM_ETC/*.nmconnection >/dev/null 2>&1
  " && ok "disco real verificado (archivos, usuario, servicios, redes movidas a $NM_BASE)" \
    || morir "la verificación del disco real falló"
  local uid gid
  uid=$(en_raiz 'id -u posred' | grep -xE '[0-9]+' | head -1)
  gid=$(en_raiz 'id -g posred' | grep -xE '[0-9]+' | head -1)
  [ -n "$uid" ] && [ -n "$gid" ] && ok "posred = uid $uid, gid $gid" || morir "no pude leer el uid/gid de posred del disco real"

  paso "5. /home/pos-red (redes de la tienda, configuración y registro)"
  install -d -m 750 -o root -g "$gid" $CASA
  install -d -m 700 -o root -g root $CONN_DIR
  [ -f $CASA/cambios.log ] || install -m 640 -o "$uid" -g "$gid" /dev/null $CASA/cambios.log
  python3 - "$CASA/config.json" "$(pos_url)" "$HUB" "$TECLADO" "$CONN_DIR" "$CASA/cambios.log" <<'PY'
import json, os, sys
ruta, pos, hub, teclado, conn, reg = sys.argv[1:]
cfg = {}
if os.path.isfile(ruta):
    cfg = json.load(open(ruta))          # conserva el PIN si ya estaba
cfg.update({"pos_url": pos, "hub": hub, "teclado": teclado, "conn_dir": conn, "registro": reg})
json.dump(cfg, open(ruta, "w"), indent=2, ensure_ascii=False)
PY
  chown root:"$gid" $CASA/config.json; chmod 640 $CASA/config.json
  [ "$(stat -c '%u:%g %a' $CASA)" = "0:$gid 750" ] && [ "$(stat -c '%u:%g %a' $CONN_DIR)" = "0:0 700" ] \
    && [ "$(stat -c '%u:%g %a' $CASA/cambios.log)" = "$uid:$gid 640" ] && [ "$(stat -c '%u:%g %a' $CASA/config.json)" = "0:$gid 640" ] \
    && ok "dueños y permisos de $CASA verificados" || morir "dueños o permisos de $CASA no son los esperados"
  ok "config: $(python3 -c "import json;c=json.load(open('$CASA/config.json'));print(c['pos_url'], '· hub', c['hub'], '· teclado', c['teclado'])")"

  paso "6. PIN de la tienda (lo escribe el encargado)"
  local args=(python3 "$ETAPA/raiz/opt/pos/red/pos_red.py" --config $CASA/config.json --fijar-pin)
  if [ $PIN_STDIN = 1 ]; then "${args[@]}" || morir "PIN no guardado"; else "${args[@]}" </dev/tty || morir "PIN no guardado"; fi
  chown root:"$gid" $CASA/config.json; chmod 640 $CASA/config.json

  paso "LISTO"
  echo "   Reiniciar la caja para que valga: systemctl reboot"
  echo "   Al cerrar la ventana: $0 --cerrar   (retira el rescate)"
  echo "   Vuelta atrás: $0 --quitar"
}

cerrar() {
  paso "cerrar la ventana: retirar el rescate"
  local extra=""
  [ $CON_SOPORTE = 0 ] && extra="rm -f '$NM_BASE/$SOPORTE_SSID.nmconnection'"
  en_raiz "systemctl disable pos-red-rescate.timer >/dev/null 2>&1; rm -f /etc/systemd/system/pos-red-rescate.timer /etc/systemd/system/pos-red-rescate.service /usr/local/sbin/pos-red-rescate; $extra" \
    && ok "rescate retirado del disco real" || mal "no se pudo retirar el rescate"
  rm -f $CASA/ensayo-caida
  echo "   (vale tras reiniciar; en vivo: systemctl stop pos-red-rescate.timer)"
  systemctl stop pos-red-rescate.timer 2>/dev/null
}

quitar() {
  paso "quitar la fase 5 (la caja queda como antes)"
  local rsp; rsp=$(cat /home/pos-red-respaldos/ORIGINAL 2>/dev/null)
  en_raiz "
    systemctl disable pos-red.service pos-red-rescate.timer >/dev/null 2>&1
    rm -f /etc/NetworkManager/conf.d/50-pos-red.conf /etc/polkit-1/rules.d/50-pos-red.rules /etc/systemd/system/NetworkManager.service.d/50-pos-red.conf
    rm -f /etc/systemd/system/pos-red.service /etc/systemd/system/pos-red-rescate.service /etc/systemd/system/pos-red-rescate.timer /usr/local/sbin/pos-red-rescate
    rm -rf /opt/pos/red
    mkdir -p $NM_ETC
    for f in $NM_BASE/*.nmconnection; do [ -e \"\$f\" ] && mv \"\$f\" $NM_ETC/; done
    rm -f '$NM_ETC/$SOPORTE_SSID.nmconnection'
  " && ok "disco real: fase 5 retirada, redes de base de vuelta en $NM_ETC" || mal "falló el retiro en el disco real"
  if [ -n "$rsp" ] && [ -f "$rsp" ]; then
    rm -rf "$ETAPA.esperando"; install -d "$ETAPA.esperando"
    if tar -xzf "$rsp" -C "$ETAPA.esperando" opt/pos/www/esperando.html; then
      en_raiz "cp $ETAPA.esperando/opt/pos/www/esperando.html /opt/pos/www/esperando.html" && ok "esperando.html restaurado del respaldo $rsp"
    else
      mal "no pude sacar esperando.html del respaldo $rsp"
    fi
  fi
  [ -d $CASA ] && mv $CASA "$CASA.quitado_$(date +%Y%m%d_%H%M%S)" && ok "redes de la tienda y registros guardados aparte en $CASA.quitado_*"
  echo "   Reiniciar la caja para que valga: systemctl reboot"
}

case "$ACCION" in
  ensayo) preflight; paso "ensayo: no se cambió nada" ;;
  aplicar) aplicar ;;
  cerrar) cerrar ;;
  quitar) quitar ;;
  estado) estado ;;
esac
