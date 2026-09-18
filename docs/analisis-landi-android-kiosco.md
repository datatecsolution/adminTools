# ¿Se puede replicar la configuración de caja1/caja2 de Samuel en un Landi con Android?

Análisis 2026-09-18. Respuesta corta: **el POS corre tal cual (es web), pero la
"configuración de caja" NO se replica 1:1**. Lo que hoy hace a las cajas de
Samuel un kiosco cerrado con periféricos por USB depende de tres cosas que
Android no ofrece igual: **Web Serial (báscula)**, **políticas de Chromium por
archivo (sin selector de dispositivos)** y **overlayroot/SSH (endurecimiento y
soporte remoto)**. Y el atractivo de un Landi Android —la **impresora térmica
integrada**— no se alcanza desde el navegador: solo por el SDK del fabricante,
lo que exige una app Android envoltorio.

> Supuesto: un Landi Android tipo smart-POS (A8 / A9 / P960 / M20: Android
> 7–12, pantalla táctil 5,5"–8", impresora térmica 58 mm integrada, USB-C OTG,
> cámara, a veces NFC). Si el modelo concreto difiere, revisar la tabla.

## Qué es hoy "la configuración de Samuel" (Landi CX20, x86, Debian 13)

| Pieza | Cómo está resuelta en caja1/caja2 |
|---|---|
| Kiosco | `cage` + `chromium --kiosk` contra `https://admintools.supermercadosurbina.com`, arranque solo en tty1, consola cerrada |
| Ticket 80 mm | **WebUSB** + ESC/POS (`printing/webusb.ts`, `ticketBytes.ts`, US-107) — Epson TM-T20IV / NXP genérica |
| Etiquetas | **WebUSB** + TSPL (`etiquetaTspl.ts`, US-164) — 4BARCODE 4B-2054TC |
| Báscula | **Web Serial** (FTDI `0403:6001`, Torrey demand `P`, US-146/US-163) |
| Sin selector de dispositivos | `/etc/chromium/policies/managed/chromium-pos.json` (`WebUsbAllowDevicesForUrls`, `SerialAllowUsbDevicesForUrls`) + reglas udev que liberan `usblp` |
| Endurecimiento | overlayroot (`/` read-only, se evapora al reiniciar), `apt-daily` masked, usuario kiosco sin sudo |
| Soporte remoto | SSH `adminpos` por **WireGuard** (10.10.0.4/.5), CDP opcional para tocar `localStorage` |
| Teclado | OSK propio del POS (US-161/165) sobre pantalla táctil |
| Config por terminal | `localStorage` (`admintools-pos.scale`, `.etiquetera`, `.osk`) |

## Pieza por pieza en Android

| Pieza | ¿Replica? | Detalle |
|---|---|---|
| **POS web** (facturación, pedidos, cierre, reportes) | ✅ | Chrome Android. La UI móvil ya existe y está validada en iPhone/iPad (facturación móvil); en 5,5"–8" se usa el modo "Móvil". Sin cambios de código. |
| **Kiosco** | ⚠️ parcial | Android no tiene `cage`. Opciones: (a) *Lock task / dedicated device* de Android Enterprise — exige provisionar el equipo como *device owner* (adb en fábrica o EMM); (b) fijado de pantalla (screen pinning) — se sale con PIN, débil; (c) un launcher de kiosco de terceros. Landi suele traer launcher propio y a veces Android sin Google Play (AOSP) — **verificar si tiene Chrome/Play**; sin Chrome no hay WebUSB. |
| **Ticket por impresora INTEGRADA** | ❌ desde web | La térmica interna no se expone como dispositivo USB al navegador: solo por el SDK Java/AIDL de Landi. Para usarla hace falta una **app Android envoltorio** (WebView/TWA + puente JS → SDK) y en el POS un `TicketTransport` nuevo (`landi-sdk`) al lado de `webusb`/`browser`. Sub-proyecto nuevo (app + firma + distribución). |
| **Ticket por impresora EXTERNA USB** | ✅ | WebUSB sí existe en Chrome Android (requiere OTG/host). El código actual (`webusb.ts` + ESC/POS) sirve sin tocar. Pierde la gracia del equipo (impresora integrada) y ocupa el único USB. |
| **Etiquetas TSPL por USB** | ✅ | Mismo motivo: WebUSB. Compite por el puerto con la ticketera (hub USB OTG alimentado). |
| **Báscula por Web Serial** | ❌ | **Web Serial no existe en Chrome Android.** Alternativa: hablarle al FTDI **por WebUSB** (el chip FT232R se maneja con control transfers para baudrate + bulk IN/OUT) — un `scaleTransport` nuevo (`ftdi-webusb`) espejo del actual, ~3–5 SP, sin dependencias; probado en la comunidad, no en nuestro código. O báscula en otra caja. |
| **Sin selector de dispositivos** | ⚠️ | Las políticas `WebUsbAllowDevicesForUrls` existen para Chrome Android **solo vía configuración administrada (EMM/Android Enterprise)**, no por archivo JSON. Sin EMM: el cajero elige el dispositivo **una vez** por origen y Chrome recuerda el permiso; si el USB se reconecta con otro puerto/ID vuelve a preguntar. Aceptable para operar, no "invisible" como hoy. Verificar en el equipo real. |
| **Endurecimiento** | ⚠️ distinto | No hay overlayroot ni falta: Android ya aísla apps. Lo que hay que controlar es la **actualización automática de Chrome por Play** (un Chrome nuevo puede cambiar WebUSB — ya nos pasó con reconexión de USB) → Play administrado o Chrome congelado. Y bloquear ajustes/notificaciones (lock task). |
| **Soporte remoto** | ⚠️ | Sin SSH. WireGuard sí (app oficial, *always-on VPN*), pero para "entrar" queda `adb` por red (inseguro) o un EMM con control remoto. Todo lo que hoy hacemos por SSH/CDP (leer `localStorage`, reiniciar kiosco, revisar udev) cambia de método. |
| **Teclado** | ✅ | Usar el teclado del sistema (`admintools-pos.osk = never`) o el OSK propio — ambos funcionan; el OSK propio tapa menos en 8" que el de Android. |
| **Lector de barras** | ⚠️ | Hoy es cuña de teclado USB. Los Landi Android traen cámara/escáner: la web puede leer con `BarcodeDetector` (Chrome Android, vía Play Services) → feature nueva pequeña (~2 SP) o escáner USB/Bluetooth HID que ya funciona. |
| **Gaveta** | ⚠️ | Hoy iría por pulso de la ticketera ESC/POS (`ESC p`). Con impresora externa sigue igual; con la integrada, depende del SDK. |
| **Pantalla** | ⚠️ | 5,5"–8" vs 15" táctil: cabe en modo "Móvil", pero la parrilla de productos (US-160), el conteo de inventario y los reportes se usan mejor en 8"+. |

