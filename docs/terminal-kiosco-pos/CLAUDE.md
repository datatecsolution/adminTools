# Terminal POS — Landi CX20 (`caja1-samuel`)

Contexto persistente del proyecto. Actualizar al cerrar cada fase.

## Objetivo

Terminal kiosco: al encender arranca directo a una app POS en React sobre Chromium
en modo kiosco, con backend local en Node para impresora ESC/POS y gaveta de dinero.
El cajero no debe poder salir de la app ni acceder al sistema.

## Estado del proyecto

| Fase | Descripción | Estado |
|------|-------------|--------|
| 1 | Inventario de hardware | ✅ Completada (2026-09-01) |
| 2 | Kiosco (`cage` + Chromium en tty1) | ✅ **Cerrada** (2026-09-01) — arranca solo, táctil 1:1 tras `video=DP-1:d`, GNOME purgado (386 paquetes) |
| 3 | Hardware POS (impresora + báscula + scanner) | ✅ **Cerrada** (2026-09-01) — báscula Torrey ✅ (`P` pelado); impresora Epson TM-T20IV-SP ✅ imprime y corta; `caja1` ✅ ya está en `lp` y `dialout`. ⏸️ **Aplazados por no tener el hardware: scanner y gaveta de dinero** |
| — | Integración con la app remota (`admintools.supermercadosurbina.com`) | ✅ **Cerrada** (2026-09-02) — config aplicada y reiniciado; **impresora emparejada e imprimiendo desde la app** |
| 4 | Endurecimiento (SSH, no auto-updates, overlayroot) | ✅ **Aplicada** (verificada 2026-09-03: overlayroot activo con `/` ro + `/home` rw, `apt-daily*` masked, SSH solo `adminpos`, cage sin `-s`) |
| — | VPN WireGuard (acceso remoto de soporte) | ✅ **Integrada** (2026-09-03) — esta caja es el peer `10.10.0.4` de la red del hub Ronal |

### ▶️ Dónde retomar (2026-09-03)

**Todas las fases cerradas y el equipo en producción.** La impresora imprime
desde la app por WebUSB, la báscula está emparejada, el disco está congelado
(overlayroot) y hay acceso remoto por VPN + SSH. Pendientes opcionales:

- Apagar `PasswordAuthentication` en SSH (la clave pública de la Mac de soporte
  ya está instalada en `adminpos` — reejecutar `fase4-ssh.sh` o editar vía
  `overlayroot-chroot`).
- Contraseña de GRUB y retirar el teclado USB de setup (ver Fase 4).
- Scanner de barras y gaveta cuando llegue el hardware (ver Fase 3).

### 🌐 VPN WireGuard (2026-09-03)

Esta caja es el peer **`10.10.0.4`** de la VPN de soporte (hub = Ronal,
`201.190.38.238:51820`; Mac de soporte = `10.10.0.2`). Config en
`/etc/wireguard/wg0.conf`, servicio `wg-quick@wg0` enabled, keepalive 25 s.
Desde la Mac se entra con `ssh caja1-samuel` (alias; usuario `adminpos`, llave
pública instalada — `/home` no está congelado, así que la llave persiste).
⚠️ La clave pública de esta caja lleva **O mayúscula** en `...Q3Oc8e...`
(no cero) — un cero ahí ya causó un día de handshakes descartados en silencio.

> 🔎 **Hallazgo 2026-09-02 sobre `usblp` — RESUELTO el 2026-09-03.** La regla
> `99-pos-liberar-usblp.rules` original usaba `ACTION=="add"` y **nunca se
> disparaba** (al procesarse el evento *add*, `usblp` aún no está enganchado).
> Mientras tanto la impresora funcionaba porque Chromium desenganchaba él mismo
> el driver (`USBDEVFS_DISCONNECT`)… hasta que el 2026-09-03 dejó de poder y la
> app tiró `Unable to claim interface` al emparejar.
> **Fix aplicado**: la regla ahora usa `ACTION=="bind"` (corregida en el disco
> real vía `overlayroot-chroot`, backup `*.bak-antes-bind`) y **quedó validada
> en el boot de 2026-09-03 09:58**: la interfaz `3-8.1:1.0` arranca sin driver
> y `/dev/usb/lp0` ya no existe. Consecuencia: `fase3-impresora.py` ya no
> funciona (no hay nodo `lp0`); la impresión va 100% por WebUSB desde la app.
> Recordatorio operativo: con overlayroot, `/etc` en vivo es **read-only** — un
> `sed -i` ahí falla; los cambios reales van por `sudo overlayroot-chroot`
> (y al salir, si dice `mount point is busy`, rematar con
> `sudo mount -o remount,ro /media/root-ro`).

---

## FASE 1 — Inventario (levantado 2026-09-01)

### Sistema

- **Hostname**: `caja1-samuel`
- **SO**: Debian GNU/Linux 13 (trixie)
- **Kernel**: 6.12.107+deb13-amd64 (x86_64)
- **Machine ID**: 2c802cac8b8947768301dfd89bcb8639
- **default target**: `graphical.target` *(cambiado a `multi-user.target` en la Fase 2)*
- **Paquetes instalados**: 1588 *(→ 1218 tras la purga de GNOME de la Fase 2)*

### CPU / RAM

- Intel Alder Lake-N (Celeron N97) — 4 núcleos, 800–3600 MHz
- RAM: 7.5 GiB · Swap: 7.7 GiB

### Almacenamiento

Disco `sda` — 238.5 GB:

| Partición | Tamaño | FS | Montaje |
|-----------|--------|-----|---------|
| sda1 | 976M | vfat | `/boot/efi` |
| sda2 | 19.2G | ext4 | `/` (5.1G usados, 29%) |
| sda3 | 7.7G | swap | `[SWAP]` |
| sda4 | 210.6G | ext4 | `/home` (492M usados, 1%) |

`sdb` (7.1G, vfat, `ESD-USB`) = pendrive de instalación montado en
`/media/adminpos/ESD-USB`. Desmontar y retirar antes de la Fase 2.

> ⚠️ `/` tiene solo 19.2G y `/home` 210.6G. Chromium + Node + logs viven en `/`.
> Tenerlo presente para overlayroot en la Fase 4.

### Gráficos y pantalla

- Intel UHD Graphics (Alder Lake-N), PCI `00:02.0` — driver en uso: **`i915`** (`xe` también cargado)
- Xwayland presente; sesión actual bajo GNOME/Wayland

#### ⚠️ Conectores DRM — hay un conector FANTASMA (hallazgo 2026-09-01, Fase 2)

