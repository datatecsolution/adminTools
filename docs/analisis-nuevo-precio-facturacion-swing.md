# Análisis: agregar un 5° precio — impacto en la facturación del Swing

> 2026-09-30. Profundiza [`analisis-impacto-nuevo-precio.md`](analisis-impacto-nuevo-precio.md) (análisis general anterior) **solo para el módulo de facturación** del Swing, con el código actual de `master`. Rutas relativas a `src/main/java/net/datatecsolution/admin_tools/` salvo indicación.

## 1. Punto de partida

**Los 4 clientes tienen hoy exactamente los mismos 4 tipos de precio** (consulta de solo lectura, 2026-09-29):

| Código | Descripción | Samuel (493 art.) | Mariposas (252) | La Fe (2.228) | dulce (194) |
|---|---|---|---|---|---|
| 1 | Público General | 493 | 252 | 2.180 | 195 |
| 2 | Clientes Especiales | 129 | 245 | 1 | 6 |
| 3 | Mayoristas | 85 | — | 1 | 3 |
| 4 | Costos | 396 | 5 | 1.960 | 3 |

Las cifras son filas en `precios_articulos` por código. La cobertura de los precios 2 y 3 es muy despareja: un artículo sin fila para un precio simplemente no tiene ese precio. Es la causa de que `keysiquijada` (vendedor con precio Mayoristas en Samuel) no encontrara artículos.

**Escenario analizado:** se agrega un **precio de venta** nuevo, por ejemplo «Ruta» o «Mayoreo+». `precios.codigo_precio` es `AUTO_INCREMENT`, así que tomará el **código 5** (o uno mayor si alguna vez se borraron filas). Cualquier código ≥ 5 se comporta igual en todo lo que sigue.

Se puede crear desde el POS (Precios → tipos de precio) o con un `INSERT`. En el Swing, el editor de artículos (`CtlArticulo.java:435-452`, `TmPrecios`) lo muestra solo, porque recorre todos los tipos.

## 2. Cómo funciona hoy el precio en la facturación del Swing

### 2.1 Al agregar un artículo a la factura
- **Precio inicial = siempre el código 1.** `ArticuloDao.java:58-59` y `:83-84` hacen `INNER JOIN` con `codigo_precio = 1`, así que un artículo **sin precio 1 no aparece** ni por código de barras ni por búsqueda.
- **Lista de precios de la línea** (la que recorren las flechas): `CtlFacturarFrame.java:214` (escáner) y `:1632` (F1 búsqueda) llaman a `FacturacionService.obtenerPreciosSinCosto` (`service/FacturacionService.java:129-130`). Esa función usa `PrecioArticuloDao.getPreciosArticuloSinCosto` (`modelo/dao/PrecioArticuloDao.java:238`):
  ```sql
  WHERE precios_articulos.codigo_articulo = ? and precios_articulos.codigo_precio<4;
  ```
  Tampoco tiene `ORDER BY` (está comentado en `:29`), así que el orden de la lista lo decide MySQL.
- **No hay precio por cliente** (`cliente.tipo_cliente` solo es contado o crédito) **ni por usuario**. `usuarios_precios` **no se lee en la facturación del Swing**: solo lo usan los usuarios «móviles» (la app de pedidos y el POS).
- **La línea guarda solo el valor**, no qué tipo de precio se aplicó (`DetalleFacturaDao.java:79-94`, `FacturaOrdenVentaDao.java:472-501`).

### 2.2 Cambiar el precio dentro de la factura
- **Flechas ←/→** (`CtlFacturarFrame.java:1176-1202`; en órdenes, `CtlOrdenVenta.java:1315-1378`): recorren la lista **por posición**, con `Articulo.netPrecio()` y `lastPrecio()` (`modelo/Articulo.java:53-90`). Usan límites, no módulo, así que no asumen una cantidad fija. Si `config_user_facturacion.pwd_entre_precio` está activo, piden la clave de administrador.
- **Ctrl+↑ «Seleccionar precio»** (`ViewSelectPrecio`; en órdenes, Ctrl+D): el combo carga **todos** los tipos (`CtlSelectPrecio.java:43`, `SELECT * FROM precios`), incluidos el costo (4) y el nuevo (5). Al aplicar, `Articulo.setPrecio()` (`Articulo.java:67-79`) **busca el código en la lista de la línea; si no está, no hace nada y no avisa.**
  - El costo tiene una rama especial que lo trae aparte (`CtlFacturarFrame.java:641-669`, `CtlOrdenVenta.java:1511-1550`).
  - El nuevo precio no tendría esa rama.
- **F8** (precio manual, con `pwd_precio`) y **F7** (descuento) no dependen del tipo de precio. Excepción: en **órdenes**, F7 primero devuelve la línea al precio 1 (`CtlOrdenVenta.java:893, 924, 1026, 1060`).

