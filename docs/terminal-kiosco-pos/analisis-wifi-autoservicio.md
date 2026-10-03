# Análisis: que el cliente conecte la terminal a una red wifi sin nosotros

> 2026-10-03. Terminales kiosco Debian 13 (cage + Chromium) de esta plantilla: caja1-samuel, caja2-samuel y caja1-lafe. La Landi Android no entra: Android ya trae su pantalla de wifi.

## 1. El problema hoy

Si cambia la clave del router, se cambia el router o la caja se mueve de lugar, **la terminal se queda sin red y solo nosotros podemos arreglarlo**:

- El cajero **no tiene dónde hacerlo**. No hay escritorio ni applet de red. `cage` va sin `-s` y la consola de `tty2` se cierra en la fase 4.
- **Nosotros tampoco llegamos por la VPN**, porque la VPN viaja por esa misma wifi. Hoy la salida es ir al lugar o el truco del hotspot con el nombre de una red conocida. Es lo que pasó con caja1-lafe el 2026-09-19.
- Aunque la red se cambie a mano en vivo, **con overlayroot se pierde al reiniciar**, porque `/etc` está en tmpfs. Ver el punto 2.3.

## 2. Restricciones que impone la terminal

### 2.1 Sin red no hay POS
La pantalla de wifi **no puede vivir en el POS remoto**. Justo cuando hace falta, el POS no carga. Tiene que ser **local**, servida por la propia terminal.

La terminal ya tiene un punto de entrada para eso: `pos-kiosk-start` abre `www/esperando.html` (`file://`) cuando `/healthz` no responde al arrancar.

### 2.2 El cajero no tiene permisos
El usuario del kiosco (`caja1`, `caja2`) **no tiene sudo**, y después de la fase 4 el sudo pide clave. Hace falta un intermediario con permisos sobre NetworkManager y una API muy acotada.

### 2.3 overlayroot congela `/etc`
NetworkManager guarda las conexiones en `/etc/NetworkManager/system-connections/`. Con overlayroot activo, una red nueva **funciona hasta el próximo reinicio y después desaparece**. Es la misma clase de problema que el `resolv.conf` congelado de La Fe (`README.md`, lección 10).

**Solución propuesta:** que NetworkManager guarde las conexiones en la partición `/home`, que es escribible y persiste. Con un drop-in en `conf.d`:

```ini
[keyfile]
path=/home/pos-red/system-connections
```

Las conexiones de base se dejan **congeladas en la raíz**, en `/usr/lib/NetworkManager/system-connections/`. Son el cable y la wifi que dejamos al instalar. El plugin keyfile lee esa carpeta además de `path`, pero no la modifica. Así:
- las redes que agrega el cliente persisten en `/home`;
- las nuestras no se pueden borrar desde la pantalla;
- si `/home` falla, la caja sigue arrancando con el cable y con la wifi original.

> A verificar en el ensayo: que la versión de NetworkManager de Debian 13 lea ambas rutas como se espera, y que NetworkManager arranque después de que `/home` esté montado. `NetworkManager.service` va después de `local-fs.target`, pero hay que confirmarlo en la caja.

### 2.4 DNS
Después del fix de La Fe (`fix-dns-resolv-nm.sh`), cada conexión lleva sus DNS a mano. Una red nueva traerá los del DHCP, y en la farmacia el DHCP daba 8.8.8.8, que no respondía.

**Propuesta:** el intermediario crea la conexión con `ipv4.dns 1.1.1.1` **sin** `ignore-auto-dns`. Se suma al DNS del router, no lo reemplaza.

Requisito previo: el enlace `resolv.conf → /run/NetworkManager/resolv.conf` tiene que estar aplicado en **todas** las cajas. caja1-samuel lo tiene pendiente.

### 2.5 Teclado
- Las CX20 de Samuel son **táctiles** y no tienen teclado físico.
- El teclado en pantalla (OSK) es parte del POS de React, así que **no existe en la página local**.
- La página de wifi necesita su **propio teclado mínimo**: letras, números, símbolos, mayúsculas y «mostrar clave».
- caja1-lafe tiene teclado físico; ahí el teclado en pantalla se oculta.

### 2.6 Hardware y drivers
La pantalla configura redes; **no arregla drivers**.

caja1-lafe necesitó el driver `8821cu` por DKMS, y el router WPA/TKIP mixto no se asociaba con `rtw88`. Esto no cambia, pero con overlayroot y `apt-daily` enmascarado el kernel no se actualiza solo, así que el módulo DKMS se mantiene.

Lección que sí se aplica: **crear la conexión desde el escaneo** (`nmcli dev wifi connect <BSSID|SSID> password …`), no a mano. Así conectó la ALPHANET.

## 3. Opciones

