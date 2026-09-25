# US-195 — El reparto de un pago por factura nunca supera el saldo general del cliente

Fecha: 2026-09-24 · Estado: Backlog · Origen: Farmacia La Fe, cliente 94, recibo 2859

## 1. Problema

Cada cliente de crédito tiene dos cuentas:

- **Saldo general** (`cuentas_por_cobrar`): el oficial; es el que usa el estado de cuenta.
- **Detalle por factura** (`cuentas_facturas` + `cuentas_por_cobrar_facturas`): cuánto queda de cada factura.

El sistema anterior dejaba el detalle por factura **desfasado por encima** del saldo general en
varios casos (cobros con sobrante o sin facturas asignadas, anulaciones de facturas ya abonadas,
saldo a favor antes de mediados de 2021, intereses duplicados). En La Fe hay 16 clientes con
L 4,659.27 de diferencia (ver `~/Documents/auditorias/LaFe_conciliacion_CxC_2026-09-24.pdf`).

El sistema nuevo (`AccountsReceivableService.applyPayment`) reparte cada pago entre las facturas
con saldo **en el detalle**, de la más antigua a la más nueva, **sin compararlo con el saldo
general**. Si el detalle está desfasado, el pago cubre primero facturas que en el saldo general ya
estaban pagadas, y la factura que el cliente quería pagar queda con un saldo que no debe.

Caso real: recibo 2859 (L 535.00). Saldo general 535 → 0, pero en el detalle se aplicó 310 a la
51869, 190 a la 52116 y solo 35 a la 52939, que quedó con L 500.00 "pendientes".

## 2. Regla

> Después de un pago, lo pendiente en el detalle por factura del cliente **nunca es mayor** que su
> saldo general.

## 3. Diseño

En `applyPayment`, dentro de la misma transacción, antes de repartir el pago:

1. `saldoGeneral` = saldo del cliente antes del pago (último movimiento de `cuentas_por_cobrar`).
2. `pendienteDetalle` = suma del saldo de sus facturas con saldo > 0.
3. `exceso = pendienteDetalle − max(saldoGeneral, 0)`. Si `exceso > 0`, **conciliar primero**: se
   rebaja ese exceso de las facturas más antiguas del detalle con un movimiento
   `tipo_movimiento = 2`, descripción **"Ajuste de conciliación con el saldo general"**, usuario
   del cobro. Esas facturas ya estaban cubiertas en el saldo general.
4. Recién entonces se reparte el pago como hoy (antigua primero, cada factura hasta su saldo).
5. Si el pago supera lo pendiente, el sobrante queda como saldo a favor en el saldo general (como
   hoy); la venta a crédito siguiente ya lo aplica en el detalle ("Pago por saldo a favor").

Queda registrado en el log (`CXC-CONCILIA cliente X exceso Y`) para poder auditarlo.

Con esto el detalle **se corrige solo** en el primer cobro de cada cliente desfasado, aunque no se
haya aplicado la corrección manual de datos. La corrección manual sigue siendo recomendable para
que los reportes por factura (morosidad, estado por factura) salgan bien antes de ese cobro.

**Anulaciones**: `reverseCreditInvoice` del sistema nuevo ya acredita el saldo general por lo
pendiente de la factura (no por su total, como hacía el sistema anterior), así que no genera
nuevos desfases. Se agrega un test que lo fije.

**Pantalla del POS** (`AccountDrawer` / `AbonoDialog`): si el detalle por factura suma más que el
saldo general, mostrar un aviso "El detalle por factura tiene L X de diferencia con el saldo del
cliente; se concilia al registrar el próximo pago", para que el cajero no se confunda.

## 4. Criterios de aceptación

1. Cliente con detalle desfasado (caso cliente 94): al cobrar el saldo general completo, el saldo
   general queda en 0 **y** el detalle por factura queda en 0; los movimientos muestran el ajuste
   de conciliación y el pago en las facturas correctas.
2. Cliente sin desfase: el reparto es idéntico al actual (sin movimientos de ajuste).
3. Pago parcial con desfase: primero la conciliación, después el pago; al terminar,
   pendiente del detalle = saldo general.
4. Cliente con saldo a favor (saldo general negativo) y detalle con saldo: el exceso se concilia por
   todo lo pendiente del detalle.
5. Todo en una sola transacción: si algo falla no queda ni el recibo ni los movimientos.
6. Tests unitarios de los casos 1–4 y de la anulación.
7. Aviso en la pantalla del POS cuando hay diferencia.

## 5. Relación

- Corrección de datos de La Fe (informe del 24-sep-2026): se puede aplicar antes o después; esta
  US evita que el problema se repita y que confunda en caja.
- Aplica a todos los clientes (Samuel, La Fe, Mariposas, dulce, venecia): los datos migrados del
  sistema anterior pueden tener el mismo desfase.

## 6. Estimación

3 SP (API 2 · POS 1).
