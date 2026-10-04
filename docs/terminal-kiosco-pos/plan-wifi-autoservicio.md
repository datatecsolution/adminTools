# Wifi en autoservicio: flujo y plan de implementación

> 2026-10-03. Complementa [`analisis-wifi-autoservicio.md`](analisis-wifi-autoservicio.md) (opción A: intermediario local `pos-red` + página local en `http://127.0.0.1:8090`). Las secciones citadas (§2.3, §4.3…) son de ese análisis.

## 0. Decisiones (propuestas, a confirmar)

| # | Decisión | Propuesta | Por qué |
|---|---|---|---|
| 1 | ¿Quién puede cambiar la wifi? | **PIN de la tienda** (4–6 dígitos), configurable, **activado por defecto** | Evita que un cajero conecte la caja al hotspot de su celular. Lo sabe el encargado |
| 2 | ¿Botón en el POS? | **Sí, en una segunda etapa**, solo para **administradores** | Lo urgente es la caja sin red (página de espera). Cambiar de red con la caja funcionando es menos frecuente |
| 3 | ¿Olvidar redes? | **Sí**, solo las agregadas por la tienda | Si no, se acumulan redes viejas con claves vencidas |
| 4 | ¿Qué terminales? | caja2-samuel → caja1-lafe → caja1-samuel, y la plantilla para las nuevas | caja2 aún no tiene fase 4 (más fácil de ensayar); caja1-samuel necesita antes el fix de DNS |

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
                               ¿PIN activado? ── sí ──► teclado numérico ──► PIN incorrecto ×3 → espera 1 min
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
   - **Con candado:** aparece el campo de clave con «Mostrar clave», el **teclado en pantalla** (letras, números, símbolos, mayúsculas) y [ Conectar ].
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

### 1.3 Cambiar de red con la caja funcionando (etapa 2)

1. En el POS, menú **Configuración → Red wifi de esta terminal**. Solo aparece en terminales con la marca `admintools-pos.terminal = {wifi: true}` en `localStorage`, y solo para administradores.
2. Se abre la misma página de wifi, con `?volver=<url del POS>`. Navega dentro de la misma ventana del kiosco.
3. Mismo flujo que en 1.2. «Volver al punto de venta» regresa al POS.

### 1.4 Soporte

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
 5. Marca en localStorage (etapa 2) y registro en el runbook de la caja

 Vuelta atrás: fase5-wifi-autoservicio.sh --quitar
   (restaura las conexiones de base en /etc, quita el conf.d, deshabilita pos-red y vuelve a esperando.html anterior)
```

## 3. Plan de implementación

### Etapa 0 — Prerrequisitos (½ día, casi todo del usuario)
- [ ] Confirmar las 4 decisiones de §0.
- [ ] Datos de las cajas (§9 del análisis, solo lectura, con `!`): versiones de NetworkManager, polkit y Chromium, y conexiones actuales.
- [ ] **Equipo de banco** con Debian 13 mínimo, wifi y overlayroot, para ensayar sin tocar cajas en producción. Opciones: una CX20 o PC de repuesto, o una VM Debian 13 en la Mac con un adaptador USB wifi conectado a la VM.

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
- HTML/JS sin dependencias, botones de al menos 44×44, estados de §1.2 y teclado en pantalla propio (alfanumérico y numérico para el PIN).
- Probar en el navegador de la Mac contra el modo simulado: todos los resultados de la tabla de §1.2.

### Etapa 3 — Integración (2 h)
- `www/esperando.html`: a los 15 s consulta `/api/estado` y muestra el caso que corresponde (§1.1).
- Si `pos-red` no está instalado (cajas sin fase 5), la página sigue como hoy.

### Etapa 4 — Instalador (2 h) · `bin/fase5-wifi-autoservicio.sh`
- `--ensayo`, `--aplicar` y `--quitar`, idempotente. Con overlayroot, vía `overlayroot-chroot` y en vivo.
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

### Etapa 6 — Botón en el POS (1–2 h, etapa 2 de §0) · repo admintools-pos
- Entrada de menú visible con `admintools-pos.terminal.wifi` y rol administrador. Navega a `http://127.0.0.1:8090/?volver=<origin>`.
- Confirmar en una caja que Chromium permite esa navegación (Local Network Access). Si la bloquea, autorizar el origen en `chromium-pos.json`.
- Flujo normal: rama → PR → merge → despliegue del POS.

### Etapa 7 — Despliegue (unos 30–45 min por caja, con el usuario)
1. **caja2-samuel**: aún sin fase 4 y con `sudo -n`. Fase 5 **antes** de cerrar su fase 4.
2. **caja1-lafe**: fase 4 completa; los comandos los corre el usuario con `!`.
3. **caja1-samuel**: primero el fix de DNS pendiente, después la fase 5.

En cada caja: §2 completo (ensayo → aplicar → reinicio con alguien al lado → prueba real con hotspot).

### Etapa 8 — Documentación y entrega (2 h)
- Guía de una página para el encargado, con capturas: «Si la caja no tiene red».
- Plantilla `docs/terminal-kiosco-pos/` actualizada (fase 5 en el README, scripts versionados) y registro por caja.

## 4. Resumen de esfuerzo

| Etapa | Tiempo |
|---|---|
| 0 Prerrequisitos | ½ día (usuario) |
| 1–5 Desarrollo y ensayo | ~2 días |
| 6 Botón en el POS | 1–2 h |
| 7 Despliegue | ~2 h en total (3 cajas) |
| 8 Documentación | 2 h |
| **Total** | **~2,5–3 días** de trabajo, más las sesiones con cada caja |

Riesgo principal: cambiar dónde guarda NetworkManager las redes podría dejar una caja sin red al arrancar. Mitigación: ensayo en banco, `--ensayo` antes de `--aplicar`, reinicio con alguien al lado y `--quitar` como vuelta atrás.
