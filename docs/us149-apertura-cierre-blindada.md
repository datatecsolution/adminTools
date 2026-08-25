# US-149 — Apertura de turno blindada: no envenenar `factura_inicial` cuando falla la consulta

**Origen:** incidente en venecia, 22 de agosto de 2026
**Estado:** US definida. Datos del incidente ya corregidos en producción; el fix de código está pendiente.

---

## El incidente que la origina

El cierre del turno **B** del cajero **Tienda** en venecia (22-ago-2026, 05:47–12:59) salió con
**L 13,164,241 de efectivo y L 20,531,570 de venta**, cuando la venta real del turno fue de
**L 12,597** (65 facturas entre las cajas 3 y 7).

### Cadena del fallo (confirmada con datos)

1. El servidor de venecia se **reinició a las 05:40** de ese día. El Swing del terminal quedó con
   conexiones muertas en el pool.
2. En la apertura del turno (05:47:42), `CierreFacturacionDao.buscarPorCajaUsuario` lanzó una
   `SQLException` en la consulta de la **caja 3** (primera del loop). El DAO **muestra un diálogo de
   error y devuelve `null` igual** — indistinguible de "este usuario nunca ha abierto turno en esta
   caja". La consulta de la caja 7, ya con conexión recuperada, salió bien.
3. `CtlFacturarFrame.setCierre()` interpretó el `null` como primera vez e hizo
   `setNoFacturaInicio(1)` **hardcodeado** → la fila de `cierre_facturacion` quedó con
   `factura_inicial = 1` en vez de `447573`.
4. Al cerrar el turno, `FacturaDao.calcularCierre` suma por rango
   `numero_factura BETWEEN factura_inicial AND factura_final` → sumó las **167,166 facturas de toda
   la historia** de la caja 3.

La réplica de las sumas con el rango envenenado reproduce los valores del cierre **al centavo**.
El mismo patrón existe en registros de mayo-2024 (misma firma: `factura_inicial=1`).

### Corrección de datos (ya aplicada, 22-ago-2026)

- `cierre_facturacion` id 13320: `factura_inicial` 1 → 447573.
- `cierre_caja` idCierre 7175: montos recalculados con las fórmulas del Swing sobre el rango
  correcto (efectivo 3,787.00; tarjeta 8,810.00; totalventa 12,597.00; total_efectivo 6,277.00;
  isv15 814.25; isv18 163.37; total_isv15 5,427.75; total_isv18 907.63; total_excento 5,284.00).
- Backup previo verificado: `~/venecia_cierre7175_backup_20260822-135011.sql` (dump completo de
  ambas tablas, restaurable con `REPLACE INTO`).
- El arqueo físico cuadraba: esperado L 6,277 vs contado L 6,379 (+102). La caja nunca estuvo mal;
  solo el registro.

---

## Historia de usuario

> Como **cajero**, cuando abro mi turno quiero que, si el sistema no puede leer dónde quedó el
> turno anterior, la apertura **se detenga con un mensaje claro** en lugar de continuar con datos
> inventados, para que mi cierre de caja nunca salga con montos absurdos.

## Alcance

### 1. Swing (adminTools) — el fix principal

- `CierreFacturacionDao.buscarPorCajaUsuario(Caja, String)`: hoy una `SQLException` termina en
  `return null`. Debe **distinguirse el error del "no hay filas"** (propagar la excepción o devolver
  un resultado tipado), para que el caller no pueda confundirlos.
- `CtlFacturarFrame.setCierre()`:
  - Si la consulta **falla** → abortar la apertura con mensaje al cajero ("No se pudo verificar el
    turno anterior, intente de nuevo") y **no registrar nada**.
  - Si la consulta responde que **genuinamente no hay fila previa** (usuario/caja nuevos) → en vez
    del `1` hardcodeado, usar `última factura emitida por el usuario en esa caja + 1`; solo si
    tampoco hay facturas, usar 1 (ahí sí es primera vez real).
- Aplicar el mismo criterio a la otra sobrecarga `buscarPorCajaUsuario(Caja, String, int)` y al
  camino "reparador" de `CierreCajaService.verificarCierrePendiente` (misma familia de inserciones).

### 2. API (admintools, Desktop) — mismo bug latente

- `CierreCajaService` (apertura, commit a9a6dd7):
  `findFirstByUsuarioAndCodigoCajaOrderByIdDesc(...).map(prev -> prev.getFacturaFinal() + 1).orElse(1)`
  tiene el mismo fallback ciego a `1`. Cambiar el `orElse(1)` por el mismo criterio: última factura
  del usuario en esa caja + 1, y 1 solo si no existe ninguna.

### 3. Red de seguridad en el cierre (ambos)

- Al cerrar, si `factura_inicial` de alguna caja es `<= 1` **y** existen facturas anteriores al
  turno para ese usuario/caja, avisar y no cerrar en automático (evita que un rango envenenado
  vuelva a sumar la historia completa aunque la apertura se haya colado).

## Criterios de aceptación

1. Con la BD caída/inaccesible al momento de abrir turno, la apertura falla con mensaje claro y no
   inserta filas en `cierre_caja` ni `cierre_facturacion`.
2. Usuario/caja genuinamente nuevos: la apertura registra `factura_inicial = última factura + 1`
   (o 1 si no hay ninguna), nunca 1 con historia previa.
3. Un cierre cuyo rango abarcaría facturas de turnos anteriores no se completa en silencio.
4. El flujo normal de apertura/cierre multi-caja no cambia (regresión: suite del Swing y de la API
   en verde).

## Pruebas

- Unitarias en el Swing para el cálculo de `factura_inicial` (fila previa presente / ausente / error
  de conexión simulado).
- Unitarias en la API para el reemplazo del `orElse(1)`.
- Manual: apagar MySQL local, intentar abrir turno en el Swing → debe abortar con mensaje; encender
  y reintentar → apertura normal con el rango correcto.

## Notas de despliegue

- Solo código; **sin migración de BD**.
- Clientes con el patrón de riesgo (servidor que se apaga/reinicia con terminales encendidas):
  venecia confirmado; revisar el resto al desplegar.
