# Terminal kiosco POS — plantilla para montar una terminal nueva

Copia de respaldo del proyecto que convirtió la **Landi CX20 `caja1-samuel`**
(cliente Samuel) en una terminal kiosco de producción. Extraída de la caja el
2026-09-03 (`/home/adminpos/pos-terminal/` + scripts sueltos de `~`).

**La documentación principal es [`CLAUDE.md`](CLAUDE.md)**: bitácora completa
fase por fase (inventario, kiosco, hardware POS, integración con la app,
endurecimiento), con cada bug encontrado, su causa y su fix. Para montar otra
PC, ese archivo ES el runbook: se sigue en orden y ya avisa de todas las trampas
(conector fantasma DP-1, la purga de GNOME que casi se lleva `sudo` y la red,
el comando `P` pelado de la báscula Torrey, el choque `usblp`↔WebUSB, el
overlayroot que congela `/etc`…).

## Qué hay acá

| Carpeta | Contenido |
|---|---|
| `CLAUDE.md` | La bitácora/runbook completo del proyecto (contexto para Claude Code en la terminal nueva) |
| `bin/` | Scripts de todas las fases: instaladores idempotentes, diagnósticos y pruebas (`fase2-*`, `fase3-*`, `fase4-*`) |
| `etc/` | Unidades systemd (`pos-kiosk.service`), política de Chromium (`chromium-pos.json`), reglas udev |
| `www/` | Página de prueba local (teclado táctil + captura de scanner) usada antes de integrar la app real |
| `extras/` | `wg-caja1-setup.sh` (alta en la VPN WireGuard), `liberar-usblp.sh` y `fix-regla-disco.sh` (fix del claim de WebUSB con overlayroot activo) |

## Cómo usarlo para una terminal nueva

1. Copiar esta carpeta a la PC nueva como `~/pos-terminal` del usuario admin
   (p. ej. `adminpos`) y abrir Claude Code ahí — `CLAUDE.md` le da todo el
   contexto.
2. Seguir las fases en orden. Los instaladores son idempotentes, pero varias
   cosas son **específicas de la caja 1 y hay que adaptarlas**:
   - Hostname, IP de VPN (`10.10.0.X` siguiente libre — ver
     `docs/vpn-wireguard.md`) y claves WireGuard (en `extras/wg-caja1-setup.sh`).
   - El conector fantasma `video=DP-1:d` es propio del board del CX20; en otro
     hardware verificar los conectores DRM antes de copiarlo.
   - IDs USB de impresora/báscula si el hardware es otro (están en
     `etc/chromium-pos.json` y las reglas udev, en decimal y hex).
   - Usuarios: `caja1` (kiosco, grupos `lp`+`dialout`) y `adminpos` (sudo).
3. La Fase 4 (endurecimiento) va al final y **el orden interno importa**
   (consola antes que overlayroot). Con el disco congelado, los cambios
   persistentes van por `sudo overlayroot-chroot`.

## Estado de origen (2026-09-03)

Todo aplicado y validado en producción en `caja1-samuel`: kiosco arrancando
solo, impresora Epson TM-T20IV por WebUSB e imprimiendo desde
`admintools.supermercadosurbina.com`, báscula Torrey emparejada, overlayroot
activo, VPN operativa (peer `10.10.0.4`). La regla
`etc/99-pos-liberar-usblp.rules` ya está en su versión corregida
(`ACTION=="bind"`), validada en boot real.

Pendientes de la caja original: scanner y gaveta (falta el hardware), apagar
`PasswordAuthentication` de SSH, contraseña de GRUB.

## Terminales montadas con esta plantilla

| Terminal | VPN | Notas de la instalación |
|---|---|---|
| `caja1-samuel` | `10.10.0.4` | Origen de la plantilla (2026-09-03). Se instaló con GNOME y hubo que purgarlo (386 paquetes). |
| `caja2-samuel` | `10.10.0.5` | **2026-09-11/12**. Instalación mínima desde el inicio (331 paquetes, sin escritorio) → no hizo falta purgar. Ver «Lecciones de caja2» abajo. |
- **caja1-lafe** (Farmacia La Fe, 2026-09-19): Dell OptiPlex 3070, Debian 13.7 mínimo,
  **no táctil** (mouse+teclado), wifi por adaptador USB Realtek `0bda:c820`, solo
  impresora de tickets (sin báscula ni etiquetera). Admin `farmacialafe`, kiosco `caja1`
  (uid 1001, sin `dialout`). VPN `10.10.0.6`. **Las 4 fases completas por SSH el
  2026-09-19** (ticketera NXP Printer-80 por WebUSB; `/home` en `sda3`, la antigua swap;
  overlayroot ensayado con un arranque de un solo uso antes de dejarlo fijo; GRUB con
  clave; sudo pide contraseña). Lección: `su -c` sin `-` no tiene `/usr/sbin` en el
  PATH → `usermod` "no encontrado"; usar rutas completas o `su -`.

