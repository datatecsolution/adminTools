# Cierre de Sprint — Desacople de Facturación

Estado de cierre de las primeras tres historias de usuario del sprint de
desacople del módulo de facturación. Cada US se evalúa contra sus
criterios de aceptación originales.

---

## US-001 — Encapsular DAOs en `FacturacionService`

**Tarea:** Crear clase `FacturacionService` que encapsule `FacturaDao`,
`PrecioArticuloDao`, `DetalleFacturaOrdenDao` y `FacturaOrdenVentaDao`.
La clase debe ser instanciada una sola vez y consumida desde
`CtlFacturarFrame`.

### Criterios de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| `FacturacionService` creado en paquete `service` | OK | `src/main/java/net/datatecsolution/admin_tools/service/FacturacionService.java` |
| DAOs movidos al servicio | OK | Los 4 DAOs son campos `private final` del servicio (líneas 12-15) |
| `CtlFacturarFrame` consume servicio sin instanciar DAOs directamente | OK | Instancia única en `CtlFacturarFrame` (línea 66 actual). Tras commit `fb70054` se eliminó el residuo de `FacturaDao` que quedaba como código muerto en el controller. |
| Pruebas manuales OK | OK | Documentadas en commit `b0f4b54`: facturar contado, facturar crédito, guardar orden, cargar orden pendiente, eliminar orden, cambiar precio. |

### Notas

- `ArticuloDao`, `ClienteDao`, `CodBarraDao`, `InsumoDao` y `UsuarioDao`
  permanecen en el controller por diseño (DAOs de búsqueda UI, fuera del
  alcance de la US).
- Cleanup post-cierre: commit `fb70054` removió la declaración e
  instanciación residual de `FacturaDao` en `CtlFacturarFrame` (campo
  muerto sin uso tras la migración).

**Estado:** Cerrada.

---

## US-002 — Migrar `guardar/actualizar/anular` al servicio

**Tarea:** Migrar los métodos `guardarFactura`, `actualizarFactura` y
`anularFactura` desde `CtlFacturarFrame` hacia `FacturacionService`.

### Criterios de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Métodos migrados al servicio | OK por responsabilidad | Ver mapeo abajo |
| `CtlFacturarFrame` solo orquesta | OK | El controller mantiene rotación de cajas, impresión y refresco UI; la persistencia delega al servicio. |
| Validación manual de los 3 flujos | OK | Pruebas documentadas en plan (commit `b0f4b54`). |
| No hay regresión en facturación | OK | Plan reporta pruebas exitosas. |

### Mapeo de responsabilidades

| Operación pedida | Método del servicio | Sitio de consumo |
|---|---|---|
| Guardar factura | `registrarFactura(Factura)` | `CtlFacturarFrame:1881` |
| Actualizar (orden pendiente) | `actualizarFacturaTemporal(Factura)` | `CtlFacturarFrame:1487` |
| Anular (orden pendiente) | `cambiarEstadoOrden(Factura, 5)` | `CtlFacturarFrame:374, 1984` |

### Notas

- Decisión validada con stakeholder: **importa la responsabilidad, no el
  nombre**. Los nombres del servicio (`registrarFactura`,
  `actualizarFacturaTemporal`, `cambiarEstadoOrden`) son más precisos
  semánticamente que los genéricos del requisito.
- Anular/actualizar facturas **definitivas** no estaba en el flujo de
  `CtlFacturarFrame` (vive en `CtlFacturas`, fuera de alcance del
  sprint).

**Estado:** Cerrada.

---

## US-003 — Migrar lógica de cálculo financiero al servicio

**Tarea:** Migrar la lógica de cálculo financiero desde el controller
hacia el servicio. Los cálculos deben ser determinísticos y testeables.

### Criterios de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Métodos `calcularSubtotal`/`calcularImpuesto`/`calcularTotal` en servicio | OK por responsabilidad | El servicio expone `calcularTotalesDetalle(Factura, DetalleFactura)` y `calcularTotales(Factura, List<DetalleFactura>)` que cubren los tres cálculos en una sola pasada (subtotal, impuesto 15%/18%, otros impuestos, total). |
| Resultados idénticos a los actuales | OK | Tras commit `e7fbc78`, la fórmula del descuento porcentual se replica con el mismo orden de operaciones, mismo `scale=0` y mismo `ROUND_HALF_EVEN`. Sin cambios numéricos. |
| Sin lógica financiera en el controller | OK tras cierre | Ver hallazgo abajo |

### Hallazgo y cierre

Durante la revisión se detectó residuo de lógica financiera en
`CtlFacturarFrame.aplicarDescuentoPorcentaje` (líneas 439-460): el
controller calculaba directamente
`cantidad * precioVenta * (porcentaje / 100)` en BigDecimal, duplicado en
las dos ramas (item individual / aplicar a todos).

Resuelto en commit `e7fbc78`:
- Servicio: nuevo método `calcularDescuentoPorcentaje(DetalleFactura,
  double)` con la fórmula determinística.
- Controller: ambas ramas reducidas a invocar el método del servicio y
  asignar el resultado al detalle.

**Estado:** Cerrada.

---

## Resumen del sprint

| US | Estado | Commits relevantes |
|---|---|---|
| US-001 | Cerrada | `b0f4b54` (Fase 1 original), `fb70054` (cleanup residual) |
| US-002 | Cerrada | `b0f4b54` |
| US-003 | Cerrada | `b0f4b54`, `e7fbc78` (cierre de residuo financiero) |

Las tres historias quedan cerradas sin deuda técnica residual contra sus
criterios de aceptación. El refactor general continúa documentado en
`docs/plan-desacoplar-facturacion.md`.
