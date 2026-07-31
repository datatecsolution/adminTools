#!/bin/bash
# ROLLBACK — restaura el backup de la ventana y revive la API vieja.
# Pide confirmación escrita: destruye el estado actual de las 7 BDs.
source "$(dirname "$0")/lib-sharon.sh"
BK=$(cat "$DEPLOY_DIR/ULTIMO_BACKUP" 2>/dev/null)
[ -f "$BK" ] || { fail "no hay backup registrado en ULTIMO_BACKUP"; exit 1; }
log "=== ROLLBACK desde $BK ==="
read -p "Escribí ROLLBACK para confirmar: " C; [ "$C" = "ROLLBACK" ] || { echo "cancelado"; exit 1; }

docker rm -f admin-tools-api-v2 >/dev/null 2>&1
# E1 (hallazgo del ensayo): sin esto el restore ABORTA al recrear funciones
q -e "SET GLOBAL log_bin_trust_function_creators=1;" && ok "log_bin_trust_function_creators=1"
time mysql -uadmin -p"$PW" -h127.0.0.1 < "$BK" 2>"$DEPLOY_DIR/backup/rollback.err"
grep -v "^mysql: \[Warning\]" "$DEPLOY_DIR/backup/rollback.err" | head -5
V=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools.schema_version;")
ok "schema restaurado a V$V (esperado 32)"
cd "$HOME/admintools-api" && docker compose up -d && sleep 20
docker logs admin-tools-api-v2 --since 2m 2>&1 | grep -E "Started|ERROR" | tail -2
curl -s -o /dev/null -w "dominio pedidos → %{http_code}\n" https://pedidos.distribuidorasharon.com/
log "ROLLBACK COMPLETO — NO distribuir el jar nuevo a las terminales"
