# Herramientas del ensayo de Sharon (2026-10-03)

Se usaron para ensayar en local, **sobre copias** de la BD de producción, la migración V49–V57 y la actualización de la app de pedidos. Ninguna se corre contra producción.

| Archivo | Para qué |
|---|---|
| `huella.sh <puerto>` | Conteo exacto de filas por tabla de las BDs `admin_tools*` de una MySQL local, para comparar antes y después |
| `comparar_app.py` | Repite las rutas que usa la app de pedidos (login, refresh, productos, clientes, pedidos de hoy, guardar y borrar) contra dos APIs (copia V48 en :18081 y copia V57 en :18080) y compara las respuestas. Requiere `ENS_APP_USER` y `ENS_APP_PASS` (clave de prueba puesta solo en las copias) |
| `servidor_app.py <build>` | Sirve el build de la app como el nginx de producción (estáticos, SPA fallback, `index.html` sin caché) y reenvía `/admin_tools/api` a la API local en :18080. Registra cada llamada |
| `medir_descuento.sql` | Clasifica las líneas de pedidos de la app contra los precios de lista (lista / −1 % / −2 % / recargo / otro). Crea la tabla temporal `m` |
| `medir_por_vendedor.sql` | Desglose por vendedor y patrones de pedidos con −1 %. Correr después de `medir_descuento.sql`, en la misma sesión |

Resultados: `../runbook-sharon-v57.md` §3, `../runbook-app-pedidos-88d421f.md` §2 y `../medicion-descuentos-app-pedidos.md`.
