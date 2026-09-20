# Análisis — App de pedidos sin verificar stock (modo cliente final)

**Fecha**: 2026-09-20 · **Origen**: canales de venta de Samuel (2026-09-09/10) y decisión #3 de
`analisis-tienda-app-pedidos.md` · **Estado**: análisis listo, implementación pendiente de que
el cliente confirme que quiere pedidos sin verificación de inventario.

## 1. El problema

La app de pedidos (`at-ordenes-ventas`) nació para vendedores de ruta (Sharon): el vendedor está
frente al inventario real y **la app topa cada cantidad al stock disponible**. En Samuel la misma
app va a estar **en manos del cliente final** (un usuario por cliente, precios propios, US-158/159):
el cliente pide lo que necesita y el supermercado resuelve después qué despacha. Con el tope de
stock, un producto en 0 no se puede pedir y el cliente no tiene cómo decirlo.

## 2. Lo que ya existe (hallazgos en el código)

| Capa | Estado hoy | Referencia |
|---|---|---|
| Servidor: guard anti-sobreventa de `/orders/save` | **Opt-in por usuario, ya implementado (US-074)**: solo bloquea si `config_user_facturacion.facturar_sin_inventario = 0`; con `= 1` o **sin fila de config acepta el pedido sin stock** (comportamiento histórico) | `OrderService.findStockConflicts` (misma semántica que `crear_venta_kardex_v2`, V33) |
| Servidor: cifra de disponible | `f_existencia_y_ordenes(art, bodega)` = saldo kardex − órdenes pendientes, en la bodega de la caja del vendedor (US-109/130) | `OrderService`, vistas de producto |
| App: tope de cantidad | **Solo en el frontend**: `stockDisponible = stock − lo ya en el carrito`; producto con disponible ≤ 0 no se puede agregar; botones `+` y «Agregar» deshabilitados si `amount > stockDisponible`; badge «Stock: N» verde/rojo | `src/componets/AgregarItemModal.jsx` (líneas ~20, ~103-108, ~133-150, ~205-227, ~270, ~308) |
| App: login | Guarda solo `token` y `username`; **no sabe nada de la configuración del usuario** | `src/componets/LoginModal.jsx` |
| POS admin: edición de la bandera | El ⚙ «Configuración de facturación» (US-151/152) **solo aparece para cajeros** (`tipoPermiso === 2`); para un vendedor la bandera hoy se tocaría por SQL | `admintools-pos/src/pages/UsersPage.tsx` ~L191, `ConfigFacturacionDialog` |
| API: lectura de la config | `GET /sellers` devuelve las banderas del usuario autenticado al POS (`SellerSettingsResponse`); `GET/PUT /users/{id}/facturacion-config` para el admin | `SellerCatalogService.getSettings`, `UserFacturacionConfigService` |

Conclusión: **el servidor ya permite pedidos sin stock**; lo único que bloquea es la app, que
aplica el tope sin saber qué tiene configurado el usuario. No hace falta migración.

## 3. Propuesta (Opción B del análisis de canales)

Un solo interruptor, el que ya existe: `facturar_sin_inventario` del usuario vendedor.

1. **API — exponer el modo del usuario** (~1 SP)
   - `GET /sellers` (o un `GET /auth/me` mínimo) devuelve `verificaInventario` =
     `facturar_sin_inventario == 0` (sin fila = `false`, coherente con el guard).
   - Sin cambios en `/orders/save`: el guard sigue igual y es la red de seguridad.
2. **App de pedidos — stock informativo cuando no se verifica** (~2 SP)
   - Tras el login, leer el modo y guardarlo con la sesión.
   - `AgregarItemModal`: si `verificaInventario === false`, **no topar**: se puede agregar con
     disponible 0 y cualquier cantidad; el badge «Stock: N» se mantiene como información (gris
     cuando es 0, sin rojo de error). Si es `true`, todo sigue exactamente como en Sharon.
   - Mensaje al confirmar el pedido en modo sin verificación: «Las cantidades se confirman al
     preparar el pedido» (evita que el cliente asuma que todo está disponible).
3. **POS — la bandera editable para vendedores** (~1 SP)
   - Mostrar el ⚙ de configuración también en usuarios `tipoPermiso === 3` (vendedor) con solo
     las opciones que le aplican («Facturar/pedir sin inventario» y el tipo de precio si procede).
   - Texto de la opción para vendedores: «Pedidos sin verificar inventario».

Total **~4 SP**, sin migración, reversible (apagar la bandera vuelve a verificar).

## 4. Qué NO cambia

- Sharon: sus vendedores tienen `facturar_sin_inventario = 0` (58 de 59 usuarios en la última
  medición) → siguen con tope en la app y guard en el servidor.
- POS y Swing: la facturación directa conserva su propia validación (US-116/117, V33).
- Stock reservado: las órdenes sin stock **sí reservan** (`f_existencia_y_ordenes` las descuenta),
  así que el disponible que ve mostrador puede quedar negativo mientras el pedido esté pendiente.
  Es el comportamiento buscado (el pedido "aparta" lo que se pueda despachar); se documenta para
  que no se lea como error.

## 5. Riesgos y decisiones pendientes

| # | Punto | Propuesta |
|---|---|---|
| 1 | ¿Samuel quiere pedidos sin verificar stock? | **Pendiente del cliente** (2026-09-10: "probablemente sí"). Sin esto no se arranca. |
| 2 | ¿Bandera por usuario o global para la instalación? | Por usuario (ya existe y permite mezclar vendedores de ruta y clientes finales). |
| 3 | ¿El cliente final debe ver el stock? | Sí, informativo: ayuda a pedir realista. Si Samuel prefiere ocultarlo, es un `if` en el badge. |
| 4 | Facturar un pedido con líneas sin stock | Ya lo resuelve el flujo actual: al facturar la orden en el POS/Swing el guard de mostrador (si está activo) avisa el conflicto y el cajero ajusta cantidades. |

## 6. Referencias

- `docs/analisis-tienda-app-pedidos.md` (decisión #3) · `docs/analisis-impacto-stock-reservado.md` (US-074, guard opt-in)
- API: `OrderService.findStockConflicts`, `SellerCatalogService.getSettings`, `UserFacturacionConfigService`
- App: `src/componets/AgregarItemModal.jsx`, `src/componets/LoginModal.jsx`
- POS: `src/pages/UsersPage.tsx`, `src/features/users/components/ConfigFacturacionDialog.tsx`, `src/features/users/cfgOptions.ts`
