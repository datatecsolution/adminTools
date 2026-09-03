#!/bin/bash
# Alta de caja1-samuel (10.10.0.4) en la VPN WireGuard hub-and-spoke.
# Fase A: claves + wg0.conf. NO levanta el tunel todavia.
# Uso:  sudo bash /home/adminpos/wg-caja1-setup.sh
set -euo pipefail

[ "$(id -u)" -eq 0 ] || { echo "Corrreme con sudo"; exit 1; }

NOMBRE=caja1-samuel
IP=10.10.0.4
HUB_PUB='OhmA52Sf2FqxvzCCTgmtz/5rmLH90HTbu0nJWCTxayU='
HUB_EP=201.190.38.238:51820

install -d -m 700 /etc/wireguard

if [ ! -s "/etc/wireguard/$NOMBRE.key" ]; then
  umask 077
  wg genkey | tee "/etc/wireguard/$NOMBRE.key" | wg pubkey > "/etc/wireguard/$NOMBRE.pub"
  echo "[+] Par de claves generado."
else
  echo "[=] Ya existia /etc/wireguard/$NOMBRE.key, se reutiliza."
fi
chmod 600 "/etc/wireguard/$NOMBRE.key"
chmod 644 "/etc/wireguard/$NOMBRE.pub"

if [ -e /etc/wireguard/wg0.conf ]; then
  cp -a /etc/wireguard/wg0.conf "/etc/wireguard/wg0.conf.bak.$(date +%s)"
  echo "[=] wg0.conf existente respaldado."
fi

umask 077
cat > /etc/wireguard/wg0.conf <<EOF
[Interface]
Address = $IP/24
PrivateKey = $(cat "/etc/wireguard/$NOMBRE.key")

[Peer]
# hub Ronal
PublicKey = $HUB_PUB
Endpoint = $HUB_EP
AllowedIPs = 10.10.0.0/24
PersistentKeepalive = 25
EOF
chmod 600 /etc/wireguard/wg0.conf

wg-quick strip wg0 >/dev/null && echo "[+] wg0.conf valido."

echo
echo "==================== CLAVE PUBLICA DE $NOMBRE ===================="
cat "/etc/wireguard/$NOMBRE.pub"
echo "=================================================================="
echo
echo "Pegar en el HUB (Ronal, como root):"
echo "  wg set wg0 peer $(cat "/etc/wireguard/$NOMBRE.pub") allowed-ips $IP/32"
echo "y agregar al final de /etc/wireguard/wg0.conf del hub:"
echo
echo "  [Peer]"
echo "  # $NOMBRE"
echo "  PublicKey = $(cat "/etc/wireguard/$NOMBRE.pub")"
echo "  AllowedIPs = $IP/32"
echo
echo "Cuando eso este hecho, aca:  sudo systemctl enable --now wg-quick@wg0 && sudo wg show"
