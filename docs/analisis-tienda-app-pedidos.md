# Tienda en la app de pedidos — análisis y plan (2026-09-15)

> Estado: **analizado, sin ejecutar**. Historias US-166 y US-167 en
> `historias_urbina_ola1.csv` (Backlog). Se arranca cuando el usuario lo pida.

## Qué se quiere

Una sección en la app de pedidos (`at-ordenes-ventas`) que muestre un
**catálogo de productos previamente seleccionado como una o varias
categorías** — una "tienda" para el cliente final de Samuel (modo
cliente-directo, ver la decisión de canales de venta del 2026-09-09/10):
abre la app, ve las categorías elegidas por el negocio, toca productos y
arma el pedido sin escribir nada en un buscador.

## Qué existe ya y se reutiliza

| Pieza | Dónde | Cómo entra en la tienda |
|---|---|---|
| Bandera de visibilidad por categoría `marcas.mostrar_pos` (V29) + árbol `parent_id` (V38, US-081) | Swing/API/POS | Es el patrón exacto para "categorías seleccionadas": bandera en la categoría, editable desde el POS (`CategoryFormDialog` ya tiene el switch *Visible en POS*) |
| Precios por usuario `usuarios_precios` (US-155) y `PreciosArticuloCRUD.findPrecioUser` | API | La tienda muestra **el precio de la lista del usuario**, igual que la búsqueda actual |
| Stock en la bodega del vendedor `f_existencia_y_ordenes(art, bodega)` (US-130) | API | El mismo número que valida `/orders/save` |
| Fotos: principal `GET /products/{id}/image?size=thumb` (US-079) + galería (US-057/059); `ProductThumb` con memoria de "sin foto" (US-061) | API / app pedidos | Las tarjetas ya tienen miniatura y detalle con galería |
| Carrito y guardado: `items`, `handleSaveProduct`, `AgregarItemModal` (stock descontando el carrito, cambio de precio), idempotencia US-150 | app pedidos | La tienda **no toca el carrito ni el save**: es otra forma de llegar a `AgregarItemModal` |
| Auto-cliente único (Opción A, pedidos#6) | app pedidos | El cliente final abre la app con su cliente ya puesto: la tienda es su pantalla natural |

## Qué NO existe

1. Una bandera "va en la tienda" distinta de "va en el grid del POS".
   `mostrar_pos` no sirve: el grid táctil de caja y la tienda son públicos
   distintos (Samuel puede querer 5 categorías en la tienda y todas en caja).
2. Un endpoint que devuelva **productos por categoría con precio del usuario
   y stock de su bodega**. `GET /products/category/{id}` devuelve el maestro
   crudo (`Select * from articulo where codigo_marca=?`): sin precios de
   lista, con la existencia de la vista (bodega 1).
3. La vista en la app: hoy `PruebaMenu` solo alterna `viewOrders`
   (orden / lista de pedidos).

## Plan — 2 historias, 4 PRs

### US-166 · Backend: categorías de tienda + catálogo con precios del usuario (~3 SP)

**Swing — V53 (`common/`)**: `marcas.mostrar_tienda TINYINT(1) NOT NULL
DEFAULT 0` + `orden_tienda INT NULL` (orden de las pestañas). Default 0 =
tienda vacía hasta que el negocio marque categorías (mismo criterio que V29).
El Swing no la usa; `CategoriaDao.actualizar` hace SET por columna y no la
toca (como pasó con `parent_id`).

**API**:
- `GET /categories/store` → categorías con `mostrar_tienda=1` ordenadas por
  `orden_tienda, descripcion`, con conteo de productos activos. Si la
  marcada es **padre**, sus productos son los de todas sus descendientes
  (reusar `CategoryService.getTree`).
- `GET /products/store?categoryId=&q=&page=0&size=24` → productos activos de
  esa categoría (o de todas las de tienda si no se pasa), **con la misma
  JOIN a `usuarios_precios` y la misma `f_existencia_y_ordenes` en la
  bodega del usuario** que `ArticuloCRUD.findByDescripcionAndUser`
  (`cajaVendedorService.bodegaParaBusqueda`). Paginado obligatorio: la
  función de existencia corre por fila (lección US-061: 536 filas = 830 ms).
  Respuesta = `Page<Product>` con los precios ya aplicados
  (`ArticuloRepository.getByDescriptionAndUser` es la referencia).
- `CategoryRequest/Response` + `CategoryMapper` + entidad `Categoria`:
  `storeVisible`/`storeOrder`, copia del patrón `posVisible`/`mostrarPos`
  (null → false en el service).
- Roles: lectura para cualquier autenticado (los vendedores son el
  consumidor); escritura de la bandera vía el PUT de categorías (ADMIN).

**POS**: en `features/categories/components/CategoryFormDialog.tsx` un
segundo switch *Mostrar en la tienda (app de pedidos)* + campo orden.
`schemas.ts` + `types/api.ts`. Sin pantalla nueva. Manual en línea: una
frase en el tema de categorías (DoD).

### US-167 · App de pedidos: sección Tienda (~4 SP)

- `PruebaMenu`: `viewOrders` (booleano) pasa a `vista: 'orden' | 'pedidos'
  | 'tienda'`; `BottomAppBar` gana un tercer botón (icono `Store`).
- `TiendaView.jsx`: riel horizontal de categorías (chips, como el POS) +
  parrilla de tarjetas (miniatura 96 px, nombre en 2 líneas, precio de la
  lista del usuario, badge de disponible, botón **+**) + búsqueda dentro de
  la tienda (`q`, debounce 300 ms como `AgregarItemModal`). Scroll infinito
  por página (`page`).
- Tocar **+** abre `AgregarItemModal` **precargado** (mismo camino que
  `handleArticuloSelect`: cantidad, stock descontando el carrito, cambio de
  precio). Cero validación duplicada.
- Tocar la foto abre `ProductGallery` (existe).
- `handleSaveProduct` fusiona líneas del mismo producto+precio (hoy cada
  agregado crea una línea nueva; en una tienda eso desconcierta).
- Contador de ítems sobre el botón del carrito; el total ya está en la barra.
- Vista inicial: **tienda si el usuario es cliente-directo (1 cliente) y hay
  categorías de tienda**; si no, `orden` como hoy → **Sharon no cambia**.
- README de la app (esta app no tiene manual en línea).

**Deploy**: V53 por túnel + runner real → API → POS → app de pedidos; primero
en dulce (pruebas), luego Samuel. Sharon no se toca (regla vigente).

## Decisiones pendientes del usuario

| # | Decisión | Propuesta |
|---|---|---|
| 1 | Selección global por categoría ¿o tiendas distintas por cliente? | Global. Por cliente = otra tabla y otra pantalla; solo si Samuel lo pide |
| 2 | Categoría padre marcada ¿incluye hijas? | Sí |
| 3 | Stock en la tienda: tope (modo Sharon) ¿o informativo para el cliente final? | Respetar `facturar_sin_inventario` del usuario (Opción B pendiente) exponiéndola en el login; mientras, tope como hoy |
| 4 | Productos sin foto: ¿tarjeta con monograma o esconderlos? | Monograma. Samuel no ha cargado fotos: esconder = tienda vacía |
| 5 | Nombre de la sección | "Tienda" |

## Referencias de código

- app pedidos: `src/componets/PruebaMenu.jsx` (estado/vistas),
  `AgregarItemModal.jsx` (`handleArticuloSelect`, stock), `ProductThumb.jsx`,
  `ProductGallery.jsx`, `BottomAppBar.jsx`.
- API: `ProductCtl` (`/products/description/{q}`, `/products/category/{id}`),
  `ArticuloCRUD.findByDescripcionAndUser` (JOIN de precios + bodega),
  `ArticuloRepository.getByDescriptionAndUser` (aplica `findPrecioUser`),
  `CategoryCtl`/`CategoryService` (`posVisible`, `getTree`).
- Swing: `V29__marcas_mostrar_pos.sql`, `V38__us081_categorias_jerarquicas.sql`.
