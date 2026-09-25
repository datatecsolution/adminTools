# US-194 — Detector de versión nueva en el POS y la app de pedidos

Fecha: 2026-09-24 · Estado: Terminada (2026-09-25: POS b485098 en Samuel, Mariposas y La Fe; app de pedidos fe9579f en Samuel y Mariposas)

## 1. Problema

Después de un deploy, las pantallas que ya estaban abiertas **siguen corriendo la versión
vieja** hasta que alguien recarga la página. Ni el POS ni la app de pedidos preguntan si hay
una versión nueva, y desde el servidor no hay forma de empujar la recarga a una pestaña abierta.

Casos reales:

- **Mariposas, 2026-09-24**: el fix de la rotación de cajas (pos#104) quedó desplegado, pero la
  PC de `tecnico` seguía con el POS viejo. Ningún equipo de Mariposas está en la VPN (los
  usuarios entran por Cloudflare), así que no hubo forma de recargarla desde soporte.
- **Samuel, cajas kiosco**: tras cada deploy hay que pedir `sudo systemctl restart pos-kiosk`
  o esperar a que alguien recargue.
- **Dulce, 2026-07-25**: una pestaña con un `index.html` viejo pedía un bundle que ya no existía
  (404) → pantalla en blanco. Lo mismo pasa con los *chunks* cargados bajo demanda: una pestaña
  vieja que navega a una ruta nueva pide un archivo que el deploy borró.

## 2. Cómo detectar la versión nueva (sin tocar el build)

Las dos apps publican su código con un nombre que cambia en cada build, referenciado desde un
`index.html` que el servidor ya entrega **sin caché**:

| App | Build | Script principal en `index.html` |
|---|---|---|
| POS (`admintools-pos`) | Vite | `/assets/index-<hash>.js` |
| App de pedidos (`at-ordenes-ventas`) | CRA | `/static/js/main.<hash>.js` |

El detector:

1. Toma el nombre del script principal **cargado ahora** (del propio `document`).
2. Pide `/index.html` con `cache: 'no-store'` y extrae el nombre del script principal.
3. Si son distintos → **hay versión nueva**.

No requiere `version.json`, ni cambios en Docker, nginx, el proxy ni la API. Funciona igual en
todos los clientes (Samuel, La Fe, Mariposas, dulce, venecia) y en los kioscos.

**Cuándo verifica**: cada 5 minutos, al volver a la pestaña (`visibilitychange`) y al recuperar
la conexión (`online`). Errores de red se ignoran (se reintenta en el siguiente ciclo).

**Además**: si falla la carga de un *chunk* bajo demanda (`vite:preloadError` en el POS;
`ChunkLoadError` en la app de pedidos), es señal segura de deploy → mismo tratamiento.

## 3. Cuándo recargar (sin perder trabajo)

Los carritos **no se guardan** en el navegador: recargar con una venta o un pedido a medio armar
los perdería. Por eso la recarga automática solo ocurre en un momento seguro:

| App | Momento seguro para recargar sola |
|---|---|
| POS | Carrito vacío, sin el cobro abierto, sin apertura/cierre de caja en curso y sin formularios con cambios sin guardar |
| App de pedidos | Sin pedido a medio armar (carrito vacío) y sin guardado en curso |

- Si el momento es seguro → **recarga sola** (en un kiosco nadie tiene que tocar nada).
- Si no → muestra un aviso discreto **«Hay una versión nueva — Actualizar»** y recarga en cuanto
  se vuelve seguro (por ejemplo, al terminar la venta). El botón permite actualizar ya.
- Nunca recarga en un bucle: si tras recargar el nombre sigue sin coincidir (caché intermedia),
  espera al siguiente ciclo y solo reintenta una vez.

## 4. Alcance

- **POS**: hook `useDetectorVersion` + aviso; la señal «es seguro» la aportan Facturación
  (carrito vacío, sin cobro abierto) y los diálogos de caja. En móvil y escritorio.
- **App de pedidos**: mismo hook adaptado (CRA) + aviso; «es seguro» = carrito vacío.
- Tests unitarios de la comparación de nombres (Vite y CRA) y de la regla «es seguro».
- **Despliegue**: la primera vez hay que recargar a mano cada pantalla (la versión vieja no
  tiene el detector). Desde ese deploy en adelante, las pantallas se actualizan solas.

Fuera de alcance: forzar una versión mínima desde la API (header `X-Min-Client-Version`), por si
algún día hace falta obligar a actualizar aunque haya trabajo en curso.

## 5. Criterios de aceptación

1. Con la pestaña abierta y el carrito vacío, un deploy nuevo del POS se toma solo en ≤ 5 min
   (o al volver a la pestaña).
2. Con una venta a medio hacer no recarga; muestra el aviso y recarga al terminar la venta.
3. Lo mismo en la app de pedidos con un pedido a medio armar.
4. Una ruta con *chunk* borrado por el deploy no deja la pantalla en blanco: recarga.
5. Sin versión nueva no hay recargas ni avisos; sin conexión no hay errores visibles.
6. Funciona en los kioscos (Chromium `--app`) sin intervención.

## 6. Estimación

3 SP (POS 2 · app de pedidos 1).
