#!/usr/bin/env bash
#
# Ensayo de migración Flyway para miscelanías Wyc (Fase 2 del runbook).
# Prueba TODO en docker aislado, SIN tocar producción:
#   1. Dump de ESQUEMA del cliente (read-only) + datos de la tabla `cajas`.
#   2. MySQL 8.0 efímero (utf8mb3) en docker.
#   3. Restaura los dumps con los nombres EXACTOS (admin_tools + cajas).
#   4. Aplica las migraciones con el runner real (SchemaMigrator) → V31 / V8.
#   5. Verifica schema_version (común=31, cajas=8, 0 fallidas) + backfill V18.
#   6. (Opcional) Levanta la API prod con ddl-auto=validate.
#
# Requisitos: docker, java, el fat-jar build/libs/AdminTools-1.0.jar
# (./gradlew jar si falta), y ~/.wyc.env con wyc_host/wyc_user/wyc_password.
#
# La contraseña de PROD viaja solo por env (MYSQL_PWD), nunca a un archivo ni a
# la línea de comandos. La del MySQL efímero es descartable.
#
# Uso:   bash deploy/ensayo-wyc/ensayo.sh            # ensayo + limpieza
#        KEEP=1 bash deploy/ensayo-wyc/ensayo.sh     # deja el contenedor para inspeccionar
#        WITH_API=1 ... API_IMAGE=<img> ...          # además valida con la API
set -euo pipefail

# ---------- config ----------
CLIENT="${CLIENT:-wyc}"          # CLIENT=venecia bash ensayo.sh → usa ~/.venecia.env y vars venecia_*
NET="ensayo-${CLIENT}-net"
MYC="ensayo-${CLIENT}-mysql"
API_C="ensayo-${CLIENT}-api"
ENS_PORT=${ENS_PORT:-13306}
ENS_PASS=throwaway
MYSQL_IMG=mysql:8.0
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
JAR="$REPO/build/libs/AdminTools-1.0.jar"
WORK="$(mktemp -d)"; DUMPS="$WORK/dumps"; mkdir -p "$DUMPS"

