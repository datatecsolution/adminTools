# Backfill de `total_letras` (bug de centavos)

Corrige el `total_letras` histórico de las facturas/recibos/cotizaciones que se
guardaron **sin los centavos** (el viejo código redondeaba el total a entero
antes de convertir a letras). Acompaña al fix de código del módulo
`NumberToLetterConverter` + los 8 callers (rama `fix/total-letras-centavos`).

## No invasivo
- **Solo** toca las filas con el bug de centavos (filtro):
  ```sql
  total <> FLOOR(total)                  -- el total tiene centavos
  AND total_letras LIKE '%LEMPIRAS%'     -- fue generado por la app
  AND total_letras NOT LIKE '%CENTAVO%'  -- pero le faltan los centavos
  ```
  Las filas con **solo** el problema de espaciado (que ya tienen sus centavos)
  **no se tocan**.
- `UPDATE` por **PK** (sin lock de tabla) en **lotes con commit cada 200** →
  el sistema **sigue facturando online**.
- Recalcula con el **mismo `NumberToLetterConverter` ya corregido**.
- **DRY-RUN por defecto**; escribe solo con `APPLY=1`.

## Tablas
`encabezado_factura` (cada caja **registrada** en `cajas` — las viejas sin
registrar NO se tocan) + `encabezado_cotizacion` / `recibo_pago` /
`recibo_pago_proveedores` (común).

## Requisitos / uso
- `java` + el fat-jar `build/libs/AdminTools-1.0.jar` **con el fix ya aplicado**
  (`./gradlew jar`).
- Credenciales por env: `BF_HOST` `BF_PORT`(=3306) `BF_USER` `BF_PASS`.

```bash
javac -cp build/libs/AdminTools-1.0.jar deploy/backfill-total-letras/BackfillTotalLetras.java -d /tmp/bf

# DRY-RUN (no escribe): cuenta filas y muestra ejemplos before→after
BF_HOST=<host> BF_USER=<user> BF_PASS=<pass> \
  java -cp "build/libs/AdminTools-1.0.jar:/tmp/bf" BackfillTotalLetras

# APLICAR (escribe, online, en lotes)
APPLY=1 BF_HOST=<host> BF_USER=<user> BF_PASS=<pass> \
  java -cp "build/libs/AdminTools-1.0.jar:/tmp/bf" BackfillTotalLetras
```

> **Orden:** desplegar primero el **jar con el fix** a las terminales (para que
> las facturas NUEVAS salgan bien) y recién después correr el backfill del
> histórico. Es idempotente y re-ejecutable; corré siempre el DRY-RUN primero.
