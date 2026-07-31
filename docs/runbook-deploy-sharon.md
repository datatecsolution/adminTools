# Runbook — Ventana de deploy Swing + API en Distribuidora Sharon

**Preparado:** 2026-07-29 · **Ejecutar SOLO con orden explícita.** Base: `docs/analisis-deploy-swing-sharon.md` (§8 = ensayo E1-E4).

## Staging listo en el servidor (`~/deploy-sharon/`)

| Pieza | Detalle |
|---|---|
| `migraciones/` | V33-V41 común (**V36 endurecida** post-E2) + V9 de caja |
| `AdminTools-1.0_sharon_8fd9dc5.jar` | Jar del Swing (master `8fd9dc5`, 49MB) |
| `admintools-api:sharon-v41` (imagen docker) | API `657a24e` — la validada en el ensayo E4 |
| `docker-compose.sharon.yml` | Igual a prod, con imagen fija + env nuevo |
| `.env-sharon-nuevo` | Env actual + `CORS_ALLOWED_ORIGINS` + `APP_PUBLIC_INVOICE_SECRET` (generado) + `APP_JWT_SECRET_NUEVO_ROTAR` (generado, aplicar en paso 5) |
| `ensayo-sharon-mysql` (127.0.0.1:3310) | BD del ensayo viva — smoke opcional del Swing por túnel antes de la ventana |

## GO / NO-GO (confirmar ANTES de arrancar)

- [ ] Orden explícita del usuario para ejecutar.
- [ ] Fuera de horario de la distribuidora; cliente avisado (pedidos ~5-10 min caídos; re-login en la app por rotación de JWT).
- [x] **DECIDIDO 2026-07-30**: `RONAL` y `MELVINC` quedan con **caja 2** (la de la mayoría de vendedores, bodega 1 — "solo manejan una bodega"). Ya la tienen asignada; solo se eliminan las extras.
- [x] **DECIDIDO 2026-07-30**: expiración de pedidos **ACTIVA a 7 días** desde el día uno (default del build — sin cambio de env). Avisar al cliente: pedidos con >7 días sin facturar se anulan solos a las 03:30.
- [ ] VPN arriba; `~/deploy-sharon/` completo (tabla anterior).

## Ventana (copy-paste, en orden)

```bash
# ===== 1. BACKUP (≈5 min) =====
cd ~/deploy-sharon && PW=$(docker exec admin-tools-api-v2 env | grep "^MYSQL_PASSWORD=" | cut -d= -f2-)
TS=$(date +%Y%m%d_%H%M%S)
mysqldump -uadmin -p"$PW" -h127.0.0.1 --single-transaction --routines --triggers --events \
  --set-gtid-purged=OFF --databases admin_tools admin_tools_caja_1 admin_tools_caja_2 \
  admin_tools_caja_3 admin_tools_caja_4 admin_tools_caja_5 admin_tools_caja_6 \
  > backup/sharon_preventana_$TS.sql && ls -lh backup/

# ===== 2. PARAR LA API v2 (pedidos queda caído desde aquí) =====
docker stop admin-tools-api-v2

# ===== 3. MIGRACIONES común V33-V41 (una a una; si una FALLA → ROLLBACK) =====
for f in migraciones/V33*.sql migraciones/V34*.sql migraciones/V35*.sql migraciones/V36*.sql \
         migraciones/V37*.sql migraciones/V38*.sql migraciones/V39*.sql migraciones/V40*.sql \
         migraciones/V41*.sql; do
  echo "== $f =="; mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools < "$f" || break
done
# Registrar en schema_version (checksum NULL: el repair() del Swing lo alinea)
for v in 33 34 35 36 37 38 39 40 41; do mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools -e \
 "INSERT INTO schema_version (installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success) \
  SELECT MAX(installed_rank)+1,$v,'sharon deploy','SQL','V$v.sql',NULL,'sharon-deploy',NOW(),0,1 FROM schema_version;"; done

# ===== 4. CAJAS: V9 ×6 + registrar =====
for i in 1 2 3 4 5 6; do
  echo "== caja_$i =="; mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools_caja_$i < migraciones/V9__trigger_venta_valida_usuario.sql
  mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools_caja_$i -e \
   "INSERT INTO schema_version (installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success) \
    SELECT MAX(installed_rank)+1,9,'sharon deploy','SQL','V9.sql',NULL,'sharon-deploy',NOW(),0,1 FROM schema_version;"
done

# ===== 5. NORMALIZAR VENDEDORES (decidido: ambos a caja 2, bodega 1) =====
mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools -e "
DELETE FROM cajas_usuarios WHERE usuario='RONAL'   AND codigo_caja <> 2;
DELETE FROM cajas_usuarios WHERE usuario='MELVINC' AND codigo_caja <> 2;
UPDATE cajas_usuarios SET por_defecto=1 WHERE usuario IN ('RONAL','MELVINC');
SELECT usuario, GROUP_CONCAT(codigo_caja) FROM cajas_usuarios WHERE usuario IN ('RONAL','MELVINC') GROUP BY usuario;"

# ===== 6. API NUEVA (rotando JWT: sesiones de la app se invalidan, re-login) =====
NUEVO=$(grep '^APP_JWT_SECRET_NUEVO_ROTAR=' .env-sharon-nuevo | cut -d= -f2-)
sed -i "s|^APP_JWT_SECRET=.*|APP_JWT_SECRET=$NUEVO|" .env-sharon-nuevo
docker rm -f admin-tools-api-v2
docker compose -f docker-compose.sharon.yml up -d
sleep 15 && docker logs admin-tools-api-v2 --since 2m 2>&1 | grep -E "Started|ERROR|Schema-validation" | tail -3
```

