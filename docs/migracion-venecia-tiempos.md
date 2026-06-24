# Reporte de tiempos — Migración venecia (2026-06-24)

> Métricas reales extraídas del log del runner (`SchemaMigrator`/Flyway) durante
> la migración en producción. Complementa
> [`migracion-venecia-analisis.md`](./migracion-venecia-analisis.md).

## Resumen ejecutivo
| Métrica | Valor |
|---|---|
| **Tiempo total de migración (runner)** | **838.9 s ≈ 13 min 59 s** |
| Wall-clock (primer→último evento) | 836 s |
| BDs migradas | 1 común + 4 cajas activas |
| Migraciones aplicadas | 30 (común) + 15 (cajas) = **45** |
| Errores | **0** |
| Tamaño total de la BD | ~290 MB |
| Servidor | MySQL 8.0.42 — Ubuntu 20.04 — `192.168.88.251` |

## Desglose por base de datos
| BD | Migraciones | Tiempo | % del total |
|---|---|---|---|
| `admin_tools` (común) | 30 | **9 m 45 s** (585.4 s) | **70 %** |
| `admin_tools_caja_3` | 3 | 1 m 25 s (85.5 s) | 10 % |
| `admin_tools_caja_2` | 6 | 1 m 13 s (73.3 s) | 9 % |
| `admin_tools_caja_7` | 3 | 42 s (41.9 s) | 5 % |
| `admin_tools_caja_6` | 3 | 40 s (39.7 s) | 5 % |
| **Cajas activas (subtotal)** | 15 | **4 m 01 s** (240.5 s) | 30 % |

La **común dominó (70 %)**; las 4 cajas juntas solo ~4 min.

## Top migraciones más lentas
| BD | Versión | Tiempo | Qué hace |
|---|---|---|---|
| común | **V10** | **112 s** | índices compuestos (`movimiento_kardex` 3.26M filas) |
| común | **V21** | **101 s** | procedures + `articulo_view` + estado→boolean |
| común | V3 | 86 s | alter columns (migración Java) |
| común | V22 | 69 s | fix drifts compras |
| común | V26 | 50 s | fix drifts devoluciones |
| caja_3 | V5 | 48 s | índices por fecha |
| común | V18 | 46 s | backfill `existencia_articulo_bodega` (4.620 filas) |
| caja_2 | **V7** | **38 s** | conversión decimal (752K líneas) |
| caja_3 | V7 | 36 s | conversión decimal (439K líneas) |
| caja_6 | V7 | 27 s | conversión decimal |
| caja_7 | V7 | 25 s | conversión decimal |

## Observaciones
- **El cuello de botella NO fue la conversión decimal** (V7: 25–38 s por caja),
  sino las migraciones de la **común**: índices (V10), procedures/vistas (V21),
  y los `alter`/drifts (V3, V22, V26). El backfill V18 tardó 46 s.
- **Factor servidor**: las mismas operaciones en un M2 Pro local tardaron
  **segundos** (V10 = 3.4 s, V7 caja_2 = 4 s). En el server del cliente fueron
  **~25–30× más lentas** → el CPU/IO del cliente es el limitante, no el volumen.

## Ventana operativa completa (downtime)
| Fase | Tiempo aprox. |
|---|---|
| Backup (290 MB, 8 BDs) | ~1 min |
| Chequeo de ventana (0 conexiones) | segundos |
| **Migración (runner)** | **~14 min** |
| Verificación (7 checks) | ~1 min |
| **Downtime total (sin facturar)** | **~16 min** |

## Conclusión para próximos clientes
Sobre un servidor modesto, presupuestá **~15–20 min de ventana** aunque la BD sea
chica (≤300 MB): el tiempo lo manda el **CPU/IO del servidor**, no el volumen de
datos. La conversión decimal (V7) es barata; **los índices y procedures de la
común (V10, V21, V3, V22, V26, V18) son lo más pesado**. Medí el ensayo con datos
si querés afinar la estimación por cliente.
