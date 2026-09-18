# Pendiente: llevar la BD de producción de Ronal a V53 (alerta de inventario, US-176)

Estado al 2026-09-18: **NO ejecutado**. V53 (`v_existencia_alert` sobre el saldo
materializado) está mergeada en master (adminTools#66) y registrada como US-176.
Falta aplicarla en la BD de producción de Ronal (10.10.0.1, MySQL nativo :3306,
usuario `admin`; la comparten el Swing, `admin-tools-api-v2` y la app de órdenes).

## Por qué no se hizo de una

Ronal quedó en **común V31 / cajas V8** el 2026-06-20 y desde entonces no se le
desplegó jar nuevo del Swing (el Swing es quien corre Flyway al arrancar). Si
sigue ahí, aplicar V53 arrastra **V32→V53 de la común (22 migraciones) + V9 de
cada caja (×6)**. Eso ya no es "solo una vista": incluye conversiones a decimal
(V34–V37), `parent_id` (V38), funciones y rangos CAI de cajas (V42+, requiere
`log_bin_trust_function_creators=1` en el servidor), multi-empresa por caja
(V47), galería (V50) y parrilla (V51/V52). Cambia el tamaño de la ventana y hay
que ensayarlo antes, como se hizo para V16→V31.

## Paso 0 — confirmar el estado real (lectura, sin riesgo)

Lee la clave del contenedor de la API sin imprimirla:

```bash
ssh ronal@10.10.0.1 'P=$(docker inspect admin-tools-api-v2 --format "{{range .Config.Env}}{{println .}}{{end}}" | grep "^MYSQL_PASSWORD=" | cut -d= -f2-);
mysql -h127.0.0.1 -uadmin -p"$P" admin_tools -N -e "SELECT MAX(CAST(version AS UNSIGNED)) comun, COUNT(*) filas, SUM(success=0) fallidas FROM schema_version; SELECT COUNT(*) alerta_actual FROM v_existencia_alert; SELECT nombre_db FROM cajas;" 2>&1 | grep -v "Using a password";
for i in 1 2 3 4 5 6; do mysql -h127.0.0.1 -uadmin -p"$P" admin_tools_caja_$i -N -e "SELECT \"caja_$i\", MAX(CAST(version AS UNSIGNED)) FROM schema_version" 2>/dev/null; done'
```

Anotar el resultado aquí antes de seguir.

## Escenario A — ya está en V52 (alguien arrancó un Swing nuevo)

Solo la vista. Sin parar nada, segundos:

1. Respaldo en caliente de las 7 BDs (obligatorio aunque no toque datos):
   `mysqldump -h127.0.0.1 -uadmin -p --all-databases --single-transaction --routines --triggers --events --set-gtid-purged=OFF | gzip > ~/backups/pre-v53_$(date +%Y%m%d_%H%M%S).sql.gz`
   y verificar (`gunzip -t`, última línea `Dump completed`, cuenta de `CREATE DATABASE`).
2. Runner real por túnel (`ssh -f -N -L 13306:127.0.0.1:3306 ronal@10.10.0.1`):
   `ENS_HOST=127.0.0.1 ENS_PORT=13306 ENS_USER=admin ENS_PASS=… java -cp AdminTools-1.0.jar:classes EnsayoMigrate`
   (`deploy/ensayo-wyc/EnsayoMigrate.java` compilado contra el fat-jar de master:
   `JAVA_HOME=$(/usr/libexec/java_home -v 1.8) ./gradlew jar`). El runner hace
   repair()+migrate() igual que el Swing; en este escenario solo aplica V53.
3. Verificar: `SELECT COUNT(cod) FROM v_existencia_alert` (debe subir respecto al
   paso 0 — ahora incluye "bajo el mínimo", no solo negativos) y `SECURITY_TYPE =
   'INVOKER'` en `information_schema.VIEWS`. La API no se toca (no valida vistas).

## Escenario B — sigue en V31 (lo esperado)

Misma operación que la migración V16→V31 del 2026-06-20, con ensayo previo:

1. **Ensayo aislado en el mismo server**: dump `--no-data --routines --triggers`
   de las 7 BDs → `mysql:8.0` efímero en una red `ensayo-net` con los nombres
   EXACTOS (`admin_tools`, `admin_tools_caja_1..6`) → runner → arrancar la imagen
   de la API que corre hoy en `admin-tools-api-v2` (`docker inspect` → `.Config.Image`)
   contra el efímero con env dummies (`APP_JWT_SECRET` base64 largo,
   `APP_JWT_EXPIRATION_MS`, `APP_TIMEZONE`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`)
   y confirmar `Started` sin `Schema-validation`. Medir el tiempo (en junio: ~3,5
   min de DDL con datos reales; el ensayo --no-data ~2,3 min).
2. Revisar `SELECT @@log_bin, @@log_bin_trust_function_creators` en prod; si
   `log_bin=1` y el flag en 0, las funciones/triggers de V42+ fallan con
   ERROR 1418 → pedir al usuario `SET GLOBAL log_bin_trust_function_creators=1`
   + persistirlo en `my.cnf`.
3. Ventana con el cliente: las 4 terminales del Swing (192.168.88.242/243/246/248)
   sin operar (pueden quedar abiertas, como en junio). Respaldo completo en
   caliente y verificado (en junio: 334 MB, ~1 min).
4. `docker stop admin-tools-api-v2` → runner por túnel (común V32→V53 + cajas V9)
   → `docker start admin-tools-api-v2` → `Started` limpio, swagger 200, app de
   órdenes 200, 0 errores de esquema en el log.
5. Verificar la vista como en A.3 y la huella (facturas por caja, artículos,
   clientes) idéntica a la del respaldo.
6. **Antes de reiniciar cualquier terminal del Swing**, desplegarles el jar de
   master (contiene V17–V53). Un jar viejo arrancando contra una BD más nueva es
   el riesgo documentado en junio.

## Rollback

Escenario A: volver a la definición de V1 de `v_existencia_alert` (está en
`V1__baseline.sql`), no toca datos. Escenario B: el respaldo del paso 3
(`docker stop` API → restore → `docker start`).
