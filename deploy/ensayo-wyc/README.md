# Ensayo de migración — miscelanías Wyc

Automatiza la **Fase 2 (ensayo en copia efímera)** del
[runbook de migración de cliente](../../docs/runbook-migracion-cliente.md) para
Wyc. Prueba que las migraciones Flyway aplican limpio sobre el esquema real de
Wyc **sin tocar producción**. Ver el análisis completo en
[`docs/migracion-wyc-analisis.md`](../../docs/migracion-wyc-analisis.md).

## Qué hace `ensayo.sh`
1. Descubre las BDs (`admin_tools` + `admin_tools_caja_N`) y **vuelca el esquema**
   (read-only) + los datos de la tabla `cajas`.
2. Levanta un **MySQL 8.0 efímero** (utf8mb3) en docker.
3. Restaura los dumps con los **nombres exactos**.
4. Aplica las migraciones con el **runner real** (`EnsayoMigrate` → `SchemaMigrator`:
   `baselineOnMigrate`, `table=schema_version`, `outOfOrder`, `repair()`+`migrate()`).
5. Verifica `schema_version`: **común=V31, cajas=V8, 0 fallidas** + backfill V18.
6. (Opcional) Levanta la API prod con `ddl-auto=validate`.

## Requisitos
- `docker` y `java` en el PATH.
- El fat-jar `build/libs/AdminTools-1.0.jar` (si falta: `./gradlew jar`). Trae
  `SchemaMigrator` + Flyway 6.5.7 + driver MySQL + las migraciones `.sql` **y las
  Java (V3/V4/V5)** — por eso NO se usa Flyway CLI.
- `~/.wyc.env` con `wyc_host` / `wyc_user` / `wyc_password` (la contraseña viaja
  solo por env `MYSQL_PWD`, nunca a disco ni a la línea de comandos).
- El host de Wyc accesible desde docker (misma LAN / VPN).

## Uso
```bash
bash deploy/ensayo-wyc/ensayo.sh                 # ensayo + limpieza automática
KEEP=1 bash deploy/ensayo-wyc/ensayo.sh          # deja el MySQL efímero vivo para inspeccionar
WITH_API=1 API_IMAGE=<imagen-api> bash deploy/ensayo-wyc/ensayo.sh   # además valida con la API
```
Salida: `✔ ENSAYO OK` (las migraciones aplican limpio) o `✘ ENSAYO CON FALLAS`
(con el detalle de qué BD/qué versión falló — resolver **antes** de tocar prod).

## Alcance y limitaciones
- **Esquema-only por defecto** (rápido, fiel a "aplica limpio + API valida"). **No**
  mide el tiempo del lock de la conversión decimal V7 sobre los millones de filas de
  `caja_1`/`caja_3`, ni el backfill de V18 (que necesita datos de `articulo_kardex`).
- Para **medir la ventana real**, repetir el ensayo incluyendo datos de una caja
  pesada: cambiar el dump de esa caja a `--routines --triggers` (sin `--no-data`) y
  cronometrar el paso 4. (Cuidado: `caja_1` ≈ 1.16M filas → dump y restore grandes.)
- Borrá las credenciales al terminar: `rm ~/.wyc.env`.
