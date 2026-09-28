# US-198 — Reporte de comisiones por vendedor en el POS

Fecha: 2026-09-27 · Estado: Terminada (2026-09-27: en Samuel, Mariposas, La Fe y dulce — API a5110e1, POS 5415a76) · Origen: réplica del reporte «Ventas usuarios» del Swing

## 1. Qué hace hoy el Swing

Menú **Reportes → Ventas usuarios** (`CtlFiltroRepVentasUsuarios`, `FacturaDao.getVentasUsuarios`,
`reportes/ventas_usuarios.jasper`, solo administrador):

- Filtro: **Desde / Hasta** y **Porcentaje S/V** (1–20 %).
- Recorre **todas las cajas** y, por **usuario que facturó** (`encabezado_factura.usuario`, solo usuarios
  con rol Cajero), suma las facturas **ACT** del rango: nº de facturas («clientes atendidos») y total vendido.
- Comisión = `porcentaje / 100 × total vendido`.
- Carta: Usuario · No. clientes atendidos · Total ventas · **Comisión X %**, con totales.

Defectos conocidos que NO se replican:
- **El día «Hasta» queda fuera**: filtra `fecha BETWEEN desde AND hasta` sobre un DATETIME → solo cuenta
  las facturas de las 00:00 del último día (con desde = hasta el reporte sale vacío).
- No descuenta devoluciones.

(Existe otro reporte «Comisiones» — `CtlFiltroRepComisiones`, `comisiones2.jasper` — que agrupa por
vendedor con ventas, costos y devoluciones, pero **no imprime la comisión**.)

## 2. Decisiones (usuario, 2026-09-27)

| Tema | Decisión |
|---|---|
| A quién se atribuye la venta | **Al vendedor de la factura** (`encabezado_factura.codigo_vendedor` → `empleados`) |
| Base de la comisión | **Ventas − devoluciones** |
| Porcentaje | **Uno para todos**, se elige al sacar el reporte (sin migración) |
| Quién aparece | **Solo los vendedores con ventas** en el período |
| Fechas | Rango inclusivo: desde 00:00 del primer día hasta el fin del último día |

## 3. Diseño

**API** — `GET /reports/commissions?from=YYYY-MM-DD&to=YYYY-MM-DD&porcentaje=N[&caja=]`
(roles ADMIN; mismo patrón que `CategorySalesService`/`AbcAnalysisService`: `commonDataSource`,
recorre `cajas` con nombre de BD validado).

Por cada caja:
1. Ventas: `SELECT codigo_vendedor, COUNT(*), SUM(total), SUM(total si tipo_factura=2), SUM(total si =1)
   FROM <caja>.encabezado_factura WHERE estado_factura='ACT' AND fecha >= :from AND fecha < :to + 1 día
   GROUP BY codigo_vendedor`.
2. Devoluciones de esas facturas (misma regla que el Swing: facturas del período):
   `SUM(detalle_devoluciones.total)` con `detalle_devoluciones.codigo_caja = <caja>` y
   `numero_factura` de las facturas del paso 1.

Se suman todas las cajas por vendedor y se agrega el nombre (`empleados.nombre + apellido`).
Base = ventas − devoluciones; comisión = `round2(base × porcentaje / 100)`.

- El empleado **1 (system)** es el genérico de facturas sin vendedor: no se lista como vendedor. Su total
  va en una línea aparte «Ventas sin vendedor asignado», sin comisión, para que el total cuadre con las ventas.
- `porcentaje` entre 0 y 100 con 2 decimales (el Swing limitaba a 1–20 enteros).

Respuesta: `{ from, to, porcentaje, vendedores: [{ codigo, nombre, facturas, contado, credito, ventas,
devoluciones, base, comision }], sinVendedor: { facturas, ventas }, totales }`.

**POS** — pantalla **Reportes → Comisiones** (`/reports/comisiones`, solo ADMIN):
- Filtros: Desde / Hasta (por defecto el mes en curso), **% comisión** y opcional Caja.
- Tabla: Vendedor · Facturas · Ventas · Devoluciones · Base · **Comisión X %**, ordenada por comisión,
  con fila de totales y la línea «Ventas sin vendedor asignado».
- **PDF carta** (mismo estilo que la Declaración SAR, `sarPdf.ts`) y **CSV**.

## 4. Criterios de aceptación

1. Con desde = hasta = hoy, el reporte incluye las facturas de todo el día (sin el defecto del Swing).
2. Por vendedor: nº de facturas y ventas iguales a la suma de facturas ACT del período en todas las cajas.
3. Las devoluciones de esas facturas se restan y la comisión = (ventas − devoluciones) × %.
4. Las facturas anuladas no cuentan.
5. Solo aparecen vendedores con ventas; «system» va en la línea de ventas sin vendedor.
6. Total general = ventas del período (con la línea sin vendedor).
7. Exporta PDF carta y CSV; solo lo ve el rol ADMIN.
8. Validado contra el Swing con los datos de dulce: mismas ventas por vendedor en un período cerrado
   (sumando el último día que el Swing excluye).

## 5. Estimación

5 SP (API 2 · POS 3).
