# Runbook — Terminal POS con Epson TM-T20 IV (impresión de navegador)

Puesta en marcha de una terminal Windows 10 + Chrome que imprime tickets 80mm
desde el POS web. Camino actual: `window.print()` vía driver clásico
(hasta que se implemente US-107, que cambia a WebUSB directo).

Decisión 2026-07-22: la terminal nueva de dulce entra en operación pronto →
**Opción A**: impresión de navegador con driver de fábrica. El driver WinUSB
del spike US-107 se revierte y se vuelve a aplicar recién al implementar la US.

## 1. Driver de la impresora

La impresora debe usar el **driver clásico** (Epson/usbprint). Si la máquina
pasó por el spike de US-107 (Zadig/WinUSB), revertir:

1. Administrador de dispositivos → buscar **TM-T20IV** (en "Dispositivos de
   interfaz universal" o similar tras Zadig)
2. Click derecho → **Desinstalar dispositivo** (marcar "eliminar controlador"
   si aparece la casilla)
3. Desconectar y reconectar el USB → Windows reinstala el driver de fábrica
4. Verificar que reaparece en **Dispositivos e impresoras**

## 2. Impresora predeterminada y formato

1. Configuración → Impresoras → TM-T20 IV → **Establecer como predeterminada**
   (si Windows administra la predeterminada solo, apagar esa opción primero)
2. Preferencias de impresión del driver: papel **80mm (roll paper)**,
   sin reducción/ajuste de escala
3. Imprimir página de prueba del driver para confirmar que el camino clásico
   volvió a funcionar

## 3. Chrome sin diálogo de impresión

`--kiosk-printing` hace que `window.print()` salga directo a la predeterminada
con la **última configuración usada** — por eso el paso 4 importa.

Acceso directo de Chrome para el POS (click derecho → Propiedades → Destino):

    "C:\Program Files\Google\Chrome\Application\chrome.exe" --kiosk-printing --app=<URL-DEL-POS>

(`--app` abre sin barra de navegación; agregar `--kiosk` si se quiere pantalla
completa bloqueada. La terminal debe entrar al POS SIEMPRE por este acceso
directo — Chrome abierto normal vuelve a mostrar el diálogo.)

## 4. Calibrar la primera impresión (una vez)

1. Abrir el POS con Chrome **normal** (sin el flag) y hacer una venta de prueba
2. En el diálogo de impresión: destino TM-T20 IV, papel 80mm, márgenes
   **Ninguno**, escala **100%** (NUNCA "ajustar al área imprimible")
3. Imprimir → esa configuración queda guardada y es la que usará kiosk-printing
4. Verificar en el papel:
   - No se comen caracteres a la izquierda (fix `--tk-inset`/`--tk-width`,
     PR pos#39: contenido de 4 a 75mm del rollo)
   - La columna Total no se corta a la derecha
   - Acentos correctos
5. Si aún come caracteres a la izquierda: en `src/index.css` del POS subir
   `--tk-inset` y bajar `--tk-width` en la misma cantidad (suma ≤ 75mm)

## 5. Cierre

- Reiniciar la terminal y hacer una venta real completa con el acceso directo:
  el ticket debe salir SOLO, sin diálogo, completo de ambos márgenes
- Cajón (cuando llegue el hardware): conectar el RJ11 al puerto **DK** de la
  impresora. Con impresión de navegador el cajón NO se puede abrir por
  software — eso llega con US-107 (`ESC p`)

## Futuro — US-107 (impresión directa WebUSB)

Cuando se implemente, esta terminal migra: Zadig → WinUSB (spike ya validado,
ver backlog US-107), y los pasos 2-4 de este runbook dejan de aplicar
(sin driver, sin flag, sin diálogo, y con corte + cajón por software).