### Lecciones de caja1-lafe (2026-09-19)

1. **No validar una migración de red antes de que corra la red de seguridad.** En el
   reinicio de validación comprobé la red a los 30 s; `net-fallback` corría a los 90 s,
   su ping falló en ese instante y **restauró ifupdown y deshabilitó NetworkManager**: la
   caja llegó al cliente sabiendo solo la wifi del sitio de preparación. Regla: retirar
   `net-fallback` (o esperar sus 90 s) antes de dar por buena la migración; y no dejar en
   un equipo que cambia de sitio un fallback que deshabilite NM.
2. **Adaptador wifi USB Realtek RTL8821CU (`0bda:c820`) con un router WPA/WPA2 mixto
   TKIP en 2,4 GHz**: el driver del kernel `rtw88_8821cu` lo ve en el escaneo pero nunca
   se asocia (NM: `ssid-not-found` a los 25 s, sin intento de autenticación en el kernel);
   con un hotspot WPA2/WPA3-AES sí conecta. Probado sin éxito: proto/cifrados fijos,
   banda 2,4, powersave off, MAC aleatoria, recarga del driver + `wpa_supplicant`,
   BSSID/canal fijos, hidden, Bluetooth bloqueado. **Solución**: driver de fabricante
   `8821cu` de Morrownr por DKMS (`apt install dkms bc build-essential git
   firmware-realtek linux-headers-$(uname -r)`; `git clone
   https://github.com/morrownr/8821cu-20210916 /usr/src/8821cu-20210916 &&
   ./install-driver.sh NoPrompt`; blacklist `rtw88_8821cu`/`rtw88_8821c` en
   `/etc/modprobe.d/`) y recrear la conexión **desde el escaneo** (`nmcli dev wifi connect
   <BSSID> password … name …`), luego quitar el `bssid` y renombrar. Conecta por SSID
   solo y sobrevive al reinicio (DKMS recompila con cada kernel). El TP-Link "Archer T2U
   V2" que descargó el usuario era MediaTek MT7610U de 2015: otro chip, no aplica.
3. `rfkill` no viene en el Debian mínimo (bloquear BT: `echo 1 >
   /sys/class/rfkill/rfkillN/soft`). `nmcli con up` por SSH sin sesión gráfica necesita
   `sudo` (polkit) y un proceso lanzado con `&` muere al cerrar el SSH: usar `sudo` directo
   o `systemd-run`.
4. Con cable y wifi a la vez NM deja el cable como ruta principal (métrica 100 vs 600) y
   la wifi de respaldo; la VPN no se entera del cambio.
5. **Mover `/home` a otra partición en fase 4**: `rsync` NO viene en el Debian mínimo y
   un `a && b && c` bajo `set -e` no aborta si falla `a` → la caja reinició con `/home`
   vacío (sin llave SSH ni perfil del kiosco). Recuperada desde `tty2` (bloque D aún
   sin aplicar) con `sudo mount --bind / /mnt/raiz && sudo cp -a /mnt/raiz/home/. /home/`.
   Reglas: verificar la copia (`du`, `ls .ssh`) ANTES de tocar `fstab` o reiniciar; la
   plantilla ya instala `rsync`; tras trabajar en la consola local, cerrar la sesión de
   `tty2` o `chvt 1`, si no `cage` no arranca; borrar la copia vieja con un bind mount.