| | Opción | A favor | En contra |
|---|---|---|---|
| **A** | **Intermediario local + página local de wifi** (`http://127.0.0.1:8090/`). Se abre desde la página de espera y, con red, desde el POS | Funciona sin red, que es el caso que importa. No depende del servidor. Se instala una vez por terminal | Código nuevo en la plantilla. Requiere teclado propio en la página |
| B | Pantalla de wifi **dentro del POS** que llama al intermediario | Misma interfaz que el resto del POS | **No sirve sin red**. Además, las llamadas de una página pública a `127.0.0.1` las restringe Chromium (Local Network Access) |
| C | **Punto de acceso de configuración**: sin red, la caja levanta su propia wifi y se configura desde el celular (estilo *wifi-connect*) | No usa la pantalla | Exige que el adaptador soporte modo AP, que con el Realtek es dudoso. Más piezas, y el cliente tiene que entender el procedimiento. Las cajas ya tienen pantalla |
| D | `nmtui` en una consola con atajo de teclado | Ya existe | Reabre la consola que cerró la fase 4 y da shell al cajero. Mala interfaz táctil |

**Recomendación: A.** Desde el POS solo se agrega un **botón que navega** a la página local; la interfaz de wifi no se duplica.

## 4. Diseño propuesto (opción A)

### 4.1 Piezas
```
/opt/pos/red/pos-red.py                 intermediario (Python 3 de stdlib: http.server + subprocess nmcli)
/opt/pos/red/www/                       página de wifi (HTML/JS sin dependencias, teclado incluido)
/etc/systemd/system/pos-red.service     User=posred, escucha SOLO en 127.0.0.1:8090
/etc/polkit-1/rules.d/50-pos-red.rules  permite a posred las acciones de NM necesarias
/etc/NetworkManager/conf.d/50-pos-red.conf   [keyfile] path=/home/pos-red/system-connections
/usr/lib/NetworkManager/system-connections/  cable + wifi de instalación (congeladas)
```
`python3` ya está en las cajas: lo usan `pos-kiosk-start` y los scripts de la fase 3.

### 4.2 API del intermediario (mínima)
| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/estado` | Si hay wifi, red actual, IP, si hay internet (sondea `/healthz` del POS) y si hay VPN |
| GET | `/api/redes` | Redes visibles (`nmcli -t dev wifi list --rescan yes`): SSID, señal, seguridad, cuál está conectada |
| POST | `/api/conectar` | `{ssid, clave}` → conecta desde el escaneo y prueba (ver 4.3) |
| POST | `/api/olvidar` | Borra **solo** redes agregadas por el cliente (las de `/home/pos-red`) |

**Reglas de seguridad:**
- Escucha solo en `127.0.0.1`.
- Rechaza cualquier `Origin` que no sea el suyo. Así una página remota no puede llamarlo, ni siquiera el propio POS.
- Valida el SSID (1–32 bytes) y la clave (8–63 caracteres, o vacía si la red es abierta).
- `subprocess` con lista de argumentos, nunca con shell.
- No expone comandos genéricos ni toca el cable, la VPN ni las conexiones de base.
- Se ejecuta como un usuario dedicado, `posred`, y no como root. polkit le permite solo `org.freedesktop.NetworkManager.settings.modify.system`, `network-control`, `wifi.scan` y `enable-disable-wifi`.

### 4.3 Conectar sin dejar la caja peor que antes
1. Recordar la conexión activa.
2. `nmcli dev wifi connect "<ssid>" password "<clave>" name "<ssid>"`. La conexión nueva lleva prioridad 40, por encima de las de base (10–30), y `ipv4.dns 1.1.1.1`.
3. Esperar hasta 30 s a que haya IP **y** responda el `/healthz` del POS.
4. Si conecta: listo. La red queda guardada en `/home` y se mostrará «Conectado — volviendo al punto de venta».
5. Si falla: se borra la conexión nueva, se reactiva la anterior y se muestra el motivo. Para que el mensaje sea útil se distinguen «clave incorrecta» (`secrets were required`), «sin internet en esa red» y «no se encontró la red».
6. La VPN (`wg-quick@wg0`) no se toca. WireGuard retoma solo al cambiar la ruta, gracias al `PersistentKeepalive`. Si el `Endpoint` es un nombre DNS resuelto al arranque, se mantiene la IP resuelta.

### 4.4 Página de wifi
- Lista de redes con barras de señal, candado y cuál está conectada. Botón «Buscar de nuevo».
- Al tocar una red: campo de clave con «mostrar», el **teclado en pantalla propio** y el botón Conectar.
- Estado arriba: «Conectado a X · Internet ✔ · Soporte remoto ✔». El de soporte remoto es la VPN y ayuda cuando llamen.
- Botón «Volver al punto de venta», que navega al `POS_URL`.
- Si la terminal no tiene wifi: «Esta terminal no tiene red inalámbrica; conecte el cable».
- Botones grandes, de al menos 44×44 (misma regla `.toque-min` del POS).

### 4.5 Cómo llega el cajero a la pantalla
1. **Sin red (el caso principal):** `esperando.html` ya sondea `/healthz` cada 3 s. A los ~15 s sin respuesta mostrará un botón grande **«Configurar wifi»** que navega a `http://127.0.0.1:8090/?volver=<POS_URL>`. Navegar de `file://` a `127.0.0.1` es local → local y Chromium no lo restringe.
2. **Con red, para cambiar a otra:** botón «Red wifi de esta terminal» en el menú del POS. Solo aparece si la terminal tiene una marca en su `localStorage` (`admintools-pos.terminal = {wifi: true}`), que se escribe al instalar, igual que la impresora. Así no hace falta sondear `127.0.0.1` desde la página pública. El botón hace una **navegación de nivel superior**, no un `fetch`, y Local Network Access no debería aplicarse. *A confirmar en la versión de Chromium de las cajas*; si lo bloqueara, se autoriza el origen del POS por política en `chromium-pos.json`.

