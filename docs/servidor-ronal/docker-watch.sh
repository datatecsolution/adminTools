#!/bin/bash
# =====================================================================
# docker-watch.sh — vigilante de contenedores y dominios del server Ronal.
#
# Corre cada 2 minutos desde el crontab de `ronal` (docker en su grupo,
# sin sudo) y avisa por correo por el MISMO canal que los avisos de
# fail2ban: `mail -s ... root`, que /etc/aliases reenvía a jdmayorga82@yahoo.com
# vía postfix → smtp.gmail.com.
#
# Qué vigila:
#   1. Contenedores con politica de reinicio (unless-stopped/always) que NO
#      esten "running" (exited, dead, restarting, created).
#   2. Los dominios publicados: cada URL debe responder con uno de sus
#      codigos esperados (200 para un front, 401 para un endpoint de API
#      sin token). Un 5xx o sin respuesta = caido AUNQUE el contenedor
#      este arriba (el 502 por IP cacheada del proxy, 2026-09-15).
#
# Para no inundar: una URL/contenedor tiene que fallar en 2 chequeos
# seguidos (~4 min) antes de avisar; cuando entra en falla manda UN correo,
# lo repite cada 60 min mientras siga caido, y manda otro al recuperarse.
# Estado en ~/.docker-watch/.
#
# Uso:  docker-watch.sh            (chequeo normal, lo llama cron)
#       docker-watch.sh --test     (manda un correo de prueba)
#       docker-watch.sh --boot     (aviso de "el servidor se reinicio", cron @reboot)
#       docker-watch.sh --status   (imprime el estado sin mandar nada)
# =====================================================================
set -u
STATE=$HOME/.docker-watch
mkdir -p "$STATE"
DEST=root
HOST=$(hostname)
REALERT_MIN=60
FALLOS_PARA_AVISAR=2

# URL|codigos-ok (separados por coma)|descripcion
URLS=(
  "https://admintools.supermercadosurbina.com/|200|Samuel POS (PRODUCCION)"
  "https://admintools.supermercadosurbina.com/admin_tools/api/cierre-caja|401|Samuel API (PRODUCCION)"
  "https://pedidos.supermercadosurbina.com/|200|Samuel app de pedidos (PRODUCCION)"
  "https://pos.datatecsolution.com/|200|Sharon POS (PRODUCCION)"
  "https://pedidos.distribuidorasharon.com/|200|Sharon app de pedidos (PRODUCCION)"
  "https://pedidos.distribuidorasharon.com/admin_tools/api/cierre-caja|401|Sharon API (PRODUCCION)"
  "https://posdulce.datatecsolution.com/|200|dulce POS (pruebas)"
  "https://posdulce.datatecsolution.com/admin_tools/api/cierre-caja|401|dulce API (pruebas)"
  "https://mariposasdoradas.datatecsolution.com/|200|Mariposas POS (demo)"
  "https://mariposasdoradas.datatecsolution.com/admin_tools/api/cierre-caja|401|Mariposas API (demo)"
  "https://pedidosmariposas.datatecsolution.com/|200|Mariposas app de pedidos (demo)"
  "https://lafe.datatecsolution.com/|200|La Fe POS (demo)"
  "https://lafe.datatecsolution.com/admin_tools/api/cierre-caja|401|La Fe API (demo)"
)

enviar() { # asunto, cuerpo
  printf '%s\n\n-- %s · %s\n' "$2" "$HOST" "$(date '+%F %T')" | mail -s "$1" "$DEST"
}

clave() { printf '%s' "$1" | tr -c 'A-Za-z0-9' '_'; }

# ---- registra un fallo/ok por clave y decide si toca avisar ----
# marca <clave> <fallo:1|0> <descripcion> <detalle>
marca() {
  local k=$1 fallo=$2 desc=$3 det=$4 f="$STATE/$k" ahora
  ahora=$(date +%s)
  if [ "$fallo" = 1 ]; then
    local n=0 avisado=0
    [ -f "$f" ] && read -r n avisado < "$f"
    n=$((n + 1))
    if [ "$n" -ge "$FALLOS_PARA_AVISAR" ] && { [ "$avisado" = 0 ] || [ $((ahora - avisado)) -ge $((REALERT_MIN * 60)) ]; }; then
      enviar "[$HOST] CAIDO: $desc" "$desc esta caido.

$det

Chequeos fallidos seguidos: $n (cada 2 min).
Si sigue asi, este aviso se repite cada $REALERT_MIN min."
      avisado=$ahora
    fi
    echo "$n $avisado" > "$f"
  else
    if [ -f "$f" ]; then
      read -r n avisado < "$f"
      [ "$avisado" != 0 ] && enviar "[$HOST] RECUPERADO: $desc" "$desc volvio a responder.

$det"
      rm -f "$f"
    fi
  fi
}

case "${1:-}" in
  --test)
    enviar "[$HOST] prueba del vigilante de contenedores" "Si lees esto, los avisos de contenedores caidos llegan por este correo (mismo canal que fail2ban)."
    echo "correo de prueba enviado a $DEST"; exit 0;;
  --boot)
    sleep 120
    enviar "[$HOST] el servidor se REINICIO" "Arranque: $(uptime -s)
Contenedores arriba: $(docker ps -q | wc -l) de $(docker ps -aq | wc -l)

$(docker ps -a --format '{{.Names}}\t{{.Status}}' | grep -v ' Up ' || echo 'todos arriba')"
    exec "$0";;
  --status)
    echo "estado en $STATE:"; ls -1 "$STATE" 2>/dev/null || true;;
esac

# ---- 1. contenedores ----
while read -r nombre estado politica; do
  [ "$politica" = no ] && continue
  k="c_$(clave "$nombre")"
  if [ "$estado" != running ]; then
    marca "$k" 1 "contenedor $nombre" "Estado: $estado
$(docker inspect -f 'Salida: {{.State.ExitCode}} {{.State.Error}} · terminado: {{.State.FinishedAt}}' "$nombre" 2>/dev/null)

Ultimas lineas del log:
$(docker logs --tail 8 "$nombre" 2>&1 | cut -c1-200)"
  else
    marca "$k" 0 "contenedor $nombre" "Estado: running"
  fi
done < <(docker ps -a --format '{{.Names}} {{.State}}' | while read -r n s; do echo "$n $s $(docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' "$n" 2>/dev/null)"; done)

# contenedores que ya no existen (se borraron/recrearon con otro nombre): se
# cierra su estado como recuperado para no dejar claves huerfanas
for f in "$STATE"/c_*; do
  [ -e "$f" ] || continue
  k=$(basename "$f")
  existe=0
  while read -r n; do [ "c_$(clave "$n")" = "$k" ] && existe=1; done < <(docker ps -a --format '{{.Names}}')
  [ "$existe" = 1 ] || marca "$k" 0 "contenedor ${k#c_}" "el contenedor ya no existe"
done

# ---- 2. dominios ----
for item in "${URLS[@]}"; do
  IFS='|' read -r url oks desc <<< "$item"
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$url" 2>/dev/null || echo 000)
  k="u_$(clave "$url")"
  if echo ",$oks," | grep -q ",$code,"; then
    marca "$k" 0 "$desc" "$url → HTTP $code"
  else
    marca "$k" 1 "$desc" "$url → HTTP $code (esperado $oks)"
  fi
done
