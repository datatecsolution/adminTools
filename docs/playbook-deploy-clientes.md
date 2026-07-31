# Playbook de despliegue en clientes (Swing + API + fronts)

**Origen:** destilado del despliegue en Distribuidora Sharon (2026-07-30/31), el primero que actualizó los tres componentes a la vez. Cada regla acá tiene detrás un incidente real. Ver `runbook-deploy-sharon.md` (ejecución) y `analisis-deploy-swing-sharon.md` (análisis previo).

**Aplica a:** venecia, dulce y cualquier cliente que actualice Swing y/o API cuando su esquema esté atrasado.

---

## Las 4 reglas de oro

1. **El contrato se valida contra el BUILD EN PRODUCCIÓN, nunca contra el repo del front.** Fue el error más caro del día: el código fuente de la app de pedidos ya había migrado a los endpoints nuevos, pero el build desplegado seguía llamando a los viejos. Costó dos incidentes con vendedores bloqueados (US-123 y US-124).
2. **Ensayar las migraciones con los DATOS REALES del cliente**, no con la BD de desarrollo. Una migración que pasa en local puede abortar a mitad con los datos del cliente (US-036/E2: 60 precios NULL).
3. **Verificar las suposiciones de configuración con una consulta**, no de memoria. Afirmé dos veces que el guard de sobreventa estaba pasivo; 57 de 58 usuarios lo tenían activo.
4. **Después de desplegar, barrer TODOS los 4xx/5xx del proxy**, no arreglar de a uno. Un solo barrido detecta los tres frentes rotos; ir de a uno son tres incidentes en cadena.

---

## Fase 0 — Inventario del cliente (días antes)

Levantar y anotar, con evidencia:

| Qué | Cómo | Por qué |
|---|---|---|
| Versión de esquema | `SELECT MAX(CAST(version AS UNSIGNED)) FROM schema_version` en común **y en cada caja** | Define el gap real de migraciones |
| Versión de la API | commit del clon + imagen docker corriendo | Decide si debe actualizarse en la misma ventana |
| **Build de cada front** | Ver los endpoints que realmente llama (logs del proxy) y/o `grep` sobre el bundle servido | **Regla de oro #1** |
| Privilegios MySQL | `SHOW GRANTS` del usuario que aplicará migraciones | CREATE ROUTINE/VIEW/TRIGGER son imprescindibles |
| Recursos | `df -h`, `free -m` | Los ALTER de tablas grandes necesitan disco temporal |
| Topología | cajas → bodegas, vendedores → cajas, roles y flags por usuario | Los cambios de comportamiento dependen de esto |
| Flags de negocio | `config_user_facturacion.facturar_sin_inventario` por usuario | **Regla de oro #3** |

**Detectar el gap de contrato** (lo que evita los incidentes post-deploy):

```bash
# 1. Qué endpoints llama el front DESPLEGADO
docker exec nginx-proxy-manager sh -c "cat /data/logs/proxy-host-N_access.log" \
  | awk -F\" '{split($0,a," "); print a[4], $2}' | sort -u

# 2. Qué nombres de campo usa su bundle (typos históricos incluidos)
docker exec <front> sh -c 'grep -o "costomer[A-Za-z]*" /usr/share/nginx/html/static/js/main.*.js | sort -u'

# 3. Comparar los DTO del repo con los de la versión que corre en el cliente
git diff <commit-en-prod>..main -- src/.../domain/Order.java src/.../domain/Product.java
```

---

## Fase 1 — Ensayo con datos reales (obligatorio)

MySQL descartable en el propio servidor + dump fresco del cliente:

```bash
docker run -d --name ensayo-mysql -e MYSQL_ROOT_PASSWORD=... -p 127.0.0.1:3310:3306 mysql:8.0
mysqldump --single-transaction --routines --triggers --events --set-gtid-purged=OFF \
  --databases <las 7 BDs> | sed -E 's/DEFINER=`[^`]+`@`[^`]+`//g' > dump.sql
```

Qué probar y qué buscar:

