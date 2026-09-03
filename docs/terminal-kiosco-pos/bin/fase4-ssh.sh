#!/bin/bash
# FASE 4 · Bloque B — SSH de administracion (solo adminpos).
#
# Por que SSH en un kiosco: al final de la fase el cajero se queda encerrado en
# la app (sin Ctrl+Alt+Fn) y el disco se congela con overlayroot. Sin SSH, el
# unico modo de tocar este terminal seria ir al local con teclado y monitor.
#
# Como queda configurado:
#   - solo el usuario adminpos puede entrar; root prohibido
#   - sin reenvio X11 ni de puertos: esto es una consola de administracion,
#     no un tunel
#   - si adminpos ya tiene una clave publica en ~/.ssh/authorized_keys, se
#     APAGA la autenticacion por contrasena. Si no la tiene, se deja encendida
#     y el script lo avisa: es lo unico que evitaria que te quedaras fuera.
#
# Se ejecuta ANTES de overlayroot a proposito: las claves de host del servidor
# se generan al instalar, en /etc/ssh. Si el disco ya estuviera congelado se
# regenerarian en cada arranque y cada reconexion daria la alerta de
# "REMOTE HOST IDENTIFICATION HAS CHANGED".
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-ssh.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0" >&2
  exit 1
fi

AUTH=/home/adminpos/.ssh/authorized_keys

echo "== 1/5 · Instalando openssh-server"
if dpkg -s openssh-server >/dev/null 2>&1; then
  echo "   ya estaba instalado"
else
  DEBIAN_FRONTEND=noninteractive apt-get update -qq
  DEBIAN_FRONTEND=noninteractive apt-get install -y -qq openssh-server
fi

echo "== 2/5 · Decidiendo si se permite contrasena"
if [ -s "$AUTH" ]; then
  PW=no
  echo "   adminpos tiene clave publica ($(grep -c . "$AUTH") linea/s) -> contrasena APAGADA"
else
  PW=yes
  echo "   adminpos NO tiene ~/.ssh/authorized_keys -> contrasena ENCENDIDA (ver aviso final)"
fi

echo "== 3/5 · Escribiendo /etc/ssh/sshd_config.d/99-pos.conf"
install -d -o root -g root -m 755 /etc/ssh/sshd_config.d
cat > /etc/ssh/sshd_config.d/99-pos.conf <<CONF
# Fase 4 — consola de administracion del terminal POS.
# Este fichero manda sobre /etc/ssh/sshd_config (se incluye antes).
PermitRootLogin no
AllowUsers adminpos
PasswordAuthentication $PW
KbdInteractiveAuthentication $PW
PubkeyAuthentication yes
X11Forwarding no
AllowTcpForwarding no
AllowAgentForwarding no
PermitTunnel no
MaxAuthTries 3
MaxSessions 4
LoginGraceTime 30
ClientAliveInterval 300
ClientAliveCountMax 2
CONF
chmod 644 /etc/ssh/sshd_config.d/99-pos.conf

echo "== 4/5 · Validando la configuracion antes de arrancar"
if ! sshd -t; then
  echo "   !! sshd -t rechaza la configuracion. Se retira el fichero y se aborta." >&2
  rm -f /etc/ssh/sshd_config.d/99-pos.conf
  exit 1
fi
echo "   sshd -t: OK"

# Debian 13 arranca sshd por socket (ssh.socket), no por ssh.service.
# Se reinicia lo que este realmente en uso.
systemctl enable ssh.socket >/dev/null 2>&1 || systemctl enable ssh.service >/dev/null 2>&1 || true
if systemctl is-enabled ssh.socket >/dev/null 2>&1; then
  systemctl restart ssh.socket
else
  systemctl restart ssh.service
fi

echo "== 5/5 · Comprobaciones"
echo -n "   escuchando: "; ss -lnt 2>/dev/null | awk '$4 ~ /:22$/ {print $4}' | paste -sd' ' || echo "(ss no disponible)"
echo    "   IP de este terminal:"
ip -4 -o addr show scope global | awk '{printf "     %-10s %s\n", $2, $4}'
echo    "   fingerprints del host (apuntalos para reconocerlo desde el otro equipo):"
for k in /etc/ssh/ssh_host_*_key.pub; do ssh-keygen -lf "$k" | sed 's/^/     /'; done

cat <<TXT

   ---------------------------------------------------------------
   PRUEBALO AHORA desde otro equipo de la red, ANTES de seguir:

       ssh adminpos@$(ip -4 -o addr show scope global | awk 'NR==1{split($4,a,"/"); print a[1]}')

   No cierres esa sesion hasta terminar la fase.
   ---------------------------------------------------------------
TXT

if [ "$PW" = yes ]; then
cat <<'TXT'
   AVISO — la entrada por contrasena esta encendida porque adminpos no tiene
   clave publica. Es aceptable en una LAN, pero lo correcto es dejar tu clave
   y apagarla. Desde TU equipo:

       ssh-copy-id adminpos@<ip-de-la-caja>

   y despues, aqui:

       sudo bash ~/pos-terminal/bin/fase4-ssh.sh     (se reejecuta y la apaga)

   Hazlo antes del bloque C: con overlayroot activo, authorized_keys sigue
   siendo escribible (esta en /home, que no se congela), pero el cambio de
   sshd_config.d ya no.
TXT
fi
echo "   Siguiente:  sudo bash ~/pos-terminal/bin/fase4-cerrar-consola.sh"
