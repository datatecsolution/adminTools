# VPN WireGuard — soporte remoto a clientes

Red **hub-and-spoke** para alcanzar los servidores de los clientes (MySQL, SSH)
desde la Mac de soporte **sin estar en la LAN del cliente**. Todo el tráfico de
la VPN va por la subred `10.10.0.0/24`.

## Topología

| Nodo | IP WG | Rol | Acceso |
|---|---|---|---|
| **Ronal** | `10.10.0.1` | **Hub** (servidor central) | pública `201.190.38.238:51820`, `ssh root@10.10.0.1` |
| Mac soporte | `10.10.0.2` | Peer (cliente) | interface local `utun6` |
| **venecia** | `10.10.0.3` | Peer (server cliente) | `ssh <user>@10.10.0.3` |
| *(próximo)* | `10.10.0.4` | — | siguiente IP libre |

- El **hub (Ronal)** es el único con IP pública fija y `ListenPort`. Todos los
  peers se conectan a él; el hub **reenvía** el tráfico entre peers.
- Pubkey del hub (la que va en cada peer): `OhmA52Sf2FqxvzCCTgmtz/5rmLH90HTbu0nJWCTxayU=`
- Cada peer tiene `PersistentKeepalive = 25` apuntando al hub → si se cae y
  vuelve el internet, **reconecta solo en ≤25 s** (el endpoint del hub es una IP
  fija, no depende de DNS).

---

## Agregar un cliente nuevo a la red

Asigná la **siguiente IP libre** (`10.10.0.4`, `10.10.0.5`, …).

### 1) En el server del cliente — instalar WireGuard + generar claves
```bash
sudo apt update && sudo apt install -y wireguard
wg genkey | sudo tee /etc/wireguard/<cliente>.key | wg pubkey | sudo tee /etc/wireguard/<cliente>.pub
sudo cat /etc/wireguard/<cliente>.pub     # ← copiar esta clave PÚBLICA
```
> La clave **privada** (`.key`) **nunca sale del server del cliente.** Solo se
> comparte la pública.

### 2) En el server del cliente — `/etc/wireguard/wg0.conf`
Reemplazá `10.10.0.X` por la IP asignada:
```bash
KEY=$(sudo cat /etc/wireguard/<cliente>.key)
sudo tee /etc/wireguard/wg0.conf >/dev/null <<EOF
[Interface]
Address = 10.10.0.X/24
PrivateKey = $KEY

[Peer]
PublicKey = OhmA52Sf2FqxvzCCTgmtz/5rmLH90HTbu0nJWCTxayU=
Endpoint = 201.190.38.238:51820
AllowedIPs = 10.10.0.0/24
PersistentKeepalive = 25
EOF
sudo chmod 600 /etc/wireguard/wg0.conf
```

### 3) En el hub (Ronal) — registrar el peer
En vivo (no corta el túnel de los demás):
```bash
sudo wg set wg0 peer <CLIENTE_PUB> allowed-ips 10.10.0.X/32
```
Y para que persista al reboot, agregar el bloque al final de
`/etc/wireguard/wg0.conf` del hub (**cada peer su propio header `[Peer]`**):
```ini
[Peer]
# <cliente>
PublicKey = <CLIENTE_PUB>
AllowedIPs = 10.10.0.X/32
```

### 4) En el server del cliente — levantar + habilitar al boot
```bash
sudo wg-quick up wg0
sudo systemctl enable wg-quick@wg0
sudo systemctl start  wg-quick@wg0      # que systemd "tome" el túnel (no solo wg-quick up)
sudo wg show                            # debe verse el peer del hub con handshake
```
> ⚠️ Importante: además de `enable`, hacé `systemctl start` (o levantá con
> `systemctl` desde el principio). Si solo corrés `wg-quick up`, systemd lo ve
> `inactive` aunque la interfaz exista (estado inconsistente).

### 5) Verificar desde la Mac de soporte
```bash
ping -c2 10.10.0.X
nc -zv 10.10.0.X 22      # SSH
nc -zv 10.10.0.X 3306    # MySQL
```

### 6) Acceso a MySQL del cliente por la VPN
Dos requisitos en el server del cliente:
1. **`bind-address`** de MySQL debe incluir la IP de la VPN. `sudo ss -tlnp | grep 3306`
   → si ves `0.0.0.0:3306` ya está; si está atado solo a la LAN, poné
   `bind-address = 0.0.0.0` en `my.cnf` y `sudo systemctl restart mysql`.
