# Análisis de impacto — Stock disponible descontando pedidos pendientes

**Fecha:** 2026-07-27 · **Alcance:** ecosistema completo (Swing, API, POS React, app de pedidos, BD)

## Resumen ejecutivo

La funcionalidad que hoy tiene la app de pedidos — mostrar y validar **disponible = stock físico − cantidades en pedidos pendientes** — **no vive en la app**: vive en la base de datos común, en la función `f_existencia_y_ordenes(articulo, bodega)` (saldo kardex − pedidos con `estado < 3`), más el guard anti-sobreventa de `POST /orders/save` (US-074). La app solo pinta el número que le da la API.

Implementarla "en todo el sistema" **no es construirla desde cero: es empezar a usar una fórmula que ya existe y cerrar sus huecos**. Hoy el ecosistema está partido en dos mundos que no coinciden:

| Mundo | Cifra que usa | ¿Descuenta pedidos? |
|---|---|---|
| App de pedidos (`/products/*` → `articulo_view`) | `f_existencia_y_ordenes` | ✅ Sí |
| Guard de `/orders/save` (US-074) | `f_existencia_y_ordenes` + add-back | ✅ Sí |
| **Swing completo** (facturación, listados, reportes) | `f_can_saldo_kardex` (bruto) | ❌ No |
| **POS React** (`/inventory/*`: stock, low-stock, valoración) | `existencia_articulo_bodega` (bruto) | ❌ No |
| **Guard de venta** (trigger V9 + SP V33 al facturar) | saldo kardex bruto | ❌ No |

Consecuencia práctica hoy: **un vendedor de la app reserva stock que la caja del Swing/POS puede vender igual** — la reserva solo protege contra otros vendedores de la app, no contra el mostrador.

---

## 1. Cómo funciona hoy (verificado en código)

### La fórmula (BD común, `V7__recrear_funciones_procedures_triggers.sql:143`)

```sql
f_existencia_y_ordenes(art, bodega) =
    último saldo kardex (movimiento tipo 3)
  − SUM(detalle_factura_temp.cantidad)
      WHERE encabezado_factura_temp.estado < 3        -- 1 Activa, 2 Modificada
        AND cajas.codigo_bodega = bodega              -- vía encabezado.codigo_caja
```

- Consumida **solo** por `articulo_view.existencia` (con **bodega hardcodeada a 1**, V1:846 / V21:50), que la API mapea al campo `stock` de `/products/*`.
- **El Swing no la llama en ningún lado** (cero referencias en Java/Jasper).

### Ciclo de vida del pedido (la "reserva" es implícita)

| estado | significado | ¿reserva? |
|---|---|---|
| 1 / 2 | Activa / Modificada | ✅ (`estado < 3`) |
| 3 | Imprimida = **facturada** | ❌ se libera |
| 4 | Enviado | ❌ **se libera sin haberse facturado** ⚠️ |
| 5 | Eliminado (soft) | ❌ se libera |

No hay tabla ni columna de "reservado": la reserva ES la fila del pedido. Borrar/facturar el pedido la libera sola. No existe expiración: un pedido olvidado en estado 1 **reserva stock para siempre**.

### El guard que sí funciona (US-074, `OrderService.save`)

`POST /orders/save` valida con lock pesimista (`FOR UPDATE` sobre `articulo`, ids ascendentes, `READ_COMMITTED`): disponible = `f_existencia_y_ordenes` + add-back de la propia orden en updates → si no alcanza, 409 con `conflicts[{productId, nombre, pedida, disponible}]`. La app lo muestra como toast y conserva el carrito. Los artículos **sin kardex en la bodega quedan exentos** (misma regla que el Swing).

---

## 2. Brechas y riesgos detectados (lo que hay que cerrar antes/al implementar)

Ordenados por severidad:

