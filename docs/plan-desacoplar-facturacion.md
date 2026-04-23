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

### Fase 1 - Extraer FacturacionService (prioridad alta)

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

### Fase 2 - Extraer CierreCajaService

**Objetivo:** Separar la lógica de cierre de caja en su propio servicio.

**Archivo nuevo:** `src/main/java/net/datatecsolution/admin_tools/service/CierreCajaService.java`

**Responsabilidades:**
- Ejecutar cierre de caja
- Calcular totales de ventas del turno
- Generar reporte de cierre

**DAOs que se mueven:**
- `CierreCajaDao`
- `FacturaDao` (consultas de cierre)

### Fase 3 - Eliminar duplicación CtlFacturar / CtlFacturarFrame

**Objetivo:** Unificar la lógica duplicada entre ambos controllers.

**Opciones:**
- **Opción A:** Hacer que `CtlFacturar` delegue a `CtlFacturarFrame` (si aún se usa)
- **Opción B:** Eliminar `CtlFacturar` y `ViewFacturar` si ya no se usan (el MDI frame los reemplazó)
- **Opción C:** Ambos controllers usan `FacturacionService`, reduciendo duplicación naturalmente

### Fase 4 - Desacoplar View del Controller

**Objetivo:** Reducir las ~200 llamadas directas `view.get*/set*`.

**Estrategia:**
- Crear DTOs para transferir datos entre view y controller (ej. `FacturaFormData`)
- La view expone un método `getFormData()` y `setFormData(FacturaFormData)` en vez de 50 getters individuales
- Opcionalmente crear interfaces para las vistas (`IViewFacturar`) para facilitar testing

---

## Orden de ejecución recomendado

```
Fase 1 (FacturacionService) → Fase 2 (CierreCajaService) → Fase 3 (Eliminar duplicación) → Fase 4 (Desacoplar views)
```

Cada fase se puede entregar de forma independiente sin romper funcionalidad.

## Rama de trabajo

- **Rama:** `refactor/desacoplar-facturacion`
- **Base:** `master` (commit `a3e68ba`)