### 2.3 Órdenes guardadas que se cargan a la factura
`DetalleFacturaOrdenDao.detallesFacturaPendiente` (`:150`) arma la lista con `getPreciosArticulo`, **sin el filtro `<4`**: entran el costo y cualquier código nuevo. Se cargan desde `CtlFacturarFrame.java:788, 1690` y `CtlOrdenVenta.java:450`. Además:
- el `INNER JOIN` con `codigo_precio = 1` (`:46-47`) **descarta las líneas de artículos sin precio 1**;
- la posición del precio vuelve a 0.

### 2.4 Costo (código 4)
- Sigue siendo el costo para la ganancia de los reportes de ventas: `DetalleFacturaDao.java:55-56, 538`, más `f_costo_factura` y `f_costo_dev` en las migraciones, que usan las comisiones.
- **No hay validación de «precio menor al costo»** en la facturación.
- Un 5° precio **no cambia nada de esto**, siempre que el 4 siga siendo el costo.

### 2.5 Impresión
Ningún reporte de factura, ticket, cotización, orden ni cierre usa códigos de precio. La factura imprime el valor guardado en la línea. **Sin impacto.**

## 3. Qué pasaría si mañana se agrega el precio 5 (sin tocar el código)

| # | Situación en la facturación del Swing | Resultado | Gravedad |
|---|---|---|---|
| 1 | Escanear o buscar un artículo y recorrer precios con ←/→ | **El precio 5 nunca aparece** (lo excluye `codigo_precio<4`) | 🔴 El precio no se puede usar al facturar |
| 2 | Ctrl+↑ «Seleccionar precio» → elegir el precio 5 → Aplicar | El combo lo muestra, pero `setPrecio()` no lo encuentra en la línea: **el precio no cambia y no avisa**. Con «Aplicar a toda la factura», tampoco | 🔴 El cajero cree que lo aplicó y cobra otro precio |
| 3 | Cargar una orden guardada (de caja o de la app) y recorrer precios | **Aquí sí aparece el 5, y también el costo (4)**: la lista viene sin filtro. Comportamiento distinto al de un artículo recién escaneado | 🟠 Inconsistente; el acceso al costo **ya existe hoy** |
| 4 | Orden de los precios con ←/→ | Sin `ORDER BY`: en la práctica sale por orden de inserción, sin garantía. Con 5 precios aumenta el riesgo de que el «siguiente» no sea el esperado | 🟡 Ya existe hoy |
| 5 | Artículos sin fila del precio 5 | Simplemente no lo ofrecen. Recién creado, **ningún artículo tiene precio 5** hasta que se cargue (como pasó con Mayoristas en Samuel) | 🟡 Es de datos, no de código |
| 6 | Compras (`DmtFacturaProveedores`, columnas «P/venta 2», «P/venta 3», «P/costo») | Con `size()>=3` asume que existen los códigos 2, 3 y 4 (`:309-365`). **Un artículo con precios {1, 4, 5} tiene 3 precios sin el 2: editar la celda «P/venta 2» da NullPointerException.** El precio 5 tampoco se puede cargar desde Compras | 🔴 Fuera de facturación, pero se rompe al ingresar mercadería. El caso {1, 3, 4} **ya puede fallar hoy** |
| 7 | Costo, ganancia, cierre de caja, comisiones e impresión | Sin cambios (siguen usando el 4 o el valor de la línea) | 🟢 |
| 8 | Precio inicial al facturar | Sigue siendo el 1 | 🟢 |

**Conclusión:** sin cambios de código, el precio 5 **queda inservible en la facturación del Swing**. No se llega a él con las flechas, y desde «Seleccionar precio» falla en silencio. Además puede **romper Compras**.

### 3.1 POS y API (misma base de datos)
Están **preparados**: tratan el 4 como costo con una constante (`RolVista.TIPO_PRECIO_COSTO` en la API; `TIPO_PRECIO_COSTO` en `PreciosDialog.tsx` y `FacturacionDesktop.tsx` del POS) y recorren el resto dinámicamente.
- Las flechas ← → del POS filtran el costo y ordenan por código, así que el 5 entra solo, después del 3.
- `usuarios_precios` permite asignar el precio 5 a un vendedor desde Usuarios.

En un cliente que usa **solo el POS** (Samuel, Mariposas, La Fe), el 5° precio funciona sin tocar código; solo hay que cargar los valores. El problema es exclusivo de las terminales con el **Swing**.

## 4. Cambios necesarios en el Swing

