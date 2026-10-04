# Laboratorio: caja1-lafe virtual con wifi simulada

Etapa C del [plan](../../plan-wifi-autoservicio.md) (§5.1). Una VM Debian 13 en QEMU que arranca **como caja1-lafe**: kiosco cage + Chromium con la página de espera de la plantilla, NetworkManager, usuario `caja1`, `/home` en otro disco y overlayroot. Además tiene **wifi simulada de verdad** con `mac80211_hwsim`: NetworkManager escanea, se asocia y pide la clave igual que con una placa real.

## Topología

```
 Mac ──ssh 127.0.0.1:2222──► mgmt0 10.0.2.15 (administración, SIN puerta de enlace)
   │   VNC 127.0.0.1:5901 = pantalla de la caja
   │
 ┌─┴────────────── VM «caja1-lab» ─────────────────────────────────────────────────┐
 │ wlan0 = placa wifi de la caja ← NetworkManager (la caja sale a internet SOLO por acá)│
 │   ┆ aire simulado (mac80211_hwsim, 6 radios)                                       │
 │ ┌─┴──────── espacio de red «router» (lab-router) ─────────────────────────────┐   │
 │ │ ap-alphanet ALPHANET-LAB  WPA/WPA2 TKIP+CCMP, canal 9   192.168.1.0/24       │   │
 │ │ ap-prueba   PRUEBA-LAFE   WPA2 (hotspot del operador)   192.168.43.0/24      │   │
 │ │ ap-soporte  LAFE-SOPORTE  WPA2 (emergencia)             192.168.44.0/24      │   │
 │ │ ap-sinred   SIN-INTERNET  WPA2, sin salida              192.168.45.0/24      │   │
 │ │ ap-abierta  ABIERTA       sin clave                     192.168.46.0/24      │   │
 │ │ dnsmasq (DHCP + DNS) · NAT → uplink0 → internet                              │   │
 │ │ POS falso 10.99.0.1:8080 (/healthz con CORS) · «hub» VPN 10.10.0.1 (ping)    │   │
 │ └──────────────────────────────────────────────────────────────────────────────┘   │
 └─────────────────────────────────────────────────────────────────────────────────┘
```

**Claves del laboratorio** (solo laboratorio): ALPHANET-LAB `alphanet1`, PRUEBA-LAFE `prueba123`, LAFE-SOPORTE `soporte123`, SIN-INTERNET `sinred123`.

## Uso (desde `docs/terminal-kiosco-pos/red/lab/`)

```bash
bash vm.sh crear                     # una vez: discos sobre la imagen Debian 13 arm64, llave y cloud-init
bash vm.sh iniciar                   # arranca en segundo plano
bash vm.sh scp -r ../.. soporte@127.0.0.1:/tmp/plantilla
bash vm.sh ssh 'sudo bash /tmp/plantilla/red/lab/aprovisionar.sh'
bash vm.sh apagar && bash vm.sh instantanea guardar c-aprovisionada   # ANTES de congelar
bash vm.sh iniciar && bash vm.sh ssh 'sudo bash /tmp/plantilla/red/lab/congelar.sh' && bash vm.sh reiniciar
bash verificar-c.sh                  # puerta de la etapa C

bash vm.sh lab estado                          # routers y servidor
bash vm.sh lab router ALPHANET-LAB apagar      # «se cae la wifi de la farmacia»
bash vm.sh lab router PRUEBA-LAFE prender      # «el operador comparte internet»
bash vm.sh lab clave ALPHANET-LAB otraclave1   # «cambiaron la clave del router»
bash vm.sh lab servidor apagar                 # «servidor caído»
bash vm.sh lab inicial                         # vuelve al estado inicial

bash vm.sh captura nombre            # PNG de la pantalla de la caja en ~/pos-lab-wifi/capturas
bash vm.sh instantanea guardar c-lista        # con la VM apagada
bash vm.sh instantanea volver c-lista
```

Pantalla y teclado de la caja: **VNC a `127.0.0.1:5901`** (en la Mac: Finder → Ir → Conectarse al servidor → `vnc://127.0.0.1:5901`).

Todo vive en `~/pos-lab-wifi` (`LAB=` para cambiarlo): imagen base verificada con su SHA-512, discos, llave SSH, `vars.fd` de UEFI, capturas y logs.

## Qué cubre y qué no
- ✅ Arranque, overlayroot, NetworkManager, escaneo y asociación wifi (incluido WPA/WPA2 mixto con TKIP como la ALPHANET), DHCP, clave incorrecta, red sin internet, servidor caído, «hub» de la VPN y el kiosco real de la plantilla.
- ❌ El driver Realtek `8821cu` de caja1-lafe (ver §5.4 del plan). Tampoco WireGuard real: el «soporte remoto» es un ping a 10.10.0.1.
- La VM es **arm64** (nativa en la Mac); caja1-lafe es amd64. No importa para NetworkManager, systemd, overlayroot, cage, Chromium ni pos-red.

## Lecciones del laboratorio

1. **overlayroot necesita `busybox` en el initramfs** (2026-10-03). Su script hace `mount --move`. Sin busybox, el `mount` mínimo de klibc responde «invalid option --» y la caja **entra en pánico al arrancar**. La imagen cloud de Debian no trae busybox; la instalación de caja1-lafe sí. `congelar.sh` lo verifica antes de congelar. **Pendiente: agregar el mismo control a `bin/fase4-overlayroot.sh`** para que una caja armada con una instalación mínima no quede sin arrancar.
2. **Instantánea antes de congelar**: la misma regla que el «arranque de un solo uso» de la fase 4 (lección 6 del README de la plantilla).
3. La imagen cloud de Debian 13 configura la red con **netplan + systemd-networkd**, no con ifupdown (`aprovisionar.sh` edita `/etc/netplan/50-cloud-init.yaml`).
4. **La raíz en vivo es de solo lectura** con este overlayroot, igual que en caja1-samuel y caja1-lafe: los cambios van por `overlayroot-chroot`. Por eso dnsmasq guarda sus concesiones en `/run/lab`, y la fase 5 guarda las redes en `/home`.
5. **En la VM, el kiosco necesita dibujo por software** (solo laboratorio, `kiosco-lab.conf`): `WLR_RENDERER=pixman` para cage y `--disable-gpu` para Chromium. Sin eso, pantalla en blanco o **congelada en el primer cuadro aunque Chromium siga navegando**. Por eso la puerta C mira los píxeles de la pantalla (`vista.py`), no solo lo que dice Chromium.
6. En systemd, `Environment=` con espacios va **entre comillas**; si no, se corta y la segunda opción se pierde sin aviso.

## Instantáneas
- `c-aprovisionada`: aprovisionada, todavía sin congelar.
- `c-lista`: congelada, kiosco mostrando el POS, puerta C en verde. **Punto de partida de las etapas D y E.**

