# Wifi en autoservicio: flujo y plan de implementación

> 2026-10-03. Complementa [`analisis-wifi-autoservicio.md`](analisis-wifi-autoservicio.md) (opción A: intermediario local `pos-red` + página local en `http://127.0.0.1:8090`). Las secciones citadas (§2.3, §4.3…) son de ese análisis.

## 0. Decisiones (confirmadas el 2026-10-03)

| # | Decisión | Elegido |
|---|---|---|
| 1 | ¿Quién puede cambiar la wifi? | **PIN de la tienda** (4–6 dígitos). 3 intentos fallidos = espera de 1 minuto |
| 2 | ¿Teclado en pantalla? | **Automático + ajuste por caja.** Se muestra si la pantalla es táctil (`any-pointer: coarse`, la misma regla del POS); con teclado físico se escribe directo. Al instalar se puede forzar con `--teclado pantalla|fisico` |
| 3 | ¿Olvidar redes? | **Sí, solo las de la tienda.** Las de base (cable y wifi de instalación) nunca |
| 4 | ¿Qué terminales? | **Solo caja1-lafe** por ahora, más la plantilla para las nuevas |
| — | ¿El POS? | **No se toca.** Todo vive en la terminal: página de espera, servicio y página de wifi. Sin botón en el POS |

**caja1-lafe** es el caso típico:
- opera **solo por wifi** (su cable pasó a pc-lafe) con el adaptador USB Realtek y el driver `8821cu`;
- **no es táctil**: mouse y teclado físico, así que el teclado en pantalla queda oculto;
- tiene la fase 4 completa y el fix de DNS aplicado desde el 2026-09-20.

## 1. Flujo del cajero

### 1.1 La caja arranca sin red (caso principal)

```
 Encender la caja
        │
        ▼
 pos-kiosk-start: ¿responde /healthz del POS?
        │ no                                      │ sí
        ▼                                         ▼
 Página de espera (esperando.html)          Punto de venta (normal)
 «Conectando con el punto de venta…»
        │ reintenta cada 3 s
        │ a los ~15 s sin respuesta pregunta a pos-red /api/estado
        ▼
 ┌───────────────────────────┬──────────────────────────────┬─────────────────────────────┐
 │ la caja no tiene wifi     │ sin internet                 │ hay internet pero el POS    │
 │ (solo cable)              │                              │ no responde                 │
 ├───────────────────────────┼──────────────────────────────┼─────────────────────────────┤
 │ «Sin red. Revise que el   │ Botón grande                 │ «La red de esta caja        │
 │ cable esté conectado.»    │ [ Configurar wifi ]          │ funciona; el servidor no    │
 │ (sigue reintentando)      │                              │ responde. Llame a soporte.» │
 │                           │                              │ (enlace chico a la wifi)    │
 └───────────────────────────┴──────────────┬───────────────┴─────────────────────────────┘
                                            ▼
                               ¿PIN activado? ── sí ──► PIN (teclado físico o numérico en pantalla) ──► incorrecto ×3 → espera 1 min
                                            │ no / PIN correcto
                                            ▼
                               Página de wifi (http://127.0.0.1:8090)
```

### 1.2 Página de wifi

```
 ┌─────────────────────────────────────────────────────────┐
 │ Red de esta caja                                        │
 │ Conectado a: —   Internet ✘   Soporte remoto ✘         │  ← /api/estado (cada 5 s)
 ├─────────────────────────────────────────────────────────┤
 │ Redes disponibles                    [ Buscar de nuevo ]│  ← /api/redes
 │  ▂▄▆█ 🔒 Farmacia la Fe ALPHANET                        │
 │  ▂▄▆_ 🔒 Samuel 2.4G                                    │
 │  ▂▄__    Invitados (abierta)                            │
 ├─────────────────────────────────────────────────────────┤
 │ Redes guardadas por la tienda          [ Olvidar ]      │  ← solo las de /home/pos-red
 ├─────────────────────────────────────────────────────────┤
 │                       [ Volver al punto de venta ]      │
 └─────────────────────────────────────────────────────────┘
```

1. El cajero toca una red.
   - **Abierta:** se conecta directo.
   - **Con candado:** aparece el campo de clave con «Mostrar clave» y [ Conectar ]. Se escribe con el teclado físico o, si la caja es táctil, con el teclado en pantalla (§1.3).
