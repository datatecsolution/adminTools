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

### Fase 2 — consolidado persistente y compra sugerida (~4 SP, opcional)

- Guardar el consolidado como documento (`consolidados_pedidos` + `consolidados_pedidos_ordenes`,
  estado Abierto/Comprado/Cerrado), con la misma regla del Swing: **una orden no puede estar en dos
  consolidados abiertos** (equivalente a `verificarExistenciaEnRuta`). Sirve para no comprar dos
  veces lo mismo y para el historial.
- Botón **«Generar compra»**: pre-carga el formulario de compras del POS con los faltantes
  agrupados por proveedor (si el maestro trae proveedor; si no, elección manual). Cierra el
  círculo pedido → compra → recepción → despacho.

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
