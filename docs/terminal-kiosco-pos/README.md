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