2. Pantalla «Conectando a *Samuel 2.4G*…», hasta 30 s. Por dentro (§4.3 del análisis):
   1. anota la conexión activa;
   2. `nmcli dev wifi connect "<ssid>" password … name "<ssid> (tienda)"`, con prioridad 40 y DNS 1.1.1.1;
   3. espera IP y **salida a internet**.
3. Resultado:

| Resultado | Qué ve el cajero | Qué queda en la caja |
|---|---|---|
| **Conectó y el POS responde** | «Conectado ✔ · Internet ✔ · Soporte remoto ✔». A los 3 s vuelve solo al punto de venta | La red guardada en `/home/pos-red` (sobrevive reinicios); registro en `cambios.log` |
| Conectó pero el POS no responde | «La wifi funciona; el servidor no responde. Llame a soporte» | La red se guarda: la wifi está bien |
| **Clave incorrecta** | «La clave no es correcta» y vuelve al campo de clave | Se borra la conexión nueva y se reactiva la anterior |
| Red sin internet | «Esa red no tiene internet» | Igual: se deshace y se vuelve a la anterior |
| No se encontró la red | «No se encontró la red; acérquese o búsquela de nuevo» | Igual |

4. **Reinicios posteriores:** NetworkManager lee `/home/pos-red` y se conecta solo a la red de la tienda (prioridad 40). Si no está disponible, usa las de base: el cable y la wifi de instalación.
5. **Si el operador no logra configurarla:** no hace falta que haga nada técnico.
   - NetworkManager **sigue intentando** las redes conocidas en segundo plano. La página de espera y la de wifi consultan el estado cada pocos segundos: **cuando vuelve la red de siempre** (por ejemplo, al prender de nuevo el router), muestran «La red volvió» y **regresan solas al punto de venta**.
   - La página de wifi tiene además un botón **[ Reiniciar la caja ]**, sin PIN porque reiniciar no cambia nada. Al arrancar, la caja se conecta sola a la red conocida que esté disponible.

### 1.3 Escribir la clave según la terminal

| Terminal | Cómo escribe el cajero |
|---|---|
| **Teclado físico, sin pantalla táctil** (caja1-lafe) | El campo de clave toma el foco solo. Se escribe con el teclado; **Enter = Conectar** y **Esc = Cancelar**. El PIN también se escribe con los números del teclado. El teclado en pantalla **no aparece** |
| **Pantalla táctil sin teclado** (CX20 de Samuel) | Aparece el teclado en pantalla: alfanumérico para la clave y numérico para el PIN |
| Táctil **y** con teclado | Aparece el teclado en pantalla y además funciona el físico |

La regla automática se puede forzar por caja al instalar (`--teclado pantalla|fisico`) y queda en `/opt/pos/red/config.json`.

### 1.4 Si la wifi se cae con la caja funcionando

La página de espera aparece **al arrancar el kiosco**. Si la red se cae con el POS ya abierto, el POS sigue en pantalla mostrando errores y **la página de wifi no aparece sola**.

- **Primera versión:** la guía del encargado dice «**Si la caja dice que no hay conexión: apáguela y enciéndala**». Al arrancar sin red aparece la página de espera y, a los 15 s, «Configurar wifi».
- **Opcional, decisión pendiente:** un vigía en la terminal (timer de systemd) que, tras **2 minutos sin salida a internet** (no basta con que el POS no responda), reinicia el kiosco para que aparezca la página de espera. Sin internet no se puede vender igual. Con solo el servidor caído no actúa, para no cortar una venta.

### 1.5 Limitación: cambiar de red con la caja funcionando

Como el POS no se toca, **la página de wifi solo se ofrece cuando la caja no llega al POS**. Ese es el caso que se quiere resolver: la red se cayó o cambió la clave. Si la tienda quiere pasar a otra red **mientras la actual funciona**, lo hace soporte por SSH. Un botón dentro del POS queda para el futuro, si se decide tocarlo.

### 1.6 Soporte

- `ssh <caja>` → `cat /home/pos-red/cambios.log`: fecha, red, resultado y red anterior de cada cambio.
- `nmcli -f NAME,UUID,FILENAME,AUTOCONNECT-PRIORITY con`: redes de base (`/usr/lib/...`) y de la tienda (`/home/pos-red/...`).
- Cambiar o quitar el PIN: `sudo pos-red-pin`, desde soporte.

## 2. Flujo de instalación por terminal (fase 5)