log(){ printf '\n\033[1;36m== %s\033[0m\n' "$*"; }
die(){ printf '\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

cleanup(){
  if [ "${KEEP:-0}" = "1" ]; then
    echo "KEEP=1 → contenedor $MYC vivo en 127.0.0.1:$ENS_PORT (user root / $ENS_PASS). Limpiá con: docker rm -f $MYC ${WITH_API:+$API_C}; docker network rm $NET"
  else
    docker rm -f "$MYC" ${WITH_API:+$API_C} >/dev/null 2>&1 || true
    docker network rm "$NET" >/dev/null 2>&1 || true
  fi
  rm -rf "$WORK"
}
trap cleanup EXIT

# ---------- prereqs ----------
command -v docker >/dev/null || die "falta docker"
command -v java   >/dev/null || die "falta java"
[ -f "$JAR" ] || die "no existe $JAR — corré: (cd $REPO && ./gradlew jar)"
ENVFILE="$HOME/.${CLIENT}.env"
[ -f "$ENVFILE" ] || die "no existe $ENVFILE (vars ${CLIENT}_host/${CLIENT}_user/${CLIENT}_password)"
set -a; source "$ENVFILE"; set +a
# Lookup indirecto de las vars <cliente>_host/_user/_password
_h="${CLIENT}_host"; _u="${CLIENT}_user"; _p="${CLIENT}_password"
DBHOST="${!_h:-}"; DBUSER="${!_u:-}"; DBPASS="${!_p:-}"
[ -n "$DBHOST" ] && [ -n "$DBUSER" ] && [ -n "$DBPASS" ] || die "credenciales vacías en $ENVFILE"
export MYSQL_PWD="$DBPASS"   # mysqldump/mysql lo leen del entorno (no a argv)
echo "   cliente=$CLIENT  destino=$DBUSER@$DBHOST"

dump(){ # dump <args...>  → mysqldump dentro de un contenedor mysql, MYSQL_PWD por -e (sin valor en argv)
  docker run --rm -e MYSQL_PWD "$MYSQL_IMG" \
    mysqldump -h"$DBHOST" -u"$DBUSER" --no-tablespaces --set-gtid-purged=OFF --column-statistics=0 "$@"
}
sanitize(){ sed -E 's/DEFINER=`[^`]*`@`[^`]*`//g'; }   # quita DEFINER (el user del cliente no existe en la copia)

# ---------- 1. descubrir BDs + dump de esquema ----------
log "1/6  Descubriendo BDs y volcando esquema (read-only)"
mapfile -t DBS < <(docker run --rm -e MYSQL_PWD "$MYSQL_IMG" \
  mysql -h"$DBHOST" -u"$DBUSER" -N -e "SHOW DATABASES" | grep -E '^admin_tools(_caja_[0-9]+)?$' | sort)
[ "${#DBS[@]}" -ge 1 ] || die "no se encontraron BDs admin_tools*"
echo "   BDs: ${DBS[*]}"
for db in "${DBS[@]}"; do
  echo "   · dump esquema $db"
  dump --no-data --routines --triggers --skip-add-drop-table --databases "$db" | sanitize > "$DUMPS/$db.sql"
done
echo "   · dump datos de admin_tools.cajas (para descubrir las cajas)"
dump --no-create-info --skip-triggers admin_tools cajas > "$DUMPS/_cajas_data.sql"
# US-150: si el cliente YA corre Flyway (Ronal), el historial debe viajar con el
# esquema — con schema_version vacía, Flyway re-aplica desde V1 y choca con las
# tablas existentes. En clientes sin Flyway (Wyc) la tabla no existe y esto
# queda vacío sin romper nada.
for db in "${DBS[@]}"; do
  if dump --no-create-info --skip-triggers "$db" schema_version > "$DUMPS/_sv_$db.sql" 2>/dev/null; then
    echo "   · dump historial schema_version de $db"
  else
    rm -f "$DUMPS/_sv_$db.sql"; echo "   · $db sin schema_version (cliente pre-Flyway, ok)"
  fi
done

# ---------- 2. MySQL efímero ----------
log "2/6  Levantando MySQL efímero ($MYSQL_IMG, utf8mb3)"
docker network create "$NET" >/dev/null 2>&1 || true
docker rm -f "$MYC" >/dev/null 2>&1 || true
docker run -d --name "$MYC" --network "$NET" -p 127.0.0.1:$ENS_PORT:3306 \
  -e MYSQL_ROOT_PASSWORD="$ENS_PASS" "$MYSQL_IMG" \
  --character-set-server=utf8mb3 --collation-server=utf8mb3_general_ci \
  --log-bin-trust-function-creators=1 >/dev/null
echo -n "   esperando a que MySQL acepte conexiones"
for i in $(seq 1 60); do
  if docker exec -e MYSQL_PWD="$ENS_PASS" "$MYC" mysql -uroot -e "SELECT 1" >/dev/null 2>&1; then echo " ✓"; break; fi
  echo -n "."; sleep 2
  [ "$i" = 60 ] && die "MySQL efímero no levantó"
done
myc(){ docker exec -i -e MYSQL_PWD="$ENS_PASS" "$MYC" mysql -uroot "$@"; }

# ---------- 3. restaurar ----------
log "3/6  Restaurando esquema en la copia efímera"
for db in "${DBS[@]}"; do echo "   · restore $db"; myc < "$DUMPS/$db.sql"; done
echo "   · datos de cajas"; myc admin_tools < "$DUMPS/_cajas_data.sql"
for db in "${DBS[@]}"; do
  [ -f "$DUMPS/_sv_$db.sql" ] && { echo "   · historial schema_version de $db"; myc "$db" < "$DUMPS/_sv_$db.sql"; }
done

# ---------- 4. migrar (runner real) ----------
log "4/6  Aplicando migraciones (SchemaMigrator real)"
mkdir -p "$WORK/classes"
javac -cp "$JAR" "$HERE/EnsayoMigrate.java" -d "$WORK/classes"
ENS_HOST=127.0.0.1 ENS_PORT="$ENS_PORT" ENS_USER=root ENS_PASS="$ENS_PASS" \
  java -cp "$JAR:$WORK/classes" EnsayoMigrate

# ---------- 5. verificar ----------
log "5/6  Verificación post-migración"
PASS=1
check(){ # check <db> <target>
  local db="$1" target="$2"
  local out; out=$(myc -N -e "SELECT IFNULL(MAX(CAST(version AS UNSIGNED)),-1), IFNULL(SUM(success=0),0) FROM \`$db\`.schema_version" 2>/dev/null) || { echo "   ✗ $db: sin schema_version"; PASS=0; return; }
  local maxv fails; maxv=$(echo "$out"|awk '{print $1}'); fails=$(echo "$out"|awk '{print $2}')
  if [ "$maxv" = "$target" ] && [ "$fails" = "0" ]; then echo "   ✓ $db → V$maxv, 0 fallidas"
  else echo "   ✗ $db → V$maxv (esperado V$target), fallidas=$fails"; PASS=0; fi
}
# Targets parametrizables (US-150: CLIENT=ronal usa 48/9; default = los de Wyc)
check admin_tools "${EXPECT_COMMON:-31}"
for db in "${DBS[@]}"; do [ "$db" = admin_tools ] && continue; check "$db" "${EXPECT_CAJA:-8}"; done
# backfill V18: existencia_articulo_bodega debería tener filas (si hay datos de kardex)
EAB=$(myc -N -e "SELECT COUNT(*) FROM admin_tools.existencia_articulo_bodega" 2>/dev/null || echo "n/a")
echo "   · existencia_articulo_bodega (backfill V18): $EAB filas  (nota: esquema-only → puede ser 0; con datos debe poblar)"

# ---------- 6. API opcional ----------
if [ "${WITH_API:-0}" = "1" ]; then
  log "6/6  Validando con la API (ddl-auto=validate)"
  [ -n "${API_IMAGE:-}" ] || die "WITH_API=1 requiere API_IMAGE=<imagen>"
  docker rm -f $API_C >/dev/null 2>&1 || true
  docker run -d --name $API_C --network "$NET" \
    -e SPRING_PROFILES_ACTIVE=pdn -e MYSQL_HOST="$MYC" -e MYSQL_PORT=3306 -e MYSQL_DB=admin_tools \
    -e MYSQL_USER=root -e MYSQL_PASSWORD="$ENS_PASS" -e MYSQL_TZ=GMT-6 \
    -e APP_JWT_SECRET="$(head -c48 /dev/urandom | base64)" -e APP_JWT_EXPIRATION_MS=86400000 \
    -e APP_TIMEZONE=America/Tegucigalpa -e CORS_ALLOWED_ORIGINS='*' -e SERVER_PORT=8080 \
    "$API_IMAGE" >/dev/null
  echo -n "   esperando arranque de la API"
  for i in $(seq 1 40); do
    if docker logs $API_C 2>&1 | grep -q "Started AdmintoolsApplication"; then echo " ✓ valida OK"; break; fi
    if docker logs $API_C 2>&1 | grep -qiE "Schema-validation|missing|wrong column"; then echo " ✗"; docker logs $API_C 2>&1 | grep -iE "Schema-validation|missing|wrong column" | head; PASS=0; break; fi
    echo -n "."; sleep 3; [ "$i" = 40 ] && { echo " (timeout — revisá: docker logs $API_C)"; PASS=0; }
  done
else
  log "6/6  API: omitida (pasá WITH_API=1 API_IMAGE=<img> para validarla)"
fi

# ---------- veredicto ----------
echo
if [ "$PASS" = 1 ]; then printf '\033[1;32m✔ ENSAYO OK — las migraciones aplican limpio sobre el esquema real de %s.\033[0m\n' "$CLIENT"
else printf '\033[1;31m✘ ENSAYO CON FALLAS — revisá arriba ANTES de tocar producción.\033[0m\n'; exit 1; fi
