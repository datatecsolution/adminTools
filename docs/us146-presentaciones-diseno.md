# Presentaciones de venta — diseño (US-146, fase futura)

**Estado: SOLO DISEÑO.** Nada de esto está implementado; este documento fija el
modelo para que la US que lo implemente no tenga que re-decidir, y para dejar
constancia de que la bandera `se_pesa` (V46) no lo bloquea.

## El problema

Un mismo producto se vende en unidades de distinto tamaño con precio propio:

| Presentación | Ejemplo | Precio |
|---|---|---|
| Unidad | 1 lb de frijol | L 28 |
| Docena | 12 unidades de X | menor por unidad |
| Quintal | 100 lb de frijol | precio de mayoreo |

Hoy el sistema solo modela una unidad de venta por artículo. El multi-precio
existente (`precios_articulos` + `precios`) **no sirve** para esto: su dimensión
es *tipo de cliente* (Público General / Especiales / Mayoristas / Costos), está
cableado en `f_precio_articulo`, `v_precios_general` y `PRECIO_PUBLICO_GENERAL=1`
de la API, y `usuarios_precios` gobierna qué lista ve cada usuario. Mezclar ahí
la dimensión *tamaño del empaque* rompería las dos semánticas.

## Modelo propuesto

### Tabla nueva `presentaciones_articulos` (migración futura, común)

```sql
CREATE TABLE presentaciones_articulos (
  id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo_articulo INT UNSIGNED NOT NULL,           -- FK articulo
  nombre          VARCHAR(50)  NOT NULL,            -- "Docena", "Quintal", "Fardo 1x24"
  factor          DECIMAL(10,3) NOT NULL,           -- unidades BASE por presentación
  precio          DECIMAL(15,2) NOT NULL,           -- precio de la presentación completa
  codigo_barra    VARCHAR(255) NULL,                -- escanear el empaque directo (opcional)
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  KEY idx_pres_articulo (codigo_articulo),
  CONSTRAINT fk_pres_articulo FOREIGN KEY (codigo_articulo)
    REFERENCES articulo (codigo_articulo) ON DELETE CASCADE
);
```

- `factor` con 3 decimales por presentaciones fraccionarias (media docena, ½ lb).
- `codigo_barra` propio permite que el escáner resuelva directo "quintal de
  frijol" sin pasar por `codigos_articulos` (que identifica al artículo, no al
  empaque). La búsqueda del POS revisaría ambas tablas.
- La fila "unidad base" NO existe en la tabla: la unidad es el artículo mismo
  (su precio de `precios_articulos`). Solo se registran los empaques mayores.

### Inventario

El kardex sigue en unidades base — **no cambia nada del modelo de stock**. Una
venta de presentación descuenta `cantidad × factor` unidades base. El guard de
sobreventa (V45, sobre disponible) funciona sin tocarse porque el trigger recibe
la cantidad ya convertida.

### Venta (POS)

- `CartLine.presentationId?: number` + `presentationName`/`factor` para pintar.
- **Identidad de línea: ya resuelta.** El refactor de US-146 (`lineId` uuid)
  existe precisamente para esto: "2 quintales" y "5 lb sueltas" del mismo
  artículo son dos líneas distintas del mismo `product.id`, igual que hoy dos
  pesadas conviven separadas.
- El detalle de factura guarda la cantidad en unidades de la presentación y el
  precio de la presentación; la conversión a base ocurre al descontar kardex.
  (Alternativa descartada: guardar en base y "reconstruir" la presentación al
  imprimir — pierde la fidelidad del documento fiscal: el cliente compró
  "2 quintales", no "200 libras".)
  - Implicación: el descuento a kardex necesita conocer el factor en el momento
    de facturar → la API/Swing resuelven la presentación al armar el detalle.

### Convivencia con `se_pesa`

Sin conflicto — son ejes ortogonales:

- `se_pesa` (columna de `articulo`, V46) significa: *la venta a GRANEL de este
  artículo exige báscula* (unidad base = libra).
- Un artículo pesado puede tener presentaciones preempacadas: el quintal de
  frijol (`factor = 100.000`) se vende **contado, sin báscula** — el empaque ya
  tiene peso conocido. La línea con `presentationId` es entera y no pesa; la
  línea a granel (sin presentación) pesa.
- Un artículo por unidad con presentaciones (docena) tampoco interactúa con la
  báscula.

Regla al implementar: en el POS, `requestAdd` pesa solo si `sePesa && !presentationId`.

### API (bosquejo)

- `GET/PUT /products/{id}/presentations` (patrón de `/products/{id}/prices`:
  reemplazo completo, ADMIN).
- `ProductResponse` gana `presentations: [{id, nombre, factor, precio, codigoBarra}]`
  (o endpoint aparte si pesa en el listado).
- `InvoiceLineRequest` gana `presentationId?` — la API valida que pertenezca al
  producto y convierte a base para el kardex.

### Swing

Fuera del alcance inicial: el Swing seguiría vendiendo por unidad base. Si el
cliente lo necesita en el Swing, es una US aparte (tabla ya definida aquí).

## Por qué V46 no se pinta en un rincón

- `se_pesa` es una bandera del artículo, no de la línea: no asume nada sobre
  presentaciones.
- La identidad de línea del POS (`lineId`) ya soporta N líneas por producto.
- `detalle_factura.cantidad` (float(11,2)) admite los 2 decimales que usan
  tanto libras como factores usuales; si una presentación fraccionaria exigiera
  3 decimales, la migración de `cantidad` a DECIMAL(12,3) es independiente y
  está descrita en el análisis de US-146 (toca SPs de kardex — hacerla solo si
  aparece la necesidad real).