```
 0. Prerrequisitos de la caja
    · fix de DNS aplicado (resolv.conf → /run/NetworkManager/resolv.conf)
    · respaldo de /etc/NetworkManager y de la unidad del kiosco
        │
        ▼
 1. bash fase5-wifi-autoservicio.sh --ensayo     (solo muestra lo que hará; no toca nada)
        │
        ▼
 2. bash fase5-wifi-autoservicio.sh --aplicar    (con overlayroot: escribe vía overlayroot-chroot y en vivo)
    · usuario posred + /opt/pos/red (pos-red.py + www/)
    · pos-red.service (127.0.0.1:8090) enabled
    · regla polkit 50-pos-red.rules
    · conexiones de base → /usr/lib/NetworkManager/system-connections/
    · /etc/NetworkManager/conf.d/50-pos-red.conf ([keyfile] path=/home/pos-red/system-connections)
    · /home/pos-red/system-connections root:root 700
    · esperando.html nuevo + PIN inicial (lo escribe el encargado en la pantalla)
        │
        ▼
 3. Reinicio CON ALGUIEN AL LADO → fase4-verificar.sh (ampliado):
    cable/wifi de base conectados · VPN arriba · kiosco arriba · pos-red responde
        │
        ▼
 4. Prueba real: hotspot del celular con un nombre nuevo → conectar desde la pantalla
    → reiniciar → sigue conectada a esa red → olvidarla → vuelve a la de base
        │
        ▼
 5. Registro en el runbook de la caja

 Vuelta atrás: fase5-wifi-autoservicio.sh --quitar
   (restaura las conexiones de base en /etc, quita el conf.d, deshabilita pos-red y vuelve a esperando.html anterior)
```

## 3. Plan de implementación

### Etapa 0 — Prerrequisitos (½ día, casi todo del usuario)
- [x] Decisiones de §0 (confirmadas).
- [ ] Datos de **caja1-lafe** (§9 del análisis, solo lectura, con `!`): versiones de NetworkManager, polkit y Chromium, conexiones actuales y salida real de `nmcli -t dev wifi list` con el driver `8821cu` (para las pruebas del parseo).
- [x] **Equipo de banco: no hay.** El ensayo real se hace en caja1-lafe, con una ventana coordinada con la farmacia (§4).
- [ ] **Recomendado (gratis): VM Debian 13 en la Mac** (UTM), sin wifi. No prueba el escaneo ni la conexión wifi, pero sí **lo más riesgoso**: mover las conexiones a `/usr/lib`, el `path` en `/home`, polkit, el servicio, overlayroot, el reinicio y la red de rescate (§4.2), usando perfiles de cable. Así, en la farmacia solo queda por probar la parte wifi.

### Etapa 1 — Intermediario `pos-red` (5 h) · repo adminTools, `docs/terminal-kiosco-pos/red/`
- `pos-red.py` (Python 3 stdlib):
  - `/api/estado`, `/api/redes`, `/api/conectar`, `/api/olvidar` y `/api/pin`.
  - Validaciones de SSID y clave.
  - Chequeos de `Host` y `Origin`.
  - Protección de las conexiones de base por `FILENAME`.
  - Registro en `cambios.log`.
  - Límite de intentos de PIN.
- Separar la capa de `nmcli` detrás de una interfaz, con un **modo simulado** (`POS_RED_SIMULADO=1`: redes falsas, clave correcta conocida, red sin internet) para desarrollar y probar la página en la Mac sin NetworkManager.
- Pruebas unitarias:
  - parseo de la salida de `nmcli -t`, con muestras reales de las cajas;
  - validaciones;
  - que no deje modificar conexiones de base;
  - `Host`/`Origin` rechazados.
- `pos-red.service` (`User=posred`, `NoNewPrivileges`, `ProtectSystem=strict`, `ReadWritePaths=/home/pos-red`) y `50-pos-red.rules` (polkit).

### Etapa 2 — Página de wifi (4 h) · `red/www/`
- HTML/JS sin dependencias, botones de al menos 44×44 y estados de §1.2.
- **Teclado** (§1.3):
  - con teclado físico: foco automático en el campo, Enter = Conectar, Esc = Cancelar, PIN con los números del teclado;
  - con pantalla táctil (`matchMedia('(any-pointer: coarse)')` o `teclado: "pantalla"` en la configuración): teclado en pantalla propio, alfanumérico y numérico para el PIN.
