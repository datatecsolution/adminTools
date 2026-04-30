# Plan de Desacoplamiento - Módulo Facturación

## Estado actual

### Arquitectura
- Patrón actual: **View → Controller → DAO** (sin capa de servicios)
- No existe inyección de dependencias, todos los DAOs se instancian con `new` directamente en los controllers

### Archivos principales

| Archivo | Líneas | Rol |
|---------|--------|-----|
| `CtlFacturarFrame.java` | 2,179 | Controller activo (MDI frame). Instancia ~12 DAOs, ~206 llamadas directas a `view.*` |
| `CtlFacturar.java` | 1,890 | Controller antiguo (dialog). Lógica duplicada de CtlFacturarFrame |
| `ViewFacturarFrame.java` | ~656 | Vista activa. Limpia de lógica de negocio, solo expone getters/setters |
| `ViewFacturar.java` | 656 | Vista antigua (dialog). Misma estructura que ViewFacturarFrame |
| `ViewModuloFacturar.java` | 177 | Marco MDI (`JDesktopPane`). Delega acciones al frame activo |

### DAOs instanciados directamente en CtlFacturarFrame
- `ArticuloDao`, `ClienteDao`, `FacturaDao`, `PrecioArticuloDao`
- `CodBarraDao`, `FacturaOrdenVentaDao`, `DetalleFacturaOrdenDao`
- `InsumoDao`, `UsuarioDao`
- Inline: `CotizacionDao`, `CierreCajaDao` (x2), `CategoriaDao`

### Problemas identificados
1. Controllers de ~2000 líneas con toda la lógica de negocio
2. DAOs acoplados directamente (no hay interfaces ni servicios)
3. Lógica duplicada entre `CtlFacturar` y `CtlFacturarFrame`
4. ~200 llamadas directas `view.getTxt*()` / `view.set*()` por controller
5. Instanciación inline de DAOs dentro de métodos (`new CierreCajaDao()`)

---

## Plan de refactorización

### Fase 1 - Extraer FacturacionService (prioridad alta) ✅ COMPLETADA

**Estado:** Implementada en commit `b0f4b54`. Pruebas manuales OK (facturar contado, facturar crédito, guardar orden, cargar orden pendiente, eliminar orden, cambiar precio).

**Objetivo:** Crear una capa de servicio que encapsule la lógica de negocio de facturación.

**Archivo nuevo:** `src/main/java/net/datatecsolution/admin_tools/service/FacturacionService.java`

**Responsabilidades del servicio:**
- Guardar / actualizar / anular facturas
- Calcular totales, subtotales e impuestos
- Resolver precios de artículos (precio venta, precio costo)
- Verificar existencia de artículos en inventario
- Gestionar detalle de factura (agregar/quitar items)
- Integración con órdenes de venta (`FacturaOrdenVentaDao`, `DetalleFacturaOrdenDao`)

**DAOs que se mueven al servicio:**
- `FacturaDao`
- `PrecioArticuloDao`
- `DetalleFacturaOrdenDao`
- `FacturaOrdenVentaDao`

**DAOs que permanecen en el controller** (son de búsqueda UI):
- `ArticuloDao` (búsqueda de artículos F1)
- `ClienteDao` (búsqueda de clientes F3)
- `CodBarraDao` (lectura código barras)

**Pasos:**
1. Crear clase `FacturacionService` con los DAOs necesarios
2. Mover métodos de lógica de negocio de `CtlFacturarFrame` al servicio
3. Hacer que `CtlFacturarFrame` use `FacturacionService` en vez de los DAOs directos
4. Verificar que la funcionalidad no cambia (pruebas manuales)

### Fase 2 - Extraer CierreCajaService ✅ COMPLETADA

**Estado:** Implementada. Pruebas manuales OK (cerrar facturación desde menú, abrir lista de cierres, reporte detalle por categoría).

**Objetivo:** Separar la lógica de cierre de caja en su propio servicio.

**Archivo nuevo:** `src/main/java/net/datatecsolution/admin_tools/service/CierreCajaService.java`

**Responsabilidades:**
- Registrar cierre de caja (`registrarCierreActual`)
- Verificar facturas pendientes por cerrar (`verificarCierrePendiente` - orquesta múltiples DAOs)
- Búsquedas y paginación de cierres (`todos`, `buscarPorFecha`, `buscarPorId`)
- Cargar cierres por facturación (`cargarCierreFacturas`, `buscarFacturacionPorCajaUsuario`)
- Ventas por categoría para reporte de cierre (`getVentasCategorias`)