2. **Grant** del usuario: debe permitir el origen `10.10.0.%` (o `%`). Si está
   como `usuario@'192.168.x.%'`, no entra por la VPN.

---

## El "gotcha" del reenvío entre peers (FORWARD)

El hub tiene `-P FORWARD DROP` (por Docker/ufw). El **ping** entre peers cruza
(ufw acepta ICMP reenviado) **pero el TCP nuevo NO** → cae en el DROP. Síntoma:
`ping 10.10.0.X` funciona pero `nc 10.10.0.X 3306` (o 22) falla.

**Solución (ya aplicada en Ronal):** permitir el forward `wg0`→`wg0`:
```bash
# en vivo:
sudo iptables -I FORWARD -i wg0 -o wg0 -j ACCEPT
```
Y persistirlo con `PostUp`/`PostDown` en la sección `[Interface]` del
`wg0.conf` del hub:
```ini
PostUp = iptables -I FORWARD -i wg0 -o wg0 -j ACCEPT
PostDown = iptables -D FORWARD -i wg0 -o wg0 -j ACCEPT
```
Esto vale **una sola vez** para todo el hub; los peers nuevos ya quedan cubiertos.

---

## Si cambia la IP pública del HUB (Ronal)

Es el **único** escenario que no se recupera solo, porque todos los peers
apuntan a `201.190.38.238` por IP fija. Si Ronal cambia de IP pública (cambio de
ISP, etc.), hay que actualizar el `Endpoint` en **cada peer**:

1. Averiguar la nueva IP pública del hub (en Ronal):
   ```bash
   curl -s ifconfig.me ; echo
   ```
2. En **cada** server cliente + la Mac de soporte, editar el `Endpoint` del
   `[Peer]` del hub en `/etc/wireguard/wg0.conf`:
   ```ini
   Endpoint = <NUEVA_IP>:51820
   ```
   y recargar sin cortar:
   ```bash
   sudo wg-quick down wg0 && sudo wg-quick up wg0     # o: sudo systemctl restart wg-quick@wg0
   ```
3. Verificar handshake: `sudo wg show`.

> **Cómo evitar este problema a futuro:** usar un **dominio dinámico (DDNS)** en
> el `Endpoint` (ej. `vpn.midominio.com:51820`) en vez de la IP. Así, si cambia
> la IP del hub, solo se actualiza el DNS y los peers re-resuelven (con
> `wg-quick` hay que re-levantar para que re-resuelva, o usar un timer que
> reinicie el servicio periódicamente).
>
> Nota: la IP pública de un **peer** (cliente) sí puede cambiar libremente — el
> hub la re-aprende del handshake. Solo importa la IP del **hub**.

---

## Verificar persistencia (sin reiniciar el server)

En cada server:
```bash
systemctl is-enabled wg-quick@wg0     # esperado: enabled  (arranca al boot)
systemctl is-active  wg-quick@wg0     # esperado: active
sudo wg-quick strip wg0 >/dev/null && echo "conf OK"
# clave privada real (no quedó en blanco/"hidden"), sin imprimirla:
sudo sed -n 's/^PrivateKey *= *//p' /etc/wireguard/wg0.conf | awk '{print length($0)" chars "(length($0)==44?"OK":"REVISAR")}'
```
En el **hub** además:
```bash
grep -c 'PostUp.*FORWARD.*wg0.*ACCEPT' /etc/wireguard/wg0.conf   # esperado: 1
sudo iptables -C FORWARD -i wg0 -o wg0 -j ACCEPT && echo "regla FORWARD viva: OK"
```
Prueba definitiva del arranque (simula el boot) — **solo si tu SSH NO va por el
túnel**: `sudo wg-quick down wg0 && sudo systemctl start wg-quick@wg0`.

---

## Comandos útiles de diagnóstico

| Qué | Comando |
|---|---|
| Estado del túnel + peers + handshakes | `sudo wg show` |
| ¿En qué interfaz sale el tráfico? (Mac) | `route -n get 10.10.0.X` → `interface: utun*` |
| ¿Puerto abierto por la VPN? (Mac) | `nc -zv 10.10.0.X <puerto>` |
| MySQL escuchando dónde | `sudo ss -tlnp | grep 3306` |
| Forward del hub | `sudo iptables -S FORWARD` |
