#!/bin/bash
# Vigilancia post-migración Sharon: tráfico de la app de pedidos y errores de la API desde DESDE (UTC, HH:MM del 03/Oct)
DESDE=${1:-20:40}
docker exec nginx-proxy-manager cat /data/logs/proxy-host-3_access.log \
 | grep "03/Oct/2026:" | grep -v curl/ \
 | awk -v d="$DESDE" '{ split($1,a,":"); hm=a[2]":"a[3]; if (hm >= d) print }' \
 | grep -oE ' - [0-9]{3} [0-9]{3} - [A-Z]+ https [^ ]+ "[^"?]*' \
 | awk '{print $2, $5, $8}' | sed -E 's#(products/description|customers/name)/.*#\1/{txt}#; s#/[0-9]+$#/{n}#' \
 | sort | uniq -c | sort -rn
echo "--- errores de la API desde la migración:"
docker logs admin-tools-api-v2 --since "2026-10-03T${DESDE}:00Z" 2>&1 | grep -E " ERROR " | sed -E 's/^.{30}//' | sort | uniq -c | head -10
