#!/bin/bash
# Vigilancia de Sharon: tráfico de la app de pedidos (proxy host 3) y errores de la API
# desde un instante dado, cruzando días.
#
#   bash vigilar.sh "2026-10-03 23:51"     # inicio en UTC (el log del proxy está en UTC)
#
# Clasifica aparte los 404 esperados de fotos/galería (Sharon no tiene imágenes).
DESDE=${1:?uso: bash vigilar.sh "AAAA-MM-DD HH:MM" (UTC)}
CLAVE=$(date -u -d "$DESDE" +%Y%m%d%H%M%S) || exit 1
ISO=$(date -u -d "$DESDE" +%Y-%m-%dT%H:%M:%SZ)

# Líneas del log con fecha >= DESDE. "[03/Oct/2026:23:51:10 +0000]" → 20261003235110
docker exec nginx-proxy-manager sh -c 'cat /data/logs/proxy-host-3_access.log' 2>/dev/null \
 | awk -v d="$CLAVE" 'BEGIN { split("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec", m, " "); for (i = 1; i <= 12; i++) mes[m[i]] = sprintf("%02d", i) }
   { t = substr($1, 2); split(t, a, /[\/:]/); k = a[3] mes[a[2]] a[1] a[4] a[5] a[6]; if (k >= d) print }' \
 | grep -v curl/ > /tmp/vigilar_$$.log

TOTAL=$(wc -l < /tmp/vigilar_$$.log)
echo "peticiones reales desde $DESDE UTC: $TOTAL"
grep -oE ' - [0-9]{3} [0-9]{3} - [A-Z]+ https [^ ]+ "[^"?]*' /tmp/vigilar_$$.log \
 | awk '{print $2, $5, $8}' \
 | sed -E 's#(products/description|customers/name)/.*#\1/{txt}#; s#/products/[0-9]+/#/products/{id}/#; s#/[0-9]+$#/{n}#' \
 | sort | uniq -c | sort -rn
echo "--- 4xx/5xx que NO son fotos/galería (investigar si aparece algo):"
grep -E ' - [45][0-9]{2} [0-9]{3} - ' /tmp/vigilar_$$.log | grep -vE '/image|/gallery' \
 | grep -oE ' - [0-9]{3} [0-9]{3} - [A-Z]+ https [^ ]+ "[^"?]*' | awk '{print $2, $5, $8}' | sort | uniq -c
echo "--- bundles servidos (versión de la app que van cargando los celulares):"
grep -oE 'main\.[a-z0-9]+\.js' /tmp/vigilar_$$.log | sort | uniq -c
echo "--- errores de la API desde $DESDE UTC:"
docker logs admin-tools-api-v2 --since "$ISO" 2>&1 | grep -E ' ERROR ' | sed -E 's/^.{30}//' | sort | uniq -c | head -10
rm -f /tmp/vigilar_$$.log