1. **Restore del dump** → si aborta con `ERROR 1418`, hace falta `SET GLOBAL log_bin_trust_function_creators=1`. **Esto también aplica al rollback de la ventana.**
2. **Migraciones una a una** → cualquier `ERROR 1265 Data truncated` significa datos que violan la restricción nueva (NULLs, valores fuera de rango). Se corrige **dentro de la propia migración** con un `UPDATE` previo; el `repair()` del arranque realinea el checksum en los clientes que ya la aplicaron.
3. **API vieja contra el esquema migrado** → si muere con `Schema-validation: wrong column type`, actualizar la API es **obligatorio en la misma ventana**.
4. **API nueva contra los datos reales** → verifica `validate` limpio y detecta variables de entorno faltantes (a nosotros nos faltó `CORS_ALLOWED_ORIGINS`).
5. **Perf de consultas nuevas** con el volumen real (no con la BD de dev, que es 100× menor).

> Bajar el MySQL del ensayo **antes** de la ventana: compite por RAM en servidores ajustados.

---

## Fase 2 — Preparación (todo listo para copy-paste)

- **Imágenes docker etiquetadas por versión** (`cliente-v41`, `v42`…) y **etiquetar también la actual como rollback** antes de tocar nada. Volver atrás es cambiar una línea.
- **Construir en un clon con credenciales que funcionen** (deploy keys SSH) y que producción **solo corra la imagen**. Evita que un `git pull` falle en plena ventana.
- **`.env` nuevo derivado del ARCHIVO real** (`sudo cat`), nunca de `docker exec env` filtrado por prefijos: así perdimos 4 orígenes de CORS. Y **siempre correr el diff de nombres**:
  ```bash
  diff <(grep -oE "^[A-Z_]+=" .env.viejo | sort -u) <(grep -oE "^[A-Z_]+=" .env.nuevo | sort -u)
  ```
- **Scripts instrumentados** (versionados en `docs/deploy-sharon/`, reutilizables):
  `00-preflight` (valida staging + captura baseline) · `01-backup` (crea **y verifica**) · `02-migrar` (una a una, con post-condición, **reanudable**) · `03-verificar` (integridad vs baseline) · `99-rollback`.
- **Auto-rollback** en el script de actualización de imagen: si no arranca en 90s, vuelve sola a la anterior.

---

## Fase 3 — La ventana

Orden probado (Sharon: **5m41s** de downtime):

1. Backup **verificado** (tamaño, marca `Dump completed`, todas las BDs, conteo de rutinas). Un backup no verificado no es un backup.
2. Parar la API (empieza el downtime).
3. Migraciones **una a una**, con post-condición y aborto ante el primer fallo.
4. Migraciones de cajas (×N).
5. Ajustes de datos acordados (normalizaciones, limpiezas).
6. API nueva + rotación de secretos → **avisar que todos deben volver a iniciar sesión**.
7. Checkpoint: API local, dominio, y **smoke con el front VIEJO** antes de seguir.
8. Distribuir el jar del Swing: **primero UNA terminal**, verificar, y recién después el resto.

**Tiempos de referencia** (1.6M filas de kardex): los ALTER decimal pesados ~100s cada uno, el resto por debajo de 20s.

---

## Fase 4 — Post-deploy (la fase que casi nos come)

**Barrido obligatorio a los 15 minutos y otra vez a las 2 horas:**

```bash
docker exec nginx-proxy-manager sh -c "cat /data/logs/proxy-host-N_access.log" \
 | awk '/<fecha de hoy>/' | awk -F\" '{split($0,a," "); print a[4], $2}' \
 | sort | uniq -c | sort -rn | head -20
```

Todo 404/500/409 es un frente roto. En Sharon aparecieron tres:

| Síntoma | Causa | Lección |
|---|---|---|
| 404 en `/products/despriciouser`, `/costomers/name` | endpoints borrados que el build viejo seguía usando | Regla de oro #1 |
| 500 en `/orders/save` — `codigo_cliente cannot be null` | el DTO renombró `costomerId` → `customerId` | Los **renombres de campos** rompen igual que borrar endpoints |
| 409 esporádicos | el guard de stock rechazando correctamente | Distinguir **error** de **comportamiento nuevo** |

