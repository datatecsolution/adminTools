#!/bin/bash
# Actualización de Sharon (servidor de Ronal) de común V48 → V57 (2026-10).
# Se corre EN EL SERVIDOR como `ronal`, desde ~/deploy-sharon/v57/.
#
#   bash v57.sh preflight   # solo lectura: versiones, conexiones activas, disco
#   bash v57.sh backup      # respaldo de las 8 BDs + verificación + huella
#   bash v57.sh verificar   # después de migrar: V57, 0 fallidas, huella, API, dominio
#   bash v57.sh rollback    # SOLO si la BD quedó dañada: restaura el respaldo
#
# La migración en sí NO se corre aquí: va desde la Mac por túnel SSH con el
# runner real de Flyway (ver migrar-v57.sh y el runbook).
set -u
export LC_ALL=C   # sort y join con el mismo orden
DIR="$HOME/deploy-sharon/v57"
BK_DIR="$HOME/deploy-sharon/backup"
LOG="$DIR/v57_$(date +%Y%m%d).log"
BDS="admin_tools admin_tools_caja_1 admin_tools_caja_2 admin_tools_caja_3 admin_tools_caja_4 admin_tools_caja_5 admin_tools_caja_6 admin_tools_caja_7"
API=admin-tools-api-v2
DOMINIO=https://pedidos.distribuidorasharon.com/
mkdir -p "$DIR" "$BK_DIR"

# La clave sale del contenedor de la API y viaja solo por MYSQL_PWD (nunca se imprime).
export MYSQL_PWD=$(docker inspect $API --format '{{range .Config.Env}}{{println .}}{{end}}' | grep '^MYSQL_PASSWORD=' | cut -d= -f2-)
[ -n "$MYSQL_PWD" ] || { echo "no pude leer la clave del contenedor $API"; exit 1; }

q()   { mysql -h127.0.0.1 -uadmin -N -B "$@"; }
log() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$LOG"; }
ok()  { echo "   OK    $*" | tee -a "$LOG"; }
mal() { echo "   FALLA $*" | tee -a "$LOG"; }

# Conteo exacto de filas por tabla base (la "huella" que se compara antes/después).
huella() {
  local sql
  sql=$(q -e "SET SESSION group_concat_max_len=10000000; SELECT GROUP_CONCAT(CONCAT('SELECT ''',table_schema,'.',table_name,''', COUNT(*) FROM \`',table_schema,'\`.\`',table_name,'\`') SEPARATOR ' UNION ALL ') FROM information_schema.tables WHERE table_schema LIKE 'admin_tools%' AND table_type='BASE TABLE'")
  q -e "$sql" | sort
}

versiones() {
  q -e "SELECT 'comun', MAX(CAST(version AS UNSIGNED)), SUM(success=0) FROM admin_tools.schema_version"
  for i in 1 2 3 4 5 6 7; do
    q -e "SELECT 'caja_$i', MAX(CAST(version AS UNSIGNED)), SUM(success=0) FROM admin_tools_caja_$i.schema_version"
  done
}

case "${1:-}" in
preflight)
  log "=== PREFLIGHT (solo lectura) ==="
  versiones | tee -a "$LOG"
  q -e "SELECT @@log_bin, @@log_bin_trust_function_creators" | tee -a "$LOG"
  log "conexiones con transacción abierta (>5 s) — deben ser 0 antes de migrar:"
  q -e "SELECT trx_mysql_thread_id, TIMESTAMPDIFF(SECOND, trx_started, NOW()) seg, trx_query FROM information_schema.innodb_trx WHERE TIMESTAMPDIFF(SECOND, trx_started, NOW()) > 5" | tee -a "$LOG"
  log "conexiones por host:"
  q -e "SELECT SUBSTRING_INDEX(host,':',1) h, user, COUNT(*) FROM information_schema.processlist WHERE db LIKE 'admin_tools%' GROUP BY 1,2" | tee -a "$LOG"
  df -h ~ | tail -1 | tee -a "$LOG"
  docker ps --format '{{.Names}} {{.Image}} {{.Status}}' | grep -E "$API|ordenes" | tee -a "$LOG"
  ;;

