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

## Preguntas para decidir
1. **Modelo exacto** del Landi Android y si trae **Google Play / Chrome** (los
   AOSP puros no sirven para la ruta web).
2. ¿Qué se quiere resolver: **movilidad** (vender en piso/ruta) o **reemplazar
   una caja fija** (ticket integrado, báscula)? Cambia entre A/C y B.
3. ¿Se acepta un **EMM** (Android Enterprise) para kiosco y políticas, o se
   prefiere launcher + permisos manuales?
4. ¿Hay **productos pesados** en esa caja? Si sí, B (o báscula en otra caja).

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