**Checkpoint API** (todo debe pasar antes de seguir):
```bash
curl -s -o /dev/null -w "8083 → %{http_code}\n" http://127.0.0.1:8083/admin_tools/api/company           # 401
curl -s -o /dev/null -w "dominio → %{http_code}\n" https://pedidos.distribuidorasharon.com/            # 200
# Si el dominio da 502: el npm cacheó la IP vieja → docker exec nginx-proxy-manager nginx -s reload
```
Smoke con la app de pedidos VIEJA (usuario real del cliente): login → listar → **guardar un pedido** → verificar en BD que quedó con la caja del vendedor (`SELECT numero_factura,codigo_caja,usuario FROM encabezado_factura_temp ORDER BY 1 DESC LIMIT 1;`).

```bash
# ===== 7. JAR DEL SWING =====
# Distribuir ~/deploy-sharon/AdminTools-1.0_sharon_8fd9dc5.jar a las terminales
# (renombrar reemplazando el jar actual). Arrancar UNA terminal primero:
#  - Flyway: repair alinea checksums, migrate sin pendientes (todo pre-aplicado)
#  - Venta E2E + consulta de existencias (label Disponible) + listado (columna)
```

**Smoke final**: venta en 2 cajas de bodegas distintas (caja 2→b1 y caja 3→b2) · pedido desde la app y su reserva visible (`v_reservado_por_articulo`) · facturar ese pedido desde el Swing (no choca contra su propia reserva) · `SELECT MAX(CAST(version AS UNSIGNED)) FROM schema_version;` = 41.

## Rollback (en cualquier punto)

```bash
docker rm -f admin-tools-api-v2
mysql -uadmin -p"$PW" -h127.0.0.1 -e "SET GLOBAL log_bin_trust_function_creators=1;"   # E1: sin esto el restore ABORTA
mysql -uadmin -p"$PW" -h127.0.0.1 < ~/deploy-sharon/backup/sharon_preventana_$TS.sql
cd ~/admintools-api && docker compose up -d      # imagen vieja intacta (39b795d)
curl -s -o /dev/null -w "dominio → %{http_code}\n" https://pedidos.distribuidorasharon.com/
# Terminales: siguen con el jar viejo (no distribuir el nuevo si hubo rollback)
```

## EJECUTADA 2026-07-30 — resultado

**Downtime de pedidos: 5m41s** (20:28:20 → 20:34:01). Sin rollback. Scripts en `docs/deploy-sharon/`.

| Paso | Resultado |
|---|---|
| Backup | 349MB verificado (7 BDs, 46 rutinas, marca de cierre) en 10s |
| Migraciones | V33 (0s) · **V34 103s** · V35 18s · V36 5s · **V37 96s** · V38 2s · V39 1s · V40 1s · V41 0s — todas con post-condición verificada |
| Cajas V9 ×6 | OK, trigger apunta a `crear_venta_kardex_v2` en las 6 |
| Vendedores | RONAL y MELVINC → caja 2; los 11 vendedores quedan mono-caja |
| API nueva | `admintools-api:sharon-v41`, **Started 8.1s**, validate limpio, JWT rotado |
| Verificación | schema V41, cajas V9, cero float en tablas migradas, **todos los conteos de filas idénticos al baseline** |

**Incidencias (todas de los scripts de verificación, ninguna de datos):**
1. `information_schema.statistics` devuelve **una fila por columna** del índice → el chequeo de V39 comparaba contra 1 y veía 2 (falso negativo; V39 estaba perfecta). Fix: `COUNT(DISTINCT index_name)` + el script quedó **reanudable** (salta lo ya registrado en `schema_version`).
2. Las sumas float→decimal **cambian por definición** (`518699079.1` → `518699079.08`): el float redondeaba. No es pérdida de datos (conteo idéntico); se reportan como informativas.
3. Consulta de vendedores multi-caja fallaba por tablas sin calificar (`No database selected`) y por *collation* en el JOIN. Fix: subconsulta correlacionada con nombres calificados.

**Pendiente del cliente**: smoke funcional de la app de pedidos (login → guardar pedido) y distribución del jar `~/deploy-sharon/AdminTools-1.0_sharon_8fd9dc5.jar` a las terminales.

## Post-ventana

- Verificar a la mañana siguiente el log del job de expiración (si quedó activo) y el reservado (`SELECT SUM(reservado) FROM v_reservado_por_articulo;`).
- `docker rm -f ensayo-sharon-mysql && rm -rf ~/ensayo-sharon` (limpieza del ensayo).
- Guard de sobreventa: activar por usuario cuando el cliente lo pida (`UPDATE config_user_facturacion SET facturar_sin_inventario=0 WHERE usuario='...';`).
- Actualizar memoria/backlog y, si aplica, mergear PR api#33 para que main == lo desplegado.
