#!/bin/bash
# Deja la VM de laboratorio como caja1-lafe ANTES de la fase 5 (etapa C del plan).
# Se corre DENTRO de la VM, como root, con la plantilla copiada en /tmp/plantilla:
#   bash vm.sh scp -r ../.. soporte@127.0.0.1:/tmp/plantilla   (docs/terminal-kiosco-pos)
#   bash vm.sh ssh 'sudo bash /tmp/plantilla/red/lab/aprovisionar.sh'
#
# Como caja1-lafe: Debian 13, NetworkManager maneja la wifi, kiosco cage + Chromium con
# la página de espera de la plantilla, usuario de kiosco caja1, /home en un disco aparte
# y (con congelar.sh, aparte) overlayroot: raíz congelada, /home escribible. Agrega SOLO para el laboratorio: los
# routers simulados (lab-router), el POS falso y lab-ctl.
set -euo pipefail
P=/tmp/plantilla
LAB=$P/red/lab
POS_URL=http://10.99.0.1:8080/
paso() { echo; echo "=== $* ==="; }

paso "1/7 paquetes"
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get install -y -q --no-install-recommends \
  network-manager wpasupplicant iw hostapd dnsmasq-base nftables wireless-regdb \
  cage chromium fonts-dejavu-core wget socat python3 overlayroot busybox rsync ca-certificates

paso "2/7 /home en disco aparte (vdb), como sda3 en caja1-lafe"
if ! grep -q 'LABEL=home' /etc/fstab; then
  mkfs.ext4 -q -L home /dev/vdb
  mkdir -p /mnt/home-nuevo && mount /dev/vdb /mnt/home-nuevo
  cp -a /home/. /mnt/home-nuevo/
  diff -r --exclude=lost+found /home /mnt/home-nuevo >/dev/null && echo "copia de /home verificada" \
    || { echo "la copia de /home NO coincide: abortado"; umount /mnt/home-nuevo; exit 1; }
  umount /mnt/home-nuevo
  echo 'LABEL=home /home ext4 defaults 0 2' >> /etc/fstab
  mount /home
fi
findmnt /home

paso "3/7 usuario de kiosco caja1 (uid 1001)"
id caja1 >/dev/null 2>&1 || useradd -m -u 1001 -s /bin/bash -G video,input,plugdev caja1
loginctl enable-linger caja1 >/dev/null 2>&1 || true

paso "4/7 laboratorio: routers simulados, POS falso, lab-ctl"
install -m 755 "$LAB/lab-router" /usr/local/sbin/lab-router
install -m 755 "$LAB/lab-ctl" /usr/local/sbin/lab-ctl
install -D -m 644 "$LAB/pos-falso.py" /usr/local/lib/lab/pos-falso.py
mkdir -p /home/lab
[ -f /home/lab/estado.json ] || python3 -c "import importlib.util,json,sys;s=importlib.util.spec_from_loader('c',loader=None);m=importlib.util.module_from_spec(s);exec(open('$LAB/lab-ctl').read().split('def cargar')[0],m.__dict__);json.dump(m.INICIAL,open('/home/lab/estado.json','w'),indent=1)"
cat > /etc/systemd/system/lab-router.service <<'EOF'
[Unit]
Description=LABORATORIO - routers wifi simulados (mac80211_hwsim + hostapd)
DefaultDependencies=no
After=local-fs.target systemd-udevd.service systemd-remount-fs.service
Before=NetworkManager.service network-pre.target
Wants=network-pre.target
RequiresMountsFor=/home

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/sbin/lab-router iniciar

[Install]
WantedBy=multi-user.target
EOF
cat > /etc/systemd/system/lab-pos-falso.service <<'EOF'
[Unit]
Description=LABORATORIO - POS falso en 10.99.0.1:8080 (espacio de red router)
After=lab-router.service
Requires=lab-router.service