**Puente de compatibilidad** (patrón que funcionó): una sola clase con los endpoints legacy delegando en los servicios actuales, más accesores `@JsonProperty` en el DTO para aceptar y emitir los nombres viejos. Marcado `@Deprecated` y aislado para borrarlo cuando el front se actualice.

**Además del proxy, revisar:**
- Logs de la API (con logging en INFO **no** se registran las peticiones — el proxy es la fuente de verdad).
- Logs del Swing en la terminal del cliente (`%APPDATA%\AdminTools\logs`): ahí apareció un bug preexistente (US-126) que solo se manifiesta con ciertos datos.
- Volumen de operación comparado con días previos: *"hoy 2 pedidos vs 150 diarios"* fue lo que destapó que los vendedores estaban bloqueados.
- Stock negativo: distinguir **histórico** de **nuevo** por `fecha_actualizacion`. En Sharon los 64 negativos eran todos anteriores.

---

## Catálogo de gotchas técnicos

**MySQL / SQL**
- `information_schema.statistics` devuelve **una fila por columna** del índice → contar con `COUNT(DISTINCT index_name)`.
- Las sumas `float → DECIMAL` **cambian por definición** (el float redondeaba). No es pérdida de datos: comparar **conteos de filas**, no sumas.
- Restaurar dumps con funciones exige `log_bin_trust_function_creators=1`.
- `UPDATE ... SET col = NULL` sobre columna `NOT NULL` **solo falla si hay filas que coincidan** → bugs latentes por años (US-126). Usar `SET col = DEFAULT`, que es portable.
- Cuidado con **collation** al unir `usuario` (utf8mb3 vs utf8mb4) y con tablas **sin calificar** cuando la conexión no seleccionó BD.

**Java / Spring**
- Query nativa multi-columna: `Optional<Object[]>` anida la fila y revienta en runtime → usar `List<Object[]>`.
- Compatibilidad de nombres JSON: par getter/setter con el **mismo** `@JsonProperty` (Jackson lo trata como una propiedad; mezclar `@JsonAlias` con un getter homónimo genera ambigüedad).
- Un fallo de mapeo de rutas revienta **al arrancar**, no en caliente: un arranque limpio ya es evidencia de que las rutas quedaron registradas.

**Swing**
- Los renderers leen columnas **ocultas** del modelo por índice fijo: al insertar una columna, `grep getValueAt(row, N)` en `view/rendes/`.
- Los DAO se instancian por pantalla con estado parcial (`myBodega` puede venir null) → todo acceso debe tolerarlo.
- Un `NullPointerException` no lo captura el `catch (SQLException)` de los listados: rompe la pantalla **en silencio**.
- Commits que normalizan CRLF→LF inflan el diff: verificar cambios reales con `diff <(git show A:f | tr -d '\r') <(git show B:f | tr -d '\r')`.

**Docker / infra**
- `docker logs` **muere con cada recreate** → la fuente persistente son los logs del proxy.
- Un contenedor lanzado con `docker run` sin `restart: unless-stopped` no vuelve tras un reinicio del daemon.
- Tras recrear la API, el proxy puede cachear la IP vieja (502) → `docker exec nginx-proxy-manager nginx -s reload`.

---

## Checklist de una página

**Antes (días)**
- [ ] Esquema (común + cajas), versión de API, **endpoints y campos del build desplegado**
- [ ] Grants, disco, RAM, topología cajas/bodegas/vendedores, flags por usuario
- [ ] Ensayo con dump real: restore, migraciones, API vieja, API nueva, perf
- [ ] Decisiones de negocio acordadas con el cliente (por escrito)

**Preparación**
- [ ] Imágenes etiquetadas (nueva **y rollback**), jar construido, scripts staged
- [ ] `.env` desde el archivo real + **diff de nombres de variables**
- [ ] Preflight en verde y baseline capturado

**Ventana**
- [ ] Backup verificado → migraciones instrumentadas → cajas → datos → API → checkpoint → jar en UNA terminal

**Después**
- [ ] Barrido de 4xx/5xx a los 15 min y a las 2 h
- [ ] Volumen de operación vs días previos
- [ ] Logs del Swing del cliente
- [ ] Job de expiración a la mañana siguiente
- [ ] Bajar el MySQL del ensayo y documentar lo aprendido acá