6. **Ensayar overlayroot con un arranque de un solo uso antes de dejarlo fijo** (sirve
   para cualquier caja sin nadie al lado). overlayroot también se activa por parámetro
   del kernel, así que: (a) entrada extra en `/etc/grub.d/40_custom` — copia de la
   línea `linux` de la entrada normal más `overlayroot=tmpfs:swap=1,recurse=0`, con
   `--unrestricted` si GRUB ya tiene clave — y `update-grub`; (b) `grub-reboot 'Debian
   (ensayo overlayroot, un solo arranque)'` (usa `next_entry` de `grubenv`: vale UNA vez,
   el siguiente arranque vuelve solo a la entrada normal); (c) `reboot` y, si vuelve por
   VPN, verificar `/` overlay, `/media/root-ro` ro, `/home` rw, kiosco y VPN; si NO
   vuelve, basta apagar y encender: arranca escribible como antes. (d) Aprobado el
   ensayo, la config permanente se puede dejar desde ese mismo arranque con
   `overlayroot-chroot` (escribir `/etc/overlayroot.conf`, quitar la entrada de ensayo,
   `update-initramfs -u`); **`update-grub` dentro de `overlayroot-chroot` falla**
   (`grub-probe: failed to get canonical path of /dev/sda2`, no monta `/dev`): hacer
   `mount -o remount,rw /media/root-ro`, bind-montar `dev dev/pts proc sys run` en
   `/media/root-ro`, `chroot /media/root-ro update-grub`, desmontar y `remount,ro`.
   Un reinicio más y queda fijo. Si el cierre (`fase4-cierre-instalacion.sh --sudo`)
   deja `/media/root-ro` en rw ("mount point is busy") y el `remount,ro` no entra, un
   reinicio lo deja `ro` (nada escribe ahí mientras tanto).
7. Un `grub-mkpasswd-pbkdf2 | grep …` por SSH parece colgado: el «Enter password:» va a
   stdout y se lo traga el `grep`. Usar `grub-mkpasswd-pbkdf2 | tee /dev/tty | grep -o
   "grub.pbkdf2.*" | sudo tee /root/grub-pass.hash` y comprobar con `test -s` — el
   `tee` no falla aunque reciba vacío, así que el "HASH GUARDADO" del eco no prueba nada.
8. Chromium mostraba el globo «Traducir» sobre el kiosco (app en español, Chromium en
   inglés) → `TranslateEnabled: false` en la política (ya en la plantilla). Para ver la
   pantalla del kiosco por SSH: `sudo -u caja1 env XDG_RUNTIME_DIR=/run/user/1001
   WAYLAND_DISPLAY=wayland-0 grim /tmp/kiosco.png` (`grim` ya en la plantilla).

### Lecciones de caja2 (aplicar en la próxima)

1. **En el instalador de Debian**: desmarcar todo entorno de escritorio; dejar solo
   «utilidades estándar del sistema» + «servidor SSH». Eso evita la purga de GNOME.
2. **Con contraseña de root, Debian NO instala `sudo` ni mete al primer usuario en
   ese grupo**: hay que instalarlo a mano (`su -` → `apt-get install sudo` →
   `usermod -aG sudo adminpos`). Y **guardar la contraseña de root**: si se pierde,
   se recupera desde GRUB con `init=/bin/bash` (`mount -o remount,rw /` + `passwd root`).
3. **Las unidades systemd traen el usuario `caja1` cableado**: adaptar `User=`/`Group=`
   **y** `XDG_RUNTIME_DIR=/run/user/<UID>` en `pos-kiosk.service` **y** en
   `pos-web.service` (este último se olvidó y falló con `status=217/USER`).
4. **`video=DP-1:d` hace falta igual** en otro CX20: el conector fantasma corre la
   consola y el kiosco hacia la derecha.
5. **Migrar de ifupdown a NetworkManager: hacerlo con un REINICIO, no en caliente.**
   En caliente falla porque el `wpa_supplicant` lanzado por ifupdown sigue sujetando
   la tarjeta y NM deja el dispositivo `unmanaged`. Procedimiento que funcionó:
   comentar la estrofa de la interfaz en `/etc/network/interfaces`, `systemctl disable
   networking`, `enable NetworkManager`, cargar las conexiones con `nmcli` y **reiniciar**.
   Conviene dejar una unidad de rescate (`net-fallback.service`) que restaure ifupdown
   si a los 90 s del arranque no hay conectividad.
6. **No dar por buena una migración de red porque «hay conectividad»**: verificar
   QUIÉN la sostiene (`nmcli dev status` debe decir `connected`, no `unmanaged`) y
   validar con reinicio.
7. **Redes guardadas en NM**: la del sitio donde se prepara el equipo + las dos del
   cliente (`Samuel 5G` y `Samuel 2.4G`), con `autoconnect-priority` mayor en las del
   cliente, así al llegar se conecta sin tocar nada.

### Fase 3 en remoto: configurar impresoras y báscula sin estar delante (2026-09-13)

Toda la fase 3 se puede hacer por SSH. Lo aprendido montándola en caja2:

