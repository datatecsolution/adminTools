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