[Service]
NetworkNamespacePath=/run/netns/router
ExecStart=/usr/bin/python3 /usr/local/lib/lab/pos-falso.py
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable lab-router.service lab-pos-falso.service

paso "5/7 red: uplink0 pasa al router; mgmt0 solo administración; cloud-init no toca la red"
# La imagen cloud de Debian 13 usa netplan + systemd-networkd (no ifupdown): mgmt0 queda
# estática y sin puerta de enlace; uplink0 se quita (la toma lab-router al arrancar).
cat > /etc/netplan/50-cloud-init.yaml <<'NETPLAN'
network:
  version: 2
  ethernets:
    mgmt0:
      match:
        macaddress: "52:54:00:00:00:10"
      set-name: "mgmt0"
      dhcp4: false
      addresses: ["10.0.2.15/24"]
    uplink0:
      match:
        macaddress: "52:54:00:00:00:11"
      set-name: "uplink0"
      dhcp4: false
      optional: true
      link-local: []
NETPLAN
chmod 600 /etc/netplan/50-cloud-init.yaml
grep -q "dhcp4: true" /etc/netplan/50-cloud-init.yaml && { echo "uplink0 sigue con DHCP"; exit 1; } || echo "netplan: mgmt0 estática, uplink0 sin configurar"
echo 'network: {config: disabled}' > /etc/cloud/cloud.cfg.d/99-lab-red.cfg
mkdir -p /etc/NetworkManager/conf.d
cat > /etc/NetworkManager/conf.d/50-lab.conf <<'EOF'
# LABORATORIO: NetworkManager maneja solo la wifi del cliente (wlan0), como en caja1-lafe.
[keyfile]
unmanaged-devices=interface-name:mgmt0;interface-name:uplink0
EOF
# Conexión de base: la wifi de la «farmacia», como 'Farmacia la Fe ALPHANET' (prio 30)
cat > /etc/NetworkManager/system-connections/ALPHANET-LAB.nmconnection <<'EOF'
[connection]
id=ALPHANET-LAB
type=wifi
autoconnect=true
autoconnect-priority=30

[wifi]
mode=infrastructure
ssid=ALPHANET-LAB

[wifi-security]
key-mgmt=wpa-psk
pairwise=tkip;ccmp;
group=tkip;ccmp;
psk=alphanet1

[ipv4]
method=auto

[ipv6]
method=ignore
EOF
chmod 600 /etc/NetworkManager/system-connections/ALPHANET-LAB.nmconnection
systemctl enable NetworkManager.service

paso "6/7 kiosco (plantilla): pos-kiosk-start + esperando.html + pos-kiosk.service"
install -D -m 755 "$P/bin/pos-kiosk-start" /opt/pos/bin/pos-kiosk-start
install -D -m 644 "$P/www/esperando.html" /opt/pos/www/esperando.html
sed -e 's/^User=.*/User=caja1/' -e 's/^Group=.*/Group=caja1/' \
    -e 's#^Environment=XDG_RUNTIME_DIR=.*#Environment=XDG_RUNTIME_DIR=/run/user/1001#' \
    -e "s#^Environment=POS_URL=.*#Environment=POS_URL=$POS_URL#" \
    "$P/etc/pos-kiosk.service" > /etc/systemd/system/pos-kiosk.service
grep -E '^(User|Environment)' /etc/systemd/system/pos-kiosk.service
systemctl disable getty@tty1.service >/dev/null 2>&1 || true
systemctl set-default multi-user.target
systemctl enable pos-kiosk.service
# SOLO laboratorio: Chromium sin GPU (virtio-gpu) + depuración para la batería (etapa E)
install -D -m 644 "$LAB/kiosco-lab.conf" /etc/systemd/system/pos-kiosk.service.d/lab.conf
install -m 644 "$LAB/lab-cdp.service" /etc/systemd/system/lab-cdp.service
systemctl enable lab-cdp.service

paso "7/7 listo"
echo "Siguiente: apagar, guardar instantánea y recién después congelar (congelar.sh)."
