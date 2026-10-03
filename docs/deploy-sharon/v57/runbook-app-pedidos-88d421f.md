# Runbook — App de pedidos de Sharon: `47a62e5` → `88d421f`

> 2026-10-03. `pedidos.distribuidorasharon.com`, contenedor `at-ordenes-ventas-v2` en el servidor de Ronal.
>
> **Restricción vigente:** el despliegue de esta app en Ronal se hace **solo con orden explícita del usuario**.

## 1. Diferencia

- **Corre:** `47a62e5` (US-150). La imagen se construyó el 2026-09-05 a las 09:11, dos minutos después del `git pull` a ese commit.
- **El clon** `~/at-ordenes-ventas-v2` ya está en `88d421f`, igual a `main` en GitHub, pero nunca se reconstruyó.
- **Alcance:** 16 commits, 11 archivos, **solo front** (`src/`). Sin cambios de BD, nginx ni Dockerfile.

| Función | ¿La API de Sharon (`481b4b0`) la soporta? | Efecto en Sharon |
|---|---|---|
| US-061 búsqueda: espera 300 ms + memoria de «sin foto» | No necesita API | Menos llamadas (hoy, una por tecla) |
| US-057 miniaturas en listados | Sí (`/image?size=thumb`) | Sharon tiene **0 fotos**: ícono gris. Un 404 por producto y por sesión |
| US-056 galería en el detalle | No (US-059 no está en esa API) | Nada visible; un 404 por detalle |
| Cliente automático si el vendedor tiene uno solo | Sí | Un paso menos para esos vendedores |
| US-187 stock informativo | No (sin `verificaInventario`) | Sin cambio: verifica stock como hoy |
| US-194 detector de versión nueva | No necesita API | Los próximos despliegues llegan solos a los celulares |
| US-202 permiso de descuento | No (sin `permiteDescuento`) | Sin cambio: permite descuento como hoy |

## 2. Ensayo local (hecho el 2026-10-03)

- Worktree de `88d421f`, `npm ci` con `package-lock.json`. Pruebas: **36/36 en verde**. Build: `main.347524a9.js`.
- Servido como en producción: estáticos, SPA fallback, `index.html` sin caché y `/admin_tools/api` reenviado a la API `481b4b0` sobre la copia de Sharon en V57.
- Recorrido de un vendedor real (`FIGUEROA25NORLAN`, clave de prueba solo en la copia), en el navegador:
  - login → `GET /sellers` 200;
  - cliente → `customers?size=2` y búsqueda de clientes, 200;
  - producto: escribir «Rexona» letra por letra = **1 búsqueda en vez de 6**;
  - 21 miniaturas con 404 → íconos grises; **al repetir la búsqueda, 0 pedidos** (memoria);
  - galería y foto del detalle con 404, sin error visible;
  - el descuento se abre como hoy;
  - **pedido guardado** (201, #111896 con 1 línea) → aparece en la lista de pedidos → eliminado (200).
- **0 errores en la API.** Todos los 404 son de foto o galería, los esperados.
- Nota: al aceptar el diálogo de descuento sin tocarlo se aplica la opción preseleccionada (168 → 166). Ya pasaba antes; no es de esta versión.

## 3. Despliegue

Toma segundos y se puede hacer de día. Un pedido abierto en un celular sigue funcionando, porque la API no cambia. Los celulares toman la versión nueva al recargar, porque `index.html` va sin caché.

```bash
cd ~/at-ordenes-ventas-v2
git fetch -q origin && git rev-parse --short HEAD          # debe ser 88d421f (= origin/main)
docker tag at-ordenes-ventas-v2-at-ordenes-ventas-v2 at-ordenes-ventas:respaldo-47a62e5   # VUELTA ATRÁS
touch ~/.docker-watch/pausa
docker compose build at-ordenes-ventas-v2
docker compose up -d --no-deps at-ordenes-ventas-v2
docker tag at-ordenes-ventas-v2-at-ordenes-ventas-v2 at-ordenes-ventas:sharon-88d421f
rm ~/.docker-watch/pausa
```

**Ojo con el clon:**
- **No usar `redeploy.sh`**: apunta al servicio `at-ordenes-ventas`, que no existe; el compose lo llama `at-ordenes-ventas-v2`.
- El `docker-compose.yml` del clon tiene cambios locales (nombre `-v2` y red `n8n-docker_default`). Se respetan.

**Verificar:**
- `curl -s https://pedidos.distribuidorasharon.com/ | grep -o 'main\.[a-z0-9]*\.js'` debe dar el bundle nuevo, distinto de `main.3ac2837e.js`;
- el encabezado `Cache-Control: no-store` en `/`;
- `/admin_tools/api/orders/today` sin sesión debe dar 401;
- el contenedor `Up`.

El proxy resuelve la app por nombre (`set $server`), así que no hay caché de IP.

## 4. Vigilancia

`bash ~/deploy-sharon/v57/vigilar.sh <HH:MM UTC del despliegue>` a los 15 minutos y a las 2 horas:
- **404 esperados:** `/products/{id}/image?size=thumb`, `/products/{id}/image` y `/products/{id}/gallery`.
- **Investigar** cualquier otro 4xx o 5xx.
- **Confirmar** que siguen entrando `orders/save` 201.

## 5. Vuelta atrás

```bash
docker tag at-ordenes-ventas:respaldo-47a62e5 at-ordenes-ventas-v2-at-ordenes-ventas-v2
docker compose up -d --no-deps --force-recreate at-ordenes-ventas-v2
```

Toma segundos y no toca datos. Los celulares toman la versión anterior al recargar.
