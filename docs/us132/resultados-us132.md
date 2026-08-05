# US-132 — resultados de verificación

**Fecha:** 2026-08-05 · **Alcance:** V44 aplicada y probada SOLO en local;
Sharon consultada en modo lectura. Apilada sobre US-131 (V43).

## Por qué existe

US-131 optimizó `f_existencia_y_ordenes`, pero **el Swing no la usa**: todo
su cálculo de stock pasa por `f_can_saldo_kardex` — incrustada POR FILA en
las consultas de listado de `ArticuloDao` (líneas 50/74) y en
`getDisponible`/`getDisponibleVenta`/`getExistencia`. V44 le aplica la misma
cirugía: saldo por PK sobre `existencia_articulo_bodega` + fallback al paseo
del kardex.

## Matiz de contrato (documentado en la V44)

La vieja devolvía **NULL** para una ficha sin saldo tipo 3; como el backfill
de V18 creó fila con 0 para todas las fichas, la nueva devuelve 0.00 en ese
caso. Auditado: ningún caller distingue NULL de 0 (Java `getDouble()` ya
coercionaba; los SQL envuelven en `IFNULL`). El fallback conserva el NULL
donde la tabla no alcanza (paridad exacta).

## Arnés local (`verificacion-us132.sql`, BD con V43+V44)

| Sección | Resultado |
|---|---|
| [A] IFNULL(función,0) vs IFNULL(paseo,0), TODAS las fichas | **0 diferencias** |
| [C1] Sin fila ni kardex → NULL (contrato histórico) | OK |
| [C2] Sin fila, con kardex → fallback devuelve 88.00 | OK |
| [C3] Con fila → manda la tabla (61.00) | OK |
| [C4] Cero legítimo en la tabla no dispara fallback | OK |
| [E] `articulo_view` + V43 siguen coherentes (35.351 artículos) | OK |

## Benchmark en Sharon (solo lectura)

| Medición | Vieja | Nueva | Mejora |
|---|---|---|---|
| Suma sobre 5.141 fichas | 7,59 s | **0,011 s** | ~**690×** |
| Suma de control | 712.441,96 | **712.441,96** | idéntica |
| Listado estilo Swing: 500 artículos, función por fila | 1,06 s | ~despreciable | — |

La mejora es mayor que en US-131 porque esta función es saldo puro (sin
subconsulta de reservas): la tabla materializada la reduce a un join.

**Dónde pega:** cada pantalla de catálogo/búsqueda del Swing (facturación,
artículos, órdenes) paga hoy ~2 ms por fila mostrada contra el kardex de
1.6M; con V44 ese costo desaparece. Es la optimización que el Swing sí
recibe.

## Estado de la BD local

V43 + V44 aplicadas a mano (idempotentes); Flyway las registra en el
próximo arranque del Swing.