| # | Cambio | Archivo | Esfuerzo |
|---|---|---|---|
| A | Cambiar `codigo_precio<4` por `codigo_precio<>4` y agregar `ORDER BY codigo_precio` | `PrecioArticuloDao.java:238` (y el `ORDER BY` comentado en `:29`) | 15 min |
| B | Al cargar órdenes, usar la misma lista sin costo, para que las flechas no lleguen al costo y el comportamiento sea igual al de un artículo escaneado | `DetalleFacturaOrdenDao.java:150` | 15 min |
| C | «Seleccionar precio» muestra **solo los precios que tiene el artículo** (decisión 2026-10-03, ver §7). `CtlSelectPrecio` recibe la lista en vez de leer `SELECT * FROM precios` | `CtlSelectPrecio.java:38-50`, `Articulo.java:67-79`, `CtlFacturarFrame.java:621-669`, `CtlOrdenVenta.java:1479-1550` | 2 h |
| D | Compras: buscar cada código por su valor (no por `size()>=3`) y crear la fila si falta | `DmtFacturaProveedores` columnas 8-10 (`:305-365`) | 1 h |
| E | (Opcional) Compras: columna para actualizar el precio 5 al ingresar mercadería | `DmtFacturaProveedores`, `FacturaCompraDao.java:133-177` | 2-3 h |
| F | Datos: cargar los valores del precio 5 por artículo (importador del POS o un `INSERT` calculado, por ejemplo % sobre el precio 1) | — | según el cliente |
| G | Pruebas: escanear y buscar → ←/→ llega al 5 y nunca al costo; «Seleccionar precio» con y sin fila; orden cargada; compras con {1, 4, 5} | — | 2 h |

Sin la opción E: **unas 5-6 horas** de desarrollo y pruebas. Los cambios A, B y D **corrigen también comportamientos que ya fallan hoy**: el costo accesible desde órdenes cargadas, el orden no determinístico y la NullPointerException en Compras. Conviene hacerlos aunque el precio 5 no se agregue.

## 5. Despliegue y riesgo
- **Sin migración de esquema:** el precio nuevo es una fila en `precios` más sus valores en `precios_articulos`.
- **Orden recomendado:** primero desplegar el jar corregido del Swing en **todas** las terminales del cliente y después crear el precio 5. Una terminal con el jar viejo no lo vería nunca y fallaría en silencio con «Seleccionar precio».
- Clientes con Swing en uso (Sharon, venecia, Ronal y otros con terminales): coordinar la actualización del jar. Los clientes solo-POS no dependen de esto.
- **Si el 5° precio fuera otro costo** (no de venta), aplica lo del análisis anterior: hay que pasar a un indicador `precios.es_costo` antes, porque el 4 está fijo como costo en DAOs, funciones SQL y reportes.

## 6. Preguntas abiertas
Respondidas el 2026-10-03; ver §7.

## 7. Decisiones (2026-10-03)

| # | Pregunta | Decisión |
|---|---|---|
| 1 | Cliente | **Solo para el Swing.** El POS y la API ya lo soportan (§3.1); no se tocan |
| 2 | Tipo y valores | **Precio de venta.** Los valores se cargan **a mano** (editor de artículos) o con el **importador del POS** (misma base). No hay carga calculada (F queda en manos del cliente) |
| 3 | «Seleccionar precio» | **Solo los precios que tiene el artículo** |
| 4 | Compras | **No** se agrega la columna del precio 5 (E queda fuera). Sí se corrige la NullPointerException (D) |

### 7.1 Cómo queda el cambio C
- **Una línea:** el combo lista los precios de venta de esa línea (los de `obtenerPreciosSinCosto`, ya sin el filtro `<4`) en orden de código.
- **El costo se mantiene como opción** cuando el artículo lo tiene. Hoy vender a costo es una función a propósito: pide la clave de administrador y tiene su rama en `CtlFacturarFrame.java:641-669`. Quitarlo del combo sería un cambio de comportamiento que nadie pidió.
- **«Aplicar a toda la factura»:** el combo muestra la **unión** de los precios de las líneas. Las líneas que no tienen el precio elegido **conservan el suyo**, y al final un aviso dice cuántas quedaron sin cambiar («3 líneas no tienen precio Ruta»). Así ya no hay fallo silencioso.
- Lo mismo en órdenes (`CtlOrdenVenta`, Ctrl+D).

### 7.2 Alcance final
**A + B + C + D + G**, unas **6 horas**. Sin migración de esquema.

Orden de despliegue:
1. Mergear y armar el jar desde `master`.
2. Instalarlo en **todas** las terminales Swing del cliente.
3. Crear el precio 5. Puede ser desde el POS (Precios → tipos) o con un `INSERT` en `precios`.
4. El cliente carga los valores, a mano o con el importador.
5. Si algún vendedor de la app o del POS debe usarlo, se asigna en `usuarios_precios` desde Usuarios.

Falta saber **qué cliente con Swing** lo pide y **el nombre del precio**. Hacen falta para coordinar el paso 2 y crear la fila en el paso 3.
