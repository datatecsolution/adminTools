# US-131 — resultados de verificación

**Fecha:** 2026-08-03 · **Alcance:** V43 aplicada y probada SOLO en local;
Sharon consultada en modo lectura. Sin deploy a clientes.

## Auditoría previa (la premisa de la US)

- **Escritores del kardex**: todos los flujos runtime pasan por los 7 SPs
  (Swing: crear_venta/insumo/dev_venta/ajuste; API: compra, dev_compa,
  requisición entrada/salida, venta v2). El único camino directo en código
  (`KardexDao.registrarKardex`) solo crea la FICHA `articulo_kardex`, sin
  movimientos — no afecta saldos.
- **Reconciliación COMPLETA en producción (Sharon)**: 5.141 fichas
  comparadas tabla-vs-paseo del kardex → **0 mismatches, 0 filas
  faltantes**.

## Arnés local (`verificacion-us131.sql`, BD admin_tools con V43)

| Sección | Resultado |
|---|---|
| [A] Equivalencia función nueva vs cálculo viejo, TODAS las fichas | **0 diferencias** |
| [C] Fallback sin fila materializada → camina el kardex (77.00, no 0) | OK |
| [C] Con fila, manda la tabla (55.00) | OK |
| [C] Cero legítimo en la tabla NO dispara fallback | OK |
| [D] Estado 1 (activa) resta · 2 (modificada) resta | OK (50→40) |
| [D] Estados 3/4/5 NO restan | OK (50) |

## Benchmark en Sharon (solo lectura, 5.141 fichas, kardex 1.6M filas)

| Implementación | Tiempo total | Por llamada | Suma de control |
|---|---|---|---|
| V41 vigente (pasea el kardex) | **10,81 s** | ~2,1 ms | 692.328,96 |
| V43 simulada (tabla materializada) | **2,15 s** | ~0,42 ms | **692.328,96** (idéntica) |

**5× más rápida con resultado bit a bit idéntico sobre datos de
producción.** El costo restante es la subconsulta de reservas (pocas
órdenes vivas, indexada).

Dónde pega: cada búsqueda de la app de pedidos (US-130) llama la función
por producto del resultado; cada listado del Swing sobre `articulo_view`
la llama por fila. Un listado de 2.300 productos pasa de ~5 s a ~1 s de
costo de función.

## Estado de la BD local

V43 aplicada a mano para las pruebas. Es idempotente (`DROP FUNCTION IF
EXISTS` + `CREATE`): el próximo arranque del Swing la re-aplica vía
Flyway sin conflicto y registra la fila en `schema_version`.
