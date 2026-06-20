# Upgrade del stack de un cliente (API + app de órdenes)

Complemento del [runbook de migración de esquema](./runbook-migracion-cliente.md).
La migración de **BD** (Flyway) es independiente y ya se documentó allí; este doc
cubre el upgrade del **código desplegado**: la **API** (`admin-tools-api-v2`) y la
**app de órdenes** (`at-ordenes-ventas-v2`), que en estos clientes están **acoplados**
y deben subir **coordinados**.

## Estado de referencia — Ronal (2026-06-20)

| Componente | Versión desplegada | Repo `main`/`master` | Desfase |
|---|---|---|---|
| **BD** | **V31** (migrada hoy) | V31 | ✅ al día |
| **API** (`admin-tools-api-v2`) | `39b795d` (24-may) | mucho más nuevo (`d88f7e9`+, US-041…) | atrasada |
| **App órdenes** (`at-ordenes-ventas-v2`) | `a5a7083` (17-may) | `1d6f5d0` (8-jun) | 3 commits atrás |

**Importante — hoy el stack está SANO y CONSISTENTE:** API y frontend de órdenes
están ambos en la versión de mayo y **alineados entre sí** (ambos usan `costomer`,
ver abajo). La BD V31 la valida la API `39b795d` sin error (verificado: arranca
limpio, 0 errores de esquema/SQL, app de órdenes 200, endpoints 401). **No hay nada
urgente que actualizar.**

## El acoplamiento crítico: `costomer` → `customer` (US-022)

El commit `d1daf3c` del frontend ("Adaptar frontend a API US-022: rename
costomer→customer + endpoints borrados") cambia las llamadas del frontend de
`/costomer*` a `/customer*`. La API de Ronal (`39b795d`) **todavía expone
`/costomer`** (`CostomerCtl.java`, no `CustomerCtl`).

→ **No se puede actualizar el frontend de órdenes solo.** Si subís el frontend
nuevo (`customer`) contra la API vieja (`costomer`), la app de órdenes **se rompe**
en esos endpoints. El frontend y la API deben subir **juntos** a versiones
compatibles.

## Commits que le faltan al frontend de órdenes de Ronal

`a5a7083` (desplegado) → `1d6f5d0` (objetivo):
- `d1daf3c` (27-may) — **US-022: rename `costomer`→`customer` + endpoints borrados** (acoplado a la API).
- `668d2a5` (28-may) — merge.
- `1d6f5d0` (8-jun) — borrado de órdenes **físico** (`?fisico=true`); depende de que la API soporte el parámetro.

Archivos: `AgregarItemModal.jsx`, `BuscarAutocomplete.jsx`, `OrdenesList.jsx`, `PruebaMenu.jsx`.

---

## Procedimiento de upgrade coordinado

> Prerrequisito: la BD ya debe estar en la versión que la API objetivo espera
> (en Ronal ya está en V31). Si no, hacer primero el
> [runbook de migración](./runbook-migracion-cliente.md).

### Fase 0 — Definir versiones objetivo y compatibilidad
1. Elegir el commit objetivo de **API** (normalmente `main`) y de **app de órdenes**
   (`main`, `1d6f5d0`). Confirmar que esa API expone lo que ese frontend llama
   (`/customer*`, `?fisico=true`).
2. Confirmar que la API objetivo valida contra la BD del cliente (`ddl-auto=validate`)
   — ya verificado para V31 con la API vieja; **re-verificar con la API objetivo**
   (puede esperar columnas nuevas). Usar el **ensayo en copia efímera** del runbook
   de migración (levantar la imagen de la API objetivo contra la BD migrada y leer
   `Started AdmintoolsApplication` vs `Schema-validation`).

### Fase 1 — Ensayo (sin tocar prod)
3. Buildear la **API objetivo** y la **app de órdenes objetivo** en una red docker
   aislada, apuntando a una **copia** de la BD del cliente, y probar el flujo
   end-to-end (login, listar/crear orden, borrado físico). Esto valida el
   acoplamiento `customer` + la compatibilidad con la BD.

### Fase 2 — Backup y ventana
4. Backup en caliente (BD ya cubierto; además guardar las imágenes/commits actuales
   para rollback: `git rev-parse HEAD` de cada repo, o tag de la imagen docker).
5. Ventana de mantenimiento (la app de órdenes quedará fuera mientras se reconstruye).

### Fase 3 — Deploy coordinado
6. **API:**
   ```bash
   cd /home/ronal/admintools-api && git pull origin main
   docker compose up -d --build --force-recreate   # reconstruye admin-tools-api-v2
   docker logs -f admin-tools-api-v2   # confirmar Started AdmintoolsApplication (validate OK)
   ```
7. **App de órdenes** — ⚠️ el dir `/home/ronal/at-ordenes-ventas-v2` **NO es un repo
   git** (es copia plana). Actualizarlo con el mismo método del stack de pruebas:
   ```bash
   # desde la Mac, con el repo local en el commit objetivo:
   cd ~/Sites/at-ordenes-ventas
   git archive --format=tar HEAD | ssh ronal@<host> 'tar -xf - -C /home/ronal/at-ordenes-ventas-v2'
   # en el server, reconstruir el contenedor (compose de n8n-docker):
   ssh ronal@<host> 'cd /home/ronal/n8n-docker && docker compose up -d --build at-ordenes-ventas-v2'
   ```
   - El bundle hornea `VITE_API_BASE_URL` en build (ruta relativa vía NPM); no cambia.
   - Considerar convertir ese dir a un clone git para futuros upgrades (`git pull`).

### Fase 4 — Validación
8. Smoke: app de órdenes carga (200), login real, crear una orden de prueba,
   autocomplete de cliente (`/customer`), borrado físico. API sin errores de esquema.
9. App admin (`admintools-pos`) y Swing siguen operando (comparten la misma API/BD).

### Fase 5 — Rollback
10. API: `git checkout <commit-previo> && docker compose up -d --build --force-recreate`.
    App órdenes: restaurar la copia previa (o `git archive` del commit previo) y rebuild.
    BD: el upgrade de código no la toca; no requiere restaurar.

---

## Notas

- **Hacerlo coordinado con el resto del stack:** la API de Ronal está varios meses
  atrás de `main`. Subirla trae muchas features (US-041 facturas, etc.) además del
  rename `customer`. Conviene un upgrade único y bien ensayado, no parches sueltos.
- **`admintools-pos`** (panel admin / facturación táctil) también consume esta API;
  al subir la API verificar que la versión de POS desplegada siga compatible.
- Layout y comandos operativos del server: ver memoria `project_deploy_pdn_paralelo`.

_Referencias: `project_ronal_migracion_v16_v31`, `project_deploy_pdn_paralelo`,
`reference_related_repos`, y [runbook-migracion-cliente.md](./runbook-migracion-cliente.md)._
