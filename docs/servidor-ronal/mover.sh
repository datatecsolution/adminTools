#!/bin/bash
# Mueve la IP fija de la API de un stack y hace que el proxy la re-resuelva
# con un RELOAD gracioso de nginx (sin corte del proxy; el restart de hoy fue
# porque nginx estaba caido). Uso: mover.sh <stack> <api-container> <ip-nueva>
#   mover.sh pruebas-dulce api-pruebas-dulce 172.25.0.204
# Rollback = el mismo script con la IP anterior.
set -eu
S=$1; C=$2; IP=$3; SVC=${4:-$2}   # servicio del compose (dulce: api-pruebas != api-pruebas-dulce)
F=/home/ronal/$S/stack/docker-compose.yml
( cd /home/ronal/$S/stack && docker compose config --services | grep -qx "$SVC" ) || { echo "   !! el servicio $SVC no existe en $F"; exit 4; }
ACT=$(grep -m1 "ipv4_address:" "$F" | tr -d " " | cut -d: -f2)
echo "== $C: $ACT -> $IP"
sed -i "s/ipv4_address: *$ACT/ipv4_address: $IP/" "$F"
grep -n "ipv4_address" "$F" | tr -d " " | sed "s/^/   compose: /"
T0=$(date +%s.%N)
( cd /home/ronal/$S/stack && docker compose up -d "$SVC" 2>&1 | grep -E "Started|Recreat|Error" | sed "s/^/   /" )
INI=${T0%.*}
for i in $(seq 1 90); do docker logs "$C" --since "$INI" 2>&1 | grep -q "Started AdmintoolsApplication" && break; sleep 1; done
NOW=$(docker inspect "$C" -f "{{(index .NetworkSettings.Networks \"n8n-docker_default\").IPAddress}}")
printf "   %s arriba en %.1f s con IP %s\n" "$C" "$(echo "$(date +%s.%N) - $T0" | bc)" "$NOW"
[ "$NOW" = "$IP" ] || { echo "   !! la IP no es la esperada"; exit 2; }
# el proxy tiene cacheada la IP vieja en la custom location: recarga graciosa
docker exec nginx-proxy-manager nginx -t >/dev/null 2>&1 || { echo "   !! nginx -t FALLA, no recargo"; exit 3; }
docker exec nginx-proxy-manager nginx -s reload && echo "   proxy: reload OK (sin corte)"