1. **Los IDs USB cambian por terminal.** caja2 NO tiene la Epson de caja1: la
   ticketera es una 3nStar RPT004 (`1fc9:2016`), la etiquetera una 4BARCODE
   4B-2054TC (`2d84:8ffa`, la «3nStar LTT214» rebadgeada) y la báscula un FTDI
   (`0403:6001`). Copiar las reglas de caja1 sin cambiar los IDs no hace nada.
2. **La política de Chromium pide los IDs en DECIMAL**, no en hexadecimal
   (`1fc9:2016` → `8137:8214`). Con ella el cajero **nunca ve el selector de
   dispositivos** y el permiso sobrevive al borrado del perfil.
3. **Las impresoras van por WebUSB y la báscula por WebSerial**, y son caminos
   distintos: las primeras necesitan que udev las suelte de `usblp`; la báscula
   **no** hay que desengancharla de `ftdi_sio` — Chromium la usa por su
   `/dev/ttyUSB*`, y basta que el usuario del kiosco esté en `dialout`.
4. **Si se reconecta un USB con Chromium corriendo, hay que reiniciar Chromium.**
   El navegador conserva la referencia al dispositivo viejo: abre el puerto sin
   error pero **toda lectura muere al instante con «The device has been lost»**,
   y el puerto queda abierto con su lado de lectura inutilizable. Costó media
   tarde de diagnóstico. Regla: **cualquier manipulación de USB termina con
   `systemctl restart pos-kiosk.service`.**
5. **La config de impresora y báscula vive en el `localStorage` de CADA terminal**
   (`admintools-pos.scale`, `admintools-pos.printer`), y la pantalla que la edita
   (`/settings`) **es solo para ADMIN**. O sea: configurarla desde el
   administrador de otra PC no sirve de nada — hay que entrar como admin **en esa
   terminal**, o escribirla en remoto (punto siguiente).
6. **Para tocar la terminal en remoto** el script de arranque acepta
   `POS_EXTRA_FLAGS`. Con un drop-in de systemd
   (`Environment=POS_EXTRA_FLAGS=--remote-debugging-port=9222`) se levanta el
   puerto de depuración **solo en loopback**, y desde la propia máquina se puede
   evaluar JavaScript en la página (leer o escribir `localStorage`, comprobar
   permisos de dispositivo, pulsar botones). Dos detalles: hace falta
   `python3-websocket`, y hay que conectarse **sin cabecera `Origin`**
   (`suppress_origin=True`) o Chromium responde 403 — la alternativa sería
   `--remote-allow-origins=*`, que en una caja no se debe usar. **Quitar el
   drop-in al terminar.**
7. **Diagnóstico del hardware sin navegador**: `python3-serial` para la báscula
   (comando `P` pelado, sin retorno de carro, 9600 8N1) y `python3-usb` para las
   impresoras. Este último es imprescindible **después** de aplicar la regla que
   suelta `usblp`, porque ahí desaparece `/dev/usb/lp*` y ya no se puede probar
   escribiendo a un archivo.

### Etiquetera 3nStar LTT214 / 4BARCODE 4B-2054TC — lo que hay que saber

**Antes de diagnosticar nada por software, mirar el LED.** Es la información más
barata y la que más tiempo ahorra (manual LTT204/LTT214, §4.1):

| LED | Significa |
|---|---|
| Azul fijo | Lista |
| Azul parpadeando | Recibiendo datos o en pausa |
| Morado | Formateando los datos |
| **Rojo fijo** | **Cabezal o tapa ABIERTOS** (o error del cortador) |
| Rojo parpadeando | Sin papel, atasco o error de memoria |

**Síntoma real (caja2, 2026-09-13): «jala el papel pero no imprime».** La
impresora **aceptaba bytes por USB pero no respondía a ninguna consulta**, ni
siquiera al reinicio en tiempo real `<ESC>!R`. Causa: **el cabezal no había
quedado bien cerrado** — el LED estaba en rojo fijo. El manual lo marca como
paso crítico al instalar la cinta: *«asegúrese de que el cabezal quede
completamente cerrado»*, presionando con las dos manos a ambos lados.

**CORRECCIÓN (2026-09-14): el silencio a las consultas NO es señal de nada.**
Aquí quedó escrito que «si acepta datos y no contesta, es físico». Es falso:
una vez cerrada la tapa la impresora **imprime perfectamente y sigue sin
contestar** ni a `<ESC>!?` ni a `~!F` (solo respondió la primera vez que se la
interrogó, recién conectada). O sea que su canal de lectura por USB no es
fiable. **La señal buena es el LED**, y el papel que sale. No diagnosticar por
la ausencia de respuesta.