- Probar en el navegador de la Mac contra el modo simulado: todos los resultados de la tabla de §1.2, **con teclado físico** (el caso de caja1-lafe) y simulando pantalla táctil.

### Etapa 3 — Integración (2 h)
- `www/esperando.html`: a los 15 s consulta `/api/estado` y muestra el caso que corresponde (§1.1).
- Si `pos-red` no está instalado (cajas sin fase 5), la página sigue como hoy.

### Etapa 4 — Instalador (2 h) · `bin/fase5-wifi-autoservicio.sh`
- `--ensayo`, `--aplicar` y `--quitar`, idempotente. Con overlayroot, vía `overlayroot-chroot` y en vivo.
- `--teclado auto|pantalla|fisico` (por defecto `auto`) y el **PIN inicial**, que el encargado escribe al instalar; se guarda con hash.
- **Lección de la fase 4:** verificar cada paso antes del siguiente; nada de cadenas largas con `&&` bajo `set -e` que puedan dejar la caja sin red.
- `fase4-verificar.sh` ampliado con el bloque de la fase 5.

### Etapa 5 — Ensayo (4 h): VM en la Mac + escenarios wifi en caja1-lafe

Los escenarios 1, 8, 10 y 11 (y la red de rescate de §4.2) se prueban en la **VM**. Los de wifi y teclado se prueban en **caja1-lafe** durante la ventana (§4).

| # | Escenario | Esperado |
|---|---|---|
| 1 | Instalar con overlayroot activo y reiniciar | Conecta con las redes de base; `pos-red` arriba |
| 2 | Red nueva con clave correcta | Conecta, vuelve al POS; tras reiniciar sigue en esa red |
| 3 | **Misma red, clave nueva** (cambiar la clave del hotspot) | Crea «<ssid> (tienda)» y conecta; la de base queda debajo |
| 4 | Clave incorrecta | Mensaje claro y vuelve a la red anterior |
| 5 | Red sin internet | Mensaje y vuelve a la anterior |
| 6 | **Servidor caído** (bloquear el dominio del POS) | Página de espera: «el servidor no responde», no ofrece la wifi en grande; una red buena **no** se deshace |
| 7 | Sin adaptador wifi (desenchufar el USB) | «Sin red, revise el cable» |
| 8 | Intentar olvidar o modificar una red de base por la API | Rechazado |
| 9 | PIN incorrecto 3 veces | Bloqueo de 1 minuto |
| 10 | Llamar a la API con `Host` u `Origin` ajenos | Rechazado |
| 11 | `--quitar` y reiniciar | La caja queda como antes de la fase 5 |
| 12 | **Solo teclado físico** (sin táctil) | No aparece el teclado en pantalla; foco en la clave; Enter conecta y Esc cancela |
| 13 | Adaptador **Realtek USB (`8821cu`)**: escaneo y conexión | Lista de redes completa; conecta desde el escaneo (lección de la ALPHANET) |

### Etapa 6 — Ventana en caja1-lafe (~60–75 min, coordinada con la farmacia)
Ver §4: preparación remota el día anterior, redes de seguridad, guion con el personal y criterios para abortar.

### Etapa 7 — Documentación y entrega (2 h)
- Guía de una página para el encargado, con capturas: «Si la caja no tiene red».
- Plantilla `docs/terminal-kiosco-pos/` actualizada (fase 5 en el README, scripts versionados) y registro por caja.

## 4. Ensayo y despliegue en caja1-lafe sin banco de pruebas

### 4.1 Qué cambia sin banco
caja1-lafe opera **solo por wifi** y con la fase 4 cerrada (consola bloqueada, GRUB con clave). Si la fase 5 la dejara sin red, **no hay SSH ni cable** para arreglarla. Por eso, antes de tocarla, se arman redes de seguridad y la prueba se hace con el personal en la tienda.

### 4.2 Redes de seguridad (se instalan ANTES de la fase 5)