1. **La venta no respeta la reserva.** El guard de facturación (`crear_venta_kardex_v2`, V33 + trigger caja V9) valida contra saldo kardex **bruto**. La caja puede vender lo que un pedido tiene reservado. *Nota de diseño*: al facturar EL pedido, usar bruto es correcto (la unidad reservada es la que se está facturando); el hueco es la venta directa de mostrador.
2. **Los pedidos de la API caen siempre en la bodega de la caja 1.** `Orden.codigoCaja` tiene `DEFAULT 1`, es `@JsonIgnore` y **ningún código lo asigna** desde el tenant/JWT. Si la caja 1 no existe o su bodega ≠ 1, la reserva se pierde en silencio. Además `articulo_view` y el guard US-074 hardcodean bodega 1 → **todo el mecanismo actual solo es correcto en negocios de una sola bodega**.
3. **Doble descuento transitorio al facturar un pedido.** El flujo es: insertar `detalle_factura` en la caja (baja kardex) → después `estado = 3` (libera reserva) — dos sentencias, sin transacción común. Si el paso 2 falla, la venta bajó el kardex **y** el pedido sigue reservando: disponible doblemente descontado. (`CtlFacturarFrame:1993` en Swing; `markOrderInvoiced` en la API tiene compensación en `/invoices/from-order` pero el camino `/invoices` con `orderId` solo hace warn-and-skip si la orden no está visible.)
4. **`FacturaOrdenVentaDao.actualizar()` (Swing) borra y re-inserta los detalles sin transacción** → durante la edición la reserva desaparece unos instantes (otro vendedor podría colarse).
5. **Estado 4 "Enviado" libera la reserva sin facturar** (queda fuera de `estado < 3`). Decidir si "Enviado" debe seguir reservando.
6. **Insumos/servicios no son reservables** con la fórmula actual: el pedido guarda el servicio, pero el kardex descuenta los **insumos** (vía `crear_venta_insumo_kardex`, que además llama al SP viejo sin guard). Estructuralmente fuera del alcance de esta fórmula.
7. **Sin expiración (TTL) de reservas** — un pedido abandonado congela inventario indefinidamente.
8. **Rendimiento**: `f_existencia_y_ordenes` hace 2 subconsultas por llamada y está marcada `DETERMINISTIC` siendo que lee tablas (riesgo de caching/replicación). Usarla en listados fila-por-fila (el `sqlBaseJoin` del Swing, `articulo_view` completa) es costoso con catálogos grandes (dulce ~1.16M filas en caja_1 de kardex). Cualquier extensión debe medirse; probablemente convenga una vista/consulta agregada de "reservado por artículo" en vez de N llamadas a la función.
9. **`GET /products/category/{id}`** de la API lee la tabla cruda (`SELECT * FROM articulo`) — inconsistente con el resto (sin existencia real).
10. **`POST /inventory/counts` (toma física)** recibe el "sistema" desde el cliente — si el cliente manda disponible en vez de físico, el acta queda mal. El inventario físico SIEMPRE debe compararse contra bruto.

---

## 3. Impacto por componente

### 3.1 BD común (la base de todo)

- **Parametrizar la bodega de verdad**: dejar de hardcodear 1 en `articulo_view` y en el guard US-074. Requiere el fix del punto 2 (atribuir caja/bodega real al pedido al guardarlo).
- **Índices**: `detalle_factura_temp(codigo_articulo)` existe; falta apoyar el JOIN a `cajas` y considerar una vista agregada `v_reservado_por_articulo` para listados.
- Opcional (recomendado): SP `crear_venta_kardex_v3` con un flag para validar contra **disponible** en ventas directas y contra **bruto** al facturar pedidos (o pasar `numero_factura_temp` para add-back). Esfuerzo: medio. Riesgo: alto si se toca el SP sin ensayo (es el corazón del kardex) — misma disciplina que V33: ensayo en dulce primero.

### 3.2 Swing (el mayor impacto de código)

- **Choke point único**: `ArticuloDao.getExistencia()` (`f_can_saldo_kardex`) alimenta los ~10 puntos de validación de `CtlFacturarFrame` + `CtlOrdenVenta`. Crear `getDisponible()` (con `f_existencia_y_ordenes`) y decidir dónde usar cada una:
  - **Armar pedido/orden** → disponible.
  - **Venta directa** → disponible (es el objetivo del proyecto).
  - **Facturar una orden cargada** → disponible **+ add-back de la propia orden** (sin esto, facturar el pedido choca contra su propia reserva — el bug que la API ya resolvió en US-074).
- Arreglar `actualizar()` con transacción (punto 4).
- Listados/reportes: mantener columna física y **agregar** "Disponible" donde aporte (listado de artículos, alerta). Los reportes de valoración/existencia física quedan en bruto.
- Esfuerzo: medio (2-4 días + pruebas). Riesgo: medio — es la pantalla más crítica del negocio; validar en dulce antes de clientes.

### 3.3 API (huecos puntuales)

- Poblar `Orden.codigoCaja` desde el tenant al guardar (fix estructural, chico).
- `StockResponse` de `/inventory/stock`: agregar `reserved` y `available` junto al físico (el POS decide qué mostrar).
- `low-stock`: decidir política (alerta sobre físico o sobre disponible; recomendado: físico, con columna disponible informativa).
- `POST /invoices` directo: hoy no valida stock a nivel aplicación (solo el trigger). Si se quiere que el mostrador respete reservas, el guard va acá (mismo patrón US-074) o en el SP v3.
- `products/category/{id}`: normalizar a `articulo_view`.
- Esfuerzo: chico-medio (2-3 días). Riesgo: bajo, es código nuevo con patrón ya probado.