**DAOs encapsulados en el servicio:**
- `CierreCajaDao`
- `CierreFacturacionDao`
- `FacturaDao` (consultas de cierre, no se mueve completo)

**Controllers migrados (activos):**
- `CtlFacturarFrame`: `cierreCaja()` y `setCierre()`
- `CtlCierresCajaLista`: todas las consultas de cierre y reporte por categoría
- `CtlMenuPrincipal`: caso `CERRARFACTURACION`

**Controllers migrados (muertos, churn sobre código a borrar en Fase 3):**
- `CtlMenuPrincipalFrame`: caso `CERRARFACTURACION`
- `CtlFacturar`: método `cierreCaja()`

**Refactor adicional:** `FacturaDao.verificarCierre(List<Caja>)` instanciaba DAOs internamente (DAO-DAO problem). La orquestación se movió al servicio; en `FacturaDao` solo permanece el query per-caja `verificarCierre(Caja, CierreFacturacion)`.

### Fase 3 - Eliminar duplicación CtlFacturar / CtlFacturarFrame ✅ COMPLETADA

**Estado:** Implementada. Se eligió Opción B: el usuario confirmó que `CtlFacturar`/`ViewFacturar` y `CtlMenuPrincipalFrame`/`ViewMenuPrincipalFrame` ya no se usan (la facturación va por `Principal:153 → ViewModuloFacturar → CtlFacturarFrame`).

**Archivos eliminados:**
- `CtlFacturar.java` y `ViewFacturar.java`
- `CtlMenuPrincipalFrame.java` y `ViewMenuPrincipalFrame.java`

**Limpieza de referencias activas:**
- `CtlMenuPrincipal:FACTURAR` (rama `permiso==4`, muerta en producción)
- `CtlCotizacionLista:INSERTAR` (no se usa)
- Bloques comentados en `Principal`, `CtlSalidasListas`, `CtlEntradasListas`
- `ViewFacturar.class.getResource(...)` para iconos en `ViewFacturaDevolucion`, `ViewModuloFacturar`, `ViewCxCPagos` redirigidos a la clase propia
- Comentarios obsoletos en `ViewPagoProveedor`, `ViewCobro`, `ViewCobroFactura`

### Fase 3.1 - Borrar CtlFactCredito, ViewFactCredito y CtlModuloFacturar ✅ COMPLETADA

**Estado:** Implementada. Compila limpio.

**Archivos eliminados:**
- `CtlFactCredito.java` y `ViewFactCredito.java` (solo referenciados desde bloques comentados)
- `CtlModuloFacturar.java` (no instanciado; el módulo de facturación abre `ViewModuloFacturar` directamente)

**Limpieza:**
- Bloque comentado y comentarios sueltos en `Principal.java` (rama `permiso==2`)
- Método huérfano `ViewModuloFacturar.conectarContralador(CtlModuloFacturar)` eliminado
- Import `CtlFactCredito` borrado de `Principal.java`

### Fase 4 - Desacoplar View del Controller (en progreso)

**Objetivo:** Reducir las ~200 llamadas directas `view.get*/set*`.

**Estrategia:**
- Crear DTOs para transferir datos entre view y controller (ej. `FacturaFormData`)
- La view expone un método `getFormData()` y `setFormData(FacturaFormData)` en vez de 50 getters individuales
- Opcionalmente crear interfaces para las vistas (`IViewFacturar`) para facilitar testing

**Enfoque:** refactor incremental por clusters, con commit + pruebas manuales por cluster.

#### Clusters completados

| Cluster | Commit | Cambios |
|---------|--------|---------|
| **Cabecera** (`tipoFactura`, `fecha`, radios contado/crédito) | `30165d7` | DTO `FacturaCabeceraData`. View: `get/setCabeceraData()`. Controller: 5 sitios migrados. |
| **Cliente** (`txtIdcliente`, `txtNombrecliente`, `txtRtn`) | `debe1b5` | DTO `FacturaClienteData`. View: `get/setClienteData()`. Controller: 6 sitios. Bonus: fix latente — al cargar orden no restauraba `tipoFactura` ni `myCliente`, lo que rompía facturación a crédito de orden cargada. Resets siempre limpian `rtn`. |
| **Totales** (`txtSubtotal`, `txtImpuesto`, `txtImpuesto18`, `txtTotal`, `txtDescuento`) | `3b64c72` | View: `resetTotales()`. `actualizarTotales(Factura)` ya existía. Controller: 5 setText directos en `setEmptyView` consolidados en una sola llamada. |
| **Búsqueda** (`txtBuscar`) | `3ce5492` | View: `getTextoBusqueda`, `setTextoBusqueda`, `limpiarBusqueda`, `enfocarBusqueda`, `limpiarYEnfocarBusqueda`, `marcarBusquedaNivelFact(boolean)`. Controller: ~17 sitios migrados. Identity check en `keyTyped` queda directo. |
| **Botones / acciones** (`btnGuardar`, `btnActualizar`, `panelAcciones`, `btnsGuardador`) | `1bbf9e4` | View: `setEstadoBotonesNuevo`, `setEstadoBotonesEditandoOrden`, `setModoActualizarFactura`, `ocultarPanelAcciones`, `puedeGuardar`, `puedeActualizar`, `limpiarOrdenesGuardadas`, `getOrdenSeleccionadaPanel`, `buscarOrdenEnPanel(int)`. Controller: ~30 sitios migrados (8 pares setEnabled, 9 deleteAll, 3 getFacturaSeleted, 2 isEnabled en KeyListener, 1 setVisible/setVisible, 1 panelAcciones, 1 buscarFactura). |

