# Medición: descuentos en la app de pedidos de Sharon

> 2026-10-03. Medido sobre una **copia local** de la BD de producción de Sharon (volcado del 2026-10-03 a las 14:21), sin tocar producción. Surgió del ensayo de la actualización de la app (`runbook-app-pedidos-88d421f.md`).

## 1. Por qué se midió

En la app de pedidos, al tocar una línea se abre el diálogo «DESCUENTO PARA…» (`src/componets/ItemList.jsx`):

| Opción | Efecto en el precio |
|---|---|
| +14 % … +1 % | Recargo (sube) |
| 0 % | Sin cambio |
| **−1 %** ← preseleccionada (`useState(1)`) | Baja 1 % |
| −2 % | Baja 2 % |

Si el vendedor pulsa **ACEPTAR sin cambiar la lista**, se aplica el −1 %. El precio se **redondea a entero** a propósito (decisión de negocio documentada en el código: sin centavos, para el cobro en efectivo). Ejemplo: 168 × 0,99 = 166,32 → **166**.

Funciona así desde la primera versión (2024-11). La actualización `47a62e5 → 88d421f` **no lo cambia**; solo agrega el permiso de US-202, que la API de Sharon todavía no envía.

**Duda:** ¿se están dando descuentos del 1 % sin querer?

## 2. Método

- **Pedidos de la app:** los que tienen `client_ref` (desde US-150, 2026-09-05), excluyendo el pedido del ensayo.
- Cada línea (`detalle_factura_temp.precio`) se compara con los precios de lista **actuales** del artículo (códigos 1, 2, 3 y 5; sin el costo) y se clasifica en este orden:
  1. igual a un precio de lista;
  2. **−1 %**: `ROUND(lista × 0,99)`;
  3. **−2 %**: `ROUND(lista × 0,98)`;
  4. recargo de +1 % a +14 %;
  5. otro.
- **Límites:**
  - Si un precio de lista cambió en el período, la línea cae en «otro».
  - En productos baratos, el −1 % redondeado puede dar el mismo precio de lista y no se detecta.
- SQL en `ensayo/medir_descuento.sql` y `ensayo/medir_por_vendedor.sql`. Se corren sobre una copia de la BD, nunca sobre producción, porque crean una tabla temporal.

## 3. Resultados (2026-09-05 → 2026-10-03: 3.757 pedidos, 21.502 líneas)

| Precio de la línea | Líneas | % | Pedidos | Monto descontado |
|---|---|---|---|---|
| Precio de lista | 15.118 | 70 % | 3.243 | — |
| **−2 %** (se elige a mano) | **3.845** | **18 %** | 1.667 | **L 29.546,98** |
| **−1 %** (preseleccionado) | **974** | **4,5 %** | 669 | **L 4.544,49** |
| Recargo +1 % … +14 % | 623 | 3 % | 489 | — |
| Otro | 942 | 4 % | 106 | — |

### Por vendedor

| Vendedor | Líneas | −1 % | −2 % | Recargo | % de líneas con −1 % | L −1 % | L −2 % |
|---|---|---|---|---|---|---|---|
| ELMER22GUTI | 2.273 | 233 | 1.277 | 93 | 10,3 % | 1.042,00 | 8.725,00 |
| FIGUEROA25NORLAN | 2.981 | 215 | 437 | 62 | 7,2 % | 978,50 | 3.645,00 |
| ANGELO | 1.314 | 117 | 260 | 43 | 8,9 % | 419,49 | 1.460,00 |
| LUIS21AVILA | 2.798 | 111 | 607 | 37 | 4,0 % | 581,00 | 3.642,98 |
| DORIANM | 2.844 | 89 | 226 | 192 | 3,1 % | 481,50 | 2.173,00 |
| CARLOS22ALFRE | 1.735 | 53 | 328 | 27 | 3,1 % | 285,00 | 2.580,00 |
| SERRANOALE | 1.506 | 49 | 149 | 54 | 3,3 % | 154,00 | 1.714,00 |
| WACAESTRA | 800 | 44 | 277 | 12 | 5,5 % | 303,00 | 2.677,00 |
| GUSTAVOM | 1.868 | 31 | 172 | 61 | 1,7 % | 164,00 | 1.513,00 |
| SELVINPER | 610 | 29 | 103 | 11 | 4,8 % | 128,00 | 1.215,00 |
| OSCARCHINO | 834 | 3 | 4 | 26 | 0,4 % | 8,00 | 36,00 |
| LORENZOFER | 122 | 0 | 2 | 1 | 0 % | — | 70,00 |
| TOMASROATAN | 517 | 0 | 3 | 1 | 0 % | — | 96,00 |
| TOMASZ | 538 | 0 | 0 | 1 | 0 % | — | — |
| MOISESV | 45 | 0 | 0 | 0 | 0 % | — | — |
| GABRIEL | 717 | 0 | 0 | 2 | 0 % | — | — (vende a costo, ver §5) |

### Pedidos con −1 %

| Patrón | Pedidos | Líneas con −1 % |
|---|---|---|
| El pedido también tiene líneas con −2 % | 389 | 612 |
| Algunas líneas con −1 %, ninguna con −2 % | 223 | 280 |
| Todas las líneas del pedido con −1 % | 57 | 82 |

## 4. Lectura

- **El −1 % parece mayormente intencional:**
  - Lo usan los mismos vendedores que dan −2 %.
  - Los vendedores que no descuentan casi no tienen −1 % (0 a 3 líneas). Si aceptar el diálogo sin querer fuera frecuente, aparecería en todos.
  - 389 de 669 pedidos con −1 % también tienen −2 %: el vendedor elige línea por línea.
  - Es posible que se use el −1 % preseleccionado **como atajo** (tocar la línea y ACEPTAR).
- **Peor caso** si todo el −1 % fuera accidental: **L 4.544 en 4 semanas**. Lo realista es bastante menos.
- **Lo que sí muestran los datos:** los vendedores de Sharon **descuentan con frecuencia y a propósito**, ~L 34.000 en 4 semanas entre −1 % y −2 %. Es una cuestión de política comercial, no de la app.

**Opciones (decisión de Sharon):**
1. Abrir el diálogo en **0 %**. Es un cambio chico en la app, pero quita el atajo a quien lo use: consultarlo con los vendedores.
2. Restringir quién puede descontar: permiso de US-202 por usuario. Requiere actualizar la API de Sharon (hoy en `481b4b0`).
3. Dejarlo como está.

## 5. Hallazgo aparte: usuario que pide a precio de costo

| | |
|---|---|
| Usuario | `GABRIEL` («DISTRIBUIDORA MYM», `tipo_permiso` 2) |
| Precios asignados en `usuarios_precios` | **solo el 4, «Costos»** |
| Pedidos de la app (5-sep → 30-sep) | **11**, todos al cliente 1972 **Distribuidora M Y M**; ya facturados |
| Monto | **L 187.150,94 a costo**; esos mismos productos a precio Público serían **L 228.819,00** |
| Líneas | 685 de 717 coinciden con el precio de costo |

Puede ser intencional, por ejemplo una empresa relacionada que compra a costo. **Confirmar con Sharon** que esa asignación es correcta.