### 3.4 POS React

- Solo consume: mostrar "Disponible / Reservado" en Productos e Inventario cuando la API exponga los campos; opcionalmente badge en el catálogo de facturación. Esfuerzo: chico (1-2 días). Riesgo: bajo.

### 3.5 App de pedidos

- **Ya funciona**; se beneficia gratis de los fixes de bodega/caja. Nada obligatorio.

---

## 4. Plan recomendado (fases independientes, cada una con valor propio)

1. **Fase 0 — Cimientos (BD + API), sin cambiar comportamiento visible:**
   atribuir caja/bodega real al pedido · parametrizar bodega en vista/guard · índice/vista agregada de reservado · exponer `reserved/available` en `/inventory/stock`. *Deja el dato correcto y visible.*

   **Mecanismo acordado (2026-07-27) para atribuir la caja al pedido**: reutilizar la misma resolución usuario→caja del `TenantInterceptor` — `cajas_usuarios` con `ORDER BY por_defecto DESC, codigo_caja ASC` y fallback legacy `usuario.codigo_caja` — pero devolviendo `cajas.codigo`, y asignarla a `encabezado_factura_temp.codigo_caja` en `OrderService.save`. Con eso `f_existencia_y_ordenes` reserva en la bodega real del vendedor (misma tabla `cajas` que usa el trigger de facturación → coherente de punta a punta). Notas: (a) el vendedor siempre tiene caja en la práctica (sin ella el tenant no resuelve y la app no funciona); (b) el `stock` que muestra la app viene de `articulo_view` clavada en bodega 1 — correcto para clientes mono-bodega, a recalcular por bodega del usuario si algún día hay vendedores contra otra bodega; (c) si un cajero de OTRA bodega factura el pedido, la reserva se libera de una bodega y el kardex baja en otra — con cajas del negocio compartiendo bodega (caso típico, regla US-105) no ocurre.
2. **Fase 1 — Visibilidad:** POS y Swing muestran "Disponible" junto al físico (sin bloquear nada todavía). *Los usuarios ven el efecto de los pedidos y se validan los números en producción real.*
3. **Fase 2 — Enforcement en mostrador:** venta directa valida contra disponible (Swing `getDisponible()` + guard en `/invoices` o SP v3), con el mismo opt-in por usuario de V33 (`facturar_sin_inventario`) para no frenar a nadie de golpe. Add-back al facturar pedidos.
4. **Fase 3 — Robustez del ciclo de vida:** transacción en `actualizar()` · política para estado 4 · expiración/limpieza de pedidos viejos (job o aviso) · compensación transaccional al facturar.

Regla operativa ya establecida en el proyecto: **todo se valida primero en local y en el stack de dulce; nada llega a clientes sin OK explícito** (misma disciplina que V33/US-074).

## 4b. Decisiones ya tomadas (2026-07-27)

1. **Caja del pedido = caja del vendedor**, resuelta con la misma lógica del `TenantInterceptor` (ver mecanismo en Fase 0).
2. **Los usuarios VENDEDORES solo pueden tener UNA caja asignada** (a diferencia de los cajeros, que pueden tener varias — US-102/105). Esto hace la atribución del pedido inequívoca: no hay "por defecto entre varias", hay una sola. Enforcement pendiente en la administración de usuarios: al asignar cajas (`cajas_usuarios`) a un usuario con permiso de vendedor (tipo 3), la API debe rechazar más de una y la pantalla de usuarios del POS debe limitar la selección; además del guard, conviene una validación de datos existentes al migrar (detectar vendedores que hoy tengan 2+ cajas y normalizarlos).

## 5. Decisiones que hay que tomar (antes de la Fase 2)

1. ¿La venta de mostrador debe **bloquearse** por reservas de pedidos, o solo **avisar**? (el opt-in por usuario permite un despliegue gradual)
2. ¿"Enviado" (estado 4) sigue reservando o no?
3. ¿Expiran los pedidos? ¿A las cuántas horas/días, y quién los limpia?
4. ¿La alerta de stock mínimo se dispara por físico o por disponible?
5. Multi-bodega: ¿se necesita ya, o se documenta la limitación a bodega 1 y se difiere?

---

*Fuentes: exploración de código 2026-07-27 sobre adminTools (Swing), la API Spring Boot, admintools-pos y at-ordenes-ventas. Referencias clave: `V7__recrear_funciones_procedures_triggers.sql:143` (fórmula), `V33__crear_venta_kardex_v2_valida_usuario.sql` (guard de venta), `OrderService.save` US-074 (guard de pedidos), `CtlFacturarFrame` (validaciones Swing), `docs/inventario-api-design.md` (hardcode bodega 1 documentado).*