| # | Red de seguridad | Cómo funciona | Quién la usa |
|---|---|---|---|
| 1 | **Rescate automático** (`pos-red-rescate.service` + timer) | Si **5 minutos después de arrancar** no hay ninguna conexión activa, deshace solo la parte de NetworkManager de la fase 5: quita el `conf.d`, devuelve las conexiones de base a `/etc` y reinicia NetworkManager. **Espera 5 minutos** (lección del 2026-09-19: el `net-fallback` actuó antes de tiempo). Mientras exista la marca `/home/pos-red/ensayo-caida` (pasos +15 a +45 del guion, cuando la caja arranca sin red **a propósito**), espera **25 minutos** en vez de 5, para no deshacer la fase 5 en plena prueba. Se retira al cerrar la ventana | Solo, sin intervención |
| 2 | **Red de soporte de emergencia** | Conexión de base nueva con un nombre y una clave conocidos, por ejemplo `LAFE-SOPORTE`, prioridad 5. Cualquier celular de la tienda que cree un hotspot con ese nombre y esa clave le devuelve la red a la caja, y con ella la VPN | El encargado, con su celular |
| 3 | **pc-lafe vende** | Durante la ventana, las ventas siguen en pc-lafe (POS en Chromium + ticketera, por cable) | Personal |
| 4 | **Vuelta atrás manual** | `fase5-wifi-autoservicio.sh --quitar` por SSH, en cuanto vuelva la red | Soporte |
| 5 | **Último recurso** | Teclado + clave de GRUB (la tiene el usuario) para arrancar sin overlayroot y deshacer a mano | El usuario, en la tienda |

### 4.3 Preparación remota (el día anterior, con la caja en la tienda y operando)
1. Leer el estado de la caja (solo lectura): conexiones, versiones y `nmcli dev wifi list` real.
2. Respaldar `/etc/NetworkManager`, la unidad del kiosco y `esperando.html` en `/home` de la caja.
3. Subir los archivos de la fase 5 y correr `--ensayo` (no cambia nada).
4. Instalar **solo** las redes de seguridad 1 y 2 y verificarlas sin reiniciar.
5. Acordar con el encargado: fecha y hora, quién estará, qué celular hará de hotspot y el PIN de la tienda.

### 4.4 Coordinación con la farmacia

**Cuándo:** 60–75 minutos fuera de la hora de más venta: antes de abrir o en el día más tranquilo.

**Quién:** el encargado frente a caja1-lafe, con un celular que pueda compartir datos. Al otro lado, el usuario y soporte por teléfono o WhatsApp.

**Qué hace el personal** (guion para entregarle):
1. Seguir vendiendo en **pc-lafe** mientras dure la prueba.
2. Cuando se lo pidan: **activar el hotspot del celular** con el nombre y la clave que se le indiquen.
3. Cuando se lo pidan: **apagar y encender caja1-lafe** con el botón.
4. Contar qué ve en la pantalla y seguir las indicaciones: «Configurar wifi», PIN, elegir la red, escribir la clave, Enter.
5. Si la caja queda sin red más de 10 minutos: activar el hotspot **`LAFE-SOPORTE`** (red de seguridad 2).

### 4.5 Guion de la ventana

| T | Paso | Cómo se verifica |
|---|---|---|
| 0 | Por SSH: `--aplicar` (redes de seguridad ya puestas) | `fase4-verificar.sh` con el bloque de la fase 5 |
| +5 | **Reinicio 1** (lo pide soporte por SSH) → la caja vuelve con la red de la farmacia | Vuelve por VPN en menos de 2 min; ALPHANET conectada desde `/usr/lib`; kiosco y `pos-red` arriba |
| +15 | **Simular la caída:** se crea la marca `ensayo-caida`, se apaga el autoconectar de ALPHANET (`nmcli con mod … connection.autoconnect no`; como ALPHANET está en `/usr/lib`, NetworkManager guarda ese cambio en `/home/pos-red`) y se reinicia. La caja arranca sin ninguna red conocida | El encargado ve la página de espera y, a los 15 s, «Configurar wifi» |
| +20 | El encargado activa el hotspot `PRUEBA-LAFE`; en la caja: PIN → elegir la red → **clave incorrecta** a propósito | «La clave no es correcta» y vuelve al campo de clave |
| +25 | Clave correcta, escrita con el **teclado físico** + Enter | «Conectado ✔», vuelve al POS; **la VPN vuelve sola** por el hotspot y soporte entra por SSH |
| +30 | **Reinicio 2** con el hotspot encendido | Sigue conectada a `PRUEBA-LAFE` (persistencia en `/home`) |
| +35 | **Misma red, clave nueva:** el encargado cambia la clave del hotspot → reinicio 3 → la página la pide de nuevo | Conecta con la clave nueva |
| +45 | Volver a la normalidad: autoconectar de ALPHANET encendido (o borrar su copia modificada en `/home/pos-red`), «Olvidar» `PRUEBA-LAFE` desde la pantalla, apagar el hotspot, quitar la marca `ensayo-caida` → reinicio 4 | Vuelve con ALPHANET; `cambios.log` registra todo |
| +55 | Cierre: retirar la red de rescate 1 (y la 2, si se decide) y verificar | Caja operando; registro en el runbook de la caja |

