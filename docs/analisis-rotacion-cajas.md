# Rotación automática de cajas — algoritmo, bug de paridad (US-191) y porcentaje configurable (US-192)

Fecha: 2026-09-22

## 1. Para qué sirve

Un cajero con **2 cajas asignadas** (`cajas_usuarios`) y el flag
`config_user_facturacion.rotacion_automatica_cajas = 1` reparte sus facturas
entre los rangos CAI de las dos cajas. Aplica en el Swing (`CtlFacturarFrame`)
y en el POS (la decisión la toma la API, el cajero no ve nada distinto).

## 2. Qué ventas rotan

| Venta | Caja |
|---|---|
| Mostrador, **consumidor final** (cliente id 1), flag encendido | 1 de cada 3 va a la segunda caja, el resto a la default |
| Mostrador, cliente identificado | Siempre la default (no rota ni cuenta para la cadencia) |
| Facturar una orden / pedido (`createFromOrder`, `CtlOrdenVenta`) | Siempre la default de quien factura |
| Caja elegida a mano (Ctrl+P del Swing / `cajaId` del POS) | La elegida; con el flag encendido el Swing bloquea Ctrl+P y el POS oculta el selector |

"Default" = la caja con `por_defecto = 1` del usuario que factura (sin marca:
la primera por código).

## 3. El algoritmo del Swing (referencia)

1. **Bandera** (`RotacionCajas.java`): contador por sesión 0→1→2→0 que solo
   avanzan las ventas a consumidor final **ya guardadas**
   (`banderaPostGuardar`). Antes de guardar, si vale 0 se rota
   (`debeRotarPreGuardar`).
2. **Rotar** = `usuario.nextCaja()` (`Usuario.java`): busca la caja activa y
   activa la siguiente de la lista (vuelve al inicio si es la última).
3. **Recarga tras cada venta** (`CtlFacturarFrame` ~l.2012): con el flag
   encendido hace `usuario.setCajas(cajasDao.getCajasUsuario(usuario))`, y
   `CajaDao.getCajasUsuario` marca activa la `por_defecto`. Por eso **cada
   rotación parte de la default**: con 2 cajas siempre cae en la segunda.

Resultado con 2 cajas (A default, B segunda), solo ventas CF:

```
Venta CF:  1  2  3 | 4  5  6 | 7 ...
Caja:      B  A  A | B  A  A | B       → A 2/3 (≈67 %), B 1/3 (≈33 %)
```

Sobre el total de facturas del cajero, B recibe menos: 33 % × (fracción de
sus ventas que son CF de mostrador).

## 4. US-191 — bug de paridad en la API (corregido)

El port de US-102 (`RotacionCajasService`) guardaba un **puntero round-robin
en memoria** entre rotaciones. Con 2 cajas la segunda rotación "daba la
vuelta" a la propia default, así que la mitad de las rotaciones no cambiaban
de caja:

```
API vieja: B  A  A | A  A  A | B       → A 5/6 (≈83 %), B 1/6 (≈17 %)
```

Además la bandera avanzaba al **decidir**, antes de guardar: una venta que
fallaba (409 por stock, error de BD) consumía el turno.

Corrección (rama API `fix/us-191-rotacion-paridad-swing`):

- `decideCaja` ya no guarda puntero: si la bandera vale 0 devuelve la caja
  siguiente a la default (espejo de `nextCaja()` tras la recarga).
- La bandera avanza en `registrarVentaGuardada`, que `InvoiceService.createDirect`
  llama después de guardar la factura y solo sin caja manual (espejo de
  `banderaPostGuardar`).
- Con 3+ cajas también queda como el Swing: siempre la siguiente a la
  default (el Swing nunca llega a la tercera).
- Tests: cadencia B/A/A ×3, 100 de 300 ventas CF a la segunda, venta fallida
  sin consumir bandera, default al final de la lista.

Sigue igual que el Swing: la bandera vive en memoria; reiniciar la API la
vuelve a 0 (como cerrar el Swing).

## 5. US-192 — porcentaje configurable (FUTURO, no implementar aún)

Pedido: que el administrador elija qué porcentaje de las ventas CF va a la
segunda caja, en lugar del 33 % fijo.

### Algoritmo propuesto: acumulador

```
por cada venta CF guardada, con el flag encendido:
    acumulado += porcentaje
    si acumulado >= 100: esta venta va a la segunda caja; acumulado -= 100
```

(La decisión se toma antes de guardar mirando si `acumulado + porcentaje >= 100`,
y el acumulador se actualiza después de guardar, igual que la bandera hoy.)

- Reparto exacto y parejo: 30 % → 3 de cada 10 intercaladas; 50 % → una sí,
  una no; 25 % → 1 de cada 4.
- Con 33 % reproduce la cadencia actual (B/A/A), así que es el default y
  quien no lo toque no nota cambio.
- Se mantienen las reglas de la sección 2 y la dirección "siempre desde la
  default hacia la segunda".

### Piezas

| Dónde | Cambio |
|---|---|
| BD (Flyway, dueño = Swing) | `config_user_facturacion.porcentaje_rotacion` TINYINT default 33 (migración aditiva, número siguiente libre) |
| Swing | `RotacionCajas`: bandera → acumulador leyendo el porcentaje; config de usuario con el campo |
| API | `RotacionCajasService` con acumulador; `PUT /users/{id}/rotacion` acepta `{enabled, porcentaje}`; `UserResponse` lo expone |
| POS | Usuarios: control de porcentaje junto al toggle de rotación, habilitado solo con la rotación encendida |

### Decisiones pendientes

1. Swing y API deben cambiar juntos (si no, el mismo cajero reparte distinto
   según la app). Recomendado: sí, juntos.
2. Control: valor libre 1–99 o lista fija (10/20/25/30/33/40/50).
   Recomendado: lista.
3. Tope en 50 % (más que eso invierte el papel de la default).
   Recomendado: sí.
