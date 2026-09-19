# UAT final Hito 2 — Supermercados Urbina (Samuel)  ·  US-069

Sesión de pruebas de aceptación **con el cliente operando su propio sistema**
(POS en `admintools.supermercadosurbina.com`, cajas kiosco caja1/caja2, app de
pedidos en `pedidos.supermercadosurbina.com`). Objetivo: que el cliente ejecute
cada caso, marque ✔/✘, se corrijan los ✘ (US-061) y se firme el acta.

Duración estimada: **2 h en sitio** (1 h caja + 1 h administración) o dos
sesiones remotas de 1 h. Participan: dueño/administrador, 1 cajero (Walter o
Cristian), soporte (DataTec).

## Antes de la sesión (checklist de soporte)

- [ ] caja1 y caja2 con el bundle vigente (`sudo systemctl restart pos-kiosk`
      en caja1; caja2 ya recargada el 2026-09-19).
- [ ] Walter con empleado válido (Usuarios → Empleado), para que pueda guardar
      pedidos (US-178).
- [ ] Turnos abiertos conocidos (Cierres de caja): quién tiene turno abierto y
      con qué apertura.
- [ ] Ticketera, báscula y etiquetera de cada caja funcionando (prueba de
      impresión desde Configuración).
- [ ] Respaldo de la BD de Samuel del día (`~/samuel/backups/`).
- [ ] Vigilante de contenedores activo (correo si algo se cae durante la sesión).

## Casos de prueba (los ejecuta el cliente)

Marcar ✔ si el resultado coincide; anotar ✘ con qué pasó. Casos con **(N)**
son funcionalidades nuevas del Hito 2.

### A. Caja — venta diaria (cajero)
| # | Caso | Resultado esperado | ✔/✘ |
|---|---|---|---|
| A1 | Abrir turno con efectivo inicial | Aparece turno abierto con la hora correcta | |
| A2 | Venta de contado con 3 productos, uno por código de barras y uno desde la parrilla (N) | Ticket impreso en la térmica; totales e ISV correctos | |
| A3 | Venta de un **producto pesado** con la báscula (N) | El peso entra solo, con decimales; precio por libra correcto | |
| A4 | Cobro en efectivo con billete mayor | El vuelto se muestra e imprime; **el cierre no cuenta el vuelto** (N, US-179) | |
| A5 | Venta con tarjeta y venta mixta | Se registran en su forma de pago | |
| A6 | Venta a crédito a un cliente gestionado | Queda en CxC del cliente; no entra a caja | |
| A7 | Buscar y seleccionar un cliente registrado (cajero) (N, US-177) | El cajero ve todos los clientes gestionados | |
| A8 | Cambiar el precio de una línea (N, US-180) | Se ven Público/Especial/Mayorista; **no aparece "Costos"** | |
| A9 | Descuento a una línea (con clave si aplica) | Total recalculado | |
| A10 | Anular una factura recién hecha | Estado anulada; stock devuelto | |
| A11 | Guardar un pedido y recuperarlo desde "Lista de órdenes" (N) | Pedido reservado y facturado después | |
| A12 | Salida y entrada de caja con comprobante | Se imprimen y aparecen en el cierre | |
| A13 | Imprimir etiquetas de un producto (N, US-162/164) | Etiqueta con EAN-13 legible por el escáner | |
| A14 | Teclado en pantalla en la caja táctil (N, US-161/165) | Aparece al tocar un campo; botón de acción dentro del teclado | |
| A15 | Cerrar turno contando el efectivo (N, US-179) | Cierre impreso con efectivo esperado = neto de ventas; descuadre real | |

### B. Administración (dueño/administrador)
| # | Caso | Resultado esperado | ✔/✘ |
|---|---|---|---|
| B1 | Dashboard del día | KPIs coinciden con lo vendido en A | |
| B2 | Cierres de caja (N, US-169): ver el cierre de A15, detalle por categoría, reimprimir carta y ticket | Números iguales al ticket | |
| B3 | Órdenes (N, US-170): ver el pedido de A11, cambiar estado, borrado lógico | Estados correctos | |
| B4 | Empleados (N, US-168): crear, editar, intentar borrar uno referenciado | El referenciado no se borra (aviso) | |
| B5 | Usuarios: cajero con "Vendedores que atiende" (N, US-173/174) | El cajero ve los pedidos de sus vendedores; supervisor ve todos | |
| B6 | Declaración SAR ventas y compras (N, US-171/172): consolidado y por caja, PDF carta | Una página, formato como el Swing | |
| B7 | Analítica: ABC, rotación, proyección de compras, ventas por categoría (N) | Datos y exportes CSV/Excel/PDF | |
| B8 | Correo programado del reporte (N, US-068) | Llega el correo a la hora | |
| B9 | Inventario: alertas de stock mínimo y toma física con decimales (N, US-175) | Conteo de pesados con 2.5 lb, acta generada | |
| B10 | Compras: registrar una compra y ver el costo/margen (admin sí ve costo) | Stock actualizado | |
| B11 | Manual en línea (N, US-054) | Se abre y se imprime | |

### C. App de pedidos (vendedor)
| # | Caso | Resultado esperado | ✔/✘ |
|---|---|---|---|
| C1 | Vendedor entra en `pedidos.supermercadosurbina.com`, ve productos con su precio asignado (N, US-155) | Lista de productos con precios | |
| C2 | Crea un pedido para un cliente de su cartera | Aparece en la caja de su cajero (B5) y se factura | |
| C3 | Pedido idempotente: enviar dos veces el mismo (N, US-150) | Un solo pedido | |

## Después de la sesión

1. **Registro de hallazgos**: cada ✘ se anota en el backlog como Bug (US-061 es
   el contenedor de horas), con prioridad: crítico (bloquea vender) → 24 h.
2. **Corrección y re-prueba** de los ✘ con el cliente (remoto).
3. **Acta de aceptación** (plantilla abajo), firmada por el cliente y DataTec.
4. **Cierre**: US-069 y US-061 → Terminado; US-078/058/166/167 → Futuro o
   siguiente hito según decida el cliente; **cobro de la cuota final ($900)**.

## Plantilla del acta de aceptación

```
ACTA DE ACEPTACIÓN — HITO 2 · Supermercados Urbina
Fecha: ____/____/2026        Lugar: ______________________

Se realizó la sesión de pruebas de aceptación (UAT) del Hito 2 del sistema
AdminTools POS con los casos del documento docs/uat-hito2-samuel.md.

Casos ejecutados: ___   Aprobados: ___   Con observaciones: ___
Observaciones pendientes (si las hay) y fecha comprometida:
  1. ____________________________________________  ____/____
  2. ____________________________________________  ____/____

El cliente declara que el sistema cumple con el alcance del Hito 2 y acepta
el entregable. Con esta firma queda habilitado el cobro de la cuota final.

Por el cliente: ______________________   Por DataTec Solution: ______________________
Nombre / cargo                           Nombre / cargo
```
