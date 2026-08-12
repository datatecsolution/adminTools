# Facturas con diferencia entre el total y sus productos

**Fecha del informe:** 12 de agosto de 2026
**Detectado en:** caja CAJA 2 y caja CAJA_SAR
**Período afectado:** del 31 de julio al 11 de agosto de 2026
**Estado:** causa identificada y corregida en el sistema. Pendiente: decidir qué hacer con las facturas ya emitidas.

---

## Qué pasó, en una línea

Once facturas se emitieron con un total mayor a la suma de los productos que aparecen en ellas, porque un producto se quedó sin existencia entre el momento en que el vendedor levantó el pedido y el momento en que el cajero lo cobró.

---

## Cómo ocurre, paso a paso

Tomamos como ejemplo la factura **36114**, del 11 de agosto, para el cliente PUL. IVET.

### 1. El vendedor levanta el pedido (9:51 de la mañana)

El vendedor LUIS registra un pedido con cuatro productos:

| Producto | Valor |
|---|---|
| Caja Alka Seltzer Azul | L 250.00 |
| Jabón Doña Blanca | L 120.00 |
| Ego Pana Atracción | L 78.00 |
| Paq Ego Ristra | L 98.00 |
| **Total del pedido** | **L 546.00** |

En ese momento había existencia de todos los productos. El pedido queda guardado, completo y correcto.

### 2. Durante la mañana, el producto se agota

Sin relación con este pedido, salen tres requisiciones de la bodega que se llevan las últimas unidades del Alka Seltzer:

```
salida de 11 unidades  →  quedan 17
salida de  1 unidad    →  quedan 16
salida de 16 unidades  →  quedan  0
```

El pedido del vendedor sigue en espera, con un producto que ya no está disponible.

### 3. El cajero cobra el pedido (11:35 de la mañana)

El cajero factura el pedido. El sistema:

1. Calcula el total tomándolo **del pedido original**: L 546.00.
2. Guarda la cabecera de la factura con ese total.
3. Va agregando los productos uno por uno:
   - **Alka Seltzer** → el sistema detecta que no hay existencia y **rechaza ese producto**.
   - Jabón Doña Blanca → se agrega correctamente.
   - Ego Pana → se agrega correctamente.
   - Paq Ego Ristra → se agrega correctamente.

El sistema mostraba un aviso en pantalla, pero **la factura se guardaba igual**. El cajero, facturando pedidos uno tras otro, cerraba el aviso y continuaba.

### 4. El resultado

La factura quedó así:

| | |
|---|---|
| Total impreso y cobrado | **L 546.00** |
| Suma de los productos que aparecen | **L 296.00** |
| Diferencia | **L 250.00** (el Alka Seltzer) |

Más tarde ese mismo día entró una compra de 72 unidades de Alka Seltzer. Llegó después de la venta.

---

## Las tres consecuencias

**1. La factura no cuadra ante la SAR.** Es un documento con CAI que declara un total mayor al de los productos que detalla.

**2. El cliente pagó el total completo.** Si se llevó el producto, pagó por algo que la factura no menciona. Si no se lo llevó, pagó de más.

**3. El inventario quedó descuadrado.** Ese producto salió del negocio sin registrarse como venta, así que el sistema cree que sigue en bodega. Es el tipo de diferencia que aparece recién en la toma física, meses después y sin explicación aparente.

---

## Facturas afectadas

Once facturas entre el 31 de julio y el 11 de agosto:

| Caja | Factura | Fecha | Cliente | Vendedor | Cajero | Facturado | Productos | Cobrado de más |
|---|---|---|---|---|---|---|---|---|
| CAJA 2 | 260411 | 31-jul 11:58 | ERICK RUBI CARRANZA | RONAL RAMIREZ | JAFETH2 | 43,693.00 | 43,147.00 | **L 546.00** |
| CAJA 2 | 260492 | 31-jul 14:33 | PULPERIA DIANA | CARLOS LOPEZ | JAFETH2 | 1,174.00 | 1,112.00 | **L 62.00** |
| CAJA 2 | 260700 | 03-ago 10:11 | VARIEDADES MI FE | NORLAN FIGUEROA | SURTYIYI | 535.00 | 445.00 | **L 90.00** |
| CAJA 2 | 260753 | 03-ago 17:08 | PULP SOFIA | NORLAN FIGUEROA | SURTYIYI | 1,332.00 | 1,116.00 | **L 216.00** |
| CAJA 2 | 260908 | 04-ago 14:10 | PULP RAQUEL | ALEJANDRO SERRANO | JAFETH2 | 2,476.00 | 2,076.00 | **L 400.00** |
| CAJA 2 | 261055 | 04-ago 22:14 | PUL SAN ANTONIO | CARLOS LOPEZ | JAFETH2 | 966.00 | 816.00 | **L 150.00** |
| CAJA_SAR | 35654 | 05-ago 16:51 | PUL. FRANKLIN | SELVIN PERDOMO | JAFETH2 | 2,734.00 | 2,510.00 | **L 224.00** |
| CAJA_SAR | 35778 | 06-ago 16:35 | PULP. ELEODORO | MELVIN CALLEJAS | JAFETH2 | 2,364.00 | 2,148.00 | **L 216.00** |
| CAJA_SAR | 35916 | 07-ago 16:03 | PULPERIA ALEX | CARLOS LOPEZ | JAFETH2 | 1,629.00 | 849.00 | **L 780.00** |
| CAJA_SAR | 35981 | 07-ago 17:38 | EDDY MYM | MYMEDDY | JAFETH2 | 7,057.30 | 6,957.28 | **L 100.02** |
| CAJA_SAR | 36114 | 11-ago 11:35 | PUL. IVET | LUIS AVILA | JAFETH2 | 546.00 | 296.00 | **L 250.00** |
| | | | | | | | **Total** | **L 3,034.02** |

**Aparte, la factura 36113** (CAJA_SAR, 11 de agosto 11:35, cliente **PULP ESPERANZA**, vendedor **LUIS AVILA**) es el mismo caso llevado al extremo: su pedido tenía **un solo producto**, el mismo Alka Seltzer. Al rechazarse, quedó una factura activa de **L 250.00 sin ningún producto detallado**.

### Qué muestran estos datos

- Son **ocho vendedores distintos** y **once clientes distintos**, ninguno repetido. No es un problema de un vendedor en particular ni afecta siempre al mismo cliente: es el proceso de facturación de pedidos.
- El caso de **PULPERIA ALEX** es el más desproporcionado: se facturaron L 1,629.00 sobre productos por L 849.00.
- El de **ERICK RUBI CARRANZA** es el de mayor volumen (106 productos), donde una diferencia de L 546.00 es fácil de pasar por alto al revisar.

---

## Por qué pasó ahora y no antes

El sistema incorporó el 30 de julio un control que **impide vender productos sin existencia**. Ese control funciona correctamente y es deseable: evita que se venda lo que no hay.

El problema no era el control, sino la reacción del sistema cuando el control se activaba: rechazaba el producto pero **guardaba la factura de todos modos**, con el total original.

Antes de ese control, el sistema simplemente vendía sin existencia y la factura quedaba cuadrada (aunque el inventario quedara en negativo). Por eso el problema aparece a partir del 31 de julio.

---

## Qué se corrigió

A partir de la corrección, cuando un producto del pedido ya no tiene existencia:

- **No se emite la factura.** Ni cabecera ni productos: no queda nada a medias.
- **No se cobra.**
- **El cajero recibe un aviso claro**, que explica que un producto ya no tiene existencia suficiente, que pudo agotarse desde que se levantó el pedido, y que debe revisar existencias, quitar o ajustar ese producto y volver a cobrar.

De esta forma el cajero se entera **antes** de cobrar, y decide: quitar el producto del pedido, ajustar la existencia si es un error de inventario, o esperar la reposición.

---

## Qué queda por decidir

Las once facturas ya emitidas siguen como están. Requieren dos definiciones del negocio:

1. **Corrección fiscal** — cómo regularizar los documentos ante la SAR (nota de crédito, anulación y reemisión, u otro mecanismo que indique el contador).

2. **Ajuste de inventario** — los productos que salieron sin registrarse deben descontarse del sistema para que el stock refleje la realidad. Podemos preparar el detalle exacto de qué producto y qué cantidad por cada factura.

3. **Cobros** — revisar con cada cliente si el producto faltante se entregó o no, para determinar si corresponde una devolución.

Quedamos a disposición para preparar los ajustes una vez definido el criterio.