backup)
  TS=$(date +%Y%m%d_%H%M%S)
  OUT="$BK_DIR/sharon_pre_v57_$TS.sql.gz"
  log "=== BACKUP → $OUT ==="
  mysqldump -h127.0.0.1 -uadmin --single-transaction --routines --triggers --events \
    --set-gtid-purged=OFF --databases $BDS 2>"$OUT.err" | gzip > "$OUT"
  [ "${PIPESTATUS[0]}" -eq 0 ] || { mal "mysqldump falló — ver $OUT.err"; exit 1; }
  # Un respaldo no verificado no es respaldo.
  gunzip -t "$OUT" && ok "gzip íntegro" || { mal "gzip dañado"; exit 1; }
  zcat "$OUT" | tail -1 | grep -q "Dump completed" && ok "marca 'Dump completed'" || { mal "dump truncado"; exit 1; }
  for db in $BDS; do
    zcat "$OUT" | grep -q "^CREATE DATABASE.*\`$db\`" && ok "incluye $db" || { mal "NO incluye $db"; exit 1; }
  done
  MB=$(du -m "$OUT" | cut -f1); [ "$MB" -ge 60 ] && ok "tamaño ${MB} MB" || { mal "tamaño sospechoso ${MB} MB (ensayo: 79 MB)"; exit 1; }
  sha256sum "$OUT" > "$OUT.sha256" && ok "sha256 guardado"
  echo "$OUT" > "$DIR/ULTIMO_BACKUP"
  huella > "$DIR/huella_antes_$TS.txt"; ln -sf "huella_antes_$TS.txt" "$DIR/huella_antes.txt"
  ok "huella: $(wc -l < "$DIR/huella_antes.txt") tablas, $(awk '{s+=$2} END {print s}' "$DIR/huella_antes.txt") filas"
  versiones > "$DIR/versiones_antes.txt"
  log "BACKUP VERIFICADO: $OUT"
  ;;

verificar)
  log "=== VERIFICACIÓN POST-MIGRACIÓN ==="
  versiones | tee -a "$LOG"
  V=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools.schema_version")
  F=$(q -e "SELECT COUNT(*) FROM (SELECT success FROM admin_tools.schema_version WHERE success=0) x")
  [ "$V" = "57" ] && ok "común en V57" || mal "común en V$V (esperado 57)"
  [ "$F" = "0" ] && ok "0 migraciones fallidas" || mal "$F migraciones fallidas"
  huella > "$DIR/huella_despues.txt"
  CAMBIOS=$(join -t $'\t' "$DIR/huella_antes.txt" "$DIR/huella_despues.txt" | awk -F'\t' '$2!=$3 && $1!="admin_tools.schema_version"')
  [ -z "$CAMBIOS" ] && ok "ninguna tabla existente cambió de conteo" || { mal "tablas con conteo distinto:"; echo "$CAMBIOS" | tee -a "$LOG"; }
  NUEVAS=$(comm -13 <(cut -f1 "$DIR/huella_antes.txt") <(cut -f1 "$DIR/huella_despues.txt") | tr '\n' ' ')
  log "tablas nuevas: $NUEVAS"
  q -e "SELECT COUNT(*), (SELECT SECURITY_TYPE FROM information_schema.VIEWS WHERE table_schema='admin_tools' AND table_name='v_existencia_alert') FROM admin_tools.v_existencia_alert" | sed 's/^/   alerta de inventario: /' | tee -a "$LOG"
  log "API ($API), errores en los últimos 15 min:"
  docker logs $API --since 15m 2>&1 | grep -E " ERROR |Exception" | grep -v "JWT\|Unauthorized\|AccessDenied" | tail -10 | tee -a "$LOG"
  curl -s -o /dev/null -w "   dominio pedidos → %{http_code}\n" "$DOMINIO" | tee -a "$LOG"
  ;;

rollback)
  BK=$(cat "$DIR/ULTIMO_BACKUP" 2>/dev/null)
  [ -f "$BK" ] || { mal "no hay respaldo registrado en $DIR/ULTIMO_BACKUP"; exit 1; }
  sha256sum -c "$BK.sha256" || { mal "el respaldo no coincide con su sha256"; exit 1; }
  log "=== ROLLBACK DE BD desde $BK ==="
  echo "Esto REEMPLAZA las 8 BDs por el respaldo: se pierde todo lo registrado después."
  echo "Antes: terminales sin operar. La API se detiene durante el restore."
  read -r -p "Escribí ROLLBACK para confirmar: " C; [ "$C" = "ROLLBACK" ] || { echo "cancelado"; exit 1; }
  docker stop $API >/dev/null && ok "API detenida"
  q -e "SET GLOBAL log_bin_trust_function_creators=1" && ok "log_bin_trust_function_creators=1"
  zcat "$BK" | mysql -h127.0.0.1 -uadmin 2>"$DIR/rollback.err"; RC=$?
  [ $RC -eq 0 ] && ok "restore terminado" || { mal "restore con errores (ver $DIR/rollback.err)"; head -5 "$DIR/rollback.err"; }
  versiones | tee -a "$LOG"
  docker start $API >/dev/null && sleep 20
  docker logs $API --since 1m 2>&1 | grep -E "Started|APPLICATION FAILED" | tail -2 | tee -a "$LOG"
  curl -s -o /dev/null -w "   dominio pedidos → %{http_code}\n" "$DOMINIO" | tee -a "$LOG"
  log "ROLLBACK COMPLETO — reinstalar en las terminales el jar anterior"
  ;;

*)
  echo "uso: bash v57.sh preflight|backup|verificar|rollback"; exit 1 ;;
esac