### 4.6 ¿Quién puede cambiar la wifi?
Es una decisión pendiente (ver §7). Opciones:
- **Libre:** quien está frente a la caja ya tiene acceso físico. Es lo más simple.
- **Con PIN de la tienda:** un PIN local guardado con hash en `/home/pos-red/`. Evita que un cajero conecte la caja al hotspot de su celular.

## 5. Instalación

- Script nuevo en la plantilla: **`bin/fase5-wifi-autoservicio.sh`**, idempotente, con `--ensayo`, `--aplicar` y `--quitar`.
  - Si overlayroot está activo, escribe en el disco real con `overlayroot-chroot` y también en vivo.
  - Mueve las conexiones de base a `/usr/lib/NetworkManager/system-connections/`.
  - Crea `/home/pos-red` con permisos 700 para `posred`.
- **Ensayo antes de dejarlo fijo**, siguiendo la regla de la fase 4: cambiar dónde guarda NetworkManager las redes puede dejar la caja sin red al arrancar.
  - Primero en una caja del banco de pruebas.
  - En cada caja del cliente, aplicarlo **con alguien al lado** y reiniciar para validar: cable o wifi de base conectados, VPN arriba y kiosco arriba.
- Después de la fase 4 el sudo pide clave, así que el usuario lo corre con `! ssh <caja> 'sudo bash ~/pos-terminal/bin/fase5-wifi-autoservicio.sh --aplicar'` y yo reviso la salida.
- **Orden por caja:**
  1. Fix de DNS, si falta. caja1-samuel lo tiene pendiente.
  2. fase 5.
  3. Reinicio.
  4. Prueba real: el hotspot del celular con un nombre nuevo, conectado desde la pantalla. Reiniciar y comprobar que sigue.
- caja2-samuel sigue **sin fase 4**: conviene instalar la fase 5 **antes** de cerrarla.

## 6. Esfuerzo estimado
| Pieza | Tiempo |
|---|---|
| Intermediario `pos-red.py` + unidad + regla polkit | 4 h |
| Página de wifi con teclado propio | 4 h |
| Botón en `esperando.html` + botón en el POS (marca en `localStorage`) | 1-2 h |
| `fase5-wifi-autoservicio.sh` + `fase4-verificar.sh` ampliado | 2 h |
| Ensayo en banco: overlayroot, reinicios, clave mala, red sin internet, sin adaptador | 3 h |
| Despliegue por terminal, con el usuario | ~30 min c/u |

**Total: unos 2 días** de desarrollo y pruebas, más el despliegue.

## 7. Decisiones pendientes
1. **¿Quién puede cambiar la wifi?** Libre o con PIN de la tienda.
2. **¿Botón en el POS** además de la página de espera, o solo la página de espera?
3. **¿Se permite «olvidar» redes** desde la pantalla? Siempre solo las agregadas por el cliente.
4. **¿En qué terminales?** caja1-samuel, caja2-samuel y caja1-lafe, y las que se monten de aquí en adelante (la fase 5 entra en la plantilla).

## 8. Por confirmar en las cajas (solo lectura, antes de programar)
No pude leerlo yo en esta sesión. Lo puede correr el usuario:
```
! for h in caja1-samuel caja2-samuel caja1-lafe-vpn; do echo "== $h"; ssh $h 'nmcli -t -f DEVICE,TYPE,STATE dev; ls /etc/NetworkManager/system-connections/; chromium --version; dpkg -l polkitd network-manager | grep ^ii'; done
```
- Wifi por caja (según `CLAUDE.md` y la memoria): caja1-samuel Intel AX210 interna (`wlp2s0`), caja2-samuel interna ("Samuel 2.4G"), caja1-lafe Realtek USB `0bda:c820` con driver DKMS. Confirmar que siguen así.
- Versiones de NetworkManager, polkit y Chromium, para el `path` del keyfile, las reglas JS de polkit y Local Network Access.
- Qué conexiones de base tiene cada caja, para moverlas a `/usr/lib/...`.
