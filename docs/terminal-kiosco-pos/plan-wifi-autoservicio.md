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

### 1.3 Escribir la clave según la terminal

| Terminal | Cómo escribe el cajero |
|---|---|
| **Teclado físico, sin pantalla táctil** (caja1-lafe) | El campo de clave toma el foco solo. Se escribe con el teclado; **Enter = Conectar** y **Esc = Cancelar**. El PIN también se escribe con los números del teclado. El teclado en pantalla **no aparece** |
| **Pantalla táctil sin teclado** (CX20 de Samuel) | Aparece el teclado en pantalla: alfanumérico para la clave y numérico para el PIN |
| Táctil **y** con teclado | Aparece el teclado en pantalla y además funciona el físico |

La regla automática se puede forzar por caja al instalar (`--teclado pantalla|fisico`) y queda en `/opt/pos/red/config.json`.

### 1.4 Limitación: cambiar de red con la caja funcionando

Como el POS no se toca, **la página de wifi solo se ofrece cuando la caja no llega al POS**. Ese es el caso que se quiere resolver: la red se cayó o cambió la clave. Si la tienda quiere pasar a otra red **mientras la actual funciona**, lo hace soporte por SSH. Un botón dentro del POS queda para el futuro, si se decide tocarlo.

### 1.5 Soporte

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
- [ ] **Equipo de banco** con Debian 13 mínimo, wifi y overlayroot, para ensayar sin tocar cajas en producción. Lo ideal es **sin pantalla táctil y con un adaptador USB Realtek como el de caja1-lafe**. Opciones: una PC de repuesto, o una VM Debian 13 en la Mac con un adaptador USB wifi conectado a la VM.

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

### Etapa 5 — Ensayo en banco (4 h)

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

### Etapa 6 — Despliegue en caja1-lafe (~45 min, con el usuario)
- La caja tiene fase 4 completa: `sudo` pide clave, así que el usuario corre los pasos con `! ssh caja1-lafe-vpn 'sudo bash …'` y yo reviso cada salida.
- **Opera solo por wifi**: si la fase 5 la dejara sin red, no hay cable de respaldo. Por eso el reinicio de validación se hace **con alguien en la farmacia** y con el hotspot del celular a mano, que es la vuelta atrás de emergencia.
- §2 completo: ensayo → aplicar → reinicio → prueba real con hotspot → olvidar la red de prueba.
- El resto de las cajas (caja2-samuel, caja1-samuel) queda para después, con la misma plantilla.

### Etapa 7 — Documentación y entrega (2 h)
- Guía de una página para el encargado, con capturas: «Si la caja no tiene red».
- Plantilla `docs/terminal-kiosco-pos/` actualizada (fase 5 en el README, scripts versionados) y registro por caja.

## 4. Resumen de esfuerzo

| Etapa | Tiempo |
|---|---|
| 0 Prerrequisitos | ½ día (usuario) |
| 1–5 Desarrollo y ensayo | ~2 días |
| 6 Despliegue en caja1-lafe | ~45 min |
| 7 Documentación | 2 h |
| **Total** | **~2,5 días** de trabajo, más la sesión con la caja |

Riesgo principal: cambiar dónde guarda NetworkManager las redes podría dejar una caja sin red al arrancar, y **caja1-lafe opera solo por wifi**. Mitigación: ensayo en banco con un adaptador igual, `--ensayo` antes de `--aplicar`, reinicio con alguien en la farmacia y el hotspot a mano, y `--quitar` como vuelta atrás.
