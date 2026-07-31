#!/bin/bash
# PASO 1 — Backup de las 7 BDs + VERIFICACIÓN de integridad del archivo.
# Un backup no verificado no es un backup: si el dump quedó truncado, el
# rollback fallaría justo cuando más se necesita.
source "$(dirname "$0")/lib-sharon.sh"
TS=$(date +%Y%m%d_%H%M%S)
OUT="$DEPLOY_DIR/backup/sharon_preventana_$TS.sql"
log "=== BACKUP → $OUT ==="

time mysqldump -uadmin -p"$PW" -h127.0.0.1 --single-transaction --routines --triggers --events \
  --set-gtid-purged=OFF --databases admin_tools admin_tools_caja_1 admin_tools_caja_2 \
  admin_tools_caja_3 admin_tools_caja_4 admin_tools_caja_5 admin_tools_caja_6 > "$OUT" 2>"$OUT.err"
RC=$?
[ $RC -eq 0 ] || { fail "mysqldump salió con código $RC — ver $OUT.err"; exit 1; }

# --- verificación del archivo ---
MB=$(du -m "$OUT" | cut -f1)
[ "$MB" -ge 250 ] && ok "tamaño: ${MB}MB" || { fail "tamaño sospechoso: ${MB}MB (esperado ~350MB)"; exit 1; }
tail -3 "$OUT" | grep -q "Dump completed" && ok "marca 'Dump completed' presente" || { fail "dump TRUNCADO (sin marca final)"; exit 1; }
for db in admin_tools admin_tools_caja_1 admin_tools_caja_2 admin_tools_caja_3 admin_tools_caja_4 admin_tools_caja_5 admin_tools_caja_6; do
  grep -q "CREATE DATABASE.*\`$db\`" "$OUT" && ok "incluye $db" || { fail "NO incluye $db"; exit 1; }
done
RUT=$(grep -c "CREATE.*FUNCTION\|CREATE.*PROCEDURE" "$OUT")
[ "$RUT" -ge 30 ] && ok "rutinas incluidas: $RUT" || fail "AVISO: solo $RUT rutinas (esperado ~40)"
echo "$OUT" > "$DEPLOY_DIR/ULTIMO_BACKUP"
log "BACKUP VERIFICADO · ruta guardada en ~/deploy-sharon/ULTIMO_BACKUP"
log "RECORDATORIO rollback: antes del restore → SET GLOBAL log_bin_trust_function_creators=1 (hallazgo E1)"
