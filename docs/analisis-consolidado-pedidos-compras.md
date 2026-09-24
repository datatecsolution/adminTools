# Análisis — Consolidado de pedidos para compras (POS)

**Fecha**: 2026-09-20 · **Pedido por**: el usuario, para Samuel, junto con la confirmación de
US-187 · **Referencia en el Swing**: módulo *Rutas de entrega* (`CtlRutaEntrega` /
`CtlRutasEntregas`, tablas `rutas_entregas` + `entregas_facturas`, reporte
`reporte_rutas.jrxml`).

## 1. Qué hace hoy el Swing (lo que se quiere replicar)

1. **Crear ruta**: se elige vendedor, caja y fecha; se van **agregando facturas por número**
   (`buscarPorIdAndVendedor`); el sistema rechaza una factura que ya está en otra ruta
   (`verificarExistenciaEnRuta`) o repetida en la misma; se guarda como `rutas_entregas`
   (`id_vendedor`, `fecha`, `estado` = "Creado") + una fila en `entregas_facturas` por factura.
2. **Imprimir ruta** (`CtlRutasEntregas` → `IMPRIMIR`): recorre las facturas de la ruta, lee sus
   detalles y **acumula por artículo en memoria** (si el artículo ya está, suma cantidad y total);
   el Jasper `reporte_rutas` lista `cantidad · código | artículo · total`, con cabecera de fecha,
   ruta, vendedor, usuario y **número de facturas** incluidas. Es la hoja de carga del camión.

Lo que el cliente pide ahora es lo mismo pero **sobre pedidos** (órdenes de venta de la app) y con
otro fin: **saber qué comprar** para poder despachar lo pedido. Encaja con US-187: si los clientes
piden sin verificar stock, el supermercado necesita ver el agregado de lo pedido contra lo que hay.

## 2. Con qué contamos en el POS/API

| Pieza | Estado | Referencia |
|---|---|---|
| Órdenes (admin) | Listado de todas las órdenes con filtro por vendedor, estado y texto, drawer de acciones (US-170) | `OrdenesPage.tsx`, `GET /orders/admin` (`vendedorId`, `estado`, `q`, paginado) |
| Estados de orden | 1 Activa · 2 Modificada · 3 Imprimida · 4 Enviado · 5 Eliminado (3 = facturada por el POS) | `features/ordenes/api.ts` |
| Tablas | `encabezado_factura_temp` (orden) y `detalle_factura_temp` (líneas: artículo, cantidad, precio) | entidades `Orden`, `DetalleOrden` |
| Disponible por artículo | `f_existencia_y_ordenes(art, bodega)` = saldo − órdenes pendientes; `existencia_articulo_bodega` (saldo materializado US-131/132) | `OrderService`, `/inventory/low-stock` |
| Compras | Módulo de compras a proveedor (`POST /purchases`, `encabezado_factura_compra`/`detalle_factura_compra`), proveedores en `proveedor` | `PurchaseCtl`, `PurchaseService` |
| Reportes imprimibles | Infraestructura de PDF en el navegador (`sarPdf.ts`, tickets) y `window.print` con hoja carta | `features/reports`, `features/printing` |
| Sugerencia de compra existente | `GET /reports/purchase-suggestions` (rotación/horizonte) — otra lógica: proyecta demanda histórica, no pedidos reales | `ReportsCtl` |

## 3. Propuesta: "Consolidado de pedidos" (POS, admin/inventario)

### Fase 1 — consolidado al vuelo + reporte (~5 SP)

1. **API** `POST /orders/consolidado` con `{ orderIds: [...] }` (o `GET` con filtros
   `desde/hasta/vendedorId/estado`) → agrega `detalle_factura_temp` de esas órdenes:

   | campo | origen |
   |---|---|
   | producto (código, nombre, categoría) | maestro |
   | **pedido** = Σ cantidad | líneas de las órdenes |
   | **existencia** (bodega principal) | `existencia_articulo_bodega` |
   | **faltante** = max(0, pedido − existencia) | calculado |
   | costo unitario (precio tipo 4) y **costo del faltante** | `precios_articulos` (solo ADMIN/INVENTORY, US-180) |
   | nº de órdenes en las que aparece | conteo |

   Cabecera: rango de fechas, vendedores, **nº de órdenes** y **nº de clientes**, totales.
   Solo órdenes en estado 1/2 (pendientes) por defecto; excluye 5.
