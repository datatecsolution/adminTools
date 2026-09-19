# Plan: los cajeros no ven ni seleccionan el precio de costo en el POS

Pedido del usuario (2026-09-19). Estado: **plan, sin ejecutar**.

## Cómo lo hace el Swing hoy

- El tipo de precio **4 = "Costos"** es un tipo de precio más en `precios` /
  `precios_articulos` (la vista `v_precios_costos` es `codigo_precio = 4`).
- En facturación, la lista de precios de **cada línea** se carga con
  `PrecioArticuloDao.getPreciosArticuloSinCosto` → `WHERE codigo_precio < 4`:
  el costo **no aparece** en el combo de la línea.
- PERO el diálogo **"Seleccionar precio / aplicar a todo"** (`CtlSelectPrecio`)
  carga `getTipoPrecios()` (todos los tipos, incluido Costos) y
  `CtlFacturarFrame` tiene rama explícita `if (codigoPrecio == 4)` que va a
  buscar el costo y lo aplica a la línea. O sea: **en el Swing el cajero SÍ
  puede vender a costo** eligiéndolo en ese diálogo; solo está escondido del
  combo por línea. No hay regla por `tipo_permiso` en ninguno de los dos.
- Lo que sí está por rol en el Swing es el **menú**: Artículos/Compras/
  Inventario (donde se ve el costo) solo para supervisor (1) y admin (4).

Conclusión: el POS va a quedar **más estricto que el Swing** (que hoy deja
la puerta abierta por el diálogo). Es lo que el usuario pide.

## Dónde ve/selecciona costo un cajero en el POS hoy

| Superficie | Rol que llega | Qué expone |
|---|---|---|
| Facturación → `PreciosDialog` (elegir precio de una línea / aplicar a todo) | cajero, vendedor | `GET /products/{id}/prices` devuelve **todos** los tipos, incluido **Costos (4)** → se puede vender a costo. **Es el hueco principal.** |
| `GET /products/{id}/prices` | cualquier rol autenticado (sin `@PreAuthorize`) | idem, por API directa |
| `GET /price/all`, `GET /prices/all` (catálogo de tipos) | cualquier rol | lista el tipo "Costos" (solo el nombre; sin montos) |
| Búsqueda de productos en facturación (`ProductResponse.stock`) | cajero | trae `stock.costoUnitario` en el JSON aunque la pantalla no lo pinte |
| `GET /inventory/stock`, `GET /inventory/product/{id}/stock` | cualquier rol (sin `@PreAuthorize`) | `costoUnitario` por artículo |
| `POST /invoices` (`precioUnitario` libre por línea) | cajero | la API no valida el precio contra el catálogo: acepta cualquier monto |
| Pantallas admin (Productos, Inventario valorización, Compras) | solo ADMIN / INVENTORY (`App.tsx`, `RequireAdmin`) | ya no llegan los cajeros ✔ |

## Regla propuesta

**Cajero (2) y vendedor (3): no ven ni pueden aplicar el tipo de precio
"Costos" (4) ni el costo unitario.** Supervisor (1, INVENTORY) y admin (4)
siguen viéndolo (gestionan compras, valorización y márgenes).

La regla vive en la **API** (fuente de verdad: vale para POS, app de pedidos
y cualquier cliente), y el POS además la refleja en la UI para no mostrar
nada que luego se rechace.

## Cambios

### API (`admintools-api`) — ~3 SP

1. `ProductPriceCtl.get` / `ProductPriceService.getByProduct`: si el usuario
   no es ADMIN ni INVENTORY, filtrar `priceTypeId == 4` (misma idea que el
   `codigo_precio < 4` del Swing, pero por rol y no por posición: un cliente
   podría tener más tipos después del 4).
2. `PriceCtl.getAll` / `PricesProducCtl.getAll`: ocultar el tipo 4 a esos
   roles (evita que aparezca en selectores).
3. `ProductStock.costoUnitario` → **0 / null para cajero y vendedor** en las
   respuestas de `/products/**`, `/inventory/stock` y
   `/inventory/product/{id}/stock` (un `ProductStock.sinCosto()` en el
   controller según el rol). La pantalla de facturación no lo usa; solo se
   cierra la fuga del JSON.
4. `InvoiceService.createDirect`: rechazar con **422** una línea cuyo
   `priceTypeId` sea 4 (hay que agregar `priceTypeId` opcional a
   `InvoiceLineRequest`; el POS ya lo conoce como `line.priceItemId`) cuando el
   rol no es ADMIN/INVENTORY. No se valida el monto libre (`precioUnitario`)
   porque descuentos y precios manuales son legítimos; solo se cierra la
   puerta del tipo "Costos".
5. Tests: `ProductPriceServiceTest` (filtra 4 por rol), `InvoiceService…`
   (422 con priceTypeId 4 para cajero, OK para admin), controller test de
   `costoUnitario` en 0 para cajero.

### POS (`admintools-pos`) — ~2 SP

1. `PreciosDialog`: filtro defensivo `priceTypeId !== 4` salvo ADMIN/INVENTORY
   (aunque la API ya no lo mande) y mandar `priceTypeId` en cada línea de
   `POST /invoices`.
2. `features/prices` (selector de tipo de precio donde lo use un cajero, p.ej.
   config de vendedor US-155): ocultar "Costos" para roles operativos.
3. Manual: nota en "Facturación → Cambiar el precio de una línea".
4. E2E: como cajero, abrir el diálogo de precios → no aparece "Costos";
   `GET /products/{id}/prices` con token de cajero → sin tipo 4; con admin →
   con tipo 4; `POST /invoices` con `priceTypeId: 4` como cajero → 422.

### Swing — no se toca (fuera de alcance). Queda documentado que su diálogo
de "aplicar precio" sí permite Costos; si el cliente lo quiere cerrar ahí
también, es una mini-US aparte (`getTipoPrecios()` filtrando 4 salvo
supervisor/admin).

## Decisiones que necesito

1. ¿Supervisor (INVENTORY) sigue viendo costo? (propuesta: **sí**).
2. ¿El vendedor de la app de pedidos también queda sin costo? (propuesta:
   **sí**, misma regla que cajero).
3. ¿Se cierra también el Swing (diálogo aplicar precio) o solo el POS?
   (propuesta: **solo POS ahora**; Swing como US aparte si el cliente lo pide).

## Riesgos / notas

- Un cliente con un tipo de precio adicional numerado > 4 no se ve afectado:
  el filtro es por id 4, no por `< 4`.
- Si algún cajero hoy vende "a costo" a propósito (p. ej. venta a empleados),
  tras el cambio necesitará que un admin le cree un tipo de precio propio
  (p. ej. "Empleados") — conviene avisarlo al cliente antes de desplegar.
- Sin migración; deploy API + POS (Samuel primero, luego el resto).
