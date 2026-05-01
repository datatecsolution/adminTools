# Checklist de pruebas pre-merge

Rama: `refactor/desacoplar-facturacion` → destino `master`.

Marcar con `[x]` cada caso a medida que se valida. Si algo falla, anotar el commit/línea sospechoso debajo del caso.

---

## 1. Flujos core de facturación

- [x] Facturar contado (Ctrl+G)
- [x] Facturar crédito (cliente con crédito habilitado)
- [x] Guardar orden pendiente
- [x] Cargar orden pendiente desde panel lateral (clic directo en el botón)
- [x] Cargar orden pendiente desde F3 — verificar que el botón del panel lateral queda marcado
- [x] Cargar orden por F3 cuando la orden NO está en el panel (otro usuario/caja) — la selección previa no debe cambiar
- [x] Eliminar orden pendiente desde panel lateral (estado 5, con confirmación)
- [x] Actualizar orden pendiente (Ctrl+A)
- [x] Cancelar/limpiar factura en curso (Ctrl+N)

## 2. Edición del detalle

- [x] Agregar artículo por F1 (búsqueda)
- [x] Agregar artículo por código de barras
- [x] Agregar artículo escribiendo nombre + Enter
- [x] Editar cantidad con `+` / `-` — la selección debe quedarse en la fila editada (no saltar a la última)
- [x] Editar cantidad con `F7` (entrada manual)
- [x] Aplicar descuento por ítem
- [x] Cambiar precio del artículo (Ctrl+↑ y diálogo)
- [X] Eliminar línea del detalle
- [x] Click en cualquier celda de una fila → toda la fila queda resaltada (selección por fila, no por celda)
- [x] Agregar nueva fila → queda seleccionada por defecto

## 3. Cliente y cabecera

- [x] F3-cliente / búsqueda de cliente
- [x] Escribir nombre cliente y limpiar id (`txtIdcliente` vuelve a `-1`)
- [x] Cambiar entre contado / crédito refleja el `tipoFactura` correcto
- [x] Reset al cargar orden pendiente: tipo, cliente, RTN, totales se restauran correctamente

## 4. Atajos de teclado

- [x] F1 → buscar artículo
- [x] F3 → buscar orden
- [x] F5 → refresca panel de pendientes
- [x] F7 → editar cantidad
- [x] Ctrl+G → guardar
- [x] Ctrl+A → actualizar
- [x] Ctrl+N → nueva factura
- [x] Ctrl+P → rotación manual de caja
- [x] Esc en diálogos de búsqueda → no rompe estado

## 5. Menú contextual y panel lateral

- [x] Click derecho sobre un botón del panel pendientes → menú contextual aparece
- [x] Eliminar desde menú contextual → orden desaparece, estado=5
- [x] Imprimir desde menú contextual → genera ticket

## 6. Trabajo paralelo (no estaba en el plan)

### Cobrador en Cliente
- [x] Crear cliente nuevo y asignar cobrador (combo de empleado)
- [x] Editar cliente existente y cambiar cobrador
- [x] Migración `V9__cliente_id_cobrador.sql` se aplica limpia en BD nueva
- [x] Migración no rompe BD existente (FK nullable)
- [x] Reporte de cuentas por cobrador (`CtlCuentasFacturasReporte`) filtra correctamente

### Estado 5 = Eliminado en órdenes
- [x] Eliminar orden desde `CtlOrdenesBuscar` (F3) con confirmación → estado=5
- [x] `CtlOrdenesLista`: `btnEliminar` deshabilitado hasta seleccionar fila
- [x] `CtlOrdenesLista`: `btnEliminar` (físico) vs `btnCambiarEstado` (lógico) — ambos confirman
- [x] Selección de vendedor en `CtlOrdenesLista` persiste entre aperturas
- [x] Auto-recarga al cambiar combo con radio "Todos" activo

### Gestión de precios por usuario
- [x] Crear usuario y asignar precios permitidos
- [x] Editar usuario existente: lista refleja precios actuales
- [x] Al facturar, usuario solo puede seleccionar precios habilitados

## 7. Cierre y reportes

- [x] Cerrar facturación (Fase 2 — `CierreCajaService`)
- [x] Verificar facturas pendientes por cerrar
- [x] Lista de cierres (paginación, búsqueda por fecha, búsqueda por id)
- [x] Reporte detalle por categoría desde lista de cierres
- [x] Cargar cierres por facturación

## 8. Servicios y migraciones (smoke)

- [ ] App arranca sin errores en BD limpia
- [x] App arranca sin errores en BD con datos
- [x] Login con BCrypt: usuario migrado desde texto plano funciona en primer login y en logins posteriores
- [x] Conexión cifrada AES (`connection.dat`) carga correctamente

## 9. Regresiones que vale la pena revisar

- [x] Que ningún botón / atajo que antes funcionaba quedó muerto tras la migración por clusters
- [x] Que los renderers de tabla (`RenderizadorTablaFactura`) siguen pintando colores correctos en filas seleccionadas/normales
- [x] Reporte de ventas por artículo/vendedor (commit `a3e68ba`) — abre y filtra
- [x] Visor de reportes mejorado — abre, navega, imprime

---

## Notas sueltas

(Espacio para registrar bugs encontrados durante las pruebas, antes de marcar el merge como listo.)