2. **POS**: en Órdenes, **casillas de selección** (con "seleccionar todas las pendientes del
   filtro") y botón **«Consolidar»** → pantalla/drawer con la tabla anterior, ordenable por
   faltante, filtro "solo con faltante", **imprimir (PDF carta)** y **exportar CSV** para
   mandar al proveedor. Vista previa por orden (qué cliente pidió qué) al expandir una fila.
3. Sin migración. Sin persistir nada: es un reporte.

### Fase 2 — consolidado persistente y compra (US-190, ~4–5 SP)

**Estado 2026-09-20**: la fase 1 (US-188) está en producción en Samuel. Es **solo un reporte**: no
guarda nada, no marca las órdenes, y una misma orden puede entrar en tantos consolidados como se
quiera. La fase 2 añade lo que el Swing sí tiene en Rutas de entrega (la ruta se guarda y una
factura no puede estar en dos rutas) y el enlace con la compra.

#### 2a. Consolidado persistente (~2,5 SP)

- **Migración V54 (aditiva)**: `consolidados_pedidos` (`id`, `fecha`, `usuario`, `codigo_bodega`,
  `estado` = Abierto / Comprado / Cerrado, `observacion`, `numero_compra` nullable) y
  `consolidados_pedidos_ordenes` (`id_consolidado`, `numero_factura` de la orden). Equivalentes a
  `rutas_entregas` + `entregas_facturas`.
- **API**: `POST /orders/consolidados` (crea desde las órdenes seleccionadas y devuelve el reporte),
  `GET /orders/consolidados` (lista con filtros fecha/estado/usuario), `GET /orders/consolidados/{id}`
  (el mismo reporte de la fase 1 recalculado contra la existencia actual), `PATCH
  /orders/consolidados/{id}/estado`. `POST` rechaza con 422 y el número del consolidado si alguna
  orden ya está en uno **Abierto** (regla `verificarExistenciaEnRuta` del Swing); al pasar a
  Comprado/Cerrado la orden se libera.
- **POS**: en Órdenes, «Consolidar N órdenes» pasa a **crear** el consolidado y abrir su reporte;
  pantalla nueva **Consolidados** (fecha, estado, nº órdenes, faltante, usuario; reimprimir PDF/CSV,
  cambiar estado); en la fila de cada orden, chip «en consolidado #N».

#### 2b. «Generar compra» (~1,5 SP)

- Botón en el consolidado que **precarga el formulario de Compras** del POS (`POST /purchases`, ya
  existe) con los productos con faltante y sus cantidades; el usuario elige proveedor y confirma. El
  consolidado pasa a *Comprado* y guarda `numero_compra`.
- Si el maestro trae proveedor por producto, se agrupa (una compra por proveedor); si no, una sola
  compra con proveedor a elegir.

#### Decisiones de la fase 2 (tomadas por el usuario el 2026-09-23)

| # | Decisión | Resultado |
|---|---|---|
| 1 | ¿Bloquear la orden repetida solo mientras el consolidado esté *Abierto*, o siempre? | **Solo *Abierto***: al pasar a Comprado/Cerrado la orden se libera (422 con el nº del consolidado si se repite). |
| 2 | ¿Las órdenes cambian de estado al consolidar o cerrar? | **No**: el consolidado guarda la relación; la orden sigue su ciclo de venta (chip «consol. #N» en Órdenes). |
| 3 | ¿«Generar compra» entra en esta fase? | **Sí**: precarga Compras y al guardar el consolidado queda *Comprado* con su nº de compra. Una sola compra (el maestro no tiene proveedor por producto). |
| 4 | ¿Qué cantidad precarga «Generar compra»? | **Lo pedido completo, de todos los productos** (no descuenta la existencia). Existencia y faltante quedan en pantalla solo como referencia; el filtro «Solo productos con faltante» arranca desmarcado. |

#### Líneas congeladas (V55, 2026-09-23)

La V54 guardaba solo los números de orden y el reporte se recalculaba desde
`encabezado/detalle_factura_temp`. Pero el Swing **borra físicamente** la orden al facturarla
(`CtlFacturarFrame` → `eliminarOrden`) y al eliminarla de la lista: un consolidado quedaba vacío en
cuanto sus órdenes se facturaban, y con la decisión 4 la compra se arma con lo pedido.

- **V55** `consolidados_pedidos_lineas`: una fila por línea de orden (artículo, cantidad, precio,
  total, cliente, fecha de la orden) congelada al consolidar; rellena los consolidados existentes.
  La V54 no se tocó: ya está aplicada en producción de dulce (2026-09-23).
- **API**: `OrderConsolidadoService` arma el reporte sobre una *fuente* intercambiable — órdenes
  vivas (fase 1, `POST /orders/consolidado`) o líneas guardadas (`GET /orders/consolidados/{id}`);
  existencia, faltante y costo se calculan al consultar. Un consolidado sin líneas guardadas cae al
  cálculo sobre las órdenes vivas.
- **Verificado en local**: con el detalle de una orden borrado (simulando la facturación en el
  Swing), el consolidado conserva sus productos; el reporte al vuelo de la fase 1 ya los perdía.
- **Orden de despliegue**: la V55 debe aplicarse **antes** de subir la API (al crear, la API escribe
  en la tabla nueva).

## 4. Decisiones pendientes del usuario

| # | Decisión | Propuesta |
|---|---|---|
| 1 | ¿Contra qué existencia se calcula el faltante? | Bodega principal de la instalación (Samuel: bodega 1); selector de bodega si hay varias. |
| 2 | ¿Fase 2 desde el inicio o solo el reporte? | Fase 1 primero: es lo que hoy hace el Swing (reporte al vuelo); persistir cuando se valide el uso. |
| 3 | ¿Marcar las órdenes consolidadas (nuevo estado) o dejarlas como están? | Dejarlas: el estado de la orden sigue reflejando su ciclo de venta; el consolidado (fase 2) guarda la relación. |
| 4 | ¿Mostrar precio de venta y total además del costo? | Sí, como el Swing (`total`): útil para dimensionar la ruta/venta; el costo solo a admin/inventario. |

## 5. Relación con otras historias

- **US-187** (pedidos sin verificar stock): habilitante — sin ella no hay pedidos "por encima" del stock que consolidar.
- **US-113/116** (stock reservado): las órdenes ya descuentan del disponible; el consolidado muestra el faltante real.
- **US-170** (órdenes admin): la selección múltiple se monta sobre esa pantalla.
- `GET /reports/purchase-suggestions`: complementario (demanda proyectada vs pedidos reales); podrían converger en una sola pantalla de "qué comprar".
