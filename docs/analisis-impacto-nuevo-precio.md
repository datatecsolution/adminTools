# Análisis de impacto: agregar un 5° precio al sistema

## Contexto

La aplicación maneja actualmente 4 precios por artículo. Este documento analiza qué tan preparado está el código para soportar un 5° precio en el módulo de facturación y cuál sería el impacto de la modificación.

## Arquitectura actual

### Lo que SÍ es dinámico

- **Schema normalizado** (en `V1__baseline.sql`):
  - `precios` (master con `codigo_precio`, `descripcion`).
  - `precios_articulos` (relación artículo↔precio con valor).
  - `usuarios_precios` (permisos de qué precios puede usar cada vendedor).
  - Agregar un 5° precio = 1 `INSERT` en `precios`.

- **Modelo Java**:
  - `Articulo.preciosVenta` es `List<PrecioArticulo>` (dinámico, sin tope).
  - `TmPrecios` (table model del form de artículo) renderiza el listado completo.
  - `CtlArticulo.actualizar()` carga vía `PrecioArticuloDao.getTipoPrecios()` — itera todo lo que haya en `precios`. **El 5° precio aparecerá automáticamente en el form.**

### Lo que NO es dinámico — códigos hardcodeados

El código asume un convenio rígido:

- `codigo_precio = 1` → "precio público / venta principal".
- `codigo_precio = 4` → "costo / compra" (tratamiento especial).

| Tipo | Archivo | Línea(s) | Qué hardcodea |
|------|---------|----------|---------------|
| Costo (=4) | `DetalleFacturaDao.java` | 55, 492 | JOIN al costo por cada detalle facturado |
| Costo (=4) | `CajaDao.java` | 492 | Costo en cierre de caja |
| Costo (=4) | `CtlFacturarFrame.java` | 637, 658 | Rama especial al elegir costo |
| Costo (=4) | `CtlOrdenVenta.java` | 1511, 1539 | Misma rama en órdenes |
| Costo (<4) | `PrecioArticuloDao.java` | 238 | `getPreciosArticuloSinCosto` filtra `codigo_precio<4` — **bug si agregas precio 5** |
| Precio 1 | `ArticuloDao.java` | 59, 84, 711, 945 | Listas/búsquedas asumen precio público |
| Precio 1 | `InsumoDao.java` | 41 | JOIN con `codigo_precio=1` |
| Precio 1 | `DetalleFacturaOrdenDao.java` | 46 | JOIN con `codigo_precio=1` |
| Precios 2,3,4 | `DmtFacturaProveedores.java` | 6 bloques | Columnas duplicadas en form de compras |
| Precios 2,3,4 | `FacturaCompraDao.java` | 142, 160, 177 | Persistencia desde compras |
| Códigos 1,2,3 | `ReporteArticuloPrecios.jrxml` | 53, 63, 73 | Self-joins fijos en SQL del reporte |

## Veredicto

**¿El sistema está preparado?** Para facturación pura, **mayormente sí** — el flujo lee dinámicamente desde `precios` y deja al usuario elegir vía `ViewSelectPrecio`.

Sin embargo, hay 4 trabajos **obligatorios** y 1 opcional al agregar un 5° precio:

### 🔴 Crítico

1. **`PrecioArticuloDao.getPreciosArticuloSinCosto:238`**: el filtro `codigo_precio<4` excluye al costo (=4) e **incluiría incorrectamente al nuevo precio 5**. Reemplazar por filtro explícito (`<>4` o por descripción).

### 🟡 Necesario

2. **Reporte `ReporteArticuloPrecios.jrxml`**: agregar self-join para `codigo_precio=5` o el reporte seguirá mostrando solo 1, 2, 3.
3. **Migración de datos**: por cada artículo existente, `INSERT` en `precios_articulos` con `codigo_precio=5` (sino el JOIN del form devolverá vacío).
4. **Permisos `usuarios_precios`**: asignar a vendedores el nuevo precio (UI ya existe, solo correr `UPDATE`s).

### 🟢 Opcional

5. **Compras**: si se quiere actualizar el precio 5 al ingresar mercadería, replicar el patrón hardcodeado de 2/3/4 en `DmtFacturaProveedores` (~6 bloques) y `FacturaCompraDao` (~3 bloques).

## Riesgo si el 5° precio fuera otro "costo"

Sería **caro** — los 5 archivos que asumen `=4` como único costo fallarían. Habría que refactorizar a un flag `precios.es_costo` para soportar múltiples precios de costo.

## Recomendación

- **Si el 5° precio es otro precio de venta** (ej. "Especial", "Mayoreo+"): es **viable**, ~1-2 horas distribuido en los 5 puntos. Aprovechar para corregir el bug del `<4` independientemente.
- **Si es otro costo** o tiene semántica especial: **refactorizar antes** a un modelo basado en flags en `precios`, no en códigos fijos. Estimar ~1 día.