**El otro clásico de este modelo — la cinta**: las especificaciones exigen
cinta **con la tinta hacia AFUERA** (*Ink outside Coating*, *Outer roll type*).
Con una cinta de tinta hacia adentro, la tinta nunca toca la etiqueta: avanza el
papel y no marca, exactamente el mismo síntoma. Y si el papel es **térmico
directo**, la cinta sobra: se comprueba rayando una etiqueta con la uña (si deja
marca gris, es térmico directo).

**La prueba que resuelve la duda sin software**: apagar, mantener el botón FEED,
encender y soltar cuando el LED parpadee **morado** → calibra los sensores e
imprime su hoja de configuración interna. Si esa hoja sale, el mecanismo, la
cinta y el papel están bien y el problema es el formato que se le manda.

**⚠️ El POS todavía NO puede imprimir etiquetas en estas terminales.** US-162
imprime con `window.print()`, que necesita una impresora dada de alta en el
sistema operativo — y estas terminales **no tienen CUPS** (se purgó en caja1 y
nunca se instaló en caja2), así que el diálogo de impresión no tendría a dónde
mandar el trabajo. El camino que sí funciona está probado: **TSPL por WebUSB**,
igual que el ticket, que ya tiene su vía directa en `features/printing/escpos/`
+ `webusb.ts` (US-107). Falta el equivalente para la etiqueta — un
`etiquetaBytes.ts` que emita TSPL espejando `EtiquetaProducto.tsx`, con la misma
regla que el ticket: **los cambios de diseño van en los dos lados**.

Verificado el 2026-09-14 en caja2 con cinta y papel reales: la impresora dibuja
el EAN-13 con su comando nativo `BARCODE ... "EAN13"` a 203 dpi, que sale mucho
mejor que mandar una imagen.

**Herramientas dejadas en la terminal** (`~/pos-terminal/bin/`, requieren
`python3-usb` porque la regla de `usblp` elimina `/dev/usb/lp*`):
`etiquetera-estado.py` (consulta sin imprimir), `etiquetera-reset.py`
(`<ESC>!R` + limpieza de endpoints), `etiquetera-puesta-a-punto.py` (calibra y
saca tres etiquetas **numeradas** —  bloque negro, texto normal y térmico
directo — para que quien esté delante solo tenga que decir *cuál* salió; también
`--config` para la autoprueba interna y `--fabrica` para `INITIALPRINTER`).

### Estado de `caja2-samuel` — PENDIENTE (al 2026-09-12)

Listo y validado con reinicios reales: Debian mínimo, kiosco arrancando solo,
`video=DP-1:d`, NetworkManager con las 3 redes (la del sitio de preparación +
`Samuel 5G`/`Samuel 2.4G` con prioridad mayor) y **VPN peer `10.10.0.5`**
(`ssh caja2-samuel`).

Queda por hacer:

1. ~~**Fase 3 — impresora y báscula**~~ **HECHA el 2026-09-13** (ver la sección de
   arriba). Estado real: ticketera ✔ imprime y corta, por WebUSB desde el
   navegador; báscula ✔ lee (`1.06 lb`) por WebSerial desde la página del POS;
   **etiquetera ✘ jala papel pero no marca** — pendiente de resolver con el
   rollo/cinta correctos, el software ya le habla (responde TSPL).
2. **Fase 4 — endurecimiento**: `overlayroot`, contraseña de GRUB,
   `PasswordAuthentication no`. Hacer `overlayroot` **con alguien cerca del
   equipo**: es el único paso cuyo fallo deja la máquina sin arrancar y sin
   acceso remoto.
3. **Quitar los dos permisos temporales de la instalación** (paso obligatorio del
   cierre): `/etc/sudoers.d/90-instalacion-kiosco` (sudo sin contraseña) y
   `net-fallback.service` (la unidad de rescate que restaura ifupdown si no hay
   red a los 90 s del arranque).
4. **Traslado al sitio del cliente**: no hay que tocar la red, se conecta sola.

> Capacidad útil para el trabajo remoto: con `grim` (disponible en trixie) se
> puede **capturar la pantalla del kiosco** desde SSH usando el socket Wayland de
> la sesión (`/run/user/<UID>/wayland-*`), así no hace falta que nadie describa
> lo que se ve.
