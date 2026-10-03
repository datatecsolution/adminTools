#!/bin/bash
# Aplica V49–V57 a la BD de Sharon (servidor de Ronal) DESDE LA MAC, por túnel SSH,
# con el runner real de Flyway (EnsayoMigrate → SchemaMigrator: repair()+migrate(),
# lo mismo que hace el Swing al arrancar).
#
#   bash docs/deploy-sharon/v57/migrar-v57.sh <jar-de-master> <dir-con-EnsayoMigrate.class>
#
# Requisito: haber corrido `bash v57.sh backup` en el servidor (respaldo verificado).
set -u
JAR=${1:?jar de master}; RUNNER=${2:?carpeta con EnsayoMigrate.class}
SRV=ronal@10.10.0.1; PUERTO=13306
JAVA8=$(/usr/libexec/java_home -v1.8 --arch arm64)/bin/java
LOG=$(dirname "$JAR")/migrar-v57_$(date +%Y%m%d_%H%M%S).log

# 1) El jar debe traer exactamente hasta V57 (gotcha 2026-09-24: un jar de una rama
#    con migraciones sin mergear las aplicaría en producción).
ULT=$(unzip -l "$JAR" | grep -oE "db/migration/common/V[0-9]+" | grep -oE "[0-9]+$" | sort -n | tail -1)
[ "$ULT" = "57" ] || { echo "el jar trae hasta V$ULT, no V57 — abortado"; exit 1; }

# 2) Debe existir un respaldo verificado de hoy.
BK=$(ssh $SRV 'cat ~/deploy-sharon/v57/ULTIMO_BACKUP 2>/dev/null')
[ -n "$BK" ] && ssh $SRV "test -f '$BK.sha256' && sha256sum -c '$BK.sha256' >/dev/null" \
  || { echo "no hay respaldo verificado (correr v57.sh backup) — abortado"; exit 1; }
echo "respaldo: $BK"

# 3) Túnel y clave (la clave solo vive en esta variable, no se imprime).
ssh -f -N -o ExitOnForwardFailure=yes -L $PUERTO:127.0.0.1:3306 $SRV || exit 1
trap 'pkill -f "L $PUERTO:127.0.0.1:3306"' EXIT
PASS=$(ssh $SRV "docker inspect admin-tools-api-v2 --format '{{range .Config.Env}}{{println .}}{{end}}' | grep '^MYSQL_PASSWORD=' | cut -d= -f2-")

# 4) Vigilancia en paralelo: cualquier consulta esperando un metadata lock durante
#    los ALTER (una terminal con una transacción abierta frenaría a las demás).
( for i in $(seq 1 120); do
    ssh $SRV "MYSQL_PWD='$PASS' mysql -h127.0.0.1 -uadmin -N -e \"SELECT NOW(), id, user, SUBSTRING_INDEX(host,':',1), time, LEFT(info,80) FROM information_schema.processlist WHERE state LIKE '%metadata lock%'\"" 2>/dev/null
    sleep 1
  done ) > "$LOG.bloqueos" 2>&1 &
VIG=$!

# 5) Migrar.
echo "migrando… (log: $LOG)"
S=$(date +%s)
ENS_HOST=127.0.0.1 ENS_PORT=$PUERTO ENS_USER=admin ENS_PASS="$PASS" \
  "$JAVA8" -cp "$JAR:$RUNNER" EnsayoMigrate > "$LOG" 2>&1
RC=$?
kill $VIG 2>/dev/null
echo "rc=$RC, $(( $(date +%s)-S )) s"
grep -E "Migrating schema|Successfully applied|is up to date|ERROR|Exception" "$LOG"
[ -s "$LOG.bloqueos" ] && { echo "ATENCIÓN: hubo esperas por bloqueo:"; cat "$LOG.bloqueos"; } || echo "sin esperas por bloqueo"
exit $RC