## Tres formas de plantearlo

**A. Landi Android como "caja liviana" con lo que ya existe (0 código)**
Chrome Android + WebUSB → impresora de tickets **externa** ESC/POS + (opcional)
etiquetera TSPL por hub OTG. Sin báscula. Kiosco por lock task o launcher.
Sirve para una caja de mostrador sin productos pesados o para ventas móviles.
Costo: provisionar el equipo (device owner), hub OTG alimentado, permisos USB una
vez. No aprovecha la impresora integrada.

**B. Paridad real con Samuel (proyecto nuevo, ~15–25 SP + app Android)**
1. App envoltorio (Capacitor/TWA + plugin) con el SDK Landi → `TicketTransport`
   `landi-sdk` en el POS (ticket 80/58 mm, gaveta si el modelo la tiene).
2. `scaleTransport` `ftdi-webusb` para la Torrey (~3–5 SP).
3. Provisionamiento Android Enterprise (lock task, Play administrado,
   `WebUsbAllowDevicesForUrls` por managed config, always-on WireGuard).
4. Distribución/actualización de la app (APK firmado o Play privado).
Es otra plataforma que mantener (SDK propietario, firmware Landi, versión
de Android); antes de eso conviene tener el modelo en la mano y probar el SDK
con un "hola mundo" que imprima.

**C. Mixto (recomendado si lo que se busca es movilidad)**
Las cajas fijas siguen en **Landi CX20/Linux** (báscula, etiquetas, kiosco
cerrado, SSH), y el Landi Android se usa para **pedidos / cobro en piso** con la
UI móvil que ya está — sin impresión local (el ticket se imprime en la caja
fija al cerrar la venta, o por la externa USB si hace falta). Cero desarrollo
nuevo; solo provisionar.

## Escenario concreto (usuario, 2026-09-18): sin báscula, solo impresora de tickets

Con ese alcance el problema se reduce a **una sola pregunta: ¿qué impresora?**

| Impresora | Código nuevo | Cómo queda |
|---|---|---|
| **Integrada del Landi** (58 mm) | App Android envoltorio (WebView/TWA + puente JS → SDK Landi) + `TicketTransport` `android-bridge` en el POS + **layout 58 mm** (`ticketBytes.ts` hoy está a 48 columnas / 576 dots = 80 mm; la integrada es 32 col / 384 dots) | ~8–10 SP + mantener un APK. Es la única forma de usar la térmica interna: el navegador no la ve. |
| **Externa USB ESC/POS** (80 mm o 58 mm) por cable OTG | **Ninguno** para 80 mm (WebUSB + `ticketBytes.ts` actuales). Para 58 mm, solo el layout (~2–3 SP) | Funciona hoy. Contras: ocupa el único USB (cargar el equipo = hub OTG alimentado), cable colgando de un equipo pensado para ir en la mano. |
| **Externa Bluetooth** 58/80 mm | `TicketTransport` `webbluetooth` (~3 SP) — **solo si la impresora es BLE** (GATT con característica de escritura, típica en las portátiles chinas); las Bluetooth clásicas (SPP) NO se ven desde Chrome | Sin cables, el equipo sigue siendo móvil. Hay que validar el modelo de impresora con `chrome://bluetooth-internals` antes de comprar. |

