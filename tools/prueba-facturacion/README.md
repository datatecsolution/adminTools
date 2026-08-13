# Prueba de integración del guardado de facturas

Ejercita `FacturaDao.registrar()` contra una base real, sin levantar la interfaz gráfica.

## Por qué existe

El bug que originó US-142 (facturas de Sharon con el encabezado inflado, agosto 2026) vivía en un punto que ninguna capa de pruebas alcanzaba:

- los **tests de JUnit** cubren lógica pura, pero no tocan la base;
- las **pruebas SQL** confirman que el trigger rechaza lo que debe rechazar;
- lo que fallaba era **el pegamento**: el SQL fallaba correctamente y el código Java se tragaba el error.

Esa franja sólo se puede verificar corriendo el DAO contra una base viva. Eso hace este programa.

## Qué verifica

| Caso | Qué comprueba |
|---|---|
| **1. Factura válida** | Se guarda completa y el encabezado cuadra con la suma del detalle |
| **2. Sin disponible** | La excepción se propaga y el rollback deja la base **exactamente igual** — ni encabezado suelto |
| **3. Pedido propio** | Con todo el stock reservado por el mismo pedido, la factura igual se emite: US-144 lo libera dentro de la transacción y no se bloquea a sí mismo |

## Qué NO cubre

- **La interfaz.** El diálogo de US-143 ("Quitar y continuar" / "Revisar antes"), el recálculo del total tras quitar líneas y el flujo visual necesitan que alguien abra el Swing y lo opere.
- **Facturas a crédito.** Sólo prueba el camino de contado.
- **No corre en `gradlew build`**, a propósito: necesita una base viva y **escribe** en ella.

## Cómo se ejecuta

**1. Construir el jar**

```bash
./gradlew jar
```

**2. Apuntar la conexión a una base de PRUEBAS**

El programa usa el mismo `connection.dat` que el Swing (`~/Library/Application Support/AdminTools/` en macOS, `%APPDATA%\AdminTools\` en Windows). Respaldá el que tengas antes de cambiarlo:

```bash
cp ~/Library/Application\ Support/AdminTools/connection.dat \
   ~/Library/Application\ Support/AdminTools/connection.dat.bak
```

Si la base de pruebas está en un servidor remoto y sólo escucha en loopback, abrí un túnel:

```bash
ssh -f -N -L 3307:127.0.0.1:3307 usuario@servidor
```

y configurá el `.dat` contra `127.0.0.1:3307`.

**3. Compilar y correr**

```bash
javac -cp build/libs/AdminTools-1.0.jar tools/prueba-facturacion/PruebaFacturacion.java
java  -cp build/libs/AdminTools-1.0.jar:tools/prueba-facturacion PruebaFacturacion
```

Ajustá el mínimo de facturas exigido por la guarda si tu base de pruebas es chica:

```bash
java -Dprueba.minFacturas=100 -cp ... PruebaFacturacion
```

**4. Restaurar tu conexión**

```bash
cp ~/Library/Application\ Support/AdminTools/connection.dat.bak \
   ~/Library/Application\ Support/AdminTools/connection.dat
```

## La guarda de destino — leer esto

Antes de escribir nada, el programa consulta contra qué servidor está y **aborta** si no parece una base de pruebas.

Está por una razón concreta: **si la lectura de `connection.dat` falla, `ConexionStatic` cae a `127.0.0.1:3306` en silencio**. No hay error ni aviso; simplemente conectás a otra base. Sin la guarda ya se escribieron pruebas en la base equivocada, y por el mismo mecanismo se llegó a correr el Swing contra producción de un cliente.

Dos detalles del criterio:

- **El puerto no distingue nada.** Dentro de un contenedor, MySQL escucha en 3306 igual que una instalación local; el 3307 es sólo tu extremo del túnel. La guarda mira el *hostname* (rechaza lo que parezca una máquina de escritorio) y el volumen de facturas.
- Si tu base de pruebas es nueva y tiene pocas facturas, bajá el mínimo con `-Dprueba.minFacturas=N` en vez de quitar la guarda.

## Efectos sobre la base

Crea y borra: una factura por caso, un pedido (`999501`), una fila en `config_user_facturacion` y movimientos de kardex. Todo queda marcado como `PRUEBA_FACT` y se elimina al terminar.

**Modifica la existencia** del artículo que elige para armar cada escenario y **no la restaura**. En una base de pruebas es irrelevante; es otra razón para no apuntar esto a producción.

Si una corrida se interrumpe a la mitad, la limpieza manual es:

```sql
DELETE FROM admin_tools_caja_N.detalle_factura   WHERE numero_factura IN (...);
DELETE FROM admin_tools_caja_N.encabezado_factura WHERE observacion = 'PRUEBA_FACT';
DELETE FROM admin_tools.detalle_factura_temp     WHERE numero_factura = 999501;
DELETE FROM admin_tools.encabezado_factura_temp  WHERE numero_factura = 999501;
DELETE FROM admin_tools.config_user_facturacion  WHERE usuario = 'PRUEBA_FACT';
DELETE mk FROM admin_tools.movimiento_kardex mk
  JOIN admin_tools.detalle_movimiento_kardex dmk ON dmk.codigo_movimiento = mk.codigo_movimiento
  WHERE dmk.no_documento = 'PRUEBA_FACT';
DELETE FROM admin_tools.detalle_movimiento_kardex WHERE no_documento = 'PRUEBA_FACT';
```

## Salida esperada

```
destino: 59f973f67ed9   facturas en caja_1: 19179   (minimo exigido: 1000)
(destino confirmado)

entorno: caja=1 (admin_tools_caja_1)  bodega=1  articulo=319652

== CASO 1: factura valida (debe guardarse completa) ==
   registrar()=true   lineas=2 (esperado 2)
   encabezado=60.00   detalle=60.00
   -> OK
== CASO 2: sin disponible (no debe quedar nada) ==
   (JOptionPane no disponible en headless: esperado)
   registrar()=false   encabezados antes=19179 despues=19179
   -> OK (rollback completo)
== CASO 3: pedido propio, todo el stock reservado por el ==
   reservado por el propio pedido: 5.00 (disponible quedaria en 0)
   registrar()=true   estado del pedido=3 (3=facturado)   lineas=1
   -> OK (no se bloqueo a si mismo)

(datos de prueba eliminados)

RESULTADO: TODOS OK
```

Devuelve `0` si todo pasa, `1` si algún caso falla y `2` si abortó por la guarda de destino.