**Abortar** (y correr `--quitar`) si:
- en el reinicio 1 la caja no vuelve con ALPHANET en 5 minutos (actúa la red de rescate 1);
- `pos-red` no arranca o la página no aparece;
- la VPN no vuelve con el hotspot;
- la ventana se pasa de 75 minutos.

## 5. Plan de desarrollo con margen alto de éxito

La idea: **que nada se pruebe por primera vez en la farmacia**. Cada etapa tiene una puerta (criterio de salida) y no se pasa a la siguiente sin cumplirla.

### 5.1 Laboratorio: una «caja1-lafe virtual» en la Mac
- **Máquina virtual Debian 13** (UTM o QEMU, gratis) montada **con los mismos scripts de la plantilla**: fases 1 a 4 (cage + Chromium en kiosco, NetworkManager, overlayroot, consola cerrada, `esperando.html`) y la misma `POS_URL`. Es una copia de caja1-lafe salvo el hardware.
- **Wifi simulada de verdad con `mac80211_hwsim`.** Es un módulo del kernel de Linux (incluido en Debian) que crea radios wifi virtuales. En una radio corre `hostapd` como **router**; la caja virtual usa otra como su placa wifi. NetworkManager escanea, se asocia y pide la clave **igual que con una placa real**. Con eso se arman:

| Red simulada | Para qué |
|---|---|
| `ALPHANET-LAB`: WPA/WPA2 mixto TKIP+CCMP, canal 9, con salida a internet | La red de la farmacia, con la misma configuración del router real |
| `PRUEBA-LAFE`: WPA2 | El hotspot del celular del operador |
| `LAFE-SOPORTE` | El hotspot de emergencia |
| `SIN-INTERNET` | Red que asocia pero no tiene salida |
| `ABIERTA` | Red sin clave |

  - «Apagar el router» = detener su `hostapd`.
  - «Cambiar la clave» = reiniciarlo con otra.
  - «Servidor caído» = bloquear el dominio del POS en la VM.
- **Lo que el laboratorio no cubre:** el driver `8821cu` del adaptador Realtek. Se compensa en §5.4.

### 5.2 Etapas y puertas

| Etapa | Qué | Puerta para pasar a la siguiente |
|---|---|---|
| A | `pos-red.py` + modo simulado + **pruebas unitarias** (parseo de `nmcli` con muestras reales, validaciones, protección de redes de base, `Host`/`Origin`, PIN) | 100 % de las pruebas en verde |
| B | Página de wifi en la Mac contra el modo simulado, recorrida en el navegador con **teclado físico** y simulando pantalla táctil | Todos los resultados de §1.2 vistos y capturados |
| C | Montar el laboratorio (§5.1) | La VM arranca como caja1-lafe: kiosco, overlayroot y `ALPHANET-LAB` conectada |
| D | `fase5-wifi-autoservicio.sh` + redes de seguridad (rescate, `LAFE-SOPORTE`) instalados en la VM | `--ensayo`, `--aplicar` y `--quitar` limpios; reinicio con overlayroot OK |
| E | **Batería automática** en la VM: un script que recorre los escenarios de §5.3 controlando los `hostapd`, reiniciando la VM y comprobando el estado por la API y por `nmcli` | **3 corridas seguidas sin fallas** |
| F | **Ensayo general:** el guion completo de §4.5 en la VM, cronometrado, tú haciendo de operador frente a la pantalla y yo de soporte por SSH. Incluye **practicar abortar**: el rescate actuando y `--quitar` | Guion completo dentro de los 75 minutos; vuelta atrás practicada |
| G | Lectura de caja1-lafe (solo lectura) + preparación remota del día anterior (§4.3) | Muestras reales de `nmcli` iguales en formato a las del laboratorio; redes de seguridad instaladas y verificadas |
| H | Ventana en la farmacia (§4.5) | Criterios de §4.5; si alguno falla, se aborta |

### 5.3 Escenarios de la batería (etapa E)