Lo que NO cambia con ninguna de las tres: el POS web corre en Chrome Android
sin tocar código; la gaveta (si la hubiera) va por pulso ESC/POS solo en las
externas; el kiosco/políticas siguen siendo tema de provisionamiento (lock
task o launcher), no de código; y sin SSH el soporte remoto cambia de método.

**Recomendación**: antes de decidir, la prueba de 1 hora de abajo con el equipo
en la mano, sobre todo el punto 4 (el APK demo del fabricante): si la
integrada imprime bien con su SDK y el cliente la quiere sí o sí, se
presupuesta la app envoltorio como US propia (app + transporte + 58 mm). Si
acepta una externa, se arranca hoy con USB (80 mm, cero código) o BLE (3 SP).

## Modelo concreto (usuario, 2026-09-18): Landi All-in-One, Android 13, 15,6" FHD, 4/32 GB

Cambia el cuadro a favor: **no es un equipo de mano, es una caja de mostrador
como el CX20 pero con Android**. Consecuencias:

- **Pantalla 15,6" táctil** → misma UI de escritorio/táctil que caja1/caja2
  (parrilla US-160, OSK propio, toma de inventario); no hace falta el modo
  Móvil. Android Chrome renderiza igual que el Chromium de Debian.
- **USB host de sobra** (los all-in-one traen varios USB-A) → la ticketera
  ESC/POS **externa por WebUSB funciona con el código actual**, igual que en
  caja1/caja2 y sin hub OTG. Android no tiene `usblp`, así que tampoco hay
  que "liberar" la impresora con reglas udev; el sistema pregunta una vez
  "¿Permitir que Chrome acceda al dispositivo USB?" (marcar "usar por
  defecto") y Chrome recuerda el permiso por sitio.
- Si el modelo trae **impresora integrada en la base** (algunas variantes
  la tienen), aplica lo del escenario anterior: solo por SDK → app
  envoltorio. Si no la trae, **cero código**.
- **Android 13** → kiosco por *lock task* (device owner) o el launcher de
  Landi; WireGuard con la app oficial (always-on). Sin SSH: soporte remoto
  por AnyDesk/TeamViewer Host para Android o `adb` por red.
- El teclado del sistema aparecerá además del OSK propio; se resuelve usando
  el del sistema (`admintools-pos.osk = never`) o poniendo `inputmode="none"`
  en los campos cuando el OSK propio está activo (ajuste chico en el POS).

**Condición que lo decide todo: que traiga Google Play / Chrome.** Muchos
all-in-one Android para POS vienen con AOSP sin GMS; ahí no hay Chrome y sin
Chrome no hay WebUSB (WebView y otros navegadores no lo soportan). Si no trae
GMS, la salida es la app envoltorio (WebView + SDK Landi para la impresora),
es decir, el escenario B reducido.

**Veredicto**: con Chrome disponible y ticketera externa, este equipo se
monta como una caja más de Samuel **sin desarrollo**; solo cambia el
provisionamiento (kiosco Android en vez de cage/overlayroot) y el método de
soporte remoto. Lo primero es encender el equipo y mirar si tiene Play Store.

## Preguntas para decidir
1. ~~Modelo exacto~~ Respondido: All-in-One Android 13, 15,6" FHD. Falta
   confirmar si trae **Google Play / Chrome** (los AOSP puros no sirven para
   la ruta web) y si tiene **impresora integrada**.
2. ¿Qué se quiere resolver: **movilidad** (vender en piso/ruta) o **reemplazar
   una caja fija** (ticket integrado, báscula)? Cambia entre A/C y B.
3. ¿Se acepta un **EMM** (Android Enterprise) para kiosco y políticas, o se
   prefiere launcher + permisos manuales?
4. ~~¿Hay productos pesados en esa caja?~~ Respondido: **no hay báscula**, solo impresora de tickets (ver escenario concreto).

## Cómo verificarlo en 1 hora con el equipo en la mano
1. Abrir `https://admintools.supermercadosurbina.com` en Chrome del Landi:
   login, una venta en modo Móvil, cierre → confirma la ruta web.
2. Conectar por OTG una ticketera ESC/POS → Configuración → Impresora →
   WebUSB → imprimir prueba (confirma WebUSB + host USB).
3. Conectar la báscula FTDI por OTG → `chrome://device-log` para ver si
   enumera; el POS actual **no** la va a abrir (Web Serial) — confirma la
   necesidad del transporte `ftdi-webusb`.
4. Instalar el APK "demo" del SDK Landi que trae el fabricante e imprimir un
   ticket de prueba → confirma que la integrada solo va por SDK.