| Conector | Estado | Panel (EDID) | Tamaño | Modo |
|----------|--------|--------------|--------|------|
| `card0-eDP-1` | connected | BOE `NV156FHM-N22` | 34×19 cm (15.6") | **1920x1080** ← panel real |
| `card0-DP-1` | connected | BOE `NE173QHM-NY3` | 38×22 cm (17.3") | 1280x800 ← **NO EXISTE físicamente** · hoy `disconnected`/`disabled` por `video=DP-1:d` |
| `card0-DP-2` | disconnected | — | — | — |

`DP-1` reporta `connected` con un EDID válido de 128 bytes, pero **este equipo no tiene
segunda pantalla**. El board del CX20 es compartido con las variantes de doble pantalla
(display para el cliente) y trae el EDID grabado aunque el panel no esté montado.

La consola corre en `eDP-1` (`/sys/class/graphics/fb0` → `i915drmfb`, 1920x1080), así que
apagar `DP-1` es seguro.

**Fix aplicado y validado** (2026-09-01 12:54, boot con `video=DP-1:d` en `/proc/cmdline`): `video=DP-1:d` en `GRUB_CMDLINE_LINUX_DEFAULT` (backup en
`/etc/default/grub.bak`). Rescate si algo sale mal: en el menú de GRUB, `e`, borrar el
parámetro, `Ctrl+X`.

### 🖐️ Pantalla táctil — FUNCIONA SIN DRIVERS DEL PROVEEDOR

- **Dispositivo**: `ILTK0001:00 222A:0001` (Ilitek), bus **I2C HID** vía
  `i2c_designware.0` → PCI `00:15.0`
- **Nodo**: `/dev/input/event4` (+ `mouse0`)
- **Sysfs**: `/devices/pci0000:00/0000:00:15.0/i2c_designware.0/i2c-1/i2c-ILTK0001:00/0018:222A:0001.0001`
- **Módulos del kernel en uso**: `hid_multitouch`, `i2c_hid_acpi`, `i2c_hid`, `hid`
  — todos **stock de Debian**, ningún blob del fabricante
- **Capacidades reportadas**:
  - `PROP=2` → `INPUT_PROP_DIRECT` (pantalla táctil directa, no touchpad)
  - `KEY` → `BTN_TOUCH`
  - `ABS=0x260800000000003` → `ABS_X`, `ABS_Y`, `ABS_MT_SLOT`,
    `ABS_MT_POSITION_X`, `ABS_MT_POSITION_Y`, `ABS_MT_TRACKING_ID`
    → **multitouch protocolo tipo B operativo**

**Conclusión para el reclamo**: el táctil es enumerado y manejado íntegramente por
el kernel Linux mainline mediante HID sobre I2C. El hardware **no depende de los
drivers retenidos por el proveedor**.

`libinput-tools` no está instalado, por eso no se corrió `libinput list-devices`;
la evidencia anterior sale de `/proc/bus/input/devices`, sysfs y `lsmod`.
Instalarlo en la Fase 2 para calibración/verificación interactiva.

### Puertos serie (RS232)

`/dev/ttyS0`–`/dev/ttyS3` presentes, permisos `crw-rw---- root:dialout`.
Ninguno se usa: los cuatro son puertos de la placa y están libres.

> **Actualizado 2026-09-01 (Fase 3):** ya existe **`/dev/ttyUSB0`** — es el
> adaptador CH340 con la **báscula Torrey**. Sigue sin haber `/dev/ttyACM*`.
> (El texto original de la Fase 1 decía "nada USB-serie conectado todavía".)

> El usuario `caja1` necesita pertenecer al grupo `dialout` si la impresora o la
> gaveta se conectan por RS232.
> **✅ RESUELTO (verificado 2026-09-01): `caja1` YA está en `dialout` (gid 20).**
> Le sigue faltando `lp` para la impresora USB — ver Fase 3 › Permisos.

### USB (estado actual)

| ID | Dispositivo | Nodo | Estado |
|-----|-------------|------|--------|
| `1a86:7523` | QinHeng **CH340** serial converter | `/dev/ttyUSB0` | ⚖️ **Báscula Torrey** — ✅ funciona |
| `04b8:0e39` | Seiko Epson **TM-T20IV-SP** | `/dev/usb/lp0` | 🖨️ **Impresora** — ✅ imprime y corta |
| `045e:0750` | Microsoft Wired Keyboard 600 | — | Teclado de setup (quitar en Fase 4) |
| `8087:0032` | Intel AX210 Bluetooth | — | Sin uso |
| `125f:c08a` | A-DATA C008 Flash Drive | — | Pendrive de instalación, **no montado**. Retirar. |
| `0424:2514` ×2 | Hubs internos USB 2.0 (Microchip/SMSC) | — | Internos |

Controladores: Thunderbolt 4 USB (`00:0d.0`) y xHCI USB 3.2 (`00:14.0`).

**Aún no conectado**: scanner de código de barras (⏸️ aplazado, ver Fase 3).

> La tabla original de la Fase 1 listaba Bus:Dev, pero esos números cambian en
> cada reconexión. Lo estable es el **ID de vendor:product**, que es lo que se
> lista arriba y lo que debe usar cualquier regla `udev`.

### Red

| Interfaz | Tipo | Estado |
|----------|------|--------|
| `wlp2s0` | Intel Wi-Fi 6E AX210 | **UP** — 192.168.1.228/24 |
| `enp3s0` | Realtek RTL8111/8168 Gigabit | DOWN (sin cable) |

Gestionadas por NetworkManager + wpa_supplicant.

### Otros

- Audio: Intel HDA (mic, headphone, 4× HDMI/DP)
- No se detectó puerto RJ11 / caja de dinero como dispositivo propio — cuelga de la
  impresora ESC/POS (la gaveta se abre por pulso desde la impresora).
  **⏸️ Confirmado 2026-09-01: no hay gaveta física disponible.** Ver Fase 3.

---

## FASE 2 — Kiosco (✅ validada en reboot limpio 2026-09-01 12:54)

### Decisión de arquitectura

Ante la discrepancia nº 1 (había un GNOME 48 completo instalado) se eligió la
**opción (a): kiosco mínimo propio**, sin GDM ni sesión de escritorio.

- **Compositor**: `cage` — compositor Wayland de una sola ventana, sin escritorio,
  sin barra, sin lanzador. Si la ventana muere, `cage` muere.
- **Navegador**: `chromium --kiosk` de los repos de Debian (nada de Chrome ni snaps).
- **Arranque**: `multi-user.target` (consola) + unidad systemd propia en tty1.
  **No hay display manager ni autologin de sesión gráfica**: `pos-kiosk.service`
  corre como `caja1` y `PAMName=login` le crea la sesión logind con seat.
- **La purga de GNOME se difirió a propósito** hasta tener el kiosco probado, para no
  quedarse sin escritorio de rescate si algo fallaba. **Ejecutada el 2026-09-01 13:11**
  una vez validado el kiosco (ver «Purga de GNOME» más abajo).

### Piezas (fuente en `~/pos-terminal`, desplegado en `/opt/pos` y `/etc/systemd/system`)

| Fuente | Destino | Qué hace |
|--------|---------|----------|
| `bin/pos-kiosk-start` | `/opt/pos/bin/pos-kiosk-start` | `exec cage -d -s -- chromium --kiosk …` |
| `www/index.html` | `/opt/pos/www/index.html` | Página de prueba: teclado táctil + captura de scanner |
| `etc/pos-web.service` | `/etc/systemd/system/` | `python3 -m http.server 8080 --bind 127.0.0.1` (provisional) |
| `etc/pos-kiosk.service` | `/etc/systemd/system/` | El kiosco en tty1, `Restart=always` |
| `bin/fase2-instalar.sh` | — | Instalador idempotente de todo lo anterior (7 pasos) |
| `bin/fase2-probar.sh`, `bin/fase2-probar-vt.sh` | — | Pruebas del kiosco con vuelta automática a tty2 |
| `bin/diag-vt.sh`, `bin/diag-timeline.sh` | — | Diagnóstico de VT / `VT_GETMODE` / línea de tiempo |
| `bin/red-de-seguridad.sh` | — | Gettys de rescate + parada del kiosco de prueba |

Detalles que importan de `pos-kiosk.service`:

- `User=caja1` + `PAMName=login` → sesión logind real con seat0, necesaria para que
  `cage` tome el DRM y los `/dev/input/*` **sin ser root**.
- `Conflicts=getty@tty1.service` y `getty@tty1` deshabilitado → tty1 es solo del kiosco.
- `StartLimitIntervalSec=0` + `Restart=always` → un kiosco tiene que insistir para siempre.
- `XDG_RUNTIME_DIR=/run/user/1000` (UID de `caja1`; lo crea `pam_systemd` al abrir sesión).
- `pos-kiosk-start` limpia `exit_type`/`exited_cleanly` en el `Preferences` del perfil
  antes de arrancar: si no, Chromium muestra el globo *«restaurar páginas»* tras cada
  `Restart`.

### Estado del sistema tras el despliegue (verificado 2026-09-01 12:05)

| Ítem | Estado |
|------|--------|
| `cage`, `chromium`, `libinput-tools` | instalados |
| `/opt/pos/**` y unidades systemd | idénticos al repo (`diff` limpio en los 4 archivos) |
| `pos-web.service` | `enabled` + `active` — responde HTTP 200 · 2463 bytes en `127.0.0.1:8080` |
| `pos-kiosk.service` | `enabled` (hoy `failed`, ver más abajo) |
| `getty@tty1` | `disabled` |
| `getty@tty2`, `getty@tty3` | `enabled` + `active` — rescate garantizado |
| default target | `multi-user.target` |
| `display-manager.service` | symlink eliminado → GDM no arranca en el próximo boot |

### 🔍 Por qué el kiosco NO se pudo probar en caliente

`cage` arranca bien y logind le crea la sesión a `caja1` en **tty1/seat0**, pero
**se queda dormido esperando a que su VT esté activo** para tomar el DRM, y por eso
nunca lanza Chromium (`Tasks: 0`, 22 ms de CPU).

Cronología del diagnóstico, por si reaparece:

1. `systemctl start pos-kiosk` → servicio activo, `cage` con PID, **0 hijos**.
2. `chvt 1` desde `pkexec` → falla en silencio (pkexec corre sin consola controladora).
3. `loginctl activate <sesión>` → también falla en silencio.
4. `diag-vt.sh` → `VT_GETMODE` confirma que **alguien tiene el VT en `VT_PROCESS`**
   (es mutter: debe autorizar cada cambio de VT).
5. `diag-timeline.sh` (muestreo 1 s × 20 s) → **el VT sí cambia y se mantiene en tty1
   los 20 segundos**, pero la sesión del kiosco sigue con **`Active=no`** todo el rato.

**Conclusión**: no es un fallo del kiosco, es que **la prueba está contaminada por la
sesión GNOME/mutter en marcha**, que retiene seat0 e impide que `logind` marque activa
la sesión de tty1. En un arranque limpio (sin GDM, `multi-user.target`) tty1 es el VT
activo desde el principio y no hay competencia.

> El `failed (Result: timeout)` que muestra hoy `pos-kiosk.service` es el
> `systemctl stop` de `red-de-seguridad.sh` matando ese `cage` colgado (SIGKILL,
> exit 137). **No es una falla real del servicio.**

### 🛟 Vías de rescate activas (antes de reiniciar)

- `Ctrl+Alt+F2` / `Ctrl+Alt+F3` → gettys permanentes, login como `adminpos`.
- `cage` corre con **`-s`** (permite `Ctrl+Alt+Fn` desde dentro del kiosco).
  **Quitar ese `-s` en la Fase 4** para encerrar al cajero, no antes.
- Desde una consola de rescate:
  `sudo systemctl stop pos-kiosk && sudo systemctl set-default graphical.target && sudo reboot`
  devuelve el sistema al GNOME de siempre.

### 🐛 Primer reboot real (2026-09-01 12:20) — el kiosco arrancó pero el táctil "no hacía nada"

Síntoma reportado: *"me aparece un teclado numérico en la parte derecha de la pantalla,
toco los números y no pasa nada"*.

No era el táctil ni el kiosco: era **el conector fantasma `DP-1`** (ver Fase 1).

`cage` 0.2.0 no sabe elegir salida. Con dos outputs los pone en fila y arma un layout
virtual de **3200x1080** (1920 de `eDP-1` + 1280 de `DP-1`). De ahí los dos síntomas:

1. `index.html` centra su tarjeta → cae en x ≈ 1600 del layout, que sobre el panel de
   1920 es **el tercio derecho**. De ahí "el teclado a la derecha".
2. El táctil se mapea al layout completo, no al panel → un toque en `x` físico aterriza
   en `x × 3200/1920` ≈ `x × 1.67`. Los botones no responden porque el puntero cae 67%
   más a la derecha. (Prueba que lo confirma: tocando el **centro horizontal** de la
   pantalla se activan los botones que se ven a la derecha.)

**Solución**: apagar `DP-1` por cmdline (`video=DP-1:d`). Con una sola salida el layout
queda 1920x1080 y el táctil mapea 1:1.

> Si en el futuro se quiere usar de verdad un display para el cliente, `cage` no alcanza:
> hay que pasar a `labwc` o `sway`, que sí permiten configurar salidas y mapear el táctil
> a una de ellas. Dejarlo para la Fase 3/4.

Nota aparte: `pos-kiosk.service` mostró `NRestarts=1` en ese boot (Chromium salió a los
~70 s y systemd relevantó todo). Pendiente de mirar en el journal.

### ✅ Validación post-fix (boot 2026-09-01 12:54)

`sudo bash bin/fase2-fix-dp1.sh` + `reboot`. Evidencia recogida en el boot siguiente:

| Comprobación | Resultado |
|---|---|
| `/proc/cmdline` | `… ro quiet video=DP-1:d` ← el parámetro llegó al kernel |
| `card0-DP-1` | `disconnected` · `enabled=disabled` ← el fantasma quedó apagado |
| `card0-eDP-1` | `connected` · `enabled` · `1920x1080` ← única salida viva |
| `/sys/class/graphics/fb0/virtual_size` | `1920,1080` (ya no 3200x1080) |
| `pos-kiosk.service` | `active (running)`, `NRestarts=0` ← el reinicio a los ~70 s del boot anterior no se repitió |
| `pos-web.service` | `active (running)` |
| procesos | `cage` PID 890 + **10 procesos de Chromium** |
| `loginctl` | sesión 1 de `caja1`, `seat0`, `tty1`, `Type=wayland` |
| **prueba táctil del operador** | **el teclado numérico responde donde se toca** ✔ |

> `loginctl show-session 1 -p Active` da `Active=no` **solo mientras se está en tty2**
> (que es donde corre esta consola de admin). Es lo esperado: el VT activo manda.
> Si se necesita comprobarlo de verdad, hay que mirarlo desde tty1.

Notas del boot:

- `cage` y Chromium quedan en el cgroup `user-1000.slice/session-1.scope`, **no** en el
  del servicio — se los lleva `pam_systemd` al crear la sesión con `PAMName=login`.
  Por eso `systemctl status pos-kiosk` muestra `Tasks: 0` aunque todo esté corriendo.
  Consecuencia práctica: un `systemctl stop/restart` puede dejar Chromium huérfano.
  **Revisar `KillMode`/`ExecStopPost` en la Fase 4.**
- El pendrive `ESD-USB` ya no aparece en `lsusb` — fue retirado. ✔
- `/` al 32% (5.6G de 19G). La purga de GNOME bajó esto al 23% (4.0G de 19G).

### 🧹 Purga de GNOME (✅ ejecutada 2026-09-01 13:11)

`sudo bin/fase2-purgar-gnome.sh --aplicar`. Los 51 paquetes `gnome*`/`gdm3`/`task-gnome*`
ya no tenían función (default target `multi-user.target` + symlink de
`display-manager.service` borrado), y con el kiosco validado el escritorio de rescate
dejó de ser necesario — el rescate real son los gettys de tty2/tty3 y `cage` con `-s`.

Ambas cosas estaban puestas antes de purgar; el paso 1/5 del script las verifica y
aborta si falta alguna.

#### ☠️ Trampa del `autoremove` (analizada en simulacro, 2026-09-01)

`apt purge --autoremove` de los metapaquetes de GNOME se lleva **402 paquetes**, y
entre ellos:

| Paquete | Por qué se iba | Consecuencia |
|---|---|---|
| `sudo` | marcado `auto`, colgaba de `task-desktop` | **`adminpos` se queda sin sudo** |
| `network-manager` | marcado `auto` | **sin Wi-Fi** (`wlp2s0` es la única red; `enp3s0` no tiene cable) |
| `wpasupplicant` | marcado `auto` | ídem |
| `fonts-noto-color-emoji` | dependencia de GNOME | emojis en cuadritos en la UI del POS |

O sea: el comando obvio deja el equipo **sin red y sin privilegios de admin a la vez**.
No habría forma de arreglarlo en remoto.

Por eso `bin/fase2-purgar-gnome.sh` hace `apt-mark manual` sobre la lista
`IMPRESCINDIBLE` **antes** de purgar, y luego vuelve a simular y **aborta** si alguno
de esos paquetes sigue apareciendo en la lista de borrado. Con la protección puesta:

- 402 → **386 paquetes**, ~**1.3 GB** liberados en `/`
- `sudo`, `network-manager`, `wpasupplicant`, `cage`, `chromium`, kernel, GRUB y
  firmware Wi-Fi: **intactos** ✔
- Fuentes: quedan `fonts-dejavu*` y `fonts-liberation*` → Chromium renderiza bien ✔
- `dbus` y `polkitd` no se tocan (solo se van los bindings `gir1.2-polkit-1.0`,
  `libdbusmenu-*`, `python3-dbus`) ✔
- `--no-install-recommends` evita que apt **instale** `notification-daemon` en medio
  de una purga (lo hacía para cubrir el virtual que dejaba `gnome-shell`)

> Regla general para este equipo: **nunca correr `apt autoremove` a secas.** Media
> instalación está marcada `auto` porque vino de los tasksel de Debian.

#### Resultado real de la purga

| Comprobación (verificada 2026-09-01 13:15) | Resultado |
|---|---|
| Paquetes purgados | **386** — lista en `/home/adminpos/paquetes-purgados.txt` |
| Paquetes `ii` | 1588 → **1218** |
| `/` libre | 13G → **14G** (uso 23%) |
| Los 13 `IMPRESCINDIBLE` | los 13 `install ok installed` ✔ |
| `sudo` / Wi-Fi | `sudo` presente · `wlp2s0` connected, 192.168.1.228 ✔ |
| `pos-kiosk` · `pos-web` · `getty@tty2` · `getty@tty3` | los 4 `active` ✔ |
| `cage` + Chromium | corriendo (PID 890, con `-s`) ✔ |
| `http://localhost:8080` | HTTP 200 ✔ |
| `dpkg --audit` / paquetes en estado `rc` | limpio · 0 ✔ |

Restos de GNOME que **sí** quedan (9, todos como dependencia legítima, no basura):
`gjs`, `gnome-desktop3-data`, `gnome-keyring`, `gnome-keyring-pkcs11`,
`gnome-settings-daemon`(`-common`), `gnome-sushi`, `gnome-terminal`(`-data`).

> `gnome-keyring` ya no hace falta: `pos-kiosk-start` lanza Chromium con
> `--password-store=basic`. Candidato a purgar en la Fase 4.

#### 🐛 Dos secuelas menores (ambas resueltas)

1. **El script murió en la línea 131, después de purgar y antes del paso 5/5.**
   `dpkg -l` ordena con collation C pero `comm` esperaba la del locale `es_ES`
   → `comm` salió con código 1 → `set -e` mató el script. Por eso `salida.txt` termina
   en seco, no se imprimió el 5/5 y `paquetes-purgados.txt` quedó `root:root` (el
   `chown` de la línea 132 tampoco corrió) **con 396 líneas, 10 de ellas falsos
   positivos** (`python3.13`, `python3.13-minimal`, `libcolord2`, `libjxl0.11`,
   `libavif16`, `libgtk3-perl`, `libgtksourceview-4-0`/`-common`,
   `libebackend-1.2-11t64`, `imagemagick-7-common` — todos siguen instalados).
   *Arreglado*: `LC_ALL=C sort` en ambos lados del `comm` y `|| true` en la línea 131,
   para que un fallo cosmético post-purga no vuelva a abortar la verificación.
   La lista se regeneró: 386 entradas, 0 falsos positivos.
2. **`fwupd-refresh.service` en `failed`** — su timer disparó a las 13:10:46, en mitad
   de la purga, cuando la unidad ya no existía. Fantasma en `systemctl --failed`, sin
   efecto real. *Se limpia con* `sudo systemctl reset-failed fwupd-refresh.service`
   (o solo, en el próximo reboot).

#### ✅ Reboot de confirmación tras la purga (boot 2026-09-01 13:18)

| Comprobación | Resultado |
|---|---|
| `/proc/cmdline` | `… ro quiet video=DP-1:d` ✔ |
| default target | `multi-user.target` ✔ |
| `pos-kiosk` / `pos-web` / `getty@tty2` / `getty@tty3` | `enabled` + `active` ✔ |
| `getty@tty1` | `disabled` ✔ |
| `pos-kiosk.service` | `Result=success`, **`NRestarts=0`** ✔ |
| `systemctl --failed` | **vacío** — el fantasma de `fwupd-refresh` se fue solo en el reboot ✔ |
| `journalctl -b -p err` | sin entradas ✔ |
| procesos | `cage -d -s` PID 855 + **10 procesos de Chromium** ✔ |
| conectores DRM | `eDP-1` connected/enabled · `DP-1` y `DP-2` disconnected/disabled ✔ |
| `fb0/virtual_size` | `1920,1080` ✔ |
| `http://127.0.0.1:8080` | HTTP 200 · 2463 bytes ✔ |
| Wi-Fi | `wlp2s0` UP · 192.168.1.228/24 ✔ |
| `dpkg --audit` · paquetes `rc` | limpio · 0 ✔ |
| paquetes `ii` | 1218 ✔ |
| `/` | 4.0G de 19G (23%) ✔ |
| los 13 `IMPRESCINDIBLE` | los 13 `install ok installed` ✔ |
| arranque | userspace **2.9 s** hasta `multi-user.target` (27.5 s totales, 19.5 s son firmware+GRUB) |

> El arranque sigue limpio tras la purga. **Fase 2 cerrada del todo.** Lo único que
> queda por probar a mano en este boot es el táctil (ya validado en el boot de 12:54,
> y nada de lo purgado toca el camino `i2c-hid` → `cage`).

---

## FASE 3 — Hardware POS

### ⚖️ Báscula Torrey (L-PC / L-EQ) — ✅ RESUELTA (2026-09-01)

**El dato que hay que recordar: el comando es `P` PELADO.** Un solo byte ASCII
`0x50`, **sin CR y sin LF**. La báscula contesta con el peso + CR.

```
puerto     /dev/ttyUSB0   (adaptador CH340, USB 1a86:7523)
velocidad  9600 8N1
pedir      b"P"           <- pelado. NO b"P\r"
recibir    peso + CR
```

Verificado corriendo:

```
sudo python3 ~/pos-terminal/bin/fase3-torrey.py /dev/ttyUSB0
```

y respondió la primera petición de la lista, `'P' pelado (el del manual)`.
Ese script **funciona tal cual: no tocarlo.**

#### 🐛 Por qué el diagnóstico genérico dijo que estaba muda

`bin/fase3-bascula.py` recorre su lista `COMANDOS`, y en esa lista la `P` va
**siempre con CR** (`b"P\r"`). La `P` pelada no está en la lista, así que
nunca se mandó el único comando que esta báscula entiende. De ahí salieron
los `no contesta a ningun comando` de `~/fase3.txt` y `~/bascula-encendida.txt`.

**✅ HECHO (2026-09-01):** `("P pelado", b"P")` está ahora **al principio** de
`COMANDOS` en `bin/fase3-bascula.py`, así que el sondeo genérico ya la encuentra
a la primera en vez de darla por muda.

#### ⚠️ Pistas falsas que ya están descartadas — no volver a recorrerlas

- **El cable NO es el problema.** El loopback en el extremo lejano dio eco
  exacto a 9600 y 115200 (`~/loopback.txt`, paso 4/4): adaptador y cable
  conducen bien en ambos sentidos. **No hace falta ningún adaptador
  null-modem.** El veredicto de `fase3-torrey.py` que apunta al cable solo
  aplica si la báscula calla, y ya no calla.
- **Las líneas de control no sirven de señal aquí.** CTS/DSR/DCD/RI salen
  todas bajas con la báscula conectada y desconectada por igual: esta
  báscula no las cablea. Eso no significa que esté muerta.
- **La velocidad tampoco era el problema.** 9600 8N1 es la correcta.

#### ModemManager — ⛔ YA NO APLICA (verificado 2026-09-01)

**ModemManager ya no existe en el sistema**: `systemctl is-enabled ModemManager`
devuelve `not-found`. Se lo llevó por delante la purga de GNOME de la Fase 2.

Nada puede robarle `/dev/ttyUSB0` a la báscula. En consecuencia:

- `bin/fase3-ignorar-mm.sh` es **letra muerta**, no hay que ejecutarlo.
- El `ID_MM_CANDIDATE=1` que pone `udev` es inofensivo: no queda ningún
  demonio que lo lea.
- Ya no hay nada que deshabilitar en la Fase 4 por este motivo.

### 🖨️ Impresora Epson TM-T20IV-SP — ESC/POS directo (2026-09-01)

```
USB        04b8:0e39  Seiko Epson Corp. TM-T20IV-SP
nodo       /dev/usb/lp0   (lo crea el modulo usblp, ya cargado)
permisos   root:lp 660
hablar     bytes ESC/POS crudos escritos al nodo
```

**No hay CUPS** (purgado en la Fase 2): no existen `lpstat`, `lpr` ni colas de
impresión. No buscarlas. El backend Node escribirá los bytes directamente.

Script de prueba:

```
sudo python3 ~/pos-terminal/bin/fase3-impresora.py                # estado + ticket
sudo python3 ~/pos-terminal/bin/fase3-impresora.py --solo-estado  # sin gastar papel
sudo python3 ~/pos-terminal/bin/fase3-impresora.py --gaveta       # ademas dispara la gaveta
```

Hace cuatro cosas: revisa nodo y grupos, **pregunta el estado en tiempo real**
(`DLE EOT 1..4`), imprime un ticket de prueba y opcionalmente lanza el pulso de
la gaveta. La gaveta **no se dispara sin `--gaveta`**.

#### Comandos que hay que recordar

| Qué | Bytes | Nota |
|---|---|---|
| Inicializar | `1B 40` | `ESC @` — deja estado conocido |
| Página de códigos | `1B 74 10` | `ESC t 16` = WPC1252, necesaria para `ñ` y acentos |
| Corte parcial | `1D 56 42 00` | `GS V B 0`, con avance previo |
| **Pulso de gaveta** | `1B 70 00 19 FA` | `ESC p 0 25 250`, pin 2 del RJ11 |
| Estado tiempo real | `10 04 n` | `DLE EOT n`, n = 1..4; contesta aunque esté ocupada |

`DLE EOT 1` bit 2 = pin 3 del conector de gaveta: **ALTO = gaveta conectada y
cerrada**, BAJO = abierta *o* no hay gaveta enchufada. Es la forma de saber si
la gaveta existe sin abrirla.
`DLE EOT 2` bit 2 = tapa abierta, bit 5 = sin papel, bit 6 = error.

#### ✅ Impresión validada (2026-09-01)

**Ticket de prueba impreso y cortado correctamente.** La impresora imprime y el
corte parcial (`GS V B 0`) funciona. **No hace falta volver a probar la
impresión**: el camino ESC/POS → `/dev/usb/lp0` está confirmado de punta a punta.

#### ✅ Lo que quedaba pendiente de la impresora — hecho

- ~~`sudo usermod -aG lp caja1`~~ → **ejecutado y verificado (2026-09-01)**:
  `caja1` ya está en `lp`. Estaba en `lpadmin` (de CUPS, ya purgado), que no
  sirve para esto. ✅ **Ya tomó efecto**: tras el boot de las 20:56 el proceso
  del kiosco lleva `Groups: 7 20 …` (lp y dialout).
- ~~Confirmar si hay gaveta física~~ → **⏸️ APLAZADO: no hay gaveta.** Ver abajo.

---

### ⏸️ Periféricos APLAZADOS — no están físicamente disponibles

Ninguno de los dos está bloqueado por software: **falta el aparato**. No volver
a diagnosticarlos ni a buscarles causa, y no dar la Fase 3 por incompleta por
su culpa.

| Periférico | Estado | Cuando aparezca |
|---|---|---|
| **Scanner de código de barras** | ⏸️ No disponible (2026-09-01) | Enchufar y mirar `/dev/input/by-id/` (si es HID, lo normal) o `ls /dev/ttyUSB* /dev/ttyACM*` (si es serie). `www/index.html` ya trae el campo de captura para probarlo. |
| **Gaveta de dinero** | ⏸️ No disponible (2026-09-01) | Conectar al RJ11 de la impresora y correr `fase3-impresora.py --gaveta`. El pulso es `ESC p 0 25 250` (`1B 70 00 19 FA`). Verificar antes con `--solo-estado`: bit 2 de `DLE EOT 1` en ALTO = gaveta detectada. |

**El backend Node se puede escribir igual**: los dos son código ya conocido —
el scanner llega como pulsaciones de teclado terminadas en Enter, y la gaveta
son 5 bytes al mismo `/dev/usb/lp0` que ya funciona. Se dejan implementados y
se prueban el día que llegue el hardware.

---

### 🔑 Permisos

| Nodo | Dueño | Grupo que hace falta | `caja1` |
|---|---|---|---|
| `/dev/ttyUSB0` (báscula) | `root:dialout 660` | `dialout` | ✅ ya lo tiene (gid 20) |
| `/dev/usb/lp0` (impresora) | `root:lp 660` | `lp` | ❌ **le falta** |

⚠️ **`lpadmin` NO es `lp`.** `caja1` está en `lpadmin`, que sólo servía para
administrar colas de CUPS — y CUPS fue purgado en la Fase 2. Para escribir
ESC/POS directo al nodo hace falta el grupo `lp` a secas:

```
sudo usermod -aG lp caja1        # ✅ ya ejecutado el 2026-09-01
```

(Requería volver a iniciar sesión para tomar efecto. **Ya hecho**: en el boot
de las 20:56 del 2026-09-01 el proceso del kiosco arrancó con `Groups: 7 20 …`.)

---

---

## 🌐 INTEGRACIÓN CON LA APP REMOTA (2026-09-01, tarde)

### Qué es

`https://admintools.supermercadosurbina.com/` — **AdminTools · DataTec**, SPA de
React/Vite servida tras Cloudflare, con `manifest.webmanifest` (es PWA, también
se usa desde iPad). **Ahí vive todo: frontend y backend.** Este terminal solo
aporta la pantalla táctil y el hardware.

> Por eso **`nodejs` y `npm` no se instalan**. El plan viejo de un backend Node
> local está cancelado, no archivado: no hay nada que escribir aquí.

### 🔌 Cómo llega la app al hardware — WebUSB y WebSerial, sin puente local

Leído del bundle `/assets/index-*.js` de la propia app (2026-09-01):

| Periférico | API del navegador | Filtro que usa la app |
|---|---|---|
| Impresora Epson | **WebUSB** (`navigator.usb.requestDevice`) | `vendorId: 1208` = `0x04b8` (Epson) |
| Báscula Torrey | **Web Serial** (`navigator.serial.requestPort`) | puerto elegido a mano, `open({baudRate, dataBits, parity, stopBits})` |

Las dos exigen **HTTPS + Chromium** (la app comprueba `window.isSecureContext`),
y las dos se cumplen aquí. **No hace falta ningún agente local en `127.0.0.1`.**

#### ✅ La app ya trae el protocolo correcto de la Torrey

En su registro de protocolos hay tres: `simulator`, `cas-continuous`
(CAS, streaming) y **`torrey-demand` — «Torrey (a demanda)», cuyo `pollCommand`
es `new TextEncoder().encode("P")`**: la **`P` pelada, sin CR**. Es exactamente
el hallazgo de la Fase 3. No hay que pedirle ningún cambio a la app.

> ⚠️ **El valor por defecto es `protocol: "simulator"`**, que **inventa pesos**
> (`Math.random()`, ±0.15 lb). Si no se cambia, la caja parecerá funcionar y
> estará pesando mentiras. Hay que entrar a **Configuración → Báscula** y elegir
> «Torrey (a demanda)» + 9600 8N1.
> La app lee kg o lb y **devuelve libras** (factor 2.2046226218).

Los ajustes se guardan en `localStorage`, clave `admintools-pos.scale`, dentro
del perfil `/home/caja1/.config/pos-chromium`. `/home` es partición aparte
(`sda4`), así que sobrevive; **tenerlo presente al montar overlayroot en la
Fase 4 — si el overlay tapa ese perfil, la caja vuelve a "simulator" en cada
arranque.**

### Lo que se preparó en este terminal (2026-09-01) — falta aplicar

| Archivo | Destino | Para qué |
|---|---|---|
| `etc/pos-kiosk.service` | `/etc/systemd/system/` | `POS_URL` → la app remota, y `Wants/After=network-online.target` |
| `etc/chromium-pos.json` | `/etc/chromium/policies/managed/` | Concede impresora y báscula a ese origen **sin selector de dispositivos** |
| `etc/99-pos-webusb.rules` | `/etc/udev/rules.d/` | `TAG+="uaccess"` sobre `04b8:0e39` y `1a86:7523` |
| `etc/99-pos-liberar-usblp.rules` | ⚠️ **sin instalar** | Red de emergencia, ver abajo |
| `bin/app-remota-instalar.sh` | — | Instalador idempotente de los tres primeros |

```
sudo bash ~/pos-terminal/bin/app-remota-instalar.sh && sudo reboot
```

#### Por qué `network-online.target`

La app ya no es local. Sin red, Chromium abre su página de error **y se queda
ahí**: no reintenta solo, y `Restart=always` no ayuda porque el proceso no muere.
`NetworkManager-wait-online` estaba `enabled` pero **`inactive`**, porque nadie
tiraba de `network-online.target` en un `multi-user.target`. Ahora lo hace el
propio kiosco. (Red: Wi-Fi `wlp2s0`, SSID `Mayorga 5G`; `enp3s0` sin cable.)

#### IDs en decimal (que es como los quiere la política de Chromium)

| Dispositivo | Hex | Decimal |
|---|---|---|
| Epson TM-T20IV-SP | `04b8:0e39` | `1208:3641` |
| CH340 (báscula) | `1a86:7523` | `6790:29987` |

#### ⚠️ El choque `usblp` ↔ WebUSB — previsto, no resuelto a ciegas

La interfaz 0 de la impresora (`3-8.1:1.0`) está **enganchada al módulo
`usblp`**, que es justo lo que crea `/dev/usb/lp0`. Un driver del kernel
sujetando la interfaz puede impedir que WebUSB la reclame.

**No se tocó por adelantado**, a propósito: `/dev/usb/lp0` es un camino ya
validado (imprime y corta) y soltarlo lo mata junto con `fase3-impresora.py`.
El orden correcto es **probar primero** el emparejamiento en la app; si falla
con «no se pudo reclamar la interfaz», instalar
`etc/99-pos-liberar-usblp.rules` (desengancha `usblp` **solo** de esta
impresora, sin descargar el módulo) y reiniciar.

Permisos del USB crudo, ya comprobados — no son el problema:
`/dev/bus/usb/003/008` (impresora) sale `root:lp 0664` y `caja1` está en `lp` ✔

### ✅ Cerrado el 2026-09-02

1. ✅ `sudo bash bin/app-remota-instalar.sh` + reboot — aplicado.
2. ✅ Báscula emparejada en Configuración → Báscula.
3. ✅ **Impresora emparejada e imprimiendo desde la app** (WebUSB).
4. ▶️ Con la caja operando de punta a punta, se pasa a la Fase 4.

---

## 🔒 FASE 4 — Endurecimiento (scripts escritos 2026-09-02, sin aplicar)

### Decisiones tomadas (usuario, 2026-09-02)

| Decisión | Elegido | Por qué |
|---|---|---|
| SSH | **Sí, solo `adminpos`** | Con el disco congelado y la consola cerrada, sin SSH cualquier cambio exige ir al local con teclado y monitor. |
| Vía de rescate `Ctrl+Alt+Fn` | **Cerrarla al final** | Se deja abierta durante toda la fase y se cierra en el penúltimo paso, ya con SSH probado. |
| overlayroot | **Congelar `/`, dejar `/home` escribible** | El perfil de Chromium de `caja1` vive en `/home` (sda4) y con él la config de la báscula. Si se congelara, la caja volvería a `simulator` —pesos inventados— en cada arranque. |

### Orden de aplicación — importa

Los bloques van de menos a más irreversible, y **el D va antes que el C** a
propósito: cerrar la consola toca `/opt/pos/bin/pos-kiosk-start`, y una vez
congelado el disco eso ya solo se puede editar desde `overlayroot-chroot`.

| Paso | Script | Qué hace | Reversible |
|---|---|---|---|
| **A** | `bin/fase4-endurecer.sh` | `mask` de `apt-daily{,-upgrade}.{timer,service}`, `avahi-daemon`, `udisks2`; retira `pos-web.service`; journal a RAM (64M) | Sí, `systemctl unmask` |
| **B** | `bin/fase4-ssh.sh` | Instala `openssh-server`; `AllowUsers adminpos`, root prohibido, sin forwarding. Apaga la contraseña **solo si** `adminpos` ya tiene `~/.ssh/authorized_keys` | Sí |
| **D** | `bin/fase4-cerrar-consola.sh` | Quita el `-s` de `cage` (bloquea `Ctrl+Alt+Fn`) y enmascara `ctrl-alt-del.target` | Sí, `--abrir` |
| **C** | `bin/fase4-overlayroot.sh` | `overlayroot="tmpfs:swap=1,recurse=0"` + `update-initramfs`. **Requiere reboot** | Sí, `--desactivar` |
| — | `bin/fase4-verificar.sh` | Informe de estado de los cuatro bloques + hardware. No toca nada | — |

Atajos en `~`: `fase4-endurecer.sh`, `fase4-ssh.sh`, `fase4-cerrar-consola.sh`,
`fase4-overlayroot.sh`, `fase4-verificar.sh`.

### Por qué `recurse=0` y no el overlay completo

`overlayroot` con `recurse=1` (su valor por defecto) overlaya **todos** los
montajes de `/etc/fstab`, `/home` incluido. Aquí eso sería un tiro en el pie:
los ajustes de la báscula viven en `localStorage` (`admintools-pos.scale`) dentro
de `/home/caja1/.config/pos-chromium`, y el protocolo por defecto de la app es
`simulator`, **que inventa pesos con `Math.random()`**. Con `recurse=1` la caja
amanecería cada día pesando mentiras convincentes. `recurse=0` congela solo la
raíz. El preflight del script aborta si `/home` no es partición propia.

`swap=1` deja que el overlay en RAM tire de la swap de 7.7G en un turno largo en
vez de morir sin memoria.

### 🚨 Cómo tocar el sistema con el disco ya congelado

Un `sudo apt install` o un `sudo nano /etc/...` **parecen funcionar y se evaporan
al reiniciar**. Para un cambio de verdad:

```
sudo overlayroot-chroot        # entra al disco real, en rw
...cambios...  exit
sudo reboot
```

Descongelar del todo: `sudo bash ~/fase4-overlayroot.sh --desactivar && sudo reboot`.
Si un arranque falla: en GRUB, `e`, añadir `overlayroot=disabled` a la línea del
kernel, `Ctrl+X`.

> El journal pasa a `Storage=volatile`: **no sobrevive al reinicio**. Con
> overlayroot no sobreviviría igualmente. Para diagnosticar un fallo hay que
> mirarlo en caliente por SSH: `journalctl -b -u pos-kiosk.service`.

### Pendientes opcionales de esta fase

- **Contraseña de GRUB.** Con teclado puesto, cualquiera puede editar la línea
  del kernel en el menú y arrancar con `overlayroot=disabled` o
  `systemd.unit=rescue.target`. No se ha hecho: es la última puerta y bloquearla
  también complica el rescate legítimo. Decidir aparte.
- **Retirar el teclado USB de setup** (`045e:0750`) cuando la caja quede en
  producción.
- **Clave pública para `adminpos`** (`ssh-copy-id`) y reejecutar `fase4-ssh.sh`
  para apagar la autenticación por contraseña.

---

## Usuarios

| Usuario | UID | Home | Grupos | Rol |
|---------|-----|------|--------|-----|
| `caja1` | 1000 | `/home/caja1` | `caja1`, **`lp`**, **`dialout`**, `audio`, `video`, `cdrom`, `floppy`, `dip`, `plugdev`, `users`, `netdev`, `scanner`, `bluetooth`, `lpadmin` | Cajero, sin privilegios |
| `adminpos` | 1001 | `/home/adminpos` | `adminpos`, `sudo`, `users` | Administrador |

Ambos existen ya con shell `/bin/bash`.

**Grupos que importan para el POS:**

| Grupo | Para qué | `caja1` |
|---|---|---|
| `dialout` | `/dev/ttyUSB0` — báscula | ✅ lo tiene |
| `lp` | `/dev/usb/lp0` — impresora | ✅ lo tiene y **ya está activo** (boot 2026-09-01 20:56) |

⚠️ **Dos falsos amigos en esa lista de grupos, no confundirse:**
- **`lpadmin` no es `lp`.** `lpadmin` administraba colas de CUPS y CUPS fue
  purgado. No da acceso a `/dev/usb/lp0`.
- **`scanner` no tiene nada que ver con el scanner de código de barras.** Es el
  grupo de SANE (escáneres de imagen). El lector de barras es HID (teclado) y
  no necesita ningún grupo.

Comprobar de un vistazo si falta el `lp`:

```
id -nG caja1 | tr ' ' '\n' | grep -x lp && echo "ya esta" || echo "FALTA lp"
```

---

## ⚠️ Discrepancias con el plan original

1. ~~**No es un "sistema mínimo"**~~ → **RESUELTO del todo en la Fase 2**: se eligió la
   opción (a), kiosco mínimo con `cage`, y el 2026-09-01 13:11 se **purgó GNOME**
   (386 paquetes). Ya no queda ni GDM ni sesión de escritorio. Texto original del hallazgo:
   hay un **GNOME 48 completo instalado y corriendo**
   (`task-gnome-desktop`, `gnome-shell`, `gdm3` activo, Firefox ESR con 4 locales).
   El plan de la Fase 2 asumía instalar una sesión gráfica mínima desde cero.
   Hay que decidir: **(a)** desinstalar GNOME y montar kiosco mínimo
   (cage/weston o Xorg+openbox), o **(b)** dejar GNOME y usar GDM autologin con
   una sesión personalizada. La opción (a) es más limpia y segura para kiosco.
2. `caja1` tiene UID 1000 y `adminpos` UID 1001 — o sea, `caja1` se creó durante la
   instalación de Debian. Verificar que `caja1` **no** esté en `sudo` (confirmado: no lo está).
3. El hostname es `caja1-samuel`, no algo genérico. Cambiarlo si molesta.

## Software añadido / pendiente

Instalado en la Fase 2: `cage`, `chromium`, `libinput-tools`.

Fase 4 (decidido 2026-09-02, aún sin instalar): **`openssh-server` SÍ**
(`bin/fase4-ssh.sh`) y **`overlayroot` SÍ** (`bin/fase4-overlayroot.sh`, ya
disponible en trixie: `0.18.debian14`). ❌ **`unattended-upgrades` NO** — una caja
no se actualiza sola a mitad de turno; en su lugar se enmascaran los timers
`apt-daily*` y las actualizaciones se hacen a mano.

Descartados por la opción (a) de la Fase 2: `xserver-xorg`, `openbox`, `weston`,
`greetd`, `lightdm`.

❌ **`nodejs` / `npm` — descartados** (2026-09-01). Estaban previstos para un
backend ESC/POS local que **ya no existe**: la app corre en
`admintools.supermercadosurbina.com` y habla con la impresora y la báscula por
WebUSB/WebSerial desde el propio Chromium. **No instalarlos.**

## Servicios en marcha que habrá que revisar en la Fase 4

> Resuelto en `bin/fase4-endurecer.sh` — ver «FASE 4» arriba. Esta tabla queda
> como el porqué de cada decisión.

La purga de la Fase 2 se llevó por delante **`gdm`, `bluetooth`, `cups`, `cups-browsed`,
`fwupd`, `power-profiles-daemon` y `ModemManager`** — esas unidades ya no existen
(reverificado 2026-09-01). Quedan por revisar:

> 🖨️ **Sin CUPS no hay `lpstat`, `lpr` ni colas de impresión.** A la impresora se
> le habla escribiendo bytes ESC/POS directos a `/dev/usb/lp0`. No buscar una cola.

| Servicio | Estado | Qué hacer en la Fase 4 |
|---|---|---|
| `avahi-daemon` (+ `.socket`) | `enabled` | Descubrimiento mDNS en la LAN. Deshabilitar salvo que la impresora se conecte por red. |
| `udisks2` | `enabled`, pero **`inactive`** | Automontaje de USB — **deshabilitar** (`mask`) para que el cajero no monte pendrives. Ahora está parado, pero arranca solo por activación D-Bus. |

---

## Convenciones

- Todo el proyecto vive en `~/pos-terminal` (usuario `adminpos`).
- Comandos con `sudo` se revisan antes de aplicar (especialmente Fase 2: autologin + systemd).
- Vía de rescate si el arranque queda en negro: `Ctrl+Alt+F2` → entrar como `adminpos`.