#### Sitios directos restantes en `CtlFacturarFrame` (candidatos a próximos clusters)

- **`modeloTabla`** (~50 sitios): `setArticulo`, `agregarDetalle`, `eliminarDetalle`, `getDetalle(idx)`, `getDetalles`, `setDetalles`, `setEmptyDetalles`, `masCantidad`, `restarCantidad`, `buscarCantidadPorArticulo`, `getRowCount`, `getValueAt`, `fireTableDataChanged`. Es operación de dominio sobre el modelo de tabla, no UI puro. Considerar exponer operaciones más semánticas en la view (ej. `view.agregarArticuloAlDetalle(art)`, `view.eliminarFila(idx)`, etc.) o aceptar que `modeloTabla` es parte legítima del API view.
- **`tableDetalle`** (~10 sitios): `getSelectedRow`, `getRowCount`, `changeSelection`, `addColumnSelectionInterval`. Mayormente para selección de filas tras inserción. Encapsular como `view.seleccionarFila(row, col)` o similar.
- **Otros campos sueltos**: `txtFechafactura`, `getRdbtnContado/Credito` (ya cubiertos por `getCabeceraData`, queda solo el listener attach), `getBtnCobrar`, `getBtnCerrar`, `getBtnGuardarCotizacion`, `getBtnBuscar`, `getBtnBuscarCliente` (registro de listeners en construcción).
- **`getMenuContextual()`**: menú contextual de la tabla.

**Orden sugerido:** tableDetalle → modeloTabla (el más grande y delicado, porque toca lógica de negocio sobre el detalle de factura).

---

## Orden de ejecución recomendado

```
Fase 1 (FacturacionService) → Fase 2 (CierreCajaService) → Fase 3 (Eliminar duplicación) → Fase 4 (Desacoplar views)
```

Cada fase se puede entregar de forma independiente sin romper funcionalidad.

## Trabajo paralelo incluido en la rama

Cambios fuera del alcance original del plan que viajan junto con la refactor (commit `88ed263`). Se entregan en la misma fusión a `master`.

### Cobrador en Cliente
- Campo `cobrador` (FK a `empleado`) en `Cliente` con migración `V9__cliente_id_cobrador.sql`.
- DAOs y vistas relacionadas: `ClienteDao`, `EmpleadoDao`, `CtlCliente`, `CtlClienteBuscar`, `CtlClienteLista`, `CtlCuentasFacturas`, `CtlCuentasFacturasReporte`, `CuentaFacturaDao`.

### Estado 5 = Eliminado en órdenes
- Borrado lógico (estado 5) desde `CtlOrdenesBuscar` y desde el panel de órdenes pendientes en `CtlFacturarFrame` (vía `facturacionService.cambiarEstadoOrden`).
- En `CtlOrdenesLista`: separación de borrado físico (`btnEliminar`) y cambio de estado (nuevo `BotonCambiarEstado`), ambos con confirmación.
- Persistencia de la selección de vendedor entre aperturas y auto-recarga al cambiar combo con radio "Todos" activo.
- `btnEliminar` deshabilitado hasta seleccionar fila (`ListSelectionListener`).

### Gestión de precios por usuario
- Nuevo `UsuarioPrecioDao` y ajustes en `Usuario`, `ViewCrearUsuario`, `CtlUsuario` para asignar precios permitidos por usuario.

### Documentación
- `docs/analisis-impacto-nuevo-precio.md`: análisis de impacto de agregar un quinto precio en el módulo de facturación.

---

## Rama de trabajo

- **Rama:** `refactor/desacoplar-facturacion`
- **Base:** `master` (commit `a3e68ba`)