| # | Escenario | Esperado |
|---|---|---|
| 1 | Arranque normal con `ALPHANET-LAB` | POS directo; `pos-red` arriba |
| 2 | **Router apagado** al arrancar | Página de espera → «Configurar wifi» a los 15 s |
| 3 | Router apagado, el operador **no hace nada** y el router vuelve | La caja se reconecta sola y vuelve al POS sin intervención |
| 4 | Router apagado, el operador **pulsa «Reiniciar la caja»** con el router ya prendido | Arranca conectada a `ALPHANET-LAB` |
| 5 | Router apagado → hotspot `PRUEBA-LAFE` con clave correcta (teclado físico + Enter) | Conecta, vuelve al POS; la VPN de la VM vuelve |
| 6 | Igual, con clave incorrecta | Mensaje claro; vuelve al campo de clave |
| 7 | Reinicio con el hotspot encendido | Sigue en `PRUEBA-LAFE` (persistencia en `/home`) |
| 8 | **Misma red, clave nueva** (cambiar la clave de `ALPHANET-LAB`) | La pide de nuevo; crea «ALPHANET-LAB (tienda)» y conecta |
| 9 | `SIN-INTERNET` | «Esa red no tiene internet»; se deshace |
| 10 | **Servidor caído** con la wifi bien | «El servidor no responde»; **no** deshace la red |
| 11 | Olvidar la red de la tienda | Vuelve a la de base |
| 12 | Intentar borrar o modificar una red de base por la API | Rechazado |
| 13 | PIN incorrecto ×3 | Bloqueo de 1 minuto |
| 14 | **Rescate:** romper a propósito el `conf.d` y reiniciar sin redes | A los 5 min el rescate deshace la fase 5 y la caja vuelve a conectar |
| 15 | `LAFE-SOPORTE` como salvavidas | Con todo lo demás apagado, el hotspot de emergencia devuelve la red y la VPN |
| 16 | `--quitar` y reiniciar | La caja queda como antes de la fase 5 |
| 17 | Caída **con el POS abierto** | El POS muestra el error; con «apague y encienda» aparece la página de wifi (o actúa el vigía, si se decide) |

### 5.4 Lo que solo se puede comprobar en la caja real: el driver Realtek
- **Antes de la ventana**, solo lectura: `nmcli -t dev wifi list` y `iw dev` en caja1-lafe, con la farmacia abierta. Confirmar que el escaneo funciona con el driver `8821cu` y que el formato es el que entiende el parseo.
- **Mismo camino que ya funcionó:** la conexión se crea desde el escaneo con `nmcli dev wifi connect`, como se conectó la ALPHANET el 2026-09-19.
- **En la ventana:** si el escaneo o la conexión fallan por el driver, se aborta con las redes de seguridad. La red de la farmacia nunca se toca: solo se apaga su autoconectar, y eso se deshace.

### 5.5 Tiempo
| | |
|---|---|
| A + B (servicio, página, pruebas) | 1,5 días |
| C (laboratorio) | ½–1 día |
| D + E (instalador y batería) | 1 día |
| F (ensayo general) | 2 h (con el usuario) |
| G + H (preparación y ventana) | 30 min + 75 min |
| **Total** | **~4–4,5 días**, más la ventana |

Es más que los ~2,5 días sin laboratorio. A cambio, **cada paso de la ventana ya se habrá hecho varias veces** en una copia de la caja, incluidas la vuelta atrás y la falla del operador.

## 6. Resumen de esfuerzo (sin laboratorio, referencia)

| Etapa | Tiempo |
|---|---|
| 0 Prerrequisitos | ½ día (usuario) |
| 1–5 Desarrollo y ensayo | ~2 días |
| (opcional) VM Debian 13 en la Mac para ensayar lo riesgoso | ½ día |
| 6 Preparación remota + ventana en caja1-lafe | ~30 min el día anterior + 60–75 min de ventana |
| 7 Documentación | 2 h |
| **Total** | **~2,5–3 días** de trabajo, más la ventana coordinada |

Riesgo principal: cambiar dónde guarda NetworkManager las redes podría dejar una caja sin red al arrancar, y **caja1-lafe opera solo por wifi**. Mitigación: ensayo en banco con un adaptador igual, `--ensayo` antes de `--aplicar`, reinicio con alguien en la farmacia y el hotspot a mano, y `--quitar` como vuelta atrás.
