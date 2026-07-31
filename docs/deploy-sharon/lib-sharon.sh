#!/bin/bash
# Librería común de la ventana Sharon. La password NUNCA se imprime.
DEPLOY_DIR="$HOME/deploy-sharon"
LOG="$DEPLOY_DIR/ventana_$(date +%Y%m%d).log"
CAJAS="1 2 3 4 5 6"

PW=$(docker exec admin-tools-api-v2 env 2>/dev/null | grep "^MYSQL_PASSWORD=" | cut -d= -f2-)
[ -z "$PW" ] && PW=$(grep "^MYSQL_PASSWORD=" "$DEPLOY_DIR/.env-v2-actual" 2>/dev/null | cut -d= -f2-)
[ -z "$PW" ] && { echo "FATAL: no se pudo obtener la password de MySQL"; exit 1; }

q()  { mysql -uadmin -p"$PW" -h127.0.0.1 -N -B "$@" 2>/dev/null; }     # query cruda
qq() { mysql -uadmin -p"$PW" -h127.0.0.1 "$@" 2>/dev/null; }           # query con formato
log(){ echo "[$(date +%H:%M:%S)] $*" | tee -a "$LOG"; }
ok()  { echo "   OK   · $*" | tee -a "$LOG"; }
fail(){ echo "  FALLA · $*" | tee -a "$LOG"; }
