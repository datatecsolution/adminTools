# US-196 — Requisiciones en el POS: traslados entre bodegas y mermas

Fecha: 2026-09-25 · Estado: Terminada (2026-09-25: en Samuel, Mariposas y La Fe — API 27d16f4, POS af7d87e) · Origen: handoff de diseño `design_handoff_requisiciones`
(prototipo `Requisiciones.html` + `README.md`, alta fidelidad)

## 1. Problema

Los traslados de mercadería entre bodegas y las mermas (envío a la bodega **Pérdidas**) solo se
pueden hacer desde el Swing (`CtlRequisicionesLista` / `CtlRequisicion`). El POS no tiene pantalla:
solo usa `POST /requisitions` por dentro, en el cierre de la toma física (faltantes → Pérdidas).

## 2. Qué ya existe (verificado 2026-09-25)

**API** (`RequisitionCtl`):

| Endpoint | Uso |
|---|---|
| `POST /requisitions` (rol INVENTORY) | `{ originWarehouseCode, destinationWarehouseCode, date?, lines: [{ productId, quantity, unitPrice }] }`. El trigger `d_requisicion_b_insert` mueve el kardex de las dos bodegas; la API no toca el kardex. |
| `GET /requisitions?origin&destination&from&to&page&size` | `Page<RequisitionResponse>` |
| `GET /requisitions/{id}` | `RequisitionResponse { id, origin/destination Code+Name, date (LocalDateTime), status, total, user, lines }` |
| `GET /products?warehouse=` | cada producto trae `stock: ProductStock { cantidad, reservado, disponible, costoUnitario, … }` de esa bodega → existencia en origen y costo kardex para el buscador |
| `GET /inventory/valuation?warehouse=` | valoración completa por bodega (la usa la toma física) |
| `GET /warehouses` | bodegas; **Pérdidas** se reconoce por nombre (`DEFAULT_WAREHOUSE_NAME` en `cierre.ts`) |

**POS**: `src/features/inventario/requisitions.ts` solo tiene `create` + `useCreateRequisition`.
Se extiende (list, getById, hooks); no se duplica.

## 3. Brechas de la API

1. **`RequisitionLineResponse` no trae nombre ni código del producto** (`id, productId, quantity,
   unitPrice, total`). El detalle y la impresión los necesitan. **Propuesta**: agregar
   `productName` y `productCode` al DTO (cambio aditivo, sin migración).
2. **Búsqueda por No.**: el `GET` no filtra por id → el POS usa `GET /requisitions/{id}` cuando se
   escribe un número (404 = lista vacía).
3. **Anular**: no existe (en el Swing estaba comentado). Fuera de alcance.

## 4. Alcance (POS)

Idéntico al prototipo; detalle completo en el `README.md` del handoff.

- **Lista `/requisiciones`** + entrada en el sidebar (icono de flechas, junto a Compras):
  - Filtros: No. (solo dígitos), De, Para, Desde/Hasta y Limpiar filtros. Todos van a los query params del GET.
  - Tabla: No., Fecha (hora · usuario debajo), Traslado (Origen → Destino, merma en ámbar), Artículos, Total y Reimprimir.
  - Paginado.
- **Detalle** en un drawer derecho de 540 px:
  - Encabezado: #id, badge Activa y, si el destino es Pérdidas, badge Merma.
  - Tarjeta Origen → Destino, tabla de líneas, total a costo y Reimprimir.
- **Nueva requisición** (página completa, mismo patrón que el formulario de Compras):
  - Origen y destino: el origen excluye Pérdidas; el destino excluye el origen.
  - Chip "Registrar merma (enviar a Pérdidas)".
  - **Cambiar el origen con líneas pide confirmación y vacía el detalle** (paridad con el Swing).
  - Buscador con "Disponible N" (existencia en origen menos lo ya agregado). Deshabilita lo que no tiene existencia. Repetir un artículo suma 1.
  - Líneas: existencia con "Quedan N" / "Faltan N" (en rojo), stepper de cantidad (mínimo 1), costo kardex no editable y total.
  - Si la cantidad supera la existencia, la fila y un banner se ponen en rojo y no se puede guardar.
  - Resumen lateral de 320 px (pasa abajo con menos de 1100 px), con Cancelar / Guardar.
- **Guardar**:
  - `POST /requisitions`, toast e invalidación de las queries de requisiciones e inventario.
  - Vuelve a la lista y abre la **impresión en hoja carta**: No., fecha, De/Para, tabla con bordes, total y firmas Entregó / Recibió.
- **Atajos** (paridad con el Swing, opcionales): F1 enfoca el buscador; Supr quita la línea.
- **Permisos**: la pantalla de alta solo para el rol INVENTORY (el mismo que exige el POST).
- No se portan `ui.jsx`, `tweaks-panel.jsx` ni `data.js`: son solo del prototipo.

## 5. Criterios de aceptación

1. La lista filtra por No., origen, destino y fechas. Los filtros se reflejan en el GET y el paginado funciona.
2. El detalle muestra nombre y código de cada artículo (requiere la brecha 1).
3. No se puede guardar sin destino, sin líneas o con una cantidad mayor que la existencia en el origen.
4. Cambiar el origen con líneas pide confirmación y vacía el detalle.
5. Una merma (destino Pérdidas) se guarda, aparece en ámbar y su impresión sale en carta.
6. Después de guardar, el kardex de las dos bodegas refleja el traslado (lo hace el trigger).
7. A unos 920 px de ancho no se corta nada: selects, montos y tabla. Funciona en tema claro y oscuro.

## 6. Estimación

5 SP: API 1 (DTO de líneas con nombre/código + test) y POS 4 (lista, detalle, alta, impresión).
